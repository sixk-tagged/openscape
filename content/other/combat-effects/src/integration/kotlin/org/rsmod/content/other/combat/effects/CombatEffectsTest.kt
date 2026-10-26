package org.rsmod.content.other.combat.effects

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import jakarta.inject.Inject
import kotlin.reflect.KClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.combat.commons.CombatAttack
import org.rsmod.api.combat.commons.CombatStance
import org.rsmod.api.combat.commons.styles.MeleeAttackStyle
import org.rsmod.api.combat.commons.types.MeleeAttackType
import org.rsmod.api.combat.manager.PlayerAttackManager
import org.rsmod.api.combat.weapon.scripts.WeaponAttackStylesScript
import org.rsmod.api.combat.weapon.scripts.WeaponAttackTypesScript
import org.rsmod.api.combat.weapon.styles.AttackStyles
import org.rsmod.api.combat.weapon.types.AttackTypes
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.hit.plugin.PlayerHitScript
import org.rsmod.api.player.hit.queueHit
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.player.stat.stat
import org.rsmod.api.player.stat.statSub
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.api.weapons.WeaponModule
import org.rsmod.api.weapons.WeaponRegistry
import org.rsmod.api.weapons.attack
import org.rsmod.content.other.combat.effects.configs.effect_objs
import org.rsmod.content.other.combat.effects.configs.effect_varps
import org.rsmod.content.other.special.weapons.MeleeWeaponsModule
import org.rsmod.game.entity.Player
import org.rsmod.game.hit.HitType
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.obj.Wearpos
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript

@Execution(ExecutionMode.SAME_THREAD)
class CombatEffectsTest {
    @Test
    fun GameTestState.`smite drains a quarter of the damage dealt from the target's prayer`() =
        effectsTest {
            val (a, b) = fighters()
            a.setVarBit(varbits.smite, 1)
            hit(a, b, 20)
            assertEquals(99 - 5, b.stat(stats.prayer))
        }

    @Test
    fun GameTestState.`redemption heals and burns prayer when hp falls under ten percent`() =
        effectsTest {
            val (a, b) = fighters()
            b.setVarBit(varbits.redemption, 1)
            b.statSub(stats.hitpoints, 87, 0)
            hit(a, b, 3)
            assertEquals(9 + 99 / 4, b.hitpoints)
            assertEquals(0, b.stat(stats.prayer))
        }

    @Test
    fun GameTestState.`redemption stays quiet while hp is above ten percent`() = effectsTest {
        val (a, b) = fighters()
        b.setVarBit(varbits.redemption, 1)
        b.statSub(stats.hitpoints, 49, 0)
        hit(a, b, 3)
        assertEquals(47, b.hitpoints)
        assertEquals(99, b.stat(stats.prayer))
    }

    @Test
    fun GameTestState.`retribution hurts an adjacent killer`() = effectsTest {
        val (a, b) = fighters()
        b.setVarBit(varbits.retribution, 1)
        b.statSub(stats.hitpoints, 94, 0)
        random.next = 7
        hit(a, b, 10)
        assertEquals(0, b.hitpoints)
        assertEquals(99 - 7, a.hitpoints)
    }

    @Test
    fun GameTestState.`retribution only fires on the killing hit`() = effectsTest {
        val (a, b) = fighters()
        b.setVarBit(varbits.retribution, 1)
        b.statSub(stats.hitpoints, 94, 0)
        random.next = 7
        b.queueHit(a, delay = 1, HitType.Melee, 10)
        b.queueHit(a, delay = 1, HitType.Melee, 10)
        advance(ticks = 2)
        assertEquals(0, b.hitpoints)
        assertEquals(99 - 7, a.hitpoints)
    }

