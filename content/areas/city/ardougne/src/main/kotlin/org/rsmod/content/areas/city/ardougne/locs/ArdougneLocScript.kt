package org.rsmod.content.areas.city.ardougne.locs

import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.stat
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLoc2
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.content.areas.city.ardougne.configs.ardougne_locs
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.loc.LocAngle
import org.rsmod.game.type.loc.LocType
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The outdoor wooden staircases of East and West Ardougne (their landings are the RuneLite
 * transports' exact tiles), the plague wall's doors (which Plague City's completion leaves open),
 * the farm stiles, the Tree Gnome Village's loose railing and the doors that Pick-lock opens.
 */
class ArdougneLocScript : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1(ardougne_locs.outdoor_stairs_bottom) { climb(it.loc, UP) }
        onOpLoc1(ardougne_locs.outdoor_stairs_top) { climb(it.loc, DOWN) }
        onOpLoc1(ardougne_locs.wall_door_left) { crossWall() }
        onOpLoc1(ardougne_locs.wall_door_right) { crossWall() }
        onOpLoc1(ardougne_locs.stile) { climbStile(it.loc) }
        onOpLoc1(ardougne_locs.gnome_railing) { squeezeThrough(it.loc) }

        for (lock in locks()) {
            onOpLoc1(lock.type) { open(it.loc, lock) }
            onOpLoc2(lock.type) { pickLock(it.loc, lock) }
        }
    }

    private fun ProtectedAccess.climb(stairs: BoundLocInfo, landings: Map<CoordGrid, CoordGrid>) {
        val dest = landings[stairs.coords]
        if (dest == null) {
            mes("Nothing interesting happens.")
            return
        }
        telejump(dest)
    }

    /** The wall is two tiles thick (x 2557 and 2558): step to the far side, on the same row. */
    private fun ProtectedAccess.crossWall() {
        val east = coords.x > WALL_EAST_EDGE
        telejump(CoordGrid(if (east) WALL_WEST_SIDE else WALL_EAST_SIDE, coords.z, coords.level))
    }

    // The stile pairs are the RuneLite transports' tiles on either side.
    private suspend fun ProtectedAccess.climbStile(stile: BoundLocInfo) {
        val (west, east) = STILES[stile.coords] ?: return
        arriveDelay()
        anim(ArdougneSeqs.stile)
        delay(2)
        telejump(if (coords.x <= west.x) east else west)
    }

    /** The railing's two sides are the tile it stands on and the one south of it. */
    private fun ProtectedAccess.squeezeThrough(railing: BoundLocInfo) {
        val gap = railing.coords
        telejump(gap.copy(z = if (coords.z >= gap.z) gap.z - 1 else gap.z))
    }

    /** A lock only holds from the outside: from the inside Open lets you through. */
    private class Lock(val type: LocType, val level: Int, val xp: Double)

    private fun locks() =
        with(ardougne_locs) {
            listOf(
                Lock(locked_door_coins, 1, 3.8),
                Lock(locked_door_nature, 16, 15.0),
                Lock(sewer_gate_left, 31, 25.0),
                Lock(sewer_gate_right, 31, 25.0),
                Lock(chaos_tower_door, 46, 37.5),
                Lock(castle_locked_door, 61, 50.0),
                Lock(ross_door, 13, 15.0),
            )
        }

    private fun ProtectedAccess.open(door: BoundLocInfo, lock: Lock) {
        if (onInside(door)) {
            crossDoor(door)
        } else {
            mes("The door is locked.")
        }
    }

    private suspend fun ProtectedAccess.pickLock(door: BoundLocInfo, lock: Lock) {
        if (player.stat(stats.thieving) < lock.level) {
            mes("You need a Thieving level of ${lock.level} to pick this lock.")
            return
        }
        if (onInside(door)) {
            mes("The door is already unlocked.")
            return
        }
        mes("You attempt to pick the lock.")
        delay(2)
        mes("You manage to pick the lock.")
        statAdvance(stats.thieving, lock.xp)
        crossDoor(door)
    }

    /** Whether the door's axis puts the player on its near side (the one [sides] lists first). */
    private fun ProtectedAccess.onNearSide(door: BoundLocInfo): Boolean {
        val (near, _) = sides(door)
        return if (alongX(door)) coords.x <= near.x else coords.z <= near.z
    }

    private fun ProtectedAccess.onInside(door: BoundLocInfo): Boolean =
        onNearSide(door) == (door.coords in INSIDE_NEAR)

    /** Steps straight across the door, keeping the row or column the player is on. */
    private fun ProtectedAccess.crossDoor(door: BoundLocInfo) {
        val (near, far) = sides(door)
        val dest = if (onNearSide(door)) far else near
        telejump(if (alongX(door)) coords.copy(x = dest.x) else coords.copy(z = dest.z))
    }

    private fun alongX(door: BoundLocInfo) =
        door.angle == LocAngle.West || door.angle == LocAngle.East

    /** The two tiles either side of a wall door, in the order its angle gives. */
    private fun sides(door: BoundLocInfo): Pair<CoordGrid, CoordGrid> {
        val at = door.coords
        return when (door.angle) {
            LocAngle.West -> at.translateX(-1) to at
            LocAngle.North -> at to at.translateZ(1)
            LocAngle.East -> at to at.translateX(1)
            LocAngle.South -> at.translateZ(-1) to at
        }
    }

    private companion object {
        // UNVERIFIED: the side behind each lock is the one with the loot (the chests, the tower);
        // it is the far side of every door except these.
        val INSIDE_NEAR =
            setOf(CoordGrid(2674, 3304, 0), CoordGrid(2565, 3356, 0), CoordGrid(2572, 3305, 1))

        const val WALL_WEST_SIDE = 2556
        const val WALL_EAST_SIDE = 2559
        const val WALL_EAST_EDGE = 2558

        val STILES =
            mapOf(
                CoordGrid(2638, 3350, 0) to (CoordGrid(2637, 3350, 0) to CoordGrid(2640, 3350, 0)),
                CoordGrid(2647, 3375, 0) to (CoordGrid(2646, 3375, 0) to CoordGrid(2649, 3375, 0)),
                CoordGrid(2655, 3347, 0) to (CoordGrid(2654, 3347, 0) to CoordGrid(2657, 3347, 0)),
            )

        val UP =
            mapOf(
                CoordGrid(2662, 3291, 0) to CoordGrid(2661, 3291, 1),
                CoordGrid(2646, 3302, 0) to CoordGrid(2648, 3301, 1),
                CoordGrid(2648, 3294, 0) to CoordGrid(2649, 3296, 1),
                CoordGrid(2526, 3291, 0) to CoordGrid(2527, 3293, 1),
                CoordGrid(2530, 3317, 0) to CoordGrid(2529, 3317, 1),
            )
        val DOWN =
            mapOf(
                CoordGrid(2662, 3291, 1) to CoordGrid(2662, 3294, 0),
                CoordGrid(2647, 3302, 1) to CoordGrid(2645, 3302, 0),
                CoordGrid(2648, 3295, 1) to CoordGrid(2648, 3293, 0),
                CoordGrid(2526, 3292, 1) to CoordGrid(2526, 3290, 0),
                CoordGrid(2530, 3317, 1) to CoordGrid(2530, 3320, 0),
            )
    }
}

object ArdougneSeqs : SeqReferences() {
    val stile = find("human_walk_style")
}
