@file:Suppress("SpellCheckingInspection")

package org.rsmod.content.areas.dungeon.swampcaves

import org.rsmod.api.type.builders.area.AreaBuilder
import org.rsmod.api.type.builders.map.area.MapAreaBuilder
import org.rsmod.api.type.builders.map.npc.MapNpcSpawnBuilder
import org.rsmod.api.type.builders.varp.VarpBuilder
import org.rsmod.api.type.refs.area.AreaReferences
import org.rsmod.api.type.refs.interf.InterfaceReferences
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.api.type.refs.timer.TimerReferences
import org.rsmod.api.type.refs.varp.VarpReferences
import org.rsmod.game.area.polygon.VertexCoord

object SwampCaveNpcs : NpcReferences() {
    val hole = find("swamp_wallbeast")
    val wall_beast = find("swamp_wallbeast_combat")
}

object SwampCaveObjs : ObjReferences() {
    /** A spiny helmet or any Slayer helmet stops the wall beasts' grab. */
    val helmets =
        listOf(
            find("wallbeast_spike_helmet"),
            find("slayer_helm"),
            find("slayer_helm_i"),
            find("slayer_helm_black"),
            find("slayer_helm_i_black"),
            find("slayer_helm_green"),
            find("slayer_helm_i_green"),
            find("slayer_helm_red"),
            find("slayer_helm_i_red"),
            find("slayer_helm_purple"),
            find("slayer_helm_i_purple"),
            find("slayer_helm_turquoise"),
            find("slayer_helm_i_turquoise"),
            find("slayer_helm_hydra"),
            find("slayer_helm_i_hydra"),
            find("slayer_helm_twisted"),
            find("slayer_helm_i_twisted"),
            find("slayer_helm_jad"),
            find("slayer_helm_i_jad"),
            find("slayer_helm_verzik"),
            find("slayer_helm_i_verzik"),
            find("slayer_helm_zuk"),
            find("slayer_helm_i_zuk"),
            find("slayer_helm_araxyte"),
            find("slayer_helm_i_araxyte"),
        )
}

object SwampCaveSeqs : SeqReferences() {
    val beast_grab = find("wall_beast_specialattack_attack")
    val beast_foiled = find("wall_beast_specialattack_fail")
    val player_grabbed = find("human_wall_beast_specialattack_start")
}

object SwampCaveInterfaces : InterfaceReferences() {
    val dark = find("darkness_dark")
    val medium = find("darkness_medium")
    val light = find("darkness_light")
}

object SwampCaveAreas : AreaReferences() {
    val caves = find("lumbridge_swamp_caves")
}

object SwampCaveTimers : TimerReferences() {
    /** Ticks every cycle while a player is in the caves: darkness, insects, gas, wall beasts. */
    val tick = find("swamp_caves_tick")
}

object SwampCaveVarps : VarpReferences() {
    val dark_ticks = find("swamp_dark_ticks")
    val gas_ticks = find("swamp_gas_ticks")
    /** The darkness overlay on screen: 0 none, 1 light, 2 medium, 3 dark. */
    val overlay = find("swamp_darkness_overlay")
    /** The packed coords of the wall beast tile the player last stood on (no second grab). */
    val beast_tile = find("swamp_beast_tile")
}

internal object SwampCaveVarpBuilder : VarpBuilder() {
    init {
        build("swamp_dark_ticks") { temporary = true }
        build("swamp_gas_ticks") { temporary = true }
        build("swamp_darkness_overlay") { temporary = true }
        build("swamp_beast_tile") { temporary = true }
    }
}

internal object SwampCaveAreaBuilder : AreaBuilder() {
    init {
        build("lumbridge_swamp_caves")
    }
}

/** The whole of map squares 49_149 and 50_149, level 0. */
object SwampCaveMapAreas : MapAreaBuilder() {
    override fun onPackMapTask() {
        area(SwampCaveAreas.caves) {
            for (mx in 49..50) {
                polygon(levels = 0..0) {
                    vertex(VertexCoord(mx, 149, 0, 0))
                    vertex(VertexCoord(mx, 149, 63, 0))
                    vertex(VertexCoord(mx, 149, 63, 63))
                    vertex(VertexCoord(mx, 149, 0, 63))
                }
            }
        }
    }
}

object SwampCaveNpcSpawns : MapNpcSpawnBuilder() {
    override fun onPackMapTask() {
        resourceFile<SwampCaveScript>("npcs.toml")
    }
}
