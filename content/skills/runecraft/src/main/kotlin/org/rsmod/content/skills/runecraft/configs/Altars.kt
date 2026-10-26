package org.rsmod.content.skills.runecraft.configs

import org.rsmod.content.skills.runecraft.configs.RunecraftLocs as locs
import org.rsmod.content.skills.runecraft.configs.RunecraftObjs as objs
import org.rsmod.content.skills.runecraft.configs.RunecraftVarBits as bits
import org.rsmod.game.type.loc.LocType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.varbit.VarBitType
import org.rsmod.map.CoordGrid

/**
 * One runecrafting altar. Levels, xp and multiples are the OSRS wiki's; [divisor] gives `level /
 * divisor + 1` runes per essence, and is null where the wiki lists no multiples.
 */
class Altar(
    val name: String,
    val level: Int,
    val xp: Double,
    val rune: ObjType,
    val divisor: Int?,
    val pureOnly: Boolean,
    val members: Boolean,
    val loc: LocType,
    /** The Camdozaal golem core made for this altar: 11 runes and 10x the xp each. */
    val core: ObjType? = null,
    val ruins: Ruins? = null,
    val binding: Binding? = null,
)

/**
 * The mysterious ruins above an altar, and the altar's exit portal. The ruins is a multiloc on
 * [bit]: it shows `Enter` while a tiara or cape is worn. [inside] and [outside] are the tiles next
 * to the exit portal and in front of the ruins, picked from the map's collision (the wiki gives
 * none).
 */
class Ruins(
    val loc: LocType,
    val bit: VarBitType,
    val coords: CoordGrid,
    val portal: LocType,
    val inside: CoordGrid,
    val outside: CoordGrid,
)

/**
 * What opens the ruins and what a tiara of the altar is: [elemental] altars take elemental ones.
 */
class Binding(
    val talisman: ObjType,
    val tiara: ObjType,
    val tiaraXp: Double,
    val elemental: Boolean,
)

