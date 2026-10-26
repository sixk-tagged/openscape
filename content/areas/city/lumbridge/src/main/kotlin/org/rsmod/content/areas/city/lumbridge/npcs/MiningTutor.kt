package org.rsmod.content.areas.city.lumbridge.npcs

import org.rsmod.api.config.refs.objs
import org.rsmod.api.invtx.invAdd
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.baseMiningLvl
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.type.refs.content.ContentReferences
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.entity.Npc
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

object MiningTutorNpcs : NpcReferences() {
    val tutor = find("aide_tutor_mining")
}

object MiningTutorObjs : ObjReferences() {
    val mining_site_icon = find("mining_site_icon_dummy")
}

object MiningTutorContent : ContentReferences() {
    val pickaxe = find("mining_pickaxe")
}

class MiningTutor : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(MiningTutorNpcs.tutor) { startDialogue(it.npc) }
    }

    private suspend fun ProtectedAccess.startDialogue(npc: Npc) {
        startDialogue(npc) { mainMenu() }
    }

    // Note: "Tell me about different rocks and picks." submenu and world-map offers skipped.
    private suspend fun Dialogue.mainMenu() {
        val lvl = player.baseMiningLvl
        val advice =
            when {
                lvl >= 39 -> "Any advice for an advanced miner?"
                lvl >= 25 -> "I already know about the basics of mining, got any tips?"
                else -> "Can you teach me the basics of mining please?"
            }
        when (choice3(advice, 1, "Are there any mining related quests?", 2, "Goodbye.", 3)) {
            1 -> {
                chatPlayer(quiz, advice)
                when {
                    lvl >= 39 -> advancedAdvice()
                    lvl >= 25 -> tips()
                    else -> basics()
                }
                mainMenu()
            }
            2 -> {
                chatNpc(
                    happy,
                    "Oh yes, if you haven't already, speak to Doric who can be found around the " +
                        "anvils north of Falador. I'm sure he can help you out.",
                )
                chatNpc(
                    happy,
                    "You could investigate the Dig Site east of Varrock, I'm sure you'll find " +
                        "something rewarding there.",
                )
                mainMenu()
            }
            3 -> chatPlayer(neutral, "Goodbye.")
        }
    }

    private suspend fun Dialogue.givePickaxe() {
        val pickaxe = MiningTutorContent.pickaxe
        if (pickaxe in player.inv || pickaxe in player.worn) {
            chatNpc(happy, "As you already have a pick, you can mine the rocks around me.")
        } else if (player.invAdd(player.inv, objs.bronze_pickaxe).success) {
            chatNpc(
                happy,
                "As you're already here, have a pick so that you can mine the rocks around me.",
            )
        } else {
            chatNpc(
                sad,
                "I'd give you a pick to mine the rocks around me, but you don't have room in " +
                    "your inventory.",
            )
        }
    }

    private suspend fun Dialogue.basics() {
        objbox(
            MiningTutorObjs.mining_site_icon,
            "Look for this icon on your minimap to find mining rocks.",
        )
        givePickaxe()
        chatNpc(
            happy,
            "You can tell the ore you'll get from a rock by looking at it. The colour of the " +
                "rock helps, too.",
        )
        chatNpc(
            happy,
            "To mine, simply click on the rock to mine it, while you've got a pickaxe with you.",
        )
        chatNpc(
            happy,
            "When you have a full inventory, take it to the bank. There's a bank on the roof " +
                "of the castle in Lumbridge.",
        )
        objbox(
            objs.bank_icon,
            "Look for this symbol on your minimap after climbing the stairs of the Lumbridge " +
                "Castle to the top.",
        )
    }

    private suspend fun Dialogue.tips() {
        givePickaxe()
        chatNpc(
            happy,
            "You can get different rocks in different places throughout the land, explore and " +
                "find them.",
        )
        chatNpc(
            happy,
            "You could start by traveling to the Al Kharid mine. Follow the path north and " +
                "pay the 10 coins toll to go through the gate. Head north after passing " +
                "through the gate and you will find the mine.",
        )
        chatNpc(worried, "Be careful though, the scorpions in the mine will attack you.")
    }

    private suspend fun Dialogue.advancedAdvice() {
        givePickaxe()
        chatNpc(
            happy,
            "Investigate the mining guild, the entrance to this is in the east of Falador down " +
                "some steps. It could be most useful to you!",
        )
        chatNpc(
            happy,
            "You might also like to check out the coal mine west of Seers' village. This will " +
                "enable you to use the coal trucks.",
        )
        chatPlayer(happy, "Thank you, I'll remember that.")
    }
}
