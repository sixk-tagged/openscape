package org.rsmod.content.skills.runecraft.configs

import org.rsmod.api.type.builders.varp.VarpBuilder
import org.rsmod.api.type.refs.inv.InvReferences
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.api.type.refs.spot.SpotanimReferences
import org.rsmod.api.type.refs.varbit.VarBitReferences
import org.rsmod.api.type.refs.varp.VarpReferences

object RunecraftObjs : ObjReferences() {
    val rune_essence = find("blankrune")
    val pure_essence = find("blankrune_high")

    val air_rune = find("airrune")
    val mind_rune = find("mindrune")
    val water_rune = find("waterrune")
    val earth_rune = find("earthrune")
    val fire_rune = find("firerune")
    val body_rune = find("bodyrune")
    val cosmic_rune = find("cosmicrune")
    val chaos_rune = find("chaosrune")
    val astral_rune = find("astralrune")
    val nature_rune = find("naturerune")
    val law_rune = find("lawrune")
    val death_rune = find("deathrune")
    val blood_rune = find("bloodrune")
    val wrath_rune = find("wrathrune")

    val mist_rune = find("mistrune")
    val dust_rune = find("dustrune")
    val mud_rune = find("mudrune")
    val smoke_rune = find("smokerune")
    val steam_rune = find("steamrune")
    val lava_rune = find("lavarune")

    val air_talisman = find("air_talisman")
    val mind_talisman = find("mind_talisman")
    val water_talisman = find("water_talisman")
    val earth_talisman = find("earth_talisman")
    val fire_talisman = find("fire_talisman")
    val body_talisman = find("body_talisman")
    val cosmic_talisman = find("cosmic_talisman")
    val chaos_talisman = find("chaos_talisman")
    val nature_talisman = find("nature_talisman")
    val law_talisman = find("law_talisman")
    val death_talisman = find("death_talisman")
    val blood_talisman = find("blood_talisman")
    val wrath_talisman = find("wrath_talisman")
    val elemental_talisman = find("elemental_talisman")
    val catalytic_talisman = find("catalytic_talisman")

    val tiara = find("tiara")
    val tiara_gold = find("tiara_gold")
    val tiara_air = find("tiara_air")
    val tiara_mind = find("tiara_mind")
    val tiara_water = find("tiara_water")
    val tiara_earth = find("tiara_earth")
    val tiara_fire = find("tiara_fire")
    val tiara_body = find("tiara_body")
    val tiara_cosmic = find("tiara_cosmic")
    val tiara_chaos = find("tiara_chaos")
    val tiara_nature = find("tiara_nature")
    val tiara_law = find("tiara_law")
    val tiara_death = find("tiara_death")
    val tiara_blood = find("tiara_blood")
    val tiara_wrath = find("tiara_wrath")
    val tiara_elemental = find("tiara_elemental")
    val tiara_catalytic = find("tiara_catalytic")

    val mind_core = find("camdozaal_golem_core_mind")
    val body_core = find("camdozaal_golem_core_body")
    val chaos_core = find("camdozaal_golem_core_chaos")

    val binding_necklace = find("magic_emerald_necklace")

    val pack_air_rune = find("pack_airrune")
    val pack_water_rune = find("pack_waterrune")
    val pack_earth_rune = find("pack_earthrune")
    val pack_fire_rune = find("pack_firerune")
    val pack_mind_rune = find("pack_mindrune")
    val pack_chaos_rune = find("pack_chaosrune")

    val small_pouch = find("rcu_pouch_small")
    val medium_pouch = find("rcu_pouch_medium")
    val medium_pouch_decayed = find("rcu_pouch_medium_degrade")
    val large_pouch = find("rcu_pouch_large")
    val large_pouch_decayed = find("rcu_pouch_large_degrade")
    val giant_pouch = find("rcu_pouch_giant")
    val giant_pouch_decayed = find("rcu_pouch_giant_degrade")
    val colossal_pouch = find("rcu_pouch_colossal")
    val colossal_pouch_decayed = find("rcu_pouch_colossal_degrade")

    val runecrafting_cape = find("skillcape_runecrafting")
    val runecrafting_cape_trimmed = find("skillcape_runecrafting_trimmed")
}

object RunecraftLocs : LocReferences() {
    val air_altar = find("air_altar")
    val mind_altar = find("mind_altar")
    val water_altar = find("water_altar")
    val earth_altar = find("earth_altar")
    val fire_altar = find("fire_altar")
    val body_altar = find("body_altar")
    val cosmic_altar = find("cosmic_altar")
    val chaos_altar = find("chaos_altar")
    val astral_altar = find("astral_altar")
    val nature_altar = find("nature_altar")
    val law_altar = find("law_altar")
    val death_altar = find("death_altar")
    val blood_altar = find("blood_altar")
    val wrath_altar = find("wrath_altar")

