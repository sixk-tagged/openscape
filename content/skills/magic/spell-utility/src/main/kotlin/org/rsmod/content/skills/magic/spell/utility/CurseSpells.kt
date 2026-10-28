package org.rsmod.content.skills.magic.spell.utility

import jakarta.inject.Inject
import kotlin.math.max
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.walktriggers
import org.rsmod.api.player.magic.freeze
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.stat
import org.rsmod.api.player.stat.statBase
import org.rsmod.api.player.stat.statDrain
import org.rsmod.api.script.onNpcWalkTrigger
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
import org.rsmod.game.type.stat.StatType
import org.rsmod.game.type.synth.SynthType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The curses (Confuse, Weaken, Curse, Vulnerability, Enfeeble, Stun) and the binds (Bind, Snare,
 * Entangle), on npcs and players. A curse lowers one stat by a percent of its base level and does
 * not stack until the stat is back at base; a bind holds the target in place. Both ride on the
 * normal magic accuracy roll, so a splash does nothing. The runes, levels and xp come from the
 * cache.
 *
 * Note: swampbark armour (+2 bind ticks per piece) and npcs immune to curses are not built.
 */
class CurseSpells @Inject constructor(private val objTypes: ObjTypeList) : SpellAttackMap {
    private class Curse(val obj: ObjType, val stat: DrainStat, val percent: Int, val fx: SpellFx)

    private class Bind(val obj: ObjType, val ticks: Int, val maxHit: Int, val fx: SpellFx)

    override fun SpellAttackRepository.register(manager: SpellAttackManager) {
        for (spell in CURSES) {
            val attack =
                TargetedSpell(
                    manager,
                    objTypes,
                    spell.fx,
                    onHit = { target, _ -> applyCurse(target, spell) },
                )
            register(spell.obj, attack)
        }
        for (spell in BINDS) {
            val attack =
                TargetedSpell(
                    manager,
                    objTypes,
                    spell.fx,
                    maxHit = { spell.maxHit },
                    onHit = { target, _ -> bind(target, spell.ticks) },
                )
            register(spell.obj, attack)
        }
    }

    private fun ProtectedAccess.applyCurse(target: PathingEntity, curse: Curse) {
        if (!drain(target, curse.stat, constant = 0, percent = curse.percent)) {
            val kind = if (target is Player) "player" else "monster"
            mes("The spell has no effect because the $kind has already been weakened.")
        }
    }

