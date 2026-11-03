package org.rsmod.content.areas.misc.wilderness.npcs

import jakarta.inject.Inject
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.script.onApNpc1
import org.rsmod.api.script.onApNpc3
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.script.onOpNpc4
import org.rsmod.api.shops.Shops
import org.rsmod.content.areas.misc.wilderness.configs.wilderness_invs
import org.rsmod.content.areas.misc.wilderness.configs.wilderness_npcs
import org.rsmod.game.entity.Npc
import org.rsmod.game.type.inv.InvType
import org.rsmod.game.type.npc.NpcType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The Wilderness shopkeepers: the nine cape merchants, Tony's pizza base shop, the Bandit Duty Free
 * store in the Bandit Camp and Lundail's rune shop in the Mage Arena bank. Stock is in
 * [org.rsmod.content.areas.misc.wilderness.configs.WildernessInvEditor].
 */
class WildernessShops @Inject constructor(private val shops: Shops) : PluginScript() {
    override fun ScriptContext.startup() {
        val sellers =
            with(wilderness_npcs) {
                listOf(william, ian, larry, darren, edward, neil, edmond, simon, sam)
            }
        for ((index, seller) in sellers.withIndex()) {
            val shop = ShopDef("Wilderness Cape Shop", wilderness_invs.cape_shops[index])
            shop(seller, shop) { capeMerchant(it, shop) }
        }
        shop(wilderness_npcs.fat_tony, PIZZA_BASES, counter = true) { fatTony(it) }
        shop(wilderness_npcs.noterazzo, DUTY_FREE, counter = true) { noterazzo(it) }
        shop(wilderness_npcs.lundail, LUNDAIL) { lundail(it) }
        onOpNpc4(wilderness_npcs.lundail) { startDialogue(it.npc) { freeRunes() } }
    }

    private class ShopDef(val title: String, val inv: InvType)

    /** Talk-to runs [talk]; Trade opens the shop. Counter keepers also answer from their front. */
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

    private suspend fun Dialogue.ask(npc: Npc, shop: ShopDef, greeting: String, yes: String) {
        chatNpc(happy, greeting)
        val choice = choice2(yes, 1, "No thanks.", 2)
        if (choice == 1) {
            shops.open(player, npc, shop.title, shop.inv)
            return
        }
        chatPlayer(neutral, "No thanks.")
    }

    // Note: wording of all three greetings unverified.
    private suspend fun Dialogue.capeMerchant(npc: Npc, shop: ShopDef) =
        ask(npc, shop, "Hello there! Would you like to buy a team cape?", "What are you selling?")

    private suspend fun Dialogue.fatTony(npc: Npc) =
        ask(npc, PIZZA_BASES, "Would you like to buy some pizza bases?", "Yes please.")

    private suspend fun Dialogue.noterazzo(npc: Npc) =
        ask(
            npc,
            DUTY_FREE,
            "Welcome to the Bandit Duty Free! Can I help you?",
            "What are you selling?",
        )

    /** Lundail's lines are the wiki's transcript. */
    private suspend fun Dialogue.lundail(npc: Npc) {
        chatNpc(happy, "Hello there. How can I help you, brave adventurer?")
        val choice =
            choice3(
                "What are you selling?",
                1,
                "What's that big old building above us?",
                2,
                "Claim free runes.",
                3,
            )
        when (choice) {
            1 -> {
                chatPlayer(quiz, "What are you selling?")
                chatNpc(
                    happy,
                    "I sell rune stones. I've got some good stuff, some really powerful little " +
                        "rocks. Take a look.",
                )
                shops.open(player, npc, LUNDAIL.title, LUNDAIL.inv)
            }
            2 -> {
                chatPlayer(quiz, "What's that big old building above us?")
                chatNpc(
                    happy,
                    "That, my friend, is the mage battle arena. Top mages come from all over " +
                        "Gielinor to compete in the arena.",
                )
                chatPlayer(neutral, "Wow.")
                chatNpc(happy, "Few return, most get fried, hence the smell.")
                chatPlayer(neutral, "Hmmm.. I did notice.")
            }
            else -> {
                chatPlayer(neutral, "Claim free runes.")
                freeRunes()
            }
        }
    }

    // The Wilderness Diary is out of scope, so nobody has finished its Easy tier.
    private suspend fun Dialogue.freeRunes() {
        chatNpc(
            neutral,
            "You'll need to have completed the Easy Wilderness Achievement Diary to qualify for " +
                "free runes.",
        )
    }

    private companion object {
        const val COUNTER_RANGE = 2
        val PIZZA_BASES = ShopDef("Tony's Pizza Bases", wilderness_invs.pizza_bases)
        val DUTY_FREE = ShopDef("Bandit Duty Free", wilderness_invs.duty_free)
        val LUNDAIL = ShopDef("Lundail's Arena-side Rune Shop.", wilderness_invs.lundail_shop)
    }
}
