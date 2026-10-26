package org.rsmod.content.areas.city.draynor.npcs

import jakarta.inject.Inject
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.stat.farmingLvl
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.script.onOpNpc4
import org.rsmod.api.script.onOpNpcU
import org.rsmod.content.areas.city.draynor.configs.draynor_npcs
import org.rsmod.content.areas.city.draynor.configs.draynor_objs
import org.rsmod.game.type.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** Ned's ropes, Aggie's dyes and the cape, crystal and device vendors of Draynor. */
class DraynorServices @Inject constructor(private val objRepo: ObjRepository) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(draynor_npcs.ned) { startDialogue(it.npc) { ned() } }

        onOpNpc1(draynor_npcs.aggie) { startDialogue(it.npc) { aggie() } }
        onOpNpc3(draynor_npcs.aggie) { startDialogue(it.npc) { dyeMenu() } }
        for (dye in Dye.entries) {
            for (ingredient in dye.ingredients) {
                onOpNpcU(draynor_npcs.aggie, ingredient.obj) {
                    startDialogue(it.npc) { makeDye(dye) }
                }
            }
        }

        onOpNpc1(draynor_npcs.wise_old_man) { startDialogue(it.npc) { wiseOldMan() } }
        onOpNpc1(draynor_npcs.martin) { startDialogue(it.npc) { martin() } }
        onOpNpc1(draynor_npcs.twiggy) { startDialogue(it.npc) { twiggy() } }

        onOpNpc1(draynor_npcs.rick) { startDialogue(it.npc) { rick() } }
        onOpNpc3(draynor_npcs.rick) { startDialogue(it.npc) { buyCrystal() } }

        onOpNpc1(draynor_npcs.jalarast) { startDialogue(it.npc) { jalarast() } }
        onOpNpc3(draynor_npcs.jalarast) { startDialogue(it.npc) { splitbarkMenu() } }

        onOpNpc4(draynor_npcs.ava) { startDialogue(it.npc) { avaDevices() } }
    }

    // ---- Ned

    private suspend fun Dialogue.ned() {
        chatNpc(happy, "Could I be making or selling you some rope?")
        val choice =
            choice2("Yes, I would like some rope.", 1, "No thanks, Ned, I don't need any.", 2)
        if (choice == 2) {
            chatPlayer(neutral, "No thanks, Ned, I don't need any.")
            return
        }
        chatPlayer(happy, "Yes, I would like some rope.")
        chatNpc(
            happy,
            "Well, I can sell you some rope for 15 coins. Or I can be making you some if you " +
                "gets me 4 balls of wool.",
        )
        val buy =
            choice3(
                "I'll buy some rope for 15 coins.",
                1,
                "I'll get you some wool.",
                2,
                "No thanks, Ned.",
                3,
            )
        when (buy) {
            1 -> buyRope()
            2 -> {
                chatPlayer(neutral, "I'll get you some wool.")
                if (access.invTotal(access.inv, draynor_objs.ball_of_wool) >= WOOL_PER_ROPE) {
                    chatNpc(happy, "I see you have the wool. Let me make you a rope.")
                    makeRope()
                } else {
                    chatNpc(happy, "Good. Bring me 4 balls of wool and I'll make a rope for you.")
                }
            }
            3 -> chatPlayer(neutral, "No thanks, Ned.")
        }
    }

    private suspend fun Dialogue.buyRope() {
        chatPlayer(happy, "I'll buy some rope for 15 coins.")
        if (access.inv.isFull()) {
            chatNpc(sad, "You don't have room for it, lad.")
        } else if (!access.invTakeFee(ROPE_PRICE)) {
            chatPlayer(sad, "Sorry, I haven't got enough coins.")
        } else {
            access.invAdd(access.inv, draynor_objs.rope)
            access.mes("You hand Ned 15 coins and he hands you a rope.")
        }
    }

    private suspend fun Dialogue.makeRope() {
        val result = access.invDel(access.inv, draynor_objs.ball_of_wool, WOOL_PER_ROPE)
        if (result.failure) {
            return
        }
        access.invAdd(access.inv, draynor_objs.rope)
        access.mes("You hand Ned 4 balls of wool and he spins you a rope.")
    }

    // ---- Aggie

    private suspend fun Dialogue.aggie() {
        chatNpc(neutral, "What can I help you with?")
        val choice =
            choice3(
                "What could you make for me?",
                1,
                "Cool, do you turn people into frogs?",
                2,
                "You mad old witch, you can't help me.",
                3,
            )
        when (choice) {
            1 -> {
                chatPlayer(quiz, "What could you make for me?")
                chatNpc(
                    happy,
                    "I mostly make what I find pretty. I sometimes make dye for people, if " +
                        "they bring me the ingredients and a few coins.",
                )
                dyeMenu()
            }
            2 -> {
                chatPlayer(laugh, "Cool, do you turn people into frogs?")
                chatNpc(angry, "Oh, don't be silly. That sort of thing is for storybooks, dearie!")
            }
            3 -> {
                chatPlayer(angry, "You mad old witch, you can't help me.")
                chatNpc(angry, "Well, if you're going to be like that, I won't help you!")
            }
        }
    }

    private suspend fun Dialogue.dyeMenu() {
        val choice =
            choice4(
                "Red dye (3 redberries and 5 coins)",
                Dye.Red,
                "Yellow dye (2 onions and 5 coins)",
                Dye.Yellow,
                "Blue dye (2 woad leaves and 5 coins)",
                Dye.Blue,
                "No thanks.",
                null,
                title = "What dye do you want?",
            )
        if (choice == null) {
            chatPlayer(neutral, "No thanks.")
            return
        }
        makeDye(choice)
    }

    private suspend fun Dialogue.makeDye(dye: Dye) {
        chatPlayer(quiz, "Could you make me some ${dye.label} dye?")
        val inv = access.inv
        val hasIngredients = dye.ingredients.all { access.invTotal(inv, it.obj) >= it.count }
        if (!hasIngredients) {
            chatNpc(neutral, "You don't have the ingredients I need for ${dye.label} dye.")
            return
        }
        if (access.invCoinTotal() < DYE_PRICE) {
            chatNpc(neutral, "You need to give me 5 coins as well.")
            return
        }
        for (ingredient in dye.ingredients) {
            if (access.invDel(inv, ingredient.obj, ingredient.count).failure) {
                return
            }
        }
        access.invTakeFee(DYE_PRICE)
        access.invAddOrDrop(objRepo, dye.obj)
        access.mes("You hand Aggie the ingredients and 5 coins, and she makes you some dye.")
        chatNpc(happy, "There you go, dearie. Your ${dye.label} dye is ready.")
    }

    private enum class Dye(val label: String, val obj: ObjType, val ingredients: List<Ingredient>) {
        Red("red", draynor_objs.red_dye, listOf(Ingredient(draynor_objs.redberries, 3))),
        Yellow("yellow", draynor_objs.yellow_dye, listOf(Ingredient(draynor_objs.onion, 2))),
        Blue("blue", draynor_objs.blue_dye, listOf(Ingredient(draynor_objs.woad_leaf, 2))),
    }

    private class Ingredient(val obj: ObjType, val count: Int)

    // ---- Wise Old Man, Martin and Twiggy

    // Note: bank-junk recycling, his tasks and the lore questions are skipped.
    private suspend fun Dialogue.wiseOldMan() {
        chatNpc(neutral, "Greetings, ${player.displayName}.")
        val choice =
            choice3(
                "Can I get another hood for my Quest Point Cape?",
                1,
                "Can I buy a Quest Point Cape?",
                2,
                "I'll be going now.",
                3,
            )
        when (choice) {
            1 -> {
                chatPlayer(quiz, "Can I get another hood for my Quest Point Cape?")
                if (draynor_objs.qp_cape !in player.inv && draynor_objs.qp_cape !in player.worn) {
                    chatNpc(angry, "You don't even have a cape, you silly goose!")
                } else if (
                    draynor_objs.qp_hood in player.inv ||
                        draynor_objs.qp_hood in player.worn ||
                        draynor_objs.qp_hood in access.bank
                ) {
                    chatNpc(neutral, "You already have a hood.")
                } else if (access.inv.isFull()) {
                    chatNpc(neutral, "You don't have room for it.")
                } else {
                    access.invAdd(access.inv, draynor_objs.qp_hood)
                    chatNpc(happy, "Here you go.")
                }
            }
            2 -> {
                chatPlayer(quiz, "Can I buy a Quest Point Cape?")
                chatNpc(neutral, "Certainly. That will be 99,000 coins.")
                buyCape(draynor_objs.qp_cape, draynor_objs.qp_hood, CAPE_PRICE)
            }
            3 -> chatPlayer(neutral, "I'll be going now.")
        }
    }

    // Note: the trimmed Farming cape for a second 99 is skipped.
    private suspend fun Dialogue.martin() {
        chatNpc(happy, "Hello there. Come to see how a master gardener lives?")
        val choice = choice2("Can I buy a Farming cape?", 1, "Just passing through.", 2)
        if (choice == 2) {
            chatPlayer(neutral, "Just passing through.")
            return
        }
        chatPlayer(quiz, "Can I buy a Farming cape?")
        if (player.farmingLvl < 99) {
            chatNpc(
                neutral,
                "I only sell the cape to those who have mastered Farming. Come back at level 99.",
            )
            return
        }
        chatNpc(happy, "Of course. That will be 99,000 coins.")
        buyCape(draynor_objs.farming_cape, draynor_objs.farming_hood, CAPE_PRICE)
    }

    private suspend fun Dialogue.buyCape(cape: ObjType, hood: ObjType, price: Int) {
        if (access.inv.freeSpace() < 2) {
            chatNpc(neutral, "You'll need two free inventory spaces to carry the cape and hood.")
        } else if (!access.invTakeFee(price)) {
            chatPlayer(sad, "I'm afraid I haven't got that many coins.")
        } else {
            access.invAdd(access.inv, cape)
            access.invAdd(access.inv, hood)
            access.mes("You hand over 99,000 coins and receive a cape and hood.")
        }
    }

    private suspend fun Dialogue.twiggy() {
        chatNpc(happy, "Oh, hello! Isn't it a lovely day to be out in the village?")
        // 1/50: she shares a chocolate bar.
        if (access.random.randomBoolean(CHOCOLATE_ODDS) && !access.inv.isFull()) {
            access.invAdd(access.inv, draynor_objs.chocolate_bar)
            chatNpc(happy, "Here, have a chocolate bar. I always carry a few.")
        }
    }

    // ---- Wizards' Tower

    private suspend fun Dialogue.rick() {
        chatNpc(neutral, "Hello there. I make escape crystals, if you've a use for one.")
        val choice = choice2("Buy an escape crystal.", 1, "No thanks.", 2)
        if (choice == 1) {
            buyCrystal()
        } else {
            chatPlayer(neutral, "No thanks.")
        }
    }

    private suspend fun Dialogue.buyCrystal() {
        val crystal = draynor_objs.escape_crystal
        if (crystal in player.inv || crystal in player.worn) {
            chatNpc(neutral, "You already have an escape crystal.")
        } else if (access.inv.isFull()) {
            chatNpc(neutral, "You don't have room for it.")
        } else if (!access.invTakeFee(CRYSTAL_PRICE)) {
            chatNpc(neutral, "That will be 75,000 coins, and you don't have that much.")
        } else {
            access.invAdd(access.inv, crystal)
            access.mes("You hand over 75,000 coins and receive an escape crystal.")
        }
    }

    private suspend fun Dialogue.jalarast() {
        chatNpc(
            neutral,
            "I make splitbark armour for those who bring me the bark and the fine cloth, " +
                "and a fee for my trouble.",
        )
        splitbarkMenu()
    }

    private suspend fun Dialogue.splitbarkMenu() {
        val choice =
            choice5(
                "Splitbark helm (2 bark, 2 fine cloth, 6,000 coins)",
                Splitbark.Helm,
                "Splitbark body (4 bark, 4 fine cloth, 37,000 coins)",
                Splitbark.Body,
                "Splitbark legs (3 bark, 3 fine cloth, 32,000 coins)",
                Splitbark.Legs,
                "More...",
                null,
                "No thanks.",
                null,
            )
        if (choice != null) {
            makeSplitbark(choice)
            return
        }
        val more =
            choice3(
                "Splitbark gauntlets (1 bark, 1 fine cloth, 1,000 coins)",
                Splitbark.Gauntlets,
                "Splitbark boots (1 bark, 1 fine cloth, 1,000 coins)",
                Splitbark.Boots,
                "No thanks.",
                null,
            )
        if (more != null) {
            makeSplitbark(more)
        }
    }

    private suspend fun Dialogue.makeSplitbark(piece: Splitbark) {
        val inv = access.inv
        val have =
            minOf(
                access.invTotal(inv, draynor_objs.bark),
                access.invTotal(inv, draynor_objs.fine_cloth),
            )
        if (have < piece.pieces) {
            chatNpc(
                neutral,
                "You need ${piece.pieces} bark and ${piece.pieces} fine cloth for that.",
            )
        } else if (access.invCoinTotal() < piece.fee) {
            chatNpc(neutral, "My fee for that is ${piece.fee} coins.")
        } else {
            access.invDel(inv, draynor_objs.bark, piece.pieces)
            access.invDel(inv, draynor_objs.fine_cloth, piece.pieces)
            access.invTakeFee(piece.fee)
            access.invAddOrDrop(objRepo, piece.obj)
            chatNpc(happy, "There you go. Wear it well.")
        }
    }

    private enum class Splitbark(val obj: ObjType, val pieces: Int, val fee: Int) {
        Gauntlets(draynor_objs.splitbark_gauntlets, 1, 1_000),
        Boots(draynor_objs.splitbark_boots, 1, 1_000),
        Helm(draynor_objs.splitbark_helm, 2, 6_000),
        Legs(draynor_objs.splitbark_legs, 3, 32_000),
        Body(draynor_objs.splitbark_body, 4, 37_000),
    }

    // ---- Ava

    // Note: the accumulator upgrade (75 steel arrows at Ranged 50) is skipped.
    private suspend fun Dialogue.avaDevices() {
        chatNpc(neutral, "I can sell you an attractor for 999 coins. It'll pull in your arrows.")
        val choice = choice2("Buy an attractor.", 1, "No thanks.", 2)
        if (choice == 2) {
            chatPlayer(neutral, "No thanks.")
        } else if (draynor_objs.attractor in player.inv || draynor_objs.attractor in player.worn) {
            chatNpc(neutral, "You already have one.")
        } else if (access.inv.isFull()) {
            chatNpc(neutral, "You don't have room for it.")
        } else if (!access.invTakeFee(ATTRACTOR_PRICE)) {
            chatNpc(neutral, "You don't have enough coins.")
        } else {
            access.invAdd(access.inv, draynor_objs.attractor)
            access.mes("You hand over 999 coins and receive Ava's attractor.")
        }
    }

    private companion object {
        const val WOOL_PER_ROPE = 4
        const val ROPE_PRICE = 15
        const val DYE_PRICE = 5
        const val CAPE_PRICE = 99_000
        const val CRYSTAL_PRICE = 75_000
        const val ATTRACTOR_PRICE = 999
        const val CHOCOLATE_ODDS = 50
    }
}
