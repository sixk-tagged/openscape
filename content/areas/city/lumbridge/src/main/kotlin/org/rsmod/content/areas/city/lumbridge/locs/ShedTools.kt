package org.rsmod.content.areas.city.lumbridge.locs

import org.rsmod.api.config.refs.objs
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.type.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The tools in the Lumbridge Swamp shed: Take gives a rake or a spade, whichever the backpack
 * lacks, and a choice when it holds neither.
 */
class ShedTools : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1(ShedToolsLocs.tools) { takeTool() }
    }

    private suspend fun ProtectedAccess.takeTool() {
        val hasRake = ShedToolsObjs.rake in inv
        val hasSpade = objs.spade in inv
        val tool: ObjType =
            when {
                hasRake && hasSpade -> {
                    // Not on the wiki: OSRS's wording here is unconfirmed.
                    mes("You already have a rake and a spade.")
                    return
                }
                hasRake -> objs.spade
                hasSpade -> ShedToolsObjs.rake
                else -> choice2("Rake", ShedToolsObjs.rake, "Spade", objs.spade)
            }
        if (invAdd(inv, tool).failure) {
            mes("You don't have enough inventory space.")
        }
    }
}

object ShedToolsLocs : LocReferences() {
    val tools = find("farming_shed_tools")
}

object ShedToolsObjs : ObjReferences() {
    val rake = find("rake")
}
