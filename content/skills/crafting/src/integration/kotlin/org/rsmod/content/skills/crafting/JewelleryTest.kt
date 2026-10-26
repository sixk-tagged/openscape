package org.rsmod.content.skills.crafting

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.components
import org.rsmod.api.config.refs.stats
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class JewelleryTest {
    @Test
    fun GameTestState.`a gold bar on a furnace opens the gold panel and makes a ring`() =
        runGameTest(Crafting::class, Jewellery::class) {
            val furnace = placeFurnace()
            setUp(5, CraftingObjs.gold_bar, obj("ring_mould"))
            val xp = craftingXp()

            useOnFurnace(furnace, CraftingObjs.gold_bar)
            advance()
            assertModalOpen(CraftingInterfaces.crafting_gold)
            player.ifButton(CraftingComponents.gold_quantity.first())
            player.ifButton(CraftingComponents.gold.getValue("gold_ring"))
            advanceUntil({ player.count(obj("gold_ring")) == 1 }, timeoutTicks = 20)
            assertEquals(0, player.count(CraftingObjs.gold_bar))
            assertEquals(1, player.count(obj("ring_mould")))
            assertEquals(fineXp(15.0), craftingXp() - xp)
        }

    @Test
    fun GameTestState.`a sapphire needs the gem and the level`() =
        runGameTest(Crafting::class, Jewellery::class) {
            val furnace = placeFurnace()
            val sapphire = obj("sapphire")
            setUp(19, CraftingObjs.gold_bar, sapphire, obj("ring_mould"))
            useOnFurnace(furnace, CraftingObjs.gold_bar)
            advance()
            player.ifButton(CraftingComponents.gold.getValue("sapphire_ring"))
            advance()
            assertMessageSent("You need a Crafting level of 20 to make a sapphire ring.")
            assertEquals(1, player.count(CraftingObjs.gold_bar))

            player.stats[stats.crafting] = 20
            useOnFurnace(furnace, CraftingObjs.gold_bar)
            advance()
            player.ifButton(CraftingComponents.gold_quantity.first())
            player.ifButton(CraftingComponents.gold.getValue("sapphire_ring"))
            advanceUntil({ player.count(obj("sapphire_ring")) == 1 }, timeoutTicks = 20)
            assertEquals(0, player.count(sapphire))
        }

    @Test
    fun GameTestState.`silver tiara and stringing an amulet`() =
        runGameTest(Crafting::class, Jewellery::class) {
            val furnace = placeFurnace()
            setUp(23, CraftingObjs.silver_bar, CraftingObjs.tiara_mould)
            useOnFurnace(furnace, CraftingObjs.silver_bar)
            advance()
            assertModalOpen(CraftingInterfaces.silver_crafting)
            player.ifButton(CraftingComponents.silver_quantity.first())
            player.ifButton(CraftingComponents.silver.getValue("tiara"))
            advanceUntil({ player.count(obj("tiara")) == 1 }, timeoutTicks = 20)

            val amulet = obj("unstrung_sapphire_amulet")
            setUp(1, CraftingObjs.ball_of_wool, amulet)
            val xp = craftingXp()
            useOnAny(CraftingObjs.ball_of_wool, amulet)
            advance(ticks = 2)
            player.resumePauseButton(components.skillmulti_a, sub = 1)
            advanceUntil({ player.count(obj("strung_sapphire_amulet")) == 1 }, timeoutTicks = 20)
            assertEquals(fineXp(4.0), craftingXp() - xp)
        }

    private fun GameTestScope.placeFurnace(): BoundLocInfo =
        placeMapLoc(FURNACE, findLocType(CraftingContent.furnace))

    private companion object {
        val FURNACE = CoordGrid(0, 50, 50, 34, 31)
    }
}
