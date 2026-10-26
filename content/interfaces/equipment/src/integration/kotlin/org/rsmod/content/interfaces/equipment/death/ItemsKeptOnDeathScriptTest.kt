package org.rsmod.content.interfaces.equipment.death

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.player.ui.IfOverlayButton
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.interfaces.equipment.configs.EquipmentTabComponents
import org.rsmod.content.interfaces.equipment.configs.EquipmentTabInvs
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.interf.IfButtonOp

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class ItemsKeptOnDeathScriptTest {
    @Test
    fun GameTestState.`the screen counts an active protect item prayer`() =
        runGameTest(ItemsKeptOnDeathScript::class) {
            carryWhipPlatebodyAndTenCoins()
            player.setVarBit(varbits.protect_item, 1)
            openKeptOnDeath()
            assertEquals(8, lostCount())
        }

    @Test
    fun GameTestState.`the screen counts an active skull`() =
        runGameTest(ItemsKeptOnDeathScript::class) {
            carryWhipPlatebodyAndTenCoins()
            player.skullIcon = 0
            openKeptOnDeath()
            assertEquals(12, lostCount())
        }

    private fun GameTestScope.carryWhipPlatebodyAndTenCoins() {
        player.inv[0] = InvObj(objs.abyssal_whip)
        player.inv[1] = InvObj(objs.rune_platebody)
        player.inv[2] = InvObj(objs.coins, 10)
    }

    private fun GameTestScope.openKeptOnDeath() {
        val button = EquipmentTabComponents.items_kept_on_death
        // A fresh test world starts at map clock 0, which still counts as "in combat".
        advance(ticks = 10)
        eventBus.publish(IfOverlayButton(player, button, -1, null, IfButtonOp.Op1))
        advance(ticks = 1)
    }

    private fun GameTestScope.lostCount(): Int =
        player.invMap.getValue(EquipmentTabInvs.death).filterNotNull().sumOf { it.count }
}
