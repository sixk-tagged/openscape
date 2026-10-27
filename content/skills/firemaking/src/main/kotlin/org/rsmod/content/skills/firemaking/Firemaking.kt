package org.rsmod.content.skills.firemaking

import jakarta.inject.Inject
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.firemakingLvl
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.script.onOpHeldU
import org.rsmod.api.script.onOpObj4
import org.rsmod.api.script.onPlayerQueueWithArgs
import org.rsmod.api.stats.levelmod.InvisibleLevels
import org.rsmod.api.stats.xpmod.XpModifiers
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.queue.QueueReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.game.MapClock
import org.rsmod.game.loc.LocAngle
import org.rsmod.game.loc.LocShape
import org.rsmod.game.obj.Obj
import org.rsmod.game.type.obj.ObjType
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext
import org.rsmod.routefinder.StepValidator
import org.rsmod.routefinder.collision.CollisionFlagMap
import org.rsmod.routefinder.flag.CollisionFlag

class Firemaking
@Inject
constructor(
    private val objRepo: ObjRepository,
    private val locRepo: LocRepository,
    private val collision: CollisionFlagMap,
    private val clock: MapClock,
    private val xpMods: XpModifiers,
    private val invisibleLvls: InvisibleLevels,
) : PluginScript() {
    private val steps = StepValidator(collision)

    override fun ScriptContext.startup() {
        for (log in FiremakingLogs.all) {
            onOpHeldU(objs.tinderbox, log.obj) { lightHeld(log, it.secondSlot) }
            onOpObj4(log.obj) { lightGround(log, it.obj) }
        }
        onPlayerQueueWithArgs<LightTask>(FiremakingQueues.light) { attempt(it.args) }
    }

    /** OSRS drops the log on the player's tile and lights it there. */
    private fun ProtectedAccess.lightHeld(log: FiremakingLogs.Log, slot: Int) {
        if (!canLight(log, player.coords)) {
            return
        }
        if (!invDel(inv, log.obj, count = 1, slot = slot).success) {
            return
        }
        val ground = objRepo.add(log.obj, player.coords, constants.lootdrop_duration, player)
        start(LightTask(log, ground), queueDelay(ATTEMPT_TICKS))
    }

    private fun ProtectedAccess.lightGround(log: FiremakingLogs.Log, ground: Obj) {
        if (objs.tinderbox !in inv) {
            mes("You need a tinderbox to light a fire.")
            return
        }
        // An obj op runs after the queues, so this first attempt needs no extra tick.
        if (canLight(log, ground.coords)) {
            start(LightTask(log, ground), ATTEMPT_TICKS)
        }
    }

    private fun ProtectedAccess.start(task: LightTask, delay: Int) {
        mes("You attempt to light the logs.")
        anim(FiremakingSeqs.light)
        weakQueue(FiremakingQueues.light, delay, task)
    }

    private fun ProtectedAccess.attempt(task: LightTask) {
        val ground = task.ground
        if (objRepo.findAll(ground.coords).none { it == ground }) {
            return
        }
        if (!statRandom(stats.firemaking, LIGHT_LOW, LIGHT_HIGH, invisibleLvls)) {
            anim(FiremakingSeqs.light)
            weakQueue(FiremakingQueues.light, queueDelay(ATTEMPT_TICKS), task)
            return
        }
        objRepo.del(ground)
        val burnTicks = random.of(FIRE_MIN_TICKS, FIRE_MAX_TICKS)
        val fire = FiremakingLocs.fire
        locRepo.add(ground.coords, fire, burnTicks, LocAngle.West, LocShape.CentrepieceStraight)
        val ashes = Obj.fromServer(clock, ground.coords, FiremakingObjs.ashes, 1)
        objRepo.addDelayed(ashes, burnTicks, constants.lootdrop_duration)
        statAdvance(stats.firemaking, task.log.xp * xpMods.get(player, stats.firemaking))
        spam("The fire catches and the logs begin to burn.")
        stepAway()
    }

    /** Fires need empty ground: no loc, ground decoration or water on the tile. */
    private fun ProtectedAccess.canLight(log: FiremakingLogs.Log, coords: CoordGrid): Boolean {
        if (player.firemakingLvl < log.level) {
            val name = ocName(log.obj).lowercase()
            mes("You need a Firemaking level of ${log.level} to burn $name.")
            return false
        }
        val blockers = CollisionFlag.LOC or CollisionFlag.GROUND_DECOR or CollisionFlag.BLOCK_WALK
        val blocked = collision[coords.x, coords.z, coords.level] and blockers != 0
        // Fires and flowers don't block walking, so check the tile's own locs too.
        if (blocked || TILE_SHAPES.any { locRepo.findExact(coords, it) != null }) {
            mes("You can't light a fire here.")
            return false
        }
        return true
    }

    /** After lighting, OSRS steps west, else east, else south, else north. */
    private fun ProtectedAccess.stepAway() {
        val from = player.coords
        val step =
            STEP_ORDER.firstOrNull { (dx, dz) ->
                steps.canTravel(from.level, from.x, from.z, dx, dz)
            }
        if (step != null) {
            walk(from.translate(step.first, step.second))
        }
    }

    private data class LightTask(val log: FiremakingLogs.Log, val ground: Obj)

    private companion object {
        const val ATTEMPT_TICKS = 4

        // Note: classic-engine light chance for every log type; the OSRS wiki has no chart.
        const val LIGHT_LOW = 65
        const val LIGHT_HIGH = 513

        // Fires last 60-119 seconds (Mod Ash, OSRS wiki "Fire").
        const val FIRE_MIN_TICKS = 100
        const val FIRE_MAX_TICKS = 198

        val STEP_ORDER = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)

        val TILE_SHAPES =
            listOf(LocShape.CentrepieceStraight, LocShape.CentrepieceDiagonal, LocShape.GroundDecor)

        /** A weak queue added while handling input fires one tick early, so ask for one more. */
        fun queueDelay(ticks: Int): Int = ticks + 1
    }
}

object FiremakingLogs {
    data class Log(val obj: ObjType, val level: Int, val xp: Double)

    val all =
        with(objs) {
            listOf(
                Log(logs, 1, 40.0),
                Log(achey_tree_logs, 1, 40.0),
                Log(oak_logs, 15, 60.0),
                Log(willow_logs, 30, 90.0),
                Log(teak_logs, 35, 105.0),
                Log(arctic_pine_logs, 42, 125.0),
                Log(maple_logs, 45, 135.0),
                Log(mahogany_logs, 50, 157.5),
                Log(yew_logs, 60, 202.5),
                Log(magic_logs, 75, 303.8),
                Log(redwood_logs, 90, 350.0),
            )
        }
}

object FiremakingObjs : ObjReferences() {
    val ashes = find("ashes")
}

object FiremakingLocs : LocReferences() {
    val fire = find("fire")
}

object FiremakingSeqs : SeqReferences() {
    val light = find("human_createfire")
}

object FiremakingQueues : QueueReferences() {
    val light = find("firemaking_light")
}
