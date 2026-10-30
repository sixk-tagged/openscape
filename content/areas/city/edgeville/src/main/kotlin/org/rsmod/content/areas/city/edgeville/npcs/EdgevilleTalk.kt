package org.rsmod.content.areas.city.edgeville.npcs

import org.rsmod.api.combat.commons.npc.queueCombatRetaliate
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.stat.statBase
import org.rsmod.api.script.onOpNpc1
import org.rsmod.content.areas.city.edgeville.configs.edgeville_npcs
import org.rsmod.game.entity.Npc
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Out-of-quest Talk-to lines: the barbarians and Goblin Village goblins that start a fight, the
 * Oracle's jokes, the canoe tutors and the villagers with a line or two to say.
 */
class EdgevilleTalk : PluginScript() {
    override fun ScriptContext.startup() {
        for (type in edgeville_npcs.barbarians + edgeville_npcs.gunthor) {
            onOpNpc1(type) {
                val npc = it.npc
                startDialogue(npc) { barkThenFight(npc, BARBARIAN_LINES) }
            }
        }
        for (type in edgeville_npcs.red_goblins + edgeville_npcs.green_goblins) {
            onOpNpc1(type) {
                val npc = it.npc
                startDialogue(npc) { goblinTalk(npc) }
            }
        }

        onOpNpc1(edgeville_npcs.oracle) {
            startDialogue(it.npc) {
                chatPlayer(quiz, "Can you impart your wise knowledge to me, O Oracle?")
                chatNpc(neutral, access.random.pick(ORACLE_LINES))
            }
        }
        onOpNpc1(edgeville_npcs.hunding) { startDialogue(it.npc) { hunding() } }
        onOpNpc1(edgeville_npcs.tassie) {
            startDialogue(it.npc) {
                chatNpc(
                    happy,
                    "Please feel free to use the pottery wheel, I won't be using it all the " +
                        "time. Put your pots in the kiln when you've made one.",
                )
                chatNpc(neutral, "And make sure you tidy up after yourself!")
            }
        }
        onOpNpc1(edgeville_npcs.hari) { startDialogue(it.npc) { canoeTutor() } }
        onOpNpc1(edgeville_npcs.sigurd) { startDialogue(it.npc) { canoeTutor() } }
        onOpNpc1(edgeville_npcs.bentnoze) {
            startDialogue(it.npc) { chatNpc(angry, access.random.pick(BENTNOZE_LINES)) }
        }
        onOpNpc1(edgeville_npcs.wartface) {
            startDialogue(it.npc) { chatNpc(angry, access.random.pick(WARTFACE_LINES)) }
        }
    }

    /** One line, and then the barbarian fights. Walking away before the line ends avoids it. */
    private suspend fun Dialogue.barkThenFight(npc: Npc, lines: List<String>) {
        chatNpc(angry, access.random.pick(lines))
        npc.queueCombatRetaliate(player)
    }

    private suspend fun Dialogue.goblinTalk(npc: Npc) {
        when (access.random.of(GOBLIN_GREETINGS)) {
            0 -> barkThenFight(npc, listOf("I kill you human!"))
            1 -> barkThenFight(npc, listOf("Go away smelly human!"))
            else -> {
                chatNpc(angry, "What you doing here?")
                val choice =
                    choice2("I'm here to kill all you goblins!", 1, "I'm just looking around.", 2)
                if (choice == 1) {
                    chatPlayer(angry, "I'm here to kill all you goblins!")
                    npc.queueCombatRetaliate(player)
                } else {
                    chatPlayer(neutral, "I'm just looking around.")
                    chatNpc(neutral, "Me not sure that allowed. You have to check with generals.")
                }
            }
        }
    }

