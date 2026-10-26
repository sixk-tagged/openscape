package org.rsmod.content.other.special.weapons.barrows

import org.rsmod.api.player.hat
import org.rsmod.api.player.legs
import org.rsmod.api.player.righthand
import org.rsmod.api.player.torso
import org.rsmod.game.entity.Player
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.obj.ObjTypeList

/**
 * True if [player] wears a full [brother] Barrows set (helm, body, legs and weapon, any degradation
 * level but not broken). [brother] is the lowercase name used in the obj names, e.g. `verac`.
 */
fun ObjTypeList.wearsFullSet(player: Player, brother: String): Boolean =
    isPiece(player.hat, "barrows_${brother}_head_") &&
        isPiece(player.torso, "barrows_${brother}_body_") &&
        isPiece(player.legs, "barrows_${brother}_legs_") &&
        isPiece(player.righthand, "barrows_${brother}_weapon_")

private fun ObjTypeList.isPiece(obj: InvObj?, prefix: String): Boolean {
    val name = getOrNull(obj)?.internalName ?: return false
    return (name == prefix.dropLast(1) || name.startsWith(prefix)) && !name.endsWith("broken")
}
