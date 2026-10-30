package org.rsmod.content.areas.city.portsarim.npcs

import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.content.areas.city.portsarim.configs.port_sarim_npcs
import org.rsmod.content.areas.city.portsarim.configs.port_sarim_objs
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The old ferry between Port Sarim and Musa Point: 30 coins each way. The sailors at Port Sarim
 * just ask for the fare; the customs officer at Musa Point searches the player for Karamjan rum
 * first, and takes it. The other boats (Entrana, Ardougne, Brimhaven, ...) leave the map.
 */
class PortSarimBoats : PluginScript() {
    override fun ScriptContext.startup() {
        for (sailor in
            listOf(port_sarim_npcs.tobias, port_sarim_npcs.lorris, port_sarim_npcs.thresnor)) {
            onOpNpc1(sailor) { startDialogue(it.npc) { sailorTalk() } }
            onOpNpc3(sailor) {
                if (payFare()) {
                    sail(MUSA_POINT, "The ship arrives at Musa Point.")
                }
            }
        }
        onOpNpc1(port_sarim_npcs.customs_officer) { startDialogue(it.npc) { customsTalk() } }
        onOpNpc3(port_sarim_npcs.customs_officer) {
            if (confiscateRum()) {
                mes("The customs officer confiscates your rum.")
                mes(SMUGGLE_HINT)
            } else if (payFare()) {
                sail(PORT_SARIM, "The ship arrives at Port Sarim.")
            }
        }
    }

    private suspend fun Dialogue.sailorTalk() {
        chatNpc(happy, "Do you want to go on a trip to Karamja?")
        chatNpc(neutral, "The trip will cost you 30 coins.")
        val choice = choice2("Yes please.", 1, "No, thank you.", 2)
        if (choice == 2) {
            chatPlayer(neutral, "No, thank you.")
            return
        }
        chatPlayer(happy, "Yes please.")
        if (access.payFare()) {
            access.sail(MUSA_POINT, "The ship arrives at Musa Point.")
        }
    }

    // The Pirate's Treasure quest counts as done, so she knows the player from the plantation.
    private suspend fun Dialogue.customsTalk() {
        chatNpc(neutral, "Can I help you?")
        val choice =
            choice2("Can I journey on this ship?", 1, "Does Karamja have unusual customs then?", 2)
        if (choice == 2) {
            chatPlayer(quiz, "Does Karamja have unusual customs then?")
            chatNpc(neutral, "I'm not that sort of customs officer.")
            return
        }
        chatPlayer(quiz, "Can I journey on this ship?")
        if (access.invTotal(access.inv, port_sarim_objs.karamja_rum) > 0) {
            chatNpc(neutral, "Spot inspection. You don't mind do you?")
            chatNpc(angry, "Aha, trying to smuggle rum are we?")
            chatPlayer(shifty, "Umm... it's for personal use?")
            access.confiscateRum()
            mesbox("The customs officer confiscates your rum.")
            access.mes(SMUGGLE_HINT)
            return
        }
        chatNpc(
            neutral,
            "Hey, I know you, you work at the plantation. I don't think you'll try smuggling " +
                "anything, you just need to pay a boarding charge of 30 coins.",
        )
        val pay = choice2("Ok.", 1, "Oh, I'll not bother then.", 2)
        if (pay == 2) {
            chatPlayer(neutral, "Oh, I'll not bother then.")
            return
        }
        chatPlayer(neutral, "Ok.")
        if (access.payFare()) {
            access.sail(PORT_SARIM, "The ship arrives at Port Sarim.")
        }
    }

    /** Takes the 30 coin fare, or says why the player can't board. */
    private fun ProtectedAccess.payFare(): Boolean {
        if (!invTakeFee(FARE)) {
            mes("You do not have enough coins to pay passage, you need $FARE.")
            return false
        }
        mes("You pay $FARE coins and board the ship.")
        return true
    }

    /** True if the player had any Karamjan rum on them; the customs officer keeps it all. */
    private fun ProtectedAccess.confiscateRum(): Boolean {
        val rum = invTotal(inv, port_sarim_objs.karamja_rum)
        if (rum == 0) {
            return false
        }
        invDel(inv, port_sarim_objs.karamja_rum, rum)
        return true
    }

    private suspend fun ProtectedAccess.sail(dest: CoordGrid, arrival: String) {
        ifClose()
        fadeOverlay(
            startColour = 0,
            startTransparency = 0,
            endColour = 0,
            endTransparency = 255,
            clientDuration = FADE,
        )
        delay(3)
        telejump(dest)
        delay(2)
        fadeOverlay(
            startColour = 0,
            startTransparency = 255,
            endColour = 0,
            endTransparency = 0,
            clientDuration = FADE,
        )
        delay(2)
        closeFadeOverlay()
        mes(arrival)
    }

    private companion object {
        const val FARE = 30
        const val FADE = 50
        const val SMUGGLE_HINT = "You will need to find some way to smuggle it off the island..."

        // UNVERIFIED: the wiki gives no landing tiles. These are the docks beside the gangplanks.
        val MUSA_POINT = CoordGrid(2956, 3146, 0)
        val PORT_SARIM = CoordGrid(3029, 3217, 0)
    }
}
