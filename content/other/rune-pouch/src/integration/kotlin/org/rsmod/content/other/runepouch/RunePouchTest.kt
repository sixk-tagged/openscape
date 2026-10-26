package org.rsmod.content.other.runepouch

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.objs
import org.rsmod.api.spells.runes.compact.CompactRuneRepository
import org.rsmod.api.spells.runes.compact.CompactRuneScript
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.inv.InvObj

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class RunePouchTest {
    @Test
    fun GameTestState.`using runes on the pouch puts the stack in`() = pouchTest { pouch ->
        player.inv[0] = InvObj(objs.rune_pouch)
        player.inv[1] = InvObj(objs.blood_rune, 300)
        pouch.store(player, slot = 1)
        assertEquals(null, player.inv[1])
        assertEquals(300, pouch.count(player, objs.blood_rune))
    }

    @Test
    fun GameTestState.`a rune pouch holds three kinds of rune, a divine one four`() =
        pouchTest { pouch ->
            player.inv[0] = InvObj(objs.rune_pouch)
            val runes = listOf(objs.blood_rune, objs.death_rune, objs.water_rune)
            for ((i, rune) in (runes + objs.soul_rune).withIndex()) {
                player.inv[i + 1] = InvObj(rune, 10)
                pouch.store(player, slot = i + 1)
            }
            assertEquals(10, player.inv[4]?.count)
            assertEquals(0, pouch.count(player, objs.soul_rune))
            player.inv[0] = InvObj(objs.divine_rune_pouch)
            pouch.store(player, slot = 4)
            assertEquals(10, pouch.count(player, objs.soul_rune))
        }

    @Test
    fun GameTestState.`a pouch slot holds 16,000 runes at most`() = pouchTest { pouch ->
        player.inv[0] = InvObj(objs.rune_pouch)
        player.inv[1] = InvObj(objs.blood_rune, 20_000)
        pouch.store(player, slot = 1)
        assertEquals(16_000, pouch.count(player, objs.blood_rune))
        assertEquals(4_000, player.inv[1]?.count)
    }

    @Test
    fun GameTestState.`emptying the pouch puts every rune back`() = pouchTest { pouch ->
        player.inv[0] = InvObj(objs.rune_pouch)
        player.inv[1] = InvObj(objs.blood_rune, 300)
        player.inv[2] = InvObj(objs.death_rune, 200)
        pouch.store(player, slot = 1)
        pouch.store(player, slot = 2)
        pouch.empty(player)
        assertEquals(300, player.inv.count(objs.blood_rune))
        assertEquals(200, player.inv.count(objs.death_rune))
        assertEquals(0, pouch.count(player, objs.blood_rune))
    }

    @Test
    fun GameTestState.`the pouch lists what it holds`() = pouchTest { pouch ->
        player.inv[0] = InvObj(objs.rune_pouch)
        assertEquals("Your rune pouch is empty.", pouch.describe(player))
        player.inv[1] = InvObj(objs.blood_rune, 1_500)
        pouch.store(player, slot = 1)
        assertEquals("Rune pouch: 1,500 x Blood rune.", pouch.describe(player))
    }

    private fun GameTestState.pouchTest(body: GameTestScope.(RunePouch) -> Unit) =
        runInjectedGameTest(RunePouch::class, PouchTestModule, CompactRuneScript::class) { pouch ->
            player.clearInv()
            body(pouch)
        }

    /** Its startup script fills the compact rune ids in, so every user must share one instance. */
    internal object PouchTestModule : AbstractModule() {
        override fun configure() {
            bind(CompactRuneRepository::class.java).`in`(Scopes.SINGLETON)
        }
    }

    private fun org.rsmod.game.inv.Inventory.count(obj: org.rsmod.game.type.obj.ObjType): Int =
        sumOf {
            if (it?.id == obj.id) it.count else 0
        }
}
