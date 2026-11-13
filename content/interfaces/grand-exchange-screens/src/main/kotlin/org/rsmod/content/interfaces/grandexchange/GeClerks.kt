package org.rsmod.content.interfaces.grandexchange

import jakarta.inject.Inject
import org.rsmod.api.ge.GeVarps
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.script.onOpNpc4
import org.rsmod.content.other.grandexchangeextras.ge_extras_npcs
import org.rsmod.content.other.grandexchangeextras.openItemSets
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** The Grand Exchange clerks (Talk-to, Exchange, History) and the exchange booths. */
class GeClerks @Inject constructor(private val screen: GeOfferScreen) : PluginScript() {
    override fun ScriptContext.startup() {
        for (clerk in ge_extras_npcs.clerks) {
            onOpNpc1(clerk) { startDialogue(it.npc) { clerk() } }
            onOpNpc3(clerk) { with(screen) { openExchange() } }
            onOpNpc4(clerk) { with(screen) { openHistory() } }
        }
        onOpLoc1(GeScreenLocs.exchange_booth) { with(screen) { openExchange() } }
    }

    // Note: the Combat path voucher branch is skipped (no Adventure Paths).
    private suspend fun Dialogue.clerk() {
        chatNpc(neutral, WELCOME)
        menu(withHelp = true)
    }

    private suspend fun Dialogue.menu(withHelp: Boolean) {
        val reminders = access.vars[GeVarps.collect_reminder] != 0
        val reminderOption = if (reminders) REMINDERS_OFF else REMINDERS_ON
        val choice =
            if (withHelp) {
                choice5(
                    "How do I use the Grand Exchange?",
                    Option.Help,
                    TRADE,
                    Option.Trade,
                    SETS,
                    Option.Sets,
                    reminderOption,
                    Option.Reminders,
                    "I'm fine, thanks.",
                    Option.Done,
                )
            } else {
                choice4(
                    TRADE,
                    Option.Trade,
                    SETS,
                    Option.Sets,
                    reminderOption,
                    Option.Reminders,
                    "I'm fine, thanks.",
                    Option.Done,
                )
            }
        when (choice) {
            Option.Help -> help()
            Option.Trade -> {
                chatPlayer(neutral, TRADE)
                with(screen) { access.openExchange() }
            }
            Option.Sets -> {
                chatPlayer(neutral, SETS)
                access.openItemSets()
            }
            Option.Reminders -> reminders(on = !reminders)
            Option.Done -> chatPlayer(neutral, "I'm fine, thanks.")
        }
    }

    private suspend fun Dialogue.help() {
        chatPlayer(quiz, "How do I use the Grand Exchange?")
        chatNpc(
            neutral,
            "My colleague and I can let you set up trade offers. You can offer to Sell items or " +
                "Buy items.",
        )
        chatNpc(
            neutral,
            "When you want to sell something, you give us the items and tell us how much money " +
                "you want for them.",
        )
        chatNpc(
            neutral,
            "We'll look for someone who wants to buy those items at your price, and we'll " +
                "perform the trade. You can then collect the cash here, or at any bank.",
        )
        chatNpc(
            neutral,
            "When you want to buy something, you tell us what you want, and give us the cash " +
                "you're willing to spend on it.",
        )
        chatNpc(
            neutral,
            "We'll look for someone who's selling those items at your price, and we'll perform " +
                "the trade. You can then collect the items here, or at any bank, along with any " +
                "left-over cash.",
        )
        chatNpc(
            neutral,
            "Sometimes it takes a while to find a matching trade offer. If you change your mind, " +
                "we'll let you cancel your trade offer, and we'll return your unused items and " +
                "cash.",
        )
        chatNpc(
            neutral,
            "That's all the essential information you need to get started. Would you like to " +
                "trade now, or exchange item sets?",
        )
        menu(withHelp = false)
    }

    private suspend fun Dialogue.reminders(on: Boolean) {
        var turnOn = on
        while (true) {
            if (turnOn) {
                chatPlayer(neutral, REMINDERS_ON)
                access.vars[GeVarps.collect_reminder] = 1
                chatNpc(
                    neutral,
                    "Okay, when you log in, we'll send you a message about any items that are " +
                        "waiting for you to collect them.",
                )
            } else {
                chatPlayer(neutral, REMINDERS_OFF)
                access.vars[GeVarps.collect_reminder] = 0
                chatNpc(
                    neutral,
                    "Okay, when you log in, you will no longer be reminded about items waiting " +
                        "to be collected, unless the items only arrived after you logged out.",
                )
            }
            val other = if (turnOn) REMINDERS_OFF else REMINDERS_ON
            if (!choice2("Thank you.", false, other, true)) {
                chatPlayer(neutral, "Thank you.")
                return
            }
            turnOn = !turnOn
        }
    }

    private enum class Option {
        Help,
        Trade,
        Sets,
        Reminders,
        Done,
    }

    private companion object {
        const val WELCOME =
            "Welcome to the Grand Exchange. Would you like to trade now, or exchange item sets?"
        const val TRADE = "I'd like to set up trade offers please."
        const val SETS = "Can you help me with item sets?"
        const val REMINDERS_ON = "I'd like Exchange collection reminders on login, please."
        const val REMINDERS_OFF = "I don't want Exchange collection reminders on login."
    }
}
