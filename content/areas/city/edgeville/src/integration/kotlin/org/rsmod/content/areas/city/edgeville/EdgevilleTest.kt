package org.rsmod.content.areas.city.edgeville

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.varns
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.player.stat.stat
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.areas.city.edgeville.configs.EdgevilleLocs
import org.rsmod.content.areas.city.edgeville.configs.EdgevilleNpcs
import org.rsmod.content.areas.city.edgeville.configs.EdgevilleObjs
import org.rsmod.content.areas.city.edgeville.configs.EdgevilleVarps
import org.rsmod.content.areas.city.edgeville.locs.EdgevilleLocScript
import org.rsmod.content.areas.city.edgeville.npcs.EdgevilleShops
import org.rsmod.content.areas.city.edgeville.npcs.EdgevilleTalk
import org.rsmod.content.areas.city.edgeville.npcs.Fortress
import org.rsmod.content.areas.city.edgeville.npcs.Monastery
import org.rsmod.content.generic.locs.doors.DoubleDoorScript
import org.rsmod.game.inv.InvObj
import org.rsmod.game.loc.LocAngle
import org.rsmod.game.loc.LocShape
import org.rsmod.game.type.npc.NpcType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.Wearpos
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class EdgevilleTest {
    @Test
    fun GameTestState.`the general store's trade opens a stocked shop`() =
        runGameTest(EdgevilleShops::class) {
            tradeOpens(EdgevilleNpcs.general_keeper, objs.pot_empty, 5)
        }

    @Test
    fun GameTestState.`peksa's trade opens his helmet shop`() =
        runGameTest(EdgevilleShops::class) {
            tradeOpens(EdgevilleNpcs.peksa, EdgevilleObjs.adamant_full_helm, 1)
        }

    @Test
    fun GameTestState.`richard's trade opens the cape shop`() =
        runGameTest(EdgevilleShops::class) {
            tradeOpens(EdgevilleNpcs.richard, EdgevilleObjs.team46_cape, 100)
        }

    @Test
    fun GameTestState.`oziach's trade opens the armour shop`() =
        runGameTest(EdgevilleShops::class) {
            tradeOpens(EdgevilleNpcs.oziach, EdgevilleObjs.rune_platebody, 2)
        }

    @Test
    fun GameTestState.`oziach attaches a draconic visage for 1,250,000 coins`() =
        runGameTest(EdgevilleShops::class) {
            player.clearInv()
            player.inv[0] = InvObj(EdgevilleObjs.anti_dragon_shield)
            player.inv[1] = InvObj(EdgevilleObjs.draconic_visage)
            player.inv[2] = InvObj(objs.coins, 1_250_000)
            val oziach = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[EdgevilleNpcs.oziach])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc1(oziach)
            talkThrough(5, 1)

            assertEquals(1, player.count(EdgevilleObjs.dragonfire_shield))
            assertEquals(0, player.count(EdgevilleObjs.anti_dragon_shield))
            assertEquals(0, player.count(objs.coins))
        }

    @Test
    fun GameTestState.`the abbot lets a Prayer 31 player join the order`() =
        runGameTest(Monastery::class) {
            player.setBaseLevel(stats.prayer, 31)
            player.setCurrentLevel(stats.prayer, 31)
            val abbot = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[EdgevilleNpcs.abbot])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc1(abbot)
            talkThrough(3, 1)

            assertEquals(1, player.vars[EdgevilleVarps.monastery_joined])
        }

    @Test
    fun GameTestState.`the abbot turns a Prayer 30 player away`() =
        runGameTest(Monastery::class) {
            player.setBaseLevel(stats.prayer, 30)
            player.setCurrentLevel(stats.prayer, 30)
            val abbot = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[EdgevilleNpcs.abbot])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc1(abbot)
            talkThrough(3, 1)

            assertEquals(0, player.vars[EdgevilleVarps.monastery_joined])
        }

    @Test
    fun GameTestState.`a monk heals two hitpoints and a fifth of the level`() =
        runGameTest(Monastery::class) {
            player.stats[stats.hitpoints] = 99
            player.setCurrentLevel(stats.hitpoints, 10)
            val monk = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[EdgevilleNpcs.monk])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc1(monk)
            talkThrough(1)

            // 2 + 20% of 99 is 21.
            assertEquals(31, player.hitpoints)
        }

    @Test
    fun GameTestState.`the monastery ladder wants a member of the order`() =
        runGameTest(Monastery::class, EdgevilleLocScript::class) {
            val ladder = placeMapLoc(OPEN_GROUND.translateX(1), EdgevilleLocs.monastery_ladder)
            player.teleport(OPEN_GROUND)
            advance()

            player.opLoc1(ladder)
            talkThrough()
            assertEquals(0, player.coords.level)

            player.setVarp(EdgevilleVarps.monastery_joined, 1)
            player.opLoc1(ladder)
            advanceUntil(max = 10) { player.coords.level == 1 }
            assertEquals(1, player.coords.level)
        }

    @Test
    fun GameTestState.`the monastery altar recharges Prayer two points past the maximum`() =
        runGameTest(EdgevilleLocScript::class) {
            val altar = placeMapLoc(OPEN_GROUND.translateX(1), EdgevilleLocs.monastery_altar)
            player.setBaseLevel(stats.prayer, 10)
            player.setCurrentLevel(stats.prayer, 3)
            player.teleport(OPEN_GROUND)
            advance()

            player.opLoc1(altar)
            advanceUntil(max = 10) { player.stat(stats.prayer) == 12 }
            assertEquals(12, player.stat(stats.prayer))
        }

    @Test
    fun GameTestState.`the brass key door only opens inward for the key`() =
        runGameTest(EdgevilleLocScript::class) {
            player.clearInv()
            val doorTile = OPEN_GROUND.translateZ(1)
            val door =
                placeMapLoc(
                    doorTile,
                    EdgevilleLocs.brass_key_door,
                    LocShape.WallStraight,
                    LocAngle.South,
                )
            player.teleport(OPEN_GROUND)
            advance()

            player.opLoc1(door)
            advance(ticks = 3)
            assertEquals(OPEN_GROUND, player.coords)

            player.inv[0] = InvObj(EdgevilleObjs.brass_key)
            player.opLoc1(door)
            advanceUntil(max = 10) { player.coords == doorTile }
            assertEquals(doorTile, player.coords)
        }

    @Test
    fun GameTestState.`the fortress door wants a guard's disguise`() =
        runGameTest(EdgevilleLocScript::class, Fortress::class) {
            val door =
                placeMapLoc(
                    OPEN_GROUND,
                    EdgevilleLocs.fortress_door,
                    LocShape.WallStraight,
                    LocAngle.North,
                )
            player.teleport(OPEN_GROUND)
            advance()

            player.opLoc1(door)
            talkThrough(2)
            assertEquals(OPEN_GROUND, player.coords)

            player.worn[Wearpos.Hat.slot] = InvObj(EdgevilleObjs.bronze_med_helm)
            player.worn[Wearpos.Torso.slot] = InvObj(EdgevilleObjs.iron_chainbody)
            player.opLoc1(door)
            advanceUntil(max = 10) { player.coords == OPEN_GROUND.translateZ(1) }
            assertEquals(OPEN_GROUND.translateZ(1), player.coords)
        }

    @Test
    fun GameTestState.`each goblin village crate gives its goblin mail once`() =
        runGameTest(EdgevilleLocScript::class) {
            player.clearInv()
            val crate = placeMapLoc(OPEN_GROUND.translateX(1), EdgevilleLocs.goblin_crates[1])
            player.teleport(OPEN_GROUND)
            advance()

            player.opLoc1(crate)
            advanceUntil(max = 10) { player.count(EdgevilleObjs.goblin_mail) > 0 }
            assertEquals(1, player.count(EdgevilleObjs.goblin_mail))

            player.opLoc1(crate)
            advance(ticks = 4)
            assertEquals(1, player.count(EdgevilleObjs.goblin_mail))
        }

    @Test
    fun GameTestState.`pushing the odd looking wall lets the player into the room`() =
        runGameTest(DoubleDoorScript::class) {
            val shape = LocShape.WallStraight
            val left = placeMapLoc(WALL_LEFT, EdgevilleLocs.oddwall_left, shape, LocAngle.West)
            placeMapLoc(WALL_RIGHT, EdgevilleLocs.oddwall_right, shape, LocAngle.West)
            player.teleport(WALL_LEFT)
            advance()

            player.opLoc1(left)
            advance(ticks = 3)
            player.moveGameClick(WALL_LEFT.translateX(-1))
            advanceUntil(max = 10) { player.coords == WALL_LEFT.translateX(-1) }
        }

    @Test
    fun GameTestState.`a barbarian fights whoever talks to it`() =
        runGameTest(EdgevilleTalk::class) {
            val type = npcTypes[EdgevilleNpcs.barbarians.first()]
            val barbarian = spawnNpc(OPEN_GROUND.translateX(2), type)
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc1(barbarian)
            talkThrough()

            assertTrue(barbarian.vars[varns.lastcombat] > 0)
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
        // The Lumbridge spawn tile: truly empty ground for locs placed just for a test.
        val OPEN_GROUND = CoordGrid(0, 50, 50, 30, 18)

        // Real placements from the rev 233 map.
        val WALL_LEFT = CoordGrid(3094, 9895)
        val WALL_RIGHT = CoordGrid(3094, 9896)
    }
}
