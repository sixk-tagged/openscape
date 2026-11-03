package org.rsmod.content.skills.agility

import org.rsmod.content.skills.agility.configs.AgilityLocs
import org.rsmod.content.skills.agility.configs.AgilitySeqs
import org.rsmod.game.type.loc.LocType
import org.rsmod.map.CoordGrid

/**
 * Every course, from the OSRS wiki (levels, xp, who can fail, marks of grace). The wiki gives no
 * landing tiles: every [Dest] is worked out from the cache's loc placements and the wiki's route
 * maps, and the plane a step lands on is the plane of the next obstacle's loc.
 *
 * UNVERIFIED: all landing tiles, the animations, the failure curves (the wiki gives only the level
 * failing stops at) and where a fall drops the player (the course's first wall on the ground).
 */
object Courses {
    val gnome = gnomeStronghold()
    val draynor = draynorVillage()
    val alKharid = alKharid()
    val varrock = varrock()
    val falador = falador()
    val wilderness = wilderness()
    val canifis = canifis()
    val barbarian = barbarianOutpost()
    val seers = seersVillage()
    val pollnivneach = pollnivneach()
    val rellekka = rellekka()

    val all: List<Course> =
        listOf(
            gnome,
            draynor,
            alKharid,
            varrock,
            falador,
            wilderness,
            canifis,
            barbarian,
            seers,
            pollnivneach,
            rellekka,
        )

    private fun tile(x: Int, z: Int, level: Int = 0) = CoordGrid(x, z, level)

    private fun climb(dest: Dest, wait: Int = 3) = Step.Hop(AgilitySeqs.climb, wait, dest)

    /** Walks along a rope, log or ledge at one tile a tick. */
    private fun balance(dest: Dest) =
        Step.Glide(AgilitySeqs.balance_loop, ticks = 0, dest = dest, balance = true)

    private fun jump(dest: Dest, ticks: Int = 3) = Step.Glide(AgilitySeqs.jump_down, ticks, dest)

    private fun leapUp(dest: Dest, ticks: Int = 3) = Step.Glide(AgilitySeqs.jump_up, ticks, dest)

    private fun swing(dest: Dest, ticks: Int) = Step.Glide(AgilitySeqs.ropeswing, ticks, dest)

    /** Hand over hand along a line or a row of holds, one tile a tick. */
    private fun bars(dest: Dest) = Step.Glide(AgilitySeqs.monkeybars, ticks = 0, dest = dest)

    private fun pipe(dest: Dest, ticks: Int) = Step.Glide(AgilitySeqs.pipe, ticks, dest)

    private fun vault(dest: Dest) = Step.Glide(AgilitySeqs.pole_vault, ticks = 4, dest = dest)

    private fun hurdle(dest: Dest) = Step.Glide(AgilitySeqs.hurdle, ticks = 2, dest = dest)

    private fun zip(dest: Dest) = Step.Glide(AgilitySeqs.zipline, ticks = 4, dest = dest)

    private fun stones(dest: Dest) = Step.Glide(AgilitySeqs.stepping_stone, ticks = 5, dest = dest)

    private fun obstacle(
        loc: LocType,
        tile: CoordGrid,
        level: Int,
        xp: Double,
        vararg steps: Step,
        fail: Fail? = null,
    ) = Obstacle(loc, tile, level, xp, steps.toList(), fail)

