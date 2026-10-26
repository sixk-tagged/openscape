package org.rsmod.content.other.special.weapons

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
import org.rsmod.api.player.events.interact.HeldUEvents
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.api.weapons.WeaponModule
import org.rsmod.api.weapons.WeaponRegistry
import org.rsmod.content.other.special.weapons.scripts.blowpipe.ToxicBlowpipeScripts
import org.rsmod.events.EventBus
import org.rsmod.game.entity.Player
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.obj.Wearpos
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class ToxicBlowpipeTest {
    @Test
    fun GameTestState.`blowpipe has check and unload ops`() = blowpipeTest { deps ->
        val blowpipe = BlowpipeDarts.loaded
        assertEquals("Check", deps.objTypes[blowpipe].iop[2], blowpipe.internalName)
        assertEquals("Unload", deps.objTypes[blowpipe].iop[3], blowpipe.internalName)
    }

    @Test
    fun GameTestState.`using darts on the blowpipe loads them`() = blowpipeTest { deps ->
        val dart = RUNE_DART
        player.inv[0] = InvObj(BlowpipeDarts.empty)
        player.inv[1] = InvObj(dart, 100)
        player.withProtectedAccess {
            val event =
                HeldUEvents.Type(deps.objTypes[BlowpipeDarts.empty], 0, deps.objTypes[dart], 1)
            deps.eventBus.publish(this, event)
        }
        val blowpipe = player.inv[0]
        assertEquals(BlowpipeDarts.loaded.id, blowpipe?.id)
        assertEquals(dart.id, BlowpipeDarts.dartType(blowpipe)?.id)
        assertEquals(100, BlowpipeDarts.dartCount(blowpipe))
        assertEquals(null, player.inv[1])
    }

    @Test
    fun GameTestState.`unloading returns the darts and empties the blowpipe`() = blowpipeTest {
        player.inv[0] = BlowpipeDarts.withDarts(InvObj(BlowpipeDarts.empty), RUNE_DART, 30)
        player.withProtectedAccess { opHeld4(0) }
        assertEquals(BlowpipeDarts.loaded.id, player.inv[0]?.id)
        assertEquals(0, BlowpipeDarts.dartCount(player.inv[0]))
        assertEquals(30, player.inv[1]?.count)
        assertEquals(RUNE_DART.id, player.inv[1]?.id)
    }

    @Test
    fun GameTestState.`firing spends one dart and the blowpipe stays in hand`() =
        blowpipeTest { deps ->
            val (a, b) = fighters(darts = 10)
            random.next = HIT
            random.then = 6
            a.withProtectedAccess { deps.fire(this, b, attack(a)) }
            advance(ticks = 5)
            assertEquals(99 - 6, b.hitpoints)
            val hand = a.worn[Wearpos.RightHand.slot]
            assertEquals(BlowpipeDarts.loaded.id, hand?.id)
            assertEquals(9, BlowpipeDarts.dartCount(hand))
        }

    @Test
    fun GameTestState.`the last dart leaves an empty blowpipe in hand`() = blowpipeTest { deps ->
        val (a, b) = fighters(darts = 1)
        random.next = HIT
        random.then = 6
        a.withProtectedAccess { deps.fire(this, b, attack(a)) }
        advance(ticks = 5)
        val hand = a.worn[Wearpos.RightHand.slot]
        assertEquals(BlowpipeDarts.loaded.id, hand?.id)
        assertEquals(0, BlowpipeDarts.dartCount(hand))
    }

    @Test
    fun GameTestState.`an empty blowpipe fires nothing and is not thrown away`() =
        blowpipeTest { deps ->
            val (a, b) = fighters(darts = 0)
            a.withProtectedAccess { deps.fire(this, b, attack(a)) }
            advance(ticks = 5)
            assertEquals(99, b.hitpoints)
            assertEquals(BlowpipeDarts.loaded.id, a.worn[Wearpos.RightHand.slot]?.id)
        }

    class Deps
    @Inject
    constructor(val weapons: WeaponRegistry, val objTypes: ObjTypeList, val eventBus: EventBus) {
        suspend fun fire(access: ProtectedAccess, target: Player, attack: CombatAttack.Ranged) {
            val weapon = weapons.getRanged(attack.weapon) ?: error("blowpipe is not registered")
            with(weapon) { access.run { attack(target, attack) } }
        }
    }

    private object WeaponsModule : AbstractModule() {
        override fun configure() {
            install(WeaponModule())
            install(RangedWeaponsModule())
            bind(AttackTypes::class.java).`in`(Scopes.SINGLETON)
            bind(AttackStyles::class.java).`in`(Scopes.SINGLETON)
        }
    }

    private fun GameTestState.blowpipeTest(test: GameTestScope.(Deps) -> Unit) =
        runInjectedGameTest(
            Deps::class,
            WeaponsModule,
            weaponScript(),
            ToxicBlowpipeScripts::class,
            PlayerHitScript::class,
            WeaponAttackStylesScript::class,
            WeaponAttackTypesScript::class,
            testBody = test,
        )

    private fun GameTestScope.fighters(darts: Int): Pair<Player, Player> {
        val a = player
        a.teleport(START)
        val blowpipe = BlowpipeDarts.withDarts(InvObj(BlowpipeDarts.empty), RUNE_DART, darts)
        a.worn[Wearpos.RightHand.slot] = blowpipe
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

    /** The weapons' registration script is `internal` to api/weapons, so it's found by name. */
    @Suppress("UNCHECKED_CAST")
    private fun weaponScript(): KClass<out PluginScript> =
        Class.forName("org.rsmod.api.weapons.scripts.WeaponScript").kotlin
            as KClass<out PluginScript>

    private companion object {
        const val HIT = 0

        val RUNE_DART = BlowpipeDarts.darts[5]
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
