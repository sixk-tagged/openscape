package org.rsmod.content.skills.agility

import jakarta.inject.Inject
import kotlin.math.max
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.agilityLvl
import org.rsmod.api.player.stat.baseAgilityLvl
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.script.onOpLoc1
import org.rsmod.content.skills.agility.configs.AgilityObjs
import org.rsmod.content.skills.agility.configs.AgilitySeqs
import org.rsmod.content.skills.agility.configs.AgilityVarps
import org.rsmod.game.hit.HitType
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.type.bas.UnpackedBasType
import org.rsmod.game.type.loc.LocType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Runs every obstacle of [Courses]: the level check, the moves, the chance to fall, xp, the lap
 * bonus and the marks of grace (OSRS wiki). A lap is only a lap if every obstacle was passed in
 * order; the progress is `(course id shl 8) or obstacles passed`.
 */
class CourseScript
@Inject
constructor(private val objRepo: ObjRepository, private val objTypes: ObjTypeList) :
    PluginScript() {
    private var ProtectedAccess.progress by intVarp(AgilityVarps.progress)
    private var ProtectedAccess.markMinute by intVarp(AgilityVarps.mark_minute)

    private val balanceBas =
        UnpackedBasType(
            readyAnim = AgilitySeqs.balance_ready,
            turnOnSpot = AgilitySeqs.balance_ready,
            walkForward = AgilitySeqs.balance_walk,
            walkBack = AgilitySeqs.balance_walk,
            walkLeft = AgilitySeqs.balance_walk,
            walkRight = AgilitySeqs.balance_walk,
            running = AgilitySeqs.balance_walk,
            internalId = null,
            internalName = null,
        )

    override fun ScriptContext.startup() {
        // A loc type can start several obstacles (the Barbarian walls): the placement tells which.
        val byType = LinkedHashMap<LocType, MutableList<Pair<Course, Int>>>()
        for (course in Courses.all) {
            for ((index, obstacle) in course.obstacles.withIndex()) {
                for (type in obstacle.locs) {
                    byType.getOrPut(type) { mutableListOf() }.add(course to index)
                }
            }
        }
        for ((type, entries) in byType) {
            onOpLoc1(type) {
                val (course, index) =
                    entries.firstOrNull { (c, i) -> it.loc.coords in c.obstacles[i].starts }
                        ?: entries.first()
                attempt(course, index, it.loc)
            }
        }
    }

    private suspend fun ProtectedAccess.attempt(course: Course, index: Int, loc: BoundLocInfo) {
        val obstacle = course.obstacles[index]
        if (obstacle.starts.isNotEmpty() && loc.coords !in obstacle.starts) {
            mes("You can't do that from here.")
            return
        }
        if (player.agilityLvl < obstacle.level) {
            mes("You need an Agility level of ${obstacle.level} to use this obstacle.")
            return
        }
        faceLoc(loc)
        arriveDelay()
        val inOrder = index == 0 || progress == pack(course, index)
        val fail = obstacle.fail
        if (fail != null && !passes(fail, obstacle.level)) {
            fall(fail, loc)
            return
        }
        for (step in obstacle.steps) {
            if (!perform(step, loc)) {
                return
            }
        }
        if (obstacle.xp > 0.0 && (!obstacle.lapOnly || inOrder)) {
            statAdvance(stats.agility, obstacle.xp)
        }
        if (obstacle.strengthXp > 0.0) {
            statAdvance(stats.strength, obstacle.strengthXp)
        }
        advance(course, index, inOrder)
    }

    private fun ProtectedAccess.advance(course: Course, index: Int, inOrder: Boolean) {
        val last = course.obstacles.size - 1
        if (!inOrder) {
            progress = 0
            return
        }
        if (index < last) {
            progress = pack(course, index + 1)
            return
        }
        progress = 0
        completeLap(course)
    }

    private fun ProtectedAccess.completeLap(course: Course) {
        if (course.bonusXp > 0.0) {
            statAdvance(stats.agility, course.bonusXp)
        }
        if (course.bonusStrengthXp > 0.0) {
            statAdvance(stats.strength, course.bonusStrengthXp)
        }
        if (course.tickets) {
            invAddOrDrop(objRepo, AgilityObjs.wilderness_ticket)
            dispenserLap(objTypes, objRepo)
        }
        rollMark(course)
    }

    /**
     * A lap done after the cooldown rolls for a mark of grace on one of the course's tiles. The
     * chance is 20% of normal once the unboosted level is 20 over the course's.
     */
    private fun ProtectedAccess.rollMark(course: Course) {
        if (course.markTiles.isEmpty()) {
            return
        }
        val minute = (System.currentTimeMillis() / MILLIS_PER_MINUTE).toInt()
        if (minute < markMinute) {
            return
        }
        val penalty = course.markPenalty && player.baseAgilityLvl >= course.level + 20
        val range = if (penalty) course.markDen * PENALTY_FACTOR else course.markDen
        if (random.of(range) >= course.markNum) {
            return
        }
        val tile = random.pick(course.markTiles)
        objRepo.add(AgilityObjs.mark_of_grace, tile, MARK_TICKS, player, reveal = MARK_TICKS)
        markMinute = minute + COOLDOWN_MINUTES
    }

    /** The chance of passing rises linearly from [Fail.base]/256 to certain at the stop level. */
    private fun ProtectedAccess.passes(fail: Fail, level: Int): Boolean {
        val current = player.agilityLvl
        if (current >= fail.stopLevel) {
            return true
        }
        val chance = fail.base + (256 - fail.base) * (current - level) / (fail.stopLevel - level)
        return random.of(256) < chance
    }

    // UNVERIFIED: the message and the lack of a falling animation.
    private suspend fun ProtectedAccess.fall(fail: Fail, loc: BoundLocInfo) {
        mes("You slip and fall.")
        delay(2)
        telejump(fail.to.at(loc))
        val damage =
            if (fail.hpPercent > 0) {
                player.hitpoints * fail.hpPercent / 100 + 1
            } else {
                random.of(fail.damage)
            }
        takeInstantHit(HitType.Typeless, damage)
    }

    /** Returns false if the move could not be made (the destination is not on the map). */
    private suspend fun ProtectedAccess.perform(step: Step, loc: BoundLocInfo): Boolean {
        return when (step) {
            is Step.Hop -> hop(step, loc)
            is Step.Glide -> glide(step, loc)
        }
    }

    private suspend fun ProtectedAccess.hop(step: Step.Hop, loc: BoundLocInfo): Boolean {
        val dest = step.dest.at(loc)
        step.anim?.let { anim(it) }
        delay(step.wait)
        telejump(dest)
        return coords == dest
    }

    private suspend fun ProtectedAccess.glide(step: Step.Glide, loc: BoundLocInfo): Boolean {
        val start = coords
        val dest = step.dest.at(loc)
        val ticks = if (step.ticks > 0) step.ticks else max(1, start.chebyshevDistance(dest))
        faceSquare(dest)
        step.anim?.let { anim(it) }
        if (step.balance) {
            player.bas = balanceBas
        }
        try {
            exactMove(start, dest, 0, ticks * CYCLES_PER_TICK, facing(start, dest))
            if (coords != dest) {
                return false
            }
            delay(ticks)
        } finally {
            if (step.balance) {
                player.bas = null
            }
        }
        return true
    }

    private fun pack(course: Course, passed: Int): Int = (course.id shl 8) or passed

    private companion object {
        const val MILLIS_PER_MINUTE = 60_000L
        const val COOLDOWN_MINUTES = 3
        const val PENALTY_FACTOR = 5

        /** Marks of grace stay on the ground for ten minutes. */
        const val MARK_TICKS = 1000
    }
}
