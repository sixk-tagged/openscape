package org.rsmod.content.areas.city.edgeville.npcs

import jakarta.inject.Inject
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.stat.statBase
import org.rsmod.api.script.onApNpc1
import org.rsmod.api.script.onApNpc3
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.shops.Shops
import org.rsmod.content.areas.city.edgeville.configs.edgeville_invs
import org.rsmod.content.areas.city.edgeville.configs.edgeville_npcs
import org.rsmod.content.areas.city.edgeville.configs.edgeville_objs
import org.rsmod.game.entity.Npc
import org.rsmod.game.type.inv.InvType
import org.rsmod.game.type.npc.NpcType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Edgeville's shopkeepers: Oziach (the Dragon Slayer quest counts as done, so he sells and attaches
 * visages), Peksa, the general store and Richard. Stock is in
 * [org.rsmod.content.areas.city.edgeville.configs.EdgevilleInvEditor].
 */
class EdgevilleShops @Inject constructor(private val shops: Shops) : PluginScript() {
    override fun ScriptContext.startup() {
        shop(edgeville_npcs.general_keeper, GENERAL, counter = true) { generalStore(it) }
        shop(edgeville_npcs.general_assistant, GENERAL, counter = true) { generalStore(it) }
        shop(edgeville_npcs.peksa, PEKSA, counter = true) { peksa(it) }
        shop(edgeville_npcs.richard, RICHARD) { richard(it) }
        shop(edgeville_npcs.oziach, OZIACH) { oziach(it) }
    }

    private class ShopDef(val title: String, val inv: InvType)

    private fun Dialogue.open(npc: Npc, shop: ShopDef) =
        shops.open(player, npc, shop.title, shop.inv)

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

    private suspend fun Dialogue.generalStore(npc: Npc) {
        chatNpc(happy, "Can I help you at all?")
        val choice = choice2("Yes please. What are you selling?", 1, "No thanks.", 2)
        if (choice == 1) {
            open(npc, GENERAL)
            return
        }
        chatPlayer(neutral, "No thanks.")
    }

    // Note: wording unverified.
    private suspend fun Dialogue.peksa(npc: Npc) {
        chatNpc(quiz, "Are you interested in buying or selling a helmet?")
        val choice = choice2("I could be, yes.", 1, "No, I'll pass on that.", 2)
        if (choice == 1) {
            chatPlayer(happy, "I could be, yes.")
            open(npc, PEKSA)
            return
        }
        chatPlayer(neutral, "No, I'll pass on that.")
    }

    // Note: wording unverified.
    private suspend fun Dialogue.richard(npc: Npc) {
        chatNpc(happy, "Hello there! Would you like to buy a team cape?")
        val choice = choice2("Yes please. What are you selling?", 1, "No thanks.", 2)
        if (choice == 1) {
            open(npc, RICHARD)
            return
        }
        chatPlayer(neutral, "No thanks.")
    }

    private suspend fun Dialogue.oziach(npc: Npc) {
        chatNpc(neutral, "Aye, 'tis a fair day, my mighty dragon-slaying friend.")
        val combine = visageToAttach()
        val extra =
            when {
                combine != null -> "Look at this visage."
                edgeville_objs.anti_dragon_shield in access.inv ->
                    "What's your idea about anti-dragon shields?"
                else -> null
            }
        val choice =
            if (extra == null) {
                choice4(
                    "Can I buy a rune platebody now, please?",
                    1,
                    "I'm not your friend.",
                    2,
                    "Yes, it's a very nice day.",
                    3,
                    "Can I have another key to Melzar's Maze?",
                    4,
                )
            } else {
                choice5(
                    "Can I buy a rune platebody now, please?",
                    1,
                    "I'm not your friend.",
                    2,
                    "Yes, it's a very nice day.",
                    3,
                    "Can I have another key to Melzar's Maze?",
                    4,
                    extra,
                    5,
                )
            }
        when (choice) {
            1 -> {
                chatPlayer(quiz, "Can I buy a rune platebody now, please?")
                open(npc, OZIACH)
            }
            2 -> {
                chatPlayer(angry, "I'm not your friend.")
                chatNpc(
                    angry,
                    "I'm surprised if you're anyone's friend with those kind of manners.",
                )
            }
            3 -> {
                chatPlayer(happy, "Yes, it's a very nice day.")
                chatNpc(neutral, "Aye, may the gods walk by yer side. Now leave me alone.")
            }
            4 -> {
                chatPlayer(quiz, "Can I have another key to Melzar's Maze?")
                chatNpc(
                    neutral,
                    "It's the Guildmaster in the Champions' Guild who hands those keys out now. " +
                        "Go talk to him. No need to bother me if you don't need armour.",
                )
            }
            5 ->
                if (combine != null) {
                    attachVisage(combine)
                } else {
                    chatPlayer(quiz, "What's your idea about anti-dragon shields?")
                    // Note: wording unverified.
                    chatNpc(
                        neutral,
                        "Bring me the visage of a dragon and an anti-dragon shield and I'll " +
                            "fix them together, for a fee of 1,250,000 coins.",
                    )
                }
        }
    }

    /** A visage in the inventory next to an anti-dragon shield, and what they combine into. */
    private fun Dialogue.visageToAttach(): Pair<ObjType, ObjType>? {
        val inv = access.inv
        if (edgeville_objs.anti_dragon_shield !in inv) {
            return null
        }
        return when {
            edgeville_objs.draconic_visage in inv ->
                edgeville_objs.draconic_visage to edgeville_objs.dragonfire_shield
            edgeville_objs.skeletal_visage in inv ->
                edgeville_objs.skeletal_visage to edgeville_objs.dragonfire_ward
            else -> null
        }
    }

    private suspend fun Dialogue.attachVisage(combine: Pair<ObjType, ObjType>) {
        chatPlayer(happy, "Look at this visage.")
        chatNpc(
            neutral,
            "Ah, I can attach that to your anti-dragon shield for 1,250,000 coins. What do ye " +
                "say?",
        )
        val choice = choice2("Yes please.", 1, "That's a bit expensive!", 2)
        if (choice == 2) {
            chatPlayer(sad, "That's a bit expensive!")
            if (player.statBase(stats.smithing) >= SMITHING_SELF_SUFFICIENT) {
                chatNpc(neutral, "I suppose ye don't need me for this then.")
            } else {
                chatNpc(neutral, "That's my price. Take it or leave it.")
            }
            return
        }
        chatPlayer(happy, "Yes please.")
        if (!access.invTakeFee(VISAGE_PRICE)) {
            chatNpc(neutral, "Ye don't have enough coins for that.")
            return
        }
        access.invDel(access.inv, edgeville_objs.anti_dragon_shield)
        access.invDel(access.inv, combine.first)
        access.invAdd(access.inv, combine.second)
        mesbox("Oziach skillfully attaches the visage to your shield.")
    }

    private companion object {
        const val COUNTER_RANGE = 2
        const val VISAGE_PRICE = 1_250_000
        const val SMITHING_SELF_SUFFICIENT = 90

        val GENERAL = ShopDef("Edgeville General Store", edgeville_invs.general)
        val PEKSA = ShopDef("Peksa's Helmet Shop", edgeville_invs.peksa)
        val RICHARD = ShopDef("Richard's Wilderness Cape Shop", edgeville_invs.richard)
        val OZIACH = ShopDef("Oziach's Armour Shop", edgeville_invs.oziach)
    }
}
