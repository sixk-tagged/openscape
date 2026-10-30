package org.rsmod.content.areas.city.portsarim.npcs

import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.script.onOpNpc4
import org.rsmod.content.areas.city.portsarim.configs.port_sarim_npcs
import org.rsmod.content.areas.city.portsarim.configs.port_sarim_objs
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Out-of-quest Talk-to lines for the jail, the pub and Rimmington's neighbours. Quests are not
 * built, so these only gossip; the security guard hands out his book.
 */
class PortSarimTalk : PluginScript() {
    override fun ScriptContext.startup() {
        // Note: all of the wording below is unverified.
        onOpNpc1(port_sarim_npcs.security_guard) { startDialogue(it.npc) { securityBook() } }
        onOpNpc1(port_sarim_npcs.cap_n_hand) {
            startDialogue(it.npc) {
                chatNpc(sad, "Arr, they locked me up for nothing! I'm an honest pirate, I am.")
            }
        }
        onOpNpc1(port_sarim_npcs.ahab) {
            startDialogue(it.npc) { chatNpc(angry, "Oi! Keep your hands off me beer!") }
        }
        onOpNpc1(port_sarim_npcs.jack_seagull) {
            startDialogue(it.npc) {
                chatNpc(
                    drunk,
                    "Arr, if ye be wantin' adventure, talk to Redbeard Frank, or the bartender, " +
                        "or the Wise Old Man.",
                )
            }
        }
        onOpNpc1(port_sarim_npcs.longbow_ben) {
            startDialogue(it.npc) {
                chatNpc(
                    drunk,
                    "Quests, eh? Redbeard Frank is yer man. He's over there by the window.",
                )
            }
        }
        onOpNpc1(port_sarim_npcs.sleeping_guard) {
            startDialogue(it.npc) { chatNpc(drunk, "Guh... mwww... zzzzz...") }
        }
        onOpNpc1(port_sarim_npcs.lookout_guard) {
            startDialogue(it.npc) {
                chatNpc(neutral, "Keep your voice down. I'm supposed to be keeping watch.")
            }
        }
        onOpNpc1(port_sarim_npcs.wormbrain) {
            startDialogue(it.npc) { chatNpc(angry, "Me not talk to you. Go away, human!") }
        }
        onOpNpc1(port_sarim_npcs.chemist) {
            startDialogue(it.npc) {
                chatNpc(neutral, "Careful with the still, it's full of lamp oil.")
            }
        }
        onOpNpc1(port_sarim_npcs.hetty) {
            startDialogue(it.npc) {
                chatNpc(happy, "Hee hee hee! I'm busy with my potions, dearie. Off you go.")
            }
        }
        onOpNpc1(port_sarim_npcs.chancy) {
            startDialogue(it.npc) { chatNpc(happy, "Fancy a bet? Perhaps another day.") }
        }
        onOpNpc1(port_sarim_npcs.da_vinci) {
            startDialogue(it.npc) { chatNpc(happy, "Ah, I am an artist! I paint what I see.") }
        }
        onOpNpc1(port_sarim_npcs.hops) {
            startDialogue(it.npc) { chatNpc(drunk, "Hic! Wanna... hic... drink?") }
        }
        onOpNpc1(port_sarim_npcs.morris) {
            startDialogue(it.npc) {
                chatNpc(
                    neutral,
                    "This ship isn't sailing anywhere. If you want to go to Great Kourend, you " +
                        "want Veos's ship at the northern dock.",
                )
            }
        }
        onOpNpc1(port_sarim_npcs.veos) {
            startDialogue(it.npc) {
                chatNpc(happy, "Ahoy there! My ship sails for Great Kourend, free of charge.")
                chatNpc(sad, "I'm afraid the seas are too rough for you to join me today.")
            }
        }
        onOpNpc1(port_sarim_npcs.barnaby) {
            startDialogue(it.npc) {
                chatNpc(
                    happy,
                    "Hello there. Would you like to travel somewhere? I can take you to " +
                        "Ardougne or Brimhaven for 30 coins.",
                )
                chatNpc(sad, "Though I'm afraid the ship is out of action for now.")
            }
        }
        onOpNpc3(port_sarim_npcs.barnaby) { startDialogue(it.npc) { shipOutOfAction() } }
        onOpNpc4(port_sarim_npcs.barnaby) { startDialogue(it.npc) { shipOutOfAction() } }
        onOpNpc1(port_sarim_npcs.colin) {
            startDialogue(it.npc) {
                chatNpc(
                    neutral,
                    "Hello again. Coming back to Corsair Cove? The ship isn't sailing today.",
                )
            }
        }
        for (monk in port_sarim_npcs.entrana_monks) {
            onOpNpc3(monk) { startDialogue(it.npc) { boatNotSailing() } }
            onOpNpc1(monk) {
                startDialogue(it.npc) {
                    chatNpc(
                        neutral,
                        "Do you seek passage to holy Entrana? If so, you must leave your " +
                            "weaponry and armour behind. This is Saradomin's will.",
                    )
                    boatNotSailing()
                }
            }
        }
    }

    private suspend fun Dialogue.boatNotSailing() {
        chatNpc(sad, "Alas, the boat is not sailing today.")
    }

    private suspend fun Dialogue.shipOutOfAction() {
        chatNpc(sad, "I'm afraid the ship is out of action for now.")
    }

    private suspend fun Dialogue.securityBook() {
        chatNpc(happy, "Hello there! Stay safe out there. Would you like a copy of my book?")
        val choice = choice2("Yes please.", 1, "No thanks.", 2)
        if (choice == 2) {
            chatPlayer(neutral, "No thanks.")
            return
        }
        chatPlayer(happy, "Yes please.")
        if (port_sarim_objs.sos_security_book in access.inv) {
            chatNpc(neutral, "You already have one of my books.")
        } else if (access.inv.isFull()) {
            chatNpc(neutral, "You don't have room for it.")
        } else {
            access.invAdd(access.inv, port_sarim_objs.sos_security_book)
            access.mes("The guard gives you a book on staying safe.")
        }
    }
}
