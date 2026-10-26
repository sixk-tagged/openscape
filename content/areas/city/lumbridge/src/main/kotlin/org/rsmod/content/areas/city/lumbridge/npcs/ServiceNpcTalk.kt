package org.rsmod.content.areas.city.lumbridge.npcs

import org.rsmod.api.config.refs.npcs
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.content.areas.city.lumbridge.configs.lumbridge_npcs
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

object ServiceNpcs : NpcReferences() {
    val vasquen = find("farming_gardener_hops_3")
    val fayeth = find("farming_gardener_tree_4")
}

/** Talk-to for NPCs whose shop or service ops (Trade, Pay, Exchange, Scry) are not scripted. */
class ServiceNpcTalk : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(lumbridge_npcs.perdu) { startDialogue(it.npc) { perdu() } }
        onOpNpc1(lumbridge_npcs.abigaila) {
            startDialogue(it.npc) {
                chatNpc(happy, "What can I help you with, dear?")
                abigailaMenu()
            }
        }
        onOpNpc1(ServiceNpcs.fayeth) { startDialogue(it.npc) { gardener() } }
        // Note: Vasquen's transcript is missing; the wiki says it matches the other farmers.
        onOpNpc1(ServiceNpcs.vasquen) { startDialogue(it.npc) { gardener() } }
        onOpNpc1(npcs.farming_tools_leprechaun) {
            startDialogue(it.npc) {
                chatNpc(
                    happy,
                    "Ah, 'tis a foine day to be sure! Were yez wantin' me to store yer tools, " +
                        "or maybe ye might be wantin' yer stuff back from me?",
                )
                leprechaunMenu()
            }
        }
    }

    // Note: owed-XP claims, the lost property shop and Trouver locking are skipped.
    private suspend fun Dialogue.perdu() {
        chatNpc(
            happy,
            "Hello. I've found some stuff that I think is yours. You can pay me with cash " +
                "you're carrying, or I can take payments from your bank. Would you like to " +
                "buy anything from me?",
        )
        val choice =
            choice3(
                "Why do you charge for returning lost items?",
                1,
                "About items that break when people kill me...",
                2,
                "No thanks.",
                3,
                title = "What would you like to say?",
            )
        when (choice) {
            1 -> {
                chatPlayer(quiz, "Why do you charge for returning lost items?")
                chatNpc(
                    neutral,
                    "You could get your stuff back elsewhere, sure. Now, maybe it'd be free, " +
                        "or maybe you'd have to pay someone, but you'd definitely have to go " +
                        "on a journey to get it back.",
                )
                chatNpc(
                    happy,
                    "So I'm earning my fee by saving you a trip. Do you want to have a look?",
                )
            }
            2 -> {
                chatPlayer(quiz, "About items that break when people kill me...")
                chatNpc(
                    neutral,
                    "Oh yes, some items do break specially when you're killed by your " +
                        "rivals. And if you die in the deep Wilderness, they vanish altogether.",
                )
            }
            3 -> chatPlayer(neutral, "No thanks.")
        }
    }

    // Note: the scrying ball (Theatre of Blood spectating) is skipped.
    private suspend fun Dialogue.abigailaMenu() {
        val choice = choice2("Who are you and what are you doing?", 1, "I'll leave you be.", 2)
        if (choice == 2) {
            chatPlayer(neutral, "I'll leave you be.")
            return
        }
        chatPlayer(quiz, "Who are you and what are you doing?")
        chatNpc(
            sad,
            "I'm Abigaila, from Morytania. It's a terrible place, run by the vampyres. They " +
                "visit the towns and collect everyone's blood for food!",
        )
        chatNpc(
            sad,
            "I tried for years to escape, with my son, Suro, to make a better life for " +
                "ourselves in Misthalin. I scraped together enough money to bribe a boatman " +
                "to get us out.",
        )
        chatNpc(
            sad,
            "The journey was tough. Some men in pink hoods attacked our boat, shouting at us " +
                "to go back where we belonged, and to keep out of their country. In the " +
                "struggle, the boat overturned.",
        )
        chatNpc(
            sad,
            "It was dark, and I couldn't find Suro in the water. I swam as deep as I could, " +
                "until my lungs screamed, but I couldn't find him. He wasn't good at swimming.",
        )
        chatNpc(
            sad,
            "Eventually I got myself to the shore. The sun was rising, and I found Suro " +
                "there. On the shore, where the water had dumped his... body.",
        )
        chatNpc(sad, "<col=0000ff>*sob*</col>")
        chatNpc(
            neutral,
            "I've done alright in Misthalin, I guess. I know a bit of magic, and the local " +
                "gentry pay me well for my scrying skills. I'm good at turning a profit on " +
                "Grand Exchange trades too.",
        )
        chatNpc(
            neutral,
            "Anyway, I'm able to rent a room in Lumbridge, and to buy food. I'm not being a " +
                "burden on anyone, and there are no vampyres coming for my blood.",
        )
        chatNpc(happy, "I am free!")
        chatNpc(
            neutral,
            "But I haven't forgotten my people in the east. There's one vampyre there who " +
                "runs a place she calls the Theatre of Blood, challenging humans to face " +
                "monsters in her arena.",
        )
        chatNpc(
            sad,
            "My husband went there, trying to win freedom from the blood-gatherers by " +
                "surviving her arena, but he never returned. No-one does.",
        )
        chatNpc(
            neutral,
            "So now I pass my spare time scrying on the Theatre of Blood, watching other " +
                "adventurers there. You can have a look, if you like.",
        )
        abigailaMenu()
    }

    // Note: crop protection (Pay) and the farming shop (Trade) are skipped with farming.
    private suspend fun Dialogue.gardener() {
        val choice =
            choice2("Can you give me any farming advice?", 1, "I'll come back another time.", 2)
        if (choice == 2) {
            chatPlayer(neutral, "I'll come back another time.")
            return
        }
        chatPlayer(quiz, "Can you give me any farming advice?")
        chatNpc(neutral, access.random.pick(farmingAdvice))
        gardener()
    }

    // Note: "Yes please." opens the tool store, which is skipped with farming.
    private suspend fun Dialogue.leprechaunMenu() {
        val choice =
            choice3(
                "What can you store?",
                1,
                "What do you do with the tools you're storing?",
                2,
                "No thanks, I'll keep hold of my stuff.",
                3,
            )
        when (choice) {
            1 -> {
                chatPlayer(quiz, "What can you store?")
                chatNpc(
                    happy,
                    "We'll hold onto yer rake, yer seed dibber, yer spade, yer secateurs, yer " +
                        "waterin' can and yer trowel - but mind it's not one of them fancy " +
                        "trowels only archaeologists use!",
                )
                chatNpc(
                    happy,
                    "We'll take a few buckets off yer hands too, and even yer compost, " +
                        "supercompost an' ultracompost! Also plant cure vials.",
                )
                chatNpc(
                    happy,
                    "Aside from that, if ye hands me yer farming produce, I can mebbe change " +
                        "it into banknotes for ye.",
                )
                chatNpc(happy, "So... do ye want to be using the store?")
                leprechaunMenu()
            }
            2 -> {
                chatPlayer(
                    quiz,
                    "What do you do with the tools you're storing? They can't possibly all " +
                        "fit in your pockets!",
                )
                chatNpc(
                    happy,
                    "We leprechauns have a shed where we keep 'em. It's a magic shed, so ye " +
                        "can get yer items back from any of us leprechauns whenever ye want. " +
                        "Saves ye havin' to carry loads of stuff around the country!",
                )
                chatNpc(happy, "So... do ye want to be using the store?")
                leprechaunMenu()
            }
            3 -> {
                chatPlayer(neutral, "No thanks, I'll keep hold of my stuff.")
                chatNpc(
                    laugh,
                    "Ye must be dafter than ye look if ye likes luggin' yer tools everywhere " +
                        "ye goes!",
                )
            }
        }
    }

    private companion object {
        val farmingAdvice =
            listOf(
                "The only way to cure a bush or tree of disease is to prune away the diseased " +
                    "leaves with a pair of secateurs. For all other crops I would just apply " +
                    "some plant-cure.",
                "Don't just throw away your weeds after you've raked a patch - put them in a " +
                    "compost bin and make some compost.",
                "If you want to grow fruit trees you could try a few places: Catherby, " +
                    "Brimhaven, Gnome Stronghold and the Farming Guild all have fruit tree " +
                    "patches.",
                "Applying compost to a patch will not only reduce the chance that your crops " +
                    "will get diseased, but you will also grow more crops to harvest.",
                "Vegetables, hops and flowers need constant watering - if you ignore my " +
                    "advice, you will sooner or later find yourself in possession of a dead " +
                    "farming patch.",
                "Tree seeds must be grown in a plantpot of soil into a sapling, and then " +
                    "transferred to a tree patch to continue growing to adulthood.",
            )
    }
}
