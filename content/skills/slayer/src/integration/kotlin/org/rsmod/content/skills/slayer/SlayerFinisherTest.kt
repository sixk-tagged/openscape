package org.rsmod.content.skills.slayer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.params
import org.rsmod.api.hit.plugin.NpcHitScript
import org.rsmod.api.player.events.interact.NpcUEvents
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.entity.Npc
import org.rsmod.game.hit.HitType
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.obj.ObjType
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class SlayerFinisherTest {
    @Test
    fun GameTestState.`a gargoyle stops at 1 hitpoint until a rock hammer smashes it`() =
        runGameTest(NpcHitScript::class, SlayerFinishers::class) {
            val gargoyle = spawnNpc(OPEN_GROUND.translateX(1), npcTypes.types.getValue(GARGOYLE))
            assertEquals(1, gargoyle.visType.param(params.hitpoints_floor))

            gargoyle.queueHit(player, delay = 1, HitType.Melee, damage = gargoyle.hitpoints)
            advance(ticks = 3)
            assertEquals(1, gargoyle.hitpoints)

            player.clearInv()
            player.inv[0] = InvObj(FinisherObjs.rock_hammer)
            use(gargoyle, FinisherObjs.rock_hammer)
            assertEquals(0, gargoyle.hitpoints)
            assertEquals(1, player.inv.count(FinisherObjs.rock_hammer))
        }

    @Test
    fun GameTestState.`salt on a healthy rockslug is wasted`() =
        runGameTest(SlayerFinishers::class) {
            val slug = spawnNpc(OPEN_GROUND.translateX(1), npcTypes.types.getValue(ROCKSLUG))
            val hitpoints = slug.hitpoints
            assertTrue(hitpoints > 4)

            player.clearInv()
            player.inv[0] = InvObj(FinisherObjs.bag_of_salt)
            use(slug, FinisherObjs.bag_of_salt)
            assertEquals(hitpoints, slug.hitpoints)
            assertEquals(0, player.inv.count(FinisherObjs.bag_of_salt))
        }

    private fun GameTestScope.use(npc: Npc, obj: ObjType) {
        val event = NpcUEvents.Op(npc, 0, objTypes[obj], npc.type)
        player.withProtectedAccess { eventBus.publish(this, event) }
    }

    private companion object {
        // Cache ids (npc.sym): `slayer_gargoyle_1` and `slayer_rockslug`.
        const val GARGOYLE = 412
        const val ROCKSLUG = 421

        val OPEN_GROUND = CoordGrid(0, 50, 50, 30, 18)
    }
}
