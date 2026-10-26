package org.rsmod.content.areas.city.draynor.npcs

import jakarta.inject.Inject
import org.rsmod.api.combat.commons.npc.queueCombatRetaliate
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.varps
import org.rsmod.api.hunt.Hunt
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.script.onOpNpc4
import org.rsmod.api.script.onOpNpcU
import org.rsmod.api.shops.Shops
import org.rsmod.content.areas.city.draynor.configs.draynor_invs
import org.rsmod.content.areas.city.draynor.configs.draynor_npcs
import org.rsmod.content.areas.city.draynor.configs.draynor_objs
import org.rsmod.content.areas.city.draynor.configs.draynor_varps
import org.rsmod.game.entity.Npc
import org.rsmod.game.type.hunt.HuntVis
import org.rsmod.game.type.inv.InvType
import org.rsmod.game.type.npc.NpcType
import org.rsmod.game.type.varp.VarpType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** Draynor's plain shops: Talk-to offers the shop, Trade (or Spria's op4) opens it directly. */
class DraynorShops @Inject constructor(private val shops: Shops, private val hunt: Hunt) :
    PluginScript() {
    override fun ScriptContext.startup() {
        keeper(
            draynor_npcs.diango,
            "Diango's Toy Store",
            draynor_invs.diango,
            "Why, hello there! Welcome to Diango's Toy Store. Care to take a look at my toys?",
        )
        keeper(
            draynor_npcs.olivia,
            "Draynor Seed Market",
            draynor_invs.seeds,
            "Hello, would you like to buy any seeds?",
            grudge = draynor_varps.olivia_angry_until,
        )
        keeper(
            draynor_npcs.fortunato,
            "Fortunato's Fine Wine",
            draynor_invs.wine,
            "Welcome to my fine wine store! Would you like to have a look?",
        )
        keeper(
            draynor_npcs.ava,
            "Ava's Odds and Ends",
            draynor_invs.ava,
            "Hello there. Can I interest you in some of my wares?",
        )
        keeper(
            draynor_npcs.spria,
            "Slayer Equipment",
            draynor_invs.slayer,
            "Hello there. I can sell you some Slayer equipment, if you like.",
            tradeOp = 4,
        )
        // Ned's Talk-to is his rope dialogue (DraynorServices). The Fremennik Isles counts as done,
        // so the shop has its renamed title and 90% prices.
        onOpNpc3(draynor_npcs.ned) {
            shops.open(
                player,
                it.npc,
                "Ned's Handmade Rope (100% Wool - not yak, honest!)",
                draynor_invs.rope,
            )
        }

        onOpNpcU(draynor_npcs.fortunato, draynor_objs.bottle_empty) { sellBottles() }
    }

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
        if (tradeOp == 4) {
            onOpNpc4(npc) { trade(it.npc, title, inv, grudge) }
        } else {
            onOpNpc3(npc) { trade(it.npc, title, inv, grudge) }
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

    /** A robbed stall owner (Olivia's wiki page) turns the thief away and calls the guards. */
    private suspend fun ProtectedAccess.callGuards(owner: Npc) {
        for (guard in hunt.findNpcs(player.coords, GUARD_RANGE, HuntVis.LineOfSight)) {
            if (guard.type.id == draynor_npcs.market_guard.id) {
                guard.facePlayer(player)
                guard.queueCombatRetaliate(player)
            }
        }
        startDialogue(owner) {
            chatNpc(angry, "You're the one who stole something from me! Guards, guards!")
        }
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

    // Fortunato pays 2 coins for every empty wine bottle handed over; there is no dialogue.
    private fun ProtectedAccess.sellBottles() {
        val count = invTotal(inv, draynor_objs.bottle_empty)
        if (count == 0 || invDel(inv, draynor_objs.bottle_empty, count).failure) {
            return
        }
        invAdd(inv, objs.coins, count * BOTTLE_PRICE)
        mes("Fortunato gives you ${count * BOTTLE_PRICE} coins for the empty bottles.")
    }

    private companion object {
        const val BOTTLE_PRICE = 2
        // UNVERIFIED: how far away the Market Guards hear the call (the stalls use the same 8).
        const val GUARD_RANGE = 8
    }
}
