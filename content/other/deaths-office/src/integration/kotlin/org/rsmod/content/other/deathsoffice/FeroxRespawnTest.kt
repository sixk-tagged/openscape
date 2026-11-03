package org.rsmod.content.other.deathsoffice

import kotlin.math.abs
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.varps
import org.rsmod.api.death.PlayerDeath
import org.rsmod.api.testing.GameTestState
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class FeroxRespawnTest {
    @Test
    fun GameTestState.`a player who moved their respawn to the Ferox Enclave dies into it`() =
        deathTest {
            finishDeathTutorial()
            player.setVarp(varps.ferox_respawn, PlayerDeath.FEROX_RESPAWN_SET)
            val ferox = PlayerDeath.FEROX_RESPAWN
            for (dx in -2..2 step 2) {
                for (dz in -2..2 step 2) {
                    allocZoneCollision(ferox.translate(dx, dz))
                }
            }
            player.teleport(START)
            die()

            val at = player.coords
            assertTrue(abs(at.x - ferox.x) <= 2 && abs(at.z - ferox.z) <= 2) { "respawned at $at" }
        }

    private companion object {
        val START = CoordGrid(3000, 3525)
    }
}
