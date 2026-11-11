package org.rsmod.content.areas.city.lumbridge.npcs

import jakarta.inject.Inject
import org.rsmod.api.config.refs.objs
import org.rsmod.api.invtx.invAdd
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.righthand
import org.rsmod.api.player.stat.baseFishingLvl
import org.rsmod.api.player.stat.baseSmithingLvl
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.content.areas.city.lumbridge.configs.lumbridge_npcs
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.obj.Wearpos
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

object LumbridgeGuideObjs : ObjReferences() {
    val quest_start_icon = find("quest_start_icon_dummy")
    val combat_tutor_icon = find("combat_tutor_icon_dummy")
    val smithing_tutor_icon = find("smithing_tutor_icon_dummy")
    val mining_icon = find("mining_site_icon_dummy")
    val fishing_icon = find("fishing_spot_icon_dummy")
    val range_icon = find("range_icon_dummy")
    val net = find("net")
}

/**
 * Phileas, as the wiki transcript has him at rev 233. Quests aren't tracked, so he always points at
 * Cook's Assistant, as he does before any quest is started.
 */
// Note: the wiki gives no level cut-offs for "strong enough", "reasonable Smithing" and
// "experienced fisher"; 30 is a guess for all three. "More information" doesn't open the account
// tab's useful links, though he says it did.
class LumbridgeGuide @Inject constructor(private val objTypes: ObjTypeList) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(lumbridge_npcs.guide) {
            startDialogue(it.npc) {
                chatNpc(
                    happy,
                    "Greetings, adventurer. I am Phileas, the Lumbridge Guide. I am here to give " +
                        "information and directions to new players. Is there anything I can " +
                        "help you with?",
                )
                mainMenu()
            }
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

    private suspend fun Dialogue.anythingElse() {
        chatNpc(happy, "Is there anything else you need help with?")
        if (choice2("Yes please.", true, "No thank you.", false)) {
            chatPlayer(happy, "Yes please.")
            mainMenu()
        } else {
            chatPlayer(neutral, "No thank you.")
        }
    }

    private suspend fun Dialogue.somethingElse() {
        chatPlayer(neutral, "I'd like to know about something else.")
        chatNpc(happy, "Well, what would you like to know?")
        mainMenu()
    }

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

    /* Combat */

    private suspend fun Dialogue.monsters() {
        chatPlayer(quiz, "What monsters should I fight?")
        if (player.combatLevel >= STRONG_COMBAT_LEVEL) {
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
        } else if (!hasWeapon()) {
            chatNpc(
                neutral,
                "It's going to be tough fighting monsters without a weapon. I suggest you find " +
                    "one of those first.",
            )
            noWeaponMenu()
        } else {
            whatToFight()
        }
    }

    private fun Dialogue.hasWeapon(): Boolean =
        player.righthand != null ||
            player.inv.objs.any { it != null && objTypes[it].wearpos1 == Wearpos.RightHand.slot }

    private suspend fun Dialogue.noWeaponMenu() {
        val choice =
            choice3(
                "How can I get a weapon?",
                1,
                "Could I get a staff like yours?",
                2,
                "Yes, but what should I fight once I have got a weapon?",
                3,
            )
        when (choice) {
            1 -> {
                chatPlayer(quiz, "How can I get a weapon?")
                chatNpc(happy, "Well you can either make them, find them or buy them.")
                getWeaponMenu()
            }
            2 -> staff { noWeaponMenu() }
            3 -> {
                chatPlayer(quiz, "Yes, but what should I fight once I have got a weapon?")
                whatToFight()
            }
        }
    }

    private suspend fun Dialogue.getWeaponMenu() {
        val choice =
            choice3(
                "Where can I find a weapon?",
                1,
                "How do I make a weapon?",
                2,
                "Where can I buy a weapon?",
                3,
            )
        when (choice) {
            1 -> findWeapon()
            2 -> makeWeapon()
            3 -> buyWeapon { getWeaponMenu() }
        }
    }

    private suspend fun Dialogue.weaponsFollowUp(previous: suspend Dialogue.() -> Unit) {
        chatNpc(happy, "Is there anything else you need help with?")
        val choice =
            choice3(
                "No thank you.",
                1,
                "I'd like to know more about getting weapons.",
                2,
                "I'd like to know about something else.",
                3,
            )
        when (choice) {
            1 -> chatPlayer(neutral, "No thank you.")
            2 -> previous()
            3 -> somethingElse()
        }
    }

