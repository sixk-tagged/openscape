package org.rsmod.content.areas.city.alkharid.npcs

import jakarta.inject.Inject
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.random.GameRandom
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.shops.Shops
import org.rsmod.content.areas.city.alkharid.configs.alkharid_invs
import org.rsmod.content.areas.city.alkharid.configs.alkharid_npcs
import org.rsmod.content.areas.city.alkharid.configs.alkharid_objs
import org.rsmod.game.type.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The dialogue vendors and gate guards of Al Kharid: Karim, the silk trader, Ali and Selim. Also
 * the camels.
 */
class AlKharidServices
@Inject
constructor(private val shops: Shops, private val random: GameRandom) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(alkharid_npcs.karim) { startDialogue(it.npc) { karim() } }
        onOpNpc1(alkharid_npcs.silk_trader) { startDialogue(it.npc) { silkTrader() } }

        onOpNpc1(alkharid_npcs.ali_morrisane) { startDialogue(it.npc) { aliMorrisane() } }
        onOpNpc3(alkharid_npcs.ali_morrisane) {
            shops.open(player, it.npc, ALI_SHOP, alkharid_invs.ali)
        }

        onOpNpc1(alkharid_npcs.border_guard1) { startDialogue(it.npc) { borderGuard() } }
        onOpNpc1(alkharid_npcs.border_guard2) { startDialogue(it.npc) { borderGuard() } }

        onOpNpc1(alkharid_npcs.selim) { startDialogue(it.npc) { selim() } }
        onOpNpc1(alkharid_npcs.hassan) {
            startDialogue(it.npc) {
                chatNpc(neutral, "Greetings. The city of Al-Kharid is grateful for your help.")
            }
        }
        onOpNpc1(alkharid_npcs.prince_ali) {
            startDialogue(it.npc) { chatNpc(happy, "Hello, my friend. It is good to be free.") }
        }

        // Note: the members' Camulet chat is skipped; without one a camel spits or glares
        // (wording unverified).
        for (camel in alkharid_npcs.camels) {
            onOpNpc1(camel) {
                mes(
                    if (random.randomBoolean()) "The camel spits at you."
                    else "The camel turns its head and glares at you."
                )
            }
        }
    }

    private suspend fun Dialogue.karim() {
        chatNpc(happy, "Would you like to buy a nice kebab? Only one gold.")
        val choice = choice2("I think I'll give it a miss.", 2, "Yes please.", 1)
        if (choice == 2) {
            chatPlayer(neutral, "I think I'll give it a miss.")
            return
        }
        chatPlayer(happy, "Yes please.")
        if (access.invCoinTotal() < KEBAB_PRICE) {
            chatPlayer(sad, "Oops, I forgot to bring any money with me.")
            chatNpc(neutral, "Come back when you have some.")
        } else if (access.inv.isFull()) {
            chatNpc(neutral, "You don't have room for it.")
        } else {
            access.invTakeFee(KEBAB_PRICE)
            access.invAdd(access.inv, alkharid_objs.kebab)
            access.mes("You buy a kebab.")
        }
    }

    // He asks 3 gp; refuse and he drops to 2 gp, "as low as I'll go".
    private suspend fun Dialogue.silkTrader() {
        chatNpc(happy, "Do you want to buy any fine silks?")
        val choice = choice2("How much are they?", 1, "No, thanks.", 2)
        if (choice == 2) {
            chatPlayer(neutral, "No, thanks.")
            return
        }
        chatPlayer(quiz, "How much are they?")
        chatNpc(neutral, "3 gp.")
        var price = SILK_PRICE
        if (choice2("No. That's too much for me.", 1, "Okay, that sounds fair.", 2) == 1) {
            chatPlayer(neutral, "No. That's too much for me.")
            chatNpc(neutral, "2 gp and that's as low as I'll go.")
            if (choice2("No, thanks.", 1, "Okay, that's not too bad.", 2) == 1) {
                chatPlayer(neutral, "No, thanks.")
                return
            }
            price = SILK_LOW_PRICE
        }
        chatPlayer(neutral, "Okay, I'll take one.")
        if (access.invCoinTotal() < price) {
            chatPlayer(sad, "Oh dear, I don't have enough money.")
        } else if (access.inv.isFull()) {
            chatNpc(neutral, "You don't have room for it.")
        } else {
            access.invTakeFee(price)
            access.invAdd(access.inv, alkharid_objs.silk)
            access.mes("You buy a silk for $price gp.")
        }
    }

    // Ali pushes one piece of junk on anyone carrying coins before showing his stock.
    private suspend fun Dialogue.aliMorrisane() {
        chatNpc(
            happy,
            "Greetings, my friend! I am Ali Morrisane, the greatest salesman in the world.",
        )
        // UNVERIFIED: the Ali Morrisane page says 101 coins, the shop page says 10.
        if (access.invCoinTotal() >= JUNK_MIN_COINS && !access.inv.isFull()) {
            access.invTakeFee(JUNK_PRICE)
            access.invAdd(access.inv, access.random.pick(JUNK))
            access.mes(
                "Despite your best efforts Ali Morrisane, the greatest salesman in the world, " +
                    "still manages to sell you some junk."
            )
        }
        val choice = choice2("Can I see your wares?", 1, "No thanks.", 2)
        if (choice == 1) {
            shops.open(player, npc!!, ALI_SHOP, alkharid_invs.ali)
        } else {
            chatPlayer(neutral, "No thanks.")
        }
    }

    // The toll is free: Prince Ali Rescue counts as done.
    private suspend fun Dialogue.borderGuard() {
        chatPlayer(quiz, "Can I come through this gate?")
        chatNpc(neutral, "You may pass for free, you are a friend of Al-Kharid.")
    }

    private suspend fun Dialogue.selim() {
        chatNpc(neutral, "Greetings. I have a few fine blades to sell, should you be interested.")
        val choice =
            choice3(
                "Buy a keris (20,000 coins).",
                alkharid_objs.keris,
                "Buy a keris partisan (60,000 coins).",
                alkharid_objs.keris_partisan,
                "No thanks.",
                null,
            )
        if (choice == null) {
            chatPlayer(neutral, "No thanks.")
            return
        }
        val price = if (choice == alkharid_objs.keris) KERIS_PRICE else PARTISAN_PRICE
        if (access.inv.isFull()) {
            chatNpc(neutral, "You don't have room for it.")
        } else if (!access.invTakeFee(price)) {
            chatNpc(neutral, "You don't have enough coins for that.")
        } else {
            access.invAdd(access.inv, choice)
            access.mes("You hand over $price coins and receive a blade.")
        }
    }

    private companion object {
        const val ALI_SHOP = "Ali's Discount Wares"
        const val KEBAB_PRICE = 1
        const val SILK_PRICE = 3
        const val SILK_LOW_PRICE = 2
        const val JUNK_MIN_COINS = 101
        const val JUNK_PRICE = 10
        const val KERIS_PRICE = 20_000
        const val PARTISAN_PRICE = 60_000

        val JUNK: List<ObjType> =
            listOf(
                alkharid_objs.bronze_pickaxe,
                alkharid_objs.bucket,
                alkharid_objs.knife,
                alkharid_objs.raw_chicken,
                alkharid_objs.tinderbox,
            )
    }
}
