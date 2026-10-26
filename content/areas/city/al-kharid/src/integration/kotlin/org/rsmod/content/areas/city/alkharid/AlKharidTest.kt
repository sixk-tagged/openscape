package org.rsmod.content.areas.city.alkharid

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.varns
import org.rsmod.api.hit.plugin.NpcHitScript
import org.rsmod.api.testing.GameTestState
import org.rsmod.content.areas.city.alkharid.configs.AlKharidLocs
import org.rsmod.content.areas.city.alkharid.configs.AlKharidNpcs
import org.rsmod.content.areas.city.alkharid.configs.AlKharidObjs
import org.rsmod.content.areas.city.alkharid.npcs.AlKharidServices
import org.rsmod.content.areas.city.alkharid.npcs.AlKharidShops
import org.rsmod.content.areas.city.alkharid.npcs.PalaceWarriors
import org.rsmod.content.areas.city.alkharid.npcs.ShantayPass
import org.rsmod.content.generic.locs.gate.PicketGate
import org.rsmod.game.hit.HitType
import org.rsmod.game.inv.InvObj
import org.rsmod.game.loc.LocAngle
import org.rsmod.game.loc.LocShape
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class AlKharidTest {
    @Test
    fun GameTestState.`zeke trade opens the scimitar shop`() =
        runGameTest(AlKharidShops::class) {
            val zeke = spawnNpc(CoordGrid(3288, 3190), npcTypes[AlKharidNpcs.zeke])
            player.teleport(CoordGrid(3290, 3190))
            advance()

            player.opNpc3(zeke)
            advanceUntil(max = 10) { player.openedShop != null }

            val shop = checkNotNull(player.openedShop)
            assertEquals(5, shop.inv.count(AlKharidObjs.bronze_scimitar))
            assertEquals(1, shop.inv.count(AlKharidObjs.mithril_scimitar))
        }

    @Test
    fun GameTestState.`karim sells a kebab for a coin`() =
        runGameTest(AlKharidServices::class) {
            player.clearInv()
            player.inv[0] = InvObj(objs.coins, 3)
            val karim = spawnNpc(CoordGrid(3272, 3182), npcTypes[AlKharidNpcs.karim])
            player.teleport(CoordGrid(3274, 3182))
            advance()

            player.opNpc1(karim)
            talkThrough(2)

            assertEquals(1, player.count(AlKharidObjs.kebab))
            assertEquals(2, player.count(objs.coins))
        }

    @Test
    fun GameTestState.`the toll gate opens without a toll`() =
        runGameTest(PicketGate::class) {
            player.clearInv()
            player.inv[0] = InvObj(objs.coins, 100)
            val left =
                placeMapLoc(GATE_LEFT, AlKharidLocs.gate_left, LocShape.WallStraight, LocAngle.West)
            placeMapLoc(GATE_RIGHT, AlKharidLocs.gate_right, LocShape.WallStraight, LocAngle.West)
            player.teleport(GATE_LEFT.translateX(-1))
            advance()

            player.opLoc1(left)
            advanceUntil(max = 10) {
                locExists(GATE_LEFT.translateX(-1), AlKharidLocs.gate_left_open)
            }

            assertTrue(locExists(GATE_LEFT.translateX(-1), AlKharidLocs.gate_left_open))
            assertEquals(100, player.count(objs.coins))
        }

    @Test
    fun GameTestState.`shantay pass gateway takes a pass going south only`() =
        runGameTest(ShantayPass::class) {
            player.clearInv()
            val gateway = placeMapLoc(DOORWAY, AlKharidLocs.pass_doorway)
            player.teleport(DOORWAY.translateZ(1))
            advance()

            // The doorway is 4 wide, so only the side of the gate is checked, not the column.
            player.opLoc1(gateway)
            advance(ticks = 6)
            assertEquals(NORTH_Z, player.coords.z)

            player.inv[0] = InvObj(AlKharidObjs.shantay_pass, 2)
            player.opLoc1(gateway)
            advanceUntil(max = 10) { player.coords.z == SOUTH_Z }
            assertEquals(SOUTH_Z, player.coords.z)
            assertEquals(1, player.count(AlKharidObjs.shantay_pass))

            player.opLoc1(gateway)
            advanceUntil(max = 10) { player.coords.z == NORTH_Z }
            assertEquals(NORTH_Z, player.coords.z)
            assertEquals(1, player.count(AlKharidObjs.shantay_pass))
        }

    @Test
    fun GameTestState.`the shantay cell door opens both ways`() =
        runGameTest(ShantayPass::class) {
            val door =
                placeMapLoc(
                    PRISON_DOOR,
                    AlKharidLocs.prison_door,
                    LocShape.WallStraight,
                    LocAngle.East,
                )
            player.teleport(PRISON_DOOR.translateX(1))
            advance()

            player.opLoc1(door)
            advanceUntil(max = 10) { player.coords == PRISON_DOOR }
            assertEquals(PRISON_DOOR, player.coords)

            player.opLoc1(door)
            advanceUntil(max = 10) { player.coords == PRISON_DOOR.translateX(1) }
            assertEquals(PRISON_DOOR.translateX(1), player.coords)
        }

    @Test
    fun GameTestState.`a camel spits at you`() =
        runGameTest(AlKharidServices::class) {
            val camel = spawnNpc(CoordGrid(3274, 3167), npcTypes[AlKharidNpcs.camels.first()])
            player.teleport(CoordGrid(3276, 3167))
            advance()

            random.next = 0
            player.opNpc1(camel)
            advanceUntil(max = 10) {
                runCatching { assertMessageSent("The camel spits at you.") }.isSuccess
            }
        }

    @Test
    fun GameTestState.`the gate guard points you to shantay`() =
        runGameTest(ShantayPass::class) {
            val guard = spawnNpc(CoordGrid(3303, 3118), npcTypes[AlKharidNpcs.shantay_guard_still])
            player.teleport(CoordGrid(3303, 3120))
            advance()

            player.opNpc1(guard)
            talkThrough(1)
        }

    @Test
    fun GameTestState.`hitting a palace warrior brings his brothers`() =
        runGameTest(PalaceWarriors::class, NpcHitScript::class) {
            val warrior = spawnNpc(OPEN_GROUND, npcTypes[AlKharidNpcs.al_kharid_warrior])
            val brother =
                spawnNpc(OPEN_GROUND.translateX(3), npcTypes[AlKharidNpcs.al_kharid_warrior])
            player.teleport(OPEN_GROUND.translateZ(-1))
            // A fresh npc counts as in combat for its first 8 ticks.
            advance(ticks = 10)
            assertEquals(0, brother.vars[varns.lastcombat])

            warrior.queueHit(player, delay = 1, HitType.Melee, damage = 1)
            advanceUntil(max = 5) { brother.vars[varns.lastcombat] > 0 }
        }

    private companion object {
        // The Lumbridge spawn tile: open ground for fights that need clear sight lines.
        val OPEN_GROUND = CoordGrid(0, 50, 50, 30, 18)

        // Real placements from the rev 233 map (maplocs.py).
        val GATE_LEFT = CoordGrid(3268, 3227)
        val GATE_RIGHT = CoordGrid(3268, 3228)
        val DOORWAY = CoordGrid(3302, 3116)
        val PRISON_DOOR = CoordGrid(3297, 3124)
        val NORTH_Z = DOORWAY.z + 1
        val SOUTH_Z = DOORWAY.z - 1
    }
}
