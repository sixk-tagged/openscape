package org.rsmod.api.combat.commons.poison

import kotlin.math.max
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.timers
import org.rsmod.api.npc.vars.intVarn
import org.rsmod.api.npc.vars.typePlayerUidVarn
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.type.builders.varn.VarnBuilder
import org.rsmod.api.type.refs.varn.VarnReferences
import org.rsmod.api.type.refs.varp.VarpReferences
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Player
import org.rsmod.game.entity.player.PlayerUid

/**
 * Poison works on a severity: each poison clock ([INTERVAL] ticks) hits for [damage] and lowers the
 * severity by 1, until it is 0. A poison that starts at N damage is severity `N * 5`.
 */
public object Poison {
    public const val INTERVAL: Int = 30

    /** Ranged and thrown poison starts this much lower than melee poison. */
    public const val RANGED_REDUCTION: Int = 14

    /** Ticks without combat after which a poisoned npc is no longer poisoned. */
    public const val NPC_OUT_OF_COMBAT: Int = 90

    public fun damage(severity: Int): Int = (severity + 4) / 5

    public fun severity(startDamage: Int): Int = startDamage * 5
}

internal object PoisonVarps : VarpReferences() {
    /** The cache varp the hitpoints orb reads. */
    val poison = find("poison")
}

internal object PoisonVarns : VarnReferences() {
    val severity = find("poison_severity")
    val poisoned_by = find("poisoned_by")
}

internal object PoisonVarnBuilder : VarnBuilder() {
    init {
        build("poison_severity")
        build("poisoned_by")
    }
}

/**
 * A player's poison, in the cache varp `poison` (it persists and drives the hitpoints orb): `1+` is
 * the severity of the poison, `-1` and below is the antipoison immunity left, in poison clocks, and
 * `0` is neither. The `toxins` timer runs while it is not 0.
 */
public var Player.poisonState: Int by intVarp(PoisonVarps.poison)

public val Player.isPoisoned: Boolean
    get() = poisonState > 0

public val Player.isPoisonImmune: Boolean
    get() = poisonState < 0

/** Poisons this player, replacing any poison they have. Immune players are left alone. */
public fun Player.poison(severity: Int): Boolean {
    val state = poisonState
    if (state < 0) {
        return false
    }
    poisonState = severity
    if (state == 0) {
        timer(timers.toxins, Poison.INTERVAL)
        mes("You have been poisoned.")
    }
    return true
}

public fun Player.curePoison() {
    if (poisonState > 0) {
        poisonState = 0
    }
}

/** Cures this player's poison and makes them immune for [clocks] poison clocks. */
public fun Player.antipoison(clocks: Int) {
    val state = poisonState
    poisonState = -max(clocks, -state)
    if (state == 0) {
        timer(timers.toxins, Poison.INTERVAL)
    }
}

/** Restarts the poison clock of a player who logged in poisoned or immune. */
public fun Player.resumePoison() {
    if (poisonState != 0) {
        timer(timers.toxins, Poison.INTERVAL)
    }
}

/** An npc's poison severity (`0` is not poisoned). The `toxins` timer runs while it is above 0. */
public var Npc.poisonSeverity: Int by intVarn(PoisonVarns.severity)

/** The player credited with the npc's poison damage, so they get the drops if it kills it. */
public var Npc.poisonedBy: PlayerUid? by typePlayerUidVarn(PoisonVarns.poisoned_by)

public val Npc.isPoisonImmune: Boolean
    get() = visType.param(params.poison_immunity) != 0

public fun Npc.poison(severity: Int, source: Player?): Boolean {
    if (isPoisonImmune) {
        return false
    }
    val wasPoisoned = poisonSeverity > 0
    poisonSeverity = severity
    poisonedBy = source?.uid
    if (!wasPoisoned) {
        timer(timers.toxins, Poison.INTERVAL)
    }
    return true
}
