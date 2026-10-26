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
        // Note: the armour/pottery/glass/jewellery/weapons submenu is skipped.
        chatNpc(happy, "Is there anything else you want to know?")
        mainMenu()
    }
}
