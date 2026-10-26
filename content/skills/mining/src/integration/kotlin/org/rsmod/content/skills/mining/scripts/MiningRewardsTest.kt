package org.rsmod.content.skills.mining.scripts

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.feet
import org.rsmod.api.player.hands
import org.rsmod.api.player.hat
import org.rsmod.api.player.legs
import org.rsmod.api.player.righthand
import org.rsmod.api.player.torso
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.testing.GameTestState
import org.rsmod.content.skills.mining.ProspectorXpMod
import org.rsmod.content.skills.mining.configs.MiningContent
import org.rsmod.content.skills.mining.configs.MiningObjs
import org.rsmod.content.skills.mining.configs.ProductRoll
import org.rsmod.content.skills.mining.scripts.Mining.Companion.rockDepleteChance
import org.rsmod.content.skills.mining.scripts.Mining.Companion.rockLevelReq
import org.rsmod.content.skills.mining.scripts.Mining.Companion.rockProduct
import org.rsmod.content.skills.mining.scripts.Mining.Companion.rockProductRoll
import org.rsmod.game.inv.InvObj
import org.rsmod.map.CoordGrid

class MiningRewardsTest {
    @Test
    fun GameTestState.`golden nugget drop`() =
        runGameTest(Mining::class) {
            val rock = startMiningCopper()
            random.next = 0 // Success.
            random.then = 2 // No gem.
            random.then = 1 // Nugget.
            advance(ticks = 1)
            assertContains(player.inv, MiningObjs.golden_nugget)
            assertMessageSent("You find a golden nugget!")
            assertDoesNotExist(rock)
        }

    @Test
    fun GameTestState.`random gem drop`() =
        runGameTest(Mining::class) {
            startMiningCopper()
            random.next = 0 // Success.
            random.then = 1 // Gem.
            random.then = 1 // Gem table: first bucket (sapphire).
            random.then = 2 // No nugget.
            advance(ticks = 1)
            assertContains(player.inv, MiningObjs.uncut_sapphire)
            assertMessageSent("You just found an uncut sapphire!")
        }

    @Test
    fun GameTestState.`bonus drop with a full inventory lands on the floor`() =
        runInjectedGameTest(ObjRepository::class, null, Mining::class) { objRepo ->
            player.observerUUID = 1 // Logged-in players have one; floor drops need an owner.
            startMiningCopper()
            player.fillInv()
            player.inv[0] = null // One free slot: the ore takes it.
            random.next = 0 // Success.
            random.then = 2 // No gem.
            random.then = 1 // Nugget.
            advance(ticks = 1)
            assertDoesNotContain(player.inv, MiningObjs.golden_nugget)
            val nuggetId = objTypes[MiningObjs.golden_nugget].id
            assertTrue(objRepo.findAll(player.coords).any { it.type == nuggetId })
        }

    @Test
    fun GameTestState.`mining gloves can save a silver rock`() =
        runGameTest(Mining::class) {
            val type =
                findLocType(MiningContent.rock) { it.rockProduct.id == MiningObjs.silver_ore.id }
            val rock = placeMapLoc(ROCK_COORDS, type)
            player.teleport(rock.coords.translateX(-1))
            player.clearInv()
            player.righthand = InvObj(objs.bronze_pickaxe)
            player.hands = InvObj(MiningObjs.mining_gloves)
            player.stats[stats.mining] = type.rockLevelReq
            player.opLoc1(rock)
            advance(ticks = 3)

            random.next = 0 // Success.
            random.then = 2 // No gem.
            random.then = 2 // No nugget.
            random.then = 1 // Gloves save the rock (1 <= 500).
            advance(ticks = 1)
            assertContains(player.inv, MiningObjs.silver_ore)
            assertMessageSent("Your mining gloves stop the rock from being depleted.")
            assertExists(rock)
        }

    @Test
    fun GameTestState.`glove tiers`() = runBasicGameTest {
        val gloves = InvObj(MiningObjs.mining_gloves)
        val superior = InvObj(MiningObjs.superior_mining_gloves)
        val expert = InvObj(MiningObjs.expert_mining_gloves)
        assertEquals(500, MiningRewards.gloveSavePermille(gloves, MiningObjs.silver_ore))
        assertEquals(0, MiningRewards.gloveSavePermille(gloves, MiningObjs.mithril_ore))
        assertEquals(500, MiningRewards.gloveSavePermille(superior, MiningObjs.silver_ore))
        assertEquals(125, MiningRewards.gloveSavePermille(superior, MiningObjs.runite_ore))
        assertEquals(0, MiningRewards.gloveSavePermille(superior, MiningObjs.amethyst))
        assertEquals(250, MiningRewards.gloveSavePermille(expert, MiningObjs.amethyst))
        assertEquals(0, MiningRewards.gloveSavePermille(null, MiningObjs.coal))
        assertEquals(0, MiningRewards.gloveSavePermille(expert, MiningObjs.copper_ore))
    }

    @Test
    fun GameTestState.`charged glories`() = runBasicGameTest {
        assertTrue(MiningRewards.isChargedGlory(InvObj(objs.amulet_of_glory_4)))
        assertTrue(MiningRewards.isChargedGlory(InvObj(MiningObjs.amulet_of_glory_inf)))
        assertFalse(MiningRewards.isChargedGlory(null))
    }

    @Test
    fun GameTestState.`prospector outfit bonus`() = runGameTest {
        val mod = ProspectorXpMod()
        player.hat = InvObj(MiningObjs.prospector_helmet)
        player.torso = InvObj(MiningObjs.prospector_jacket)
        player.legs = InvObj(MiningObjs.prospector_legs)
        assertEquals(0.018, with(mod) { player.modifier(stats.mining) }, 1e-9)

        player.feet = InvObj(MiningObjs.prospector_boots)
        assertEquals(0.025, with(mod) { player.modifier(stats.mining) }, 1e-9)
        assertEquals(0.0, with(mod) { player.modifier(stats.woodcutting) }, 1e-9)
    }

    /** Places a copper/tin/clay rock and starts mining; the next tick is the first roll. */
    private fun org.rsmod.api.testing.scope.GameTestScope.startMiningCopper():
        org.rsmod.game.loc.BoundLocInfo {
        val type =
            findLocType(MiningContent.rock) {
                it.rockLevelReq == 1 &&
                    it.rockDepleteChance == 0 &&
                    it.rockProductRoll == ProductRoll.FIXED
            }
        val rock = placeMapLoc(ROCK_COORDS, type)
        player.teleport(rock.coords.translateX(-1))
        player.clearInv()
        player.righthand = InvObj(objs.bronze_pickaxe)
        player.stats[stats.mining] = 1
        player.opLoc1(rock)
        advance(ticks = 3)
        return rock
    }

    private companion object {
        val ROCK_COORDS = CoordGrid(0, 50, 50, 34, 31)
    }
}
