package org.rsmod.api.ge

import kotlin.math.min

/** The 2% convenience fee, per item, rounded down and capped. */
public object GeTax {
    public const val MAX_PER_ITEM: Long = 5_000_000
    public const val RATE_TENTHS_PERCENT: Int = 20

    /**
     * Objs that pay no fee: bond, energy potions, cheap ammo and runes, some food/teleports/tools.
     */
    private val exempt: Set<Int> =
        setOf(
            // Old school bond
            13190,
            // Energy potion (4) to (1)
            3008,
            3010,
            3012,
            3014,
            // Bronze/iron/steel arrow and dart, Mind rune
            882,
            806,
            884,
            807,
            886,
            808,
            558,
            // Bass, Bread, Cake, Cooked chicken, Cooked meat, Herring, Lobster, Mackerel,
            // Meat pie, Pike, Salmon, Shrimps, Tuna
            365,
            2309,
            1891,
            2140,
            2142,
            347,
            379,
            355,
            2327,
            351,
            329,
            315,
            361,
            // Ardougne, Camelot, Civitas illa fortis, Falador, Kourend castle, Lumbridge, house and
            // Varrock teleports; Games necklace(8); Ring of dueling(8)
            8011,
            8010,
            28824,
            8009,
            28790,
            8008,
            8013,
            8007,
            3853,
            2552,
            // Chisel, Gardening trowel, Glassblowing pipe, Hammer, Needle, Pestle and mortar, Rake,
            // Saw, Secateurs, Seed dibber, Shears, Spade, Watering can(0)
            1755,
            5325,
            1785,
            2347,
            1733,
            233,
            5341,
            8794,
            5329,
            5343,
            1735,
            952,
            5331,
        )

    public fun isExempt(obj: Int): Boolean = obj in exempt

    /** Fee on one item sold at [unitPrice]. */
    public fun perItem(obj: Int, unitPrice: Int): Long {
        if (isExempt(obj)) {
            return 0
        }
        return min(MAX_PER_ITEM, unitPrice.toLong() * RATE_TENTHS_PERCENT / 1000)
    }
}
