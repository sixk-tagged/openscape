package org.rsmod.content.areas.misc.wilderness.rules

/**
 * Where the Wilderness' PvP area is, as boxes of absolute tiles (both ends inclusive). They are
 * packed into the `wilderness` area on every level, and are the one place the bounds live.
 * - The surface starts at y 3525: the ditch fills y 3521-3522 and the two safe steps north of it
 *   are not part of the area. It ends at the map edge.
 * - The dungeons sit 6400 tiles north of the surface (y 9920-10367).
 * - The Ferox Enclave (x 3123-3154, y 3617-3639, along its barriers) is cut out of the surface.
 */
object WildernessBounds {
    data class Box(val minX: Int, val minZ: Int, val maxX: Int, val maxZ: Int) {
        fun contains(x: Int, z: Int): Boolean = x in minX..maxX && z in minZ..maxZ
    }

    // UNVERIFIED: none of these edges are on the wiki. The x range and the y 3525 start follow the
    // ditch and safe-step locs, and the dungeon rows follow the level formula.
    val BOXES =
        listOf(
            Box(2944, 3525, 3122, 3967),
            Box(3155, 3525, 3391, 3967),
            Box(3123, 3525, 3154, 3616),
            Box(3123, 3640, 3154, 3967),
            Box(2944, 9920, 3391, 10367),
        )

    /** The Ferox Enclave inside its barriers, which the surface boxes above leave out. */
    val FEROX = Box(3123, 3617, 3154, 3639)

    fun contains(x: Int, z: Int): Boolean = BOXES.any { it.contains(x, z) }
}
