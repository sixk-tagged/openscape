package org.rsmod.api.death

import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.events.SuspendEvent
import org.rsmod.map.CoordGrid

/**
 * Published once per player death, after the respawn teleport and before prayers and other state
 * are reset, so handlers can still read Protect Item. Content takes the lost items here; with no
 * handler the player keeps everything.
 */
public data class PlayerDeathItems(val deathCoords: CoordGrid) : SuspendEvent<ProtectedAccess> {
    override val id: Long = 0
}
