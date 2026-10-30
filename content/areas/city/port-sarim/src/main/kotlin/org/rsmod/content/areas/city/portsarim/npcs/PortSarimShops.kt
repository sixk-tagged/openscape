package org.rsmod.content.areas.city.portsarim.npcs

import jakarta.inject.Inject
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.script.onApNpc1
import org.rsmod.api.script.onApNpc3
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.script.onOpNpc4
import org.rsmod.api.shops.Shops
import org.rsmod.content.areas.city.portsarim.configs.port_sarim_invs
import org.rsmod.content.areas.city.portsarim.configs.port_sarim_npcs
import org.rsmod.content.areas.city.portsarim.configs.port_sarim_objs
import org.rsmod.game.entity.Npc
import org.rsmod.game.type.inv.InvType
import org.rsmod.game.type.npc.NpcType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The shopkeepers of Port Sarim, Rimmington and Musa Point, and the Rusty Anchor's bartender. Stock
 * is in [org.rsmod.content.areas.city.portsarim.configs.PortSarimInvEditor].
 */
class PortSarimShops @Inject constructor(private val shops: Shops) : PluginScript() {
    override fun ScriptContext.startup() {
        shop(port_sarim_npcs.wydin, WYDIN) { wydin(it) }
        shop(port_sarim_npcs.betty, BETTY) { betty(it) }
        // Note: the greetings of the shops below are unverified.
        shop(port_sarim_npcs.battleaxe_brian, BATTLEAXES) {
            ask(
                it,
                BATTLEAXES,
                "Welcome to Brian's Battleaxe Bazaar! Do you want a battleaxe?",
                "Yes please. What are you selling?",
            )
        }
        shop(port_sarim_npcs.gerrant, GERRANT) {
            ask(
                it,
                GERRANT,
                "Welcome! You can buy fishing equipment at my store. We'll also buy anything " +
                    "you catch off you.",
                "Let's see what you've got then.",
                "Sorry, I'm not interested.",
            )
        }
        shop(port_sarim_npcs.grum, GRUM) {
            ask(
                it,
                GRUM,
                "Would you like to buy or sell some gold jewellery?",
                "Yes please.",
                "No, I'm not interested in jewellery.",
            )
        }
        shop(port_sarim_npcs.rommik, ROMMIK) {
            ask(
                it,
                ROMMIK,
                "Hello. How can I help you?",
                "I'm looking for some crafting equipment.",
                "No thanks, I'm fine.",
            )
        }
        shop(port_sarim_npcs.archery_brian, ARCHERY) {
            ask(
                it,
                ARCHERY,
                "Welcome to Brian's Archery Supplies. Do you want to buy any archery equipment?",
                "Yes please. What are you selling?",
            )
        }
        shop(port_sarim_npcs.zembo, ZEMBO) {
            ask(
                it,
                ZEMBO,
                "Welcome to Karamja Wines, Spirits and Beers! What can I get you?",
                "What have you got?",
                "Nothing, thanks.",
            )
        }
        for (keeper in
            listOf(
                port_sarim_npcs.rimmington_keeper to RIMMINGTON,
                port_sarim_npcs.rimmington_assistant to RIMMINGTON,
                port_sarim_npcs.karamja_keeper to KARAMJA,
                port_sarim_npcs.karamja_assistant to KARAMJA,
            )) {
            shop(keeper.first, keeper.second) { generalStore(it, keeper.second) }
        }

        for (npc in listOf(port_sarim_npcs.stan) + port_sarim_npcs.stan_crew) {
            shop(npc, STAN) { stan(it) }
            // Note: every charter destination is off the map, so the op only apologises.
            onOpNpc4(npc) {
                startDialogue(it.npc) {
                    chatNpc(
                        neutral,
                        "Sorry, matey, none of my ships can sail anywhere you could go.",
                    )
                }
            }
        }

        onOpNpc1(port_sarim_npcs.bartender) { startDialogue(it.npc) { bartender() } }
    }

    private class ShopDef(val title: String, val inv: InvType)

    private fun Dialogue.open(npc: Npc, shop: ShopDef) =
        shops.open(player, npc, shop.title, shop.inv)

