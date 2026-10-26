package org.rsmod.content.areas.city.lumbridge.npcs

import org.rsmod.api.config.constants
import org.rsmod.api.invtx.invAdd
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

object DukeHoracioNpcs : NpcReferences() {
    val duke = find("duke_of_lumbridge")
}

object DukeHoracioObjs : ObjReferences() {
    val anti_dragon_shield = find("antidragonbreathshield")
}

class DukeHoracio : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(DukeHoracioNpcs.duke) { startDialogue(it.npc) { duke() } }
    }

    private suspend fun Dialogue.duke() {
        chatNpc(neutral, "Greetings. Welcome to my castle.")
        val shield = DukeHoracioObjs.anti_dragon_shield
        val choice =
            if (shield in player.inv || shield in player.worn || shield in access.bank) {
                choice2("Have you any quests for me?", 2, "Where can I find money?", 3)
            } else {
                choice3(
                    "I seek a shield that will protect me from dragonbreath.",
                    1,
                    "Have you any quests for me?",
                    2,
                    "Where can I find money?",
                    3,
                )
            }
        when (choice) {
            1 -> shieldRequest()
            2 -> {
                chatPlayer(quiz, "Have you any quests for me?")
                // Note: Rune Mysteries is not scripted; this is his line once it is done.
                chatNpc(neutral, "No, all is well for me.")
            }
            3 -> {
                chatPlayer(quiz, "Where can I find money?")
                chatNpc(
                    neutral,
                    "I've heard that the blacksmiths are prosperous amongst the peasantry. " +
                        "Maybe you could try your hand at that?",
                )
            }
        }
    }

    private suspend fun Dialogue.shieldRequest() {
        chatPlayer(quiz, "I seek a shield that will protect me from dragonbreath.")
        chatNpc(quiz, "A knight going on a dragon quest, hmm? What dragon do you intend to slay?")
        // Note: Elvarg is only offered during Dragon Slayer I. With no quest it always is,
        // so this pre-completion path is the one that hands out the shield.
        val elvarg =
            choice2(
                "Elvarg, the dragon of Crandor island!",
                true,
                "Oh, no dragon in particular",
                false,
            )
        if (!elvarg) {
            chatPlayer(neutral, "Oh, no dragon in particular. I just feel like killing a dragon.")
            chatNpc(
                neutral,
                "I don't have an infinite supply of these shields, you know. " +
                    "I'll only give one for a truly worthy cause.",
            )
            return
        }
        chatPlayer(neutral, "Elvarg, the dragon of Crandor island!")
        chatNpc(shocked, "Elvarg? Are you sure?")
        if (!choice2("Yes.", true, "No.", false, title = "Well, are you sure?")) {
            chatNpc(neutral, "Very wise. There are some monsters that are best left alone.")
            return
        }
        val female = player.appearance.bodyType == constants.bodytype_b
        chatPlayer(neutral, "Yes.")
        chatNpc(happy, "Well, you're a braver ${if (female) "woman" else "man"} than I!")
        chatPlayer(quiz, "Why is everyone so scared of this dragon?")
        chatNpc(
            neutral,
            "Back in my father's day, Crandor was an important city-state. Politically it " +
                "was as important as Falador and Varrock and its ships traded with every port.",
        )
        chatNpc(
            sad,
            "But, one day, when I was little, all contact was lost. The trading ships and " +
                "the diplomatic envoys just stopped coming.",
        )
        chatNpc(
            worried,
            "I remember my father being very scared. He posted lookouts on the roof to warn " +
                "if the dragon was approaching. All the city rulers worried that Elvarg " +
                "would devastate the whole continent.",
        )
        val give =
            choice2(
                "I'd better leave that dragon alone.",
                false,
                "So, are you going to give me the shield or not?",
                true,
            )
        if (!give) {
            chatPlayer(neutral, "I'd better leave that dragon alone.")
            chatNpc(
                happy,
                "That's a relief. I would hate to see such a promising adventurer cut down " +
                    "in ${if (female) "her" else "his"} prime.",
            )
            return
        }
        chatPlayer(quiz, "So, are you going to give me the shield or not?")
        if (player.inv.isFull()) {
            // Note: unverified; the wiki has no full-inventory branch for the Duke.
            chatNpc(neutral, "You'll need some free space in your inventory for the shield.")
            return
        }
        chatNpc(
            neutral,
            "If you really think you're up to it then perhaps you are the one who can kill " +
                "this dragon.",
        )
        val shield = DukeHoracioObjs.anti_dragon_shield
        player.invAdd(player.inv, shield)
        objbox(shield, "The Duke hands you a heavy orange shield.")
        chatNpc(neutral, "Take care out there. If you kill it...")
        chatNpc(worried, "If you kill it, for Saradomin's sake make sure it's really dead!")
    }
}
