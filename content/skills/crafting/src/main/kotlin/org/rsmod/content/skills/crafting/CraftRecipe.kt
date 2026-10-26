package org.rsmod.content.skills.crafting

import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.craftingLvl
import org.rsmod.api.player.ui.SKILLMULTI_OPTIONS
import org.rsmod.api.player.ui.skillMulti
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.script.onOpHeldU
import org.rsmod.api.script.onPlayerQueueWithArgs
import org.rsmod.game.entity.Player
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.seq.SeqType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** [count] of [obj] in a recipe. */
class Ingredient(val obj: ObjType, val count: Int = 1)

fun ObjType.x(count: Int = 1): Ingredient = Ingredient(this, count)

/**
 * A chance to ruin the item instead of making it, as the `low`/`high` pair of RSMod's `statRandom`.
 * A ruined item is still used up; it may leave [product] and give [xp].
 */
class Spoil(
    val low: Int,
    val high: Int,
    val message: String,
    val product: ObjType? = null,
    val xp: Double = 0.0,
)

/**
 * One crafting action. [inputs] are used up, [tools] only have to be carried (a needle recipe sets
 * [thread] instead: needle and thread, or a costume needle, and one use of thread per action). The
 * action repeats every [ticks] ticks while the materials last.
 */
class CraftRecipe(
    val product: ObjType,
    val level: Int,
    val xp: Double,
    val inputs: List<Ingredient>,
    val tools: List<ObjType> = emptyList(),
    val thread: Boolean = false,
    val makes: Int = 1,
    val returns: List<Ingredient> = emptyList(),
    val ticks: Int = 3,
    val anim: SeqType? = null,
    val message: String? = null,
    val levelMessage: String? = null,
    val spoil: Spoil? = null,
)

/** Using [a] on [b] starts one action of [recipe]. */
class Combo(val a: ObjType, val b: ObjType, val recipe: CraftRecipe)

internal data class CraftTask(val recipe: CraftRecipe, val remaining: Int)

/** Cache enum 1809 verbs for the make-X chatbox. */
internal object Verb {
    const val MAKE = 0
    const val FIRE = 9
    const val STRING = 10
    const val CUT = 12
}

internal const val MAX_AMOUNT = 28
internal const val NOTHING_HAPPENS = "Nothing interesting happens."
private const val THREAD_USES = 5

private var Player.threadUses: Int by intVarp(CraftingVarps.thread_uses)

/** A weak queue added while handling input fires one tick early, so ask for one more. */
internal fun queueDelay(ticks: Int): Int = ticks + 1

class Crafting : PluginScript() {
    override fun ScriptContext.startup() {
        onPlayerQueueWithArgs<CraftTask>(CraftingQueues.make) { craftStep(it.args) }
        for (combo in NeedleRecipes.combos + GlassRecipes.combos) {
            onOpHeldU(combo.a, combo.b) { startCrafting(combo.recipe, 1) }
        }
    }
}

/** Offers [recipes] in the make-X chatbox and starts the one the player picks. */
internal suspend fun ProtectedAccess.offer(verb: Int, what: String, recipes: List<CraftRecipe>) {
    val menu = recipes.take(SKILLMULTI_OPTIONS)
    val products = menu.map { it.product }
    val choice =
        skillMulti(verb, "What would you like to $what?", products, products.map { ocName(it) })
            ?: return
    startCrafting(menu[choice.index], choice.amount)
}

/** Starts up to [amount] actions of [recipe]; the first lands after the recipe's ticks. */
internal fun ProtectedAccess.startCrafting(recipe: CraftRecipe, amount: Int) {
    ifClose() // Clears weak queues: must come before the queue below.
    if (!canCraft(recipe)) {
        return
    }
    recipe.anim?.let { anim(it) }
    weakQueue(CraftingQueues.make, queueDelay(recipe.ticks), CraftTask(recipe, amount))
}

