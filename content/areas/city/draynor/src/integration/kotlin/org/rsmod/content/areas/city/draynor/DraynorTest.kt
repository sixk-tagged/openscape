package org.rsmod.content.areas.city.draynor

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.invs
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.varns
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.testing.GameTestState
import org.rsmod.content.areas.city.draynor.configs.DraynorLocs
import org.rsmod.content.areas.city.draynor.configs.DraynorNpcs
import org.rsmod.content.areas.city.draynor.configs.DraynorObjs
import org.rsmod.content.areas.city.draynor.configs.DraynorVarps
import org.rsmod.content.areas.city.draynor.locs.DraynorLocScript
import org.rsmod.content.areas.city.draynor.npcs.DraynorServices
import org.rsmod.content.areas.city.draynor.npcs.DraynorShops
import org.rsmod.game.inv.InvObj
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.loc.LocAngle
import org.rsmod.game.loc.LocShape
import org.rsmod.game.type.obj.Wearpos
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class DraynorTest {
    @Test
    fun GameTestState.`diango trade opens the toy store`() =
        runGameTest(DraynorShops::class) {
            val diango = spawnNpc(CoordGrid(3081, 3247), npcTypes[DraynorNpcs.diango])
            player.teleport(CoordGrid(3083, 3247))
            advance()

            player.opNpc3(diango)
            advanceUntil(max = 10) { player.openedShop != null }

            val shop = checkNotNull(player.openedShop)
            assertEquals(5, shop.inv.count(DraynorObjs.horsey_brown))
            assertEquals(250, shop.inv.count(DraynorObjs.spinning_plate))
        }

    @Test
    fun GameTestState.`spria trade opens the slayer equipment shop`() =
        runGameTest(DraynorShops::class) {
            val spria = spawnNpc(CoordGrid(3091, 3266), npcTypes[DraynorNpcs.spria])
            player.teleport(CoordGrid(3093, 3266))
            advance()

            player.opNpc4(spria)
            advanceUntil(max = 10) { player.openedShop != null }

            val shop = checkNotNull(player.openedShop)
            assertEquals(50, shop.inv.count(DraynorObjs.slayer_gem))
            assertEquals(50, shop.inv.count(DraynorObjs.reinforced_goggles))
        }

    @Test
    fun GameTestState.`ned sells a rope for 15 coins`() =
        runGameTest(DraynorServices::class) {
            player.clearInv()
            player.inv[0] = InvObj(objs.coins, 20)
            val ned = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[DraynorNpcs.ned])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc1(ned)
            talkThrough(1, 1)

            assertEquals(1, player.count(DraynorObjs.rope))
            assertEquals(5, player.count(objs.coins))
        }

    @Test
    fun GameTestState.`jalarast's no thanks ends the chat instead of opening more`() =
        runGameTest(DraynorServices::class) {
            val jalarast = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[DraynorNpcs.jalarast])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc3(jalarast)
            // A second menu here would need a second option and fail.
            talkThrough(5)
        }

    @Test
    fun GameTestState.`aggie makes red dye from berries and coins`() =
        runGameTest(DraynorServices::class) {
            player.clearInv()
            repeat(3) { player.inv[it] = InvObj(DraynorObjs.redberries) }
            player.inv[3] = InvObj(objs.coins, 5)
            val aggie = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[DraynorNpcs.aggie])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc3(aggie)
            talkThrough(1)

            assertEquals(1, player.count(DraynorObjs.red_dye))
            assertEquals(0, player.count(DraynorObjs.redberries))
            assertEquals(0, player.count(objs.coins))
        }

    @Test
    fun GameTestState.`opening the sewer trapdoor swaps in the open one`() =
        runGameTest(DraynorLocScript::class) {
            val trapdoor = placeMapLoc(TRAPDOOR, DraynorLocs.sewer_trapdoor)
            player.teleport(TRAPDOOR.translateX(1))
            advance()

            player.opLoc1(trapdoor)
            advanceUntil(max = 10) { locExists(TRAPDOOR, DraynorLocs.sewer_trapdoor_open) }

            assertTrue(locExists(TRAPDOOR, DraynorLocs.sewer_trapdoor_open))
        }

    @Test
    fun GameTestState.`ned's trade opens his rope shop`() =
        runGameTest(DraynorShops::class) {
            val ned = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[DraynorNpcs.ned])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc3(ned)
            advanceUntil(max = 10) { player.openedShop != null }

            assertEquals(30, checkNotNull(player.openedShop).inv.count(DraynorObjs.rope))
        }

    @Test
    fun GameTestState.`the manor's front doors let you in but not out`() =
        runGameTest(DraynorLocScript::class) {
            val door =
                placeMapLoc(FRONT_DOOR, DraynorLocs.manor_front_door_left, WALL, LocAngle.North)
            player.teleport(FRONT_DOOR)
            advance()

            player.opLoc1(door)
            advanceUntil(max = 10) { player.coords == FRONT_DOOR.translateZ(1) }
            assertEquals(FRONT_DOOR.translateZ(1), player.coords)

            player.opLoc1(door)
            advanceUntil(max = 10) {
                runCatching { assertMessageSent("The doors won't open.") }.isSuccess
            }
            assertEquals(FRONT_DOOR.translateZ(1), player.coords)
        }

    @Test
    fun GameTestState.`two players on one cabbage get one cabbage between them`() =
        runGameTest(DraynorLocScript::class) {
            advance()
            val cabbage = placeMapLoc(OPEN_GROUND.translateX(1), DraynorLocs.manor_cabbage)
            player.clearInv()
            player.teleport(OPEN_GROUND)
            val other = registerPlayer(OPEN_GROUND.translateX(2))
            advance()

            player.opLoc1(cabbage)
            other.opLoc1(cabbage)
            advance(ticks = 6)
            val picked =
                player.count(DraynorObjs.magic_cabbage) + other.count(DraynorObjs.magic_cabbage)
            assertEquals(1, picked)
        }

    @Test
    fun GameTestState.`bare hands get stung by nettles, gloves pick them`() =
        runGameTest(DraynorLocScript::class) {
            advance()
            player.clearInv()
            player.stats[stats.hitpoints] = 99
            player.setCurrentLevel(stats.hitpoints, 10)
            val nettles = placeMapLoc(OPEN_GROUND.translateX(1), DraynorLocs.nettles.first())
            player.teleport(OPEN_GROUND)
            advance()

            player.opLoc1(nettles)
            advanceUntil(max = 10) { player.hitpoints < 10 }
            assertTrue(player.hitpoints >= 8)
            assertEquals(0, player.count(DraynorObjs.nettles))

            player.worn[Wearpos.Hands.slot] = InvObj(objs.graceful_gloves_worn)
            player.opLoc1(nettles)
            advanceUntil(max = 10) { player.count(DraynorObjs.nettles) > 0 }
            assertEquals(1, player.count(DraynorObjs.nettles))
        }

    @Test
    fun GameTestState.`morgan's cupboard opens and hands out garlic`() =
        runGameTest(DraynorLocScript::class) {
            player.clearInv()
            val cupboard = placeMapLoc(OPEN_GROUND.translateX(1), DraynorLocs.garlic_cupboard)
            player.teleport(OPEN_GROUND)
            advance()

            player.opLoc1(cupboard)
            advanceUntil(max = 10) {
                locExists(OPEN_GROUND.translateX(1), DraynorLocs.garlic_cupboard_open)
            }
            val openType = locTypes[DraynorLocs.garlic_cupboard_open]
            val openLoc = findLoc(OPEN_GROUND.translateX(1), DraynorLocs.garlic_cupboard_open)
            val open = BoundLocInfo(checkNotNull(openLoc), openType)
            player.opLoc1(open)
            advanceUntil(max = 10) { player.count(DraynorObjs.garlic) > 0 }
            assertEquals(1, player.count(DraynorObjs.garlic))
        }

    @Test
    fun GameTestState.`the manor's crypt stairs lead down and back up`() =
        runGameTest(DraynorLocScript::class) {
            val stairs = placeMapLoc(CRYPT_STAIRS, DraynorLocs.crypt_stairs, angle = LocAngle.East)
            player.teleport(CoordGrid(3115, 3356))
            advance()

            player.opLoc1(stairs)
            advanceUntil(max = 10) { player.coords == CoordGrid(3077, 9771) }
            assertEquals(CoordGrid(3077, 9771), player.coords)
        }

    @Test
    fun GameTestState.`the wise old man won't hand out a second hood`() =
        runGameTest(DraynorServices::class) {
            player.clearInv()
            player.worn[Wearpos.Back.slot] = InvObj(DraynorObjs.qp_cape)
            player.invMap.getOrPut(cacheTypes.invs[invs.bank])[0] = InvObj(DraynorObjs.qp_hood)
            val wiseOldMan = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[DraynorNpcs.wise_old_man])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc1(wiseOldMan)
            talkThrough(1)

            assertEquals(0, player.count(DraynorObjs.qp_hood))
        }

    @Test
    fun GameTestState.`olivia turns a recent thief away and calls the guards`() =
        runGameTest(DraynorShops::class) {
            player.setVarp(DraynorVarps.olivia_angry_until, 1000)
            // North-south, clear of the locs other tests place east of OPEN_GROUND.
            val olivia = spawnNpc(OPEN_GROUND.translateZ(2), npcTypes[DraynorNpcs.olivia])
            val guard = spawnNpc(OPEN_GROUND.translateZ(-1), npcTypes[DraynorNpcs.market_guard])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc3(olivia)
            advanceUntil(max = 10) { guard.vars[varns.lastcombat] > 0 }
            assertNull(player.openedShop)
            talkThrough()

            // Once the grudge has passed she trades again.
            player.setVarp(DraynorVarps.olivia_angry_until, 0)
            player.opNpc3(olivia)
            advanceUntil(max = 11) { player.openedShop != null }
        }

    private companion object {
        // Real placements from the rev 233 map (maplocs.py).
        val TRAPDOOR = CoordGrid(3118, 3244)
        val FRONT_DOOR = CoordGrid(3108, 3353)
        val CRYPT_STAIRS = CoordGrid(3115, 3357)

        // The Lumbridge spawn tile: truly empty ground for locs placed just for a test.
        val OPEN_GROUND = CoordGrid(0, 50, 50, 30, 18)
        val WALL = LocShape.WallStraight
    }
}
