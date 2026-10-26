package org.rsmod.content.other.special.attacks

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import jakarta.inject.Inject
import kotlin.reflect.KClass
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
import org.rsmod.api.config.refs.stats
import org.rsmod.api.hit.plugin.PlayerHitScript
import org.rsmod.api.player.bonus.BlowpipeDarts
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.specials.SpecialAttack
import org.rsmod.api.specials.SpecialAttackModule as ApiSpecialAttackModule
import org.rsmod.api.specials.SpecialAttackRegistry
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.entity.Player
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.obj.Wearpos
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class ToxicBlowpipeSpecialTest {
    @Test
    fun GameTestState.`toxic siphon hits harder and heals half the damage`() = siphonTest { deps ->
        val (a, b) = fighters(darts = 10)
        a.setCurrentLevel(stats.hitpoints, 50)
        random.next = 0
        random.then = 20
        a.withProtectedAccess { deps.siphon().attack(this, b, attack(a)) }
        advance(ticks = 5)
        assertEquals(99 - 20, b.hitpoints)
        assertEquals(60, a.hitpoints)
        val hand = a.worn[Wearpos.RightHand.slot]
        assertEquals(BlowpipeDarts.loaded.id, hand?.id)
        assertEquals(9, BlowpipeDarts.dartCount(hand))
    }

    class Deps @Inject constructor(val specials: SpecialAttackRegistry) {
        fun siphon(): SpecialAttack.Ranged =
            specials[InvObj(BlowpipeDarts.loaded)] as? SpecialAttack.Ranged
                ?: error("No ranged special for the loaded blowpipe")
    }

    private object SpecialsModule : AbstractModule() {
        override fun configure() {
            install(ApiSpecialAttackModule())
            install(SpecialAttackModule())
            bind(AttackTypes::class.java).`in`(Scopes.SINGLETON)
            bind(AttackStyles::class.java).`in`(Scopes.SINGLETON)
        }
    }

    private fun GameTestState.siphonTest(test: GameTestScope.(Deps) -> Unit) =
        runInjectedGameTest(
            Deps::class,
            SpecialsModule,
            specialAttackScript(),
            PlayerHitScript::class,
            WeaponAttackStylesScript::class,
            WeaponAttackTypesScript::class,
            testBody = test,
        )

    private fun GameTestScope.fighters(darts: Int): Pair<Player, Player> {
        val a = player
        a.teleport(START)
        a.worn[Wearpos.RightHand.slot] =
            BlowpipeDarts.withDarts(InvObj(BlowpipeDarts.empty), BlowpipeDarts.darts[5], darts)
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
            style = RangedAttackStyle.Rapid,
        )

    /** The specials' registration script is `internal` to api/specials, so it's found by name. */
    @Suppress("UNCHECKED_CAST")
    private fun specialAttackScript(): KClass<out PluginScript> =
        Class.forName("org.rsmod.api.specials.scripts.SpecialAttackScript").kotlin
            as KClass<out PluginScript>

    private companion object {
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
