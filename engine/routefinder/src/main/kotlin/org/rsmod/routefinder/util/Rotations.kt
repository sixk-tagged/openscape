package org.rsmod.routefinder.util

public object Rotations {
    public fun rotate(angle: Int, dimensionA: Int, dimensionB: Int): Int =
        if (angle and 0x1 != 0) {
            dimensionB
        } else {
            dimensionA
        }

    public fun rotate(angle: Int, blockAccessFlags: Int): Int {
        // Only the low 4 bits are sides (N 1, E 2, S 4, W 8). Cache values such as 27 or 30 also
        // carry bit 4, which must not be rotated into the side bits.
        val sides = blockAccessFlags and 0xF
        return if (angle == 0) sides else ((sides shl angle) and 0xF) or (sides shr (4 - angle))
    }
}
