package org.rsmod.content.other.npcdrops

import jakarta.inject.Inject
import org.rsmod.api.config.refs.objs
import org.rsmod.api.death.Drop
import org.rsmod.api.death.NpcDropTables
import org.rsmod.api.death.NpcDrops
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Drop tables (OSRS wiki, Oct 2025) for the Draynor monsters: village, jail, Wizards' Tower, the
 * sewers and the manor. Also holds the tables Varrock reuses (dark wizards, black knights, the
 * sewer skeletons and zombies).
 */
class DraynorDrops @Inject constructor(private val tables: NpcDropTables) : PluginScript() {
    override fun ScriptContext.startup() {
        with(draynor_npcs) {
            tables[young_dark_wizard] = darkWizardLowDrops
            tables[black_knight] = blackKnightDrops
            tables[highwayman] = highwaymanDrops
            listOf(jailguard, jailguard_two, jailguard_three, jailguard_four, market_guard)
                .forEach { tables[it] = bonesOnly }
            listOf(house_ghost, house_ghost2, house_ghost3, house_ghost4, house_ghost5).forEach {
                tables[it] = NpcDrops()
            }
            tables[draynor_skeleton] = NpcDrops()

            // The wiki gives only skeletons 70 and 74 the unarmed table and 77 the armed one;
            // every other id is "Plain" (bones).
            listOf(skeleton_unarmed, skeleton_unaggressive).forEach {
                tables[it] = skeletonUnarmedDrops
            }
            tables[skeleton_armed] = skeletonArmedDrops
            listOf(
                    skeleton_unarmed2,
                    skeleton_unarmed3,
                    skeleton_unarmed4,
                    skeleton_unaggressive2,
                    skeleton_unaggressive3,
                    skeleton_armed2,
                    skeleton_armed3,
                    skeleton_armed4,
                    skeleton_armed5,
                )
                .forEach { tables[it] = bonesOnly }

            zombie_unarmed_ids.forEach { tables[it] = zombieLevel13Drops }
            listOf(zombie2, zombie2_b, zombie2_c).forEach { tables[it] = bonesOnly }
            zombie_armed_ids.forEach { tables[it] = zombieLevel24Drops }

            tables[wizard] = wizardDrops
            tables[lesser_demon] = lesserDemonDrops
            tables[sourhog] = sourhogDrops
            listOf(skeletal_wyvern1, skeletal_wyvern2, skeletal_wyvern3, skeletal_wyvern4).forEach {
                tables[it] = skeletalWyvernDrops
            }
        }
    }
}

/** The level 7 dark wizard table (Draynor and the Varrock stone circle). */
internal val darkWizardLowDrops =
    with(extra_objs) {
        with(drop_objs) {
            NpcDrops(
                always = listOf(bones),
                main =
                    table(
                        drop(plainstaff, 8),
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
                        drop(chaos_rune, 6, 5),
                        drop(mind_rune, 3, 10),
                        drop(body_rune, 3, 10),
                        drop(mind_rune, 2, 18),
                        drop(body_rune, 2, 18),
                        // The free-world wizard hat rate (6/128) takes this slot's place.
                        members(blood_rune, 2, 2..2, f2p = always(wizard_hat)),
                        drop(cosmic_rune, 1, 2),
                        drop(law_rune, 1, 3),
                        drop(objs.coins, 17, 1),
                        drop(objs.coins, 16, 2),
                        drop(objs.coins, 7, 4),
                        drop(objs.coins, 3, 29),
                        drop(objs.coins, 1, 30),
                        drop(water_talisman, 1),
                        drop(fire_talisman, 1),
                    ),
            )
        }
    }

internal val blackKnightDrops =
    with(extra_objs) {
        with(drop_objs) {
            NpcDrops(
                always = listOf(bones),
                main =
                    table(
                        drop(iron_sword, 4),
                        drop(iron_full_helm, 2),
                        drop(steel_mace, 1),
                        drop(mithril_arrow, 4, 3),
                        drop(body_rune, 3, 9),
                        drop(chaos_rune, 3, 6),
                        drop(earth_rune, 3, 10),
                        drop(death_rune, 2, 2),
                        drop(law_rune, 2, 3),
                        drop(cosmic_rune, 1, 7),
                        drop(mind_rune, 1, 2),
                        herbs(3),
                        generalSeeds(18),
                        drop(steel_bar, 6),
                        drop(tin_ore, 1),
                        drop(pot_of_flour, 1),
                        drop(objs.coins, 21, 35),
                        drop(objs.coins, 11, 6),
                        drop(objs.coins, 10, 58),
                        drop(objs.coins, 9, 12),
                        drop(objs.coins, 2, 80),
                        drop(objs.coins, 14, 1),
                        drop(bread, 1),
                        gems(3),
                    ),
            )
        }
    }

