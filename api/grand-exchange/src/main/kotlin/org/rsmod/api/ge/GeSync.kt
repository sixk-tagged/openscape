package org.rsmod.api.ge

import org.rsmod.api.player.output.VarpSync
import org.rsmod.api.player.vars.VarPlayerIntMapSetter
import org.rsmod.game.entity.Player
import org.rsmod.game.type.varbit.VarBitType
import org.rsmod.game.type.varp.VarpType

/** Writes varps to the player and the client whatever their transmit level or previous value. */
public object GeSync {
    public fun setVarp(player: Player, varp: VarpType, value: Int) {
        player.vars.backing[varp.id] = value
        VarpSync.writeVarp(player, varp, value)
    }

    public fun setVarBit(player: Player, varbit: VarBitType, value: Int) {
        VarPlayerIntMapSetter.set(player, varbit, value)
        VarpSync.writeVarp(player, varbit.baseVar, player.vars[varbit.baseVar])
    }
}
