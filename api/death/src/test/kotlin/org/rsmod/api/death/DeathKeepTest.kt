@file:OptIn(UncheckedType::class)

package org.rsmod.api.death

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.util.UncheckedType

class DeathKeepTest {
    @Test
    fun `keep count follows skull and protect item`() {
        assertEquals(3, DeathKeep.keepCount(skulled = false, protectItem = false))
        assertEquals(4, DeathKeep.keepCount(skulled = false, protectItem = true))
        assertEquals(0, DeathKeep.keepCount(skulled = true, protectItem = false))
        assertEquals(1, DeathKeep.keepCount(skulled = true, protectItem = true))
    }

    @Test
    fun `split keeps the dearest single units`() {
        val prices = mapOf(1 to 10L, 2 to 50L, 3 to 30L, 4 to 40L, 5 to 20L)
        val carried = (1..5).map { InvObj(it, 1) }
        val (kept, lost) = DeathKeep.split(carried, keep = 3) { prices.getValue(it.id) }
        assertEquals(listOf(2, 4, 3), kept.map { it.id })
        assertEquals(setOf(5, 1), lost.map { it.id }.toSet())
    }

    @Test
    fun `a stack gives one unit per kept slot`() {
        val prices = mapOf(1 to 1_000L, 2 to 5L)
        val carried = listOf(InvObj(2, 1), InvObj(1, 100))
        val (kept, lost) = DeathKeep.split(carried, keep = 3) { prices.getValue(it.id) }
        assertEquals(listOf(InvObj(1, 1), InvObj(1, 1), InvObj(1, 1)), kept)
        assertEquals(listOf(InvObj(1, 97), InvObj(2, 1)), lost)
    }

    @Test
    fun `ties keep carried order`() {
        val carried = listOf(InvObj(7, 1), InvObj(8, 1), InvObj(9, 1))
        val (kept, lost) = DeathKeep.split(carried, keep = 1) { 100L }
        assertEquals(listOf(7), kept.map { it.id })
        assertEquals(listOf(8, 9), lost.map { it.id })
    }

    @Test
    fun `keeping nothing loses everything`() {
        val carried = listOf(InvObj(1, 5))
        val (kept, lost) = DeathKeep.split(carried, keep = 0) { 1L }
        assertEquals(emptyList<InvObj>(), kept)
        assertEquals(carried, lost)
    }

    @Test
    fun `grave fee tiers`() {
        assertEquals(0L, DeathKeep.graveFee(99_999))
        assertEquals(1_000L, DeathKeep.graveFee(100_000))
        assertEquals(10_000L, DeathKeep.graveFee(1_000_000))
        assertEquals(100_000L, DeathKeep.graveFee(10_000_000))
        assertEquals(500_000L, DeathKeep.GRAVE_FEE_CAP)
    }

    @Test
    fun `office fee is five percent from 100k`() {
        assertEquals(0L, DeathKeep.officeFee(99_999))
        assertEquals(10_000L, DeathKeep.officeFee(200_000))
    }
}
