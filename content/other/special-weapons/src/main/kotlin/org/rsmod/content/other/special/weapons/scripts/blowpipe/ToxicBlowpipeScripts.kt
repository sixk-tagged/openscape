package org.rsmod.content.other.special.weapons.scripts.blowpipe

import jakarta.inject.Inject
import org.rsmod.api.player.bonus.BlowpipeDarts
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.righthand
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.script.onOpHeld3
import org.rsmod.api.script.onOpHeld4
import org.rsmod.api.script.onOpHeld5
import org.rsmod.api.script.onOpHeldU
import org.rsmod.api.script.onOpWorn2
import org.rsmod.game.inv.InvObj
import org.rsmod.game.inv.Inventory
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Loading, checking and unloading the toxic blowpipe. Deviation from OSRS: only darts are needed
 * (no Zulrah's scales).
 */
class ToxicBlowpipeScripts
@Inject
constructor(private val objTypes: ObjTypeList, private val objRepo: ObjRepository) :
    PluginScript() {
    override fun ScriptContext.startup() {
        // The "(empty)" cache obj only has a Dismantle op: it can be loaded but never wielded.
        val loaded = BlowpipeDarts.loaded
        onOpHeld3(loaded) { checkDarts(it.obj) }
        onOpHeld4(loaded) { unload(it.inventory, it.slot) }
        onOpHeld5(loaded) { unload(it.inventory, it.slot) }
        for (blowpipe in listOf(BlowpipeDarts.empty, loaded)) {
            for (dart in BlowpipeDarts.darts) {
                onOpHeldU(blowpipe, dart) { load(it.firstSlot, it.second) }
            }
        }
        onOpWorn2(BlowpipeDarts.loaded) { checkDarts(player.righthand) }
    }

    private fun ProtectedAccess.checkDarts(obj: InvObj?) {
        val dart = BlowpipeDarts.dartType(obj)
        val count = BlowpipeDarts.dartCount(obj)
        if (dart == null || count == 0) {
            mes("The blowpipe is empty.")
            return
        }
        mes("Darts: ${objTypes[dart].name} x $count.")
    }

    private fun ProtectedAccess.load(blowpipeSlot: Int, dart: ObjType) {
        val blowpipe = inv[blowpipeSlot] ?: return
        val loadedDart = BlowpipeDarts.dartType(blowpipe)
        val loadedCount = BlowpipeDarts.dartCount(blowpipe)
        if (loadedDart != null && loadedCount > 0 && loadedDart.id != dart.id) {
            mes("You can't mix different types of darts in the blowpipe.")
            return
        }

        val room = MAX_DARTS - loadedCount
        val add = minOf(room, invTotal(inv, dart))
        if (add <= 0) {
            mes(if (room <= 0) "The blowpipe is full of darts." else "You have no darts to load.")
            return
        }
        if (invDel(inv, dart, add).failure) {
            return
        }

        inv[blowpipeSlot] = BlowpipeDarts.withDarts(blowpipe, dart, loadedCount + add)
        mes("You load the blowpipe with $add darts.")
    }

    private fun ProtectedAccess.unload(inventory: Inventory, slot: Int) {
        val blowpipe = inventory[slot] ?: return
        val dart = BlowpipeDarts.dartType(blowpipe)
        val count = BlowpipeDarts.dartCount(blowpipe)
        if (dart == null || count == 0) {
            mes("The blowpipe is empty.")
            return
        }
        inventory[slot] = InvObj(BlowpipeDarts.loaded)
        invAddOrDrop(objRepo, dart, count)
        mes("You unload the blowpipe.")
    }

    private companion object {
        const val MAX_DARTS = 16_383
    }
}
