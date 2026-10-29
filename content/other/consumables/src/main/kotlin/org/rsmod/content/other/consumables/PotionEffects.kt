package org.rsmod.content.other.consumables

import jakarta.inject.Inject
import kotlin.math.min
import org.rsmod.api.combat.commons.npc.queueCombatRetaliate
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.timers
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.hunt.NpcSearch
import org.rsmod.api.npc.isOutOfCombat
import org.rsmod.api.npc.isValidTarget
import org.rsmod.api.player.isInCombat
import org.rsmod.api.player.output.UpdateRun
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.stat.stat
import org.rsmod.api.player.stat.statBase
import org.rsmod.api.player.stat.statHeal
import org.rsmod.api.player.stat.statSub
import org.rsmod.api.player.vars.intVarBit
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.script.onPlayerSoftTimer
import org.rsmod.content.other.consumables.configs.PotionTimers
import org.rsmod.content.other.consumables.configs.PotionVarps
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Player
import org.rsmod.game.type.hunt.HuntVis
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** Which antifire potion is active, for the dragonfire code to read. */
object Antifire {
    const val NONE = 0
    const val NORMAL = 1
    const val EXTENDED = 2
    const val SUPER = 3
    const val EXTENDED_SUPER = 4
}

private var Player.staminaActive by intVarBit(varbits.stamina_active)
private var Player.staminaUntil by intVarp(PotionVarps.stamina_until)
private var Player.antifireUntil by intVarp(PotionVarps.antifire_until)
private var Player.regenUntil by intVarp(PotionVarps.regen_until)
private var Player.remedyUntil by intVarp(PotionVarps.remedy_until)
private var Player.divineUntil by intVarp(PotionVarps.divine_until)
private var Player.goadingUntil by intVarp(PotionVarps.goading_until)

/** Absolute map clock cycle at which the next surge potion may be drunk. */
internal var Player.surgeReady by intVarp(PotionVarps.surge_ready)

/** One of the [Antifire] kinds. */
var Player.antifireKind by intVarp(PotionVarps.antifire_kind)
    internal set

private const val STAMINA_TICKS = 200
private const val REGEN_TICKS = 4800
private const val REGEN_INTERVAL = 12
private const val REMEDY_TICKS = 3000
private const val REMEDY_INTERVAL = 25
private const val DIVINE_TICKS = 3000
private const val ANTIFIRE_WARNING = 25
private const val GOADING_TICKS = 600
private const val GOADING_INTERVAL = 6
private const val GOADING_RANGE = 4

/** The index of a monster's `Attack` option (op 2). */
private const val ATTACK_OP = 1

/** The combat stats the remedy restores and a divine potion's end resets. */
private val Player.combatStats
    get() = listOf(stats.attack, stats.strength, stats.defence, stats.ranged, stats.magic)

internal fun Player.restoreRunEnergy(percent: Int) {
    runEnergy = min(constants.run_max_energy, runEnergy + constants.run_max_energy * percent / 100)
    UpdateRun.energy(this, runEnergy)
}

internal fun Player.startStamina() {
    staminaActive = 1
    staminaUntil = currentMapClock + STAMINA_TICKS
    effectsTick()
}

internal fun Player.startAntifire(kind: Int, ticks: Int) {
    antifireKind = kind
    antifireUntil = currentMapClock + ticks
    effectsTick()
}

internal fun Player.startPrayerRegeneration() {
    regenUntil = currentMapClock + REGEN_TICKS
    effectsTick()
}

internal fun Player.startRemedy() {
    remedyUntil = currentMapClock + REMEDY_TICKS
    effectsTick()
}

/** Divine potions hold the boost for 5 minutes: no stat decay, then back to base. */
internal fun Player.startDivine() {
    clearSoftTimer(timers.stat_boost_restore)
    divineUntil = currentMapClock + DIVINE_TICKS
    effectsTick()
}

/** Goading checks for monsters from the tick after the sip, then every [GOADING_INTERVAL]. */
internal fun Player.startGoading() {
    goadingUntil = currentMapClock + GOADING_TICKS
    effectsTick()
}

