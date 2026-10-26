package org.rsmod.api.game.process

import com.github.michaelbull.logging.InlineLogger
import jakarta.inject.Inject
import org.rsmod.api.game.process.controller.ControllerMainProcess
import org.rsmod.api.game.process.npc.NpcMainProcess
import org.rsmod.api.game.process.npc.NpcPreTickProcess
import org.rsmod.api.game.process.player.PlayerIdShuffleProcess
import org.rsmod.api.game.process.player.PlayerInputProcess
import org.rsmod.api.game.process.player.PlayerLoginProcess
import org.rsmod.api.game.process.player.PlayerLogoutProcess
import org.rsmod.api.game.process.player.PlayerMainProcess
import org.rsmod.api.game.process.player.PlayerPostTickProcess
import org.rsmod.api.game.process.player.PlayerRouteRequestProcess
import org.rsmod.api.game.process.world.WorldMainTickProcess
import org.rsmod.api.game.process.world.WorldPostTickProcess
import org.rsmod.api.registry.account.AccountAutosave
import org.rsmod.events.EventBus
import org.rsmod.game.MapClock

public data class GameCycle
@Inject
constructor(
    private val eventBus: EventBus,
    private val mapClock: MapClock,
    private val worldMain: WorldMainTickProcess,
    private val npcPreTick: NpcPreTickProcess,
    private val playerShuffle: PlayerIdShuffleProcess,
    private val playerInput: PlayerInputProcess,
    private val playerRouteRequest: PlayerRouteRequestProcess,
    private val npcMain: NpcMainProcess,
    private val controllerMain: ControllerMainProcess,
    private val playerMain: PlayerMainProcess,
    private val playerLogout: PlayerLogoutProcess,
    private val playerLogin: PlayerLoginProcess,
    private val accountAutosave: AccountAutosave,
    private val worldPostTick: WorldPostTickProcess,
    private val playerPostTick: PlayerPostTickProcess,
) {
    private val logger = InlineLogger()
    private val phaseNanos = LongArray(PHASES.size)
    private var phase = 0
    private var phaseStart = 0L

    public fun tick() {
        val start = System.nanoTime()
        phase = 0
        phaseStart = start
        eventBus.publish(GameLifecycle.StartCycle)
        lap()
        preTick()
        mapClock.tick()
        eventBus.publish(GameLifecycle.LateCycle)
        lap()
        postTick()
        eventBus.publish(GameLifecycle.EndCycle)
        lap()
        val elapsed = System.nanoTime() - start
        if (elapsed >= SLOW_CYCLE_NANOS) {
            logSlowCycle(elapsed)
        }
    }

    private fun preTick() {
        worldMain.process()
        lap()
        npcPreTick.process()
        lap()
        playerShuffle.process()
        lap()
        playerInput.process()
        lap()
        playerRouteRequest.process()
        lap()
        npcMain.process()
        lap()
        controllerMain.process()
        lap()
        playerMain.process()
        lap()
        playerLogout.process()
        lap()
        playerLogin.process()
        lap()
        accountAutosave.process()
        lap()
    }

    /*
     * Making an educated guess that these processes occur _after_ the map clock cycles. Drew this
     * conclusion due to some clues, like `loc_del`. i.e., `loc_del(100)` will actually trigger
     * after 99 cycles, not 100.
     */
    private fun postTick() {
        worldPostTick.process()
        lap()
        playerPostTick.process()
        lap()
    }

    /** Ends the current phase of [PHASES], timing it. */
    private fun lap() {
        val now = System.nanoTime()
        phaseNanos[phase++] = now - phaseStart
        phaseStart = now
    }

    /** A slow cycle names its slowest phases, so a lag spike can be traced to its cause. */
    private fun logSlowCycle(elapsedNanos: Long) {
        val slowest = PHASES.indices.sortedByDescending { phaseNanos[it] }.take(SLOWEST_SHOWN)
        logger.warn {
            "Slow cycle (${elapsedNanos / NANOS_PER_MILLI}ms): " +
                slowest.joinToString { "${PHASES[it]}=${phaseNanos[it] / NANOS_PER_MILLI}ms" }
        }
    }

    private companion object {
        /** In the order [tick] runs them; `lateCycle` includes the map clock tick. */
        val PHASES =
            listOf(
                "startCycle",
                "worldMain",
                "npcPreTick",
                "playerShuffle",
                "playerInput",
                "playerRouteRequest",
                "npcMain",
                "controllerMain",
                "playerMain",
                "playerLogout",
                "playerLogin",
                "accountAutosave",
                "lateCycle",
                "worldPostTick",
                "playerPostTick",
                "endCycle",
            )
        const val SLOWEST_SHOWN = 4
        const val NANOS_PER_MILLI = 1_000_000L
        /** Half a tick: the cycle is not overdue yet, but something in it is heavy. */
        const val SLOW_CYCLE_NANOS = 300 * NANOS_PER_MILLI
    }
}
