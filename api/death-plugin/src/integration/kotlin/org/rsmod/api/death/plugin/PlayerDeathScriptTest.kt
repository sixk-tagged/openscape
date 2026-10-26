package org.rsmod.api.death.plugin

import kotlin.math.abs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.varps
import org.rsmod.api.death.PlayerDeathItems
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.queueDeath
import org.rsmod.api.realm.Realm
import org.rsmod.api.testing.GameTestState
import org.rsmod.game.inv.InvObj
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class PlayerDeathScriptTest {
    @Test
    fun GameTestState.`death respawns at the realm respawn`() =
        runInjectedGameTest(Realm::class, null, PlayerDeathScript::class) { realm ->
            player.setCurrentLevel(stats.hitpoints, 0)
            player.queueDeath()
            advance(ticks = 12)
            val respawn = realm.config.respawnCoord
            val near =
                abs(player.coords.x - respawn.x) <= 2 && abs(player.coords.z - respawn.z) <= 2
            assertTrue(near) { "Respawned at ${player.coords}, expected near $respawn" }
        }

    @Test
    fun GameTestState.`death restores run and special energy`() =
        runInjectedGameTest(Realm::class, null, PlayerDeathScript::class) {
            player.runEnergy = 0
            player.setVarp(varps.sa_energy, 0)
            player.setCurrentLevel(stats.hitpoints, 0)
            player.queueDeath()
            advance(ticks = 12)
            assertEquals(constants.run_max_energy, player.runEnergy)
            assertEquals(constants.sa_max_energy, player.vars[varps.sa_energy])
        }

    @Test
    fun GameTestState.`death without a subscriber keeps every item`() =
        runInjectedGameTest(Realm::class, null, PlayerDeathScript::class) {
            player.inv[0] = InvObj(objs.abyssal_whip)
            player.inv[1] = InvObj(objs.coins, 500)
            player.setCurrentLevel(stats.hitpoints, 0)
            player.queueDeath()
            advance(ticks = 12)
            assertEquals(InvObj(objs.abyssal_whip), player.inv[0])
            assertEquals(InvObj(objs.coins, 500), player.inv[1])
        }

    @Test
    fun GameTestState.`death publishes the tile the player died on`() =
        runInjectedGameTest(Realm::class, null, PlayerDeathScript::class) {
            var published: CoordGrid? = null
            eventBus.subscribeSuspend<ProtectedAccess, PlayerDeathItems>(
                PlayerDeathItems::class.java,
                0L,
            ) { event ->
                published = event.deathCoords
            }
            val deathTile = player.coords
            player.setCurrentLevel(stats.hitpoints, 0)
            player.queueDeath()
            advance(ticks = 12)
            assertEquals(deathTile, published)
        }
}
