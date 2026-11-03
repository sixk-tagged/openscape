package org.rsmod.content.skills.agility

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.skills.agility.configs.AgilityObjs
import org.rsmod.content.skills.agility.configs.AgilityVarps
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.loc.LocShape
import org.rsmod.game.stat.PlayerStatMap
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class CoursesTest {
    @Test
    fun GameTestState.`course xp adds up to the wiki totals`() = runGameTest {
        val totals =
            mapOf(
                Courses.gnome to 110.5,
                Courses.draynor to 120.0,
                Courses.alKharid to 216.0,
                Courses.varrock to 269.7,
                Courses.falador to 586.0,
                Courses.wilderness to 571.4,
                Courses.canifis to 240.0,
                Courses.barbarian to 153.3,
                Courses.seers to 570.0,
                Courses.pollnivneach to 890.0,
                Courses.rellekka to 780.0,
            )
        for ((course, total) in totals) {
            val sum = course.obstacles.sumOf { it.xp } + course.bonusXp
            assertEquals(total, sum, 0.001, course.name)
        }
    }

    @Test
    fun GameTestState.`every course lap runs end to end`() {
        // Every course is run and every problem reported at once: a run on CI is slow.
        val problems = mutableListOf<String>()
        for (course in Courses.all) {
            runInjectedGameTest(ObjRepository::class, null, CourseScript::class) { objRepo ->
                runCatching { checkLap(objRepo, course) }
                    .onFailure { problems += "${course.name}: ${it.message}" }
            }
        }
        assertEquals(emptyList<String>(), problems)
    }

    @Test
    fun GameTestState.`marks of grace wait out their cooldown`() =
        runInjectedGameTest(ObjRepository::class, null, CourseScript::class) { objRepo ->
            lap(Courses.draynor)
            assertEquals(1, marks(objRepo, Courses.draynor))
            lap(Courses.draynor)
            assertEquals(1, marks(objRepo, Courses.draynor))
            player.withProtectedAccess { vars[AgilityVarps.mark_minute] = 0 }
            advance()
            lap(Courses.draynor)
            assertEquals(2, marks(objRepo, Courses.draynor))
        }

    @Test
    fun GameTestState.`a low level is refused`() =
        runGameTest(CourseScript::class) {
            val obstacle = Courses.alKharid.obstacles.first()
            val loc = place(obstacle)
            player.stats[stats.agility] = 19
            val start = approach(obstacle)
            player.teleport(start)
            player.opLoc1(loc)
            advanceUntil(max = 10) { messageSent(LEVEL_MESSAGE) }
            assertEquals(start, player.coords)
        }

    @Test
    fun GameTestState.`failing drops the player and hurts`() =
        runGameTest(CourseScript::class) {
            val obstacle = Courses.alKharid.obstacles[1]
            val loc = place(obstacle)
            val ground = CoordGrid(3273, 3195)
            allocZoneCollision(ground)
            player.stats[stats.agility] = 20
            player.stats[stats.hitpoints] = 20
            player.teleport(approach(obstacle))
            val hitpoints = player.hitpoints
            val xp = player.statMap.getFineXP(stats.agility)
            random.next = 255 // fails the roll
            random.next = 3 // the damage
            player.opLoc1(loc)
            advanceUntil(max = 20) { player.coords == ground }
            advance()
            assertEquals(hitpoints - 3, player.hitpoints)
            assertEquals(xp, player.statMap.getFineXP(stats.agility))
            assertEquals(0, player.vars[AgilityVarps.progress])
        }

    private fun GameTestScope.checkLap(objRepo: ObjRepository, course: Course) {
        val gained = lap(course)
        val expected = course.obstacles.sumOf { fine(it.xp) } + fine(course.bonusXp)
        assertEquals(expected, gained, "xp")
        assertEquals(0, player.vars[AgilityVarps.progress], "progress")
        if (course.markTiles.isNotEmpty()) {
            assertEquals(1, marks(objRepo, course), "marks of grace")
        }
        if (course.tickets) {
            assertEquals(1, player.count(AgilityObjs.wilderness_ticket), "tickets")
        }
    }

    /**
     * Does every obstacle of [course] in order and returns the fine xp gained. Fails, after trying
     * all of them, listing each obstacle the player could not get across.
     */
    private fun GameTestScope.lap(course: Course): Int {
        // A fresh test player's clock reads -1 until their first tick.
        advance()
        player.observerUUID = player.uuid
        player.stats[stats.agility] = course.level
        repeat(32) { random.next = 0 }
        val start = player.statMap.getFineXP(stats.agility)
        val problems = mutableListOf<String>()
        var landing: CoordGrid? = null
        for ((index, obstacle) in course.obstacles.withIndex()) {
            val name = "obstacle ${index + 1} at ${obstacle.tile}"
            if (landing != null && obstacle.tile.level != landing.level) {
                // Each obstacle has to start on the plane the last one landed on.
                problems += "$name: the last obstacle landed on plane ${landing.level}"
            }
            val loc = place(obstacle)
            val dest = obstacle.steps.last().dest.at(loc)
            allocZoneCollision(dest)
            val from = click(obstacle, loc)
            val stuck = stuck(dest, max = 80)
            // Long enough for the longest move to finish and the xp to land.
            advance(MOVE_TICKS)
            if (stuck != null || player.coords != dest) {
                problems += "$name: from $from the player never got to $dest: $stuck"
            }
            landing = dest
        }
        check(problems.isEmpty()) { problems.joinToString("; ") }
        return player.statMap.getFineXP(stats.agility) - start
    }

    /**
     * Clicks [loc] from the first tile that reaches it and returns that tile. The test map has the
     * real map's walls, which can shut off the side an obstacle is meant to be used from once the
     * loc is placed as ground decor; the loc's own tile always reaches it.
     */
    private fun GameTestScope.click(obstacle: Obstacle, loc: BoundLocInfo): CoordGrid {
        val tile = obstacle.tile
        val type = locTypes[obstacle.locs.first()]
        val sides =
            listOf(
                BLOCKED_WEST to tile.translateX(-1),
                BLOCKED_SOUTH to tile.translateZ(-1),
                BLOCKED_EAST to tile.translateX(type.width),
                BLOCKED_NORTH to tile.translateZ(type.length),
            )
        val open = sides.filter { type.forceApproachFlags and it.first == 0 }.map { it.second }
        return (open + tile).first { from ->
            player.teleport(from)
            player.opLoc1(loc)
            advance()
            from == tile || !said(CANNOT_REACH)
        }
    }

    private fun GameTestScope.place(obstacle: Obstacle) =
        placeMapLoc(obstacle.tile, obstacle.locs.first(), shape = LocShape.GroundDecor)

    /**
     * The tile beside [obstacle] that its loc can be used from: many obstacles refuse to be
     * approached from some sides, and a player standing on one of those never gets to the loc.
     */
    private fun GameTestScope.approach(obstacle: Obstacle): CoordGrid {
        val type = locTypes[obstacle.locs.first()]
        val blocked = type.forceApproachFlags
        val tile = obstacle.tile
        return when {
            blocked and BLOCKED_WEST == 0 -> tile.translateX(-1)
            blocked and BLOCKED_SOUTH == 0 -> tile.translateZ(-1)
            blocked and BLOCKED_EAST == 0 -> tile.translateX(type.width)
            blocked and BLOCKED_NORTH == 0 -> tile.translateZ(type.length)
            else -> tile
        }
    }

    private fun GameTestScope.marks(objRepo: ObjRepository, course: Course): Int =
        objRepo
            .findAll(course.markTiles.first())
            .filter { it.type == AgilityObjs.mark_of_grace.id }
            .sumOf { it.count }

    private fun fine(xp: Double): Int = PlayerStatMap.toFineXP(xp).toInt()

    /** Messages only last the tick they are sent in, so check while advancing. */
    private fun GameTestScope.messageSent(text: String): Boolean =
        runCatching { assertMessageSent(text) }.isSuccess

    private companion object {
        const val LEVEL_MESSAGE = "You need an Agility level of 20 to use this obstacle."
        const val CANNOT_REACH = "I can't reach that!"
        const val MOVE_TICKS = 14

        // A loc's force-approach flags: the sides it cannot be used from.
        const val BLOCKED_NORTH = 0x1
        const val BLOCKED_EAST = 0x2
        const val BLOCKED_SOUTH = 0x4
        const val BLOCKED_WEST = 0x8
    }
}
