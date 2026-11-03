package org.rsmod.content.areas.misc.wilderness.rules

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.components
import org.rsmod.api.config.refs.interfaces
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.map.CoordGrid

@Execution(ExecutionMode.SAME_THREAD)
class DitchTest {
    @Test
    fun GameTestState.`the ditch is crossed in both directions`() =
        wildernessTest(DitchScript::class) {
            player.setVarBit(WildernessVarBits.ditch_warning_off, 1)
            val ditch = placeMapLoc(DITCH, WildernessLocs.ditch)
            allocZoneCollision(NORTH_BANK)
            player.teleport(SOUTH_BANK)

            player.opLoc1(ditch)
            advanceUntil(max = 15) { player.coords == NORTH_BANK }

            player.opLoc1(ditch)
            advanceUntil(max = 15) { player.coords == SOUTH_BANK }
        }

    @Test
    fun GameTestState.`going in asks first until the player asks not to be asked again`() =
        wildernessTest(DitchScript::class) {
            val ditch = placeMapLoc(DITCH, WildernessLocs.ditch)
            allocZoneCollision(NORTH_BANK)
            player.teleport(SOUTH_BANK)

            // Coming back later stays put.
            player.opLoc1(ditch)
            answerWarning(choice = 3)
            advance(ticks = 5)
            assertEquals(SOUTH_BANK, player.coords)
            assertEquals(0, player.vars[WildernessVarBits.ditch_warning_off])

            // Ticking "don't ask me again" jumps and saves it.
            player.opLoc1(ditch)
            answerWarning(choice = 2)
            advanceUntil(max = 15) { player.coords == NORTH_BANK }
            assertEquals(1, player.vars[WildernessVarBits.ditch_warning_off])

            // Neither way asks any more.
            player.opLoc1(ditch)
            advanceUntil(max = 15) { player.coords == SOUTH_BANK }
            player.opLoc1(ditch)
            advanceUntil(max = 15) { player.coords == NORTH_BANK }
        }

    /** Reads the warning, then picks [choice] (1-3) from the menu that follows. */
    private fun GameTestScope.answerWarning(choice: Int) {
        advanceUntil(max = 10) { player.ui.containsModal(interfaces.messagebox) }
        player.resumePauseButton(components.messagebox_pbutton)
        advanceUntil(max = 10) { player.ui.containsModal(interfaces.chatmenu) }
        player.resumePauseButton(components.chatmenu_pbutton, choice)
    }

    private companion object {
        val DITCH = CoordGrid(3000, 3521)
        val SOUTH_BANK = CoordGrid(3000, 3520)
        val NORTH_BANK = CoordGrid(3000, 3523)
    }
}
