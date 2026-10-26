package org.rsmod.content.areas.city.lumbridge.locs

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.components
import org.rsmod.api.config.refs.interfaces
import org.rsmod.api.config.refs.objs
import org.rsmod.api.testing.GameTestState
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class ShedToolsTest {
    @Test
    fun GameTestState.`tools give a rake then a spade`() =
        runGameTest(ShedTools::class) {
            player.clearInv()
            val tools = placeMapLoc(TOOLS, ShedToolsLocs.tools)
            // Inside the shed, in front of the tools.
            player.teleport(TOOLS.translateZ(-1))

            player.opLoc1(tools)
            advanceUntil(max = 10) { player.ui.containsModal(interfaces.chatmenu) }
            player.resumePauseButton(components.chatmenu_pbutton, sub = 1)
            advanceUntil(max = 5) { player.count(ShedToolsObjs.rake) == 1 }

            player.opLoc1(tools)
            advanceUntil(max = 10) { player.count(objs.spade) == 1 }

            player.opLoc1(tools)
            advance(ticks = 5)
            assertEquals(1, player.count(ShedToolsObjs.rake))
            assertEquals(1, player.count(objs.spade))
        }

    private companion object {
        // Real placement from the rev 233 map (maplocs.py), rot 0 (west).
        val TOOLS = CoordGrid(3203, 3170)
    }
}
