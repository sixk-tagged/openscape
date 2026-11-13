package org.rsmod.content.interfaces.grandexchange

import org.rsmod.api.type.refs.comp.ComponentReferences
import org.rsmod.api.type.refs.interf.InterfaceReferences
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.varbit.VarBitReferences
import org.rsmod.api.type.refs.varp.VarpReferences

typealias ge_screen_interfaces = GeScreenInterfaces

typealias ge_screen_components = GeScreenComponents

typealias ge_screen_varps = GeScreenVarps

typealias ge_screen_varbits = GeScreenVarBits

object GeScreenInterfaces : InterfaceReferences() {
    val offers = find("ge_offers")
    val offers_side = find("ge_offers_side")
    val history = find("ge_history")
}

object GeScreenComponents : ComponentReferences() {
    val history = find("ge_offers:history")
    val back = find("ge_offers:back")
    /** Sub 0 collects everything (op1 inventory, op2 bank), sub 1 repeats the last offer. */
    val collectall = find("ge_offers:collectall")
    /** Sub 2 is the slot's offer, 3 "Create Buy offer", 4 "Create Sell offer". */
    val index =
        listOf(
            find("ge_offers:index_0"),
            find("ge_offers:index_1"),
            find("ge_offers:index_2"),
            find("ge_offers:index_3"),
            find("ge_offers:index_4"),
            find("ge_offers:index_5"),
            find("ge_offers:index_6"),
            find("ge_offers:index_7"),
        )
    val details_desc = find("ge_offers:details_desc")
    val details_marketprice = find("ge_offers:details_marketprice")
    val details_fee = find("ge_offers:details_fee")
    /** Sub 0 aborts the offer. */
    val details_status = find("ge_offers:details_status")
    /** Sub 2 is the item box, 3 the coin box. */
    val details_collect = find("ge_offers:details_collect")
    /** The new offer's buttons, one sub each (see [GeOfferScreen]). */
    val setup = find("ge_offers:setup")
    val setup_desc = find("ge_offers:setup_desc")
    val setup_marketprice = find("ge_offers:setup_marketprice")
    val setup_fee = find("ge_offers:setup_fee")
    val setup_confirm = find("ge_offers:setup_confirm")
    val side_items = find("ge_offers_side:items")
    val history_exchange = find("ge_history:exchange")
    /** Row n's clickable box is sub n * 6. */
    val history_list = find("ge_history:list")
}

object GeScreenVarps : VarpReferences() {
    /** The obj of the offer being set up; -1 is none. */
    val tradingpost_search = find("tradingpost_search")
    val last_searched = find("ge_last_searched")
    val last_offer_item = find("ge_last_offer_item")
    val last_offer_quantity = find("ge_last_offer_quantity")
    val last_offer_price = find("ge_last_offer_price")
    /** 0 buy, 1 sell. */
    val last_offer_type = find("ge_last_offer_type")
}

object GeScreenVarBits : VarBitReferences() {
    /** 0 is the index panel, else the slot + 1. */
    val selectedslot = find("ge_selectedslot")
    val newoffer_quantity = find("ge_newoffer_quantity")
    val newoffer_price = find("ge_newoffer_price")
    /** 0 buy, 1 sell. */
    val newoffer_type = find("ge_newoffer_type")
    /** The custom -X% / +X% buttons' percentage; 0 when not set. */
    val price_custom = find("ge_price_custom")
}

internal object GeScreenLocs : LocReferences() {
    val exchange_booth = find("exchange_bank_wall_exchange")
}
