package org.rsmod.content.skills.magic.spell.utility

import org.rsmod.api.combat.commons.CombatAttack
import org.rsmod.api.combat.manager.MagicRuneManager.Companion.isFailure
import org.rsmod.api.config.refs.categories
import org.rsmod.api.config.refs.projanims
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.spells.attack.SpellAttack
import org.rsmod.api.spells.attack.SpellAttackManager
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.PathingEntity
import org.rsmod.game.entity.Player
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.obj.UnpackedObjType
import org.rsmod.game.type.proj.ProjAnimType
import org.rsmod.game.type.seq.SeqType
import org.rsmod.game.type.spot.SpotanimType
import org.rsmod.game.type.synth.SynthType

/** How a [TargetedSpell] looks and sounds. Every graphic but the cast animation is optional. */
class SpellFx(
    val staffAnim: SeqType,
    val unarmedAnim: SeqType = staffAnim,
    val launch: SpotanimType? = null,
    val travel: SpotanimType? = null,
    val impact: SpotanimType? = null,
    val proj: ProjAnimType = projanims.magic_spell,
    val castSound: SynthType? = null,
    val hitSound: SynthType? = null,
)

/**
 * A spell cast on an npc or a player that rolls the usual magic accuracy: curses, binds, the god
 * spells... A hit runs [onHit] with the damage (also for max hit `0` spells); a splash only plays
 * the splash effects. [allowed] runs before the runes are taken.
 *
 * @param maxHit the base max hit, or `0` for a spell that deals no damage.
 */
class TargetedSpell(
    private val manager: SpellAttackManager,
    private val objTypes: ObjTypeList,
    private val fx: SpellFx,
    private val maxHit: (ProtectedAccess) -> Int = { 0 },
    private val allowed: ProtectedAccess.(PathingEntity) -> Boolean = { true },
    private val onHit: ProtectedAccess.(PathingEntity, Int) -> Unit = { _, _ -> },
) : SpellAttack {
    override suspend fun ProtectedAccess.attack(target: Npc, attack: CombatAttack.Spell) {
        cast(target, attack)
    }

    override suspend fun ProtectedAccess.attack(target: Player, attack: CombatAttack.Spell) {
        cast(target, attack)
    }

    private fun ProtectedAccess.cast(target: PathingEntity, attack: CombatAttack.Spell) {
        if (!allowed(target)) {
            manager.stopCombat(this)
            return
        }
        val castResult = manager.attemptCast(this, attack)
        if (castResult.isFailure()) {
            return
        }
        val weapon = objTypes.getOrNull(attack.weapon)
        anim(if (weapon.isStaff()) fx.staffAnim else fx.unarmedAnim)
        fx.launch?.let { spotanim(it, height = 92) }

        val travel = fx.travel
        val serverDelay: Int
        val clientDelay: Int
        if (travel != null) {
            val proj = manager.spawnProjectile(this, target, travel, fx.proj)
            serverDelay = proj.durations.serverDelay
            clientDelay = proj.durations.clientDelay
        } else {
            serverDelay = 2
            clientDelay = 30
        }

        val spell = attack.spell.obj
        if (manager.rollSplash(this, target, attack, castResult)) {
            manager.playSplashFx(this, target, clientDelay, fx.castSound, soundRadius = 8)
            manager.queueSplashHit(this, target, spell, clientDelay, serverDelay)
            manager.continueCombatIfAutocast(this, target)
            return
        }

        val base = maxHit(this)
        val damage = if (base > 0) manager.rollMaxHit(this, target, attack, castResult, base) else 0
        manager.playHitFx(
            source = this,
            target = target,
            clientDelay = clientDelay,
            castSound = fx.castSound,
            soundRadius = 8,
            hitSpot = fx.impact,
            hitSpotHeight = 124,
            hitSound = fx.hitSound,
        )
        manager.giveCombatXp(this, target, attack, damage)
        manager.queueMagicHit(this, target, spell, damage, clientDelay, serverDelay)
        onHit(target, damage)
        manager.continueCombatIfAutocast(this, target)
    }

    private fun UnpackedObjType?.isStaff(): Boolean =
        this != null && isCategoryType(categories.staff)
}
