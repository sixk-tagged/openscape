package org.rsmod.api.combat.formulas

import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.config.refs.varps
import org.rsmod.game.entity.Player
import org.rsmod.game.type.npc.UnpackedNpcType

/**
 * Hit chance formulas internally use decimals (e.g., `1%` = `0.01`, `100%` = `1.0`). To maintain
 * consistency with other combat formulas that use whole integers, we scale them using this
 * constant.
 */
internal const val HIT_CHANCE_SCALE: Int = 10_000

internal fun scale(base: Int, multiplier: Int, divisor: Int): Int = (base * multiplier) / divisor

/**
 * openscape: the npc's `slayer_category` (cache param 50) is the player's current task, with kills
 * left. Krystilia's tasks (master 7) only count inside the Wilderness.
 */
internal fun UnpackedNpcType.isSlayerTask(player: Player): Boolean {
    val task = param(params.slayer_category)
    if (task <= 0 || player.vars[varps.slayer_count] <= 0) {
        return false
    }
    if (player.vars[varps.slayer_target] != task) {
        return false
    }
    return player.vars[varbits.slayer_master] != KRYSTILIA ||
        player.vars[varbits.inside_wilderness] == 1
}

private const val KRYSTILIA = 7
