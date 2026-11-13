package org.rsmod.api.ge

import jakarta.inject.Inject
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign
import org.rsmod.api.config.refs.invs
import org.rsmod.api.config.refs.objs
import org.rsmod.api.db.DatabaseConnection
import org.rsmod.api.invtx.delete
import org.rsmod.api.invtx.invAdd
import org.rsmod.api.invtx.invTransaction
import org.rsmod.api.invtx.select
import org.rsmod.api.player.output.UpdateInventory
import org.rsmod.api.player.output.UpdateStockMarket
import org.rsmod.api.player.output.mes
import org.rsmod.game.entity.Player
import org.rsmod.game.entity.PlayerList
import org.rsmod.game.inv.InvObj
import org.rsmod.game.inv.Inventory
import org.rsmod.game.type.inv.InvTypeList
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.obj.UnpackedObjType

/**
 * The [GrandExchange]: all offers of all characters live in memory (the order book), loaded at boot
 * with [load].
 *
 * ## Matching
 * A new offer is matched against the book right away, on the game thread. The trade goes to the
 * best other-owner offer on the opposite side (lowest sell / highest buy price, oldest first at the
 * same price) and happens at the price of the offer that was there first. Partial fills are normal.
 * A player's own offers never match each other, and offers keep matching while their owner is
 * offline.
 *
 * A buy may ask for more than its buy limit: it fills up to the limit and waits. Only such buys can
 * sit in the book across a matching sell, and [retryStalledBuys] picks them up after the reset.
 *
 * ## Persistence: how items and coins can't be duped or lost
 * Coins and items change hands in exactly two places, [place] (inventory to offer) and [collect]
 * (collect box to inventory). Each of them saves the actor's whole account and every Grand Exchange
 * row it touched in ONE database transaction (see [GePersistence.commit]), so a stop between the
 * two can't leave the coins in both places or in neither. Offers of other players that the trade
 * filled are in that same transaction, so a trade is all-or-nothing too. Nothing else moves value
 * between a player and the exchange ([abort] only moves it between an offer and its collect box).
 *
 * Rows are written per character and always whole (that character's offers, history and buy-limit
 * windows are deleted and re-inserted), so a write is a complete state and never a delta. Every
 * account save of a player, autosave and logout included, also writes that player's rows, captured
 * on the game thread with the account snapshot ([saveWork], hooked into `AccountManager`); a failed
 * Grand Exchange save is therefore repaired by the next save of that account. All writes go through
 * the single account writer queue in the order they were requested, and none is retried (an account
 * save that carries these rows isn't either, even at logout), so an older state is never written
 * over a newer one.
 *
 * What is left: if the writer fails for good (disk full) and the server then stops before another
 * save of that account, the database is behind the game by those trades; the inventory and the
 * offers of the actor are always behind together, but a counterparty's row from the same trade may
 * be ahead or behind. That needs a failing database on top of a stop and is only logged.
 *
 * ## Prices
 * Guide prices start at the store value and move with every trade: the guide price takes one eighth
 * of the distance to the traded price (at least 1 coin toward it), never below 1.
 *
 * ## Not implemented
 * Buy limits shared between linked items (e.g. potion doses): only the per-obj limit applies.
 * New-account trade restrictions and the item sink are skipped on purpose.
 */
