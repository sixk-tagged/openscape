package org.rsmod.content.other.levelup

import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.stat.statAdvance
import org.rsmod.api.player.stat.statBase
import org.rsmod.api.testing.GameTestState

class LevelUpScriptTest {
    @Test
    fun GameTestState.`a level up shows the box until continue is clicked`() =
        runGameTest(LevelUpScript::class) {
            player.statAdvance(stats.cooking, 100.0, rate = 1.0, globalRate = 1.0)
            assertEquals(2, player.statBase(stats.cooking))
            advance()
            assertMessageSent(
                "Congratulations, you've just advanced your Cooking level. You are now level 2."
            )
            assertTrue(player.ui.containsModal(LevelUpInterfaces.display))

            player.resumePauseButton(LevelUpComponents.pause)
            advance()
            assertFalse(player.ui.containsModal(LevelUpInterfaces.display))
        }
}
