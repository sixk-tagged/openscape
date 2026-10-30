package org.rsmod.content.other.npcdrops

import jakarta.inject.Inject
import org.rsmod.api.config.refs.objs
import org.rsmod.api.death.Drop
import org.rsmod.api.death.NpcDropTables
import org.rsmod.api.death.NpcDrops
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.type.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Drop tables (OSRS wiki, Oct 2025) for the Edgeville, Barbarian Village, Ice Mountain, Goblin
 * Village and Black Knights' Fortress monsters, and for the Edgeville Dungeon's hill giants and
 * hobgoblins. The dungeon's rats, spiders, skeletons and zombies use [DraynorDrops]'s tables, the
 * men, imps and unicorns [LumbridgeDrops]'s. Monks, fortress guards and rams drop only bones.
 */
class EdgevilleDrops @Inject constructor(private val tables: NpcDropTables) : PluginScript() {
    override fun ScriptContext.startup() {
        with(edgeville_drop_npcs) {
            hill_giants.forEach { tables[it] = hillGiantDrops }
            tables[hobgoblin_unarmed] = hobgoblinUnarmedDrops
            tables[hobgoblin_armed] = hobgoblinArmedDrops

            low_barbarians.forEach { tables[it] = barbarianDrops }
            high_barbarians.forEach { tables[it] = barbarianHighDrops }
            red_goblins.forEach { tables[it] = redGoblinDrops }
            green_goblins.forEach { tables[it] = greenGoblinDrops }
            // The armed goblins east of the village use the Lumbridge level 5 table; the first of
            // them is set by VarrockDrops.
            listOf(goblin_armed_2, goblin_armed_3, goblin_armed_4).forEach {
                tables[it] = goblinLevel5Drops
            }

            outlaws.forEach { tables[it] = outlawDrops }
            dwarf_workers.forEach { tables[it] = dwarfDrops }
            tables[icefiend] = icefiendDrops
            // The Chaos Temple's Monks of Zamorak share Varrock's table.
            chaos_temple_monks.forEach { tables[it] = monkDrops }
            tables[guard] = guardDrops
            listOf(black_knight, black_knight_kr).forEach { tables[it] = blackKnightDrops }
        }
    }
}

/**
 * The giant key is a pre-roll before the table; the herb, seed and gem sub-tables take 7, 18 and 3
 * of its 128.
 */
private val hillGiantDrops =
    with(extra_objs) {
        with(drop_objs) {
            with(edgeville_drop_objs) {
                NpcDrops(
                    always =
                        listOf(always(big_bones), Drop(table = tableOf(128, drop(giant_key, 1)))),
                    main =
                        table(
                            drop(iron_dagger, 4),
                            drop(iron_full_helm, 5),
                            drop(iron_kiteshield, 3),
                            drop(steel_longsword, 2),
                            drop(iron_arrow, 6, 3),
                            drop(fire_rune, 3, 15),
                            drop(water_rune, 3, 7),
                            drop(law_rune, 3, 2),
                            drop(steel_arrow, 2, 10),
                            drop(mind_rune, 2, 3),
                            drop(cosmic_rune, 2, 2),
                            drop(nature_rune, 2, 6),
                            drop(chaos_rune, 1, 2),
                            drop(death_rune, 1, 2),
                            drop(objs.coins, 14, 38),
                            drop(objs.coins, 10, 52),
                            drop(objs.coins, 8, 15),
                            drop(objs.coins, 6, 8),
                            drop(objs.coins, 2, 88),
                            drop(limpwurt_root, 11),
                            drop(beer, 6),
                            drop(body_talisman, 2),
                            herbs(7),
                            generalSeeds(18),
                            gems(3, underground = true),
                        ),
                )
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
                            gems(2, underground = true),
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
                            gems(2, underground = true),
                        ),
                )
            }
        }
    }

