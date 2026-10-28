package org.rsmod.api.combat

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.combat.commons.npc.NpcAttack
import org.rsmod.api.combat.commons.npc.NpcAttackProfile
import org.rsmod.api.combat.commons.npc.NpcAttackStyle
import org.rsmod.api.combat.commons.npc.NpcAttackTable
import org.rsmod.api.combat.commons.npc.NpcDrain
import org.rsmod.api.combat.commons.npc.queueCombatRetaliate
import org.rsmod.api.combat.commons.poison.NpcPoisonTable
import org.rsmod.api.combat.commons.poison.poisonState
import org.rsmod.api.combat.scripts.NpcRetaliateScript
import org.rsmod.api.combat.scripts.NvPCombatScript
import org.rsmod.api.combat.weapon.scripts.WeaponAttackStylesScript
import org.rsmod.api.combat.weapon.scripts.WeaponAttackTypesScript
import org.rsmod.api.combat.weapon.styles.AttackStyles
import org.rsmod.api.combat.weapon.types.AttackTypes
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.seqs
import org.rsmod.api.config.refs.spotanims
import org.rsmod.api.config.refs.stats
import org.rsmod.api.hit.plugin.PlayerHitScript
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.player.stat.stat
import org.rsmod.api.random.GameRandom
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.factory.npcTypeFactory
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.npc.NpcMode
import org.rsmod.game.type.npc.UnpackedNpcType
import org.rsmod.game.type.util.ParamMapBuilder
import org.rsmod.map.CoordGrid

@Execution(ExecutionMode.SAME_THREAD)
class NvPCombatTest {
    @Test
    fun GameTestState.`a magic npc casts from a distance and stays put until the hit lands`() =
        combatTest { deps ->
            val npc = shooter(deps, magic(maxHit = 4))
            random.next = PICK
            random.then = HIT
            random.then = 3
            advance()
            assertEquals(99, player.hitpoints)

            advanceUntil(max = 8) { player.hitpoints < 99 }
            assertEquals(96, player.hitpoints)
            assertEquals(START, npc.coords)
        }

    @Test
    fun GameTestState.`a ranged npc shoots from where it stands`() = combatTest { deps ->
        val npc = shooter(deps, ranged(maxHit = 13))
        random.next = PICK
        random.then = HIT
        random.then = 11
        advance()

        advanceUntil(max = 8) { player.hitpoints < 99 }
        assertEquals(88, player.hitpoints)
        assertEquals(START, npc.coords)
    }

    @Test
    fun GameTestState.`a magic npc that fails its accuracy roll does no damage`() =
        combatTest { deps ->
            shooter(deps, magic(maxHit = 4))
            random.next = PICK
            random.then = MISS
            advance(ticks = 6)
            assertEquals(99, player.hitpoints)
        }

    @Test
    fun GameTestState.`a curse drains a stat instead of dealing damage`() = combatTest { deps ->
        shooter(deps, magic(maxHit = 0, drain = NpcDrain(stats.attack, percent = 5)))
        random.next = PICK
        random.then = HIT
        advance(ticks = 6)
        assertEquals(99, player.hitpoints)
        assertEquals(95, player.stat(stats.attack))
    }

    @Test
    fun GameTestState.`a poisonous npc poisons when its attack lands even for 0 damage`() =
        combatTest { deps ->
            val npc = shooter(deps, magic(maxHit = 4))
            deps.poisons[npc.type] = 3
            random.next = PICK
            random.then = HIT
            random.then = 0
            advance()
            assertEquals(15, player.poisonState)
        }

    @Test
    fun GameTestState.`an npc without a profile still hits in melee`() = combatTest { deps ->
        prepare()
        val type =
            npcTypeFactory.create(id = MELEE_NPC) {
                strength = 20
                attack = 20
                hitpoints = 20
                paramMap =
                    ParamMapBuilder()
                        .also { it[params.attack_anim] = seqs.human_caststrike }
                        .toParamMap()
            }
        val npc = spawn(type, START)
        player.teleport(START.translateX(1))
        advance()
        npc.opPlayer2(player)
        random.next = HIT
        random.then = 2
        advance()
        advance()
        assertEquals(97, player.hitpoints)
    }

