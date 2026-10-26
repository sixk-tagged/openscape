package org.rsmod.api.game.process.player

import jakarta.inject.Inject
import kotlin.collections.iterator
import kotlin.math.abs
import org.rsmod.api.config.refs.varps
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.registry.region.RegionRegistry
import org.rsmod.game.area.AreaIndex
import org.rsmod.game.entity.Player
import org.rsmod.map.CoordGrid

public class PlayerAreaProcessor
@Inject
constructor(private val index: AreaIndex, private val regions: RegionRegistry) {
    private var Player.toleranceRegion by intVarp(varps.tolerance_region)
    private var Player.toleranceRegionOld by intVarp(varps.tolerance_region_old)
    private var Player.toleranceSince by intVarp(varps.tolerance_since)
    private val Player.playtime by intVarp(varps.playtime)

    public fun process(player: Player) {
        player.processAreaChange()
    }

    private fun Player.processAreaChange() {
        // Area exit queues must be sent on the same cycle the player is queued to log out -
        // either via manual logout or forced disconnect.
        val forceExitAreas = pendingLogout || forceDisconnect

        val processAreas = forceExitAreas || coords != lastProcessedAreaCoord
        if (!processAreas) {
            return
        }
        lastProcessedAreaCoord = coords
        updateToleranceRegions()

        val normalizedCoords = normalizedCoords()
        pendingAreas.clear()
        if (!forceExitAreas) {
            index.putAreas(normalizedCoords, pendingAreas)
        }

        for (area in activeAreas.iterator()) {
            if (area in pendingAreas) {
                continue
            }
            engineQueueAreaExit(area)
        }

        for (area in pendingAreas.iterator()) {
            if (area in activeAreas) {
                continue
            }
            engineQueueArea(area)
        }

        activeAreas.clear()
        activeAreas.addAll(pendingAreas)
    }

    // Tolerance (OSRS wiki): the player keeps two 21x21 regions. Leaving both swaps the older one
    // for a region centred here and restarts the timer `NpcPlayerHuntProcessor` checks. Varps, so
    // logging out keeps them, as on the wiki.
    private fun Player.updateToleranceRegions() {
        if (
            coords.inToleranceRegion(toleranceRegion) ||
                coords.inToleranceRegion(toleranceRegionOld)
        ) {
            return
        }
        toleranceRegionOld = toleranceRegion
        toleranceRegion = coords.packed
        toleranceSince = playtime
    }

    private fun CoordGrid.inToleranceRegion(packedCentre: Int): Boolean {
        val centre = CoordGrid(packedCentre)
        return level == centre.level && abs(x - centre.x) <= 10 && abs(z - centre.z) <= 10
    }

    private fun Player.normalizedCoords(): CoordGrid =
        if (RegionRegistry.inWorkingArea(coords)) {
            regions.normalizeCoords(coords)
        } else {
            coords
        }
}
