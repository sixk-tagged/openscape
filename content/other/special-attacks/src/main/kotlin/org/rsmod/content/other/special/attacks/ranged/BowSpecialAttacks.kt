package org.rsmod.content.other.special.attacks.ranged

import jakarta.inject.Inject
import org.rsmod.api.combat.commons.CombatAttack
import org.rsmod.api.combat.manager.RangedAmmoManager
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.projanims
import org.rsmod.api.config.refs.seqs
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.quiver
import org.rsmod.api.specials.SpecialAttackManager
import org.rsmod.api.specials.SpecialAttackMap
import org.rsmod.api.specials.SpecialAttackRepository
import org.rsmod.api.specials.combat.RangedSpecialAttack
import org.rsmod.content.other.special.attacks.configs.special_objs
import org.rsmod.content.other.special.attacks.configs.special_seqs
import org.rsmod.content.other.special.attacks.configs.special_spots
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.PathingEntity
import org.rsmod.game.entity.Player
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.obj.UnpackedObjType

/** Magic shortbow Snapshot and magic longbow Powershot (OSRS wiki values). */
class BowSpecialAttacks
@Inject
constructor(private val objTypes: ObjTypeList, private val ammunition: RangedAmmoManager) :
    SpecialAttackMap {
    override fun SpecialAttackRepository.register(manager: SpecialAttackManager) {
        val snapshot = Snapshot(manager, ammunition, objTypes)
        registerRanged(objs.magic_shortbow, snapshot)
        registerRanged(special_objs.magic_shortbow_i, snapshot)
        registerRanged(special_objs.magic_longbow, Powershot(manager, ammunition, objTypes))
    }

    /** Shared ammo checks; [shoot] does the special itself once the arrows are ready. */
    private abstract class BowSpecial(
        val manager: SpecialAttackManager,
        val ammunition: RangedAmmoManager,
        val objTypes: ObjTypeList,
        private val arrowsNeeded: Int,
    ) : RangedSpecialAttack {
        abstract fun ProtectedAccess.shoot(
            target: PathingEntity,
            attack: CombatAttack.Ranged,
            arrows: UnpackedObjType,
        )

        override suspend fun ProtectedAccess.attack(target: Npc, attack: CombatAttack.Ranged) =
            fire(target, attack)

        override suspend fun ProtectedAccess.attack(target: Player, attack: CombatAttack.Ranged) =
            fire(target, attack)

        private fun ProtectedAccess.fire(
            target: PathingEntity,
            attack: CombatAttack.Ranged,
        ): Boolean {
            val bow = objTypes[attack.weapon]
            val arrows = objTypes.getOrNull(player.quiver)
            if (!ammunition.attemptAmmoUsage(player, bow, arrows) || arrows == null) {
                manager.stopCombat(this)
                return false
            }
            if (arrows.paramOrNull(params.proj_travel) == null) {
                manager.stopCombat(this)
                mes("You are unable to fire your ammunition.")
                return false
            }
            if ((player.quiver?.count ?: 0) < arrowsNeeded) {
                manager.stopCombat(this)
                mes(
                    "You need at least $arrowsNeeded arrows in your quiver for this special attack."
                )
                return false
            }
            shoot(target, attack, arrows)
            manager.continueCombat(this, target)
            return true
        }

        /** Rolls happen before any ammo is used, so the arrows' drop rolls come last. */
        fun ProtectedAccess.loose(
            target: PathingEntity,
            arrows: UnpackedObjType,
            damage: Int,
            projanim: org.rsmod.game.type.proj.ProjAnimType,
        ) {
            val proj =
                manager.spawnProjectile(this, target, special_spots.special_arrow_travel, projanim)
            ammunition.useQuiverAmmo(
                player = player,
                quiverType = arrows,
                dropCoord = target.coords,
                dropDelay = proj.serverCycles,
            )
            manager.queueRangedHit(
                this,
                target,
                arrows,
                damage,
                proj.clientCycles,
                proj.serverCycles,
            )
        }
    }

    /** Two arrows in one shot, each with +43% (10/7) accuracy. */
    private class Snapshot(
        manager: SpecialAttackManager,
        ammunition: RangedAmmoManager,
        objTypes: ObjTypeList,
    ) : BowSpecial(manager, ammunition, objTypes, arrowsNeeded = 2) {
        override fun ProtectedAccess.shoot(
            target: PathingEntity,
            attack: CombatAttack.Ranged,
            arrows: UnpackedObjType,
        ) {
            anim(special_seqs.magic_shortbow)
            spotanim(
                special_spots.magic_shortbow,
                height = 96,
                slot = constants.spotanim_slot_combat,
            )
            val hits = List(2) { manager.rollRangedDamage(this, target, attack, 10.0 / 7.0, 1.0) }
            manager.giveCombatXp(this, target, attack, hits.sum())
            loose(target, arrows, hits[0], projanims.doublearrow_one)
            loose(target, arrows, hits[1], projanims.doublearrow_two)
        }
    }

    /** One arrow that always hits: no accuracy roll is made. */
    private class Powershot(
        manager: SpecialAttackManager,
        ammunition: RangedAmmoManager,
        objTypes: ObjTypeList,
    ) : BowSpecial(manager, ammunition, objTypes, arrowsNeeded = 1) {
        override fun ProtectedAccess.shoot(
            target: PathingEntity,
            attack: CombatAttack.Ranged,
            arrows: UnpackedObjType,
        ) {
            anim(seqs.human_bow)
            spotanim(
                special_spots.special_arrow_launch,
                height = 96,
                slot = constants.spotanim_slot_combat,
            )
            val hit =
                manager.rollRangedMaxHit(
                    this,
                    target,
                    attack.type,
                    attack.style,
                    multiplier = 1.0,
                    boltSpecDamage = 0,
                )
            manager.giveCombatXp(this, target, attack, hit)
            loose(target, arrows, hit, projanims.doublearrow_one)
        }
    }
}
