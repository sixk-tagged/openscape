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
 * Drop tables (OSRS wiki, Sep 2025) for the Lumbridge Swamp Caves: cave bugs, cave slimes, big and
 * giant frogs, cave crawlers, rockslugs, wall beasts and cave goblins. Tertiaries (clues, quest
 * items) are left out.
 */
class SwampCaveDrops @Inject constructor(private val tables: NpcDropTables) : PluginScript() {
    override fun ScriptContext.startup() {
        with(swamp_drop_npcs) {
            tables[cave_bug] = caveBugDrops
            tables[cave_slime] = caveSlimeDrops
            tables[big_frog] = bigFrogDrops
            tables[giant_frog] = giantFrogDrops
            crawlers.forEach { tables[it] = caveCrawlerDrops }
            rockslugs.forEach { tables[it] = rockslugDrops }
            // The hole in the wall is the spawned npc; it fights as the wall beast.
            tables[wall_beast_hole] = wallBeastDrops
            tables[wall_beast] = wallBeastDrops
            cave_goblins.forEach { tables[it] = caveGoblinDrops }
            tables[superior_drop_npcs.cave_crawler] = superiorDrops(caveCrawlerDrops, 10)
            tables[superior_drop_npcs.rockslug] = superiorDrops(rockslugDrops, 20)
        }
    }
}

object swamp_drop_npcs : NpcReferences() {
    val cave_bug = find("swamp_cave_bug")
    val cave_slime = find("swamp_cave_slime")
    val big_frog = find("medium_frog")
    val giant_frog = find("giant_frog")
    val crawlers =
        listOf(
            find("slayer_cave_crawler_1"),
            find("slayer_cave_crawler_2"),
            find("slayer_cave_crawler_3"),
            find("slayer_cave_crawler_4"),
        )
    val rockslugs = listOf(find("slayer_rockslug"), find("slayer_rockslug_baby"))
    val wall_beast_hole = find("swamp_wallbeast")
    val wall_beast = find("swamp_wallbeast_combat")
    val cave_goblins =
        listOf(
            find("cave_goblin"),
            find("cave_goblin2"),
            find("cave_goblin3"),
            find("cave_goblin4"),
        )
}

private val caveBugDrops =
    with(swamp_drop_objs) {
        NpcDrops(
            main =
                table(
                    drop(earthrune, 2, 6),
                    drop(waterrune, 5, 8),
                    drop(naturerune, 5),
                    drop(naturerune, 1, 2),
                    Drop(table = herbs, weight = 23),
                    drop(unidentified_guam, 16),
                    drop(unicorn_horn_dust, 2),
                    drop(eye_of_newt, 2),
                    drop(limpwurt_root, 1),
                    drop(snape_grass, 1),
                    drop(red_spiders_eggs, 2),
                    drop(coins, 8, 3),
                    drop(coins, 3, 8),
                    drop(tinderbox, 3),
                    drop(candle_lantern_empty, 1),
                    drop(unlit_candle, 5),
                )
        )
    }

private val caveSlimeDrops =
    with(swamp_drop_objs) {
        NpcDrops(
            always = listOf(Drop(swamp_tar, 1..6)),
            main =
                table(
                    drop(bronze_axe, 3),
                    drop(iron_sword, 7),
                    drop(bronze_full_helm, 1),
                    drop(iron_kiteshield, 2),
                    drop(iron_armoured_boots, 1),
                    drop(waterrune, 5, 15),
                    drop(earthrune, 3, 5),
                    drop(coins, 7),
                    drop(coins, 30, 4),
                    drop(coins, 39, 10),
                    drop(coins, 10, 22),
                    drop(coins, 2, 46),
                    drop(torch_unlit, 11),
                    drop(oil_lantern_frame, 1),
                    drop(gold_bar, 2),
                    gems(4, underground = true),
                ),
        )
    }

private val bigFrogDrops =
    with(swamp_drop_objs) {
        NpcDrops(
            always = listOf(always(bones)),
            main =
                table(
                    drop(waterrune, 10, 12),
                    drop(earthrune, 10, 12),
                    drop(naturerune, 7, 4),
                    drop(cosmicrune, 6, 2),
                    drop(coins, 32, 5),
                    drop(coins, 19, 15),
                    drop(water_talisman, 1),
                    drop(earth_talisman, 1),
                ),
        )
    }

private val giantFrogDrops =
    with(swamp_drop_objs) {
        NpcDrops(
            always = listOf(always(big_bones)),
            main =
                table(
                    drop(mithril_spear, 2),
                    drop(naturerune, 10),
                    drop(naturerune, 10, 3),
                    drop(naturerune, 10, 9),
                    drop(bloodrune, 1),
                    drop(cosmicrune, 3, 5),
                    drop(iron_arrow, 2, 22),
                    drop(steel_arrow, 1, 45),
                    drop(coins, 8, 2),
                    drop(coins, 10, 30),
                    drop(coins, 5, 37),
                    drop(giant_frog_legs, 64),
                    drop(coal, 1),
                    drop(spinach_roll, 1),
                ),
        )
    }

private val caveCrawlerDrops =
    with(swamp_drop_objs) {
        NpcDrops(
            main =
                table(
                    drop(bronze_armoured_boots, 1),
                    drop(naturerune, 6, 3..4),
                    drop(firerune, 5, 12),
                    drop(earthrune, 2, 9),
                    Drop(table = allotmentSeeds, weight = 26),
                    Drop(table = herbs, weight = 22),
                    drop(vial_water, 13),
                    drop(white_berries, 5),
                    drop(unicorn_horn_dust, 2),
                    drop(eye_of_newt, 1),
                    drop(red_spiders_eggs, 1),
                    drop(limpwurt_root, 1),
                    drop(snape_grass, 1),
                    drop(coins, 5, 3),
                    drop(coins, 3, 8),
                    drop(coins, 3, 29),
                    drop(coins, 1, 10),
                    gems(1, underground = true),
                )
        )
    }

