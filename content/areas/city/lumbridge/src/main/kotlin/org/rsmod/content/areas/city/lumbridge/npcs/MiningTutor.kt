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
    val furnace_icon = find("furnace_icon_dummy")
    val clay = find("clay")
    val rune_essence = find("blankrune")
    val copper_ore = find("copper_ore")
    val tin_ore = find("tin_ore")
    val iron_ore = find("iron_ore")
    val coal = find("coal")
    val silver_ore = find("silver_ore")
    val gold_ore = find("gold_ore")
    val mithril_ore = find("mithril_ore")
    val adamantite_ore = find("adamantite_ore")
    val runite_ore = find("runite_ore")
    val iron_pickaxe = find("iron_pickaxe")
    val steel_pickaxe = find("steel_pickaxe")
    val mithril_pickaxe = find("mithril_pickaxe")
    val rune_pickaxe = find("rune_pickaxe")
}

object MiningTutorContent : ContentReferences() {
    val pickaxe = find("mining_pickaxe")
}

// Note: "Yes please" to his map offers doesn't open the world map at the place; there is no
// world map pan here yet.
class MiningTutor : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(MiningTutorNpcs.tutor) { startDialogue(it.npc) }
    }

    private suspend fun ProtectedAccess.startDialogue(npc: Npc) {
        startDialogue(npc) { mainMenu() }
    }

    private suspend fun Dialogue.mainMenu() {
        val lvl = player.baseMiningLvl
        val advice =
            when {
                lvl >= 39 -> "Any advice for an advanced miner?"
                lvl >= 25 -> "I already know about the basics of mining, got any tips?"
                else -> "Can you teach me the basics of mining please?"
            }
        val choice =
            choice4(
                advice,
                1,
                "Are there any mining related quests?",
                2,
                "Tell me about different rocks and picks.",
                3,
                "Goodbye.",
                4,
            )
        when (choice) {
            1 -> {
                chatPlayer(quiz, advice)
                when {
                    lvl >= 39 -> advancedAdvice()
                    lvl >= 25 -> tips()
                    else -> basics()
                }
            }
            2 -> {
                chatNpc(
                    happy,
                    "Oh yes, if you haven't already, speak to Doric who can be found around the " +
                        "anvils north of Falador. I'm sure he can help you out.",
                )
                if (player.members) {
                    chatNpc(
                        happy,
                        "You could investigate the Dig Site east of Varrock, I'm sure you'll find " +
                            "something rewarding there.",
                    )
                }
                mainMenu()
            }
            3 -> {
                chatPlayer(quiz, "Tell me about different rocks and picks.")
                chatNpc(happy, "Ok then.. here goes....")
                rocksAndPicksMenu()
            }
            4 -> chatPlayer(neutral, "Goodbye.")
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
        mainMenu()
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
        chatNpc(happy, "I could show you where Al Kharid mine is on your map if you like?")
        mapOffer()
    }

    private suspend fun Dialogue.advancedAdvice() {
        givePickaxe()
        chatNpc(
            happy,
            "Investigate the mining guild, the entrance to this is in the east of Falador down " +
                "some steps. It could be most useful to you!",
        )
        if (!player.members) {
            chatPlayer(happy, "Thank you, I'll remember that.")
            chatNpc(happy, "I could show you where the mining guild is on your map if you like?")
            mapOffer()
            return
        }
        chatNpc(
            happy,
            "You might also like to check out the coal mine west of Seers' village. This will " +
                "enable you to use the coal trucks.",
        )
        chatPlayer(happy, "Thank you, I'll remember that.")
        chatNpc(happy, "I could show you where either of the places are on your map if you like?")
        val choice =
            choice3(
                "Can you show me where the mining guild is?",
                1,
                "Can you show me where the coal mine is?",
                2,
                "No thanks.",
                3,
            )
        when (choice) {
            1 -> chatPlayer(quiz, "Can you show me where the mining guild is?")
            2 -> chatPlayer(quiz, "Can you show me where the coal mine is?")
            3 -> {
                chatPlayer(neutral, "No thanks.")
                mainMenu()
            }
        }
    }

    private suspend fun Dialogue.mapOffer() {
        if (choice2("Yes please.", true, "No thanks.", false)) {
            chatPlayer(happy, "Yes please.")
        } else {
            chatPlayer(neutral, "No thanks.")
            mainMenu()
        }
    }

    /* Rocks and picks */

    private suspend fun Dialogue.rocksAndPicksMenu() {
        when (choice3("Rocks and Ores.", 1, "Pickaxes.", 2, "Teach me about mining.", 3)) {
            1 -> rocksMenu()
            2 -> pickaxesMenu()
            3 -> mainMenu()
        }
    }

    private suspend fun Dialogue.rocksMenu() {
        val choice =
            choice5(
                "Clay and Rune Essence.",
                1,
                "Copper and Tin.",
                2,
                "Iron and Coal.",
                3,
                "Teach me about mining.",
                4,
                "More...",
                5,
            )
        when (choice) {
            1 -> {
                objbox(
                    MiningTutorObjs.clay,
                    "Clay can be mined in many places. Try south west of Varrock or the crafting " +
                        "guild. See the crafting tutor upstairs in Lumbridge Castle for more " +
                        "information on what you can make with clay.",
                )
                objbox(
                    MiningTutorObjs.rune_essence,
                    "Rune Essence can only be mined at secret places... I don't know where these " +
                        "are but you can speak to the Duke of Lumbridge to find out more.",
                )
                chatNpc(
                    happy,
                    "You can make all sorts of things from clay, if you have the crafting skill. " +
                        "Rune Essence is used for magic to cast spells.",
                )
                afterRocks()
            }
            2 -> {
                objbox(
                    MiningTutorObjs.copper_ore,
                    "Rocks which yield copper ore can be found around here, and other places " +
                        "such as Al Kharid, Rimmington or Varrock mines. They're quite common.",
                )
                objbox(
                    MiningTutorObjs.tin_ore,
                    "Tin ore is much the same as copper. It can be found around this area as well " +
                        "as Varrock, Al Kharid and the Barbarian Village.",
                )
                afterRocks()
            }
            3 -> {
                objbox(
                    MiningTutorObjs.iron_ore,
                    "Iron can be mined around Rimmington, Al Kharid and Varrock. It's tricky to " +
                        "smelt though - see the boy at the furnace in Lumbridge for tips.",
                )
                objbox(
                    MiningTutorObjs.furnace_icon,
                    "Look for this icon on your minimap to find the furnace.",
                )
                objbox(
                    MiningTutorObjs.coal,
                    "You'll need to be a fairly good miner before you can start mining coal. You " +
                        "can find it in the Dwarf mine, Al Kharid and quite a few other places.",
                )
                afterRocks()
            }
            4 -> mainMenu()
            5 -> moreRocksMenu()
        }
    }

    private suspend fun Dialogue.moreRocksMenu() {
        val choice =
            choice4(
                "Silver and Gold.",
                1,
                "Mithril, Adamantite and Runite.",
                2,
                "Teach me about mining.",
                3,
                "Back...",
                4,
            )
        when (choice) {
            1 -> {
                objbox(
                    MiningTutorObjs.silver_ore,
                    "Silver can be mined to help you with your crafting. You can find it in the " +
                        "Crafting guild, Al Kharid and many other mines.",
                )
                objbox(
                    MiningTutorObjs.gold_ore,
                    "Gold requires quite a lot of experience in mining before you can " +
                        "successfully extract it from the rocks. It can be found in Al Kharid, " +
                        "the Crafting guild, Rimmington and other places.",
                )
                afterRocks()
            }
            2 -> {
                doubleobjbox(
                    MiningTutorObjs.mithril_ore,
                    MiningTutorObjs.adamantite_ore,
                    "Mithril and Adamantite rocks are fairly rare and require great skill to " +
                        "mine. You can find them in Al Kharid, the Dwarven mines, Lumbridge " +
                        "Swamps and other places.",
                )
                objbox(
                    MiningTutorObjs.runite_ore,
                    "Runite is the most prized of ores and is very rare. I'll leave you to find " +
                        "where to mine it.",
                )
                afterRocks()
            }
            3 -> mainMenu()
            4 -> rocksMenu()
        }
    }

    private suspend fun Dialogue.afterRocks() {
        if (choice2("Teach me about mining.", true, "Teach me about different rocks.", false)) {
            mainMenu()
        } else {
            rocksMenu()
        }
    }

    private suspend fun Dialogue.pickaxesMenu() {
        val choice =
            choice3(
                "Bronze, Iron and Steel.",
                1,
                "Mithril, Adamantite and Runite.",
                2,
                "Teach me about mining.",
                3,
            )
        when (choice) {
            1 -> {
                objbox(
                    objs.bronze_pickaxe,
                    "The Bronze Pick is your basic tool of the trade. You can buy one from Bob's " +
                        "Axe shop very cheaply, that's north of here in Lumbridge, I also heard " +
                        "that exploring the castle walls may yield a surprise or two.",
                )
                doubleobjbox(
                    MiningTutorObjs.iron_pickaxe,
                    MiningTutorObjs.steel_pickaxe,
                    "The Iron and Steel Picks are faster than the bronze pickaxe, but you will " +
                        "need to progress with your mining a little before you can use them. You " +
                        "can buy them from Nurmof in the Dwarven mines.",
                )
                pickaxesMenu()
            }
            2 -> {
                doubleobjbox(
                    MiningTutorObjs.mithril_pickaxe,
                    MiningTutorObjs.rune_pickaxe,
                    "Mithril, Adamantite, and Rune Picks may be used as you progress with your " +
                        "skill of mining. They can be obtained from Nurmof in the Dwarven mines.",
                )
                pickaxesMenu()
            }
            3 -> mainMenu()
        }
    }
}
