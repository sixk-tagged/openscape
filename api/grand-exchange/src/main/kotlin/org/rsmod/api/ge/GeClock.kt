package org.rsmod.api.ge

/** Wall clock for buy-limit windows; replaced in tests. */
public fun interface GeClock {
    public fun millis(): Long
}

public object SystemGeClock : GeClock {
    override fun millis(): Long = System.currentTimeMillis()
}
