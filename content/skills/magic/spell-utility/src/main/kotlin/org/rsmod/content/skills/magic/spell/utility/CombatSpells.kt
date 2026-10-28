package org.rsmod.content.skills.magic.spell.utility

import jakarta.inject.Inject
import org.rsmod.api.combat.manager.MagicRuneManager
import org.rsmod.api.combat.manager.MagicRuneManager.Companion.isFailure
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.protect.ProtectedAccessLauncher
import org.rsmod.api.player.stat.magicLvl
import org.rsmod.api.player.stat.statSub
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.script.onIfOverlayButton
import org.rsmod.api.script.onPlayerQueue
import org.rsmod.api.spells.MagicSpellRegistry
import org.rsmod.api.spells.attack.SpellAttackManager
import org.rsmod.api.spells.attack.SpellAttackMap
import org.rsmod.api.spells.attack.SpellAttackRepository
import org.rsmod.api.type.refs.proj.ProjAnimReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.api.type.refs.spot.SpotanimReferences
import org.rsmod.api.type.refs.synth.SynthReferences
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.PathingEntity
import org.rsmod.game.entity.Player
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.spot.SpotanimType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The combat spells the elemental module does not cover: Crumble Undead, Iban Blast, Magic Dart and
 * the three god spells. The runes, the staff, the level and the xp come from the cache, so a
 * missing Iban's staff or god staff fails the cast like any other missing rune.
 *
 * Note: Iban's staff charges, the Slayer staff (e) on-task Magic Dart bonus and the Mage
 * Arena's first-100-casts rule are not modelled.
 */
class CombatSpells @Inject constructor(private val objTypes: ObjTypeList) : SpellAttackMap {
    override fun SpellAttackRepository.register(manager: SpellAttackManager) {
        register(
            objs.spell_crumble_undead,
            TargetedSpell(
                manager,
                objTypes,
                SpellFx(
                    staffAnim = CombatSeqs.crumble_undead,
                    launch = CombatSpots.crumble_casting,
                    travel = CombatSpots.crumble_travel,
                    impact = CombatSpots.crumble_impact,
                    proj = CombatProjs.crumble_undead,
                    castSound = CombatSynths.crumble_cast,
                    hitSound = CombatSynths.crumble_hit,
                ),
                maxHit = { 15 },
                allowed = { target -> onlyUndead(target) },
            ),
        )
        register(
            objs.spell_iban_blast,
            TargetedSpell(
                manager,
                objTypes,
                SpellFx(
                    staffAnim = CombatSeqs.iban_blast,
                    launch = CombatSpots.iban_casting,
                    travel = CombatSpots.iban_travel,
                    impact = CombatSpots.iban_impact,
                    proj = CombatProjs.iban_blast,
                ),
                maxHit = { 25 },
            ),
        )
        register(
            objs.spell_magic_dart,
            TargetedSpell(
                manager,
                objTypes,
                SpellFx(
                    staffAnim = CombatSeqs.magic_dart,
                    travel = CombatSpots.dart_travel,
                    impact = CombatSpots.dart_impact,
                    hitSound = CombatSynths.dart_hit,
                ),
                maxHit = { DART_BASE + it.player.magicLvl / DART_LEVEL_DIVISOR },
                allowed = { enoughSlayer() },
            ),
        )
        godSpell(manager, objs.spell_saradomin_strike, CombatSpots.saradomin, saradominCapes) {
            target ->
            // Saradomin Strike drains a Prayer point; npcs have no Prayer to drain.
            if (target is Player) {
                target.statSub(stats.prayer, 1, 0)
            }
        }
        godSpell(manager, objs.spell_claws_of_guthix, CombatSpots.guthix, guthixCapes) { target ->
            drain(target, DrainStat.Defence, constant = 1, percent = 5)
        }
        godSpell(manager, objs.spell_flames_of_zamorak, CombatSpots.zamorak, zamorakCapes) { target
            ->
            drain(target, DrainStat.Magic, constant = 1, percent = 5)
        }
    }

    private fun SpellAttackRepository.godSpell(
        manager: SpellAttackManager,
        spell: ObjType,
        impact: SpotanimType,
        capes: List<ObjType>,
        effect: (PathingEntity) -> Unit,
    ) {
        register(
            spell,
            TargetedSpell(
                manager,
                objTypes,
                SpellFx(staffAnim = CombatSeqs.god_spell, impact = impact),
                maxHit = { if (it.charged(capes)) GOD_CHARGED_MAX_HIT else GOD_MAX_HIT },
                onHit = { target, _ -> effect(target) },
            ),
        )
    }

