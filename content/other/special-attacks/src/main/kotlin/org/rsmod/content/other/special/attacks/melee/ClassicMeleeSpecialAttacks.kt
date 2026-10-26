package org.rsmod.content.other.special.attacks.melee

import jakarta.inject.Inject
import kotlin.math.min
import org.rsmod.api.combat.commons.CombatAttack
import org.rsmod.api.combat.commons.types.MeleeAttackType
import org.rsmod.api.combat.formulas.AccuracyFormulae
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.objs
import org.rsmod.api.player.output.UpdateRun
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.severProtectionPrayers
import org.rsmod.api.random.GameRandom
import org.rsmod.api.specials.SpecialAttackManager
import org.rsmod.api.specials.SpecialAttackMap
import org.rsmod.api.specials.SpecialAttackRepository
import org.rsmod.api.specials.combat.MeleeSpecialAttack
import org.rsmod.content.other.special.attacks.configs.special_objs
import org.rsmod.content.other.special.attacks.configs.special_seqs
import org.rsmod.content.other.special.attacks.configs.special_spots
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.PathingEntity
import org.rsmod.game.entity.Player
import org.rsmod.game.type.seq.SeqType
import org.rsmod.game.type.spot.SpotanimType

/** OSRS wiki values. Energy costs come from the cache's `sa_energy_requirements` enum. */
class ClassicMeleeSpecialAttacks
@Inject
constructor(private val accuracy: AccuracyFormulae, private val random: GameRandom) :
    SpecialAttackMap {
    override fun SpecialAttackRepository.register(manager: SpecialAttackManager) {
        registerMelee(special_objs.dragon_dagger, Puncture(manager))
        registerMelee(
            objs.armadyl_godsword,
            Swing(
                manager,
                special_seqs.armadyl_godsword,
                special_spots.armadyl_godsword,
                accuracy = 2.0,
                damage = 1.375,
                block = MeleeAttackType.Slash,
            ),
        )
        registerMelee(
            special_objs.dragon_mace,
            Swing(
                manager,
                special_seqs.dragon_mace,
                special_spots.dragon_mace,
                accuracy = 1.25,
                damage = 1.5,
                block = MeleeAttackType.Crush,
            ),
        )
        registerMelee(objs.abyssal_whip, EnergyDrain(manager))
        registerMelee(special_objs.dragon_scimitar, Sever(manager))
        registerMelee(special_objs.vestas_longsword, Feint(manager, accuracy, random))
        registerMelee(objs.dragon_claws, SliceAndDice(manager, random))
    }

    /** Lets a special treat npc and player targets the same way. */
    private abstract class AnyTargetSpecial : MeleeSpecialAttack {
        abstract fun ProtectedAccess.special(
            target: PathingEntity,
            attack: CombatAttack.Melee,
        ): Boolean

        override suspend fun ProtectedAccess.attack(target: Npc, attack: CombatAttack.Melee) =
            special(target, attack)

        override suspend fun ProtectedAccess.attack(target: Player, attack: CombatAttack.Melee) =
            special(target, attack)
    }

    /** Dragon dagger: two quick hits, each +15% accuracy and damage. */
    private class Puncture(private val manager: SpecialAttackManager) : AnyTargetSpecial() {
        override fun ProtectedAccess.special(
            target: PathingEntity,
            attack: CombatAttack.Melee,
        ): Boolean {
            anim(special_seqs.dragon_dagger)
            spotanim(
                special_spots.dragon_dagger,
                height = 96,
                slot = constants.spotanim_slot_combat,
            )
            val hits =
                List(2) {
                    manager.rollMeleeDamage(
                        this,
                        target,
                        attack,
                        accuracyMultiplier = 1.15,
                        maxHitMultiplier = 1.15,
                        blockType = MeleeAttackType.Slash,
                    )
                }
            manager.giveCombatXp(this, target, attack, hits.sum())
            for (damage in hits) {
                manager.queueMeleeHit(this, target, damage)
            }
            manager.continueCombat(this, target)
            return true
        }
    }

    /** One boosted hit: the Armadyl godsword and dragon mace. */
    private class Swing(
        private val manager: SpecialAttackManager,
        private val seq: SeqType,
        private val spot: SpotanimType,
        private val accuracy: Double,
        private val damage: Double,
        private val block: MeleeAttackType,
    ) : AnyTargetSpecial() {
        override fun ProtectedAccess.special(
            target: PathingEntity,
            attack: CombatAttack.Melee,
        ): Boolean {
            anim(seq)
            spotanim(spot, height = 96, slot = constants.spotanim_slot_combat)
            val hit =
                manager.rollMeleeDamage(this, target, attack, accuracy, damage, blockType = block)
            manager.giveCombatXp(this, target, attack, hit)
            manager.queueMeleeHit(this, target, hit)
            manager.continueCombat(this, target)
            return true
        }
    }

    /** Abyssal whip: +25% accuracy; a hit siphons 10% of a player target's run energy. */
    private class EnergyDrain(private val manager: SpecialAttackManager) : AnyTargetSpecial() {
        override fun ProtectedAccess.special(
            target: PathingEntity,
            attack: CombatAttack.Melee,
        ): Boolean {
            anim(special_seqs.abyssal_whip)
            target.spotanim(special_spots.abyssal_whip, height = 100)
            val landed = rollAccuracy(manager, target, attack, 1.25, MeleeAttackType.Slash)
            val hit =
                if (landed) manager.rollMeleeMaxHit(this, target, attack.type, attack.style, 1.0)
                else 0
            if (landed && target is Player) {
                val siphoned = target.runEnergy / 10
                target.runEnergy -= siphoned
                player.runEnergy = min(constants.run_max_energy, player.runEnergy + siphoned)
                UpdateRun.energy(target, target.runEnergy)
                UpdateRun.energy(player, player.runEnergy)
            }
            manager.giveCombatXp(this, target, attack, hit)
            manager.queueMeleeHit(this, target, hit)
            manager.continueCombat(this, target)
            return true
        }
    }

    /**
     * Dragon scimitar: +25% accuracy; a hit on a player turns off their protection prayers and
     * keeps them off for 8 ticks.
     */
    private class Sever(private val manager: SpecialAttackManager) : AnyTargetSpecial() {
        override fun ProtectedAccess.special(
            target: PathingEntity,
            attack: CombatAttack.Melee,
        ): Boolean {
            anim(special_seqs.dragon_scimitar)
            spotanim(
                special_spots.dragon_scimitar,
                height = 96,
                slot = constants.spotanim_slot_combat,
            )
            val landed = rollAccuracy(manager, target, attack, 1.25, MeleeAttackType.Slash)
            val hit =
                if (landed) manager.rollMeleeMaxHit(this, target, attack.type, attack.style, 1.0)
                else 0
            if (landed && target is Player) {
                target.severProtectionPrayers(cycles = SEVER_TICKS)
            }
            manager.giveCombatXp(this, target, attack, hit)
            manager.queueMeleeHit(this, target, hit)
            manager.continueCombat(this, target)
            return true
        }
    }

    /**
     * Vesta's longsword: rolls stab accuracy against a quarter of a player target's defence, then
     * hits for 20%-120% of the normal max hit. Npc targets get a normal stab accuracy roll.
     */
    private class Feint(
        private val manager: SpecialAttackManager,
        private val accuracy: AccuracyFormulae,
        private val random: GameRandom,
    ) : AnyTargetSpecial() {
        override fun ProtectedAccess.special(
            target: PathingEntity,
            attack: CombatAttack.Melee,
        ): Boolean {
            anim(special_seqs.vestas_longsword)
            val landed =
                if (target is Player) {
                    val chance =
                        accuracy.getMeleeHitChance(
                            player,
                            target,
                            attack.type,
                            attack.style,
                            MeleeAttackType.Stab,
                            specMultiplier = 1.0,
                            defenceMultiplier = 0.25,
                        )
                    AccuracyFormulae.isSuccessfulHit(chance, random)
                } else {
                    rollAccuracy(manager, target, attack, 1.0, MeleeAttackType.Stab)
                }
            val hit =
                if (landed) {
                    val max =
                        manager.calculateMeleeMaxHit(this, target, attack.type, attack.style, 1.2)
                    val least =
                        manager.calculateMeleeMaxHit(this, target, attack.type, attack.style, 0.2)
                    random.of(least, max)
                } else {
                    0
                }
            manager.giveCombatXp(this, target, attack, hit)
            manager.queueMeleeHit(this, target, hit)
            manager.continueCombat(this, target)
            return true
        }
    }

    /** Dragon claws: up to four accuracy rolls, split into four hitsplats by [DragonClaws]. */
    private class SliceAndDice(
        private val manager: SpecialAttackManager,
        private val random: GameRandom,
    ) : AnyTargetSpecial() {
        override fun ProtectedAccess.special(
            target: PathingEntity,
            attack: CombatAttack.Melee,
        ): Boolean {
            anim(special_seqs.dragon_claws)
            spotanim(special_spots.dragon_claws, height = 96, slot = constants.spotanim_slot_combat)
            val landedOn =
                (1..4).firstOrNull {
                    rollAccuracy(manager, target, attack, 1.0, MeleeAttackType.Slash)
                } ?: 0
            val hits =
                if (landedOn == 0) {
                    if (random.of(3) < 2) DragonClaws.MISS_PATTERNS[random.of(4)] else List(4) { 0 }
                } else {
                    val max =
                        manager.calculateMeleeMaxHit(this, target, attack.type, attack.style, 1.0)
                    val range = DragonClaws.firstHitRange(max, landedOn)
                    DragonClaws.hits(landedOn, random.of(range.first, range.last))
                }
            manager.giveCombatXp(this, target, attack, hits.sum())
            // Two hitsplats land straight away, the other two a tick later.
            hits.forEachIndexed { index, damage ->
                manager.queueMeleeHit(this, target, damage, delay = if (index < 2) 1 else 2)
            }
            manager.continueCombat(this, target)
            return true
        }
    }

    private companion object {
        const val SEVER_TICKS = 8

        fun ProtectedAccess.rollAccuracy(
            manager: SpecialAttackManager,
            target: PathingEntity,
            attack: CombatAttack.Melee,
            multiplier: Double,
            block: MeleeAttackType,
        ): Boolean =
            manager.rollMeleeAccuracy(this, target, attack.type, attack.style, block, multiplier)
    }
}
