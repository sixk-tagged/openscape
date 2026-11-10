package org.rsmod.content.other.npcdrops

import jakarta.inject.Inject
import org.rsmod.api.death.Drop
import org.rsmod.api.death.NpcDropTables
import org.rsmod.api.death.NpcDrops
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Drop tables (OSRS wiki, Sep 2025) for the Slayer Tower: crawling hands, banshees, infernal mages,
 * bloodveld, aberrant spectres, gargoyles, nechryael and abyssal demons (the basement's variants
 * share them). Rare main-table drops (mystic pieces, the granite maul, the abyssal whip and dagger)
 * keep their odds: the table total is the rates' common multiple. Tertiaries (clues, ensouled
 * heads, brimstone keys, the crawling hand item) are left out.
 */
class SlayerTowerDrops @Inject constructor(private val tables: NpcDropTables) : PluginScript() {
    override fun ScriptContext.startup() {
        with(tower_drop_npcs) {
            crawling_hands.forEach { tables[it] = crawlingHandDrops }
            tables[banshee] = bansheeDrops
            infernal_mages.forEach { tables[it] = infernalMageDrops }
            bloodveld.forEach { tables[it] = bloodveldDrops }
            aberrant_spectres.forEach { tables[it] = aberrantSpectreDrops }
            gargoyles.forEach { tables[it] = gargoyleDrops }
            nechryael.forEach { tables[it] = nechryaelDrops }
            abyssal_demons.forEach { tables[it] = abyssalDemonDrops }
            with(superior_drop_npcs) {
                tables[crawling_hand] = superiorDrops(crawlingHandDrops, 5)
                tables[banshee] = superiorDrops(bansheeDrops, 15)
                tables[infernal_mage] = superiorDrops(infernalMageDrops, 45)
                tables[bloodveld] = superiorDrops(bloodveldDrops, 50)
                tables[aberrant_spectre] = superiorDrops(aberrantSpectreDrops, 60)
                tables[gargoyle] = superiorDrops(gargoyleDrops, 75)
                tables[nechryael] = superiorDrops(nechryaelDrops, 80)
                tables[abyssal_demon] = superiorDrops(abyssalDemonDrops, 85)
            }
        }
    }
}

object tower_drop_npcs : NpcReferences() {
    val crawling_hands =
        listOf(
            find("slayer_crawling_hand_1"),
            find("slayer_crawling_hand_big_1"),
            find("slayer_crawling_hand_big_2"),
        )
    val banshee = find("slayer_banshee_1")
    val infernal_mages = (1..5).map { find("slayer_infernal_mage_$it") }
    val bloodveld =
        listOf(
            find("slayer_bloodveld"),
            find("slayer_bloodveld_baby"),
            find("slayer_bloodveld_strongholdcave"),
            find("slayer_bloodveld_baby_strongholdcave"),
        )
    val aberrant_spectres = (1..4).map { find("slayer_abberant_spectre_$it") }
    val gargoyles = listOf(find("slayer_gargoyle_1"), find("slayer_cave_gargoyle"))
    val nechryael = listOf(find("slayer_nechryael"), find("slayer_nechryael_strongholdcave"))
    val abyssal_demons = listOf(find("slayer_abyssal"), find("slayer_abyssal_strongholdcave"))
}

private val crawlingHandDrops =
    with(tower_drop_objs) {
        NpcDrops(
            always = listOf(always(bones)),
            main =
                table(
                    drop(leather_gloves, 21),
                    drop(wolfengloves_purple, 2),
                    drop(wolfengloves_tangerine, 2),
                    drop(wolfengloves_crimson, 2),
                    drop(wolfengloves_ocean, 2),
                    drop(gold_ring, 3),
                    drop(sapphire_ring, 2),
                    drop(emerald_ring, 2),
                    drop(coins, 21, 5),
                    drop(coins, 23, 8),
                    gems(2, underground = true),
                ),
        )
    }

