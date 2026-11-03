package org.rsmod.content.areas.misc.wilderness.npcs

import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.varps
import org.rsmod.api.death.PlayerDeath
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpcU
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.content.areas.misc.wilderness.configs.wilderness_npcs
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Player
import org.rsmod.game.type.obj.UnpackedObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Ferox, the Enclave's founder, who moves a player's respawn point there for a one-time 5,000,000
 * coins, and the mercenary beside its bank, who un-notes banknotes and swaps coins for platinum
 * tokens. Their lines are the wiki's transcripts, bar the gendered words (the wiki says man or
 * woman).
 */
class FeroxEnclave : PluginScript() {
    private var Player.respawn by intVarp(varps.ferox_respawn)

    override fun ScriptContext.startup() {
        onOpNpc1(wilderness_npcs.ferox) { startDialogue(it.npc) { ferox() } }
        onOpNpcU(wilderness_npcs.mercenary) { exchange(it.npc, it.invSlot, it.objType) }
    }

    private suspend fun Dialogue.ferox() {
        chatNpc(neutral, "Welcome wanderer.")
        val paid = player.respawn != 0
        val choice =
            choice3(
                "What is this place?",
                1,
                if (paid) "Ask about your respawn point." else "What can you do for me?",
                2,
                "Nevermind.",
                3,
            )
        when {
            choice == 1 -> whatIsThisPlace(offer = !paid)
            choice == 2 && paid -> respawnSettings()
            choice == 2 -> offerRespawn()
            else -> {
                chatPlayer(neutral, "Never mind, I'm just looking around.")
                chatNpc(neutral, "So be it.")
            }
        }
    }

    private suspend fun Dialogue.whatIsThisPlace(offer: Boolean) {
        chatPlayer(quiz, "What is this place?")
        chatNpc(neutral, "You're surrounded by a relic of a long lost kingdom.")
        chatPlayer(quiz, "What happened here?")
        chatNpc(neutral, "Oh, I couldn't really tell you that.")
        chatNpc(
            neutral,
            "This place has sat in ruin for an age, slowly destroyed by the harsh climate of the " +
                "Wilderness.",
        )
        chatPlayer(quiz, "If it's a ruin, what's the point in being here?")
        chatNpc(
            neutral,
            "I search for knowledge. I want to know what happened to this part of the world.",
        )
        chatNpc(
            neutral,
            "Whilst looking for answers, I slowly turned this broken town into a safe haven for " +
                "travellers. Welcome to Ferox Enclave.",
        )
        chatPlayer(quiz, "Quite a dangerous place for a town, don't you think?")
        chatNpc(
            neutral,
            "It is, but that's why it's needed! Look around, you can see how many others share my " +
                "opinion.",
        )
        chatNpc(
            neutral,
            "You'll find yourself quite safe inside my enclave - the protective barriers should " +
                "keep any troublemakers out.",
        )
        chatNpc(
            neutral,
            "I will warn you though, if you've had Teleblock cast upon you, I will not allow you " +
                "back into the Enclave. You'd be a danger to the others gathered here.",
        )
        if (offer && choice2("What can you do for me?", true, "I'll be heading off then.", false)) {
            offerRespawn()
        } else if (offer) {
            chatPlayer(happy, "Thanks for the information! I'll take a look around.")
            chatNpc(neutral, "Tread carefully my friend.")
        }
    }

    private suspend fun Dialogue.offerRespawn() {
        chatPlayer(quiz, "What can you do for me?")
        chatNpc(
            neutral,
            "Well, if you'd like, I can make it so you respawn in our Enclave. It'll put you " +
                "right in the heart of all the chaos that surrounds us.",
        )
        if (!choice2("Tell me more.", true, "That doesn't interest me.", false)) {
            chatPlayer(neutral, "That doesn't interest me.")
            chatNpc(neutral, "Come find me if you change your mind.")
            return
        }
        chatPlayer(quiz, "Tell me more.")
        chatNpc(
            neutral,
            "This is how it'll work: When you die, you'll respawn right here in Ferox Enclave, " +
                "just outside the pub we've set up. You will lose your items as normal of course.",
        )
        chatNpc(
            neutral,
            "I won't be able to do anything if you die somewhere like Castle Wars or the Emir's " +
                "Arena; they have their own magic to keep you safe.",
        )
        chatNpc(
            neutral,
            "It'll cost you though! Transporting you here is no easy task. 5 million coins " +
                "should do it, that'd be a one time fee of course.",
        )
        if (access.invCoinTotal() < RESPAWN_FEE) {
            chatPlayer(sad, "I don't have that much money on me.")
            chatNpc(
                neutral,
                "Understandable, you'd have to be a brave soul to be carrying that much gold out " +
                    "here.",
            )
            return
        }
        if (
            !choice2(
                "Okay, switch my respawn to Ferox Enclave.",
                true,
                "I'm not interested.",
                false,
            )
        ) {
            chatPlayer(neutral, "I'm not interested.")
            chatNpc(neutral, "Can't say I blame you, this wouldn't be my choice either.")
            return
        }
        chatPlayer(neutral, "Okay, switch my respawn to Ferox Enclave.")
        if (access.invTakeFee(RESPAWN_FEE)) {
            player.respawn = PlayerDeath.FEROX_RESPAWN_SET
            chatNpc(happy, "Thank you, you'll respawn in the Enclave from here on out.")
        }
    }

