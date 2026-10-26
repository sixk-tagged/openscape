package org.rsmod.api.game.process.npc.hunt

import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.huntmodes
import org.rsmod.api.config.refs.varps
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.factory.npcTypeFactory
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.npc.NoopNpcInfo
import org.rsmod.game.entity.npc.NpcInfoProtocol
import org.rsmod.game.entity.npc.NpcMode
import org.rsmod.map.CoordGrid

class NpcPlayerHuntProcessorTest {
    @Test
    fun GameTestState.`an aggressive npc goes for players up to twice its level`() = runGameTest {
        val scorpion = spawnScorpion()
        player.teleport(START)
        player.appearance.combatLevel = 29
        // Past the start-of-game window `isInCombat` counts as recent combat.
        advance(10)
        assertNotEquals(NpcMode.OpPlayer2, scorpion.mode)

        player.appearance.combatLevel = 28
        advanceUntil(max = 3) { scorpion.mode == NpcMode.OpPlayer2 }
    }

    @Test
    fun GameTestState.`ten minutes in the same spot makes aggressive npcs tolerant`() =
        runGameTest {
            val scorpion = spawnScorpion()
            player.setVarp(varps.playtime, 5000)
            player.setVarp(varps.tolerance_region, START.packed)
            player.teleport(START)
            advance(10)
            assertNotEquals(NpcMode.OpPlayer2, scorpion.mode)

            // Leaving both regions restarts the timer, so coming back brings the aggression back.
            player.teleport(START.translateX(30))
            advance()
            assertEquals(5000, player.vars[varps.tolerance_since])
            player.teleport(START)
            advanceUntil(max = 3) { scorpion.mode == NpcMode.OpPlayer2 }
        }

    private fun GameTestScope.spawnScorpion(): Npc {
        val type =
            npcTypeFactory.create {
                vislevel = 14
                huntMode = huntmodes.cowardly.id
            }
        // Npcs only hunt while some client can see them; tests have no clients.
        return spawnNpc(START.translateX(3), type) { infoProtocol = SeenNpcInfo }
    }

    private object SeenNpcInfo : NpcInfoProtocol by NoopNpcInfo {
        override fun isActive(): Boolean = true
    }

    private companion object {
        // The Lumbridge spawn tile: open ground with nothing blocking line of sight.
        val START = CoordGrid(0, 50, 50, 30, 18)
    }
}
