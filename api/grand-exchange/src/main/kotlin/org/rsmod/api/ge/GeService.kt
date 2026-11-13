package org.rsmod.api.ge

import com.github.michaelbull.logging.InlineLogger
import jakarta.inject.Inject
import org.rsmod.api.account.AccountManager
import org.rsmod.api.db.Database
import org.rsmod.server.services.ListenerService

/**
 * Loads the order book once every service is up (the database is connected and migrated by then)
 * and makes every account save carry the saving player's Grand Exchange rows.
 */
public class GeService
@Inject
constructor(
    private val database: Database,
    private val accounts: AccountManager,
    private val exchange: GrandExchangeImpl,
) : ListenerService {
    private val logger = InlineLogger()

    override suspend fun signalStartup() {
        database.withTransaction { exchange.load(it) }
        accounts.addSaveHook { player -> exchange.saveWork(player.characterId) }
        logger.info { "Loaded the Grand Exchange." }
    }

    override suspend fun signalShutdown() {}

    override suspend fun startup() {}

    override suspend fun shutdown() {}
}
