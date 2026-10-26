package org.rsmod.content.other.deathsoffice

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.objs
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.other.deathsoffice.configs.DeathInvs
import org.rsmod.content.other.deathsoffice.configs.DeathNpcs
import org.rsmod.content.other.deathsoffice.configs.DeathOfficeComponents
import org.rsmod.content.other.deathsoffice.configs.DeathOfficeInterfaces
import org.rsmod.content.other.deathsoffice.configs.DeathOfficeVarps
import org.rsmod.content.other.deathsoffice.configs.DeathVarps
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.npc.UnpackedNpcType

private lateinit var deathType: UnpackedNpcType

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class DeathRetrievalTest {
    @Test
    fun GameTestState.`collect takes free items`() = retrievalTest {
        office()[0] = InvObj(objs.logs)
        office()[1] = InvObj(objs.bones)
        collect()
        select(slot = 0)
        player.ifButton(DeathOfficeComponents.takeAll)
        advance(ticks = 1)
        assertEquals(setOf(logs), carried())
        assertEquals(setOf(bones), objIds(DeathInvs.death_permanent))
        assertEquals(-1, player.vars[DeathOfficeVarps.selected_slot])
    }

    @Test
    fun GameTestState.`collect charges 5 percent from 100k`() = retrievalTest {
        val cost = cacheTypes.objs[objs.abyssal_whip].cost
        assertTrue(cost >= 100_000) { "the whip should not be free" }
        val fee = cost * 5 / 100
        office()[0] = InvObj(objs.abyssal_whip)
        player.setVarp(DeathVarps.death_coffer, fee + 7)
        collect()
        assertEquals(fee + 7, player.vars[DeathOfficeVarps.coffer_shown])
        select(slot = 0)
        assertEquals(fee, player.vars[DeathOfficeVarps.fee_each])
        player.ifButton(DeathOfficeComponents.take1)
        advance(ticks = 1)
        assertEquals(setOf(whip), carried())
        assertEquals(7, player.vars[DeathVarps.death_coffer])
        assertEquals(7, player.vars[DeathOfficeVarps.coffer_shown])
    }

    @Test
    fun GameTestState.`an unpaid fee keeps items with death`() = retrievalTest {
        office()[0] = InvObj(objs.abyssal_whip)
        collect()
        select(slot = 0)
        player.ifButton(DeathOfficeComponents.take1)
        advance(ticks = 1)
        assertEquals(emptySet<Int>(), carried())
        assertEquals(setOf(whip), objIds(DeathInvs.death_permanent))
    }

    @Test
    fun GameTestState.`x takes part of a stack`() = retrievalTest {
        office()[0] = InvObj(objs.coins, 1_000)
        collect()
        select(slot = 0)
        player.ifButton(DeathOfficeComponents.takeX)
        advance(ticks = 1)
        player.resumeCountDialog(300)
        advance(ticks = 1)
        assertEquals(300, player.inv.filterNotNull().sumOf { it.count })
        assertEquals(700, office()[0]?.count)
    }

    @Test
    fun GameTestState.`take-all stops when the backpack is full`() = retrievalTest {
        office()[0] = InvObj(objs.logs)
        office()[1] = InvObj(objs.bones)
        office()[2] = InvObj(objs.logs)
        repeat(26) { player.inv[it] = InvObj(objs.iron_battleaxe) }
        collect()
        player.ifButton(DeathOfficeComponents.takeEverything)
        advance(ticks = 1)
        assertEquals(setOf(bones), objIds(DeathInvs.death_permanent))
        assertEquals(2, player.inv.count { it?.id == logs })
    }

    @Test
    fun GameTestState.`saying yes to death opens the office`() = retrievalTest {
        player.opNpc1(spawnDeath())
        talkThrough(GREETING_YES)
        assertTrue(player.ui.containsModal(DeathOfficeInterfaces.retrieval))
    }

    private fun GameTestState.retrievalTest(body: GameTestScope.() -> Unit) {
        deathType = cacheTypes.npcs[DeathNpcs.death]
        deathTest(DeathRetrievalScript::class, DeathScript::class) {
            finishDeathTutorial()
            body()
        }
    }

    private fun GameTestScope.office() = inv(DeathInvs.death_permanent)

    private fun GameTestScope.spawnDeath() = spawnNpc(player.coords.translateX(1), deathType)

    private fun GameTestScope.collect() {
        player.opNpc3(spawnDeath())
        advanceUntil(max = 5) { player.ui.containsModal(DeathOfficeInterfaces.retrieval) }
    }

    private fun GameTestScope.select(slot: Int) {
        player.ifButton(DeathOfficeComponents.items, comsub = slot)
        advance(ticks = 1)
        assertEquals(slot, player.vars[DeathOfficeVarps.selected_slot])
    }

    private companion object {
        /** "Yes, have you got anything for me?" in Death's greeting menu. */
        const val GREETING_YES = 3
    }
}
