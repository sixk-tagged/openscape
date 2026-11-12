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
import org.rsmod.routefinder.flag.CollisionFlag

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
    fun GameTestState.`the Musa Point cage spot catches lobsters and the harpoon swordfish`() =
        runGameTest(Fishing::class) {
            val pot = InvObj(FishingObjs.lobster_pot)
            val spot = setUp(FishingNpcs.musa_cage_spot, level = 99, pot)

            player.opNpc1(spot)
            advanceUntil({ player.count(FishingObjs.raw_lobster) > 0 }, timeoutTicks = 400)
            assertMessageSent("You catch a lobster.")

            player.inv[0] = InvObj(FishingObjs.harpoon)
            player.opNpc3(spot)
            advanceUntil({ player.count(FishingObjs.raw_swordfish) > 0 }, timeoutTicks = 400)
            assertMessageSent("You catch a swordfish.")
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

    @Test
    fun GameTestState.`a spot that moves away stops whoever was fishing it`() =
        runGameTest(Fishing::class) {
            advance()
            player.stats[stats.fishing] = 1
            player.clearInv()
            player.inv[0] = InvObj(FishingObjs.net)
            val (type, home) = REAL_SPOTS.first()
            val spot = spawnNpc(home, npcTypes[type])
            val shore =
                listOf(
                        home.translateX(-1),
                        home.translateX(1),
                        home.translateZ(-1),
                        home.translateZ(1),
                    )
                    .first { collision[it.x, it.z, it.level] and CollisionFlag.BLOCK_WALK == 0 }
            player.teleport(shore)
            advance()

            player.opNpc1(spot)
            advance(ticks = 2)
            spot.aiTimer(1)
            advanceUntil({ spot.coords != home }, timeoutTicks = 5)
            advance(ticks = 3)

            assertEquals(null, player.interaction)
            assertEquals(shore, player.coords)
        }

    @Test
    fun GameTestState.`no spot walks off its water, whichever area it is in`() =
        runGameTest(Fishing::class) {
            advance()
            val spots = ALL_SPOTS.map { (type, home) -> spawnNpc(home, npcTypes[type]) }
            repeat(600) {
                advance()
                for (spot in spots) {
                    val flags = collision[spot.coords.x, spot.coords.z, spot.coords.level]
                    assertTrue(flags and CollisionFlag.BLOCK_WALK != 0) {
                        "${spot.name} stands on land at ${spot.coords}"
                    }
                }
            }
        }

    @Test
    fun GameTestState.`a spot that has moved stays where it moved to until its next timer`() =
        runGameTest(Fishing::class) {
            advance()
            for ((type, home) in ALL_SPOTS.filter { it.first !in FishingData.fixed }) {
                val spot = spawnNpc(home, npcTypes[type])
                spot.aiTimer(1)
                advanceUntil({ spot.coords != home }, timeoutTicks = 5)
                val moved = spot.coords
                spot.aiTimer(10_000)
                advance(ticks = 600)
                assertTrue(spot.coords == moved) { "${spot.name} went back to its spawn" }
            }
        }

    @Test
    fun GameTestState.`a spot never relocates onto the mud bank beside the Lumbridge tutor`() =
        runGameTest(Fishing::class) {
            advance()
            val spot = spawnNpc(SWAMP_TUTOR_SPOT, npcTypes[FishingNpcs.lumbridge_sea_spot])
            repeat(200) {
                spot.aiTimer(1)
                advance(ticks = 2)
                assertTrue(spot.coords != SWAMP_MUD_BANK) { "Spot moved onto the mud bank" }
            }
        }

    @Test
    fun GameTestState.`the lava spot is baited with an oily rod and uses up bait`() =
        runGameTest(Fishing::class) {
            val rod = InvObj(FishingObjs.oily_fishing_rod)
            val bait = InvObj(FishingObjs.fishing_bait, 5)
            val spot = setUp(FishingNpcs.lava_eel_spot, level = 99, rod, bait)

            player.opNpc1(spot)
            advanceUntil({ player.count(FishingObjs.raw_lava_eel) > 0 }, timeoutTicks = 400)

            assertMessageSent("You catch a lava eel.")
            assertEquals(4, player.count(FishingObjs.fishing_bait))
        }

    @Test
    fun GameTestState.`the lava spot needs level 53 and an oily rod`() =
        runGameTest(Fishing::class) {
            val spot = setUp(FishingNpcs.lava_eel_spot, level = 52, InvObj(FishingObjs.fishing_rod))
            player.opNpc1(spot)
            advance()
            assertMessageSent("You need a Fishing level of at least 53 to bait these fish.")

            player.stats[stats.fishing] = 53
            player.opNpc1(spot)
            advance()
            assertMessageSent("You need an oily fishing rod to bait these fish.")
        }

    @Test
    fun GameTestState.`dark crabs take a lobster pot and dark fishing bait`() =
        runGameTest(Fishing::class) {
            val pot = InvObj(FishingObjs.lobster_pot)
            val bait = InvObj(FishingObjs.wilderness_fishing_bait, 5)
            val spot = setUp(FishingNpcs.resource_area_crab_spot, level = 99, pot, bait)

            player.opNpc1(spot)
            advanceUntil({ player.count(FishingObjs.raw_dark_crab) > 0 }, timeoutTicks = 800)

            assertMessageSent("You catch a dark crab.")
            assertEquals(4, player.count(FishingObjs.wilderness_fishing_bait))
        }

    @Test
    fun GameTestState.`dark crabs need level 85`() =
        runGameTest(Fishing::class) {
            val pot = InvObj(FishingObjs.lobster_pot)
            val spot = setUp(FishingNpcs.wilderness_crab_spot, level = 84, pot)
            player.opNpc1(spot)
            advance()
            assertMessageSent("You need a Fishing level of at least 85 to catch these fish.")
        }

    @Test
    fun GameTestState.`the Resource Area crab spots never relocate`() =
        runGameTest(Fishing::class) {
            advance()
            val spot = spawnNpc(RESOURCE_CRAB_SPOT, npcTypes[FishingNpcs.resource_area_crab_spot])
            advance(ticks = 700)
            assertTrue(spot.coords == RESOURCE_CRAB_SPOT) { "Moved to ${spot.coords}" }
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
        // One real spawn per spot type: the tests need the real coast and river collision.
        val ALL_SPOTS =
            listOf(
                FishingNpcs.lumbridge_sea_spot to CoordGrid(0, 50, 49, 40, 10),
                FishingNpcs.lumbridge_river_spot to CoordGrid(0, 50, 50, 38, 52),
                FishingNpcs.draynor_sea_spot to CoordGrid(0, 48, 50, 13, 30),
                FishingNpcs.alkharid_sea_spot to CoordGrid(0, 51, 49, 12, 4),
                FishingNpcs.barbarian_river_spot to CoordGrid(0, 48, 53, 32, 32),
                FishingNpcs.musa_sea_spot to CoordGrid(0, 45, 49, 43, 43),
                FishingNpcs.mudskipper_sea_spot to CoordGrid(0, 46, 49, 42, 40),
                FishingNpcs.musa_cage_spot to CoordGrid(0, 45, 49, 43, 44),
                FishingNpcs.bandit_camp_sea_spot to CoordGrid(0, 47, 57, 42, 56),
                FishingNpcs.lava_eel_spot to CoordGrid(0, 47, 59, 62, 63),
                FishingNpcs.wilderness_crab_spot to CoordGrid(0, 52, 59, 21, 17),
                FishingNpcs.sinclair_river_spot to CoordGrid(0, 42, 55, 26, 12),
                FishingNpcs.catherby_sea_spot to CoordGrid(0, 44, 53, 20, 39),
                FishingNpcs.catherby_cage_spot to CoordGrid(0, 44, 53, 28, 37),
                FishingNpcs.catherby_net_spot to CoordGrid(0, 44, 53, 30, 37),
                FishingNpcs.guild_cage_spot to CoordGrid(0, 40, 53, 39, 27),
                FishingNpcs.guild_net_spot to CoordGrid(0, 40, 53, 41, 30),
                FishingNpcs.observatory_river_spot to CoordGrid(0, 38, 49, 29, 14),
                FishingNpcs.ardougne_river_spot to CoordGrid(0, 40, 52, 1, 46),
                FishingNpcs.gnome_river_spot to CoordGrid(0, 37, 53, 14, 22),
                FishingNpcs.baxtorian_river_spot to CoordGrid(0, 39, 53, 12, 29),
            )
        val RESOURCE_CRAB_SPOT = CoordGrid(0, 49, 61, 45, 22)
        // The northernmost swamp spot, and the blocked mud tile two squares from it that is not
        // water: it has floor blocked on only three of its eight sides.
        val SWAMP_TUTOR_SPOT = CoordGrid(0, 50, 49, 46, 19)
        val SWAMP_MUD_BANK = CoordGrid(0, 50, 49, 45, 21)
    }
}
