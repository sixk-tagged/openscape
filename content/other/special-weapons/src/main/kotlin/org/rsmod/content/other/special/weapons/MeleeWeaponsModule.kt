package org.rsmod.content.other.special.weapons

import org.rsmod.api.weapons.WeaponMap
import org.rsmod.content.other.special.weapons.melee.VeracsFlailWeapons
import org.rsmod.plugin.module.PluginModule

class MeleeWeaponsModule : PluginModule() {
    override fun bind() {
        addSetBinding<WeaponMap>(VeracsFlailWeapons::class.java)
    }
}
