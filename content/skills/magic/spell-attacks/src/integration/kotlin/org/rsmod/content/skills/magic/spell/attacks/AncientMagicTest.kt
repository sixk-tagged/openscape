package org.rsmod.content.skills.magic.spell.attacks

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import jakarta.inject.Inject
import kotlin.reflect.KClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.combat.commons.CombatAttack
import org.rsmod.api.combat.weapon.scripts.WeaponAttackStylesScript
import org.rsmod.api.combat.weapon.scripts.WeaponAttackTypesScript
import org.rsmod.api.combat.weapon.styles.AttackStyles
import org.rsmod.api.combat.weapon.types.AttackTypes
import org.rsmod.api.config.refs.components
import org.rsmod.api.config.refs.interfaces
import org.rsmod.api.config.refs.npcs
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.hit.plugin.PlayerHitScript
import org.rsmod.api.player.hit.queueHit
import org.rsmod.api.player.magic.freeze
import org.rsmod.api.player.magic.isFrozen
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.player.stat.stat
import org.rsmod.api.spells.MagicSpellModule
import org.rsmod.api.spells.MagicSpellRegistry
import org.rsmod.api.spells.attack.SpellAttackRegistry
import org.rsmod.api.spells.runes.combo.ComboRuneScript
import org.rsmod.api.spells.runes.compact.CompactRuneScript
import org.rsmod.api.spells.runes.fake.FakeRuneScript
import org.rsmod.api.spells.runes.staves.StaffSubstituteScript
import org.rsmod.api.spells.runes.subs.RuneSubstituteScript
import org.rsmod.api.spells.runes.unlimited.UnlimitedRuneScript
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.skills.magic.spell.attacks.configs.LunarComponents
import org.rsmod.content.skills.magic.spell.attacks.lunar.MagicEffectsScript
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Player
import org.rsmod.game.hit.HitType
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.obj.ObjType
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class AncientMagicTest {
    @Test
    fun GameTestState.`ice rush freezes for 8 ticks and cannot be chained`() = magicTest { deps ->
        val (a, b) = fighters()
        random.next = 0
        random.then = 5
        deps.cast(this, a, b, objs.spell_ice_rush)
        assertTrue(b.isFrozen())
        advance(ticks = 9)
        assertFalse(b.isFrozen())
        // The 5-tick immunity after a freeze stops a second one.
        assertFalse(b.freeze(8))
        advance(ticks = 6)
        assertTrue(b.freeze(8))
    }

    @Test
    fun GameTestState.`a frozen player cannot walk`() = magicTest { _ ->
        val (_, b) = fighters()
        b.freeze(8)
        val start = b.coords
        b.moveGameClick(start.translateX(3))
        advance(ticks = 3)
        assertEquals(start, b.coords)
        advance(ticks = 8)
        b.moveGameClick(start.translateX(3))
        advance(ticks = 3)
        assertNotEquals(start, b.coords)
    }

    @Test
    fun GameTestState.`blood rush heals a quarter of the damage`() = magicTest { deps ->
        val (a, b) = fighters()
        a.setCurrentLevel(stats.hitpoints, 50)
        random.next = 0
        random.then = 12
        deps.cast(this, a, b, objs.spell_blood_rush)
        assertEquals(53, a.hitpoints)
    }

    @Test
    fun GameTestState.`blood rush heals against an npc too`() = magicTest { deps ->
        val (a, _) = fighters()
        val man = spawnNpc(START.translateZ(2), npcTypes[npcs.man])
        a.setCurrentLevel(stats.hitpoints, 50)
        random.next = 0
        random.then = 12
        deps.cast(this, a, man, objs.spell_blood_rush)
        // A man has 7 hitpoints, so the heal is capped at a quarter of those.
        assertEquals(51, a.hitpoints)
    }

    @Test
    fun GameTestState.`shadow rush drains attack once`() = magicTest { deps ->
        val (a, b) = fighters()
        random.next = 0
        random.then = 5
        deps.cast(this, a, b, objs.spell_shadow_rush)
        assertEquals(90, b.stat(stats.attack))
        advance(ticks = 5)
        random.next = 0
        random.then = 5
        deps.cast(this, a, b, objs.spell_shadow_rush)
        assertEquals(90, b.stat(stats.attack))
    }

    @Test
    fun GameTestState.`vengeance reflects three quarters of the next hit`() = magicTest { _ ->
        val (a, b) = fighters()
        b.setVarBit(varbits.spellbook, 2)
        b.ifOpenOverlay(interfaces.magic_spellbook, components.toplevel_target_side6)
        b.ifButton(LunarComponents.vengeance)
        advance()
        // A queued hit from a lands on b: b takes it all, a takes 75% back.
        b.queueHit(a, delay = 1, HitType.Magic, damage = 20)
        advance(ticks = 3)
        assertEquals(99 - 20, b.hitpoints)
        assertEquals(99 - 15, a.hitpoints)
        // Used up: the next hit is not reflected.
        b.queueHit(a, delay = 1, HitType.Magic, damage = 8)
        advance(ticks = 3)
        assertEquals(99 - 15, a.hitpoints)
    }

    @Test
    fun GameTestState.`vengeance cannot be stacked or recast during its cooldown`() =
        magicTest { _ ->
            val (a, _) = fighters()
            a.setVarBit(varbits.spellbook, 2)
            a.ifOpenOverlay(interfaces.magic_spellbook, components.toplevel_target_side6)
            a.ifButton(LunarComponents.vengeance)
            advance()
            a.ifButton(LunarComponents.vengeance)
            advance()
            assertMessageSent("You already have Vengeance cast.", a)
        }

    class Deps
    @Inject
    constructor(val spells: MagicSpellRegistry, val attacks: SpellAttackRegistry) {
        fun cast(scope: GameTestScope, source: Player, target: Player, obj: ObjType) {
            val spell = checkNotNull(spells.getObjSpell(obj))
            val spellAttack = checkNotNull(attacks[obj])
            with(scope) {
                source.withProtectedAccess {
                    with(spellAttack) { attack(target, CombatAttack.Spell(null, spell, false)) }
                }
            }
        }

        fun cast(scope: GameTestScope, source: Player, target: Npc, obj: ObjType) {
            val spell = checkNotNull(spells.getObjSpell(obj))
            val spellAttack = checkNotNull(attacks[obj])
            with(scope) {
                source.withProtectedAccess {
                    with(spellAttack) { attack(target, CombatAttack.Spell(null, spell, false)) }
                }
            }
        }
    }

    private object MagicModule : AbstractModule() {
        override fun configure() {
            install(MagicSpellModule())
            install(SpellAttacksModule())
            install(internalModule("org.rsmod.api.spells.attack.SpellAttackModule"))
            install(internalModule("org.rsmod.api.spells.runes.MagicRunesModule"))
            bind(AttackTypes::class.java).`in`(Scopes.SINGLETON)
            bind(AttackStyles::class.java).`in`(Scopes.SINGLETON)
        }

        private fun internalModule(name: String): AbstractModule =
            Class.forName(name).getDeclaredConstructor().newInstance() as AbstractModule
    }

    private fun GameTestState.magicTest(test: GameTestScope.(Deps) -> Unit) =
        runInjectedGameTest(
            Deps::class,
            MagicModule,
            script("org.rsmod.api.spells.attack.scripts.SpellAttackScript"),
            MagicEffectsScript::class,
            ComboRuneScript::class,
            CompactRuneScript::class,
            FakeRuneScript::class,
            StaffSubstituteScript::class,
            RuneSubstituteScript::class,
            UnlimitedRuneScript::class,
            PlayerHitScript::class,
            WeaponAttackStylesScript::class,
            WeaponAttackTypesScript::class,
            testBody = test,
        )

    @Suppress("UNCHECKED_CAST")
    private fun script(name: String): KClass<out PluginScript> =
        Class.forName(name).kotlin as KClass<out PluginScript>

    /** Two players with 99s and plenty of runes. */
    private fun GameTestScope.fighters(): Pair<Player, Player> {
        val a = player
        a.teleport(START)
        val b = registerPlayer(START.translateX(1))
        for (fighter in listOf(a, b)) {
            fighter.observerUUID = fighter.uuid
            for (stat in COMBAT_STATS) {
                fighter.stats[stat] = 99
            }
            fighter.setVarBit(varbits.spellbook, 1)
            for ((slot, rune) in RUNES.withIndex()) {
                fighter.inv[slot] = InvObj(runeType(rune), 1000)
            }
        }
        advance()
        return a to b
    }

    private fun GameTestScope.runeType(name: String): ObjType =
        findObjType { it.internalName == name }.first()

    private companion object {
        val START = CoordGrid(0, 50, 50, 10, 10)
        val RUNES =
            listOf(
                "airrune",
                "firerune",
                "waterrune",
                "earthrune",
                "chaosrune",
                "deathrune",
                "bloodrune",
                "soulrune",
                "astralrune",
            )
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
