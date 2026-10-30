package org.rsmod.content.areas.city.falador.npcs

import jakarta.inject.Inject
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.statBase
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.script.onOpNpc4
import org.rsmod.api.shops.Shops
import org.rsmod.content.areas.city.falador.configs.falador_currencies
import org.rsmod.content.areas.city.falador.configs.falador_invs
import org.rsmod.content.areas.city.falador.configs.falador_npcs
import org.rsmod.content.areas.city.falador.configs.falador_objs
import org.rsmod.content.areas.city.falador.configs.falador_varps
import org.rsmod.game.entity.Npc
import org.rsmod.game.type.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Falador's dialogue services: Sir Vyvin's and Sir Tiffy's armouries, the Rising Sun Inn's
 * barmaids, Wyson's woad leaves, Gadrin's Mining cape, Belona's expert gloves, the Mining Guild
 * dwarves and Malignius Mortifer's magic secateurs.
 */
class FaladorServices @Inject constructor(private val shops: Shops) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(falador_npcs.sir_vyvin) { startDialogue(it.npc) { sirVyvin(it.npc) } }
        onOpNpc1(falador_npcs.sir_tiffy) { startDialogue(it.npc) { sirTiffy(it.npc) } }
        for (barmaid in listOf(falador_npcs.emily, falador_npcs.kaylee, falador_npcs.tina)) {
            onOpNpc1(barmaid) { startDialogue(it.npc) { barmaid() } }
        }
        onOpNpc1(falador_npcs.wyson) { startDialogue(it.npc) { wyson() } }
        onOpNpc1(falador_npcs.gadrin) { startDialogue(it.npc) { gadrin() } }
        onOpNpc1(falador_npcs.belona) { startDialogue(it.npc) { belona(it.npc) } }
        onOpNpc3(falador_npcs.belona) { openMineralShop(it.npc) }
        onOpNpc4(falador_npcs.belona) { toggleMinerals() }
        for (dwarf in GUILD_DWARVES) {
            onOpNpc1(dwarf) { startDialogue(it.npc) { guildDwarf() } }
        }
        onOpNpc1(falador_npcs.malignius) { startDialogue(it.npc) { malignius() } }
    }

    // ---- Sir Vyvin and Sir Tiffy

    private suspend fun Dialogue.sirVyvin(npc: Npc) {
        // UNVERIFIED: his greeting; the rank lines are the wiki's.
        chatNpc(neutral, "Greetings traveller.")
        val choice = choice2("Do you have anything to trade?", 1, "Nothing, thanks.", 2)
        if (choice == 2) {
            chatPlayer(neutral, "Nothing, thanks.")
            return
        }
        chatPlayer(quiz, "Do you have anything to trade?")
        val rank = player.whiteKnightRank
        if (rank == null) {
            chatNpc(
                neutral,
                "I'm sorry, but you need to prove yourself worthy as a White Knight before I " +
                    "will sell you anything.",
            )
            return
        }
        shops.open(player, npc, "White Knight ${rank.label} Armoury", rank.shop)
    }

    // Note: only the armoury; his "switch respawns" option needs a per-player respawn
    // point, and the respawn is one realm-wide coord for now.
    private suspend fun Dialogue.sirTiffy(npc: Npc) {
        chatNpc(happy, "What ho, sirrah! What can I do for you?")
        val choice = choice2("Can I buy some armour?", 1, "Nothing, thanks.", 2)
        if (choice == 2) {
            chatPlayer(neutral, "Nothing, thanks.")
            return
        }
        chatPlayer(quiz, "Can I buy some armour?")
        shops.open(player, npc, "Proselyte Temple Knight Armoury", falador_invs.temple_knight)
    }

    // ---- The Rising Sun Inn

    private suspend fun Dialogue.barmaid() {
        chatNpc(happy, "Heya! What can I get you?")
        val glasses =
            access.invTotal(access.inv, falador_objs.beer_glass) +
                access.invTotal(access.inv, falador_objs.beer_glass_noted)
        val choice =
            if (glasses > 0) {
                choice3(
                    "What ales are you serving?",
                    1,
                    "I've got this beer glass...",
                    2,
                    "Nothing, thanks.",
                    3,
                )
            } else {
                choice2("What ales are you serving?", 1, "Nothing, thanks.", 3)
            }
        when (choice) {
            1 -> ales()
            2 -> sellGlasses(glasses)
            else -> chatPlayer(neutral, "Nothing, thanks.")
        }
    }

    private suspend fun Dialogue.ales() {
        chatPlayer(quiz, "What ales are you serving?")
        chatNpc(
            happy,
            "Well, we've got Asgarnian Ale, Wizard's Mind Bomb and Dwarven Stout, all for only " +
                "3 coins.",
        )
        val ale =
            choice4(
                "One Asgarnian Ale, please.",
                falador_objs.asgarnian_ale,
                "I'll try the Mind Bomb.",
                falador_objs.wizards_mind_bomb,
                "Can I have a Dwarven Stout?",
                falador_objs.dwarven_stout,
                "I don't feel like any of those.",
                null,
            )
        if (ale == null) {
            chatPlayer(neutral, "I don't feel like any of those.")
            return
        }
        buyAle(ale)
    }

    private suspend fun Dialogue.buyAle(ale: ObjType) {
        if (access.inv.isFull()) {
            chatNpc(neutral, "You don't have room to carry it!")
            return
        }
        if (!access.invTakeFee(ALE_PRICE)) {
            chatNpc(angry, "I said 3 coins! You haven't got 3 coins!")
            return
        }
        access.invAdd(access.inv, ale)
        chatNpc(happy, "There you go. Enjoy!")
    }

    private suspend fun Dialogue.sellGlasses(count: Int) {
        chatPlayer(neutral, "I've got this beer glass...")
        chatNpc(happy, "Ooh, we'll buy those off you if you're interested. 2 coins per glass.")
        val choice = choice2("Okay, sure.", 1, "No thanks, I like my empty beer glasses.", 2)
        if (choice == 2) {
            chatPlayer(neutral, "No thanks, I like my empty beer glasses.")
            return
        }
        // Noted glasses are bought too.
        for (glass in listOf(falador_objs.beer_glass, falador_objs.beer_glass_noted)) {
            val held = access.invTotal(access.inv, glass)
            if (held > 0 && access.invDel(access.inv, glass, held).failure) {
                return
            }
        }
        access.invAdd(access.inv, objs.coins, count * GLASS_PRICE)
        chatPlayer(happy, "Okay, sure.")
    }

    // ---- Wyson the gardener

    private suspend fun Dialogue.wyson() {
        chatNpc(
            neutral,
            "I'm the head gardener around here. If you're looking for woad leaves, or if you " +
                "need help with owt, I'm your man.",
        )
        val choice =
            choice2("Yes please, I need woad leaves.", 1, "Sorry, but I'm not interested.", 2)
        if (choice == 2) {
            chatPlayer(neutral, "Sorry, but I'm not interested.")
            return
        }
        chatPlayer(happy, "Yes please, I need woad leaves.")
        chatNpc(neutral, "How much are you willing to pay?")
        val offer =
            choice4(
                "How about 5 coins?",
                5,
                "How about 10 coins?",
                10,
                "How about 15 coins?",
                15,
                "How about 20 coins?",
                20,
            )
        chatPlayer(quiz, "How about $offer coins?")
        val leaves =
            when (offer) {
                WOAD_PRICE -> 1
                WOAD_PRICE_TWO -> 2
                else -> 0
            }
        if (leaves == 0) {
            chatNpc(
                sad,
                "No no, that's far too little. Woad leaves are hard to get. I used to have " +
                    "plenty but someone kept stealing them off me.",
            )
            return
        }
        if (access.invCoinTotal() < offer) {
            chatPlayer(sad, "I don't have enough coins to buy the leaves.")
            return
        }
        val hasStack = access.invTotal(access.inv, falador_objs.woad_leaf) > 0
        if (access.inv.freeSpace() < 1 && !hasStack) {
            access.mes("You don't have enough inventory space.")
            return
        }
        access.invTakeFee(offer)
        access.invAdd(access.inv, falador_objs.woad_leaf, leaves)
        chatNpc(happy, "Here you go. Hope they help.")
    }

    // ---- The Mining Guild

    // Note: the trimmed cape for a second 99 is skipped.
    private suspend fun Dialogue.gadrin() {
        chatNpc(happy, "Welcome to the Mining Guild. My name's Gadrin and I'm the Guildmaster.")
        if (player.statBase(stats.mining) < CAPE_LEVEL) {
            chatNpc(
                neutral,
                "Once you've mastered Mining, come back to me and I'll sell you a Skillcape " +
                    "of Mining.",
            )
            return
        }
        val choice = choice2("Can I buy a Skillcape of Mining from you?", 1, "Nothing, thanks.", 2)
        if (choice == 2) {
            chatPlayer(neutral, "Nothing, thanks.")
            return
        }
        chatPlayer(quiz, "Can I buy a Skillcape of Mining from you?")
        if (access.invCoinTotal() < CAPE_PRICE) {
            chatNpc(neutral, "That will be 99,000 coins, which you don't seem to have.")
            return
        }
        if (access.inv.freeSpace() < 2) {
            chatNpc(
                neutral,
                "Unfortunately all Skillcapes are only available with a free hood, it's part of " +
                    "a skill promotion deal; buy one get one free, you know. So you'll need to " +
                    "free up some inventory space before I can sell you one.",
            )
            return
        }
        access.invTakeFee(CAPE_PRICE)
        access.invAdd(access.inv, falador_objs.mining_cape)
        access.invAdd(access.inv, falador_objs.mining_hood)
        chatNpc(happy, "Excellent! Wear that cape with pride my friend.")
    }

    private suspend fun Dialogue.belona(npc: Npc) {
        chatNpc(
            happy,
            "Hello there. I'll trade you some of my goods for any unidentified minerals you " +
                "find while mining in the guild.",
        )
        val gloves =
            access.invTotal(access.inv, falador_objs.mining_gloves) > 0 &&
                access.invTotal(access.inv, falador_objs.superior_mining_gloves) > 0
        val choice =
            if (gloves) {
                choice3("Let's trade.", 1, "Can you combine my mining gloves?", 3, "No thanks.", 2)
            } else {
                choice2("Let's trade.", 1, "No thanks.", 2)
            }
        when (choice) {
            1 -> access.openMineralShop(npc)
            3 -> expertGloves()
            else -> chatPlayer(neutral, "No thanks.")
        }
    }

    // UNVERIFIED: the wording; the cost (both gloves and 40 minerals) is the wiki's.
    private suspend fun Dialogue.expertGloves() {
        chatPlayer(quiz, "Can you combine my mining gloves?")
        if (access.invTotal(access.inv, falador_objs.minerals) < EXPERT_GLOVES_MINERALS) {
            chatNpc(
                neutral,
                "I can make you some expert mining gloves from those, but it'll cost you " +
                    "$EXPERT_GLOVES_MINERALS unidentified minerals.",
            )
            return
        }
        access.invDel(access.inv, falador_objs.mining_gloves, 1)
        access.invDel(access.inv, falador_objs.superior_mining_gloves, 1)
        access.invDel(access.inv, falador_objs.minerals, EXPERT_GLOVES_MINERALS)
        access.invAdd(access.inv, falador_objs.expert_mining_gloves)
        chatNpc(happy, "There you go, a pair of expert mining gloves.")
    }

    private fun ProtectedAccess.openMineralShop(npc: Npc) {
        shops.open(
            player,
            npc,
            "Mining Guild Mineral Exchange",
            falador_invs.mineral_shop,
            currency = falador_currencies.minerals,
        )
    }

    // UNVERIFIED: the toggle's messages.
    private fun ProtectedAccess.toggleMinerals() {
        val off = player.vars[falador_varps.minerals_off] == 0
        vars[falador_varps.minerals_off] = if (off) 1 else 0
        if (off) {
            mes("You will no longer receive unidentified minerals while mining.")
        } else {
            mes("You will now receive unidentified minerals while mining.")
        }
    }

    private suspend fun Dialogue.guildDwarf() {
        chatNpc(happy, "Welcome to the Mining Guild. Can I help you with anything?")
        val choice = choice2("What have you got in the Guild?", 1, "No thanks, I'm fine.", 2)
        if (choice == 2) {
            chatPlayer(neutral, "No thanks, I'm fine.")
            return
        }
        chatPlayer(quiz, "What have you got in the Guild?")
        chatNpc(
            happy,
            "Ooh, it's WONDERFUL! There are plenty of coal rocks along with some iron, mithril " +
                "and adamantite as well. Deeper in the guild you'll even find runite and " +
                "amethyst.",
        )
    }

    // ---- Malignius Mortifer

    // UNVERIFIED: all but his refusal line; Fairytale I counts as done.
    private suspend fun Dialogue.malignius() {
        chatPlayer(quiz, "Can I get another set of enchanted secateurs?")
        chatNpc(neutral, "If you have 40,000 coins, they're yours.")
        if (choice2("Okay, here you go.", 1, "No thanks.", 2) == 2) {
            chatPlayer(neutral, "No thanks.")
            return
        }
        if (access.invCoinTotal() < SECATEURS_PRICE) {
            chatNpc(angry, "You don't have 40,000 gp with you! Do you take me for a fool?")
            return
        }
        if (access.inv.freeSpace() < 1) {
            chatNpc(neutral, "You'll need a free space in your inventory for them.")
            return
        }
        access.invTakeFee(SECATEURS_PRICE)
        access.invAdd(access.inv, falador_objs.magic_secateurs)
        chatPlayer(happy, "Okay, here you go.")
    }

    private companion object {
        const val ALE_PRICE = 3
        const val GLASS_PRICE = 2
        const val WOAD_PRICE = 15
        const val WOAD_PRICE_TWO = 20
        const val CAPE_LEVEL = 99
        const val CAPE_PRICE = 99_000
        const val EXPERT_GLOVES_MINERALS = 40
        const val SECATEURS_PRICE = 40_000

        val GUILD_DWARVES =
            listOf(
                falador_npcs.guild_dwarf,
                falador_npcs.guild_dwarf2,
                falador_npcs.guild_guard,
                falador_npcs.guild_guard2,
            )
    }
}
