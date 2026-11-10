package org.rsmod.content.skills.slayer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.stats
import org.rsmod.api.npc.hit.processor.damageCap
import org.rsmod.api.player.events.interact.HeldUEvents
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.skills.slayer.configs.SlayerVarps
import org.rsmod.game.inv.InvObj
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class SlayerGearTest {
    @Test
    fun GameTestState.`the parts make a slayer helmet once it is unlocked`() =
        runGameTest(SlayerGear::class) {
            setUpParts()
            combine()
            assertEquals(0, player.inv.count(GearObjs.slayer_helm))

            // Malevolent Masquerade is unlock bit 5.
            player.withProtectedAccess { vars[SlayerVarps.unlocks] = 1 shl 5 }
            combine()
            assertEquals(1, player.inv.count(GearObjs.slayer_helm))
            assertEquals(1, player.inv.occupiedSpace())
        }

    @Test
    fun GameTestState.`disassembling gives the parts back`() =
        runGameTest(SlayerGear::class) {
            player.clearInv()
            player.inv[0] = InvObj(GearObjs.slayer_helm)
            player.withProtectedAccess { opHeld4(0) }
            assertEquals(0, player.inv.count(GearObjs.slayer_helm))
            assertEquals(1, player.inv.count(GearObjs.mask))
            for (part in GearObjs.other_parts) {
                assertEquals(1, player.inv.count(part))
            }
        }

    @Test
    fun GameTestState.`black masks and slayer helmets carry the combat bonus`() =
        runGameTest(SlayerGear::class) {
            assertEquals(1, objTypes[GearObjs.mask].param(params.blackmask))
            assertEquals(1, objTypes[GearObjs.imbued_mask].param(params.blackmask_imbued))
            assertEquals(1, objTypes[GearObjs.slayer_helm].param(params.slayer_helm))
            assertEquals(1, objTypes[GearObjs.slayer_helm_i].param(params.slayer_helm_imbued))
        }

    @Test
    fun GameTestState.`turoth take no damage or xp but from leaf-bladed weapons`() =
        runGameTest(SlayerGear::class) {
            val turoth = spawnNpc(CoordGrid(0, 50, 50, 31, 18), npcTypes.types.getValue(TUROTH))
            assertEquals(1, turoth.visType.param(params.leafy))
            assertEquals(0, turoth.damageCap({ false }, { false }))
            val sword = objs.leafbladed_sword.id
            assertEquals(turoth.hitpoints, turoth.damageCap({ it.id == sword }, { false }))
        }

    private fun GameTestScope.setUpParts() {
        player.clearInv()
        player.setBaseLevel(stats.crafting, 55)
        player.setCurrentLevel(stats.crafting, 55)
        player.inv[0] = InvObj(GearObjs.masks[10])
        for ((i, part) in GearObjs.other_parts.withIndex()) {
            player.inv[i + 1] = InvObj(part)
        }
    }

    private fun GameTestScope.combine() {
        val event =
            HeldUEvents.Type(objTypes[GearObjs.masks[10]], 0, objTypes[GearObjs.other_parts[0]], 1)
        player.withProtectedAccess { eventBus.publish(this, event) }
    }

    private companion object {
        // Cache id (npc.sym): `slayer_turoth`.
        const val TUROTH = 426
    }
}
