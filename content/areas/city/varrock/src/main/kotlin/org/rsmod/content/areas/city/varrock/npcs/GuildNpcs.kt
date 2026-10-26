package org.rsmod.content.areas.city.varrock.npcs

import jakarta.inject.Inject
import org.rsmod.api.config.refs.objs
import org.rsmod.api.invtx.invAddOrDrop
import org.rsmod.api.invtx.invTakeFee
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.stat.cookingLvl
import org.rsmod.api.player.stat.smithingLvl
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.script.onOpNpc1
import org.rsmod.content.areas.city.varrock.configs.varrock_npcs
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** The Head chef of the Cooks' Guild, the Master smithing tutor and the Guildmaster. */
class GuildNpcs @Inject constructor(private val objRepo: ObjRepository) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(varrock_npcs.head_chef) { startDialogue(it.npc) { headChef() } }
        onOpNpc1(varrock_npcs.smithing_tutor) { startDialogue(it.npc) { smithingTutor() } }
        onOpNpc1(varrock_npcs.guildmaster) { startDialogue(it.npc) { guildmaster() } }
    }

    // Note: Treasure Trail challenge scrolls are skipped (no clues).
    private suspend fun Dialogue.headChef() {
        if (player.cookingLvl < 99) {
            chatNpc(
                happy,
                "Hello, welcome to the Cooking Guild. Only accomplished chefs and cooks are " +
                    "allowed in here. Feel free to use any of our facilities.",
            )
            val choice = choice2("Nice cape you're wearing!", 1, "Thanks, bye.", 2)
            if (choice == 1) {
                chatPlayer(happy, "Nice cape, you're wearing!")
                chatNpc(
                    happy,
                    "Thank you! It's my most prized possession, it's a Skillcape of Cooking; it " +
                        "shows that I've achieved level 99 Cooking and am one of the best chefs " +
                        "in the land!",
                )
                chatNpc(
                    happy,
                    "If you ever achieve level 99 cooking you'll get to wear one too and doing " +
                        "so means you'll never burn any food!",
                )
            } else {
                chatPlayer(neutral, "Thanks, bye.")
            }
            return
        }
        chatNpc(
            happy,
            "Hello, welcome to the Cooking Guild. It's always great to have such an " +
                "accomplished chef visit us. Say, would you be interested in a Skillcape of " +
                "Cooking? They're only available to master chefs.",
        )
        val choice = choice2("No thanks.", 1, "Yes please.", 2)
        if (choice == 1) {
            chatPlayer(neutral, "No thanks.")
            chatNpc(neutral, "Okay, come back to me if you change your mind.")
            return
        }
        if (ownsCape()) {
            val which = choice2("Skillcape", 1, "Hood", 2)
            if (which == 2) {
                chatPlayer(quiz, "May I have another hood for my cape, please?")
                chatNpc(happy, "Most certainly, and free of charge!")
                player.invAddOrDrop(objRepo, objs.cooking_hood)
                mesbox("The head chef hands you another hood for your skillcape.")
                return
            }
        }
        buyCape()
    }

    private fun Dialogue.ownsCape(): Boolean =
        access.invTotal(access.inv, objs.cooking_skillcape) > 0 ||
            access.invTotal(access.bank, objs.cooking_skillcape) > 0 ||
            access.invTotal(access.inv, objs.cooking_skillcape_t) > 0

    private suspend fun Dialogue.buyCape() {
        chatPlayer(quiz, "Can I buy a Skillcape of Cooking from you?")
        chatNpc(
            happy,
            "Most certainly, by wearing this cape you'll never burn any food, I will have to " +
                "ask for 99000 gold coins for such a privilege.",
        )
        val choice = choice2("That's much too expensive.", 1, "Sure.", 2)
        if (choice == 1) {
            chatPlayer(neutral, "That's much too expensive.")
            chatNpc(sad, "I'm sorry you feel that way.")
            return
        }
        chatPlayer(neutral, "Sure.")
        if (access.invCoinTotal() < CAPE_PRICE) {
            chatPlayer(sad, "But, unfortunately, I don't have enough money with me.")
            chatNpc(neutral, "Well, come back and see me when you do.")
            return
        }
        if (access.inv.freeSpace() < 2) {
            chatNpc(
                neutral,
                "Unfortunately all Skillcapes are only available with a free hood, it's part of " +
                    "a skill promotion deal; buy one get one free, you know. So you'll need to " +
                    "free up some inventory space before I can sell you one.",
            )
            return
        }
        player.invTakeFee(CAPE_PRICE)
        player.invAddOrDrop(objRepo, objs.cooking_skillcape)
        player.invAddOrDrop(objRepo, objs.cooking_hood)
        chatNpc(happy, "Now you can use the title Master Chef.")
    }

    private suspend fun Dialogue.smithingTutor() {
        chatPlayer(neutral, "Hello.")
        chatNpc(happy, "Well met! Are you interested in hearing about the art of smithing?")
        smithingMenu()
    }

    private suspend fun Dialogue.smithingMenu() {
        val choice =
            choice3(
                "How can I train my smithing?",
                1,
                "What kind of things can I smith?",
                2,
                "Not right now, thank you.",
                3,
            )
        when (choice) {
            1 -> {
                chatPlayer(quiz, "How can I train my smithing?")
                giveHammer()
                trainingAdvice()
                chatNpc(quiz, "Is there anything else you would like to know?")
                smithingMenu()
            }
            2 -> {
                chatPlayer(quiz, "What kind of things can I smith?")
                chatNpc(
                    happy,
                    "There are many things you can make, from weapons to your good old " +
                        "fashioned armour.",
                )
                chatNpc(
                    neutral,
                    "Weapons are the cheapest to smith. They range from a measly one bar, all " +
                        "the way to three bars.",
                )
                chatNpc(
                    neutral,
                    "Armour can be the costliest item to smith, the cost of each item ranges " +
                        "from a measly one bar all the way up to a whopping five bars.",
                )
                chatNpc(
                    neutral,
                    "Some weapons and armour, such as darts, will require you to have gained " +
                        "knowledge on how to smith them. This is due to the complex nature of " +
                        "the weapon.",
                )
                chatNpc(
                    neutral,
                    "You might find that other items don't require conventional bars you " +
                        "would gather. Some may require you to piece blades back together or " +
                        "even infuse a crystal into a piece of armour.",
                )
                chatNpc(quiz, "Is there anything else you want to know?")
                smithingMenu()
            }
            3 -> {
                chatPlayer(neutral, "Not right now, thank you.")
                chatNpc(happy, "Well, just come back any time you want to know anything!")
            }
        }
    }

    private suspend fun Dialogue.giveHammer() {
        if (access.invTotal(access.inv, objs.hammer) > 0) {
            chatNpc(happy, "I see you already have a hammer. Nice one.")
            return
        }
        if (access.inv.isFull()) {
            chatNpc(neutral, "To be able to smith anything, you're going to need a hammer.")
            chatNpc(
                neutral,
                "Hammers can be purchased in most general stores, or picked up for free in the " +
                    "Dwarven Mine.",
            )
            return
        }
        chatNpc(
            neutral,
            "To be able to smith anything, you're going to need one of these beauties.",
        )
        player.invAddOrDrop(objRepo, objs.hammer)
        objbox(objs.hammer, "The Master Smithing tutor gives you a hammer.")
    }

    // Note: the level 21-37 tier skips the Knight's Sword quest hint and the 99 tier skips
    // the Barbarian Smithing line, as neither quest is tracked.
    private suspend fun Dialogue.trainingAdvice() {
        val level = player.smithingLvl
        when {
            level <= 20 -> {
                chatNpc(
                    neutral,
                    "You're going to need to get your hand on some metal bars. You could do " +
                        "this by mining your own ores and smelting them at a furnace.",
                )
                chatNpc(
                    neutral,
                    "There is a furnace located in Lumbridge, just north of the castle " +
                        "opposite the general store.",
                )
                chatNpc(
                    neutral,
                    "If you are looking for some ore, there is a mine east of Varrock. There " +
                        "you can find some copper and tin ore. Don't forget to bring a pickaxe.",
                )
                chatNpc(
                    neutral,
                    "When you have your bars, bring them to an anvil to open the smithing " +
                        "interface. If the item name is in black, this means you do not have " +
                        "the level to smith the item.",
                )
                chatNpc(
                    neutral,
                    "If the item name is in white, this means you do have the level to smith " +
                        "the item. You will see the bars required to smith the item underneath " +
                        "the name of the item.",
                )
                chatNpc(
                    neutral,
                    "If the number of bars is in orange, this means you do not have enough " +
                        "bars to smith the item. If it is in green, this means you have " +
                        "enough bars for the item.",
                )
                chatPlayer(happy, "Thanks for the advice.")
            }
            level <= 37 -> {
                chatNpc(
                    neutral,
                    "You might find it beneficial to keep a nice stack of bars in your bank " +
                        "before you start smithing. Unless you like to jump from task to task " +
                        "of course.",
                )
                chatNpc(
                    neutral,
                    "If you want to smith something that you may not have the level for, you " +
                        "could try gaining some temporary knowledge in smithing from a boost.",
                )
                chatNpc(neutral, "You can buy a Dwarven stout at the Rising Sun Inn in Falador.")
            }
            else -> {
                if (level >= 99) {
                    chatNpc(
                        shocked,
                        "Cor blimey, I don't think there is anything I can teach you, but " +
                            "I'll do my best.",
                    )
                }
                chatNpc(
                    neutral,
                    "As you get better you'll find you will be able to smith Mithril and " +
                        "eventually Adamantite and even Runite. This can be very lucrative but " +
                        "very expensive on the coal front.",
                )
                chatNpc(
                    neutral,
                    "It may be worth you stockpiling coal for a while before attempting these " +
                        "difficult metals or even sticking to good old reliable iron by the " +
                        "bucket load.",
                )
                chatNpc(
                    neutral,
                    "If you want to stop yourself from failing to smith iron, I suggest " +
                        "purchasing some rings of forging. Be aware that they will break after " +
                        "a certain number of bars are smelted.",
                )
                chatNpc(
                    neutral,
                    "If you want to save on coal, you could try smithing your bars over at " +
                        "the Blast Furnace in Keldagrim.",
                )
                chatNpc(
                    neutral,
                    "If you are looking for something more interesting to smith, you could " +
                        "talk to Otto Godblessed about smithing hastae.",
                )
            }
        }
    }

    // Note: the maze key and champion's scroll options belong to quests and the Champions'
    // Challenge, which are not built; the door has no quest point check since all are met.
    private suspend fun Dialogue.guildmaster() {
        chatNpc(happy, "Greetings!")
        val choice = choice2("What is this place?", 1, "Nothing, thanks.", 2)
        if (choice == 1) {
            chatPlayer(quiz, "What is this place?")
            chatNpc(
                neutral,
                "This is the Champions' Guild. Only adventurers who have proved themselves " +
                    "worthy by gaining influence from quests are allowed in here.",
            )
        } else {
            chatPlayer(neutral, "Nothing, thanks.")
        }
    }

    private companion object {
        const val CAPE_PRICE = 99_000
    }
}