    private fun gnomeStronghold() =
        Course(
            id = 1,
            name = "Gnome Stronghold Agility Course",
            level = 1,
            bonusXp = 50.0,
            markTiles = listOf(tile(2486, 3422, 2)),
            obstacles =
                listOf(
                    obstacle(
                        AgilityLocs.gnome_log,
                        tile(2474, 3435),
                        1,
                        10.0,
                        balance(Dest.abs(2474, 3429)),
                    ),
                    obstacle(
                        AgilityLocs.gnome_net_up,
                        tile(2473, 3425),
                        1,
                        10.0,
                        climb(Dest.rel(0, -2, 1), wait = 2),
                    ),
                    obstacle(
                        AgilityLocs.gnome_branch_up,
                        tile(2473, 3422, 1),
                        1,
                        6.5,
                        climb(Dest.rel(0, -2, 1), wait = 2),
                    ),
                    obstacle(
                        AgilityLocs.gnome_rope,
                        tile(2478, 3420, 2),
                        1,
                        10.0,
                        balance(Dest.abs(2483, 3420, 2)),
                    ),
                    obstacle(
                        AgilityLocs.gnome_branch_down,
                        tile(2486, 3419, 2),
                        1,
                        6.5,
                        climb(Dest.abs(2487, 3420), wait = 2),
                    ),
                    obstacle(
                        AgilityLocs.gnome_net_down,
                        tile(2485, 3426),
                        1,
                        10.0,
                        climb(Dest.rel(0, 2), wait = 2),
                    ),
                    Obstacle(
                        listOf(AgilityLocs.gnome_pipe_1, AgilityLocs.gnome_pipe_2),
                        tile(2484, 3431),
                        1,
                        7.5,
                        listOf(pipe(Dest.rel(0, 6), ticks = 6)),
                        starts = listOf(tile(2484, 3431), tile(2487, 3431)),
                    ),
                ),
        )

    private fun draynorVillage() =
        Course(
            id = 2,
            name = "Draynor Village Rooftop Course",
            level = 1,
            markTiles =
                listOf(
                    tile(3099, 3259, 3),
                    tile(3099, 3279, 3),
                    tile(3092, 3266, 3),
                    tile(3088, 3274, 3),
                ),
            obstacles =
                listOf(
                    obstacle(
                        AgilityLocs.draynor_wall,
                        tile(3103, 3279),
                        1,
                        5.0,
                        climb(Dest.abs(3102, 3279, 3)),
                    ),
                    obstacle(
                        AgilityLocs.draynor_rope_1,
                        tile(3098, 3277, 3),
                        1,
                        8.0,
                        balance(Dest.abs(3090, 3277, 3)),
                    ),
                    obstacle(
                        AgilityLocs.draynor_rope_2,
                        tile(3092, 3276, 3),
                        1,
                        7.0,
                        balance(Dest.abs(3092, 3266, 3)),
                    ),
                    obstacle(
                        AgilityLocs.draynor_narrow_wall,
                        tile(3089, 3264, 3),
                        1,
                        7.0,
                        balance(Dest.abs(3088, 3261, 3)),
                    ),
                    obstacle(
                        AgilityLocs.draynor_scramble,
                        tile(3088, 3256, 3),
                        1,
                        10.0,
                        leapUp(Dest.abs(3088, 3255, 3)),
                    ),
                    obstacle(
                        AgilityLocs.draynor_gap,
                        tile(3095, 3255, 3),
                        1,
                        4.0,
                        jump(Dest.abs(3096, 3256, 3)),
                    ),
                    obstacle(
                        AgilityLocs.draynor_crate,
                        tile(3102, 3261, 3),
                        1,
                        79.0,
                        climb(Dest.abs(3103, 3261)),
                    ),
                ),
        )

