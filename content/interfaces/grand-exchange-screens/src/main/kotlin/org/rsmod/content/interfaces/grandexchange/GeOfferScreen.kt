package org.rsmod.content.interfaces.grandexchange

import jakarta.inject.Inject
import kotlin.math.max
import kotlin.math.min
import org.rsmod.api.ge.GeOfferType
import org.rsmod.api.ge.GePlaceResult
import org.rsmod.api.ge.GeSync
import org.rsmod.api.ge.GrandExchange
import org.rsmod.api.ge.GrandExchangeImpl
import org.rsmod.api.ge.clickCollectBox
import org.rsmod.api.player.output.objExamine
import org.rsmod.api.player.output.runClientScript
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onIfModalButton
import org.rsmod.api.utils.format.formatAmount
import org.rsmod.game.type.comp.ComponentType
import org.rsmod.game.type.interf.IfButtonOp
import org.rsmod.game.type.interf.IfEvent
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.obj.UnpackedObjType
import org.rsmod.game.type.varbit.VarBitType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The Grand Exchange offer screen (465, over the inventory side panel 467) and the trade history
 * (383). The offer itself lives in the engine ([GrandExchange]); this is only the screens.
 *
 * The client keeps its own copy of the new offer's varbits and changes them itself on most buttons,
 * without telling the server the result. The server's varps are the real copy: every button repeats
 * the client's sums ([GeSetupMath]) and sends the varbits back, so a client that drifted is put
 * right.
 */
