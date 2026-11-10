package org.rsmod.content.areas.city.lumbridge.npcs

import org.rsmod.api.config.refs.objs
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.script.onOpNpcU
import org.rsmod.content.areas.city.lumbridge.configs.lumbridge_npcs
import org.rsmod.game.entity.Npc
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** The "sheep" in Fred's field is two penguin spies in disguise: it won't be sheared or talk. */
class PenguinSheep : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(lumbridge_npcs.penguin_sheep) { getsAway() }
        onOpNpcU(lumbridge_npcs.penguin_sheep, objs.shears) { getsAway() }
        onOpNpc3(lumbridge_npcs.penguin_sheep) { talk(it.npc) }
    }

    private fun ProtectedAccess.getsAway() {
        mes("The... whatever it is... manages to get away from you!")
    }

    private suspend fun ProtectedAccess.talk(npc: Npc) {
        startDialogue(npc) {
            chatPlayer(confused, "That's a sheep...I think. I can't talk to sheep.")
        }
    }
}
