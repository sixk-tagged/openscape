package org.rsmod.content.areas.misc.morytania

import jakarta.inject.Inject
import org.rsmod.api.config.refs.seqs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.agilityLvl
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.script.onOpLoc1
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.type.loc.LocType
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The way into Morytania under Paterdomus (Priest in Peril counts as done: the temple doors, the
 * mausoleum gates and the holy barrier let players through; the trapdoor is Edgeville's generic
 * one) and the Slayer Tower's doors, stairs and basement ladder. Destinations are the RuneLite
 * Shortest Path transport table's.
 */
class MorytaniaLocScript @Inject constructor(private val locRepo: LocRepository) : PluginScript() {
    override fun ScriptContext.startup() {
        with(MorytaniaLocs) {
            for (door in
                listOf(
                    temple_door_left,
                    temple_door_right,
                    mausoleum_gate_west,
                    mausoleum_gate_east,
                    tower_door,
                    tower_door_mirror,
                    tower_door_arch,
                    tower_door_arch_mirror,
                )) {
                onOpLoc1(door) { walkThrough(it.loc) }
            }
            onOpLoc1(east_trapdoor) { open(it.loc, east_trapdoor_open) }
            onOpLoc1(east_trapdoor_open) { climbDown(BARRIER_INSIDE) }
            onOpLoc1(holy_barrier) { passBarrier() }

            onOpLoc1(stairs_1) { climb(CoordGrid(3433, 3537, 1)) }
            onOpLoc1(stairs_1_top) { climb(CoordGrid(3438, 3537, 0)) }
            onOpLoc1(stairs_2) { climb(CoordGrid(3417, 3540, 2)) }
            onOpLoc1(stairs_2_top) { climb(CoordGrid(3412, 3540, 1)) }
            onOpLoc1(basement_ladder_top) { climbDown(CoordGrid(3412, 9932, 3)) }
            onOpLoc1(basement_ladder_bottom) { climb(CoordGrid(3417, 3536, 0)) }
            onOpLoc1(chain_bottom) { chain(it.loc, up = true) }
            onOpLoc1(chain_top) { chain(it.loc, up = false) }
            onOpLoc1(window) { window(it.loc) }
            onOpLoc1(ivy) { shortcut(IVY_LEVEL) { IVY_TOP } }
            // UNVERIFIED: that the way down needs 81 and gives xp too.
            onOpLoc1(ivy_window) { shortcut(IVY_LEVEL) { IVY_BOTTOM } }
            onOpLoc1(danger_sign) { readDangerSign() }
        }
    }

    /**
     * The spikey chains: ground floor to first floor needs 61 Agility, first to second 71. The
     * player climbs to the same tile a floor up or down.
     *
     * Note: never slips (the wiki's fail: 6 xp and 2-5 damage, still climbs).
     */
    private suspend fun ProtectedAccess.chain(loc: BoundLocInfo, up: Boolean) {
        val lowerFloor = if (up) loc.coords.level else loc.coords.level - 1
        shortcut(if (lowerFloor == 0) 61 else 71) {
            val here = player.coords
            CoordGrid(here.x, here.z, here.level + if (up) 1 else -1)
        }
    }

    /** The broken window by the entrance, to the banshees: diagonally through it. */
    private suspend fun ProtectedAccess.window(loc: BoundLocInfo) {
        shortcut(WINDOW_LEVEL) {
            // The window runs north-west to south-east: the tower's side is south-west of it.
            val inside = player.coords.x + player.coords.z < loc.coords.x + loc.coords.z
            loc.coords.translate(if (inside) 1 else -1, if (inside) 1 else -1)
        }
    }

    /** An Agility shortcut to [dest] (worked out once the player is there): 3 xp, never fails. */
    private suspend fun ProtectedAccess.shortcut(level: Int, dest: () -> CoordGrid) {
        if (player.agilityLvl < level) {
            // UNVERIFIED: the wording.
            mes("You need an Agility level of $level to use this shortcut.")
            return
        }
        arriveDelay()
        anim(seqs.human_reachforladder)
        delay(1)
        telejump(dest())
        statAdvance(stats.agility, SHORTCUT_XP)
    }

    private suspend fun ProtectedAccess.readDangerSign() {
        mesbox(
            "<col=ff0000>WARNING!</col><br>This area contains very dangerous creatures!<br>" +
                "Do not pass unless properly prepared!"
        )
    }

    /**
     * Crosses a wall door: from the door's own tile to the tile on its wall side, or back.
     *
     * Note: the player steps through without the door swinging open.
     */
    private suspend fun ProtectedAccess.walkThrough(door: BoundLocInfo) {
        arriveDelay()
        val outside = door.coords.translate(SIDE_X[door.angleId], SIDE_Z[door.angleId])
        val dest = if (player.coords == door.coords) outside else door.coords
        delay(1)
        telejump(dest)
    }

    private fun open(trapdoor: BoundLocInfo, open: LocType) {
        locRepo.change(trapdoor, open, TRAPDOOR_OPEN_CYCLES)
    }

    private suspend fun ProtectedAccess.climbDown(dest: CoordGrid) {
        arriveDelay()
        anim(seqs.human_pickupfloor)
        delay(1)
        telejump(dest)
    }

    private suspend fun ProtectedAccess.climb(dest: CoordGrid) {
        arriveDelay()
        anim(seqs.human_reachforladder)
        delay(1)
        telejump(dest)
    }

    private suspend fun ProtectedAccess.passBarrier() {
        arriveDelay()
        delay(1)
        telejump(MORYTANIA_SIDE)
    }

    private companion object {
        const val TRAPDOOR_OPEN_CYCLES = 300
        const val SHORTCUT_XP = 3.0
        const val WINDOW_LEVEL = 18
        const val IVY_LEVEL = 81

        // A wall's angle: 0 west, 1 north, 2 east, 3 south.
        val SIDE_X = intArrayOf(-1, 0, 1, 0)
        val SIDE_Z = intArrayOf(0, 1, 0, -1)

        val BARRIER_INSIDE = CoordGrid(3440, 9887)
        val MORYTANIA_SIDE = CoordGrid(3423, 3485)
        val IVY_TOP = CoordGrid(3420, 3534, 2)
        val IVY_BOTTOM = CoordGrid(3418, 3532, 0)
    }
}
