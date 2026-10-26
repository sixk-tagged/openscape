package org.rsmod.content.skills.prayer

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.stat.stat
import org.rsmod.api.testing.GameTestState
import org.rsmod.game.inv.InvObj
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class PrayerTest {
    @Test
    fun GameTestState.`burying bones removes them and gives prayer xp`() =
        runGameTest(Prayer::class) {
            // A fresh test player's clock reads -1 until their first tick.
            advance()
            player.clearInv()
            player.inv[0] = InvObj(prayer_objs.big_bones)
            val xp = player.statMap.getFineXP(stats.prayer)

            // Each advance clears captured messages, so check each one in the tick it is sent.
            player.withProtectedAccess { opHeld1(0) }
            assertMessageSent("You dig a hole in the ground...")
            advance()

            assertNull(player.inv[0])
            assertTrue(player.statMap.getFineXP(stats.prayer) > xp)
            assertMessageSent("You bury the bones.")
        }

    @Test
    fun GameTestState.`praying at an altar restores prayer points`() =
        runGameTest(Prayer::class) {
            val altar = placeMapLoc(ALTAR, prayer_locs.altar)
            player.teleport(ALTAR.translateX(-1))
            player.setBaseLevel(stats.prayer, 50)
            player.setCurrentLevel(stats.prayer, 10)

            player.opLoc1(altar)
            advance()

            assertEquals(50, player.stat(stats.prayer))
            assertMessageSent("You recharge your Prayer points.")
        }

    @Test
    fun GameTestState.`praying with full prayer points says so`() =
        runGameTest(Prayer::class) {
            val altar = placeMapLoc(ALTAR, prayer_locs.altar)
            player.teleport(ALTAR.translateX(-1))
            player.setBaseLevel(stats.prayer, 50)
            player.setCurrentLevel(stats.prayer, 50)

            player.opLoc1(altar)
            advance()

            assertMessageSent("You already have full Prayer points.")
        }

    private companion object {
        val ALTAR = CoordGrid(0, 50, 50, 34, 31)
    }
}
