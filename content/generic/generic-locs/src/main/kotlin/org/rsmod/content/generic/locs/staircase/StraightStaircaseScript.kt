package org.rsmod.content.generic.locs.staircase

import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Straight staircases: climbing moves the player 4 tiles through the stairs, onto the staircase's
 * first lane, one level up or down. The rule fits every placement of these types in RuneLite's
 * shortest-path transport list (Draynor, Yanille, Ardougne, Catherby, ...).
 */
class StraightStaircaseScript : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1(StraightStaircaseLocs.bottom) { climb(it.loc, levels = 1) }
        onOpLoc1(StraightStaircaseLocs.top) { climb(it.loc, levels = -1) }
        onOpLoc1(StraightStaircaseLocs.narrow_bottom) { climb(it.loc, levels = 1) }
        onOpLoc1(StraightStaircaseLocs.narrow_top) { climb(it.loc, levels = -1) }
    }

    private fun ProtectedAccess.climb(stairs: BoundLocInfo, levels: Int) {
        val x = stairs.coords.x
        val z = stairs.coords.z
        val level = coords.level + levels
        val dest =
            when {
                coords.x < x -> CoordGrid(coords.x + STEP, z, level)
                coords.x >= x + stairs.adjustedWidth -> CoordGrid(coords.x - STEP, z, level)
                coords.z < z -> CoordGrid(x, coords.z + STEP, level)
                else -> CoordGrid(x, coords.z - STEP, level)
            }
        telejump(dest)
    }

    private companion object {
        const val STEP = 4
    }
}

object StraightStaircaseLocs : LocReferences() {
    val bottom = find("stairs")
    val top = find("stairstop")
    val narrow_bottom = find("narrowstairs_wooden_bottom")
    val narrow_top = find("narrowstairs_wooden_top")
}