    @Test
    fun GameTestState.`a hit ranged npc fights back from range and a melee one closes in`() =
        combatTest { deps ->
            prepare()
            val type =
                npcTypeFactory.create(id = CASTER_NPC) {
                    attackRange = 7
                    maxRange = 100
                }
            deps.table[type] = NpcAttackProfile(7, listOf(magic(maxHit = 4)))
            val caster = spawn(type, START)
            val brawler =
                spawn(npcTypeFactory.create(id = MELEE_NPC) { maxRange = 100 }, START.translateZ(4))
            player.teleport(START.translateX(5))
            advance()

            caster.queueCombatRetaliate(player)
            brawler.queueCombatRetaliate(player)
            advance(ticks = 2)
            assertEquals(NpcMode.ApPlayer2, caster.mode)
            assertEquals(NpcMode.OpPlayer2, brawler.mode)
        }

    @Test
    fun GameTestState.`melee attacks are only picked next to the target`() = combatTest { deps ->
        val profile =
            NpcAttackProfile(
                7,
                listOf(
                    NpcAttack(NpcAttackStyle.Melee, maxHit = 13),
                    magic(maxHit = 13),
                    ranged(maxHit = 13),
                ),
            )
        repeat(100) {
            assertTrue(profile.pick(deps.random, adjacent = false).style != NpcAttackStyle.Melee)
        }
        val adjacent = List(100) { profile.pick(deps.random, adjacent = true).style }
        assertTrue(NpcAttackStyle.Melee in adjacent)
    }

    class Deps
    @Inject
    constructor(val table: NpcAttackTable, val poisons: NpcPoisonTable, val random: GameRandom)

    private object CombatModule : AbstractModule() {
        override fun configure() {
            bind(AttackTypes::class.java).`in`(Scopes.SINGLETON)
            bind(AttackStyles::class.java).`in`(Scopes.SINGLETON)
        }
    }

    private fun GameTestState.combatTest(test: GameTestScope.(Deps) -> Unit) =
        runInjectedGameTest(
            Deps::class,
            CombatModule,
            WeaponAttackStylesScript::class,
            WeaponAttackTypesScript::class,
            NvPCombatScript::class,
            NpcRetaliateScript::class,
            PlayerHitScript::class,
            testBody = test,
        )

    private fun magic(maxHit: Int, drain: NpcDrain? = null) =
        NpcAttack(
            NpcAttackStyle.Magic,
            maxHit,
            anim = seqs.human_caststrike,
            travel = spotanims.windstrike_travel,
            drain = drain,
        )

    private fun ranged(maxHit: Int) =
        NpcAttack(
            NpcAttackStyle.Ranged,
            maxHit,
            anim = seqs.human_caststrike,
            travel = spotanims.windstrike_travel,
        )

    private fun GameTestScope.spawn(type: UnpackedNpcType, coords: CoordGrid): Npc {
        for (offset in 0..2) {
            allocZoneCollision(coords.translateX(8 * offset))
        }
        return spawnNpc(coords, type)
    }

    /**
     * Gives the player full stats and lets the map clock past the first 10 ticks, which count as
     * recent combat for everyone and stop npcs from attacking.
     */
    private fun GameTestScope.prepare() {
        advance(ticks = 11)
        for (stat in COMBAT_STATS) {
            player.stats[stat] = 99
        }
    }

    /** An npc 5 tiles from the player that attacks them from `ap` range with [attack]. */
    private fun GameTestScope.shooter(deps: Deps, attack: NpcAttack): Npc {
        prepare()
        val type =
            npcTypeFactory.create(id = SHOOTER_NPC) {
                magic = 60
                ranged = 60
                hitpoints = 30
                attackRange = 7
                maxRange = 100
            }
        deps.table[type] = NpcAttackProfile(range = 7, attacks = listOf(attack))
        val npc = spawn(type, START)
        player.enableWalk()
        player.teleport(START.translateX(5))
        advance()
        npc.apPlayer2(player)
        return npc
    }

    private companion object {
        /** Rolls: the attack pick, `of(1)` always 0. Accuracy hits below its hit chance. */
        const val PICK = 0
        const val HIT = 0
        const val MISS = 9999

        // Test npc types default to id 0, which the attack table would key them all under.
        const val SHOOTER_NPC = 60001
        const val MELEE_NPC = 60002
        const val CASTER_NPC = 60003

        val START = CoordGrid(0, 1, 1, 0, 0)
        val COMBAT_STATS =
            listOf(
                stats.attack,
                stats.strength,
                stats.defence,
                stats.ranged,
                stats.magic,
                stats.prayer,
                stats.hitpoints,
            )
    }
}
