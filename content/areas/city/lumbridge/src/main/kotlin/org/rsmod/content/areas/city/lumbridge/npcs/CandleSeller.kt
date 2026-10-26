package org.rsmod.content.areas.city.lumbridge.npcs

import jakarta.inject.Inject
import org.rsmod.api.invtx.invAddOrDrop
import org.rsmod.api.invtx.invTakeFee
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

object CandleSellerNpcs : NpcReferences() {
    val candle_seller = find("swamp_candle_guy")
}

object CandleSellerObjs : ObjReferences() {
    val lit_candle = find("lit_candle")
}

class CandleSeller @Inject constructor(private val objRepo: ObjRepository) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(CandleSellerNpcs.candle_seller) { startDialogue(it.npc) { offer() } }
    }

    // Note: OSRS adds "How do you make lanterns?" to this menu once you have heard it.
    private suspend fun Dialogue.offer() {
        chatNpc(quiz, "Do you want a lit candle for 1000 gold?")
        val choice =
            choice3(
                "Yes please.",
                1,
                "One thousand gold?!",
                2,
                "No thanks, I'd rather curse the darkness.",
                3,
            )
        when (choice) {
            1 -> {
                chatPlayer(happy, "Yes please.")
                buy()
            }
            2 -> haggle()
            3 -> chatPlayer(neutral, "No thanks, I'd rather curse the darkness.")
        }
    }

    private suspend fun Dialogue.buy() {
        if (!player.invTakeFee(fee = 1000)) {
            chatPlayer(sad, "But I don't have that kind of money on me.")
            chatNpc(neutral, "Well then, no candle for you!")
            return
        }
        player.invAddOrDrop(objRepo, CandleSellerObjs.lit_candle)
        chatNpc(happy, "Here you go then.")
        chatNpc(
            neutral,
            "I should warn you, though, it can be dangerous to take a naked flame down " +
                "there. You'd be better off making a lantern.",
        )
        val choice =
            choice3(
                "What's so dangerous about a naked flame?",
                1,
                "How do you make lanterns?",
                2,
                "Thanks, bye.",
                3,
            )
        when (choice) {
            1 -> {
                chatPlayer(quiz, "What's so dangerous about a naked flame?")
                chatNpc(laugh, "Heh heh... You'll find out.")
            }
            2 -> lanterns()
            3 -> chatPlayer(happy, "Thanks, bye.")
        }
    }

    private suspend fun Dialogue.haggle() {
        chatPlayer(shocked, "One thousand gold?!")
        chatNpc(
            neutral,
            "Look, you're not going to be able to survive down that hole without a light " +
                "source.",
        )
        chatNpc(
            neutral,
            "So you could go off to the candle shop to buy one more cheaply. You could even " +
                "make your own lantern, which is a lot better.",
        )
        chatNpc(
            happy,
            "But I bet you want to find out what's down there right now, don't you? And you " +
                "can pay me 1000 gold for the privilege!",
        )
        val choice =
            choice3(
                "All right, you win, I'll buy a candle.",
                1,
                "No way.",
                2,
                "How do you make lanterns?",
                3,
            )
        when (choice) {
            1 -> {
                chatPlayer(neutral, "All right, you win, I'll buy a candle.")
                buy()
            }
            2 -> chatPlayer(angry, "No way.")
            3 -> lanterns()
        }
    }

    private suspend fun Dialogue.lanterns() {
        chatPlayer(quiz, "How do you make lanterns?")
        chatNpc(neutral, "Out of glass. The more advanced lanterns have a metal component as well.")
        chatNpc(
            neutral,
            "Firstly you can make a simple candle lantern out of glass. It's just like a " +
                "candle, but the flame isn't exposed, so it's safer.",
        )
        chatNpc(
            neutral,
            "Then you can make an oil lamp, which is brighter but has an exposed flame. But " +
                "if you make an iron frame for it you can turn it into an oil lantern.",
        )
        chatNpc(
            neutral,
            "Finally there's the bullseye lantern. You'll need to make a frame out of steel " +
                "and add a glass lens.",
        )
        chatNpc(
            neutral,
            "Once you've made your lamp or lantern, you'll need to make lamp oil for it. The " +
                "chemist near Rimmington has a machine for that.",
        )
        chatNpc(
            neutral,
            "For any light source, you'll need a tinderbox to light it. Keep your tinderbox " +
                "handy in case it goes out!",
        )
        chatNpc(
            happy,
            "But if all that's too complicated, you can buy a candle right here for 1000 gold!",
        )
        val wantsCandle =
            choice2(
                "All right, you win, I'll buy a candle.",
                true,
                "No thanks, I'd rather curse the darkness.",
                false,
            )
        if (wantsCandle) {
            chatPlayer(neutral, "All right, you win, I'll buy a candle.")
            buy()
        } else {
            chatPlayer(neutral, "No thanks, I'd rather curse the darkness.")
        }
    }
}
