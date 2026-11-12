package org.rsmod.content.skills.fishing

import org.rsmod.api.config.refs.stats
import org.rsmod.api.stats.levelmod.InvisibleLevelMod
import org.rsmod.game.entity.Player
import org.rsmod.map.CoordGrid

/** The Fishing Guild's invisible +7 Fishing boost (wiki, Fishing Guild); the platform has none. */
object FishingGuild {
    const val LEVEL_BOOST = 7

    // UNVERIFIED: the guild's bounds, from its fence and the docks' spot tiles.
    fun contains(coords: CoordGrid): Boolean =
        coords.level == 0 && coords.x in 2584..2616 && coords.z in 3394..3428
}

class FishingGuildBoost : InvisibleLevelMod(stats.fishing) {
    override fun Player.calculateBoost(): Int =
        if (FishingGuild.contains(coords)) FishingGuild.LEVEL_BOOST else 0
}
