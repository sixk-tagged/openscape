package org.rsmod.content.other.windmill

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.testing.GameTestState
import org.rsmod.game.inv.InvObj
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class WindmillFlourTest {
    @Test
    fun GameTestState.`grain through the hopper fills a pot with flour`() =
        runGameTest(WindmillFlourScript::class) {
            advance()
            player.clearInv()
            player.inv[0] = InvObj(MillObjs.grain)
            player.inv[1] = InvObj(MillObjs.pot)
            val hopper = placeMapLoc(SPOT, MillLocs.hopper)
            val controls = placeMapLoc(SPOT.translateX(3), MillLocs.hopper_controls)
            val bin = placeMapLoc(SPOT.translateX(5), MillLocs.flour_bin)

            // The hopper and its controls can only be used from the north (force-approach flags).
            player.teleport(SPOT.translateZ(1))
            player.opLoc1(hopper)
            advanceUntil(max = 20) { MillObjs.grain !in player.inv }
            assertEquals(1, player.vars[MillVarps.hopper_grain])

            player.teleport(controls.coords.translateZ(1))
            player.opLoc1(controls)
            advanceUntil(max = 20) { player.vars[MillVarps.hopper_grain] == 0 }
            assertEquals(1, player.vars[MillVarBits.flour])
            assertEquals(1, player.vars[MillVarBits.show_flour])

            player.teleport(bin.coords.translateZ(2))
            player.opLoc1(bin)
            advanceUntil(max = 20) { MillObjs.pot_of_flour in player.inv }
            assertEquals(0, player.vars[MillVarBits.flour])
            assertEquals(0, player.vars[MillVarBits.show_flour])
        }

    private companion object {
        val SPOT = CoordGrid(0, 50, 50, 34, 31)
    }
}
