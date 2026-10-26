package org.rsmod.content.generic.locs.containers

import org.junit.jupiter.api.Test
import org.rsmod.api.testing.GameTestState
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.map.CoordGrid

class ContainerScriptTest {
    @Test
    fun GameTestState.`open, search and shut a chest`() =
        runGameTest(ContainerScript::class) {
            val chest = placeMapLoc(SPOT, ContainerLocs.chest)
            player.teleport(SPOT.translateZ(1)) // Chests only open from the north (fa 30).

            player.opLoc1(chest)
            advance(ticks = 3)
            assertExists(SPOT, ContainerLocs.chest_open)

            val openType = locTypes[ContainerLocs.chest_open]
            val open = BoundLocInfo(checkNotNull(findLoc(SPOT, ContainerLocs.chest_open)), openType)
            player.opLoc2(open)
            advance() // advance() clears messages every tick: assert on the search's tick.
            assertMessageSent("You search the chest but find nothing.")

            player.opLoc3(open)
            advance(ticks = 3)
            assertExists(SPOT, ContainerLocs.chest)
            assertDoesNotExist(SPOT, ContainerLocs.chest_open)
        }

    private companion object {
        val SPOT = CoordGrid(0, 50, 50, 34, 31)
    }
}
