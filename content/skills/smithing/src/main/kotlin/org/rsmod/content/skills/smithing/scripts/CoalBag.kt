package org.rsmod.content.skills.smithing.scripts

import org.rsmod.api.config.refs.varbits
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.intVarBit
import org.rsmod.api.script.onOpHeld1
import org.rsmod.api.script.onOpHeld3
import org.rsmod.api.script.onOpHeld4
import org.rsmod.content.skills.smithing.configs.SmithingObjs
import org.rsmod.game.entity.Player
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** Coal in the bag, kept in RSMod's saved `coal_bag_storage_count` varbit. */
var Player.coalBagCount: Int by intVarBit(varbits.coal_bag_storage_count)

const val COAL_BAG_CAPACITY = 27

fun ProtectedAccess.fillCoalBag() {
    val space = COAL_BAG_CAPACITY - player.coalBagCount
    if (space <= 0) {
        mes("Your coal bag is full.")
        return
    }
    val moved = minOf(space, invTotal(inv, SmithingObjs.coal))
    if (moved == 0) {
        mes("You don't have any coal to fill your coal bag with.")
        return
    }
    invDel(inv, SmithingObjs.coal, moved)
    player.coalBagCount += moved
    mes("You fill the coal bag with $moved coal.")
}

fun ProtectedAccess.emptyCoalBag() {
    val stored = player.coalBagCount
    if (stored == 0) {
        mes("Your coal bag is empty.")
        return
    }
    val moved = minOf(stored, inv.freeSpace())
    if (moved == 0) {
        mes("You don't have any free space in your inventory.")
        return
    }
    invAdd(inv, SmithingObjs.coal, moved)
    player.coalBagCount -= moved
    mes("You empty $moved coal from the coal bag.")
}

fun ProtectedAccess.checkCoalBag() {
    val stored = player.coalBagCount
    if (stored == 0) {
        mes("Your coal bag is empty.")
    } else {
        mes("Your coal bag contains $stored of $COAL_BAG_CAPACITY coal.")
    }
}

/** Coal the furnace can use: the inventory's plus, if the bag is carried, the bag's. */
fun ProtectedAccess.coalAvailable(): Int = invTotal(inv, SmithingObjs.coal) + bagCoal()

/** Uses [count] coal, inventory first, then the bag. Check [coalAvailable] first. */
fun ProtectedAccess.takeCoal(count: Int) {
    val fromInv = minOf(count, invTotal(inv, SmithingObjs.coal))
    if (fromInv > 0) {
        invDel(inv, SmithingObjs.coal, fromInv)
    }
    player.coalBagCount -= count - fromInv
}

private fun ProtectedAccess.bagCoal(): Int =
    if (SmithingObjs.coal_bag in inv) player.coalBagCount else 0

class CoalBagScript : PluginScript() {
    override fun ScriptContext.startup() {
        onOpHeld1(SmithingObjs.coal_bag) { fillCoalBag() }
        onOpHeld3(SmithingObjs.coal_bag) { checkCoalBag() }
        onOpHeld4(SmithingObjs.coal_bag) { emptyCoalBag() }
    }
}
