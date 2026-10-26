package org.rsmod.routefinder

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.rsmod.routefinder.util.Rotations

class RotationsTest {
    @Test
    fun `block access flags rotate only their side bits`() {
        // Draynor's straight staircase top (fa 30 = bit 4 + E/S/W blocked) placed at angle 1:
        // only its east side stays open, which is where players climb down from.
        assertEquals(0b1101, Rotations.rotate(1, 30))
        // The bottom (fa 27) at angle 1 is open to the west only.
        assertEquals(0b0111, Rotations.rotate(1, 27))
        // At angle 0 the extra bit is dropped too.
        assertEquals(0b1110, Rotations.rotate(0, 30))
    }
}
