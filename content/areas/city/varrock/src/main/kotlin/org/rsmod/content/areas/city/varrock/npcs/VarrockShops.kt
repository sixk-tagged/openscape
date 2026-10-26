package org.rsmod.content.areas.city.varrock.npcs

import jakarta.inject.Inject
import org.rsmod.api.config.refs.objs
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.ui.skillMulti
import org.rsmod.api.script.onApNpc1
import org.rsmod.api.script.onApNpc3
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.script.onOpNpc4
import org.rsmod.api.shops.Shops
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.content.areas.city.varrock.configs.varrock_invs
import org.rsmod.content.areas.city.varrock.configs.varrock_npcs
import org.rsmod.game.entity.Npc
import org.rsmod.game.type.inv.InvType
import org.rsmod.game.type.npc.NpcType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The Varrock shopkeepers: Talk-to dialogue from the wiki transcripts and Trade, with stock in
 * [org.rsmod.content.areas.city.varrock.configs.VarrockInvEditor]. Shopkeepers who stand behind a
 * counter also get an approach handler, like bankers, so Talk-to works from the customer's side.
 */
class VarrockShops @Inject constructor(private val shops: Shops) : PluginScript() {
    override fun ScriptContext.startup() {
        shop(varrock_npcs.zaff, ZAFF, counter = true) { zaff(it) }
        shop(varrock_npcs.horvik, HORVIK) { horvik(it) }
        shop(varrock_npcs.lowe, LOWE, counter = true) { lowe(it) }
        shop(varrock_npcs.thessalia, THESSALIA, counter = true) { thessalia(it) }
        shop(varrock_npcs.sword_keeper, SWORDS, counter = true) { swords(it) }
        shop(varrock_npcs.sword_assistant, SWORDS, counter = true) { swords(it) }
        shop(varrock_npcs.general_keeper, GENERAL, counter = true) { generalStore(it) }
        shop(varrock_npcs.general_assistant, GENERAL, counter = true) { generalStore(it) }
        shop(varrock_npcs.asyff, FANCY_CLOTHES, counter = true) { asyff(it) }
        shop(varrock_npcs.tea_seller, TEA, counter = true) { teaSeller(it) }
        shop(varrock_npcs.scavvo, SCAVVO, counter = true) { scavvo(it) }
        shop(varrock_npcs.valaine, VALAINE, counter = true) { valaine(it) }
        shop(varrock_npcs.romily, PIES) { romily(it) }
        sawmill()

        // Note: the makeover and custom fur clothing interfaces are driven by cache
        // clientscripts with no known arguments, so both ops only explain themselves.
        onOpNpc4(varrock_npcs.thessalia) { mes("Thessalia's makeover is not available yet.") }
        onOpNpc4(varrock_npcs.asyff) { mes("Asyff's custom fur clothing is not available yet.") }
    }

    private class ShopDef(val title: String, val inv: InvType)

    private fun Dialogue.open(npc: Npc, shop: ShopDef) =
        shops.open(player, npc, shop.title, shop.inv)

    private fun ScriptContext.shop(
        npc: NpcType,
        shop: ShopDef,
        counter: Boolean = false,
        talk: suspend Dialogue.(Npc) -> Unit,
    ) {
        onOpNpc1(npc) { startDialogue(it.npc) { talk(it.npc) } }
        onOpNpc3(npc) { shops.open(player, it.npc, shop.title, shop.inv) }
        if (counter) {
            onApNpc1(npc) {
                if (isWithinApRange(it.npc, COUNTER_RANGE)) {
                    startDialogue(it.npc) { talk(it.npc) }
                }
            }
            onApNpc3(npc) {
                if (isWithinApRange(it.npc, COUNTER_RANGE)) {
                    shops.open(player, it.npc, shop.title, shop.inv)
                }
            }
        }
    }

