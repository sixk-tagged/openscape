package org.rsmod.content.skills.smithing.scripts

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.components
import org.rsmod.api.config.refs.interfaces
import org.rsmod.api.config.refs.stats
import org.rsmod.api.game.process.GameLifecycle
import org.rsmod.api.player.hands
import org.rsmod.api.player.interact.WornInteractions
import org.rsmod.api.player.ring
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.skills.smithing.configs.SmithingContent
import org.rsmod.content.skills.smithing.configs.SmithingLocs
import org.rsmod.content.skills.smithing.configs.SmithingObjs
import org.rsmod.content.skills.smithing.configs.SmithingSeqs
import org.rsmod.game.inv.InvObj
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.stat.PlayerStatMap
import org.rsmod.game.type.comp.ComponentType
import org.rsmod.game.type.interf.IfButtonOp
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.Wearpos
import org.rsmod.map.CoordGrid

/* Inventory transactions share a backing list and are not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class SmeltingTest {
    @Test
    fun GameTestState.`smelt bronze through the make-x chatbox`() =
        runGameTest(Smelting::class) {
            val furnace = setUp(level = 1, SmithingObjs.copper_ore, SmithingObjs.tin_ore)
            val xpBefore = player.statMap.getFineXP(stats.smithing)

            player.opLoc2(furnace)
            advance(ticks = 1)
            assertModalOpen(interfaces.skillmulti)
            player.resumePauseButton(components.skillmulti_a, sub = 1)
            advanceUntil { SmithingObjs.bronze_bar in player.inv }

            assertMessageSent("You retrieve a bar of bronze.")
            assertEquals(0, player.count(SmithingObjs.copper_ore))
            assertEquals(0, player.count(SmithingObjs.tin_ore))
            assertEquals(fineXp(6.2), player.statMap.getFineXP(stats.smithing) - xpBefore)
        }

    @Test
    fun GameTestState.`the lumbridge furnace smelts`() =
        runGameTest(Smelting::class) {
            val furnace = placeMapLoc(FURNACE_COORDS, SmithingLocs.lumbridge_furnace)
            player.teleport(furnace.coords.translateX(furnace.adjustedWidth))
            player.clearInv()
            player.inv[0] = InvObj(SmithingObjs.copper_ore)
            player.inv[1] = InvObj(SmithingObjs.tin_ore)

            player.opLoc2(furnace)
            advanceUntil { player.ui.containsModal(interfaces.skillmulti) }
            player.resumePauseButton(components.skillmulti_a, sub = 1)
            advanceUntil { SmithingObjs.bronze_bar in player.inv }
        }

    @Test
    fun GameTestState.`nothing to smelt without ore`() =
        runGameTest(Smelting::class) {
            val furnace = setUp(level = 1)
            player.opLoc2(furnace)
            advance(ticks = 1)
            assertMessageSent(Smelting.NO_ORE)
            assertModalNotOpen(interfaces.skillmulti)
        }

    @Test
    fun GameTestState.`level and coal requirements`() =
        runGameTest(Smelting::class) {
            val furnace = setUp(level = 14, SmithingObjs.iron_ore, SmithingObjs.coal)
            pick(furnace, components.skillmulti_a, amount = 1) // a = iron, b = steel
            assertMessageSent("You need a Smithing level of 15 to smelt an iron bar.")

            player.stats[stats.smithing] = 30
            pick(furnace, components.skillmulti_b, amount = 1)
            assertMessageSent("You need 2 coal to smelt a steel bar.")
            assertEquals(1, player.count(SmithingObjs.iron_ore))
        }

    @Test
    fun GameTestState.`iron can fail without a ring of forging`() =
        runGameTest(Smelting::class) {
            val furnace = setUp(level = 15, SmithingObjs.iron_ore)
            random.next = 100 // Above the 50% success roll.
            pick(furnace, components.skillmulti_a, amount = 1)
            advanceUntil { SmithingObjs.iron_ore !in player.inv }
            assertMessageSent(Smelting.IMPURE_IRON)
            assertEquals(0, player.count(SmithingObjs.iron_bar))
        }

    @Test
    fun GameTestState.`ring of forging always smelts iron and melts after 140 bars`() =
        runGameTest(Smelting::class) {
            val furnace = setUp(level = 15, SmithingObjs.iron_ore)
            player.ring = InvObj(SmithingObjs.ring_of_forging)
            player.ringOfForgingUsed = SmithingGear.RING_OF_FORGING_CHARGES - 1

            pick(furnace, components.skillmulti_a, amount = 1)
            advanceUntil { SmithingObjs.iron_bar in player.inv }

            assertMessageSent(Smelting.RING_MELTED)
            assertEquals(null, player.ring)
            assertEquals(0, player.ringOfForgingUsed)
        }

    @Test
    fun GameTestState.`ring of forging charges are only used by iron`() =
        runGameTest(Smelting::class) {
            val furnace = setUp(level = 1, SmithingObjs.copper_ore, SmithingObjs.tin_ore)
            player.ring = InvObj(SmithingObjs.ring_of_forging)
            pick(furnace, components.skillmulti_a, amount = 1)
            advanceUntil { SmithingObjs.bronze_bar in player.inv }
            assertEquals(0, player.ringOfForgingUsed)
        }

    @Test
    fun GameTestState.`goldsmith gauntlets boost gold bar xp`() =
        runGameTest(Smelting::class) {
            val furnace = setUp(level = 40, SmithingObjs.gold_ore)
            player.hands = InvObj(SmithingObjs.goldsmith_gauntlets)
            val xpBefore = player.statMap.getFineXP(stats.smithing)
            pick(furnace, components.skillmulti_a, amount = 1)
            advanceUntil { SmithingObjs.gold_bar in player.inv }
            assertEquals(
                fineXp(SmeltRecipes.GOLD_XP_WITH_GAUNTLETS),
                player.statMap.getFineXP(stats.smithing) - xpBefore,
            )
        }

    @Test
    fun GameTestState.`cannonballs need the ammo mould`() =
        runGameTest(Smelting::class) {
            val furnace = setUp(level = 35, SmithingObjs.steel_bar)
            player.opLoc2(furnace)
            advance(ticks = 1)
            assertMessageSent(Smelting.NO_ORE)

            player.inv[1] = InvObj(SmithingObjs.ammo_mould)
            val xpBefore = player.statMap.getFineXP(stats.smithing)
            pick(furnace, components.skillmulti_a, amount = 1)
            advanceUntil { SmithingObjs.cannonball in player.inv }
            assertEquals(4, player.count(SmithingObjs.cannonball))
            assertEquals(0, player.count(SmithingObjs.steel_bar))
            assertEquals(fineXp(25.6), player.statMap.getFineXP(stats.smithing) - xpBefore)
        }

    @Test
    fun GameTestState.`bronze bars take five ticks`() =
        runGameTest(Smelting::class) {
            val furnace =
                setUp(
                    level = 1,
                    SmithingObjs.copper_ore,
                    SmithingObjs.tin_ore,
                    SmithingObjs.copper_ore,
                    SmithingObjs.tin_ore,
                )
            pick(furnace, components.skillmulti_a, amount = 2)
            advanceUntil { player.count(SmithingObjs.bronze_bar) == 1 }
            assertEquals(5, advanceUntil { player.count(SmithingObjs.bronze_bar) == 2 })
        }

    @Test
    fun GameTestState.`steel bars take four ticks`() =
        runGameTest(Smelting::class) {
            val furnace =
                setUp(
                    level = 30,
                    SmithingObjs.iron_ore,
                    SmithingObjs.coal,
                    SmithingObjs.coal,
                    SmithingObjs.iron_ore,
                    SmithingObjs.coal,
                    SmithingObjs.coal,
                )
            pick(furnace, components.skillmulti_b, amount = 2) // a = iron, b = steel
            advanceUntil { player.count(SmithingObjs.steel_bar) == 1 }
            assertEquals(4, advanceUntil { player.count(SmithingObjs.steel_bar) == 2 })
        }

    @Test
    fun GameTestState.`cannonballs take ten ticks per steel bar`() =
        runGameTest(Smelting::class) {
            val furnace =
                setUp(
                    level = 35,
                    SmithingObjs.ammo_mould,
                    SmithingObjs.steel_bar,
                    SmithingObjs.steel_bar,
                )
            pick(furnace, components.skillmulti_a, amount = 2)
            advanceUntil { player.count(SmithingObjs.cannonball) == 4 }
            assertEquals(10, advanceUntil { player.count(SmithingObjs.cannonball) == 8 })
        }

    @Test
    fun GameTestState.`the furnace animation plays while each bar smelts`() =
        runGameTest(Smelting::class) {
            val furnace = setUp(level = 1, SmithingObjs.copper_ore, SmithingObjs.tin_ore)
            // The pending animation is cleared at the end of every tick, so record it just before.
            val anims = mutableListOf<Int>()
            eventBus.subscribeUnbound(GameLifecycle.LateCycle::class.java) {
                anims += player.pendingSequence.id
            }
            player.opLoc2(furnace)
            advance(ticks = 1)

            anims.clear()
            player.resumePauseButton(components.skillmulti_a, sub = 1)
            advance(ticks = 1)
            assertEquals(listOf(SmithingSeqs.furnace.id), anims) // Starts when the bar is chosen.

            advanceUntil { SmithingObjs.bronze_bar in player.inv }
            assertTrue(anims.last() != SmithingSeqs.furnace.id) // Not replayed after the last bar.
        }

    @Test
    fun GameTestState.`check shows the ring of forging charges left`() =
        runInjectedGameTest(WornInteractions::class, null, Smelting::class) { worn ->
            player.ring = InvObj(SmithingObjs.ring_of_forging)
            player.ringOfForgingUsed = 40
            player.withProtectedAccess {
                worn.interact(this, player.worn, Wearpos.Ring.slot, IfButtonOp.Op2)
            }
            assertMessageSent(
                "You can smelt 100 more pieces of iron ore before your ring of forging melts."
            )
        }

    @Test
    fun GameTestState.`walking away stops smelting`() =
        runGameTest(Smelting::class) {
            val furnace =
                setUp(
                    level = 1,
                    SmithingObjs.copper_ore,
                    SmithingObjs.tin_ore,
                    SmithingObjs.copper_ore,
                    SmithingObjs.tin_ore,
                )
            pick(furnace, components.skillmulti_a, amount = 2)
            advanceUntil { player.count(SmithingObjs.bronze_bar) == 1 }
            player.moveGameClick(player.coords.translateX(-3))
            advance(ticks = 10)
            assertEquals(1, player.count(SmithingObjs.bronze_bar))
        }

    private fun GameTestScope.setUp(level: Int, vararg objs: ObjType): BoundLocInfo {
        val furnace = placeMapLoc(FURNACE_COORDS, findLocType(SmithingContent.furnace))
        // The furnace can only be used from its east side (force-approach flags).
        player.teleport(furnace.coords.translateX(furnace.adjustedWidth))
        player.clearInv()
        objs.forEachIndexed { slot, obj -> player.inv[slot] = InvObj(obj) }
        player.stats[stats.smithing] = level
        return furnace
    }

    /** Opens the furnace chatbox and clicks [button] with [amount]. */
    private fun GameTestScope.pick(furnace: BoundLocInfo, button: ComponentType, amount: Int) {
        player.opLoc2(furnace)
        advance(ticks = 1)
        player.resumePauseButton(button, sub = amount)
        advance(ticks = 1)
    }

    private fun fineXp(xp: Double): Int = PlayerStatMap.toFineXP(xp).toInt()

    private companion object {
        val FURNACE_COORDS = CoordGrid(0, 50, 50, 34, 31)
    }
}
