package org.rsmod.content.areas.city.lumbridge.npcs

import org.rsmod.api.invtx.invAdd
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.baseRangedLvl
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.entity.Npc
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

object RangedTutorNpcs : NpcReferences() {
    val tutor = find("aide_tutor_ranging")
}

object RangedTutorObjs : ObjReferences() {
    val bow = find("aide_shortbow")
    val arrows = find("aide_arrow")
}

class RangedTutor : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(RangedTutorNpcs.tutor) { startDialogue(it.npc) }
        onOpNpc3(RangedTutorNpcs.tutor) { claim(it.npc) }
    }

    private suspend fun ProtectedAccess.startDialogue(npc: Npc): Unit =
        startDialogue(npc) {
            chatNpc(
                happy,
                "Hey there adventurer, I am the Ranged combat tutor. Is there anything you " +
                    "would like to know?",
            )
            mainMenu()
        }

    // Note: the "toggle my ammo" option is skipped; there is no ammo auto-equip setting.
    private suspend fun Dialogue.mainMenu() {
        val choice =
            choice3(
                "How can I train my Ranged?",
                1,
                "How do I create a bow and arrows?",
                2,
                "Goodbye.",
                3,
            )
        when (choice) {
            1 -> training()
            2 -> fletching()
            // Note: the wiki transcript for Goodbye is missing.
            3 -> chatPlayer(neutral, "Goodbye.")
        }
    }

    private suspend fun Dialogue.training() {
        chatPlayer(quiz, "How can I train my Ranged?")
        when {
            player.baseRangedLvl < 29 -> noviceAdvice()
            player.baseRangedLvl < 44 -> intermediateAdvice()
            else -> advancedAdvice()
        }
        chatNpc(
            happy,
            "If you are ever in the market for a new bow or some arrows, you should head on " +
                "over to Lowe's Archery Emporium in Varrock. I can show you where the shop is " +
                "if you like.",
        )
        val choice = choice2("Yes please.", 1, "No thanks.", 2)
        if (choice == 1) {
            // Note: the world map does not pan to Lowe's Archery Emporium.
            chatPlayer(happy, "Yes please.")
            return
        }
        chatPlayer(neutral, "No thanks")
        chatNpc(happy, "Is there anything else you want to know?")
        mainMenu()
    }

    // Note: no skills/combat tab switches and no Adventure paths line.
    private suspend fun Dialogue.noviceAdvice() {
        chatNpc(
            neutral,
            "To start with you'll need a bow and arrows, you were given a Shortbow and some " +
                "arrows when you arrived here from Tutorial island.",
        )
        chatNpc(happy, "Alternatively, you can claim a training bow and some arrows from me.")
        chatNpc(
            neutral,
            "Mikasi, the Magic Combat tutor and I both give out items every 30 minutes, " +
                "however you must choose whether you want runes or ranged equipment.",
        )
        chatNpc(
            neutral,
            "To claim the Training bow and arrows, right-click on me and choose Claim, to " +
                "claim runes right-click on the Magic Combat tutor and select Claim.",
        )
        chatNpc(
            neutral,
            "Not all bows can use every type of arrow, most bows have a limit. You can find " +
                "out your bows limit by checking the Ranged skill guide.",
        )
        chatNpc(
            neutral,
            "If you do decide to use the Training bow, you will only be able to use the " +
                "Training arrows with it. Remember to pick up your arrows, re-use them and " +
                "come back when you need more.",
        )
        chatNpc(
            neutral,
            "Once you have your bow and arrows, equip them by selecting their Wield option in " +
                "your inventory.",
        )
        chatNpc(
            neutral,
            "You can change the way you attack by going to the combat options tab. There are " +
                "three attack styles for bows. Those styles are Accurate, Rapid and Longrange.",
        )
        chatNpc(
            neutral,
            "Accurate increases your bows attack accuracy. Rapid will increase your attack " +
                "speed with the bow. Longrange will let you attack your enemies from a " +
                "greater distance.",
        )
    }

    private suspend fun Dialogue.intermediateAdvice() {
        chatNpc(
            neutral,
            "Use the best bow and arrows you can get your hands on and practically use, for " +
                "your level. This will ensure maximum damage.",
        )
        chatNpc(
            neutral,
            "Choose carefully where you shoot from. If you can, use fences and other things to " +
                "help protect you whilst you fight.",
        )
        chatNpc(
            neutral,
            "You may also find that the better the ranged armour you use, the more accurate " +
                "you will be with your bow.",
        )
    }

    private suspend fun Dialogue.advancedAdvice() {
        if (player.baseRangedLvl >= 99) {
            chatNpc(
                happy,
                "My word, I think you should be the one giving me advice instead, but I will " +
                    "do my best.",
            )
        }
        chatNpc(
            neutral,
            "You should know the basics of ranged combat by now, but if you are looking for " +
                "somewhere to test your skills and make some money, you should try hunting " +
                "Moss giants in Varrock sewers.",
        )
        chatNpc(
            neutral,
            "If you are lucky you might find a Mossy key, with this key you will be able to " +
                "enter Bryophyta's lair. Make sure to bring plenty of food and good armour " +
                "and weapons when you fight her as you will need it.",
        )
        chatNpc(
            happy,
            "If your lucky you might snag some goodies that you can sell for a decent profit.",
        )
        chatNpc(
            neutral,
            "If you are looking to do something different, visit the ranging guild over near " +
                "Hemenster, there you can practice your archery and earn tickets for it at the " +
                "same time.",
        )
        chatNpc(
            neutral,
            "You can then hand those tickets in for a variety of goods you can use to train " +
                "your Ranged skill.",
        )
    }

    private suspend fun Dialogue.fletching() {
        chatPlayer(quiz, "How do I create a bow and arrows?")
        chatNpc(
            happy,
            "Ahh the art of fletching. Fletching is used to create your own bow and arrows.",
        )
        if (!player.members) {
            chatNpc(
                neutral,
                "To create your bow and arrows you would need to gather some wood, feathers, " +
                    "arrow heads and bow string. The wood would be to make arrow shafts which " +
                    "can be turned into unstrung bows and arrow shafts.",
            )
            chatNpc(
                sad,
                "Unfortunately, Fletching is a member only skill so you will have to get your " +
                    "ranging supplies from shops or other players.",
            )
        } else {
            chatNpc(
                neutral,
                "It's quite simple really. You'll need an axe to cut some logs from trees and a " +
                    "knife. Knives can be found in and around the Lumbridge castle and in the " +
                    "Varrock General store upstairs.",
            )
            chatNpc(
                neutral,
                "Use your knife on the logs. This will bring up a menu listing items you can " +
                    "fletch.",
            )
            chatNpc(
                neutral,
                "For arrows you will need to smith some arrow heads and kill some chickens for " +
                    "feathers.",
            )
            chatNpc(
                neutral,
                "Add the feathers to your Arrow shafts to make Headless arrows, then use your " +
                    "chosen arrow heads on the Headless arrows to make your arrows.",
            )
            chatNpc(
                neutral,
                "Now for making bows. When accessing the fletching menu, instead of choosing " +
                    "Arrows shafts, you can make an unstrung bow instead.",
            )
            chatNpc(neutral, "To complete the bow you will need to get your hands on a Bow string.")
            chatNpc(
                neutral,
                "First you will need to get some flax from a flax field. There's one south of " +
                    "Seers' Village. Gather flax, then spin it on a spinning wheel, there's one " +
                    "in Seers' Village too.",
            )
            chatNpc(
                happy,
                "This makes bow strings which you can then use on the unstrung bows to make a " +
                    "working bow!",
            )
            chatPlayer(happy, "Brilliant. If I forget anything I'll come talk to you again.")
        }
        chatNpc(happy, "Is there anything else you want to know?")
        mainMenu()
    }

    private suspend fun ProtectedAccess.claim(npc: Npc): Unit =
        startDialogue(npc) {
            if (player.tutorClaimOnCooldown) {
                chatNpc(
                    neutral,
                    "I work with the Magic tutor to give out consumable items that you may need " +
                        "for combat such as arrows and runes. However we have had some cheeky " +
                        "people try to take both!",
                )
                chatNpc(
                    neutral,
                    "So, every half an hour, you may come back and claim either arrows OR " +
                        "runes, but not both. Come back in a while for arrows, or simply buy some.",
                )
                return@startDialogue
            }
            val bow = RangedTutorObjs.bow
            val arrows = RangedTutorObjs.arrows
            val needBow = bow !in player.inv && bow !in access.bank
            val needArrows = arrows !in player.inv && arrows !in access.bank
            // Note: the wiki has no line for owning both; both "already have" lines play.
            if (!needArrows) {
                chatNpc(neutral, "You already have some training arrows.")
            }
            if (!needBow) {
                chatNpc(neutral, "You already have a training bow.")
            }
            val gaveBow = needBow && player.invAdd(player.inv, bow).success
            val gaveArrows = needArrows && player.invAdd(player.inv, arrows, 25).success
            if (gaveBow || gaveArrows) {
                player.markTutorClaim()
            }
            if (needBow && !gaveBow) {
                chatNpc(
                    sad,
                    "If you had any room in your pack I'd give you a training bow. That's a " +
                        "shame. Come back when you do.",
                )
            }
            if (needArrows && !gaveArrows) {
                chatNpc(
                    sad,
                    "If you had enough space in your inventory I'd give you some training " +
                        "arrows, come back when you do.",
                )
            }
            when {
                gaveBow && gaveArrows ->
                    doubleobjbox(
                        bow,
                        arrows,
                        "Nemarti gives you a Training shortbow and 25 arrows. They can only be " +
                            "used together.",
                    )
                gaveBow ->
                    objbox(
                        bow,
                        "Nemarti gives you a Training shortbow. It can only be used with " +
                            "Training arrows.",
                    )
                gaveArrows ->
                    objbox(
                        arrows,
                        "Nemarti gives you 25 training arrows. They can only be used with the " +
                            "Training shortbow.",
                    )
            }
        }
}
