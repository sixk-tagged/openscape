package org.rsmod.content.skills.magic.spell.utility

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.events.interact.SpellOnObj
import org.rsmod.api.testing.GameTestState
import org.rsmod.game.obj.Obj
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class TelegrabTest {
    @Test
    fun GameTestState.`telekinetic grab takes a far item and the runes`() = utilityTest { deps ->
        setUp(objs.law_rune to 5, objs.air_rune to 5)
        player.teleport(START)
        player.observerUUID = player.uuid
        advance()
        val item = Obj.fromOwner(player, START.translateX(6), UtilityObjs.banana, 1)
        check(deps.objRepo.add(item, 100))

        val spell = checkNotNull(deps.spells.getObjSpell(objs.spell_tele_grab))
        eventBus.publish(SpellOnObj(player, item, spell.component))
        advance(ticks = 4)

        assertEquals(1, player.inv.count(UtilityObjs.banana))
        assertEquals(4, player.inv.count(objs.law_rune))
        assertEquals(4, player.inv.count(objs.air_rune))
        assertEquals(fineXp(43.0), gained(stats.magic))
    }

    @Test
    fun GameTestState.`telekinetic grab refuses an item out of range`() = utilityTest { deps ->
        setUp(objs.law_rune to 5, objs.air_rune to 5)
        player.teleport(START)
        player.observerUUID = player.uuid
        advance()
        val item = Obj.fromOwner(player, START.translateX(12), UtilityObjs.banana, 1)
        check(deps.objRepo.add(item, 100))

        val spell = checkNotNull(deps.spells.getObjSpell(objs.spell_tele_grab))
        eventBus.publish(SpellOnObj(player, item, spell.component))
        advance()

        assertEquals(0, player.inv.count(UtilityObjs.banana))
        assertEquals(5, player.inv.count(objs.law_rune))
    }

    private companion object {
        val START = CoordGrid(0, 50, 50, 10, 10)
    }
}
