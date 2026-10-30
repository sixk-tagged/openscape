package org.rsmod.content.areas.city.portsarim.npcs

import org.rsmod.api.config.refs.objs
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.baseCraftingLvl
import org.rsmod.api.player.stat.baseSmithingLvl
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc4
import org.rsmod.api.script.onOpNpcU
import org.rsmod.content.areas.city.portsarim.configs.port_sarim_npcs
import org.rsmod.content.areas.city.portsarim.configs.port_sarim_objs
import org.rsmod.content.areas.city.portsarim.configs.port_sarim_varps
import org.rsmod.game.entity.Npc
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.UnpackedObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Phials (un-notes for 5 coins an item), Luthas's banana job, Thurgo's and the eldest Master
 * Crafter's skillcapes, the Crafting Guild's tanner, Makeover Mage and balloon.
 */
class PortSarimServices : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(port_sarim_npcs.phials) { startDialogue(it.npc) { phials() } }
        onOpNpcU(port_sarim_npcs.phials) { exchange(it.npc, it.invSlot, it.objType) }

        onOpNpc1(port_sarim_npcs.luthas) { startDialogue(it.npc) { luthas() } }
        onOpNpc1(port_sarim_npcs.thurgo) { startDialogue(it.npc) { thurgo() } }

        val eldest = port_sarim_npcs.master_crafters.first()
        onOpNpc1(eldest) { startDialogue(it.npc) { masterCrafter(sellsCape = true) } }
        for (crafter in port_sarim_npcs.master_crafters.drop(1)) {
            onOpNpc1(crafter) { startDialogue(it.npc) { masterCrafter(sellsCape = false) } }
        }

        // Note: the tanner's Trade is the tanning interface (Tanning); this only chats.
        onOpNpc1(port_sarim_npcs.tanner) {
            startDialogue(it.npc) {
                chatNpc(happy, "Hello! I can tan your hides for you, if you have the coins.")
            }
        }

        onOpNpc1(port_sarim_npcs.makeover_mage) { startDialogue(it.npc) { makeoverMage() } }
        // Note: like Thessalia's, the makeover interface is not built.
        onOpNpc4(port_sarim_npcs.makeover_mage) { mes("The makeover is not available yet.") }

        // Note: every balloon destination is off the map.
        onOpNpc1(port_sarim_npcs.brock) {
            startDialogue(it.npc) {
                chatNpc(
                    happy,
                    "Welcome to the Crafting Guild's balloon station. Bring me an oak log and " +
                        "I can fly you somewhere.",
                )
                chatNpc(sad, "Sadly, the balloon is being repaired at the moment.")
            }
        }
    }

    private suspend fun Dialogue.phials() {
        chatNpc(
            happy,
            "Hello. Do you wish me to exchange banknotes for you? I charge only 5 coins for " +
                "each banknote.",
        )
        val choice = choice3("Yes please.", 1, "Who are you?", 2, "No thanks.", 3)
        when (choice) {
            1 -> {
                chatPlayer(happy, "Yes please.")
                chatNpc(happy, "Hand me the banknotes you wish me to exchange.")
            }
            2 -> {
                chatPlayer(quiz, "Who are you?")
                chatNpc(
                    happy,
                    "I am Phials. I can exchange banknotes for real items. Do you want any " +
                        "banknotes converted?",
                )
            }
            3 -> chatPlayer(neutral, "No thanks.")
        }
    }

    // Note: Phials's replies to a poor or crowded player are unverified.
    private suspend fun ProtectedAccess.exchange(npc: Npc, slot: Int, type: UnpackedObjType) {
        if (!type.isCert) {
            startDialogue(npc) { chatNpc(neutral, "I can only exchange banknotes, I'm afraid.") }
            return
        }
        val stack = inv[slot] ?: return
        val count = minOf(stack.count, inv.freeSpace(), invTotal(inv, objs.coins) / NOTE_FEE)
        if (count == 0) {
            startDialogue(npc) {
                chatNpc(sad, "You need 5 coins and a free space for each item you want back.")
            }
            return
        }
        invDel(inv, objs.coins, count * NOTE_FEE)
        if (invReplace(inv, slot, count, ocUncert(type)).failure) {
            invAdd(inv, objs.coins, count * NOTE_FEE)
            return
        }
        mes("Phials exchanges your banknotes.")
    }

    private suspend fun Dialogue.luthas() {
        val job = vars[port_sarim_varps.banana_job]
        if (job == 0) {
            chatNpc(happy, "Hello I'm Luthas, I run the banana plantation here.")
        } else {
            chatNpc(quiz, "Have you completed your task yet?")
        }
        val options = mutableListOf<Pair<String, Int>>()
        if (job == 0) {
            options += "Could you offer me employment on your plantation?" to EMPLOYMENT
        } else {
            options += "I've filled a crate with bananas." to FILLED
            options += "No, the crate isn't full yet..." to NOT_FULL
        }
        options += "That customs officer is annoying isn't she?" to CUSTOMS
        options += "So where are these bananas going to be delivered to?" to DELIVERY
        val choice =
            if (options.size == 3) {
                choice3(
                    options[0].first,
                    options[0].second,
                    options[1].first,
                    options[1].second,
                    options[2].first,
                    options[2].second,
                )
            } else {
                choice4(
                    options[0].first,
                    options[0].second,
                    options[1].first,
                    options[1].second,
                    options[2].first,
                    options[2].second,
                    options[3].first,
                    options[3].second,
                )
            }
        when (choice) {
            EMPLOYMENT -> {
                chatPlayer(quiz, "Could you offer me employment on your plantation?")
                chatNpc(
                    happy,
                    "Yes, I can sort something out. There's a crate ready to be loaded onto the " +
                        "ship.",
                )
                chatNpc(neutral, "If you could fill it up with bananas, I'll pay you 30 gold.")
                vars[port_sarim_varps.banana_job] = 1
            }
            FILLED -> {
                chatPlayer(happy, "I've filled a crate with bananas.")
                if (vars[port_sarim_varps.banana_crate] < CRATE_SIZE) {
                    chatNpc(neutral, "Well come back when it is.")
                } else if (!access.invAdd(access.inv, objs.coins, PAYMENT).success) {
                    access.mes("You don't have enough inventory space.")
                } else {
                    vars[port_sarim_varps.banana_crate] = 0
                    chatNpc(happy, "Well done, here's your payment.")
                    chatPlayer(quiz, "Will you pay me for another crate full?")
                    chatNpc(happy, "Yes certainly.")
                    chatNpc(
                        neutral,
                        "If you go outside you should see the old crate has been loaded on to " +
                            "the ship, and there is another empty crate in its place.",
                    )
                }
            }
            NOT_FULL -> {
                chatPlayer(neutral, "No, the crate isn't full yet...")
                chatNpc(neutral, "Well come back when it is.")
            }
            CUSTOMS -> {
                chatPlayer(quiz, "That customs officer is annoying isn't she?")
                chatNpc(
                    happy,
                    "Well, I can't say I mind her. She has stopped searching my export crates, " +
                        "at least.",
                )
            }
            DELIVERY -> {
                chatPlayer(quiz, "So where are these bananas going to be delivered to?")
                chatNpc(neutral, "Wydin, who runs a grocery store in Port Sarim.")
            }
        }
    }

    private suspend fun Dialogue.thurgo() {
        chatNpc(neutral, "Hello there. What can I do for you?")
        val choice = choice2("Can I buy a Skillcape of Smithing?", 1, "Nothing, thanks.", 2)
        if (choice == 2) {
            chatPlayer(neutral, "Nothing, thanks.")
            return
        }
        chatPlayer(quiz, "Can I buy a Skillcape of Smithing?")
        if (player.baseSmithingLvl < CAPE_LEVEL) {
            chatNpc(
                neutral,
                "I only sell the cape to those who have mastered Smithing. Come back at level 99.",
            )
            return
        }
        chatNpc(happy, "Certainly. That will be 99,000 coins.")
        buyCape(port_sarim_objs.smithing_cape, port_sarim_objs.smithing_hood)
    }

    private suspend fun Dialogue.masterCrafter(sellsCape: Boolean) {
        chatNpc(
            happy,
            "Welcome to the Crafting Guild! Only the finest crafters may use our facilities.",
        )
        if (!sellsCape) {
            return
        }
        val choice = choice2("Can I buy a Skillcape of Crafting?", 1, "Just looking, thanks.", 2)
        if (choice == 2) {
            chatPlayer(neutral, "Just looking, thanks.")
            return
        }
        chatPlayer(quiz, "Can I buy a Skillcape of Crafting?")
        if (player.baseCraftingLvl < CAPE_LEVEL) {
            chatNpc(
                neutral,
                "I only sell the cape to those who have mastered Crafting. Come back at level " +
                    "99.",
            )
            return
        }
        chatNpc(happy, "Of course. That will be 99,000 coins.")
        buyCape(port_sarim_objs.crafting_cape, port_sarim_objs.crafting_hood)
    }

    // Note: the trimmed cape for a second 99 is skipped.
    private suspend fun Dialogue.buyCape(cape: ObjType, hood: ObjType) {
        if (access.inv.freeSpace() < 2) {
            chatNpc(neutral, "You'll need two free inventory spaces to carry the cape and hood.")
        } else if (!access.invTakeFee(CAPE_PRICE)) {
            chatPlayer(sad, "I'm afraid I haven't got that many coins.")
        } else {
            access.invAdd(access.inv, cape)
            access.invAdd(access.inv, hood)
            access.mes("You hand over 99,000 coins and receive a cape and hood.")
        }
    }

    // Note: wording unverified; the free makeover itself is not built.
    private suspend fun Dialogue.makeoverMage() {
        chatNpc(
            happy,
            "Hello there! I can change how you look for free. I also sell a Yin yang amulet for " +
                "100 coins.",
        )
        val choice = choice2("I'll take the amulet.", 1, "No thanks.", 2)
        if (choice == 2) {
            chatPlayer(neutral, "No thanks.")
            return
        }
        chatPlayer(happy, "I'll take the amulet.")
        if (access.inv.isFull()) {
            chatNpc(neutral, "You don't have room for it.")
        } else if (!access.invTakeFee(AMULET_PRICE)) {
            chatPlayer(sad, "I'm afraid I haven't got 100 coins.")
        } else {
            access.invAdd(access.inv, port_sarim_objs.yin_yang_amulet)
            access.mes("You hand over 100 coins and receive an amulet.")
        }
    }

    private companion object {
        const val NOTE_FEE = 5
        const val CRATE_SIZE = 10
        const val PAYMENT = 30
        const val CAPE_LEVEL = 99
        const val CAPE_PRICE = 99_000
        const val AMULET_PRICE = 100

        const val EMPLOYMENT = 1
        const val FILLED = 2
        const val NOT_FULL = 3
        const val CUSTOMS = 4
        const val DELIVERY = 5
    }
}
