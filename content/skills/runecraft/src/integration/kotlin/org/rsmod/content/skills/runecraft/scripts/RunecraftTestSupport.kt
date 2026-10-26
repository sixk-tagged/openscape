package org.rsmod.content.skills.runecraft.scripts

import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.events.interact.LocUEvents
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.stat.PlayerStatMap
import org.rsmod.game.type.obj.ObjType
import org.rsmod.map.CoordGrid

/** Where the tests put locs and the player; an arbitrary open spot of the test map. */
internal val SPOT = CoordGrid(0, 50, 50, 20, 20)

internal fun fineXp(xp: Double): Int = PlayerStatMap.toFineXP(xp).toInt()

internal fun GameTestScope.runecraftingXp(): Int = player.statMap.getFineXP(stats.runecrafting)

internal fun GameTestScope.setRunecrafting(level: Int) {
    // A fresh test player's clock reads -1 until their first tick.
    advance()
    player.stats[stats.runecrafting] = level
    player.clearInv()
}

/** Uses [obj] on [loc], as a client would after the player walked up to it. */
internal fun GameTestScope.useOn(loc: BoundLocInfo, obj: ObjType) {
    val event = LocUEvents.Op(loc, loc, locTypes[loc], objTypes[obj], 0)
    player.withProtectedAccess { eventBus.publish(this, event) }
}
