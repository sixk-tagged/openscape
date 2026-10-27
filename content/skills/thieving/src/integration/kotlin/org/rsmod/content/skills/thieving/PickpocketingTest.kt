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

    @Test
    fun GameTestState.`warriors and guards pay their wiki coins and xp`() =
        runGameTest(Pickpocketing::class) {
            player.teleport(SPOT)
            player.clearInv()
            player.stats[stats.thieving] = 40
            val warrior = spawnNpc(SPOT.translateX(1), npcTypes[ThievingNpcs.al_kharid_warrior])
            val guard = spawnNpc(SPOT.translateZ(1), npcTypes[ThievingNpcs.varrock_guard])
            // The guards Varrock spawns really offer Pickpocket as op3.
            for (type in VARROCK_GUARDS) {
                assertEquals("Pickpocket", npcTypes[type].op[2])
            }

            random.next = 0 // Rolls 0.00: success.
            player.opNpc3(warrior)
            advanceUntil({ player.count(objs.coins) > 0 }, timeoutTicks = 5)
            assertEquals(18, player.count(objs.coins))

            random.next = 0
            player.opNpc3(guard)
            advanceUntil({ player.count(objs.coins) > 18 }, timeoutTicks = 12)
            assertEquals(48, player.count(objs.coins))
        }

    @Test
    fun GameTestState.`a master farmer gives seeds`() =
        runGameTest(Pickpocketing::class) {
            player.teleport(SPOT)
            player.clearInv()
            player.stats[stats.thieving] = 38
            val farmer = spawnNpc(SPOT.translateX(1), npcTypes[ThievingNpcs.master_farmer_1])

            random.next = 0 // Success.
            random.then = 0 // The first seed of the table: potato.
            random.then = 3 // Its quantity.
            player.opNpc3(farmer)
            advanceUntil({ player.count(ThievingObjs.potato_seed) > 0 }, timeoutTicks = 5)
            assertEquals(3, player.count(ThievingObjs.potato_seed))
        }

    private companion object {
        val SPOT = CoordGrid(0, 50, 50, 34, 31)
        val VARROCK_GUARDS =
            with(ThievingNpcs) {
                listOf(
                    varrock_guard,
                    varrock_guard_2,
                    varrock_guard_3,
                    varrock_guard_f,
                    varrock_guard_f2,
                    varrock_guard_f3,
                    varrock_guard_captain,
                )
            }
    }
}
