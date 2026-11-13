package org.rsmod.content.other.grandexchangeextras

import org.rsmod.api.type.builders.inv.InvBuilder
import org.rsmod.api.type.refs.comp.ComponentReferences
import org.rsmod.api.type.refs.interf.InterfaceReferences
import org.rsmod.api.type.refs.inv.InvReferences
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.type.inv.InvScope
import org.rsmod.game.type.inv.InvStackType

typealias ge_extras_interfaces = GeExtrasInterfaces

typealias ge_extras_components = GeExtrasComponents

typealias ge_extras_npcs = GeExtrasNpcs

typealias ge_extras_invs = GeExtrasInvs

typealias ge_extras_objs = GeExtrasObjs

object GeExtrasInterfaces : InterfaceReferences() {
    val item_sets = find("itemsets")
    val item_sets_side = find("itemsets_side")
    val price_list = find("ge_pricelist")
}

object GeExtrasComponents : ComponentReferences() {
    val item_sets_list = find("itemsets:itemlist")
    val item_sets_side_items = find("itemsets_side:items")
    val price_list_items = find("ge_pricelist:list")
}

object GeExtrasNpcs : NpcReferences() {
    val ge_clerk_1 = find("ge_clerk_1")
    val ge_clerk_2 = find("ge_clerk_2")
    val ge_clerk_3 = find("ge_clerk_3")
    val ge_clerk_4 = find("ge_clerk_4")
    val ge_boss = find("ge_boss")
    val ge_expert_ores = find("ge_expert_ores")
    val ge_expert_herbs = find("ge_expert_herbs")
    val ge_expert_runes = find("ge_expert_runes")
    val ge_expert_logs = find("ge_expert_logs")
    val ge_expert_combat = find("ge_expert_combat")

    val clerks = listOf(ge_clerk_1, ge_clerk_2, ge_clerk_3, ge_clerk_4)
}

object GeExtrasInvs : InvReferences() {
    /** Custom: the price list's rows. Slot n holds the item, its count is the item's price. */
    val price_list = find("ge_pricelist_data")
}

object GeExtrasObjs : ObjReferences() {
    val vial_empty = find("vial_empty")
    val ruby_ring = find("ruby_ring")
    val ring_of_forging = find("ring_of_forging")
}

internal object GeExtrasInvBuilder : InvBuilder() {
    init {
        build("ge_pricelist_data") {
            scope = InvScope.Temp
            stack = InvStackType.Always
            size = PRICE_LIST_SIZE
            protect = false
        }
    }
}

/** Rows the price list can hold; the longest list (weapons and armour) has 95. */
const val PRICE_LIST_SIZE: Int = 100
