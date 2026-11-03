package org.rsmod.content.skills.agility

import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.map.CoordGrid

/** Advances one tick and describes what the player was doing, to explain a stuck obstacle. */
internal fun GameTestScope.tick(): String {
    advance()
    val said = client.outgoingMessages.map { it.toString() }.filter { "MessageGame" in it }
    return "${player.coords} delayed=${player.isDelayed} busy=${player.isAccessProtected} " +
        "interaction=${player.interaction != null} $said"
}

/** Whether the player was sent a game message containing [text] in the tick just advanced. */
internal fun GameTestScope.said(text: String): Boolean =
    client.outgoingMessages.any { "MessageGame" in it.toString() && text in it.toString() }

/**
 * Advances until the player is on [dest] and returns null, or gives up after [max] ticks and
 * returns what the player did in the first few of them.
 */
internal fun GameTestScope.stuck(dest: CoordGrid, max: Int): String? {
    val seen = mutableListOf<String>()
    repeat(max) {
        if (player.coords == dest) {
            return null
        }
        seen += tick()
    }
    return if (player.coords == dest) null else seen.take(10).joinToString(" | ")
}
