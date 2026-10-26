package org.rsmod.api.combat

import jakarta.inject.Inject
import org.rsmod.api.combat.commons.CombatAttack
import org.rsmod.api.combat.manager.PlayerAttackManager
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.player.stat.stat
import org.rsmod.api.player.stat.statDrain
import org.rsmod.api.player.stat.statHeal
import org.rsmod.api.random.GameRandom
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.spot.SpotanimReferences
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.PathingEntity
import org.rsmod.game.entity.Player
import org.rsmod.game.type.obj.ObjType

internal object BoltObjs : ObjReferences() {
    val opal = find("xbows_crossbow_bolts_bronze_tipped_opal_enchanted")
    val pearl = find("xbows_crossbow_bolts_iron_tipped_pearl_enchanted")
    val topaz = find("xbows_crossbow_bolts_steel_tipped_redtopaz_enchanted")
    val sapphire = find("xbows_crossbow_bolts_mithril_tipped_sapphire_enchanted")
    val ruby = find("xbows_crossbow_bolts_adamantite_tipped_ruby_enchanted")
    val diamond = find("xbows_crossbow_bolts_adamantite_tipped_diamond_enchanted")
    val dragonstone = find("xbows_crossbow_bolts_runite_tipped_dragonstone_enchanted")
    val onyx = find("xbows_crossbow_bolts_runite_tipped_onyx_enchanted")
    val dragon_opal = find("dragon_bolts_enchanted_opal")
    val dragon_pearl = find("dragon_bolts_enchanted_pearl")
    val dragon_topaz = find("dragon_bolts_enchanted_topaz")
    val dragon_sapphire = find("dragon_bolts_enchanted_sapphire")
    val dragon_ruby = find("dragon_bolts_enchanted_ruby")
    val dragon_diamond = find("dragon_bolts_enchanted_diamond")
    val dragon_dragonstone = find("dragon_bolts_enchanted_dragonstone")
    val dragon_onyx = find("dragon_bolts_enchanted_onyx")
}

internal object BoltSpots : SpotanimReferences() {
    val lucky_lightning = find("xbows_lucky_lightening_strike_spot_anim")
    val sea_curse = find("xbows_sea_curse_waterfall_spot_anim")
    val clear_mind = find("xbows_clear_mind_glowing_spot_anim")
    val life_leech = find("xbows_life_leach_spot_anim")
    val blood_forfeit = find("xbows_blood_sacrifice_spot_anim")
    val dragons_breath = find("xbows_dragons_breath_spot_anim")
    val down_to_earth = find("xbows_down_to_earth_spot_anim")
    val armour_piercing = find("xbows_diamond_tips_spotanim")
}

/**
 * Enchanted bolt procs. Chances are per-mille, without the Kandarin hard diary bonus.
 *
 * Skipped: Jade (needs a freeze system) and Emerald (needs a poison system). Neither exists yet.
 * Skipped details: fiery/water-staff modifiers on Pearl and Dragonstone, bolt proc sounds (no bolt
 * synths in the cache symbols).
 */
