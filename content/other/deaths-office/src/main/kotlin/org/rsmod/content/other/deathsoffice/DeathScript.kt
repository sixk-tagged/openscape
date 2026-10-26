package org.rsmod.content.other.deathsoffice

import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.output.ClientScripts.toplevelSidebuttonSwitch
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onPlayerQueue
import org.rsmod.content.other.deathsoffice.GravestoneScript.Companion.TUTORIAL_DONE
import org.rsmod.content.other.deathsoffice.configs.DeathNpcs
import org.rsmod.content.other.deathsoffice.configs.DeathQueues
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Death's Talk-to and the first-death tutorial he gives in his office. Lines are from the OSRS
 * wiki's Transcript:Death (NPC); the gravestone cosmetics and death animations are not offered.
 */
class DeathScript : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(DeathNpcs.death) { startDialogue { talkToDeath() } }
        onPlayerQueue(DeathQueues.tutorial) { startDialogue { tutorialIntro() } }
    }

    private suspend fun Dialogue.talkToDeath() {
        when {
            player.inDeathTutorial -> {
                death(
                    "I believe I was explaining gravestones to you. We still have some more " +
                        "topics to cover."
                )
                tutorialTopics()
            }
            player.heldByDeath -> {
                death("Would you like me to remind you about gravestones?")
                tutorialTopics()
            }
            else -> {
                death("Hello mortal. Have you come to retrieve something you lost when you died?")
                greetingOptions()
            }
        }
    }

    private suspend fun Dialogue.greetingOptions() {
        when (
            choice4(
                "How does that work?",
                1,
                "What is this place?",
                2,
                "Yes, have you got anything for me?",
                3,
                "More options...",
                4,
            )
        ) {
            1 -> howDoesThatWork()
            2 -> whatIsThisPlace()
            3 -> {
                chatPlayer(neutral, "Yes, have you got anything for me?")
                access.openDeathsOffice()
            }
            4 -> moreOptions()
        }
    }

    private suspend fun Dialogue.howDoesThatWork() {
        chatPlayer(quiz, "How does that work?")
        death(
            "When you die, you often drop things you were carrying. In most cases, a gravestone " +
                "appears near where you died, and your items can be retrieved there."
        )
        death(
            "However, gravestones do not last for long. When your gravestone collapses, your " +
                "items come here, to me. I can return them to you - though I charge a fee for " +
                "my services."
        )
        death(
            "Aside from that, if you bring me your unwanted possessions, and sacrifice them " +
                "into my Coffer here, I'll count their value against any future fees I charge you."
        )
        death(
            "I don't accept just any junk, but I'll try to give you a good price for anything decent."
        )
        death("So do you need to retrieve something you lost when you died?")
        greetingOptions()
    }

    private suspend fun Dialogue.whatIsThisPlace() {
        chatPlayer(quiz, "What is this place?")
        death(
            "As you should be able to see, I am Death. The Grim Reaper. The Inexorable One. The " +
                "Inescapable Grasp of Mortal Destiny. And this is my little office. Nice, isn't it?"
        )
        val lovely =
            choice2("It's lovely.", true, "Your decorations leave a bit to be desired.", false)
        if (lovely) {
            chatPlayer(neutral, "It's lovely.")
            death(
                "Thank you. I feel the colour scheme suits my style. Now, can I help you " +
                    "retrieve something you lost when you died?"
            )
        } else {
            chatPlayer(neutral, "Your decorations leave a bit to be desired.")
            death(
                "Sometimes it's hard to care for beauty when one's very existence is just seen " +
                    "as a curse upon the world. Now, do you actually want to retrieve something " +
                    "from me?"
            )
        }
    }

    // Note: the gravestone cosmetic and death animation options are left out of this menu.
    private suspend fun Dialogue.moreOptions() {
        val previous = choice2("Not just now, thanks.", false, "Previous options...", true)
        if (previous) {
            greetingOptions()
            return
        }
        chatPlayer(neutral, "Not just now, thanks.")
        death("Really? There isn't much else for you to do here. But you know your business.")
    }

    private suspend fun Dialogue.tutorialIntro() {
        death(
            "So, mortal... you have died, as all mortals do. As you have suffered the loss of " +
                "some items on this occasion, I have brought you here to explain for you how " +
                "that works."
        )
        chatPlayer(quiz, "I lost items? Can I get them back?")
        death(
            "Let me explain... Across most of the land of Gielinor, when you die, some of the " +
                "items you're carrying are dropped."
        )
        explainGravestones()
        tutorialTopics()
    }

    // Note: OSRS draws a grave, coins or coffer picture beside these boxes; mesboxes here.
    private suspend fun Dialogue.explainGravestones() {
        mesbox(
            "A gravestone appears on the ground ${red("near where you died")}. You can go there " +
                "and retrieve your items by clicking on it."
        )
        mesbox(
            "Your gravestone would be marked with an arrow to help to spot it. A grave icon " +
                "appears on the ${red("world map")} too, if your grave is in a mapped area."
        )
        mesbox(
            "Gravestones charge a ${red("small fee")} to return especially valuable items, but " +
                "most things are returned free of charge. Conveniently, a gravestone can take " +
                "its fee ${red("straight from your bank")}."
        )
        mesbox(
            "A gravestone does not last forever. After ${red("about 15 minutes")}, a gravestone " +
                "will collapse. Anything in it will be sent here, to me."
        )
        death(
            "I will look after such items, ${red("without a time limit")}. However, I do charge " +
                "more for my services. It would be cheaper to retrieve them from the gravestone " +
                "directly, if you can reach it in time."
        )
    }

    private suspend fun Dialogue.tutorialTopics() {
        while (true) {
            val topic =
                choice5(
                    "<str>Tell me about gravestones again.</str>",
                    0,
                    topicText(TOPIC_FEE, "How do I pay a gravestone fee?"),
                    TOPIC_FEE,
                    topicText(TOPIC_TIME, "How long do I have to return to my gravestone?"),
                    TOPIC_TIME,
                    topicText(
                        TOPIC_ITEMS,
                        "How do I know what will happen to my items when I die?",
                    ),
                    TOPIC_ITEMS,
                    "I think I'm done here.",
                    -1,
                )
            when (topic) {
                0 -> gravestonesAgain()
                TOPIC_FEE -> payingFees()
                TOPIC_TIME -> howLong()
                TOPIC_ITEMS -> whatHappens()
                else -> {
                    finishTutorial()
                    return
                }
            }
            if (topic > 0) {
                player.deathTutorial = player.deathTutorial or topic
            }
        }
    }

    private fun Dialogue.topicText(topic: Int, text: String): String =
        if (player.deathTutorial and topic != 0) "<str>$text</str>" else text

    private suspend fun Dialogue.gravestonesAgain() {
        chatPlayer(quiz, "Tell me about gravestones again.")
        death(
            "Gladly... Across most of the land of Gielinor, when you die, some of the items " +
                "you're carrying are dropped."
        )
        explainGravestones()
    }

    private suspend fun Dialogue.payingFees() {
        chatPlayer(quiz, "How do I pay a gravestone fee?")
        death(
            "Cash is always acceptable. Also, a gravestone can take it directly from you bank, " +
                "in case you didn't have enough with you."
        )
        death(
            "Besides that, take a look at my Coffer here. You can bring your unwanted " +
                "possessions, and sacrifice them into my Coffer. I'll count their value against " +
                "any future gravestone fees you're charged."
        )
        mesbox(
            "Death's Coffer won't accept items worth less than ${red("10,000 coins per item")} " +
                "- Death doesn't want cheap junk! But more valuable items can be sacrificed to " +
                "pay down future reclamation fees."
        )
    }

    private suspend fun Dialogue.howLong() {
        chatPlayer(quiz, "How long do I have to return to my gravestone?")
        mesbox(
            "A gravestone lasts for ${red("at least 15 minutes")} before it collapses. However, " +
                "that timer ${red("pauses if you become inactive")}, in case you have lost your " +
                "connection to the world."
        )
        mesbox(
            "A timer ${red("on your head-up display")} shows the remaining time before your " +
                "gravestone will collapse."
        )
    }

    private suspend fun Dialogue.whatHappens() {
        chatPlayer(quiz, "How do I know what will happen to my items when I die?")
        death(
            "There is a menu to predict these things for you. Take a look at your Worn Items " +
                "side-panel..."
        )
        toplevelSidebuttonSwitch(player, WORN_ITEMS_SIDE)
        mesbox(
            "The ${red("Items Kept on Death")} menu tells you roughly what your items will do " +
                "when you next die, including ${red("estimating gravestone fees")}."
        )
    }

    private suspend fun Dialogue.finishTutorial() {
        chatPlayer(neutral, "I think I'm done here.")
        if (player.deathTutorial and ALL_TOPICS != ALL_TOPICS) {
            death(
                "I'm afraid we have more topics to cover before you leave. Speak to me when you " +
                    "are ready to continue."
            )
            return
        }
        player.deathTutorial = player.deathTutorial or TUTORIAL_DONE
        death(
            "Very well. Once you pass through my portal, you will have 15 minutes to reach your " +
                "gravestone."
        )
    }

    private suspend fun Dialogue.death(text: String) {
        chatNpcSpecific("Death", DeathNpcs.death, neutral, text)
    }

    private fun red(text: String): String = "<col=9f0000>$text</col>"

    private companion object {
        /** `death_tutorial` topic bits; see [GravestoneScript.TUTORIAL_DONE]. */
        const val TOPIC_FEE = 2
        const val TOPIC_TIME = 4
        const val TOPIC_ITEMS = 16
        const val ALL_TOPICS = TOPIC_FEE or TOPIC_TIME or TOPIC_ITEMS

        /** The worn equipment tab in the client's `toplevel_sidebutton_switch` order. */
        const val WORN_ITEMS_SIDE = 4
    }
}
