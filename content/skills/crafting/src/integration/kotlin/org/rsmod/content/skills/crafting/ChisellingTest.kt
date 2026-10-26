package org.rsmod.content.skills.crafting

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.testing.GameTestState

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class ChisellingTest {
    @Test
    fun GameTestState.`chisel cuts every sapphire without a prompt`() =
        runGameTest(Crafting::class, Chiselling::class) {
            val uncut = obj("uncut_sapphire")
            setUp(20, CraftingObjs.chisel, uncut, uncut)
            val xp = craftingXp()

            useOnAny(CraftingObjs.chisel, uncut)
            advanceUntil({ player.count(obj("sapphire")) == 2 }, timeoutTicks = 20)
            assertEquals(0, player.count(uncut))
            assertEquals(fineXp(50.0) * 2, craftingXp() - xp)
        }

    @Test
    fun GameTestState.`a low roll crushes an opal`() =
        runGameTest(Crafting::class, Chiselling::class) {
            val uncut = obj("uncut_opal")
            setUp(1, CraftingObjs.chisel, uncut, uncut)
            val xp = craftingXp()
            random.next = 100 // fails the first cut
            random.next = 0 // passes the second

            useOnAny(CraftingObjs.chisel, uncut)
            advanceUntil({ player.count(obj("crushed_gemstone")) == 1 }, timeoutTicks = 20)
            assertMessageSent("You mis-hit the chisel and smash the opal into pieces!")
            assertEquals(fineXp(3.8), craftingXp() - xp)
            advanceUntil({ player.count(obj("opal")) == 1 }, timeoutTicks = 20)
            assertEquals(fineXp(3.8 + 15.0), craftingXp() - xp)
        }
}