private val highwaymanDrops =
    NpcDrops(
        always = listOf(bones, always(extra_objs.black_cape)),
        main = tableOf(100, members(extra_objs.iron_bolts, 5, 2..12)),
    )

private val skeletonUnarmedDrops =
    with(extra_objs) {
        with(drop_objs) {
            NpcDrops(
                always = listOf(bones),
                main =
                    table(
                        drop(bronze_arrow, 7, 2),
                        drop(bronze_arrow, 4, 5),
                        drop(iron_arrow, 4, 1),
                        drop(air_rune, 2, 15),
                        drop(earth_rune, 2, 3),
                        drop(fire_rune, 2, 2),
                        drop(chaos_rune, 2, 3),
                        drop(nature_rune, 1, 3),
                        drop(steel_arrow, 1, 1),
                        herbs(21),
                        drop(objs.coins, 18, 2),
                        drop(objs.coins, 15, 12),
                        drop(objs.coins, 7, 4),
                        drop(objs.coins, 4, 16),
                        drop(objs.coins, 4, 25),
                        drop(objs.coins, 4, 33),
                        drop(objs.coins, 1, 48),
                        drop(iron_dagger, 6),
                        drop(fire_talisman, 2),
                        drop(iron_ore, 1),
                        drop(grain, 1),
                        gems(1, underground = true),
                    ),
            )
        }
    }

internal val skeletonArmedDrops =
    with(extra_objs) {
        with(drop_objs) {
            NpcDrops(
                always = listOf(bones),
                main =
                    table(
                        drop(iron_med_helm, 6),
                        drop(iron_sword, 4),
                        drop(iron_axe, 2),
                        drop(iron_scimitar, 1),
                        // UNVERIFIED: the wiki page shows both 12 and 15 for this line.
                        drop(air_rune, 3, 12),
                        drop(water_rune, 3, 9),
                        drop(chaos_rune, 3, 5),
                        drop(iron_arrow, 2, 12),
                        drop(law_rune, 2, 2),
                        drop(cosmic_rune, 1, 2),
                        herbs(20),
                        drop(objs.coins, 24, 10),
                        drop(objs.coins, 25, 5),
                        drop(objs.coins, 8, 25),
                        drop(objs.coins, 4, 45),
                        drop(objs.coins, 3, 65),
                        drop(objs.coins, 2, 1),
                        drop(bronze_bar, 5),
                        gems(2, underground = true),
                    ),
            )
        }
    }

internal val zombieLevel13Drops =
    with(extra_objs) {
        with(drop_objs) {
            NpcDrops(
                always = listOf(bones),
                main =
                    table(
                        drop(bronze_med_helm, 4),
                        drop(bronze_longsword, 1),
                        drop(iron_axe, 1),
                        drop(iron_arrow, 7, 5),
                        drop(body_rune, 5, 6),
                        drop(mind_rune, 5, 5),
                        drop(air_rune, 4, 13),
                        drop(iron_arrow, 4, 8),
                        drop(steel_arrow, 2, 5),
                        drop(nature_rune, 1, 6),
                        herbs(25),
                        drop(objs.coins, 11, 10),
                        drop(objs.coins, 4, 4),
                        drop(objs.coins, 3, 18),
                        drop(objs.coins, 2, 13),
                        drop(objs.coins, 2, 28),
                        drop(fishing_bait, 37, 5),
                        drop(copper_ore, 2),
                    ),
            )
        }
    }

internal val zombieLevel24Drops =
    with(extra_objs) {
        with(drop_objs) {
            NpcDrops(
                always = listOf(bones),
                main =
                    table(
                        drop(iron_mace, 3),
                        drop(iron_dagger, 2),
                        drop(bronze_kiteshield, 1),
                        drop(mithril_arrow, 3, 1),
                        drop(body_rune, 2, 3),
                        drop(air_rune, 3, 3),
                        drop(chaos_rune, 1, 4),
                        drop(cosmic_rune, 1, 2),
                        drop(fire_rune, 1, 7),
                        herbs(30),
                        drop(objs.coins, 10, 10),
                        drop(objs.coins, 21, 18),
                        drop(objs.coins, 8, 26),
                        drop(objs.coins, 6, 35),
                        drop(objs.coins, 2, 1),
                        drop(fishing_bait, 26, 7),
                        drop(tinderbox, 2),
                        drop(eye_of_newt, 1),
                        drop(tin_ore, 1),
                        gems(1, underground = true),
                    ),
            )
        }
    }

