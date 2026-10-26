package org.rsmod.content.skills.magic.spell.attacks.ancient

import jakarta.inject.Inject
import kotlin.math.min
import org.rsmod.api.combat.commons.CombatAttack
import org.rsmod.api.combat.manager.MagicRuneManager.Companion.isFailure
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.projanims
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.magic.freeze
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.player.stat.statDrain
import org.rsmod.api.player.stat.statHeal
import org.rsmod.api.spells.attack.SpellAttack
import org.rsmod.api.spells.attack.SpellAttackManager
import org.rsmod.api.spells.attack.SpellAttackMap
import org.rsmod.api.spells.attack.SpellAttackRepository
import org.rsmod.content.skills.magic.spell.attacks.configs.AncientSeqs
import org.rsmod.content.skills.magic.spell.attacks.configs.AncientSpotanims as spots
import org.rsmod.content.skills.magic.spell.attacks.configs.AncientSynths as sounds
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.PathingEntity
import org.rsmod.game.entity.Player
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.spot.SpotanimType
import org.rsmod.game.type.synth.SynthType

/**
 * The Ancient Magicks combat spells (Smoke, Shadow, Blood and Ice: Rush, Burst, Blitz, Barrage).
 *
 * Note: Burst and Barrage only hit the target; the multi-combat splash (up to 9 targets) needs
 * multi-way areas first. Smoke's poison is not implemented (there is no poison system); Smoke only
 * deals damage. Effects are applied when the spell is cast on a successful (non-splash) roll, not
 * when it lands.
 */
class AncientSpells @Inject constructor() : SpellAttackMap {
    override fun SpellAttackRepository.register(manager: SpellAttackManager) {
        for (spell in spells) {
            register(spell.obj, AncientSpellAttack(manager, spell))
        }
    }

    sealed interface Effect {
        data object None : Effect

        data class Freeze(val ticks: Int) : Effect

        /** Heals the caster for 25% of the damage dealt. */
        data object Blood : Effect

        /** Drains the target's Attack by [percent]; never stacks past that (OSRS). */
        data class Drain(val percent: Int) : Effect
    }

    /** [travel] is null for the spells that have no projectile (they land three ticks later). */
    class Spell(
        val obj: ObjType,
        val maxHit: Int,
        val vertical: Boolean,
        val travel: SpotanimType?,
        val impact: SpotanimType,
        val castSound: SynthType,
        val hitSound: SynthType,
        val effect: Effect,
    )

    private class AncientSpellAttack(
        private val manager: SpellAttackManager,
        private val spell: Spell,
    ) : SpellAttack {
        override suspend fun ProtectedAccess.attack(target: Npc, attack: CombatAttack.Spell) {
            cast(target, attack)
        }

        override suspend fun ProtectedAccess.attack(target: Player, attack: CombatAttack.Spell) {
            cast(target, attack)
        }

        private fun ProtectedAccess.cast(target: PathingEntity, attack: CombatAttack.Spell) {
            val castResult = manager.attemptCast(this, attack)
            if (castResult.isFailure()) {
                return
            }
            anim(
                if (spell.vertical) AncientSeqs.burst_barrage_cast else AncientSeqs.rush_blitz_cast
            )

            val serverDelay: Int
            val clientDelay: Int
            if (spell.travel != null) {
                val proj =
                    manager.spawnProjectile(this, target, spell.travel, projanims.magic_spell)
                serverDelay = proj.durations.serverDelay
                clientDelay = proj.durations.clientDelay
            } else {
                serverDelay = 3
                clientDelay = 60
            }

            val spellObj = attack.spell.obj
            if (manager.rollSplash(this, target, attack, castResult)) {
                manager.playSplashFx(this, target, clientDelay, spell.castSound, soundRadius = 8)
                manager.queueSplashHit(this, target, spellObj, clientDelay, serverDelay)
                manager.continueCombatIfAutocast(this, target)
                return
            }

            val damage = manager.rollMaxHit(this, target, attack, castResult, spell.maxHit)
            manager.playHitFx(
                source = this,
                target = target,
                clientDelay = clientDelay,
                castSound = spell.castSound,
                soundRadius = 8,
                hitSpot = spell.impact,
                hitSpotHeight = if (spell.vertical) 0 else 124,
                hitSound = spell.hitSound,
            )
            manager.giveCombatXp(this, target, attack, damage)
            manager.queueMagicHit(this, target, spellObj, damage, clientDelay, serverDelay)
            if (target is Player) {
                applyEffect(target, damage)
            }
            manager.continueCombatIfAutocast(this, target)
        }

        private fun ProtectedAccess.applyEffect(target: Player, damage: Int) {
            when (val effect = spell.effect) {
                Effect.None -> {}
                is Effect.Freeze -> {
                    if (target.freeze(effect.ticks)) {
                        target.mes("You have been frozen!")
                    }
                }
                Effect.Blood -> {
                    val healed = min(damage, target.hitpoints) / 4
                    player.statHeal(stats.hitpoints, constant = healed, percent = 0)
                }
                is Effect.Drain ->
                    target.statDrain(stats.attack, constant = 0, percent = effect.percent)
            }
        }
    }

