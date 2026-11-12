package org.rsmod.content.skills.thieving

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.testing.GameTestState
import org.rsmod.game.inv.InvObj
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class ChestsTest {
    @Test
    fun GameTestState.`searching a chest for traps gives its coins and turns it empty`() =
        runGameTest(ChestThieving::class) {
            val coords = SPOT.translateZ(-1)
            val chest = placeMapLoc(coords, ChestLocs.coins_10)
            player.teleport(SPOT)
            advance()
            player.clearInv()
            player.stats[stats.thieving] = 13

            player.opLoc2(chest)
            advanceUntil({ player.count(objs.coins) > 0 }, timeoutTicks = 8)

            assertEquals(10, player.count(objs.coins))
            assertTrue(locExists(coords, ChestLocs.empty))
        }

    @Test
    fun GameTestState.`opening a trapped chest hurts and gives nothing`() =
        runGameTest(ChestThieving::class) {
            val coords = SPOT.translateZ(-1)
            val chest = placeMapLoc(coords, ChestLocs.coins_10)
            player.teleport(SPOT)
            advance()
            player.clearInv()
            player.stats[stats.thieving] = 13
            player.setBaseLevel(stats.hitpoints, 50)
            player.setCurrentLevel(stats.hitpoints, 50)
            val hp = player.hitpoints

            player.opLoc1(chest)
            advanceUntil({ player.hitpoints < hp }, timeoutTicks = 8)

            assertMessageSent("You have activated a trap on the chest.")
            assertEquals(41, player.hitpoints)
            assertEquals(0, player.count(objs.coins))
            assertTrue(locExists(coords, ChestLocs.coins_10))
        }

    @Test
    fun GameTestState.`a full inventory takes none of the loot and leaves the chest full`() =
        runGameTest(ChestThieving::class) {
            val coords = SPOT.translateZ(-1)
            val chest = placeMapLoc(coords, ChestLocs.castle)
            player.teleport(SPOT)
            advance()
            player.clearInv()
            player.inv[0] = InvObj(objs.coins, 5)
            for (slot in 1 until player.inv.size) {
                player.inv[slot] = InvObj(ChestObjs.nature_rune, 1)
            }
            player.stats[stats.thieving] = 72

            player.opLoc2(chest)
            advance(ticks = 6)

            assertEquals(5, player.count(objs.coins))
            assertTrue(locExists(coords, ChestLocs.castle))
        }

    @Test
    fun GameTestState.`the castle chest needs level 72 and sends you to Witchaven`() =
        runGameTest(ChestThieving::class) {
            val chest = placeMapLoc(SPOT.translateZ(-1), ChestLocs.castle)
            player.teleport(SPOT)
            advance()
            player.clearInv()

            player.stats[stats.thieving] = 71
            player.opLoc2(chest)
            advance()
            assertMessageSent("You find nothing.")

            player.stats[stats.thieving] = 72
            player.opLoc2(chest)
            advanceUntil({ player.count(objs.coins) > 0 }, timeoutTicks = 8)
            assertEquals(1000, player.count(objs.coins))
            advance(ticks = 2)
            assertEquals(CoordGrid(2696, 3281, 0), player.coords)
        }

    private companion object {
        val SPOT = CoordGrid(0, 50, 50, 34, 31)
    }
}
