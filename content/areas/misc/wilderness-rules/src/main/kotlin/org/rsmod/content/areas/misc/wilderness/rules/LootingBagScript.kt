package org.rsmod.content.areas.misc.wilderness.rules

import jakarta.inject.Inject
import org.rsmod.api.area.checker.AreaChecker
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.invs
import org.rsmod.api.market.MarketPrices
import org.rsmod.api.player.mapWilderness
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stopInvTransmit
import org.rsmod.api.player.vars.boolVarBit
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.script.advanced.onDestroyHeld
import org.rsmod.api.script.onIfClose
import org.rsmod.api.script.onIfModalButton
import org.rsmod.api.script.onOpHeld1
import org.rsmod.api.script.onOpHeld2
import org.rsmod.api.script.onOpHeld3
import org.rsmod.api.script.onOpHeld4
import org.rsmod.api.script.onOpHeldU
import org.rsmod.game.entity.Player
import org.rsmod.game.inv.InvObj
import org.rsmod.game.inv.Inventory
import org.rsmod.game.type.interf.IfButtonOp
import org.rsmod.game.type.interf.IfEvent
import org.rsmod.game.type.inv.InvTypeList
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The looting bag: 28 stacks of tradeable items that weigh nothing, which can only be filled in the
 * Wilderness or the Ferox Enclave. Open/Close, Check, Deposit and Settings are its item ops;
 * Destroy is the engine's, and drops what the bag holds (bar food and potions) when it is in the
 * Wilderness. Its contents on death are the death script's.
 *
 * Note: items picked up off the floor do not go straight into an open bag; that is the ground-item
 * take script's. The messages and menu wording here are UNVERIFIED (not on the wiki).
 */
