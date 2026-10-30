package org.rsmod.content.areas.city.portsarim

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.areas.city.portsarim.configs.PortSarimLocs
import org.rsmod.content.areas.city.portsarim.configs.PortSarimNpcs
import org.rsmod.content.areas.city.portsarim.configs.PortSarimObjs
import org.rsmod.content.areas.city.portsarim.configs.PortSarimVarps
import org.rsmod.content.areas.city.portsarim.locs.PortSarimLocScript
import org.rsmod.content.areas.city.portsarim.npcs.PortSarimBoats
import org.rsmod.content.areas.city.portsarim.npcs.PortSarimServices
import org.rsmod.content.areas.city.portsarim.npcs.PortSarimShops
import org.rsmod.game.inv.InvObj
import org.rsmod.game.loc.LocAngle
import org.rsmod.game.loc.LocShape
import org.rsmod.game.type.npc.NpcType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.Wearpos
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class PortSarimTest {
    @Test
    fun GameTestState.`wydin's trade opens the food store`() =
        runGameTest(PortSarimShops::class) {
            tradeOpens(PortSarimNpcs.wydin, PortSarimObjs.pot_flour, 3)
        }

    @Test
    fun GameTestState.`the rimmington general store sells pots`() =
        runGameTest(PortSarimShops::class) {
            tradeOpens(PortSarimNpcs.rimmington_keeper, PortSarimObjs.pot_empty, 5)
        }

    @Test
    fun GameTestState.`trader stan's trade opens his trading post`() =
        runGameTest(PortSarimShops::class) { tradeOpens(PortSarimNpcs.stan, PortSarimObjs.rope, 2) }

    @Test
    fun GameTestState.`zembo's bar sells his three lines`() =
        runGameTest(PortSarimShops::class) {
            tradeOpens(PortSarimNpcs.zembo, PortSarimObjs.karamja_rum, 3)
            val shop = checkNotNull(player.openedShop)
            assertEquals(3, shop.inv.count(PortSarimObjs.beer))
            assertEquals(1, shop.inv.count(PortSarimObjs.jug_wine))
        }

    @Test
    fun GameTestState.`grum's shop starts empty`() =
        runGameTest(PortSarimShops::class) {
            tradeOpens(PortSarimNpcs.grum, PortSarimObjs.gold_ring, 0)
        }

    @Test
    fun GameTestState.`a bartender sells a beer for 2 coins`() =
        runGameTest(PortSarimShops::class) {
            player.clearInv()
            player.inv[0] = InvObj(objs.coins, 5)
            val bartender = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[PortSarimNpcs.bartender])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc1(bartender)
            talkThrough(1)

            assertEquals(1, player.count(PortSarimObjs.beer))
            assertEquals(3, player.count(objs.coins))
        }

    @Test
    fun GameTestState.`the customs officer takes smuggled rum and keeps the fare`() =
        runGameTest(PortSarimBoats::class) {
            player.clearInv()
            player.inv[0] = InvObj(objs.coins, 30)
            player.inv[1] = InvObj(PortSarimObjs.karamja_rum)
            player.inv[2] = InvObj(PortSarimObjs.karamja_rum)
            val type = npcTypes[PortSarimNpcs.customs_officer]
            val officer = spawnNpc(OPEN_GROUND.translateX(2), type)
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc3(officer)
            advanceUntil(max = 20) { player.count(PortSarimObjs.karamja_rum) == 0 }

            assertEquals(0, player.count(PortSarimObjs.karamja_rum))
            assertEquals(30, player.count(objs.coins))
        }

    @Test
    fun GameTestState.`a sailor takes the 30 coin fare`() =
        runGameTest(PortSarimBoats::class) {
            player.clearInv()
            player.inv[0] = InvObj(objs.coins, 35)
            val sailor = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[PortSarimNpcs.tobias])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc3(sailor)
            advanceUntil(max = 20) { player.count(objs.coins) == 5 }

            assertEquals(5, player.count(objs.coins))
        }

    @Test
    fun GameTestState.`luthas pays 30 coins for a full banana crate`() =
        runGameTest(PortSarimServices::class) {
            player.clearInv()
            player.setVarp(PortSarimVarps.banana_job, 1)
            player.setVarp(PortSarimVarps.banana_crate, 10)
            val luthas = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[PortSarimNpcs.luthas])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc1(luthas)
            talkThrough(1)

            assertEquals(30, player.count(objs.coins))
            assertEquals(0, player.vars[PortSarimVarps.banana_crate])
        }

    @Test
    fun GameTestState.`filling the banana crate moves bananas out of the inventory`() =
        runGameTest(PortSarimLocScript::class) {
            player.clearInv()
            repeat(4) { player.inv[it] = InvObj(PortSarimObjs.banana) }
            player.setVarp(PortSarimVarps.banana_job, 1)
            player.setVarp(PortSarimVarps.banana_crate, 8)
            val crate = placeMapLoc(LOC_GROUND.translateX(1), PortSarimLocs.job_crate)
            player.teleport(LOC_GROUND)
            advance()

            player.opLoc2(crate)
            advanceUntil(max = 10) { player.vars[PortSarimVarps.banana_crate] == 10 }

            assertEquals(2, player.count(PortSarimObjs.banana))
        }

    @Test
    fun GameTestState.`picking a banana tree leaves a tree with one banana fewer`() =
        runGameTest(PortSarimLocScript::class) {
            player.clearInv()
            val tree = placeMapLoc(LOC_GROUND.translateX(1), PortSarimLocs.banana_trees.first())
            player.teleport(LOC_GROUND)
            advance()

            player.opLoc1(tree)
            advanceUntil(max = 10) { player.count(PortSarimObjs.banana) > 0 }

            assertEquals(1, player.count(PortSarimObjs.banana))
            assertTrue(locExists(LOC_GROUND.translateX(1), PortSarimLocs.banana_trees[1]))
        }

    @Test
    fun GameTestState.`wydin's storeroom door wants a white apron`() =
        runGameTest(PortSarimLocScript::class) {
            val door = placeMapLoc(LOC_GROUND, PortSarimLocs.wydin_door, WALL, LocAngle.West)
            player.clearInv()
            player.teleport(LOC_GROUND)
            advance()

            player.opLoc1(door)
            advance(ticks = 3)
            assertEquals(LOC_GROUND, player.coords)

            player.worn[Wearpos.Torso.slot] = InvObj(PortSarimObjs.white_apron)
            player.opLoc1(door)
            advanceUntil(max = 10) { player.coords == LOC_GROUND.translateX(-1) }
            assertEquals(LOC_GROUND.translateX(-1), player.coords)
        }

    @Test
    fun GameTestState.`the crafting guild door wants level 40 and a brown apron`() =
        runGameTest(PortSarimLocScript::class) {
            val door = placeMapLoc(LOC_GROUND, PortSarimLocs.guild_door, WALL, LocAngle.South)
            player.clearInv()
            player.worn[Wearpos.Torso.slot] = InvObj(PortSarimObjs.brown_apron)
            player.teleport(LOC_GROUND)
            advance()

            player.opLoc1(door)
            advance(ticks = 3)
            assertEquals(LOC_GROUND, player.coords)

            player.setBaseLevel(stats.crafting, 40)
            player.setCurrentLevel(stats.crafting, 40)
            player.worn[Wearpos.Torso.slot] = null
            player.opLoc1(door)
            advance(ticks = 3)
            assertEquals(LOC_GROUND, player.coords)

            player.worn[Wearpos.Torso.slot] = InvObj(PortSarimObjs.brown_apron)
            player.opLoc1(door)
            advanceUntil(max = 10) { player.coords == LOC_GROUND.translateZ(-1) }
            assertEquals(LOC_GROUND.translateZ(-1), player.coords)
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

        // Locs stay in the world for the whole class, so they get their own tile (east of the
        // castle) instead of the one the npcs stand on.
        val LOC_GROUND = CoordGrid(0, 50, 50, 34, 31)
        val WALL = LocShape.WallStraight
    }
}
