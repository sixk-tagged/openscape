package org.rsmod.content.areas.city.falador

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.death.NpcKilled
import org.rsmod.api.testing.GameTestState
import org.rsmod.content.areas.city.falador.configs.FaladorLocs
import org.rsmod.content.areas.city.falador.configs.FaladorNpcs
import org.rsmod.content.areas.city.falador.configs.FaladorObjs
import org.rsmod.content.areas.city.falador.configs.FaladorVarps
import org.rsmod.content.areas.city.falador.locs.FaladorLocScript
import org.rsmod.content.areas.city.falador.npcs.FaladorServices
import org.rsmod.content.areas.city.falador.npcs.FaladorShops
import org.rsmod.content.areas.city.falador.npcs.WhiteKnightRankScript
import org.rsmod.content.areas.city.falador.npcs.black_knights
import org.rsmod.game.inv.InvObj
import org.rsmod.game.loc.LocAngle
import org.rsmod.map.CoordGrid
import org.rsmod.routefinder.flag.CollisionFlag

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class FaladorTest {
    @Test
    fun GameTestState.`flynn's trade opens the mace shop`() =
        runGameTest(FaladorShops::class) {
            val flynn = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[FaladorNpcs.flynn])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc3(flynn)
            advanceUntil(max = 10) { player.openedShop != null }

            val shop = checkNotNull(player.openedShop)
            assertEquals(5, shop.inv.count(FaladorObjs.bronze_mace))
            assertEquals(2, shop.inv.count(FaladorObjs.adamant_mace))
        }

    @Test
    fun GameTestState.`a new account trades with sir vyvin as a novice`() =
        runGameTest(FaladorServices::class) {
            val vyvin = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[FaladorNpcs.sir_vyvin])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc1(vyvin)
            talkThrough(1)

            val shop = checkNotNull(player.openedShop)
            assertEquals(10, shop.inv.count(FaladorObjs.white_chainbody))
            assertEquals(0, shop.inv.count(FaladorObjs.white_dagger))
        }

    @Test
    fun GameTestState.`a master white knight gets the full armoury`() =
        runGameTest(FaladorServices::class) {
            player.withProtectedAccess { vars[FaladorVarps.white_knight_score] = 1200 }
            val vyvin = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[FaladorNpcs.sir_vyvin])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc1(vyvin)
            talkThrough(1)

            val shop = checkNotNull(player.openedShop)
            assertEquals(20, shop.inv.count(FaladorObjs.white_claws))
            assertEquals(20, shop.inv.count(FaladorObjs.white_chainbody))
        }

    @Test
    fun GameTestState.`black knight kills raise and white knight kills lower the score`() =
        runGameTest(WhiteKnightRankScript::class) {
            val black = spawnNpc(OPEN_GROUND, npcTypes[black_knights.black_knight])
            val white = spawnNpc(OPEN_GROUND.translateX(1), npcTypes[FaladorNpcs.white_knight])

            eventBus.publish(NpcKilled(black, player))
            eventBus.publish(NpcKilled(black, player))
            eventBus.publish(NpcKilled(white, player))

            assertEquals(1, player.vars[FaladorVarps.white_knight_score])
        }

    @Test
    fun GameTestState.`the mining guild ladder needs mining 60`() =
        runGameTest(FaladorLocScript::class) {
            val ladder = placeMapLoc(GUILD_LADDER, FaladorLocs.guild_ladder)
            player.setBaseLevel(stats.mining, 59)
            player.setCurrentLevel(stats.mining, 59)
            player.teleport(GUILD_LADDER.translateX(-1))
            advance()

            player.opLoc1(ladder)
            advance(ticks = 5)
            assertEquals(GUILD_LADDER.translateX(-1), player.coords)

            talkThrough()
            player.setCurrentLevel(stats.mining, 60)
            player.opLoc1(ladder)
            advanceUntil(max = 10) { player.coords.z > DUNGEON_Z }
            assertEquals(GUILD_LADDER.translate(-1, DUNGEON_Z), player.coords)
        }

    @Test
    fun GameTestState.`wyson sells a woad leaf for 15 coins`() =
        runGameTest(FaladorServices::class) {
            player.clearInv()
            player.inv[0] = InvObj(objs.coins, 20)
            val wyson = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[FaladorNpcs.wyson])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc1(wyson)
            talkThrough(1, 3)

            assertEquals(1, player.count(FaladorObjs.woad_leaf))
            assertEquals(5, player.count(objs.coins))
        }

    @Test
    fun GameTestState.`belona combines both mining gloves for 40 minerals`() =
        runGameTest(FaladorServices::class) {
            player.clearInv()
            player.inv[0] = InvObj(FaladorObjs.mining_gloves)
            player.inv[1] = InvObj(FaladorObjs.superior_mining_gloves)
            player.inv[2] = InvObj(FaladorObjs.minerals, 45)
            val belona = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[FaladorNpcs.belona])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc1(belona)
            talkThrough(2)

            assertEquals(1, player.count(FaladorObjs.expert_mining_gloves))
            assertEquals(0, player.count(FaladorObjs.mining_gloves))
            assertEquals(0, player.count(FaladorObjs.superior_mining_gloves))
            assertEquals(5, player.count(FaladorObjs.minerals))
        }

    @Test
    fun GameTestState.`malignius sells magic secateurs for 40,000 coins`() =
        runGameTest(FaladorServices::class) {
            player.clearInv()
            player.inv[0] = InvObj(objs.coins, 40_000)
            val malignius = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[FaladorNpcs.malignius])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc1(malignius)
            talkThrough(1)

            assertEquals(1, player.count(FaladorObjs.magic_secateurs))
            assertEquals(0, player.count(objs.coins))
        }

    @Test
    fun GameTestState.`the crypt leads down and back up onto open tiles`() =
        runGameTest(FaladorLocScript::class) {
            assertWalkable(CRYPT_TOP)
            assertWalkable(CRYPT_BOTTOM)
            val entrance =
                placeMapLoc(CRYPT_ENTRANCE, FaladorLocs.crypt_entrance, angle = LocAngle.North)
            val exit = placeMapLoc(CRYPT_EXIT, FaladorLocs.crypt_exit, angle = LocAngle.South)
            player.teleport(CRYPT_TOP)
            advance()

            player.opLoc1(entrance)
            advanceUntil(max = 10) { player.coords.z > DUNGEON_Z }
            assertEquals(CRYPT_BOTTOM, player.coords)

            player.opLoc1(exit)
            advanceUntil(max = 10) { player.coords.z < DUNGEON_Z }
            assertEquals(CRYPT_TOP, player.coords)
        }

    /** Fails with a map of the 7x7 tiles around [tile] (`#` blocked) if it can't be walked on. */
    private fun GameTestState.assertWalkable(tile: CoordGrid) {
        fun blocked(x: Int, z: Int) = collision[x, z, tile.level] and CollisionFlag.BLOCK_WALK != 0
        assertFalse(blocked(tile.x, tile.z)) {
            (3 downTo -3).joinToString("\n", "$tile is blocked; z+3 at the top:\n") { dz ->
                (-3..3).joinToString("") { dx ->
                    if (blocked(tile.x + dx, tile.z + dz)) "#" else "."
                }
            }
        }
    }

    private companion object {
        // Real placements from the rev 233 map.
        val GUILD_LADDER = CoordGrid(3018, 3339)
        val CRYPT_ENTRANCE = CoordGrid(2965, 3330)
        val CRYPT_EXIT = CoordGrid(2957, 9736)
        val CRYPT_TOP = CoordGrid(2964, 3331)
        val CRYPT_BOTTOM = CoordGrid(2960, 9737)
        const val DUNGEON_Z = 6400

        // The Lumbridge spawn tile: truly empty ground for NPCs and locs placed for a test.
        val OPEN_GROUND = CoordGrid(0, 50, 50, 30, 18)
    }
}