class LootingBagScript
@Inject
constructor(
    private val objTypes: ObjTypeList,
    private val invTypes: InvTypeList,
    private val marketPrices: MarketPrices,
    private val areaChecker: AreaChecker,
    private val objRepo: ObjRepository,
) : PluginScript() {
    private var Player.storeAll by boolVarBit(LootingBagVarBits.store_all)

    override fun ScriptContext.startup() {
        onOpHeld1(LootingBagObjs.closed) { replaceBag(it.slot, LootingBagObjs.open) }
        onOpHeld1(LootingBagObjs.open) { replaceBag(it.slot, LootingBagObjs.closed) }
        for (bag in listOf(LootingBagObjs.closed, LootingBagObjs.open)) {
            onOpHeld2(bag) { check() }
            onOpHeld3(bag) { deposit() }
            onOpHeld4(bag) { settings() }
            onOpHeldU(bag) { useOnBag(it.secondSlot) }
        }
        onIfModalButton(LootingBagComponents.items) { press(it.comsub, it.op) }
        onIfClose(LootingBagInterfaces.bag) {
            player.invMap[LootingBagInvs.bag]?.let(player::stopInvTransmit)
        }
        onDestroyHeld {
            if (type.id == LootingBagObjs.closed.id || type.id == LootingBagObjs.open.id) {
                player.destroyedBag()
            }
        }
    }

    private fun ProtectedAccess.bag(): Inventory = inv(LootingBagInvs.bag)

    private fun ProtectedAccess.replaceBag(slot: Int, to: ObjType) {
        invReplaceSlot(inv, slot, 1, to)
    }

    /** Items can only be stored where [WildernessBounds] says the Wilderness or Ferox is. */
    private fun ProtectedAccess.canStore(): Boolean =
        mapWilderness() || WildernessBounds.FEROX.contains(coords.x, coords.z)

    private fun ProtectedAccess.check() {
        ifOpenMainModal(LootingBagInterfaces.bag)
        runClientScript(SETUP_SCRIPT, 0, "Looting bag")
        invTransmit(bag())
        ifSetEvents(LootingBagComponents.items, 0 until SIZE, IfEvent.Op10)
        refresh(bag(), LootingBagInvs.bag.id)
    }

    private fun ProtectedAccess.deposit() {
        if (!canStore()) {
            mes(NOT_HERE)
            return
        }
        ifOpenMainModal(LootingBagInterfaces.bag)
        runClientScript(SETUP_SCRIPT, 1, "Add to bag")
        ifSetEvents(
            LootingBagComponents.items,
            0 until SIZE,
            IfEvent.Op1,
            IfEvent.Op2,
            IfEvent.Op3,
            IfEvent.Op4,
            IfEvent.Op9,
        )
        refresh(inv, invs.inv.id)
    }

    private suspend fun ProtectedAccess.settings() {
        player.storeAll =
            choice2(
                "Ask how many to store each time.",
                false,
                "Always store as many as possible.",
                true,
                title = "Looting bag settings",
            )
    }

    /** An item used on the bag is stored: one, five or all of it, as the player chooses. */
    private suspend fun ProtectedAccess.useOnBag(slot: Int) {
        val problem = problem(inv.getOrNull(slot) ?: return)
        if (problem != null) {
            mes(problem)
            return
        }
        val count =
            if (player.storeAll) {
                Int.MAX_VALUE
            } else {
                choice3("One.", 1, "Five.", 5, "All.", Int.MAX_VALUE, title = "Store how many?")
            }
        store(slot, count)
    }

    private suspend fun ProtectedAccess.press(slot: Int, op: IfButtonOp) {
        when (op) {
            IfButtonOp.Op1 -> store(slot, 1)
            IfButtonOp.Op2 -> store(slot, 5)
            IfButtonOp.Op3 -> store(slot, Int.MAX_VALUE)
            IfButtonOp.Op4 -> store(slot, countDialog())
            IfButtonOp.Op9 -> if (inv.getOrNull(slot) != null) objExamine(inv, slot)
            IfButtonOp.Op10 -> if (bag().getOrNull(slot) != null) objExamine(bag(), slot)
            else -> Unit
        }
    }

    /** Moves up to [count] of the pack stack in [slot] (and, if it is not stackable, its twins). */
    private fun ProtectedAccess.store(slot: Int, count: Int) {
        val stored = inv.getOrNull(slot) ?: return
        val problem = problem(stored)
        if (problem != null) {
            mes(problem)
            return
        }
        var left = count
        for (from in inv.indices) {
            val held = inv[from]?.takeIf { it.id == stored.id } ?: continue
            if (left == 0) {
                break
            }
            val move = minOf(left, held.count)
            if (invMoveFromSlot(inv, bag(), from, move).failure) {
                mes(FULL)
                break
            }
            left -= move
        }
        if (player.ui.containsModal(LootingBagInterfaces.bag)) {
            refresh(inv, invs.inv.id)
        }
    }

    /** Why [obj] cannot be stored right now, or null. */
    private fun ProtectedAccess.problem(obj: InvObj): String? =
        when {
            !canStore() -> NOT_HERE
            isBag(obj) -> BAGCEPTION
            !objTypes[obj].tradeable -> UNTRADEABLE
            else -> null
        }

    /**
     * Tells the interface's tooltips what each slot of [source] is worth (-1 for what cannot be
     * stored), and writes the bag's total.
     */
    private fun ProtectedAccess.refresh(source: Inventory, sourceInv: Int) {
        val depositing = source === inv
        val prices = Array<Any>(SIZE) { price(source.getOrNull(it), depositing) }
        runClientScript(PRICES_SCRIPT, sourceInv, *prices)
        val total =
            if (depositing) {
                0L
            } else {
                source.filterNotNull().sumOf { price(it, false).toLong() * it.count }
            }
        val text = if (depositing) "" else "Total value: %,d".format(total)
        ifSetText(LootingBagComponents.total, text)
    }

    private fun ProtectedAccess.price(obj: InvObj?, depositing: Boolean): Int {
        if (obj == null) {
            return -1
        }
        val type = objTypes[obj]
        if (depositing && (!type.tradeable || isBag(obj))) {
            return -1
        }
        return marketPrices[type] ?: type.cost
    }

    /**
     * In the Wilderness what the destroyed bag held lands at the player's feet, in view of
     * everyone, except un-noted food, potions and vials of water, which are destroyed with it;
     * anywhere else all of it is lost.
     */
    private fun Player.destroyedBag() {
        val bag = invMap.getOrPut(invTypes[LootingBagInvs.bag])
        val dropHere = mapWilderness(areaChecker)
        val duration = lootDropDuration ?: constants.lootdrop_duration
        for (slot in bag.indices) {
            val held = bag[slot] ?: continue
            bag[slot] = null
            if (dropHere && !perishes(held)) {
                objRepo.add(held, coords, duration)
            }
        }
    }

    // Note: the cache has no food/potion marker on objs; anything you Eat or Drink counts.
    private fun perishes(obj: InvObj): Boolean =
        objTypes[obj].iop.any { it == "Eat" || it == "Drink" } ||
            obj.id == LootingBagObjs.vial_water.id

    private fun Inventory.getOrNull(slot: Int): InvObj? = if (slot in indices) this[slot] else null

    private fun isBag(obj: InvObj): Boolean =
        obj.id == LootingBagObjs.closed.id || obj.id == LootingBagObjs.open.id

    companion object {
        const val SIZE = 28

        /** `wilderness_lootingbag_setup(mode, title)`: 0 shows the bag, 1 the pack to add from. */
        private const val SETUP_SCRIPT = 495

        /** Sets the interface's price tooltips: the inventory, then 28 prices. */
        private const val PRICES_SCRIPT = 1235

        // UNVERIFIED: none of these messages are on the wiki.
        private const val NOT_HERE = "You can only add items to the looting bag in the Wilderness."
        private const val BAGCEPTION =
            "You may be surprised to learn that bagception is not permitted."
        private const val UNTRADEABLE = "You can't put that in the looting bag."
        private const val FULL = "Your looting bag is full."
    }
}
