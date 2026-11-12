package org.rsmod.content.areas.dungeon.stronghold

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.loc.LocAngle
import org.rsmod.game.loc.LocShape
import org.rsmod.map.CoordGrid

class StrongholdSlayerCaveTest {
    @Test
    fun GameTestState.`the cave entrance leads in and the tunnel leads back out`() =
        runGameTest(StrongholdSlayerCaveScript::class) {
            val cave =
                placeMapLoc(CoordGrid(2428, 3423), StrongholdLocs.entrance, angle = LocAngle.East)
            player.teleport(CoordGrid(2430, 3424))
            advance()

            player.opLoc1(cave)
            advanceUntil(max = 10) { player.coords == CoordGrid(2429, 9824) }
        }

    @Test
    fun GameTestState.`the tunnel joins the two halves of the cave`() =
        runGameTest(StrongholdSlayerCaveScript::class) {
            val tunnel =
                placeMapLoc(
                    CoordGrid(2434, 9807),
                    StrongholdLocs.tunnel_right,
                    shape = LocShape.WallStraight,
                    angle = LocAngle.East,
                )
            player.teleport(CoordGrid(2435, 9807))
            player.stats[stats.hitpoints] = 99
            advance()

            // Without 72 Agility the way out still works, for up to 12 damage.
            player.opLoc1(tunnel)
            advanceUntil(max = 10) { player.coords == CoordGrid(2429, 9807) }
            assertTrue(player.hitpoints in 87..99) { "hp=${player.hitpoints}" }
        }

    @Test
    fun GameTestState.`the tunnel in needs 72 agility`() =
        runGameTest(StrongholdSlayerCaveScript::class) {
            val tunnel =
                placeMapLoc(
                    CoordGrid(2430, 9807),
                    StrongholdLocs.tunnel_left,
                    shape = LocShape.WallStraight,
                    angle = LocAngle.West,
                )
            player.teleport(CoordGrid(2429, 9807))
            player.stats[stats.agility] = 71
            player.stats[stats.hitpoints] = 99
            advance()

            player.opLoc1(tunnel)
            advanceUntil(max = 10) {
                messageSent("You need an Agility level of 72 to use this shortcut.")
            }
            assertTrue(player.coords.x <= 2432)

            player.stats[stats.agility] = 72
            player.opLoc1(tunnel)
            advanceUntil(max = 10) { player.coords == CoordGrid(2435, 9807) }
            assertEquals(99, player.hitpoints)
        }

    /** Messages only last the tick they are sent in, so check while advancing. */
    private fun GameTestScope.messageSent(text: String): Boolean =
        runCatching { assertMessageSent(text) }.isSuccess
}
