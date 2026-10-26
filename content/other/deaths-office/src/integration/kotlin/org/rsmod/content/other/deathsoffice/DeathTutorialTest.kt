package org.rsmod.content.other.deathsoffice

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.interfaces
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.other.deathsoffice.configs.DeathLocs
import org.rsmod.content.other.deathsoffice.configs.DeathNpcs
import org.rsmod.content.other.deathsoffice.configs.DeathVarps
import org.rsmod.game.loc.LocAngle
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class DeathTutorialTest {
    @Test
    fun GameTestState.`first death with lost items starts the tutorial in the office`() =
        deathTest(DeathScript::class) {
            carrySix()
            die()
            assertEquals(DeathsOfficeScript.OFFICE_ARRIVAL, player.coords)
            assertEquals(GravestoneScript.TUTORIAL_ACTIVE, tutorial())
            assertTrue(player.ui.containsModal(interfaces.chat_left)) { "Death is not talking" }
        }

    @Test
    fun GameTestState.`leaving before every topic is covered is refused`() =
        deathTest(DeathScript::class) {
            carrySix()
            die()
            talkThrough(TOPIC_FEE, DONE)
            assertEquals(GravestoneScript.TUTORIAL_ACTIVE or 2, tutorial())
        }

    @Test
    fun GameTestState.`the grave waits for the portal after the tutorial`() =
        deathTest(DeathScript::class, DeathsOfficeScript::class) {
            carrySix()
            die()
            talkThrough(TOPIC_FEE, TOPIC_TIME, TOPIC_ITEMS, DONE)
            assertEquals(1 or 2 or 4 or 8 or 16, tutorial())
            advance(ticks = 10)
            assertEquals(GravestoneScript.GRAVE_TICKS, graveTicks())

            leaveThroughPortal()
            assertEquals(GravestoneScript.TUTORIAL_DONE or 2 or 4 or 16, tutorial())
            advance(ticks = 10)
            assertTrue(graveTicks() < GravestoneScript.GRAVE_TICKS) { "grave is not ticking" }
        }

    @Test
    fun GameTestState.`leaving the office another way starts the grave`() =
        deathTest(DeathScript::class) {
            carrySix()
            die()
            talkThrough(DONE)
            player.teleport(DeathsOfficeScript.LUMBRIDGE_RETURN)
            advance(ticks = 10)
            assertTrue(graveTicks() < GravestoneScript.GRAVE_TICKS) { "grave is not ticking" }
            assertEquals(0, tutorial() and GravestoneScript.TUTORIAL_ACTIVE)
        }

    @Test
    fun GameTestState.`talking to death mid-tutorial picks up the topics`() =
        deathTest(DeathScript::class) {
            carrySix()
            die()
            talkThrough(DONE)
            // Beside the player: the harness has no desk to talk across.
            val beside = DeathsOfficeScript.OFFICE_ARRIVAL.translateX(1)
            val death = spawnNpc(beside, cacheTypes.npcs[DeathNpcs.death])
            player.opNpc1(death)
            talkThrough(TOPIC_FEE, TOPIC_TIME, TOPIC_ITEMS, DONE)
            assertEquals(1 or 2 or 4 or 8 or 16, tutorial())
        }

    @Test
    fun GameTestState.`second death skips the tutorial`() =
        deathTest(DeathScript::class) {
            finishDeathTutorial()
            carrySix()
            die()
            assertTrue(player.coords.z < DEATH_TILE.z - 100) { "sent to ${player.coords}" }
            assertEquals(GravestoneScript.TUTORIAL_DONE, tutorial())
        }

    private fun GameTestScope.tutorial(): Int = player.vars[DeathVarps.death_tutorial]

    private fun GameTestScope.graveTicks(): Int = player.vars[DeathVarps.gravestone_ticks]

    private fun GameTestScope.leaveThroughPortal() {
        val portal = placeMapLoc(PORTAL_TILE, DeathLocs.exit_portal, angle = LocAngle.South)
        player.opLoc1(portal)
        advanceUntil(max = 10) { player.coords == DeathsOfficeScript.LUMBRIDGE_RETURN }
    }

    private companion object {
        /** Menu slots of Death's tutorial topics. */
        const val TOPIC_FEE = 2
        const val TOPIC_TIME = 3
        const val TOPIC_ITEMS = 4
        const val DONE = 5

        val DEATH_TILE = CoordGrid(3180, 5727)
        val PORTAL_TILE = CoordGrid(3169, 5726)
    }
}
