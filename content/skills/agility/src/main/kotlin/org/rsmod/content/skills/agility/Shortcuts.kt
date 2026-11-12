package org.rsmod.content.skills.agility

import kotlin.math.abs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.agilityLvl
import org.rsmod.api.script.onApLoc1
import org.rsmod.api.script.onOpLoc1
import org.rsmod.content.skills.agility.configs.AgilityLocs
import org.rsmod.content.skills.agility.configs.AgilitySeqs
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.loc.LocAngle
import org.rsmod.game.type.loc.LocType
import org.rsmod.game.type.seq.SeqType
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * A shortcut over [loc]: the player crosses to the other side. Where the obstacle is a single loc
 * the far side is found from the loc: across the wall ([wall]) or straight over it; otherwise
 * [links] give the start tiles and where each ends up.
 */
class Shortcut(
    val loc: LocType,
    val level: Int,
    val xp: Double,
    val anim: SeqType,
    val ticks: Int,
    val wall: Boolean = false,
    val links: List<Pair<CoordGrid, CoordGrid>> = emptyList(),
    /** Whether the player is moved in one jump (a long climb) rather than walked across. */
    val jump: Boolean = false,
)

/**
 * The shortcuts built so far (levels and xp from the OSRS wiki's list of shortcuts): the ones near
 * places players can reach whose locs could be found in the cache. Failing a stepping stone or log
 * balance is not built.
 */
