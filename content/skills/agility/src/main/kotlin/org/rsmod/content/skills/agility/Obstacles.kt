package org.rsmod.content.skills.agility

import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.type.loc.LocType
import org.rsmod.game.type.seq.SeqType
import org.rsmod.map.CoordGrid

/** Where a step ends: a fixed tile, or an offset from the obstacle's own loc. */
class Dest
private constructor(private val fixed: CoordGrid?, val dx: Int, val dz: Int, val dl: Int) {
    fun at(loc: BoundLocInfo): CoordGrid = fixed ?: loc.coords.translate(dx, dz, dl)

    companion object {
        fun abs(x: Int, z: Int, level: Int = 0): Dest = Dest(CoordGrid(x, z, level), 0, 0, 0)

        fun rel(dx: Int, dz: Int, dl: Int = 0): Dest = Dest(null, dx, dz, dl)
    }
}

sealed class Step {
    abstract val dest: Dest

    /** Plays [anim], waits [wait] ticks, then jumps to [dest]: climbs, nets, ladders, crates. */
    class Hop(val anim: SeqType?, val wait: Int, override val dest: Dest) : Step()

    /**
     * Glides to [dest] over [ticks] ticks while playing [anim]: jumps, swings, ropes. [balance]
     * walks with the balancing pose (ropes, logs, ledges).
     */
    class Glide(
        val anim: SeqType?,
        val ticks: Int,
        override val dest: Dest,
        val balance: Boolean = false,
    ) : Step()
}

/**
 * An obstacle that can be failed below [stopLevel] (the wiki gives only the level where failing
 * stops, not the curve): the chance of passing is [base] out of 256 at the requirement and rises
 * linearly to 256 at [stopLevel]. A fall deals [damage] (inclusive) or, if [hpPercent] is set, that
 * percent of the current hitpoints plus one, and drops the player on [to].
 */
class Fail(
    val stopLevel: Int,
    val to: Dest,
    val damage: IntRange = 1..1,
    val hpPercent: Int = 0,
    val base: Int = 160,
)

/**
 * One obstacle: [locs] are the loc types that start it (a few obstacles have several placed types),
 * [tile] a representative placement (used by tests). [lapOnly] xp is only given when the lap has
 * been done in order. If [starts] is not empty only the locs placed there start it.
 */
class Obstacle(
    val locs: List<LocType>,
    val tile: CoordGrid,
    val level: Int,
    val xp: Double,
    val steps: List<Step>,
    val fail: Fail? = null,
    val lapOnly: Boolean = false,
    val strengthXp: Double = 0.0,
    val starts: List<CoordGrid> = emptyList(),
) {
    constructor(
        loc: LocType,
        tile: CoordGrid,
        level: Int,
        xp: Double,
        steps: List<Step>,
        fail: Fail? = null,
    ) : this(listOf(loc), tile, level, xp, steps, fail)
}

/**
 * A course of [obstacles] done in order. [bonusXp] (and [bonusStrengthXp]) come with the last
 * obstacle of a lap done in order; marks of grace roll then with chance [markNum]/[markDen].
 */
class Course(
    val id: Int,
    val name: String,
    val level: Int,
    val obstacles: List<Obstacle>,
    val markTiles: List<CoordGrid>,
    val markNum: Int = 2,
    val markDen: Int = 6,
    val markPenalty: Boolean = true,
    val bonusXp: Double = 0.0,
    val bonusStrengthXp: Double = 0.0,
    val tickets: Boolean = false,
)
