package org.rsmod.content.other.water

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.player.events.interact.LocUEvents
import org.rsmod.api.testing.GameTestState
import org.rsmod.game.inv.InvObj
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class WaterSourcesTest {
    @Test
    fun GameTestState.`a sink fills every bucket but not the jug`() =
        runGameTest(WaterSources::class) {
            advance()
            player.clearInv()
            player.inv[0] = InvObj(WaterObjs.bucket)
            player.inv[1] = InvObj(WaterObjs.jug)
            player.inv[2] = InvObj(WaterObjs.bucket)
            val sink = placeMapLoc(SPOT, WaterLocs.sink)
            player.teleport(SPOT.translateX(-1))

            val event = LocUEvents.Op(sink, sink, locTypes[sink], objTypes[WaterObjs.bucket], 0)
            player.withProtectedAccess { eventBus.publish(this, event) }
            assertMessageSent("You fill the bucket from the sink.")
            advanceUntil(max = 10) { WaterObjs.bucket !in player.inv }

            assertEquals(2, player.count(WaterObjs.bucket_of_water))
            assertEquals(1, player.count(WaterObjs.jug))
        }

    private companion object {
        val SPOT = CoordGrid(0, 50, 50, 34, 31)
    }
}
