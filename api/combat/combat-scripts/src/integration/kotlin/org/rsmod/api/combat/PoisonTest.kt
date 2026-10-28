package org.rsmod.api.combat

import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.combat.commons.poison.Poison
import org.rsmod.api.combat.commons.poison.PoisonedWeapons
import org.rsmod.api.combat.commons.poison.antipoison
import org.rsmod.api.combat.commons.poison.curePoison
import org.rsmod.api.combat.commons.poison.isPoisoned
import org.rsmod.api.combat.commons.poison.poison
import org.rsmod.api.combat.commons.poison.poisonSeverity
import org.rsmod.api.combat.commons.poison.poisonState
import org.rsmod.api.combat.scripts.PoisonScript
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.stats
import org.rsmod.api.hit.plugin.NpcHitScript
import org.rsmod.api.hit.plugin.PlayerHitScript
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.random.GameRandom
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.factory.npcTypeFactory
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.util.ParamMapBuilder
import org.rsmod.map.CoordGrid

@Execution(ExecutionMode.SAME_THREAD)
class PoisonTest {
    @Test
    fun GameTestState.`poison hits every 30 ticks and loses a point of severity each time`() =
        poisonTest { _ ->
            // Severity 6 hits for 2 and 5 hits for 1: damage is (severity + 4) / 5.
            assertTrue(player.poison(6))
            assertTrue(player.isPoisoned)

            advance(ticks = 28)
            assertEquals(99, player.hitpoints)

            advance(ticks = 3)
            assertEquals(97, player.hitpoints)
            assertEquals(5, player.poisonState)

            advance(ticks = 30)
            assertEquals(96, player.hitpoints)
            assertEquals(4, player.poisonState)
        }

    @Test
    fun GameTestState.`poison runs out after its last point of severity`() = poisonTest { _ ->
        player.poison(1)
        advance(ticks = 31)
        assertEquals(98, player.hitpoints)
        assertEquals(0, player.poisonState)

        advance(ticks = 60)
        assertEquals(98, player.hitpoints)
    }

    @Test
    fun GameTestState.`antipoison cures poison and blocks it until the immunity runs out`() =
        poisonTest { _ ->
            player.poison(10)
            player.antipoison(2)
            assertFalse(player.isPoisoned)
            assertFalse(player.poison(10))

            advance(ticks = 31)
            assertEquals(-1, player.poisonState)
            advance(ticks = 30)
            assertEquals(0, player.poisonState)
            assertEquals(99, player.hitpoints)

            assertTrue(player.poison(10))
        }

    @Test
    fun GameTestState.`curing poison stops the damage`() = poisonTest { _ ->
        player.poison(10)
        player.curePoison()
        advance(ticks = 61)
        assertEquals(99, player.hitpoints)
    }

    @Test
    fun GameTestState.`a poisoned npc takes the damage and the poisoner gets the credit`() =
        poisonTest { _ ->
            val npc = spawnNpc(START, npcTypeFactory.create { hitpoints = 30 })
            assertTrue(npc.poison(10, player))

            advance(ticks = 33)
            assertEquals(28, npc.hitpoints)
            assertEquals(9, npc.poisonSeverity)
            assertEquals(player, npc.findHero(players))
        }

    @Test
    fun GameTestState.`a poison immune npc is not poisoned`() = poisonTest { _ ->
        val npc =
            spawnNpc(
                START,
                npcTypeFactory.create {
                    paramMap =
                        ParamMapBuilder().also { it[params.poison_immunity] = 1 }.toParamMap()
                },
            )
        assertFalse(npc.poison(10, player))
        assertEquals(0, npc.poisonSeverity)
    }

    @Test
    fun GameTestState.`poisoned weapons poison a hit npc one melee hit in four`() =
        poisonTest { deps ->
            val npc = spawnNpc(START, npcTypeFactory.create { hitpoints = 30 })
            val dagger = deps.objTypes[Weapons.dragon_dagger_pp]
            assertEquals(30, deps.weapons.meleeSeverity(dagger))

            random.next = 1
            assertFalse(deps.weapons.poisonNpc(npc, player, dagger, false, 5, deps.random))
            assertEquals(0, npc.poisonSeverity)

            // A hit that did no damage never poisons.
            assertFalse(deps.weapons.poisonNpc(npc, player, dagger, false, 0, deps.random))
            assertEquals(0, npc.poisonSeverity)

            random.next = 0
            assertTrue(deps.weapons.poisonNpc(npc, player, dagger, false, 5, deps.random))
            assertEquals(30, npc.poisonSeverity)
        }

    @Test
    fun GameTestState
        .`poisoned ammo is weaker, rolls one in eight and plain weapons never poison`() =
        poisonTest { deps ->
            val npc = spawnNpc(START, npcTypeFactory.create { hitpoints = 30 })
            val arrow = deps.objTypes[Weapons.rune_arrow_p]
            assertEquals(20 - Poison.RANGED_REDUCTION, deps.weapons.rangedSeverity(arrow))

            random.next = 1
            assertFalse(deps.weapons.poisonNpc(npc, player, arrow, true, 5, deps.random))

            random.next = 0
            assertTrue(deps.weapons.poisonNpc(npc, player, arrow, true, 5, deps.random))
            assertEquals(6, npc.poisonSeverity)

            val plain = deps.objTypes[Weapons.rune_arrow]
            assertEquals(0, deps.weapons.rangedSeverity(plain))
        }

    class Deps
    @Inject
    constructor(val weapons: PoisonedWeapons, val objTypes: ObjTypeList, val random: GameRandom)

    internal object Weapons : ObjReferences() {
        val dragon_dagger_pp = find("dragon_dagger_p++")
        val rune_arrow_p = find("rune_arrow_p")
        val rune_arrow = find("rune_arrow")
    }

    private fun GameTestState.poisonTest(test: GameTestScope.(Deps) -> Unit) =
        runInjectedGameTest(
            Deps::class,
            null,
            PoisonScript::class,
            PlayerHitScript::class,
            NpcHitScript::class,
            testBody = { deps ->
                // A fresh test player's clock reads -1 until their first tick.
                advance()
                player.stats[stats.hitpoints] = 99
                allocZoneCollision(START)
                test(deps)
            },
        )

    private companion object {
        val START = CoordGrid(0, 1, 1, 0, 0)
    }
}
