package org.rsmod.content.areas.city.ardougne

import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.varns
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.areas.city.ardougne.configs.ArdougneNpcs
import org.rsmod.content.areas.city.ardougne.configs.ArdougneObjs
import org.rsmod.content.areas.city.ardougne.configs.ArdougneVarps
import org.rsmod.content.areas.city.ardougne.npcs.ArdougneShops
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.npc.NpcType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class ArdougneTest {
    @Test
    fun GameTestState.`aemad's trade opens the adventurers' store`() =
        runGameTest(ArdougneShops::class) {
            tradeOpens(ArdougneNpcs.aemad, ArdougneObjs.vial_water, 500)
        }

    @Test
    fun GameTestState.`the silver stall sells silver ore and bars`() =
        runGameTest(ArdougneShops::class) {
            tradeOpens(ArdougneNpcs.silver_merchant, ArdougneObjs.silver_ore, 1)
        }

    @Test
    fun GameTestState.`richard's shop sells rakes`() =
        runGameTest(ArdougneShops::class) {
            tradeOpens(ArdougneNpcs.richard, ArdougneObjs.rake, 500)
        }

    @Test
    fun GameTestState.`the fight arena's barman sells a khali brew for 5 coins`() =
        runGameTest(ArdougneShops::class) {
            player.clearInv()
            player.inv[0] = InvObj(objs.coins, 7)
            val barman = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[ArdougneNpcs.khazard_barman])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc1(barman)
            talkThrough(2)

            assertEquals(1, player.count(ArdougneObjs.khali_brew))
            assertEquals(2, player.count(objs.coins))
        }

    @Test
    fun GameTestState.`a robbed baker will not trade and calls the guards`() =
        runGameTest(ArdougneShops::class) {
            player.setVarp(ArdougneVarps.baker_angry_until, 1000)
            val baker = spawnNpc(OPEN_GROUND.translateZ(2), npcTypes[ArdougneNpcs.baker_east])
            val guard = spawnNpc(OPEN_GROUND.translateZ(-1), npcTypes[ArdougneNpcs.guards.first()])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc3(baker)
            advanceUntil(max = 10) { guard.vars[varns.lastcombat] > 0 }
            assertNull(player.openedShop)
        }

    private fun GameTestScope.tradeOpens(keeper: NpcType, stocked: ObjType, count: Int) {
        val npc = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[keeper])
        player.teleport(OPEN_GROUND)
        advance()

        player.opNpc3(npc)
        advanceUntil(max = 10) { player.openedShop != null }

        assertEquals(count, checkNotNull(player.openedShop).inv.count(stocked))
    }

    private companion object {
        // The Lumbridge spawn tile: truly empty ground for the npcs.
        val OPEN_GROUND = CoordGrid(0, 50, 50, 30, 18)
    }
}
