package org.rsmod.content.skills.magic.spell.utility

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.npcs
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.walktriggers
import org.rsmod.api.player.magic.isFrozen
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.npc.NoopNpcInfo
import org.rsmod.game.entity.npc.NpcInfoProtocol
import org.rsmod.game.entity.npc.NpcMode
import org.rsmod.game.type.obj.ObjType
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class TargetedSpellTest {
    @Test
    fun GameTestState.`confuse lowers an npc attack by five percent once`() = utilityTest { deps ->
        val body = findObjType { it.internalName == "bodyrune" }.first()
        giveRunes(body to 5, objs.water_rune to 20, objs.earth_rune to 20)
        val man = target()
        man.attackLvl = 40
        man.baseAttackLvl = 40
        random.next = 0
        castAt(deps, objs.spell_confuse, man)
        assertEquals(38, man.attackLvl)
        assertEquals(fineXp(13.0), gained(stats.magic))

        // The stat is below its base now, so a second cast has nothing left to take.
        random.next = 0
        castAt(deps, objs.spell_confuse, man)
        assertEquals(38, man.attackLvl)
    }

    @Test
    fun GameTestState.`stun lowers attack by ten percent`() = utilityTest { deps ->
        giveRunes(objs.soul_rune to 5, objs.water_rune to 20, objs.earth_rune to 20)
        val man = target()
        man.attackLvl = 50
        man.baseAttackLvl = 50
        random.next = 0
        castAt(deps, objs.spell_stun, man)
        assertEquals(45, man.attackLvl)
    }

    @Test
    fun GameTestState.`bind holds an npc for eight ticks and cannot be chained`() =
        utilityTest { deps ->
            giveRunes(objs.nature_rune to 5, objs.water_rune to 20, objs.earth_rune to 20)
            val man = target()
            val cast = player.currentMapClock
            random.next = 0
            castAt(deps, objs.spell_bind, man)
            assertEquals(cast + 8, man.vars[UtilityVarns.bind_until])

            // Still bound: a second bind does not extend it, and the immunity outlasts the hold.
            advance(ticks = 2)
            random.next = 0
            castAt(deps, objs.spell_bind, man)
            assertEquals(cast + 8, man.vars[UtilityVarns.bind_until])
            assertEquals(cast + 8 + 5, man.vars[UtilityVarns.bind_immune_until])
        }

    @Test
    fun GameTestState.`a missed bind leaves the npc free`() = utilityTest { deps ->
        giveRunes(objs.nature_rune to 5, objs.water_rune to 20, objs.earth_rune to 20)
        val man = target()
        // A huge roll beats any hit chance, so the spell splashes.
        random.next = Int.MAX_VALUE
        castAt(deps, objs.spell_bind, man)
        assertEquals(0, man.vars[UtilityVarns.bind_until])
    }

    @Test
    fun GameTestState.`a bound npc stays put until the bind ends`() = utilityTest { _ ->
        giveRunes(objs.nature_rune to 5)
        val man = target()
        val start = man.coords
        man.vars[UtilityVarns.bind_until] = man.currentMapClock + 4
        man.walkTrigger(walktriggers.frozen)
        man.walk(start.translateX(3))
        advance(ticks = 3)
        assertEquals(start, man.coords)

        advance(ticks = 3)
        man.walk(start.translateX(3))
        advance(ticks = 3)
        assertNotEquals(start, man.coords)
    }

    @Test
    fun GameTestState.`bind freezes a player`() = utilityTest { deps ->
        giveRunes(objs.nature_rune to 5, objs.water_rune to 20, objs.earth_rune to 20)
        val other = registerPlayer(START.translateX(1))
        other.observerUUID = other.uuid
        random.next = 0
        castAt(deps, objs.spell_bind, other)
        assertTrue(other.isFrozen())
        advance(ticks = 9)
        assertFalse(other.isFrozen())
    }

    @Test
    fun GameTestState.`crumble undead refuses a living target and keeps the runes`() =
        utilityTest { deps ->
            giveRunes(objs.chaos_rune to 5, objs.earth_rune to 20, objs.air_rune to 20)
            val man = target()
            castAt(deps, objs.spell_crumble_undead, man)
            assertMessageSent("This spell only affects skeletons, zombies, ghosts and shades.")
            assertEquals(5, player.inv.count(objs.chaos_rune))
        }

    @Test
    fun GameTestState.`charge lasts seven minutes and then fades`() = utilityTest { deps ->
        giveRunes(objs.blood_rune to 5, objs.air_rune to 5, objs.fire_rune to 5)
        val spell = checkNotNull(deps.spells.getObjSpell(objs.spell_charge))
        player.ifButton(spell.component)
        advance()
        assertEquals(fineXp(180.0), gained(stats.magic))
        assertTrue(player.vars[UtilityVarps.charge_until] > 600)

        val ticks = advanceUntil(max = 720) { sent("Your magical charge fades away.") }
        assertTrue(ticks in 695..705) { "Faded after $ticks ticks" }
    }

    private fun GameTestScope.sent(text: String): Boolean =
        runCatching { assertMessageSent(text) }.isSuccess

    private fun GameTestScope.giveRunes(vararg runes: Pair<ObjType, Int>) {
        setUp(*runes)
        player.teleport(START)
        player.observerUUID = player.uuid
        player.stats[stats.attack] = 99
        advance()
    }

    private fun GameTestScope.target(): Npc {
        allocZoneCollision(START)
        return spawnNpc(START.translateX(1), npcTypes[npcs.man]) {
            // Npcs only move while some client can see them; tests have no clients.
            infoProtocol = SeenNpcInfo
            mode = NpcMode.None
        }
    }

    private object SeenNpcInfo : NpcInfoProtocol by NoopNpcInfo {
        override fun isActive(): Boolean = true
    }

    private companion object {
        val START = CoordGrid(0, 50, 50, 10, 10)
    }
}
