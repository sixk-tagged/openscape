package org.rsmod.content.areas.city.lumbridge.npcs

import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.content.areas.city.lumbridge.configs.lumbridge_npcs
import org.rsmod.game.entity.Npc
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class Doomsayer : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(lumbridge_npcs.doomsayer) { startDialogue(it.npc) }
        // Note: the real op opens the cws_doomsayer warnings interface; nothing tracks
        // warnings yet.
        onOpNpc3(lumbridge_npcs.doomsayer) { mes("Warnings can't be toggled yet.") }
    }

    // Note: no warnings can be disabled yet, so the "turn them back on" branch is skipped.
    private suspend fun ProtectedAccess.startDialogue(npc: Npc): Unit =
        startDialogue(npc) {
            chatNpc(worried, "Dooooom!")
            chatPlayer(confused, "Where?")
            chatNpc(
                worried,
                "All around us! I can feel it in the air, hear it on the wind, smell it... also " +
                    "in the air!",
            )
            chatPlayer(quiz, "Is there anything we can do about this doom?")
            chatNpc(
                neutral,
                "There is nothing you need to do my friend! I am the Doomsayer, although my real " +
                    "title could be something like the Danger Tutor.",
            )
            chatPlayer(confused, "Danger Tutor?")
            chatNpc(happy, "Yes! I roam the world sensing danger.")
            chatNpc(
                neutral,
                "If you see the signs often enough, then you can turn them off; by that time you " +
                    "likely know what the area has in store for you.",
            )
            chatNpc(
                neutral,
                "If I find a dangerous area, then I put up warning signs that will tell you what " +
                    "is so dangerous about that area.",
            )
            chatPlayer(quiz, "But what if I want to see the warnings again?")
            chatNpc(happy, "That's why I'm waiting here!")
            chatNpc(
                happy,
                "If you want to see the warning messages again, I can turn them back on for you.",
            )
            chatPlayer(happy, "Thanks, I'll remember that if I see any warning messages.")
            chatNpc(happy, "You're welcome!")
        }
}
