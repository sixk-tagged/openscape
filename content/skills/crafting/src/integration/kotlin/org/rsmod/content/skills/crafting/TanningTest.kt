package org.rsmod.content.skills.crafting

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.components
import org.rsmod.api.config.refs.interfaces
import org.rsmod.api.player.events.interact.NpcUDefaultEvents
import org.rsmod.api.testing.GameTestState
import org.rsmod.game.inv.InvObj
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class TanningTest {
    @Test
    fun GameTestState.`ellis tans hides for coins from the tanner interface`() =
        runGameTest(Tanning::class) {
            val start = CoordGrid(0, 50, 50, 10, 10)
            player.teleport(start)
            val ellis = spawnNpc(start.translateX(1), npcTypes[CraftingNpcs.ellis])
            advance()
            val cowhide = TannerHides.all[0].raw
            val soft = TannerHides.all[0].leather
            val hard = TannerHides.all[1].leather
            val slots = CraftingComponents.tanning

            player.clearInv()
            repeat(5) { player.inv[it] = InvObj(cowhide) }
            player.inv[5] = InvObj(CraftingObjs.coins, 10)
            player.opNpc3(ellis)
            advance(ticks = 2)
            assertModalOpen(CraftingInterfaces.tanner)

            player.ifButton(slots[0].all)
            advance()
            assertEquals(5, player.count(soft))
            assertEquals(0, player.count(cowhide))
            assertEquals(5, player.count(CraftingObjs.coins))

            // Hard leather costs 3 coins a hide: 5 coins only pay for one of the three.
            repeat(3) { player.inv[10 + it] = InvObj(cowhide) }
            player.ifButton(slots[1].all)
            advance()
            assertEquals(1, player.count(hard))
            assertEquals(2, player.count(cowhide))
            assertEquals(2, player.count(CraftingObjs.coins))
        }

    @Test
    fun GameTestState.`a hide used on a tanner opens the interface`() =
        runGameTest(Tanning::class) {
            val start = CoordGrid(0, 50, 50, 10, 10)
            player.teleport(start)
            val ellis = spawnNpc(start.translateX(1), npcTypes[CraftingNpcs.ellis])
            advance()
            player.clearInv()

            val hide = TannerHides.all[4].raw
            val event =
                NpcUDefaultEvents.OpType(ellis, 0, objTypes[hide], npcTypes[CraftingNpcs.ellis])
            player.withProtectedAccess { eventBus.publish(this, event) }
            advance()
            assertModalOpen(CraftingInterfaces.tanner)
        }

    @Test
    fun GameTestState.`talking to ellis with a hide offers to tan it`() =
        runGameTest(Tanning::class) {
            val start = CoordGrid(0, 50, 50, 10, 10)
            player.teleport(start)
            val ellis = spawnNpc(start.translateX(1), npcTypes[CraftingNpcs.ellis])
            advance()
            player.clearInv()
            player.inv[0] = InvObj(TannerHides.all[0].raw)

            player.opNpc1(ellis)
            repeat(40) {
                advance()
                val ui = player.ui
                when {
                    ui.containsModal(CraftingInterfaces.tanner) -> return@repeat
                    ui.containsModal(interfaces.chatmenu) ->
                        player.resumePauseButton(components.chatmenu_pbutton, 1)
                    ui.containsModal(interfaces.chat_left) ->
                        player.resumePauseButton(components.chat_left_pbutton)
                    ui.containsModal(interfaces.chat_right) ->
                        player.resumePauseButton(components.chat_right_pbutton)
                }
            }
            assertModalOpen(CraftingInterfaces.tanner)
        }
}
