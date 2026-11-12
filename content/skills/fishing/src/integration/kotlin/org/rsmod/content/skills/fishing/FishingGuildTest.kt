package org.rsmod.content.skills.fishing

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.rsmod.api.testing.GameTestState
import org.rsmod.map.CoordGrid

class FishingGuildTest {
    @Test
    fun GameTestState.`the guild gives an invisible plus 7 fishing`() = runGameTest {
        val boost = FishingGuildBoost()
        player.teleport(CoordGrid(2600, 3410, 0))
        assertEquals(FishingGuild.LEVEL_BOOST, with(boost) { player.calculateBoost() })
        player.teleport(CoordGrid(2600, 3440, 0))
        assertEquals(0, with(boost) { player.calculateBoost() })
    }
}
