package org.rsmod.content.skills.crafting

import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.craftingLvl
import org.rsmod.api.player.ui.skillMulti
import org.rsmod.api.script.onOpLoc2
import org.rsmod.api.script.onOpLocU
import org.rsmod.api.script.onPlayerQueueWithArgs
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.isType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** Spinning wheel: wool, flax, sinew/roots, magic roots and yak hair (OSRS wiki levels and xp). */
class Spinning : PluginScript() {
    override fun ScriptContext.startup() {
        for (wheel in CraftingLocs.spinning_wheels) {
            onOpLoc2(wheel) { choose() }
            onOpLocU(wheel) { event ->
                if (
                    SpinRecipes.all.any { recipe -> recipe.inputs.any { event.objType.isType(it) } }
                ) {
                    choose()
                } else {
                    mes("Nothing interesting happens.")
                }
            }
        }
        onPlayerQueueWithArgs<SpinTask>(CraftingQueues.spin) { spin(it.args) }
    }

    private suspend fun ProtectedAccess.choose() {
        val offers = SpinRecipes.all.filter { inputFor(it) != null }
        if (offers.isEmpty()) {
            mes("You don't have anything to spin.")
            return
        }
        val products = offers.map { it.product }
        val choice = skillMulti(SPIN_VERB, TITLE, products, products.map { ocName(it) }) ?: return
        val recipe = offers[choice.index]
        if (canSpin(recipe)) {
            anim(CraftingSeqs.spin)
            weakQueue(CraftingQueues.spin, queueDelay(SPIN_TICKS), SpinTask(recipe, choice.amount))
        }
    }

    private fun ProtectedAccess.spin(task: SpinTask) {
        val recipe = task.recipe
        val input = inputFor(recipe) ?: return
        if (!canSpin(recipe)) {
            return
        }
        invDel(inv, input)
        invAdd(inv, recipe.product)
        statAdvance(stats.crafting, recipe.xp)
        if (task.remaining > 1 && inputFor(recipe) != null) {
            anim(CraftingSeqs.spin)
            val next = task.copy(remaining = task.remaining - 1)
            weakQueue(CraftingQueues.spin, queueDelay(SPIN_TICKS), next)
        }
    }

    private fun ProtectedAccess.canSpin(recipe: SpinRecipes.Recipe): Boolean {
        if (player.craftingLvl < recipe.level) {
            val name = ocName(recipe.product).lowercase()
            mes("You need a Crafting level of ${recipe.level} to make a $name.")
            return false
        }
        return true
    }

    private fun ProtectedAccess.inputFor(recipe: SpinRecipes.Recipe): ObjType? =
        recipe.inputs.firstOrNull { it in inv }

    private data class SpinTask(val recipe: SpinRecipes.Recipe, val remaining: Int)

    private companion object {
        /** Cache enum 1809: verb 16 reads "Spin". */
        const val SPIN_VERB = 16
        const val TITLE = "What would you like to spin?"
        const val SPIN_TICKS = 3

        /** A weak queue added while handling input fires one tick early, so ask for one more. */
        fun queueDelay(ticks: Int): Int = ticks + 1
    }
}

object SpinRecipes {
    data class Recipe(
        val product: ObjType,
        val inputs: List<ObjType>,
        val level: Int,
        val xp: Double,
    )

    val all =
        with(CraftingObjs) {
            listOf(
                Recipe(ball_of_wool, listOf(wool), 1, 2.5),
                Recipe(bow_string, listOf(flax), 10, 15.0),
                Recipe(
                    crossbow_string,
                    listOf(sinew, oak_roots, willow_roots, maple_roots, yew_roots),
                    10,
                    15.0,
                ),
                Recipe(magic_string, listOf(magic_roots), 19, 30.0),
                Recipe(rope, listOf(yak_hair), 30, 25.0),
            )
        }
}
