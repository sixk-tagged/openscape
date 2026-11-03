package org.rsmod.api.combat

import org.rsmod.api.combat.player.pkPredator1
import org.rsmod.api.combat.player.pkPredator2
import org.rsmod.api.combat.player.pkPredator3
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.varps
import org.rsmod.api.player.isInPvpCombat
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.script.onPlayerLogin
import org.rsmod.api.script.onPlayerSoftTimer
import org.rsmod.api.type.refs.timer.TimerReferences
import org.rsmod.game.entity.Player
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** How long a skull lasts after the player last started a fight: 30 minutes. */
public const val SKULL_TICKS: Int = 3000

private var Player.skullTicks: Int by intVarp(varps.skull_ticks)

internal object SkullTimers : TimerReferences() {
    val skull = find("wild_skull")
}

/** Skulls the player and restarts the 30 minute clock. */
public fun Player.skull() {
    val fresh = skullIcon != constants.skullicon_default
    skullIcon = constants.skullicon_default
    skullTicks = SKULL_TICKS
    softTimer(SkullTimers.skull, 1)
    if (fresh) {
        rebuildAppearance()
    }
}

/**
 * Skulls the attacker for starting a fight with [target]. A player fighting back against someone
 * who attacked them first (one of their last three attackers) is never skulled.
 */
public fun ProtectedAccess.skullUnlessRetaliating(target: Player) {
    val attacker = target.uid
    val retaliating =
        attacker == player.pkPredator1 ||
            attacker == player.pkPredator2 ||
            attacker == player.pkPredator3
    if (!retaliating) {
        player.skull()
    }
}

/**
 * The skull's clock. It runs only while logged in, and is saved in `skull_ticks` so a skull is
 * still there, with the time it had left, at the next login.
 */
public class SkullScript : PluginScript() {
    override fun ScriptContext.startup() {
        onPlayerLogin { player.resumeSkull() }
        onPlayerSoftTimer(SkullTimers.skull) { player.tickSkull() }
    }

    private fun Player.resumeSkull() {
        if (skullTicks <= 0) {
            return
        }
        skullIcon = constants.skullicon_default
        rebuildAppearance()
        softTimer(SkullTimers.skull, 1)
    }

    private fun Player.tickSkull() {
        val left = skullTicks
        if (left <= 0) {
            clearSoftTimer(SkullTimers.skull)
            return
        }
        // A skull cannot wear off in the middle of a fight.
        if (left == 1 && isInPvpCombat()) {
            return
        }
        skullTicks = left - 1
        if (left == 1) {
            skullIcon = null
            rebuildAppearance()
            clearSoftTimer(SkullTimers.skull)
        }
    }
}
