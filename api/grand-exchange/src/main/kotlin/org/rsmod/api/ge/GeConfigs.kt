package org.rsmod.api.ge

import org.rsmod.api.type.builders.varp.VarpBuilder
import org.rsmod.api.type.refs.comp.ComponentReferences
import org.rsmod.api.type.refs.inv.InvReferences
import org.rsmod.api.type.refs.varbit.VarBitReferences
import org.rsmod.api.type.refs.varp.VarpReferences
import org.rsmod.game.type.comp.ComponentType
import org.rsmod.game.type.inv.InvType
import org.rsmod.game.type.varbit.VarBitType
import org.rsmod.game.type.varp.VarpType

public object GeInvs : InvReferences() {
    /** The collect box of each slot (item in inv slot 0, coins in inv slot 1). */
    public val collect: List<InvType> =
        listOf(
            find("tradingpost_sell_0"),
            find("tradingpost_sell_1"),
            find("tradingpost_sell_2"),
            find("tradingpost_sell_3"),
            find("tradingpost_sell_4"),
            find("tradingpost_sell_5"),
            find("ge_collect_6"),
            find("ge_collect_7"),
        )
}

public object GeComponents : ComponentReferences() {
    public val collect_inv: ComponentType = find("ge_collect:collect_inv")
    public val collect_bank: ComponentType = find("ge_collect:collect_bank")

    /** The box pair of each slot on the collection box: sub 1 = items, sub 2 = coins. */
    public val collect: List<ComponentType> =
        listOf(
            find("ge_collect:collect_0"),
            find("ge_collect:collect_1"),
            find("ge_collect:collect_2"),
            find("ge_collect:collect_3"),
            find("ge_collect:collect_4"),
            find("ge_collect:collect_5"),
            find("ge_collect:collect_6"),
            find("ge_collect:collect_7"),
        )
}

public object GeVarps : VarpReferences() {
    /** 1 if the player wants a reminder at login when something waits in the collect boxes. */
    public val collect_reminder: VarpType = find("ge_collect_reminder")

    /** Total tax paid so far by the offer in each slot; the client reads these by slot. */
    public val tax_slot: List<VarpType> =
        listOf(
            find("ge_tax_slot_0"),
            find("ge_tax_slot_1"),
            find("musiclength"),
            find("last_musiclength"),
            find("last_song"),
            find("currentsong"),
            find("ge_tax_slot_6"),
            find("ge_tax_slot_7"),
        )

    /** The client's Jagex-side "item sink" offers. We never use them, see [GeSync]. */
    public val itemsink_obj: List<VarpType> =
        listOf(
            find("ge_itemsink_obj_0"),
            find("ge_itemsink_obj_1"),
            find("ge_itemsink_obj_2"),
            find("ge_itemsink_obj_3"),
            find("ge_itemsink_obj_4"),
            find("ge_itemsink_obj_5"),
            find("ge_itemsink_obj_6"),
            find("ge_itemsink_obj_7"),
        )
    public val itemsink_price: List<VarpType> =
        listOf(
            find("ge_itemsink_price_0"),
            find("ge_itemsink_price_1"),
            find("ge_itemsink_price_2"),
            find("ge_itemsink_price_3"),
            find("ge_itemsink_price_4"),
            find("ge_itemsink_price_5"),
            find("ge_itemsink_price_6"),
            find("ge_itemsink_price_7"),
        )

    /** Obj varps that must read -1 (no item) until the offer screen sets them. */
    public val tradingpost_search: VarpType = find("tradingpost_search")
    public val last_searched: VarpType = find("ge_last_searched")
    public val last_offer_item: VarpType = find("ge_last_offer_item")
}

public object GeVarBits : VarBitReferences() {
    /** The fee in tenths of a percent. The client hides the fee text when it is 0. */
    public val taxrate: VarBitType = find("ge_transmit_taxrate")
}

internal object GeVarpBuilder : VarpBuilder() {
    init {
        build("ge_collect_reminder") { permanent = true }
    }
}
