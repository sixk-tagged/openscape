package org.rsmod.content.other.deathsoffice

import jakarta.inject.Inject
import org.rsmod.api.death.DeathKeep
import org.rsmod.api.market.MarketPrices
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stopInvTransmit
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.script.onIfClose
import org.rsmod.api.script.onIfModalButton
import org.rsmod.api.script.onOpNpc3
import org.rsmod.content.other.deathsoffice.configs.DeathInvs
import org.rsmod.content.other.deathsoffice.configs.DeathNpcs
import org.rsmod.content.other.deathsoffice.configs.DeathOfficeComponents
import org.rsmod.content.other.deathsoffice.configs.DeathOfficeInterfaces
import org.rsmod.content.other.deathsoffice.configs.DeathOfficeVarps
import org.rsmod.content.other.deathsoffice.configs.DeathVarps
import org.rsmod.game.entity.Player
import org.rsmod.game.inv.InvObj
import org.rsmod.game.inv.Inventory
import org.rsmod.game.type.interf.IfButtonOp
import org.rsmod.game.type.interf.IfEvent
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

private var Player.deathCoffer by intVarp(DeathVarps.death_coffer)
private var Player.cofferShown by intVarp(DeathOfficeVarps.coffer_shown)
private var Player.selectedSlot by intVarp(DeathOfficeVarps.selected_slot)
private var Player.feeEach by intVarp(DeathOfficeVarps.fee_each)

/** Opens Death's `death_office` screen on the items he is holding for the player. */
internal fun ProtectedAccess.openDeathsOffice() {
    player.selectedSlot = -1
    player.feeEach = 0
    player.cofferShown = player.deathCoffer
    ifOpenMainModal(DeathOfficeInterfaces.retrieval)
    invTransmit(inv(DeathInvs.death_permanent))
    ifSetEvents(DeathOfficeComponents.items, 0..119, IfEvent.Op1, IfEvent.Op10)
    for (button in BUTTONS) {
        ifSetEvents(button, -1..-1, IfEvent.Op1)
    }
}

private val BUTTONS =
    listOf(
        DeathOfficeComponents.take1,
        DeathOfficeComponents.take5,
        DeathOfficeComponents.takeX,
        DeathOfficeComponents.takeAll,
        DeathOfficeComponents.takeEverything,
    )

/**
 * Death's Collect: pick a stored item, then take 1, 5, X or all of it for his fee (5% of its value
 * from 100k), or take everything. Fees come from Death's Coffer, then coins carried, then banked.
 */
class DeathRetrievalScript
@Inject
constructor(private val objTypes: ObjTypeList, private val marketPrices: MarketPrices) :
    PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc3(DeathNpcs.death) { openDeathsOffice() }
        onIfModalButton(DeathOfficeComponents.items) { select(it.comsub, it.op) }
        onIfModalButton(DeathOfficeComponents.take1) { takeSelected(1) }
        onIfModalButton(DeathOfficeComponents.take5) { takeSelected(5) }
        onIfModalButton(DeathOfficeComponents.takeX) { takeSelected(countDialog()) }
        onIfModalButton(DeathOfficeComponents.takeAll) { takeSelected(Int.MAX_VALUE) }
        onIfModalButton(DeathOfficeComponents.takeEverything) { takeEverything() }
        onIfClose(DeathOfficeInterfaces.retrieval) {
            player.invMap[DeathInvs.death_permanent]?.let(player::stopInvTransmit)
        }
    }

    private fun ProtectedAccess.select(slot: Int, op: IfButtonOp) {
        val office = office()
        val obj = office.getOrNull(slot) ?: return
        if (op == IfButtonOp.Op10) {
            objExamine(office, slot)
            return
        }
        player.selectedSlot = slot
        player.feeEach = minOf(unitFee(obj), Int.MAX_VALUE.toLong()).toInt()
    }

    private fun ProtectedAccess.takeSelected(count: Int) {
        val obj = office().getOrNull(player.selectedSlot) ?: return
        take(obj.id, count)
        afterTake()
    }

    private fun ProtectedAccess.takeEverything() {
        for (id in office().filterNotNull().map { it.id }.distinct()) {
            if (!take(id, Int.MAX_VALUE)) {
                break
            }
        }
        afterTake()
    }

    /**
     * Takes up to [count] stored units of obj [id], as many as the backpack holds, once their fee
     * is paid. False when space or money ran out first.
     */
    private fun ProtectedAccess.take(id: Int, count: Int): Boolean {
        val office = office()
        val stored = office.filterNotNull().filter { it.id == id }
        val wanted = minOf(count, stored.sumOf { it.count })
        val stackable = objTypes[stored.first()].isStackable
        val fits =
            when {
                !stackable -> minOf(wanted, inv.freeSpace())
                inv.freeSpace() > 0 || inv.any { it?.id == id } -> wanted
                else -> 0
            }
        if (fits == 0) {
            mes("You don't have enough inventory space.")
            return false
        }
        val unitFee = unitFee(stored.first())
        if (!canPayDeathFee(unitFee * fits)) {
            // Not on the wiki: OSRS's wording here is undocumented.
            mes("You don't have enough money to pay Death's fee.")
            return false
        }
        var left = fits
        for (slot in office.indices) {
            val obj = office[slot]?.takeIf { it.id == id } ?: continue
            val move = minOf(left, obj.count)
            if (invMoveFromSlot(office, inv, slot, move).success) {
                left -= move
            }
            if (left == 0) {
                break
            }
        }
        // Only what reached the backpack is paid for.
        val moved = fits - left
        if (unitFee * moved > 0) {
            payDeathFee(unitFee * moved)
        }
        return moved == wanted
    }

    private fun ProtectedAccess.afterTake() {
        if (office().getOrNull(player.selectedSlot) == null) {
            player.selectedSlot = -1
            player.feeEach = 0
        }
        player.cofferShown = player.deathCoffer
    }

    private fun ProtectedAccess.office(): Inventory = inv(DeathInvs.death_permanent)

    private fun Inventory.getOrNull(slot: Int): InvObj? = if (slot in indices) this[slot] else null

    // Note: shop value until a Grand Exchange exists; its prices plug into MarketPrices.
    private fun unitFee(obj: InvObj): Long {
        val type = objTypes[obj]
        return DeathKeep.officeFee((marketPrices[type] ?: type.cost).toLong())
    }
}
