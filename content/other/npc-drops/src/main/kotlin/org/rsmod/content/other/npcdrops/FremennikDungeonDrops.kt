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
 * Drop tables (OSRS wiki, Sep 2025) for the Fremennik Slayer Dungeon: cockatrice, pyrefiends,
 * basilisks, jellies, turoth and kurask (its cave crawlers and rockslugs use [SwampCaveDrops]'s
 * tables). Tertiaries (clues, heads, brimstone keys, crystal shards) are left out.
 */
class FremennikDungeonDrops @Inject constructor(private val tables: NpcDropTables) :
    PluginScript() {
    override fun ScriptContext.startup() {
        with(fremennik_drop_npcs) {
            cockatrice.forEach { tables[it] = cockatriceDrops }
            pyrefiends.forEach { tables[it] = pyrefiendDrops }
            basilisks.forEach { tables[it] = basiliskDrops }
            jellies.forEach { tables[it] = jellyDrops }
            turoth.forEach { tables[it] = turothDrops }
            kurask.forEach { tables[it] = kuraskDrops }
            with(superior_drop_npcs) {
                tables[cockatrice] = superiorDrops(cockatriceDrops, 25)
                tables[pyrefiend] = superiorDrops(pyrefiendDrops, 30)
                tables[basilisk] = superiorDrops(basiliskDrops, 40)
                tables[jelly] = superiorDrops(jellyDrops, 52)
                tables[turoth] = superiorDrops(turothDrops, 55)
                tables[kurask] = superiorDrops(kuraskDrops, 70)
            }
        }
    }
}

object fremennik_drop_npcs : NpcReferences() {
    val cockatrice = listOf(find("slayer_cockatrice"), find("slayer_cockatrice_daddy"))
    val pyrefiends = (1..4).map { find("slayer_pyrefiend_$it") }
    val basilisks = listOf(find("slayer_basilisk"), find("slayer_basilisk_baby"))
    val jellies = (1..6).map { find("slayer_jelly_$it") }
    val turoth =
        listOf(
            find("slayer_turoth"),
            find("slayer_turoth_dad"),
            find("slayer_turoth_mum"),
            find("slayer_turoth_child"),
            find("slayer_turoth_baby"),
            find("slayer_turoth_dad2"),
            find("slayer_turoth_dad3"),
        )
    val kurask = listOf(find("slayer_kursk_1"), find("slayer_kursk_2"), find("slayer_kurask_troll"))
}

private val cockatriceDrops =
    with(fremennik_drop_objs) {
        NpcDrops(
            always = listOf(always(bones)),
            main =
                tableOf(
                    512,
                    drop(iron_sword, 12),
                    drop(steel_dagger, 12),
                    drop(iron_armoured_boots, 4),
                    drop(iron_javelin, 4, 5),
                    drop(steel_longsword, 4),
                    drop(mystic_boots_light, 1),
                    drop(naturerune, 24, 2),
                    drop(naturerune, 16, 4),
                    drop(naturerune, 8, 6),
                    drop(lawrune, 12, 2),
                    drop(waterrune, 8, 2),
                    drop(firerune, 8, 7),
                    Drop(table = herbs, weight = 40),
                    drop(coins, 64, 15),
                    drop(coins, 48, 5),
                    drop(coins, 48, 28),
                    drop(coins, 16, 62),
                    drop(coins, 12, 42),
                    drop(coins, 4),
                    drop(limpwurt_root, 84),
                    gems(8, underground = true),
                ),
        )
    }

private val pyrefiendDrops =
    with(fremennik_drop_objs) {
        NpcDrops(
            always = listOf(always(fiendish_ashes)),
            main =
                table(
                    drop(steel_axe, 4),
                    drop(steel_full_helm, 4),
                    drop(mithril_chainbody, 2),
                    drop(staff_of_fire, 3),
                    drop(steel_armoured_boots, 1),
                    drop(adamant_med_helm, 1),
                    drop(firerune, 21, 30),
                    drop(firerune, 8, 60),
                    drop(chaosrune, 5, 12),
                    drop(deathrune, 3, 3),
                    drop(coins, 7, 10),
                    drop(coins, 24, 40),
                    drop(coins, 20, 120),
                    drop(coins, 10, 200),
                    drop(coins, 2, 450),
                    drop(gold_ore, 8),
                    drop(jug_wine, 2),
                    gems(3, underground = true),
                ),
        )
    }

private val basiliskDrops =
    with(fremennik_drop_objs) {
        NpcDrops(
            always = listOf(always(bones)),
            main =
                tableOf(
                    513,
                    drop(mithril_axe, 12),
                    drop(mithril_spear, 8),
                    drop(steel_battleaxe, 12),
                    drop(adamant_full_helm, 4),
                    drop(mithril_kiteshield, 4),
                    drop(rune_dagger, 4),
                    drop(mystic_hat_light, 1),
                    drop(waterrune, 32, 75),
                    drop(naturerune, 20, 15),
                    drop(lawrune, 12, 3),
                    drop(naturerune, 4, 37),
                    Drop(table = herbs, weight = 140),
                    drop(adamantite_ore, 12),
                    drop(coins, 116, 44),
                    drop(coins, 68, 200),
                    drop(coins, 20, 132),
                    drop(coins, 20, 11),
                    drop(coins, 4, 440),
                    gems(20, underground = true),
                ),
        )
    }

