package org.rsmod.api.account.saver

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.account.saver.request.AccountSaveRequest
import org.rsmod.api.account.saver.request.AccountSaveResponse
import org.rsmod.api.testing.GameTestState

/**
 * An autosave is never retried: a retry is re-queued behind later saves, so an old snapshot could
 * land after the logout save (or a newer autosave) and roll the account back. (The test database is
 * never connected, so every write here fails.)
 */
class AccountSaveRetryTest {
    @Test
    fun GameTestState.`a failed save that can't be retried is dropped, not re-queued`() =
        runInjectedGameTest(AccountSavingService::class) { service ->
            val responses = mutableListOf<AccountSaveResponse>()
            service.queue(AccountSaveRequest(1, 1, player, { responses += it }, retry = false))
            runBlocking { service.run() }
            assertEquals(1, responses.size)
            assertTrue(responses.single() is AccountSaveResponse.ExcessiveRetries)
        }

    @Test
    fun GameTestState.`a failed logout save is retried`() =
        runInjectedGameTest(AccountSavingService::class) { service ->
            val responses = mutableListOf<AccountSaveResponse>()
            service.queue(AccountSaveRequest(1, 1, player, { responses += it }))
            runBlocking { service.run() }
            assertTrue(responses.isEmpty())
        }
}
