package org.rsmod.content.other.npcdrops

import jakarta.inject.Inject
import org.rsmod.api.config.refs.objs
import org.rsmod.api.death.NpcDropTables
import org.rsmod.api.death.NpcDrops
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Drop tables (OSRS wiki, Oct 2025) for the Varrock monsters. The dark wizards, black knights,
 * skeletons and zombies share their tables with [DraynorDrops]; bosses are not covered.
 */
class VarrockDrops @Inject constructor(private val tables: NpcDropTables) : PluginScript() {
    override fun ScriptContext.startup() {
        with(varrock_npcs) {
            listOf(guard, guard_2, guard_3, guard_f, guard_f2, guard_f3, guard_captain).forEach {
                tables[it] = guardDrops
            }
            tables[warrior_woman] = bonesOnly
            tables[barbarian_woman] = barbarianDrops
            listOf(thief, thief_male1, thief_female1, thief_female2, varrock_man, varrock_woman)
                .forEach { tables[it] = manDrops }
            tables[farmer] = farmerDrops
            listOf(dark_wizard1, dark_wizard2).forEach { tables[it] = darkWizardLowDrops }
            listOf(dark_wizard3, dark_wizard4).forEach { tables[it] = darkWizardHighDrops }
            tables[goblin_armed] = goblinLevel5Drops
            listOf(grizzly_bear, black_bear).forEach { tables[it] = bearDrops }
            tables[monk_of_zamorak] = monkDrops
            listOf(moss_giant, moss_giant2, moss_giant3, moss_giant4).forEach {
                tables[it] = mossGiantDrops
            }
            listOf(giantspider2, deadly_red_spider, rat_indoors).forEach { tables[it] = NpcDrops() }
            listOf(ghost, ghost2, ghost3, ghost4, ghost5, ghost6, ghost7, ghost8).forEach {
                tables[it] = NpcDrops()
            }
        }
    }
}

private val guardDrops =
    with(extra_objs) {
        with(drop_objs) {
            NpcDrops(
                always = listOf(bones),
                main =
                    table(
                        members(iron_bolts, 10, 2..12),
                        drop(steel_arrow, 4, 1),
                        drop(bronze_arrow, 3, 1),
                        drop(air_rune, 2, 6),
                        drop(earth_rune, 2, 3),
                        drop(fire_rune, 2, 2),
                        members(blood_rune, 1, f2p = always(body_talisman)),
                        drop(chaos_rune, 1, 1),
                        drop(nature_rune, 1, 1),
                        drop(steel_arrow, 1, 5),
                        drop(bronze_arrow, 2, 2),
                        generalSeeds(18),
                        drop(objs.coins, 19, 1),
                        drop(objs.coins, 16, 7),
                        drop(objs.coins, 9, 12),
                        drop(objs.coins, 8, 4),
                        drop(objs.coins, 4, 25),
                        drop(objs.coins, 4, 17),
                        drop(objs.coins, 2, 30),
                        drop(iron_dagger, 6),
                        drop(body_talisman, 3),
                        drop(grain, 1),
                        drop(iron_ore, 1),
                    ),
            )
        }
    }

private val darkWizardHighDrops =
    with(extra_objs) {
        with(drop_objs) {
            NpcDrops(
                always = listOf(bones),
                main =
                    table(
                        drop(plainstaff, 4),
                        drop(wizard_hat, 4),
                        drop(black_robe, 3),
                        drop(earth_rune, 4, 36),
                        drop(air_rune, 3, 10),
                        drop(water_rune, 3, 10),
                        drop(earth_rune, 3, 10),
                        drop(fire_rune, 3, 10),
                        drop(air_rune, 2, 18),
                        drop(water_rune, 2, 18),
                        drop(earth_rune, 2, 18),
                        drop(fire_rune, 2, 18),
                        drop(nature_rune, 7, 4),
                        drop(chaos_rune, 6, 4),
                        drop(mind_rune, 3, 10),
                        drop(body_rune, 3, 10),
                        drop(mind_rune, 2, 18),
                        drop(body_rune, 2, 18),
                        members(blood_rune, 2, 2..2, f2p = always(wizard_hat)),
                        drop(cosmic_rune, 1, 2),
                        drop(law_rune, 1, 3),
                        drop(objs.coins, 17, 1),
                        drop(objs.coins, 16, 2),
                        drop(objs.coins, 9, 4),
                        drop(objs.coins, 3, 29),
                        drop(objs.coins, 1, 30),
                        drop(water_talisman, 2),
                        drop(fire_talisman, 2),
                    ),
            )
        }
    }