    private fun alKharid(): Course {
        val ground = Dest.abs(3273, 3195)
        // UNVERIFIED: the wiki lists no stop-failing level for this course.
        val tightrope = Fail(stopLevel = 55, to = ground, damage = 1..5)
        val zipLine = Fail(stopLevel = 55, to = ground, damage = 1..5)
        return Course(
            id = 3,
            name = "Al Kharid Rooftop Course",
            level = 20,
            markTiles =
                listOf(
                    tile(3315, 3161, 1),
                    tile(3266, 3166, 3),
                    tile(3284, 3172, 3),
                    tile(3276, 3185, 3),
                    tile(3302, 3187, 3),
                ),
            obstacles =
                listOf(
                    obstacle(
                        AgilityLocs.kharid_wall,
                        tile(3273, 3195),
                        20,
                        12.0,
                        climb(Dest.abs(3273, 3192, 3)),
                    ),
                    obstacle(
                        AgilityLocs.kharid_rope_1,
                        tile(3272, 3181, 3),
                        20,
                        36.0,
                        balance(Dest.abs(3272, 3172, 3)),
                        fail = tightrope,
                    ),
                    obstacle(
                        AgilityLocs.kharid_cable,
                        tile(3269, 3166, 3),
                        20,
                        48.0,
                        swing(Dest.abs(3284, 3172, 3), ticks = 4),
                    ),
                    obstacle(
                        AgilityLocs.kharid_zip,
                        tile(3302, 3163, 3),
                        20,
                        48.0,
                        zip(Dest.abs(3315, 3163, 1)),
                        fail = zipLine,
                    ),
                    obstacle(
                        AgilityLocs.kharid_tree,
                        tile(3318, 3166, 1),
                        20,
                        12.0,
                        swing(Dest.abs(3318, 3170, 2), ticks = 3),
                    ),
                    obstacle(
                        AgilityLocs.kharid_beams,
                        tile(3316, 3179, 2),
                        20,
                        6.0,
                        climb(Dest.abs(3316, 3180, 3)),
                    ),
                    obstacle(
                        AgilityLocs.kharid_rope_2,
                        tile(3313, 3186, 3),
                        20,
                        18.0,
                        balance(Dest.abs(3302, 3186, 3)),
                    ),
                    obstacle(
                        AgilityLocs.kharid_gap,
                        tile(3300, 3193, 3),
                        20,
                        36.0,
                        jump(Dest.abs(3299, 3194)),
                    ),
                ),
        )
    }

    private fun varrock(): Course {
        val ground = Dest.abs(3221, 3414)
        // UNVERIFIED: the wiki lists no stop-failing level for this course.
        val clothesLine = Fail(stopLevel = 65, to = ground, damage = 3..8)
        val wall = Fail(stopLevel = 65, to = ground, damage = 2..5)
        return Course(
            id = 4,
            name = "Varrock Rooftop Course",
            level = 30,
            markTiles =
                listOf(
                    tile(3185, 3394, 3),
                    tile(3191, 3394, 3),
                    tile(3196, 3415, 3),
                    tile(3238, 3406, 3),
                    tile(3220, 3401, 3),
                ),
            obstacles =
                listOf(
                    obstacle(
                        AgilityLocs.varrock_wall,
                        tile(3221, 3414),
                        30,
                        13.5,
                        climb(Dest.abs(3219, 3414, 3)),
                    ),
                    obstacle(
                        AgilityLocs.varrock_clothesline,
                        tile(3213, 3414, 3),
                        30,
                        23.0,
                        bars(Dest.abs(3204, 3414, 3)),
                        fail = clothesLine,
                    ),
                    obstacle(
                        AgilityLocs.varrock_gap_1,
                        tile(3200, 3416, 3),
                        30,
                        19.0,
                        jump(Dest.abs(3194, 3416, 1), ticks = 4),
                    ),
                    obstacle(
                        AgilityLocs.varrock_balance,
                        tile(3191, 3415, 1),
                        30,
                        28.0,
                        balance(Dest.abs(3190, 3407, 3)),
                        fail = wall,
                    ),
                    obstacle(
                        AgilityLocs.varrock_scramble,
                        tile(3193, 3401, 3),
                        30,
                        10.0,
                        leapUp(Dest.abs(3193, 3399, 3)),
                    ),
                    obstacle(
                        AgilityLocs.varrock_gap_2,
                        tile(3209, 3397, 3),
                        30,
                        24.5,
                        jump(Dest.abs(3218, 3399, 3), ticks = 4),
                    ),
                    obstacle(
                        AgilityLocs.varrock_gap_3,
                        tile(3233, 3402, 3),
                        30,
                        4.5,
                        jump(Dest.abs(3236, 3403, 3)),
                    ),
                    obstacle(
                        AgilityLocs.varrock_ledge,
                        tile(3236, 3409, 3),
                        30,
                        3.5,
                        hurdle(Dest.abs(3236, 3410, 3)),
                    ),
                    obstacle(
                        AgilityLocs.varrock_edge,
                        tile(3236, 3416, 3),
                        30,
                        143.7,
                        jump(Dest.abs(3236, 3417)),
                    ),
                ),
        )
    }