private val wizardDrops =
    with(extra_objs) {
        with(drop_objs) {
            NpcDrops(
                always = listOf(bones),
                main =
                    table(
                        drop(plainstaff, 8),
                        drop(blue_wizard_robe, 7),
                        drop(blue_wizard_hat, 3),
                        drop(chaos_rune, 8, 2),
                        drop(nature_rune, 8, 2),
                        drop(air_rune, 3, 5),
                        drop(body_rune, 3, 5),
                        drop(earth_rune, 3, 5),
                        drop(fire_rune, 3, 5),
                        drop(mind_rune, 3, 5),
                        drop(water_rune, 3, 5),
                        drop(air_rune, 2, 12),
                        drop(body_rune, 2, 12),
                        drop(earth_rune, 2, 12),
                        drop(fire_rune, 2, 12),
                        drop(mind_rune, 2, 12),
                        drop(water_rune, 2, 12),
                        members(blood_rune, 1, 2..2, f2p = always(mind_talisman)),
                        drop(law_rune, 1, 2),
                        drop(water_talisman, 3),
                        drop(mind_talisman, 3),
                        drop(objs.coins, 23, 1),
                        drop(objs.coins, 9, 2),
                        drop(objs.coins, 7, 18),
                        drop(objs.coins, 1, 30),
                    ),
            )
        }
    }

internal val lesserDemonDrops =
    with(extra_objs) {
        with(drop_objs) {
            NpcDrops(
                always = listOf(always(vile_ashes)),
                main =
                    table(
                        drop(steel_full_helm, 4),
                        drop(steel_axe, 4),
                        drop(steel_scimitar, 3),
                        drop(mithril_sq_shield, 1),
                        drop(mithril_chainbody, 1),
                        drop(rune_med_helm, 1),
                        drop(fire_rune, 8, 60),
                        drop(chaos_rune, 5, 12),
                        drop(death_rune, 3, 3),
                        drop(fire_rune, 1, 30),
                        herbs(1),
                        drop(objs.coins, 40, 120),
                        drop(objs.coins, 29, 40),
                        drop(objs.coins, 10, 200),
                        drop(objs.coins, 7, 10),
                        drop(objs.coins, 1, 450),
                        drop(jug_of_wine, 3),
                        drop(gold_ore, 2),
                        gems(4),
                    ),
            )
        }
    }

private val sourhogDrops =
    with(extra_objs) {
        with(drop_objs) {
            NpcDrops(
                always = listOf(always(big_bones)),
                main =
                    tableOf(
                        110,
                        drop(adamant_scimitar, 2),
                        drop(adamant_kiteshield, 1),
                        drop(mind_rune, 6, 3..10),
                        drop(air_rune, 6, 5..15),
                        drop(earth_rune, 6, 5..15),
                        drop(nature_rune, 4, 1..5),
                        drop(chaos_rune, 5, 1..5),
                        drop(steel_arrow, 5, 3..7),
                        Drop(table = herbs, weight = 10),
                        Drop(table = allotmentSeeds, weight = 15),
                        drop(objs.coins, 15, 5..30),
                        drop(limpwurt_root, 5),
                        drop(cabbage, 5),
                        drop(potato, 5),
                        drop(cooking_apple, 5),
                        rareTable(1),
                        gems(5, underground = true),
                    ),
            )
        }
    }