private val rockslugDrops =
    with(swamp_drop_objs) {
        NpcDrops(
            main =
                tableOf(
                    512,
                    drop(mystic_gloves_light, 1),
                    drop(earthrune, 120, 5),
                    drop(earthrune, 16, 42),
                    drop(chaosrune, 16, 2),
                    drop(coal, 52),
                    drop(iron_ore, 88),
                    drop(bronze_bar, 8),
                    drop(iron_bar, 12),
                    drop(copper_ore, 12),
                    drop(tin_ore, 32),
                    drop(mithril_ore, 4),
                    drop(dwarven_stout, 52),
                    drop(hammer, 40),
                    gems(24, underground = true),
                )
        )
    }

private val wallBeastDrops =
    with(swamp_drop_objs) {
        NpcDrops(
            main =
                tableOf(
                    512,
                    drop(airrune, 12, 3),
                    drop(cosmicrune, 12, 2),
                    drop(chaosrune, 8, 3),
                    drop(chaosrune, 4, 7),
                    drop(bronze_med_helm, 8),
                    drop(bronze_full_helm, 8),
                    drop(iron_med_helm, 32),
                    drop(steel_med_helm, 28),
                    drop(steel_full_helm, 16),
                    drop(mithril_med_helm, 4),
                    drop(mithril_full_helm, 4),
                    drop(adamant_med_helm, 4),
                    drop(mystic_hat_light, 1),
                    drop(unidentified_guam, 128),
                    drop(coins, 40, 15),
                    drop(tinderbox, 84),
                    drop(bullseye_lantern_lens, 48),
                    drop(torch_unlit, 36),
                    drop(eye_of_newt, 4),
                    drop(bullseye_lantern_nolens, 20),
                )
        )
    }

private val caveGoblinDrops =
    with(swamp_drop_objs) {
        NpcDrops(
            always = listOf(always(bones)),
            main =
                tableOf(
                    50,
                    drop(bodyrune, 5, 7),
                    drop(waterrune, 5, 6),
                    drop(earthrune, 5, 4),
                    drop(coins, 4, 5),
                    drop(coins, 4, 9),
                    drop(coins, 6, 15),
                    drop(coins, 5, 20),
                    drop(coins, 1),
                    drop(hammer, 4),
                    drop(brass_necklace, 1),
                    drop(tinderbox, 4),
                ),
        )
    }

object swamp_drop_objs : ObjReferences() {
    val adamant_med_helm = find("adamant_med_helm")
    val airrune = find("airrune")
    val big_bones = find("big_bones")
    val bloodrune = find("bloodrune")
    val bodyrune = find("bodyrune")
    val bones = find("bones")
    val brass_necklace = find("brass_necklace")
    val bronze_armoured_boots = find("bronze_armoured_boots")
    val bronze_axe = find("bronze_axe")
    val bronze_bar = find("bronze_bar")
    val bronze_full_helm = find("bronze_full_helm")
    val bronze_med_helm = find("bronze_med_helm")
    val bullseye_lantern_lens = find("bullseye_lantern_lens")
    val bullseye_lantern_nolens = find("bullseye_lantern_nolens")
    val candle_lantern_empty = find("candle_lantern_empty")
    val chaosrune = find("chaosrune")
    val coal = find("coal")
    val coins = find("coins")
    val copper_ore = find("copper_ore")
    val cosmicrune = find("cosmicrune")
    val dwarven_stout = find("dwarven_stout")
    val earth_talisman = find("earth_talisman")
    val earthrune = find("earthrune")
    val eye_of_newt = find("eye_of_newt")
    val firerune = find("firerune")
    val giant_frog_legs = find("giant_frog_legs")
    val gold_bar = find("gold_bar")
    val hammer = find("hammer")
    val iron_armoured_boots = find("iron_armoured_boots")
    val iron_arrow = find("iron_arrow")
    val iron_bar = find("iron_bar")
    val iron_kiteshield = find("iron_kiteshield")
    val iron_med_helm = find("iron_med_helm")
    val iron_ore = find("iron_ore")
    val iron_sword = find("iron_sword")
    val limpwurt_root = find("limpwurt_root")
    val mithril_full_helm = find("mithril_full_helm")
    val mithril_med_helm = find("mithril_med_helm")
    val mithril_ore = find("mithril_ore")
    val mithril_spear = find("mithril_spear")
    val mystic_gloves_light = find("mystic_gloves_light")
    val mystic_hat_light = find("mystic_hat_light")
    val naturerune = find("naturerune")
    val oil_lantern_frame = find("oil_lantern_frame")
    val red_spiders_eggs = find("red_spiders_eggs")
    val snape_grass = find("snape_grass")
    val spinach_roll = find("spinach_roll")
    val steel_arrow = find("steel_arrow")
    val steel_full_helm = find("steel_full_helm")
    val steel_med_helm = find("steel_med_helm")
    val swamp_tar = find("swamp_tar")
    val tin_ore = find("tin_ore")
    val tinderbox = find("tinderbox")
    val torch_unlit = find("torch_unlit")
    val unicorn_horn_dust = find("unicorn_horn_dust")
    val unidentified_guam = find("unidentified_guam")
    val unlit_candle = find("unlit_candle")
    val vial_water = find("vial_water")
    val water_talisman = find("water_talisman")
    val waterrune = find("waterrune")
    val white_berries = find("white_berries")
}
