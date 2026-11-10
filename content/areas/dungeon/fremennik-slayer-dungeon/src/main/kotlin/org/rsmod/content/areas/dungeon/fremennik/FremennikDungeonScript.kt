package org.rsmod.content.areas.dungeon.fremennik

import org.rsmod.api.config.refs.seqs
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.agilityLvl
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.type.builders.map.npc.MapNpcSpawnBuilder
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The Fremennik Slayer Dungeon, south-east of Rellekka: the cave in and the tunnel out, the steps
 * to the kurask, the strange floor the 43 Agility shortcut jumps over and the 61 Agility crevice
 * between the basilisks and the turoth. Destinations are the RuneLite Shortest Path transport
 * table's.
 */
class FremennikDungeonScript : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1(FremennikLocs.entrance) { enter(CoordGrid(2808, 10002)) }
        onOpLoc1(FremennikLocs.exit) { enter(CoordGrid(2796, 3615)) }
        onOpLoc1(FremennikLocs.steps) { steps() }
        onOpLoc1(FremennikLocs.strange_floor) { jump(it.loc) }
        onOpLoc1(FremennikLocs.crevice) { squeeze() }
    }

    private suspend fun ProtectedAccess.enter(dest: CoordGrid) {
        arriveDelay()
        delay(1)
        telejump(dest)
    }

    private suspend fun ProtectedAccess.steps() {
        arriveDelay()
        anim(seqs.human_reachforladder)
        delay(1)
        telejump(if (player.coords.z < STEPS_Z) STEPS_TOP else STEPS_BOTTOM)
    }

    /** Over the floor spikes from one side to the other, two tiles along x. */
    private suspend fun ProtectedAccess.jump(floor: BoundLocInfo) {
        if (player.agilityLvl < JUMP_LEVEL) {
            // UNVERIFIED: the wording.
            mes("You need an Agility level of $JUMP_LEVEL to jump over the strange floor.")
            return
        }
        arriveDelay()
        val west = floor.coords.translateX(-1)
        val east = floor.coords.translateX(1)
        delay(1)
        telejump(if (player.coords.x <= floor.coords.x) east else west)
    }

    private suspend fun ProtectedAccess.squeeze() {
        if (player.agilityLvl < CREVICE_LEVEL) {
            // UNVERIFIED: the wording.
            mes("You need an Agility level of $CREVICE_LEVEL to squeeze through the crevice.")
            return
        }
        arriveDelay()
        delay(1)
        telejump(if (player.coords.x < CREVICE_MID_X) CREVICE_EAST else CREVICE_WEST)
    }

    private companion object {
        const val JUMP_LEVEL = 43
        const val CREVICE_LEVEL = 61
        const val CREVICE_MID_X = 2733
        const val STEPS_Z = 9990

        val STEPS_TOP = CoordGrid(2703, 9991)
        val STEPS_BOTTOM = CoordGrid(2703, 9989)
        val CREVICE_WEST = CoordGrid(2730, 10008)
        val CREVICE_EAST = CoordGrid(2735, 10008)
    }
}

object FremennikLocs : LocReferences() {
    val entrance = find("slayer_dungeon_entrance")
    val exit = find("slayer_dungeon_exit")
    val steps = find("kurask_steps")
    val strange_floor = find("slayer_dungeon_floor_spikes_sc")
    val crevice = find("slayer_dungeon_2_sc_wall_crack")
}

object FremennikNpcSpawns : MapNpcSpawnBuilder() {
    override fun onPackMapTask() {
        resourceFile<FremennikDungeonScript>("npcs.toml")
    }
}