    private suspend fun Dialogue.findWeapon() {
        chatPlayer(quiz, "Where can I find a weapon?")
        chatNpc(
            happy,
            "If you visit the combat tutors south of the general store they may be able to help " +
                "you with that. Failing that there are a few cheap weapons lying around in the " +
                "world. For example, you can get an iron dagger if you",
        )
        chatNpc(
            happy,
            "cross the bridge and look in the little hut where some goblins live. If it's not " +
                "there it probably means someone else has taken it. If you wait a while a new " +
                "one should appear. As you explore you may find other weapons,",
        )
        chatNpc(happy, "too.")
        weaponsFollowUp { getWeaponMenu() }
    }

    private suspend fun Dialogue.makeWeapon() {
        chatPlayer(quiz, "How do I make a weapon?")
        if (player.baseSmithingLvl >= SKILLED_LEVEL) {
            chatNpc(
                happy,
                "You already have a reasonable Smithing level, so I think you know that already.",
            )
            return
        }
        chatNpc(
            happy,
            "The Smithing skill allows you to make armour and weapons. Talk to the boy who " +
                "smelts metal in the furnace, I'm sure he can help.",
        )
        chatPlayer(quiz, "Where can I smith?")
        whereToSmith()
    }

    private suspend fun Dialogue.whereToSmith() {
        chatNpc(
            happy,
            "You will find a helpful Smithing tutor in the west of Varrock - that's north of " +
                "here. Follow the path across the river and head north.",
        )
        objbox(LumbridgeGuideObjs.smithing_tutor_icon, "Look for this icon in the west of Varrock.")
        objbox(
            LumbridgeGuideObjs.mining_icon,
            "The minimap in the top right corner of the game screen has various icons showing " +
                "the locations of things. This icon to the left shows where Mining sites are.",
        )
        chatNpc(
            happy,
            "I suggest you go and mine some ore; find the Mining symbol - with the guide symbol " +
                "near it - in the swamp south of here. The Mining guide there can teach you how " +
                "to mine ore.",
        )
        anythingElse()
    }

    private suspend fun Dialogue.buyWeapon(previous: suspend Dialogue.() -> Unit) {
        chatPlayer(quiz, "Where can I buy a weapon?")
        chatNpc(
            happy,
            "You can get a weapon free from the combat tutors if they think you need it. Failing " +
                "that, the nearest shop that would sell you something of that nature is Bob's " +
                "Brilliant Axes in this very town. If you want a bigger",
        )
        chatNpc(
            happy,
            "choice I suggest you head to one of the big cities. If you follow the road east " +
                "over the bridge and then head north you will eventually reach Varrock, where " +
                "you can buy all manner of things. You can use the world map",
        )
        chatNpc(happy, "to help you find your way.")
        weaponsFollowUp(previous)
    }

    private suspend fun Dialogue.staff(previous: suspend Dialogue.() -> Unit) {
        chatPlayer(quiz, "Could I get a staff like yours?")
        chatNpc(
            neutral,
            "There is no other staff like this in all the land. It's a very important staff. It " +
                "shows who holds the job as the Lumbridge Guide, and that's me.",
        )
        chatNpc(happy, "Is there anything else you need help with?")
        val choice =
            choice3(
                "I'd like to know more about getting weapons.",
                1,
                "I'd like to know about something else.",
                2,
                "No thank you.",
                3,
            )
        when (choice) {
            1 -> previous()
            2 -> somethingElse()
            3 -> chatPlayer(neutral, "No thank you.")
        }
    }

    private suspend fun Dialogue.whatToFight() {
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
        fightMenu()
    }

    private suspend fun Dialogue.fightMenu() {
        val choice =
            choice4(
                "Where can I get food to heal myself?",
                1,
                "Where can I get better armour and weapons?",
                2,
                "Okay, thanks, I will go and kill things.",
                3,
                "Can I kill other players?",
                4,
            )
        when (choice) {
            1 -> {
                chatPlayer(quiz, "Where can I get food to heal myself?")
                chatNpc(
                    happy,
                    "There are many different foods in the game such as cabbage, fish, meat and " +
                        "many more. Which do you wish to hear about?",
                )
                foodMenu()
            }
            2 -> betterGearMenu()
            3 -> chatPlayer(happy, "Okay, thanks, I will go and kill things.")
            4 -> killPlayers()
        }
    }

    private suspend fun Dialogue.betterGearMenu() {
        chatNpc(
            happy,
            "Well, you can make them, buy them or talk to the combat tutors just west of here.",
        )
        val choice =
            choice3(
                "How do I make a weapon?",
                1,
                "Where can I buy a weapon?",
                2,
                "Could I get a staff like yours?",
                3,
            )
        when (choice) {
            1 -> makeWeapon()
            2 -> buyWeapon { betterGearMenu() }
            3 -> staff { betterGearMenu() }
        }
    }

