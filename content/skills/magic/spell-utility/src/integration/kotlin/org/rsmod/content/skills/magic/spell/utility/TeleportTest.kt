package org.rsmod.content.skills.magic.spell.utility

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.testing.GameTestState
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class TeleportTest {
    @Test
    fun GameTestState.`varrock teleport moves the player and takes the runes`() =
        utilityTest { deps ->
            setUp(objs.law_rune to 5, objs.air_rune to 10, objs.fire_rune to 5)
            player.teleport(START)
            advance()

            val spell = checkNotNull(deps.spells.getObjSpell(objs.spell_varrock_teleport))
            player.ifButton(spell.component)
            val ticks = advanceUntil(max = 10) { player.coords == VARROCK }

            assertTrue(ticks >= 3) { "Landed after $ticks ticks" }
            assertEquals(4, player.inv.count(objs.law_rune))
            assertEquals(7, player.inv.count(objs.air_rune))
            assertEquals(4, player.inv.count(objs.fire_rune))
            assertEquals(fineXp(35.0), gained(stats.magic))
        }

    @Test
    fun GameTestState.`a teleport without runes goes nowhere`() = utilityTest { deps ->
        setUp(objs.law_rune to 5, objs.air_rune to 2, objs.fire_rune to 5)
        player.teleport(START)
        advance()

        val spell = checkNotNull(deps.spells.getObjSpell(objs.spell_varrock_teleport))
        player.ifButton(spell.component)
        advance(ticks = 8)

        assertEquals(START, player.coords)
        assertEquals(5, player.inv.count(objs.law_rune))
    }

    @Test
    fun GameTestState.`every standard teleport has a destination`() = utilityTest { deps ->
        // The script fails at startup when a teleport spell has no cache destination or button.
        for (obj in listOf(objs.spell_apeatoll_teleport, objs.spell_trollheim_teleport)) {
            checkNotNull(deps.spells.getObjSpell(obj))
        }
    }

    private companion object {
        val START = CoordGrid(0, 50, 50, 34, 31)
        val VARROCK = CoordGrid(0, 50, 53, 13, 32)
    }
}