    private fun ProtectedAccess.onlyUndead(target: PathingEntity): Boolean {
        val undead = target is Npc && target.visType.param(params.undead) != 0
        if (!undead) {
            mes("This spell only affects skeletons, zombies, ghosts and shades.")
        }
        return undead
    }

    private fun ProtectedAccess.enoughSlayer(): Boolean {
        val enough = statBase(stats.slayer) >= DART_SLAYER_LEVEL
        if (!enough) {
            // UNVERIFIED: the wiki lists the Slayer requirement but no message.
            mes("You need a Slayer level of $DART_SLAYER_LEVEL to cast this spell.")
        }
        return enough
    }

    private companion object {
        const val GOD_MAX_HIT = 20
        const val GOD_CHARGED_MAX_HIT = 30
        const val DART_BASE = 10
        const val DART_LEVEL_DIVISOR = 10
        const val DART_SLAYER_LEVEL = 55

        val saradominCapes = listOf(UtilityObjs.saradomin_cape, UtilityObjs.imbued_saradomin_cape)
        val guthixCapes = listOf(UtilityObjs.guthix_cape, UtilityObjs.imbued_guthix_cape)
        val zamorakCapes = listOf(UtilityObjs.zamorak_cape, UtilityObjs.imbued_zamorak_cape)
    }
}

/** Charge: for [DURATION_TICKS] the god spells hit for 30 instead of 20, if the cape is worn. */
internal fun ProtectedAccess.charged(capes: List<ObjType>): Boolean =
    player.vars[UtilityVarps.charge_until] > mapClock && capes.any { it in player.worn }

private var Player.chargeUntil: Int by intVarp(UtilityVarps.charge_until)

class ChargeScript
@Inject
constructor(
    private val spells: MagicSpellRegistry,
    private val runes: MagicRuneManager,
    private val launcher: ProtectedAccessLauncher,
) : PluginScript() {
    override fun ScriptContext.startup() {
        val spell = checkNotNull(spells.getObjSpell(objs.spell_charge))
        onIfOverlayButton(spell.component) {
            launcher.launch(player) {
                // UNVERIFIED: the wiki gives a 100 tick cooldown but no message for casting early.
                if (player.chargeUntil - mapClock > DURATION_TICKS - COOLDOWN_TICKS) {
                    mes("You can only cast Charge once every 60 seconds.")
                    return@launch
                }
                if (runes.attemptCast(player, spell).isFailure()) {
                    return@launch
                }
                // UNVERIFIED: the cache names no Charge-specific animation or graphic.
                anim(UtilitySeqs.charge)
                statAdvance(stats.magic, spell.castXp)
                player.chargeUntil = mapClock + DURATION_TICKS
                queue(UtilityQueues.charge_end, DURATION_TICKS)
                mes("You feel charged with magic power.")
            }
        }
        onPlayerQueue(UtilityQueues.charge_end) {
            // The queue can fire a tick before the charge is over; a recast is much further out.
            if (player.chargeUntil - mapClock <= QUEUE_SLACK_TICKS) {
                mes("Your magical charge fades away.")
            }
        }
    }
}

private const val DURATION_TICKS = 700
private const val COOLDOWN_TICKS = 100
private const val QUEUE_SLACK_TICKS = 2

object CombatSeqs : SeqReferences() {
    val crumble_undead = find("human_castcrumbleundead")
    val iban_blast = find("human_castibanblast")
    val magic_dart = find("slayer_magicdart_cast")
    val god_spell = find("human_casting")
}

object CombatSpots : SpotanimReferences() {
    val crumble_casting = find("crumbleundead_casting")
    val crumble_travel = find("crumbleundead_travel")
    val crumble_impact = find("crumbleundead_impact")
    val iban_casting = find("ibanblast_casting")
    val iban_travel = find("ibanblast_travel")
    val iban_impact = find("ibanblast_impact")
    val dart_travel = find("slayer_magicdart_travel")
    val dart_impact = find("slayer_magicdart_impact")
    val saradomin = find("saradomin_lightning")
    val guthix = find("gunthix_claw")
    val zamorak = find("zamorak_flame")
}

object CombatSynths : SynthReferences() {
    val crumble_cast = find("crumble_cast_and_fire")
    val crumble_hit = find("crumble_hit")
    val dart_hit = find("magic_dart_hit")
}

object CombatProjs : ProjAnimReferences() {
    val crumble_undead = find("crumble_undead")
    val iban_blast = find("iban_blast")
}
