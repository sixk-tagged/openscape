package org.rsmod.content.skills.magic.spell.utility

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.annotations.InternalApi
import org.rsmod.api.config.refs.areas
import org.rsmod.api.config.refs.components
import org.rsmod.api.config.refs.interfaces
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.config.refs.varps
import org.rsmod.api.player.isTeleblocked
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.entity.Player
import org.rsmod.game.entity.player.SessionStateEvent
import org.rsmod.game.inv.InvObj
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class TeleBlockTest {
    @Test
    fun GameTestState.`tele block lasts five minutes and stops teleports`() = utilityTest { deps ->
        val victim = blockedVictim(deps)
        assertEquals(500, victim.vars[varps.teleblock_ticks])
        assertTrue(victim.isTeleblocked())
        assertMessageSent(
            "<col=800080>A Tele Block has been cast on you by ${player.displayName}. " +
                "It will expire in 5 minutes.</col>",
            victim,
        )
        assertEquals(4, player.inv.count(objs.law_rune))
        assertEquals(4, player.inv.count(objs.chaos_rune))
        assertEquals(4, player.inv.count(objs.death_rune))

        val varrock = checkNotNull(deps.spells.getObjSpell(objs.spell_varrock_teleport))
        victim.ifButton(varrock.component)
        advanceUntil(max = 3) { sent("A magical force stops you from teleporting.", victim) }
        advance(ticks = 8)
        assertEquals(START.translateX(1), victim.coords)
        assertEquals(5, victim.inv.count(objs.law_rune))
    }

    @Test
    fun GameTestState.`protect from magic halves a tele block`() = utilityTest { deps ->
        setUpCaster()
        val victim = prepareVictim()
        victim.setVarBit(varbits.protect_from_magic, 1)
        random.next = 0
        castAt(deps, objs.spell_tele_block, victim)
        assertEquals(250, victim.vars[varps.teleblock_ticks])
        assertMessageSent(
            "<col=800080>A Tele Block has been cast on you by ${player.displayName}. " +
                "It will expire in 2 minutes, 30 seconds.</col>",
            victim,
        )
    }

    @Test
    fun GameTestState.`a tele block runs out`() = utilityTest { deps ->
        val victim = blockedVictim(deps)
        victim.setVarp(varps.teleblock_ticks, 3)
        advanceUntil(max = 6) { sent("Your Tele Block has expired.", victim) }
        assertEquals(0, victim.vars[varps.teleblock_ticks])
        assertFalse(victim.isTeleblocked())
    }

    @Test
    fun GameTestState.`logging out ends a tele block`() = utilityTest { deps ->
        val victim = blockedVictim(deps)
        eventBus.publish(SessionStateEvent.Logout(victim))
        assertFalse(victim.isTeleblocked())
    }

    @Test
    fun GameTestState.`a blocked player cannot be blocked again`() = utilityTest { deps ->
        val victim = blockedVictim(deps)
        victim.setVarp(varps.teleblock_ticks, 100)
        random.next = 0
        castAt(deps, objs.spell_tele_block, victim)
        assertMessageSent("This player is already effected by this spell.")
        assertEquals(100, victim.vars[varps.teleblock_ticks])
        assertEquals(4, player.inv.count(objs.law_rune))
    }

    @Test
    fun GameTestState.`teleports are refused deeper than Wilderness level 20`() =
        utilityTest { deps ->
            setUp(objs.law_rune to 5, objs.air_rune to 10, objs.fire_rune to 5)
            deps.wilderness(LEVEL_21)
            player.teleport(LEVEL_21)
            advance()

            val varrock = checkNotNull(deps.spells.getObjSpell(objs.spell_varrock_teleport))
            player.ifButton(varrock.component)
            advanceUntil(max = 3) { sent("You can't teleport above level 20 in the Wilderness.") }
            advance(ticks = 8)
            assertEquals(LEVEL_21, player.coords)
            assertEquals(5, player.inv.count(objs.law_rune))
        }

    @Test
    fun GameTestState.`teleports still work at Wilderness level 20`() = utilityTest { deps ->
        setUp(objs.law_rune to 5, objs.air_rune to 10, objs.fire_rune to 5)
        deps.wilderness(LEVEL_20)
        player.teleport(LEVEL_20)
        advance()

        val varrock = checkNotNull(deps.spells.getObjSpell(objs.spell_varrock_teleport))
        player.ifButton(varrock.component)
        advanceUntil(max = 10) { player.coords == VARROCK }
        assertEquals(4, player.inv.count(objs.law_rune))
    }

    private fun GameTestScope.sent(text: String, to: Player = player): Boolean =
        runCatching { assertMessageSent(text, to) }.isSuccess

    /** The caster is the test player, who has Magic 99 and the runes for several casts. */
    private fun GameTestScope.setUpCaster() {
        setUp(objs.law_rune to 5, objs.chaos_rune to 5, objs.death_rune to 5)
        player.teleport(START)
        player.observerUUID = player.uuid
        advance()
    }

    /** A second player with the runes for a Varrock teleport. */
    private fun GameTestScope.prepareVictim(): Player {
        val victim = registerPlayer(START.translateX(1))
        victim.observerUUID = victim.uuid
        victim.inv[0] = InvObj(objs.law_rune, 5)
        victim.inv[1] = InvObj(objs.air_rune, 10)
        victim.inv[2] = InvObj(objs.fire_rune, 5)
        victim.stats[stats.magic] = 99
        victim.setVarBit(varbits.spellbook, 0)
        victim.ifOpenOverlay(interfaces.magic_spellbook, components.toplevel_target_side6)
        return victim
    }

    /** The test player casts Tele Block on a second player, and it lands. */
    private fun GameTestScope.blockedVictim(deps: Deps): Player {
        setUpCaster()
        val victim = prepareVictim()
        random.next = 0
        castAt(deps, objs.spell_tele_block, victim)
        return victim
    }

    @OptIn(InternalApi::class)
    private fun Deps.wilderness(coord: CoordGrid) {
        areaIndex.registerAll(coord, listOf(areas.wilderness.id.toShort()).iterator())
    }

    private companion object {
        val START = CoordGrid(0, 50, 50, 10, 10)
        val VARROCK = CoordGrid(0, 50, 53, 13, 32)
        val LEVEL_20 = CoordGrid(3000, 3679)
        val LEVEL_21 = CoordGrid(3000, 3680)
    }
}
