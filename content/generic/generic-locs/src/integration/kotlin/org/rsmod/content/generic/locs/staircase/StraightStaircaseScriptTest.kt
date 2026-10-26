package org.rsmod.content.generic.locs.staircase

import org.junit.jupiter.api.Test
import org.rsmod.api.testing.GameTestState
import org.rsmod.game.loc.LocAngle
import org.rsmod.game.loc.LocShape
import org.rsmod.map.CoordGrid

/** Real Draynor placements; the landings are the ones RuneLite's shortest-path plugin records. */
class StraightStaircaseScriptTest {
    @Test
    fun GameTestState.`a straight staircase goes 4 tiles through, up and back down`() =
        runGameTest(StraightStaircaseScript::class) {
            val bottom = placeMapLoc(STAIRS, StraightStaircaseLocs.bottom, SHAPE, LocAngle.North)
            val top = placeMapLoc(STAIRS_TOP, StraightStaircaseLocs.top, SHAPE, LocAngle.North)
            player.teleport(CoordGrid(3098, 3267))

            player.opLoc1(bottom)
            advanceUntil(max = 10) { player.coords.level == 1 }
            assertEquals(CoordGrid(3102, 3266, 1), player.coords)

            player.opLoc1(top)
            advanceUntil(max = 10) { player.coords.level == 0 }
            assertEquals(CoordGrid(3098, 3266, 0), player.coords)
        }

    @Test
    fun GameTestState.`the narrow wooden stairs work the same way`() =
        runGameTest(StraightStaircaseScript::class) {
            val bottom =
                placeMapLoc(NARROW, StraightStaircaseLocs.narrow_bottom, SHAPE, LocAngle.South)
            val top =
                placeMapLoc(NARROW_TOP, StraightStaircaseLocs.narrow_top, SHAPE, LocAngle.South)
            player.teleport(CoordGrid(3089, 3251))

            player.opLoc1(bottom)
            advanceUntil(max = 10) { player.coords.level == 1 }
            assertEquals(CoordGrid(3093, 3251, 1), player.coords)

            player.opLoc1(top)
            advanceUntil(max = 10) { player.coords.level == 0 }
            assertEquals(CoordGrid(3089, 3251, 0), player.coords)
        }

    private companion object {
        val SHAPE = LocShape.CentrepieceStraight
        val STAIRS = CoordGrid(3099, 3266)
        val STAIRS_TOP = CoordGrid(3100, 3266, 1)
        val NARROW = CoordGrid(3090, 3251)
        val NARROW_TOP = CoordGrid(3091, 3251, 1)
    }
}
