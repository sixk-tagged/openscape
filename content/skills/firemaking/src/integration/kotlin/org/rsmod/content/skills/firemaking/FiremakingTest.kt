package org.rsmod.content.skills.firemaking

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.events.interact.HeldUEvents
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.obj.ObjType
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class FiremakingTest {
    @Test
    fun GameTestState.`lighting logs makes a fire, gives xp and steps away`() =
        runGameTest(Firemaking::class) {
            setUp(level = 1, objs.logs)
            val xp = player.statMap.getFineXP(stats.firemaking)

            useTinderboxOnLogs(objs.logs)
            assertMessageSent("You attempt to light the logs.")
            advanceUntil({ locExists(LAND, FiremakingLocs.fire) }, timeoutTicks = 200)

            assertMessageSent("The fire catches and the logs begin to burn.")
            assertTrue(player.statMap.getFineXP(stats.firemaking) > xp)
            assertNull(player.inv[1])
            advance(ticks = 2)
            assertNotEquals(LAND, player.coords)
        }

    @Test
    fun GameTestState.`fires can't be lit on top of a loc`() =
        runGameTest(Firemaking::class) {
            setUp(level = 1, objs.logs)
            placeMapLoc(LAND, FiremakingLocs.fire)
            useTinderboxOnLogs(objs.logs)
            assertMessageSent("You can't light a fire here.")
        }

    @Test
    fun GameTestState.`oak logs need firemaking level 15`() =
        runGameTest(Firemaking::class) {
            setUp(level = 14, objs.oak_logs)
            useTinderboxOnLogs(objs.oak_logs)
            assertMessageSent("You need a Firemaking level of 15 to burn oak logs.")
        }

    private fun GameTestScope.setUp(level: Int, log: ObjType) {
        // A fresh test player's clock reads -1 until their first tick.
        advance()
        // The dropped log is owned by the player's observer id, which a test player lacks.
        player.observerUUID = player.uuid
        player.stats[stats.firemaking] = level
        player.clearInv()
        player.inv[0] = InvObj(objs.tinderbox)
        player.inv[1] = InvObj(log)
        player.teleport(LAND)
    }

    private fun GameTestScope.useTinderboxOnLogs(log: ObjType) {
        val event = HeldUEvents.Type(objTypes[objs.tinderbox], 0, objTypes[log], 1)
        player.withProtectedAccess { eventBus.publish(this, event) }
    }

    private companion object {
        // The Lumbridge spawn tile: open ground with no loc or decoration on it.
        val LAND = CoordGrid(0, 50, 50, 30, 18)
    }
}
