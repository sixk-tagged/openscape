package org.rsmod.content.areas.city.edgeville.npcs

import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.stat.statBase
import org.rsmod.api.script.onOpNpc1
import org.rsmod.content.areas.city.edgeville.configs.edgeville_npcs
import org.rsmod.content.areas.city.edgeville.configs.edgeville_objs
import org.rsmod.content.areas.city.edgeville.configs.edgeville_varps
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The Edgeville Monastery: Abbot Langley lets Prayer 31 join the order (the first floor's ladders
 * check for it), the monks and the Abbot heal, and Brother Jered blesses holy symbols and sells the
 * Prayer cape.
 */
class Monastery : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(edgeville_npcs.abbot) { startDialogue(it.npc) { abbot() } }
        onOpNpc1(edgeville_npcs.monk) {
            val groundFloor = it.npc.coords.level == 0
            startDialogue(it.npc) { monk(groundFloor) }
        }
        onOpNpc1(edgeville_npcs.jered) { startDialogue(it.npc) { jered() } }
        onOpNpc1(edgeville_npcs.althric) {
            startDialogue(it.npc) {
                chatPlayer(happy, "Very nice rosebushes you have here.")
                chatNpc(
                    happy,
                    "Yes, it has taken me many long hours in this garden to get them looking so " +
                        "good.",
                )
            }
        }
    }

    private suspend fun Dialogue.abbot() {
        chatNpc(neutral, "Greetings traveller.")
        val joined = vars[edgeville_varps.monastery_joined] != 0
        val choice =
            if (joined) {
                choice2(
                    "Can you heal me? I'm injured.",
                    1,
                    "Isn't this place built a bit out of the way?",
                    2,
                )
            } else {
                choice3(
                    "Can you heal me? I'm injured.",
                    1,
                    "Isn't this place built a bit out of the way?",
                    2,
                    "How do I get further into the monastery?",
                    3,
                )
            }
        when (choice) {
            1 -> heal(ABBOT_HEAL, ABBOT_HEAL_PERCENT, ABBOT_HEAL_MESSAGE)
            2 -> outOfTheWay()
            3 -> {
                chatPlayer(quiz, "How do I get further into the monastery?")
                // Note: wording unverified.
                chatNpc(neutral, "Only members of our order can go further into the monastery.")
                joinOrder()
            }
        }
    }

    private suspend fun Dialogue.monk(groundFloor: Boolean) {
        chatNpc(neutral, "Greetings traveller.")
        val choice =
            if (groundFloor) {
                choice3(
                    "Can you heal me? I'm injured.",
                    1,
                    "Isn't this place built a bit out of the way?",
                    2,
                    "How do I get further into the monastery?",
                    3,
                )
            } else {
                choice2(
                    "Can you heal me? I'm injured.",
                    1,
                    "Isn't this place built a bit out of the way?",
                    2,
                )
            }
        when (choice) {
            1 -> heal(MONK_HEAL, MONK_HEAL_PERCENT, MONK_HEAL_MESSAGE)
            2 -> outOfTheWay()
            3 -> {
                chatPlayer(quiz, "How do I get further into the monastery?")
                chatNpc(
                    neutral,
                    "You'll need to talk to Abbot Langley about that. He's usually to be found " +
                        "walking the halls of the monastery.",
                )
            }
        }
    }

    /** Heals [constant] hitpoints plus [percent] of the player's level (never past the maximum). */
    private suspend fun Dialogue.heal(constant: Int, percent: Int, message: String) {
        chatPlayer(quiz, "Can you heal me? I'm injured.")
        chatNpc(neutral, "Ok.")
        access.statHeal(stats.hitpoints, constant, percent)
        access.mes(message)
    }

    // Note: the second half of the wiki's answer is unverified.
    private suspend fun Dialogue.outOfTheWay() {
        chatPlayer(quiz, "Isn't this place built a bit out of the way?")
        chatNpc(
            happy,
            "We like it that way actually! We get disturbed less. We still get rather a lot of " +
                "visitors, though.",
        )
    }

    private suspend fun Dialogue.jered() {
        // Note: wording unverified.
        chatNpc(neutral, "Greetings. I am Brother Jered. How may I help you?")
        val choice =
            choice3(
                "What can you do to help a bold adventurer like myself?",
                1,
                "Can I buy a Skillcape of Prayer?",
                2,
                "Nothing, thanks.",
                3,
            )
        when (choice) {
            1 -> {
                chatPlayer(quiz, "What can you do to help a bold adventurer like myself?")
                if (edgeville_objs.unblessed_symbol in access.inv) {
                    blessSymbol()
                } else {
                    chatNpc(
                        neutral,
                        "I can bless an unblessed holy symbol for you, free of charge. Those who " +
                            "have mastered Prayer may also buy a Skillcape of Prayer from me.",
                    )
                }
            }
            2 -> prayerCape()
            3 -> chatPlayer(neutral, "Nothing, thanks.")
        }
    }

    private suspend fun Dialogue.blessSymbol() {
        chatNpc(happy, "I see you carry an unblessed holy symbol. Shall I bless it for you?")
        val choice = choice2("Bless star, please.", 1, "No thanks.", 2)
        if (choice == 2) {
            chatPlayer(neutral, "No thanks.")
            return
        }
        chatPlayer(happy, "Bless star, please.")
        if (access.invDel(access.inv, edgeville_objs.unblessed_symbol).failure) {
            return
        }
        access.invAdd(access.inv, edgeville_objs.blessed_symbol)
        mesbox(
            "You give Jered the symbol. Jered closes his eyes and chants for a moment, then " +
                "passes you the holy symbol."
        )
    }

    // Note: the trimmed cape for a second 99 is skipped.
    private suspend fun Dialogue.prayerCape() {
        chatPlayer(quiz, "Can I buy a Skillcape of Prayer?")
        if (access.player.statBase(stats.prayer) < CAPE_LEVEL) {
            chatNpc(
                neutral,
                "I only sell the cape to those who have mastered Prayer. Come back at level 99.",
            )
            return
        }
        chatNpc(happy, "Of course. That will be 99,000 coins.")
        if (access.inv.freeSpace() < 2) {
            chatNpc(neutral, "You'll need two free inventory spaces to carry the cape and hood.")
        } else if (!access.invTakeFee(CAPE_PRICE)) {
            chatPlayer(sad, "I'm afraid I haven't got that many coins.")
        } else {
            access.invAdd(access.inv, edgeville_objs.prayer_cape)
            access.invAdd(access.inv, edgeville_objs.prayer_hood)
            access.mes("You hand over 99,000 coins and receive a cape and hood.")
        }
    }

    private companion object {
        const val ABBOT_HEAL = 4
        const val ABBOT_HEAL_PERCENT = 12
        const val MONK_HEAL = 2
        const val MONK_HEAL_PERCENT = 20
        const val ABBOT_HEAL_MESSAGE =
            "Abbot Langley places his hands on your head. You feel a little better."
        // Note: the monks' own message is unverified.
        const val MONK_HEAL_MESSAGE =
            "The monk places his hands on your head. You feel a little better."
        const val CAPE_LEVEL = 99
        const val CAPE_PRICE = 99_000
    }
}

