package org.rsmod.content.skills.thieving

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.stats
import org.rsmod.api.testing.GameTestState
import org.rsmod.game.stat.PlayerStatMap
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class StallsTest {
    @Test
    fun GameTestState.`steal a cup of tea, then wait for the stall to respawn`() =
        runGameTest(StallThieving::class) {
            val coords = SPOT.translateX(1)
            val stall = placeMapLoc(coords, ThievingLocs.tea_stall)
            player.teleport(SPOT)
            player.clearInv()

            player.stats[stats.thieving] = 4
            player.opLoc2(stall)
            advanceUntil(max = 5) {
                runCatching {
                        assertMessageSent(
                            "You need a Thieving level of 5 to steal from this stall."
                        )
                    }
                    .isSuccess
            }

            player.stats[stats.thieving] = 5
            val xp = player.statMap.getFineXP(stats.thieving)
            player.opLoc2(stall)
            advanceUntil({ player.count(ThievingStallObjs.cup_of_tea) > 0 }, timeoutTicks = 6)
            assertMessageSent("You steal a cup of tea.")
            val gained = player.statMap.getFineXP(stats.thieving) - xp
            assertEquals(PlayerStatMap.toFineXP(16.0).toInt(), gained)
            assertTrue(locExists(coords, ThievingLocs.market))
            assertFalse(locExists(coords, ThievingLocs.tea_stall))

            advance(ticks = 5)
            assertTrue(locExists(coords, ThievingLocs.tea_stall))
        }

    @Test
    fun GameTestState.`a market guard in sight goes for a seed thief`() =
        runGameTest(StallThieving::class) {
            // Draynor's real west seed stall and the south Market Guard's spawn, across open
            // ground.
            val stall = placeMapLoc(CoordGrid(3075, 3249), ThievingLocs.seed_stall)
            player.teleport(CoordGrid(3077, 3249))
            player.clearInv()
            player.stats[stats.thieving] = 27
            val guard = spawnNpc(CoordGrid(3083, 3247), npcTypes[ThievingStallNpcs.market_guard])
            val xp = player.statMap.getFineXP(stats.thieving)

            player.opLoc2(stall)
            advanceUntil({ player.statMap.getFineXP(stats.thieving) > xp }, timeoutTicks = 6)
            assertTrue(guard.queueList.isNotEmpty)
            // Olivia holds a grudge for 10 minutes of playtime (it starts at 0 here).
            assertEquals(1000, player.vars[ThievingVarps.olivia_angry_until])
        }

    private companion object {
        val SPOT = CoordGrid(0, 50, 50, 34, 31)
    }
}
