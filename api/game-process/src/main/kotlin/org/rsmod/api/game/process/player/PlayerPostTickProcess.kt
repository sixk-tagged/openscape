package org.rsmod.api.game.process.player

import com.github.michaelbull.logging.InlineLogger
import jakarta.inject.Inject
import org.rsmod.api.game.process.GameLifecycle
import org.rsmod.api.player.forceDisconnect
import org.rsmod.api.player.output.MiscOutput
import org.rsmod.api.utils.logging.GameExceptionHandler
import org.rsmod.events.EventBus
import org.rsmod.game.entity.Player
import org.rsmod.game.entity.util.EntityFaceAngle
import org.rsmod.game.entity.util.ShuffledPlayerList
import org.rsmod.game.seq.EntitySeq

public class PlayerPostTickProcess
@Inject
constructor(
    private val eventBus: EventBus,
    private val playerList: ShuffledPlayerList,
    private val zoneUpdates: PlayerZoneUpdateProcessor,
    private val buildAreas: PlayerBuildAreaProcessor,
    private val regions: PlayerRegionProcessor,
    private val facing: PlayerFaceSquareProcessor,
    private val mapUpdates: PlayerMapUpdateProcessor,
    private val runUpdates: PlayerRunUpdateProcessor,
    private val invUpdates: PlayerInvUpdateProcessor,
    private val statUpdates: PlayerStatUpdateProcessor,
    private val exceptionHandler: GameExceptionHandler,
) {
    private val logger = InlineLogger()
    private val stepNanos = LongArray(STEPS.size)

    public fun process() {
        stepNanos.fill(0)
        val start = System.nanoTime()
        timed(SHARED) { computeSharedBuffers() }
        updateProtocolInfo()
        processPostTick()
        timed(FINALIZE) { finalizePostTick() }
        val elapsed = System.nanoTime() - start
        if (elapsed >= SLOW_NANOS) {
            logSlow(elapsed)
        }
    }

    private fun computeSharedBuffers() {
        zoneUpdates.computeEnclosedBuffers()
    }

    private fun updateProtocolInfo() {
        for (player in playerList) {
            player.tryOrDisconnect {
                timed(FACING) { facing.process(this) }
                timed(REGIONS) { regions.process(this) }
                timed(BUILD_AREAS) { buildAreas.process(this) }
                timed(CYCLE_UPDATE) { clientCycle.update(this) }
            }
        }
        timed(UPDATE_INFO) { eventBus.publish(GameLifecycle.UpdateInfo) }
    }

    private fun processPostTick() {
        for (player in playerList) {
            player.tryOrDisconnect {
                timed(MAP_CHANGES) { processMapChanges() }
                timed(CYCLE_FLUSH) { processClientCycle() }
                timed(ZONE_UPDATES) { processZoneUpdates() }
                timed(INV_UPDATES) { processInvUpdates() }
                timed(STAT_UPDATES) { processStatUpdates() }
                timed(RUN_UPDATES) { processRunUpdates() }
                timed(CLIENT_STATE) { processClientState() }
                cleanUpPendingUpdates()
            }
        }
    }

    /** Adds the time [block] takes to [step] (summed over every player this tick). */
    private inline fun timed(step: Int, block: () -> Unit) {
        val start = System.nanoTime()
        block()
        stepNanos[step] += System.nanoTime() - start
    }

    /** A slow post-tick names its slowest steps (see the slow cycle log in `GameCycle`). */
    private fun logSlow(elapsedNanos: Long) {
        val slowest = STEPS.indices.sortedByDescending { stepNanos[it] }.take(SLOWEST_SHOWN)
        logger.warn {
            "Slow post-tick (${elapsedNanos / NANOS_PER_MILLI}ms, ${playerList.count()} players): " +
                slowest.joinToString { "${STEPS[it]}=${stepNanos[it] / NANOS_PER_MILLI}ms" }
        }
    }

    private fun Player.processMapChanges() {
        mapUpdates.process(this)
    }

    private fun Player.processClientCycle() {
        if (loggingOut) {
            clientCycle.release()
            return
        }
        clientCycle.flush(this)
    }

    private fun Player.processZoneUpdates() {
        zoneUpdates.process(this)
    }

    private fun Player.processInvUpdates() {
        invUpdates.process(this)
    }

    private fun Player.processStatUpdates() {
        statUpdates.process(this)
    }

    private fun Player.processRunUpdates() {
        runUpdates.process(this)
    }

    private fun Player.processClientState() {
        if (closeClient) {
            closeClient = false
            closeClient()
            return
        }
        flushClient()
    }

    private fun Player.closeClient() {
        MiscOutput.logout(this)
        client.flushHighPriority()
        client.close()
    }

    private fun Player.flushClient() {
        MiscOutput.serverTickEnd(this)
        client.flush()
    }

    private fun Player.cleanUpPendingUpdates() {
        pendingSay = null
        pendingStepCount = 0
        pendingTeleport = false
        pendingTelejump = false
        pendingRunWeight = false
        pendingExactMove = null
        pendingFaceAngle = EntityFaceAngle.NULL
        pendingSequence = EntitySeq.NULL
        pendingSpotanims.clear()
        activeHeadbars.clear()
        activeHitmarks.clear()
        appearance.clearRebuildFlag()
    }

    private fun finalizePostTick() {
        zoneUpdates.clearEnclosedBuffers()
        zoneUpdates.clearPendingZoneUpdates()
        invUpdates.cleanUp()
    }

    private companion object {
        val STEPS =
            listOf(
                "sharedBuffers",
                "facing",
                "regions",
                "buildAreas",
                "cycleUpdate",
                "updateInfo",
                "mapChanges",
                "cycleFlush",
                "zoneUpdates",
                "invUpdates",
                "statUpdates",
                "runUpdates",
                "clientState",
                "finalize",
            )
        const val SHARED = 0
        const val FACING = 1
        const val REGIONS = 2
        const val BUILD_AREAS = 3
        const val CYCLE_UPDATE = 4
        const val UPDATE_INFO = 5
        const val MAP_CHANGES = 6
        const val CYCLE_FLUSH = 7
        const val ZONE_UPDATES = 8
        const val INV_UPDATES = 9
        const val STAT_UPDATES = 10
        const val RUN_UPDATES = 11
        const val CLIENT_STATE = 12
        const val FINALIZE = 13
        const val SLOWEST_SHOWN = 4
        const val NANOS_PER_MILLI = 1_000_000L
        const val SLOW_NANOS = 200 * NANOS_PER_MILLI
    }

    private inline fun Player.tryOrDisconnect(block: Player.() -> Unit) =
        try {
            block(this)
        } catch (e: Exception) {
            forceDisconnect()
            exceptionHandler.handle(e) { "Error processing post-tick for player: $this." }
        } catch (e: NotImplementedError) {
            forceDisconnect()
            exceptionHandler.handle(e) { "Error processing post-tick for player: $this." }
        }
}
