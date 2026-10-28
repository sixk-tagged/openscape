package org.rsmod.api.combat

import jakarta.inject.Inject
import org.rsmod.api.combat.commons.npc.NpcAttack
import org.rsmod.api.combat.commons.npc.NpcAttackStyle
import org.rsmod.api.combat.commons.npc.NpcAttackTable
import org.rsmod.api.combat.commons.npc.attackRate
import org.rsmod.api.combat.commons.player.combatPlayDefendAnim
import org.rsmod.api.combat.commons.player.queueCombatRetaliate
import org.rsmod.api.combat.commons.poison.NpcPoisonTable
import org.rsmod.api.combat.commons.poison.Poison
import org.rsmod.api.combat.commons.poison.poison
import org.rsmod.api.combat.commons.types.MeleeAttackType
import org.rsmod.api.combat.formulas.AccuracyFormulae
import org.rsmod.api.combat.formulas.MaxHitFormulae
import org.rsmod.api.combat.npc.attackingPlayer
import org.rsmod.api.combat.npc.lastAttack
import org.rsmod.api.combat.player.aggressiveNpc
import org.rsmod.api.combat.player.lastCombat
import org.rsmod.api.config.refs.categories
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.projanims
import org.rsmod.api.config.refs.spotanims
import org.rsmod.api.config.refs.synths
import org.rsmod.api.npc.access.StandardNpcAccess
import org.rsmod.api.npc.isInCombat
import org.rsmod.api.player.hit.queueHit
import org.rsmod.api.player.isValidTarget
import org.rsmod.api.player.output.soundSynth
import org.rsmod.api.player.stat.statDrain
import org.rsmod.api.repo.world.WorldRepository
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Player
import org.rsmod.game.hit.HitType
import org.rsmod.game.type.category.isType
import org.rsmod.game.type.obj.ObjTypeList

/**
 * An npc attacking a player. Npcs without an [NpcAttackTable] profile hit in melee with their
 * `attack_anim`; the rest roll one of their profile's attacks each time and close in with `ap`.
 */