private fun ProtectedAccess.craftStep(task: CraftTask) {
    val recipe = task.recipe
    if (!canCraft(recipe)) {
        resetAnim()
        return
    }
    val spoil = recipe.spoil
    val spoiled = spoil != null && !statRandom(stats.crafting, spoil.low, spoil.high, 0)
    if (!use(recipe, spoiled)) {
        return
    }
    if (spoiled) {
        mes(spoil!!.message)
    } else {
        recipe.message?.let { mes(it) }
            ?: mes("You make ${article(ocName(recipe.product).removeSuffix(" (u)"))}.")
    }
    if (task.remaining > 1 && hasMaterials(recipe)) {
        recipe.anim?.let { anim(it) }
        weakQueue(
            CraftingQueues.make,
            queueDelay(recipe.ticks),
            task.copy(remaining = task.remaining - 1),
        )
    } else {
        resetAnim()
    }
}

/** Takes the inputs, gives the product (or what is left of a ruined one) and the xp. */
private fun ProtectedAccess.use(recipe: CraftRecipe, spoiled: Boolean): Boolean {
    for (input in recipe.inputs) {
        invDel(inv, input.obj, input.count)
    }
    val spoil = recipe.spoil
    val product = if (spoiled) spoil?.product else recipe.product
    val made = if (product == null) 0 else recipe.makes
    if (product != null && !invAdd(inv, product, made).success) {
        // No room: put the inputs back rather than lose them.
        for (input in recipe.inputs) {
            invAdd(inv, input.obj, input.count)
        }
        mes("You don't have enough inventory space.")
        resetAnim()
        return false
    }
    for (back in recipe.returns) {
        invAdd(inv, back.obj, back.count)
    }
    val xp = if (spoiled) spoil?.xp ?: 0.0 else recipe.xp
    if (xp > 0.0) {
        statAdvance(stats.crafting, xp)
    }
    if (recipe.thread && CraftingObjs.costume_needle !in inv) {
        useThread()
    }
    return true
}

/** Each thread lasts five actions, however many pieces of leather they cost. */
private fun ProtectedAccess.useThread() {
    player.threadUses++
    if (player.threadUses >= THREAD_USES) {
        player.threadUses = 0
        invDel(inv, CraftingObjs.thread)
        mes("You use up a reel of thread.")
    }
}

private fun ProtectedAccess.canCraft(recipe: CraftRecipe): Boolean {
    val name = article(ocName(recipe.product))
    if (player.craftingLvl < recipe.level) {
        mes(recipe.levelMessage ?: "You need a Crafting level of ${recipe.level} to make $name.")
        return false
    }
    if (recipe.thread && !hasNeedle()) {
        mes("You need a needle and some thread to make that.")
        return false
    }
    val tool = recipe.tools.firstOrNull { it !in inv }
    if (tool != null) {
        mes("You need ${article(ocName(tool).lowercase())} to make that.")
        return false
    }
    val missing = recipe.inputs.firstOrNull { invTotal(inv, it.obj) < it.count }
    if (missing != null) {
        mes("You don't have enough ${ocName(missing.obj).lowercase()} to make $name.")
        return false
    }
    return true
}

private fun ProtectedAccess.hasMaterials(recipe: CraftRecipe): Boolean =
    (!recipe.thread || hasNeedle()) &&
        recipe.tools.all { it in inv } &&
        recipe.inputs.all { invTotal(inv, it.obj) >= it.count }

private fun ProtectedAccess.hasNeedle(): Boolean =
    CraftingObjs.costume_needle in inv || CraftingObjs.needle in inv && CraftingObjs.thread in inv

private val PAIRS = listOf("gloves", "boots", "vambraces", "chaps", "greaves")
private val PLURALS = listOf("tips", "heads", "bolts", "arrowheads")

/** "a leather body", "a pair of leather gloves", "some bolts". [name] may be capitalised. */
internal fun article(name: String): String {
    val lower = name.lowercase()
    return when {
        PAIRS.any { lower.endsWith(it) } -> "a pair of $lower"
        PLURALS.any { lower.endsWith(it) } -> "some $lower"
        lower.first() in "aeiou" -> "an $lower"
        else -> "a $lower"
    }
}