    @Test
    fun GameTestState.`ring of recoil reflects a tenth plus one and shatters at forty`() =
        effectsTest {
            val (a, b) = fighters()
            b.worn[Wearpos.Ring.slot] = InvObj(effect_objs.ring_of_recoil)
            hit(a, b, 25)
            assertEquals(99 - 3, a.hitpoints)
            // 39 is within one hit of the limit: the next recoil deals only what's left.
            b.setVarp(effect_varps.recoil_damage_dealt, 39)
            hit(a, b, 25)
            assertEquals(99 - 3 - 1, a.hitpoints)
            assertNull(b.worn[Wearpos.Ring.slot])
        }

    @Test
    fun GameTestState.`guthans heals the attacker by the damage dealt`() = effectsTest { deps ->
        val (a, b) = fighters()
        deps.wear(a, "guthan")
        a.statSub(stats.hitpoints, 49, 0)
        random.next = 0
        hit(a, b, 10)
        assertEquals(60, a.hitpoints)
    }

    @Test
    fun GameTestState.`torags drains a fifth of the target's run energy`() = effectsTest { deps ->
        val (a, b) = fighters()
        deps.wear(a, "torag")
        b.runEnergy = 5000
        random.next = 0
        hit(a, b, 10)
        assertTrue(b.runEnergy in 3950..4100) { "energy=${b.runEnergy}" }
    }

    @Test
    fun GameTestState.`karils drains twenty percent of agility on a ranged hit`() =
        effectsTest { deps ->
            val (a, b) = fighters()
            deps.wear(a, "karil")
            random.next = 0
            hit(a, b, 10, HitType.Ranged)
            assertEquals(99 - 19, b.stat(stats.agility))
        }

    @Test
    fun GameTestState.`ahrims drains five strength on a magic hit`() = effectsTest { deps ->
        val (a, b) = fighters()
        deps.wear(a, "ahrim")
        random.next = 0
        hit(a, b, 10, HitType.Magic)
        assertEquals(94, b.stat(stats.strength))
    }

    @Test
    fun GameTestState.`an incomplete set does nothing`() = effectsTest { deps ->
        val (a, b) = fighters()
        deps.wear(a, "torag")
        a.worn[Wearpos.Legs.slot] = null
        b.runEnergy = 5000
        random.next = 0
        hit(a, b, 10)
        assertTrue(b.runEnergy >= 5000)
    }

    @Test
    fun GameTestState.`veracs ignores protection prayers and hits one harder`() =
        effectsTest { deps ->
            val (a, b) = fighters()
            deps.wear(a, "verac")
            b.setVarBit(varbits.protect_from_melee, 1)
            random.next = 0 // The 25% proc.
            random.then = 15 // The damage roll.
            a.withProtectedAccess {
                deps.weapons.getMelee(a.worn[Wearpos.RightHand.slot])!!.attack(this, b, melee(a))
            }
            advance(ticks = 2)
            assertEquals(99 - 16, b.hitpoints)
        }

    @Test
    fun GameTestState.`dharoks hits harder the lower its wearer's hp`() = effectsTest { deps ->
        val (a, b) = fighters()
        deps.wear(a, "dharok")
        val atFull = deps.manager.calculateMeleeMaxHit(a, b, SLASH, STYLE, 1.0)
        a.statSub(stats.hitpoints, 89, 0)
        val atLow = deps.manager.calculateMeleeMaxHit(a, b, SLASH, STYLE, 1.0)
        assertTrue(atLow > atFull) { "full=$atFull low=$atLow" }
    }

    @Test
    fun GameTestState.`undegraded shop pieces complete a proc set`() = effectsTest { deps ->
        val (a, b) = fighters()
        deps.wear(a, "guthan", base = true)
        a.statSub(stats.hitpoints, 49, 0)
        random.next = 0
        hit(a, b, 10)
        assertEquals(60, a.hitpoints)
    }

