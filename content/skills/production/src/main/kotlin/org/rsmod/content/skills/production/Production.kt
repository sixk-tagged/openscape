package org.rsmod.content.skills.production

import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.stat
import org.rsmod.api.player.ui.SKILLMULTI_OPTIONS
import org.rsmod.api.player.ui.skillMulti
import org.rsmod.api.script.onOpHeldU
import org.rsmod.api.script.onPlayerQueueWithArgs
import org.rsmod.api.type.refs.queue.QueueReferences
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.seq.SeqType
import org.rsmod.game.type.stat.StatType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** [count] of [obj] in a recipe. */
class Ingredient(val obj: ObjType, val count: Int = 1)

fun ObjType.x(count: Int = 1): Ingredient = Ingredient(this, count)

/**
 * One production action for Fletching, Herblore and the like. [inputs] are used up, [tools] only
 * have to be carried. [xp] and [makes] are for one unit: a recipe made in sets ([batch] units per
 * action, e.g. 15 arrows) makes a smaller set when the materials fall short. [rollMakes], if set,
 * rolls the pieces one unit makes in place of [makes].
 */
class Recipe(
    val stat: StatType,
    val product: ObjType,
    val level: Int,
    val xp: Double,
    val inputs: List<Ingredient>,
    val tools: List<ObjType> = emptyList(),
    val makes: Int = 1,
    val batch: Int = 1,
    val ticks: Int = 2,
    val anim: SeqType? = null,
    val message: String? = null,
    val levelMessage: String? = null,
    val rollMakes: (ProtectedAccess.() -> Int)? = null,
)

/** Using [a] on [b] makes [recipe], right away or through a one-option make-X [verb] prompt. */
class Combo(
    val a: ObjType,
    val b: ObjType,
    val recipe: Recipe,
    val verb: Int? = null,
    val title: String = "make",
)

/** Cache enum 1809 verbs for the make-X chatbox. */
object Verb {
    const val MAKE = 0
    const val STRING = 10
    const val CUT = 12
}

/** Repeat until the materials run out. */
const val ALL = Int.MAX_VALUE

object ProductionQueues : QueueReferences() {
    val make = find("production_make")
}

internal data class Task(val recipe: Recipe, val remaining: Int)

class SkillProduction : PluginScript() {
    override fun ScriptContext.startup() {
        onPlayerQueueWithArgs<Task>(ProductionQueues.make) { step(it.args) }
    }
}

/** Registers every pair in [combos]; each pair may only appear once across all scripts. */
fun ScriptContext.register(combos: List<Combo>) {
    for (combo in combos) {
        onOpHeldU(combo.a, combo.b) {
            if (combo.verb == null) {
                start(combo.recipe, ALL)
            } else {
                offer(combo.verb, "What would you like to ${combo.title}?", listOf(combo.recipe))
            }
        }
    }
}

/** Offers [recipes] in the make-X chatbox and starts the one the player picks. */
suspend fun ProtectedAccess.offer(verb: Int, title: String, recipes: List<Recipe>) {
    val menu = recipes.take(SKILLMULTI_OPTIONS)
    val products = menu.map { it.product }
    val choice = skillMulti(verb, title, products, products.map { ocName(it) }) ?: return
    start(menu[choice.index], choice.amount)
}

/** Starts up to [amount] actions of [recipe]; the first lands after the recipe's ticks. */
fun ProtectedAccess.start(recipe: Recipe, amount: Int) {
    ifClose() // Clears weak queues: must come before the queue below.
    problem(recipe)?.let {
        mes(it)
        return
    }
    recipe.anim?.let { anim(it) }
    // A weak queue added while handling input fires one tick early, so ask for one more.
    weakQueue(ProductionQueues.make, recipe.ticks + 1, Task(recipe, amount))
}

private fun ProtectedAccess.step(task: Task) {
    val recipe = task.recipe
    val units = units(recipe)
    if (problem(recipe) != null) {
        resetAnim()
        return
    }
    for (input in recipe.inputs) {
        invDel(inv, input.obj, input.count * units)
    }
    val made = (recipe.rollMakes?.invoke(this) ?: recipe.makes) * units
    if (!invAdd(inv, recipe.product, made).success) {
        // No room: put the inputs back rather than lose them.
        for (input in recipe.inputs) {
            invAdd(inv, input.obj, input.count * units)
        }
        mes("You don't have enough inventory space.")
        resetAnim()
        return
    }
    if (recipe.xp > 0.0) {
        statAdvance(recipe.stat, recipe.xp * units)
    }
    mes(message(recipe, made))
    if (task.remaining > 1 && problem(recipe) == null) {
        recipe.anim?.let { anim(it) }
        weakQueue(ProductionQueues.make, recipe.ticks + 1, Task(recipe, task.remaining - 1))
    } else {
        resetAnim()
    }
}

/** [Recipe.message], with `#` the [made] pieces and `{0}`, `{1}`.. the lowercase input names. */
private fun ProtectedAccess.message(recipe: Recipe, made: Int): String {
    var text =
        recipe.message?.replace("#", "$made") ?: "You make ${ocName(recipe.product).lowercase()}."
    recipe.inputs.forEachIndexed { i, input ->
        text = text.replace("{$i}", ocName(input.obj).lowercase())
    }
    return text
}

/** How many units of [recipe] the inventory holds the materials for, up to its batch. */
private fun ProtectedAccess.units(recipe: Recipe): Int =
    recipe.inputs.minOf { invTotal(inv, it.obj) / it.count }.coerceIn(0, recipe.batch)

/** Why [recipe] can't be made right now, or null. */
private fun ProtectedAccess.problem(recipe: Recipe): String? {
    if (player.stat(recipe.stat) < recipe.level) {
        return recipe.levelMessage
            ?: "You need a ${recipe.stat.displayName} level of ${recipe.level} to make that."
    }
    val tool = recipe.tools.firstOrNull { it !in inv }
    if (tool != null) {
        return "You need a ${ocName(tool).lowercase()} to do that."
    }
    val short = recipe.inputs.firstOrNull { invTotal(inv, it.obj) < it.count }
    if (short != null) {
        return "You don't have enough ${ocName(short.obj).lowercase()} to make that."
    }
    return null
}
