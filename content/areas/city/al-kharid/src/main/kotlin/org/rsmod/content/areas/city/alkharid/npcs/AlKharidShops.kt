package org.rsmod.content.areas.city.alkharid.npcs

import jakarta.inject.Inject
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.shops.Shops
import org.rsmod.content.areas.city.alkharid.configs.alkharid_invs
import org.rsmod.content.areas.city.alkharid.configs.alkharid_npcs
import org.rsmod.game.entity.Npc
import org.rsmod.game.type.inv.InvType
import org.rsmod.game.type.npc.NpcType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** Al Kharid's shops: Talk-to offers the shop and Trade opens it directly. */
class AlKharidShops @Inject constructor(private val shops: Shops) : PluginScript() {
    override fun ScriptContext.startup() {
        for (npc in listOf(alkharid_npcs.shop_keeper, alkharid_npcs.shop_assistant)) {
            keeper(npc, "Al Kharid General Store", alkharid_invs.general, "Can I help you at all?")
        }
        keeper(
            alkharid_npcs.dommik,
            "Dommik's Crafting Store",
            alkharid_invs.crafting,
            "Hello. Would you like to buy any crafting equipment?",
        )
        keeper(
            alkharid_npcs.louie,
            "Louie's Armoured Legs Bazaar",
            alkharid_invs.legs,
            "Hey, wanna buy some armour?",
        )
        keeper(
            alkharid_npcs.ranael,
            "Ranael's Super Skirt Store",
            alkharid_invs.skirts,
            "Do you want to buy any armoured skirts? Designed especially for ladies who like " +
                "to fight.",
        )
        keeper(
            alkharid_npcs.zeke,
            "Zeke's Superior Scimitars",
            alkharid_invs.scimitars,
            "A thousand greetings, my friend. Would you like to buy a fine scimitar?",
        )
        keeper(
            alkharid_npcs.gem_trader,
            "Gem Trader",
            alkharid_invs.gems,
            "Good day to you, traveller. Would you be interested in buying some gems?",
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
