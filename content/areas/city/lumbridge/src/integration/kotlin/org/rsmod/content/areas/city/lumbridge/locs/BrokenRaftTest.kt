package org.rsmod.content.areas.city.lumbridge.locs

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.quiver
import org.rsmod.api.player.righthand
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.inv.InvObj
import org.rsmod.game.loc.LocShape
import org.rsmod.game.type.stat.StatType

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class BrokenRaftTest {
    @Test
    fun GameTestState.`grapple crosses west to east and back`() =
        runGameTest(BrokenRaft::class) {
            readyToGrapple()
            val raft = placeRaft()
            player.teleport(BrokenRaft.WEST_BANK)
            random.next = 1 // the grapple holds
            player.opLoc1(raft)
            advanceUntil(max = 20) { player.coords == BrokenRaft.EAST_BANK }
            random.next = 1
            player.opLoc1(raft)
            advanceUntil(max = 20) { player.coords == BrokenRaft.WEST_BANK }
        }

    @Test
    fun GameTestState.`the grapple can break`() =
        runGameTest(BrokenRaft::class) {
            readyToGrapple()
            random.next = 0 // the 1/25 break
            player.teleport(BrokenRaft.WEST_BANK)
            player.opLoc1(placeRaft())
            advanceUntil(max = 20) { player.coords == BrokenRaft.EAST_BANK }
            assertNull(player.quiver)
        }

    @Test
    fun GameTestState.`missing levels or gear refuses`() =
        runGameTest(BrokenRaft::class) {
            readyToGrapple()
            level(stats.ranged, 36)
            val raft = placeRaft()
            player.teleport(BrokenRaft.WEST_BANK)
            player.opLoc1(raft)
            advanceUntil(max = 10) { messageSent(LEVELS_MESSAGE) }
            assertEquals(BrokenRaft.WEST_BANK, player.coords)

            level(stats.ranged, 37)
            player.quiver = null
            player.opLoc1(raft)
            advanceUntil(max = 10) { messageSent(GEAR_MESSAGE) }
            assertEquals(BrokenRaft.WEST_BANK, player.coords)
        }

    @Test
    fun GameTestState.`free players cannot grapple`() =
        runGameTest(BrokenRaft::class) {
            readyToGrapple()
            player.members = false
            player.teleport(BrokenRaft.WEST_BANK)
            player.opLoc1(placeRaft())
            advanceUntil(max = 10) { messageSent(MEMBERS_MESSAGE) }
            assertEquals(BrokenRaft.WEST_BANK, player.coords)
        }

    private fun GameTestScope.readyToGrapple() {
        player.members = true
        level(stats.agility, 8)
        level(stats.strength, 19)
        level(stats.ranged, 37)
        player.righthand = InvObj(objs.dragon_hunter_crossbow)
        player.quiver = InvObj(BrokenRaftObjs.mith_grapple)
    }

    private fun GameTestScope.placeRaft() =
        placeMapLoc(BrokenRaft.RAFT, BrokenRaftLocs.raft, shape = LocShape.GroundDecor)

    private fun GameTestScope.level(stat: StatType, level: Int) {
        player.setBaseLevel(stat, level)
        player.setCurrentLevel(stat, level)
    }

    /** Messages only last the tick they are sent in, so check while advancing. */
    private fun GameTestScope.messageSent(text: String): Boolean =
        runCatching { assertMessageSent(text) }.isSuccess

    private companion object {
        const val MEMBERS_MESSAGE = "You need to be on a members' world to use this shortcut."
        const val LEVELS_MESSAGE =
            "You need an Agility level of 8, a Strength level of 19 and a Ranged level of 37 " +
                "to do this."
        const val GEAR_MESSAGE = "You need a crossbow and a mith grapple equipped to do this."
    }
}