object Shortcuts {
    // UNVERIFIED: every animation and crossing time, and the tiles, which are the walkable tiles
    // either side of each loc in the cache's collision map.
    val all =
        listOf(
            // Falador, next to the west bank.
            Shortcut(AgilityLocs.falador_crumbling_wall, 5, 0.5, AgilitySeqs.climb, 3),
            // Varrock's south fence.
            Shortcut(AgilityLocs.varrock_south_fence, 13, 0.0, AgilitySeqs.hurdle, 2, wall = true),
            // The Goblin Village wall, which can be reached from the north or the east.
            Shortcut(
                AgilityLocs.burthorpe_tight_gap,
                14,
                0.0,
                AgilitySeqs.crumbled_wall,
                3,
                links =
                    listOf(
                        CoordGrid(2927, 3523) to CoordGrid(2927, 3520),
                        CoordGrid(2927, 3520) to CoordGrid(2927, 3523),
                    ),
            ),
            // The Salve's railings, from east Varrock into Morytania.
            Shortcut(AgilityLocs.salve_railing_south, 65, 0.0, AgilitySeqs.pipe, 2, wall = true),
            Shortcut(AgilityLocs.salve_railing_north, 65, 0.0, AgilitySeqs.pipe, 2, wall = true),
        ) +
            // The monkey bars under Edgeville, to the Earth warriors.
            crossing(
                15,
                20.0,
                AgilitySeqs.monkeybars,
                7,
                CoordGrid(3120, 9963),
                CoordGrid(3120, 9970),
                AgilityLocs.edgeville_bars_end,
                AgilityLocs.edgeville_bars_end_2,
            ) +
            // The Grand Exchange's north-west corner.
            crossing(
                21,
                0.0,
                AgilitySeqs.pipe,
                5,
                CoordGrid(3137, 3516),
                CoordGrid(3142, 3513),
                AgilityLocs.varrock_tunnel_west,
                AgilityLocs.varrock_tunnel_east,
            ) +
            // Falador's south-west corner.
            crossing(
                26,
                0.0,
                AgilitySeqs.pipe,
                4,
                CoordGrid(2948, 3309),
                CoordGrid(2948, 3313),
                AgilityLocs.falador_tunnel_south,
                AgilityLocs.falador_tunnel_north,
            ) +
            // Draynor Manor to the Champions' Guild.
            crossing(
                31,
                3.0,
                AgilitySeqs.stepping_stone,
                5,
                CoordGrid(3149, 3363),
                CoordGrid(3154, 3363),
                AgilityLocs.draynor_manor_stones,
            ) +
            // The Varrock Palace garden.
            crossing(
                35,
                0.0,
                AgilitySeqs.climb,
                2,
                CoordGrid(3228, 3470),
                CoordGrid(3228, 3471),
                AgilityLocs.varrock_trellis,
            ) +
            // The Gnome Stronghold's north-east rocks, to the Barbarian Outpost.
            crossing(
                37,
                0.0,
                AgilitySeqs.climb,
                4,
                CoordGrid(2486, 3515),
                CoordGrid(2489, 3521),
                AgilityLocs.gnome_rocks_top,
                AgilityLocs.gnome_rocks_bottom,
            ) +
            // The Al Kharid mining pit's cliff.
            crossing(
                38,
                0.0,
                AgilitySeqs.climb,
                3,
                CoordGrid(3306, 3315),
                CoordGrid(3302, 3315),
                AgilityLocs.alkharid_rocks_top,
                AgilityLocs.alkharid_rocks_bottom,
            ) +
            // Draynor to Port Sarim.
            crossing(
                42,
                0.0,
                AgilitySeqs.pipe,
                4,
                CoordGrid(3066, 3257),
                CoordGrid(3070, 3257),
                AgilityLocs.draynor_tunnel_west,
                AgilityLocs.draynor_tunnel_east,
            ) +
            // North of Camelot, to the Fremennik Province.
            crossing(
                48,
                4.0,
                AgilitySeqs.balance_walk,
                4,
                CoordGrid(2722, 3592),
                CoordGrid(2722, 3596),
                AgilityLocs.fremennik_log_south,
                AgilityLocs.fremennik_log_north,
            ) +
            // The Edgeville Dungeon to the Varrock Sewers.
            crossing(
                51,
                10.0,
                AgilitySeqs.pipe,
                6,
                CoordGrid(3149, 9906),
                CoordGrid(3155, 9906),
                AgilityLocs.sewers_pipe,
            ) +
            // The Al Kharid Palace's southern wall.
            crossing(
                70,
                0.0,
                AgilitySeqs.climb,
                3,
                CoordGrid(3295, 3156),
                CoordGrid(3295, 3159),
                AgilityLocs.alkharid_palace_window,
            ) +
            // The Wilderness's lava: the Chaos Temple, Lava Dragon Isle and the Lava Maze.
            crossing(
                72,
                0.0,
                AgilitySeqs.stepping_stone,
                4,
                CoordGrid(3268, 3625),
                CoordGrid(3268, 3629),
                AgilityLocs.chaos_temple_stones,
            ) +
            crossing(
                74,
                0.0,
                AgilitySeqs.stepping_stone,
                4,
                CoordGrid(3201, 3807),
                CoordGrid(3201, 3811),
                AgilityLocs.lava_dragon_stones,
            ) +
            crossing(
                82,
                0.0,
                AgilitySeqs.stepping_stone,
                4,
                CoordGrid(3092, 3878),
                CoordGrid(3092, 3883),
                AgilityLocs.lava_maze_stones,
            ) +
            // The Lumbridge Swamp Caves, to the giant frogs and the Chasm of Tears.
            // Note: never falls in (the wiki: 1 xp and the light source goes out).
            crossing(
                1,
                3.0,
                AgilitySeqs.stepping_stone,
                4,
                CoordGrid(3204, 9572),
                CoordGrid(3208, 9572),
                AgilityLocs.swamp_cave_stones_a,
            ) +
            crossing(
                1,
                3.0,
                AgilitySeqs.stepping_stone,
                4,
                CoordGrid(3221, 9556),
                CoordGrid(3221, 9552),
                AgilityLocs.swamp_cave_stones_b,
            ) +
            // Yanille: under the north wall (one loc each way), and the rocks out of the city.
            crossing(
                16,
                0.0,
                AgilitySeqs.pipe,
                5,
                CoordGrid(2575, 3107),
                CoordGrid(2575, 3112),
                AgilityLocs.yanille_wall,
                AgilityLocs.yanille_hole,
            ) +
            Shortcut(
                AgilityLocs.yanille_rocks,
                5,
                25.0,
                AgilitySeqs.climb,
                3,
                links = listOf(CoordGrid(2561, 3108) to CoordGrid(2561, 3111)),
            ) +
            // The Yanille Agility Dungeon: the balancing ledge, the monkey bars (two lines of the
            // loc Edgeville also uses), the pipe and the pile of rubble.
            // Note: the ledge never drops you into the poison spiders' pit.
            crossing(
                40,
                22.5,
                AgilitySeqs.balance_walk,
                8,
                CoordGrid(2580, 9520),
                CoordGrid(2580, 9512),
                AgilityLocs.yanille_ledge,
            ) +
            Shortcut(
                AgilityLocs.edgeville_bars_end_2,
                57,
                20.0,
                AgilitySeqs.monkeybars,
                6,
                links =
                    listOf(2598, 2599).flatMap { x ->
                        val south = CoordGrid(x, 9489)
                        val north = CoordGrid(x, 9494)
                        listOf(south to north, north to south)
                    },
            ) +
            crossing(
                49,
                7.5,
                AgilitySeqs.pipe,
                6,
                CoordGrid(2578, 9506),
                CoordGrid(2572, 9506),
                AgilityLocs.yanille_pipe,
            ) +
            // UNVERIFIED: where the rubble's top lands you. The two ends are 66 tiles apart.
            listOf(AgilityLocs.yanille_rubble_down, AgilityLocs.yanille_rubble_up).map {
                val top = CoordGrid(2615, 9506)
                val bottom = CoordGrid(2616, 9571)
                Shortcut(
                    it,
                    67,
                    5.5,
                    AgilitySeqs.climb,
                    3,
                    links = listOf(top to bottom, bottom to top),
                    jump = true,
                )
            }