    private suspend fun Dialogue.zaff(npc: Npc) {
        val question =
            if (player.members) {
                "Would you like to buy or sell some staffs? Or is there something else you need?"
            } else {
                "Would you like to buy or sell some staffs?"
            }
        chatNpc(quiz, question)
        // Note: the diary barrel, beacon ring and Bryophyta's staff options are skipped.
        val choice = choice2("Yes, please!", 1, "No, thank you.", 2)
        if (choice == 1) {
            open(npc, ZAFF)
            return
        }
        chatPlayer(neutral, "No, thank you.")
        chatNpc(laugh, "Well, 'stick' your head in again if you change your mind.")
        chatPlayer(laugh, "Huh, terrible pun! You just can't get the 'staff' these days!")
    }

    private suspend fun Dialogue.horvik(npc: Npc) {
        chatNpc(quiz, "Hello, do you need any help?")
        val choice = choice2("No thanks. I'm just looking around.", 1, "Do you want to trade?", 2)
        if (choice == 2) {
            open(npc, HORVIK)
            return
        }
        chatPlayer(neutral, "No thanks. I'm just looking around.")
        chatNpc(happy, "Well, come and see me if you're ever in need of armour!")
    }

    private suspend fun Dialogue.lowe(npc: Npc) {
        chatNpc(quiz, "Welcome to Lowe's Archery Emporium. Do you want to see my wares?")
        val choice = choice2("Yes, please.", 1, "No, I prefer to bash things close up.", 2)
        if (choice == 1) {
            open(npc, LOWE)
            return
        }
        chatPlayer(neutral, "No, I prefer to bash things close up.")
        chatNpc(angry, "Humph, philistine.")
    }

    // Note: the scythe, bunny ears and frog token options need Halloween and random event
    // items that cannot be obtained here, and the makeover branch needs its interface.
    private suspend fun Dialogue.thessalia(npc: Npc) {
        chatNpc(quiz, "Do you want to buy any fine clothes?")
        val choice = choice2("What have you got?", 1, "No, thank you.", 2)
        if (choice == 1) {
            chatPlayer(quiz, "What have you got?")
            chatNpc(happy, "Well, I have a number of fine pieces of clothing on sale.")
            open(npc, THESSALIA)
            return
        }
        chatPlayer(neutral, "No, thank you.")
        chatNpc(neutral, "Well, please return if you change your mind.")
    }

    // Note: the Adventure Path voucher rewards are not implemented.
    private suspend fun Dialogue.swords(npc: Npc) {
        chatNpc(happy, "Hello, bold adventurer! Can I interest you in some swords?")
        val choice = choice2("Yes, please!", 1, "No, I'm okay for swords right now.", 2)
        if (choice == 1) {
            open(npc, SWORDS)
            return
        }
        chatPlayer(neutral, "No, I'm okay for swords right now.")
        chatNpc(neutral, "Come back if you need any.")
    }

    private suspend fun Dialogue.generalStore(npc: Npc) {
        chatNpc(happy, "Can I help you at all?")
        val choice = choice2("Yes please. What are you selling?", 1, "No thanks.", 2)
        if (choice == 1) {
            open(npc, GENERAL)
            return
        }
        chatPlayer(neutral, "No thanks.")
    }

    // Note: the bird costume and fur clothing options are skipped (no Hunter yet).
    private suspend fun Dialogue.asyff(npc: Npc) {
        if (!player.members) {
            chatNpc(neutral, "Sorry, we're closed at the moment.")
            return
        }
        chatNpc(neutral, "Now you look like someone who goes to a lot of fancy dress parties.")
        chatPlayer(confused, "Errr...what are you saying exactly?")
        chatNpc(
            neutral,
            "I'm just saying that perhaps you would like to peruse my selection of garments.",
        )
        chatNpc(
            quiz,
            "Or, if that doesn't interest you, then maybe you have something else to offer? " +
                "I'm always on the look out for interesting or unusual new materials.",
        )
        val choice =
            choice3(
                "Okay, lets see what you've got then.",
                1,
                "I think I might just leave the perusing for now thanks.",
                2,
                "What sort of unusual materials did you have in mind?",
                3,
            )
        when (choice) {
            1 -> {
                chatPlayer(neutral, "Okay, let's see what you've got then.")
                open(npc, FANCY_CLOTHES)
            }
            2 -> chatPlayer(neutral, "I think I might just leave the perusing for now, thanks.")
            3 -> {
                chatPlayer(quiz, "What sort of unusual materials did you have in mind?")
                chatNpc(
                    neutral,
                    "Well, some more colourful feathers might be useful. For some surreal " +
                        "reason, all I normally seem to get offered are large quantities of " +
                        "rather beaten-up looking chicken feathers.",
                )
                chatNpc(
                    neutral,
                    "People must have some very strange pastimes around these parts, that's " +
                        "all I can say.",
                )
                chatPlayer(neutral, "Ok, let's see what you've got then.")
                open(npc, FANCY_CLOTHES)
            }
        }
    }

