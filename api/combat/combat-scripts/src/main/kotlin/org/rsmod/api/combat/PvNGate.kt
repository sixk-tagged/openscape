package org.rsmod.api.combat

import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.varps
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.game.entity.Npc

/**
 * Whether the player may attack [npc] in the Stronghold Slayer Cave: only the monster of their
 * current Slayer task, and only while the task has kills left. Anywhere else this allows it. Sends
 * the refusal itself.
 *
 * The cave is picked out by its map squares (not by npc type: the Slayer Tower spawns some of the
 * same types).
 */
public fun ProtectedAccess.canAttackInSlayerCave(npc: Npc): Boolean {
    val at = npc.coords
    if (at.level != 0 || at.x !in CAVE_X || at.z !in CAVE_Z) {
        return true
    }
    val task = npc.visType.param(params.slayer_category)
    if (
        task > 0 && player.vars[varps.slayer_count] > 0 && player.vars[varps.slayer_target] == task
    ) {
        return true
    }
    // UNVERIFIED: the wiki prints no refusal text for the cave.
    mes("You can only attack your assigned Slayer monster here.")
    return false
}

private val CAVE_X = 2368..2495
private val CAVE_Z = 9728..9855
