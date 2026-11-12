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
 * Drop tables (OSRS wiki, Sep 2025) for the Stronghold Slayer Cave's variants of monsters whose
 * tables live elsewhere (aberrant spectres in [SlayerTowerDrops]; ankou, fire giants and hellhounds
 * in [WildernessDrops]), and for Yanille's ogres, soldiers, tower guards, watchmen and chaos druid
 * warriors. Tertiaries (clues, ensouled heads, looting bags, ogre ribs, bones) are left out.
 */
class GnomeYanilleDrops @Inject constructor(private val tables: NpcDropTables) : PluginScript() {
    override fun ScriptContext.startup() {
        with(stronghold_drop_npcs) {
            aberrant_spectres.forEach { tables[it] = aberrantSpectreDrops }
            ankous.forEach { tables[it] = ankouDrops }
            fire_giants.forEach { tables[it] = fireGiantDrops }
            tables[hellhound] = hellhoundDrops
        }
        with(yanille_drop_npcs) {
            ogres.forEach { tables[it] = ogreDrops }
            tables[soldier] = soldierDrops
            tables[tower_guard] = soldierDrops
            tables[watchman] = watchmanDrops
            tables[druid_warrior] = druidWarriorDrops
        }
    }
}

object stronghold_drop_npcs : NpcReferences() {
    val aberrant_spectres =
        listOf(
            find("slayer_aberrantspectre_1_strongholdcave"),
            find("slayer_aberrantspectre_2_strongholdcave"),
        )
    val ankous =
        listOf(
            find("sos_death_ankou_strongholdcave"),
            find("sos_death_ankou_strongholdcave_2"),
            find("sos_death_ankou_strongholdcave_3"),
        )
    val fire_giants = (1..4).map { find("firegiant_strongholdcave_$it") }
    val hellhound = find("hellhound_strongholdcave")
}

object yanille_drop_npcs : NpcReferences() {
    val ogres = listOf(find("ogre"), find("ogre2"))
    val soldier = find("yanille_soldier")
    val tower_guard = find("tower_guard")
    val watchman = find("yanille_watchman")
    val druid_warrior = find("chaos_druid_warrior")
}

private val ogreDrops = NpcDrops(always = listOf(always(yanille_drop_objs.big_bones)))

private val watchmanDrops = NpcDrops(always = listOf(always(yanille_drop_objs.bones)))

private val soldierDrops =
    with(yanille_drop_objs) {
        NpcDrops(
            always = listOf(always(bones)),
            main =
                table(
                    drop(steel_longsword, 6),
                    drop(steel_med_helm, 1),
                    drop(bronze_arrow, 5, 4),
                    drop(steel_arrow, 4, 2),
                    drop(airrune, 2, 8),
                    drop(earthrune, 2, 4),
                    drop(firerune, 2, 6),
                    drop(chaosrune, 1, 3),
                    drop(bloodrune, 1, 2),
                    drop(naturerune, 1, 2),
                    drop(steel_arrow, 1, 8),
                    drop(coins, 37, 9),
                    drop(coins, 16, 8),
                    drop(coins, 9, 24),
                    drop(coins, 8, 6),
                    drop(coins, 4, 30),
                    drop(coins, 4, 12),
                    drop(coins, 2, 35),
                    drop(mind_talisman, 3),
                    drop(iron_ore, 1),
                ),
        )
    }

private val druidWarriorDrops =
    with(yanille_drop_objs) {
        NpcDrops(
            always = listOf(always(bones)),
            main =
                table(
                    drop(black_dagger, 1),
                    drop(firerune, 5, 12),
                    drop(lawrune, 4, 2),
                    drop(earthrune, 2, 9),
                    drop(airrune, 1, 36),
                    drop(naturerune, 1, 3),
                    Drop(table = herbs, weight = 44),
                    drop(white_berries, 5),
                    drop(unicorn_horn_dust, 2),
                    drop(limpwurt_root, 1),
                    drop(limpwurt_root, 1, 2),
                    drop(snape_grass, 1),
                    drop(vial_water, 1),
                    drop(coins, 15, 3),
                    drop(coins, 3, 29),
                    drop(coins, 1, 10),
                    drop(limpwurt_seed, 1),
                    drop(obj_1dose2defense, 1),
                    gems(1, underground = true),
                ),
        )
    }

object yanille_drop_objs : ObjReferences() {
    val airrune = find("airrune")
    val big_bones = find("big_bones")
    val black_dagger = find("black_dagger")
    val bloodrune = find("bloodrune")
    val bones = find("bones")
    val bronze_arrow = find("bronze_arrow")
    val chaosrune = find("chaosrune")
    val coins = find("coins")
    val earthrune = find("earthrune")
    val firerune = find("firerune")
    val iron_ore = find("iron_ore")
    val lawrune = find("lawrune")
    val limpwurt_root = find("limpwurt_root")
    val limpwurt_seed = find("limpwurt_seed")
    val mind_talisman = find("mind_talisman")
    val naturerune = find("naturerune")
    val obj_1dose2defense = find("1dose2defense")
    val snape_grass = find("snape_grass")
    val steel_arrow = find("steel_arrow")
    val steel_longsword = find("steel_longsword")
    val steel_med_helm = find("steel_med_helm")
    val unicorn_horn_dust = find("unicorn_horn_dust")
    val vial_water = find("vial_water")
    val white_berries = find("white_berries")
}
