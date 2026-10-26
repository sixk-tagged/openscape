package org.rsmod.content.other.special.weapons.ranged

import jakarta.inject.Inject
import org.rsmod.api.combat.commons.CombatAttack
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.params
import org.rsmod.api.player.bonus.BlowpipeDarts
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.righthand
import org.rsmod.api.weapons.RangedWeapon
import org.rsmod.api.weapons.WeaponAttackManager
import org.rsmod.api.weapons.WeaponMap
import org.rsmod.api.weapons.WeaponRepository
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.PathingEntity
import org.rsmod.game.entity.Player
import org.rsmod.game.type.obj.ObjTypeList

/**
 * Fires one loaded dart per attack. Deviation from OSRS: no Zulrah's scales are needed, and darts
 * are never dropped or saved by Ava's devices.
 */
class ToxicBlowpipeWeapons @Inject constructor(private val objTypes: ObjTypeList) : WeaponMap {
    override fun WeaponRepository.register(manager: WeaponAttackManager) {
        val blowpipe = Blowpipe(manager, objTypes)
        register(BlowpipeDarts.loaded, blowpipe)
        // Registered so an empty blowpipe never falls through to the generic thrown-weapon path,
        // which would throw the blowpipe itself.
        register(BlowpipeDarts.empty, blowpipe)
    }

    private class Blowpipe(
        private val manager: WeaponAttackManager,
        private val objTypes: ObjTypeList,
    ) : RangedWeapon {
        override suspend fun ProtectedAccess.attack(
            target: Npc,
            attack: CombatAttack.Ranged,
        ): Boolean = shoot(target, attack)

        override suspend fun ProtectedAccess.attack(
            target: Player,
            attack: CombatAttack.Ranged,
        ): Boolean = shoot(target, attack)

        private fun ProtectedAccess.shoot(
            target: PathingEntity,
            attack: CombatAttack.Ranged,
        ): Boolean {
            val dart = BlowpipeDarts.dartType(player.righthand)
            if (dart == null || BlowpipeDarts.dartCount(player.righthand) == 0) {
                manager.stopCombat(this)
                mes("Your blowpipe has no darts in it.")
                return true
            }

            val dartType = objTypes[dart]
            val projanimType = objTypes[attack.weapon].paramOrNull(params.proj_type)
            val travelSpotanim = dartType.paramOrNull(params.proj_travel)
            if (projanimType == null || travelSpotanim == null) {
                manager.stopCombat(this)
                mes("You are unable to fire your ammunition.")
                return true
            }

            manager.playWeaponFx(this, attack)
            val launchSpotanim = dartType.paramOrNull(params.proj_launch)
            spotanim(launchSpotanim, height = 96, slot = constants.spotanim_slot_combat)

            val projanim = manager.spawnProjectile(this, target, travelSpotanim, projanimType)
            val (serverDelay, clientDelay) = projanim.durations

            // Damage is rolled before the dart is taken out so the dart bonuses still apply to
            // the last shot.
            val damage = manager.rollRangedDamage(this, target, attack)
            BlowpipeDarts.useDart(player)
            manager.giveCombatXp(this, target, attack, damage)
            manager.queueRangedHit(this, target, dartType, damage, clientDelay, serverDelay)
            manager.continueCombat(this, target)
            return true
        }
    }
}
