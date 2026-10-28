package org.rsmod.content.skills.magic.spell.utility

import org.rsmod.api.spells.attack.SpellAttackMap
import org.rsmod.plugin.module.PluginModule

class UtilityModule : PluginModule() {
    override fun bind() {
        addSetBinding<SpellAttackMap>(CurseSpells::class.java)
        addSetBinding<SpellAttackMap>(CombatSpells::class.java)
    }
}
