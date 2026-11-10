package org.rsmod.content.areas.dungeon.swampcaves

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.other.lightsources.LightSourceObjs
import org.rsmod.game.inv.InvObj
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class SwampCaveTest {
    @Test
    fun GameTestState.`insects bite in the dark`() = caveTest { deps ->
        setUp(hitpoints = 99)
        deps.inCaves(DEEP_IN)
        player.teleport(DEEP_IN)
        advance(ticks = 29)
        assertEquals(99, player.hitpoints)
        advanceUntil(max = 10) { player.hitpoints < 99 }
    }

    @Test
    fun GameTestState.`a lantern keeps the insects off`() = caveTest { deps ->
        setUp(hitpoints = 99)
        player.inv[0] = InvObj(LightSourceObjs.bullseye_lantern_lit)
        deps.inCaves(DEEP_IN)
        player.teleport(DEEP_IN)
        advance(ticks = 40)
        assertEquals(99, player.hitpoints)
    }

    @Test
    fun GameTestState.`without a light the player climbs back up the rope`() = caveTest { deps ->
        setUp(hitpoints = 10)
        deps.inCaves(ROPE_LANDING)
        player.teleport(ROPE_LANDING)
        advanceUntil(max = 5) { player.coords == SWAMP_LANDING }
    }

    @Test
    fun GameTestState.`a candle sets off the swamp gas`() = caveTest { deps ->
        setUp(hitpoints = 40)
        player.inv[0] = InvObj(LightSourceObjs.candle_lit)
        deps.inCaves(GAS)
        player.teleport(GAS)
        advanceUntil(max = 15) { player.inv[0]?.id == LightSourceObjs.candle_unlit.id }
        assertEquals(30, player.hitpoints)
    }

    @Test
    fun GameTestState.`a wall beast grabs a player without a spiny helmet`() = caveTest { deps ->
        setUp(hitpoints = 99)
        player.inv[0] = InvObj(LightSourceObjs.bullseye_lantern_lit)
        spawnNpc(HOLE, npcTypes[SwampCaveNpcs.hole])
        deps.inCaves(HOLE.translateZ(-1))
        player.teleport(HOLE.translateZ(-1))
        advanceUntil(max = 5) { player.hitpoints < 99 }
    }

    @Test
    fun GameTestState.`a spiny helmet brings the wall beast out to fight`() = caveTest { deps ->
        setUp(hitpoints = 99)
        player.inv[0] = InvObj(LightSourceObjs.bullseye_lantern_lit)
        player.worn[HEAD_SLOT] = InvObj(SwampCaveObjs.helmets.first())
        val hole = spawnNpc(HOLE, npcTypes[SwampCaveNpcs.hole])
        deps.inCaves(HOLE.translateZ(-1))
        player.teleport(HOLE.translateZ(-1))
        advanceUntil(max = 5) { hole.visType.id == npcTypes[SwampCaveNpcs.wall_beast].id }
        assertTrue(player.hitpoints > 80)
    }

    private fun GameTestScope.setUp(hitpoints: Int) {
        player.clearInv()
        player.setBaseLevel(stats.hitpoints, hitpoints)
        player.setCurrentLevel(stats.hitpoints, hitpoints)
    }

    private companion object {
        const val HEAD_SLOT = 0

        val ROPE_LANDING = CoordGrid(3168, 9572)
        val SWAMP_LANDING = CoordGrid(3169, 3173)
        val DEEP_IN = CoordGrid(3230, 9580)
        val GAS = CoordGrid(3165, 9590)
        val HOLE = CoordGrid(3215, 9560)
    }
}