public class GrandExchangeImpl
@Inject
constructor(
    private val objTypes: ObjTypeList,
    private val invTypes: InvTypeList,
    private val players: PlayerList,
    private val persistence: GePersistence,
    private val clock: GeClock,
) : GrandExchange {
    private val chars = HashMap<Int, CharState>()
    // Note: a plain list per side scanned for the best offer, O(offers of that obj); a sorted
    //  structure if one item ever has thousands of open offers.
    private val books = HashMap<Int, Book>()
    private val prices = HashMap<Int, Int>()
    private var nextOfferId = 1L

    private class Offer(
        val id: Long,
        val characterId: Int,
        val slot: Int,
        val type: GeOfferType,
        val obj: Int,
        val price: Int,
        val count: Int,
    ) {
        var completedCount = 0
        var completedGold = 0L
        var taxPaid = 0L
        var status = GeOfferStatus.Active
        var collectItems = 0
        var collectCoins = 0L

        val remaining: Int
            get() = count - completedCount
    }

    private class Book {
        val buys = ArrayList<Offer>()
        val sells = ArrayList<Offer>()
    }

    private class Window(var start: Long, var bought: Int)

    private class CharState {
        val slots = arrayOfNulls<Offer>(SLOTS)
        /** Newest first. */
        val history = ArrayList<GeHistoryEntry>()
        val windows = HashMap<Int, Window>()
    }

    /** Replaces the whole state with what the database holds. Call before the game starts. */
    public fun load(connection: DatabaseConnection) {
        val loaded = GeSql.load(connection)
        chars.clear()
        books.clear()
        prices.clear()
        prices += loaded.prices
        for (rows in loaded.characters) {
            val state = CharState()
            for (row in rows.offers) {
                val offer =
                    Offer(
                        row.offerId,
                        row.characterId,
                        row.slot,
                        row.type,
                        row.obj,
                        row.price,
                        row.count,
                    )
                offer.completedCount = row.completedCount
                offer.completedGold = row.completedGold
                offer.taxPaid = row.taxPaid
                offer.status = row.status
                offer.collectItems = row.collectItems
                offer.collectCoins = row.collectCoins
                state.slots[row.slot] = offer
                if (offer.status == GeOfferStatus.Active) {
                    list(offer).add(offer)
                }
                nextOfferId = max(nextOfferId, row.offerId + 1)
            }
            state.history += rows.history
            for (window in rows.windows) {
                state.windows[window.obj] = Window(window.start, window.bought)
            }
            chars[rows.characterId] = state
        }
    }

    /**
     * What an account save of [characterId] writes along with the account: the rows of that
     * character, captured now. Null if the character never used the exchange.
     */
    public fun saveWork(characterId: Int): ((DatabaseConnection) -> Unit)? {
        val rows = rows(characterId) ?: return null
        return { connection -> GeSql.writeCharacter(connection, rows) }
    }

    override fun offers(player: Player): List<GeOffer?> {
        val state = chars[player.characterId]
        return List(SLOTS) { state?.slots?.get(it)?.view() }
    }

    override fun place(
        player: Player,
        slot: Int,
        type: GeOfferType,
        obj: ObjType,
        count: Int,
        price: Int,
    ): GePlaceResult {
        if (slot !in 0 until SLOTS) {
            return reject("That is not a Grand Exchange slot.")
        }
        if (slot >= FREE_SLOTS && !player.members) {
            return reject("You need to be a member to use that slot.")
        }
        val characterId = player.characterId
        if (chars[characterId]?.slots?.get(slot) != null) {
            return reject("That Grand Exchange slot is already in use.")
        }
        val base = objTypes.uncert(objTypes[obj])
        if (!base.tradeableOnExchange()) {
            return reject("You can't trade that item on the Grand Exchange.")
        }
        if (count < 1 || price < 1 || count.toLong() * price > Int.MAX_VALUE) {
            return reject("That offer is too large.")
        }
        if (type == GeOfferType.Buy) {
            if (!player.members && base.members) {
                return reject("You need to be a member to buy that item.")
            }
            if (!takeCoins(player, count.toLong() * price)) {
                return reject("You don't have enough coins.")
            }
        } else if (!takeItems(player, base, count)) {
            return reject("You don't have enough of that item.")
        }

        val offer = Offer(nextOfferId++, characterId, slot, type, base.id, price, count)
        state(characterId).slots[slot] = offer
        list(offer).add(offer)
        settle(offer, base, player)
        return GePlaceResult.Placed
    }

    /** Lets buys held back by their limit trade again, once the limit has reset. */
    public fun retryStalledBuys() {
        for (book in books.values) {
            for (buy in book.buys.sortedWith(compareBy<Offer>({ -it.price }, { it.id }))) {
                if (buy.status == GeOfferStatus.Active) {
                    settle(buy, objTypes.getValue(buy.obj), actor = null)
                }
            }
        }
    }

    /** Matches [offer], then tells the owners and saves what changed. */
    private fun settle(offer: Offer, base: UnpackedObjType, actor: Player?) {
        val fills = LinkedHashMap<Offer, Int>()
        val touched = linkedSetOf(offer.characterId)
        val priceChanges = HashSet<Int>()
        match(offer, base, fills, touched, priceChanges)
        if (actor == null && fills.isEmpty()) {
            return
        }
        for (filled in fills.keys) {
            refresh(filled)
            message(filled, base)
        }
        refresh(offer)
        commit(actor, touched, priceChanges)
    }

    override fun abort(player: Player, slot: Int): Boolean {
        val offer = chars[player.characterId]?.slots?.getOrNull(slot) ?: return false
        if (offer.status != GeOfferStatus.Active) {
            return false
        }
        if (offer.type == GeOfferType.Buy) {
            offer.collectCoins += offer.remaining.toLong() * offer.price
        } else {
            offer.collectItems += offer.remaining
        }
        finish(offer, GeOfferStatus.Aborted)
        refresh(offer)
        val base = objTypes.getValue(offer.obj)
        player.mes(
            "Grand Exchange: Cancelled ${offer.type.verb()} ${offer.count} x ${base.name}." // UNVERIFIED text
        )
        commit(null, setOf(player.characterId), emptySet())
        return true
    }

    override fun collect(player: Player, slot: Int, box: Int, mode: GeCollectMode): Boolean {
        val offer = chars[player.characterId]?.slots?.getOrNull(slot) ?: return false
        if (!moveBox(player, offer, box, mode)) {
            return false
        }
        retireIfEmpty(offer)
        refresh(offer)
        commit(player, setOf(player.characterId), emptySet())
        return true
    }

    override fun collectAll(player: Player, toBank: Boolean) {
        val state = chars[player.characterId] ?: return
        val mode = if (toBank) GeCollectMode.Bank else GeCollectMode.Inventory
        var any = false
        for (offer in state.slots.filterNotNull()) {
            val moved = moveBox(player, offer, 0, mode) or moveBox(player, offer, 1, mode)
            if (moved) {
                any = true
                retireIfEmpty(offer)
                refresh(offer)
            }
        }
        if (any) {
            commit(player, setOf(player.characterId), emptySet())
        }
    }

    override fun history(player: Player): List<GeHistoryEntry> =
        chars[player.characterId]?.history?.toList().orEmpty()

    override fun guidePrice(obj: ObjType): Int {
        val base = objTypes.uncert(objTypes[obj])
        return prices[base.id] ?: max(1, base.cost)
    }

    override fun buyLimitRemaining(player: Player, obj: ObjType): Int? =
        limitRoom(player.characterId, objTypes.uncert(objTypes[obj]))

    override fun sync(player: Player) {
        for (slot in 0 until SLOTS) {
            sendSlot(player, slot)
        }
        // The client's "item sink" offers read these varps as objs: a raw 0 would be a phantom
        // offer of obj 0, so every one must say -1 (no item).
        for (slot in 0 until SLOTS) {
            GeSync.setVarp(player, GeVarps.itemsink_obj[slot], -1)
            GeSync.setVarp(player, GeVarps.itemsink_price[slot], 0)
        }
        GeSync.setVarBit(player, GeVarBits.taxrate, GeTax.RATE_TENTHS_PERCENT)
    }

    /* Matching */

    private fun match(
        incoming: Offer,
        base: UnpackedObjType,
        fills: MutableMap<Offer, Int>,
        touched: MutableSet<Int>,
        priceChanges: MutableSet<Int>,
    ) {
        while (incoming.status == GeOfferStatus.Active) {
            val other = bestCounter(incoming, base) ?: return
            val buy = if (incoming.type == GeOfferType.Buy) incoming else other
            val sell = if (incoming.type == GeOfferType.Buy) other else incoming
            var qty = min(incoming.remaining, other.remaining)
            val room = limitRoom(buy.characterId, base)
            if (room != null) {
                qty = min(qty, room)
            }
            // The offer that was in the book first sets the price.
            execute(
                buy,
                sell,
                qty,
                if (other.id < incoming.id) other.price else incoming.price,
                base,
            )
            fills.merge(buy, qty, Int::plus)
            fills.merge(sell, qty, Int::plus)
            touched += buy.characterId
            touched += sell.characterId
            priceChanges += base.id
        }
    }

    private fun bestCounter(incoming: Offer, base: UnpackedObjType): Offer? {
        val book = books[incoming.obj] ?: return null
        if (incoming.type == GeOfferType.Buy) {
            if (limitRoom(incoming.characterId, base) == 0) {
                return null
            }
            return book.sells
                .filter { it.characterId != incoming.characterId && it.price <= incoming.price }
                .minWithOrNull(compareBy<Offer>({ it.price }, { it.id }))
        }
        return book.buys
            .filter {
                it.characterId != incoming.characterId &&
                    it.price >= incoming.price &&
                    limitRoom(it.characterId, base) != 0
            }
            .minWithOrNull(compareBy<Offer>({ -it.price }, { it.id }))
    }

    private fun execute(buy: Offer, sell: Offer, qty: Int, price: Int, base: UnpackedObjType) {
        val gross = qty.toLong() * price
        val tax = qty * GeTax.perItem(base.id, price)

        buy.completedCount += qty
        buy.completedGold += gross
        buy.collectItems += qty
        buy.collectCoins += qty.toLong() * (buy.price - price)

        sell.completedCount += qty
        sell.completedGold += gross
        sell.taxPaid += tax
        sell.collectCoins += gross - tax

        if (GeBuyLimits[base.id] > 0) {
            addBought(buy.characterId, base, qty)
        }
        updateGuidePrice(base.id, price)
        if (buy.remaining == 0) {
            finish(buy, GeOfferStatus.Completed)
        }
        if (sell.remaining == 0) {
            finish(sell, GeOfferStatus.Completed)
        }
    }

    private fun updateGuidePrice(obj: Int, tradedPrice: Int) {
        val current = prices[obj] ?: max(1, objTypes.getValue(obj).cost)
        val diff = tradedPrice - current
        // A trade moves the price an eighth of the way, but always at least 1 toward it.
        val step = if (diff / 8 != 0) diff / 8 else diff.sign
        prices[obj] = max(1, current + step)
    }

    /** Ends an offer: out of the book, into the history if it traded anything. */
    private fun finish(offer: Offer, status: GeOfferStatus) {
        offer.status = status
        list(offer).remove(offer)
        if (offer.completedCount > 0) {
            val history = state(offer.characterId).history
            history.add(
                0,
                GeHistoryEntry(
                    offer.obj,
                    offer.type,
                    offer.completedCount,
                    offer.completedGold,
                    offer.taxPaid,
                ),
            )
            while (history.size > HISTORY_SIZE) {
                history.removeAt(history.lastIndex)
            }
        }
        retireIfEmpty(offer)
    }

    private fun list(offer: Offer): MutableList<Offer> {
        val book = books.getOrPut(offer.obj) { Book() }
        return if (offer.type == GeOfferType.Buy) book.buys else book.sells
    }

    /** A finished offer whose boxes are empty is gone: the slot is free again. */
    private fun retireIfEmpty(offer: Offer) {
        if (
            offer.status != GeOfferStatus.Active &&
                offer.collectItems == 0 &&
                offer.collectCoins == 0L
        ) {
            val state = chars[offer.characterId]
            if (state?.slots?.get(offer.slot) === offer) {
                state.slots[offer.slot] = null
            }
        }
    }

    /* Buy limits */

    /** Items [characterId] may still buy in the current window, null if [base] has no limit. */
    private fun limitRoom(characterId: Int, base: UnpackedObjType): Int? {
        val limit = GeBuyLimits[base.id]
        if (limit <= 0) {
            return null
        }
        val window = chars[characterId]?.windows?.get(base.id)
        if (window == null || clock.millis() - window.start >= LIMIT_WINDOW_MILLIS) {
            return limit
        }
        return max(0, limit - window.bought)
    }

    private fun addBought(characterId: Int, base: UnpackedObjType, qty: Int) {
        val now = clock.millis()
        val windows = state(characterId).windows
        val window = windows[base.id]
        if (window == null || now - window.start >= LIMIT_WINDOW_MILLIS) {
            windows[base.id] = Window(now, qty)
        } else {
            window.bought += qty
        }
    }

    /* Taking coins and items */

    private fun takeCoins(player: Player, total: Long): Boolean {
        val coins = objs.coins.id
        val inv = player.inv
        val bank = bank(player)
        val inInv = count(inv, coins)
        val inBank = count(bank, coins)
        if (inInv + inBank < total) {
            return false
        }
        val fromInv = min(inInv, total).toInt()
        val fromBank = (total - fromInv).toInt()
        val result =
            player.invTransaction(inv, bank) {
                if (fromInv > 0) {
                    delete(select(inv), coins, fromInv)
                }
                if (fromBank > 0) {
                    delete(select(bank), coins, fromBank)
                }
            }
        return result.success
    }

    private fun takeItems(player: Player, base: UnpackedObjType, count: Int): Boolean {
        val inv = player.inv
        val noted = if (base.canCert) objTypes.cert(base).id else -1
        val haveUnnoted = count(inv, base.id)
        val haveNoted = if (noted != -1) count(inv, noted) else 0
        if (haveUnnoted + haveNoted < count) {
            return false
        }
        val fromUnnoted = min(haveUnnoted, count.toLong()).toInt()
        val fromNoted = count - fromUnnoted
        val result =
            player.invTransaction(inv) {
                val target = select(inv)
                if (fromUnnoted > 0) {
                    delete(target, base.id, fromUnnoted)
                }
                if (fromNoted > 0) {
                    delete(target, noted, fromNoted)
                }
            }
        return result.success
    }

    private fun count(inv: Inventory, obj: Int): Long =
        inv.objs.sumOf { if (it != null && it.id == obj) it.count.toLong() else 0L }

    private fun bank(player: Player): Inventory = player.invMap.getOrPut(invTypes[invs.bank])

    /* Collecting */

    /** Moves what fits of one collect box of [offer]; false if nothing moved. */
    private fun moveBox(player: Player, offer: Offer, box: Int, mode: GeCollectMode): Boolean {
        val inv = if (mode == GeCollectMode.Bank) bank(player) else player.inv
        if (box == 0) {
            val count = offer.collectItems
            if (count <= 0) {
                return false
            }
            val base = objTypes.getValue(offer.obj)
            val noted =
                when (mode) {
                    GeCollectMode.Inventory -> count > 1 && base.canCert
                    GeCollectMode.Noted -> base.canCert
                    GeCollectMode.Unnoted,
                    GeCollectMode.Bank -> false
                }
            val moved = add(player, inv, base.id, count, noted)
            offer.collectItems -= moved
            return moved > 0
        }
        if (box == 1) {
            val amount = min(offer.collectCoins, Int.MAX_VALUE.toLong()).toInt()
            if (amount <= 0) {
                return false
            }
            val moved = add(player, inv, objs.coins.id, amount, false)
            offer.collectCoins -= moved
            return moved > 0
        }
        return false
    }

    private fun add(player: Player, inv: Inventory, obj: Int, count: Int, noted: Boolean): Int {
        val result = player.invAdd(inv, obj, count, strict = false, cert = noted)
        return if (result.success) result.completed() else 0
    }

    /* Persistence */

    private fun commit(actor: Player?, characters: Set<Int>, priceChanges: Set<Int>) {
        val rows = characters.mapNotNull { rows(it) }
        val changed = priceChanges.associateWith { prices.getValue(it) }
        persistence.commit(actor) { connection ->
            rows.forEach { GeSql.writeCharacter(connection, it) }
            GeSql.writePrices(connection, changed)
        }
    }

    private fun rows(characterId: Int): CharacterRows? {
        val state = chars[characterId] ?: return null
        val now = clock.millis()
        return CharacterRows(
            characterId = characterId,
            offers = state.slots.filterNotNull().map { it.row() },
            history = state.history.toList(),
            windows =
                state.windows
                    .filterValues { now - it.start < LIMIT_WINDOW_MILLIS }
                    .map { (obj, window) -> WindowRow(obj, window.start, window.bought) },
        )
    }

    private fun Offer.row(): OfferRow =
        OfferRow(
            characterId,
            slot,
            id,
            type,
            obj,
            price,
            count,
            completedCount,
            completedGold,
            taxPaid,
            status,
            collectItems,
            collectCoins,
        )

    private fun Offer.view(): GeOffer =
        GeOffer(
            slot,
            type,
            obj,
            price,
            count,
            completedCount,
            completedGold,
            taxPaid,
            status,
            collectItems,
            collectCoins,
        )

    /* Telling players */

    private fun state(characterId: Int): CharState = chars.getOrPut(characterId) { CharState() }

    private fun online(characterId: Int): Player? =
        players.firstOrNull { it.characterId == characterId }

    private fun refresh(offer: Offer) {
        val player = online(offer.characterId) ?: return
        sendSlot(player, offer.slot)
    }

    private fun sendSlot(player: Player, slot: Int) {
        val offer = chars[player.characterId]?.slots?.get(slot)
        // Built fresh for each send and never kept on the player: the engine holds the contents,
        // and a kept inv would also be saved with the account.
        val box = Inventory.create(invTypes[GeInvs.collect[slot]])
        if (offer == null) {
            UpdateStockMarket.resetSlot(player, slot)
            GeSync.setVarp(player, GeVarps.tax_slot[slot], 0)
        } else {
            val base = objTypes.getValue(offer.obj)
            if (offer.collectItems > 0) {
                box[0] = InvObj(base, offer.collectItems)
            }
            if (offer.collectCoins > 0) {
                box[1] = InvObj(objs.coins, min(offer.collectCoins, Int.MAX_VALUE.toLong()).toInt())
            }
            UpdateStockMarket.setSlot(
                player,
                slot,
                offer.statusByte(),
                offer.obj,
                offer.price,
                offer.count,
                offer.completedCount,
                min(offer.completedGold, Int.MAX_VALUE.toLong()).toInt(),
            )
            val tax = if (offer.type == GeOfferType.Sell) offer.taxPaid else 0L
            GeSync.setVarp(player, GeVarps.tax_slot[slot], min(tax, Int.MAX_VALUE.toLong()).toInt())
        }
        UpdateInventory.updateInvFull(player, box)
    }

    /**
     * The client's flags: 8 sell, 4 finished, 2 live, 1 local. Finished is 5; the client shows a
     * cancel from `completedCount < count`.
     */
    private fun Offer.statusByte(): Int =
        (if (type == GeOfferType.Sell) 8 else 0) + (if (status == GeOfferStatus.Active) 2 else 5)

    // UNVERIFIED: the wording of these messages.
    private fun message(offer: Offer, base: UnpackedObjType) {
        val player = online(offer.characterId) ?: return
        val text =
            if (offer.status == GeOfferStatus.Completed) {
                "Grand Exchange: Finished ${offer.type.verb()} ${offer.count} x ${base.name}."
            } else {
                val past = if (offer.type == GeOfferType.Buy) "Bought" else "Sold"
                "Grand Exchange: $past ${offer.completedCount} / ${offer.count} x ${base.name}."
            }
        player.mes(text)
    }

    private fun GeOfferType.verb(): String = if (this == GeOfferType.Buy) "buying" else "selling"

    private fun reject(message: String) = GePlaceResult.Rejected(message)

    private fun UnpackedObjType.tradeableOnExchange(): Boolean =
        stockmarket && tradeable && !isCert && !isPlaceholder

    public companion object {
        public const val SLOTS: Int = 8
        public const val FREE_SLOTS: Int = 3
        public const val HISTORY_SIZE: Int = 40
        public const val LIMIT_WINDOW_MILLIS: Long = 4L * 60 * 60 * 1000
    }
}