private val bansheeDrops =
    with(tower_drop_objs) {
        NpcDrops(
            main =
                tableOf(
                    513,
                    drop(iron_mace, 8),
                    drop(iron_dagger, 8),
                    drop(iron_kiteshield, 4),
                    drop(mystic_gloves_dark, 1),
                    drop(airrune, 12, 3),
                    drop(cosmicrune, 12, 2),
                    drop(chaosrune, 8, 3),
                    drop(firerune, 4, 7),
                    drop(chaosrune, 4, 7),
                    Drop(table = herbs, weight = 136),
                    drop(noted_pure_essence, 88, 13),
                    drop(iron_ore, 4),
                    drop(coins, 40, 13),
                    drop(coins, 32, 26),
                    drop(coins, 32, 35),
                    drop(fishing_bait, 88, 15),
                    drop(fishing_bait, 20, 7),
                    drop(eye_of_newt, 4),
                    gems(8, underground = true),
                )
        )
    }

private val infernalMageDrops =
    with(tower_drop_objs) {
        NpcDrops(
            always = listOf(always(bones)),
            main =
                tableOf(
                    64000,
                    drop(lava_battlestaff, 64),
                    drop(mystic_boots_dark, 125),
                    drop(mystic_hat_dark, 125),
                    drop(plainstaff, 4000),
                    drop(staff_of_fire, 500),
                    drop(earthrune, 3000, 10),
                    drop(firerune, 3000, 10),
                    drop(earthrune, 2000, 36),
                    drop(airrune, 1500, 10),
                    drop(waterrune, 1500, 10),
                    drop(airrune, 1000, 18),
                    drop(waterrune, 1000, 18),
                    drop(earthrune, 1000, 18),
                    drop(firerune, 1000, 18),
                    drop(mindrune, 1000, 18),
                    drop(bodyrune, 1000, 18),
                    drop(bloodrune, 1000, 4),
                    drop(deathrune, 9000, 7),
                    drop(coins, 10500),
                    drop(coins, 8000, 2),
                    drop(coins, 4500, 4),
                    drop(coins, 1500, 29),
                ),
        )
    }

private val bloodveldDrops =
    with(tower_drop_objs) {
        NpcDrops(
            always = listOf(always(vile_ashes)),
            main =
                tableOf(
                    138,
                    drop(steel_axe, 4),
                    drop(steel_full_helm, 4),
                    drop(steel_scimitar, 2),
                    drop(black_armoured_boots, 1),
                    drop(mithril_sq_shield, 1),
                    drop(mithril_chainbody, 1),
                    drop(rune_med_helm, 1),
                    drop(firerune, 8, 60),
                    drop(bloodrune, 3, 3),
                    drop(bloodrune, 5, 10),
                    drop(bloodrune, 1, 30),
                    Drop(table = herbs, weight = 1),
                    drop(coins, 7, 10),
                    drop(coins, 29, 40),
                    drop(coins, 30, 120),
                    drop(coins, 10, 200),
                    drop(coins, 1, 450),
                    drop(bones, 10),
                    drop(big_bones, 7),
                    drop(big_bones, 3, 3),
                    drop(gold_ore, 2),
                    drop(meat_pizza, 3),
                    gems(4, underground = true),
                ),
        )
    }

private val aberrantSpectreDrops =
    with(tower_drop_objs) {
        NpcDrops(
            main =
                tableOf(
                    512,
                    drop(steel_axe, 12),
                    drop(mithril_kiteshield, 4),
                    drop(lava_battlestaff, 4),
                    drop(adamant_platelegs, 4),
                    drop(rune_full_helm, 4),
                    drop(mystic_robe_bottom_dark, 1),
                    Drop(table = herbs, weight = 312),
                    drop(coins, 4, 460),
                    gems(20, underground = true),
                )
        )
    }

