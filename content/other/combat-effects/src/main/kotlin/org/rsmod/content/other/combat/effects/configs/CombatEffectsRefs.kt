package org.rsmod.content.other.combat.effects.configs

import org.rsmod.api.type.builders.varp.VarpBuilder
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.spot.SpotanimReferences
import org.rsmod.api.type.refs.varp.VarpReferences

typealias effect_varps = EffectVarps

typealias effect_objs = EffectObjs

typealias effect_spotanims = EffectSpotanims

object EffectVarps : VarpReferences() {
    /** Hitpoints of recoil the worn ring of recoil has dealt so far; it shatters at 40. */
    val recoil_damage_dealt = find("recoil_damage_dealt")
}

object EffectObjs : ObjReferences() {
    val ring_of_recoil = find("ring_of_recoil")
}

object EffectSpotanims : SpotanimReferences() {
    val redemption = find("redemption_spotanim")
}

internal object EffectVarpBuilder : VarpBuilder() {
    init {
        build("recoil_damage_dealt") { temporary = true }
    }
}
