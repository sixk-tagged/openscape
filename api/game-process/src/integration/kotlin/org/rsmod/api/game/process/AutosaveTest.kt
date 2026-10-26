package org.rsmod.api.game.process

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.registry.account.AccountAutosave
import org.rsmod.api.registry.account.AccountAutosave.Companion.AUTOSAVE_INTERVAL_CYCLES
import org.rsmod.api.registry.account.AccountAutosave.Companion.AUTOSAVE_SPACING_CYCLES
import org.rsmod.api.testing.GameTestState

class AutosaveTest {
    @Test
    fun GameTestState.`an online player is saved after the interval, not before`() =
        runInjectedGameTest(AccountAutosave::class) { autosave ->
            player.modLevel = cacheTypes.modLevels.default()
            advance()
            val first = checkNotNull(autosave.lastSavedCycle(player))
            advance(ticks = AUTOSAVE_INTERVAL_CYCLES - 2 * AUTOSAVE_SPACING_CYCLES)
            assertEquals(first, autosave.lastSavedCycle(player))
            advance(ticks = 3 * AUTOSAVE_SPACING_CYCLES)
            assertTrue(checkNotNull(autosave.lastSavedCycle(player)) > first)
        }

    @Test
    fun GameTestState.`saves are spread out`() =
        runInjectedGameTest(AccountAutosave::class) { autosave ->
            val other = registerPlayer(player.coords)
            for (p in listOf(player, other)) p.modLevel = cacheTypes.modLevels.default()
            advance(ticks = AUTOSAVE_INTERVAL_CYCLES + 3 * AUTOSAVE_SPACING_CYCLES)
            assertNotEquals(autosave.lastSavedCycle(player), autosave.lastSavedCycle(other))
        }

    @Test
    fun GameTestState.`a player logging out is skipped`() =
        runInjectedGameTest(AccountAutosave::class) { autosave ->
            player.modLevel = cacheTypes.modLevels.default()
            advance()
            val first = autosave.lastSavedCycle(player)
            player.loggingOut = true
            advance(ticks = AUTOSAVE_INTERVAL_CYCLES + 3 * AUTOSAVE_SPACING_CYCLES)
            assertEquals(first, autosave.lastSavedCycle(player))
        }

    @Test
    fun GameTestState.`saveNow saves at once`() =
        runInjectedGameTest(AccountAutosave::class) { autosave ->
            player.modLevel = cacheTypes.modLevels.default()
            advance(ticks = 3)
            autosave.saveNow(player)
            assertEquals(mapClock.cycle, autosave.lastSavedCycle(player))
        }

    @Test
    fun GameTestState.`a player the snapshot can't copy doesn't stop autosave`() =
        runInjectedGameTest(AccountAutosave::class) { autosave ->
            // A test player has no modLevel: its snapshot throws. Another player still saves.
            val other = registerPlayer(player.coords)
            other.modLevel = cacheTypes.modLevels.default()
            advance()
            val first = checkNotNull(autosave.lastSavedCycle(other))
            advance(ticks = AUTOSAVE_INTERVAL_CYCLES + 3 * AUTOSAVE_SPACING_CYCLES)
            assertTrue(checkNotNull(autosave.lastSavedCycle(other)) > first)
        }
}
