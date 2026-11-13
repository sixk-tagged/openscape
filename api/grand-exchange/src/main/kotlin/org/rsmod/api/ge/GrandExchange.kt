package org.rsmod.api.ge

import org.rsmod.game.entity.Player
import org.rsmod.game.type.obj.ObjType

public enum class GeOfferType {
    Buy,
    Sell,
}

public enum class GeOfferStatus {
    Active,
    Completed,
    Aborted,
}

/** Which form to collect items in. [Inventory] is the default form of the first "Collect" op. */
public enum class GeCollectMode {
    Inventory,
    Noted,
    Unnoted,
    Bank,
}

public data class GeOffer(
    /** 0..7 */
    val slot: Int,
    val type: GeOfferType,
    /** The un-noted obj id. */
    val obj: Int,
    /** Unit price. */
    val price: Int,
    val count: Int,
    val completedCount: Int,
    /** Gross coins traded so far (before tax). */
    val completedGold: Long,
    val taxPaid: Long,
    val status: GeOfferStatus,
    /** Items waiting in collect box slot 0. */
    val collectItems: Int,
    /** Coins waiting in collect box slot 1. */
    val collectCoins: Long,
)

public data class GeHistoryEntry(
    val obj: Int,
    val type: GeOfferType,
    val count: Int,
    val gold: Long,
    val tax: Long,
)

public sealed class GePlaceResult {
    public data object Placed : GePlaceResult()

    public data class Rejected(val message: String) : GePlaceResult()
}

/**
 * The Grand Exchange. Players only: an offer trades against other players' offers, nothing else,
 * and keeps trading while its owner is offline. See [GrandExchangeImpl] for how state is kept and
 * saved.
 *
 * Everything is called on the game thread.
 */
public interface GrandExchange {
    /** 8 entries, null = empty slot (no offer and nothing to collect). */
    public fun offers(player: Player): List<GeOffer?>

    /**
     * Validates, takes coins (inventory then bank) or items (un-noted then noted), stores the offer
     * and matches it. A rejected offer changes nothing and carries the message to show.
     */
    public fun place(
        player: Player,
        slot: Int,
        type: GeOfferType,
        obj: ObjType,
        count: Int,
        price: Int,
    ): GePlaceResult

    /**
     * Aborts an active offer: what is unsold or unspent goes to the slot's collect box. Returns
     * false if the slot has no active offer.
     */
    public fun abort(player: Player, slot: Int): Boolean

    /** box 0 = items, 1 = coins. Moves what fits; returns false if nothing moved. */
    public fun collect(player: Player, slot: Int, box: Int, mode: GeCollectMode): Boolean

    /** Collects every slot: to the bank (un-noted) or the inventory (default form). */
    public fun collectAll(player: Player, toBank: Boolean)

    /** Newest first, at most 40. */
    public fun history(player: Player): List<GeHistoryEntry>

    /** Our guide price (starts at the store value, follows trades). */
    public fun guidePrice(obj: ObjType): Int

    /** Remaining buy limit in the current 4h window, or null if the item has no limit. */
    public fun buyLimitRemaining(player: Player, obj: ObjType): Int?

    /** Re-sends all 8 slots, the 8 collect invs and the GE varps. */
    public fun sync(player: Player)
}