    private companion object {
        val CURSES =
            listOf(
                Curse(
                    objs.spell_confuse,
                    DrainStat.Attack,
                    5,
                    SpellFx(
                        staffAnim = CurseSeqs.confuse_staff,
                        unarmedAnim = CurseSeqs.confuse,
                        launch = CurseSpots.confuse_casting,
                        travel = CurseSpots.confuse_travel,
                        impact = CurseSpots.confuse_impact,
                        proj = CurseProjs.confuse,
                        castSound = CurseSynths.confuse_cast,
                        hitSound = CurseSynths.confuse_hit,
                    ),
                ),
                Curse(
                    objs.spell_weaken,
                    DrainStat.Strength,
                    5,
                    SpellFx(
                        staffAnim = CurseSeqs.weaken_staff,
                        unarmedAnim = CurseSeqs.weaken,
                        launch = CurseSpots.weaken_casting,
                        travel = CurseSpots.weaken_travel,
                        impact = CurseSpots.weaken_impact,
                        castSound = CurseSynths.weaken,
                    ),
                ),
                Curse(
                    objs.spell_curse,
                    DrainStat.Defence,
                    5,
                    SpellFx(
                        staffAnim = CurseSeqs.curse_staff,
                        unarmedAnim = CurseSeqs.curse,
                        launch = CurseSpots.curse_casting,
                        travel = CurseSpots.curse_travel,
                        impact = CurseSpots.curse_impact,
                        castSound = CurseSynths.curse_cast,
                        hitSound = CurseSynths.curse_hit,
                    ),
                ),
                Curse(
                    objs.spell_vulnerability,
                    DrainStat.Defence,
                    10,
                    SpellFx(
                        // UNVERIFIED: the Curse animation.
                        staffAnim = CurseSeqs.curse,
                        launch = CurseSpots.vulnerability_casting,
                        travel = CurseSpots.vulnerability_travel,
                        impact = CurseSpots.vulnerability_impact,
                        proj = CurseProjs.vulnerability,
                        castSound = CurseSynths.vulnerability,
                    ),
                ),
                Curse(
                    objs.spell_enfeeble,
                    DrainStat.Strength,
                    10,
                    SpellFx(
                        staffAnim = CurseSeqs.enfeeble,
                        launch = CurseSpots.enfeeble_casting,
                        travel = CurseSpots.enfeeble_travel,
                        impact = CurseSpots.enfeeble_impact,
                        proj = CurseProjs.enfeeble,
                        castSound = CurseSynths.enfeeble_cast,
                        hitSound = CurseSynths.enfeeble_hit,
                    ),
                ),
                Curse(
                    objs.spell_stun,
                    DrainStat.Attack,
                    10,
                    SpellFx(
                        staffAnim = CurseSeqs.stun,
                        launch = CurseSpots.stun_casting,
                        travel = CurseSpots.stun_travel,
                        impact = CurseSpots.stun_impact,
                        proj = CurseProjs.stun,
                        castSound = CurseSynths.stun,
                    ),
                ),
            )

        // Bind, Snare and Entangle share the cast and flight; only the impact and sounds differ.
        val BINDS =
            listOf(
                Bind(
                    objs.spell_bind,
                    ticks = 8,
                    maxHit = 0,
                    bindFx(CurseSpots.bind_impact, CurseSynths.bind_cast, CurseSynths.bind_hit),
                ),
                Bind(
                    objs.spell_snare,
                    ticks = 16,
                    maxHit = 3,
                    bindFx(CurseSpots.snare_impact, CurseSynths.snare, null),
                ),
                Bind(
                    objs.spell_entangle,
                    ticks = 24,
                    maxHit = 5,
                    bindFx(
                        CurseSpots.entangle_impact,
                        CurseSynths.entangle_cast,
                        CurseSynths.entangle_hit,
                    ),
                ),
            )

        fun bindFx(impact: SpotanimType, cast: SynthType, hit: SynthType?) =
            SpellFx(
                staffAnim = CurseSeqs.entangle_staff,
                unarmedAnim = CurseSeqs.entangle,
                launch = CurseSpots.entangle_casting,
                travel = CurseSpots.entangle_travel,
                impact = impact,
                proj = CurseProjs.bind,
                castSound = cast,
                hitSound = hit,
            )
    }
}

/** The stats a spell can lower on a target, for players and npcs alike. */
enum class DrainStat(val stat: StatType) {
    Attack(stats.attack),
    Strength(stats.strength),
    Defence(stats.defence),
    Magic(stats.magic);

    fun level(npc: Npc): Int =
        when (this) {
            Attack -> npc.attackLvl
            Strength -> npc.strengthLvl
            Defence -> npc.defenceLvl
            Magic -> npc.magicLvl
        }

    fun base(npc: Npc): Int =
        when (this) {
            Attack -> npc.baseAttackLvl
            Strength -> npc.baseStrengthLvl
            Defence -> npc.baseDefenceLvl
            Magic -> npc.baseMagicLvl
        }

    fun set(npc: Npc, level: Int) {
        when (this) {
            Attack -> npc.attackLvl = level
            Strength -> npc.strengthLvl = level
            Defence -> npc.defenceLvl = level
            Magic -> npc.magicLvl = level
        }
    }
}

/**
 * Lowers [stat] of [target] by [constant] plus [percent] of its base level, once: nothing happens
 * (and `false` is returned) while the stat is already below its base or the drain would be `0`.
 */
internal fun drain(target: PathingEntity, stat: DrainStat, constant: Int, percent: Int): Boolean =
    when (target) {
        is Player -> {
            val base = target.statBase(stat.stat)
            val worthIt = target.stat(stat.stat) >= base && constant + base * percent / 100 > 0
            if (worthIt) {
                target.statDrain(stat.stat, constant, percent)
            }
            worthIt
        }
        is Npc -> {
            val base = stat.base(target)
            val amount = constant + base * percent / 100
            val worthIt = stat.level(target) >= base && amount > 0
            if (worthIt) {
                stat.set(target, max(0, base - amount))
            }
            worthIt
        }
    }

