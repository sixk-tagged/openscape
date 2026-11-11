package org.rsmod.content.areas.city.lumbridge.npcs

import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.boolVarp
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.type.builders.varp.VarpBuilder
import org.rsmod.api.type.refs.varp.VarpReferences
import org.rsmod.content.areas.city.lumbridge.configs.lumbridge_npcs
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Player
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

object CountCheckVarps : VarpReferences() {
    val sos_teleport = find("count_check_sos_teleport")
}

internal object CountCheckVarpBuilder : VarpBuilder() {
    init {
        build("count_check_sos_teleport") { permanent = true }
    }
}

// There is no Stronghold of Security yet, so "visited it" means "took the Count's teleport".
private var Player.visitedStronghold by boolVarp(CountCheckVarps.sos_teleport)

// Note: no Jagex Accounts or bank PINs exist here, so every check fails the OSRS way and the
// lightning strike on the Count's square is skipped.
class CountCheck : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(lumbridge_npcs.count_check) { startDialogue(it.npc) }
    }

    private suspend fun ProtectedAccess.startDialogue(npc: Npc) {
        startDialogue(npc) {
            chatNpc(
                laugh,
                "Ahahahaha! I am Count Check, the renowned security expert. Would you like me " +
                    "to check your account to see if it is secure?",
            )
            mainMenu()
        }
    }

    private suspend fun Dialogue.mainMenu() {
        val choice =
            choice5(
                "Check my account, Count Check!",
                1,
                "Can you give me any advice?",
                2,
                "Where can I learn more about security?",
                3,
                "Who are you?",
                4,
                "I'll see you another time.",
                5,
            )
        when (choice) {
            1 -> checkAccount()
            2 -> advice()
            3 -> learnMore()
            4 -> whoAreYou()
            5 -> chatPlayer(neutral, "I'll see you another time.")
        }
    }

    private suspend fun Dialogue.checkAccount() {
        chatPlayer(happy, "Check my account, Count Check!")
        // "so so" is the game's own typo.
        chatNpc(
            angry,
            "You do not have a Jagex Account, nor a Bank PIN, so so you fail my checks! Please " +
                "upgrade before we meet again.",
        )
    }

    private suspend fun Dialogue.advice() {
        chatPlayer(quiz, "Can you give me any advice?")
        chatNpc(happy, "I can! Count with the Count as I count the steps to securing your account:")
        chatNpc(happy, "ONE... unique password!")
        chatNpc(
            neutral,
            "Never use the same password for different accounts, or on different websites. If " +
                "it got leaked, all of your accounts would become vulnerable at once!",
        )
        chatNpc(happy, "TWO... step verification for your email account!")
        chatNpc(
            neutral,
            "Good email providers let you set up two-step verification, maybe by generating " +
                "security codes on a second device, or by sending codes to your phone. Make " +
                "sure you've enabled this.",
        )
        chatNpc(happy, "THREE... Um... Use a Jagex Account.")
        chatNpc(neutral, "Visit the RuneScape website and log in to update your Account settings.")
        if (choice2("Carry on.", false, "What's that got to do with THREE?", true)) {
            chatPlayer(quiz, "What's that got to do with THREE?")
            chatNpc(
                angry,
                "Look, I'd got something for ONE, TWO and FOUR, and I like counting, okay? " +
                    "Don't interrupt my flow!",
            )
            chatPlayer(neutral, "...")
        } else {
            chatPlayer(neutral, "Carry on.")
        }
        chatNpc(happy, "FOUR... digit Bank PIN!")
        chatNpc(
            neutral,
            "Next time you visit a bank, talk to a banker and ask to check your PIN settings.",
        )
        chatNpc(madlaugh, "Muhahahahhaa! Such fun it is to count!")
        mainMenu()
    }

    private suspend fun Dialogue.learnMore() {
        chatPlayer(quiz, "Where can I learn more about security?")
        if (player.visitedStronghold) {
            chatNpc(
                happy,
                "The Stronghold of Security, in Barbarian Village. There is much to learn there. " +
                    "But while you are here, may I check your account for you?",
            )
            mainMenu()
            return
        }
        chatNpc(
            happy,
            "The Stronghold of Security, in Barbarian Village. I see you have not visited it. " +
                "Would you like to? I can send you straight there - but only once.",
        )
        if (choice2("Yes", true, "No", false, title = "Teleport to the Stronghold of Security?")) {
            player.visitedStronghold = true
            access.telejump(STRONGHOLD_ENTRANCE)
            return
        }
        chatNpc(happy, "So may I check your account for you?")
        mainMenu()
    }

    private suspend fun Dialogue.whoAreYou() {
        chatPlayer(quiz, "Who are you?")
        chatNpc(
            neutral,
            "As you may see, I am a vampyre, from Morytania. I left there when I heard the " +
                "terrible news and now I choose to spend my days warning others of the perils " +
                "that lay in wait for them.",
        )
        chatPlayer(confused, "The perils?")
        chatNpc(
            worried,
            "Yes, be warned! The travellers of these lands face danger from outside. Accounts " +
                "can be stolen, and when that happens one can lose everything one has worked " +
                "for.",
        )
        chatNpc(
            neutral,
            "I've left my vampyric ways behind me, and now I spend my days letting everyone " +
                "know of this terrible danger.",
        )
        chatNpc(
            happy,
            "In return for my dedication, I have been blessed with the ability to check if " +
                "adventurers are ready to defend themselves against these account thieves.",
        )
        // The Priest in Peril question isn't offered: quests aren't tracked.
        mainMenu()
    }

    private companion object {
        /** Just north of the entrance in Barbarian Village (3081,3420). */
        val STRONGHOLD_ENTRANCE = CoordGrid(3081, 3421, 0)
    }
}
