package org.rsmod.content.areas.city.lumbridge.npcs

import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpNpc1
import org.rsmod.content.areas.city.lumbridge.configs.lumbridge_npcs
import org.rsmod.game.entity.Npc
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

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

    // Note: "Check my account, Count Check!" (Jagex Account + PIN check, one-time lamp) is
    // skipped; there are no Jagex Accounts here.
    private suspend fun Dialogue.mainMenu() {
        val choice =
            choice4(
                "Can you give me any advice?",
                1,
                "Where can I learn more about security?",
                2,
                "Who are you?",
                3,
                "I'll see you another time.",
                4,
            )
        when (choice) {
            1 -> advice()
            2 -> learnMore()
            3 -> whoAreYou()
            4 -> chatPlayer(neutral, "I'll see you another time.")
        }
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
        // Note: no bank PIN state is read; always the "not set" line.
        chatNpc(happy, "FOUR... digit Bank PIN!")
        chatNpc(
            neutral,
            "Next time you visit a bank, talk to a banker and ask to check your PIN settings.",
        )
        // Note: the lightning strike on the Count's square is skipped.
        chatNpc(madlaugh, "Muhahahahhaa! Such fun it is to count!")
        mainMenu()
    }

    // Note: the one-time Stronghold of Security teleport is skipped.
    private suspend fun Dialogue.learnMore() {
        chatPlayer(quiz, "Where can I learn more about security?")
        chatNpc(
            happy,
            "The Stronghold of Security, in Barbarian Village. There is much to learn there.",
        )
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
        mainMenu()
    }
}
