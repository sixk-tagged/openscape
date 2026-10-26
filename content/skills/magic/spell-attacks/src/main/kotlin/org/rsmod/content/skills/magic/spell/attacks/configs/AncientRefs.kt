package org.rsmod.content.skills.magic.spell.attacks.configs

import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.api.type.refs.spot.SpotanimReferences
import org.rsmod.api.type.refs.synth.SynthReferences

object AncientSeqs : SeqReferences() {
    val rush_blitz_cast = find("zaros_casting")
    val burst_barrage_cast = find("zaros_vertical_casting")
    val vengeance_cast = find("vengeance_spell_anim_nostalling")
}

object AncientSpotanims : SpotanimReferences() {
    val smoke_rush_travel = find("smoke_rush_travel")
    val smoke_rush_impact = find("smoke_rush_impact")
    val smoke_burst_travel = find("smoke_burst_travel")
    val smoke_burst_impact = find("smoke_burst_impact")
    val smoke_blitz_travel = find("smoke_blitz_travel")
    val smoke_blitz_impact = find("smoke_blitz_impact")
    val smoke_barrage_travel = find("smoke_barrage_travel")
    val smoke_barrage_impact = find("smoke_barrage_impact")

    val shadow_rush_travel = find("shadow_rush_travel")
    val shadow_rush_impact = find("shadow_rush_impact")
    val shadow_burst_impact = find("shadow_burst_impact")
    val shadow_blitz_travel = find("shadow_blitz_travel")
    val shadow_blitz_impact = find("shadow_blitz_impact")
    val shadow_barrage_impact = find("shadow_barrage_impact")

    val blood_rush_travel = find("blood_rush_travel")
    val blood_rush_impact = find("blood_rush_impact")
    val blood_burst_impact = find("spell_blood_burst_impact")
    val blood_blitz_travel = find("blood_blitz_travel")
    val blood_blitz_impact = find("blood_blitz_impact")
    val blood_barrage_impact = find("spell_blood_barrage_impact")

    val ice_rush_travel = find("ice_rush_travel")
    val ice_rush_impact = find("ice_rush_impact")
    val ice_burst_travel = find("ice_burst_travel")
    val ice_burst_impact = find("ice_burst_impact")
    val ice_blitz_travel = find("ice_blitz_travel")
    val ice_blitz_impact = find("ice_blitz_impact")
    val ice_barrage_travel = find("ice_barrage_travel")
    val ice_barrage_impact = find("ice_barrage_impact")

    val vengeance_cast = find("quest_lunar_spellbook_vengeance_spot_anim")
}

object AncientSynths : SynthReferences() {
    val smoke_cast = find("smoke_cast")
    val smoke_rush_impact = find("smoke_rush_impact")
    val smoke_burst_impact = find("smoke_burst_impact")
    val smoke_blitz_impact = find("smoke_blitz_impact")
    val smoke_barrage_impact = find("smoke_barrage_impact")

    val shadow_cast = find("shadow_cast")
    val shadow_rush_impact = find("shadow_rush_impact")
    val shadow_burst_impact = find("shadow_burst_impact")
    val shadow_blitz_impact = find("shadow_blitz_impact")
    val shadow_barrage_impact = find("shadow_barrage_impact")

    val blood_cast = find("blood_cast")
    val blood_rush_impact = find("blood_rush_impact")
    val blood_burst_impact = find("blood_burst_impact")
    val blood_blitz_impact = find("blood_blitz_impact")
    val blood_barrage_impact = find("blood_barrage_impact")

    val ice_cast = find("ice_cast")
    val ice_rush_impact = find("ice_rush_impact")
    val ice_burst_impact = find("ice_burst_impact")
    val ice_blitz_impact = find("ice_blitz_impact")
    val ice_barrage_impact = find("ice_barrage_impact")
}
