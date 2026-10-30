package org.rsmod.content.other.npcdrops

import jakarta.inject.Inject
import org.rsmod.api.config.refs.objs
import org.rsmod.api.death.NpcDropTables
import org.rsmod.api.death.NpcDrops
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.type.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Drop tables (OSRS wiki, members rates, at the 2025-09-10 cache date) for the Falador monsters:
 * the town, the farms, the White Knights' Castle, the Dwarven Mine and the Dark Wizards' Tower.
 * Guards, men, dark wizards, chickens and cows share the other areas' tables.
 */
class FaladorDrops @Inject constructor(private val tables: NpcDropTables) : PluginScript() {
    override fun ScriptContext.startup() {
        with(falador_npcs) {
            tables[white_knight] = whiteKnightLevel36Drops
            tables[white_knight_yellow] = whiteKnightLevel38Drops
            tables[white_knight_green] = whiteKnightLevel39Drops
            tables[white_knight_blue] = whiteKnightLevel42Drops

            tables[dwarf] = dwarfDrops(underground = true)
            listOf(
                    dwarf_lv11,
                    dwarf_lv11_2,
                    dwarf_lv10,
                    dwarf_worker1,
                    dwarf_worker2,
                    dwarf_worker3,
                    dwarf_worker4,
                    black_guard1,
                    black_guard2,
                    black_guard3,
                    black_guard4,
                )
                .forEach { tables[it] = dwarfDrops(underground = false) }

            // `guard1` (the south gate) is registered by EdgevilleDrops.
            listOf(guard2, guard3, guard4, guard5, guard6, guard7, doric_guard).forEach {
                tables[it] = guardDrops
            }
            listOf(man, drunken_man, man2, man3, cuffs, narf, rusty, jeff).forEach {
                tables[it] = manDrops
            }
            listOf(young_dark_wizard, young_dark_wizard2).forEach {
                tables[it] = darkWizardLowDrops
            }
            listOf(old_dark_wizard, old_dark_wizard2).forEach { tables[it] = darkWizardHighDrops }

            tables[air_wizard] = elementalWizardDrops(extra_objs.air_rune, drop_objs.air_talisman)
            tables[water_wizard] =
                elementalWizardDrops(drop_objs.water_rune, extra_objs.water_talisman)
            tables[earth_wizard] =
                elementalWizardDrops(drop_objs.earth_rune, drop_objs.earth_talisman)
            tables[fire_wizard] =
                elementalWizardDrops(drop_objs.fire_rune, extra_objs.fire_talisman)

            listOf(gardener, gardener2, gang_dwarf, gang_dwarf2, gang_dwarf3, hooded_highwayman)
                .forEach { tables[it] = bonesOnly }
            // King Scorpions leave no bones; their only drops are tertiaries (ensouled head, clue).
            tables[king_scorpion] = NpcDrops()

            listOf(farm_chicken, farm_chicken2, farm_chicken3).forEach { tables[it] = chickenDrops }
            tables[calf] = cowDrops
        }
    }
}

private val whiteKnightLevel36Drops =
    with(extra_objs) {
        with(drop_objs) {
            with(falador_drop_objs) {
                NpcDrops(
                    always = listOf(bones),
                    main =
                        table(
                            drop(iron_longsword, 2),
                            drop(steel_sword, 1),
                            drop(steel_med_helm, 1),
                            drop(mind_rune, 11, 5),
                            drop(nature_rune, 4, 4),
                            drop(body_rune, 3, 9..13),
                            drop(chaos_rune, 3, 2),
                            drop(water_rune, 3, 25..30),
                            drop(mithril_arrow, 2, 5),
                            drop(adamant_arrow, 1, 2),
                            members(blood_rune, 1, 2..2),
                            drop(law_rune, 1, 2),
                            herbs(5),
                            generalSeeds(15),
                            gems(1),
                            drop(iron_bar, 6, 2),
                            drop(iron_bar, 2, 1),
                            drop(half_apple_pie, 1),
                            drop(iron_ore, 1),
                            drop(pot_of_flour, 1),
                            drop(objs.coins, 15, 48),
                            drop(objs.coins, 15, 15),
                            drop(objs.coins, 10, 8),
                            drop(objs.coins, 10, 45..53),
                            drop(objs.coins, 5, 1),
                            drop(objs.coins, 5, 2),
                            drop(objs.coins, 1, 120),
                        ),
                )
            }
        }
    }

