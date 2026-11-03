package org.rsmod.content.areas.misc.wilderness.npcs

import org.rsmod.api.config.refs.objs
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpcU
import org.rsmod.content.areas.misc.wilderness.configs.wilderness_npcs
import org.rsmod.content.areas.misc.wilderness.locs.ChaosAltarBones
import org.rsmod.game.entity.Npc
import org.rsmod.game.type.obj.UnpackedObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The Elder Chaos druid outside the Chaos Temple's hut: he un-notes banknotes of any bone the altar
 * takes for 50 coins each. His lines are the wiki's transcript.
 */
class ChaosDruid : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(wilderness_npcs.bone_druid) { startDialogue(it.npc) { talk() } }
        onOpNpcU(wilderness_npcs.bone_druid) { exchange(it.npc, it.invSlot, it.objType) }
    }

    private suspend fun Dialogue.talk() {
        chatNpc(
            neutral,
            "Hello, and welcome to The Dark Lord's temple. Zamorak will reward those who offer " +
                "the bones of the vanquished on his altar.",
        )
        chatNpc(neutral, "I can exchange banknotes for bones, should you require such a service.")
        chatNpc(neutral, "My services, however, are not free. I charge $FEE coins per banknote.")
        when (choice3("Yes please.", 1, "Who are you?", 2, "No thanks.", 3)) {
            1 -> handOver()
            2 -> {
                chatPlayer(quiz, "Who are you?")
                chatNpc(
                    neutral,
                    "That is not important. I am here to facilitate your offerings to The Dark " +
                        "Lord. Would you like any bone banknotes exchanged?",
                )
                if (choice2("Yes please.", true, "No thanks.", false)) {
                    handOver()
                } else {
                    chatPlayer(neutral, "No thanks.")
                }
            }
            else -> chatPlayer(neutral, "No thanks.")
        }
    }

    private suspend fun Dialogue.handOver() {
        chatPlayer(neutral, "Yes please.")
        chatNpc(neutral, "Hand me the banknotes you wish me to exchange.")
    }

    private suspend fun ProtectedAccess.exchange(npc: Npc, slot: Int, type: UnpackedObjType) {
        if (!type.isCert) {
            return say(npc, "I exchange banknotes for real items. That's not a banknote.")
        }
        val bone = ocUncert(type)
        if (ChaosAltarBones.all.none { it.id == bone.id }) {
            return say(
                npc,
                "I only exchange items that can be offered as a sacrifice to The Dark Lord's altar.",
            )
        }
        if (invCoinTotal() < FEE) {
            return say(npc, "I charge $FEE coins for exchanging each banknote.")
        }
        val stack = inv[slot] ?: return
        val count = minOf(stack.count, inv.freeSpace(), invCoinTotal() / FEE)
        if (count == 0) {
            return say(npc, "Your inventory is too full.")
        }
        invDel(inv, objs.coins, count * FEE)
        if (invReplace(inv, slot, count, bone).failure) {
            invAdd(inv, objs.coins, count * FEE)
        }
    }

    private suspend fun ProtectedAccess.say(npc: Npc, text: String) {
        startDialogue(npc) { chatNpc(neutral, text) }
    }

    private companion object {
        const val FEE = 50
    }
}
