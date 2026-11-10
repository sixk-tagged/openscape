package org.rsmod.content.skills.slayer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.config.refs.varps
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.skills.slayer.configs.SlayerNpcs
import org.rsmod.content.skills.slayer.configs.SlayerVarBits
import org.rsmod.content.skills.slayer.configs.SlayerVarps
import org.rsmod.game.type.interf.IfButtonOp
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class SlayerRewardsTest {
    @Test
    fun GameTestState.`the rewards option opens the rewards screen for that master`() =
        runGameTest(SlayerScript::class, SlayerRewards::class) {
            openRewards()
            assertTrue(player.ui.containsModal(RewardInterfaces.slayer_rewards))
            assertEquals(3, player.vars[RewardVarBits.master_in_focus])
        }

    @Test
    fun GameTestState.`confirming an unlock spends the points and sets the bit`() =
        runGameTest(SlayerScript::class, SlayerRewards::class) {
            player.withProtectedAccess { vars[SlayerVarBits.points] = 50 }
            openRewards()
            // Bit 1, "Slug Salter", costs 10 points.
            player.ifButton(RewardComponents.confirm_button, comsub = 1)
            advance()
            assertEquals(40, player.vars[SlayerVarBits.points])
            assertEquals(1, (player.vars[SlayerVarps.unlocks] shr 1) and 1)
        }

    @Test
    fun GameTestState.`an unlock without the points does nothing`() =
        runGameTest(SlayerScript::class, SlayerRewards::class) {
            player.withProtectedAccess { vars[SlayerVarBits.points] = 5 }
            openRewards()
            player.ifButton(RewardComponents.confirm_button, comsub = 1)
            advance()
            assertEquals(5, player.vars[SlayerVarBits.points])
            assertEquals(0, player.vars[SlayerVarps.unlocks])
        }

    @Test
    fun GameTestState.`blocking a task with vannaka costs 60 and fills his first slot`() =
        runGameTest(SlayerScript::class, SlayerRewards::class) {
            player.withProtectedAccess {
                vars[SlayerVarBits.points] = 100
                vars[varbits.slayer_master] = 3
                vars[varps.slayer_target] = GOBLINS
                vars[varps.slayer_count] = 10
            }
            openRewards()
            player.ifButton(RewardComponents.confirm_button, comsub = 59)
            advance()
            assertEquals(40, player.vars[SlayerVarBits.points])
            assertEquals(GOBLINS, player.vars[RewardVarBits.vannaka_blocks.first()])
            assertEquals(0, player.vars[varps.slayer_count])

            // Unblocking slot 1 (code 60) clears it again, with no refund.
            player.ifButton(RewardComponents.confirm_button, comsub = 60)
            advance()
            assertEquals(0, player.vars[RewardVarBits.vannaka_blocks.first()])
            assertEquals(40, player.vars[SlayerVarBits.points])
        }

    @Test
    fun GameTestState.`cancelling a task costs 30 points`() =
        runGameTest(SlayerScript::class, SlayerRewards::class) {
            player.withProtectedAccess {
                vars[SlayerVarBits.points] = 30
                vars[varps.slayer_target] = GOBLINS
                vars[varps.slayer_count] = 10
            }
            openRewards()
            player.ifButton(RewardComponents.confirm_button, comsub = 58)
            advance()
            assertEquals(0, player.vars[SlayerVarBits.points])
            assertEquals(0, player.vars[varps.slayer_count])
        }

    @Test
    fun GameTestState.`buying a slayer ring costs 75 points`() =
        runGameTest(SlayerScript::class, SlayerRewards::class) {
            player.clearInv()
            player.withProtectedAccess { vars[SlayerVarBits.points] = 100 }
            openRewards()
            player.ifButton(RewardComponents.buy_items, comsub = 0, op = IfButtonOp.Op2)
            advance()
            assertEquals(25, player.vars[SlayerVarBits.points])
            assertEquals(1, player.inv.occupiedSpace())
        }

    private fun GameTestScope.openRewards() {
        val vannaka = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[SlayerNpcs.vannaka])
        player.teleport(OPEN_GROUND)
        advance()
        player.opNpc5(vannaka)
        advanceUntil(max = 10) { player.ui.containsModal(RewardInterfaces.slayer_rewards) }
    }

    private companion object {
        const val GOBLINS = 2
        val OPEN_GROUND = CoordGrid(0, 50, 50, 30, 18)
    }
}