/** The lv 15 and 17 barbarians and Gunthor; the lv 9 and 10 ones use [barbarianDrops]. */
private val barbarianHighDrops =
    with(extra_objs) {
        with(drop_objs) {
            with(edgeville_drop_objs) {
                NpcDrops(
                    always = listOf(bones),
                    main =
                        table(
                            drop(iron_axe, 6),
                            drop(bronze_battleaxe, 4),
                            drop(iron_mace, 1),
                            drop(bronze_arrow, 4, 10),
                            drop(chaos_rune, 4, 3),
                            drop(iron_arrow, 3, 8),
                            drop(earth_rune, 3, 5),
                            drop(mind_rune, 2, 10),
                            drop(fire_rune, 2, 8),
                            drop(law_rune, 1, 2),
                            drop(objs.coins, 42, 8),
                            drop(objs.coins, 9, 12),
                            drop(objs.coins, 5, 25),
                            drop(objs.coins, 3, 32),
                            drop(cooked_meat, 1),
                            drop(tin_ore, 1),
                            drop(amulet_mould, 1),
                            drop(beer, 1),
                            drop(bear_fur, 1),
                            drop(flyer, 1),
                            gems(1),
                        ),
                )
            }
        }
    }

private val redGoblinDrops = villageGoblinDrops(edgeville_drop_objs.red_mail)

private val greenGoblinDrops = villageGoblinDrops(edgeville_drop_objs.green_mail)

/** The Goblin Village goblins wear coloured goblin mail, which replaces the plain kind. */
private fun villageGoblinDrops(mail: ObjType) =
    with(drop_objs) {
        NpcDrops(
            always = listOf(bones),
            main =
                table(
                    drop(bronze_axe, 3),
                    drop(bronze_scimitar, 1),
                    members(bronze_spear, 9),
                    drop(bronze_arrow, 3, 7),
                    drop(mind_rune, 3, 2),
                    drop(earth_rune, 3, 4),
                    drop(body_rune, 3, 2),
                    members(bronze_javelin, 2, 5..5),
                    drop(chaos_rune, 1),
                    drop(nature_rune, 1),
                    herbs(2),
                    drop(objs.coins, 34, 1),
                    drop(objs.coins, 13, 3),
                    drop(objs.coins, 8, 5),
                    drop(objs.coins, 7, 16),
                    drop(objs.coins, 3, 24),
                    drop(hammer, 9),
                    members(goblin_book, 2),
                    drop(mail, 10),
                    drop(grapes, 1),
                    drop(red_cape, 1),
                    drop(tin_ore, 1),
                ),
        )
    }

private val outlawDrops =
    with(extra_objs) {
        with(drop_objs) {
            NpcDrops(
                always = listOf(bones),
                main =
                    table(
                        drop(bronze_med_helm, 2),
                        drop(mind_rune, 2, 9),
                        drop(water_rune, 2, 6),
                        drop(earth_rune, 2, 5),
                        drop(objs.coins, 12, 5),
                        drop(objs.coins, 4, 15),
                        drop(objs.coins, 1, 25),
                        drop(rope, 46),
                        drop(fishing_bait, 15),
                        drop(copper_ore, 2),
                        drop(cabbage, 6),
                        drop(knife, 1),
                        herbs(32),
                    ),
            )
        }
    }

private val dwarfDrops =
    with(extra_objs) {
        with(drop_objs) {
            with(edgeville_drop_objs) {
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
                            gems(1),
                        ),
                )
            }
        }
    }

/** Icefiends leave no bones. */
private val icefiendDrops =
    with(extra_objs) {
        NpcDrops(
            main =
                table(
                    drop(air_rune, 1, 1..5),
                    drop(drop_objs.water_rune, 4, 1..5),
                    drop(objs.coins, 4, 1..30),
                    drop(objs.coins, 4, 1..20),
                    gems(3),
                )
        )
    }

