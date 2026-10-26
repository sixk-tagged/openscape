package org.rsmod.content.skills.crafting

import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.script.onIfModalButton
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.script.onOpNpcU
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Player
import org.rsmod.game.type.comp.ComponentType
import org.rsmod.game.type.npc.NpcType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.isType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** The click targets of one hide cell of the tanner interface (cache interface 324). */
class TanningSlot(
    val model: ComponentType,
    val text: ComponentType,
    val price: ComponentType,
    val all: ComponentType,
    val x: ComponentType,
    val five: ComponentType,
    val one: ComponentType,
)

/** One cell of the tanner interface: [raw] becomes [leather]. */
class Hide(val raw: ObjType, val leather: ObjType, val label: String)

/** Cell order a-h of the tanner interface. */
object TannerHides : ObjReferences() {
    val all =
        listOf(
            Hide(find("cow_hide"), find("leather"), "Soft leather"),
            Hide(find("cow_hide"), find("hard_leather"), "Hard leather"),
            Hide(find("templetrek_swamp_snake_hide"), find("village_snake_skin"), "Snakeskin"),
            Hide(find("village_snake_hide"), find("village_snake_skin"), "Snakeskin"),
            Hide(find("dragonhide_green"), find("dragon_leather"), "Green d'hide"),
            Hide(find("dragonhide_blue"), find("dragon_leather_blue"), "Blue d'hide"),
            Hide(find("dragonhide_red"), find("dragon_leather_red"), "Red d'hide"),
            Hide(find("dragonhide_black"), find("dragon_leather_black"), "Black d'hide"),
        )

    /** Every tanned product, for "I make the stuff!". */
    val leathers = all.map { it.leather }
}

/** Coins per hide in cell order, one list per kind of tanner. */
object TannerPrices {
    val standard = listOf(1, 3, 20, 15, 20, 20, 20, 20)
    /** Sbott and Chouani. */
    val dear = listOf(2, 5, 45, 25, 45, 45, 45, 45)
    // UNVERIFIED: Eodan's two snake prices conflict between his page and the Tanning page.
    val eodan = listOf(10, 30, 150, 200, 200, 200, 200, 200)
    val lists = listOf(standard, dear, eodan)
}

private var Player.tannerPrices: Int by intVarp(CraftingVarps.tanner)

class Tanning : PluginScript() {
    private class Tanner(val npc: NpcType, val prices: Int)

    private val tanners =
        with(CraftingNpcs) {
            listOf(
                Tanner(ellis, 0),
                Tanner(crafting_guild_tanner, 0),
                Tanner(mary, 0),
                Tanner(sbott, 1),
                Tanner(chouani, 1),
                Tanner(eodan, 2),
            )
        }

    override fun ScriptContext.startup() {
        for (tanner in tanners) {
            onOpNpc3(tanner.npc) { openTanner(tanner.prices) }
            onOpNpcU(tanner.npc) { useOnTanner(it.npc, tanner.prices, it.objType) }
        }
        onOpNpc1(CraftingNpcs.ellis) { talk(it.npc) }
        CraftingComponents.tanning.forEachIndexed { cell, slot ->
            onIfModalButton(slot.one) { tan(cell, 1) }
            onIfModalButton(slot.five) { tan(cell, 5) }
            onIfModalButton(slot.x) { tan(cell, countDialog()) }
            onIfModalButton(slot.all) { tan(cell, MAX_AMOUNT) }
        }
    }

    private fun ProtectedAccess.openTanner(prices: Int) {
        player.tannerPrices = prices
        ifOpenMainModal(CraftingInterfaces.tanner)
        CraftingComponents.tanning.forEachIndexed { cell, slot ->
            val hide = TannerHides.all[cell]
            // UNVERIFIED: the count argument is the model zoom or item count, 1 as the guide
            // prices search does.
            ifSetObj(slot.model, hide.leather, 1)
            ifSetText(slot.text, hide.label)
            val coins = TannerPrices.lists[prices][cell]
            ifSetText(slot.price, if (coins == 1) "1 coin" else "$coins coins")
        }
    }

    private fun ProtectedAccess.tan(cell: Int, amount: Int) {
        val hide = TannerHides.all[cell]
        val price = TannerPrices.lists[player.tannerPrices][cell]
        val name = ocName(hide.raw).lowercase()
        val hides = invTotal(inv, hide.raw)
        if (hides == 0) {
            mes("You don't have any $name to tan.")
            return
        }
        val count = minOf(amount.coerceAtLeast(1), hides, invTotal(inv, CraftingObjs.coins) / price)
        if (count == 0) {
            mes("You don't have enough coins to tan that.")
            return
        }
        invDel(inv, CraftingObjs.coins, count * price)
        invDel(inv, hide.raw, count)
        invAdd(inv, hide.leather, count)
    }

    private suspend fun ProtectedAccess.useOnTanner(npc: Npc, prices: Int, item: ObjType) {
        when {
            TannerHides.all.any { item.isType(it.raw) } -> openTanner(prices)
            TannerHides.leathers.any { item.isType(it) } ->
                startDialogue(npc) {
                    chatNpc(laugh, "Er... I have no use for that, I make the stuff!")
                }
            else -> startDialogue(npc) { chatNpc(confused, "Er... Thanks, but no thanks!") }
        }
    }

    private suspend fun ProtectedAccess.talk(npc: Npc) {
        startDialogue(npc) {
            val carried =
                TannerHides.all.map { it.raw }.distinctBy { it.id }.filter { it in player.inv }
            if (carried.isEmpty()) {
                chatNpc(happy, "Greetings friend. I am a manufacturer of leather.")
                offerLeather()
                return@startDialogue
            }
            val many = carried.sumOf { access.invTotal(player.inv, it) } > 1
            val hides = if (many) "some hides" else "a hide"
            chatNpc(
                happy,
                "I see you have brought me $hides. Would you like me to tan ${if (many) "them" else "it"} for you?",
            )
            if (choice2("Yes please.", true, "No thanks.", false)) {
                chatPlayer(happy, "Yes please.")
                access.openTanner(0)
            } else {
                chatPlayer(neutral, "No thanks.")
            }
        }
    }

    private suspend fun Dialogue.offerLeather() {
        val ask =
            choice2("Can I buy some leather then?", true, "Leather is rather weak stuff.", false)
        if (ask) {
            chatPlayer(quiz, "Can I buy some leather then?")
            chatNpc(
                neutral,
                "I make leather from animal hides. Bring me some cowhides and one gold coin per " +
                    "hide, and I'll tan them into soft leather for you.",
            )
        } else {
            chatPlayer(neutral, "Leather is rather weak stuff.")
            chatNpc(
                happy,
                "Normal leather may be quite weak, but it's very cheap - I make it from cowhides " +
                    "for only 1 gp per hide - and it's so easy to craft that anyone can work " +
                    "with it.",
            )
            chatNpc(
                happy,
                "Alternatively you could try hard leather. It's not so easy to craft, but I only " +
                    "charge 3 gp per hide to prepare it, and it makes much sturdier armour.",
            )
            chatNpc(
                happy,
                "I can also tan snake hides and dragonhides, suitable for crafting into the " +
                    "highest quality armour for rangers.",
            )
        }
    }
}