    /** One row per loc of an obstacle that takes the player between [from] and [to]. */
    private fun crossing(
        level: Int,
        xp: Double,
        anim: SeqType,
        ticks: Int,
        from: CoordGrid,
        to: CoordGrid,
        vararg locs: LocType,
    ) = locs.map { Shortcut(it, level, xp, anim, ticks, links = listOf(from to to, to to from)) }
}

/** Runs [Shortcuts]. */
class ShortcutScript : PluginScript() {
    override fun ScriptContext.startup() {
        val outOfReach =
            listOf(
                AgilityLocs.chaos_temple_stones,
                AgilityLocs.swamp_cave_stones_a,
                AgilityLocs.swamp_cave_stones_b,
            )
        // One handler per loc type: a type can serve several shortcuts, told apart by where the
        // player stands.
        for ((loc, shortcuts) in Shortcuts.all.groupBy { it.loc }) {
            onOpLoc1(loc) { cross(shortcuts, it.loc) }
            // These stones stand in lava or water, out of reach of either bank.
            if (loc in outOfReach) {
                onApLoc1(loc) {
                    if (isWithinApRange(it.loc, LINK_RANGE)) {
                        cross(shortcuts, it.loc)
                    }
                }
            }
        }
    }

    // UNVERIFIED: the refusal messages.
    private suspend fun ProtectedAccess.cross(shortcuts: List<Shortcut>, loc: BoundLocInfo) {
        val shortcut =
            shortcuts.firstOrNull { s ->
                s.links.any { coords.chebyshevDistance(it.first) <= LINK_RANGE }
            } ?: shortcuts.first()
        if (player.agilityLvl < shortcut.level) {
            mes("You need an Agility level of ${shortcut.level} to use this shortcut.")
            return
        }
        val dest = farSide(shortcut, loc)
        if (dest == null) {
            mes("You can't reach that from here.")
            return
        }
        faceLoc(loc)
        arriveDelay()
        val start = coords
        faceSquare(dest)
        anim(shortcut.anim)
        if (shortcut.jump) {
            delay(shortcut.ticks)
            telejump(dest)
        } else {
            exactMove(start, dest, 0, shortcut.ticks * CYCLES_PER_TICK, facing(start, dest))
            if (coords != dest) {
                return
            }
            delay(shortcut.ticks)
        }
        if (shortcut.xp > 0.0) {
            statAdvance(stats.agility, shortcut.xp)
        }
    }

    private fun ProtectedAccess.farSide(shortcut: Shortcut, loc: BoundLocInfo): CoordGrid? {
        if (shortcut.links.isNotEmpty()) {
            val link = shortcut.links.minBy { coords.chebyshevDistance(it.first) }
            return link.second.takeIf { coords.chebyshevDistance(link.first) <= LINK_RANGE }
        }
        val dx = coords.x - loc.coords.x
        val dz = coords.z - loc.coords.z
        if (shortcut.wall) {
            return throughWall(loc, dx, dz)
        }
        return if (abs(dx) + abs(dz) == 1) loc.coords.translate(-dx, -dz) else null
    }

    /** A wall has the loc's tile on one side of it and the tile it faces on the other. */
    private fun ProtectedAccess.throughWall(loc: BoundLocInfo, dx: Int, dz: Int): CoordGrid {
        val (ox, oz) =
            when (loc.angle) {
                LocAngle.West -> -1 to 0
                LocAngle.North -> 0 to 1
                LocAngle.East -> 1 to 0
                LocAngle.South -> 0 to -1
            }
        val outside = dx * ox + dz * oz > 0
        return if (outside) loc.coords else loc.coords.translate(ox, oz)
    }

    private companion object {
        const val LINK_RANGE = 2
    }
}
