package org.rsmod.content.skills.mining

import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.feet
import org.rsmod.api.player.hat
import org.rsmod.api.player.legs
import org.rsmod.api.player.torso
import org.rsmod.api.stats.xpmod.XpMod
import org.rsmod.content.skills.mining.configs.MiningObjs
import org.rsmod.game.entity.Player
import org.rsmod.game.inv.isType
import org.rsmod.game.type.stat.StatType

/**
 * Prospector outfit: +0.4/0.8/0.6/0.2% per piece, +2.5% total for the full set. A custom mod
 * because `xpmod_percent` only takes whole percents.
 */
class ProspectorXpMod : XpMod {
    override fun Player.modifier(stat: StatType): Double {
        if (stat != stats.mining) {
            return 0.0
        }
        val worn =
            listOf(
                hat.isType(MiningObjs.prospector_helmet),
                torso.isType(MiningObjs.prospector_jacket),
                legs.isType(MiningObjs.prospector_legs),
                feet.isType(MiningObjs.prospector_boots),
            )
        val bonus = PIECE_BONUS.zip(worn).sumOf { (piece, isWorn) -> if (isWorn) piece else 0.0 }
        return if (worn.all { it }) bonus + SET_BONUS else bonus
    }

    private companion object {
        val PIECE_BONUS = listOf(0.004, 0.008, 0.006, 0.002)
        const val SET_BONUS = 0.005
    }
}
