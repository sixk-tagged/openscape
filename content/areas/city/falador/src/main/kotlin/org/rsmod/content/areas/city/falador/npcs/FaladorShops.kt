package org.rsmod.content.areas.city.falador.npcs

import jakarta.inject.Inject
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.shops.Shops
import org.rsmod.content.areas.city.falador.configs.falador_invs
import org.rsmod.content.areas.city.falador.configs.falador_npcs
import org.rsmod.game.entity.Npc
import org.rsmod.game.type.inv.InvType
import org.rsmod.game.type.npc.NpcType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Falador's, the Dwarven Mine's and the Mining Guild's shops: Talk-to offers the shop and Trade
 * opens it directly.
 */
// UNVERIFIED: the greetings are the keepers' usual openers; the wiki reference has none.
class FaladorShops @Inject constructor(private val shops: Shops) : PluginScript() {
    override fun ScriptContext.startup() {
        for (npc in listOf(falador_npcs.shop_keeper, falador_npcs.shop_assistant)) {
            keeper(npc, "Falador General Store", falador_invs.general, "Can I help you at all?")
        }
        keeper(
            falador_npcs.flynn,
            "Flynn's Mace Market",
            falador_invs.maces,
            "Hello. Do you want to buy or sell any maces?",
        )
        keeper(
            falador_npcs.cassie,
            "Cassie's Shield Shop",
            falador_invs.shields,
            "I buy and sell shields; do you want to trade?",
        )
        keeper(falador_npcs.herquin, "Herquin's Gems", falador_invs.gems, "Do you wish to trade?")
        keeper(
            falador_npcs.wayne,
            "Wayne's Chains",
            falador_invs.chains,
            "Welcome to Wayne's Chains. Do you wanna buy or sell some chain mail?",
        )
        keeper(
            falador_npcs.sarah,
            "Sarah's Farming Shop",
            falador_invs.farming,
            "Hello. I sell farming tools and buy crops. Would you like to trade?",
        )
        keeper(
            falador_npcs.drogo,
            "Drogo's Mining Emporium",
            falador_invs.drogo,
            "'Ello. Welcome to my Mining shop, friend.",
        )
        keeper(
            falador_npcs.nurmof,
            "Nurmof's Pickaxe Shop",
            falador_invs.pickaxes,
            "Greetings and welcome to my pickaxe shop. Do you want to buy my premium quality " +
                "pickaxes?",
        )
        keeper(
            falador_npcs.hura,
            "Crossbow Shop",
            falador_invs.crossbows,
            "Hello there. Would you like to see my crossbow parts?",
        )
        keeper(
            falador_npcs.dwarven_shopkeeper,
            "Dwarven Shopping Store",
            falador_invs.dwarven_general,
            "Can I help you at all?",
        )
        keeper(
            falador_npcs.hendor,
            "Hendor's Awesome Ores",
            falador_invs.guild_ores,
            "Welcome to Hendor's Awesome Ores. Would you like to trade?",
        )
        keeper(
            falador_npcs.yarsul,
            "Yarsul's Prodigious Pickaxes",
            falador_invs.guild_pickaxes,
            "Welcome to Yarsul's Prodigious Pickaxes. Would you like to trade?",
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
