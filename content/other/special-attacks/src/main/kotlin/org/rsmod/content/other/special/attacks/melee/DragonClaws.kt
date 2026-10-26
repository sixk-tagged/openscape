package org.rsmod.content.other.special.attacks.melee

/**
 * Slice and Dice's damage split (OSRS wiki, Dragon claws). Up to four accuracy rolls are made;
 * [hits] turns the first one that lands ([landedOn], 1-4) and the damage rolled for it within
 * [firstHitRange] into the four hitsplats.
 */
object DragonClaws {
    fun firstHitRange(maxHit: Int, landedOn: Int): IntRange =
        when (landedOn) {
            1 -> maxHit / 2..maxHit - 1
            2 -> maxHit * 3 / 8..maxHit * 7 / 8
            3 -> maxHit / 4..maxHit * 3 / 4
            4 -> maxHit / 4..maxHit * 5 / 4
            else -> error("Only swings 1-4 can land: $landedOn")
        }

    fun hits(landedOn: Int, firstHit: Int): List<Int> =
        when (landedOn) {
            1 -> listOf(firstHit, firstHit / 2, firstHit / 4, firstHit / 4 + 1)
            2 -> listOf(0, firstHit, firstHit / 2, firstHit / 2 + 1)
            3 -> listOf(0, 0, firstHit, firstHit + 1)
            4 -> listOf(0, 0, 0, firstHit)
            else -> error("Only swings 1-4 can land: $landedOn")
        }

    /** When all four swings miss: a 2-in-3 chance of 1 damage on two of the four hitsplats. */
    val MISS_PATTERNS =
        listOf(listOf(1, 1, 0, 0), listOf(0, 0, 1, 1), listOf(1, 0, 1, 0), listOf(0, 1, 0, 1))
}
