package org.rsmod.content.areas.misc.kandarinnorth.npcs

import jakarta.inject.Inject
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.shops.Shops
import org.rsmod.content.areas.misc.kandarinnorth.configs.kandarin_north_invs
import org.rsmod.content.areas.misc.kandarinnorth.configs.kandarin_north_npcs
import org.rsmod.game.entity.Npc
import org.rsmod.game.type.inv.InvType
import org.rsmod.game.type.npc.NpcType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Catherby's, the guilds' and the Legends' Guild's shops: Talk-to offers the shop and Trade opens
 * it directly. Stock is in `KandarinNorthInvEditor`.
 */
// UNVERIFIED: the greetings are the keepers' usual openers; the wiki reference has none.
class KandarinNorthShops @Inject constructor(private val shops: Shops) : PluginScript() {
    override fun ScriptContext.startup() {
        keeper(
            kandarin_north_npcs.harry,
            "Harry's Fishing Shop",
            kandarin_north_invs.harry,
            "Welcome to Harry's Fishing Shop. Would you like to buy or sell some fishing gear?",
        )
        keeper(
            kandarin_north_npcs.arhein,
            "Arhein's General Goods",
            kandarin_north_invs.arhein,
            "Can I help you at all?",
        )
        keeper(
            kandarin_north_npcs.hickton,
            "Hickton's Archery Emporium",
            kandarin_north_invs.hickton,
            "Welcome to Hickton's Archery Emporium. Do you want to see my wares?",
        )
        keeper(
            kandarin_north_npcs.candle_maker,
            "Candle Shop",
            kandarin_north_invs.candle,
            "Hi! Would you like to buy a candle?",
        )
        keeper(
            kandarin_north_npcs.vanessa,
            "Vanessa's Farming Shop",
            kandarin_north_invs.vanessa,
            "Hello. I sell farming tools and buy crops. Would you like to trade?",
        )
        keeper(
            kandarin_north_npcs.roachey,
            "Fishing Guild Shop",
            kandarin_north_invs.roachey,
            "Welcome to the Fishing Guild shop. Would you like to buy or sell?",
        )
        keeper(
            kandarin_north_npcs.aaron,
            "Aaron's Archery Appendages",
            kandarin_north_invs.aaron,
            "Welcome to Aaron's Archery Appendages. Would you like to trade?",
        )
        keeper(
            kandarin_north_npcs.dargaud,
            "Dargaud's Bows and Arrows",
            kandarin_north_invs.dargaud,
            "Welcome to Dargaud's Bows and Arrows. Would you like to trade?",
        )
        keeper(
            kandarin_north_npcs.tribal_salesman,
            "Authentic Throwing Weapons",
            kandarin_north_invs.tribal,
            "Welcome to Authentic Throwing Weapons. Would you like to trade?",
        )
        keeper(
            kandarin_north_npcs.fionella,
            "Legends' Guild General Store",
            kandarin_north_invs.legends_general,
            "Can I help you at all?",
        )
        keeper(
            kandarin_north_npcs.siegfried,
            "Legends' Guild Shop of Useful Items",
            kandarin_north_invs.erkle,
            "Hello, would you like to buy some of my useful items?",
        )
    }

    private fun ScriptContext.keeper(npc: NpcType, title: String, inv: InvType, greeting: String) {
        onOpNpc1(npc) { startDialogue(it.npc) { shopTalk(it.npc, title, inv, greeting) } }
        onOpNpc3(npc) { shops.open(player, it.npc, title, inv) }
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
}
