package org.rsmod.content.skills.agility

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.components
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.game.process.player.PlayerRunUpdateProcessor
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.skills.agility.configs.AgilityCurrencies
import org.rsmod.content.skills.agility.configs.AgilityInvs
import org.rsmod.content.skills.agility.configs.AgilityLocs
import org.rsmod.content.skills.agility.configs.AgilityNpcs
import org.rsmod.content.skills.agility.configs.AgilityObjs
import org.rsmod.game.inv.InvObj
import org.rsmod.game.inv.Inventory
import org.rsmod.game.inv.isType
import org.rsmod.game.loc.LocAngle
import org.rsmod.game.loc.LocShape
import org.rsmod.game.shop.Shop
import org.rsmod.game.stat.PlayerStatMap
import org.rsmod.game.type.obj.Wearpos
import org.rsmod.map.CoordGrid
import org.rsmod.routefinder.flag.CollisionFlag

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class ServicesTest {
    @Test
    fun GameTestState.`a members gate opens from both sides`() =
        runGameTest(AgilityGates::class) {
            val gate = CoordGrid(2934, 3320)
            val west = CoordGrid(2933, 3320)
            val loc = placeMapLoc(gate, AgilityLocs.member_gate_left, shape = LocShape.GroundDecor)
            player.teleport(west)
            player.opLoc1(loc)
            advanceUntil(max = 10) { player.coords == gate }
            advance(2)
            player.opLoc1(loc)
            advanceUntil(max = 10) { player.coords == west }
        }

    @Test
    fun GameTestState.`the trapdoor leads down to the Rogues Den and the passage back up`() =
        runGameTest(AgilityTravel::class) {
            val trapdoor = CoordGrid(2905, 3537)
            val den = CoordGrid(3061, 4985, 1)
            allocZoneCollision(den)
            val down =
                placeMapLoc(trapdoor, AgilityLocs.roguesden_trapdoor, shape = LocShape.GroundDecor)
            // The test map has the real walls, so stand on the locs: no side of them is walled off.
            player.teleport(trapdoor)
            player.opLoc1(down)
            reach(den, max = 10)
            advance(2)

            val passage = CoordGrid(3061, 4986, 1)
            val up =
                placeMapLoc(passage, AgilityLocs.roguesden_passage, shape = LocShape.GroundDecor)
            player.teleport(passage)
            player.opLoc1(up)
            reach(trapdoor, max = 10)
        }

    @Test
    fun GameTestState.`a full graceful set restores run energy 30 percent faster`() =
        runInjectedGameTest(PlayerRunUpdateProcessor::class) { processor ->
            // 15 + 10 / 10 energy a tick at level 10.
            player.stats[stats.agility] = 10
            player.runEnergy = 0
            processor.process(player)
            assertEquals(16, player.runEnergy)

            val set =
                mapOf(
                    Wearpos.Hat to objs.graceful_hood_worn,
                    Wearpos.Torso to objs.graceful_top_worn,
                    Wearpos.Legs to objs.graceful_legs_worn,
                    Wearpos.Hands to objs.graceful_gloves_worn,
                    Wearpos.Feet to objs.graceful_boots_worn,
                    Wearpos.Back to objs.graceful_cape_worn,
                )
            for ((slot, piece) in set) {
                player.worn[slot.slot] = InvObj(piece)
            }
            player.runEnergy = 0
            processor.process(player)
            assertEquals(20, player.runEnergy)
        }

    @Test
    fun GameTestState.`a spirit tree takes you to the one you pick`() =
        runGameTest(AgilityTravel::class) {
            val tree = CoordGrid(3184, 3509)
            val stronghold = CoordGrid(2461, 3444)
            allocZoneCollision(stronghold)
            val type = AgilityLocs.spirit_tree_small_2
            val loc = placeMapLoc(tree, type, shape = LocShape.GroundDecor)
            player.teleport(tree.translateX(-1))
            player.opLoc1(loc)
            advanceUntil(max = 30) {
                // The second option, once the menu is up.
                player.resumePauseButton(components.chatmenu_pbutton, 2)
                player.coords == stronghold
            }
        }

    @Test
    fun GameTestState.`Grace sells her shop for marks of grace`() =
        runGameTest(GraceScript::class) {
            val coords = CoordGrid(3050, 4962, 1)
            allocZoneCollision(coords)
            val grace = spawnNpc(coords, npcTypes[AgilityNpcs.grace])
            player.teleport(coords.translateX(-1))
            player.opNpc3(grace)
            advanceUntil(max = 10) { player.openedShop != null }
            assertEquals(AgilityCurrencies.mark_of_grace.id, player.openedShop?.currency?.id)
        }

    @Test
    fun GameTestState.`graceful hood costs 35 marks`() =
        runInjectedGameTest(GraceShopOperations::class) { ops ->
            val inv = Inventory.create(cacheTypes.invs[AgilityInvs.grace_shop])
            val shop = Shop(inv, AgilityCurrencies.mark_of_grace, 0.0, 0.0, 0.0)
            val slot = (0 until inv.size).first { inv[it].isType(AgilityObjs.graceful_hood) }
            player.clearInv()

            player.inv[0] = InvObj(AgilityObjs.mark_of_grace, 34)
            ops.shopBuy(player, player.inv, shop, slot, request = 1)
            assertMessageSent("You don't have enough marks of grace.")
            assertDoesNotContain(player.inv, AgilityObjs.graceful_hood)

            player.inv[0] = InvObj(AgilityObjs.mark_of_grace, 35)
            ops.shopBuy(player, player.inv, shop, slot, request = 1)
            assertContains(player.inv, AgilityObjs.graceful_hood)
            assertEquals(0, player.count(AgilityObjs.mark_of_grace))
        }

    @Test
    fun GameTestState.`the wall by the Falador bank is a shortcut`() =
        runGameTest(ShortcutScript::class) {
            val wall = CoordGrid(2935, 3355)
            val loc = placeMapLoc(wall, AgilityLocs.falador_crumbling_wall, LocShape.GroundDecor)
            player.teleport(wall.translateX(1))
            player.stats[stats.agility] = 4
            player.opLoc1(loc)
            advanceUntil(max = 10) { messageSent(SHORTCUT_MESSAGE) }
            assertEquals(wall.translateX(1), player.coords)

            player.stats[stats.agility] = 5
            val xp = player.statMap.getFineXP(stats.agility)
            player.opLoc1(loc)
            advanceUntil(max = 10) { player.coords == wall.translateX(-1) }
            advanceUntil(max = 10) { player.statMap.getFineXP(stats.agility) > xp }
            assertEquals(PlayerStatMap.toFineXP(0.5).toInt(), gained(xp))
        }

    @Test
    fun GameTestState.`the Draynor Manor stones cross the water from either bank`() =
        runGameTest(ShortcutScript::class) {
            val west = CoordGrid(3149, 3363)
            val east = CoordGrid(3154, 3363)
            val stone = CoordGrid(3150, 3363)
            val loc = placeMapLoc(stone, AgilityLocs.draynor_manor_stones, LocShape.GroundDecor)
            player.teleport(west)
            player.stats[stats.agility] = 30
            player.opLoc1(loc)
            advanceUntil(max = 10) {
                messageSent("You need an Agility level of 31 to use this shortcut.")
            }
            assertEquals(west, player.coords)

            player.stats[stats.agility] = 31
            player.opLoc1(loc)
            advanceUntil(max = 15) { player.coords == east }
        }

    @Test
    fun GameTestState.`the Yanille monkey bars cross both lines, sharing Edgeville's loc`() =
        runGameTest(ShortcutScript::class) {
            val loc =
                placeMapLoc(
                    CoordGrid(2599, 9490),
                    AgilityLocs.edgeville_bars_end_2,
                    LocShape.GroundDecor,
                )
            player.teleport(CoordGrid(2599, 9489))
            player.stats[stats.agility] = 56
            player.opLoc1(loc)
            advanceUntil(max = 10) {
                messageSent("You need an Agility level of 57 to use this shortcut.")
            }

            player.stats[stats.agility] = 57
            player.opLoc1(loc)
            advanceUntil(max = 15) { player.coords == CoordGrid(2599, 9494) }
        }

    @Test
    fun GameTestState.`the rocks out of the Gnome Stronghold need 37 Agility`() =
        runGameTest(ShortcutScript::class) {
            val loc =
                placeMapLoc(
                    CoordGrid(2486, 3515),
                    AgilityLocs.gnome_rocks_top,
                    LocShape.GroundDecor,
                )
            player.teleport(CoordGrid(2486, 3515))
            player.stats[stats.agility] = 36
            player.opLoc1(loc)
            advanceUntil(max = 10) {
                messageSent("You need an Agility level of 37 to use this shortcut.")
            }

            player.stats[stats.agility] = 37
            player.opLoc1(loc)
            advanceUntil(max = 15) { player.coords == CoordGrid(2489, 3521) }
        }

    @Test
    fun GameTestState.`every shortcut starts and ends on a tile a player can stand on`() =
        runGameTest(ShortcutScript::class) {
            for (shortcut in Shortcuts.all) {
                for (end in shortcut.links.flatMap { listOf(it.first, it.second) }) {
                    val flags = collision[end.x, end.z, end.level]
                    assertEquals(0, flags and CollisionFlag.BLOCK_WALK, "$end is blocked")
                }
            }
        }

    @Test
    fun GameTestState.`the trellis and the stones in the lava cross from either side`() =
        runGameTest(ShortcutScript::class) {
            player.stats[stats.agility] = 99
            val trellis = CoordGrid(3228, 3471)
            val crossings =
                listOf(
                    Triple(
                        placeMapLoc(
                            trellis,
                            AgilityLocs.varrock_trellis,
                            LocShape.WallStraight,
                            LocAngle.South,
                        ),
                        CoordGrid(3228, 3470),
                        trellis,
                    ),
                    Triple(
                        placeMapLoc(CoordGrid(3268, 3627), AgilityLocs.chaos_temple_stones),
                        CoordGrid(3268, 3625),
                        CoordGrid(3268, 3629),
                    ),
                    Triple(
                        placeMapLoc(CoordGrid(3092, 3879), AgilityLocs.lava_maze_stones),
                        CoordGrid(3092, 3878),
                        CoordGrid(3092, 3883),
                    ),
                    Triple(
                        placeMapLoc(
                            CoordGrid(3206, 9572),
                            AgilityLocs.swamp_cave_stones_a,
                            LocShape.GroundDecor,
                        ),
                        CoordGrid(3204, 9572),
                        CoordGrid(3208, 9572),
                    ),
                    Triple(
                        placeMapLoc(
                            CoordGrid(3221, 9554),
                            AgilityLocs.swamp_cave_stones_b,
                            LocShape.GroundDecor,
                        ),
                        CoordGrid(3221, 9556),
                        CoordGrid(3221, 9552),
                    ),
                )
            for ((loc, near, far) in crossings) {
                player.teleport(near)
                player.opLoc1(loc)
                advanceUntil(max = 15) { player.coords == far }
                advance(ticks = 6)
                player.opLoc1(loc)
                advanceUntil(max = 15) { player.coords == near }
                advance(ticks = 6)
            }
        }

    /** Advances until the player is on [dest]; if never, says what the player was doing. */
    private fun GameTestScope.reach(dest: CoordGrid, max: Int) {
        val stuck = stuck(dest, max)
        check(stuck == null) { "never reached $dest: $stuck" }
    }

    private fun GameTestScope.gained(before: Int): Int =
        player.statMap.getFineXP(stats.agility) - before

    /** Messages only last the tick they are sent in, so check while advancing. */
    private fun GameTestScope.messageSent(text: String): Boolean =
        runCatching { assertMessageSent(text) }.isSuccess

    private companion object {
        const val SHORTCUT_MESSAGE = "You need an Agility level of 5 to use this shortcut."
    }
}
