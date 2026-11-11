package org.rsmod.content.areas.city.lumbridge.npcs

import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.baseCraftingLvl
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.game.entity.Npc
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

object CraftingTutorNpcs : NpcReferences() {
    val tutor = find("aide_tutor_crafting")
}

class CraftingTutor : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(CraftingTutorNpcs.tutor) { startDialogue(it.npc) }
    }

    private suspend fun ProtectedAccess.startDialogue(npc: Npc) {
        startDialogue(npc) {
            chatPlayer(happy, "Hello.")
            chatNpc(happy, "Hello there! Are you interested in hearing all about crafting?")
            mainMenu()
        }
    }

    private suspend fun Dialogue.mainMenu() {
        val choice =
            choice3(
                "How can I train my crafting?",
                1,
                "What kinds of things can be crafted?",
                2,
                "No, thank you.",
                3,
            )
        when (choice) {
            1 -> training()
            2 -> kindsOfCrafts()
            3 -> {
                chatPlayer(neutral, "No, thank you.")
                chatNpc(happy, "Well, just come back any time you want to know anything!")
            }
        }
    }

    private suspend fun Dialogue.training() {
        chatPlayer(quiz, "How can I train my crafting?")
        val lvl = player.baseCraftingLvl
        if (lvl < 10) {
            chatNpc(
                happy,
                "To get started, you might like to try your hand at crafting some armour from " +
                    "cow's leather. There's a tanner just over in Al Kharid who could tan any " +
                    "hides for you.",
            )
            chatNpc(
                happy,
                "Once you've managed to get your hand on some kind of leather, you just need a " +
                    "needle and some thread to get to work!",
            )
            chatNpc(
                happy,
                "If that doesn't take your fancy, you could try your hand at spinning wool " +
                    "right here on the spinning wheel.",
            )
            chatNpc(
                happy,
                "Come to think of it... Farmer Fred, just north of here, sounds like he could " +
                    "use a hand shearing some of his sheep. Maybe you could help him out, and " +
                    "get some practice while you're at it.",
            )
        } else if (lvl < 20) {
            chatNpc(
                happy,
                "Lots of budding craftsmen seem to spend their time here spinning Flax into bow " +
                    "strings on the wheel, if that takes your fancy.",
            )
            chatNpc(
                happy,
                "Or you might like to try your hand at crafting some armour from cow's leather. " +
                    "There's a tanner just over in Al Kharid who could tan any hides for you.",
            )
        } else {
            if (lvl < 99) {
                chatNpc(
                    happy,
                    "Now that you've got a little experience under your belt, the world's your " +
                        "oyster!",
                )
            } else {
                chatNpc(
                    happy,
                    "I can't help but feel like I should be the one asking you! At this point, " +
                        "the world's your oyster!",
                )
            }
            chatNpc(
                happy,
                "If you wanted to try your hand at making some jewellery, you'll want to get " +
                    "your hand on some gold and some kind of gem, with a mould to work with and " +
                    "a chisel at hand.",
            )
            chatNpc(
                happy,
                "But if that's not your thing, maybe you'd like to try making some more armour!",
            )
            chatNpc(
                happy,
                "Cow's leather's a good place to start, but you can try all other kinds of " +
                    "hides, like a dragon's!",
            )
            chatNpc(
                happy,
                "Glass blowing seems to be a popular trade, too. You can forge molten glass from " +
                    "some sand and soda ash, then make all kinds of things using a glass " +
                    "blowing pipe.",
            )
        }
        chatPlayer(happy, "Thanks!")
        chatNpc(happy, "Is there anything else you want to know?")
        mainMenu()
    }

    private suspend fun Dialogue.kindsOfCrafts() {
        chatPlayer(quiz, "What kinds of things can be crafted?")
        chatNpc(
            happy,
            "All kinds of things, really! You can make armour from leather, make some pottery, " +
                "try your hand at glass blowing, make some jewellery, weapons even...",
        )
        chatNpc(happy, "What would you like to hear about?")
        topicMenu(lastAsked = null)
    }

    /**
     * OSRS menus hold five options: once a topic is heard, it gives way to "Not right now." (the
     * wiki only says that option appears after asking).
     */
    private suspend fun Dialogue.topicMenu(lastAsked: Int?) {
        val options = TOPICS.indices.filter { it != lastAsked }.map { TOPICS[it] to it }
        val shown = if (lastAsked == null) options else options + ("Not right now." to NOT_NOW)
        val choice =
            when (shown.size) {
                5 ->
                    choice5(
                        shown[0].first,
                        shown[0].second,
                        shown[1].first,
                        shown[1].second,
                        shown[2].first,
                        shown[2].second,
                        shown[3].first,
                        shown[3].second,
                        shown[4].first,
                        shown[4].second,
                    )
                else -> error("Unexpected menu size: ${shown.size}")
            }
        if (choice == NOT_NOW) {
            chatPlayer(neutral, "Not right now.")
            chatNpc(happy, "Is there anything else you want to know?")
            if (choice2("How can I train my crafting?", true, "No, thank you.", false)) {
                training()
            } else {
                chatPlayer(neutral, "No, thank you.")
                chatNpc(happy, "Well, just come back any time you want to know anything!")
            }
            return
        }
        chatPlayer(quiz, TOPICS[choice])
        when (choice) {
            0 -> armour()
            1 -> pottery()
            2 -> glassBlowing()
            3 -> jewellery()
            4 -> weapons()
        }
        chatNpc(happy, "Would you like to hear about anything else I mentioned?")
        topicMenu(lastAsked = choice)
    }

    private suspend fun Dialogue.armour() {
        chatNpc(
            happy,
            "Sure thing! Most armour you can craft just involves taking a needle and some thread " +
                "to whatever material you can get your hands on.",
        )
        chatNpc(
            happy,
            "You might like to try with different kinds of animal hide, from cows, yaks, snakes, " +
                "dragons... Or any other sort of fabric you can find!",
        )
        chatNpc(
            happy,
            "Some crafty types have started making some leather- covered wooden shields " +
                "recently, too!",
        )
        chatNpc(
            happy,
            "You might find some creatures make for a pretty sturdy helmet, too. The best way to " +
                "make something like that's just to take a good old chisel to it!",
        )
        chatPlayer(happy, "That sounds great.")
    }

    private suspend fun Dialogue.pottery() {
        chatNpc(
            happy,
            "Of course! Pottery's all kind of the same, it just takes a lot of getting used to.",
        )
        chatNpc(
            happy,
            "All that there really is to it is getting your hands on some clay, getting it wet " +
                "and getting to work on a potter's wheel.",
        )
        chatNpc(
            happy,
            "Once you've got the shape you want out of it, you want to put it in a pottery oven " +
                "to fire it up.",
        )
        chatNpc(
            happy,
            "The barbarians west of Varrock seem to be pretty keen on their pottery. I'd take a " +
                "look around there, if you wanted to try it out!",
        )
        chatNpc(
            happy,
            "Once you get the hang of it, you can make pots, pie dishes, bowls... You know.",
        )
        chatPlayer(happy, "Neat!")
    }

    private suspend fun Dialogue.glassBlowing() {
        chatNpc(
            happy,
            "Glass blowing? Well, to get started you'll need to get yourself some molten glass. " +
                "You can make some for yourself by heating sand and soda ash in a furnace.",
        )
        chatNpc(
            happy,
            "Once you've got a hold of that, you'll want a glass blowing pipe to blow it out in " +
                "to shape, before it cools down.",
        )
        chatNpc(
            happy,
            "It's not the easiest thing to get to grips with, but it shouldn't be too difficult!",
        )
        chatPlayer(happy, "Nifty.")
    }

    private suspend fun Dialogue.jewellery() {
        chatNpc(
            happy,
            "Sure. All it boils down to is a bar of gold or silver, some kind of cut gem if " +
                "you're feeling fancy, and a mould to help shape the thing.",
        )
        chatNpc(
            happy,
            "You can use a chisel to make a gem really shine and sparkle. You wouldn't want to " +
                "use an uncut one for any jewellery.",
        )
        chatNpc(
            happy,
            "Once you've got all of your materials together, you'll want to use a furnace to " +
                "actually craft whatever it is you're trying to make!",
        )
        chatNpc(
            happy,
            "Wizards seem to like enchanting their jewellery to do all kinds of things, too, but " +
                "I don't know a lot about that.",
        )
        chatPlayer(happy, "Sounds good!")
    }

    private suspend fun Dialogue.weapons() {
        chatNpc(
            happy,
            "Well, there aren't too many weapons that people tend to craft, but there are a " +
                "couple worth noting!",
        )
        chatNpc(
            happy,
            "Battlestaves are probably the most common. It boils down to fastening an orb of " +
                "some kind onto the end of a battlestaff.",
        )
        chatNpc(
            happy,
            "You do need to get the orb in the first place, though... You can make one through " +
                "glass blowing, but it's not much use without being charged.",
        )
        chatNpc(
            happy,
            "I can't help much with crafting it, though! There's a little more magic involved " +
                "there than I'm familiar with.",
        )
        chatNpc(
            happy,
            "Aside from battlestaves, there are a couple of weapons that people like to make " +
                "from silver... Sickles and bolts, usually.",
        )
        chatPlayer(happy, "Interesting...")
    }

    private companion object {
        val TOPICS =
            listOf(
                "Tell me about crafting armour.",
                "Tell me about pottery.",
                "Tell me about glass blowing.",
                "Tell me about making jewellery.",
                "Tell me about crafting weapons.",
            )
        const val NOT_NOW = -1
    }
}
