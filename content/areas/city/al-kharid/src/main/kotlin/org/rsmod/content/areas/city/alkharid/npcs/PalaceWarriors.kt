package org.rsmod.content.areas.city.alkharid.npcs

import jakarta.inject.Inject
import org.rsmod.api.combat.commons.npc.queueCombatRetaliate
import org.rsmod.api.hunt.Hunt
import org.rsmod.api.npc.isInCombat
import org.rsmod.api.script.onNpcHit
import org.rsmod.content.areas.city.alkharid.configs.alkharid_npcs
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.PlayerList
import org.rsmod.game.hit.Hit
import org.rsmod.game.type.hunt.HuntVis
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The palace's Al Kharid warriors stand together (wiki): when a player hits one, the others in
 * sight shout and join the fight.
 */
class PalaceWarriors @Inject constructor(private val hunt: Hunt, private val players: PlayerList) :
    PluginScript() {
    override fun ScriptContext.startup() {
        onNpcHit(alkharid_npcs.al_kharid_warrior) { callBrothers(npc, hit) }
    }

    private fun callBrothers(warrior: Npc, hit: Hit) {
        if (!hit.isFromPlayer) {
            return
        }
        val attacker = hit.resolvePlayerSource(players) ?: return
        for (brother in hunt.findNpcs(warrior.coords, HELP_RANGE, HuntVis.LineOfSight)) {
            if (brother === warrior || brother.type.id != warrior.type.id || brother.isInCombat()) {
                continue
            }
            brother.say("Brother, I will help thee with this infidel!")
            brother.facePlayer(attacker)
            brother.queueCombatRetaliate(attacker)
        }
    }

    private companion object {
        // UNVERIFIED: the wiki only says "nearby"; 10 covers most of the palace hall.
        const val HELP_RANGE = 10
    }
}
