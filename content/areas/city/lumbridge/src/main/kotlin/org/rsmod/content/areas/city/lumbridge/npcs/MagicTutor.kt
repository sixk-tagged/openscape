package org.rsmod.content.areas.city.lumbridge.npcs

import org.rsmod.api.config.refs.objs
import org.rsmod.api.invtx.invAdd
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.baseMagicLvl
import org.rsmod.api.player.stat.baseRunecraftingLvl
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.game.entity.Npc
import org.rsmod.game.type.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

object MagicTutorNpcs : NpcReferences() {
    val tutor = find("aide_tutor_magic")
}

class MagicTutor : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(MagicTutorNpcs.tutor) { startDialogue(it.npc) }
        onOpNpc3(MagicTutorNpcs.tutor) { claim(it.npc) }
    }

    private suspend fun ProtectedAccess.startDialogue(npc: Npc): Unit =
        startDialogue(npc) {
            chatNpc(
                happy,
                "Hello there adventurer, I am the Magic combat tutor. Would you like to learn " +
                    "about magic combat, or perhaps how to make runes?",
            )
            mainMenu()
        }

    // Note: the rune pouch pickup toggle option is skipped.
    private suspend fun Dialogue.mainMenu() {
        val choice =
            choice3(
                "Tell me about magic combat please.",
                1,
                "How do I make runes?",
                2,
                "Goodbye.",
                3,
            )
        when (choice) {
            1 -> magicCombat()
            2 -> makingRunes()
            3 -> chatPlayer(neutral, "Goodbye.")
        }
    }

    // Note: no spellbook tab switch, no Adventure paths line, and the quest-gated Mage Arena
    // and Desert Treasure lines are skipped.
    private suspend fun Dialogue.magicCombat() {
        chatPlayer(quiz, "Tell me about magic combat please.")
        if (player.baseMagicLvl < 42) {
            chatNpc(
                neutral,
                "This is your spell book, this contains all the spells you can cast. To cast a " +
                    "spell, you will need to have the required magic level and have the right " +
                    "amount of the runes used to cast the spell.",
            )
            chatNpc(
                neutral,
                "To check the level and runes required to cast a spell, hover your mouse cursor " +
                    "over the spell.",
            )
            chatNpc(
                neutral,
                "If you have the right amount of runes and the magic level the spell will light " +
                    "up, meaning you will be able to cast it.",
            )
            chatNpc(
                neutral,
                "When you have a combat spell available, click on it once, then select your " +
                    "target. A good target would be a monster that is below your combat level.",
            )
            chatNpc(
                neutral,
                "Try rats in the castle or, if you're feeling brave, the goblins to the west " +
                    "have been causing a nuisance of themselves.",
            )
            chatNpc(
                neutral,
                "Nemarti, the Ranged Combat tutor and I both give out items every 30 minutes, " +
                    "however you must choose whether you want runes or ranged equipment.",
            )
            chatNpc(
                neutral,
                "To claim runes, right-click on me and choose Claim, to claim ranged equipment " +
                    "right-click on the Ranged Combat tutor and select Claim.",
            )
        } else {
            chatNpc(
                happy,
                "Of course ${player.displayName}! As a rule of thumb, if you cast the highest " +
                    "spell of which you're capable, you'll get the best experience possible.",
            )
            chatNpc(
                neutral,
                "Wearing metal armour and ranged armour can seriously impair your magical " +
                    "abilities. Make sure you wear some robes to maximise your capabilities.",
            )
            chatNpc(
                neutral,
                "Superheat Item and the Alchemy spells are good ways to level magic if you are " +
                    "not interested in the combat aspect of magic.",
            )
            if (player.members) {
                chatNpc(
                    neutral,
                    "There's always the Magic Training Arena. You can find it north of the " +
                        "Emir's Arena, north-east of Al Kharid. You will be able to earn some " +
                        "special rewards there by practicing your magic there.",
                )
            }
        }
        chatNpc(happy, "Is there anything else you would like to know?")
        mainMenu()
    }

    // Note: Rune Mysteries is not tracked, so this is always the before-quest essence line.
    private suspend fun Dialogue.makingRunes() {
        chatPlayer(quiz, "How do I make runes?")
        chatNpc(
            neutral,
            "There are a couple of things you will need to make runes, rune essence and a " +
                "talisman to enter the temple ruins.",
        )
        chatNpc(
            neutral,
            "To get rune essence you will need to gather them somehow. You should talk to the " +
                "Duke of Lumbridge, he may be able to help you with that. Alternatively, other " +
                "players may sell you the essence.",
        )
        if (player.baseRunecraftingLvl < 5) {
            chatNpc(
                neutral,
                "As you're fairly new to runecrafting you should start with air runes and mind " +
                    "runes.",
            )
        } else {
            chatNpc(
                neutral,
                "I see you have some experience already in Runecrafting. Perhaps you should try " +
                    "crafting some runes which you can then use in magic.",
            )
            chatNpc(neutral, "Check the skill guide to see which runes you can craft.")
        }
        chatNpc(
            neutral,
            "You will need a talisman for the rune you would like to create. You can " +
                "right-click on it and select the Locate option. This will tell you the rough " +
                "location of the altar.",
        )
        chatNpc(
            neutral,
            "When you find the ruined altar, use the talisman on it to be transported to a " +
                "temple where you can craft your runes.",
        )
        chatNpc(
            neutral,
            "Clicking on the temple's altar will imbue your rune essence with the altar's " +
                "magical property.",
        )
        chatNpc(
            neutral,
            "If you want to save yourself an inventory space, you could always try binding the " +
                "talisman to a tiara.",
        )
        chatNpc(
            neutral,
            "To make one, take a tiara and talisman to the ruins and use the tiara on the " +
                "temple altar. This will bind the talisman to your tiara.",
        )
        chatNpc(happy, "Is there anything else you would like to know?")
        mainMenu()
    }

    // Note: when both rune types are given, two objboxes play (the wiki has no such case).
    private suspend fun ProtectedAccess.claim(npc: Npc): Unit =
        startDialogue(npc) {
            if (player.tutorClaimOnCooldown) {
                chatNpc(
                    neutral,
                    "I work with the Ranged Combat tutor to give out consumable items that you " +
                        "may need for combat such as arrows and runes. However we have had some " +
                        "cheeky people try to take both!",
                )
                chatNpc(
                    neutral,
                    "So, every half an hour, you may come back and claim either arrows OR " +
                        "runes, but not both. Come back in a while for runes, or simply make " +
                        "your own.",
                )
                return@startDialogue
            }
            val given = mutableListOf<Pair<ObjType, String>>()
            var inBank = false
            for ((rune, name) in listOf(objs.mind_rune to "mind", objs.air_rune to "air")) {
                if (rune in access.bank) {
                    inBank = true
                    chatNpc(neutral, "You have some $name runes in your bank.")
                } else if (rune in player.inv) {
                    chatNpc(neutral, "You already have some $name runes.")
                } else if (player.invAdd(player.inv, rune, 30).success) {
                    player.markTutorClaim()
                    given += rune to name
                } else {
                    chatNpc(
                        sad,
                        "If you had enough space in your inventory I'd give you some $name " +
                            "runes, come back when you do.",
                    )
                }
            }
            for ((rune, name) in given) {
                objbox(rune, "Mikasi gives you 30 $name runes.")
            }
            if (inBank) {
                objbox(
                    objs.bank_icon,
                    "You have some runes in your bank. Climb the stairs in Lumbridge Castle " +
                        "until you see this icon on your minimap. There you will find a bank.",
                )
            }
        }
}
