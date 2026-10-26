package org.rsmod.content.skills.mining.scripts

import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.righthand
import org.rsmod.api.testing.GameTestState
import org.rsmod.content.skills.mining.configs.MiningContent
import org.rsmod.content.skills.mining.configs.MiningObjs
import org.rsmod.content.skills.mining.configs.ProductRoll
import org.rsmod.content.skills.mining.scripts.Mining.Companion.rockBonusRolls
import org.rsmod.content.skills.mining.scripts.Mining.Companion.rockDepleteChance
import org.rsmod.content.skills.mining.scripts.Mining.Companion.rockDepleted
import org.rsmod.content.skills.mining.scripts.Mining.Companion.rockLevelReq
import org.rsmod.content.skills.mining.scripts.Mining.Companion.rockProduct
import org.rsmod.content.skills.mining.scripts.Mining.Companion.rockProductRoll
import org.rsmod.content.skills.mining.scripts.Mining.Companion.rockRespawnTime
import org.rsmod.content.skills.mining.scripts.Mining.Companion.rockXp
import org.rsmod.game.inv.InvObj
import org.rsmod.game.stat.PlayerStatMap
import org.rsmod.map.CoordGrid

class MiningScriptTest {
    @Test
    fun GameTestState.`level and pickaxe requirements`() =
        runGameTest(Mining::class) {
            val type = findLocType(MiningContent.rock) { it.rockLevelReq > 1 }
            val rock = placeMapLoc(ROCK_COORDS, type)
            player.teleport(rock.coords.translateX(-1))
            player.clearInv()

            player.stats[stats.mining] = type.rockLevelReq - 1
            player.opLoc1(rock)
            advance(ticks = 1)
            assertMessageSent("You need a Mining level of ${type.rockLevelReq} to mine this rock.")

            player.stats[stats.mining] = type.rockLevelReq
            player.opLoc1(rock)
            advance(ticks = 2)
            assertMessagesSent(
                "You need a pickaxe to mine this rock.",
                "You do not have a pickaxe which you have the Mining level to use.",
            )

            player.actionDelay = -1 // Reset the delay set by the first attempt.
            player.righthand = InvObj(objs.bronze_pickaxe)
            player.opLoc1(rock)
            advance(ticks = 2)
            assertMessageSent("You swing your pick at the rock.")
        }

    @Test
    fun GameTestState.`full inventory stops mining`() =
        runGameTest(Mining::class) {
            val type =
                findLocType(MiningContent.rock) {
                    it.rockLevelReq == 1 && it.rockDepleteChance == 0
                }
            val rock = placeMapLoc(ROCK_COORDS, type)
            val productName = objTypes[type.rockProduct].name.lowercase()
            player.teleport(rock.coords.translateX(-1))
            player.righthand = InvObj(objs.bronze_pickaxe)
            player.fillInv()
            player.opLoc1(rock)
            advance(ticks = 1)
            assertMessageSent("Your inventory is too full to hold any more $productName.")
        }

    @Test
    fun GameTestState.`mine, deplete and respawn a rock`() =
        runGameTest(Mining::class) {
            val type =
                findLocType(MiningContent.rock) {
                    it.rockLevelReq == 1 &&
                        it.rockDepleteChance == 0 &&
                        it.rockProductRoll == ProductRoll.FIXED
                }
            val rock = placeMapLoc(ROCK_COORDS, type)
            val product = type.rockProduct
            player.teleport(rock.coords.translateX(-1))
            player.clearInv()
            player.righthand = InvObj(objs.bronze_pickaxe)
            player.stats[stats.mining] = 1
            val xpBefore = player.statMap.getFineXP(stats.mining)

            player.opLoc1(rock)
            advance(ticks = 2)
            assertMessageSent("You swing your pick at the rock.")
            advance(ticks = 1)
            assertDoesNotContain(player.inv, product)

            random.next = 0 // Success roll.
            if (type.rockBonusRolls) {
                random.then = 2 // No random gem.
                random.then = 2 // No golden nugget.
            }
            advance(ticks = 1)
            assertContains(player.inv, product)
            assertEquals(
                PlayerStatMap.toFineXP(type.rockXp).toInt(),
                player.statMap.getFineXP(stats.mining) - xpBefore,
            )
            assertDoesNotExist(rock)
            assertExists(rock.coords, type.rockDepleted)

            advance(ticks = type.rockRespawnTime)
            assertExists(rock)

            // The loop ended when the rock depleted: no more ore even once it is back.
            random.next = 0
            advance(ticks = 10)
            assertEquals(1, player.count(product))
        }

    @Test
    fun GameTestState.`essence rock never depletes and switches to pure essence at 30`() =
        runGameTest(Mining::class) {
            val type = findLocType(MiningContent.rock) { it.rockProductRoll == ProductRoll.ESSENCE }
            val rock = placeMapLoc(ROCK_COORDS, type)
            player.teleport(rock.coords.translateX(-1))
            player.clearInv()
            player.righthand = InvObj(objs.bronze_pickaxe)
            player.stats[stats.mining] = 29

            player.opLoc1(rock)
            advance(ticks = 3)
            random.next = 0
            advance(ticks = 1)
            assertContains(player.inv, MiningObjs.rune_essence)
            assertExists(rock)

            player.stats[stats.mining] = 30
            random.next = 0
            advance(ticks = 8) // Bronze pickaxe rolls every 8 ticks.
            assertContains(player.inv, MiningObjs.pure_essence)
            assertExists(rock)
        }

    @Test
    fun GameTestState.`depleted rock says it is empty`() =
        runGameTest(Mining::class) {
            val type = findLocType(MiningContent.depleted_rock)
            val rock = placeMapLoc(ROCK_COORDS, type)
            player.teleport(rock.coords.translateX(-1))
            player.opLoc1(rock)
            advance(ticks = 1)
            assertMessageSent(Mining.NO_ORE)
        }

    private companion object {
        val ROCK_COORDS = CoordGrid(0, 50, 50, 34, 31)
    }
}