    private suspend fun Dialogue.killPlayers() {
        chatPlayer(quiz, "Can I kill other players?")
        chatNpc(
            neutral,
            "Well, you can go to the Wilderness. That is the area for killing other players. " +
                "Just keep heading north and eventually you will be bound to reach it. Be very " +
                "careful, though. Player-killing is not for the unprepared.",
        )
        chatNpc(
            neutral,
            "I'd suggest that you leave any items you don't want to lose in the bank and take a " +
                "good supply of food with you.",
        )
        chatNpc(happy, "Is there anything else you need help with?")
        val choice =
            choice3(
                "No thank you.",
                1,
                "I'd like to know more about combat.",
                2,
                "I'd like to know about something else.",
                3,
            )
        when (choice) {
            1 -> chatPlayer(neutral, "No thank you.")
            2 -> fightMenu()
            3 -> somethingElse()
        }
    }

    /* Food */

    private suspend fun Dialogue.foodMenu() {
        val choice =
            choice3("How do I get cabbages?", 1, "How do I fish?", 2, "Where can I find meat?", 3)
        when (choice) {
            1 -> {
                chatPlayer(quiz, "How do I get cabbages?")
                chatNpc(
                    happy,
                    "There is a field a little distance to the north of here packed full of " +
                        "cabbages which are there for the picking.",
                )
                chatNpc(
                    happy,
                    "You could also farm your own cabbages; there's a Farming patch just south " +
                        "of Falador.",
                )
                foodFollowUp()
            }
            2 -> if (howToFish()) foodFollowUp()
            3 -> {
                chatPlayer(quiz, "Where can I find meat?")
                chatNpc(
                    happy,
                    "I suggest you go and kill some chickens. The roads on either side of this " +
                        "river eventually go past a chicken farm. When you have killed some " +
                        "chickens, cook them. You could either make a fire or use a range.",
                )
                chatNpc(
                    happy,
                    "There is a range at the southern end in this town and a Cooking tutor in " +
                        "south Lumbridge near Bob's Brilliant Axes shop.",
                )
                objbox(
                    LumbridgeGuideObjs.range_icon,
                    "The minimap in the top right corner of the game screen has various icons " +
                        "showing the locations of things. This icon to the left shows where " +
                        "Cooking ranges are.",
                )
                foodFollowUp()
            }
        }
    }

    private suspend fun Dialogue.foodFollowUp() {
        chatNpc(happy, "Is there anything else you need help with?")
        val choice =
            choice3(
                "I'd like to know about other food.",
                1,
                "I'd like to know about something else.",
                2,
                "No thank you.",
                3,
            )
        when (choice) {
            1 -> foodMenu()
            2 -> somethingElse()
            3 -> chatPlayer(neutral, "No thank you.")
        }
    }

    /** False if he only says you should know already. */
    private suspend fun Dialogue.howToFish(): Boolean {
        chatPlayer(quiz, "How do I fish?")
        if (player.baseFishingLvl >= SKILLED_LEVEL) {
            chatNpc(
                confused,
                "I am confused. You are already an experienced fisher. You should know the " +
                    "answer to that already.",
            )
            return false
        }
        chatNpc(
            happy,
            "Fishing spots require different levels and equipment to use. To start Fishing, " +
                "you'll want to talk to the Fishing tutor who can be found in the swamps south " +
                "of here. He will also give you a small fishing net if you don't",
        )
        chatNpc(happy, "own one already.")
        objbox(
            LumbridgeGuideObjs.fishing_icon,
            "The minimap in the top right corner of the game screen has various icons showing " +
                "the locations of things. This icon to the left shows where Fishing spots are.",
        )
        objbox(
            LumbridgeGuideObjs.net,
            "You will need some Fishing equipment. At the Fishing spots to the south you can " +
                "only use a small fishing net.",
        )
        chatPlayer(quiz, "Where could I find one of those?")
        chatNpc(
            happy,
            "You can get them from a Fishing shop or our Fishing Tutor south of here in the " +
                "swamp. There is a Fishing shop in Port Sarim; you can find it on the world " +
                "map. Port Sarim is some way to the west of here, beyond",
        )
        chatNpc(happy, "the village of Draynor.")
        return true
    }