/** Holds [target] in place for [ticks], unless it is immune from a bind that just ended. */
internal fun ProtectedAccess.bind(target: PathingEntity, ticks: Int): Boolean =
    when (target) {
        is Player -> {
            val frozen = target.freeze(ticks)
            if (frozen) {
                target.mes("You have been frozen!")
            }
            frozen
        }
        is Npc -> {
            val now = mapClock
            val immune =
                target.vars[UtilityVarns.bind_immune_until] > now ||
                    target.type.param(params.freeze_resistance) != 0
            if (!immune) {
                target.vars[UtilityVarns.bind_until] = now + ticks
                target.vars[UtilityVarns.bind_immune_until] = now + ticks + BIND_IMMUNITY_TICKS
                target.abortRoute()
                target.walkTrigger(walktriggers.frozen)
            }
            !immune
        }
    }

// UNVERIFIED: players get 5 ticks of freeze immunity after a freeze, npcs are assumed to match.
private const val BIND_IMMUNITY_TICKS = 5

/** Stops a bound npc from walking: the `frozen` walk trigger is re-armed until the bind ends. */
class BoundNpcScript : PluginScript() {
    override fun ScriptContext.startup() {
        onNpcWalkTrigger(walktriggers.frozen) {
            if (npc.vars[UtilityVarns.bind_until] > npc.currentMapClock) {
                npc.routeDestination.clear()
                npc.walkTrigger(walktriggers.frozen)
            }
        }
    }
}

object CurseSeqs : SeqReferences() {
    val confuse = find("human_castconfuse")
    val confuse_staff = find("human_castconfuse_staff")
    val weaken = find("human_castweaken")
    val weaken_staff = find("human_castweaken_staff")
    val curse = find("human_castcurse")
    val curse_staff = find("human_castcurse_staff")
    val enfeeble = find("human_castenfeeble")
    val stun = find("human_caststun")
    val entangle = find("human_castentangle")
    val entangle_staff = find("human_castentangle_staff")
}

object CurseSpots : SpotanimReferences() {
    val confuse_casting = find("confuse_casting")
    val confuse_travel = find("confuse_travel")
    val confuse_impact = find("confuse_impact")
    val weaken_casting = find("weaken_casting")
    val weaken_travel = find("weaken_travel")
    val weaken_impact = find("weaken_impact")
    val curse_casting = find("curse_casting")
    val curse_travel = find("curse_travel")
    val curse_impact = find("curse_impact")
    val vulnerability_casting = find("vulnerability_casting")
    val vulnerability_travel = find("vulnerability_travel")
    val vulnerability_impact = find("vulnerability_impact")
    val enfeeble_casting = find("enfeeble_casting")
    val enfeeble_travel = find("enfeeble_travel")
    val enfeeble_impact = find("enfeeble_impact")
    val stun_casting = find("stun_casting")
    val stun_travel = find("stun_travel")
    val stun_impact = find("stun_impact")
    val entangle_casting = find("entangle_casting")
    val entangle_travel = find("entangle_travel")
    val entangle_impact = find("entangle_impact")
    val snare_impact = find("snare_impact")
    val bind_impact = find("bind_impact")
}

object CurseSynths : SynthReferences() {
    val confuse_cast = find("confuse_cast_and_fire")
    val confuse_hit = find("confuse_hit")
    val weaken = find("weaken_all")
    val curse_cast = find("curse_cast_and_fire")
    val curse_hit = find("curse_hit")
    val vulnerability = find("vulnerability_all")
    val enfeeble_cast = find("enfeeble_cast_and_fire")
    val enfeeble_hit = find("enfeeble_hit")
    val stun = find("stun_all")
    val bind_cast = find("bind_cast")
    val bind_hit = find("bind_impact")
    val snare = find("snare_all")
    val entangle_cast = find("entangle_cast_and_fire")
    val entangle_hit = find("entangle_hit")
}

object CurseProjs : ProjAnimReferences() {
    val confuse = find("confuse")
    val vulnerability = find("vulnerability")
    val enfeeble = find("enfeeble")
    val stun = find("stun")
    val bind = find("bind")
}
