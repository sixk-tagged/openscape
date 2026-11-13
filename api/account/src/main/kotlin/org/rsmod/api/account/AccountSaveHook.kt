package org.rsmod.api.account

import org.rsmod.api.db.DatabaseConnection
import org.rsmod.game.entity.Player

/**
 * Adds data to every account save of a [Player], written in the same transaction as the account
 * rows (see [AccountManager.addSaveHook]).
 */
public fun interface AccountSaveHook {
    /**
     * Runs on the game thread when a save is requested. Capture what to write now (a detached copy)
     * and return the writer, which runs later on the account writer thread; or null for nothing to
     * write.
     */
    public fun capture(player: Player): ((DatabaseConnection) -> Unit)?
}
