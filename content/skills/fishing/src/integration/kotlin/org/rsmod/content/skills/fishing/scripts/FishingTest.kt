package org.rsmod.content.skills.fishing.scripts

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.stats
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.entity.Npc
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.npc.NpcType
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class FishingTest {
    @Test
    fun GameTestState.`netting at the swamp spot catches shrimps`() =
        runGameTest(Fishing::class) {
            val spot = setUp(FishingNpcs.lumbridge_sea_spot, level = 1, InvObj(FishingObjs.net))
            val xp = player.statMap.getFineXP(stats.fishing)

            player.opNpc1(spot)
            advance()
            assertMessageSent("You cast out your net...")
            advanceUntil({ player.count(FishingObjs.raw_shrimp) > 0 }, timeoutTicks = 200)

            assertMessageSent("You catch some shrimps.")
            assertTrue(player.statMap.getFineXP(stats.fishing) > xp)
        }

    @Test
    fun GameTestState.`luring trout uses up a feather per catch`() =
        runGameTest(Fishing::class) {
            val rod = InvObj(FishingObjs.fly_fishing_rod)
            val feathers = InvObj(FishingObjs.feather, 5)
            val spot = setUp(FishingNpcs.lumbridge_river_spot, level = 20, rod, feathers)

            player.opNpc1(spot)
            advanceUntil({ player.count(FishingObjs.raw_trout) > 0 }, timeoutTicks = 200)

            assertMessageSent("You catch a trout.")
            assertEquals(4, player.count(FishingObjs.feather))
        }

    @Test
    fun GameTestState.`fishing without a net says which tool is needed`() =
        runGameTest(Fishing::class) {
            val spot = setUp(FishingNpcs.lumbridge_sea_spot, level = 1)
            player.opNpc1(spot)
            advance()
            assertMessageSent("You need a small fishing net to catch these fish.")
        }

    @Test
    fun GameTestState.`a full inventory stops fishing`() =
        runGameTest(Fishing::class) {
            val spot = setUp(FishingNpcs.lumbridge_sea_spot, level = 1, InvObj(FishingObjs.net))
            player.fillInv()
            player.inv[0] = InvObj(FishingObjs.net)
            player.opNpc1(spot)
            advance()
            assertMessageSent("You can't carry any more fish.")
        }

    @Test
    fun GameTestState.`spots move to nearby fishable water when their timer runs out`() =
        runGameTest(Fishing::class) {
            advance()
            for ((type, home) in REAL_SPOTS) {
                val spot = spawnNpc(home, npcTypes[type])
                spot.aiTimer(1)
                advanceUntil({ spot.coords != home }, timeoutTicks = 5)
                assertTrue(spot.coords.chebyshevDistance(home) <= 2) { "Moved to ${spot.coords}" }
            }
        }

    private fun GameTestScope.setUp(spotType: NpcType, level: Int, vararg objs: InvObj): Npc {
        // A fresh test player's clock reads -1 until their first tick.
        advance()
        player.stats[stats.fishing] = level
        player.clearInv()
        objs.forEachIndexed { slot, obj -> player.inv[slot] = obj }
        val spot = spawnNpc(LAND, npcTypes[spotType])
        player.teleport(LAND.translateX(-1))
        return spot
    }

    private fun CoordGrid.chebyshevDistance(other: CoordGrid): Int =
        maxOf(kotlin.math.abs(x - other.x), kotlin.math.abs(z - other.z))

    private companion object {
        // The fishing logic doesn't need water, so these tests fish an open tile east of the
        // castle.
        val LAND = CoordGrid(0, 50, 50, 34, 31)
        // Real Lumbridge spawns, so the move test uses the real swamp coast and River Lum.
        val REAL_SPOTS =
            listOf(
                FishingNpcs.lumbridge_sea_spot to CoordGrid(0, 50, 49, 40, 10),
                FishingNpcs.lumbridge_river_spot to CoordGrid(0, 50, 50, 38, 52),
            )
    }
}
