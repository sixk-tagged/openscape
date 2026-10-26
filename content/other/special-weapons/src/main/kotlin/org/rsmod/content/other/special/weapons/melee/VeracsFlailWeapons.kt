package org.rsmod.content.other.special.weapons.melee

import jakarta.inject.Inject
import org.rsmod.api.combat.commons.CombatAttack
import org.rsmod.api.combat.commons.player.combatPlayDefendAnim
import org.rsmod.api.combat.commons.player.queueCombatRetaliate
import org.rsmod.api.config.refs.objs
import org.rsmod.api.player.hit.modifier.NoopPlayerHitModifier
import org.rsmod.api.player.hit.queueHit
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.random.GameRandom
import org.rsmod.api.weapons.MeleeWeapon
import org.rsmod.api.weapons.WeaponAttackManager
import org.rsmod.api.weapons.WeaponMap
import org.rsmod.api.weapons.WeaponRepository
import org.rsmod.content.other.special.weapons.barrows.wearsFullSet
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Player
import org.rsmod.game.hit.HitType
import org.rsmod.game.type.obj.ObjTypeList

/**
 * Verac's Defiler: with the full set, a quarter of attacks ignore the target's Defence roll and
 * protection prayers and hit for one more than usual. Every other swing is a normal attack.
 */
class VeracsFlailWeapons
@Inject
constructor(private val objTypes: ObjTypeList, private val random: GameRandom) : WeaponMap {
    override fun WeaponRepository.register(manager: WeaponAttackManager) {
        val flail = VeracsFlail(manager)
        register(objs.veracs_flail, flail)
        register(objs.veracs_flail_100, flail)
        register(objs.veracs_flail_75, flail)
        register(objs.veracs_flail_50, flail)
        register(objs.veracs_flail_25, flail)
    }

    private inner class VeracsFlail(private val manager: WeaponAttackManager) : MeleeWeapon {
        override suspend fun ProtectedAccess.attack(target: Npc, attack: CombatAttack.Melee) = false

        override suspend fun ProtectedAccess.attack(
            target: Player,
            attack: CombatAttack.Melee,
        ): Boolean {
            if (!objTypes.wearsFullSet(player, "verac") || !random.randomBoolean(4)) {
                return false
            }
            val maxHit =
                manager.rollMeleeMaxHit(this, target, attack.type, attack.style, multiplier = 1.0)
            val damage = maxHit + 1
            manager.giveCombatXp(this, target, attack, damage)
            manager.playWeaponFx(this, attack)
            target.queueCombatRetaliate(player)
            target.queueHit(
                player,
                delay = 1,
                HitType.Melee,
                damage,
                modifier = NoopPlayerHitModifier,
            )
            target.combatPlayDefendAnim(objTypes)
            manager.continueCombat(this, target)
            return true
        }
    }
}
