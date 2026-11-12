package org.rsmod.content.areas.misc.kandarinnorth.locs

import org.rsmod.api.config.refs.seqs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.stat
import org.rsmod.api.script.onOpLoc1
import org.rsmod.content.areas.misc.kandarinnorth.configs.kandarin_north_locs
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.type.stat.StatType
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Seers' Village's, Camelot's and the Legends' Guild's stairs, the Ranging Guild tower's ladder,
 * and the guild doors: the Fishing Guild (68 Fishing), the Ranging Guild (40 Ranged) and the
 * Legends' Guild (its quest counts as done). Destinations are the RuneLite Shortest Path transport
 * table's.
 */
class KandarinNorthLocScript : PluginScript() {
    override fun ScriptContext.startup() {
        with(kandarin_north_locs) {
            // Seers' spiral stairs (and the Kourend ones that share the type) face north: up lands
            // north of the stairs, down lands west.
            for (up in listOf(spiral_stairs, sinclair_stairs)) {
                onOpLoc1(up) { climb(it.loc.coords.translate(0, -1, 1)) }
            }
            for (down in listOf(spiral_stairs_top, sinclair_stairs_top, courthouse_stairs_top)) {
                // UNVERIFIED: Sinclair Mansion's and the courthouse's landings follow Seers' rule.
                onOpLoc1(down) { climb(it.loc.coords.translate(-1, 0, -1)) }
            }
            onOpLoc1(camelot_stairs) { climb(CoordGrid(coords.x, CAMELOT_STAIRS_TOP_Z, 1)) }
            onOpLoc1(camelot_stairs_top) { climb(CoordGrid(coords.x, CAMELOT_STAIRS_Z, 0)) }
            onOpLoc1(tower_ladder_up) { climb(CoordGrid(2668, 3429, 2)) }
            onOpLoc1(tower_ladder_down) { climb(CoordGrid(2667, 3429, 0)) }

            onOpLoc1(fishing_guild_door) { guildDoor(it.loc, stats.fishing, 68, "Fishing") }
            onOpLoc1(ranging_guild_door) { rangingDoor() }
            for (door in listOf(legends_door_left, legends_door_right)) {
                onOpLoc1(door) { walkThrough(it.loc) }
            }
            val gates =
                listOf(
                    legends_gate_left,
                    legends_gate_right,
                    // The Fishing Contest's gates open without the quest.
                    hemenster_gate_left,
                    hemenster_gate_right,
                    loose_railing,
                )
            for (gate in gates) {
                onOpLoc1(gate) { walkThrough(it.loc) }
            }

            // UNVERIFIED wording: the gates stay locked; the loose railing and the fairy ring are
            // the ways in.
            for (gate in listOf(mcgrubor_gate_left, mcgrubor_gate_right)) {
                onOpLoc1(gate) { mes("The gate is locked.") }
            }

            // The White Wolf Mountain tunnel (the Fishing Contest counts as done).
            onOpLoc1(tunnel_stairs_down) { climb(CoordGrid(2820, 9882)) }
            onOpLoc1(tunnel_stairs_up) { climb(CoordGrid(2820, 3486)) }
            onOpLoc1(tunnel_stairs_down_east) { climb(CoordGrid(2876, 9878)) }
            onOpLoc1(tunnel_stairs_up_east) { climb(CoordGrid(2877, 3482)) }
        }
    }

    private suspend fun ProtectedAccess.climb(dest: CoordGrid) {
        arriveDelay()
        anim(seqs.human_reachforladder)
        delay(1)
        telejump(dest)
    }

    /** A guild door that lets in anyone with [level] in [stat] (boosts count). */
    private suspend fun ProtectedAccess.guildDoor(
        door: BoundLocInfo,
        stat: StatType,
        level: Int,
        name: String,
    ) {
        if (player.coords != door.coords && player.stat(stat) < level) {
            mesbox("You need a $name level of $level to enter the $name Guild.")
            return
        }
        walkThrough(door)
    }

    /**
     * The Ranging Guild's door stands diagonally in the fence: the guild's side is south-east of
     * it. Players need 40 Ranged to come in.
     */
    private suspend fun ProtectedAccess.rangingDoor() {
        val inside = player.coords.x - player.coords.z > RANGING_DIAGONAL
        if (!inside && player.stat(stats.ranged) < RANGING_LEVEL) {
            mesbox("You need a Ranged level of $RANGING_LEVEL to enter the Ranging Guild.")
            return
        }
        arriveDelay()
        delay(1)
        telejump(if (inside) RANGING_OUTSIDE else RANGING_INSIDE)
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

    private companion object {
        const val CAMELOT_STAIRS_Z = 3508
        const val CAMELOT_STAIRS_TOP_Z = 3513
        const val RANGING_LEVEL = 40

        /** `x - z` of the tiles on the wall's outside: the door is on x - z = -780. */
        const val RANGING_DIAGONAL = -780
        val RANGING_INSIDE = CoordGrid(2659, 3437)
        val RANGING_OUTSIDE = CoordGrid(2657, 3439)

        // A wall's angle: 0 west, 1 north, 2 east, 3 south.
        val SIDE_X = intArrayOf(-1, 0, 1, 0)
        val SIDE_Z = intArrayOf(0, 1, 0, -1)
    }
}
