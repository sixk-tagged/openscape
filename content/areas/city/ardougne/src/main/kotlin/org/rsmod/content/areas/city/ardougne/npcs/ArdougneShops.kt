package org.rsmod.content.areas.city.ardougne.npcs

import jakarta.inject.Inject
import org.rsmod.api.combat.commons.npc.queueCombatRetaliate
import org.rsmod.api.config.refs.varps
import org.rsmod.api.hunt.Hunt
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.script.onOpNpc4
import org.rsmod.api.shops.Shops
import org.rsmod.content.areas.city.ardougne.configs.ArdougneVarps
import org.rsmod.content.areas.city.ardougne.configs.ardougne_invs
import org.rsmod.content.areas.city.ardougne.configs.ardougne_npcs
import org.rsmod.content.areas.city.ardougne.configs.ardougne_objs
import org.rsmod.game.entity.Npc
import org.rsmod.game.type.hunt.HuntVis
import org.rsmod.game.type.inv.InvType
import org.rsmod.game.type.npc.NpcType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.varp.VarpType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The shops of East and West Ardougne, Port Khazard, Witchaven and the Tree Gnome Village, and the
 * two bars: Talk-to offers the shop and Trade opens it directly.
 */
// UNVERIFIED: the greetings are the keepers' usual openers; the wiki reference has none.
class ArdougneShops @Inject constructor(private val shops: Shops, private val hunt: Hunt) :
    PluginScript() {
    override fun ScriptContext.startup() {
        with(ardougne_npcs) {
            for (npc in listOf(aemad, kortan)) {
                keeper(npc, "Aemad's Adventuring Supplies", ardougne_invs.aemad, GENERAL)
            }
            keeper(
                zenesha,
                "Zenesha's Plate Mail Body Shop",
                ardougne_invs.zenesha,
                "Hello there, can I interest you in some armour?",
            )
            for (npc in listOf(baker_west, baker_east)) {
                keeper(
                    npc,
                    "Ardougne Baker's Stall",
                    ardougne_invs.bakery,
                    "Good day, would you like to buy some of my baked goods?",
                    grudge = ArdougneVarps.baker_angry_until,
                )
            }
            keeper(
                silver_merchant,
                "Ardougne Silver Stall",
                ardougne_invs.silver,
                "Do you want to buy any of my fine silver?",
            )
            keeper(
                gem_merchant,
                "Ardougne Gem Stall",
                ardougne_invs.gems,
                "Here, look at my lovely gems.",
            )
            keeper(
                spice_seller,
                "Ardougne Spice Stall",
                ardougne_invs.spice,
                "Would you like to buy some spices?",
                grudge = ArdougneVarps.spice_angry_until,
            )
            keeper(
                fur_trader,
                "Ardougne Fur Stall",
                ardougne_invs.fur,
                "Would you like to trade in fur?",
            )
            keeper(
                richard,
                "Richard's Farming Shop",
                ardougne_invs.farming,
                "Hello. I sell farming tools and buy crops. Would you like to trade?",
            )
            keeper(chadwell, "West Ardougne General Store", ardougne_invs.west_general, GENERAL)
            keeper(
                khazard_shopkeeper,
                "Khazard General Store",
                ardougne_invs.khazard_general,
                GENERAL,
            )
            keeper(
                lovecraft,
                "Lovecraft's Tackle",
                ardougne_invs.witchaven_tackle,
                "Welcome to Lovecraft's Tackle! Can I interest you in some fishing supplies?",
                tradeOp = 4,
            )
            keeper(bolkoy, "Bolkoy's Village Shop", ardougne_invs.gnome_village, GENERAL)

            onOpNpc1(flying_horse_bartender) { startDialogue(it.npc) { bar(BEER_ONLY) } }
            onOpNpc1(khazard_barman) { startDialogue(it.npc) { bar(BEER_AND_BREW) } }
        }
    }

    /** [tradeOp] is the npc's Trade op in the cache: 3 for most, 4 ("Shop") for Lovecraft. */
    private fun ScriptContext.keeper(
        npc: NpcType,
        title: String,
        inv: InvType,
        greeting: String,
        tradeOp: Int = 3,
        grudge: VarpType? = null,
    ) {
        onOpNpc1(npc) {
            if (holdsGrudge(grudge)) {
                callGuards(it.npc)
            } else {
                startDialogue(it.npc) { shopTalk(it.npc, title, inv, greeting) }
            }
        }
        when (tradeOp) {
            3 -> onOpNpc3(npc) { trade(it.npc, title, inv, grudge) }
            4 -> onOpNpc4(npc) { trade(it.npc, title, inv, grudge) }
            else -> error("No trade op $tradeOp")
        }
    }

    private suspend fun ProtectedAccess.trade(
        npc: Npc,
        title: String,
        inv: InvType,
        grudge: VarpType?,
    ) {
        if (holdsGrudge(grudge)) {
            callGuards(npc)
        } else {
            shops.open(player, npc, title, inv)
        }
    }

    private fun ProtectedAccess.holdsGrudge(grudge: VarpType?): Boolean =
        grudge != null && player.vars[varps.playtime] < player.vars[grudge]

    /** A robbed stall owner (OSRS wiki) turns the thief away and calls the guards. */
    private suspend fun ProtectedAccess.callGuards(owner: Npc) {
        for (guard in hunt.findNpcs(player.coords, GUARD_RANGE, HuntVis.LineOfSight)) {
            if (ardougne_npcs.guards.any { it.id == guard.type.id }) {
                guard.facePlayer(player)
                guard.queueCombatRetaliate(player)
            }
        }
        startDialogue(owner) { chatNpc(angry, "Guards, guards! It's that thief!") }
    }

    private suspend fun Dialogue.shopTalk(npc: Npc, title: String, inv: InvType, greeting: String) {
        chatNpc(happy, greeting)
        val choice = choice2("Yes please. What are you selling?", 1, "No thanks.", 2)
        if (choice == 1) {
            shops.open(player, npc, title, inv)
        } else {
            chatPlayer(neutral, "No thanks.")
        }
    }

    private class Drink(val name: String, val obj: ObjType, val price: Int)

    private suspend fun Dialogue.bar(drinks: List<Drink>) {
        chatNpc(neutral, "Yes please? What can I get you?")
        val choice =
            if (drinks.size == 1) {
                choice2("A beer, please.", 0, "Nothing, thanks.", -1)
            } else {
                choice3("A beer, please.", 0, "A Khali brew, please.", 1, "Nothing, thanks.", -1)
            }
        if (choice < 0) {
            chatPlayer(neutral, "Nothing, thanks.")
            return
        }
        val drink = drinks[choice]
        chatPlayer(happy, "A ${drink.name}, please.")
        chatNpc(neutral, "That'll be ${drink.price} coins.")
        if (access.inv.isFull()) {
            chatNpc(neutral, "You don't have room for it.")
        } else if (!access.invTakeFee(drink.price)) {
            chatPlayer(sad, "Oh dear, I don't seem to have enough money.")
        } else {
            access.invAdd(access.inv, drink.obj)
            access.mes("You buy a ${drink.name}.")
        }
    }

    private companion object {
        const val GENERAL = "Can I help you at all?"
        // UNVERIFIED: how far away the guards hear the call (the stalls use the same 8).
        const val GUARD_RANGE = 8

        val BEER = Drink("beer", ardougne_objs.beer, 2)
        val BEER_ONLY = listOf(BEER)
        // UNVERIFIED: the Khali brew's 5 coins are the wiki's; the Fight Arena's brew is only
        // meant to be sold once the head guard has been spoken to.
        val BEER_AND_BREW = listOf(BEER, Drink("Khali brew", ardougne_objs.khali_brew, 5))
    }
}