public class BoltEffects
@Inject
constructor(private val random: GameRandom, private val manager: PlayerAttackManager) {
    private enum class Bolt(val pvp: Int, val pvn: Int, val ignoresAccuracy: Boolean) {
        Opal(50, 50, true),
        Pearl(60, 60, true),
        Topaz(40, 0, true),
        Sapphire(50, 250, false),
        Ruby(110, 60, true),
        Diamond(50, 100, true),
        Dragonstone(60, 60, false),
        Onyx(100, 110, false),
    }

    private val bolts: Map<ObjType, Bolt> =
        mapOf(
            BoltObjs.opal to Bolt.Opal,
            BoltObjs.dragon_opal to Bolt.Opal,
            BoltObjs.pearl to Bolt.Pearl,
            BoltObjs.dragon_pearl to Bolt.Pearl,
            BoltObjs.topaz to Bolt.Topaz,
            BoltObjs.dragon_topaz to Bolt.Topaz,
            BoltObjs.sapphire to Bolt.Sapphire,
            BoltObjs.dragon_sapphire to Bolt.Sapphire,
            BoltObjs.ruby to Bolt.Ruby,
            BoltObjs.dragon_ruby to Bolt.Ruby,
            BoltObjs.diamond to Bolt.Diamond,
            BoltObjs.dragon_diamond to Bolt.Diamond,
            BoltObjs.dragonstone to Bolt.Dragonstone,
            BoltObjs.dragon_dragonstone to Bolt.Dragonstone,
            BoltObjs.onyx to Bolt.Onyx,
            BoltObjs.dragon_onyx to Bolt.Onyx,
        )

    /** Rolls a ranged hit against [target], applying the proc of [ammo] if it is an (e) bolt. */
    public fun ProtectedAccess.rollDamage(
        target: PathingEntity,
        attack: CombatAttack.Ranged,
        ammo: ObjType?,
    ): Int {
        val bolt = ammo?.let { bolts[it] }
        val chance = if (bolt == null) 0 else if (target is Player) bolt.pvp else bolt.pvn
        // The proc roll is always consumed first so that proc order is predictable.
        if (bolt == null || chance == 0 || random.of(1000) >= chance) {
            return manager.rollRangedDamage(player, target, attack)
        }

        val ranged = player.stat(stats.ranged)
        var maxHitMultiplier = 1.0
        var bonus = 0
        var forcedDamage = -1
        when (bolt) {
            Bolt.Opal -> bonus = ranged / 10
            Bolt.Pearl -> bonus = ranged / 20
            Bolt.Dragonstone -> bonus = ranged / 5
            Bolt.Diamond -> maxHitMultiplier = 1.15
            Bolt.Onyx -> maxHitMultiplier = 1.2
            Bolt.Ruby -> {
                val targetHp = if (target is Player) target.hitpoints else (target as Npc).hitpoints
                forcedDamage = minOf(100, targetHp / 5)
                player.statDrain(stats.hitpoints, player.hitpoints / 10, 0)
            }
            Bolt.Sapphire -> {
                if (target is Player) {
                    val drain = ranged / 20
                    target.statDrain(stats.prayer, drain, 0)
                    player.statHeal(stats.prayer, drain / 2, 0)
                }
            }
            Bolt.Topaz -> if (target is Player) target.statDrain(stats.magic, 1, 0)
        }

        spotanim(target, bolt)

        if (forcedDamage >= 0) {
            return forcedDamage
        }

        val damage =
            if (bolt.ignoresAccuracy) {
                manager.rollRangedMaxHit(
                    source = player,
                    target = target,
                    attackType = attack.type,
                    attackStyle = attack.style,
                    multiplier = maxHitMultiplier,
                    boltSpecDamage = bonus,
                )
            } else {
                manager.rollRangedDamage(
                    player,
                    target,
                    attack,
                    maxHitMultiplier = maxHitMultiplier,
                    boltSpecDamage = bonus,
                )
            }

        if (bolt == Bolt.Onyx) {
            player.statHeal(stats.hitpoints, damage / 4, 0)
        }
        return damage
    }

    private fun spotanim(target: PathingEntity, bolt: Bolt) {
        val spot =
            when (bolt) {
                Bolt.Opal -> BoltSpots.lucky_lightning
                Bolt.Pearl -> BoltSpots.sea_curse
                Bolt.Topaz -> BoltSpots.down_to_earth
                Bolt.Sapphire -> BoltSpots.clear_mind
                Bolt.Ruby -> BoltSpots.blood_forfeit
                Bolt.Diamond -> BoltSpots.armour_piercing
                Bolt.Dragonstone -> BoltSpots.dragons_breath
                Bolt.Onyx -> BoltSpots.life_leech
            }
        target.spotanim(spot, height = 96)
    }
}
