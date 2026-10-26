package org.rsmod.content.other.login

import net.rsprot.protocol.game.outgoing.varp.VarpSmall
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.varps
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.capture.CaptureClient
import org.rsmod.game.entity.player.SessionStateEvent

class LoginScriptTest {
    /**
     * The client starts with player and npc "Attack" options hidden until these varps arrive, and
     * the default (0, "depends on combat levels") is never stored, so it must be sent anyway.
     */
    @Test
    fun GameTestState.`login sends the attack option settings even at their default`() =
        runGameTest(LoginScript::class) {
            eventBus.publish(SessionStateEvent.EngineLogin(player))
            val sent = (player.client as CaptureClient).filterIsInstance<VarpSmall>()
            for (varp in listOf(varps.option_attackpriority, varps.option_attackpriority_npc)) {
                assertTrue(sent.any { it.id == varp.id && it.value == 0 }) { "$varp: $sent" }
            }
        }
}
