package org.rsmod.content.skills.slayer

import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.config.refs.varps
import org.rsmod.api.player.stat.statBase
import org.rsmod.api.random.GameRandom
import org.rsmod.content.skills.slayer.configs.SlayerColumns
import org.rsmod.content.skills.slayer.configs.SlayerTables
import org.rsmod.content.skills.slayer.configs.slayer_varps
import org.rsmod.game.dbtable.DbTableResolver
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Player
import org.rsmod.game.stat.StatRequirement

/** A row of the cache's `slayer_task` table: [id] is what varp `slayer_target` holds. */
internal data class SlayerTask(
    val id: Int,
    val name: String,
    val combat: Int,
    val stats: List<StatRequirement>,
    /** At least one of these, where there are any (60 Strength or 60 Agility). */
    val anyStats: List<StatRequirement> = emptyList(),
    val extension: TaskExtension? = null,
)

/** The longer kill range a task gets once unlock [bit] is bought ("Augment my abbies", ...). */
internal data class TaskExtension(val bit: Int, val min: Int, val max: Int)

/** A row of `slayer_master_task`: one master's weight and kill range for a task. */
internal data class MasterTask(
    val task: SlayerTask,
    val weight: Int,
    val min: Int,
    val max: Int,
    val unlockBit: Int?,
)

/**
 * The Slayer masters by their `slayer_master_task:master_id` (varbit `slayer_master`); block costs
 * are the client script `script823`'s.
 */
internal enum class SlayerMaster(
    val id: Int,
    val combat: Int,
    val points: Int,
    val blockCost: Int,
) {
    Turael(1, 0, 0, 40),
    Mazchna(2, 20, 6, 50),
    Vannaka(3, 40, 8, 60),
    Nieve(6, 85, 12, 90),
    Krystilia(7, 0, 25, 100),
    Spria(9, 0, 0, 40);

    companion object {
        fun byId(id: Int): SlayerMaster? = entries.firstOrNull { it.id == id }
    }
}

/**
 * The cache's Slayer task tables, read once at startup, and the rules that pick from them: weights,
 * kill ranges, Slayer level / stat / combat requirements (quests count as done) and the
 * reward-point unlocks some tasks need.
 */
@Singleton
internal class SlayerTasks
@Inject
constructor(private val dbTables: DbTableResolver, private val random: GameRandom) {
    lateinit var byId: Map<Int, SlayerTask>
        private set

    private lateinit var lists: Map<Int, List<MasterTask>>

    fun load() {
        val unlockBits =
            dbTables[SlayerTables.unlock].associate { it.type.id to it[SlayerColumns.unlock_bit] }
        val tasksByRow =
            dbTables[SlayerTables.task]
                .filter { it[SlayerColumns.task_id] > 0 }
                .associate { row ->
                    row.type.id to
                        SlayerTask(
                            id = row[SlayerColumns.task_id],
                            name = row[SlayerColumns.task_name],
                            combat = row.getOrNull(SlayerColumns.task_combat) ?: 0,
                            stats = row.getOrNull(SlayerColumns.task_stats) ?: emptyList(),
                            anyStats = row.getOrNull(SlayerColumns.task_stats_any) ?: emptyList(),
                            extension =
                                row.getOrNull(SlayerColumns.task_extension)?.let {
                                    (unlock, min, max) ->
                                    unlockBits[unlock]?.let { TaskExtension(it, min, max) }
                                },
                        )
                }
        byId = tasksByRow.values.associateBy { it.id }
        lists =
            dbTables[SlayerTables.master_task]
                .mapNotNull { row ->
                    // Boss tasks (amount -1, chosen by the player) aren't built.
                    if (row[SlayerColumns.master_max] <= 0) {
                        return@mapNotNull null
                    }
                    val task =
                        tasksByRow[row[SlayerColumns.master_task].id] ?: return@mapNotNull null
                    val unlock = row.getOrNull(SlayerColumns.master_unlock)?.id
                    row[SlayerColumns.master_id] to
                        MasterTask(
                            task = task,
                            weight = row[SlayerColumns.master_weight],
                            min = row[SlayerColumns.master_min],
                            max = row[SlayerColumns.master_max],
                            unlockBit = unlock?.let { unlockBits[it] },
                        )
                }
                .groupBy({ it.first }, { it.second })
    }

    fun list(master: SlayerMaster): List<MasterTask> = lists[master.id].orEmpty()

    fun gives(master: SlayerMaster, task: Int): Boolean = list(master).any { it.task.id == task }

    /** A weighted pick among the tasks [player] can be given, or null if there are none. */
    fun pick(player: Player, master: SlayerMaster): Pair<SlayerTask, Int>? {
        val options = list(master).filter { eligible(player, master, it) }
        val total = options.sumOf { it.weight }
        if (total == 0) {
            return null
        }
        var roll = random.of(total)
        val chosen =
            options.first { option -> (roll < option.weight).also { roll -= option.weight } }
        val extension = chosen.task.extension?.takeIf { player.hasLongerTask(it.bit) }
        val amount =
            if (extension != null) {
                random.of(extension.min, extension.max)
            } else {
                random.of(chosen.min, chosen.max)
            }
        return chosen.task to amount
    }

    private fun Player.hasLongerTask(bit: Int): Boolean {
        if (!hasSlayerUnlock(bit)) {
            return false
        }
        val toggle = SlayerToggles.byBit[bit] ?: return true
        return vars[toggle] == 0
    }

    private fun eligible(player: Player, master: SlayerMaster, option: MasterTask): Boolean {
        val task = option.task
        if (option.unlockBit != null && !player.hasSlayerUnlock(option.unlockBit)) {
            return false
        }
        if (task.id in blockedTasks(player, master)) {
            return false
        }
        // Task requirements use base levels: boosts never get a harder task.
        if (task.stats.any { player.statBase(it.stat) < it.level }) {
            return false
        }
        if (
            task.anyStats.isNotEmpty() &&
                task.anyStats.none { player.statBase(it.stat) >= it.level }
        ) {
            return false
        }
        val checksCombat =
            master != SlayerMaster.Krystilia && player.vars[slayer_varps.any_combat] == 0
        return !checksCombat || player.combatLevel >= task.combat
    }
}

/** Bit [bit] of `slayer_unlock:bit`, spread over varps `slayer_rewards_unlocks` and `...1`. */
internal fun Player.hasSlayerUnlock(bit: Int): Boolean {
    val varp = if (bit < 32) slayer_varps.unlocks else slayer_varps.unlocks1
    return ((vars[varp] shr (bit % 32)) and 1) == 1
}

/** The task id [npc] counts for (the cache's `slayer_category`), or 0 or less for none. */
internal fun Npc.slayerTask(): Int =
    visType.param(params.slayer_category).takeIf { it > 0 } ?: type.param(params.slayer_category)

/** Whether a kill of [npc] counts for [player]'s current task. */
internal fun Player.countsForTask(npc: Npc): Boolean {
    if (vars[varps.slayer_count] <= 0) {
        return false
    }
    val task = npc.slayerTask()
    if (task <= 0 || task != vars[varps.slayer_target]) {
        return false
    }
    val krystilia = vars[varbits.slayer_master] == SlayerMaster.Krystilia.id
    return !krystilia || vars[varbits.inside_wilderness] == 1
}

/** Slayer xp for a kill: the npc's `slayer_experience` (xp x10) where set, else its hitpoints. */
internal fun Npc.slayerXp(): Double {
    val special = visType.param(params.slayer_experience)
    return if (special > 0) special / 10.0 else visType.hitpoints.toDouble()
}