private val jellyDrops =
    with(fremennik_drop_objs) {
        NpcDrops(
            main =
                table(
                    drop(steel_battleaxe, 11),
                    drop(steel_2h_sword, 7),
                    drop(steel_axe, 3),
                    drop(mithril_kiteshield, 2),
                    drop(mithril_armoured_boots, 1),
                    drop(rune_full_helm, 1),
                    drop(coins, 39, 102),
                    drop(coins, 30, 44),
                    drop(coins, 10, 220),
                    drop(coins, 7, 11),
                    drop(coins, 2, 460),
                    drop(chaosrune, 5, 15),
                    drop(deathrune, 3, 5),
                    drop(gold_bar, 2),
                    drop(thread, 1, 10),
                    gems(4, underground = true),
                )
        )
    }

private val turothDrops =
    with(fremennik_drop_objs) {
        NpcDrops(
            always = listOf(always(bones)),
            main =
                tableOf(
                    64000,
                    drop(steel_platelegs, 3500),
                    drop(mithril_axe, 1500),
                    drop(mithril_kiteshield, 500),
                    drop(adamant_full_helm, 500),
                    drop(rune_dagger, 500),
                    drop(leafbladed_sword, 128),
                    drop(mystic_robe_bottom_light, 125),
                    drop(lawrune, 3000, 3),
                    drop(naturerune, 2500, 15),
                    drop(naturerune, 500, 37),
                    Drop(table = herbs, weight = 15500),
                    drop(limpwurt_root, 3500),
                    drop(coins, 14500, 44),
                    drop(coins, 6000, 132),
                    drop(coins, 500, 440),
                    gems(2500, underground = true),
                ),
        )
    }

private val kuraskDrops =
    with(fremennik_drop_objs) {
        NpcDrops(
            always = listOf(always(bones)),
            main =
                tableOf(
                    124000,
                    drop(mithril_kiteshield, 3000),
                    drop(rune_longsword, 3000),
                    drop(adamant_platebody, 3000),
                    drop(rune_axe, 3000),
                    drop(leafbladed_sword, 323),
                    drop(mystic_robe_top_light, 242),
                    drop(leafbladed_battleaxe, 121),
                    drop(naturerune, 10000, 10),
                    drop(naturerune, 7000, 15),
                    drop(naturerune, 4000, 30),
                    Drop(table = herbs, weight = 18000, count = 3..3),
                    drop(limpwurt_seed, 6000),
                    drop(coins, 16000, 2000..3000),
                    drop(coins, 5000, 10000),
                    drop(noted_flax, 6000, 100),
                    drop(noted_white_berries, 6000, 12),
                    drop(noted_papaya, 4000, 10),
                    drop(noted_coconut, 4000, 10),
                    drop(noted_big_bones, 5000, 20),
                    gems(6000, underground = true),
                ),
        )
    }

object fremennik_drop_objs : ObjReferences() {
    val noted_flax = find("cert_flax")
    val noted_white_berries = find("cert_white_berries")
    val noted_papaya = find("cert_papaya")
    val noted_coconut = find("cert_coconut")
    val noted_big_bones = find("cert_big_bones")
    val adamant_full_helm = find("adamant_full_helm")
    val adamant_med_helm = find("adamant_med_helm")
    val adamant_platebody = find("adamant_platebody")
    val adamantite_ore = find("adamantite_ore")
    val big_bones = find("big_bones")
    val bones = find("bones")
    val chaosrune = find("chaosrune")
    val coconut = find("coconut")
    val coins = find("coins")
    val deathrune = find("deathrune")
    val fiendish_ashes = find("fiendish_ashes")
    val firerune = find("firerune")
    val flax = find("flax")
    val gold_bar = find("gold_bar")
    val gold_ore = find("gold_ore")
    val iron_armoured_boots = find("iron_armoured_boots")
    val iron_javelin = find("iron_javelin")
    val iron_sword = find("iron_sword")
    val jug_wine = find("jug_wine")
    val lawrune = find("lawrune")
    val leafbladed_battleaxe = find("leafbladed_battleaxe")
    val leafbladed_sword = find("leafbladed_sword")
    val limpwurt_root = find("limpwurt_root")
    val limpwurt_seed = find("limpwurt_seed")
    val mithril_armoured_boots = find("mithril_armoured_boots")
    val mithril_axe = find("mithril_axe")
    val mithril_chainbody = find("mithril_chainbody")
    val mithril_kiteshield = find("mithril_kiteshield")
    val mithril_spear = find("mithril_spear")
    val mystic_boots_light = find("mystic_boots_light")
    val mystic_hat_light = find("mystic_hat_light")
    val mystic_robe_bottom_light = find("mystic_robe_bottom_light")
    val mystic_robe_top_light = find("mystic_robe_top_light")
    val naturerune = find("naturerune")
    val papaya = find("papaya")
    val rune_axe = find("rune_axe")
    val rune_dagger = find("rune_dagger")
    val rune_full_helm = find("rune_full_helm")
    val rune_longsword = find("rune_longsword")
    val staff_of_fire = find("staff_of_fire")
    val steel_2h_sword = find("steel_2h_sword")
    val steel_armoured_boots = find("steel_armoured_boots")
    val steel_axe = find("steel_axe")
    val steel_battleaxe = find("steel_battleaxe")
    val steel_dagger = find("steel_dagger")
    val steel_full_helm = find("steel_full_helm")
    val steel_longsword = find("steel_longsword")
    val steel_platelegs = find("steel_platelegs")
    val thread = find("thread")
    val waterrune = find("waterrune")
    val white_berries = find("white_berries")
}
