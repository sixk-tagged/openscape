package org.rsmod.api.death

import org.rsmod.game.inv.InvObj

/** The OSRS rules for what a player keeps on a dangerous death, and Death's fees. */
public object DeathKeep {
    public const val GRAVE_FEE_CAP: Long = 500_000

    public fun keepCount(skulled: Boolean, protectItem: Boolean): Int {
        val base = if (skulled) 0 else 3
        return if (protectItem) base + 1 else base
    }

    /**
     * Splits [carried] into (kept, lost). The [keep] dearest single units are kept, so a stack
     * gives one unit per kept slot. The sort is stable: equal prices keep [carried] order.
     */
    public fun split(
        carried: List<InvObj>,
        keep: Int,
        price: (InvObj) -> Long,
    ): Pair<List<InvObj>, List<InvObj>> {
        val sorted = carried.sortedByDescending(price).toMutableList()
        val kept = mutableListOf<InvObj>()
        var pointer = 0
        repeat(keep) {
            val obj = sorted.getOrNull(pointer) ?: return@repeat
            kept += obj.copy(count = 1)
            if (obj.count == 1) {
                pointer++
            } else {
                sorted[pointer] = obj.copy(count = obj.count - 1)
            }
        }
        return kept to sorted.drop(pointer)
    }

    /**
     * The gravestone fee for one unit worth [unitPrice]. The total is capped at [GRAVE_FEE_CAP].
     */
    public fun graveFee(unitPrice: Long): Long =
        when {
            unitPrice < 100_000 -> 0
            unitPrice < 1_000_000 -> 1_000
            unitPrice < 10_000_000 -> 10_000
            else -> 100_000
        }

    /** Death's Office fee for one unit worth [unitPrice]: 5% from 100k. */
    public fun officeFee(unitPrice: Long): Long =
        if (unitPrice < 100_000) 0 else unitPrice * 5 / 100
}
