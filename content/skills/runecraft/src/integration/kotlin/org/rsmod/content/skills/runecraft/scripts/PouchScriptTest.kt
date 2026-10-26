package org.rsmod.content.skills.runecraft.scripts

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.back
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.skills.runecraft.configs.Pouch
import org.rsmod.content.skills.runecraft.configs.Pouches
import org.rsmod.content.skills.runecraft.configs.RunecraftObjs
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.obj.ObjType

/* Inventory transactions share a backing list and are not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class PouchScriptTest {
    @Test
    fun GameTestState.`fill, check and empty`() =
        runGameTest(PouchScript::class) {
            ready(Pouches.small, level = 1, essence = RunecraftObjs.rune_essence, count = 5)

            use(1) // Fill: only the 3 the small pouch holds.
            assertEquals(3, player.vars[Pouches.small.count])
            assertEquals(2, player.count(RunecraftObjs.rune_essence))

            use(3) // Check
            assertMessageSent("There are 3 rune essence in this pouch.")

            use(2) // Empty
            assertEquals(0, player.vars[Pouches.small.count])
            assertEquals(5, player.count(RunecraftObjs.rune_essence))
        }

    @Test
    fun GameTestState.`a pouch holds one type of essence and needs its level`() =
        runGameTest(PouchScript::class) {
            ready(Pouches.medium, level = 24, essence = RunecraftObjs.pure_essence, count = 4)
            use(1)
            assertMessageSent("You need a Runecrafting level of 25 to use this pouch.")
            assertEquals(4, player.count(RunecraftObjs.pure_essence))

            player.stats[stats.runecrafting] = 25
            use(1)
            assertEquals(4, player.vars[Pouches.medium.count])
            assertEquals(1, player.vars[Pouches.medium.type])

            player.clearInv()
            player.inv[0] = InvObj(RunecraftObjs.medium_pouch)
            player.inv[1] = InvObj(RunecraftObjs.rune_essence)
            use(1)
            assertMessageSent("You can only keep one type of essence in a pouch at a time.")
        }

    @Test
    fun GameTestState.`the colossal pouch grows with the level`() =
        runGameTest(PouchScript::class) {
            ready(Pouches.colossal, level = 50, essence = RunecraftObjs.pure_essence, count = 20)
            use(1)
            assertEquals(16, player.vars[Pouches.colossal.count])

            // At 85 it holds 40 until it wears: one wear per essence stored, a step each 320.
            assertEquals(40, Pouches.colossal.capacity(85, 319))
            assertEquals(35, Pouches.colossal.capacity(85, 320))
            assertEquals(5, Pouches.colossal.capacity(85, 1020))
            assertEquals(null, Pouches.colossal.capacity(85, 1021))
        }

    @Test
    fun GameTestState.`a medium pouch wears out, decays, and a cape stops it`() =
        runGameTest(PouchScript::class) {
            ready(Pouches.medium, level = 30, essence = RunecraftObjs.rune_essence, count = 6)
            // Three rolled per essence: 6 essence wear it by 18.
            random.next = 3
            repeat(5) { random.then = 3 }
            player.setVarp(Pouches.medium.wear!!, 390)
            use(1)
            assertContains(player.inv, RunecraftObjs.medium_pouch_decayed)
            assertMessageSent("Your pouch has decayed through use.")
            // 408 is past 400: the pouch now holds 3, so 3 essence come back.
            assertEquals(3, player.vars[Pouches.medium.count])
            assertEquals(3, player.count(RunecraftObjs.rune_essence))

            player.clearInv()
            player.inv[0] = InvObj(RunecraftObjs.medium_pouch)
            player.inv[1] = InvObj(RunecraftObjs.rune_essence)
            player.back = InvObj(RunecraftObjs.runecrafting_cape)
            player.setVarp(Pouches.medium.wear!!, 0)
            player.setVarBit(Pouches.medium.count, 0)
            use(1)
            assertEquals(0, player.vars[Pouches.medium.wear!!])
        }

    /** Pouch in slot 0, [count] of [essence] after it. */
    private fun GameTestScope.ready(pouch: Pouch, level: Int, essence: ObjType, count: Int) {
        advance()
        player.stats[stats.runecrafting] = level
        player.clearInv()
        player.inv[0] = InvObj(pouch.full)
        repeat(count) { player.inv[1 + it] = InvObj(essence) }
        for (varbit in listOf(pouch.count, pouch.type)) {
            player.setVarBit(varbit, 0)
        }
        pouch.wear?.let { player.setVarp(it, 0) }
    }

    private fun GameTestScope.use(op: Int) {
        advance()
        player.withProtectedAccess {
            when (op) {
                1 -> opHeld1(0)
                2 -> opHeld2(0)
                else -> opHeld3(0)
            }
        }
    }
}
