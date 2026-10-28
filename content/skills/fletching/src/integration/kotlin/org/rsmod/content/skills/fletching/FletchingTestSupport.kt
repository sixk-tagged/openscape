package org.rsmod.content.skills.fletching

import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.events.interact.HeldUEvents
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.inv.InvObj
import org.rsmod.game.stat.PlayerStatMap
import org.rsmod.game.type.obj.ObjType

/**
 * Empties the inventory, fills it with [objs] (a pair is an obj and how many) at Fletching [level].
 */
internal fun GameTestScope.setUp(level: Int, vararg objs: Pair<ObjType, Int>) {
    player.clearInv()
    objs.forEachIndexed { slot, (obj, count) -> player.inv[slot] = InvObj(obj, count) }
    player.stats[stats.fletching] = level
}

internal fun GameTestScope.fletchingXp(): Int = player.statMap.getFineXP(stats.fletching)

internal fun fineXp(xp: Double): Int = PlayerStatMap.toFineXP(xp).toInt()

/** [first] used on [second], where that pair has its own script. */
internal fun GameTestScope.useOn(first: ObjType, second: ObjType) {
    val event = HeldUEvents.Type(objTypes[first], 0, objTypes[second], 1)
    player.withProtectedAccess { eventBus.publish(this, event) }
}

/** The obj with this cache name. */
internal fun GameTestScope.obj(name: String): ObjType =
    objTypes.values.first { it.internalName == name }