private val gargoyleDrops =
    with(tower_drop_objs) {
        NpcDrops(
            main =
                tableOf(
                    515,
                    drop(granite_maul, 2),
                    drop(mystic_robe_top_dark, 1),
                    drop(adamant_platelegs, 16),
                    drop(rune_full_helm, 12),
                    drop(rune_2h_sword, 8),
                    drop(adamant_armoured_boots, 4),
                    drop(rune_battleaxe, 4),
                    drop(rune_platelegs, 4),
                    drop(firerune, 40, 75),
                    drop(chaosrune, 32, 30),
                    drop(firerune, 24, 150),
                    drop(deathrune, 20, 15),
                    drop(noted_gold_ore, 40, 10..20),
                    drop(noted_pure_essence, 24, 150),
                    drop(noted_steel_bar, 24, 15),
                    drop(noted_gold_bar, 12, 10..15),
                    drop(noted_mithril_bar, 8, 15),
                    drop(runite_ore, 8),
                    drop(coins, 112, 400..800),
                    drop(coins, 80, 500..1000),
                    drop(coins, 20, 10000),
                    gems(20, underground = true),
                )
        )
    }

private val nechryaelDrops =
    with(tower_drop_objs) {
        NpcDrops(
            always = listOf(always(malicious_ashes)),
            main =
                tableOf(
                    232,
                    drop(adamant_platelegs, 8),
                    drop(rune_2h_sword, 8),
                    drop(rune_full_helm, 6),
                    drop(adamant_kiteshield, 4),
                    drop(rune_armoured_boots, 2),
                    drop(chaosrune, 16, 37),
                    drop(deathrune, 12, 5),
                    drop(deathrune, 12, 10),
                    drop(lawrune, 10, 25..35),
                    drop(bloodrune, 8, 15..20),
                    drop(limpwurt_seed, 12),
                    drop(coins, 26, 1000..1499),
                    drop(coins, 21, 1500..2000),
                    drop(coins, 12, 2500..2999),
                    drop(coins, 6, 3000..3500),
                    drop(coins, 5, 500..999),
                    drop(coins, 2, 5000),
                    drop(noted_soft_clay, 8, 25),
                    drop(tuna, 6),
                    rareTable(2),
                ),
        )
    }

private val abyssalDemonDrops =
    with(tower_drop_objs) {
        NpcDrops(
            always = listOf(always(abyssal_ashes)),
            main =
                tableOf(
                    64000,
                    drop(black_sword, 2000),
                    drop(steel_battleaxe, 1500),
                    drop(black_axe, 1000),
                    drop(mithril_kiteshield, 500),
                    drop(rune_chainbody, 500),
                    drop(rune_med_helm, 500),
                    drop(abyssal_whip, 125),
                    drop(abyssal_dagger, 2),
                    drop(airrune, 4000, 50),
                    drop(chaosrune, 3500, 10),
                    drop(bloodrune, 2000, 7),
                    drop(lawrune, 500, 3),
                    Drop(table = herbs, weight = 9500),
                    drop(noted_pure_essence, 2500, 60),
                    drop(adamantite_bar, 1000),
                    drop(coins, 17500, 132),
                    drop(coins, 4500, 220),
                    drop(coins, 3500, 30),
                    drop(coins, 3000, 44),
                    drop(coins, 500, 460),
                    drop(lobster, 1000),
                    drop(cosmic_talisman, 500),
                    drop(chaos_talisman, 500),
                    drop(obj_3dose1defense, 500),
                    rareTable(1000),
                ),
        )
    }

