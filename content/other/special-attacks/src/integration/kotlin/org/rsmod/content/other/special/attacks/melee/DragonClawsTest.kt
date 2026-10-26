package org.rsmod.content.other.special.attacks.melee

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DragonClawsTest {
    @Test
    fun `the first swing landing splits the hit four ways`() {
        assertEquals(listOf(40, 20, 10, 11), DragonClaws.hits(landedOn = 1, firstHit = 40))
    }

    @Test
    fun `the second swing landing skips the first hitsplat`() {
        assertEquals(listOf(0, 30, 15, 16), DragonClaws.hits(landedOn = 2, firstHit = 30))
    }

    @Test
    fun `the third swing landing hits twice, the second for one more`() {
        assertEquals(listOf(0, 0, 25, 26), DragonClaws.hits(landedOn = 3, firstHit = 25))
    }

    @Test
    fun `the fourth swing landing puts everything on the last hitsplat`() {
        assertEquals(listOf(0, 0, 0, 60), DragonClaws.hits(landedOn = 4, firstHit = 60))
    }

    @Test
    fun `each landing swing has its OSRS damage range`() {
        val max = 48
        assertEquals(24..47, DragonClaws.firstHitRange(max, landedOn = 1))
        assertEquals(18..42, DragonClaws.firstHitRange(max, landedOn = 2))
        assertEquals(12..36, DragonClaws.firstHitRange(max, landedOn = 3))
        assertEquals(12..60, DragonClaws.firstHitRange(max, landedOn = 4))
    }
}