    private suspend fun Dialogue.teaSeller(npc: Npc) {
        chatNpc(happy, "Greetings! Are you in need of refreshment?")
        val choice = choice3("Yes please.", 1, "What are you selling?", 2, "No thanks.", 3)
        when (choice) {
            1 -> {
                chatPlayer(happy, "Yes please.")
                open(npc, TEA)
            }
            2 -> {
                chatPlayer(quiz, "What are you selling?")
                chatNpc(
                    happy,
                    "Only the most delicious infusion of the leaves of the tea plant. Grown in " +
                        "the exotic regions of this world. Buy yourself a cup !",
                )
            }
            3 -> {
                chatPlayer(neutral, "No thanks.")
                chatNpc(neutral, "Well, if you're sure. You know where to come if you do!")
            }
        }
    }

    private suspend fun Dialogue.scavvo(npc: Npc) {
        chatNpc(happy, "'Ello matey! D'ya wanna buy some exciting new toys?")
        val choice =
            choice4(
                "No, toys are for kids.",
                1,
                "Let's have a look then.",
                2,
                "Ooh, goody goody, toys!",
                3,
                "Why do you sell most rune armour but not platebodies?",
                4,
            )
        when (choice) {
            1 -> chatPlayer(neutral, "No, toys are for kids.")
            2 -> open(npc, SCAVVO)
            3 -> {
                chatPlayer(happy, "Ooh goody goody, toys!")
                open(npc, SCAVVO)
            }
            4 -> {
                chatPlayer(quiz, "Why do you sell most rune armour but not platebodies?")
                chatNpc(
                    neutral,
                    "Oh, you have to complete a special quest in order to wear rune platemail. " +
                        "You should talk to the Guildmaster downstairs about that.",
                )
            }
        }
    }

    private suspend fun Dialogue.valaine(npc: Npc) {
        chatNpc(happy, "Hello there. Want to have a look at what we're selling today?")
        val choice = choice2("Yes please.", 1, "No thank you.", 2)
        if (choice == 1) {
            open(npc, VALAINE)
            return
        }
        chatPlayer(neutral, "No thank you.")
    }

    // Note: Romily's pie-baking errand (the wiki's reward table is unverified) is skipped.
    private suspend fun Dialogue.romily(npc: Npc) {
        chatNpc(happy, "Hello and welcome to my pie shop, how can I help you?")
        val choice = choice2("I'd like to buy some pies.", 1, "I'm good thanks.", 2)
        if (choice == 1) {
            open(npc, PIES)
            return
        }
        chatPlayer(neutral, "I'm good thanks.")
    }

    private fun ScriptContext.sawmill() {
        val npc = varrock_npcs.sawmill_operator
        onOpNpc1(npc) { sawmillTalk(it.npc) }
        onOpNpc3(npc) { if (sawmillMembers()) makePlanks(it.npc) }
        onOpNpc4(npc) {
            if (sawmillMembers()) shops.open(player, it.npc, CONSTRUCTION.title, CONSTRUCTION.inv)
        }
    }

    private fun ProtectedAccess.sawmillMembers(): Boolean {
        if (player.members) {
            return true
        }
        mes("The sawmill operator pays you no attention, it doesn't seem like he'll help you.")
        return false
    }

