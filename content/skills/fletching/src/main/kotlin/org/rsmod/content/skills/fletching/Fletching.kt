package org.rsmod.content.skills.fletching

import org.rsmod.api.script.onOpHeldU
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.content.skills.production.Recipe
import org.rsmod.content.skills.production.Verb
import org.rsmod.content.skills.production.offer
import org.rsmod.content.skills.production.register
import org.rsmod.game.type.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Knife on logs, then every item-on-item recipe: stringing, assembly, tipping and feathering.
 * Chisel on gems works through the same pairs. Stat levels, messages and the repeat loop live in
 * the shared production script.
 */
class Fletching : PluginScript() {
    override fun ScriptContext.startup() {
        for (logs in FletchingRecipes.logs) {
            onOpHeldU(FletchingObjs.knife, logs.log) {
                offer(Verb.MAKE, "What would you like to make?", logs.recipes)
            }
        }
        register(FletchingRecipes.combos)
    }
}

/** What the knife makes from [log]. */
class LogRecipes(val log: ObjType, val recipes: List<Recipe>)

object FletchingObjs : ObjReferences() {
    val knife = find("knife")
    val chisel = find("chisel")
    val hammer = find("hammer")
    val feather = find("feather")
    val bow_string = find("bow_string")
    val crossbow_string = find("xbows_crossbow_string")
    val arrow_shaft = find("arrow_shaft")
    val headless_arrow = find("headless_arrow")
    val javelin_shaft = find("javelin_shaft")
    val dragon_bolts = find("dragon_bolts")
}

object FletchingSeqs : SeqReferences() {
    val whittle = find("human_fletching")
}