/** Teleporting ends goading early. */
fun Player.endGoading() {
    if (goadingUntil == 0) {
        return
    }
    goadingUntil = 0
    // UNVERIFIED: the wiki says a message is shown when goading ends early, not what it says.
    mes("Your goading potion has worn off.")
}

private fun Player.effectsTick() {
    softTimer(PotionTimers.effects, 1)
}

class PotionEffects @Inject constructor(private val npcSearch: NpcSearch) : PluginScript() {
    override fun ScriptContext.startup() {
        onPlayerSoftTimer(PotionTimers.effects) { player.tickEffects() }
    }

    private fun Player.tickEffects() {
        val now = currentMapClock
        var active = false
        if (staminaUntil != 0) {
            if (now >= staminaUntil) {
                staminaActive = 0
                staminaUntil = 0
            } else {
                active = true
            }
        }
        if (antifireUntil != 0) {
            active = tickAntifire(now) || active
        }
        if (regenUntil != 0) {
            active = tickRegeneration(now) || active
        }
        if (remedyUntil != 0) {
            active = tickRemedy(now) || active
        }
        if (divineUntil != 0) {
            active = tickDivine(now) || active
        }
        if (goadingUntil != 0) {
            active = tickGoading(now) || active
        }
        if (!active) {
            clearSoftTimer(PotionTimers.effects)
        }
    }

    private fun Player.tickAntifire(now: Int): Boolean {
        val left = antifireUntil - now
        if (left <= 0) {
            antifireUntil = 0
            antifireKind = Antifire.NONE
            mes("Your antifire potion has expired.")
            return false
        }
        if (left == ANTIFIRE_WARNING) {
            mes("Your antifire potion is about to expire.")
        }
        return true
    }

    private fun Player.tickRegeneration(now: Int): Boolean {
        val left = regenUntil - now
        if (left <= 0) {
            regenUntil = 0
            return false
        }
        if (left % REGEN_INTERVAL == 0) {
            statHeal(stats.prayer, 1, 0)
        }
        return true
    }

    private fun Player.tickRemedy(now: Int): Boolean {
        val left = remedyUntil - now
        if (left <= 0) {
            remedyUntil = 0
            return false
        }
        if (left % REMEDY_INTERVAL == 0) {
            for (stat in combatStats) {
                statHeal(stat, 6, 16)
            }
        }
        return true
    }

    private fun Player.tickGoading(now: Int): Boolean {
        val left = goadingUntil - now
        if (left <= 0) {
            goadingUntil = 0
            return false
        }
        if (left % GOADING_INTERVAL == GOADING_INTERVAL - 1) {
            goad()
        }
        return true
    }

    /**
     * Makes the monsters within 4 tiles and in line of sight attack. In single-way combat only the
     * nearest one does, and only when the player is out of combat.
     */
    private fun Player.goad() {
        val multi = vars[varbits.multiway_indicator] == 1
        if (!multi && isInCombat()) {
            return
        }
        val targets =
            npcSearch.findAllAny(coords, GOADING_RANGE, HuntVis.LineOfSight).filter {
                it.canBeGoaded(this)
            }
        if (multi) {
            targets.forEach { it.queueCombatRetaliate(this) }
        } else {
            // UNVERIFIED: the wiki doesn't say which one in single-way combat; the nearest here.
            targets.minByOrNull { coords.chebyshevDistance(it.coords) }?.queueCombatRetaliate(this)
        }
    }

    /** Monsters already fighting, and those past the player's Slayer level, are left alone. */
    private fun Npc.canBeGoaded(player: Player): Boolean =
        isValidTarget() &&
            visType.op.getOrNull(ATTACK_OP) == "Attack" &&
            isOutOfCombat() &&
            player.stat(stats.slayer) >= visType.param(params.slayer_levelrequire)

    private fun Player.tickDivine(now: Int): Boolean {
        if (now < divineUntil) {
            return true
        }
        divineUntil = 0
        for (stat in combatStats) {
            val excess = stat(stat) - statBase(stat)
            if (excess > 0) {
                statSub(stat, excess, 0)
            }
        }
        softTimer(timers.stat_boost_restore, constants.stat_boost_restore_interval)
        return false
    }
}