    private fun falador(): Course {
        val handHolds = Fail(stopLevel = 66, to = Dest.abs(3036, 3341), damage = 3..8)
        return Course(
            id = 5,
            name = "Falador Rooftop Course",
            level = 50,
            markTiles =
                listOf(
                    tile(3045, 3345, 3),
                    tile(3049, 3357, 3),
                    tile(3019, 3345, 3),
                    tile(3013, 3345, 3),
                    tile(3015, 3333, 3),
                ),
            obstacles =
                listOf(
                    obstacle(
                        AgilityLocs.falador_wall,
                        tile(3036, 3341),
                        50,
                        11.0,
                        climb(Dest.abs(3036, 3342, 3)),
                    ),
                    obstacle(
                        AgilityLocs.falador_rope_1,
                        tile(3040, 3343, 3),
                        50,
                        22.0,
                        balance(Dest.abs(3047, 3343, 3)),
                    ),
                    obstacle(
                        AgilityLocs.falador_handholds,
                        tile(3050, 3350, 3),
                        50,
                        61.0,
                        bars(Dest.abs(3050, 3356, 3)),
                        fail = handHolds,
                    ),
                    obstacle(
                        AgilityLocs.falador_gap_1,
                        tile(3048, 3359, 3),
                        50,
                        27.0,
                        jump(Dest.abs(3048, 3362, 3)),
                    ),
                    obstacle(
                        AgilityLocs.falador_gap_2,
                        tile(3044, 3361, 3),
                        50,
                        26.0,
                        jump(Dest.abs(3041, 3362, 3)),
                    ),
                    obstacle(
                        AgilityLocs.falador_rope_2,
                        tile(3034, 3361, 3),
                        50,
                        61.0,
                        balance(Dest.abs(3028, 3356, 3)),
                    ),
                    obstacle(
                        AgilityLocs.falador_rope_3,
                        tile(3026, 3353, 3),
                        50,
                        53.0,
                        balance(Dest.abs(3021, 3353, 3)),
                    ),
                    obstacle(
                        AgilityLocs.falador_gap_3,
                        tile(3016, 3352, 3),
                        50,
                        30.0,
                        jump(Dest.abs(3016, 3348, 3)),
                    ),
                    obstacle(
                        AgilityLocs.falador_ledge_1,
                        tile(3015, 3345, 3),
                        50,
                        14.0,
                        jump(Dest.abs(3014, 3342, 3)),
                    ),
                    obstacle(
                        AgilityLocs.falador_ledge_2,
                        tile(3011, 3343, 3),
                        50,
                        13.0,
                        jump(Dest.abs(3012, 3340, 3)),
                    ),
                    Obstacle(
                        listOf(AgilityLocs.falador_ledge_3a, AgilityLocs.falador_ledge_3b),
                        tile(3012, 3334, 3),
                        50,
                        13.0,
                        listOf(jump(Dest.abs(3014, 3334, 3))),
                    ),
                    obstacle(
                        AgilityLocs.falador_ledge_4,
                        tile(3018, 3332, 3),
                        50,
                        14.0,
                        jump(Dest.abs(3020, 3333, 3)),
                    ),
                    obstacle(
                        AgilityLocs.falador_edge,
                        tile(3025, 3332, 3),
                        50,
                        241.0,
                        jump(Dest.abs(3026, 3331)),
                    ),
                ),
        )
    }

