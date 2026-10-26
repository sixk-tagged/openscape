package org.rsmod.content.skills.smithing.scripts

import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.ring
import org.rsmod.api.player.stat.smithingLvl
import org.rsmod.api.player.ui.SKILLMULTI_OPTIONS
import org.rsmod.api.player.ui.skillMulti
import org.rsmod.api.script.onOpLoc2
import org.rsmod.api.script.onOpLocU
import org.rsmod.api.script.onOpWorn2
import org.rsmod.api.script.onPlayerQueueWithArgs
import org.rsmod.content.skills.smithing.configs.SmithingContent
import org.rsmod.content.skills.smithing.configs.SmithingObjs
import org.rsmod.content.skills.smithing.configs.SmithingQueues
import org.rsmod.content.skills.smithing.configs.SmithingSeqs
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class Smelting : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc2(SmithingContent.furnace) { chooseRecipe() }
        onOpLocU(SmithingContent.furnace) { chooseRecipe() }
        onPlayerQueueWithArgs<SmeltTask>(SmithingQueues.smelt) { smelt(it.args) }
        onOpWorn2(SmithingObjs.ring_of_forging) { checkRingOfForging() }
    }

    private suspend fun ProtectedAccess.chooseRecipe() {
        val offers =
            SmeltRecipes.all
                .filter { it.ore in inv && (it.tool == null || it.tool in inv) }
                .take(SKILLMULTI_OPTIONS)
        if (offers.isEmpty()) {
            mes(NO_ORE)
            return
        }
        val products = offers.map { it.product }
        val choice = skillMulti(SMELT_VERB, TITLE, products, products.map { ocName(it) }) ?: return
        val recipe = offers[choice.index]
        if (canSmelt(recipe)) {
            anim(SmithingSeqs.furnace)
            weakQueue(
                SmithingQueues.smelt,
                queueDelay(recipe.ticks),
                SmeltTask(recipe, choice.amount),
            )
        }
    }

    private fun ProtectedAccess.smelt(task: SmeltTask) {
        val recipe = task.recipe
        if (!canSmelt(recipe)) {
            return
        }
        invDel(inv, recipe.ore)
        recipe.secondOre?.let { invDel(inv, it) }
        takeCoal(recipe.coal)
        if (recipe == SmeltRecipes.iron && !ironSucceeds()) {
            mes(IMPURE_IRON)
        } else {
            invAdd(inv, recipe.product, recipe.makes)
            statAdvance(stats.smithing, xpFor(recipe))
            mes(doneMessage(recipe))
        }
        if (task.remaining > 1 && hasIngredients(recipe)) {
            anim(SmithingSeqs.furnace)
            weakQueue(
                SmithingQueues.smelt,
                queueDelay(recipe.ticks),
                task.copy(remaining = task.remaining - 1),
            )
        }
    }

    private fun ProtectedAccess.canSmelt(recipe: SmeltRecipe): Boolean {
        val name = withArticle(ocName(recipe.product).lowercase())
        if (player.smithingLvl < recipe.level) {
            mes("You need a Smithing level of ${recipe.level} to smelt $name.")
            return false
        }
        if (!hasOres(recipe)) {
            mes(NO_ORE)
            return false
        }
        if (coalAvailable() < recipe.coal) {
            mes("You need ${recipe.coal} coal to smelt $name.")
            return false
        }
        return true
    }

    private fun ProtectedAccess.hasIngredients(recipe: SmeltRecipe): Boolean =
        hasOres(recipe) && coalAvailable() >= recipe.coal

    private fun ProtectedAccess.hasOres(recipe: SmeltRecipe): Boolean {
        val second = recipe.secondOre
        val tool = recipe.tool
        return recipe.ore in inv &&
            (second == null || second in inv) &&
            (tool == null || tool in inv)
    }

    /** Iron is a coin flip unless a ring of forging is worn; each iron bar uses a ring charge. */
    private fun ProtectedAccess.ironSucceeds(): Boolean {
        if (!SmithingGear.hasRingOfForging(player)) {
            return random.of(1, 100) <= IRON_SUCCESS_PERCENT
        }
        player.ringOfForgingUsed++
        if (player.ringOfForgingUsed >= SmithingGear.RING_OF_FORGING_CHARGES) {
            player.ring = null
            player.ringOfForgingUsed = 0
            mes(RING_MELTED)
        }
        return true
    }

    private fun ProtectedAccess.checkRingOfForging() {
        val left = SmithingGear.RING_OF_FORGING_CHARGES - player.ringOfForgingUsed
        val pieces = if (left == 1) "piece" else "pieces"
        mes("You can smelt $left more $pieces of iron ore before your ring of forging melts.")
    }

    private fun ProtectedAccess.xpFor(recipe: SmeltRecipe): Double =
        if (recipe == SmeltRecipes.gold && SmithingGear.hasGoldsmithGauntlets(player)) {
            SmeltRecipes.GOLD_XP_WITH_GAUNTLETS
        } else {
            recipe.xp
        }

    private fun ProtectedAccess.doneMessage(recipe: SmeltRecipe): String =
        if (recipe == SmeltRecipes.cannonballs) {
            CANNONBALLS_DONE
        } else {
            "You retrieve a bar of ${ocName(recipe.product).lowercase().removeSuffix(" bar")}."
        }

    private data class SmeltTask(val recipe: SmeltRecipe, val remaining: Int)

    companion object {
        const val NO_ORE = "You don't have any ore to smelt."
        const val IMPURE_IRON = "The ore is too impure and you fail to refine it."
        const val RING_MELTED = "Your ring of forging has melted."
        const val CANNONBALLS_DONE = "You remove the cannonballs from the mould."
        private const val TITLE = "What would you like to smelt?"
        private const val SMELT_VERB = 13
        private const val IRON_SUCCESS_PERCENT = 50
    }
}
