package org.rsmod.content.areas.city.lumbridge.npcs

import org.rsmod.api.invtx.invAdd
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.baseFishingLvl
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.content.areas.city.lumbridge.configs.lumbridge_npcs
import org.rsmod.game.entity.Npc
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

object FishingTutorObjs : ObjReferences() {
    val net = find("net")
    val fishing_spot_icon = find("fishing_spot_icon_dummy")
}

class FishingTutor : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(lumbridge_npcs.fishing_tutor) { startDialogue(it.npc) }
    }

    private suspend fun ProtectedAccess.startDialogue(npc: Npc) {
        startDialogue(npc) { mainMenu() }
    }

    // Note: "Tell me about different fish." submenu, world-map offers and quest tips skipped.
    private suspend fun Dialogue.mainMenu() {
        val lvl = player.baseFishingLvl
        val advice =
            when {
                lvl >= 39 -> "Any advice for an advanced fisher?"
                lvl >= 29 -> "I already know about the basics of fishing, got any tips?"
                else -> "Can you teach me the basics of fishing please?"
            }
        when (choice3(advice, 1, "Where and what should I fish?", 2, "Goodbye.", 3)) {
            1 -> {
                chatPlayer(quiz, advice)
                giveNet()
                when {
                    lvl >= 39 -> advancedAdvice()
                    lvl >= 29 -> tips()
                    else -> basics()
                }
                mainMenu()
            }
            2 -> whereAndWhat()
            3 -> chatPlayer(neutral, "Goodbye.")
        }
    }

    private suspend fun Dialogue.giveNet() {
        if (FishingTutorObjs.net in player.inv) {
            chatNpc(neutral, "I see you already have a net.")
        } else if (player.invAdd(player.inv, FishingTutorObjs.net).success) {
            chatNpc(happy, "Ah, you've lost your net have you? Have another!")
        }
    }

    private suspend fun Dialogue.basics() {
        objbox(
            FishingTutorObjs.fishing_spot_icon,
            "Look for this icon on the minimap to find fishing spots, they will move as the " +
                "fish swim around so you can't be lazy.",
        )
        chatNpc(
            happy,
            "Ahoy, to fish, you click on the shrimp fishin' spot while ye'r carrying a net.",
        )
        chatPlayer(quiz, "I see... is that it?")
        chatNpc(
            happy,
            "There's far more as you progress - not just shrimps. You get more equipment, " +
                "bigger fish and other things too...",
        )
        chatNpc(
            happy,
            "When you have a full inventory, you can cook it or take it to the bank. You can " +
                "find a bank on the roof of the castle in Lumbridge and a cookin' range in the " +
                "castle kitchen.",
        )
    }

    private suspend fun Dialogue.tips() {
        chatNpc(
            happy,
            "Arrr, chose carefully where and what you fish, you can get different fish in " +
                "different places throughout the land.",
        )
        chatNpc(
            happy,
            "Make sure you hang on to your fish, don't throw them away as you can get valuable " +
                "cooking experience from them, gar! Look out for other things too...",
        )
        chatNpc(happy, "Then of course there is always the trawler...")
        chatNpc(
            happy,
            "If you venture to Port Khazard north of Yanille and speaks to me cabin boy Murphy, " +
                "he can let you and a bunch of friends use his trawler to fish with... Just " +
                "make sure you take a bailing bucket or two.",
        )
    }

    private suspend fun Dialogue.advancedAdvice() {
        chatNpc(
            happy,
            "Aye, as you get better and better you'll find that you can fish for things like " +
                "Tuna and Swordfish. These be very good for combat.",
        )
        chatNpc(
            happy,
            "Of course, after that you will get Shark, and if you venture out onto the Trawler " +
                "from Port Khazard, you can then get Manta Ray or Sea Turtle, very good eatin' " +
                "on one of them.",
        )
        chatNpc(
            happy,
            "You can also use a fishing potion if you can get your hands on one and just need " +
                "that little edge.. ask someone who is familiar with herblore.",
        )
    }

    private suspend fun Dialogue.whereAndWhat() {
        chatPlayer(quiz, "Where and what should I fish?")
        val lvl = player.baseFishingLvl
        when {
            lvl < 10 -> {
                chatNpc(
                    happy,
                    "A young and enthusiastic fisher. Try south of Draynor Village or the pier " +
                        "on the island of Karamja to fish for Shrimp at level 1 or Sardines at " +
                        "level 5. Aye, me parrot concurs.",
                )
                chatPlayer(confused, "What parrot?")
                chatNpc(happy, "Me parrot Percy on me shoulder thar! Arr!")
                chatPlayer(shifty, "Oookay...sure, a parrot, on your shoulder.")
                chatNpc(happy, "Arrr!")
            }
            lvl < 16 ->
                chatNpc(
                    happy,
                    "Herrin' can be fished from Catherby and some other places when you reach " +
                        "level 10.",
                )
            lvl < 23 ->
                chatNpc(
                    happy,
                    "You can use a big net to catch Mackerel from Catherby when you reach " +
                        "level 16.",
                )
            lvl < 28 ->
                chatNpc(
                    happy,
                    "Cod can be fished from Catherby and some other places once you reach " +
                        "level 23.",
                )
            lvl < 38 ->
                chatNpc(
                    happy,
                    "You can use your fishin' rod and some bait to catch Slimy Eel in the swamps " +
                        "at level 28.",
                )
            lvl < 46 ->
                chatNpc(
                    happy,
                    "You can use your fishin' rod and some bait to catch Cave Eel in the caves " +
                        "below Lumbridge Swamp at level 38.",
                )
            lvl < 50 ->
                chatNpc(
                    happy,
                    "Bass can be caught at level 46 in your big net if you wander along to " +
                        "Catherby.",
                )
            else -> {
                chatNpc(
                    happy,
                    "Tuna and Swordfish can be harpooned - if you're good enough - from the " +
                        "thrivin' fishing village of Catherby, or if you can get in try the " +
                        "Fishin' Guild. Level 35 for Tuna and 50 for Swordfish.",
                )
                chatNpc(
                    happy,
                    "You can also go talk to Murphy and see if you can use his trawler to catch " +
                        "Manta Ray and Sea Turtle if you're really good... Gar!",
                )
            }
        }
        mainMenu()
    }
}
