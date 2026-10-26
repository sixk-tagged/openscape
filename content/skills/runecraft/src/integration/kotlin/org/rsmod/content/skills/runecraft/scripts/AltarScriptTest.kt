package org.rsmod.content.skills.runecraft.scripts

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.player.front
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.skills.runecraft.configs.Altar
import org.rsmod.content.skills.runecraft.configs.Altars
import org.rsmod.content.skills.runecraft.configs.CombinationRunes
import org.rsmod.content.skills.runecraft.configs.RunecraftObjs
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.obj.ObjType

/* Inventory transactions share a backing list and are not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class AltarScriptTest {
    @Test
    fun GameTestState.`craft turns every essence into runes by the level's multiple`() =
        runGameTest(AltarScript::class) {
            val altar = placeAltar(Altars.air, level = 22)
            give(RunecraftObjs.rune_essence, 6)
            give(RunecraftObjs.pure_essence, 4)
            val xpBefore = runecraftingXp()

            player.opLoc1(altar)
            advance()

            // Air gives a rune more every 11 levels: 22 / 11 + 1 = 3 per essence.
            assertEquals(30, player.count(RunecraftObjs.air_rune))
            assertEquals(0, player.count(RunecraftObjs.rune_essence))
            assertEquals(0, player.count(RunecraftObjs.pure_essence))
            assertEquals(fineXp(50.0), runecraftingXp() - xpBefore)
        }

    @Test
    fun GameTestState.`a low level cannot craft`() =
        runGameTest(AltarScript::class) {
            val altar = placeAltar(Altars.water, level = 4)
            give(RunecraftObjs.rune_essence, 3)

            player.opLoc1(altar)
            advance()

            assertMessageSent("You need a Runecrafting level of 5 to craft water runes.")
            assertEquals(3, player.count(RunecraftObjs.rune_essence))
        }

    @Test
    fun GameTestState.`members altars need a members world and pure essence`() =
        runGameTest(AltarScript::class) {
            val altar = placeAltar(Altars.cosmic, level = 27)
            give(RunecraftObjs.pure_essence, 2)

            player.members = false
            player.opLoc1(altar)
            advance()
            assertMessageSent("You need to be on a members' world to craft cosmic runes.")

            player.members = true
            player.clearInv()
            give(RunecraftObjs.rune_essence, 2)
            player.opLoc1(altar)
            advance()
            assertMessageSent("You need pure essence to craft cosmic runes.")
            assertEquals(2, player.count(RunecraftObjs.rune_essence))

            give(RunecraftObjs.pure_essence, 2)
            player.opLoc1(altar)
            advance()
            assertEquals(2, player.count(RunecraftObjs.cosmic_rune))
            assertEquals(2, player.count(RunecraftObjs.rune_essence))
        }

    @Test
    fun GameTestState.`a golem core makes eleven runes and ten times the xp`() =
        runGameTest(AltarScript::class) {
            val altar = placeAltar(Altars.mind, level = 14)
            give(RunecraftObjs.mind_core, 2)
            val xpBefore = runecraftingXp()

            player.opLoc1(altar)
            advance()

            // 14 / 14 + 1 = 2 per rune: 2 cores x 11 x 2.
            assertEquals(44, player.count(RunecraftObjs.mind_rune))
            assertEquals(fineXp(110.0), runecraftingXp() - xpBefore)
        }

    @Test
    fun GameTestState.`a tiara is bound with its talisman on the altar`() =
        runGameTest(AltarScript::class) {
            val altar = placeAltar(Altars.air, level = 1)
            give(RunecraftObjs.tiara)
            give(RunecraftObjs.air_talisman)
            val xpBefore = runecraftingXp()

            useOn(altar, RunecraftObjs.air_talisman)

            assertContains(player.inv, RunecraftObjs.tiara_air)
            assertEquals(0, player.count(RunecraftObjs.air_talisman))
            assertEquals(0, player.count(RunecraftObjs.tiara))
            assertEquals(fineXp(25.0), runecraftingXp() - xpBefore)
        }

    @Test
    fun GameTestState.`a gold tiara takes the elemental talisman at elemental altars`() =
        runGameTest(AltarScript::class) {
            val altar = placeAltar(Altars.fire, level = 1)
            give(RunecraftObjs.tiara_gold)
            give(RunecraftObjs.elemental_talisman)

            useOn(altar, RunecraftObjs.tiara_gold)

            assertContains(player.inv, RunecraftObjs.tiara_elemental)

            // Without the talisman nothing is bound.
            player.clearInv()
            give(RunecraftObjs.tiara)
            useOn(altar, RunecraftObjs.tiara)
            assertDoesNotContain(player.inv, RunecraftObjs.tiara_fire)
        }

    @Test
    fun GameTestState.`combination runes use the other element's runes and talisman`() =
        runGameTest(AltarScript::class) {
            val altar = placeAltar(Altars.air, level = 6)
            player.members = true
            give(RunecraftObjs.pure_essence, 4)
            give(RunecraftObjs.water_rune, 4)
            give(RunecraftObjs.water_talisman)
            val xpBefore = runecraftingXp()

            // Each rune has a 50% chance: let the first two succeed and the last two fail.
            random.next = 0
            random.then = 0
            random.then = 1
            random.then = 1
            useOn(altar, RunecraftObjs.water_rune)

            assertEquals(2, player.count(RunecraftObjs.mist_rune))
            assertEquals(0, player.count(RunecraftObjs.pure_essence))
            assertEquals(0, player.count(RunecraftObjs.water_rune))
            assertEquals(0, player.count(RunecraftObjs.water_talisman))
            assertEquals(fineXp(16.0), runecraftingXp() - xpBefore)
        }

    @Test
    fun GameTestState.`the binding necklace skips the roll and crumbles after sixteen charges`() =
        runGameTest(AltarScript::class) {
            val altar = placeAltar(Altars.air, level = 6)
            player.members = true
            player.front = InvObj(RunecraftObjs.binding_necklace)
            give(RunecraftObjs.pure_essence, 3)
            give(RunecraftObjs.water_rune, 3)
            give(RunecraftObjs.water_talisman)

            random.next = 1 // every roll would fail
            useOn(altar, RunecraftObjs.water_talisman)

            assertEquals(3, player.count(RunecraftObjs.mist_rune))
            assertEquals(1, player.bindingNecklaceUsed)

            player.bindingNecklaceUsed = CombinationRunes.NECKLACE_CHARGES - 1
            give(RunecraftObjs.pure_essence, 1)
            give(RunecraftObjs.water_rune, 1)
            give(RunecraftObjs.water_talisman)
            useOn(altar, RunecraftObjs.water_rune)

            assertEquals(null, player.front)
            assertEquals(0, player.bindingNecklaceUsed)
            assertMessageSent("Your binding necklace has crumbled to dust.")
        }

    private fun GameTestScope.placeAltar(altar: Altar, level: Int) = run {
        setRunecrafting(level)
        val loc = placeMapLoc(SPOT, altar.loc)
        player.teleport(SPOT.translateX(-1))
        player.members = altar.members || player.members
        loc
    }

    private fun GameTestScope.give(obj: ObjType, count: Int = 1) {
        if (objTypes[obj].isStackable) {
            player.inv[player.inv.indexOfFirstNull()] = InvObj(obj, count)
        } else {
            repeat(count) { player.inv[player.inv.indexOfFirstNull()] = InvObj(obj) }
        }
    }

    private fun org.rsmod.game.inv.Inventory.indexOfFirstNull(): Int =
        (0 until size).first { this[it] == null }
}