    /** Talk-to runs [talk]; Trade opens the shop. Every keeper also answers over the counter. */
    private fun ScriptContext.shop(
        npc: NpcType,
        shop: ShopDef,
        talk: suspend Dialogue.(Npc) -> Unit,
    ) {
        onOpNpc1(npc) { startDialogue(it.npc) { talk(it.npc) } }
        onOpNpc3(npc) { shops.open(player, it.npc, shop.title, shop.inv) }
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

    private suspend fun Dialogue.ask(
        npc: Npc,
        shop: ShopDef,
        greeting: String,
        yes: String,
        no: String = "No thanks.",
    ) {
        chatNpc(happy, greeting)
        val choice = choice2(yes, 1, no, 2)
        if (choice == 1) {
            chatPlayer(happy, yes)
            open(npc, shop)
            return
        }
        chatPlayer(neutral, no)
    }

    private suspend fun Dialogue.generalStore(npc: Npc, shop: ShopDef) {
        chatNpc(happy, "Can I help you at all?")
        val choice = choice2("Yes please. What are you selling?", 1, "No thanks.", 2)
        if (choice == 1) {
            open(npc, shop)
            return
        }
        chatPlayer(neutral, "No thanks.")
    }

    private suspend fun Dialogue.wydin(npc: Npc) {
        chatNpc(happy, "Welcome to my food store! Would you like to buy anything?")
        val choice = choice3("Yes please.", 1, "No, thank you.", 2, "What can you recommend?", 3)
        when (choice) {
            1 -> {
                chatPlayer(happy, "Yes please.")
                open(npc, WYDIN)
            }
            2 -> chatPlayer(neutral, "No, thank you.")
            3 -> {
                chatPlayer(quiz, "What can you recommend?")
                chatNpc(
                    happy,
                    "We have this really exotic fruit all the way from Karamja. It's called a " +
                        "banana.",
                )
                val buy =
                    choice2("Hmm, I think I'll try one.", 1, "I don't like the sound of that.", 2)
                if (buy == 1) {
                    chatPlayer(happy, "Hmm, I think I'll try one.")
                    open(npc, WYDIN)
                } else {
                    chatPlayer(neutral, "I don't like the sound of that.")
                }
            }
        }
    }

    private suspend fun Dialogue.betty(npc: Npc) {
        chatNpc(happy, "Welcome to the magic emporium.")
        val choice = choice2("Can I see your wares?", 1, "Sorry, I'm not into magic.", 2)
        if (choice == 1) {
            chatPlayer(quiz, "Can I see your wares?")
            open(npc, BETTY)
            return
        }
        chatPlayer(neutral, "Sorry, I'm not into magic.")
        chatNpc(happy, "Well, if you see anyone who is into magic, please send them my way.")
    }

    private suspend fun Dialogue.stan(npc: Npc) {
        chatNpc(happy, "Ahoy there! Care to look at my wares?")
        val choice = choice2("Let's see what you've got.", 1, "No thanks.", 2)
        if (choice == 1) {
            chatPlayer(happy, "Let's see what you've got.")
            open(npc, STAN)
            return
        }
        chatPlayer(neutral, "No thanks.")
    }

    // Note: wording unverified.
    private suspend fun Dialogue.bartender() {
        chatNpc(neutral, "Yes please? What can I get you?")
        val choice = choice2("A beer, please.", 1, "Nothing, thanks.", 2)
        if (choice == 2) {
            chatPlayer(neutral, "Nothing, thanks.")
            return
        }
        chatPlayer(happy, "A beer, please.")
        chatNpc(neutral, "That'll be 2 coins.")
        if (access.inv.isFull()) {
            chatNpc(neutral, "You don't have room for it.")
        } else if (!access.invTakeFee(BEER_PRICE)) {
            chatPlayer(sad, "Oh dear, I don't seem to have enough money.")
        } else {
            access.invAdd(access.inv, port_sarim_objs.beer)
            access.mes("You buy a pint of beer.")
        }
    }

    private companion object {
        const val COUNTER_RANGE = 2
        const val BEER_PRICE = 2

        val WYDIN = ShopDef("Wydin's Food Store", port_sarim_invs.wydin)
        val BETTY = ShopDef("Betty's Magic Emporium", port_sarim_invs.betty)
        val BATTLEAXES = ShopDef("Brian's Battleaxe Bazaar", port_sarim_invs.battleaxes)
        val GERRANT = ShopDef("Gerrant's Fishy Business", port_sarim_invs.gerrant)
        val GRUM = ShopDef("Grum's Gold Exchange", port_sarim_invs.grum)
        val ROMMIK = ShopDef("Rommik's Crafty Supplies", port_sarim_invs.rommik)
        val ARCHERY = ShopDef("Brian's Archery Supplies", port_sarim_invs.archery)
        val RIMMINGTON = ShopDef("Rimmington General Store", port_sarim_invs.rimmington_general)
        val KARAMJA = ShopDef("Karamja General Store", port_sarim_invs.karamja_general)
        val ZEMBO = ShopDef("Karamja Wines, Spirits and Beers", port_sarim_invs.zembo)
        val STAN = ShopDef("Trader Stan's Trading Post", port_sarim_invs.trader_stan)
    }
}
