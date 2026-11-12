package org.rsmod.content.areas.dungeon.stronghold

import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.agilityLvl
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.type.builders.map.npc.MapNpcSpawnBuilder
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.game.hit.HitType
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The Stronghold Slayer Cave, beside Nieve in the Gnome Stronghold: the cave in, the tunnels out
 * and the short tunnel between its two halves. Destinations are the RuneLite Shortest Path
 * transport table's.
 */
class StrongholdSlayerCaveScript : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1(StrongholdLocs.entrance) { enter(CAVE_ENTRANCE) }
        onOpLoc1(StrongholdLocs.exit_right) { enter(SURFACE) }
        onOpLoc1(StrongholdLocs.exit_left) { enter(SURFACE) }
        onOpLoc1(StrongholdLocs.tunnel_right) { tunnel() }
        onOpLoc1(StrongholdLocs.tunnel_left) { tunnel() }
    }

    private suspend fun ProtectedAccess.enter(dest: CoordGrid) {
        arriveDelay()
        delay(1)
        telejump(dest)
    }

    /**
     * The tunnel joins x 2430 (west) and x 2434 (east) on the same row; you land beyond it. It
     * needs 72 Agility (boosts count) to go in; coming back out without it hurts for up to 12.
     */
    private suspend fun ProtectedAccess.tunnel() {
        val west = player.coords.x <= TUNNEL_MID_X
        val skilled = player.agilityLvl >= TUNNEL_AGILITY
        if (west && !skilled) {
            // UNVERIFIED: the refusal text.
            mes("You need an Agility level of $TUNNEL_AGILITY to use this shortcut.")
            return
        }
        arriveDelay()
        delay(1)
        if (!skilled) {
            takeInstantHit(HitType.Typeless, random.of(0, TUNNEL_MAX_HIT))
        }
        telejump(CoordGrid(if (west) TUNNEL_EAST_X else TUNNEL_WEST_X, player.coords.z))
    }

    private companion object {
        const val TUNNEL_AGILITY = 72
        const val TUNNEL_MAX_HIT = 12
        const val TUNNEL_MID_X = 2432
        const val TUNNEL_WEST_X = 2429
        const val TUNNEL_EAST_X = 2435

        val CAVE_ENTRANCE = CoordGrid(2429, 9824)
        val SURFACE = CoordGrid(2430, 3424)
    }
}

object StrongholdLocs : LocReferences() {
    val entrance = find("slayer_cave_entrance")
    val exit_right = find("slayer_cave_exit_right")
    val exit_left = find("slayer_cave_exit_left")
    val tunnel_right = find("slayer_cave_tunnel_right")
    val tunnel_left = find("slayer_cave_tunnel_left")
}

object StrongholdSlayerCaveSpawns : MapNpcSpawnBuilder() {
    override fun onPackMapTask() {
        resourceFile<StrongholdSlayerCaveScript>("npcs.toml")
    }
}
