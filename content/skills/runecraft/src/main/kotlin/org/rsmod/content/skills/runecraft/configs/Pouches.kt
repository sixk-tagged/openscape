package org.rsmod.content.skills.runecraft.configs

import org.rsmod.api.config.refs.varbits
import org.rsmod.content.skills.runecraft.configs.RunecraftObjs as objs
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.varbit.VarBitType
import org.rsmod.game.type.varp.VarpType

/** A pouch holds up to [capacity] while its wear counter is under [below]. */
class Tier(val below: Int, val capacity: Int)

/**
 * One essence pouch size. Contents and type live in the shared `generic_storage_65531` varbits;
 * [wear] is the custom counter that degrades it (null for the small pouch, which never does).
 * Numbers are the wiki's: levels, capacities, and the counters at which each capacity step ends.
 */
class Pouch(
    val name: String,
    val full: ObjType,
    val decayed: ObjType?,
    /** Base Runecrafting level needed; boosts do not count. */
    val level: Int,
    val type: VarBitType,
    val count: VarBitType,
    val wear: VarpType?,
    /** Wear steps for a player of the given base level. */
    val tiers: (Int) -> List<Tier>,
    /** Whether storing essence adds a random 0-3 to the wear (else exactly 1 per essence). */
    val randomWear: Boolean,
) {
    /** What the pouch holds at [wear], or null once it has worn out. */
    fun capacity(level: Int, wear: Int): Int? =
        tiers(level).firstOrNull { wear < it.below }?.capacity
}

object Pouches {
    private fun steps(vararg steps: Pair<Int, Int>): (Int) -> List<Tier> {
        val tiers = steps.map { Tier(it.first, it.second) }
        return { tiers }
    }

    /** The colossal pouch scales with the wearer's level, then loses an eighth per wear step. */
    private val colossalLimits = listOf(320, 565, 745, 870, 950, 995, 1015, 1021)

    // UNVERIFIED: the wiki gives the exact capacities only for level 85+; the lower levels scale
    // proportionally, which matches its "linearly scales downward" wording.
    private fun colossalTiers(level: Int): List<Tier> {
        val base =
            when {
                level >= 85 -> 40
                level >= 75 -> 27
                level >= 50 -> 16
                else -> 8
            }
        return colossalLimits.mapIndexed { step, limit -> Tier(limit, base * (8 - step) / 8) }
    }

    val small =
        Pouch(
            "small",
            objs.small_pouch,
            null,
            1,
            varbits.small_pouch_storage_type,
            varbits.small_pouch_storage_count,
            null,
            steps(Int.MAX_VALUE to 3),
            randomWear = false,
        )
    val medium =
        Pouch(
            "medium",
            objs.medium_pouch,
            objs.medium_pouch_decayed,
            25,
            varbits.medium_pouch_storage_type,
            varbits.medium_pouch_storage_count,
            RunecraftVarps.pouch_decay_medium,
            steps(400 to 6, 800 to 3),
            randomWear = true,
        )
    val large =
        Pouch(
            "large",
            objs.large_pouch,
            objs.large_pouch_decayed,
            50,
            varbits.large_pouch_storage_type,
            varbits.large_pouch_storage_count,
            RunecraftVarps.pouch_decay_large,
            steps(400 to 9, 600 to 7, 800 to 5, 1000 to 3),
            randomWear = true,
        )
    val giant =
        Pouch(
            "giant",
            objs.giant_pouch,
            objs.giant_pouch_decayed,
            75,
            varbits.giant_pouch_storage_type,
            varbits.giant_pouch_storage_count,
            RunecraftVarps.pouch_decay_giant,
            steps(200 to 12, 300 to 9, 400 to 8, 600 to 7, 800 to 6, 1000 to 5, 1200 to 3),
            randomWear = true,
        )
    // UNVERIFIED: that colossal wear is exactly 1 per essence; the wiki's counters imply it.
    val colossal =
        Pouch(
            "colossal",
            objs.colossal_pouch,
            objs.colossal_pouch_decayed,
            25,
            varbits.colossal_pouch_storage_type,
            varbits.colossal_pouch_storage_count,
            RunecraftVarps.pouch_decay_colossal,
            ::colossalTiers,
            randomWear = false,
        )

    val all = listOf(small, medium, large, giant, colossal)
}
