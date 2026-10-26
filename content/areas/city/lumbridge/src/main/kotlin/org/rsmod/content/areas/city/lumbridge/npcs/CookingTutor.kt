package org.rsmod.content.areas.city.lumbridge.npcs

import org.rsmod.api.config.refs.objs
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.entity.Npc
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

object CookingTutorNpcs : NpcReferences() {
    val tutor = find("aide_tutor_cooking")
}

object CookingTutorObjs : ObjReferences() {
    val range_icon = find("range_icon_dummy")
}

class CookingTutor : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(CookingTutorNpcs.tutor) { startDialogue(it.npc) }
    }

    private suspend fun ProtectedAccess.startDialogue(npc: Npc) {
        startDialogue(npc) {
            chatPlayer(happy, "Hello.")
            chatNpc(happy, "Hello there! Are you interested in hearing all about cooking?")
            mainMenu()
        }
    }

    private suspend fun Dialogue.mainMenu() {
        val choice =
            choice3(
                "How can I train my cooking?",
                1,
                "What kinds of things can I cook?",
                2,
                "No, thank you.",
                3,
            )
        when (choice) {
            1 -> training()
            2 -> kindsOfFood()
            3 -> {
                chatPlayer(neutral, "No, thank you.")
                chatNpc(
                    happy,
                    "Well, just come back any time you want to know how to cook up a storm!",
                )
            }
        }
    }

    private suspend fun Dialogue.training() {
        chatPlayer(quiz, "How can I train my cooking?")
        chatNpc(
            happy,
            "The simplest thing to cook is raw meat or fish. Shrimp can be caught south of " +
                "here, to the east of Lumbridge Swamp. You could ask the fishing tutor for a " +
                "net if you have not got one.",
        )
        chatNpc(
            happy,
            "Alternatively, you could acquire some raw beef or chicken. You can find cows and " +
                "chickens north of here. Go over the bridge then follow the path north, you " +
                "can't miss them!",
        )
        objbox(
            CookingTutorObjs.range_icon,
            "When you have a full inventory of meat or fish, find a range. Look for this icon " +
                "on your minimap.",
        )
        chatNpc(happy, "You could use my range here if you like.")
        chatNpc(
            happy,
            "If you wanted to, my friend the cook in Lumbridge Castle has a range you could " +
                "use. You might find you burn even less on that range as its top notch.",
        )
        chatNpc(
            neutral,
            "Alternatively, you can use your own fire, but it's not as effective and the food " +
                "will burn more frequently. To make a fire, use a tinderbox on some logs.",
        )
        chatNpc(
            happy,
            "Once you've found your range or fire, use your raw food on it. This will bring up " +
                "a menu of the food you can cook. Then select the food you want.",
        )
        chatNpc(
            happy,
            "When you have a full inventory of cooked food, drop the useless burnt food and " +
                "find a bank.",
        )
        objbox(
            objs.bank_icon,
            "Look for this symbol on your minimap after climbing the stairs of the Lumbridge " +
                "Castle to the top. There are numerous banks around the world, all marked with " +
                "that symbol.",
        )
        // Note: quest progress isn't tracked, so the Cook's Assistant tip always shows.
        chatNpc(
            happy,
            "If you're interested in quests, I heard my friend the cook in Lumbridge Castle is " +
                "in need of a hand. Just talk to him and he'll set you off.",
        )
        chatNpc(happy, "Is there anything else you want to know?")
        mainMenu()
    }

    private suspend fun Dialogue.kindsOfFood() {
        chatPlayer(quiz, "What kinds of things can I cook?")
        chatNpc(
            happy,
            "Many things! You can cook anything from fish to poultry. You could try your hand " +
                "at making baked potatoes or even brew some of the good stuff.",
        )
        // Note: the fish/brewing/vegetables/pies submenu is skipped.
        chatNpc(happy, "Is there anything else you want to know?")
        mainMenu()
    }
}
