package org.rsmod.api.ge

/**
 * How many of an item can be bought per 4 hours. OSRS keeps these on its servers, not in the cache,
 * so `buy-limits.tsv` (`obj<TAB>limit`) comes from the wiki's list at 2025-09-09.
 */
public object GeBuyLimits {
    private val limits: Map<Int, Int> =
        GeBuyLimits::class.java.getResourceAsStream("buy-limits.tsv")!!.bufferedReader().useLines {
            lines ->
            lines.associate { line ->
                val (obj, limit) = line.split('\t')
                obj.toInt() to limit.toInt()
            }
        }

    /** The limit of the un-noted [obj], 0 if it has none. */
    public operator fun get(obj: Int): Int = limits[obj] ?: 0
}