    private fun wilderness(): Course {
        // UNVERIFIED: OSRS drops the player into the dungeon below the rope swing and the log; here
        // they are put back on the surface before the obstacle.
        val ropeSwing = Fail(stopLevel = 100, to = Dest.abs(3004, 3950), hpPercent = 15, base = 192)
        val stones = Fail(stopLevel = 100, to = Dest.abs(3003, 3960), hpPercent = 20, base = 192)
        val log = Fail(stopLevel = 100, to = Dest.abs(3002, 3949), hpPercent = 15, base = 192)
        return Course(
            id = 6,
            name = "Wilderness Agility Course",
            level = 49,
            tickets = true,
            markTiles = emptyList(),
            obstacles =
                listOf(
                    Obstacle(
                        listOf(AgilityLocs.wilderness_pipe),
                        tile(3004, 3938),
                        49,
                        12.5,
                        listOf(pipe(Dest.abs(3004, 3950), ticks = 12)),
                        starts = listOf(tile(3004, 3938)),
                    ),
                    obstacle(
                        AgilityLocs.wilderness_ropeswing,
                        tile(3005, 3952),
                        49,
                        20.0,
                        swing(Dest.abs(3005, 3958), ticks = 4),
                        fail = ropeSwing,
                    ),
                    obstacle(
                        AgilityLocs.wilderness_stones,
                        tile(3002, 3960),
                        49,
                        20.0,
                        stones(Dest.abs(2996, 3960)),
                        fail = stones,
                    ),
                    obstacle(
                        AgilityLocs.wilderness_log,
                        tile(3001, 3945),
                        49,
                        20.0,
                        balance(Dest.abs(2994, 3945)),
                        fail = log,
                    ),
                    Obstacle(
                        listOf(AgilityLocs.wilderness_rocks),
                        tile(2994, 3936),
                        49,
                        498.9,
                        listOf(climb(Dest.abs(2994, 3933))),
                        lapOnly = true,
                    ),
                ),
        )
    }

    private fun canifis(): Course {
        // UNVERIFIED: the wiki says only that the third gap can be failed.
        val gap = Fail(stopLevel = 80, to = Dest.abs(3505, 3488), damage = 1..5)
        return Course(
            id = 7,
            name = "Canifis Rooftop Course",
            level = 40,
            markNum = 2,
            markDen = 3,
            markPenalty = false,
            markTiles =
                listOf(
                    tile(3506, 3495, 2),
                    tile(3501, 3505, 2),
                    tile(3490, 3501, 2),
                    tile(3478, 3496, 3),
                    tile(3478, 3484, 2),
                ),
            obstacles =
                listOf(
                    obstacle(
                        AgilityLocs.canifis_tree,
                        tile(3505, 3489),
                        40,
                        10.0,
                        climb(Dest.abs(3506, 3490, 2)),
                    ),
                    obstacle(
                        AgilityLocs.canifis_gap_1,
                        tile(3505, 3498, 2),
                        40,
                        8.0,
                        jump(Dest.abs(3502, 3504, 2)),
                    ),
                    obstacle(
                        AgilityLocs.canifis_gap_2,
                        tile(3496, 3504, 2),
                        40,
                        8.0,
                        jump(Dest.abs(3492, 3504, 2), ticks = 4),
                    ),
                    obstacle(
                        AgilityLocs.canifis_gap_3,
                        tile(3485, 3499, 2),
                        40,
                        10.0,
                        jump(Dest.abs(3479, 3499, 3)),
                        fail = gap,
                    ),
                    obstacle(
                        AgilityLocs.canifis_gap_4,
                        tile(3478, 3491, 3),
                        40,
                        8.0,
                        jump(Dest.abs(3478, 3486, 2)),
                    ),
                    obstacle(
                        AgilityLocs.canifis_vault,
                        tile(3480, 3483, 2),
                        40,
                        10.0,
                        vault(Dest.abs(3489, 3476, 3)),
                    ),
                    obstacle(
                        AgilityLocs.canifis_gap_5,
                        tile(3503, 3476, 3),
                        40,
                        11.0,
                        jump(Dest.abs(3510, 3476, 2)),
                    ),
                    obstacle(
                        AgilityLocs.canifis_gap_6,
                        tile(3510, 3483, 2),
                        40,
                        175.0,
                        jump(Dest.abs(3510, 3485)),
                    ),
                ),
        )
    }

