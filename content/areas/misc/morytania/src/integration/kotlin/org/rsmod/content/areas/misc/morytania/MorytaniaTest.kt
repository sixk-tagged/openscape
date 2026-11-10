package org.rsmod.content.areas.misc.morytania

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.stats
import org.rsmod.api.testing.GameTestState
import org.rsmod.game.loc.LocAngle
import org.rsmod.game.loc.LocShape
import org.rsmod.map.CoordGrid

class MorytaniaTest {
    @Test
    fun GameTestState.`the holy barrier lets players into morytania`() =
        runGameTest(MorytaniaLocScript::class) {
            val barrier =
                placeMapLoc(
                    CoordGrid(3440, 9886),
                    MorytaniaLocs.holy_barrier,
                    angle = LocAngle.North,
                )
            player.teleport(CoordGrid(3440, 9887))
            advance()

            player.opLoc1(barrier)
            advanceUntil(max = 10) { player.coords == CoordGrid(3423, 3485) }
        }

    @Test
    fun GameTestState.`the slayer tower door lets players in and out`() =
        runGameTest(MorytaniaLocScript::class) {
            val coords = CoordGrid(3428, 3535)
            val door =
                placeMapLoc(
                    coords,
                    MorytaniaLocs.tower_door,
                    shape = LocShape.WallStraight,
                    angle = LocAngle.North,
                )
            player.teleport(coords)
            advance()

            player.opLoc1(door)
            advanceUntil(max = 10) { player.coords == coords.translateZ(1) }
        }

    @Test
    fun GameTestState.`the tower stairs climb to the first floor`() =
        runGameTest(MorytaniaLocScript::class) {
            val stairs =
                placeMapLoc(CoordGrid(3434, 3537), MorytaniaLocs.stairs_1, angle = LocAngle.South)
            player.teleport(CoordGrid(3438, 3537))
            advance()

            player.opLoc1(stairs)
            advanceUntil(max = 15) { player.coords == CoordGrid(3433, 3537, 1) }
        }

    @Test
    fun GameTestState.`the spikey chains need 61 and 71 agility`() =
        runGameTest(MorytaniaLocScript::class) {
            val upper = placeMapLoc(CoordGrid(3447, 3576, 1), MorytaniaLocs.chain_bottom)
            player.teleport(CoordGrid(3447, 3575, 1))
            player.setBaseLevel(stats.agility, 61)
            player.setCurrentLevel(stats.agility, 61)
            advance()

            player.opLoc1(upper)
            advance(ticks = 3)
            assertEquals(CoordGrid(3447, 3575, 1), player.coords)

            val lower = placeMapLoc(CoordGrid(3422, 3550), MorytaniaLocs.chain_bottom)
            player.teleport(CoordGrid(3421, 3550))
            advance()
            player.opLoc1(lower)
            advanceUntil(max = 10) { player.coords == CoordGrid(3421, 3550, 1) }
        }

    @Test
    fun GameTestState.`the broken window needs 18 agility and the ivy 81`() =
        runGameTest(MorytaniaLocScript::class) {
            player.setBaseLevel(stats.agility, 18)
            player.setCurrentLevel(stats.agility, 18)
            val ivy =
                placeMapLoc(
                    CoordGrid(3418, 3533),
                    MorytaniaLocs.ivy,
                    shape = LocShape.WallStraight,
                    angle = LocAngle.North,
                )
            player.teleport(CoordGrid(3418, 3532))
            advance()
            player.opLoc1(ivy)
            advance(ticks = 3)
            assertEquals(0, player.coords.level)

            val window =
                placeMapLoc(
                    CoordGrid(3443, 3532),
                    MorytaniaLocs.window,
                    shape = LocShape.WallDiagonal,
                    angle = LocAngle.North,
                )
            player.teleport(CoordGrid(3444, 3533))
            advance()
            player.opLoc1(window)
            advanceUntil(max = 10) { player.coords == CoordGrid(3442, 3531) }
            advance(ticks = 2)
            player.opLoc1(window)
            advanceUntil(max = 10) { player.coords == CoordGrid(3444, 3533) }
        }
}
