package org.rsmod.content.other.deathsoffice

import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.script.onOpLoc1
import org.rsmod.content.other.deathsoffice.configs.DeathLocs
import org.rsmod.content.other.deathsoffice.configs.DeathNpcs
import org.rsmod.content.other.deathsoffice.configs.DeathSeqs
import org.rsmod.content.other.deathsoffice.configs.DeathVarps
import org.rsmod.game.entity.Player
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

internal var Player.officeReturn by intVarp(DeathVarps.death_office_return)

/**
 * The ways into Death's Office (Lumbridge's grave, Falador's crypt coffin) and its exit portal,
 * which leads back to where the player came in.
 */
class DeathsOfficeScript : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1(DeathLocs.lumbridge_grave) { enterOffice(LUMBRIDGE_RETURN) }
        onOpLoc1(DeathLocs.falador_coffin) { enterOffice(FALADOR_RETURN) }
        onOpLoc1(DeathLocs.exit_portal) { leaveOffice() }
    }

    private suspend fun ProtectedAccess.enterOffice(back: CoordGrid) {
        arriveDelay()
        player.officeReturn = back.packed
        telejump(OFFICE_ARRIVAL)
    }

    private suspend fun ProtectedAccess.leaveOffice() {
        if (player.inDeathTutorial) {
            startDialogue { tutorialNotFinished() }
            return
        }
        arriveDelay()
        anim(DeathSeqs.portal_exit)
        delay(2)
        val back = player.officeReturn
        telejump(if (back == 0) LUMBRIDGE_RETURN else CoordGrid(back))
        // Passing through the portal is what starts the grave's 15 minutes.
        player.deathTutorial = player.deathTutorial and GravestoneScript.TUTORIAL_ACTIVE.inv()
    }

    private suspend fun Dialogue.tutorialNotFinished() {
        chatNpcSpecific(
            "Death",
            DeathNpcs.death,
            neutral,
            "I haven't finished talking to you yet, ${player.displayName}. Please don't worry " +
                "about your gravestone - it will wait until you leave this place.",
        )
    }

    companion object {
        // Note: the wiki gives no tiles or portal timing. Arrival is on the portal's only open
        // (east) side, the return on the open tile east of the fenced-in Lumbridge grave, and
        // delay(2) is a guess at the exit anim.
        val OFFICE_ARRIVAL = CoordGrid(3171, 5726)
        val LUMBRIDGE_RETURN = CoordGrid(3239, 3192)
        // UNVERIFIED: the open tile west of the crypt's coffin.
        val FALADOR_RETURN = CoordGrid(2976, 9736)
    }
}
