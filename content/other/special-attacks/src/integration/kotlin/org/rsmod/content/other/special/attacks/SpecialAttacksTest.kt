package org.rsmod.content.other.special.attacks

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import jakarta.inject.Inject
import kotlin.reflect.KClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.combat.commons.CombatAttack
import org.rsmod.api.combat.commons.CombatStance
import org.rsmod.api.combat.commons.styles.MeleeAttackStyle
import org.rsmod.api.combat.commons.styles.RangedAttackStyle
import org.rsmod.api.combat.commons.types.MeleeAttackType
import org.rsmod.api.combat.commons.types.RangedAttackType
import org.rsmod.api.combat.formulas.AccuracyFormulae
import org.rsmod.api.combat.weapon.scripts.WeaponAttackStylesScript
import org.rsmod.api.combat.weapon.scripts.WeaponAttackTypesScript
import org.rsmod.api.combat.weapon.styles.AttackStyles
import org.rsmod.api.combat.weapon.types.AttackTypes
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.hit.plugin.PlayerHitScript
import org.rsmod.api.player.isProtectionPrayerBlocked
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.player.stat.stat
import org.rsmod.api.specials.SpecialAttack
import org.rsmod.api.specials.SpecialAttackModule as ApiSpecialAttackModule
import org.rsmod.api.specials.SpecialAttackRegistry
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.other.special.attacks.configs.special_objs
import org.rsmod.game.entity.Player
import org.rsmod.game.interact.InteractionOp
import org.rsmod.game.interact.InteractionPlayerOp
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.Wearpos
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class SpecialAttacksTest {
    @Test
    fun GameTestState.`dragon dagger punctures twice`() = specialsTest { deps ->
        val (a, b) = fighters(special_objs.dragon_dagger)
        random.next = HIT
        random.then = 10
        random.then = HIT
        random.then = 12
        a.withProtectedAccess { deps.melee(special_objs.dragon_dagger).attack(this, b, melee(a)) }
        advance(ticks = 2)
        assertEquals(99 - 22, b.hitpoints)
    }

    @Test
    fun GameTestState.`specials land where a normal swing would miss`() = specialsTest { deps ->
        val accurate =
            listOf(
                objs.armadyl_godsword to MeleeAttackType.Slash,
                special_objs.dragon_dagger to MeleeAttackType.Slash,
                special_objs.dragon_mace to MeleeAttackType.Crush,
                objs.abyssal_whip to MeleeAttackType.Slash,
                special_objs.dragon_scimitar to MeleeAttackType.Slash,
                special_objs.vestas_longsword to MeleeAttackType.Stab,
            )
        for ((weapon, block) in accurate) {
            val (a, b) = fighters(weapon)
            val normal =
                deps.accuracy.getMeleeHitChance(
                    a,
                    b,
                    MeleeAttackType.Slash,
                    MeleeAttackStyle.Accurate,
                    block,
                    specMultiplier = 1.0,
                )
            // A roll equal to the normal hit chance misses a normal swing (`chance > roll`). Each
            // swing rolls accuracy then damage; the dragon dagger swings twice.
            repeat(if (weapon == special_objs.dragon_dagger) 2 else 1) {
                random.next = normal
                random.then = 7
            }
            a.withProtectedAccess { deps.melee(weapon).attack(this, b, melee(a)) }
            advance(ticks = 2)
            assertTrue(b.hitpoints < 99) { "${weapon.internalName} missed" }
            unregisterPlayer(b)
        }
    }

    @Test
    fun GameTestState.`abyssal whip siphons a tenth of the target's run energy`() =
        specialsTest { deps ->
            val (a, b) = fighters(objs.abyssal_whip)
            a.runEnergy = 0
            b.runEnergy = 5000
            random.next = HIT
            random.then = 5
            a.withProtectedAccess { deps.melee(objs.abyssal_whip).attack(this, b, melee(a)) }
            assertEquals(4500, b.runEnergy)
            assertEquals(500, a.runEnergy)
        }

    @Test
    fun GameTestState.`dragon scimitar severs protection prayers on a hit`() =
        specialsTest { deps ->
            val (a, b) = fighters(special_objs.dragon_scimitar)
            b.setVarBit(varbits.protect_from_melee, 1)
            b.appearance.overheadIcon = constants.overhead_protect_from_melee
            random.next = MISS
            a.withProtectedAccess {
                deps.melee(special_objs.dragon_scimitar).attack(this, b, melee(a))
            }
            assertEquals(1, b.vars[varbits.protect_from_melee])
            random.next = HIT
            random.then = 5
            a.withProtectedAccess {
                deps.melee(special_objs.dragon_scimitar).attack(this, b, melee(a))
            }
            assertEquals(0, b.vars[varbits.protect_from_melee])
            assertEquals(null, b.appearance.overheadIcon)
            assertTrue(b.isProtectionPrayerBlocked())
        }

    @Test
    fun GameTestState.`dragon claws hit four times`() = specialsTest { deps ->
        val (a, b) = fighters(objs.dragon_claws)
        random.next = HIT
        random.then = 20 // First hit; the others follow from it: 10, 5, 6.
        a.withProtectedAccess { deps.melee(objs.dragon_claws).attack(this, b, melee(a)) }
        advance(ticks = 3)
        assertEquals(99 - 41, b.hitpoints)
    }

    @Test
    fun GameTestState.`dragon battleaxe rampage drains to boost strength`() = specialsTest { deps ->
        val (a, _) = fighters(special_objs.dragon_battleaxe)
        a.withProtectedAccess { deps.instant(special_objs.dragon_battleaxe).activate(this) }
        assertEquals(90, a.stat(stats.attack))
        assertEquals(90, a.stat(stats.defence))
        assertEquals(90, a.stat(stats.ranged))
        assertEquals(90, a.stat(stats.magic))
        assertEquals(99 + 10 + 36 / 4, a.stat(stats.strength))
    }

    @Test
    fun GameTestState.`granite maul smashes your target instantly`() = specialsTest { deps ->
        val (a, b) = fighters(special_objs.granite_maul)
        a.target(b)
        random.next = HIT
        random.then = 15
        var activated = false
        a.withProtectedAccess { activated = deps.instant(special_objs.granite_maul).activate(this) }
        assertTrue(activated)
        advance(ticks = 2)
        assertEquals(99 - 15, b.hitpoints)
    }

    @Test
    fun GameTestState.`granite maul needs a target`() = specialsTest { deps ->
        val (a, b) = fighters(special_objs.granite_maul)
        var activated = true
        a.withProtectedAccess { activated = deps.instant(special_objs.granite_maul).activate(this) }
        assertFalse(activated)
        advance(ticks = 2)
        assertEquals(99, b.hitpoints)
    }

    @Test
    fun GameTestState.`granite maul ignores someone you only trade with`() = specialsTest { deps ->
        val (a, b) = fighters(special_objs.granite_maul)
        a.interaction =
            InteractionPlayerOp(InteractionOp.Op4, b, hasOpTrigger = true, hasApTrigger = false)
        var activated = true
        a.withProtectedAccess { activated = deps.instant(special_objs.granite_maul).activate(this) }
        assertFalse(activated)
        advance(ticks = 2)
        assertEquals(99, b.hitpoints)
    }

    @Test
    fun GameTestState.`granite maul needs the target straight next to you, not diagonal`() =
        specialsTest { deps ->
            val (a, b) = fighters(special_objs.granite_maul)
            a.target(b)
            b.teleport(START.translate(1, 1))
            var activated = true
            a.withProtectedAccess {
                activated = deps.instant(special_objs.granite_maul).activate(this)
            }
            assertFalse(activated)
            advance(ticks = 2)
            assertEquals(99, b.hitpoints)
        }

    @Test
    fun GameTestState.`magic shortbow snapshots two arrows`() = specialsTest { deps ->
        val (a, b) = fighters(objs.magic_shortbow)
        a.worn[Wearpos.Quiver.slot] = InvObj(objs.rune_arrow, 10)
        random.next = HIT
        random.then = 8
        random.then = HIT
        random.then = 9
        a.withProtectedAccess {
            deps.ranged(objs.magic_shortbow).attack(this, b, ranged(a, objs.magic_shortbow))
        }
        advance(ticks = 5)
        assertEquals(99 - 17, b.hitpoints)
        assertEquals(8, a.worn[Wearpos.Quiver.slot]?.count)
    }

    @Test
    fun GameTestState.`magic longbow powershot always hits`() = specialsTest { deps ->
        val (a, b) = fighters(special_objs.magic_longbow)
        a.worn[Wearpos.Quiver.slot] = InvObj(objs.rune_arrow, 10)
        random.next = 20 // Only a damage roll: no accuracy roll is made.
        a.withProtectedAccess {
            deps
                .ranged(special_objs.magic_longbow)
                .attack(this, b, ranged(a, special_objs.magic_longbow))
        }
        advance(ticks = 5)
        assertEquals(99 - 20, b.hitpoints)
    }

    class Deps
    @Inject
    constructor(val specials: SpecialAttackRegistry, val accuracy: AccuracyFormulae) {
        fun melee(weapon: ObjType): SpecialAttack.Melee =
            specials[InvObj(weapon)] as? SpecialAttack.Melee
                ?: error("No melee special for ${weapon.internalName}")

        fun ranged(weapon: ObjType): SpecialAttack.Ranged =
            specials[InvObj(weapon)] as? SpecialAttack.Ranged
                ?: error("No ranged special for ${weapon.internalName}")

        fun instant(weapon: ObjType): SpecialAttack.Instant =
            specials[InvObj(weapon)] as? SpecialAttack.Instant
                ?: error("No instant special for ${weapon.internalName}")
    }

    private object SpecialsModule : AbstractModule() {
        override fun configure() {
            install(ApiSpecialAttackModule())
            install(SpecialAttackModule())
            // Their startup scripts fill these in, so every user must share one instance.
            bind(AttackTypes::class.java).`in`(Scopes.SINGLETON)
            bind(AttackStyles::class.java).`in`(Scopes.SINGLETON)
        }
    }

    private fun GameTestState.specialsTest(test: GameTestScope.(Deps) -> Unit) =
        runInjectedGameTest(
            Deps::class,
            SpecialsModule,
            specialAttackScript(),
            PlayerHitScript::class,
            WeaponAttackStylesScript::class,
            WeaponAttackTypesScript::class,
            testBody = test,
        )

    /** Two players next to each other, with [weapon] wielded. */
    private fun GameTestScope.fighters(weapon: ObjType): Pair<Player, Player> {
        val a = player
        a.teleport(START)
        a.worn[Wearpos.RightHand.slot] = InvObj(weapon)
        val b = registerPlayer(START.translateX(1))
        for (fighter in listOf(a, b)) {
            // A real login sets this; dropped ammo needs it to know whose arrows they are.
            fighter.observerUUID = fighter.uuid
            for (stat in COMBAT_STATS) {
                fighter.stats[stat] = 99
            }
        }
        advance() // A fresh test player's clock reads -1 until their first tick.
        return a to b
    }

    /** What attacking [other] sets: the instant specials hit the current target. */
    private fun Player.target(other: Player) {
        interaction =
            InteractionPlayerOp(InteractionOp.Op2, other, hasOpTrigger = true, hasApTrigger = false)
    }

    private fun melee(player: Player): CombatAttack.Melee =
        CombatAttack.Melee(
            weapon = player.worn[Wearpos.RightHand.slot],
            type = MeleeAttackType.Slash,
            style = MeleeAttackStyle.Accurate,
            stance = CombatStance.Stance1,
        )

    private fun ranged(player: Player, weapon: ObjType): CombatAttack.Ranged =
        CombatAttack.Ranged(
            weapon = player.worn[Wearpos.RightHand.slot] ?: InvObj(weapon),
            type = RangedAttackType.Standard,
            style = RangedAttackStyle.Accurate,
        )

    /** The specials' registration script is `internal` to api/specials, so it's found by name. */
    @Suppress("UNCHECKED_CAST")
    private fun specialAttackScript(): KClass<out PluginScript> =
        Class.forName("org.rsmod.api.specials.scripts.SpecialAttackScript").kotlin
            as KClass<out PluginScript>

    private companion object {
        /** Accuracy rolls hit when `hitChance > roll`, so 0 always hits and 10,000 never does. */
        const val HIT = 0
        const val MISS = 10_000

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
