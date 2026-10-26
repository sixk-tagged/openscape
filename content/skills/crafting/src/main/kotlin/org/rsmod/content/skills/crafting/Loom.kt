package org.rsmod.content.skills.crafting

import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpHeldU
import org.rsmod.api.script.onOpLoc2
import org.rsmod.api.script.onOpLocU
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** The loom and battlestaff making. */
class Loom : PluginScript() {
    override fun ScriptContext.startup() {
        for (loom in CraftingLocs.looms) {
            onOpLoc2(loom) { weave() }
            onOpLocU(loom) { weave() }
        }
        for ((orb, recipe) in LoomRecipes.battlestaves) {
            onOpHeldU(CraftingObjs.battlestaff, orb) { offer(Verb.MAKE, "make", listOf(recipe)) }
        }
    }

    private suspend fun ProtectedAccess.weave() {
        val offers =
            LoomRecipes.weaving.filter { recipe ->
                recipe.inputs.all { invTotal(inv, it.obj) >= it.count }
            }
        if (offers.isEmpty()) {
            mes("You do not have anything to weave.")
            return
        }
        offer(Verb.MAKE, "make", offers)
    }
}

/** Loom and battlestaff recipes (OSRS wiki levels and xp). */
object LoomRecipes : ObjReferences() {
    // The loom's first item takes 4 ticks and the rest 3; every item takes 3 here.
    private fun weave(product: String, level: Int, xp: Double, input: String, count: Int) =
        CraftRecipe(
            find(product),
            level,
            xp,
            listOf(find(input).x(count)),
            ticks = 3,
            anim = CraftingSeqs.loom,
        )

    val weaving =
        listOf(
            weave("regicide_cloth", 10, 12.0, "ball_of_wool", 4),
            weave("sack_empty", 21, 38.0, "jute_fibre", 4),
            weave("fossil_drift_net", 26, 55.0, "jute_fibre", 2),
            weave("basket_empty", 36, 56.0, "willow_branch", 6),
        )

    private fun staff(element: String, level: Int, xp: Double) =
        find("${element}_orb") to
            CraftRecipe(
                find("${element}_battlestaff"),
                level,
                xp,
                listOf(CraftingObjs.battlestaff.x(), find("${element}_orb").x()),
                // The wiki: the first staff of a Make-X takes 3 ticks, the rest 2.
                ticks = 2,
                anim = CraftingSeqs.battlestaff,
            )

    val battlestaves =
        listOf(
            staff("water", 54, 100.0),
            staff("earth", 58, 112.5),
            staff("fire", 62, 125.0),
            staff("air", 66, 137.5),
        )
}
