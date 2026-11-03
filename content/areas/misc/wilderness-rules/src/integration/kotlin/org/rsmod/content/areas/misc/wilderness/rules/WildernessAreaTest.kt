package org.rsmod.content.areas.misc.wilderness.rules

import net.rsprot.protocol.game.outgoing.misc.player.SetPlayerOp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.config.refs.varps
import org.rsmod.api.testing.GameTestState
import org.rsmod.map.CoordGrid

@Execution(ExecutionMode.SAME_THREAD)
class WildernessAreaTest {
    @Test
    fun `the Wilderness starts after the safe steps and ends at the top of the map`() {
        assertFalse(WildernessBounds.contains(3000, 3520))
        assertFalse(WildernessBounds.contains(3000, 3523))
        assertFalse(WildernessBounds.contains(3000, 3524))
        assertTrue(WildernessBounds.contains(3000, 3525))
        assertTrue(WildernessBounds.contains(3000, 3967))
        assertFalse(WildernessBounds.contains(3000, 3968))
        assertFalse(WildernessBounds.contains(2943, 3600))
        assertTrue(WildernessBounds.contains(2944, 3600))
        assertTrue(WildernessBounds.contains(3391, 3600))
        assertFalse(WildernessBounds.contains(3392, 3600))
    }

    @Test
    fun `the dungeons under the Wilderness are in it`() {
        assertFalse(WildernessBounds.contains(3100, 9919))
        assertTrue(WildernessBounds.contains(3100, 9920))
        assertTrue(WildernessBounds.contains(3360, 10367))
        assertFalse(WildernessBounds.contains(3100, 10368))
    }

    @Test
    fun `the Ferox Enclave is safe`() {
        // Inside the barriers: the four corners and the middle.
        assertFalse(WildernessBounds.contains(3123, 3617))
        assertFalse(WildernessBounds.contains(3154, 3617))
        assertFalse(WildernessBounds.contains(3123, 3639))
        assertFalse(WildernessBounds.contains(3154, 3639))
        assertFalse(WildernessBounds.contains(3140, 3630))
        // Just outside each barrier is not.
        assertTrue(WildernessBounds.contains(3122, 3630))
        assertTrue(WildernessBounds.contains(3155, 3630))
        assertTrue(WildernessBounds.contains(3140, 3616))
        assertTrue(WildernessBounds.contains(3140, 3640))
    }

    @Test
    fun GameTestState.`entering shows the attack option and the overlay, leaving hides them`() =
        wildernessTest(WildernessAreaScript::class) { deps ->
            deps.wilderness(INSIDE)
            player.teleport(INSIDE)
            advanceUntil(max = 5) { SetPlayerOp(2, true, "Attack") in client }
            assertEquals(1, player.vars[varbits.inside_wilderness])
            assertEquals(1, player.vars[varbits.pvp_area_client])

            // Leaving also ends a Tele Block.
            player.setVarp(varps.teleblock_ticks, 100)
            player.teleport(OUTSIDE)
            advanceUntil(max = 5) { SetPlayerOp(2, false, null) in client }
            assertEquals(0, player.vars[varbits.inside_wilderness])
            assertEquals(0, player.vars[varbits.pvp_area_client])
            assertEquals(0, player.vars[varps.teleblock_ticks])
        }

    private companion object {
        val INSIDE = CoordGrid(3000, 3600)
        val OUTSIDE = CoordGrid(3000, 3500)
    }
}
