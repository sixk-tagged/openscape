package org.rsmod.api.ge

import jakarta.inject.Inject
import org.rsmod.api.config.refs.interfaces
import org.rsmod.api.game.process.GameLifecycle
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.output.objExamine
import org.rsmod.api.player.ui.ifSetEvents
import org.rsmod.api.script.onEvent
import org.rsmod.api.script.onIfModalButton
import org.rsmod.api.script.onIfOpen
import org.rsmod.api.script.onPlayerLogin
import org.rsmod.game.MapClock
import org.rsmod.game.entity.Player
import org.rsmod.game.type.interf.IfButtonOp
import org.rsmod.game.type.interf.IfEvent
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** Sends the exchange to a player when they log in. */
public class GeLoginScript @Inject constructor(private val exchange: GrandExchange) :
    PluginScript() {
    override fun ScriptContext.startup() {
        onPlayerLogin { player.onLogin() }
    }

    private fun Player.onLogin() {
        // The offer screen reads these as objs: -1 is "no item", a raw 0 would be obj 0.
        for (varp in
            listOf(GeVarps.tradingpost_search, GeVarps.last_searched, GeVarps.last_offer_item)) {
            if (varp !in vars) {
                GeSync.setVarp(this, varp, -1)
            }
        }
        exchange.sync(this)
        if (
            vars[GeVarps.collect_reminder] != 0 && exchange.offers(this).any { it.hasCollectable() }
        ) {
            // UNVERIFIED: the wording.
            mes("You have items waiting for you to collect from the Grand Exchange.")
        }
    }

    private fun GeOffer?.hasCollectable(): Boolean =
        this != null && (collectItems > 0 || collectCoins > 0)
}

/** Buys stalled at their limit carry on after the reset, even with nobody placing offers. */
public class GeStalledBuysScript
@Inject
constructor(private val exchange: GrandExchangeImpl, private val mapClock: MapClock) :
    PluginScript() {
    override fun ScriptContext.startup() {
        onEvent<GameLifecycle.LateCycle> {
            if (mapClock.cycle % RETRY_CYCLES == 0) {
                exchange.retryStalledBuys()
            }
        }
    }

    public companion object {
        /** About a minute. */
        public const val RETRY_CYCLES: Int = 100
    }
}

/** The collection box (interface 402). */
public class GeCollectScript
@Inject
constructor(private val exchange: GrandExchange, private val objTypes: ObjTypeList) :
    PluginScript() {
    override fun ScriptContext.startup() {
        onIfOpen(interfaces.ge_collection_box) { player.enableCollectEvents() }
        onIfModalButton(GeComponents.collect_inv) { exchange.collectAll(player, toBank = false) }
        onIfModalButton(GeComponents.collect_bank) { exchange.collectAll(player, toBank = true) }
        for ((slot, component) in GeComponents.collect.withIndex()) {
            onIfModalButton(component) {
                exchange.clickCollectBox(player, objTypes, slot, it.comsub - 1, it.op)
            }
        }
    }

    private fun Player.enableCollectEvents() {
        for (component in GeComponents.collect) {
            ifSetEvents(component, 1..2, IfEvent.Op1, IfEvent.Op2, IfEvent.Op3, IfEvent.Op10)
        }
    }
}

/**
 * One click on a collect box of [slot]: [box] 0 is the items, 1 the coins. The collection box and
 * the offer screen's status panel share it.
 */
public fun GrandExchange.clickCollectBox(
    player: Player,
    objTypes: ObjTypeList,
    slot: Int,
    box: Int,
    op: IfButtonOp,
) {
    val offer = offers(player).getOrNull(slot) ?: return
    if (box !in 0..1) {
        return
    }
    if (op == IfButtonOp.Op10) {
        if (box == 0) {
            val type = objTypes.getValue(offer.obj)
            player.objExamine(type, offer.collectItems, guidePrice(type))
        }
        return
    }
    val mode =
        when (op) {
            IfButtonOp.Op3 -> GeCollectMode.Bank
            IfButtonOp.Op1 -> GeCollectMode.Inventory
            // The form op1 doesn't give: un-noted for 2+ of a note-able item, else noted.
            IfButtonOp.Op2 ->
                when {
                    box == 1 || !objTypes.getValue(offer.obj).canCert -> GeCollectMode.Unnoted
                    offer.collectItems > 1 -> GeCollectMode.Unnoted
                    else -> GeCollectMode.Noted
                }
            else -> return
        }
    if (!collect(player, slot, box, mode)) {
        // UNVERIFIED: the wording.
        player.mes("You don't have enough inventory space to collect that.")
    }
}
