package org.rsmod.content.skills.agility

import kotlin.math.sign
import org.rsmod.api.config.constants
import org.rsmod.map.CoordGrid

/** Client cycles in a game tick, for the length of an exact move. */
internal const val CYCLES_PER_TICK = 30

/** The exact move direction that faces [to] from [from]. */
internal fun facing(from: CoordGrid, to: CoordGrid): Int {
    val dx = (to.x - from.x).sign
    val dz = (to.z - from.z).sign
    return when {
        dx > 0 && dz > 0 -> constants.em_face_northeast
        dx < 0 && dz > 0 -> constants.em_face_northwest
        dx > 0 && dz < 0 -> constants.em_face_southeast
        dx < 0 && dz < 0 -> constants.em_face_southwest
        dx > 0 -> constants.em_face_east
        dx < 0 -> constants.em_face_west
        dz > 0 -> constants.em_face_north
        else -> constants.em_face_south
    }
}
