package org.rsmod.content.areas.city.lumbridge.npcs

import org.rsmod.api.invtx.invAdd
import org.rsmod.api.invtx.invDel
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpcU
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.entity.Npc
import org.rsmod.game.type.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

object MeleeTutorNpcs : NpcReferences() {
    val tutor = find("aide_tutor_melee")
}

object MeleeTutorObjs : ObjReferences() {
    val sword = find("aide_shortsword")
    val shield = find("aide_shield")
}

class MeleeTutor : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(MeleeTutorNpcs.tutor) { startDialogue(it.npc) }
        onOpNpcU(MeleeTutorNpcs.tutor, MeleeTutorObjs.sword) {
            handBack(it.npc, it.invSlot, MeleeTutorObjs.sword, "sword")
        }
        onOpNpcU(MeleeTutorNpcs.tutor, MeleeTutorObjs.shield) {
            handBack(it.npc, it.invSlot, MeleeTutorObjs.shield, "shield")
        }
        onOpNpcU(MeleeTutorNpcs.tutor) {
            startDialogue(it.npc) { chatNpc(neutral, "I have no use for that, sorry.") }
        }
    }

    // Note: the 99 Defence greeting and its Defence cape sale are skipped.
    private suspend fun ProtectedAccess.startDialogue(npc: Npc): Unit =
        startDialogue(npc) {
            chatNpc(
                happy,
                "Greetings adventurer, I am the Melee combat tutor. Is there anything I can do " +
                    "for you?",
            )
            mainMenu()
        }

    private suspend fun Dialogue.mainMenu() {
        val choice =
            choice5(
                "Tell me about melee combat.",
                1,
                "Tell me about different weapon types I can use.",
                2,
                "Tell me about skillcapes.",
                3,
                "I'd like a training sword and shield.",
                4,
                "Goodbye.",
                5,
            )
        when (choice) {
            1 -> meleeCombat()
            2 -> weaponTypes()
            3 -> skillcapes()
            4 -> trainingGear()
            5 -> chatPlayer(neutral, "Goodbye.")
        }
    }

    // Note: no worn/combat tab switches, no Adventure paths line, and no "something a bit
    // more... human" PvP follow-up (it needs a sixth option).
    private suspend fun Dialogue.meleeCombat() {
        chatPlayer(quiz, "Tell me about melee combat.")
        chatNpc(
            neutral,
            "Well adventurer, the first thing you will need is a sword and a shield " +
                "appropriate for your level.",
        )
        chatNpc(
            neutral,
            "Make sure to equip your sword and shield. Click on them in your inventory, " +
                "they will disappear from your inventory and move to your worn items. You can " +
                "see your worn items in the worn items tab here.",
        )
        chatNpc(
            neutral,
            "When you are wielding your sword you will then be able to see the correct " +
                "options in the combat interface.",
        )
        chatNpc(
            neutral,
            "There are four different melee styles. Accurate, aggressive, defensive and " +
                "controlled. Not all weapons will have all four styles though.",
        )
        chatPlayer(quiz, "Interesting, what does each style do?")
        chatNpc(
            happy,
            "Well I am glad you asked. The accurate style will give you experience points in " +
                "your Attack skill, you will also find you will deal damage more frequently " +
                "as a result of being, well, more accurate.",
        )
        chatNpc(
            neutral,
            "Next we have the aggressive style. This style will give you experience points " +
                "in your Strength skill. When using this style you will notice that your " +
                "attacks will hit a little harder.",
        )
        chatNpc(
            neutral,
            "Now for the defensive style, this style will give you experience points in your " +
                "Defensive skill. When using this style you will notice that you get hit less " +
                "often.",
        )
        chatNpc(
            neutral,
            "Finally, we have the controlled style. This style will give you the same amount " +
                "of experience as the other styles would but shared across all three of the " +
                "combat skills.",
        )
        chatNpc(
            neutral,
            "If you were using the training sword for example, there are four different " +
                "attack types. Stab, lunge, slash and block.",
        )
        chatNpc(
            neutral,
            "Each type uses one of the attack styles. Stab uses accurate, lunge and slash use " +
                "aggressive and block uses defensive.",
        )
        chatNpc(
            neutral,
            "To find out which style an attack type uses, hover your mouse cursor over the " +
                "style button.",
        )
        chatNpc(happy, "Is there anything else you would like to know?")
        mainMenu()
    }

    private suspend fun Dialogue.weaponTypes() {
        chatPlayer(quiz, "Tell me about different weapon types I can use")
        chatNpc(
            neutral,
            "Well let me see now...There are stabbing type weapons such as daggers, then you " +
                "have swords which are slashing, maces that have great crushing abilities, " +
                "battle axes which are powerful.",
        )
        chatNpc(
            happy,
            "It depends a lot on how you want to fight. Experiment and find out what is best " +
                "for you. Never be scared to try out a new weapon; you never know, you might " +
                "like it!",
        )
        chatNpc(
            happy,
            "While I tried all of them for a while, I settled on this rather good sword.",
        )
        chatNpc(
            neutral,
            "You might also find that different weapon types are more accurate against " +
                "different monsters.",
        )
        chatNpc(happy, "Is there anything else you would like to know?")
        mainMenu()
    }

    private suspend fun Dialogue.skillcapes() {
        chatPlayer(quiz, "Tell me about skillcapes.")
        chatNpc(
            neutral,
            "Of course. Skillcapes are a symbol of achievement. Only people who have mastered " +
                "a skill and reached level 99 can get their hands on them and gain the " +
                "benefits they carry.",
        )
        chatNpc(
            neutral,
            "The Cape of Defence will act as ring of life, saving you from combat if your " +
                "hitpoints become low.",
        )
        chatNpc(happy, "Is there anything else you would like to know?")
        mainMenu()
    }

    private suspend fun Dialogue.trainingGear() {
        chatPlayer(happy, "I'd like a training sword and shield.")
        val sword = MeleeTutorObjs.sword
        val shield = MeleeTutorObjs.shield
        val needSword = !owns(sword)
        val needShield = !owns(shield)
        if (!needSword && !needShield) {
            chatNpc(
                neutral,
                "You already have a training sword and shield. Save some for the other " +
                    "adventurers.",
            )
        } else if (player.inv.freeSpace() < listOf(needSword, needShield).count { it }) {
            // Note: the wiki has no full-inventory line for Harlan.
            chatNpc(neutral, "You don't have enough space in your inventory.")
            return
        } else {
            if (needSword) {
                player.invAdd(player.inv, sword)
            }
            if (needShield) {
                player.invAdd(player.inv, shield)
            }
            when {
                needSword && needShield ->
                    doubleobjbox(shield, sword, "Harlan gives you a Training sword and shield.")
                needSword -> objbox(sword, "Harlan gives you a Training sword.")
                else -> objbox(shield, "Harlan gives you a Training shield.")
            }
            chatNpc(happy, "There you go, use it well.")
        }
        chatNpc(happy, "Is there anything else I can help you with?")
        mainMenu()
    }

    private fun Dialogue.owns(obj: ObjType): Boolean =
        obj in player.inv || obj in player.worn || obj in access.bank

    private suspend fun ProtectedAccess.handBack(npc: Npc, slot: Int, obj: ObjType, name: String) {
        startDialogue(npc) {
            chatPlayer(neutral, "I no longer need my Training $name, would you like it back?")
            chatNpc(
                happy,
                "Sure thing, I can give this to another adventurer who is in need of training.",
            )
            if (player.invDel(player.inv, obj, slot = slot).success) {
                // "back you" is the wiki's own typo.
                objbox(obj, "You give back you training $name to the Melee combat tutor.")
            }
        }
    }
}