    private suspend fun ProtectedAccess.sawmillTalk(npc: Npc) {
        if (!sawmillMembers()) {
            return
        }
        startDialogue(npc) {
            chatNpc(
                quiz,
                "Hello there. Do you want me to make some planks for you? I can make planks " +
                    "from wood, oak, teak and mahogany logs. Or would you like to buy some " +
                    "other housing supplies?",
            )
            val choice =
                choice3(
                    "Yes, please make me some planks.",
                    1,
                    "Can I buy some housing supplies?",
                    2,
                    "I'm good, thanks.",
                    3,
                )
            when (choice) {
                1 -> {
                    chatPlayer(happy, "Yes, please make me some planks.")
                    access.makePlanks(npc)
                }
                2 -> {
                    chatPlayer(quiz, "Can I buy some housing supplies?")
                    chatNpc(happy, "Of course!")
                    open(npc, CONSTRUCTION)
                }
                3 -> {
                    chatPlayer(neutral, "I'm good, thanks.")
                    chatNpc(
                        happy,
                        "Well come back when you want some. You'll struggle to find quality " +
                            "planks anywhere but here!",
                    )
                }
            }
        }
    }

    // Note: the wiki does not say whether asking for more planks than logs makes as many as
    // the logs allow; this does, and only refuses when there are no logs or too few coins.
    private suspend fun ProtectedAccess.makePlanks(npc: Npc) {
        val names = PLANKS.map { ocName(it.plank) }
        val choice = skillMulti(MAKE_VERB, PLANK_TITLE, PLANKS.map { it.plank }, names) ?: return
        val plank = PLANKS[choice.index]
        val made = minOf(choice.amount, invTotal(inv, plank.log))
        if (made == 0) {
            startDialogue(npc) { chatNpc(neutral, "You'll need to bring me some more logs.") }
            return
        }
        val cost = made * plank.cost
        if (invCoinTotal() < cost) {
            startDialogue(npc) {
                chatNpc(
                    neutral,
                    "Those planks cost $cost coins. You don't have enough money for all of them.",
                )
            }
            return
        }
        invTakeFee(cost)
        invDel(inv, plank.log, made)
        invAdd(inv, plank.plank, made)
    }

    private class Plank(val plank: ObjType, val log: ObjType, val cost: Int)

    private companion object {
        const val COUNTER_RANGE = 2
        const val MAKE_VERB = 0
        const val PLANK_TITLE = "What would you like to make?"

        val ZAFF = ShopDef("Zaff's Superior Staffs!", varrock_invs.zaff_shop)
        val HORVIK = ShopDef("Horvik's Armour Shop", varrock_invs.horvik_shop)
        val LOWE = ShopDef("Lowe's Archery Emporium", varrock_invs.lowe_shop)
        val THESSALIA = ShopDef("Thessalia's Fine Clothes", varrock_invs.thessalia_shop)
        val SWORDS = ShopDef("Varrock Swordshop", varrock_invs.sword_shop)
        val GENERAL = ShopDef("Varrock General Store", varrock_invs.general_shop)
        val FANCY_CLOTHES = ShopDef("Fancy Clothes Store", varrock_invs.fancy_clothes_shop)
        val TEA = ShopDef("Ye olde Tea Shoppe", varrock_invs.tea_shop)
        val SCAVVO = ShopDef("Scavvo's Rune Store", varrock_invs.scavvo_shop)
        val VALAINE = ShopDef("Valaine's Shop of Champions", varrock_invs.valaine_shop)
        val PIES = ShopDef("Pie Shop", varrock_invs.pie_shop)
        val CONSTRUCTION = ShopDef("Construction supplies", varrock_invs.construction_shop)

        val PLANKS =
            listOf(
                Plank(PlankObjs.plank, objs.logs, 100),
                Plank(PlankObjs.oak_plank, objs.oak_logs, 250),
                Plank(PlankObjs.teak_plank, objs.teak_logs, 500),
                Plank(PlankObjs.mahogany_plank, objs.mahogany_logs, 1500),
            )
    }
}

object PlankObjs : ObjReferences() {
    val plank = find("woodplank")
    val oak_plank = find("plank_oak")
    val teak_plank = find("plank_teak")
    val mahogany_plank = find("plank_mahogany")
}
