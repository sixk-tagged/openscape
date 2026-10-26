package org.rsmod.content.skills.smithing.scripts

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.feet
import org.rsmod.api.player.hands
import org.rsmod.api.player.legs
import org.rsmod.api.player.torso
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.skills.smithing.configs.SmithingComponents
import org.rsmod.content.skills.smithing.configs.SmithingContent
import org.rsmod.content.skills.smithing.configs.SmithingInterfaces
import org.rsmod.content.skills.smithing.configs.SmithingLocs
import org.rsmod.content.skills.smithing.configs.SmithingObjs
import org.rsmod.game.inv.InvObj
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.stat.PlayerStatMap
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.UnpackedObjType
import org.rsmod.map.CoordGrid

/* Inventory transactions share a backing list and are not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class AnvilSmithingTest {
    @Test
    fun GameTestState.`every anvil button resolves to a cache product`() =
        runInjectedGameTest(AnvilProducts::class) { products ->
            // 27 buttons x 6 metals, minus the hidden ones (other_2 for bronze, mithril, adamant,
            // rune; other_3 for adamant and rune).
            assertEquals(156, products.all.size)
            // The cache gives the bronze dagger and axe level 0 (no requirement).
            for (product in products.all) {
                val ok = product.level in 0..99 && product.bars in 1..5 && product.makes >= 1
                assertTrue(ok) { "Bad anvil product: $product" }
            }
        }

    @Test
    fun GameTestState.`smith a bronze dagger`() =
        runGameTest(AnvilSmithing::class) {
            val anvil = setUp(level = 1, SmithingObjs.hammer, SmithingObjs.bronze_bar)
            val xpBefore = player.statMap.getFineXP(stats.smithing)

            player.opLoc1(anvil)
            advance(ticks = 1)
            assertModalOpen(SmithingInterfaces.smithing)
            player.ifButton(SmithingComponents.make_1)
            player.ifButton(SmithingComponents.dagger)
            advanceUntil { player.count(obj("bronze_dagger")) == 1 }

            assertModalNotOpen(SmithingInterfaces.smithing)
            assertEquals(0, player.count(SmithingObjs.bronze_bar))
            assertEquals(fineXp(12.5), player.statMap.getFineXP(stats.smithing) - xpBefore)
        }

    @Test
    fun GameTestState.`make 5 smiths five`() =
        runGameTest(AnvilSmithing::class) {
            val bars = Array(6) { SmithingObjs.bronze_bar }
            val anvil = setUp(level = 1, SmithingObjs.hammer, *bars)
            open(anvil)
            player.ifButton(SmithingComponents.make_5)
            player.ifButton(SmithingComponents.dagger)
            advanceUntil(max = 60) { player.count(obj("bronze_dagger")) == 5 }
            advance(ticks = 10)
            assertEquals(5, player.count(obj("bronze_dagger")))
            assertEquals(1, player.count(SmithingObjs.bronze_bar))
        }

    @Test
    fun GameTestState.`make-x amounts are clamped to 1-28`() =
        runGameTest(AnvilSmithing::class) {
            val anvil = setUp(level = 1, SmithingObjs.hammer, SmithingObjs.bronze_bar)
            open(anvil)
            player.ifButton(SmithingComponents.make_x)
            advance(ticks = 1)
            player.resumeCountDialog(0)
            advance(ticks = 1)
            player.ifButton(SmithingComponents.dagger)
            advanceUntil { player.count(obj("bronze_dagger")) == 1 }

            player.inv[2] = InvObj(SmithingObjs.bronze_bar)
            open(anvil)
            player.ifButton(SmithingComponents.make_x)
            advance(ticks = 1)
            player.resumeCountDialog(50)
            advance(ticks = 1)
            player.ifButton(SmithingComponents.dagger)
            advanceUntil { player.count(obj("bronze_dagger")) == 2 }
        }

    @Test
    fun GameTestState.`needs a hammer and bars`() =
        runGameTest(AnvilSmithing::class) {
            val anvil = setUp(level = 1, SmithingObjs.bronze_bar)
            player.opLoc1(anvil)
            advance(ticks = 1)
            assertMessageSent(AnvilSmithing.NO_HAMMER)
            assertModalNotOpen(SmithingInterfaces.smithing)

            player.clearInv()
            player.inv[0] = InvObj(SmithingObjs.hammer)
            player.opLoc1(anvil)
            advance(ticks = 1)
            assertMessageSent(AnvilSmithing.NO_BARS)
        }

    @Test
    fun GameTestState.`metal and item level requirements`() =
        runGameTest(AnvilSmithing::class) {
            val anvil = setUp(level = 14, SmithingObjs.hammer, SmithingObjs.iron_bar)
            player.opLoc1(anvil)
            advance(ticks = 1)
            assertMessageSent("You need a Smithing level of 15 to work iron bars.")

            player.clearInv()
            player.inv[0] = InvObj(SmithingObjs.hammer)
            for (slot in 1..5) {
                player.inv[slot] = InvObj(SmithingObjs.bronze_bar)
            }
            player.stats[stats.smithing] = 1
            open(anvil)
            player.ifButton(SmithingComponents.platebody)
            advance(ticks = 1)
            assertMessageSent("You need a Smithing level of 18 to make a bronze platebody.")
        }

    @Test
    fun GameTestState.`stale screen with too few bars makes nothing`() =
        runGameTest(AnvilSmithing::class) {
            val anvil = setUp(level = 18, SmithingObjs.hammer, SmithingObjs.bronze_bar)
            open(anvil)
            player.ifButton(SmithingComponents.platebody) // Needs 5 bars, has 1.
            advance(ticks = 1)
            assertMessageSent("You don't have enough bronze bars to make a bronze platebody.")

            open(anvil)
            player.inv[1] = null // Banked after the screen opened.
            player.ifButton(SmithingComponents.dagger)
            advance(ticks = 6)
            assertEquals(0, player.count(obj("bronze_dagger")))
        }

    @Test
    fun GameTestState.`smiths outfit makes items take four ticks`() =
        runGameTest(AnvilSmithing::class) {
            val bars = Array(4) { SmithingObjs.bronze_bar }
            val anvil = setUp(level = 1, SmithingObjs.hammer, *bars)
            open(anvil)
            player.ifButton(SmithingComponents.make_all)
            player.ifButton(SmithingComponents.dagger)
            advanceUntil { player.count(obj("bronze_dagger")) == 1 }
            assertEquals(5, advanceUntil { player.count(obj("bronze_dagger")) == 2 })

            player.torso = InvObj(SmithingObjs.smiths_tunic)
            player.legs = InvObj(SmithingObjs.smiths_trousers)
            player.feet = InvObj(SmithingObjs.smiths_boots)
            player.hands = InvObj(SmithingObjs.smiths_gloves)
            open(anvil)
            player.ifButton(SmithingComponents.dagger)
            advanceUntil { player.count(obj("bronze_dagger")) == 3 }
            assertEquals(4, advanceUntil { player.count(obj("bronze_dagger")) == 4 })
        }

    @Test
    fun GameTestState.`the rusted anvil only works bronze`() =
        runGameTest(AnvilSmithing::class) {
            val anvil = placeMapLoc(ANVIL_COORDS, SmithingLocs.lumbridge_anvil)
            player.teleport(anvil.coords.translateX(-1))
            player.clearInv()
            player.inv[0] = InvObj(SmithingObjs.hammer)
            player.inv[1] = InvObj(SmithingObjs.iron_bar)
            player.stats[stats.smithing] = 99

            player.opLoc1(anvil)
            advance(ticks = 1)
            assertMessageSent(AnvilSmithing.NO_BRONZE)
            assertModalNotOpen(SmithingInterfaces.smithing)

            player.inv[2] = InvObj(SmithingObjs.bronze_bar)
            player.opLoc1(anvil)
            advance(ticks = 1)
            assertModalOpen(SmithingInterfaces.smithing)
            player.ifButton(SmithingComponents.make_1)
            player.ifButton(SmithingComponents.dagger)
            advanceUntil { player.count(obj("bronze_dagger")) == 1 }
            assertEquals(1, player.count(SmithingObjs.iron_bar))
        }

    private fun GameTestScope.setUp(level: Int, vararg objs: ObjType): BoundLocInfo {
        val anvil = placeMapLoc(ANVIL_COORDS, findLocType(SmithingContent.anvil))
        player.teleport(anvil.coords.translateX(-1))
        player.clearInv()
        objs.forEachIndexed { slot, obj -> player.inv[slot] = InvObj(obj) }
        player.stats[stats.smithing] = level
        return anvil
    }

    private fun GameTestScope.open(anvil: BoundLocInfo) {
        player.opLoc1(anvil)
        advance(ticks = 1)
        assertModalOpen(SmithingInterfaces.smithing)
    }

    private fun GameTestScope.obj(name: String): UnpackedObjType =
        objTypes.values.first { it.internalName == name }

    private fun fineXp(xp: Double): Int = PlayerStatMap.toFineXP(xp).toInt()

    private companion object {
        val ANVIL_COORDS = CoordGrid(0, 50, 50, 34, 31)
    }
}
