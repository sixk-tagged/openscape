package org.rsmod.content.other.npcattacks

import jakarta.inject.Inject
import org.rsmod.api.combat.commons.npc.NpcAttackTable
import org.rsmod.api.type.editors.npc.NpcEditor
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** Registers [NpcAttackProfiles] so `NvPCombat` attacks with them. */
class NpcAttackProfileScript @Inject constructor(private val table: NpcAttackTable) :
    PluginScript() {
    override fun ScriptContext.startup() {
        for ((npcs, profile) in NpcAttackProfiles.PROFILES) {
            for (npc in npcs) {
                table[npc] = profile
            }
        }
    }
}

/** The distance a profile's npcs stand off from (`ap` range); the cache has none for them. */
internal object NpcAttackRangeEditor : NpcEditor() {
    init {
        for ((npcs, profile) in NpcAttackProfiles.PROFILES) {
            for (npc in npcs) {
                edit(npc) { attackRange = profile.range }
            }
        }
    }
}
