package org.rsmod.api.combat.scripts

import jakarta.inject.Inject
import org.rsmod.api.area.checker.AreaChecker
import org.rsmod.api.combat.NvPCombat
import org.rsmod.api.combat.player.aggressiveNpc
import org.rsmod.api.npc.access.StandardNpcAccess
import org.rsmod.api.player.isInPvnCombat
import org.rsmod.api.player.isInPvpCombat
import org.rsmod.api.script.advanced.onDefaultAiApPlayer2
import org.rsmod.api.script.advanced.onDefaultAiOpPlayer2
import org.rsmod.game.entity.Player
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

public class NvPCombatScript
@Inject
constructor(private val combat: NvPCombat, private val areaChecker: AreaChecker) : PluginScript() {
    override fun ScriptContext.startup() {
        onDefaultAiOpPlayer2 { attemptCombat(it.target) }
        // Npcs with a ranged or magic attack profile stand off and attack from `ap` range.
        onDefaultAiApPlayer2 { attemptCombat(it.target) }
    }

    private fun StandardNpcAccess.attemptCombat(target: Player) {
        if (!canAttack(target)) {
            resetMode()
            return
        }
        combat.attack(this, target)
    }

    private fun StandardNpcAccess.canAttack(target: Player): Boolean {
        val singleCombat = !mapMultiway(areaChecker)
        if (singleCombat) {
            if (target.isInPvpCombat()) {
                return false
            }

            if (target.isInPvnCombat()) {
                if (target.aggressiveNpc != null && target.aggressiveNpc != npc.uid) {
                    return false
                }
            }
        }
        return true
    }
}