    val air_ruins = find("airtemple_ruined")
    val mind_ruins = find("mindtemple_ruined")
    val water_ruins = find("watertemple_ruined")
    val earth_ruins = find("earthtemple_ruined")
    val fire_ruins = find("firetemple_ruined")
    val body_ruins = find("bodytemple_ruined")
    val cosmic_ruins = find("cosmictemple_ruined")
    val chaos_ruins = find("chaostemple_ruined")
    val nature_ruins = find("naturetemple_ruined")
    val law_ruins = find("lawtemple_ruined")
    val death_ruins = find("deathtemple_ruined")
    val blood_ruins = find("bloodtemple_ruined")
    val wrath_ruins = find("wrathtemple_ruined")

    val air_portal = find("airtemple_exit_portal")
    val mind_portal = find("mindtemple_exit_portal")
    val water_portal = find("watertemple_exit_portal")
    val earth_portal = find("earthtemple_exit_portal")
    val fire_portal = find("firetemple_exit_portal")
    val body_portal = find("bodytemple_exit_portal")
    val cosmic_portal = find("cosmictemple_exit_portal")
    val chaos_portal = find("chaostemple_exit_portal")
    val nature_portal = find("naturetemple_exit_portal")
    val law_portal = find("lawtemple_exit_portal")
    val death_portal = find("deathtemple_exit_portal")
    val blood_portal = find("bloodtemple_exit_portal")
    val wrath_portal = find("wrathtemple_exit_portal")

    /** Multiloc on `essencemine_portal`: its default value shows the "Use" child. */
    val mine_portal = find("blankrunestone_exit_portal")
}

object RunecraftNpcs : NpcReferences() {
    val aubury = find("aubury")
    val archmage_sedridor = find("head_wizard")
    val wizard_distentor = find("guild_wizard")
    val wizard_cromperty = find("ardounge_wizard")
    val brimstail = find("gnome_brimstail")
}

object RunecraftVarBits : VarBitReferences() {
    /** Each ruins' multiloc shows `Enter` while its bit in varp 491 / 3413 is set. */
    val air_enter = find("rc_no_tally_required_air")
    val mind_enter = find("rc_no_tally_required_mind")
    val water_enter = find("rc_no_tally_required_water")
    val earth_enter = find("rc_no_tally_required_earth")
    val fire_enter = find("rc_no_tally_required_fire")
    val body_enter = find("rc_no_tally_required_body")
    val cosmic_enter = find("rc_no_tally_required_cosmic")
    val law_enter = find("rc_no_tally_required_law")
    val nature_enter = find("rc_no_tally_required_nature")
    val chaos_enter = find("rc_no_tally_required_chaos")
    val death_enter = find("rc_no_tally_required_death")
    val wrath_enter = find("rc_no_tally_required_wrath")
    val blood_enter = find("rc_no_tally_required_blood")

    /** Flags for "teleported to the mine by this npc", read by Enter the Abyss. */
    val essence_wizards_tower = find("rcu_essencespot_wizardstower")
    val essence_aubury = find("rcu_essencespot_aubury")
    val essence_cromperty = find("rcu_essencespot_cromperty")
    val essence_brimstail = find("rcu_essencespot_brimstail")
    val essence_wizards_guild = find("rcu_essencespot_wizardsguild")
}

object RunecraftVarps : VarpReferences() {
    /** Rune Mysteries progress: 6 is complete, which shows the Teleport option on the npcs. */
    val rune_mysteries = find("runemysteries")
    /** Custom: charges the binding necklace has spent, 0..16. Per player, like recoil. */
    val binding_necklace_used = find("binding_necklace_used")
    /** Custom: which npc sent the player to the essence mine, an `EssenceMine.Origin` ordinal. */
    val essence_mine_origin = find("essence_mine_origin")
    /** Custom: how worn out each pouch is; see `PouchSizes`. */
    val pouch_decay_medium = find("pouch_decay_medium")
    val pouch_decay_large = find("pouch_decay_large")
    val pouch_decay_giant = find("pouch_decay_giant")
    val pouch_decay_colossal = find("pouch_decay_colossal")
}

internal object RunecraftVarpBuilder : VarpBuilder() {
    init {
        build("binding_necklace_used")
        build("essence_mine_origin")
        build("pouch_decay_medium")
        build("pouch_decay_large")
        build("pouch_decay_giant")
        build("pouch_decay_colossal")
    }
}

object RunecraftSeqs : SeqReferences() {
    val craft = find("human_runecraft")
    val teleport_other_cast = find("human_teleport_other_casting")
    val teleport_other_impact = find("human_teleport_other_impact")
}

object RunecraftSpotanims : SpotanimReferences() {
    val craft = find("runecrafting")
    val teleport_other_cast = find("teleport_other_casting")
    val teleport_other_impact = find("teleport_other_impact")
}

object RunecraftInvs : InvReferences() {
    val rune_shop = find("runeshop")
    val rune_shop_cape = find("runeshop_skillcape")
    val rune_shop_cape_trimmed = find("runeshop_skillcape_trimmed")
}
