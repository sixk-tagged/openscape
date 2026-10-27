package org.rsmod.content.areas.city.varrock.locs

import jakarta.inject.Inject
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.seqs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.righthand
import org.rsmod.api.player.stat.attackLvl
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.stats.xpmod.XpModifiers
import org.rsmod.content.areas.city.varrock.configs.varrock_locs
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The training dummies: 5 Attack experience a hit, one hit a click, up to and including level 7.
 */
class Dummies
@Inject
constructor(private val objTypes: ObjTypeList, private val xpMods: XpModifiers) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1(varrock_locs.dummy) { train(it.loc.coords) }
    }

    // Note: the hit uses the weapon's first stance animation and attack rate, whatever the
    // combat style is set to.
    private suspend fun ProtectedAccess.train(dummy: CoordGrid) {
        arriveDelay()
        faceSquare(dummy)
        if (player.attackLvl > MAX_LEVEL) {
            mes("There is nothing more you can learn from hitting a dummy.")
            return
        }
        val weapon = player.righthand?.let { objTypes[it] }
        anim(weapon?.paramOrNull(params.attack_anim_stance1) ?: seqs.human_unarmedpunch)
        statAdvance(stats.attack, XP_PER_HIT * xpMods.get(player, stats.attack))
        delay(weapon?.paramOrNull(params.attackrate) ?: DEFAULT_SPEED)
    }

    private companion object {
        const val MAX_LEVEL = 7
        const val XP_PER_HIT = 5.0
        const val DEFAULT_SPEED = 4
    }
}
