package org.rsmod.content.skills.smithing.scripts

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.components
import org.rsmod.api.config.refs.stats
import org.rsmod.api.testing.GameTestState
import org.rsmod.content.skills.smithing.configs.SmithingContent
import org.rsmod.content.skills.smithing.configs.SmithingObjs
import org.rsmod.game.inv.InvObj
import org.rsmod.map.CoordGrid

/* Inventory transactions share a backing list and are not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class CoalBagTest {
    @Test
    fun GameTestState.`fill, check and empty`() = runGameTest {
        player.clearInv()
        player.inv[0] = InvObj(SmithingObjs.coal_bag)
        for (slot in 1..3) {
            player.inv[slot] = InvObj(SmithingObjs.coal)
        }

        player.withProtectedAccess { fillCoalBag() }
        assertMessageSent("You fill the coal bag with 3 coal.")
        assertEquals(3, player.coalBagCount)
        assertEquals(0, player.count(SmithingObjs.coal))

        player.withProtectedAccess { checkCoalBag() }
        assertMessageSent("Your coal bag contains 3 of 27 coal.")

        player.withProtectedAccess { emptyCoalBag() }
        assertMessageSent("You empty 3 coal from the coal bag.")
        assertEquals(0, player.coalBagCount)
        assertEquals(3, player.count(SmithingObjs.coal))
    }

    @Test
    fun GameTestState.`full, empty and no-space messages`() = runGameTest {
        player.clearInv()
        player.inv[0] = InvObj(SmithingObjs.coal_bag)
        player.withProtectedAccess { emptyCoalBag() }
        assertMessageSent("Your coal bag is empty.")
        player.withProtectedAccess { fillCoalBag() }
        assertMessageSent("You don't have any coal to fill your coal bag with.")

        player.coalBagCount = COAL_BAG_CAPACITY
        player.inv[1] = InvObj(SmithingObjs.coal)
        player.withProtectedAccess { fillCoalBag() }
        assertMessageSent("Your coal bag is full.")

        player.fillInv()
        player.withProtectedAccess { emptyCoalBag() }
        assertMessageSent("You don't have any free space in your inventory.")
        assertEquals(COAL_BAG_CAPACITY, player.coalBagCount)
    }

    @Test
    fun GameTestState.`furnace uses inventory coal first, then the bag`() =
        runGameTest(Smelting::class) {
            val furnace = placeMapLoc(FURNACE_COORDS, findLocType(SmithingContent.furnace))
            player.teleport(furnace.coords.translateX(furnace.adjustedWidth))
            player.clearInv()
            player.stats[stats.smithing] = 30
            player.inv[0] = InvObj(SmithingObjs.coal_bag)
            player.inv[1] = InvObj(SmithingObjs.iron_ore)
            player.inv[2] = InvObj(SmithingObjs.coal)
            player.coalBagCount = 1

            player.opLoc2(furnace)
            advance(ticks = 1)
            player.resumePauseButton(components.skillmulti_b, sub = 1) // b = steel
            advanceUntil { SmithingObjs.steel_bar in player.inv }

            assertEquals(0, player.count(SmithingObjs.coal))
            assertEquals(0, player.coalBagCount)
        }

    private companion object {
        val FURNACE_COORDS = CoordGrid(0, 50, 50, 34, 31)
    }
}