public class NvPCombat
@Inject
constructor(
    private val accuracy: AccuracyFormulae,
    private val maxHits: MaxHitFormulae,
    private val objTypes: ObjTypeList,
    private val attacks: NpcAttackTable,
    private val poisons: NpcPoisonTable,
    private val worldRepo: WorldRepository,
) {
    public fun attack(access: StandardNpcAccess, target: Player) {
        access.runAttack(target)
    }

    private fun StandardNpcAccess.runAttack(target: Player) {
        if (!canAttack(target)) {
            resetMode()
            return
        }

        // Note: We do not need to explicitly call `opplayer2` because npcs will automatically
        // repeat their last interaction until it is canceled (e.g., by changing their `npcmode`).
        if (actionDelay > mapClock) {
            return
        }

        if (!npc.isInCombat()) {
            resetMode()
            return
        }

        val profile = attacks[npc.visType]
        actionDelay = mapClock + (profile?.speed ?: npc.attackRate())

        val attack = profile?.pick(random, adjacent = isWithinDistance(target, 1))
        val landed =
            when (attack?.style) {
                NpcAttackStyle.Ranged -> attackRanged(target, attack)
                NpcAttackStyle.Magic -> attackMagic(target, attack)
                else -> attackMelee(target, attack)
            }

        // Poison lands with the attack, whether or not it rolled any damage.
        val poisonDamage = poisons[npc.visType]
        if (landed && poisonDamage != null) {
            target.poison(Poison.severity(poisonDamage))
        }
    }

    private fun StandardNpcAccess.attackMelee(target: Player, attack: NpcAttack?): Boolean {
        val type = attack?.melee ?: npc.resolveMeleeAttackType()
        playAttackFx(attack, target)

        val successfulHit = accuracy.rollMeleeAccuracy(npc, target, type, random)

        val damage =
            if (successfulHit) {
                val maxHit = attack?.maxHit ?: maxHits.getMeleeMaxHit(npc, target, type)
                random.of(0..maxHit)
            } else {
                0
            }

        finishAttack(target, HitType.Melee, damage, hitDelay = 1, clientDelay = 0)
        return successfulHit
    }

    private fun StandardNpcAccess.attackRanged(target: Player, attack: NpcAttack): Boolean {
        playAttackFx(attack, target)
        val projAnim = worldRepo.projAnim(npc, target, attack.travel!!, projType(attack))
        val (serverDelay, clientDelay) = projAnim.durations

        val successfulHit = accuracy.rollRangedAccuracy(npc, target, random)
        val damage = if (successfulHit) random.of(0..attack.maxHit) else 0
        if (successfulHit) {
            playImpactFx(attack, target, clientDelay)
        }

        finishAttack(target, HitType.Ranged, damage, serverDelay, clientDelay)
        return successfulHit
    }

    private fun StandardNpcAccess.attackMagic(target: Player, attack: NpcAttack): Boolean {
        playAttackFx(attack, target)
        val projAnim = worldRepo.projAnim(npc, target, attack.travel!!, projType(attack))
        val (serverDelay, clientDelay) = projAnim.durations

        val successfulHit = accuracy.rollMagicAccuracy(npc, target, random)
        var damage = 0
        if (successfulHit) {
            playImpactFx(attack, target, clientDelay)
            val drain = attack.drain
            if (drain != null) {
                target.statDrain(drain.stat, 0, drain.percent)
            } else {
                damage = random.of(0..attack.maxHit)
            }
        } else {
            target.spotanim(spotanims.failedspell_impact, delay = clientDelay, height = 124)
            target.soundSynth(synths.spellfail, delay = clientDelay)
        }

        finishAttack(target, HitType.Magic, damage, serverDelay, clientDelay)
        return successfulHit
    }

    private fun StandardNpcAccess.playAttackFx(attack: NpcAttack?, target: Player) {
        anim(attack?.anim ?: npc.visType.param(params.attack_anim))
        val sound = attack?.sound ?: npc.visType.paramOrNull(params.attack_sound)
        sound?.let { target.soundSynth(it) }
        attack?.launch?.let { spotanim(it, height = 92) }
    }

    private fun playImpactFx(attack: NpcAttack, target: Player, clientDelay: Int) {
        attack.impact?.let { target.spotanim(it, delay = clientDelay, height = 124) }
        attack.impactSound?.let { target.soundSynth(it, delay = clientDelay) }
    }

    private fun projType(attack: NpcAttack) =
        attack.projAnim
            ?: if (attack.style == NpcAttackStyle.Ranged) projanims.arrow else projanims.magic_spell

    private fun StandardNpcAccess.finishAttack(
        target: Player,
        type: HitType,
        damage: Int,
        hitDelay: Int,
        clientDelay: Int,
    ) {
        setAttackVars(target)

        // Note: Retaliation must be queued _before_ the hit. If queued after, every hit would
        // trigger the "speed-up" death mechanic, since the hit queues would no longer be the
        // last entries in the queue list at the time of processing.
        target.queueCombatRetaliate(npc, hitDelay)

        target.queueHit(npc, hitDelay, type, damage)
        target.combatPlayDefendAnim(objTypes, clientDelay)
    }

    private fun canAttack(target: Player): Boolean {
        return target.isValidTarget()
    }

    private fun Npc.resolveMeleeAttackType(): MeleeAttackType {
        val category = visType.paramOrNull(params.npc_attack_type)
        return when {
            category.isType(categories.attacktype_stab) -> MeleeAttackType.Stab
            category.isType(categories.attacktype_slash) -> MeleeAttackType.Slash
            else -> MeleeAttackType.Crush
        }
    }

    private fun StandardNpcAccess.setAttackVars(target: Player) {
        npc.lastAttack = mapClock
        npc.attackingPlayer = target.uid
        target.lastCombat = mapClock
        target.aggressiveNpc = npc.uid
    }
}
