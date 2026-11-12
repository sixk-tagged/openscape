package org.rsmod.api.combat

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.varps
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.game.entity.Npc
import org.rsmod.map.CoordGrid

object GateNpcs : NpcReferences() {
    val ankou = find("sos_death_ankou_strongholdcave")
}

@Execution(ExecutionMode.SAME_THREAD)
class PvNGateTest {
    @Test
    fun GameTestState.`the slayer cave only allows the assigned monster`() = runGameTest {
        val ankou = spawnNpc(CAVE, npcTypes[GateNpcs.ankou])
        assertFalse(canAttack(ankou))
        assertMessageSent("You can only attack your assigned Slayer monster here.")

        val task = ankou.visType.param(params.slayer_category)
        assertTrue(task > 0)
        player.withProtectedAccess { vars[varps.slayer_target] = task }
        player.withProtectedAccess { vars[varps.slayer_count] = 0 }
        assertFalse(canAttack(ankou))

        player.withProtectedAccess { vars[varps.slayer_count] = 5 }
        assertTrue(canAttack(ankou))
    }

    @Test
    fun GameTestState.`the same monster outside the cave can be attacked by anyone`() =
        runGameTest {
            val ankou = spawnNpc(CoordGrid(3000, 3500), npcTypes[GateNpcs.ankou])
            assertTrue(canAttack(ankou))
        }

    private fun GameTestScope.canAttack(npc: Npc): Boolean {
        var allowed = true
        player.withProtectedAccess { allowed = canAttackInSlayerCave(npc) }
        return allowed
    }

    private companion object {
        val CAVE = CoordGrid(2430, 9800)
    }
}
