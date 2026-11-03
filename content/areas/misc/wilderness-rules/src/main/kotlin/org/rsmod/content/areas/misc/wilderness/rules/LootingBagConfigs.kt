package org.rsmod.content.areas.misc.wilderness.rules

import org.rsmod.api.type.editors.inv.InvEditor
import org.rsmod.api.type.refs.comp.ComponentReferences
import org.rsmod.api.type.refs.interf.InterfaceReferences
import org.rsmod.api.type.refs.inv.InvReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.varbit.VarBitReferences
import org.rsmod.game.type.inv.InvScope

object LootingBagObjs : ObjReferences() {
    val closed = find("looting_bag")
    val open = find("looting_bag_open")
    val vial_water = find("vial_water")
}

object LootingBagInvs : InvReferences() {
    /** The bag's 28 stacks. */
    val bag = find("looting_bag")
}

object LootingBagInterfaces : InterfaceReferences() {
    val bag = find("wilderness_lootingbag")
}

object LootingBagComponents : ComponentReferences() {
    val items = find("wilderness_lootingbag:items")
    val total = find("wilderness_lootingbag:total")
}

object LootingBagVarBits : VarBitReferences() {
    /** The bag's "Settings" choice: 1 stores every unit of an item used on it without asking. */
    val store_all = find("lootingbag_useallitems")
}

internal object LootingBagInvEditor : InvEditor() {
    init {
        edit(LootingBagInvs.bag) {
            scope = InvScope.Perm
            protect = false
            size = LootingBagScript.SIZE
        }
    }
}
