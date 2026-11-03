package org.rsmod.api.combat

import kotlin.math.abs
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.wildernessLevel
import org.rsmod.game.entity.Player

/**
 * Whether the player may attack [target] right now: both must stand in the Wilderness' PvP area
 * (the two safe steps past the ditch and the Ferox Enclave are not part of it), and their combat
 * levels may be no further apart than the Wilderness level the attacker stands in. Sends the
 * refusal itself.
 */
public fun ProtectedAccess.canAttackPlayer(target: Player): Boolean {
    // UNVERIFIED: the wiki prints none of these refusals; all three are recalled from the live
    // client (the first two are a guess at its wording).
    if (!mapWilderness()) {
        mes("You can't attack other players here.")
        return false
    }
    if (!mapWilderness(target)) {
        mes("You can't attack that player here.")
        return false
    }
    // UNVERIFIED: the wiki does not say whose Wilderness level counts; the attacker's is used.
    val levelRange = wildernessLevel(player.coords)
    if (abs(player.combatLevel - target.combatLevel) > levelRange) {
        mes("Your level difference is too great!")
        mes("You need to move deeper into the Wilderness to attack this player.")
        return false
    }
    return true
}
