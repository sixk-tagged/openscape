package org.rsmod.content.other.consumables

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.combat.scripts.NpcRetaliateScript
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.factory.npcTypeFactory
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.other.consumables.configs.PotionVarps
import org.rsmod.content.skills.herblore.Potions
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.npc.NpcMode
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.util.ParamMapBuilder
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class GoadingTest {
    @Test
    fun GameTestState.`goading makes a passive monster in range attack on the next tick`() =
        goadingTest {
            val monster = spawnMonster(START.translateX(3))
            drink()
            advance()
            assertNotEquals(NpcMode.OpPlayer2, monster.mode)
            advanceUntil(max = 3) { monster.mode == NpcMode.OpPlayer2 }
        }

    @Test
    fun GameTestState.`goading skips far monsters and npcs without an attack option`() =
        goadingTest {
            val far = spawnMonster(START.translateX(5))
            val banker = spawnNpc(START.translateX(2), npcTypeFactory.create())
            drink()
            advance(ticks = 20)
            assertNotEquals(NpcMode.OpPlayer2, far.mode)
            assertNotEquals(NpcMode.OpPlayer2, banker.mode)
        }

    @Test
    fun GameTestState.`goading checks again every six ticks`() = goadingTest {
        drink()
        advance(ticks = 8)
        val late = spawnMonster(START.translateX(3))
        // The checks fall on ticks 1, 7, 13 after the sip; the next one is five ticks away.
        advance(ticks = 4)
        assertNotEquals(NpcMode.OpPlayer2, late.mode)
        advanceUntil(max = 4) { late.mode == NpcMode.OpPlayer2 }
    }

    @Test
    fun GameTestState.`in single-way combat goading only sets one monster on the player`() =
        goadingTest {
            val near = spawnMonster(START.translateX(2))
            val far = spawnMonster(START.translateX(4))
            drink()
            advanceUntil(max = 4) { near.mode == NpcMode.OpPlayer2 }
            assertNotEquals(NpcMode.OpPlayer2, far.mode)
        }

    @Test
    fun GameTestState.`in multi-way combat goading sets every monster in range on the player`() =
        goadingTest {
            player.setVarBit(varbits.multiway_indicator, 1)
            val monsters =
                listOf(spawnMonster(START.translateX(2)), spawnMonster(START.translateX(4)))
            drink()
            advanceUntil(max = 4) { monsters.all { it.mode == NpcMode.OpPlayer2 } }
        }

    @Test
    fun GameTestState.`goading skips monsters the player lacks the Slayer level for`() =
        goadingTest {
            val monster = spawnMonster(START.translateX(3), slayerLevel = 50)
            drink()
            advance(ticks = 8)
            assertNotEquals(NpcMode.OpPlayer2, monster.mode)

            player.stats[stats.slayer] = 50
            advanceUntil(max = 8) { monster.mode == NpcMode.OpPlayer2 }
        }

    @Test
    fun GameTestState.`goading wears off after six minutes`() = goadingTest {
        drink()
        advance(ticks = 601)
        assertEquals(0, player.vars[PotionVarps.goading_until])
        val monster = spawnMonster(START.translateX(3))
        advance(ticks = 10)
        assertNotEquals(NpcMode.OpPlayer2, monster.mode)
    }

    @Test
    fun GameTestState.`ending goading early stops the checks and says so`() = goadingTest {
        val monster = spawnMonster(START.translateX(3))
        drink()
        player.endGoading()
        assertMessageSent("Your goading potion has worn off.")
        advance(ticks = 10)
        assertNotEquals(NpcMode.OpPlayer2, monster.mode)
    }

    private fun GameTestState.goadingTest(test: GameTestScope.() -> Unit) =
        runGameTest(
            ConsumablesScript::class,
            PotionEffects::class,
            NpcRetaliateScript::class,
            testBody = {
                // The first ten ticks count as recent combat for everyone.
                advance(ticks = 11)
                player.teleport(START)
                test()
            },
        )

    private fun GameTestScope.drink() {
        player.inv[0] = InvObj(Potions.goading[0])
        player.withProtectedAccess { opHeld1(0) }
    }

    private fun GameTestScope.spawnMonster(coords: CoordGrid, slayerLevel: Int = 0): Npc {
        val type =
            npcTypeFactory.create {
                op[ATTACK_OP] = "Attack"
                if (slayerLevel > 0) {
                    paramMap =
                        ParamMapBuilder()
                            .also { it[params.slayer_levelrequire] = slayerLevel }
                            .toParamMap()
                }
            }
        return spawnNpc(coords, type)
    }

    private companion object {
        const val ATTACK_OP = 1

        // The Lumbridge spawn tile: open ground with nothing blocking line of sight.
        val START = CoordGrid(0, 50, 50, 30, 18)
    }
}
