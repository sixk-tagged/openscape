package org.rsmod.content.other.deathsoffice

import jakarta.inject.Inject
import org.rsmod.api.config.refs.params
import org.rsmod.api.market.MarketPrices
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.script.onIfModalButton
import org.rsmod.api.script.onOpLoc1
import org.rsmod.content.other.deathsoffice.configs.CofferComponents
import org.rsmod.content.other.deathsoffice.configs.CofferInterfaces
import org.rsmod.content.other.deathsoffice.configs.CofferVarps
import org.rsmod.content.other.deathsoffice.configs.DeathLocs
import org.rsmod.content.other.deathsoffice.configs.DeathVarps
import org.rsmod.game.entity.Player
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.interf.IfButtonOp
import org.rsmod.game.type.interf.IfEvent
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Death's Coffer: backpack items that could go on the Grand Exchange and are worth 10,000 or more
 * are sacrificed for 105% of their value, which only pays Death's fees.
 */
class DeathCofferScript
@Inject
constructor(private val objTypes: ObjTypeList, private val marketPrices: MarketPrices) :
    PluginScript() {
    private var Player.deathCoffer by intVarp(DeathVarps.death_coffer)
    private var Player.cofferShown by intVarp(CofferVarps.coffer_shown)
    private var Player.selectedSlot by intVarp(CofferVarps.selected_slot)
    private var Player.quantity by intVarp(CofferVarps.quantity)
    private var Player.valueEach by intVarp(CofferVarps.value_each)

    override fun ScriptContext.startup() {
        onOpLoc1(DeathLocs.coffer) { openCoffer() }
        onIfModalButton(CofferComponents.side_items) { select(it.comsub, it.op) }
        onIfModalButton(CofferComponents.quantity1) { player.quantity = 1 }
        onIfModalButton(CofferComponents.quantity5) { player.quantity = 5 }
        onIfModalButton(CofferComponents.quantityX) { player.quantity = countDialog() }
        onIfModalButton(CofferComponents.quantityAll) { player.quantity = Int.MAX_VALUE }
        onIfModalButton(CofferComponents.confirm) { sacrifice() }
    }

    private fun ProtectedAccess.openCoffer() {
        player.selectedSlot = -1
        player.quantity = 0
        player.valueEach = 0
        player.cofferShown = player.deathCoffer
        ifOpenMainSidePair(main = CofferInterfaces.main, side = CofferInterfaces.side)
        // Note: the side panel's op wording is unchecked against OSRS.
        interfaceInvInit(
            inv = inv,
            target = CofferComponents.side_items,
            objRowCount = 4,
            objColCount = 7,
            op1 = "Select<col=ff9040>",
        )
        ifSetEvents(CofferComponents.side_items, inv.indices, IfEvent.Op1, IfEvent.Op10)
        for (button in BUTTONS) {
            ifSetEvents(button, -1..-1, IfEvent.Op1)
        }
    }

    private fun ProtectedAccess.select(slot: Int, op: IfButtonOp) {
        val obj = if (slot in inv.indices) inv[slot] else null
        if (obj == null) {
            return
        }
        if (op == IfButtonOp.Op10) {
            objExamine(inv, slot)
            return
        }
        player.selectedSlot = slot
        player.valueEach = sacrificeValue(obj)
    }

    private fun ProtectedAccess.sacrifice() {
        val slot = player.selectedSlot
        val obj = (if (slot in inv.indices) inv[slot] else null) ?: return
        if (player.quantity <= 0) {
            return
        }
        val value = sacrificeValue(obj)
        if (value < MIN_VALUE) {
            mes("You cannot trade that item to Death.")
            return
        }
        val type = objTypes[obj]
        val count = minOf(player.quantity, invTotal(inv, type))
        val credit = value.toLong() * count
        if (player.deathCoffer + credit > Int.MAX_VALUE) {
            mes("The coffer is too full to accommodate that.")
            return
        }
        invDel(inv, type, count)
        player.deathCoffer += credit.toInt()
        player.cofferShown = player.deathCoffer
        if (inv[slot] == null) {
            player.selectedSlot = -1
            player.valueEach = 0
        }
    }

    /**
     * Coins credited per unit of [obj], or what the client shows in red when Death refuses it: its
     * value below 10,000, or 1 for items that cannot go on the Grand Exchange.
     */
    private fun sacrificeValue(obj: InvObj): Int {
        val type = objTypes[obj]
        if (!type.tradeable || !type.stockmarket || type.param(params.bond_item)) {
            return 1
        }
        // Note: shop value until a Grand Exchange exists; its prices plug into MarketPrices.
        val price = (marketPrices[type] ?: type.cost).toLong()
        if (price < MIN_VALUE) {
            return price.toInt()
        }
        return minOf(price * 105 / 100, MAX_VALUE_EACH).toInt()
    }

    private companion object {
        const val MIN_VALUE = 10_000
        /** The wiki's cap on one item's sacrifice value. */
        const val MAX_VALUE_EACH = 2_100_000_000L

        val BUTTONS =
            listOf(
                CofferComponents.quantity1,
                CofferComponents.quantity5,
                CofferComponents.quantityX,
                CofferComponents.quantityAll,
                CofferComponents.confirm,
            )
    }
}
