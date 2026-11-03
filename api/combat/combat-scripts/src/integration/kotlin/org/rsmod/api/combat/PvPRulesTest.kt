package org.rsmod.api.combat

import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.annotations.InternalApi
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.areas
import org.rsmod.api.config.refs.varps
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.area.AreaIndex
import org.rsmod.game.entity.Player
import org.rsmod.game.entity.player.SessionStateEvent
import org.rsmod.map.CoordGrid

@Execution(ExecutionMode.SAME_THREAD)
class PvPRulesTest {
    @Test
    fun GameTestState.`attacking from outside the Wilderness is refused`() = pvpTest { deps ->
        val target = registerPlayer(OUTSIDE.translateX(1))
        deps.wilderness(OUTSIDE.translateX(1))
        player.teleport(OUTSIDE)
        assertFalse(canAttack(player, target))
        assertMessageSent("You can't attack other players here.")
    }

    @Test
    fun GameTestState.`attacking someone outside the Wilderness is refused`() = pvpTest { deps ->
        val target = registerPlayer(OUTSIDE)
        player.teleport(WILD_LEVEL_1)
        deps.wilderness(WILD_LEVEL_1)
        assertFalse(canAttack(player, target))
        assertMessageSent("You can't attack that player here.")
    }

    @Test
    fun GameTestState.`combat levels must be within the Wilderness level`() = pvpTest { deps ->
        val target = registerPlayer(WILD_LEVEL_1.translateX(1))
        deps.wilderness(WILD_LEVEL_1)
        deps.wilderness(WILD_LEVEL_1.translateX(1))
        deps.wilderness(WILD_LEVEL_11)
        deps.wilderness(WILD_LEVEL_11.translateX(1))
        player.teleport(WILD_LEVEL_1)
        player.appearance.combatLevel = 100
        target.appearance.combatLevel = 102
        assertFalse(canAttack(player, target))
        assertMessagesSent(
            "Your level difference is too great!",
            "You need to move deeper into the Wilderness to attack this player.",
        )

        // Level 11 allows an 11 level difference.
        player.teleport(WILD_LEVEL_11)
        target.teleport(WILD_LEVEL_11.translateX(1))
        assertTrue(canAttack(player, target))
    }

    @Test
    fun GameTestState.`starting a fight skulls the attacker for 30 minutes`() = pvpTest { _ ->
        val victim = registerPlayer(OUTSIDE.translateX(1))
        player.withProtectedAccess { skullUnlessRetaliating(victim) }
        assertEquals(constants.skullicon_default, player.skullIcon)
        assertEquals(SKULL_TICKS, player.vars[varps.skull_ticks])
        assertNull(victim.skullIcon)
    }

    @Test
    fun GameTestState.`fighting back does not skull`() = pvpTest { _ ->
        val aggressor = registerPlayer(OUTSIDE.translateX(1))
        player.setVarp(varps.pk_predator1, aggressor.uid.packed)
        player.withProtectedAccess { skullUnlessRetaliating(aggressor) }
        assertNull(player.skullIcon)
        assertEquals(0, player.vars[varps.skull_ticks])
    }

    @Test
    fun GameTestState.`a skull wears off when its time is up`() = pvpTest { _ ->
        advance(ticks = 11) // Out of the combat window that starts at tick 0.
        val victim = registerPlayer(OUTSIDE.translateX(1))
        player.withProtectedAccess { skullUnlessRetaliating(victim) }
        player.setVarp(varps.skull_ticks, 3)
        advance(ticks = 5)
        assertNull(player.skullIcon)
        assertEquals(0, player.vars[varps.skull_ticks])
    }

    @Test
    fun GameTestState.`a skull keeps its time through a logout`() = pvpTest { _ ->
        val victim = registerPlayer(OUTSIDE.translateX(1))
        player.withProtectedAccess { skullUnlessRetaliating(victim) }
        advance(ticks = 10)
        val before = player.vars[varps.skull_ticks]
        assertTrue(before in SKULL_TICKS - 11 until SKULL_TICKS) { "ticks=$before" }

        eventBus.publish(SessionStateEvent.Logout(player))
        unregisterPlayer(player)
        advance(ticks = 50)
        registerPlayer(player = player)
        assertEquals(before, player.vars[varps.skull_ticks])
        assertEquals(constants.skullicon_default, player.skullIcon)

        advance(ticks = 10)
        assertTrue(player.vars[varps.skull_ticks] in before - 11 until before)
    }

    class Deps @Inject constructor(val areaIndex: AreaIndex)

    private fun GameTestState.pvpTest(test: GameTestScope.(Deps) -> Unit) =
        runInjectedGameTest(Deps::class, null, SkullScript::class, testBody = test)

    /** Puts [coord] in the Wilderness area, as the packed map would. */
    @OptIn(InternalApi::class)
    private fun Deps.wilderness(coord: CoordGrid) {
        areaIndex.registerAll(coord, listOf(areas.wilderness.id.toShort()).iterator())
    }

    private fun GameTestScope.canAttack(attacker: Player, target: Player): Boolean {
        var allowed = true
        attacker.withProtectedAccess { allowed = canAttackPlayer(target) }
        return allowed
    }

    private companion object {
        val OUTSIDE = CoordGrid(3200, 3200)
        val WILD_LEVEL_1 = CoordGrid(3000, 3525)
        val WILD_LEVEL_11 = CoordGrid(3000, 3600)
    }
}