object edgeville_drop_objs : ObjReferences() {
    val giant_key = find("hillgiant_boss_key")
    val iron_kiteshield = find("iron_kiteshield")
    val steel_longsword = find("steel_longsword")
    val steel_dagger = find("steel_dagger")
    val bronze_battleaxe = find("bronze_battleaxe")
    val iron_battleaxe = find("iron_battleaxe")
    val amulet_mould = find("amulet_mould")
    val iron_spear = find("iron_spear")
    val steel_spear = find("steel_spear")
    val iron_javelin = find("iron_javelin")
    val bronze_pickaxe = find("bronze_pickaxe")
    val iron_bar = find("iron_bar")
    val red_mail = find("goblin_armour_red")
    val green_mail = find("goblin_armour_green")
}

object edgeville_drop_npcs : NpcReferences() {
    val hill_giants =
        listOf(
            find("giant"),
            find("giant2"),
            find("giant3"),
            find("giant4"),
            find("giant5"),
            find("giant6"),
        )
    val hobgoblin_unarmed = find("hobgoblin_unarmed")
    val hobgoblin_armed = find("hobgoblin_armed")

    // Lv 9 and 10: Sieglinde and the other barbarians with 18 or 20 hitpoints.
    val low_barbarians =
        listOf(
            find("fai_barbarian_2"),
            find("fai_barbarian_5"),
            find("fai_barbarian_6"),
            find("fai_barbarian_7"),
            find("fai_barbarian_10"),
            find("fai_barbarian_11"),
            find("fai_barbarian_12"),
            find("fai_barbarian_13"),
            find("fai_barbarian_female_2"),
            find("fai_barbarian_female_3"),
            find("fai_barbarian_female_4"),
        )

    // Lv 15 (Aitan) and 17, and Gunthor the Brave.
    val high_barbarians =
        listOf(
            find("fai_barbarian_1"),
            find("fai_barbarian_3"),
            find("fai_barbarian_4"),
            find("fai_barbarian_8"),
            find("fai_barbarian_14"),
            find("fai_barbarian_female_1"),
            find("gunthor_the_brave"),
        )
    val red_goblins =
        listOf(
            find("goblin_red_soldier_2"),
            find("goblin_red_soldier_3"),
            find("goblin_red_soldier_4"),
            find("goblin_red_soldier_5"),
            find("goblin_red_soldier_6"),
            find("goblin_red_soldier_7"),
            find("goblin_red_soldier_8"),
        )
    val green_goblins =
        listOf(
            find("goblin_green_soldier_2"),
            find("goblin_green_soldier_3"),
            find("goblin_green_soldier_4"),
            find("goblin_green_soldier_5"),
            find("goblin_green_soldier_7"),
            find("goblin_green_soldier_8"),
        )
    val goblin_armed_2 = find("fai_barbarian_goblin_armed_2")
    val goblin_armed_3 = find("fai_barbarian_goblin_armed_3")
    val goblin_armed_4 = find("fai_barbarian_goblin_armed_4")
    val outlaws =
        listOf(
            find("surok_outlaw1"),
            find("surok_outlaw2"),
            find("surok_outlaw3"),
            find("surok_outlaw4"),
            find("surok_outlaw5"),
            find("surok_outlaw6"),
            find("surok_outlaw7"),
            find("surok_outlaw8"),
            find("surok_outlaw9"),
            find("surok_outlaw10"),
        )
    val dwarf_workers =
        listOf(
            find("fai_dwarf_worker_05"),
            find("fai_dwarf_worker_06"),
            find("fai_dwarf_worker_07"),
            find("fai_dwarf_worker_08"),
        )
    val icefiend = find("slayer_icefiend_1")
    val chaos_temple_monks = listOf(find("chaos_temple_monk1"), find("chaos_temple_monk2"))
    val guard = find("guard1")
    val black_knight = find("aggressive_black_knight")
    val black_knight_kr = find("kr_aggressive_black_knight")
}
