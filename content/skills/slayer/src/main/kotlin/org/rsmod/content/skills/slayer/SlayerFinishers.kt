package org.rsmod.content.skills.slayer

import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.queues
import org.rsmod.api.npc.queueDeath
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpNpcU
import org.rsmod.api.type.editors.npc.NpcEditor
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.entity.Npc
import org.rsmod.game.type.npc.NpcType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Monsters only an item can finish (wiki "Gargoyle", "Rockslug"): hits stop at 1 hitpoint
 * (`hitpoints_floor`), and using the item at [Finisher.threshold] hitpoints or fewer kills them. A
 * bag of salt is used up either way.
 */
class SlayerFinishers : PluginScript() {
    override fun ScriptContext.startup() {
        for (finisher in Finisher.entries) {
            for (npc in finisher.npcs) {
                for (item in finisher.items) {
                    onOpNpcU(npc, item) { finish(it.npc, finisher, item) }
                }
            }
        }
    }

    private fun ProtectedAccess.finish(npc: Npc, finisher: Finisher, item: ObjType) {
        // Already dead or dying: nothing to finish, and nothing used up.
        if (npc.hitpoints <= 0 || queues.death in npc.queueList) {
            return
        }
        if (finisher.consumed) {
            invDel(inv, item)
        }
        if (npc.hitpoints > finisher.threshold) {
            // UNVERIFIED: both messages.
            mes(finisher.tooStrong)
            return
        }
        mes(finisher.kill)
        npc.hitpoints = 0
        npc.queueDeath()
    }
}

internal enum class Finisher(
    val npcs: List<NpcType>,
    val items: List<ObjType>,
    val threshold: Int,
    val consumed: Boolean,
    val kill: String,
    val tooStrong: String,
) {
    Gargoyle(
        FinisherNpcs.gargoyles,
        listOf(FinisherObjs.rock_hammer, FinisherObjs.granite_hammer),
        threshold = 8,
        consumed = false,
        kill = "You smash the gargoyle with the hammer.",
        tooStrong = "The gargoyle isn't weak enough to be harmed by the hammer.",
    ),
    Rockslug(
        FinisherNpcs.rockslugs,
        listOf(FinisherObjs.bag_of_salt),
        threshold = 4,
        consumed = true,
        kill = "The rockslug shrivels and dies.",
        tooStrong = "Your bag of salt is wasted on the rockslug.",
    ),
}

internal object FinisherNpcs : NpcReferences() {
    val gargoyles =
        listOf(find("slayer_gargoyle_1"), find("slayer_cave_gargoyle"), find("superior_gargoyle"))
    val rockslugs =
        listOf(find("slayer_rockslug"), find("slayer_rockslug_baby"), find("superior_rockslug"))
}

object FinisherObjs : ObjReferences() {
    val rock_hammer = find("slayer_rock_hammer")
    val granite_hammer = find("granite_hammer")
    val bag_of_salt = find("slayer_bag_of_salt")
}

internal object FinisherNpcEditor : NpcEditor() {
    init {
        for (npc in Finisher.entries.flatMap { it.npcs }) {
            edit(npc) { param[params.hitpoints_floor] = 1 }
        }
    }
}