    /* Money and information */

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
        val choice =
            choice3(
                "Where can I smith?",
                1,
                "How do I fish?",
                2,
                "What monsters should I fight?",
                3,
            )
        when (choice) {
            1 -> {
                chatPlayer(quiz, "Where can I smith?")
                whereToSmith()
            }
            2 -> if (howToFish()) foodFollowUp()
            3 -> monsters()
        }
    }

    private suspend fun Dialogue.information() {
        chatPlayer(quiz, "Where can I find more information?")
        chatNpc(
            happy,
            "What you'll want is the OSRS Wiki! I've taken the liberty of opening the useful " +
                "links section of the account management tab for you. Here you can find a link " +
                "to the wiki. There's information and guides on almost",
        )
        chatNpc(
            happy,
            "everything, all easily searched and it's even written and maintained by players.",
        )
        anythingElse()
    }

    private suspend fun Dialogue.moreOptions() {
        val choice =
            choice4(
                "I'd like to know more about security.",
                1,
                "Where can I find a bank?",
                2,
                "I don't need any help.",
                3,
                "Previous options...",
                4,
            )
        when (choice) {
            1 -> security()
            2 -> {
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
            3 -> chatPlayer(neutral, "No, I can find things myself thank you.")
            4 -> mainMenu()
        }
    }

    /* Security */

    private suspend fun Dialogue.security() {
        chatPlayer(quiz, "I'd like to know more about security.")
        chatNpc(
            neutral,
            "First I must warn you to take every precaution to keep your RuneScape password and " +
                "PIN secure. The most important thing to remember is to never give your " +
                "password to, or share your account with, anyone.",
        )
        chatNpc(
            neutral,
            "I can also tell you about password security, avoiding item scamming and in-game " +
                "moderation. I can also tell you about a place called the Stronghold of " +
                "Security, where you can learn more about account security and",
        )
        chatNpc(neutral, "have a bit of an adventure at the same time.")
        val choice =
            choice4(
                "I'd like to know about password security.",
                1,
                "I'd like to know more about avoiding item scamming.",
                2,
                "I'd like to know more about in-game moderation.",
                3,
                "I'd like to know about the Stronghold of Security.",
                4,
            )
        when (choice) {
            1 -> passwordSecurity()
            2 -> itemScamming()
            3 -> moderation()
            4 -> stronghold()
        }
    }

    private suspend fun Dialogue.securityFollowUp(somethingElseOption: String) {
        chatNpc(happy, "Is there anything else you need help with?")
        val choice =
            choice3("Tell me more about security.", 1, somethingElseOption, 2, "No thank you.", 3)
        when (choice) {
            1 -> security()
            2 -> somethingElse()
            3 -> chatPlayer(neutral, "No thank you.")
        }
    }

    private suspend fun Dialogue.passwordSecurity() {
        chatPlayer(quiz, "I'd like to know about password security.")
        chatNpc(
            neutral,
            "Well, the first thing to remember with password security, which I can't stress " +
                "enough, is to never tell your password, bank PIN or authenticator details to " +
                "anyone, not even if they claim to be Jagex staff. Real",
        )
        chatNpc(
            neutral,
            "Jagex staff will NEVER ask for your password. Sharing your account is a very bad " +
                "idea, even if you think that you know and trust the person you are sharing " +
                "with. Players have lost items and even their",
        )
        chatNpc(neutral, "account this way. Sharing accounts is also against the rules.")
        chatPlayer(quiz, "Is there anything else to be aware of?")
        chatNpc(
            neutral,
            "Yes, also be careful where you enter your password. Be aware that fake Old School " +
                "RuneScape websites exist. These are sites that claim to give things such as " +
                "item/stat upgrades, free items, beta testing and",
        )
        chatNpc(neutral, "moderator applications. Jagex do not offer any of these services.")
        chatNpc(
            neutral,
            "Also be aware of trojans and keyloggers. These nasty programs can send everything " +
                "you type into your device back to the computers of malicious people, so you " +
                "can lose more than just your Old School RuneScape",
        )
        chatNpc(
            neutral,
            "password in this way. To avoid getting these on your device, be very wary of what " +
                "you download, especially when it comes to downloading third party software. " +
                "Also be very wary of email attachments and instant",
        )
        chatNpc(neutral, "messenger file transfers, which can contain such things.")
        chatNpc(
            neutral,
            "Finally, remember the game doesn't censor your password or bank PIN when you type " +
                "them; anyone who tells you otherwise is probably trying to steal your " +
                "character or items.",
        )
        securityFollowUp("I'd like to know more about something else.")
    }

    private suspend fun Dialogue.itemScamming() {
        chatPlayer(quiz, "I'd like to know more about avoiding item scamming.")
        chatNpc(
            neutral,
            "There are many nice and helpful players in Old School RuneScape; unfortunately, as " +
                "in real life, there are some who aren't so honest. Some people may try to " +
                "trick you out of your items. Try not to fall for this as",
        )
        chatNpc(
            neutral,
            "Jagex policy is to never return lost items to players that have been scammed. " +
                "Doing so just encourages users to claim they lost items they never had to gain " +
                "an unfair advantage. However, people carrying out such",
        )
        chatNpc(neutral, "scams will get banned as it is against the rules.")
        chatNpc(
            neutral,
            "When trading, you have a second trade confirmation screen. Always double check that " +
                "you are getting the items you expect on this page as some people may try and " +
                "change what they are trading at the last minute on",
        )
        chatNpc(
            neutral,
            "the first trade screen. Remember when you have clicked accept on the second trade " +
                "screen, the trade is not reversible.",
        )
        chatNpc(
            neutral,
            "Try and find out from someone else what an item is before spending a lot of money " +
                "on it. Not all merchants are honest about the rarity or the uses of their " +
                "wares. For example, some people try to pass off spinach rolls as",
        )
        chatNpc(neutral, "rare when in fact they're as common as cabbages!")
        chatNpc(
            neutral,
            "Be wary of people offering to improve your items. For example, some people will " +
                "claim they can add a trim to your armour or upgrade your sword to a more " +
                "powerful one. This is not possible and they are trying",
        )
        chatNpc(neutral, "to steal your hard-earned equipment.")
        securityFollowUp("I'd like to know about something else.")
    }

    private suspend fun Dialogue.moderation() {
        chatPlayer(quiz, "I'd like to know more about in-game moderation.")
        chatNpc(
            neutral,
            "You will, from time to time, see moderator characters in game. Remember: a real " +
                "staff member will never ask for your password or any other personal " +
                "information. There are two types of moderator. You may see Jagex",
        )
        chatNpc(
            neutral,
            "staff characters in game - these can be identified by a gold crown next to their " +
                "name and are the people who work in the Jagex offices. They might be from " +
                "Customer Support, or they might be a tester or a",
        )
        chatNpc(
            neutral,
            "programmer. You may also see player moderators - these can be identified by a " +
                "silver crown next to their name. Player moderators are trusted players of Old " +
                "School RuneScape that help keep in-game behaviour in",
        )
        chatNpc(neutral, "line with the Old School RuneScape rules.")
        chatPlayer(quiz, "How do I become a player moderator?")
        chatNpc(
            neutral,
            "The only way to become a player moderator is to know and play by the rules laid " +
                "down by Jagex. Report those who break the rules, help players who need it and " +
                "one day you might find an invite to become a moderator in",
        )
        chatNpc(
            neutral,
            "your Old School RuneScape inbox. Note that Jagex will never contact you in game or " +
                "by email to become a player moderator.",
        )
        chatNpc(happy, "Is there anything else you need help with?")
        val choice =
            choice3("No, thank you.", 1, "Yes, please.", 2, "Tell me more about security.", 3)
        when (choice) {
            1 -> chatPlayer(neutral, "No thank you.")
            2 -> {
                chatPlayer(happy, "Yes please.")
                mainMenu()
            }
            3 -> security()
        }
    }

    private suspend fun Dialogue.stronghold() {
        chatPlayer(quiz, "I'd like to know about the Stronghold of Security.")
        chatNpc(
            happy,
            "An exciting new discovery has been made in the Barbarian Village. Soon after they " +
                "started mining in the middle of the village, one of the miners got caught in a " +
                "cave.",
        )
        chatPlayer(quiz, "Sounds painful, did he fall on his head?")
        chatNpc(happy, "No, but it turned out that he had discovered a place unlike any other.")
        chatPlayer(quiz, "How so?")
        chatNpc(
            happy,
            "Well an explorer went to look down there, it's full of new and exciting creatures " +
                "and other surprises on top of that. It turns out this place can also help you " +
                "to secure your account more effectively.",
        )
        chatPlayer(happy, "Wow, that sounds good. Where can I find the explorer to learn more?")
        chatNpc(
            neutral,
            "Sadly the explorer went missing and has not been found. However, here is some " +
                "information to get you started on securing your account. You can find the " +
                "Barbarian Village north of Falador and west of Varrock.",
        )
        val book = objs.sos_security_book
        val hasBook = book in player.inv || book in player.worn || book in access.bank
        if (!hasBook && player.invAdd(player.inv, book).success) {
            objbox(book, "The guide hands you a book.")
        }
        securityFollowUp("I'd like to know about something else.")
    }

    private companion object {
        const val STRONG_COMBAT_LEVEL = 30
        const val SKILLED_LEVEL = 30
    }
}