private val whiteKnightLevel38Drops =
    with(extra_objs) {
        with(drop_objs) {
            with(falador_drop_objs) {
                NpcDrops(
                    always = listOf(bones),
                    main =
                        table(
                            drop(iron_longsword, 2),
                            drop(steel_sword, 1),
                            drop(steel_med_helm, 2),
                            drop(mind_rune, 8, 7),
                            drop(nature_rune, 4, 5),
                            drop(body_rune, 3, 10..14),
                            drop(chaos_rune, 3, 3),
                            drop(water_rune, 3, 30..35),
                            drop(mithril_arrow, 5, 5),
                            drop(adamant_arrow, 3, 2),
                            members(blood_rune, 1, 2..2),
                            drop(law_rune, 1, 3),
                            herbs(7),
                            generalSeeds(18),
                            gems(2),
                            drop(iron_bar, 8, 2),
                            drop(iron_bar, 2, 1),
                            drop(half_apple_pie, 1),
                            drop(iron_ore, 1),
                            drop(pot_of_flour, 1),
                            drop(objs.coins, 13, 15),
                            drop(objs.coins, 10, 10),
                            drop(objs.coins, 10, 54),
                            drop(objs.coins, 10, 50..58),
                            drop(objs.coins, 6, 3),
                            drop(objs.coins, 1, 1),
                            drop(objs.coins, 1, 120),
                        ),
                )
            }
        }
    }

private val whiteKnightLevel39Drops =
    with(extra_objs) {
        with(drop_objs) {
            with(falador_drop_objs) {
                NpcDrops(
                    always = listOf(bones),
                    main =
                        table(
                            drop(iron_longsword, 2),
                            drop(steel_sword, 2),
                            drop(steel_med_helm, 2),
                            drop(mind_rune, 8, 10),
                            drop(nature_rune, 4, 6),
                            drop(body_rune, 3, 15..19),
                            drop(chaos_rune, 3, 4),
                            drop(water_rune, 3, 35..40),
                            drop(mithril_arrow, 5, 7),
                            drop(adamant_arrow, 3, 4),
                            members(blood_rune, 1, 3..3),
                            drop(law_rune, 1, 3),
                            herbs(7),
                            generalSeeds(18),
                            gems(2),
                            drop(iron_bar, 6, 2),
                            drop(iron_bar, 2, 1),
                            drop(half_apple_pie, 1),
                            drop(iron_ore, 1),
                            drop(pot_of_flour, 1),
                            drop(objs.coins, 13, 15..19),
                            drop(objs.coins, 10, 15),
                            drop(objs.coins, 10, 55..63),
                            drop(objs.coins, 10, 50..69),
                            drop(objs.coins, 6, 5),
                            drop(objs.coins, 2, 120),
                            drop(objs.coins, 1, 1),
                        ),
                )
            }
        }
    }

private val whiteKnightLevel42Drops =
    with(extra_objs) {
        with(drop_objs) {
            with(falador_drop_objs) {
                NpcDrops(
                    always = listOf(bones),
                    main =
                        table(
                            drop(iron_longsword, 2),
                            drop(steel_sword, 2),
                            drop(steel_med_helm, 2),
                            drop(mind_rune, 3, 12),
                            drop(nature_rune, 4, 7),
                            drop(body_rune, 3, 15..24),
                            drop(chaos_rune, 3, 5),
                            drop(water_rune, 3, 40..47),
                            drop(mithril_arrow, 5, 9),
                            drop(adamant_arrow, 3, 5),
                            members(blood_rune, 1, 3..3),
                            drop(law_rune, 1, 3),
                            herbs(7),
                            generalSeeds(28),
                            gems(2),
                            drop(iron_bar, 5, 2),
                            drop(iron_bar, 2, 1),
                            drop(half_apple_pie, 1),
                            drop(iron_ore, 1),
                            drop(pot_of_flour, 1),
                            drop(objs.coins, 13, 51..55),
                            drop(objs.coins, 10, 15),
                            drop(objs.coins, 10, 65..73),
                            drop(objs.coins, 6, 8),
                            drop(objs.coins, 5, 50..69),
                            drop(objs.coins, 3, 120),
                            drop(objs.coins, 1, 1),
                        ),
                )
            }
        }
    }

/**
 * The "Regular" dwarf table, which the Black Guards share; [underground] picks the gem table's
 * talisman (the mine's dwarves vs the town, Ice Mountain and Black Guard ones).
 */
