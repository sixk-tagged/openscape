package org.rsmod.content.skills.cooking

import jakarta.inject.Inject
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.cookingLvl
import org.rsmod.api.player.ui.skillMulti
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLocU
import org.rsmod.api.script.onPlayerQueueWithArgs
import org.rsmod.api.stats.xpmod.XpModifiers
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.seq.SeqType
import org.rsmod.game.type.seq.SeqTypeList
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** Dairy churn: milk, cream or butter into cream, butter or cheese (OSRS wiki levels and xp). */
class Churning
@Inject
constructor(private val seqTypes: SeqTypeList, private val xpMods: XpModifiers) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1(CookingLocs.dairy_churn) { choose() }
        onOpLocU(CookingLocs.dairy_churn) { choose() }
        onPlayerQueueWithArgs<ChurnTask>(CookingQueues.churn) { churn(it.args) }
    }

    private suspend fun ProtectedAccess.choose() {
        val products = PRODUCTS.filter { recipeFor(it) != null }
        if (products.isEmpty()) {
            mes("You need some milk, cream or butter to use the churn.")
            return
        }
        val names = products.map { ocName(it.obj) }
        val choice = skillMulti(MAKE_VERB, TITLE, products.map { it.obj }, names) ?: return
        val product = products[choice.index]
        val recipe = recipeFor(product) ?: return
        if (player.cookingLvl < recipe.level) {
            mes("You need a Cooking level of ${recipe.level} to make that.")
            return
        }
        if (!hasRoom(recipe)) {
            mes("You don't have enough inventory space to do that.")
            return
        }
        anim(product.anim)
        weakQueue(CookingQueues.churn, queueDelay(product.anim), ChurnTask(product, choice.amount))
    }

    private fun ProtectedAccess.churn(task: ChurnTask) {
        val product = task.product
        val recipe = recipeFor(product) ?: return
        if (player.cookingLvl < recipe.level || !hasRoom(recipe)) {
            return
        }
        invDel(inv, recipe.input)
        recipe.leftover?.let { invAdd(inv, it) }
        invAdd(inv, product.obj)
        statAdvance(stats.cooking, recipe.xp * xpMods.get(player, stats.cooking))
        spam(product.message)
        if (task.remaining > 1 && recipeFor(product) != null) {
            anim(product.anim)
            val next = task.copy(remaining = task.remaining - 1)
            weakQueue(CookingQueues.churn, queueDelay(product.anim), next)
        }
    }

    /** Uses the least-processed input carried: milk first, then cream, then butter. */
    private fun ProtectedAccess.recipeFor(product: Product): Recipe? =
        RECIPES.firstOrNull { it.output == product.obj && it.input in inv }

    /** The leftover bucket or pot takes the input's slot, so the product needs a free one. */
    private fun ProtectedAccess.hasRoom(recipe: Recipe): Boolean =
        recipe.leftover == null || !inv.isFull()

    /** A weak queue added while handling input fires one tick early, so ask for one more. */
    private fun queueDelay(anim: SeqType): Int = seqTypes[anim].tickDuration + 1

    private data class Product(val obj: ObjType, val anim: SeqType, val message: String)

    private data class Recipe(
        val input: ObjType,
        val output: ObjType,
        val leftover: ObjType?,
        val level: Int,
        val xp: Double,
    )

    private data class ChurnTask(val product: Product, val remaining: Int)

    private companion object {
        /** Cache enum 1809 has no "Churn" verb; 0 reads "Make". */
        const val MAKE_VERB = 0
        const val TITLE = "What would you like to make?"

        val PRODUCTS =
            with(CookingObjs) {
                listOf(
                    Product(pot_of_cream, CookingSeqs.churn_short, "You make a pot of cream."),
                    Product(pat_of_butter, CookingSeqs.churn_medium, "You make a pat of butter."),
                    Product(cheese, CookingSeqs.churn_long, "You make some cheese."),
                )
            }

        val RECIPES =
            with(CookingObjs) {
                listOf(
                    Recipe(bucket_of_milk, pot_of_cream, bucket, 21, 18.0),
                    Recipe(bucket_of_milk, pat_of_butter, bucket, 38, 40.5),
                    Recipe(bucket_of_milk, cheese, bucket, 48, 64.0),
                    Recipe(pot_of_cream, pat_of_butter, pot, 38, 22.5),
                    Recipe(pot_of_cream, cheese, pot, 48, 46.0),
                    Recipe(pat_of_butter, cheese, null, 48, 23.5),
                )
            }
    }
}
