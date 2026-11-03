package org.rsmod.api.config.editors

import org.rsmod.api.config.refs.npcs
import org.rsmod.api.type.editors.npc.NpcEditor
import org.rsmod.game.type.npc.NpcType

private val imps = setOf(npcs.imp)

internal object NpcEdits : NpcEditor() {
    init {
        imps.forEach(::imp)

        edit(npcs.farming_tools_leprechaun) {
            respawnDir = south
            wanderRange = 0
        }
    }
}

private fun NpcEditor.imp(type: NpcType) = edit(type) { giveChase = false }