private const val JOIN_LEVEL = 31

/** Offered by the Abbot when asked about the first floor: Prayer 31 joins the order for good. */
internal suspend fun Dialogue.joinOrder() {
    val choice = choice2("Well can I join your order?", 1, "Oh, I'll leave it then.", 2)
    if (choice == 2) {
        chatPlayer(neutral, "Oh, I'll leave it then.")
        return
    }
    chatPlayer(quiz, "Well can I join your order?")
    if (player.statBase(stats.prayer) < JOIN_LEVEL) {
        chatNpcSpecific(
            "Abbot Langley",
            edgeville_npcs.abbot,
            sad,
            "No. I am sorry, but I feel you are not devout enough.",
        )
        access.mes("You need a prayer level of 31 to join the order.")
        return
    }
    chatNpcSpecific(
        "Abbot Langley",
        edgeville_npcs.abbot,
        happy,
        "Ok, I see you are someone suitable for our order. You may join.",
    )
    vars[edgeville_varps.monastery_joined] = 1
}

/** The foot of a ladder before joining: the Abbot (south-east) or a monk (south-west) speaks. */
internal suspend fun Dialogue.monasteryEntrance(abbotSpeaks: Boolean) {
    val line = "Only members of our order can go up there."
    if (abbotSpeaks) {
        chatNpcSpecific("Abbot Langley", edgeville_npcs.abbot, neutral, line)
        joinOrder()
    } else {
        chatNpcSpecific(
            "Monk",
            edgeville_npcs.monk,
            neutral,
            "$line You'll need to talk to Abbot Langley if you want to explore the monastery " +
                "further.",
        )
    }
}
