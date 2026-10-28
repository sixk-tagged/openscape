package org.rsmod.content.skills.magic.spell.utility

import org.rsmod.api.combat.commons.magic.MagicSpell
import org.rsmod.api.config.refs.components
import org.rsmod.api.player.output.UpdateInventory.resendSlot
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.protect.ProtectedAccessLauncher
import org.rsmod.api.player.protect.clearPendingAction
import org.rsmod.api.script.onIfOverlayButtonT
import org.rsmod.game.type.obj.UnpackedObjType
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Registers [action] for [spell] cast on an inventory item. The slot and the item are the ones the
 * client sent; the action must check the slot still holds that item.
 */
internal fun ScriptContext.onSpellOnItem(
    spell: MagicSpell,
    launcher: ProtectedAccessLauncher,
    action: suspend ProtectedAccess.(slot: Int, obj: UnpackedObjType) -> Unit,
) {
    onIfOverlayButtonT(spell.component, components.inv_items) {
        val obj = targetObj ?: return@onIfOverlayButtonT resendSlot(player.inv, 0)
        player.clearPendingAction(eventBus)
        player.resetFaceEntity()
        if (player.isAccessProtected) {
            return@onIfOverlayButtonT resendSlot(player.inv, 0)
        }
        launcher.launch(player) { action(targetSlot, obj) }
    }
}
