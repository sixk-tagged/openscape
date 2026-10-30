package org.rsmod.content.other.deathsoffice

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.testing.GameTestState
import org.rsmod.content.other.deathsoffice.configs.DeathLocs
import org.rsmod.content.other.deathsoffice.configs.DeathVarps
import org.rsmod.game.loc.LocAngle
import org.rsmod.map.CoordGrid
import org.rsmod.routefinder.flag.CollisionFlag

class DeathsOfficeTest {
    @Test
    fun GameTestState.`lumbridge grave enters the office`() =
        deathTest(DeathsOfficeScript::class) {
            val grave =
                placeMapLoc(LUMBRIDGE_GRAVE, DeathLocs.lumbridge_grave, angle = LocAngle.East)
            // The graveyard is fenced: the grave is only reachable from its east side.
            player.teleport(DeathsOfficeScript.LUMBRIDGE_RETURN)
            player.opLoc1(grave)
            advanceUntil(max = 10) { player.coords == DeathsOfficeScript.OFFICE_ARRIVAL }
        }

    @Test
    fun GameTestState.`portal returns to lumbridge`() =
        deathTest(DeathsOfficeScript::class) {
            finishDeathTutorial()
            val portal = placeMapLoc(OFFICE_PORTAL, DeathLocs.exit_portal, angle = LocAngle.South)
            player.teleport(DeathsOfficeScript.OFFICE_ARRIVAL)
            player.opLoc1(portal)
            advanceUntil(max = 10) { player.coords.z < OFFICE_PORTAL.z }
            assertEquals(DeathsOfficeScript.LUMBRIDGE_RETURN, player.coords)
        }

    @Test
    fun GameTestState.`the falador coffin's portal leads back to the crypt`() =
        deathTest(DeathsOfficeScript::class) {
            finishDeathTutorial()
            val back = DeathsOfficeScript.FALADOR_RETURN
            assertEquals(0, collision[back.x, back.z, back.level] and CollisionFlag.BLOCK_WALK)
            val coffin =
                placeMapLoc(FALADOR_COFFIN, DeathLocs.falador_coffin, angle = LocAngle.North)
            player.teleport(back)
            player.opLoc1(coffin)
            advanceUntil(max = 10) { player.coords == DeathsOfficeScript.OFFICE_ARRIVAL }

            val portal = placeMapLoc(OFFICE_PORTAL, DeathLocs.exit_portal, angle = LocAngle.South)
            player.opLoc1(portal)
            advanceUntil(max = 10) { player.coords == back }
        }

    @Test
    fun GameTestState.`portal is blocked during the tutorial`() =
        deathTest(DeathsOfficeScript::class) {
            player.setVarp(DeathVarps.death_tutorial, 1)
            val portal = placeMapLoc(OFFICE_PORTAL, DeathLocs.exit_portal, angle = LocAngle.South)
            player.teleport(DeathsOfficeScript.OFFICE_ARRIVAL)
            player.opLoc1(portal)
            advance(ticks = 10)
            assertTrue(player.coords.z >= OFFICE_PORTAL.z - 5) { "left to ${player.coords}" }
        }

    private companion object {
        // Real placements from the rev 233 map.
        val LUMBRIDGE_GRAVE = CoordGrid(3238, 3192)
        val OFFICE_PORTAL = CoordGrid(3169, 5726)
        val FALADOR_COFFIN = CoordGrid(2977, 9736)
    }
}
