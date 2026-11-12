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
 * Drop tables (OSRS wiki, Oct 2025) for the monsters of East and West Ardougne, the Fight Arena and
 * the Battlefield of Khazard. The goblins, rats, bears, wolves, scorpions and farmers use the
 * tables the earlier areas give their types.
 */
class ArdougneDrops @Inject constructor(private val tables: NpcDropTables) : PluginScript() {
    override fun ScriptContext.startup() {
        with(ardougne_drop_npcs) {
            paladins.forEach { tables[it] = paladinDrops }
            // The knights, heroes and Khazard's soldiers drop nothing but bones.
            (knights + heroes + khazard_soldiers + mourners).forEach { tables[it] = bonesOnly }
            tables[guard] = guardDrops
            tables[snake] = bonesOnly
            tables[monk] = bonesOnly
            tables[tribesman] = tribesmanDrops
            // The poison scorpion has no bones and no table.
            tables[poison_scorpion] = NpcDrops()
        }
    }
}

private val paladinDrops =
    with(ardougne_drop_objs) {
        NpcDrops(
            always = listOf(bones),
            main =
                table(
                    drop(steel_sword, 2),
                    drop(steel_longsword, 1),
                    drop(steel_full_helm, 1),
                    drop(waterrune, 13, 30),
                    drop(bloodrune, 1),
                    drop(iron_bar, 9),
                    drop(mithril_bar, 1),
                    drop(steel_bar, 1),
                    herbs(8),
                    drop(objs.coins, 40, 48),
                    drop(objs.coins, 19, 15),
                    drop(objs.coins, 16, 2),
                    drop(objs.coins, 10, 8),
                    drop(objs.coins, 2, 120),
                    gems(2),
                ),
        )
    }

private val tribesmanDrops =
    with(ardougne_drop_objs) {
        NpcDrops(
            always = listOf(bones),
            main =
                tableOf(
                    8832,
                    drop(bronze_spear, 448),
                    drop(steel_javelin, 192, 10),
                    drop(poison_bolt, 128, 4),
                    drop(iron_spear, 128),
                    drop(steel_arrow_p, 128, 5),
                    drop(mithril_javelin, 128, 10),
                    drop(mithril_spear, 64),
                    herbs(704),
                    drop(unidentified_rogues_purse, 320),
                    drop(unidentified_snake_weed, 320),
                    drop(village_trade_sticks, 1600, 15),
                    drop(snape_grass, 1280),
                    drop(limpwurt_root, 768),
                    drop(tbwt_cleaning_cloth, 768),
                    drop(naturerune, 512, 3),
                    drop(village_trade_sticks, 320, 62),
                    drop(gold_ore, 320),
                    drop(dose2_antipoison, 192),
                    drop(dose3_antipoison, 64),
                    drop(bread, 64),
                    drop(tin_ore, 64),
                    drop(pot_flour, 64),
                    gems(128, underground = true),
                ),
        )
    }

object ardougne_drop_npcs : NpcReferences() {
    val monk = find("monk_ardougne")
    val tribesman = find("tribesman")
    val paladins = listOf(find("paladin"), find("paladin2"))
    val knights = listOf(find("knight_of_ardougne"))
    val heroes = listOf(find("hero"))
    val khazard_soldiers =
        listOf(
            find("arena_guard1"),
            find("arena_guard2"),
            find("arena_guard3"),
            find("arena_guard4"),
            find("khazard_troop"),
            find("battlefield_khazard_troop"),
            find("battlefield_khazard_troop_spearmen"),
        )
    // UNVERIFIED: the mourners' wiki table also gives their quest outfit; only bones are given.
    val mourners =
        listOf(
            find("mourner1"),
            find("mourner2"),
            find("mourner3"),
            find("mourner_armed"),
            find("mourner_armed_guard"),
            find("mourner_elena_guard"),
            find("mournerstew1"),
            find("mournerstew2"),
            find("mournerstew3"),
            find("mournertwa"),
            find("mournertwb"),
            find("mournerwatchtower"),
        )
    val guard = find("ardougne_guard")
    val snake = find("snake")
    val poison_scorpion = find("poison_scorpion")
}

object ardougne_drop_objs : ObjReferences() {
    val steel_sword = find("steel_sword")
    val steel_longsword = find("steel_longsword")
    val steel_full_helm = find("steel_full_helm")
    val waterrune = find("waterrune")
    val bloodrune = find("bloodrune")
    val iron_bar = find("iron_bar")
    val mithril_bar = find("mithril_bar")
    val steel_bar = find("steel_bar")
    val bronze_spear = find("bronze_spear")
    val steel_javelin = find("steel_javelin")
    val poison_bolt = find("poison_bolt")
    val iron_spear = find("iron_spear")
    val steel_arrow_p = find("steel_arrow_p")
    val mithril_javelin = find("mithril_javelin")
    val mithril_spear = find("mithril_spear")
    val unidentified_rogues_purse = find("unidentified_rogues_purse")
    val unidentified_snake_weed = find("unidentified_snake_weed")
    val village_trade_sticks = find("village_trade_sticks")
    val snape_grass = find("snape_grass")
    val limpwurt_root = find("limpwurt_root")
    val tbwt_cleaning_cloth = find("tbwt_cleaning_cloth")
    val naturerune = find("naturerune")
    val gold_ore = find("gold_ore")
    val dose2_antipoison = find("2dose2antipoison")
    val dose3_antipoison = find("3dose2antipoison")
    val bread = find("bread")
    val tin_ore = find("tin_ore")
    val pot_flour = find("pot_flour")
}