object Altars {
    // UNVERIFIED: every `inside` and `outside` tile below, and the cosmic altar's choice of portal.
    val air =
        Altar(
            name = "air",
            level = 1,
            xp = 5.0,
            rune = objs.air_rune,
            divisor = 11,
            pureOnly = false,
            members = false,
            loc = locs.air_altar,
            ruins =
                Ruins(
                    locs.air_ruins,
                    bits.air_enter,
                    CoordGrid(2984, 3291),
                    locs.air_portal,
                    inside = CoordGrid(2841, 4829),
                    outside = CoordGrid(2985, 3290),
                ),
            binding = Binding(objs.air_talisman, objs.tiara_air, 25.0, elemental = true),
        )
    val mind =
        Altar(
            name = "mind",
            level = 2,
            xp = 5.5,
            rune = objs.mind_rune,
            divisor = 14,
            pureOnly = false,
            members = false,
            loc = locs.mind_altar,
            core = objs.mind_core,
            ruins =
                Ruins(
                    locs.mind_ruins,
                    bits.mind_enter,
                    CoordGrid(2981, 3513),
                    locs.mind_portal,
                    inside = CoordGrid(2793, 4828),
                    outside = CoordGrid(2982, 3512),
                ),
            binding = Binding(objs.mind_talisman, objs.tiara_mind, 27.5, elemental = false),
        )
    val water =
        Altar(
            name = "water",
            level = 5,
            xp = 6.0,
            rune = objs.water_rune,
            divisor = 19,
            pureOnly = false,
            members = false,
            loc = locs.water_altar,
            ruins =
                Ruins(
                    locs.water_ruins,
                    bits.water_enter,
                    CoordGrid(3184, 3164),
                    locs.water_portal,
                    inside = CoordGrid(2726, 4832),
                    outside = CoordGrid(3185, 3163),
                ),
            binding = Binding(objs.water_talisman, objs.tiara_water, 30.0, elemental = true),
        )
    val earth =
        Altar(
            name = "earth",
            level = 9,
            xp = 6.5,
            rune = objs.earth_rune,
            divisor = 26,
            pureOnly = false,
            members = false,
            loc = locs.earth_altar,
            ruins =
                Ruins(
                    locs.earth_ruins,
                    bits.earth_enter,
                    CoordGrid(3305, 3473),
                    locs.earth_portal,
                    inside = CoordGrid(2655, 4830),
                    outside = CoordGrid(3306, 3472),
                ),
            binding = Binding(objs.earth_talisman, objs.tiara_earth, 32.5, elemental = true),
        )
    val fire =
        Altar(
            name = "fire",
            level = 14,
            xp = 7.0,
            rune = objs.fire_rune,
            divisor = 35,
            pureOnly = false,
            members = false,
            loc = locs.fire_altar,
            ruins =
                Ruins(
                    locs.fire_ruins,
                    bits.fire_enter,
                    CoordGrid(3312, 3254),
                    locs.fire_portal,
                    inside = CoordGrid(2574, 4849),
                    outside = CoordGrid(3312, 3253),
                ),
            binding = Binding(objs.fire_talisman, objs.tiara_fire, 35.0, elemental = true),
        )
    val body =
        Altar(
            name = "body",
            level = 20,
            xp = 7.5,
            rune = objs.body_rune,
            divisor = 46,
            pureOnly = false,
            members = false,
            loc = locs.body_altar,
            core = objs.body_core,
            ruins =
                Ruins(
                    locs.body_ruins,
                    bits.body_enter,
                    CoordGrid(3052, 3444),
                    locs.body_portal,
                    inside = CoordGrid(2521, 4834),
                    outside = CoordGrid(3053, 3443),
                ),
            binding = Binding(objs.body_talisman, objs.tiara_body, 37.5, elemental = false),
        )
    val cosmic =
        Altar(
            name = "cosmic",
            level = 27,
            xp = 8.0,
            rune = objs.cosmic_rune,
            divisor = 59,
            pureOnly = true,
            members = true,
            loc = locs.cosmic_altar,
            ruins =
                Ruins(
                    locs.cosmic_ruins,
                    bits.cosmic_enter,
                    CoordGrid(2407, 4376),
                    locs.cosmic_portal,
                    inside = CoordGrid(2162, 4833),
                    outside = CoordGrid(2407, 4375),
                ),
            binding = Binding(objs.cosmic_talisman, objs.tiara_cosmic, 40.0, elemental = false),
        )
    val chaos =
        Altar(
            name = "chaos",
            level = 35,
            xp = 8.5,
            rune = objs.chaos_rune,
            divisor = 74,
            pureOnly = true,
            members = true,
            loc = locs.chaos_altar,
            core = objs.chaos_core,
            ruins =
                Ruins(
                    locs.chaos_ruins,
                    bits.chaos_enter,
                    CoordGrid(3059, 3590),
                    locs.chaos_portal,
                    // The ruins drop you on the top level of the maze, by its plane 3 portal.
                    inside = CoordGrid(2273, 4855, 3),
                    outside = CoordGrid(3060, 3589),
                ),
            binding = Binding(objs.chaos_talisman, objs.tiara_chaos, 42.5, elemental = false),
        )
    val astral =
        Altar(
            name = "astral",
            level = 40,
            xp = 8.7,
            rune = objs.astral_rune,
            divisor = 82,
            pureOnly = true,
            members = true,
            loc = locs.astral_altar,
        )
    val nature =
        Altar(
            name = "nature",
            level = 44,
            xp = 9.0,
            rune = objs.nature_rune,
            divisor = 91,
            pureOnly = true,
            members = true,
            loc = locs.nature_altar,
            ruins =
                Ruins(
                    locs.nature_ruins,
                    bits.nature_enter,
                    CoordGrid(2868, 3018),
                    locs.nature_portal,
                    inside = CoordGrid(2400, 4835),
                    outside = CoordGrid(2869, 3017),
                ),
            binding = Binding(objs.nature_talisman, objs.tiara_nature, 45.0, elemental = false),
        )
    val law =
        Altar(
            name = "law",
            level = 54,
            xp = 9.5,
            rune = objs.law_rune,
            divisor = 95,
            pureOnly = true,
            members = true,
            loc = locs.law_altar,
            ruins =
                Ruins(
                    locs.law_ruins,
                    bits.law_enter,
                    CoordGrid(2857, 3380),
                    locs.law_portal,
                    inside = CoordGrid(2464, 4818),
                    outside = CoordGrid(2858, 3379),
                ),
            binding = Binding(objs.law_talisman, objs.tiara_law, 47.5, elemental = false),
        )
    val death =
        Altar(
            name = "death",
            level = 65,
            xp = 10.0,
            rune = objs.death_rune,
            divisor = 99,
            pureOnly = true,
            members = true,
            loc = locs.death_altar,
            ruins =
                Ruins(
                    locs.death_ruins,
                    bits.death_enter,
                    CoordGrid(1859, 4638),
                    locs.death_portal,
                    inside = CoordGrid(2208, 4830),
                    outside = CoordGrid(1860, 4637),
                ),
            binding = Binding(objs.death_talisman, objs.tiara_death, 50.0, elemental = false),
        )
    val blood =
        Altar(
            name = "blood",
            level = 77,
            xp = 10.5,
            rune = objs.blood_rune,
            divisor = null,
            pureOnly = true,
            members = true,
            loc = locs.blood_altar,
            ruins =
                Ruins(
                    locs.blood_ruins,
                    bits.blood_enter,
                    CoordGrid(3560, 9780),
                    locs.blood_portal,
                    inside = CoordGrid(3225, 4832),
                    outside = CoordGrid(3561, 9779),
                ),
            binding = Binding(objs.blood_talisman, objs.tiara_blood, 52.5, elemental = false),
        )
    val wrath =
        Altar(
            name = "wrath",
            level = 95,
            xp = 8.0,
            rune = objs.wrath_rune,
            divisor = null,
            pureOnly = true,
            members = true,
            loc = locs.wrath_altar,
            ruins =
                Ruins(
                    locs.wrath_ruins,
                    bits.wrath_enter,
                    CoordGrid(2445, 2824),
                    locs.wrath_portal,
                    inside = CoordGrid(2335, 4826, 1),
                    outside = CoordGrid(2446, 2823),
                ),
            // The wiki lists wrath tiaras at 52.5 xp, not 5x the rune xp.
            binding = Binding(objs.wrath_talisman, objs.tiara_wrath, 52.5, elemental = false),
        )

    val all =
        listOf(
            air,
            mind,
            water,
            earth,
            fire,
            body,
            cosmic,
            chaos,
            astral,
            nature,
            law,
            death,
            blood,
            wrath,
        )

    /** Altars with ruins above them. */
    val withRuins = all.filter { it.ruins != null }

    /** What an elemental or catalytic tiara opens: the matching group of altars' ruins. */
    val elementalRuins = withRuins.filter { it.binding?.elemental == true }
    val catalyticRuins = withRuins.filter { it.binding?.elemental == false }

    /**
     * Xp for binding a gold tiara to an elemental or catalytic talisman. UNVERIFIED (60 vs 42.5).
     */
    const val ELEMENTAL_TIARA_XP = 40.0
    const val CATALYTIC_TIARA_XP = 60.0

    /** Runes made per essence at [level]. */
    fun runesPerEssence(altar: Altar, level: Int): Int =
        (altar.divisor?.let { level / it } ?: 0) + 1
}
