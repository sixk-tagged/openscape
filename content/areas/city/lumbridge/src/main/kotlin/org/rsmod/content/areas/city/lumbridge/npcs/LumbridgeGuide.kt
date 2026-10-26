package org.rsmod.content.areas.city.lumbridge.npcs

import org.rsmod.api.config.refs.objs
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.content.areas.city.lumbridge.configs.lumbridge_npcs
import org.rsmod.game.entity.Npc
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

object LumbridgeGuideObjs : ObjReferences() {
    val quest_start_icon = find("quest_start_icon_dummy")
    val combat_tutor_icon = find("combat_tutor_icon_dummy")
}

class LumbridgeGuide : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(lumbridge_npcs.guide) { startDialogue(it.npc) }
    }

    private suspend fun ProtectedAccess.startDialogue(npc: Npc) {
        startDialogue(npc) {
            chatNpc(
                happy,
                "Greetings, adventurer. I am Phileas, the Lumbridge Guide. I am here to give " +
                    "information and directions to new players. Is there anything I can help " +
                    "you with?",
            )
            mainMenu()
        }
    }

    private suspend fun Dialogue.mainMenu() {
        val choice =
            choice5(
                "Where can I find a quest to go on?",
                1,
                "What monsters should I fight?",
                2,
                "Where can I make money?",
                3,
                "Where can I find more information?",
                4,
                "More options...",
                5,
            )
        when (choice) {
            1 -> quests()
            2 -> monsters()
            3 -> money()
            4 -> information()
            5 -> moreOptions()
        }
    }

    // Note: quest progress isn't tracked, so he always points at Cook's Assistant.
    private suspend fun Dialogue.quests() {
        chatPlayer(quiz, "Where can I find a quest to go on?")
        chatNpc(
            happy,
            "Well, I've heard my friend the cook is in need of a spot of help. He'll be in the " +
                "kitchen of this here castle. Just talk to him and he'll set you off.",
        )
        objbox(
            LumbridgeGuideObjs.quest_start_icon,
            "The minimap in the top right corner of the screen has various icons to show " +
                "different points of interest. Look for the icon to the left to find quest " +
                "start points.",
        )
        anythingElse()
    }

    private suspend fun Dialogue.monsters() {
        chatPlayer(quiz, "What monsters should I fight?")
        // Note: the wiki doesn't give the real combat level cut-off; 30 is a guess. The
        // no-weapon branch and the follow-up food/combat submenus are skipped.
        if (player.combatLevel >= 30) {
            chatNpc(
                happy,
                "You're strong enough to work out what monsters to fight for yourself now, but " +
                    "the combat tutors might help you with any questions you have about the " +
                    "skills; they're just over there to the south of the general store.",
            )
            objbox(
                LumbridgeGuideObjs.combat_tutor_icon,
                "The minimap in the top right corner of the game screen has various icons " +
                    "showing the locations of things. This icon to the left shows where Combat " +
                    "tutors are.",
            )
            mainMenu()
            return
        }
        chatNpc(
            happy,
            "There's lots of beasts to fight in the woods around here, especially to the west. " +
                "There are certainly some goblins and spiders that are pests and could do with " +
                "being cleared out. There's also a chicken farm or two up the",
        )
        chatNpc(
            happy,
            "road for some fairly easy pickings. Non-player characters usually appear as yellow " +
                "dots on your minimap, although there are some that you won't be able to " +
                "fight, such as myself. A monster's combat level is",
        )
        chatNpc(
            happy,
            "shown next to their 'Attack' option. If that level is coloured green it means the " +
                "monster is weaker than you. If it is red, it means that the monster is " +
                "tougher than you.",
        )
        chatNpc(
            happy,
            "Remember, you will do better if you have better armour and weapons and it's " +
                "always worth carrying a bit of food to heal yourself.",
        )
        anythingElse()
    }

    // Note: the smith/fish follow-up options are skipped.
    private suspend fun Dialogue.money() {
        chatPlayer(quiz, "Where can I make money?")
        chatNpc(
            happy,
            "There are many ways to make money in the game. I would suggest either killing " +
                "monsters or doing a trade skill such as Smithing or Fishing.",
        )
        chatNpc(
            neutral,
            "Please don't try to get money by begging off other players. It will make you " +
                "unpopular. Nobody likes a beggar. It is very irritating to have other players " +
                "asking for your hard-earned cash.",
        )
        anythingElse()
    }

    // Note: the real one opens the account tab's useful links; that line is trimmed.
    private suspend fun Dialogue.information() {
        chatPlayer(quiz, "Where can I find more information?")
        chatNpc(
            happy,
            "What you'll want is the OSRS Wiki! There's information and guides on almost " +
                "everything, all easily searched and it's even written and maintained by " +
                "players.",
        )
        anythingElse()
    }

    // Note: "I'd like to know more about security." (Stronghold and security book) skipped.
    private suspend fun Dialogue.moreOptions() {
        val choice =
            choice3(
                "Where can I find a bank?",
                1,
                "I don't need any help.",
                2,
                "Previous options...",
                3,
            )
        when (choice) {
            1 -> {
                chatPlayer(quiz, "Where can I find a bank?")
                chatNpc(
                    happy,
                    "You'll find a bank upstairs in Lumbridge Castle - go right to the top.",
                )
                objbox(
                    objs.bank_icon,
                    "The minimap in the top right corner of the game screen has various icons " +
                        "showing the locations of things. This icon to the left shows where " +
                        "banks are.",
                )
                anythingElse()
            }
            2 -> chatPlayer(neutral, "No, I can find things myself thank you.")
            3 -> mainMenu()
        }
    }

    private suspend fun Dialogue.anythingElse() {
        chatNpc(happy, "Is there anything else you need help with?")
        if (choice2("Yes please.", true, "No thank you.", false)) {
            chatPlayer(happy, "Yes please.")
            mainMenu()
        } else {
            chatPlayer(neutral, "No thank you.")
        }
    }
}
