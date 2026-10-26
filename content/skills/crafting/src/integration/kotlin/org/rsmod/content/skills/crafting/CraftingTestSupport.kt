package org.rsmod.content.skills.crafting

import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.events.interact.HeldUDefaultEvents
import org.rsmod.api.player.events.interact.HeldUEvents
import org.rsmod.api.player.events.interact.LocUContentEvents
import org.rsmod.api.player.events.interact.LocUDefaultEvents
import org.rsmod.api.player.events.interact.LocUEvents
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.inv.InvObj
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.stat.PlayerStatMap
import org.rsmod.game.type.obj.ObjType

/** Empties the inventory, fills it with [objs] and sets the Crafting level to [level]. */
internal fun GameTestScope.setUp(level: Int, vararg objs: ObjType) {
    player.clearInv()
    objs.forEachIndexed { slot, obj -> player.inv[slot] = InvObj(obj) }
    player.stats[stats.crafting] = level
}

internal fun GameTestScope.craftingXp(): Int = player.statMap.getFineXP(stats.crafting)

internal fun fineXp(xp: Double): Int = PlayerStatMap.toFineXP(xp).toInt()

/** [first] used on [second] where [first] has a script for any item (needle, chisel, wool...). */
internal fun GameTestScope.useOnAny(first: ObjType, second: ObjType) {
    val event = HeldUDefaultEvents.Type(objTypes[first], 0, objTypes[second], 1)
    player.withProtectedAccess { eventBus.publish(this, event) }
}

/** [first] used on [second] where that pair has its own script. */
internal fun GameTestScope.useOn(first: ObjType, second: ObjType) {
    val event = HeldUEvents.Type(objTypes[first], 0, objTypes[second], 1)
    player.withProtectedAccess { eventBus.publish(this, event) }
}

/** [item] used on a loc that scripts every item (wheel, oven, loom). */
internal fun GameTestScope.useOnLoc(loc: BoundLocInfo, item: ObjType) {
    val event = LocUDefaultEvents.OpType(loc, loc, locTypes[loc], objTypes[item], 0)
    player.withProtectedAccess { eventBus.publish(this, event) }
}

/** [item] used on a loc that has its own script for exactly that item (sand pit, fire). */
internal fun GameTestScope.useOnLocItem(loc: BoundLocInfo, item: ObjType) {
    val event = LocUEvents.Op(loc, loc, locTypes[loc], objTypes[item], 0)
    player.withProtectedAccess { eventBus.publish(this, event) }
}

/** [item] used on a furnace with its own script for that item. */
internal fun GameTestScope.useOnFurnace(furnace: BoundLocInfo, item: ObjType) {
    val event = LocUContentEvents.OpType(furnace, furnace, locTypes[furnace], objTypes[item], 0)
    player.withProtectedAccess { eventBus.publish(this, event) }
}

/** The obj with this cache name (for items the tests have no reference for). */
internal fun GameTestScope.obj(name: String): ObjType =
    objTypes.values.first { it.internalName == name }
