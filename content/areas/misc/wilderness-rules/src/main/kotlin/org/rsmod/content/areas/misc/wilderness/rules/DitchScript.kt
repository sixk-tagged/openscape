package org.rsmod.content.areas.misc.wilderness.rules

import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.boolVarBit
import org.rsmod.api.script.onOpLoc1
import org.rsmod.game.entity.Player
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The Wilderness Ditch: "Cross" jumps over it, three tiles either way (it fills two, and the two
 * safe steps start right behind it). Only going in asks first, until the player ticks "don't ask me
 * again".
 *
 * Note: the player is teleported after the jump animation; OSRS moves them over the ditch. The
 * warning is a chatbox dialogue: the cache's `wilderness_warningscreen` is empty layers that a
 * client script fills in, and nothing says what that script reads.
 */
class DitchScript : PluginScript() {
    private var Player.warningOff by boolVarBit(WildernessVarBits.ditch_warning_off)

    override fun ScriptContext.startup() {
        onOpLoc1(WildernessLocs.ditch) { cross() }
        onOpLoc1(WildernessLocs.ditch_members) { cross() }
    }

    private suspend fun ProtectedAccess.cross() {
        val entering = coords.z <= SOUTH_BANK
        if (entering && !player.warningOff && !confirmEntry()) {
            return
        }
        jump(entering)
    }

    /** Asks whether to go in. Choosing to not be asked again is saved. */
    private suspend fun ProtectedAccess.confirmEntry(): Boolean {
        // UNVERIFIED: the wiki does not give the warning's wording.
        mesbox(WARNING)
        val answer =
            choice3(
                "Enter the Wilderness.",
                ENTER,
                "Enter, and don't ask me again.",
                ENTER_AND_TICK,
                "Come back later.",
                STAY,
            )
        if (answer == ENTER_AND_TICK) {
            player.warningOff = true
        }
        return answer != STAY
    }

    private suspend fun ProtectedAccess.jump(entering: Boolean) {
        val dest = coords.translateZ(if (entering) JUMP else -JUMP)
        faceSquare(dest)
        anim(WildernessSeqs.ditch_jump)
        delay(2)
        telejump(dest)
        resetAnim()
    }

    private companion object {
        /** The row just south of the ditch; the row just north of it is [JUMP] tiles away. */
        const val SOUTH_BANK = 3520
        const val JUMP = 3

        const val STAY = 0
        const val ENTER = 1
        const val ENTER_AND_TICK = 2

        const val WARNING =
            "<col=ff0000>Warning!</col><br>The Wilderness is a dangerous area where other " +
                "players can attack you and you can lose all your items."
    }
}