    // Note: the wiki gives only the openers and the replies' gist; the history speech is short.
    private suspend fun Dialogue.hunding() {
        chatNpc(angry, "What are you doing in our village, outlander?")
        val choice =
            choice3(
                "Nothing much.",
                1,
                "I'm exploring.",
                2,
                "You look like a load of primitive savages.",
                3,
            )
        when (choice) {
            1 -> {
                chatPlayer(neutral, "Nothing much.")
                chatNpc(
                    neutral,
                    "Go down to the longhall and speak to our chieftain. Gunthor the Brave will " +
                        "give you a rousing welcome!",
                )
            }
            2 -> {
                chatPlayer(happy, "I'm exploring.")
                chatNpc(
                    neutral,
                    "Then explore. Ours is an old tribe, led by Gunthor the Brave. Do not wake " +
                        "the warriors in the longhall.",
                )
            }
            3 -> {
                chatPlayer(laugh, "You look like a load of primitive savages.")
                chatNpc(
                    angry,
                    "And you look like an arrogant fool. And you smell like a raccoon's bottom.",
                )
            }
        }
    }

    // Note: the tutors' wording is unverified; they only name the best canoe the player can
    // make.
    private suspend fun Dialogue.canoeTutor() {
        chatNpc(happy, "Hello there! I can tell you which canoe you could make.")
        val level = player.statBase(stats.woodcutting)
        val canoe = CANOES.lastOrNull { level >= it.first }
        if (canoe == null) {
            chatNpc(neutral, "You need a Woodcutting level of at least 12 to make a canoe.")
        } else {
            chatNpc(
                neutral,
                "With your Woodcutting level of $level, you can make ${canoe.second} canoes.",
            )
        }
    }

    private companion object {
        const val GOBLIN_GREETINGS = 3

        val BARBARIAN_LINES =
            listOf(
                "Wanna fight?",
                "Ah, you come for fight, ja?",
                "You look funny.",
                "Grrr!",
                "What you want?",
                "Go Away!",
            )

        val CANOES = listOf(12 to "log", 27 to "dugout", 42 to "stable dugout", 57 to "waka")

        val BENTNOZE_LINES =
            listOf(
                "When Chosen Commander come to speak to us again?",
                "Me not like worm stew! Tell him make beetle pie!",
                "Shut up Wartface!",
            )

        val WARTFACE_LINES =
            listOf(
                "Me tell Mudknuckles make worm stew for dinner.",
                "Me not know why Bentnoze so grumpy.",
                "Shut up Bentnoze!",
            )

        val ORACLE_LINES =
            listOf(
                "Capes are always in fashion!",
                "The light at the end of the tunnel is the demon-infested lava pit.",
                "Beware the cabbage: it is both green AND leafy.",
                "They say that ham does not mix well with other kinds of meat.",
                "A woodchuck does not chuck wood.",
                "No. I'm not in the mood.",
                "It's not you; it's me.",
                "Too many cooks spoil the anchovy pizza.",
                "The God Wars are over...as long as the thing they were fighting over remains " +
                    "hidden.",
                "He who uses the power of custard mixes it with his tears.",
                "Nothing like a tasty fish.",
                "Don't judge a book by its cover - judge it on its' grammar and, punctuation.",
                "Yes, I can. But I'm not going to.",
                "A bird in the hand can make a tasty snack.",
                "Pies...they're great, aren't they?",
                "Who guards the guardsmen?",
                "The great snake of Guthix guards more than she knows.",
                "Do not fear the dragons...fear their kin.",
                "Everyone you know will one day be dead.",
                "If a tree falls in the forest and no one is around, then nobody gets " +
                    "Woodcutting xp.",
                "Sometimes you get lucky, sometimes you don't.",
                "The chicken came before the egg.",
                "Is it time to wake up? I am not sure...",
                "When in Asgarnia, do as the Asgarnians do.",
                "An answer is unimportant; it is the question that matters.",
                "The goblins will never make up their minds on their own.",
                "Jas left a stone behind.",
                "Many secrets are buried under this land.",
                "There are no crisps at the party.",
                "Help wanted? Enquire within.",
            )
    }
}
