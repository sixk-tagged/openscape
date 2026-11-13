package org.rsmod.api.account

import jakarta.inject.Inject
import java.util.concurrent.CopyOnWriteArrayList
import org.rsmod.api.account.loader.AccountLoaderService
import org.rsmod.api.account.loader.request.AccountLoadAuth
import org.rsmod.api.account.loader.request.AccountLoadCallback
import org.rsmod.api.account.loader.request.AccountLoadRequest
import org.rsmod.api.account.saver.AccountSavingService
import org.rsmod.api.account.saver.request.AccountSaveCallback
import org.rsmod.api.account.saver.request.AccountSaveRequest
import org.rsmod.api.db.DatabaseConnection
import org.rsmod.game.entity.Player

public class AccountManager
@Inject
constructor(private val loader: AccountLoaderService, private val saver: AccountSavingService) {
    private val saveHooks = CopyOnWriteArrayList<AccountSaveHook>()

    /** Makes every [save] also write what [hook] captures, in the same transaction. */
    public fun addSaveHook(hook: AccountSaveHook) {
        saveHooks += hook
    }

    /**
     * [retry] false for snapshot saves, see [AccountSaveRequest.retry]. [extra] is written in the
     * same transaction as the account rows, after the [AccountSaveHook]s' work.
     *
     * Call on the game thread: the hooks read live game state to capture what they write.
     */
    public fun save(
        player: Player,
        callback: AccountSaveCallback,
        retry: Boolean = true,
        extra: ((DatabaseConnection) -> Unit)? = null,
    ) {
        val captured = saveHooks.mapNotNull { it.capture(player) }
        val combined: ((DatabaseConnection) -> Unit)? =
            if (captured.isEmpty() && extra == null) {
                null
            } else {
                { connection ->
                    captured.forEach { it(connection) }
                    extra?.invoke(connection)
                }
            }
        val request =
            AccountSaveRequest(
                player.accountId,
                player.characterId,
                player,
                callback,
                retry = retry,
                extra = combined,
            )
        saver.queue(request)
    }

    /**
     * Queues [work] to run in a transaction on the account writer, in order with the saves around
     * it. Never retried: a retry would run behind later writes.
     */
    public fun write(work: (DatabaseConnection) -> Unit) {
        val request =
            AccountSaveRequest(
                accountId = -1,
                characterId = -1,
                player = Player(),
                callback = {},
                retry = false,
                extra = work,
                savesPlayer = false,
            )
        saver.queue(request)
    }

    public fun load(
        auth: AccountLoadAuth,
        loginName: String,
        callback: AccountLoadCallback,
    ): Boolean {
        val loadRequest = AccountLoadRequest.StrictSearch(auth, loginName, callback)
        return loader.queue(loadRequest)
    }

    public fun loadOrCreate(
        auth: AccountLoadAuth,
        loginName: String,
        hashedPassword: () -> String,
        callback: AccountLoadCallback,
    ): Boolean {
        val loadRequest =
            AccountLoadRequest.SearchOrCreateWithPassword(hashedPassword, auth, loginName, callback)
        return loader.queue(loadRequest)
    }

    public fun isLoaderShuttingDown(): Boolean = loader.isShuttingDown()

    public fun isLoaderRejectingRequests(): Boolean = loader.isTemporarilyRejectingRequests()
}