class GeOfferScreen
@Inject
constructor(private val exchange: GrandExchange, private val objTypes: ObjTypeList) :
    PluginScript() {
    override fun ScriptContext.startup() {
        onIfModalButton(ge_screen_components.history) { openHistory() }
        onIfModalButton(ge_screen_components.back) { showIndex() }
        onIfModalButton(ge_screen_components.collectall) { topBar(it.comsub, it.op) }
        for ((slot, component) in ge_screen_components.index.withIndex()) {
            onIfModalButton(component) { clickSlot(slot, it.comsub, it.op) }
        }
        onIfModalButton(ge_screen_components.details_status) {
            if (it.comsub == ABORT_SUB) {
                exchange.abort(player, selectedSlot())
            }
        }
        onIfModalButton(ge_screen_components.details_collect) { collect(it.comsub, it.op) }
        onIfModalButton(ge_screen_components.setup) { setupButton(it.comsub, it.op) }
        onIfModalButton(ge_screen_components.setup_confirm) { confirm() }
        onIfModalButton(ge_screen_components.side_items) { offerFromInv(it.comsub, it.op) }
        onIfModalButton(ge_screen_components.history_exchange) { openExchange() }
        onIfModalButton(ge_screen_components.history_list) { historyRow(it.comsub, it.op) }
    }

    /** Opens the offer screen on its index panel. Used by the clerks and the exchange booths. */
    fun ProtectedAccess.openExchange() {
        showIndex()
        exchange.sync(player)
        invTransmit(inv)
        ifOpenMainSidePair(
            main = ge_screen_interfaces.offers,
            side = ge_screen_interfaces.offers_side,
        )
        ifSetEvents(ge_screen_components.collectall, 0..1, IfEvent.Op1, IfEvent.Op2)
        // "Modify offer" (op3) is left off: the rev 233 client draws it, but what it did isn't
        // known, and the menu option hides itself when the server doesn't enable it.
        for (component in ge_screen_components.index) {
            ifSetEvents(component, OFFER_SUB..SELL_SUB, IfEvent.Op1, IfEvent.Op2)
        }
        ifSetEvents(ge_screen_components.details_status, ABORT_SUB..ABORT_SUB, IfEvent.Op1)
        ifSetEvents(
            ge_screen_components.details_collect,
            ITEM_BOX_SUB..COIN_BOX_SUB,
            IfEvent.Op1,
            IfEvent.Op2,
            IfEvent.Op3,
            IfEvent.Op10,
        )
        ifSetEvents(ge_screen_components.setup, 0..CUSTOM_UP, IfEvent.Op1, IfEvent.Op2)
        ifSetEvents(ge_screen_components.side_items, inv.indices, IfEvent.Op1, IfEvent.Op10)
    }

    /** Opens the trade history, newest first. */
    fun ProtectedAccess.openHistory() {
        if (player.ui.containsModal(ge_screen_interfaces.offers_side)) {
            ifCloseSub(ge_screen_interfaces.offers_side)
        }
        ifOpenMainModal(ge_screen_interfaces.history)
        val entries = exchange.history(player)
        player.runClientScript(HISTORY_INIT)
        for ((index, entry) in entries.withIndex()) {
            player.runClientScript(
                HISTORY_ADD_LINE,
                index,
                entry.obj,
                if (entry.type == GeOfferType.Sell) 1 else 0,
                entry.count,
                entry.gold.clamped(),
                entry.tax.clamped(),
            )
        }
        player.runClientScript(HISTORY_FINISH)
        if (entries.isNotEmpty()) {
            ifSetEvents(
                ge_screen_components.history_list,
                0 until entries.size * HISTORY_ROW_SUBS,
                IfEvent.Op1,
                IfEvent.Op10,
            )
        }
    }

    /* Index panel */

    private suspend fun ProtectedAccess.clickSlot(slot: Int, sub: Int, op: IfButtonOp) {
        when (sub) {
            OFFER_SUB ->
                when (op) {
                    IfButtonOp.Op1 -> viewOffer(slot)
                    IfButtonOp.Op2 -> exchange.abort(player, slot)
                    else -> Unit
                }
            BUY_SUB -> if (op == IfButtonOp.Op1) startOffer(slot, GeOfferType.Buy)
            SELL_SUB -> if (op == IfButtonOp.Op1) startOffer(slot, GeOfferType.Sell)
        }
    }

    private fun ProtectedAccess.viewOffer(slot: Int) {
        val offer = exchange.offers(player)[slot] ?: return showIndex()
        setBit(ge_screen_varbits.selectedslot, slot + 1)
        val type = objTypes.getValue(offer.obj)
        describe(type, ge_screen_components.details_desc, ge_screen_components.details_fee)
        ifSetText(ge_screen_components.details_marketprice, priceLine(type))
    }

    private suspend fun ProtectedAccess.startOffer(slot: Int, type: GeOfferType) {
        if (!usable(slot) || exchange.offers(player)[slot] != null) {
            return showIndex()
        }
        setBit(ge_screen_varbits.selectedslot, slot + 1)
        setBit(ge_screen_varbits.newoffer_type, type.code())
        clearItem()
        if (type == GeOfferType.Buy) {
            searchItem()
        }
    }

    private fun ProtectedAccess.topBar(sub: Int, op: IfButtonOp) {
        when (sub) {
            COLLECT_SUB -> exchange.collectAll(player, toBank = op == IfButtonOp.Op2)
            REPEAT_SUB -> repeatOffer()
        }
    }

    private fun ProtectedAccess.repeatOffer() {
        val item = vars[ge_screen_varps.last_offer_item]
        if (item < 0) {
            return
        }
        val slot = freeSlot() ?: return
        setBit(ge_screen_varbits.selectedslot, slot + 1)
        setBit(ge_screen_varbits.newoffer_type, vars[ge_screen_varps.last_offer_type])
        chooseItem(objTypes.getValue(item), max(1, vars[ge_screen_varps.last_offer_quantity]))
        setBit(ge_screen_varbits.newoffer_price, max(1, vars[ge_screen_varps.last_offer_price]))
    }

    /** Back to the index panel with nothing set up. */
    private fun ProtectedAccess.showIndex() {
        setBit(ge_screen_varbits.selectedslot, 0)
        clearItem()
    }

    /* Status panel */

    private fun ProtectedAccess.collect(sub: Int, op: IfButtonOp) {
        val slot = selectedSlot()
        if (slot < 0) {
            return
        }
        exchange.clickCollectBox(player, objTypes, slot, sub - ITEM_BOX_SUB, op)
        if (exchange.offers(player)[slot] == null) {
            showIndex()
        }
    }

    /* Setup panel */

    private suspend fun ProtectedAccess.setupButton(sub: Int, op: IfButtonOp) {
        if (sub == CHOOSE_ITEM) {
            if (newOfferType() == GeOfferType.Buy) {
                searchItem()
            } else {
                // UNVERIFIED: the wording.
                mes("Choose an item from your inventory to sell.")
            }
            return
        }
        if (vars[ge_screen_varps.tradingpost_search] < 0) {
            return
        }
        when (sub) {
            QTY_MINUS_1 -> changeQuantity(-1)
            QTY_PLUS_1,
            QTY_PLUS_1_BIG -> changeQuantity(1)
            QTY_PLUS_10 -> changeQuantity(10)
            QTY_PLUS_100 -> changeQuantity(100)
            QTY_1K_OR_ALL -> changeQuantity(Int.MAX_VALUE)
            QTY_ENTER -> if (op == IfButtonOp.Op2) buyAllAffordable() else enterQuantity()
            PRICE_MINUS_1 -> setPrice(GeSetupMath.step(price(), -1))
            PRICE_PLUS_1 -> setPrice(GeSetupMath.step(price(), 1))
            PRICE_MINUS_5 -> setPrice(GeSetupMath.percent(price(), -5))
            PRICE_PLUS_5 -> setPrice(GeSetupMath.percent(price(), 5))
            PRICE_GUIDE -> setPrice(exchange.guidePrice(objTypes.getValue(searchedObj())))
            PRICE_ENTER -> enterPrice()
            CUSTOM_DOWN,
            CUSTOM_UP -> {
                if (op == IfButtonOp.Op2) {
                    customisePercent()
                } else {
                    val custom = vars[ge_screen_varbits.price_custom]
                    if (custom > 0) {
                        val sign = if (sub == CUSTOM_UP) 1 else -1
                        setPrice(GeSetupMath.percent(price(), sign * custom))
                    }
                }
            }
        }
    }

    private suspend fun ProtectedAccess.searchItem() {
        val type =
            objDialog(
                "What would you like to buy?", // UNVERIFIED: the title.
                stockMarketRestriction = true,
                showLastSearched = true,
            )
        if (selectedSlot() < 0 || newOfferType() != GeOfferType.Buy) {
            return
        }
        if (!type.onExchange()) {
            mes("You can't trade that item on the Grand Exchange.")
            return
        }
        GeSync.setVarp(player, ge_screen_varps.last_searched, type.id)
        chooseItem(type, 1)
    }

    private fun ProtectedAccess.changeQuantity(step: Int) {
        val sell = newOfferType() == GeOfferType.Sell
        val quantity =
            GeSetupMath.quantity(
                current = vars[ge_screen_varbits.newoffer_quantity],
                step = step,
                sell = sell,
                available = if (sell) available() else 0,
            )
        setBit(ge_screen_varbits.newoffer_quantity, quantity)
    }

    /** "All" on a buy's "..." button: as many as the inventory's coins pay for (cs2 5156). */
    private fun ProtectedAccess.buyAllAffordable() {
        val price = price()
        if (newOfferType() != GeOfferType.Buy || price <= 0) {
            return
        }
        setBit(ge_screen_varbits.newoffer_quantity, max(1, invCoinTotal() / price))
    }

    private suspend fun ProtectedAccess.enterQuantity() {
        val title =
            if (newOfferType() == GeOfferType.Sell) {
                "How many do you wish to sell?"
            } else {
                "How many do you wish to buy?"
            }
        val count = countDialog(title) // UNVERIFIED: the titles.
        if (count > 0 && vars[ge_screen_varps.tradingpost_search] >= 0) {
            setBit(ge_screen_varbits.newoffer_quantity, count)
        }
    }

    private suspend fun ProtectedAccess.enterPrice() {
        val price = countDialog("Set a price for each item:") // UNVERIFIED: the title.
        if (price > 0 && vars[ge_screen_varps.tradingpost_search] >= 0) {
            setPrice(price)
        }
    }

    private suspend fun ProtectedAccess.customisePercent() {
        // UNVERIFIED: the title, and the cap (the varbit holds up to 127).
        val percent = countDialog("Set a percentage to change the price by:")
        setBit(ge_screen_varbits.price_custom, min(percent, MAX_CUSTOM_PERCENT))
    }

    private fun ProtectedAccess.confirm() {
        val slot = selectedSlot()
        val item = searchedObj()
        if (slot < 0 || item < 0) {
            return
        }
        val type = newOfferType()
        val quantity = vars[ge_screen_varbits.newoffer_quantity]
        val price = price()
        val result = exchange.place(player, slot, type, objTypes.getValue(item), quantity, price)
        if (result is GePlaceResult.Rejected) {
            mes(result.message)
            return
        }
        GeSync.setVarp(player, ge_screen_varps.last_offer_item, item)
        GeSync.setVarp(player, ge_screen_varps.last_offer_quantity, quantity)
        GeSync.setVarp(player, ge_screen_varps.last_offer_price, price)
        GeSync.setVarp(player, ge_screen_varps.last_offer_type, type.code())
        showIndex()
    }

    /* Side panel: "Offer" on an inventory item starts a sell offer of it. */

    private fun ProtectedAccess.offerFromInv(invSlot: Int, op: IfButtonOp) {
        val obj = inv[invSlot] ?: return
        val type = objTypes[obj]
        if (op == IfButtonOp.Op10) {
            player.objExamine(type, obj.count, exchange.guidePrice(type))
            return
        }
        val base = objTypes.uncert(type)
        if (!base.onExchange()) {
            mes("You can't trade that item on the Grand Exchange.") // UNVERIFIED: the wording.
            return
        }
        val selected = selectedSlot()
        val slot =
            if (selected >= 0 && exchange.offers(player)[selected] == null) {
                selected
            } else {
                freeSlot() ?: return
            }
        setBit(ge_screen_varbits.selectedslot, slot + 1)
        setBit(ge_screen_varbits.newoffer_type, GeOfferType.Sell.code())
        // A stack (or notes) offers the whole stack, a single item one.
        chooseItem(base, if (type.isStackable) obj.count else 1)
    }

    /* History */

    private fun ProtectedAccess.historyRow(sub: Int, op: IfButtonOp) {
        val entry = exchange.history(player).getOrNull(sub / HISTORY_ROW_SUBS) ?: return
        val type = objTypes.getValue(entry.obj)
        if (op == IfButtonOp.Op10) {
            player.objExamine(type, entry.count, exchange.guidePrice(type))
            return
        }
        // UNVERIFIED: "Buy-offer" opens a buy offer of the item at its guide price.
        openExchange()
        val slot = freeSlot() ?: return
        setBit(ge_screen_varbits.selectedslot, slot + 1)
        setBit(ge_screen_varbits.newoffer_type, GeOfferType.Buy.code())
        chooseItem(type, 1)
    }

    /* State */

    private fun ProtectedAccess.chooseItem(type: UnpackedObjType, quantity: Int) {
        GeSync.setVarp(player, ge_screen_varps.tradingpost_search, type.id)
        setBit(ge_screen_varbits.newoffer_quantity, quantity)
        setPrice(exchange.guidePrice(type))
        describe(type, ge_screen_components.setup_desc, ge_screen_components.setup_fee)
        ifSetText(ge_screen_components.setup_marketprice, priceLine(type))
    }

    private fun ProtectedAccess.clearItem() {
        GeSync.setVarp(player, ge_screen_varps.tradingpost_search, -1)
        setBit(ge_screen_varbits.newoffer_quantity, 0)
        setBit(ge_screen_varbits.newoffer_price, 0)
        player.runClientScript(
            SET_DESC,
            "",
            "",
            ge_screen_components.setup_desc.packed,
            ge_screen_components.setup_fee.packed,
        )
        ifSetText(ge_screen_components.setup_marketprice, "")
    }

    /** The examine text; the fee info icon stays hidden (no info line). */
    private fun ProtectedAccess.describe(
        type: UnpackedObjType,
        desc: ComponentType,
        fee: ComponentType,
    ) {
        player.runClientScript(SET_DESC, type.desc, "", desc.packed, fee.packed)
    }

    // UNVERIFIED: the wording of the guide price line.
    private fun priceLine(type: UnpackedObjType): String =
        "Guide price: <col=ffffff>${exchange.guidePrice(type).formatAmount}</col> coins"

    private fun ProtectedAccess.setPrice(price: Int) {
        setBit(ge_screen_varbits.newoffer_price, price)
    }

    private fun ProtectedAccess.setBit(varbit: VarBitType, value: Int) {
        GeSync.setVarBit(player, varbit, value)
    }

    private fun ProtectedAccess.selectedSlot(): Int = vars[ge_screen_varbits.selectedslot] - 1

    private fun ProtectedAccess.searchedObj(): Int = vars[ge_screen_varps.tradingpost_search]

    private fun ProtectedAccess.price(): Int = vars[ge_screen_varbits.newoffer_price]

    private fun ProtectedAccess.newOfferType(): GeOfferType =
        if (vars[ge_screen_varbits.newoffer_type] == 1) GeOfferType.Sell else GeOfferType.Buy

    /** Of the item being set up, noted and un-noted, at least 1 (cs2 813). */
    private fun ProtectedAccess.available(): Int {
        val base = objTypes.getValue(searchedObj())
        val noted = objTypes.cert(base)
        var total = inv.count(base).toLong()
        if (noted.id != base.id) {
            total += inv.count(noted)
        }
        return total.coerceIn(1, Int.MAX_VALUE.toLong()).toInt()
    }

    private fun ProtectedAccess.usable(slot: Int): Boolean =
        slot in 0 until GrandExchangeImpl.SLOTS &&
            (slot < GrandExchangeImpl.FREE_SLOTS || player.members)

    private fun ProtectedAccess.freeSlot(): Int? {
        val offers = exchange.offers(player)
        val slot =
            (0 until GrandExchangeImpl.SLOTS).firstOrNull { usable(it) && offers[it] == null }
        if (slot == null) {
            mes("You don't have any free Grand Exchange slots.") // UNVERIFIED: the wording.
        }
        return slot
    }

    private fun UnpackedObjType.onExchange(): Boolean =
        stockmarket && tradeable && !isCert && !isPlaceholder

    private fun GeOfferType.code(): Int = if (this == GeOfferType.Sell) 1 else 0

    private fun Long.clamped(): Int = min(this, Int.MAX_VALUE.toLong()).toInt()

    private companion object {
        const val SET_DESC = 5730
        const val HISTORY_INIT = 1644
        const val HISTORY_ADD_LINE = 1645
        const val HISTORY_FINISH = 1646
        const val HISTORY_ROW_SUBS = 6

        const val OFFER_SUB = 2
        const val BUY_SUB = 3
        const val SELL_SUB = 4
        const val COLLECT_SUB = 0
        const val REPEAT_SUB = 1
        const val ABORT_SUB = 0
        const val ITEM_BOX_SUB = 2
        const val COIN_BOX_SUB = 3

        // The setup panel's buttons, by sub.
        const val CHOOSE_ITEM = 0
        const val QTY_MINUS_1 = 1
        const val QTY_PLUS_1 = 2
        const val QTY_PLUS_1_BIG = 3
        const val QTY_PLUS_10 = 4
        const val QTY_PLUS_100 = 5
        const val QTY_1K_OR_ALL = 6
        const val QTY_ENTER = 7
        const val PRICE_MINUS_1 = 8
        const val PRICE_PLUS_1 = 9
        const val PRICE_MINUS_5 = 10
        const val PRICE_GUIDE = 11
        const val PRICE_ENTER = 12
        const val PRICE_PLUS_5 = 13
        const val CUSTOM_DOWN = 14
        const val CUSTOM_UP = 15

        const val MAX_CUSTOM_PERCENT = 100
    }
}
