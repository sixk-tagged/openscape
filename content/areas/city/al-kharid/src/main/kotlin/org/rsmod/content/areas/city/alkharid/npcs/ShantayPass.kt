package org.rsmod.content.areas.city.alkharid.npcs

import jakarta.inject.Inject
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLoc2
import org.rsmod.api.script.onOpLoc3
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.script.onOpNpc4
import org.rsmod.api.shops.Shops
import org.rsmod.content.areas.city.alkharid.configs.alkharid_invs
import org.rsmod.content.areas.city.alkharid.configs.alkharid_locs
import org.rsmod.content.areas.city.alkharid.configs.alkharid_npcs
import org.rsmod.content.areas.city.alkharid.configs.alkharid_objs
import org.rsmod.game.entity.Npc
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Shantay Pass: Shantay sells passes and his shop, the guard (or the doorway itself) takes one pass
 * per walk south into the desert. The way back north is free.
 */
class ShantayPass @Inject constructor(private val shops: Shops) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(alkharid_npcs.shantay) { startDialogue(it.npc) { shantay() } }
        onOpNpc3(alkharid_npcs.shantay) { openShop(it.npc) }
        onOpNpc4(alkharid_npcs.shantay) { startDialogue(it.npc) { buyPass() } }

        onOpNpc1(alkharid_npcs.shantay_guard_still) { startDialogue(it.npc) { gateGuard() } }
        onOpNpc1(alkharid_npcs.shantay_guard) {
            startDialogue(it.npc) {
                chatNpc(
                    angry,
                    "Go talk to Shantay. I'm on duty and I don't have time to talk to the likes " +
                        "of you!",
                )
            }
        }
        onOpNpc3(alkharid_npcs.shantay_guard_still) { passGate(DOORWAY.x) }
        // The doorway is 4 tiles wide: step straight through from the tile you stand on.
        onOpLoc1(alkharid_locs.pass_doorway) { passGate(coords.x) }
        onOpLoc2(alkharid_locs.pass_doorway) {
            mes("The stone gateway marks the start of the Kharidian desert.")
        }

        // The cell door opens both ways: the "outlaw" arrest joke is not built, so nobody is held.
        onOpLoc1(alkharid_locs.prison_door) {
            telejump(coords.translateX(if (coords.x > PRISON_DOOR_X) -1 else 1))
        }

        // Note: the bank chest's Collect op shows the same made-up message as booths do.
        onOpLoc3(alkharid_locs.bank_chest) { mes("You have nothing to collect.") }
        // Note: the Sorceress's shelves message is unverified.
        onOpLoc1(alkharid_locs.sorceress_shelves) {
            mes("You search the shelves but find nothing.")
        }
    }

    private suspend fun Dialogue.gateGuard() {
        chatNpc(happy, "Hello there! What can I do for you?")
        val choice = choice2("I'd like to go into the desert please.", 1, "Nothing thanks.", 2)
        if (choice == 2) {
            chatPlayer(neutral, "Nothing thanks.")
            chatNpc(happy, "Okay then, have a nice day.")
            return
        }
        chatPlayer(neutral, "I'd like to go into the desert please.")
        chatNpc(
            neutral,
            "You'll need a Shantay pass to go through the gate into the desert. See Shantay, " +
                "he'll sell you one for a very reasonable price.",
        )
    }

    private fun ProtectedAccess.openShop(npc: Npc) {
        shops.open(player, npc, "Shantay Pass Shop", alkharid_invs.shantay)
    }

    private suspend fun Dialogue.shantay() {
        chatNpc(happy, "Hello effendi, I am Shantay.")
        val choice =
            choice5(
                "What is this place?",
                1,
                "Can I see what you have to sell please?",
                2,
                "I want to buy a Shantay pass for 5 gold coins.",
                3,
                "Why do I have to pay to go into the desert?",
                4,
                "I must be going.",
                5,
            )
        when (choice) {
            1 -> {
                chatPlayer(quiz, "What is this place?")
                chatNpc(
                    neutral,
                    "This is the pass of Shantay, the only safe way into the Kharidian desert. " +
                        "Take plenty of waterskins with you, for the desert is hot and unforgiving.",
                )
                chatNpc(
                    neutral,
                    "A knife used on a cactus will give you a little water, and desert clothes " +
                        "will help you stay cool.",
                )
            }
            2 -> {
                chatPlayer(quiz, "Can I see what you have to sell please?")
                chatNpc(happy, "Certainly, effendi.")
                access.openShop(npc!!)
            }
            3 -> {
                chatPlayer(neutral, "I want to buy a Shantay pass for 5 gold coins.")
                buyPass()
            }
            4 -> {
                chatPlayer(quiz, "Why do I have to pay to go into the desert?")
                chatNpc(
                    neutral,
                    "To keep the pass bandit free, we charge a small toll of five gold pieces.",
                )
            }
            5 -> chatPlayer(neutral, "I must be going.")
        }
    }

    private suspend fun Dialogue.buyPass() {
        if (!player.members) {
            chatNpc(sad, "Sorry effendi, I can't offer this service right now.")
        } else if (access.invCoinTotal() < PASS_PRICE) {
            chatNpc(neutral, "You don't have enough coins, effendi.")
        } else if (access.invAdd(access.inv, alkharid_objs.shantay_pass).failure) {
            chatNpc(neutral, "You don't have room for a pass.")
        } else {
            access.invTakeFee(PASS_PRICE)
            access.mes("You purchase a Shantay Pass.")
        }
    }

    private suspend fun ProtectedAccess.passGate(x: Int) {
        arriveDelay()
        val south = coords.z > DOORWAY.z
        if (south) {
            if (invDel(inv, alkharid_objs.shantay_pass, 1).failure) {
                mes(
                    "You need a Shantay pass to go through the gate. Shantay sells them for 5 coins."
                )
                return
            }
            mes("The guard takes your Shantay pass and lets you through the gate.")
        }
        delay(1)
        telejump(CoordGrid(x, if (south) DOORWAY.z - 1 else DOORWAY.z + 1))
    }

    private companion object {
        const val PASS_PRICE = 5
        const val PRISON_DOOR_X = 3297

        val DOORWAY = CoordGrid(3302, 3116)
    }
}