private val skeletalWyvernDrops =
    with(extra_objs) {
        with(drop_objs) {
            NpcDrops(
                always =
                    listOf(
                        always(wyvern_bones),
                        Drop(
                            table =
                                tableOf(
                                    512,
                                    members(granite_legs, 1),
                                    members(dragon_platelegs, 1),
                                    members(dragon_plateskirt, 1),
                                )
                        ),
                    ),
                main =
                    table(
                        drop(earth_battlestaff, 4),
                        drop(noted_battlestaff, 3, 10),
                        drop(rune_axe, 3),
                        drop(rune_battleaxe, 2),
                        drop(rune_warhammer, 2),
                        drop(rune_full_helm, 2),
                        drop(rune_kiteshield, 2),
                        drop(air_rune, 6, 225),
                        drop(rune_arrow, 5, 36),
                        drop(water_rune, 4, 150),
                        drop(chaos_rune, 4, 80),
                        drop(law_rune, 4, 45),
                        drop(death_rune, 4, 40),
                        drop(blood_rune, 4, 25),
                        drop(adamant_bolts, 3, 75..99),
                        drop(runite_bolts, 3, 35..44),
                        drop(soul_rune, 1, 20),
                        // Note: the wiki gives 1-3 herbs per roll; one roll of the table here.
                        Drop(table = herbs, weight = 7),
                        drop(noted_pure_essence, 8, 250),
                        drop(noted_magic_logs, 6, 35),
                        drop(noted_adamantite_bar, 6, 10),
                        drop(noted_iron_ore, 3, 200),
                        drop(noted_uncut_ruby, 2, 10),
                        drop(noted_uncut_diamond, 2, 5),
                        drop(objs.coins, 12, 300),
                        drop(lobster, 8, 6),
                        drop(prayer_potion_4, 7, 2),
                        drop(noted_unpowered_orb, 2, 75),
                        drop(runite_crossbow_u, 2),
                        drop(ranarr_seed, 2, 3),
                        drop(snapdragon_seed, 2, 1),
                        rareTable(3),
                    ),
            )
        }
    }

object draynor_npcs : NpcReferences() {
    val young_dark_wizard = find("young_dark_wizard")
    val black_knight = find("black_knight")
    val highwayman = find("highwayman")
    val jailguard = find("jailguard")
    val jailguard_two = find("jailguard_two_patrol")
    val jailguard_three = find("jailguard_three_patrol")
    val jailguard_four = find("jailguard_four_patrol")
    val market_guard = find("farming_market_guard")
    val house_ghost = find("house_ghost")
    val house_ghost2 = find("house_ghost2")
    val house_ghost3 = find("house_ghost3")
    val house_ghost4 = find("house_ghost4")
    val house_ghost5 = find("house_ghost5")
    val draynor_skeleton = find("draynor_skeleton")

    val skeleton_unarmed = find("skeleton_unarmed")
    val skeleton_unarmed2 = find("skeleton_unarmed2")
    val skeleton_unarmed3 = find("skeleton_unarmed3")
    val skeleton_unarmed4 = find("skeleton_unarmed4")
    val skeleton_unaggressive = find("skeleton_unagressive")
    val skeleton_unaggressive2 = find("skeleton_unagressive2")
    val skeleton_unaggressive3 = find("skeleton_unagressive3")
    val skeleton_armed = find("skeleton_armed")
    val skeleton_armed2 = find("skeleton_armed2")
    val skeleton_armed3 = find("skeleton_armed3")
    val skeleton_armed4 = find("skeleton_armed4")
    val skeleton_armed5 = find("skeleton_armed5")

    val zombie_unarmed_ids =
        listOf(
            find("zombie_unarmed"),
            find("zombie_unarmed2"),
            find("zombie_unarmed3"),
            find("zombie_unarmed4"),
            find("zombie_unarmed5"),
            find("zombie_unarmed6"),
            find("zombie_unarmed_city1"),
            find("zombie_unarmed_city2"),
            find("zombie_unarmed_city3"),
            find("zombie_unarmed_city4"),
            find("zombie_unarmed_city5"),
            find("zombie_unarmed_city6"),
            find("zombie_unarmed_sewer1"),
            find("zombie_unarmed_sewer2"),
            find("zombie_unarmed_sewer3"),
            find("zombie_unarmed_sewer4"),
        )
    val zombie2 = find("zombie2")
    val zombie2_b = find("zombie2_b")
    val zombie2_c = find("zombie2_c")
    val zombie_armed_ids =
        listOf(
            find("zombie_armed"),
            find("zombie_armed2"),
            find("zombie_armed3"),
            find("zombie_armed_city1"),
            find("zombie_armed_city2"),
            find("zombie_armed_city3"),
            find("zombie_armed_sewer1"),
            find("zombie_armed_sewer2"),
            find("zombie_armed_sewer3"),
            find("zombie_armed_sewer4"),
        )

    val wizard = find("wizard")
    val lesser_demon = find("lesser_demon")
    val sourhog = find("sourhog")
    val skeletal_wyvern1 = find("skeletal_wyvern1")
    val skeletal_wyvern2 = find("skeletal_wyvern2")
    val skeletal_wyvern3 = find("skeletal_wyvern3")
    val skeletal_wyvern4 = find("skeletal_wyvern4")
}
