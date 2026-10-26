package org.rsmod.content.skills.crafting

import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpHeldU
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLocU
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.isType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** Soft clay, the potter's wheel and the pottery oven. */
class Pottery : PluginScript() {
    override fun ScriptContext.startup() {
        for (combo in PotteryRecipes.softClay) {
            onOpHeldU(combo.a, combo.b) { offer(Verb.MAKE, "make", listOf(combo.recipe)) }
        }
        for (wheel in CraftingLocs.potters_wheels) {
            onOpLoc1(wheel) { shape() }
            onOpLocU(wheel) {
                if (it.objType.isType(CraftingObjs.soft_clay)) shape() else mes(NOTHING_HAPPENS)
            }
        }
        for (oven in CraftingLocs.pottery_ovens) {
            onOpLoc1(oven) { fire() }
            onOpLocU(oven) {
                if (PotteryRecipes.isUnfired(it.objType)) fire() else mes(NOTHING_HAPPENS)
            }
        }
    }

    private suspend fun ProtectedAccess.shape() {
        if (CraftingObjs.soft_clay !in inv) {
            mes("You need some soft clay to use on the wheel.")
            return
        }
        offer(Verb.MAKE, "make", PotteryRecipes.shaping)
    }

    private suspend fun ProtectedAccess.fire() {
        val offers = PotteryRecipes.firing.filter { recipe -> recipe.inputs.all { it.obj in inv } }
        if (offers.isEmpty()) {
            mes("You don't have anything to fire in the oven.")
            return
        }
        offer(Verb.FIRE, "fire", offers)
    }
}

/** Pottery (OSRS wiki levels and xp): shaping takes three ticks, firing seven. */
object PotteryRecipes : ObjReferences() {
    private val clay = CraftingObjs.clay

    /** Clay on a full water container; the container comes back empty. */
    val softClay =
        listOf("bucket", "bowl", "jug", "vial").map { container ->
            Combo(
                clay,
                find("${container}_water"),
                CraftRecipe(
                    CraftingObjs.soft_clay,
                    1,
                    0.0,
                    listOf(clay.x(), find("${container}_water").x()),
                    returns = listOf(find("${container}_empty").x()),
                    ticks = 2,
                    message = "You mix the clay and water. You now have some soft, workable clay.",
                ),
            )
        }

    private class Piece(
        val name: String,
        val unfired: String,
        val fired: String,
        val level: Int,
        val shapeXp: Double,
        val fireXp: Double,
        val makes: Int = 1,
    )

    private val pieces =
        listOf(
            Piece("pot", "pot_unfired", "pot_empty", 1, 6.3, 6.3),
            Piece("cup", "cup_unfired", "cup_empty", 3, 8.5, 8.5, makes = 4),
            Piece("pie dish", "piedish_unfired", "piedish", 7, 15.0, 10.0),
            Piece("bowl", "bowl_unfired", "bowl_empty", 8, 18.0, 15.0),
            Piece("plant pot", "plantpot_unfired", "plantpot_empty", 19, 20.0, 17.5),
            Piece("pot lid", "potlid_unfired", "potlid", 25, 20.0, 20.0),
        )

    val shaping =
        pieces.map {
            CraftRecipe(
                find(it.unfired),
                it.level,
                it.shapeXp,
                listOf(CraftingObjs.soft_clay.x()),
                makes = it.makes,
                anim = CraftingSeqs.potters_wheel,
                message = "You make the soft clay into ${article(it.name)}.",
            )
        }

    val firing =
        pieces.map {
            CraftRecipe(
                find(it.fired),
                1,
                it.fireXp,
                listOf(find(it.unfired).x()),
                ticks = 7,
                // UNVERIFIED: the oven animation, and the crack chance (low confidence on the
                // wiki).
                anim = CraftingSeqs.furnace,
                message = "You remove the ${it.name} from the oven.",
                spoil = Spoil(180, 800, "The ${it.name} cracks in the oven."),
            )
        }

    fun isUnfired(item: ObjType): Boolean =
        firing.any { recipe -> recipe.inputs.any { item.isType(it.obj) } }
}