    private fun barbarianOutpost(): Course {
        // UNVERIFIED: which obstacles fail and where a fall lands; the wiki gives only the level
        // failing stops at (93).
        val fall = Fail(stopLevel = 93, to = Dest.abs(2551, 3554), damage = 1..5)
        return Course(
            id = 8,
            name = "Barbarian Outpost Agility Course",
            level = 35,
            bonusXp = 46.3,
            bonusStrengthXp = 41.3,
            markTiles = listOf(tile(2532, 3547, 1)),
            obstacles =
                listOf(
                    obstacle(
                        AgilityLocs.barbarian_ropeswing,
                        tile(2551, 3550),
                        35,
                        22.0,
                        swing(Dest.abs(2551, 3549), ticks = 4),
                        fail = fall,
                    ),
                    obstacle(
                        AgilityLocs.barbarian_log,
                        tile(2550, 3546),
                        35,
                        13.7,
                        balance(Dest.abs(2541, 3546)),
                        fail = fall,
                    ),
                    obstacle(
                        AgilityLocs.barbarian_net,
                        tile(2538, 3545),
                        35,
                        8.2,
                        climb(Dest.rel(0, 2, 1), wait = 2),
                    ),
                    obstacle(
                        AgilityLocs.barbarian_ledge,
                        tile(2535, 3547, 1),
                        35,
                        22.0,
                        balance(Dest.abs(2532, 3547, 1)),
                        fail = fall,
                    ),
                    obstacle(
                        AgilityLocs.barbarian_ladder,
                        tile(2532, 3545, 1),
                        35,
                        0.0,
                        climb(Dest.abs(2532, 3546), wait = 2),
                    ),
                    crumblingWall(2536),
                    crumblingWall(2539),
                    crumblingWall(2542),
                ),
        )
    }

    /** The three walls share one loc type: each starts only where it is placed. */
    private fun crumblingWall(x: Int): Obstacle {
        val over = Step.Glide(AgilitySeqs.crumbled_wall, ticks = 3, dest = Dest.rel(2, 0))
        val tile = tile(x, 3553)
        return Obstacle(
            listOf(AgilityLocs.barbarian_wall),
            tile,
            35,
            13.7,
            listOf(over),
            starts = listOf(tile),
        )
    }

    private fun seersVillage(): Course {
        // UNVERIFIED: the damage; the wiki gives only the level failing stops at (79).
        val fall = Fail(stopLevel = 79, to = Dest.abs(2729, 3489), damage = 2..5)
        return Course(
            id = 9,
            name = "Seers' Village Rooftop Course",
            level = 60,
            markTiles =
                listOf(
                    tile(2727, 3498, 3),
                    tile(2707, 3493, 2),
                    tile(2710, 3493, 2),
                    tile(2712, 3481, 2),
                    tile(2698, 3465, 2),
                ),
            obstacles =
                listOf(
                    obstacle(
                        AgilityLocs.seers_wall,
                        tile(2729, 3489),
                        60,
                        45.0,
                        climb(Dest.abs(2729, 3491, 3)),
                    ),
                    obstacle(
                        AgilityLocs.seers_gap_1,
                        tile(2720, 3492, 3),
                        60,
                        20.0,
                        jump(Dest.abs(2710, 3493, 2), ticks = 4),
                        fail = fall,
                    ),
                    obstacle(
                        AgilityLocs.seers_rope,
                        tile(2710, 3489, 2),
                        60,
                        20.0,
                        balance(Dest.abs(2710, 3481, 2)),
                        fail = fall,
                    ),
                    obstacle(
                        AgilityLocs.seers_gap_2,
                        tile(2710, 3476, 2),
                        60,
                        35.0,
                        jump(Dest.abs(2710, 3472, 3)),
                    ),
                    obstacle(
                        AgilityLocs.seers_gap_3,
                        tile(2700, 3469, 3),
                        60,
                        15.0,
                        jump(Dest.abs(2702, 3465, 2)),
                    ),
                    obstacle(
                        AgilityLocs.seers_edge,
                        tile(2703, 3461, 2),
                        60,
                        435.0,
                        jump(Dest.abs(2703, 3461)),
                    ),
                ),
        )
    }

