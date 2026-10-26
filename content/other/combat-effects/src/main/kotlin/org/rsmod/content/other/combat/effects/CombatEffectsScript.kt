package org.rsmod.content.other.combat.effects

import jakarta.inject.Inject
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.player.disablePrayers
import org.rsmod.api.player.hit.PlayerDamaged
import org.rsmod.api.player.hit.processor.InstantPlayerHitProcessor
import org.rsmod.api.player.hit.takeInstantHit
import org.rsmod.api.player.output.ChatType
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.ring
import org.rsmod.api.player.stat.baseHitpointsLvl
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.player.stat.stat
import org.rsmod.api.player.stat.statAdd
import org.rsmod.api.player.stat.statBase
import org.rsmod.api.player.stat.statDrain
import org.rsmod.api.player.stat.statSub
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.random.GameRandom
import org.rsmod.api.script.onEvent
import org.rsmod.content.other.combat.effects.configs.effect_objs
import org.rsmod.content.other.combat.effects.configs.effect_spotanims
import org.rsmod.content.other.combat.effects.configs.effect_varps
import org.rsmod.content.other.special.weapons.barrows.wearsFullSet
import org.rsmod.game.entity.Player
import org.rsmod.game.entity.PlayerList
import org.rsmod.game.hit.HitType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * On-hit effects that react to a landed hit: Smite, Redemption, Retribution, the ring of recoil and
 * the Barrows set effects that don't change the hit itself (Verac's lives with its weapon).
 */
class CombatEffectsScript
@Inject
constructor(
    private val players: PlayerList,
    private val objTypes: ObjTypeList,
    private val random: GameRandom,
    private val instantHits: InstantPlayerHitProcessor,
) : PluginScript() {
    override fun ScriptContext.startup() {
        onEvent<PlayerDamaged> {
            val attacker = if (hit.isFromPlayer) hit.resolvePlayerSource(players) else null
            if (attacker != null && damage > 0) {
                smite(attacker, target, damage)
                recoil(attacker, target, damage)
                setEffects(attacker, target, hit.type, damage)
            }
            redemption(target)
            if (attacker != null) {
                retribution(attacker, target, damage)
            }
        }
    }

    private fun smite(attacker: Player, target: Player, damage: Int) {
        if (attacker.vars[varbits.smite] == 1) {
            target.statSub(stats.prayer, constant = damage / 4, percent = 0)
        }
    }

    private fun redemption(target: Player) {
        val hp = target.hitpoints
        if (target.vars[varbits.redemption] != 1 || hp <= 0 || hp * 10 >= target.baseHitpointsLvl) {
            return
        }
        target.statAdd(stats.hitpoints, target.statBase(stats.prayer) / 4, percent = 0)
        target.statSub(stats.prayer, target.stat(stats.prayer), percent = 0)
        target.disablePrayers()
        target.spotanim(effect_spotanims.redemption)
    }

    private fun retribution(attacker: Player, target: Player, damage: Int) {
        if (damage <= 0 || target.hitpoints > 0 || target.vars[varbits.retribution] != 1) {
            return
        }
        if (target.coords.chebyshevDistance(attacker.coords) > 1) {
            return
        }
        val max = target.statBase(stats.prayer) / 4
        attacker.takeInstantHit(HitType.Typeless, random.of(0, max), instantHits)
    }

    private fun recoil(attacker: Player, target: Player, damage: Int) {
        val ring = target.ring ?: return
        if (objTypes[ring].internalName != effect_objs.ring_of_recoil.internalName) {
            return
        }
        val dealt = target.recoilDealt
        val reflected = minOf(damage / 10 + 1, RECOIL_LIMIT - dealt)
        attacker.takeInstantHit(HitType.Typeless, reflected, instantHits)
        if (dealt + reflected >= RECOIL_LIMIT) {
            target.ring = null
            target.recoilDealt = 0
            target.mes("Your Ring of Recoil has shattered.", ChatType.GameMessage)
        } else {
            target.recoilDealt = dealt + reflected
        }
    }

    private fun setEffects(attacker: Player, target: Player, type: HitType, damage: Int) {
        when (type) {
            HitType.Melee -> {
                if (proc(attacker, "guthan")) {
                    val room = attacker.baseHitpointsLvl + GUTHAN_OVERHEAL - attacker.hitpoints
                    attacker.statAdd(stats.hitpoints, minOf(damage, room).coerceAtLeast(0), 0)
                }
                if (proc(attacker, "torag")) {
                    target.runEnergy -= target.runEnergy / 5
                }
            }
            HitType.Ranged -> {
                if (proc(attacker, "karil")) {
                    target.statDrain(stats.agility, constant = 0, percent = 20)
                }
            }
            HitType.Magic -> {
                if (proc(attacker, "ahrim")) {
                    target.statDrain(stats.strength, constant = 5, percent = 0)
                }
            }
            HitType.Typeless -> {}
        }
    }

    private fun proc(attacker: Player, brother: String): Boolean =
        objTypes.wearsFullSet(attacker, brother) && random.randomBoolean(4)

    private companion object {
        const val RECOIL_LIMIT = 40
        const val GUTHAN_OVERHEAL = 10
    }
}

private var Player.recoilDealt by intVarp(effect_varps.recoil_damage_dealt)
