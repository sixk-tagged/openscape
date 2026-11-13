package org.rsmod.content.interfaces.grandexchange

import kotlin.math.abs
import kotlin.math.max

/**
 * The new offer's quantity and price buttons. The client changes its own copy of the varbits with
 * the same sums (cs2 777 and 778) and only tells the server which button it pressed, so these must
 * match the client exactly.
 */
object GeSetupMath {
    private const val MAX = Int.MAX_VALUE

    /**
     * [step] is -1, 1, 10, 100, or [MAX] for "+1K" (buy) and "All" (sell). A sell is capped at
     * [available]. From 1, "+10" and "+100" give 10 and 100.
     */
    fun quantity(current: Int, step: Int, sell: Boolean, available: Int): Int {
        if (sell && step == MAX) {
            return available
        }
        var change = if (step == MAX) 1000 else step
        if (change <= 0) {
            return if (current <= -change) 1 else current + change
        }
        if (change > 1 && current == 1) {
            change--
        }
        val cap = if (sell) available else MAX
        return if (cap - change < current) cap else current + change
    }

    /** Adds [percent] (negative to take off) of [current], at least 1 coin; never below 1. */
    fun percent(current: Int, percent: Int): Int {
        val change = max(1L, current.toLong() * abs(percent) / 100).toInt()
        return when {
            percent > 0 -> if (MAX - change < current) MAX else current + change
            change >= current -> 1
            else -> current - change
        }
    }

    /** "-1" and "+1" on the price. */
    fun step(current: Int, step: Int): Int =
        when {
            step > 0 -> if (current < MAX) current + 1 else current
            current > 1 -> current - 1
            else -> current
        }
}