    private suspend fun Dialogue.respawnSettings() {
        chatPlayer(quiz, "Can I talk to you about my respawn location?")
        if (player.respawn != PlayerDeath.FEROX_RESPAWN_SET) {
            chatNpc(neutral, "Would you like to respawn back in Ferox Enclave again?")
            val back =
                choice2(
                    "Please switch my respawn back to the Enclave.",
                    true,
                    "No, I don't want to respawn in the Enclave.",
                    false,
                )
            if (!back) {
                chatPlayer(neutral, "No, I don't want to respawn in the Enclave.")
                chatNpc(neutral, "Understandable.")
                return
            }
            chatPlayer(neutral, "Please switch my respawn back to the Enclave.")
            chatNpc(
                neutral,
                "The Wilderness takes a toll on a person's soul. Are you sure you can handle it?",
            )
            if (
                choice2(
                    "Yes, switch my respawn to the Enclave.",
                    true,
                    "No, maybe another time.",
                    false,
                )
            ) {
                chatPlayer(neutral, "Yes, switch my respawn to the Enclave.")
                player.respawn = PlayerDeath.FEROX_RESPAWN_SET
                chatNpc(neutral, "Fair enough, it has been done.")
            } else {
                chatPlayer(neutral, "No, maybe another time.")
                chatNpc(neutral, "Understandable.")
            }
            return
        }
        chatNpc(neutral, "How are you finding our sanctuary?")
        if (
            choice2(
                "Please switch my respawn back to Lumbridge.",
                true,
                "It's working for me so far, thanks.",
                false,
            )
        ) {
            chatPlayer(neutral, "Please switch my respawn back to Lumbridge.")
            chatNpc(
                neutral,
                "Can't say I blame you, the Wilderness starts to wear down the soul after a while.",
            )
            chatNpc(
                neutral,
                "But are you sure? Come and see me if you want to respawn in our Enclave again; I " +
                    "won't need any more money from you.",
            )
            if (
                choice2(
                    "Yes, switch my respawn to Lumbridge.",
                    true,
                    "No, I'll keep the Enclave respawn.",
                    false,
                )
            ) {
                chatPlayer(neutral, "Yes, switch my respawn to Lumbridge.")
                player.respawn = PlayerDeath.FEROX_RESPAWN_PAID
                chatNpc(neutral, "Done.")
            } else {
                chatPlayer(neutral, "No, I'll keep the Enclave respawn.")
                chatNpc(neutral, "Fine.")
            }
        } else {
            chatPlayer(neutral, "It's working for me so far, thanks.")
        }
    }

    /** What the mercenary does with something used on him. */
    private suspend fun ProtectedAccess.exchange(npc: Npc, slot: Int, type: UnpackedObjType) {
        when {
            type.isCert -> unnote(npc, slot, type)
            type.id == objs.coins.id -> coinsToTokens(npc)
            type.id == MercenaryObjs.platinum.id -> tokensToCoins(npc)
            else ->
                startDialogue(npc) {
                    chatNpcNoTurn(sad, "Hand me a banknote, and I'll try to convert it to an item.")
                }
        }
    }

    // Note: the wiki gives no fee for un-noting here, so it is free like at a banker.
    private suspend fun ProtectedAccess.unnote(npc: Npc, slot: Int, type: UnpackedObjType) {
        startDialogue(npc) {
            if (!choice2("Yes", true, "No", false, "Un-note the banknote?")) {
                return@startDialogue
            }
            val held = inv[slot] ?: return@startDialogue
            val count = minOf(inv.freeSpace(), held.count)
            if (count == 0) {
                chatNpcNoTurn(sad, "You don't have any inventory space.")
                return@startDialogue
            }
            val item = ocUncert(type)
            if (invReplace(inv, slot, count, item).success) {
                objbox(item, 400, "The bank exchanges your banknote for an item.")
            }
        }
    }

    /** Every 1,000 coins carried become a platinum token. */
    private suspend fun ProtectedAccess.coinsToTokens(npc: Npc) {
        val tokens = invCoinTotal() / TOKEN_VALUE
        if (tokens == 0) {
            return
        }
        startDialogue(npc) {
            if (!choice2("Yes", true, "No", false, "Exchange your coins for platinum tokens?")) {
                return@startDialogue
            }
            // Coins left over keep their slot, so the tokens may have nowhere to go.
            val slotFreed = invCoinTotal() == tokens * TOKEN_VALUE
            if (!slotFreed && inv.freeSpace() == 0 && MercenaryObjs.platinum !in inv) {
                chatNpcNoTurn(sad, "You don't have any inventory space.")
                return@startDialogue
            }
            if (invDel(inv, objs.coins, tokens * TOKEN_VALUE).success) {
                invAdd(inv, MercenaryObjs.platinum, tokens)
                objbox(
                    MercenaryObjs.platinum,
                    400,
                    "The bank exchanges your coins for platinum tokens.",
                )
            }
        }
    }

    /** Every platinum token carried becomes 1,000 coins, if the player can hold that many. */
    private suspend fun ProtectedAccess.tokensToCoins(npc: Npc) {
        val tokens = invTotal(inv, MercenaryObjs.platinum)
        if (
            tokens == 0 || invCoinTotal().toLong() + tokens.toLong() * TOKEN_VALUE > Int.MAX_VALUE
        ) {
            return
        }
        startDialogue(npc) {
            if (!choice2("Yes", true, "No", false, "Exchange your platinum tokens for coins?")) {
                return@startDialogue
            }
            if (invDel(inv, MercenaryObjs.platinum, tokens).success) {
                invAdd(inv, objs.coins, tokens * TOKEN_VALUE)
                objbox(objs.coins, 400, "The bank exchanges your platinum tokens for coins.")
            }
        }
    }

    private companion object {
        const val RESPAWN_FEE = 5_000_000
        const val TOKEN_VALUE = 1_000
    }
}

object MercenaryObjs : ObjReferences() {
    /** The platinum token. */
    val platinum = find("platinum")
}
