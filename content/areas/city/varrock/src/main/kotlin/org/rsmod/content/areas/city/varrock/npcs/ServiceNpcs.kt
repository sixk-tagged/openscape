package org.rsmod.content.areas.city.varrock.npcs

import jakarta.inject.Inject
import org.rsmod.api.config.refs.objs
import org.rsmod.api.invtx.invAddOrDrop
import org.rsmod.api.invtx.invTakeFee
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.output.spam
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.script.onApNpc1
import org.rsmod.api.script.onIfModalButton
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.type.refs.comp.ComponentReferences
import org.rsmod.api.type.refs.interf.InterfaceReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.content.areas.city.varrock.configs.varrock_npcs
import org.rsmod.game.type.comp.ComponentType
import org.rsmod.game.type.npc.NpcType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** The Apothecary's potions, Baraek's furs, Benny's newspapers and the pub bartenders. */
class ServiceNpcs @Inject constructor(private val objRepo: ObjRepository) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(varrock_npcs.apothecary) { startDialogue(it.npc) { apothecary() } }
        onOpNpc3(varrock_npcs.apothecary) { ifOpenMainModal(ApothecaryInterfaces.potions) }
        for (potion in POTIONS) {
            for (button in potion.buttons) {
                onIfModalButton(button) { makePotion(potion) }
            }
        }

        onOpNpc1(varrock_npcs.baraek) { startDialogue(it.npc) { baraek() } }
        onOpNpc1(varrock_npcs.benny) { startDialogue(it.npc) { benny() } }

        bartender(varrock_npcs.bluemoon_bartender) { blueMoon() }
        bartender(varrock_npcs.donkey_bartender) { dancingDonkey() }
        bartender(varrock_npcs.jollyboar_bartender) { jollyBoar() }
    }

    private fun ScriptContext.bartender(npc: NpcType, talk: suspend Dialogue.() -> Unit) {
        onOpNpc1(npc) { startDialogue(it.npc) { talk() } }
        onApNpc1(npc) {
            if (isWithinApRange(it.npc, BAR_RANGE)) {
                startDialogue(it.npc) { talk() }
            }
        }
    }

    // Note: the Adventure Path voucher, A Tail of Two Cats hats, Romeo and Juliet and
    // Serum diary branches are skipped.
    private suspend fun Dialogue.apothecary() {
        chatNpc(neutral, "I am the Apothecary. I brew potions. Do you need anything specific?")
        val choice = choice2("Can you make potions for me?", 1, "Talk about something else.", 2)
        if (choice == 1) {
            chatPlayer(quiz, "Can you make potions for me?")
            access.ifOpenMainModal(ApothecaryInterfaces.potions)
            return
        }
        apothecaryChat()
    }

    private suspend fun Dialogue.apothecaryChat() {
        val choice =
            choice4(
                "Have you got any decent gossip to share?",
                1,
                "Do you know a potion to make hair fall out?",
                2,
                "Have you got any good potions to give away?",
                3,
                "No thanks.",
                4,
            )
        when (choice) {
            1 -> {
                chatPlayer(quiz, "Have you got any decent gossip to share?")
                chatNpc(
                    neutral,
                    "Well I hear young Romeo's having a little woman trouble but other than " +
                        "that all's quiet on the eastern front. Can I do something for you?",
                )
                apothecaryChat()
            }
            2 -> {
                chatPlayer(quiz, "Do you know a potion to make hair fall out?")
                chatNpc(
                    neutral,
                    "I do indeed. I gave it to my mother. That's why I now live alone.",
                )
                chatNpc(neutral, "But can I do something for you?")
                apothecaryChat()
            }
            3 -> {
                chatPlayer(quiz, "Have you got any good potions to give away?")
                chatNpc(
                    neutral,
                    "Sorry, charity is not my strong point. Do you need anything else?",
                )
                apothecaryChat()
            }
            4 -> chatPlayer(neutral, "No thanks.")
        }
    }

    // Note: the wiki does not give the interface's click behaviour or refusal wording; a click
    // makes one potion and closes it, and a missing ingredient or coin only sends a message.
    private fun ProtectedAccess.makePotion(potion: Potion) {
        ifClose()
        if (!potion.ingredients.all { (obj, count) -> invTotal(inv, obj) >= count }) {
            mes("You don't have the ingredients the Apothecary needs for that.")
            return
        }
        if (invCoinTotal() < potion.coins) {
            mes("You don't have enough coins.")
            return
        }
        invTakeFee(potion.coins)
        for ((obj, count) in potion.ingredients) {
            invDel(inv, obj, count)
        }
        invAddOrDrop(objRepo, potion.product)
        mes("The Apothecary makes you a ${ocName(potion.product).lowercase()}.")
    }

    private suspend fun Dialogue.baraek() {
        val choice =
            if (access.invTotal(access.inv, BaraekObjs.grey_wolf_fur) > 0) {
                choice3(
                    "Can you sell me some furs?",
                    1,
                    "Hello. I am in search of a quest.",
                    2,
                    "Would you like to buy my grey wolf fur?",
                    3,
                )
            } else if (hasFur()) {
                choice3(
                    "Can you sell me some furs?",
                    1,
                    "Hello. I am in search of a quest.",
                    2,
                    "Would you like to buy my fur?",
                    4,
                )
            } else {
                choice2("Can you sell me some furs?", 1, "Hello. I am in search of a quest.", 2)
            }
        when (choice) {
            1 -> sellFur()
            2 -> {
                chatPlayer(happy, "Hello! I am in search of a quest.")
                chatNpc(neutral, "Sorry kiddo, I'm a fur trader not a damsel in distress.")
            }
            3 -> buyGreyWolfFur()
            4 -> buyFur()
        }
    }

    private fun Dialogue.hasFur(): Boolean =
        access.invTotal(access.inv, BaraekObjs.bear_fur) > 0 ||
            access.invTotal(access.inv, BaraekObjs.fur) > 0

    private suspend fun Dialogue.sellFur() {
        chatPlayer(quiz, "Can you sell me some furs?")
        chatNpc(neutral, "Yeah, sure. They're 20 gold coins each.")
        val choice = choice2("Yeah, okay, here you go.", 1, "20 gold coins? That's an outrage!", 2)
        if (choice == 2) {
            chatPlayer(angry, "20 gold coins? That's an outrage!")
            chatNpc(
                neutral,
                "Well, I can't go any cheaper than that mate. I have a family to feed.",
            )
            return
        }
        chatPlayer(neutral, "Yeah, OK, here you go.")
        var price = FUR_PRICE
        if (access.invCoinTotal() < price) {
            chatPlayer(sad, "Oh dear, I don't have enough money!")
            chatNpc(neutral, "Well, my best price is 18 coins.")
            val cheaper = choice2("OK, here you go.", 1, "No thanks, I'll leave it.", 2)
            if (cheaper == 2) {
                chatPlayer(neutral, "No thanks, I'll leave it.")
                chatNpc(neutral, "It's your loss mate.")
                return
            }
            chatPlayer(neutral, "OK, here you go.")
            price = LOWEST_FUR_PRICE
            if (access.invCoinTotal() < price) {
                chatPlayer(sad, "Oh dear, I don't have that either.")
                chatNpc(
                    neutral,
                    "Well, I can't go any cheaper than that mate. I've got a family to feed.",
                )
                chatPlayer(neutral, "Oh well, never mind.")
                return
            }
        }
        player.invTakeFee(price)
        player.invAddOrDrop(objRepo, BaraekObjs.bear_fur)
        objbox(BaraekObjs.bear_fur, "Baraek sells you a fur.")
    }

    private suspend fun Dialogue.buyGreyWolfFur() {
        chatPlayer(quiz, "Would you like to buy my grey wolf fur?")
        chatNpc(shocked, "GREY WOLF FUR??? NOW you're talking!")
        chatNpc(happy, "Grey wolf fur is something of a desirable item to me.")
        chatNpc(quiz, "I'll take all you have for 120 gold pieces per fur, does that sound fair?")
        val choice =
            choice2(
                "Yep, sounds fine.",
                1,
                "No! I almost got my throat torn out by a wolf to get this!",
                2,
            )
        if (choice == 2) {
            chatPlayer(angry, "No! I almost got my throat torn out by a wolf to get this!")
            return
        }
        chatPlayer(happy, "Yep, that sounds fine.")
        buyFurs(listOf(BaraekObjs.grey_wolf_fur), GREY_WOLF_FUR_PRICE)
        chatPlayer(happy, "Thanks!")
    }

    private suspend fun Dialogue.buyFur() {
        chatPlayer(quiz, "Would you like to buy my fur?")
        chatNpc(neutral, "Let's have a look at it.")
        val total =
            access.invTotal(access.inv, BaraekObjs.bear_fur) +
                access.invTotal(access.inv, BaraekObjs.fur)
        val each = if (total == 1) "it" else "each one"
        chatNpc(
            neutral,
            "It's not in the best condition. I guess I could give you $FUR_BUY_PRICE coins for " +
                "$each.",
        )
        val choice = choice2("Yeah, that'll do.", 1, "I think I'll keep hold of it actually.", 2)
        if (choice == 2) {
            chatPlayer(neutral, "I think I'll keep hold of it actually!")
            chatNpc(neutral, "Oh ok. Didn't want it anyway!")
            return
        }
        chatPlayer(neutral, "Yeah, that'll do.")
        buyFurs(listOf(BaraekObjs.bear_fur, BaraekObjs.fur), FUR_BUY_PRICE)
        chatPlayer(happy, "Thanks!")
    }

    /** Baraek buys one fur per game tick. */
    private suspend fun Dialogue.buyFurs(furs: List<ObjType>, price: Int) {
        for (fur in furs) {
            while (access.invTotal(access.inv, fur) > 0) {
                access.invDel(access.inv, fur)
                access.invAdd(access.inv, objs.coins, price)
                delay(1)
            }
        }
    }

    private suspend fun Dialogue.benny() {
        val choice =
            choice4(
                "Can I have a newspaper, please?",
                1,
                "How much does a paper cost?",
                2,
                "Varrock Herald? Never heard of it.",
                3,
                "Anything interesting in there?",
                4,
            )
        when (choice) {
            1 -> {
                chatPlayer(quiz, "Can I have a newspaper, please?")
                newspaperOffer("Certainly, Guv. That'll be 50 coins, please.")
            }
            2 -> {
                chatPlayer(quiz, "How much does a paper cost?")
                newspaperOffer("Just 50 coins! An absolute bargain! Want one?")
            }
            3 -> {
                chatPlayer(quiz, "Varrock Herald? Never heard of it.")
                chatNpc(
                    happy,
                    "For the illiterate amongst us, I shall elucidate. The Varrock Herald is a " +
                        "new newspaper. It is edited, printed and published by myself, Benny " +
                        "Gutenberg, and each edition promises to enthrall the reader with",
                )
                newspaperOffer(
                    "captivating material! Now, can I interest you in buying one for a mere 50 coins?"
                )
            }
            4 -> {
                chatPlayer(quiz, "Anything interesting in there?")
                newspaperOffer(
                    "Of course there is, mate. Packed full of thought provoking insights, " +
                        "contentious interviews and celebrity scandalmongering! An excellent " +
                        "read and all for just 50 coins! Want one?"
                )
            }
        }
    }

    private suspend fun Dialogue.newspaperOffer(line: String) {
        chatNpc(happy, line)
        if (access.invCoinTotal() < NEWSPAPER_PRICE) {
            chatPlayer(sad, "Maybe when I've got more money on me.")
            chatNpc(neutral, "Well, no cash, no paper. Live in ignorance.")
            return
        }
        val choice = choice2("Sure, here you go...", 1, "No, thanks.", 2)
        if (choice == 2) {
            chatNpc(neutral, "Okay, suit yourself. Plenty more fish in the sea.")
            return
        }
        chatPlayer(neutral, "Sure, here you go...")
        if (player.invTakeFee(NEWSPAPER_PRICE)) {
            player.invAddOrDrop(objRepo, BaraekObjs.newspaper)
        }
    }

    private suspend fun Dialogue.blueMoon() {
        chatNpc(quiz, "What can I do yer for?")
        val choice =
            choice3(
                "A glass of your finest ale please.",
                1,
                "Can you recommend where an adventurer might make his fortune?",
                2,
                "Do you know where I can get some good equipment",
                3,
            )
        when (choice) {
            1 -> {
                chatPlayer(happy, "A glass of your finest ale please.")
                chatNpc(happy, "No problemo. That'll be 2 coins.")
                buyBeer(broke = "Oh dear. I don't seem to have enough money.")
            }
            2 -> {
                chatPlayer(quiz, "Can you recommend where an adventurer might make his fortune?")
                chatNpc(
                    neutral,
                    "Ooh I don't know if I should be giving away information, makes the game " +
                        "too easy.",
                )
                blueMoonFortune()
            }
            3 -> {
                chatPlayer(quiz, "Do you know where I can get some good equipment?")
                chatNpc(
                    neutral,
                    "Well, there's the sword shop across the road, or there's also all sorts " +
                        "of shops up around the market.",
                )
            }
        }
    }

    private suspend fun Dialogue.blueMoonFortune() {
        val choice =
            choice3(
                "Oh ah well...",
                1,
                "Game? What are you talking about?",
                2,
                "Just a small clue?",
                3,
            )
        when (choice) {
            1 -> chatPlayer(neutral, "Oh ah well...")
            2 -> {
                chatPlayer(confused, "Game? What are you talking about?")
                chatNpc(
                    neutral,
                    "This world around us... is an online game... called Old School RuneScape.",
                )
                chatPlayer(
                    confused,
                    "Nope, still don't understand what you are talking about. What does " +
                        "'online' mean?",
                )
                chatNpc(
                    neutral,
                    "It's a sort of connection between magic boxes across the world, big boxes " +
                        "on people's desktops and little ones people can carry. They can talk " +
                        "to each other to play games.",
                )
                chatPlayer(angry, "I give up. You're obviously completely mad!")
            }
            3 -> {
                chatPlayer(quiz, "Just a small clue?")
                chatNpc(
                    neutral,
                    "Go and talk to the bartender at the Jolly Boar Inn, he doesn't seem to " +
                        "mind giving away clues.",
                )
            }
        }
    }

    private suspend fun Dialogue.dancingDonkey() {
        chatPlayer(neutral, "Hello.")
        chatNpc(happy, "Good day to you, brave adventurer. Can I get you a refreshing beer?")
        val choice = choice3("Yes please!", 1, "No thanks.", 2, "How much?", 3)
        when (choice) {
            1 -> donkeyBeer()
            2 -> {
                chatPlayer(neutral, "No thanks.")
                chatNpc(neutral, "Let me know if you change your mind.")
            }
            3 -> {
                chatPlayer(quiz, "How much?")
                chatNpc(neutral, "Two gold pieces a pint. So, what do you say?")
                val again = choice2("Yes please!", 1, "No thanks.", 2)
                if (again == 1) {
                    donkeyBeer()
                } else {
                    chatPlayer(neutral, "No thanks.")
                    chatNpc(neutral, "Let me know if you change your mind.")
                }
            }
        }
    }

    private suspend fun Dialogue.donkeyBeer() {
        chatPlayer(happy, "Yes please!")
        chatNpc(neutral, "Ok then, that's two gold coins please.")
        if (player.invTakeFee(BEER_PRICE)) {
            player.invAddOrDrop(objRepo, objs.beer)
            player.spam("You buy a pint of beer.")
            chatNpc(happy, "Cheers!")
            chatPlayer(happy, "Cheers!")
        }
    }

    private suspend fun Dialogue.jollyBoar() {
        chatNpc(quiz, "Can I help you?")
        val choice =
            choice3(
                "I'll have a beer please.",
                1,
                "Any hints where I can go adventuring?",
                2,
                "Heard any good gossip?",
                3,
            )
        when (choice) {
            1 -> {
                chatPlayer(happy, "I'll have a pint of beer please.")
                chatNpc(neutral, "Ok, that'll be two coins please.")
                buyBeer(broke = "Oh dear, I don't seem to have enough money.")
            }
            2 -> {
                chatPlayer(quiz, "Any hints on where I can go adventuring?")
                chatNpc(neutral, "Ooh, now. Let me see...")
                chatNpc(
                    neutral,
                    "Well there is the Varrock sewers. There are tales of untold horrors coming " +
                        "out at night and stealing babies from houses.",
                )
                chatPlayer(happy, "Sounds perfect! Where's the entrance?")
                chatNpc(neutral, "It's just to the east of the palace.")
            }
            3 -> {
                chatPlayer(quiz, "Heard any gossip?")
                chatNpc(
                    neutral,
                    "I'm not that well up on the gossip out here. I've heard that the bartender " +
                        "in the Blue Moon Inn has gone a little crazy, he keeps claiming he is " +
                        "part of something called an online game.",
                )
                chatNpc(
                    neutral,
                    "What that means, I don't know. That's probably old news by now though.",
                )
            }
        }
    }

    private suspend fun Dialogue.buyBeer(broke: String) {
        if (!player.invTakeFee(BEER_PRICE)) {
            chatPlayer(sad, broke)
            return
        }
        player.spam("You buy a pint of beer.")
        player.invAddOrDrop(objRepo, objs.beer)
    }

    private class Potion(
        val product: ObjType,
        val ingredients: List<Pair<ObjType, Int>>,
        val coins: Int,
        val buttons: List<ComponentType>,
    )

    private companion object {
        const val BAR_RANGE = 2
        const val BEER_PRICE = 2
        const val NEWSPAPER_PRICE = 50
        const val FUR_PRICE = 20
        const val LOWEST_FUR_PRICE = 18
        const val FUR_BUY_PRICE = 12
        const val GREY_WOLF_FUR_PRICE = 120

        val POTIONS =
            listOf(
                Potion(
                    ApothecaryObjs.strength_potion,
                    listOf(ApothecaryObjs.limpwurt_root to 1, ApothecaryObjs.red_spiders_eggs to 1),
                    5,
                    ApothecaryComponents.strength,
                ),
                Potion(
                    ApothecaryObjs.energy_potion,
                    listOf(ApothecaryObjs.limpwurt_root to 2, ApothecaryObjs.chocolate_dust to 1),
                    0,
                    ApothecaryComponents.energy,
                ),
                Potion(
                    ApothecaryObjs.antipoison,
                    listOf(ApothecaryObjs.limpwurt_root to 1, ApothecaryObjs.cadava_berries to 1),
                    5,
                    ApothecaryComponents.antipoison,
                ),
            )
    }
}

