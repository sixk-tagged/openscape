package org.rsmod.content.areas.city.falador.locs

import org.rsmod.api.config.refs.seqs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.synths
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.stat
import org.rsmod.api.script.onOpLoc1
import org.rsmod.content.areas.city.falador.configs.falador_locs
import org.rsmod.content.areas.city.falador.configs.falador_npcs
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.loc.LocAngle
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Falador's ladders and stairs that don't land on the player's own tile, the Dwarven Mine stairs,
 * and the Mining Guild's entrances (Mining 60, boosts count).
 */
class FaladorLocScript : PluginScript() {
    override fun ScriptContext.startup() {
        // The town ladders stand in a wall: you arrive on the far side of the ladder's tile.
        onOpLoc1(falador_locs.ladder) { climbTownLadder(it.loc, levels = 1) }
        onOpLoc1(falador_locs.ladder_taller) { climbTownLadder(it.loc, levels = 1) }
        onOpLoc1(falador_locs.ladder_top) { climbTownLadder(it.loc, levels = -1) }
        onOpLoc1(falador_locs.ladder_top_north) { climbLadder(coords.translateLevel(-1)) }

        onOpLoc1(falador_locs.outdoor_stairs) { telejump(it.loc.coords.translate(0, 1, 1)) }
        onOpLoc1(falador_locs.outdoor_stairs_top) { telejump(it.loc.coords.translate(3, 0, -1)) }

        onOpLoc1(falador_locs.mine_stairs_down) { mineStairsDown(it.loc) }
        onOpLoc1(falador_locs.mine_stairs_up) { telejump(coords.translate(3, -DUNGEON_Z, 0)) }

        onOpLoc1(falador_locs.guild_ladder) { guildLadder() }
        onOpLoc1(falador_locs.crypt_entrance) { telejump(CRYPT_BOTTOM) }
        onOpLoc1(falador_locs.crypt_exit) { telejump(CRYPT_TOP) }
        onOpLoc1(falador_locs.guild_door) { guildDoor(it.loc) }
        // Every account is a member, so the inner doors always let you through.
        onOpLoc1(falador_locs.guild_inner_door) { stepThrough(it.loc) }
        onOpLoc1(falador_locs.guild_inner_door2) { stepThrough(it.loc) }
    }

    private suspend fun ProtectedAccess.climbTownLadder(ladder: BoundLocInfo, levels: Int) {
        val dest =
            if (ladder.coords in SAME_TILE_LADDERS) {
                coords.translateLevel(levels)
            } else {
                val x = ladder.coords.x * 2 - coords.x
                val z = ladder.coords.z * 2 - coords.z
                CoordGrid(x, z, coords.level + levels)
            }
        climbLadder(dest)
    }

    private suspend fun ProtectedAccess.climbLadder(dest: CoordGrid) {
        anim(seqs.human_reachforladder)
        delay(1)
        telejump(dest)
    }

    /**
     * Only the staircase by Falador's Party Room is scripted; `stairs_cellar` is used elsewhere.
     */
    private fun ProtectedAccess.mineStairsDown(stairs: BoundLocInfo) {
        if (stairs.coords != MINE_STAIRS) {
            mes("Nothing interesting happens.")
            return
        }
        telejump(coords.translate(-3, DUNGEON_Z, 0))
    }

    private suspend fun ProtectedAccess.guildLadder() {
        if (!canEnterGuild()) {
            return
        }
        climbLadder(coords.translateZ(DUNGEON_Z))
    }

    private suspend fun ProtectedAccess.guildDoor(door: BoundLocInfo) {
        val entering = coords != door.coords
        if (entering && !canEnterGuild()) {
            return
        }
        stepThrough(door)
    }

    private suspend fun ProtectedAccess.canEnterGuild(): Boolean {
        if (player.stat(stats.mining) >= GUILD_MINING_LEVEL) {
            return true
        }
        startDialogue {
            chatNpcSpecific(
                "Dwarf",
                falador_npcs.guild_dwarf,
                neutral,
                "Sorry, but you're not experienced enough to go in there.",
            )
            mesbox("You need a Mining level of 60 to access the Mining Guild.")
        }
        return false
    }

    /**
     * Passes through a closed wall door: from its tile out through the side the wall stands on, or
     * from outside onto its tile.
     */
    // Note: the door stays shut, like the Cooks' Guild door: swapping in an open door would
    // let anyone beside it walk in unchecked.
    private fun ProtectedAccess.stepThrough(door: BoundLocInfo) {
        soundSynth(synths.door_open)
        if (coords != door.coords) {
            telejump(door.coords)
            return
        }
        val dest =
            when (door.angle) {
                LocAngle.West -> door.coords.translateX(-1)
                LocAngle.North -> door.coords.translateZ(1)
                LocAngle.East -> door.coords.translateX(1)
                LocAngle.South -> door.coords.translateZ(-1)
            }
        telejump(dest)
    }

    private companion object {
        const val GUILD_MINING_LEVEL = 60
        const val DUNGEON_Z = 6400

        val MINE_STAIRS = CoordGrid(3058, 3376, 0)

        // UNVERIFIED: the open tiles beside the crypt's entrance (its west side) and the stairs
        // back up (their east side), from the cache's sizes and approach flags.
        val CRYPT_TOP = CoordGrid(2964, 3331, 0)
        val CRYPT_BOTTOM = CoordGrid(2960, 9737, 0)

        /** The one town ladder whose top (a force-approach type) lands back on the same tile. */
        val SAME_TILE_LADDERS = setOf(CoordGrid(3035, 3344, 0))
    }
}