    private companion object {
        fun spell(
            obj: ObjType,
            maxHit: Int,
            vertical: Boolean,
            travel: SpotanimType?,
            impact: SpotanimType,
            castSound: SynthType,
            hitSound: SynthType,
            effect: Effect,
        ) = Spell(obj, maxHit, vertical, travel, impact, castSound, hitSound, effect)

        val spells: List<Spell> =
            listOf(
                spell(
                    objs.spell_smoke_rush,
                    13,
                    false,
                    spots.smoke_rush_travel,
                    spots.smoke_rush_impact,
                    sounds.smoke_cast,
                    sounds.smoke_rush_impact,
                    Effect.None,
                ),
                spell(
                    objs.spell_smoke_burst,
                    17,
                    true,
                    spots.smoke_burst_travel,
                    spots.smoke_burst_impact,
                    sounds.smoke_cast,
                    sounds.smoke_burst_impact,
                    Effect.None,
                ),
                spell(
                    objs.spell_smoke_blitz,
                    23,
                    false,
                    spots.smoke_blitz_travel,
                    spots.smoke_blitz_impact,
                    sounds.smoke_cast,
                    sounds.smoke_blitz_impact,
                    Effect.None,
                ),
                spell(
                    objs.spell_smoke_barrage,
                    27,
                    true,
                    spots.smoke_barrage_travel,
                    spots.smoke_barrage_impact,
                    sounds.smoke_cast,
                    sounds.smoke_barrage_impact,
                    Effect.None,
                ),
                spell(
                    objs.spell_shadow_rush,
                    14,
                    false,
                    spots.shadow_rush_travel,
                    spots.shadow_rush_impact,
                    sounds.shadow_cast,
                    sounds.shadow_rush_impact,
                    Effect.Drain(10),
                ),
                spell(
                    objs.spell_shadow_burst,
                    18,
                    true,
                    null,
                    spots.shadow_burst_impact,
                    sounds.shadow_cast,
                    sounds.shadow_burst_impact,
                    Effect.Drain(10),
                ),
                spell(
                    objs.spell_shadow_blitz,
                    24,
                    false,
                    spots.shadow_blitz_travel,
                    spots.shadow_blitz_impact,
                    sounds.shadow_cast,
                    sounds.shadow_blitz_impact,
                    Effect.Drain(15),
                ),
                spell(
                    objs.spell_shadow_barrage,
                    28,
                    true,
                    null,
                    spots.shadow_barrage_impact,
                    sounds.shadow_cast,
                    sounds.shadow_barrage_impact,
                    Effect.Drain(15),
                ),
                spell(
                    objs.spell_blood_rush,
                    15,
                    false,
                    spots.blood_rush_travel,
                    spots.blood_rush_impact,
                    sounds.blood_cast,
                    sounds.blood_rush_impact,
                    Effect.Blood,
                ),
                spell(
                    objs.spell_blood_burst,
                    21,
                    true,
                    null,
                    spots.blood_burst_impact,
                    sounds.blood_cast,
                    sounds.blood_burst_impact,
                    Effect.Blood,
                ),
                spell(
                    objs.spell_blood_blitz,
                    25,
                    false,
                    spots.blood_blitz_travel,
                    spots.blood_blitz_impact,
                    sounds.blood_cast,
                    sounds.blood_blitz_impact,
                    Effect.Blood,
                ),
                spell(
                    objs.spell_blood_barrage,
                    29,
                    true,
                    null,
                    spots.blood_barrage_impact,
                    sounds.blood_cast,
                    sounds.blood_barrage_impact,
                    Effect.Blood,
                ),
                spell(
                    objs.spell_ice_rush,
                    16,
                    false,
                    spots.ice_rush_travel,
                    spots.ice_rush_impact,
                    sounds.ice_cast,
                    sounds.ice_rush_impact,
                    Effect.Freeze(8),
                ),
                spell(
                    objs.spell_ice_burst,
                    22,
                    true,
                    spots.ice_burst_travel,
                    spots.ice_burst_impact,
                    sounds.ice_cast,
                    sounds.ice_burst_impact,
                    Effect.Freeze(16),
                ),
                spell(
                    objs.spell_ice_blitz,
                    26,
                    false,
                    spots.ice_blitz_travel,
                    spots.ice_blitz_impact,
                    sounds.ice_cast,
                    sounds.ice_blitz_impact,
                    Effect.Freeze(24),
                ),
                spell(
                    objs.spell_ice_barrage,
                    30,
                    true,
                    spots.ice_barrage_travel,
                    spots.ice_barrage_impact,
                    sounds.ice_cast,
                    sounds.ice_barrage_impact,
                    Effect.Freeze(32),
                ),
            )
    }
}
