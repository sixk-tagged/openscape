package org.rsmod.content.other.special.attacks.ranged

import jakarta.inject.Inject
import org.rsmod.api.combat.commons.CombatAttack
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.bonus.BlowpipeDarts
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.righthand
import org.rsmod.api.player.stat.statHeal
import org.rsmod.api.specials.SpecialAttackManager
import org.rsmod.api.specials.SpecialAttackMap
import org.rsmod.api.specials.SpecialAttackRepository
import org.rsmod.api.specials.combat.RangedSpecialAttack
import org.rsmod.content.other.special.attacks.configs.special_seqs
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.PathingEntity
import org.rsmod.game.entity.Player
import org.rsmod.game.type.obj.ObjTypeList

/** Toxic Siphon: double accuracy, +50% damage, and the user heals half the damage dealt. */
class ToxicBlowpipeSpecialAttack @Inject constructor(private val objTypes: ObjTypeList) :
    SpecialAttackMap {
    override fun SpecialAttackRepository.register(manager: SpecialAttackManager) {
        registerRanged(BlowpipeDarts.loaded, ToxicSiphon(manager, objTypes))
    }

    private class ToxicSiphon(
        private val manager: SpecialAttackManager,
        private val objTypes: ObjTypeList,
    ) : RangedSpecialAttack {
        override suspend fun ProtectedAccess.attack(
            target: Npc,
            attack: CombatAttack.Ranged,
        ): Boolean = siphon(target, attack)

        override suspend fun ProtectedAccess.attack(
            target: Player,
            attack: CombatAttack.Ranged,
        ): Boolean = siphon(target, attack)

        private fun ProtectedAccess.siphon(
            target: PathingEntity,
            attack: CombatAttack.Ranged,
        ): Boolean {
            val dart = BlowpipeDarts.dartType(player.righthand)
            if (dart == null || BlowpipeDarts.dartCount(player.righthand) == 0) {
                manager.stopCombat(this)
                mes("Your blowpipe has no darts in it.")
                return false
            }

            val dartType = objTypes[dart]
            val projanimType = objTypes[attack.weapon].paramOrNull(params.proj_type)
            val travelSpotanim = dartType.paramOrNull(params.proj_travel)
            if (projanimType == null || travelSpotanim == null) {
                manager.stopCombat(this)
                mes("You are unable to fire your ammunition.")
                return false
            }

            anim(special_seqs.toxic_blowpipe)
            spotanim(
                dartType.paramOrNull(params.proj_launch),
                height = 96,
                slot = constants.spotanim_slot_combat,
            )
            val projanim = manager.spawnProjectile(this, target, travelSpotanim, projanimType)

            val damage =
                manager.rollRangedDamage(
                    this,
                    target,
                    attack,
                    accuracyMultiplier = 2.0,
                    maxHitMultiplier = 1.5,
                )
            BlowpipeDarts.useDart(player)
            manager.giveCombatXp(this, target, attack, damage)
            manager.queueRangedHit(
                this,
                target,
                dartType,
                damage,
                projanim.clientCycles,
                projanim.serverCycles,
            )
            player.statHeal(stats.hitpoints, damage / 2, 0)
            manager.continueCombat(this, target)
            return true
        }
    }
}