object tower_drop_objs : ObjReferences() {
    val noted_pure_essence = find("cert_blankrune_high")
    val noted_gold_ore = find("cert_gold_ore")
    val noted_steel_bar = find("cert_steel_bar")
    val noted_gold_bar = find("cert_gold_bar")
    val noted_mithril_bar = find("cert_mithril_bar")
    val noted_soft_clay = find("cert_softclay")
    val abyssal_ashes = find("abyssal_ashes")
    val abyssal_dagger = find("abyssal_dagger")
    val abyssal_whip = find("abyssal_whip")
    val adamant_armoured_boots = find("adamant_armoured_boots")
    val adamant_kiteshield = find("adamant_kiteshield")
    val adamant_platelegs = find("adamant_platelegs")
    val adamantite_bar = find("adamantite_bar")
    val airrune = find("airrune")
    val big_bones = find("big_bones")
    val black_armoured_boots = find("black_armoured_boots")
    val black_axe = find("black_axe")
    val black_sword = find("black_sword")
    val blankrune_high = find("blankrune_high")
    val bloodrune = find("bloodrune")
    val bodyrune = find("bodyrune")
    val bones = find("bones")
    val chaos_talisman = find("chaos_talisman")
    val chaosrune = find("chaosrune")
    val coins = find("coins")
    val cosmic_talisman = find("cosmic_talisman")
    val cosmicrune = find("cosmicrune")
    val deathrune = find("deathrune")
    val earthrune = find("earthrune")
    val emerald_ring = find("emerald_ring")
    val eye_of_newt = find("eye_of_newt")
    val firerune = find("firerune")
    val fishing_bait = find("fishing_bait")
    val gold_bar = find("gold_bar")
    val gold_ore = find("gold_ore")
    val gold_ring = find("gold_ring")
    val granite_maul = find("granite_maul")
    val iron_dagger = find("iron_dagger")
    val iron_kiteshield = find("iron_kiteshield")
    val iron_mace = find("iron_mace")
    val iron_ore = find("iron_ore")
    val lava_battlestaff = find("lava_battlestaff")
    val lawrune = find("lawrune")
    val leather_gloves = find("leather_gloves")
    val limpwurt_seed = find("limpwurt_seed")
    val lobster = find("lobster")
    val malicious_ashes = find("malicious_ashes")
    val meat_pizza = find("meat_pizza")
    val mindrune = find("mindrune")
    val mithril_bar = find("mithril_bar")
    val mithril_chainbody = find("mithril_chainbody")
    val mithril_kiteshield = find("mithril_kiteshield")
    val mithril_sq_shield = find("mithril_sq_shield")
    val mystic_boots_dark = find("mystic_boots_dark")
    val mystic_gloves_dark = find("mystic_gloves_dark")
    val mystic_hat_dark = find("mystic_hat_dark")
    val mystic_robe_bottom_dark = find("mystic_robe_bottom_dark")
    val mystic_robe_top_dark = find("mystic_robe_top_dark")
    val obj_3dose1defense = find("3dose1defense")
    val plainstaff = find("plainstaff")
    val rune_2h_sword = find("rune_2h_sword")
    val rune_armoured_boots = find("rune_armoured_boots")
    val rune_battleaxe = find("rune_battleaxe")
    val rune_chainbody = find("rune_chainbody")
    val rune_full_helm = find("rune_full_helm")
    val rune_med_helm = find("rune_med_helm")
    val rune_platelegs = find("rune_platelegs")
    val runite_ore = find("runite_ore")
    val sapphire_ring = find("sapphire_ring")
    val softclay = find("softclay")
    val staff_of_fire = find("staff_of_fire")
    val steel_axe = find("steel_axe")
    val steel_bar = find("steel_bar")
    val steel_battleaxe = find("steel_battleaxe")
    val steel_full_helm = find("steel_full_helm")
    val steel_scimitar = find("steel_scimitar")
    val tuna = find("tuna")
    val vile_ashes = find("vile_ashes")
    val waterrune = find("waterrune")
    val wolfengloves_crimson = find("wolfengloves_crimson")
    val wolfengloves_ocean = find("wolfengloves_ocean")
    val wolfengloves_purple = find("wolfengloves_purple")
    val wolfengloves_tangerine = find("wolfengloves_tangerine")
}
