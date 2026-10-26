package org.rsmod.api.combat

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.combat.commons.CombatAttack
import org.rsmod.api.combat.commons.styles.RangedAttackStyle
import org.rsmod.api.combat.commons.types.RangedAttackType
import org.rsmod.api.combat.weapon.scripts.WeaponAttackStylesScript
import org.rsmod.api.combat.weapon.scripts.WeaponAttackTypesScript
import org.rsmod.api.combat.weapon.styles.AttackStyles
import org.rsmod.api.combat.weapon.types.AttackTypes
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.player.stat.stat
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.entity.Player
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.obj.Wearpos
import org.rsmod.map.CoordGrid

@Execution(ExecutionMode.SAME_THREAD)
class BoltEffectsTest {
    private val ruby = Bolts.ruby
    private val onyx = Bolts.onyx
    private val sapphire = Bolts.sapphire
    private val topaz = Bolts.topaz

    @Test
    fun GameTestState.`ruby bolts take a tenth of your hp for a fifth of theirs`() =
        boltTest { effects ->
            val (a, b) = fighters()
            random.next = PROC
            var damage = -1
            a.withProtectedAccess { damage = with(effects) { rollDamage(b, attack(a), ruby) } }
            assertEquals(19, damage) // 20% of 99, rounded down.
            assertEquals(99 - 9, a.hitpoints)
        }

    @Test
    fun GameTestState.`onyx bolts heal a quarter of the damage dealt`() = boltTest { effects ->
        val (a, b) = fighters()
        a.setCurrentLevel(stats.hitpoints, 50)
        random.next = PROC
        random.then = HIT
        random.then = 20
        var damage = -1
        a.withProtectedAccess { damage = with(effects) { rollDamage(b, attack(a), onyx) } }
        assertEquals(20, damage)
        assertEquals(55, a.hitpoints)
    }

    @Test
    fun GameTestState.`sapphire bolts drain prayer and give half back`() = boltTest { effects ->
        val (a, b) = fighters()
        a.setCurrentLevel(stats.prayer, 50)
        random.next = PROC
        random.then = HIT
        random.then = 5
        a.withProtectedAccess { with(effects) { rollDamage(b, attack(a), sapphire) } }
        assertEquals(99 - 4, b.stat(stats.prayer)) // 99 ranged / 20.
        assertEquals(52, a.stat(stats.prayer))
    }

    @Test
    fun GameTestState.`topaz bolts drain a magic level`() = boltTest { effects ->
        val (a, b) = fighters()
        random.next = PROC
        a.withProtectedAccess { with(effects) { rollDamage(b, attack(a), topaz) } }
        assertEquals(98, b.stat(stats.magic))
    }

    @Test
    fun GameTestState.`no proc leaves the hit and both players untouched`() = boltTest { effects ->
        val (a, b) = fighters()
        random.next = NO_PROC
        random.then = HIT
        random.then = 7
        var damage = -1
        a.withProtectedAccess { damage = with(effects) { rollDamage(b, attack(a), ruby) } }
        assertEquals(7, damage)
        assertEquals(99, a.hitpoints)
    }

    @Test
    fun GameTestState.`plain bolts never proc`() = boltTest { effects ->
        val (a, b) = fighters()
        random.next = HIT
        random.then = 7
        var damage = -1
        a.withProtectedAccess {
            damage = with(effects) { rollDamage(b, attack(a), objs.runite_bolts) }
        }
        assertEquals(7, damage)
    }

    class Deps @Inject constructor(val effects: BoltEffects)

    private object BoltModule : AbstractModule() {
        override fun configure() {
            bind(AttackTypes::class.java).`in`(Scopes.SINGLETON)
            bind(AttackStyles::class.java).`in`(Scopes.SINGLETON)
        }
    }

    internal object Bolts : ObjReferences() {
        val ruby = find("xbows_crossbow_bolts_adamantite_tipped_ruby_enchanted")
        val onyx = find("xbows_crossbow_bolts_runite_tipped_onyx_enchanted")
        val sapphire = find("xbows_crossbow_bolts_mithril_tipped_sapphire_enchanted")
        val topaz = find("xbows_crossbow_bolts_steel_tipped_redtopaz_enchanted")
    }

    private fun GameTestState.boltTest(test: GameTestScope.(BoltEffects) -> Unit) =
        runInjectedGameTest(
            Deps::class,
            BoltModule,
            WeaponAttackStylesScript::class,
            WeaponAttackTypesScript::class,
            testBody = { deps -> test(deps.effects) },
        )

    private fun GameTestScope.fighters(): Pair<Player, Player> {
        val a = player
        a.teleport(START)
        a.worn[Wearpos.RightHand.slot] = InvObj(objs.dragon_hunter_crossbow)
        val b = registerPlayer(START.translateX(1))
        for (fighter in listOf(a, b)) {
            fighter.observerUUID = fighter.uuid
            for (stat in COMBAT_STATS) {
                fighter.stats[stat] = 99
            }
        }
        advance()
        return a to b
    }

    private fun attack(player: Player) =
        CombatAttack.Ranged(
            weapon = player.worn[Wearpos.RightHand.slot]!!,
            type = RangedAttackType.Standard,
            style = RangedAttackStyle.Accurate,
        )

    private companion object {
        /** Procs roll `of(1000) < chance`, so 0 always procs and 999 never does. */
        const val PROC = 0
        const val NO_PROC = 999
        const val HIT = 0

        val START = CoordGrid(0, 50, 50, 10, 10)
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
