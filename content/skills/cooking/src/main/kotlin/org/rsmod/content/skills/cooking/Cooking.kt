package org.rsmod.content.skills.cooking

import jakarta.inject.Inject
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.cookingLvl
import org.rsmod.api.player.ui.SKILLMULTI_OPTIONS
import org.rsmod.api.player.ui.skillMulti
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLocU
import org.rsmod.api.script.onPlayerQueueWithArgs
import org.rsmod.api.stats.levelmod.InvisibleLevels
import org.rsmod.api.stats.xpmod.XpModifiers
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.type.loc.LocType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class Cooking
@Inject
constructor(
    private val locRepo: LocRepository,
    private val xpMods: XpModifiers,
    private val invisibleLvls: InvisibleLevels,
) : PluginScript() {
    override fun ScriptContext.startup() {
        for (fire in CookingLocs.fires) {
            onOpLocU(fire) { useOn(it.objType, it.loc, it.type, onRange = false) }
        }
        onOpLoc1(CookingLocs.fire_cook) { choose(it.loc, it.type, onRange = false) }
        for (range in CookingLocs.ranges) {
            onOpLocU(range) { useOn(it.objType, it.loc, it.type, onRange = true) }
            onOpLoc1(range) { choose(it.loc, it.type, onRange = true) }
        }
        onPlayerQueueWithArgs<CookTask>(CookingQueues.cook) { cook(it.args) }
    }

    private suspend fun ProtectedAccess.useOn(
        obj: ObjType,
        loc: BoundLocInfo,
        heat: LocType,
        onRange: Boolean,
    ) {
        val recipe = CookingRecipes.byRaw[obj.id]
        if (recipe == null) {
            mes("Nothing interesting happens.")
            return
        }
        if (recipe.rangeOnly && !onRange) {
            mes("You need to cook this on a range.")
            return
        }
        val amount =
            if (invTotal(inv, recipe.raw) > 1) {
                skillMulti(COOK_VERB, TITLE, listOf(recipe.cooked), listOf(name(recipe)))?.amount
                    ?: return
            } else {
                1
            }
        start(CookTask(recipe, loc, heat, onRange, amount))
    }

    /** The 'Cook' op offers every raw food carried that this heat source can cook. */
    private suspend fun ProtectedAccess.choose(loc: BoundLocInfo, heat: LocType, onRange: Boolean) {
        val offers =
            CookingRecipes.all
                .filter { it.raw in inv && (onRange || !it.rangeOnly) }
                .distinctBy { it.cooked }
                .take(SKILLMULTI_OPTIONS)
        if (offers.isEmpty()) {
            mes("You have nothing to cook.")
            return
        }
        val names = offers.map { name(it) }
        val choice = skillMulti(COOK_VERB, TITLE, offers.map { it.cooked }, names) ?: return
        start(CookTask(offers[choice.index], loc, heat, onRange, choice.amount))
    }

    private fun ProtectedAccess.start(task: CookTask) {
        if (!canCook(task.recipe)) {
            return
        }
        anim(if (task.onRange) CookingSeqs.range else CookingSeqs.fire)
        weakQueue(CookingQueues.cook, queueDelay(COOK_TICKS), task)
    }

    private fun ProtectedAccess.cook(task: CookTask) {
        val recipe = task.recipe
        if (!canCook(recipe) || !heatSourceExists(task)) {
            return
        }
        invDel(inv, recipe.raw)
        if (statRandom(stats.cooking, rateFor(task).low, rateFor(task).high, invisibleLvls)) {
            invAdd(inv, recipe.cooked)
            statAdvance(stats.cooking, recipe.xp * xpMods.get(player, stats.cooking))
            spam(recipe.cookedMessage ?: "You successfully cook ${withArticle(name(recipe))}.")
        } else {
            invAdd(inv, recipe.burnt)
            spam(recipe.burntMessage ?: "You accidentally burn the ${name(recipe)}.")
        }
        if (task.remaining > 1 && recipe.raw in inv) {
            anim(if (task.onRange) CookingSeqs.range else CookingSeqs.fire)
            val next = task.copy(remaining = task.remaining - 1)
            weakQueue(CookingQueues.cook, queueDelay(COOK_TICKS), next)
        }
    }

    private fun ProtectedAccess.canCook(recipe: CookingRecipes.Recipe): Boolean {
        if (player.cookingLvl < recipe.level) {
            mes("You need a Cooking level of ${recipe.level} to cook this.")
            return false
        }
        return recipe.raw in inv
    }

    /** Fires burn out mid-batch; ranges never go away. */
    private fun heatSourceExists(task: CookTask): Boolean =
        task.onRange || locRepo.findExact(task.loc.coords, task.heat) != null

    private fun ProtectedAccess.rateFor(task: CookTask): CookingRecipes.Rate {
        val recipe = task.recipe
        val gauntlets = recipe.gauntlets
        if (gauntlets != null && CookingObjs.cooking_gauntlets in player.worn) {
            return gauntlets
        }
        return if (task.onRange) recipe.range else recipe.fire
    }

    private fun ProtectedAccess.name(recipe: CookingRecipes.Recipe): String =
        ocName(recipe.cooked).lowercase()

    private fun withArticle(name: String): String =
        if (name.endsWith("s")) "some $name" else "a $name"

    private data class CookTask(
        val recipe: CookingRecipes.Recipe,
        val loc: BoundLocInfo,
        val heat: LocType,
        val onRange: Boolean,
        val remaining: Int,
    )

    private companion object {
        /** Cache enum 1809: verb 7 reads "Cook" and keeps the 1-28 amount clamp. */
        const val COOK_VERB = 7
        const val TITLE = "What would you like to cook?"
        const val COOK_TICKS = 4

        /** A weak queue added while handling input fires one tick early, so ask for one more. */
        fun queueDelay(ticks: Int): Int = ticks + 1
    }
}
