package org.rsmod.content.other.deathsoffice

import org.rsmod.api.config.refs.objs
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.intVarp
import org.rsmod.content.other.deathsoffice.configs.DeathVarps
import org.rsmod.game.entity.Player

private var Player.deathCoffer by intVarp(DeathVarps.death_coffer)

/**
 * Pays one of Death's fees from Death's Coffer first, then backpack coins, then bank coins. Takes
 * nothing unless the three together cover [amount].
 */
internal fun ProtectedAccess.payDeathFee(amount: Long): Boolean {
    val carried = invTotal(inv, objs.coins).toLong()
    val banked = invTotal(bank, objs.coins).toLong()
    if (player.deathCoffer + carried + banked < amount) {
        return false
    }
    var left = amount
    val fromCoffer = minOf(left, player.deathCoffer.toLong())
    player.deathCoffer -= fromCoffer.toInt()
    left -= fromCoffer
    val fromInv = minOf(left, carried)
    if (fromInv > 0) {
        invDel(inv, objs.coins, fromInv.toInt())
    }
    left -= fromInv
    if (left > 0) {
        invDel(bank, objs.coins, left.toInt())
    }
    return true
}
