package org.rsmod.content.areas.city.draynor.npcs

import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.script.onOpNpc1
import org.rsmod.content.areas.city.draynor.configs.draynor_npcs
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Out-of-quest Talk-to lines for Draynor's guards, gossips and quest NPCs; quests are not built.
 */
class DraynorTalk : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(draynor_npcs.bank_guard) { startDialogue(it.npc) { bankGuard() } }
        onOpNpc1(draynor_npcs.miss_schism) {
            startDialogue(it.npc) {
                chatNpc(
                    worried,
                    "Have you heard? The Wise Old Man robbed the bank! Honestly, at his age. " +
                        "They say the money is for the Draynor Manor Restoration Fund.",
                )
            }
        }
        onOpNpc1(draynor_npcs.town_crier) {
            startDialogue(it.npc) {
                chatNpc(happy, "Hear ye, hear ye! Welcome to Draynor Village!")
            }
        }
        // Note: the Forestry kit and the bark shop are not built.
        onOpNpc1(draynor_npcs.forester) {
            startDialogue(it.npc) {
                chatNpc(
                    happy,
                    "Hello there! I'm the Friendly Forester. Keep an eye out for events while " +
                        "you chop, and the trees will reward you.",
                )
            }
        }
        onOpNpc1(draynor_npcs.traiborn) {
            startDialogue(it.npc) {
                chatNpc(neutral, "Ah, a visitor. Mind the bones, I'm using them for an experiment.")
            }
        }
        onOpNpc1(draynor_npcs.mizgog) {
            startDialogue(it.npc) {
                chatNpc(neutral, "Hmm, I'm sure I left those beads somewhere.")
            }
        }
        onOpNpc1(draynor_npcs.grayzag) {
            startDialogue(it.npc) {
                chatNpc(neutral, "Do mind the demon. It's quite securely caged.")
            }
        }
        onOpNpc1(draynor_npcs.onglewip) {
            startDialogue(it.npc) {
                chatNpc(neutral, "I am on sabbatical. Do keep your voice down in the library.")
            }
        }
        onOpNpc1(draynor_npcs.veronica) {
            startDialogue(it.npc) { chatNpc(worried, "Oh dear, I do hope Ernest is all right.") }
        }
        onOpNpc1(draynor_npcs.oddenstein) {
            startDialogue(it.npc) { chatNpc(neutral, "Excuse me, I'm in the middle of something.") }
        }
        onOpNpc1(draynor_npcs.morgan) {
            startDialogue(it.npc) { chatNpc(worried, "Stay indoors after dark. It isn't safe.") }
        }
        onOpNpc1(draynor_npcs.witch) {
            startDialogue(it.npc) { chatNpc(neutral, "I've no time for visitors. Off you go.") }
        }
    }

    private suspend fun Dialogue.bankGuard() {
        val choice =
            choice3(
                "Can I deposit my stuff here?",
                1,
                "That wall doesn't look very good.",
                2,
                "I'll leave you to it.",
                3,
            )
        when (choice) {
            1 -> {
                chatPlayer(quiz, "Can I deposit my stuff here?")
                chatNpc(neutral, "No. I'm a security guard, not a bank clerk.")
            }
            2 -> {
                chatPlayer(quiz, "That wall doesn't look very good.")
                chatNpc(
                    sad,
                    "Don't remind me. The bank was robbed not long ago, and they blew a hole " +
                        "right through it.",
                )
                chatNpc(shifty, "I won't tell you who did it, mind. That's all I'm saying.")
            }
            3 -> chatPlayer(neutral, "I'll leave you to it.")
        }
    }
}
