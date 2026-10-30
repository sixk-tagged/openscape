package org.rsmod.content.interfaces.bank.scripts

import jakarta.inject.Inject
import kotlin.math.max
import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.params
import org.rsmod.api.invtx.invTransfer
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.intVarBit
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.player.worn.WornUnequipOp
import org.rsmod.api.script.onIfModalButton
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.type.refs.comp.ComponentReferences
import org.rsmod.api.type.refs.interf.InterfaceReferences
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.varbit.VarBitReferences
import org.rsmod.api.type.refs.varp.VarpReferences
import org.rsmod.events.EventBus
import org.rsmod.game.inv.Inventory
import org.rsmod.game.type.interf.IfButtonOp
import org.rsmod.game.type.interf.IfEvent
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.obj.Wearpos
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The bank deposit box (`bank_depositbox`): deposit one slot (1/5/10/X/All), the whole inventory or
 * everything worn. Objs land in the first free bank slot, or on their stack or placeholder; the
 * bank's own open script tabs new ones into the main tab.
 */
class DepositBoxScript
@Inject
constructor(private val objTypes: ObjTypeList, private val eventBus: EventBus) : PluginScript() {
    private var ProtectedAccess.mode by intVarBit(DepositBoxVarBits.mode)
    private var ProtectedAccess.requestedQty by intVarp(DepositBoxVarps.requested_quantity)

    override fun ScriptContext.startup() {
        onOpLoc1(DepositBoxLocs.deposit_box) { openDepositBox() }
        onOpLoc1(content.bank_deposit_box) { openDepositBox() }
        onIfModalButton(DepositBoxComponents.inventory) { invOp(it.comsub, it.op) }
        onIfModalButton(DepositBoxComponents.deposit_inv) { depositAll(inv) }
        onIfModalButton(DepositBoxComponents.deposit_worn) { depositWorn() }
        onIfModalButton(DepositBoxComponents.quantity_1) { mode = MODE_1 }
        onIfModalButton(DepositBoxComponents.quantity_5) { mode = MODE_5 }
        onIfModalButton(DepositBoxComponents.quantity_10) { mode = MODE_10 }
        onIfModalButton(DepositBoxComponents.quantity_all) { mode = MODE_ALL }
        onIfModalButton(DepositBoxComponents.quantity_x) {
            if (it.op == IfButtonOp.Op2 || requestedQty == 0) {
                requestedQty = countDialog()
            }
            mode = MODE_X
        }
    }

    private fun ProtectedAccess.openDepositBox() {
        ifOpenMainModal(DepositBoxInterfaces.deposit_box)
        ifSetEvents(
            DepositBoxComponents.inventory,
            inv.indices,
            IfEvent.Op1,
            IfEvent.Op2,
            IfEvent.Op3,
            IfEvent.Op4,
            IfEvent.Op5,
            IfEvent.Op6,
            IfEvent.Op10,
        )
    }

    /** The client's ops: op1 is the selected quantity (unset for "1"), then 1, 5, 10, X, All. */
    private suspend fun ProtectedAccess.invOp(slot: Int, op: IfButtonOp) {
        val count =
            when (op) {
                IfButtonOp.Op1 -> leftClickQty()
                IfButtonOp.Op2 -> 1
                IfButtonOp.Op3 -> 5
                IfButtonOp.Op4 -> 10
                IfButtonOp.Op5 -> countDialog().also { requestedQty = it }
                IfButtonOp.Op6 -> Int.MAX_VALUE
                IfButtonOp.Op10 -> return objExamine(inv, slot)
                else -> return
            }
        deposit(inv, slot, count)?.let { mes(it) }
    }

    private fun ProtectedAccess.leftClickQty(): Int =
        when (mode) {
            MODE_5 -> 5
            MODE_10 -> 10
            MODE_X -> max(1, requestedQty)
            MODE_ALL -> Int.MAX_VALUE
            else -> 1
        }

    private fun ProtectedAccess.depositAll(from: Inventory) {
        if (from.isEmpty()) {
            mes("You have nothing to deposit.")
            return
        }
        from.indices.mapNotNull { deposit(from, it, Int.MAX_VALUE) }.firstOrNull()?.let { mes(it) }
    }

    private fun ProtectedAccess.depositWorn() {
        val before = Wearpos.entries.associateWith { worn[it.slot] }
        depositAll(worn)
        for ((wearpos, old) in before) {
            if (old != null && old != worn[wearpos.slot]) {
                WornUnequipOp.notifyWornUnequip(player, wearpos, objTypes[old], eventBus)
            }
        }
    }

    /** Returns why [slot] could not be deposited, or null if it was (or was empty). */
    private fun ProtectedAccess.deposit(from: Inventory, slot: Int, count: Int): String? {
        val obj = from[slot] ?: return null
        val type = objTypes.uncert(objTypes[obj])
        if (type.param(params.no_bank) != 0) {
            return "A magical force prevents you from banking this item!"
        }
        val placeholder = objTypes.placeholder(type)
        val stackSlot = bank.indexOfFirst { it?.id == type.id || it?.id == placeholder.id }
        // Note: ignores the bank_capacity varbit; the bank inv size is the only limit here.
        val moved =
            player.invTransfer(
                from = from,
                fromSlot = slot,
                count = count,
                into = bank,
                intoSlot = stackSlot.takeIf { it != -1 },
                strict = false,
                uncert = true,
                untransform = true,
            )
        return if (moved.success) null else "Your bank cannot hold your items."
    }

    private companion object {
        // `depositbox_mode` values, read from the cache's quantity buttons and drawslot script.
        const val MODE_1 = 0
        const val MODE_5 = 1
        const val MODE_ALL = 2
        const val MODE_X = 3
        const val MODE_10 = 4
    }
}

object DepositBoxLocs : LocReferences() {
    val deposit_box = find("bank_deposit_box")
}

object DepositBoxInterfaces : InterfaceReferences() {
    val deposit_box = find("bank_depositbox")
}

object DepositBoxComponents : ComponentReferences() {
    val inventory = find("bank_depositbox:inventory")
    val deposit_inv = find("bank_depositbox:deposit_inv")
    val deposit_worn = find("bank_depositbox:deposit_worn")
    val quantity_1 = find("bank_depositbox:1")
    val quantity_5 = find("bank_depositbox:5")
    val quantity_10 = find("bank_depositbox:10")
    val quantity_x = find("bank_depositbox:x")
    val quantity_all = find("bank_depositbox:all")
}

object DepositBoxVarBits : VarBitReferences() {
    val mode = find("depositbox_mode")
}

object DepositBoxVarps : VarpReferences() {
    val requested_quantity = find("depositbox_requestedquantity")
}
