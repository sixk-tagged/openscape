package org.rsmod.content.areas.city.lumbridge.npcs

import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.content.areas.city.lumbridge.configs.lumbridge_npcs
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

object FlavourNpcs : NpcReferences() {
    val father_urhney = find("father_urhney")
    val fred = find("fred_the_farmer")
    val gillie = find("gillie_the_milkmaid")
    val seth = find("favour_seth_groats")
    val sigmund = find("lost_tribe_sigmund")
    val abigale = find("mistmyst_abigale")
    val archer = find("archeradventurerpg")
    val warrior = find("warrioradventurerpg")
    val monk = find("monkadventurerpg")
    val wizard = find("wizardadventuterpg")
}

/** Out-of-quest Talk-to lines for Lumbridge's quest NPCs; the quest branches are left out. */
class FlavourNpcScript : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(lumbridge_npcs.cook) { startDialogue(it.npc) { cook() } }
        onOpNpc1(lumbridge_npcs.father_aereck) { startDialogue(it.npc) { fatherAereck() } }
        onOpNpc1(FlavourNpcs.father_urhney) { startDialogue(it.npc) { fatherUrhney() } }
        onOpNpc1(FlavourNpcs.fred) { startDialogue(it.npc) { fred() } }
        onOpNpc1(lumbridge_npcs.millie) { startDialogue(it.npc) { millie() } }
        onOpNpc1(FlavourNpcs.gillie) { startDialogue(it.npc) { gillie() } }
        onOpNpc1(FlavourNpcs.sigmund) { startDialogue(it.npc) { sigmund() } }
        onOpNpc1(FlavourNpcs.warrior) { startDialogue(it.npc) { warrior() } }
        onOpNpc1(FlavourNpcs.seth) {
            startDialogue(it.npc) { chatNpc(happy, "M'arnin'....going to milk me cowsies!") }
        }
        // Note: both start Misthalin Mystery; only the opening line is kept.
        onOpNpc1(FlavourNpcs.abigale) { startDialogue(it.npc) { chatNpc(worried, "Help! Help!") } }
        onOpNpc1(lumbridge_npcs.hewey) {
            startDialogue(it.npc) { chatNpc(sad, "I... I tried to save her...") }
        }
        onOpNpc1(FlavourNpcs.archer) {
            startDialogue(it.npc) {
                chatPlayer(quiz, "Why are you guys hanging around here?")
                chatNpc(angry, "(ahem)...'Guys'?")
                chatPlayer(
                    shifty,
                    "Uh... yeah, sorry about that. Why are you all standing around out here?",
                )
                chatNpc(neutral, "Well, that's really none of your business.")
            }
        }
        onOpNpc1(FlavourNpcs.monk) {
            startDialogue(it.npc) {
                chatPlayer(quiz, "Why are all of you standing around here?")
                chatNpc(angry, "None of your business. Get lost.")
            }
        }
        onOpNpc1(FlavourNpcs.wizard) {
            startDialogue(it.npc) {
                chatPlayer(quiz, "Why are all of you standing around here?")
                chatNpc(
                    laugh,
                    "Hahaha you dare talk to a mighty wizard such as myself? I bet you can't " +
                        "even cast windstrike yet amateur!",
                )
                chatPlayer(angry, "...You're an idiot.")
            }
        }
    }

    // Note: Cook's Assistant is not scripted, so he talks as if it is done.
    private suspend fun Dialogue.cook() {
        chatNpc(happy, "How is the adventuring going, my friend?")
        val choice =
            choice3(
                "I am getting strong and mighty.",
                1,
                "I keep on dying.",
                2,
                "Can I use your range?",
                3,
            )
        when (choice) {
            1 -> {
                chatPlayer(angry, "I am getting strong and mighty. Grrr")
                chatNpc(happy, "Glad to hear it.")
            }
            2 -> {
                chatPlayer(sad, "I keep on dying.")
                chatNpc(happy, "Ah well, at least you keep coming back to life!")
            }
            3 -> {
                chatPlayer(quiz, "Can I use your range?")
                chatNpc(
                    happy,
                    "Go ahead - it's a very good range. It's easier to use than most other " +
                        "ranges.",
                )
                chatNpc(
                    happy,
                    "Its called the Cook-o-matic 100. It uses a combination of state of the " +
                        "art temperature regulation and magic.",
                )
                chatPlayer(quiz, "Will it mean my food will burn less often?")
                chatNpc(neutral, "Well, that's what the salesman told us anyway...")
                chatPlayer(confused, "Thanks?")
            }
        }
    }

    private suspend fun Dialogue.fatherAereck() {
        chatNpc(happy, "Welcome to the church of holy Saradomin.")
        val choice = choice2("Who's Saradomin?", 1, "Nice place you've got here.", 2)
        if (choice == 2) {
            chatPlayer(happy, "Nice place you've got here.")
            chatNpc(happy, "It is, isn't it? It was built over 230 years ago.")
            return
        }
        chatPlayer(quiz, "Who's Saradomin?")
        chatNpc(shocked, "Surely you have heard of the god, Saradomin?")
        chatNpc(
            angry,
            "He who creates the forces of goodness and purity in this world? I cannot " +
                "believe your ignorance!",
        )
        chatNpc(
            neutral,
            "This is the god with more followers than any other! ...At least in this part " +
                "of the world.",
        )
        chatNpc(neutral, "He who created this world along with his brothers Guthix and Zamorak?")
        val notFromHere =
            choice2("Oh, THAT Saradomin...", false, "Oh, sorry. I'm not from this world.", true)
        if (!notFromHere) {
            chatPlayer(shifty, "Oh, THAT Saradomin...")
            chatNpc(neutral, "There... is only one Saradomin...")
            chatPlayer(shifty, "Yeah... I, uh, thought you said something else.")
            return
        }
        chatPlayer(neutral, "Oh, sorry. I'm not from this world.")
        chatNpc(confused, "...")
        chatNpc(confused, "That's... strange.")
        chatNpc(
            confused,
            "I thought things not from this world were all... You know. Slime and tentacles.",
        )
        val joke =
            choice2(
                "You don't understand. This is an online game!",
                false,
                "I am - do you like my disguise?",
                true,
            )
        if (!joke) {
            chatPlayer(neutral, "You don't understand. This is an online game!")
            chatNpc(confused, "I... beg your pardon?")
            chatPlayer(neutral, "Never mind.")
        } else {
            chatPlayer(laugh, "I am - do you like my disguise?")
            chatNpc(
                shocked,
                "Aargh! Avaunt foul creature from another dimension! Avaunt! Begone in the " +
                    "name of Saradomin!",
            )
            chatPlayer(laugh, "Ok, ok, I was only joking...")
        }
    }

    private suspend fun Dialogue.fatherUrhney() {
        chatNpc(angry, "Go away! I'm meditating!")
        val repossess =
            choice2("Well, that's friendly.", false, "I've come to repossess your house.", true)
        if (!repossess) {
            chatPlayer(neutral, "Well, that's friendly.")
            chatNpc(angry, "I SAID go AWAY.")
            chatPlayer(neutral, "Okay, okay... sheesh, what a grouch.")
            return
        }
        chatPlayer(neutral, "I've come to repossess your house.")
        chatNpc(shocked, "Under what grounds???")
        val mortgage =
            choice2(
                "Repeated failure on mortgage repayments.",
                true,
                "I don't know, I just wanted this house.",
                false,
            )
        if (mortgage) {
            chatPlayer(neutral, "Repeated failure on mortgage repayments.")
            chatNpc(shocked, "What?")
            chatNpc(angry, "But... I don't have a mortgage! I built this house myself!")
            chatPlayer(
                shifty,
                "Sorry. I must have got the wrong address. All the houses look the same " +
                    "around here.",
            )
            chatNpc(confused, "What? What houses? What ARE you talking about???")
            chatPlayer(shifty, "Never mind.")
        } else {
            chatPlayer(shifty, "I don't know. I just wanted this house...")
            chatNpc(angry, "Oh... go away and stop wasting my time!")
        }
    }

    // Note: Sheep Shearer is not scripted; these are his lines once it is done.
    private suspend fun Dialogue.fred() {
        chatNpc(angry, "What are you doing on my land?")
        val choice = choice2("I'm looking for something to kill.", 1, "I'm lost.", 2)
        if (choice == 1) {
            chatPlayer(neutral, "I'm looking for something to kill.")
            chatNpc(angry, "What, on my land? Leave my livestock alone you scoundrel!")
        } else {
            chatPlayer(confused, "I'm lost.")
            chatNpc(
                neutral,
                "How can you be lost? Just follow the road east and south. You'll end up in " +
                    "Lumbridge fairly quickly.",
            )
        }
    }

    private suspend fun Dialogue.millie() {
        chatNpc(happy, "Hello Adventurer. Welcome to Mill Lane Mill. Can I help you?")
        val choice =
            choice4(
                "Who are you?",
                1,
                "What is this place?",
                2,
                "How do I mill flour?",
                3,
                "I'm fine, thanks.",
                4,
            )
        when (choice) {
            1 -> {
                chatPlayer(quiz, "Who are you?")
                chatNpc(
                    happy,
                    "I'm Miss Millicent Miller the Miller of Mill Lane Mill. Our family have " +
                        "been milling flour for generations.",
                )
                chatPlayer(happy, "It's a good business to be in. People will always need flour.")
                millFlour()
            }
            2 -> {
                chatPlayer(quiz, "What is this place?")
                chatNpc(
                    happy,
                    "This is Mill Lane Mill. Millers of the finest flour in Gielinor, and home " +
                        "to the Miller family for many generations",
                )
                chatNpc(happy, "We take grain from the field nearby and mill into flour.")
                millFlour()
            }
            3 -> millFlour()
            4 -> chatPlayer(neutral, "I'm fine, thanks.")
        }
    }

    private suspend fun Dialogue.millFlour() {
        chatPlayer(quiz, "How do I mill flour?")
        chatNpc(
            happy,
            "Making flour is pretty easy. First of all you need to get some grain. You can " +
                "pick some from wheat fields. There is one just outside the Mill, but there " +
                "are many others scattered across Gielinor. Feel free to pick wheat",
        )
        chatNpc(happy, "from our field! There always seems to be plenty of wheat there.")
        chatPlayer(quiz, "Then I bring my wheat here?")
        chatNpc(
            neutral,
            "Yes, or one of the other mills in Gielinor. They all work the same way. Just " +
                "take your grain to the top floor of the mill (up two ladders, there are " +
                "three floors including this one) and then place some grain into the hopper.",
        )
        chatNpc(
            neutral,
            "Then you need to start the grinding process by pulling the hopper lever. You " +
                "can add more grain, but each time you add grain you have to pull the " +
                "hopper lever again.",
        )
        chatPlayer(quiz, "So where does the flour go then?")
        chatNpc(
            neutral,
            "The flour appears in this room here, you'll need a pot to put the flour into. " +
                "One pot will hold the flour made by one load of grain",
        )
        chatNpc(
            happy,
            "And that's it! You now have some pots of finely ground flour of the highest " +
                "quality. Ideal for making tasty cakes or delicous bread. I'm not a cook so " +
                "you'll have to ask a cook to find out how to bake things.",
        )
        chatPlayer(happy, "Great! Thanks for your help.")
    }

    private suspend fun Dialogue.gillie() {
        chatNpc(happy, "Hello, I'm Gillie the Milkmaid. What can I do for you?")
        val choice =
            choice3(
                "Who are you?",
                1,
                "Can you tell me how to milk a cow?",
                2,
                "I'm fine, thanks.",
                3,
            )
        when (choice) {
            1 -> {
                chatNpc(
                    happy,
                    "My name is Gillie Groats. My father is a farmer and I milk the cows " +
                        "for him.",
                )
                chatPlayer(quiz, "Do you have any buckets of milk spare?")
                chatNpc(
                    neutral,
                    "I'm afraid not. We need all of our milk to sell to market, but you can " +
                        "milk the cow yourself if you need the milk.",
                )
                chatPlayer(happy, "Thanks.")
            }
            2 -> {
                chatPlayer(quiz, "So how do you get milk from a cow then?")
                chatNpc(happy, "It's very easy. First you need an empty bucket to hold the milk.")
                chatNpc(happy, "Then find a dairy cow to milk - you can't milk just any cow.")
                chatPlayer(quiz, "How do I find a dairy cow?")
                chatNpc(
                    happy,
                    "They are easy to spot - they are dark brown and white, unlike beef " +
                        "cows, which are light brown and white. We also tether them to a " +
                        "post to stop them wandering around all over the place.",
                )
                chatNpc(happy, "There are a couple very near, in this field.")
                chatNpc(
                    happy,
                    "Then just milk the cow and your bucket will fill with tasty, " +
                        "nutritious milk.",
                )
            }
            3 -> chatPlayer(neutral, "I'm fine, thanks.")
        }
    }

    private suspend fun Dialogue.sigmund() {
        chatNpc(neutral, "Can I help you?")
        val choice = choice2("Do you have any quests for me?", 1, "Who are you?", 2)
        if (choice == 1) {
            chatPlayer(quiz, "Do you have any quests for me?")
            chatNpc(
                neutral,
                "I hear the Duke has a task for an adventurer. Otherwise, if you want to " +
                    "make yourself useful, there are always evil monsters to slay.",
            )
            chatPlayer(neutral, "Okay, I might just do that.")
        } else {
            chatPlayer(quiz, "Who are you?")
            chatNpc(neutral, "I'm the Duke's advisor.")
            chatPlayer(quiz, "Can you give me any advice then?")
            chatNpc(
                neutral,
                "I only advise the Duke. But if you want to make yourself useful, there are " +
                    "evil goblins to slay on the other side of the river.",
            )
        }
    }

    private suspend fun Dialogue.warrior() {
        chatNpc(neutral, "Hello there traveller.")
        val choice =
            choice2(
                "What are you camped out here for?",
                1,
                "Do you know any good adventures I can go on?",
                2,
            )
        if (choice == 1) {
            warriorCamped()
            return
        }
        chatPlayer(quiz, "Do you know any good adventures I can go on?")
        chatNpc(
            neutral,
            "Well we're on an adventure right now. Mind you, this is OUR adventure and we " +
                "don't want to share it - find your own!",
        )
        val beg =
            choice2(
                "Please tell me.",
                true,
                "I don't think you've found a good adventure at all!",
                false,
            )
        if (beg) {
            chatPlayer(quiz, "Please tell me?")
            chatNpc(neutral, "No.")
            chatPlayer(sad, "Please?")
            chatNpc(angry, "No!")
            chatPlayer(sad, "PLEEEEEEEEEEEEEEEEEEEEEASE?")
            chatNpc(angry, "NO!")
        } else {
            chatPlayer(neutral, "I don't think you've found a good adventure at all!")
            chatNpc(
                angry,
                "Hah! Adventurers of our calibre don't just hang around in forests for fun, " +
                    "whelp!",
            )
            chatPlayer(neutral, "Oh really?")
            warriorCamped()
        }
    }

    // Note: the Zanaris follow-ups lead into Lost City and are left out.
    private suspend fun Dialogue.warriorCamped() {
        chatPlayer(quiz, "What are you camped here for?")
        chatNpc(
            shifty,
            "We're looking for Zanaris...GAH! I mean we're not here for any particular " +
                "reason at all.",
        )
    }
}
