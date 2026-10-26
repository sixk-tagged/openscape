package org.rsmod.content.other.deathsoffice

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.player.events.interact.HeldEquipEvents
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.other.deathsoffice.configs.DeathInvs
import org.rsmod.content.other.deathsoffice.configs.DeathVarBits
import org.rsmod.content.other.deathsoffice.configs.DeathVarps
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.inv.InvType
import org.rsmod.game.type.obj.Wearpos

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class GravestoneTest {
    @Test
    fun GameTestState.`death loses all but the 3 dearest items`() = deathTest { graves ->
        carrySix()
        val deathTile = player.coords
        die()
        assertEquals(setOf(whip, platebody, mithril), carried())
        assertEquals(setOf(steel, iron, logs), graveObjs())
        assertEquals(1500, player.vars[DeathVarps.gravestone_ticks])
        assertEquals(deathTile, graves.npc(player)?.coords)
        assertEquals(1, player.vars[DeathVarBits.gravestone_visible])
    }

    @Test
    fun GameTestState.`protect item keeps a fourth`() = deathTest {
        carrySix()
        player.setVarBit(varbits.protect_item, 1)
        die()
        assertEquals(setOf(whip, platebody, mithril, steel), carried())
        assertEquals(setOf(iron, logs), graveObjs())
    }

    @Test
    fun GameTestState.`skulled keeps none`() = deathTest {
        carrySix()
        player.skullIcon = 0
        die()
        assertEquals(emptySet<Int>(), carried())
        assertEquals(setOf(whip, platebody, mithril, steel, iron, logs), graveObjs())
    }

    @Test
    fun GameTestState.`death with only kept items makes no grave`() = deathTest { graves ->
        player.inv[0] = InvObj(objs.abyssal_whip)
        player.inv[1] = InvObj(objs.logs)
        die()
        assertEquals(setOf(whip, logs), carried())
        assertEquals(0, player.vars[DeathVarps.gravestone_ticks])
        assertNull(graves.npc(player))
    }

    @Test
    fun GameTestState.`second death adds to the existing grave`() = deathTest { graves ->
        carrySix()
        val firstTile = player.coords
        die()
        player.inv[10] = InvObj(objs.bones)
        die()
        assertEquals(firstTile, graves.npc(player)?.coords)
        assertEquals(setOf(steel, iron, logs, bones), graveObjs())
        assertEquals(1500, player.vars[DeathVarps.gravestone_ticks])
    }

    @Test
    fun GameTestState.`a full grave sends the overflow to death`() = deathTest {
        // Stackables: the 28-per-unstackable trim leaves them be.
        val grave = inv(DeathInvs.gravestone)
        for (slot in grave.indices) {
            grave[slot] = InvObj(objs.air_rune)
        }
        player.setVarp(DeathVarps.gravestone_ticks, 1500)
        player.setVarp(DeathVarps.gravestone_coord, player.coords.packed)
        carrySix()
        die()
        assertEquals(setOf(steel, iron, logs), objIds(DeathInvs.death_permanent))
    }

    @Test
    fun GameTestState.`a second death first sends unstackables past 28 to death`() = deathTest {
        // The wiki's example: 35 logs in the grave, 2 more die in, the grave ends with 30.
        finishDeathTutorial()
        player.skullIcon = 0
        player.inv[0] = InvObj(objs.logs)
        die()
        val grave = inv(DeathInvs.gravestone)
        grave.indices.forEach { grave[it] = null }
        repeat(35) { grave[it] = InvObj(objs.logs) }
        player.skullIcon = 0
        player.inv[0] = InvObj(objs.logs)
        player.inv[1] = InvObj(objs.logs)
        die()
        assertEquals(30, units(DeathInvs.gravestone, logs))
        assertEquals(7, units(DeathInvs.death_permanent, logs))
    }

    @Test
    fun GameTestState.`lost worn items come off properly`() = deathTest {
        finishDeathTutorial()
        val unequipped = mutableListOf<Wearpos>()
        eventBus.subscribeUnbound(HeldEquipEvents.WearposChange::class.java) {
            unequipped += wearpos
        }
        player.skullIcon = 0
        player.worn[Wearpos.Back.slot] = InvObj(objs.max_cape_worn)
        die()
        assertEquals(setOf(objs.max_cape_inv.id), graveObjs())
        assertEquals(listOf(Wearpos.Back), unequipped)
    }

    private fun GameTestScope.units(type: InvType, id: Int): Int =
        inv(type).filterNotNull().filter { it.id == id }.sumOf { it.count }
}
