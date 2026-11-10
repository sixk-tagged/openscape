package org.rsmod.content.skills.slayer

import jakarta.inject.Inject
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.config.refs.varps
import org.rsmod.api.death.NpcKilled
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.statAdvance
import org.rsmod.api.player.vars.VarPlayerIntMapSetter
import org.rsmod.api.random.GameRandom
import org.rsmod.api.repo.npc.NpcRepository
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.script.onEvent
import org.rsmod.api.script.onOpHeld1
import org.rsmod.api.script.onOpHeld2
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.script.onOpNpc4
import org.rsmod.api.script.onOpNpc5
import org.rsmod.api.shops.Shops
import org.rsmod.api.type.builders.map.npc.MapNpcSpawnBuilder
import org.rsmod.content.skills.slayer.configs.SlayerInvs
import org.rsmod.content.skills.slayer.configs.slayer_npcs
import org.rsmod.content.skills.slayer.configs.slayer_objs
import org.rsmod.content.skills.slayer.configs.slayer_varbits
import org.rsmod.content.skills.slayer.configs.slayer_varps
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Player
import org.rsmod.game.type.npc.NpcType
import org.rsmod.game.type.npc.NpcTypeList
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Slayer: the masters' assignments (Talk-to, Assignment, Trade, Rewards), kill credit and task
 * completion (reward points and streaks), and the enchanted gem. Task lists come from the cache's
 * `slayer_master_task` table; which npcs count comes from their `slayer_category` param.
 */
