package org.rsmod.content.other.deathsoffice

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.objs
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.other.deathsoffice.configs.DeathInvs
import org.rsmod.content.other.deathsoffice.configs.DeathVarps
import org.rsmod.content.other.deathsoffice.configs.GraveComponents
import org.rsmod.game.inv.InvObj
import org.rsmod.game.inv.Inventory

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class GraveLootTest {
    @Test
    fun GameTestState.`take-all takes free items and the empty grave goes`() = lootTest { graves ->
        carrySix()
        die()
        openLoot(graves)
        player.ifButton(GraveComponents.freebutton)
        advance(ticks = 1)
        assertEquals(setOf(whip, platebody, mithril, steel, iron, logs), carried())
        assertNull(graves.npc(player))
        assertEquals(0, player.vars[DeathVarps.gravestone_ticks])
    }

    @Test
    fun GameTestState.`unlocking pays the fee from the coffer`() = lootTest { graves ->
        player.inv[0] = InvObj(objs.abyssal_whip)
        player.skullIcon = 0
        player.setVarp(DeathVarps.death_coffer, 5_000)
        die()
        openLoot(graves)
        player.ifButton(GraveComponents.paybutton)
        advance(ticks = 1)
        assertEquals(4_000, player.vars[DeathVarps.death_coffer])
        player.ifButton(GraveComponents.paybutton)
        advance(ticks = 1)
        assertEquals(setOf(whip), carried())
    }

    @Test
    fun GameTestState.`the fee is capped at 500k`() = lootTest { graves ->
        player.inv[0] = InvObj(objs.abyssal_whip)
        player.skullIcon = 0
        die()
        inv(DeathInvs.gravestone)[0] = InvObj(objs.abyssal_whip, 600)
        player.setVarp(DeathVarps.death_coffer, 600_000)
        openLoot(graves)
        player.ifButton(GraveComponents.paybutton)
        advance(ticks = 1)
        assertEquals(100_000, player.vars[DeathVarps.death_coffer])
    }

    @Test
    fun GameTestState.`an unpaid fee keeps items in the grave`() = lootTest { graves ->
        player.inv[0] = InvObj(objs.abyssal_whip)
        player.skullIcon = 0
        die()
        openLoot(graves)
        player.ifButton(GraveComponents.paybutton)
        advance(ticks = 1)
        assertEquals(setOf(whip), graveObjs())
        assertEquals(emptySet<Int>(), carried())
    }

    @Test
    fun GameTestState.`a full backpack leaves the rest in the grave`() = lootTest { graves ->
        carrySix()
        die()
        player.inv.fill(InvObj(objs.bones), first = 0, count = 27)
        openLoot(graves)
        player.ifButton(GraveComponents.freebutton)
        advance(ticks = 1)
        assertEquals(2, graveObjs().size)
        assertTrue(graves.npc(player) != null)
    }

    @Test
    fun GameTestState.`the grave waits while the loot screen is open`() = lootTest { graves ->
        carrySix()
        die()
        openLoot(graves)
        val before = player.vars[DeathVarps.gravestone_ticks]
        advance(ticks = 5)
        assertEquals(before, player.vars[DeathVarps.gravestone_ticks])
    }

    @Test
    fun GameTestState.`a second death keeps what was already paid`() = lootTest { graves ->
        player.inv[0] = InvObj(objs.abyssal_whip)
        player.skullIcon = 0
        player.setVarp(DeathVarps.death_coffer, 5_000)
        die()
        openLoot(graves)
        player.ifButton(GraveComponents.paybutton)
        advance(ticks = 1)
        assertEquals(4_000, player.vars[DeathVarps.death_coffer])
        player.ifClose()

        player.skullIcon = 0
        player.inv[0] = InvObj(objs.bones)
        die()
        openLoot(graves)
        player.ifButton(GraveComponents.paybutton)
        advance(ticks = 1)
        assertEquals(4_000, player.vars[DeathVarps.death_coffer])
        assertEquals(setOf(whip, bones), carried())
    }

    @Test
    fun GameTestState.`items a later death adds still owe their fee`() = lootTest { graves ->
        player.inv[0] = InvObj(objs.abyssal_whip)
        player.inv[1] = InvObj(objs.bones)
        player.skullIcon = 0
        player.setVarp(DeathVarps.death_coffer, 5_000)
        die()
        openLoot(graves)
        player.ifButton(GraveComponents.paybutton)
        advance(ticks = 1)
        // Unlocked and sorted dearest first: take only the whip, the bones keep the grave up.
        player.ifButton(GraveComponents.payitems, comsub = 0)
        advance(ticks = 1)
        assertEquals(setOf(whip), carried())
        player.ifClose()

        player.skullIcon = 0
        die()
        openLoot(graves)
        player.ifButton(GraveComponents.paybutton)
        advance(ticks = 1)
        assertEquals(3_000, player.vars[DeathVarps.death_coffer])
    }

    @Test
    fun GameTestState.`credit for items trimmed to death does not cover new ones`() =
        lootTest { graves ->
            player.inv[0] = InvObj(objs.abyssal_whip)
            player.skullIcon = 0
            die()
            val grave = inv(DeathInvs.gravestone)
            repeat(30) { grave[it] = InvObj(objs.abyssal_whip) }
            player.setVarp(DeathVarps.death_coffer, 100_000)
            openLoot(graves)
            player.ifButton(GraveComponents.paybutton)
            advance(ticks = 1)
            assertEquals(70_000, player.vars[DeathVarps.death_coffer])
            player.ifClose()

            // 30 whips are trimmed to 28 (2 go to Death), then the new one makes 29.
            player.skullIcon = 0
            player.inv[0] = InvObj(objs.abyssal_whip)
            die()
            openLoot(graves)
            player.ifButton(GraveComponents.paybutton)
            advance(ticks = 1)
            assertEquals(69_000, player.vars[DeathVarps.death_coffer])
        }

    private fun GameTestState.lootTest(body: GameTestScope.(Gravestones) -> Unit) =
        deathTest(GraveLootScript::class) {
            finishDeathTutorial()
            body(it)
        }

    private fun GameTestScope.openLoot(graves: Gravestones) {
        val grave = checkNotNull(graves.npc(player))
        player.telejump(grave.coords.translateX(1))
        player.opNpc3(grave)
        advance(ticks = 2)
    }
}

private fun Inventory.fill(obj: InvObj, first: Int, count: Int) {
    for (slot in first until first + count) {
        this[slot] = obj
    }
}