private fun dwarfDrops(underground: Boolean) =
    with(extra_objs) {
        with(drop_objs) {
            with(falador_drop_objs) {
                NpcDrops(
                    always = listOf(bones),
                    main =
                        table(
                            drop(bronze_pickaxe, 13),
                            drop(bronze_med_helm, 4),
                            drop(bronze_battleaxe, 2),
                            drop(iron_battleaxe, 1),
                            members(bronze_bolts, 7, 2..12),
                            drop(chaos_rune, 4, 2),
                            drop(nature_rune, 4, 2),
                            drop(objs.coins, 20, 4),
                            drop(objs.coins, 15, 10),
                            drop(objs.coins, 2, 30),
                            drop(hammer, 10),
                            drop(bronze_bar, 7),
                            drop(iron_ore, 4),
                            drop(tin_ore, 3),
                            drop(copper_ore, 3),
                            drop(iron_bar, 3),
                            drop(coal, 2),
                            gems(1, underground = underground),
                        ),
                )
            }
        }
    }

/** Ashes, then one roll: the wizard's rune (19/20) or its talisman (1/20). */
private fun elementalWizardDrops(rune: ObjType, talisman: ObjType) =
    NpcDrops(
        always = listOf(always(drop_objs.ashes)),
        main = tableOf(20, drop(rune, 19, 5..10), drop(talisman, 1)),
    )

object falador_drop_objs : ObjReferences() {
    val iron_longsword = find("iron_longsword")
    val steel_sword = find("steel_sword")
    val adamant_arrow = find("adamant_arrow")
    val half_apple_pie = find("half_an_apple_pie")
    val iron_bar = find("iron_bar")
    val bronze_pickaxe = find("bronze_pickaxe")
    val bronze_battleaxe = find("bronze_battleaxe")
    val iron_battleaxe = find("iron_battleaxe")
}

object falador_npcs : NpcReferences() {
    val white_knight = find("white_knight")
    val white_knight_yellow = find("white_knight_yellow_plumes")
    val white_knight_green = find("white_knight_green_plumes")
    val white_knight_blue = find("white_knight_blue_plumes")

    val dwarf = find("dwarf_normal")
    val dwarf_lv11 = find("fai_falador_dwarf_normal1")
    val dwarf_lv11_2 = find("fai_falador_dwarf_normal2")
    val dwarf_lv10 = find("fai_falador_dwarf_normal3")
    val dwarf_worker1 = find("fai_dwarf_worker_01")
    val dwarf_worker2 = find("fai_dwarf_worker_02")
    val dwarf_worker3 = find("fai_dwarf_worker_03")
    val dwarf_worker4 = find("fai_dwarf_worker_04")
    val black_guard1 = find("fai_black_guard_01")
    val black_guard2 = find("fai_black_guard_02")
    val black_guard3 = find("fai_black_guard_03")
    val black_guard4 = find("fai_black_guard_04")

    val guard2 = find("fai_falador_guard1")
    val guard3 = find("fai_falador_guard2")
    val guard4 = find("fai_falador_guard3")
    val guard5 = find("fai_falador_guard4")
    val guard6 = find("fai_falador_guard5")
    val guard7 = find("fai_falador_guard6")
    val doric_guard = find("falador_doric_area_guard")

    val man = find("man4")
    val drunken_man = find("falador_man1")
    val man2 = find("falador_man2")
    val man3 = find("falador_man3")
    val cuffs = find("falador_doric_area_man1")
    val narf = find("falador_doric_area_man2")
    val rusty = find("falador_doric_area_man3")
    val jeff = find("falador_doric_area_man4")

    val young_dark_wizard = find("fai_dark_wizard_young_1")
    val young_dark_wizard2 = find("fai_dark_wizard_young_2")
    val old_dark_wizard = find("fai_dark_wizard_old_1")
    val old_dark_wizard2 = find("fai_dark_wizard_old_2")

    val air_wizard = find("air_wizard")
    val water_wizard = find("water_wizard")
    val earth_wizard = find("earth_wizard")
    val fire_wizard = find("fire_wizard")

    val gardener = find("falador_gardener")
    val gardener2 = find("falador_gardener2")
    val gang_dwarf = find("favour_gangster_dwarf")
    val gang_dwarf2 = find("favour_gangster_dwarf_2")
    val gang_dwarf3 = find("favour_gangster_dwarf_3")
    val hooded_highwayman = find("highwayman2")
    val king_scorpion = find("kingscorpion")

    val farm_chicken = find("farm_chicken_brown")
    val farm_chicken2 = find("farm_chicken_dark_brown")
    val farm_chicken3 = find("farm_chicken_tan")
    val calf = find("calf")
}
