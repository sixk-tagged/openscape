package org.rsmod.content.skills.agility

import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLoc2
import org.rsmod.content.skills.agility.configs.AgilityLocs
import org.rsmod.content.skills.agility.configs.AgilitySeqs
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The trapdoor in Burthorpe's inn down to the Rogues' Den (Grace's shop), and the spirit trees of
 * the Tree Gnome Village, the Gnome Stronghold, the Battlefield of Khazard and the Grand Exchange
 * (OSRS wiki; the quest requirements count as met, so every tree goes to every other).
 */
class AgilityTravel : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1(AgilityLocs.roguesden_trapdoor) { climb(DEN) }
        onOpLoc1(AgilityLocs.roguesden_passage) { climb(INN) }
        val all = listOf(SMALL_1, SMALL_2, BIG_1, BIG_2, BIG_ORBS)
        for (tree in all) {
            onOpLoc2(tree) { travel() }
        }
        for (tree in listOf(SMALL_2, BIG_2, BIG_ORBS)) {
            onOpLoc1(tree) { travel() }
        }
    }

    // UNVERIFIED: the arrival tiles (the den's is next to its passage, the inn's is the trapdoor).
    private suspend fun ProtectedAccess.climb(dest: CoordGrid) {
        arriveDelay()
        anim(AgilitySeqs.climb)
        delay(2)
        telejump(dest)
    }

    // UNVERIFIED: the menu's wording and the arrival tiles (next to each tree).
    private suspend fun ProtectedAccess.travel() {
        val dest =
            choice4(
                "Tree Gnome Village",
                TREE_GNOME_VILLAGE,
                "Gnome Stronghold",
                GNOME_STRONGHOLD,
                "Battlefield of Khazard",
                BATTLEFIELD_OF_KHAZARD,
                "Grand Exchange",
                GRAND_EXCHANGE,
                title = "Where would you like to travel?",
            )
        delay(2)
        telejump(dest)
    }

    private companion object {
        val DEN = CoordGrid(3061, 4985, 1)
        val INN = CoordGrid(2905, 3537, 0)
        val TREE_GNOME_VILLAGE = CoordGrid(2542, 3170, 0)
        val GNOME_STRONGHOLD = CoordGrid(2461, 3444, 0)
        val BATTLEFIELD_OF_KHAZARD = CoordGrid(2555, 3259, 0)
        val GRAND_EXCHANGE = CoordGrid(3184, 3508, 0)
        val SMALL_1 = AgilityLocs.spirit_tree_small_1
        val SMALL_2 = AgilityLocs.spirit_tree_small_2
        val BIG_1 = AgilityLocs.spirit_tree_big_1
        val BIG_2 = AgilityLocs.spirit_tree_big_2
        val BIG_ORBS = AgilityLocs.spirit_tree_big_orbs
    }
}
