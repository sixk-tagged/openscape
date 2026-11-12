package org.rsmod.content.areas.city.yanille

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.stats
import org.rsmod.api.testing.GameTestState
import org.rsmod.game.loc.LocAngle
import org.rsmod.game.loc.LocShape
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class YanilleTest {
    @Test
    fun GameTestState.`aleck's trade opens the hunter shop`() =
        runGameTest(YanilleScript::class) {
            val aleck = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[yanille_npcs.aleck])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc5(aleck)
            advanceUntil(max = 10) { player.openedShop != null }
        }

    @Test
    fun GameTestState.`the magic guild door needs 66 magic`() =
        runGameTest(YanilleScript::class) {
            val door =
                placeMapLoc(
                    DOOR,
                    yanille_locs.guild_door_left,
                    shape = LocShape.WallStraight,
                    angle = LocAngle.East,
                )
            player.teleport(DOOR)
            advance()

            player.opLoc1(door)
            advance(ticks = 3)
            assertEquals(DOOR, player.coords)

            player.setBaseLevel(stats.magic, 66)
            player.setCurrentLevel(stats.magic, 66)
            player.opLoc1(door)
            advanceUntil(max = 10) { player.coords == DOOR.translateX(1) }
        }

    private companion object {
        val OPEN_GROUND = CoordGrid(0, 50, 50, 30, 18)
        val DOOR = CoordGrid(0, 50, 50, 34, 31)
    }
}
