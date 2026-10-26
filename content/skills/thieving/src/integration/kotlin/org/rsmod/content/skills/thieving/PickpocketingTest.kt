package org.rsmod.content.skills.thieving

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.testing.GameTestState
import org.rsmod.game.stat.PlayerStatMap
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class PickpocketingTest {
    @Test
    fun GameTestState.`pick a man's pocket, then get caught and stunned`() =
        runGameTest(Pickpocketing::class) {
            player.teleport(SPOT)
            player.clearInv()
            val man = spawnNpc(SPOT.translateX(1), npcTypes[ThievingNpcs.man])
            val xp = player.statMap.getFineXP(stats.thieving)

            // advance() clears messages every tick: stop on the tick the loot or hit lands.
            random.next = 0 // Rolls 0.00: success.
            player.opNpc3(man)
            advanceUntil({ player.count(objs.coins) > 0 }, timeoutTicks = 5)
            assertMessageSent("You pick the man's pocket.")
            assertEquals(3, player.count(objs.coins))
            val gained = player.statMap.getFineXP(stats.thieving) - xp
            assertEquals(PlayerStatMap.toFineXP(8.0).toInt(), gained)

            val hp = player.hitpoints
            random.next = 99 // Rolls 0.99: caught.
            player.opNpc3(man)
            advanceUntil({ player.hitpoints < hp }, timeoutTicks = 5)
            assertMessageSent("You fail to pick the man's pocket.")
            assertEquals(hp - 1, player.hitpoints)
            assertEquals(3, player.count(objs.coins))
        }

    @Test
    fun GameTestState.`farmers need Thieving level 10`() =
        runGameTest(Pickpocketing::class) {
            player.teleport(SPOT)
            val farmer = spawnNpc(SPOT.translateX(1), npcTypes[ThievingNpcs.farmer1])
            player.opNpc3(farmer)
            advance()
            assertMessageSent("You need a Thieving level of 10 to pickpocket the farmer.")
        }

    private companion object {
        val SPOT = CoordGrid(0, 50, 50, 34, 31)
    }
}
