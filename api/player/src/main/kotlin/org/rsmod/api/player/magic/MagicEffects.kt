package org.rsmod.api.player.magic

import org.rsmod.api.config.refs.walktriggers
import org.rsmod.api.player.vars.VarPlayerIntMapSetter
import org.rsmod.api.type.builders.varp.VarpBuilder
import org.rsmod.api.type.refs.varp.VarpReferences
import org.rsmod.game.entity.Player

internal object MagicVarps : VarpReferences() {
    val freeze_until = find("magic_freeze_until")
    val freeze_immune_until = find("magic_freeze_immune_until")
    val vengeance_active = find("vengeance_active")
    val vengeance_cooldown_until = find("vengeance_cooldown_until")
}

internal object MagicVarpBuilder : VarpBuilder() {
    init {
        build("magic_freeze_until") { temporary = true }
        build("magic_freeze_immune_until") { temporary = true }
        build("vengeance_active") { temporary = true }
        build("vengeance_cooldown_until") { temporary = true }
    }
}

/** openscape: ticks of freeze immunity that start the moment a freeze ends. */
public const val FREEZE_IMMUNITY_TICKS: Int = 5

/** openscape: Vengeance can be recast this many ticks after the last cast (30 seconds). */
public const val VENGEANCE_COOLDOWN_TICKS: Int = 50

public fun Player.isFrozen(): Boolean = vars[MagicVarps.freeze_until] > currentMapClock

/** True while frozen and for [FREEZE_IMMUNITY_TICKS] after, so a freeze can't be chained. */
public fun Player.isFreezeImmune(): Boolean = vars[MagicVarps.freeze_immune_until] > currentMapClock

/**
 * Freezes this player for [ticks]. Does nothing (returns `false`) while already frozen or immune.
 * The walk itself is blocked by the `frozen` walk trigger (see the magic spell-attacks module).
 */
public fun Player.freeze(ticks: Int): Boolean {
    if (isFreezeImmune()) {
        return false
    }
    val until = currentMapClock + ticks
    VarPlayerIntMapSetter.set(this, MagicVarps.freeze_until, until)
    VarPlayerIntMapSetter.set(this, MagicVarps.freeze_immune_until, until + FREEZE_IMMUNITY_TICKS)
    walkTrigger(walktriggers.frozen)
    return true
}

public fun Player.hasVengeance(): Boolean = vars[MagicVarps.vengeance_active] != 0

public fun Player.setVengeance(active: Boolean) {
    VarPlayerIntMapSetter.set(this, MagicVarps.vengeance_active, if (active) 1 else 0)
}

public fun Player.isVengeanceOnCooldown(): Boolean =
    vars[MagicVarps.vengeance_cooldown_until] > currentMapClock

public fun Player.startVengeanceCooldown() {
    val until = currentMapClock + VENGEANCE_COOLDOWN_TICKS
    VarPlayerIntMapSetter.set(this, MagicVarps.vengeance_cooldown_until, until)
}
