package org.rsmod.api.player.hit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.stats
import org.rsmod.api.hit.plugin.PlayerHitScript
import org.rsmod.api.testing.GameTestState
import org.rsmod.game.hit.HitType

class PlayerDamagedTest {
    @Test
    fun GameTestState.`a landed hit says who took how much from whom`() =
        runGameTest(PlayerHitScript::class) {
            val attacker = registerPlayer()
            player.stats[stats.hitpoints] = 99
            var seen: PlayerDamaged? = null
            eventBus.subscribeUnbound(PlayerDamaged::class.java) { seen = this }
            player.queueHit(attacker, delay = 1, HitType.Melee, damage = 7)
            advance()
            val event = checkNotNull(seen) { "No PlayerDamaged event" }
            assertSame(player, event.target)
            assertEquals(7, event.damage)
            assertSame(attacker, event.hit.resolvePlayerSource(players))
        }
}
