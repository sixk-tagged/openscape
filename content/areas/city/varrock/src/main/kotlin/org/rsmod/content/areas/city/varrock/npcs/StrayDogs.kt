package org.rsmod.content.areas.city.varrock.npcs

import org.rsmod.api.script.onOpNpc4
import org.rsmod.api.script.onOpNpc5
import org.rsmod.content.areas.city.varrock.configs.varrock_npcs
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Shoo-away and Pet on the stray dogs. Note: the dog does not back away or follow, and feeding it
 * is skipped.
 */
class StrayDogs : PluginScript() {
    override fun ScriptContext.startup() {
        for (dog in listOf(varrock_npcs.stray_dog, varrock_npcs.stray_dog2)) {
            onOpNpc4(dog) {
                player.say("Thbbbbt!")
                it.npc.say("Whine!")
            }
            onOpNpc5(dog) {
                player.say("Who's a good doggy!")
                it.npc.say("Woof!")
            }
        }
    }
}
