package org.rsmod.content.interfaces.combat.tab.configs

import org.rsmod.api.type.refs.comp.ComponentReferences
import org.rsmod.api.type.refs.interf.InterfaceReferences

object AutocastInterfaces : InterfaceReferences() {
    val autocast = find("autocast")
}

object AutocastComponents : ComponentReferences() {
    val choose_spell = find("combat_interface:autocast_normal")
    val choose_defensive_spell = find("combat_interface:autocast_defensive")

    /** Dynamic children of this component are the spells, by autocast id. */
    val spells = find("autocast:spells")
    val cancel_3 = find("autocast:com_3")
    val cancel_4 = find("autocast:com_4")
    val cancel_5 = find("autocast:com_5")
}
