package org.rsmod.content.areas.misc.wilderness.npcs

import org.rsmod.api.config.refs.objs
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpcU
import org.rsmod.content.areas.misc.wilderness.configs.wilderness_locs
import org.rsmod.content.areas.misc.wilderness.configs.wilderness_npcs
import org.rsmod.content.areas.misc.wilderness.configs.wilderness_varps
import org.rsmod.game.entity.Npc
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.type.obj.UnpackedObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The Resource Area's guardian and gate: 7,500 coins buys a pass until the player logs out. The
 * diary discounts (and Elite's free entry) are out of scope with the Wilderness Diary. Piles,
 * inside it, turns items into banknotes at 50 coins each.
 */
class ResourceArea : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(wilderness_npcs.mandrith) { startDialogue(it.npc) { mandrith() } }
        onOpLoc1(wilderness_locs.resource_gate) { gate(it.loc) }
        onOpNpc1(wilderness_npcs.piles) { startDialogue(it.npc) { piles() } }
        onOpNpcU(wilderness_npcs.piles) { note(it.npc, it.objType) }
    }

    // Note: the guardian's wording is not on the wiki.
    private suspend fun Dialogue.mandrith() {
        if (access.vars[wilderness_varps.resource_area_paid] != 0) {
            chatNpc(neutral, "You have already paid. Go on in.")
            return
        }
        chatNpc(neutral, "It costs $FEE coins to enter the Resource Area. Will you pay?")
        val choice = choice2("Yes, I'll pay.", 1, "No thanks.", 2)
        if (choice != 1) {
            chatPlayer(neutral, "No thanks.")
            return
        }
        if (!access.invTakeFee(FEE)) {
            chatNpc(neutral, "You don't have enough coins.")
            return
        }
        access.vars[wilderness_varps.resource_area_paid] = 1
        chatNpc(happy, "Thank you. The gate is open to you.")
    }

    // Lines are the wiki's transcript.
    private suspend fun Dialogue.piles() {
        chatNpc(
            neutral,
            "Hello. I can convert items to banknotes, for $NOTE_FEE coins per item. Just hand me " +
                "the items you'd like me to convert.",
        )
        when (choice2("Who are you?", 1, "Thanks.", 2)) {
            1 -> {
                chatPlayer(quiz, "Who are you?")
                chatNpc(
                    neutral,
                    "I'm Piles. I lived in Draynor Village when I was young, where I saw three men " +
                        "working in the market, converting items to banknotes.",
                )
                chatNpc(
                    neutral,
                    "Their names were Niles, Miles and Giles. I'm trying to be like them, so I've " +
                        "changed my name and started this business here.",
                )
                chatPlayer(neutral, "Thanks.")
            }
            else -> chatPlayer(neutral, "Thanks.")
        }
    }

    /** Notes every unit of [type] the player carries that they can pay for. */
    // Note: Piles's replies to what he can't note or a player who can't pay are unverified.
    private suspend fun ProtectedAccess.note(npc: Npc, type: UnpackedObjType) {
        if (!type.canCert) {
            startDialogue(npc) { chatNpc(neutral, "I can't convert that into a banknote.") }
            return
        }
        val count = minOf(invTotal(inv, type), invCoinTotal() / NOTE_FEE)
        if (count == 0) {
            startDialogue(npc) {
                chatNpc(sad, "You need $NOTE_FEE coins for each item you want me to convert.")
            }
            return
        }
        invDel(inv, objs.coins, count * NOTE_FEE)
        invDel(inv, type, count)
        invAdd(inv, ocCert(type), count)
    }

    /** The gate is a wall on the north edge of its tile; the Resource Area is to the south. */
    private fun ProtectedAccess.gate(gate: BoundLocInfo) {
        val inside = coords.z <= gate.coords.z
        if (!inside && vars[wilderness_varps.resource_area_paid] == 0) {
            mes("You need to pay the guardian before you can enter.")
            return
        }
        telejump(if (inside) gate.coords.translateZ(1) else gate.coords)
    }

    private companion object {
        const val FEE = 7_500
        const val NOTE_FEE = 50
    }
}
