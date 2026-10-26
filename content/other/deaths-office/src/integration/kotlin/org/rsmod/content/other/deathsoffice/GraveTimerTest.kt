package org.rsmod.content.other.deathsoffice

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.testing.GameTestState
import org.rsmod.content.other.deathsoffice.configs.DeathInvs
import org.rsmod.content.other.deathsoffice.configs.DeathVarBits
import org.rsmod.content.other.deathsoffice.configs.DeathVarps
import org.rsmod.game.entity.player.SessionStateEvent

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class GraveTimerTest {
    @Test
    fun GameTestState.`the grave counts down while logged in`() = deathTest {
        finishDeathTutorial()
        carrySix()
        die()
        val before = player.vars[DeathVarps.gravestone_ticks]
        advance(ticks = 10)
        val elapsed = before - player.vars[DeathVarps.gravestone_ticks]
        assertTrue(elapsed in 9..10) { "elapsed=$elapsed" }
    }

    @Test
    fun GameTestState.`grave collapses when its time runs out`() = deathTest { graves ->
        finishDeathTutorial()
        carrySix()
        die()
        player.setVarp(DeathVarps.gravestone_ticks, 3)
        advance(ticks = 5)
        assertEquals(emptySet<Int>(), graveObjs())
        assertEquals(setOf(steel, iron, logs), objIds(DeathInvs.death_permanent))
        assertNull(graves.npc(player))
        assertEquals(0, player.vars[DeathVarps.gravestone_ticks])
        assertEquals(0, player.vars[DeathVarBits.gravestone_visible])
    }

    @Test
    fun GameTestState.`logout pauses the grave and frees its slot`() = deathTest { graves ->
        finishDeathTutorial()
        carrySix()
        die()
        val graveTile = graves.npc(player)?.coords
        val before = player.vars[DeathVarps.gravestone_ticks]
        logout()
        assertNull(graves.npc(player))
        advance(ticks = 50)
        login()
        assertEquals(before, player.vars[DeathVarps.gravestone_ticks])
        assertEquals(graveTile, graves.npc(player)?.coords)
    }

    @Test
    fun GameTestState.`login re-spawns an existing grave`() = deathTest { graves ->
        carrySix()
        die()
        val graveTile = graves.npc(player)?.coords
        graves.despawn(player)
        eventBus.publish(SessionStateEvent.Login(player))
        assertEquals(graveTile, graves.npc(player)?.coords)
        assertEquals(1, player.vars[DeathVarBits.gravestone_visible])
    }

    @Test
    fun GameTestState.`login clears a stale grave slot`() = deathTest {
        player.setVarBit(DeathVarBits.gravestone_visible, 7)
        eventBus.publish(SessionStateEvent.Login(player))
        assertEquals(0, player.vars[DeathVarBits.gravestone_visible])
    }

    @Test
    fun GameTestState.`the grave waits while the death tutorial runs`() = deathTest {
        carrySix()
        die()
        advance(ticks = 10)
        assertEquals(1500, player.vars[DeathVarps.gravestone_ticks])
    }

    @Test
    fun GameTestState.`the hud timer follows the grave`() = deathTest {
        finishDeathTutorial()
        carrySix()
        die()
        assertEquals(
            player.vars[DeathVarps.gravestone_ticks],
            player.vars[DeathVarBits.gravestone_duration],
        )
        player.setVarBit(DeathVarBits.gravestone_duration, 0)
        eventBus.publish(SessionStateEvent.Login(player))
        assertEquals(
            player.vars[DeathVarps.gravestone_ticks],
            player.vars[DeathVarBits.gravestone_duration],
        )
        player.setVarp(DeathVarps.gravestone_ticks, 2)
        advance(ticks = 3)
        assertEquals(0, player.vars[DeathVarBits.gravestone_duration])
    }
}
