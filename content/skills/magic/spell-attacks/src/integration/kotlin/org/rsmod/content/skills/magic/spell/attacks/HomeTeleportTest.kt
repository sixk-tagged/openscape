package org.rsmod.content.skills.magic.spell.attacks

import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.components
import org.rsmod.api.config.refs.interfaces
import org.rsmod.api.testing.GameTestState
import org.rsmod.content.skills.magic.spell.attacks.standard.HomeTeleportComponents
import org.rsmod.content.skills.magic.spell.attacks.standard.HomeTeleportScript
import org.rsmod.content.skills.magic.spell.attacks.standard.HomeTeleportVarps
import org.rsmod.map.CoordGrid

class HomeTeleportTest {
    @Test
    fun GameTestState.`home teleport lands in Lumbridge then needs 30 minutes`() =
        runGameTest(HomeTeleportScript::class) {
            advance(ticks = 10) // Out of the combat window that starts at tick 0.
            player.teleport(START)
            player.ifOpenOverlay(interfaces.magic_spellbook, components.toplevel_target_side6)

            player.ifButton(HomeTeleportComponents.standard)
            val ticks = advanceUntil(max = 30) { player.coords == LUMBRIDGE }
            assertTrue(ticks >= 24) { "Landed after $ticks ticks" }
            val castAt = player.vars[HomeTeleportVarps.aide_tele_timer]
            assertTrue(castAt > 0)

            player.teleport(START)
            player.ifButton(HomeTeleportComponents.standard)
            advance() // advance() clears messages every tick: assert on the click's tick.
            val waited = (System.currentTimeMillis() / 60_000).toInt() - castAt
            assertMessageSent("You need to wait another ${30 - waited} minutes to cast this spell.")
            advance(ticks = 30)
            assertEquals(START, player.coords)
        }

    private companion object {
        val START = CoordGrid(0, 50, 50, 34, 31)
        val LUMBRIDGE = CoordGrid(0, 50, 50, 21, 18)
    }
}
