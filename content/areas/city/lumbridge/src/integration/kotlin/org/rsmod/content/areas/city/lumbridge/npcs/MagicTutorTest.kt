package org.rsmod.content.areas.city.lumbridge.npcs

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.objs
import org.rsmod.api.testing.GameTestState
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class MagicTutorTest {
    @Test
    fun GameTestState.`claim gives both runes once per half hour`() =
        runGameTest(MagicTutor::class) {
            val start = CoordGrid(0, 50, 50, 10, 10)
            player.teleport(start)
            player.clearInv()
            val tutor = spawnNpc(start.translateX(1), npcTypes[MagicTutorNpcs.tutor])
            advance()

            player.opNpc3(tutor)
            advanceUntil({ player.count(objs.air_rune) > 0 }, timeoutTicks = 10)
            assertEquals(30, player.count(objs.air_rune))
            assertEquals(30, player.count(objs.mind_rune))

            player.ifClose()
            player.clearInv()
            player.opNpc3(tutor)
            advance(ticks = 3)
            assertEquals(0, player.count(objs.air_rune))
        }
}
