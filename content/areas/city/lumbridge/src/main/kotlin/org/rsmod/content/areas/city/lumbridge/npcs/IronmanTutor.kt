package org.rsmod.content.areas.city.lumbridge.npcs

import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.script.onOpNpc4
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.game.entity.Npc
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

object IronmanTutorNpcs : NpcReferences() {
    val tutor = find("ironman_tutor_1")
}

// Note: there is no Ironman mode, so everyone gets the non-Ironman lines, and op4 "Setup"
// gives the Tutorial Island line instead of the mode interface.
class IronmanTutor : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(IronmanTutorNpcs.tutor) { talk(it.npc) }
        onOpNpc3(IronmanTutorNpcs.tutor) { armour(it.npc) }
        onOpNpc4(IronmanTutorNpcs.tutor) { startDialogue(it.npc) { tutorialIslandOnly() } }
    }

    private suspend fun ProtectedAccess.talk(npc: Npc) {
        startDialogue(npc) {
            chatNpc(
                happy,
                "Hello, ${player.displayName}. I'm the Ironman tutor. What can I do for you?",
            )
            val choice =
                choice3(
                    "Tell me about Ironmen.",
                    1,
                    "Have you any armour for me, please?",
                    2,
                    "I'm fine, thanks.",
                    3,
                )
            when (choice) {
                1 -> {
                    chatPlayer(quiz, "Tell me about Ironmen.")
                    chatNpc(
                        neutral,
                        "When you play as an Ironman, you do everything for yourself. You don't " +
                            "trade with other players, or take their items, or accept their help.",
                    )
                    chatNpc(
                        neutral,
                        "As an Ironman, you choose to have these restrictions imposed on you, so " +
                            "everyone knows you're doing it properly.",
                    )
                    tutorialIslandOnly()
                }
                2 -> {
                    chatPlayer(quiz, "Have you any armour for me, please?")
                    chatNpc(neutral, "You're not an Ironman. This armour is only for them.")
                }
                3 -> chatPlayer(neutral, "I'm fine, thanks.")
            }
        }
    }

    private suspend fun ProtectedAccess.armour(npc: Npc) {
        startDialogue(npc) {
            chatNpc(neutral, "You're not an Ironman. This armour is only for them.")
        }
    }

    private suspend fun Dialogue.tutorialIslandOnly() {
        chatNpc(
            neutral,
            "You can choose to become an Ironman while you're on Tutorial Island. Although you " +
                "can't do that on this account, you might like to bear it in mind for the future.",
        )
    }
}
