package org.rsmod.content.areas.misc.wilderness.rules

import org.rsmod.api.config.refs.areas
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.config.refs.varps
import org.rsmod.api.player.output.MiscOutput
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.boolVarBit
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.script.onArea
import org.rsmod.api.script.onAreaExit
import org.rsmod.game.entity.Player
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Entering the Wilderness' PvP area turns on the client's Wilderness overlay and the player
 * "Attack" option (which is hidden everywhere else). Leaving it, by walking, teleporting, dying or
 * logging out, turns them off again and ends any Tele Block.
 */
class WildernessAreaScript : PluginScript() {
    private var ProtectedAccess.insideWilderness by boolVarBit(varbits.inside_wilderness)
    private var ProtectedAccess.pvpArea by boolVarBit(varbits.pvp_area_client)
    private var Player.teleblockTicks by intVarp(varps.teleblock_ticks)

    override fun ScriptContext.startup() {
        onArea(areas.wilderness) { enter() }
        onAreaExit(areas.wilderness) { exit() }
    }

    private fun ProtectedAccess.enter() {
        insideWilderness = true
        pvpArea = true
        MiscOutput.setPlayerOp(player, slot = ATTACK_SLOT, op = "Attack", priority = true)
    }

    private fun ProtectedAccess.exit() {
        insideWilderness = false
        pvpArea = false
        MiscOutput.setPlayerOp(player, slot = ATTACK_SLOT, op = null)
        player.teleblockTicks = 0
    }

    private companion object {
        const val ATTACK_SLOT = 2
    }
}
