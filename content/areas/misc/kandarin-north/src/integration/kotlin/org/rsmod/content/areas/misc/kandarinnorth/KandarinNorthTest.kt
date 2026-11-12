package org.rsmod.content.areas.misc.kandarinnorth

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.events.interact.LocUEvents
import org.rsmod.api.testing.GameTestState
import org.rsmod.content.areas.misc.kandarinnorth.configs.KandarinNorthLocs
import org.rsmod.content.areas.misc.kandarinnorth.configs.KandarinNorthNpcs
import org.rsmod.content.areas.misc.kandarinnorth.configs.KandarinNorthObjs
import org.rsmod.content.areas.misc.kandarinnorth.configs.KandarinNorthVarps
import org.rsmod.content.areas.misc.kandarinnorth.locs.CoalTrucks
import org.rsmod.content.areas.misc.kandarinnorth.locs.KandarinNorthLocScript
import org.rsmod.content.areas.misc.kandarinnorth.npcs.KandarinNorthShops
import org.rsmod.game.inv.InvObj
import org.rsmod.game.loc.LocAngle
import org.rsmod.game.loc.LocShape
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class KandarinNorthTest {
    @Test
    fun GameTestState.`harry's trade opens the fishing shop`() =
        runGameTest(KandarinNorthShops::class) {
            val harry = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[KandarinNorthNpcs.harry])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc3(harry)
            advanceUntil(max = 10) { player.openedShop != null }

            val shop = checkNotNull(player.openedShop)
            assertEquals(2, shop.inv.count(KandarinNorthObjs.harpoon))
            assertEquals(5, shop.inv.count(KandarinNorthObjs.big_net))
        }

    @Test
    fun GameTestState.`the fishing guild door needs fishing 68`() =
        runGameTest(KandarinNorthLocScript::class) {
            val door =
                placeMapLoc(
                    FISHING_DOOR,
                    KandarinNorthLocs.fishing_guild_door,
                    shape = LocShape.WallStraight,
                    angle = LocAngle.South,
                )
            player.setBaseLevel(stats.fishing, 67)
            player.setCurrentLevel(stats.fishing, 67)
            player.teleport(FISHING_DOOR.translateZ(-1))
            advance()

            player.opLoc1(door)
            advance(ticks = 5)
            assertEquals(FISHING_DOOR.translateZ(-1), player.coords)

            talkThrough()
            player.setCurrentLevel(stats.fishing, 68)
            player.opLoc1(door)
            advanceUntil(max = 10) { player.coords == FISHING_DOOR }
        }

    @Test
    fun GameTestState.`the ranging guild door needs ranged 40`() =
        runGameTest(KandarinNorthLocScript::class) {
            val door = placeMapLoc(RANGING_DOOR, KandarinNorthLocs.ranging_guild_door)
            player.setBaseLevel(stats.ranged, 39)
            player.setCurrentLevel(stats.ranged, 39)
            player.teleport(RANGING_OUTSIDE)
            advance()

            player.opLoc1(door)
            advance(ticks = 5)
            assertEquals(RANGING_OUTSIDE, player.coords)

            talkThrough()
            player.setCurrentLevel(stats.ranged, 40)
            player.opLoc1(door)
            advanceUntil(max = 10) { player.coords == RANGING_INSIDE }
        }

    @Test
    fun GameTestState.`the seers village stairs climb to the first floor`() =
        runGameTest(KandarinNorthLocScript::class) {
            val stairs =
                placeMapLoc(
                    CoordGrid(2698, 3496),
                    KandarinNorthLocs.spiral_stairs,
                    angle = LocAngle.North,
                )
            player.teleport(CoordGrid(2698, 3498))
            advance()

            player.opLoc1(stairs)
            advanceUntil(max = 15) { player.coords == CoordGrid(2698, 3495, 1) }
        }

    @Test
    fun GameTestState.`the loose railing squeezes through`() =
        runGameTest(KandarinNorthLocScript::class) {
            val railing =
                placeMapLoc(
                    CoordGrid(2662, 3500),
                    KandarinNorthLocs.loose_railing,
                    shape = LocShape.WallStraight,
                    angle = LocAngle.West,
                )
            player.teleport(CoordGrid(2661, 3500))
            advance()

            player.opLoc1(railing)
            advanceUntil(max = 10) { player.coords == CoordGrid(2662, 3500) }
        }

    @Test
    fun GameTestState.`the tunnel stairs go down under the mountain`() =
        runGameTest(KandarinNorthLocScript::class) {
            val stairs = placeMapLoc(CoordGrid(2820, 3484), KandarinNorthLocs.tunnel_stairs_down)
            player.teleport(CoordGrid(2820, 3486))
            advance()

            player.opLoc1(stairs)
            advanceUntil(max = 15) { player.coords == CoordGrid(2820, 9882) }
        }

    @Test
    fun GameTestState.`coal goes into the truck and comes back out`() =
        runGameTest(CoalTrucks::class) {
            val truck = placeMapLoc(OPEN_GROUND.translateX(2), KandarinNorthLocs.coal_truck)
            player.clearInv()
            repeat(3) { player.inv[it] = InvObj(KandarinNorthObjs.coal) }
            player.teleport(OPEN_GROUND)
            advance()

            val coal = objTypes[KandarinNorthObjs.coal]
            player.withProtectedAccess {
                eventBus.publish(this, LocUEvents.Op(truck, truck, locTypes[truck], coal, 0))
            }
            assertEquals(3, player.vars[KandarinNorthVarps.coal_truck_store])
            assertEquals(0, player.inv.occupiedSpace())

            player.opLoc1(truck)
            advanceUntil(max = 10) { player.inv.occupiedSpace() == 3 }
            assertEquals(0, player.vars[KandarinNorthVarps.coal_truck_store])
        }

    private companion object {
        // Real placements from the rev 233 map.
        val FISHING_DOOR = CoordGrid(2611, 3394)
        val RANGING_DOOR = CoordGrid(2658, 3438)
        val RANGING_OUTSIDE = CoordGrid(2657, 3438)
        val RANGING_INSIDE = CoordGrid(2659, 3437)

        // The Lumbridge spawn tile: truly empty ground for NPCs and locs placed for a test.
        val OPEN_GROUND = CoordGrid(0, 50, 50, 30, 18)
    }
}
