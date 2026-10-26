package org.rsmod.api.account.saver.request

import org.rsmod.game.entity.Player

public data class AccountSaveRequest(
    val accountId: Int,
    val characterId: Int,
    val player: Player,
    val callback: AccountSaveCallback,
    var attempts: Int = 0,
    /**
     * False for a snapshot save (an autosave): a retry is re-queued behind later saves, so it could
     * overwrite the logout save or a newer snapshot and roll the account back. A failed one is
     * dropped instead; the next save carries newer data anyway.
     */
    val retry: Boolean = true,
)
