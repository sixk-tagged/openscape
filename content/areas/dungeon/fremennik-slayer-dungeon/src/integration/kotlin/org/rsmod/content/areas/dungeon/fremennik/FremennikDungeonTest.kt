package org.rsmod.content.areas.dungeon.fremennik

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.stats
import org.rsmod.api.testing.GameTestState
import org.rsmod.game.loc.LocAngle
import org.rsmod.game.loc.LocShape
import org.rsmod.map.CoordGrid

class FremennikDungeonTest {
    @Test
    fun GameTestState.`the cave entrance leads into the dungeon`() =
        runGameTest(FremennikDungeonScript::class) {
            val cave =
                placeMapLoc(CoordGrid(2797, 3614), FremennikLocs.entrance, angle = LocAngle.North)
            player.teleport(CoordGrid(2796, 3615))
            advance()

            player.opLoc1(cave)
            advanceUntil(max = 10) { player.coords == CoordGrid(2808, 10002) }
        }

    @Test
    fun GameTestState.`the strange floor needs 43 agility`() =
        runGameTest(FremennikDungeonScript::class) {
            val floor =
                placeMapLoc(
                    CoordGrid(2769, 10002),
                    FremennikLocs.strange_floor,
                    angle = LocAngle.East,
                )
            player.teleport(CoordGrid(2768, 10002))
            advance()

            player.opLoc1(floor)
            advance(ticks = 3)
            assertEquals(CoordGrid(2768, 10002), player.coords)

            player.setBaseLevel(stats.agility, 43)
            player.setCurrentLevel(stats.agility, 43)
            player.opLoc1(floor)
            advanceUntil(max = 10) { player.coords == CoordGrid(2770, 10002) }
        }

    @Test
    fun GameTestState.`the crevice needs 61 agility`() =
        runGameTest(FremennikDungeonScript::class) {
            val crevice =
                placeMapLoc(
                    CoordGrid(2731, 10008),
                    FremennikLocs.crevice,
                    shape = LocShape.WallStraight,
                    angle = LocAngle.West,
                )
            player.teleport(CoordGrid(2730, 10008))
            advance()

            player.opLoc1(crevice)
            advance(ticks = 3)
            assertEquals(CoordGrid(2730, 10008), player.coords)

            player.setBaseLevel(stats.agility, 61)
            player.setCurrentLevel(stats.agility, 61)
            player.opLoc1(crevice)
            advanceUntil(max = 10) { player.coords == CoordGrid(2735, 10008) }
        }
}
