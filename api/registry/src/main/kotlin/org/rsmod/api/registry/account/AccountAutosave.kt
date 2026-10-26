package org.rsmod.api.registry.account

import com.github.michaelbull.logging.InlineLogger
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.rsmod.api.account.AccountManager
import org.rsmod.api.account.saver.request.AccountSaveCallback
import org.rsmod.api.account.saver.request.AccountSaveResponse
import org.rsmod.api.account.saver.saveSnapshot
import org.rsmod.game.MapClock
import org.rsmod.game.entity.Player
import org.rsmod.game.entity.PlayerList

/**
 * Saves online accounts while they play: one player per [AUTOSAVE_SPACING_CYCLES], whoever was
 * saved longest ago, once that is [AUTOSAVE_INTERVAL_CYCLES] ago (about 5 minutes each with up to
 * 100 online). It saves a snapshot taken on the game thread; the saving service writes it on its
 * own thread, in order with logout saves (one FIFO queue), so a logout save always lands last; a
 * failed autosave is dropped, never retried behind later saves.
 */
@Singleton
public class AccountAutosave
@Inject
constructor(
    private val accounts: AccountManager,
    private val players: PlayerList,
    private val mapClock: MapClock,
) {
    private val logger = InlineLogger()
    private val lastSaved = HashMap<Player, Int>()

    /** Runs once a game cycle, on the game thread. */
    public fun process() {
        val now = mapClock.cycle
        val listed = players.toList()
        // Forget players who left; one logging out keeps its entry (its logout save covers it).
        lastSaved.keys.retainAll(listed.toSet())
        val online = listed.filter { !it.loggingOut && !it.pendingLogout }
        for (player in online) {
            // Just logged in: its account was just loaded, so it counts as saved now.
            lastSaved.putIfAbsent(player, now)
        }
        if (now % AUTOSAVE_SPACING_CYCLES != 0) {
            return
        }
        val due = online.minByOrNull { lastSaved.getValue(it) } ?: return
        if (now - lastSaved.getValue(due) >= AUTOSAVE_INTERVAL_CYCLES) {
            saveNow(due)
        }
    }

    /** Saves [player] now. */
    public fun saveNow(player: Player) {
        lastSaved[player] = mapClock.cycle
        // A player that can't be copied (say a half-loaded one) must never stop the cycle.
        val snapshot =
            try {
                player.saveSnapshot()
            } catch (e: Exception) {
                logger.warn(e) { "Could not snapshot $player for an autosave." }
                return
            }
        // Never retried: a retry would be written after later saves and roll the account back.
        val logFailure = AccountSaveCallback { response ->
            if (response !is AccountSaveResponse.Success) {
                logger.warn { "Autosave failed for $player: $response" }
            }
        }
        accounts.save(snapshot, logFailure, retry = false)
    }

    public fun lastSavedCycle(player: Player): Int? = lastSaved[player]

    public companion object {
        public const val AUTOSAVE_INTERVAL_CYCLES: Int = 500
        public const val AUTOSAVE_SPACING_CYCLES: Int = 5
    }
}