class SlayerScript
@Inject
internal constructor(
    private val tasks: SlayerTasks,
    private val shops: Shops,
    private val objRepo: ObjRepository,
    npcRepo: NpcRepository,
    npcTypes: NpcTypeList,
    random: GameRandom,
) : PluginScript() {
    private val superiors = Superiors(npcRepo, npcTypes, random)

    override fun ScriptContext.startup() {
        tasks.load()
        for ((npc, master) in MASTERS) {
            onOpNpc1(npc) { startDialogue(it.npc) { talk(it.npc, master) } }
            onOpNpc3(npc) { startDialogue(it.npc) { assignment(master) } }
            onOpNpc4(npc) { openShop(it.npc) }
            onOpNpc5(npc) { openSlayerRewards(master) }
        }
        onOpHeld1(slayer_objs.enchanted_gem) { checkSlayerTask(tasks) }
        onOpHeld2(slayer_objs.enchanted_gem) { checkSlayerTask(tasks) }
        onEvent<NpcKilled> { credit(hero, npc) }
    }

    // ---- Masters

    private suspend fun Dialogue.talk(npc: Npc, master: SlayerMaster) {
        if (master == SlayerMaster.Krystilia) {
            chatNpc(neutral, "Yeah? What do you want?")
        } else {
            chatNpc(neutral, "'Ello, and what are you after then?")
        }
        val choice =
            choice4(
                "I need another assignment.",
                1,
                "Have you any rewards for me, or anything to trade?",
                2,
                "Let's talk about the difficulty of my assignments.",
                3,
                "Er... nothing...",
                4,
            )
        when (choice) {
            1 -> {
                chatPlayer(neutral, "I need another assignment.")
                assignment(master)
            }
            2 -> trade(npc, master)
            3 -> difficulty()
            else -> chatPlayer(neutral, "Er... nothing...")
        }
    }

    private suspend fun Dialogue.assignment(master: SlayerMaster) {
        val count = vars[varps.slayer_count]
        if (count > 0) {
            current(master, count)
            return
        }
        if (player.combatLevel < master.combat) {
            // UNVERIFIED: the wording; the levels are the wiki's.
            chatNpc(
                neutral,
                "You're not yet strong enough to take tasks from me. Come back when you have a " +
                    "combat level of ${master.combat}.",
            )
            return
        }
        if (master == SlayerMaster.Krystilia && vars[slayer_varbits.wilderness_streak] == 0) {
            if (!krystiliaIntro()) {
                return
            }
        }
        assign(master)
    }

    private suspend fun Dialogue.current(master: SlayerMaster, count: Int) {
        val name = taskName()
        val fromKrystilia = vars[varbits.slayer_master] == SlayerMaster.Krystilia.id
        if (master == SlayerMaster.Turael && !tasks.gives(master, vars[varps.slayer_target])) {
            turaelReset(name, count)
            return
        }
        when {
            master == SlayerMaster.Krystilia && fromKrystilia ->
                chatNpc(
                    neutral,
                    "You're still meant to be slaying $name in the Wilderness; you have $count to " +
                        "go. Come back when you've finished your task.",
                )
            master == SlayerMaster.Turael || master == SlayerMaster.Spria ->
                chatNpc(neutral, "You're still hunting $name, you have $count to go.")
            else ->
                chatNpc(
                    neutral,
                    "You're still hunting $name; you have $count to go. Come back when you've " +
                        "finished your task.",
                )
        }
    }

    private suspend fun Dialogue.turaelReset(name: String, count: Int) {
        chatNpc(neutral, "You're still hunting $name, you have $count to go.")
        chatNpc(
            neutral,
            "Although, it's not an assignment that I'd normally give... I guess I could give you " +
                "a new assignment, if you'd like.",
        )
        // A Krystilia task resets the Wilderness streak instead (wiki: Krystilia).
        // UNVERIFIED: the wording of the Wilderness version.
        val wilderness = vars[varbits.slayer_master] == SlayerMaster.Krystilia.id
        val reset = if (wilderness) slayer_varbits.wilderness_streak else slayer_varbits.streak
        val kept = if (wilderness) slayer_varbits.streak else slayer_varbits.wilderness_streak
        val resetName = if (wilderness) "Wilderness" else "standard"
        val keptName = if (wilderness) "standard" else "Wilderness"
        chatNpc(
            neutral,
            "If you do get a new one, you will reset your $resetName task streak of " +
                "${vars[reset]}. Is that okay? It won't affect your $keptName task streak of " +
                "${vars[kept]}.",
        )
        val choice = choice2("Yes, please.", 1, "No, thanks.", 2)
        if (choice != 1) {
            chatPlayer(neutral, "No, thanks.")
            return
        }
        chatPlayer(neutral, "Yes, please.")
        vars[reset] = 0
        vars[varps.slayer_count] = 0
        assign(SlayerMaster.Turael)
    }

    private suspend fun Dialogue.krystiliaIntro(): Boolean {
        chatNpc(
            neutral,
            "Before I assign you anything, I want to make something clear. My tasks have to be " +
                "done in the Wilderness. Only kills inside the Wilderness will count.",
        )
        chatNpc(
            neutral,
            "I don't check combat levels when choosing tasks, either. So even if other masters " +
                "won't assign you 'tough' monsters, I'll pick anything for which you have the " +
                "Slayer level, provided you can physically get to it.",
        )
        chatNpc(
            neutral,
            "If you don't like my tasks, go and see Turael in Burthorpe, and ask him to assign " +
                "you something else. He won't argue.",
        )
        chatNpc(
            neutral,
            "You might get a few unusual loot drops while you're on my assignments, if you're " +
                "lucky. So, do you want me to assign you something?",
        )
        val choice =
            choice2(
                "Yes, I understand I must kill it in the Wilderness.",
                1,
                "No thanks, I don't want tasks from you.",
                2,
            )
        if (choice != 1) {
            chatPlayer(neutral, "No thanks, I don't want tasks from you.")
            return false
        }
        chatPlayer(neutral, "Yes, I understand I must kill it in the Wilderness.")
        return true
    }

    private suspend fun Dialogue.assign(master: SlayerMaster) {
        val pick = tasks.pick(player, master)
        if (pick == null) {
            // UNVERIFIED: no OSRS line; only reachable with every task blocked or locked.
            chatNpc(neutral, "I don't have anything suitable for you right now.")
            return
        }
        val (task, amount) = pick
        val first = vars[varps.slayer_target] == 0
        vars[varbits.slayer_master] = master.id
        vars[varps.slayer_target] = task.id
        vars[varps.slayer_count] = amount
        vars[slayer_varps.count_original] = amount
        if (master == SlayerMaster.Krystilia) {
            chatNpc(neutral, "Your new task is to kill $amount ${task.name}.")
        } else {
            chatNpc(
                neutral,
                "Excellent, you're doing great. Your new task is to kill $amount ${task.name}.",
            )
        }
        if (first) {
            giveGem()
        }
        chatPlayer(neutral, "Okay, great!")
        chatNpc(neutral, "Good luck! Don't forget to come back when you need a new assignment.")
    }

    private suspend fun Dialogue.giveGem() {
        chatNpc(
            neutral,
            "You'll also need this enchanted gem, it allows Slayer Masters like myself to contact " +
                "you and update you on your progress. Don't worry if you lose it, you can buy " +
                "another from any Slayer Master.",
        )
        access.invAddOrDrop(objRepo, slayer_objs.enchanted_gem)
    }

    private suspend fun Dialogue.trade(npc: Npc, master: SlayerMaster) {
        chatPlayer(neutral, "Have you any rewards for me, or anything to trade?")
        chatNpc(
            neutral,
            "I have quite a few rewards you can earn, and a wide variety of Slayer equipment for " +
                "sale.",
        )
        when (choice3("Look at rewards.", 1, "Look at shop.", 2, "Cancel.", 3)) {
            1 -> access.openSlayerRewards(master)
            2 -> shops.open(player, npc, SHOP_TITLE, SlayerInvs.equipment)
        }
    }

    private suspend fun Dialogue.difficulty() {
        chatPlayer(neutral, "Let's talk about the difficulty of my assignments.")
        if (vars[slayer_varps.any_combat] == 1) {
            chatNpc(
                neutral,
                "The Slayer Masters may currently assign you any task in our lists, regardless of " +
                    "your combat level.",
            )
            val choice =
                choice2(
                    "That's fine - I can handle any task.",
                    1,
                    "In future, please don't give anything too tough.",
                    2,
                )
            if (choice == 1) {
                chatPlayer(neutral, "That's fine - I can handle any task.")
                chatNpc(neutral, "That's the spirit.")
                return
            }
            chatPlayer(neutral, "In future, please don't give anything too tough.")
            vars[slayer_varps.any_combat] = 0
            chatNpc(
                neutral,
                "Okay, from now on, all the Slayer Masters will take your combat level into " +
                    "account when choosing tasks for you, so you shouldn't get anything too hard.",
            )
            return
        }
        chatNpc(
            neutral,
            "The Slayer Masters will take your combat level into account when choosing tasks for " +
                "you, so you shouldn't get anything too hard.",
        )
        val choice =
            choice2(
                "That's fine - I don't want anything too tough.",
                1,
                "Stop checking my combat level - I can take anything!",
                2,
            )
        if (choice == 1) {
            chatPlayer(neutral, "That's fine - I don't want anything too tough.")
            chatNpc(neutral, "Okay, we'll keep checking your combat level.")
            return
        }
        chatPlayer(neutral, "Stop checking my combat level - I can take anything!")
        vars[slayer_varps.any_combat] = 1
        chatNpc(
            neutral,
            "Okay, from now on, all the Slayer Masters will assign you anything from their lists, " +
                "regardless of your combat level.",
        )
    }

    private fun ProtectedAccess.openShop(npc: Npc) {
        shops.open(player, npc, SHOP_TITLE, SlayerInvs.equipment)
    }

    private fun Dialogue.taskName(): String =
        tasks.byId[vars[varps.slayer_target]]?.name ?: "monsters"

    // ---- Kill credit

    private fun credit(player: Player, npc: Npc) {
        val superior = superiors.isSuperior(npc)
        if (superior) {
            superiors.killed(player)
            player.statAdvance(stats.slayer, superiors.xp(npc))
        }
        if (!player.countsForTask(npc)) {
            return
        }
        if (!superior) {
            player.statAdvance(stats.slayer, npc.slayerXp())
            superiors.roll(player, npc)
        }
        val left = player.vars[varps.slayer_count] - 1
        VarPlayerIntMapSetter.set(player, varps.slayer_count, left)
        if (left == 0) {
            complete(player)
        }
    }

    private fun complete(player: Player) {
        val master =
            SlayerMaster.entries.firstOrNull { it.id == player.vars[varbits.slayer_master] }
        val wilderness = master == SlayerMaster.Krystilia
        val streakVar = if (wilderness) slayer_varbits.wilderness_streak else slayer_varbits.streak
        val streak = player.vars[streakVar] + 1
        VarPlayerIntMapSetter.set(player, streakVar, streak)
        val points = taskPoints(master?.points ?: 0, streak)
        val total = minOf(MAX_POINTS, player.vars[slayer_varbits.points] + points)
        VarPlayerIntMapSetter.set(player, slayer_varbits.points, total)
        val name = tasks.byId[player.vars[varps.slayer_target]]?.name ?: "monsters"
        // UNVERIFIED: both lines are from memory of the game, not a transcript.
        player.mes(
            "You have completed your task! You killed ${player.vars[slayer_varps.count_original]} $name."
        )
        val kind = if (wilderness) "Wilderness tasks" else "tasks"
        if (points > 0) {
            player.mes(
                "You've completed $streak $kind and received $points points, giving you a total " +
                    "of $total; return to a Slayer master."
            )
        } else {
            player.mes("You've completed $streak $kind; return to a Slayer master.")
        }
    }

    private companion object {
        const val SHOP_TITLE = "Slayer Equipment"
        const val MAX_POINTS = 64_000

        val MASTERS: List<Pair<NpcType, SlayerMaster>> =
            listOf(
                slayer_npcs.turael to SlayerMaster.Turael,
                slayer_npcs.mazchna to SlayerMaster.Mazchna,
                slayer_npcs.vannaka to SlayerMaster.Vannaka,
                slayer_npcs.krystilia to SlayerMaster.Krystilia,
                slayer_npcs.spria to SlayerMaster.Spria,
            )
    }
}

/**
 * Reward points for a finished task (wiki "Slayer reward point"): none for the first four of a
 * streak, then the master's base, times 5 / 15 / 25 / 35 / 50 on every 10th / 50th / 100th / 250th
 * / 1,000th.
 */
fun taskPoints(base: Int, streak: Int): Int =
    when {
        streak < 5 -> 0
        streak % 1000 == 0 -> base * 50
        streak % 250 == 0 -> base * 35
        streak % 100 == 0 -> base * 25
        streak % 50 == 0 -> base * 15
        streak % 10 == 0 -> base * 5
        else -> base
    }

object SlayerNpcSpawns : MapNpcSpawnBuilder() {
    override fun onPackMapTask() {
        resourceFile<SlayerScript>("npcs.toml")
    }
}