object ApothecaryInterfaces : InterfaceReferences() {
    val potions = find("apothecary_potions")
}

object ApothecaryComponents : ComponentReferences() {
    val strength =
        listOf("strength", "strength_gfx", "strength_pic", "strength_title", "strength_contents")
            .map { find("apothecary_potions:$it") }
    val energy =
        listOf("energy", "energy_gfx", "energy_pic", "energy_title", "energy_contents").map {
            find("apothecary_potions:$it")
        }
    val antipoison =
        listOf(
                "antipoison",
                "antipoison_gfx",
                "antipoison_pic",
                "antipoison_title",
                "antipoison_contents",
            )
            .map { find("apothecary_potions:$it") }
}

object ApothecaryObjs : ObjReferences() {
    val strength_potion = find("strength4")
    val energy_potion = find("4dose1energy")
    val antipoison = find("4doseantipoison")
    val limpwurt_root = find("limpwurt_root")
    val red_spiders_eggs = find("red_spiders_eggs")
    val chocolate_dust = find("chocolate_dust")
    val cadava_berries = find("cadavaberries")
}

object BaraekObjs : ObjReferences() {
    val bear_fur = find("fur")
    val fur = find("werewolve_fur")
    val newspaper = find("qip_soa_newspaper1")
    val grey_wolf_fur = find("grey_wolf_fur")
}
