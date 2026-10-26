package org.rsmod.content.other.deathsoffice

import jakarta.inject.Inject
import org.rsmod.api.death.DeathKeep
import org.rsmod.api.market.MarketPrices
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stopInvTransmit
import org.rsmod.api.player.vars.intVarBit
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.script.onIfClose
import org.rsmod.api.script.onIfModalButton
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.content.other.deathsoffice.configs.DeathInvs
import org.rsmod.content.other.deathsoffice.configs.DeathNpcs
import org.rsmod.content.other.deathsoffice.configs.DeathVarBits
import org.rsmod.content.other.deathsoffice.configs.DeathVarps
import org.rsmod.content.other.deathsoffice.configs.GraveComponents
import org.rsmod.content.other.deathsoffice.configs.GraveInterfaces
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Player
import org.rsmod.game.inv.InvObj
import org.rsmod.game.inv.Inventory
import org.rsmod.game.type.interf.IfButtonOp
import org.rsmod.game.type.interf.IfEvent
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** The gravestone's Check and Loot ops and its `gravestone_generic` loot screen. */
class GraveLootScript
@Inject
constructor(
    private val graves: Gravestones,
    private val objTypes: ObjTypeList,
    private val marketPrices: MarketPrices,
) : PluginScript() {
    private var Player.graveCoord by intVarp(DeathVarps.gravestone_coord)
    private var Player.gravePaid by intVarp(DeathVarps.gravestone_paid)
    private var Player.deathCoffer by intVarp(DeathVarps.death_coffer)
    private var Player.thresholdFree by intVarBit(DeathVarBits.threshold_free)
    private var Player.threshold1k by intVarBit(DeathVarBits.threshold_1k)
    private var Player.threshold10k by intVarBit(DeathVarBits.threshold_10k)

    override fun ScriptContext.startup() {
        onOpNpc1(DeathNpcs.grave) { checkGrave(it.npc) }
        onOpNpc3(DeathNpcs.grave) { openLoot(it.npc) }
        onIfModalButton(GraveComponents.freeitems) { takeSlot(it.comsub, it.op) }
        onIfModalButton(GraveComponents.payitems) { takeSlot(it.comsub, it.op) }
        onIfModalButton(GraveComponents.freebutton) { takeAll() }
        onIfModalButton(GraveComponents.paybutton) { unlockOrTakeAll() }
        onIfClose(GraveInterfaces.loot) {
            player.invMap[DeathInvs.gravestone]?.let(player::stopInvTransmit)
        }
    }

    private fun ProtectedAccess.checkGrave(npc: Npc) {
        if (graves.npc(player) !== npc) {
            return
        }
        // Not on the wiki: what Check shows is undocumented.
        val minutes = (player.graveTicks + 99) / 100
        mes("Your gravestone will collapse in about $minutes minutes unless you loot it.")
    }

    private fun ProtectedAccess.openLoot(npc: Npc) {
        if (graves.npc(player) !== npc) {
            return
        }
        sortByFee()
        ifOpenMainModal(GraveInterfaces.loot)
        invTransmit(graveInv())
        ifSetEvents(GraveComponents.freeitems, 0..119, IfEvent.Op1, IfEvent.Op10)
        ifSetEvents(GraveComponents.payitems, 0..119, IfEvent.Op1, IfEvent.Op10)
        ifSetEvents(GraveComponents.freebutton, -1..-1, IfEvent.Op1)
        ifSetEvents(GraveComponents.paybutton, -1..-1, IfEvent.Op1)
        sendFee()
    }

    /**
     * Orders the grave dearest-fee first and tells the client where each fee band starts, which is
     * how `gravestone_generic` splits free items from those behind the fee.
     */
    private fun ProtectedAccess.sortByFee() {
        val grave = graveInv()
        val sorted = grave.filterNotNull().sortedByDescending(::unitFee)
        for (slot in grave.indices) {
            grave[slot] = sorted.getOrNull(slot)
        }
        if (fee() == 0L) {
            player.threshold10k = 1
            player.threshold1k = 1
            player.thresholdFree = 1
            return
        }
        player.threshold10k = sorted.count { unitFee(it) >= 100_000 } + 1
        player.threshold1k = sorted.count { unitFee(it) >= 10_000 } + 1
        player.thresholdFree = sorted.count { unitFee(it) > 0 } + 1
    }

    private fun ProtectedAccess.sendFee() {
        runClientScript(TRANSMIT_DATA, fee().toInt(), player.deathCoffer)
    }

    private fun ProtectedAccess.takeSlot(slot: Int, op: IfButtonOp) {
        val grave = graveInv()
        if (slot !in grave.indices) {
            return
        }
        val obj = grave[slot] ?: return
        if (op == IfButtonOp.Op10) {
            objExamine(grave, slot)
            return
        }
        if (!isFree(obj)) {
            mes("You need to unlock your gravestone before you can take that.")
            return
        }
        if (!invMoveFromSlot(grave, inv, slot, obj.count).success) {
            mes("You don't have enough inventory space.")
        }
        afterTake()
    }

    private fun ProtectedAccess.takeAll() {
        val grave = graveInv()
        var full = false
        for (slot in grave.indices) {
            val obj = grave[slot] ?: continue
            if (isFree(obj) && !invMoveFromSlot(grave, inv, slot, obj.count).success) {
                full = true
            }
        }
        if (full) {
            mes("You don't have enough inventory space.")
        }
        afterTake()
    }

    private fun ProtectedAccess.unlockOrTakeAll() {
        val fee = fee()
        if (fee == 0L) {
            takeAll()
            return
        }
        if (!payDeathFee(fee)) {
            mes("You don't have enough money to unlock your gravestone.")
            return
        }
        player.gravePaid += fee.toInt()
        sortByFee()
        sendFee()
    }

    private fun ProtectedAccess.afterTake() {
        // Credit for items taken out is used up, so items a later death adds still owe their fee.
        player.gravePaid = minOf(player.gravePaid.toLong(), owed()).toInt()
        if (graveInv().any { it != null }) {
            return
        }
        graves.despawn(player)
        player.graveTicks = 0
        player.graveCoord = 0
        player.gravePaid = 0
        ifClose()
    }

    private fun ProtectedAccess.owed(): Long = graveOwed(graveInv(), ::unitPrice)

    /** What is still owed: the fee less the credit already paid for what is in the grave. */
    // Note: after taking items out and dying again with more, the 500k cap counts the credit
    // left, not everything paid; track the total paid if that edge ever matters.
    private fun ProtectedAccess.fee(): Long = maxOf(0, owed() - player.gravePaid)

    private fun ProtectedAccess.isFree(obj: InvObj): Boolean = unitFee(obj) == 0L || fee() == 0L

    private fun unitFee(obj: InvObj): Long = DeathKeep.graveFee(unitPrice(obj))

    private fun unitPrice(obj: InvObj): Long {
        val type = objTypes[obj]
        return (marketPrices[type] ?: type.cost).toLong()
    }

    private fun ProtectedAccess.graveInv(): Inventory = inv(DeathInvs.gravestone)

    private companion object {
        /** `[clientscript,gravestone_transmit_data](int fee, int coffer)`. */
        const val TRANSMIT_DATA = 3478
    }
}
