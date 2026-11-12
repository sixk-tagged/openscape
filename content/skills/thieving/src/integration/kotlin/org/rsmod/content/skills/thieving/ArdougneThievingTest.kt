package org.rsmod.content.skills.thieving

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.varns
import org.rsmod.api.testing.GameTestState
import org.rsmod.game.inv.InvObj
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class ArdougneThievingTest {
    @Test
    fun GameTestState.`the silk stall needs level 20, then gives silk and respawns in 8 ticks`() =
        runGameTest(StallThieving::class) {
            val coords = SPOT.translateX(1)
            val stall = placeMapLoc(coords, ArdougneStallLocs.silk)
            player.teleport(SPOT)
            player.clearInv()

            player.stats[stats.thieving] = 19
            player.opLoc2(stall)
            advance()
            assertMessageSent("You need a Thieving level of 20 to steal from this stall.")

            player.stats[stats.thieving] = 20
            player.opLoc2(stall)
            advanceUntil({ player.count(ArdougneStallObjs.silk) > 0 }, timeoutTicks = 6)
            assertMessageSent("You steal a silk.")
            assertTrue(locExists(coords, ThievingLocs.market))

            advance(ticks = 8)
            assertTrue(locExists(coords, ArdougneStallLocs.silk))
        }

    @Test
    fun GameTestState.`a paladin pays 80 coins and 2 chaos runes`() =
        runGameTest(Pickpocketing::class) {
            player.teleport(SPOT)
            player.clearInv()
            player.stats[stats.thieving] = 70
            val paladin = spawnNpc(SPOT.translateX(1), npcTypes[ArdougneNpcs.paladins.first()])

            random.next = 0 // Rolls 0.00: success.
            player.opNpc3(paladin)
            advanceUntil({ player.count(objs.coins) > 0 }, timeoutTicks = 5)
            assertEquals(80, player.count(objs.coins))
            assertEquals(2, player.count(ArdougneStallObjs.chaos_rune))
        }

    @Test
    fun GameTestState.`a paladin gives nothing when the chaos runes do not fit`() =
        runGameTest(Pickpocketing::class) {
            player.teleport(SPOT)
            player.clearInv()
            player.inv[0] = InvObj(objs.coins, 5)
            for (slot in 1 until player.inv.size) {
                player.inv[slot] = InvObj(ArdougneStallObjs.silk, 1)
            }
            player.stats[stats.thieving] = 70
            val paladin = spawnNpc(SPOT.translateX(1), npcTypes[ArdougneNpcs.paladins.first()])
            val xp = player.statMap.getFineXP(stats.thieving)

            random.next = 0
            player.opNpc3(paladin)
            advance(ticks = 4)

            assertEquals(5, player.count(objs.coins))
            assertEquals(0, player.count(ArdougneStallObjs.chaos_rune))
            assertEquals(xp, player.statMap.getFineXP(stats.thieving))
        }

    @Test
    fun GameTestState.`a knight does not stop a theft from the baker's stall`() =
        runGameTest(StallThieving::class) {
            val coords = SPOT.translateX(1)
            val stall = placeMapLoc(coords, ArdougneStallLocs.baker)
            player.teleport(SPOT)
            player.clearInv()
            player.stats[stats.thieving] = 5
            val knight = spawnNpc(SPOT.translateZ(2), npcTypes[ArdougneNpcs.knights.first()])

            player.opLoc2(stall)
            advanceUntil(max = 6) { player.inv.isNotEmpty() }

            assertFalse(knight.queueList.isNotEmpty)
            assertTrue(locExists(coords, ThievingLocs.market))
        }

    @Test
    fun GameTestState.`a guard in sight stops the theft before anything is taken`() =
        runGameTest(StallThieving::class) {
            // The same open ground and north-south layout as Draynor's guard test.
            val coords = OPEN_GROUND.translateZ(2)
            val stall = placeMapLoc(coords, ArdougneStallLocs.silk)
            player.teleport(OPEN_GROUND)
            player.clearInv()
            player.stats[stats.thieving] = 20
            val guard = spawnNpc(OPEN_GROUND.translateZ(-1), npcTypes[ArdougneNpcs.ardougne_guard])
            val xp = player.statMap.getFineXP(stats.thieving)

            player.opLoc2(stall)
            advanceUntil(max = 10) { guard.vars[varns.lastcombat] > 0 }

            assertEquals(0, player.count(ArdougneStallObjs.silk))
            assertEquals(xp, player.statMap.getFineXP(stats.thieving))
            assertTrue(locExists(coords, ArdougneStallLocs.silk))
            assertTrue(guard.vars[varns.lastcombat] > 0)
        }

    @Test
    fun GameTestState.`a gnome needs level 75`() =
        runGameTest(Pickpocketing::class) {
            player.teleport(SPOT)
            player.clearInv()
            player.stats[stats.thieving] = 75
            val gnome = spawnNpc(SPOT.translateX(1), npcTypes[ArdougneNpcs.gnomes.first()])

            random.next = 0
            player.opNpc3(gnome)
            advanceUntil({ player.inv.isNotEmpty() }, timeoutTicks = 5)
            assertMessageSent("You pick the gnome's pocket.")
        }

    private companion object {
        val OPEN_GROUND = CoordGrid(0, 50, 50, 30, 18)
        val SPOT = CoordGrid(0, 50, 50, 34, 31)
    }
}
