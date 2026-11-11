package org.rsmod.content.areas.city.lumbridge.npcs

import java.time.Duration
import java.time.LocalDateTime
import org.rsmod.api.config.refs.varps
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.content.areas.city.lumbridge.configs.lumbridge_npcs
import org.rsmod.game.entity.Npc
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class Hans : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(lumbridge_npcs.hans) { hansDialogue(it.npc) }
        onOpNpc3(lumbridge_npcs.hans) { hansAgeDialogue(it.npc) }
    }

    private suspend fun ProtectedAccess.hansDialogue(npc: Npc) =
        startDialogue(npc) { optionsDialogue(npc) }

    private suspend fun Dialogue.optionsDialogue(npc: Npc) {
        chatNpc(neutral, "Hello. What are you doing here?")
        val choice =
            choice5(
                "I'm looking for whoever is in charge of this place.",
                1,
                "I have come to kill everyone in this castle!",
                2,
                "I don't know. I'm lost. Where am I?",
                3,
                "Can you tell me how long I've been here?",
                4,
                "Nothing.",
                5,
            )
        when (choice) {
            1 -> {
                chatPlayer(neutral, "I'm looking for whoever is in charge of this place.")
                chatNpc(neutral, "Who, the Duke? He's in his study, on the first floor.")
            }
            2 -> {
                chatPlayer(angry, "I have come to kill everyone in this castle!")
                npc.playerEscape(player)
                delay(2)
                npc.say("Help! Help!")
            }
            3 -> {
                chatPlayer(confused, "I don't know. I'm lost. Where am I?")
                chatNpc(neutral, "You are in Lumbridge Castle.")
            }
            4 -> {
                chatPlayer(quiz, "Can you tell me how long I've been here?")
                chatNpc(
                    laugh,
                    "Ahh, I see all the newcomers arriving in Lumbridge, " +
                        "fresh-faced and eager for adventure. I remember you...",
                )
                playtimeDialogue()
            }
            5 -> {
                chatPlayer(shifty, "Nothing.")
            }
        }
    }

    private suspend fun ProtectedAccess.hansAgeDialogue(npc: Npc) =
        startDialogue(npc) { playtimeDialogue() }

    private suspend fun Dialogue.playtimeDialogue() {
        // `playtime` counts game ticks (0.6 s): 100 ticks a minute.
        val minutes = player.vars[varps.playtime] / 100
        val arrived = Duration.between(player.createdAt, LocalDateTime.now()).toDays()
        chatNpc(
            happy,
            "You've spent ${minutes / 1440} days, ${minutes / 60 % 24} hours, ${minutes % 60} " +
                "minutes in the world since you arrived $arrived days ago.",
        )
    }
}
