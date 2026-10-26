package org.rsmod.content.other.special.attacks.instant

import jakarta.inject.Inject
import org.rsmod.api.combat.commons.CombatAttack
import org.rsmod.api.combat.commons.CombatStance
import org.rsmod.api.combat.commons.styles.MeleeAttackStyle
import org.rsmod.api.combat.commons.types.MeleeAttackType
import org.rsmod.api.combat.weapon.styles.AttackStyles
import org.rsmod.api.combat.weapon.types.AttackTypes
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.varps
import org.rsmod.api.npc.isValidTarget
import org.rsmod.api.player.isValidTarget
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.righthand
import org.rsmod.api.player.vars.enumVarp
import org.rsmod.api.specials.SpecialAttackManager
import org.rsmod.api.specials.SpecialAttackMap
import org.rsmod.api.specials.SpecialAttackRepository
import org.rsmod.content.other.special.attacks.configs.special_objs
import org.rsmod.content.other.special.attacks.configs.special_seqs
import org.rsmod.content.other.special.attacks.configs.special_spots
import org.rsmod.game.entity.PathingEntity
import org.rsmod.game.entity.Player
import org.rsmod.game.interact.InteractionNpc
import org.rsmod.game.interact.InteractionPlayer

/** Combat specials that fire straight from the special attack bar. */
class CombatInstantSpecialAttacks
@Inject
constructor(private val types: AttackTypes, private val styles: AttackStyles) : SpecialAttackMap {
    private var Player.combatStance by enumVarp<CombatStance>(varps.com_mode)

    override fun SpecialAttackRepository.register(manager: SpecialAttackManager) {
        registerInstant(special_objs.granite_maul) { quickSmash(manager) }
        registerInstant(special_objs.dragon_battleaxe) { rampage() }
    }

    /**
     * Granite maul: an instant extra hit on whoever you're fighting, on top of your normal attacks.
     */
    private fun ProtectedAccess.quickSmash(manager: SpecialAttackManager): Boolean {
        // Note: with no target OSRS saves the spec for your next hit; here it just isn't used.
        val target = currentTarget() ?: return false
        if (!inMeleeRange(target)) {
            return false
        }
        val attack =
            CombatAttack.Melee(
                weapon = player.righthand,
                type = MeleeAttackType.from(types.get(player)),
                style = MeleeAttackStyle.from(styles.get(player)),
                stance = player.combatStance,
            )
        anim(special_seqs.granite_maul)
        spotanim(special_spots.granite_maul, height = 96, slot = constants.spotanim_slot_combat)
        val hit = manager.rollMeleeDamage(this, target, attack, 1.0, 1.0)
        manager.giveCombatXp(this, target, attack, hit)
        manager.queueMeleeHit(this, target, hit)
        return true
    }

    private fun ProtectedAccess.currentTarget(): PathingEntity? =
        when (val interaction = player.interaction) {
            is InteractionNpc -> interaction.target.takeIf { it.isValidTarget() }
            is InteractionPlayer -> interaction.target.takeIf { it.isValidTarget() }
            else -> null
        }

    /** Straight next to [target]'s footprint, not diagonal and not underneath it. */
    private fun ProtectedAccess.inMeleeRange(target: PathingEntity): Boolean {
        val (px, pz) = player.coords.x to player.coords.z
        val (tx, tz) = target.coords.x to target.coords.z
        val last = target.size - 1
        if (player.coords.level != target.coords.level) {
            return false
        }
        val besideX = px in tx..tx + last && (pz == tz - 1 || pz == tz + target.size)
        val besideZ = pz in tz..tz + last && (px == tx - 1 || px == tx + target.size)
        return besideX || besideZ
    }

    /**
     * Dragon battleaxe: drains 10% of Attack, Defence, Ranged and Magic, and boosts Strength by 10
     * plus a quarter of the levels drained.
     */
    private fun ProtectedAccess.rampage(): Boolean {
        var drained = 0
        for (stat in listOf(stats.attack, stats.defence, stats.ranged, stats.magic)) {
            val drain = stat(stat) / 10
            statSub(stat, drain, 0)
            drained += drain
        }
        statBoost(stats.strength, 10 + drained / 4, 0)
        say("Raarrrrrgggggghhhhhhh!")
        anim(special_seqs.dragon_battleaxe)
        spotanim(special_spots.dragon_battleaxe, height = 96, slot = constants.spotanim_slot_combat)
        return true
    }
}