    @Test
    fun GameTestState.`undegraded veracs ignores protection prayers and hits one harder`() =
        effectsTest { deps ->
            val (a, b) = fighters()
            deps.wear(a, "verac", base = true)
            b.setVarBit(varbits.protect_from_melee, 1)
            random.next = 0
            random.then = 15
            a.withProtectedAccess {
                deps.weapons.getMelee(a.worn[Wearpos.RightHand.slot])!!.attack(this, b, melee(a))
            }
            advance(ticks = 2)
            assertEquals(99 - 16, b.hitpoints)
        }

    @Test
    fun GameTestState.`undegraded dharoks hits harder the lower its wearer's hp`() =
        effectsTest { deps ->
            val (a, b) = fighters()
            deps.wear(a, "dharok", base = true)
            val atFull = deps.manager.calculateMeleeMaxHit(a, b, SLASH, STYLE, 1.0)
            a.statSub(stats.hitpoints, 89, 0)
            val atLow = deps.manager.calculateMeleeMaxHit(a, b, SLASH, STYLE, 1.0)
            assertTrue(atLow > atFull) { "full=$atFull low=$atLow" }
        }

    private fun GameTestScope.hit(
        from: Player,
        to: Player,
        damage: Int,
        type: HitType = HitType.Melee,
    ) {
        to.queueHit(from, delay = 1, type, damage)
        advance(ticks = 2)
    }

    private fun GameTestScope.fighters(): Pair<Player, Player> {
        val a = player
        a.teleport(START)
        val b = registerPlayer(START.translateX(1))
        for (p in listOf(a, b)) {
            p.observerUUID = p.uuid
            for (s in ALL_STATS) {
                p.stats[s] = 99
            }
        }
        advance()
        return a to b
    }

    private fun melee(player: Player): CombatAttack.Melee =
        CombatAttack.Melee(
            weapon = player.worn[Wearpos.RightHand.slot],
            type = SLASH,
            style = STYLE,
            stance = CombatStance.Stance1,
        )

    class Deps
    @Inject
    constructor(
        val objTypes: ObjTypeList,
        val weapons: WeaponRegistry,
        val manager: PlayerAttackManager,
    ) {
        /** Wears a full, undamaged [brother] set. */
        fun wear(player: Player, brother: String, base: Boolean = false) {
            fun obj(piece: String): InvObj {
                val name = "barrows_${brother}_${piece}" + if (base) "" else "_100"
                return InvObj(objTypes.values.first { it.internalName == name })
            }
            player.worn[Wearpos.Hat.slot] = obj("head")
            player.worn[Wearpos.Torso.slot] = obj("body")
            player.worn[Wearpos.Legs.slot] = obj("legs")
            player.worn[Wearpos.RightHand.slot] = obj("weapon")
        }
    }

    private object EffectsModule : AbstractModule() {
        override fun configure() {
            install(WeaponModule())
            install(MeleeWeaponsModule())
            bind(AttackTypes::class.java).`in`(Scopes.SINGLETON)
            bind(AttackStyles::class.java).`in`(Scopes.SINGLETON)
        }
    }

    private fun GameTestState.effectsTest(test: GameTestScope.(Deps) -> Unit) =
        runInjectedGameTest(
            Deps::class,
            EffectsModule,
            CombatEffectsScript::class,
            PlayerHitScript::class,
            WeaponAttackStylesScript::class,
            WeaponAttackTypesScript::class,
            weaponScript(),
            testBody = test,
        )

    @Suppress("UNCHECKED_CAST")
    private fun weaponScript(): KClass<out PluginScript> =
        Class.forName("org.rsmod.api.weapons.scripts.WeaponScript").kotlin
            as KClass<out PluginScript>

    private companion object {
        val SLASH = MeleeAttackType.Slash
        val STYLE = MeleeAttackStyle.Aggressive
        val START = CoordGrid(0, 50, 50, 10, 10)
        val ALL_STATS =
            listOf(
                stats.attack,
                stats.strength,
                stats.defence,
                stats.ranged,
                stats.magic,
                stats.hitpoints,
                stats.prayer,
                stats.agility,
            )
    }
}
