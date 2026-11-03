package org.rsmod.content.areas.misc.wilderness.rules

import org.rsmod.api.config.refs.areas
import org.rsmod.api.type.builders.map.area.MapAreaBuilder
import org.rsmod.game.area.polygon.VertexCoord

object WildernessMapAreas : MapAreaBuilder() {
    override fun onPackMapTask() {
        area(areas.wilderness) {
            for (box in WildernessBounds.BOXES) {
                packBox(box)
            }
        }
    }
}

/** Packs [box] on every level, as one rectangle per map square it touches. */
private fun MapAreaBuilder.AreaBuilder.packBox(box: WildernessBounds.Box) {
    for (mx in box.minX / SQUARE..box.maxX / SQUARE) {
        for (mz in box.minZ / SQUARE..box.maxZ / SQUARE) {
            val west = maxOf(box.minX, mx * SQUARE) - mx * SQUARE
            val east = minOf(box.maxX, mx * SQUARE + SQUARE - 1) - mx * SQUARE
            val south = maxOf(box.minZ, mz * SQUARE) - mz * SQUARE
            val north = minOf(box.maxZ, mz * SQUARE + SQUARE - 1) - mz * SQUARE
            polygon(levels = 0..3) {
                vertex(VertexCoord(mx, mz, west, south))
                vertex(VertexCoord(mx, mz, east, south))
                vertex(VertexCoord(mx, mz, east, north))
                vertex(VertexCoord(mx, mz, west, north))
            }
        }
    }
}

private const val SQUARE = 64
