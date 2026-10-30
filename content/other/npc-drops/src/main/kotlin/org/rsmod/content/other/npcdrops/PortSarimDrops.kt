package org.rsmod.content.other.npcdrops

import jakarta.inject.Inject
import org.rsmod.api.config.refs.objs
import org.rsmod.api.death.NpcDropTables
import org.rsmod.api.death.NpcDrops
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Drop tables (OSRS wiki, Oct 2025) for the monsters of Port Sarim, Rimmington, Musa Point and the
 * Karamja Volcano's dungeon. The goblins, men, imps, scorpions, skeletons and the first lesser
 * demon use the tables [LumbridgeDrops], [DraynorDrops] and [VarrockDrops] give their types.
 */
class PortSarimDrops @Inject constructor(private val tables: NpcDropTables) : PluginScript() {
    override fun ScriptContext.startup() {
        with(port_sarim_drop_npcs) {
            hobgoblins.forEach { tables[it] = hobgoblinUnarmedDrops }
            tables[hobgoblin_armed] = hobgoblinArmedDrops
            tables[pirate] = pirateDrops
            jail_guards.forEach { tables[it] = guardDrops }
            tables[jail_black_knight] = bonesOnly
            // His only drop is a map part of the Dragon Slayer quest.
            tables[wormbrain] = NpcDrops()
            tables[monkey] = NpcDrops(always = listOf(always(port_sarim_drop_objs.monkey_bones)))
            tables[bat] = NpcDrops(always = listOf(always(port_sarim_drop_objs.bat_bones)))

            humans.forEach { tables[it] = manDrops }
            tables[jail_mugger] = muggerDrops
            // The wiki gives these armed goblins the level 2 goblin table.
            armed_goblins.forEach { tables[it] = goblinLevel2Drops }

            lesser_demons.forEach { tables[it] = lesserDemonDrops }
        }
    }
}

private val hobgoblinUnarmedDrops =
    with(extra_objs) {
        with(drop_objs) {
            with(edgeville_drop_objs) {
                NpcDrops(
                    always = listOf(bones),
                    main =
                        table(
                            members(bronze_spear, 3),
                            drop(iron_sword, 3),
                            drop(steel_dagger, 3),
                            members(iron_spear, 2),
                            members(steel_spear, 2),
                            drop(steel_longsword, 1),
                            drop(law_rune, 3, 2),
                            drop(water_rune, 2, 2),
                            drop(fire_rune, 2, 7),
                            drop(body_rune, 2, 6),
                            drop(chaos_rune, 2, 3),
                            drop(nature_rune, 2, 4),
                            drop(cosmic_rune, 1, 2),
                            drop(objs.coins, 16, 15),
                            drop(objs.coins, 12, 28),
                            drop(objs.coins, 12, 5),
                            drop(objs.coins, 4, 62),
                            drop(objs.coins, 3, 42),
                            drop(objs.coins, 1, 1),
                            drop(limpwurt_root, 22),
                            drop(goblin_mail, 2),
                            herbs(7),
                            generalSeeds(18),
                            gems(2),
                        ),
                )
            }
        }
    }

private val hobgoblinArmedDrops =
    with(extra_objs) {
        with(drop_objs) {
            with(edgeville_drop_objs) {
                NpcDrops(
                    always = listOf(bones),
                    main =
                        table(
                            drop(iron_sword, 3),
                            drop(steel_dagger, 3),
                            drop(steel_longsword, 1),
                            drop(law_rune, 3, 2),
                            drop(water_rune, 2, 2),
                            drop(fire_rune, 2, 7),
                            drop(body_rune, 2, 6),
                            drop(chaos_rune, 2, 3),
                            drop(nature_rune, 2, 4),
                            drop(cosmic_rune, 1, 2),
                            members(iron_javelin, 1, 5..5),
                            drop(objs.coins, 34, 15),
                            drop(objs.coins, 12, 5),
                            drop(objs.coins, 4, 28),
                            drop(objs.coins, 4, 62),
                            drop(objs.coins, 3, 42),
                            members(objs.coins, 3, 1..1),
                            drop(objs.coins, 1, 1),
                            drop(limpwurt_root, 21),
                            drop(goblin_mail, 2),
                            herbs(7),
                            uncommonSeeds(12),
                            gems(2),
                        ),
                )
            }
        }
    }

private val pirateDrops =
    with(extra_objs) {
        with(drop_objs) {
            with(edgeville_drop_objs) {
                with(port_sarim_drop_objs) {
                    NpcDrops(
                        always = listOf(bones),
                        main =
                            table(
                                drop(iron_dagger, 6),
                                drop(bronze_scimitar, 4),
                                drop(iron_platebody, 1),
                                members(iron_bolts, 10, 2..12),
                                drop(chaos_rune, 6, 2),
                                drop(nature_rune, 5, 2),
                                drop(bronze_arrow, 3, 9),
                                drop(bronze_arrow, 2, 12),
                                drop(air_rune, 2, 10),
                                drop(earth_rune, 2, 9),
                                drop(fire_rune, 2, 5),
                                drop(law_rune, 1, 2),
                                drop(objs.coins, 29, 4),
                                drop(objs.coins, 13, 25),
                                drop(objs.coins, 8, 7),
                                drop(objs.coins, 6, 12),
                                drop(objs.coins, 4, 35),
                                drop(objs.coins, 1, 55),
                                members(eye_patch, 12),
                                drop(chefs_hat, 1),
                                drop(iron_bar, 1),
                                gems(1),
                            ),
                    )
                }
            }
        }
    }

object port_sarim_drop_objs : ObjReferences() {
    val iron_platebody = find("iron_platebody")
    val eye_patch = find("eye_patch")
    val monkey_bones = find("mm_normal_monkey_bones")
    val bat_bones = find("bat_bones")
}

object port_sarim_drop_npcs : NpcReferences() {
    val hobgoblins =
        listOf(
            find("rimmington_hobgoblin_unarmed_1"),
            find("rimmington_hobgoblin_unarmed_2"),
            find("rimmington_hobgoblin_unarmed_3"),
        )
    val hobgoblin_armed = find("rimmington_hobgoblin_armed_1")
    val pirate = find("jail_pirate")
    val jail_guards =
        listOf(
            find("jail_guard_1"),
            find("jail_guard_2"),
            find("jail_guard_3"),
            find("jail_guard_4"),
            find("jail_guard_5"),
        )
    val jail_black_knight = find("jail_black_knight")
    val jail_mugger = find("jail_mugger")
    val wormbrain = find("wormbrain")
    val monkey = find("monkey")
    val bat = find("bat")

    // The Human table: the Musa Point men, Rimmington's pair and the jail and town thieves.
    val humans =
        listOf(
            find("man4_for_musa_point"),
            find("karamja_man"),
            find("rimmington_anja"),
            find("rimmington_hengel"),
            find("jail_thief"),
            find("thief2"),
        )
    val armed_goblins =
        listOf(
            find("goblin_armed_melee_1"),
            find("goblin_armed_melee_2"),
            find("goblin_armed_melee_3"),
            find("goblin_armed_melee_4"),
        )

    // The level 82 lesser demons of the volcano dungeon, which use the Draynor table.
    val lesser_demons =
        listOf(
            find("lesser_demon2"),
            find("lesser_demon3"),
            find("lesser_demon4"),
            find("lesser_demon5"),
        )
}
