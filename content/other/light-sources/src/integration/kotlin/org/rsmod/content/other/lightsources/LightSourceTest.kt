package org.rsmod.content.other.lightsources

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.events.interact.HeldUEvents
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.obj.ObjType

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class LightSourceTest {
    @Test
    fun GameTestState.`a tinderbox lights a candle`() =
        runGameTest(LightSourceScript::class) {
            setUp(LightSourceObjs.candle_unlit)
            useTinderboxOn(LightSourceObjs.candle_unlit)
            assertEquals(LightSourceObjs.candle_lit.id, player.inv[1]?.id)
            assertEquals(1, LightSources.brightness(player))
            assertEquals(1, LightSources.nakedFlames(player).size)
        }

    @Test
    fun GameTestState.`a bullseye lantern needs firemaking 49`() =
        runGameTest(LightSourceScript::class) {
            setUp(LightSourceObjs.bullseye_lantern_unlit)
            useTinderboxOn(LightSourceObjs.bullseye_lantern_unlit)
            assertMessageSent("You need a Firemaking level of 49 to light this.")
            assertEquals(LightSourceObjs.bullseye_lantern_unlit.id, player.inv[1]?.id)
            assertEquals(0, LightSources.brightness(player))

            player.setBaseLevel(stats.firemaking, 49)
            player.setCurrentLevel(stats.firemaking, 49)
            useTinderboxOn(LightSourceObjs.bullseye_lantern_unlit)
            assertEquals(3, LightSources.brightness(player))
            assertEquals(0, LightSources.nakedFlames(player).size)
        }

    @Test
    fun GameTestState.`extinguishing a candle puts the light out`() =
        runGameTest(LightSourceScript::class) {
            player.clearInv()
            player.inv[0] = InvObj(LightSourceObjs.candle_lit)
            player.withProtectedAccess { opHeld1(0) }
            assertEquals(LightSourceObjs.candle_unlit.id, player.inv[0]?.id)
            assertEquals(0, LightSources.brightness(player))
        }

    private fun GameTestScope.setUp(unlit: ObjType) {
        player.clearInv()
        player.inv[0] = InvObj(LightSourceObjs.tinderbox)
        player.inv[1] = InvObj(unlit)
    }

    private fun GameTestScope.useTinderboxOn(unlit: ObjType) {
        val event = HeldUEvents.Type(objTypes[LightSourceObjs.tinderbox], 0, objTypes[unlit], 1)
        player.withProtectedAccess { eventBus.publish(this, event) }
    }
}
