package org.rsmod.api.ge

import com.github.michaelbull.logging.InlineLogger
import jakarta.inject.Inject
import org.rsmod.api.account.AccountManager
import org.rsmod.api.account.saver.request.AccountSaveCallback
import org.rsmod.api.account.saver.request.AccountSaveResponse
import org.rsmod.api.account.saver.saveSnapshot
import org.rsmod.api.db.DatabaseConnection
import org.rsmod.game.entity.Player

/** Where Grand Exchange writes go. See [GrandExchangeImpl] for the approach. */
public interface GePersistence {
    /**
     * Writes [work] in a transaction. With an [actor], that transaction also holds the actor's
     * account save, taken now: use it whenever the actor's inventory changed in the same action.
     */
    public fun commit(actor: Player?, work: (DatabaseConnection) -> Unit)
}

/** Writes nothing: for tests, where no database is connected. */
public object NoopGePersistence : GePersistence {
    override fun commit(actor: Player?, work: (DatabaseConnection) -> Unit) {}
}

/** Sends writes through the account writer, in the same order as account saves. */
public class AccountGePersistence @Inject constructor(private val accounts: AccountManager) :
    GePersistence {
    private val logger = InlineLogger()

    override fun commit(actor: Player?, work: (DatabaseConnection) -> Unit) {
        if (actor == null) {
            accounts.write(work)
            return
        }
        val snapshot =
            try {
                actor.saveSnapshot()
            } catch (e: Exception) {
                // Writing the offers without the inventory could dupe: leave it to the next save
                // of the account, which captures the offers too.
                logger.error(e) { "Could not snapshot $actor for a Grand Exchange write." }
                return
            }
        val logFailure = AccountSaveCallback { response ->
            if (response !is AccountSaveResponse.Success) {
                logger.error { "Grand Exchange save failed for $actor: $response" }
            }
        }
        // Never retried: a retry would run behind later saves and roll the account back.
        accounts.save(snapshot, logFailure, retry = false, extra = work)
    }
}
