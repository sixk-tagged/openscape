package org.rsmod.api.combat.scripts

import jakarta.inject.Inject
import org.rsmod.api.combat.commons.npc.NpcAttackTable
import org.rsmod.api.combat.commons.npc.combatDefaultRetaliateAp
import org.rsmod.api.combat.commons.npc.combatDefaultRetaliateOp
import org.rsmod.api.config.refs.queues
import org.rsmod.api.npc.access.StandardNpcAccess
import org.rsmod.api.npc.interact.AiPlayerInteractions
import org.rsmod.api.script.onNpcQueue
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

public class NpcRetaliateScript
@Inject
constructor(private val interactions: AiPlayerInteractions, private val attacks: NpcAttackTable) :
    PluginScript() {
    override fun ScriptContext.startup() {
        onNpcQueue(queues.com_retaliate_player) { autoRetaliatePlayer() }
    }

    private fun StandardNpcAccess.autoRetaliatePlayer() {
        // Ranged and magic npcs fight back from where they stand instead of walking up to melee.
        if (attacks[npc.visType] != null) {
            npc.combatDefaultRetaliateAp(interactions)
        } else {
            npc.combatDefaultRetaliateOp(interactions)
        }
    }
}
