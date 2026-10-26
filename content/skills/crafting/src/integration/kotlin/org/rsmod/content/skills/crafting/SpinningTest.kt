package org.rsmod.content.skills.crafting

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.components
import org.rsmod.api.config.refs.stats
import org.rsmod.api.testing.GameTestState
import org.rsmod.game.inv.InvObj
import org.rsmod.game.stat.PlayerStatMap
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class SpinningTest {
    @Test
    fun GameTestState.`spin two balls of wool and refuse flax below level 10`() =
        runGameTest(Spinning::class) {
            advance()
            player.stats[stats.crafting] = 1
            player.clearInv()
            repeat(3) { player.inv[it] = InvObj(CraftingObjs.wool) }
            player.inv[3] = InvObj(CraftingObjs.flax)
            val wheel = placeMapLoc(SPOT, CraftingLocs.spinning_wheel)
            // The wheel can only be used from its south side (force-approach flags).
            player.teleport(SPOT.translateZ(-1))
            val xp = player.statMap.getFineXP(stats.crafting)

            player.opLoc2(wheel)
            advance(ticks = 2)
            player.resumePauseButton(components.skillmulti_a, sub = 2)
            advanceUntil({ player.count(CraftingObjs.ball_of_wool) == 2 }, timeoutTicks = 20)
            advance(ticks = 6)
            assertEquals(2, player.count(CraftingObjs.ball_of_wool))
            assertEquals(1, player.count(CraftingObjs.wool))
            val gained = player.statMap.getFineXP(stats.crafting) - xp
            assertEquals(PlayerStatMap.toFineXP(5.0).toInt(), gained)

            player.opLoc2(wheel)
            advance(ticks = 2)
            player.resumePauseButton(components.skillmulti_b, sub = 1)
            advance()
            assertMessageSent("You need a Crafting level of 10 to make a bow string.")
            assertEquals(1, player.count(CraftingObjs.flax))
        }

    private companion object {
        val SPOT = CoordGrid(0, 50, 50, 34, 31)
    }
}
