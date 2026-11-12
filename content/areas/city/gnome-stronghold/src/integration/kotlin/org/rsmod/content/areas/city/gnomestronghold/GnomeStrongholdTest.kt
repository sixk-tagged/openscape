package org.rsmod.content.areas.city.gnomestronghold

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.testing.GameTestState
import org.rsmod.game.loc.LocAngle
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class GnomeStrongholdTest {
    @Test
    fun GameTestState.`rometti's trade opens the robe shop`() =
        runGameTest(GnomeStrongholdScript::class) {
            val rometti = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[gnome_npcs.rometti])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc3(rometti)
            advanceUntil(max = 10) { player.openedShop != null }
        }

    @Test
    fun GameTestState.`the ground floor ladder's top-floor option goes to the roof`() =
        runGameTest(GnomeStrongholdScript::class) {
            val ladder =
                placeMapLoc(CoordGrid(2466, 3495), gnome_locs.ladder_ground, angle = LocAngle.West)
            player.teleport(CoordGrid(2466, 3494))
            advance()

            player.opLoc2(ladder)
            advanceUntil(max = 10) { player.coords.level == 3 }
        }

    @Test
    fun GameTestState.`the south gate lets you through both ways`() =
        runGameTest(GnomeStrongholdScript::class) {
            val gate = placeMapLoc(CoordGrid(2459, 3383), gnome_locs.area_gate)
            player.teleport(CoordGrid(2461, 3382))
            advance()

            player.opLoc1(gate)
            advanceUntil(max = 10) { player.coords == CoordGrid(2461, 3385) }

            player.opLoc1(gate)
            advanceUntil(max = 10) { player.coords == CoordGrid(2461, 3382) }
        }

    private companion object {
        val OPEN_GROUND = CoordGrid(0, 50, 50, 30, 18)
    }
}
