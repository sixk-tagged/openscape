package org.rsmod.api.player

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.testing.GameTestState

class ProtectionPrayerBlockTest {
    @Test
    fun GameTestState.`severing turns protection prayers off and blocks them for a while`() =
        runGameTest {
            advance() // A fresh test player's clock reads -1 until their first tick.
            player.setVarBit(varbits.protect_from_missiles, 1)
            player.setVarBit(varbits.piety, 1)
            player.appearance.overheadIcon = constants.overhead_protect_from_missiles
            player.severProtectionPrayers(cycles = 8)
            assertEquals(0, player.vars[varbits.protect_from_missiles])
            assertEquals(1, player.vars[varbits.piety])
            assertNull(player.appearance.overheadIcon)
            assertTrue(player.isProtectionPrayerBlocked())
            advance(ticks = 8)
            assertFalse(player.isProtectionPrayerBlocked())
        }
}