private val barbarianDrops =
    with(extra_objs) {
        with(drop_objs) {
            NpcDrops(
                always = listOf(bones),
                main =
                    table(
                        drop(bronze_axe, 6),
                        drop(plainstaff, 4),
                        drop(iron_mace, 1),
                        drop(chaos_rune, 4, 2),
                        drop(bronze_arrow, 3, 15),
                        drop(earth_rune, 3, 2),
                        drop(fire_rune, 2, 5),
                        drop(mind_rune, 2, 5),
                        drop(law_rune, 1, 2),
                        drop(objs.coins, 42, 5),
                        drop(objs.coins, 9, 8),
                        drop(objs.coins, 5, 17),
                        drop(objs.coins, 3, 27),
                        drop(tin_ore, 1),
                        drop(bear_fur, 1),
                        drop(beer, 1),
                        drop(cooked_meat, 1),
                        drop(flyer, 1),
                        drop(ring_mould, 1),
                        gems(1),
                    ),
            )
        }
    }

private val bearDrops =
    NpcDrops(always = listOf(bones, always(extra_objs.bear_fur), always(extra_objs.raw_bear_meat)))

private val monkDrops =
    NpcDrops(
        always = listOf(bones),
        main =
            tableOf(
                20,
                drop(extra_objs.zamorak_monk_bottom, 1),
                drop(extra_objs.zamorak_monk_top, 1),
            ),
    )

private val mossGiantDrops =
    with(extra_objs) {
        with(drop_objs) {
            NpcDrops(
                always = listOf(always(big_bones)),
                main =
                    table(
                        drop(black_sq_shield, 5),
                        drop(magic_staff, 2),
                        drop(steel_med_helm, 2),
                        drop(mithril_sword, 2),
                        members(mithril_spear, 2, f2p = always(objs.coins, 2)),
                        drop(steel_kiteshield, 1),
                        drop(law_rune, 4, 3),
                        drop(air_rune, 3, 18),
                        drop(earth_rune, 3, 27),
                        drop(chaos_rune, 3, 7),
                        drop(nature_rune, 3, 6),
                        drop(cosmic_rune, 2, 3),
                        drop(iron_arrow, 2, 15),
                        drop(steel_arrow, 1, 30),
                        drop(death_rune, 1, 3),
                        members(blood_rune, 1, f2p = always(objs.coins, 2)),
                        herbs(5),
                        uncommonSeeds(35),
                        drop(objs.coins, 19, 37),
                        drop(objs.coins, 8, 2),
                        drop(objs.coins, 10, 119),
                        drop(objs.coins, 2, 300),
                        drop(steel_bar, 6),
                        drop(coal, 1),
                        drop(spinach_roll, 1),
                        gems(4, underground = true),
                    ),
            )
        }
    }

object varrock_npcs : NpcReferences() {
    // Varrock spawns these 2023 guards; the older fai_varrock_guard types are not placed anywhere.
    val guard = find("fai_varrock_guard02")
    val guard_2 = find("fai_varrock_guard02_variant01")
    val guard_3 = find("fai_varrock_guard02_variant02")
    val guard_f = find("fai_varrock_guard02_f")
    val guard_f2 = find("fai_varrock_guard02_f_variant01")
    val guard_f3 = find("fai_varrock_guard02_f_variant02")
    val guard_captain = find("fai_varrock_guard_captain02")
    val warrior_woman = find("warrior_woman")
    val barbarian_woman = find("barbarianwoman_varrock")
    val thief = find("thief1")
    val thief_male1 = find("qip_soa_thief_male01")
    val thief_female1 = find("qip_soa_thief_female01")
    val thief_female2 = find("qip_soa_thief_female02")
    val varrock_man = find("varrock_man1")
    val varrock_woman = find("varrock_woman1")
    val farmer = find("farmer2")
    val dark_wizard1 = find("qip_ds_young_dark_wizard1")
    val dark_wizard2 = find("qip_ds_young_dark_wizard2")
    val dark_wizard3 = find("qip_ds_young_dark_wizard3")
    val dark_wizard4 = find("qip_ds_young_dark_wizard4")
    val goblin_armed = find("fai_barbarian_goblin_armed_1")
    val grizzly_bear = find("brownbear")
    val black_bear = find("darkbear")
    val monk_of_zamorak = find("chaosmonk2")
    val moss_giant = find("mossgiant")
    val moss_giant2 = find("mossgiant2")
    val moss_giant3 = find("mossgiant3")
    val moss_giant4 = find("mossgiant4")
    val giantspider2 = find("giantspider2")
    val deadly_red_spider = find("deadly_red_spider")
    val rat_indoors = find("rat_indoors")
    val ghost = find("ghost")
    val ghost2 = find("ghost2")
    val ghost3 = find("ghost3")
    val ghost4 = find("ghost4")
    val ghost5 = find("ghost5")
    val ghost6 = find("ghost6")
    val ghost7 = find("ghost7")
    val ghost8 = find("ghost8")
}
