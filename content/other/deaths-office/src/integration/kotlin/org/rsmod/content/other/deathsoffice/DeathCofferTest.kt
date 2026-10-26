package org.rsmod.content.other.deathsoffice

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.objs
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.other.deathsoffice.configs.CofferComponents
import org.rsmod.content.other.deathsoffice.configs.CofferInterfaces
import org.rsmod.content.other.deathsoffice.configs.CofferVarps
import org.rsmod.content.other.deathsoffice.configs.DeathLocs
import org.rsmod.content.other.deathsoffice.configs.DeathVarps
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.comp.ComponentType
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class DeathCofferTest {
    @Test
    fun GameTestState.`sacrifice credits 105 percent`() {
        val value = cacheTypes.objs[objs.abyssal_whip].cost * 105 / 100
        cofferTest {
            player.inv[0] = InvObj(objs.abyssal_whip)
            openCoffer()
            select(slot = 0)
            assertEquals(value, player.vars[CofferVarps.value_each])
            sacrifice(CofferComponents.quantity1)
            assertEquals(emptySet<Int>(), carried())
            assertEquals(value, player.vars[DeathVarps.death_coffer])
            assertEquals(value, player.vars[CofferVarps.coffer_shown])
        }
    }

    @Test
    fun GameTestState.`all sacrifices every one carried`() {
        val value = cacheTypes.objs[objs.abyssal_whip].cost * 105 / 100
        cofferTest {
            repeat(3) { player.inv[it] = InvObj(objs.abyssal_whip) }
            player.inv[3] = InvObj(objs.logs)
            openCoffer()
            select(slot = 1)
            sacrifice(CofferComponents.quantityAll)
            assertEquals(setOf(logs), carried())
            assertEquals(value * 3, player.vars[DeathVarps.death_coffer])
        }
    }

    @Test
    fun GameTestState.`cheap or untradeable items are refused`() = cofferTest {
        player.inv[0] = InvObj(objs.logs)
        player.inv[1] = InvObj(objs.bones)
        openCoffer()
        select(slot = 0)
        sacrifice(CofferComponents.quantity1)
        select(slot = 1)
        sacrifice(CofferComponents.quantity1)
        assertEquals(setOf(logs, bones), carried())
        assertEquals(0, player.vars[DeathVarps.death_coffer])
    }

    @Test
    fun GameTestState.`a full coffer refuses more`() = cofferTest {
        player.inv[0] = InvObj(objs.abyssal_whip)
        player.setVarp(DeathVarps.death_coffer, Int.MAX_VALUE - 10)
        openCoffer()
        select(slot = 0)
        sacrifice(CofferComponents.quantity1)
        assertEquals(setOf(whip), carried())
        assertEquals(Int.MAX_VALUE - 10, player.vars[DeathVarps.death_coffer])
    }

    private fun GameTestState.cofferTest(body: GameTestScope.() -> Unit) =
        deathTest(DeathCofferScript::class) {
            finishDeathTutorial()
            body()
        }

    private fun GameTestScope.openCoffer() {
        val coffer = placeMapLoc(COFFER_TILE, DeathLocs.coffer)
        player.teleport(DeathsOfficeScript.OFFICE_ARRIVAL)
        player.opLoc1(coffer)
        advanceUntil(max = 20) { player.ui.containsModal(CofferInterfaces.main) }
    }

    private fun GameTestScope.select(slot: Int) {
        player.ifButton(CofferComponents.side_items, comsub = slot)
        advance(ticks = 1)
        assertEquals(slot, player.vars[CofferVarps.selected_slot])
    }

    private fun GameTestScope.sacrifice(quantity: ComponentType) {
        player.ifButton(quantity)
        advance(ticks = 1)
        player.ifButton(CofferComponents.confirm)
        advance(ticks = 1)
    }

    private companion object {
        // Real placement from the rev 233 map (maplocs.py), rot 0 (west).
        val COFFER_TILE = CoordGrid(3179, 5729)
    }
}
