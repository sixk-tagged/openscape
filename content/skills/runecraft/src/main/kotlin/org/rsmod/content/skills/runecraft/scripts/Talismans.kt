package org.rsmod.content.skills.runecraft.scripts

import kotlin.math.atan2
import kotlin.math.roundToInt
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpHeld4
import org.rsmod.content.skills.runecraft.configs.Altar
import org.rsmod.content.skills.runecraft.configs.Altars
import org.rsmod.content.skills.runecraft.configs.RunecraftObjs
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * `Locate` on a talisman: which way the nearest matching mysterious ruins lies, as one of the eight
 * compass points. The elemental talisman covers the four elemental ruins, and the catalytic one
 * does nothing. UNVERIFIED: the wiki gives the wording only for the elemental talisman, and says
 * nothing about the distance used or about being underground.
 */
class Talismans : PluginScript() {
    override fun ScriptContext.startup() {
        for (altar in Altars.withRuins) {
            val talisman = checkNotNull(altar.binding).talisman
            onOpHeld4(talisman) { locate(listOf(altar)) }
        }
        onOpHeld4(RunecraftObjs.elemental_talisman) { locate(Altars.elementalRuins) }
        onOpHeld4(RunecraftObjs.catalytic_talisman) { mes(NOTHING) }
    }

    private fun ProtectedAccess.locate(altars: List<Altar>) {
        val here = coords
        val targets = altars.map { checkNotNull(it.ruins).coords }
        // Underground and overworld positions share no direction.
        val reachable = targets.filter { sameWorld(here, it) }
        val nearest = reachable.minOfOrNull { distanceSquared(here, it) }
        val closest = reachable.filter { distanceSquared(here, it) == nearest }
        if (closest.size != 1) {
            mes(UNSURE)
            return
        }
        mes("The talisman pulls to the ${compass(here, closest.single())}.")
    }

    private fun sameWorld(from: CoordGrid, to: CoordGrid): Boolean =
        from.level == 0 && band(from.z) == band(to.z)

    /** Overworld, the 4000s (Zanaris, altar rooms) and the underground are separate maps. */
    private fun band(z: Int): Int = if (z < 4000) 0 else if (z < 9000) 1 else 2

    private fun distanceSquared(from: CoordGrid, to: CoordGrid): Long {
        val dx = (to.x - from.x).toLong()
        val dz = (to.z - from.z).toLong()
        return dx * dx + dz * dz
    }

    private fun compass(from: CoordGrid, to: CoordGrid): String {
        val angle = Math.toDegrees(atan2((to.z - from.z).toDouble(), (to.x - from.x).toDouble()))
        // 0 degrees is east; each point covers 45 degrees.
        return POINTS[((angle / 45).roundToInt() + 8) % 8]
    }

    private companion object {
        const val NOTHING = "Nothing interesting happens."
        const val UNSURE = "You cannot tell which direction the Talisman is pulling..."
        val POINTS =
            listOf(
                "East",
                "North-East",
                "North",
                "North-West",
                "West",
                "South-West",
                "South",
                "South-East",
            )
    }
}
