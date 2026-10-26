package org.rsmod.content.skills.runecraft.scripts

import jakarta.inject.Inject
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.baseRunecraftingLvl
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.shops.Shops
import org.rsmod.content.skills.runecraft.configs.RunecraftInvs
import org.rsmod.content.skills.runecraft.configs.RunecraftNpcs
import org.rsmod.game.entity.Npc
import org.rsmod.game.type.stat.StatTypeList
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Talk-to and Trade for Aubury, and Talk-to for Archmage Sedridor. Their `Teleport` option is in
 * [EssenceMine]. The dialogue is short: only the lines the wiki and the reference confirm.
 */
class EssenceNpcs
@Inject
constructor(private val shops: Shops, private val statTypes: StatTypeList) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(RunecraftNpcs.aubury) { startDialogue(it.npc) { aubury(it.npc) } }
        onOpNpc3(RunecraftNpcs.aubury) { openShop() }
        onOpNpc1(RunecraftNpcs.archmage_sedridor) { startDialogue(it.npc) { sedridor(it.npc) } }
    }

    private suspend fun Dialogue.aubury(npc: Npc) {
        chatNpc(quiz, "Do you want to buy some runes?")
        val choice =
            choice3(
                "Yes please!",
                1,
                "Oh, it's a rune shop. No thank you, then.",
                2,
                "Can you teleport me to the Rune Essence?",
                3,
            )
        when (choice) {
            1 -> {
                chatPlayer(happy, "Yes please!")
                access.openShop()
            }
            2 -> {
                chatPlayer(neutral, "Oh, it's a rune shop. No thank you, then.")
                chatNpc(
                    neutral,
                    "Well, if you find someone who does want runes, please send them my way.",
                )
            }
            3 -> {
                chatPlayer(quiz, "Can you teleport me to the Rune Essence?")
                chatNpc(
                    happy,
                    "Of course. By the way, if you end up making any runes from the essence " +
                        "you mine, I'll happily buy them from you.",
                )
                access.sendToEssenceMine(npc, EssenceOrigin.Aubury)
            }
        }
    }

    private suspend fun Dialogue.sedridor(npc: Npc) {
        chatNpc(
            happy,
            "Welcome adventurer, to the world renowned Wizards' Tower. How may I help you?",
        )
        val choice =
            choice2(
                "Can you teleport me to the Rune Essence Mine?",
                1,
                "Nothing thanks, I'm just looking around.",
                2,
            )
        if (choice == 1) {
            chatPlayer(quiz, "Can you teleport me to the Rune Essence Mine?")
            access.sendToEssenceMine(npc, EssenceOrigin.Sedridor)
        } else {
            chatPlayer(neutral, "Nothing thanks, I'm just looking around.")
        }
    }

    /**
     * Sells at 100%, buys at 60%, price change 0.1% per item. At 99 Runecraft the shop copy with
     * the Runecraft cape is used, or the trimmed cape's copy once another skill is 99 too.
     */
    private fun ProtectedAccess.openShop() {
        val shop =
            when {
                player.baseRunecraftingLvl < MAX_LEVEL -> RunecraftInvs.rune_shop
                hasAnotherMaxedSkill() -> RunecraftInvs.rune_shop_cape_trimmed
                else -> RunecraftInvs.rune_shop_cape
            }
        shops.open(
            player,
            "Aubury's Rune Shop",
            shop,
            buyPercentage = 60.0,
            sellPercentage = 100.0,
            changePercentage = 0.1,
        )
    }

    private fun ProtectedAccess.hasAnotherMaxedSkill(): Boolean =
        statTypes.values.any { it.id != stats.runecrafting.id && statBase(it) >= MAX_LEVEL }

    private companion object {
        const val MAX_LEVEL = 99
    }
}
