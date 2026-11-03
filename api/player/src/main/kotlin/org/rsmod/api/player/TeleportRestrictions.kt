package org.rsmod.api.player

import org.rsmod.api.area.checker.AreaChecker
import org.rsmod.game.entity.Player

/** The deepest Wilderness level spells, tablets and most jewellery still teleport from. */
public const val TELEPORT_LEVEL_LIMIT: Int = 20

/**
 * Why this player cannot teleport right now, or `null` if they can. Tele Block stops every
 * teleport; in the Wilderness, anything deeper than [limit] is refused too.
 *
 * Members' jewellery (glory, wealth...) works up to level 30: pass `limit = 30` for those.
 */
public fun Player.teleportRefusal(
    checker: AreaChecker,
    limit: Int = TELEPORT_LEVEL_LIMIT,
): String? {
    // UNVERIFIED: the wiki gives no refusal wording; both are recalled from the live client.
    if (isTeleblocked()) {
        return "A magical force stops you from teleporting."
    }
    if (mapWilderness(checker) && wildernessLevel(coords) > limit) {
        return "You can't teleport above level $limit in the Wilderness."
    }
    return null
}