    private fun pollnivneach(): Course {
        val stall = Fail(stopLevel = 85, to = Dest.abs(3351, 2961), damage = 3..4)
        return Course(
            id = 10,
            name = "Pollnivneach Rooftop Course",
            level = 70,
            markTiles =
                listOf(
                    tile(3347, 2969, 1),
                    tile(3353, 2975, 1),
                    tile(3359, 2983, 1),
                    tile(3357, 3002, 2),
                    tile(3362, 2993, 2),
                ),
            obstacles =
                listOf(
                    obstacle(
                        AgilityLocs.pollnivneach_basket,
                        tile(3351, 2962),
                        70,
                        10.0,
                        climb(Dest.abs(3351, 2966, 1)),
                    ),
                    obstacle(
                        AgilityLocs.pollnivneach_stall,
                        tile(3349, 2970, 1),
                        70,
                        45.0,
                        jump(Dest.abs(3350, 2972, 1)),
                        fail = stall,
                    ),
                    obstacle(
                        AgilityLocs.pollnivneach_banner,
                        tile(3356, 2978, 1),
                        70,
                        65.0,
                        swing(Dest.abs(3360, 2977, 1), ticks = 4),
                    ),
                    obstacle(
                        AgilityLocs.pollnivneach_gap,
                        tile(3363, 2976, 1),
                        70,
                        35.0,
                        jump(Dest.abs(3366, 2976, 1)),
                    ),
                    obstacle(
                        AgilityLocs.pollnivneach_tree_1,
                        tile(3367, 2977, 1),
                        70,
                        75.0,
                        jump(Dest.abs(3368, 2981, 1)),
                    ),
                    obstacle(
                        AgilityLocs.pollnivneach_wall,
                        tile(3365, 2982, 1),
                        70,
                        5.0,
                        climb(Dest.abs(3365, 2983, 2)),
                    ),
                    obstacle(
                        AgilityLocs.pollnivneach_bars,
                        tile(3358, 2985, 2),
                        70,
                        55.0,
                        bars(Dest.abs(3358, 2993, 2)),
                    ),
                    obstacle(
                        AgilityLocs.pollnivneach_tree_2,
                        tile(3359, 2996, 2),
                        70,
                        60.0,
                        jump(Dest.abs(3359, 3000, 2)),
                    ),
                    // 540 without the Desert Hard Diary, which is not built.
                    obstacle(
                        AgilityLocs.pollnivneach_line,
                        tile(3363, 3000, 2),
                        70,
                        540.0,
                        jump(Dest.abs(3363, 2998)),
                    ),
                ),
        )
    }

    private fun rellekka(): Course {
        val rope = Fail(stopLevel = 84, to = Dest.abs(2625, 3677), damage = 5..5)
        return Course(
            id = 11,
            name = "Rellekka Rooftop Course",
            level = 80,
            markNum = 2,
            markDen = 5,
            markTiles =
                listOf(
                    tile(2623, 3675, 3),
                    tile(2628, 3652, 3),
                    tile(2629, 3653, 3),
                    tile(2641, 3650, 3),
                    tile(2642, 3651, 3),
                ),
            obstacles =
                listOf(
                    obstacle(
                        AgilityLocs.rellekka_wall,
                        tile(2625, 3677),
                        80,
                        20.0,
                        climb(Dest.abs(2625, 3676, 3)),
                    ),
                    obstacle(
                        AgilityLocs.rellekka_gap_1,
                        tile(2621, 3669, 3),
                        80,
                        30.0,
                        jump(Dest.abs(2622, 3667, 3)),
                    ),
                    obstacle(
                        AgilityLocs.rellekka_rope_1,
                        tile(2623, 3658, 3),
                        80,
                        40.0,
                        balance(Dest.abs(2627, 3654, 3)),
                        fail = rope,
                    ),
                    obstacle(
                        AgilityLocs.rellekka_gap_2,
                        tile(2629, 3656, 3),
                        80,
                        85.0,
                        jump(Dest.abs(2629, 3658, 3)),
                    ),
                    obstacle(
                        AgilityLocs.rellekka_gap_3,
                        tile(2643, 3654, 3),
                        80,
                        25.0,
                        hurdle(Dest.abs(2643, 3657, 3)),
                    ),
                    obstacle(
                        AgilityLocs.rellekka_rope_2,
                        tile(2647, 3663, 3),
                        80,
                        105.0,
                        balance(Dest.abs(2654, 3670, 3)),
                    ),
                    // 475 without the Hard Fremennik Diary, which is not built.
                    obstacle(
                        AgilityLocs.rellekka_fish,
                        tile(2654, 3676, 3),
                        80,
                        475.0,
                        jump(Dest.abs(2652, 3676)),
                    ),
                ),
        )
    }
}
