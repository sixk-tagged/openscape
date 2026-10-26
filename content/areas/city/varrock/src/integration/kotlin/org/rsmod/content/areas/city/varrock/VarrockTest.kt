package org.rsmod.content.areas.city.varrock

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.components
import org.rsmod.api.config.refs.interfaces
import org.rsmod.api.config.refs.objs
import org.rsmod.api.shops.ShopScript
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.areas.city.varrock.configs.varrock_locs
import org.rsmod.content.areas.city.varrock.configs.varrock_npcs
import org.rsmod.content.areas.city.varrock.locs.Dummies
import org.rsmod.content.areas.city.varrock.locs.VarrockLocScripts
import org.rsmod.content.areas.city.varrock.npcs.BaraekObjs
import org.rsmod.content.areas.city.varrock.npcs.GuildNpcs
import org.rsmod.content.areas.city.varrock.npcs.ServiceNpcs
import org.rsmod.content.areas.city.varrock.npcs.StrayDogs
import org.rsmod.content.areas.city.varrock.npcs.VarrockShops
import org.rsmod.game.inv.InvObj
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class VarrockTest {
    @Test
    fun GameTestState.`every shopkeeper's trade opens a stocked shop`() =
        runGameTest(
            ShopScript::class,
            VarrockScript::class,
            VarrockShops::class,
            ServiceNpcs::class,
            GuildNpcs::class,
            VarrockLocScripts::class,
            Dummies::class,
            StrayDogs::class,
        ) {
            player.teleport(START)
            for (type in SHOPKEEPERS) {
                val keeper = spawnNpc(START.translateX(1), npcTypes[type])
                advance()

                player.opNpc3(keeper)
                advanceUntil(max = 10) { player.openedShop != null }

                val shop = checkNotNull(player.openedShop) { "${type.internalNameValue}: no shop" }
                assertTrue(shop.inv.isNotEmpty()) { type.internalNameValue }
                player.ifClose()
            }
        }

    @Test
    fun GameTestState.`zaff opens his shop from talk-to`() =
        runGameTest(VarrockShops::class) {
            val zaff = spawnNpc(START.translateX(1), npcTypes[varrock_npcs.zaff])
            player.teleport(START)
            advance()

            player.opNpc1(zaff)
            talkThrough(1)

            assertNotNull(player.openedShop)
        }

    @Test
    fun GameTestState.`baraek sells a fur for 20 coins`() =
        runGameTest(ServiceNpcs::class) {
            player.clearInv()
            player.inv[0] = InvObj(objs.coins, 20)
            val baraek = spawnNpc(START.translateX(1), npcTypes[varrock_npcs.baraek])
            player.teleport(START)
            advance()

            player.opNpc1(baraek)
            talkThrough(1, 1)

            assertEquals(1, player.count(BaraekObjs.bear_fur))
            assertEquals(0, player.count(objs.coins))
        }

    @Test
    fun GameTestState.`opening a manhole swaps in the open one`() =
        runGameTest(VarrockLocScripts::class) {
            val manhole = placeMapLoc(START.translateX(1), varrock_locs.manhole_closed)
            player.teleport(START)
            advance()

            player.opLoc1(manhole)
            advanceUntil(max = 10) { locExists(START.translateX(1), varrock_locs.manhole_open) }

            assertTrue(locExists(START.translateX(1), varrock_locs.manhole_open))
        }

    /** Clicks through the chatbox, picking [options] in order on each menu, until it closes. */
    private fun GameTestScope.talkThrough(vararg options: Int) {
        val choices = options.iterator()
        repeat(200) {
            advance(ticks = 1)
            val ui = player.ui
            when {
                ui.containsModal(interfaces.chatmenu) ->
                    player.resumePauseButton(components.chatmenu_pbutton, choices.next())
                ui.containsModal(interfaces.chat_left) ->
                    player.resumePauseButton(components.chat_left_pbutton)
                ui.containsModal(interfaces.chat_right) ->
                    player.resumePauseButton(components.chat_right_pbutton)
                ui.containsModal(interfaces.messagebox) ->
                    player.resumePauseButton(components.messagebox_pbutton)
                else -> {
                    check(!choices.hasNext()) { "Dialogue ended with options left over" }
                    return
                }
            }
        }
        error("Dialogue did not end")
    }

    private companion object {
        val START = CoordGrid(0, 50, 50, 10, 10)

        val SHOPKEEPERS =
            listOf(
                varrock_npcs.zaff,
                varrock_npcs.horvik,
                varrock_npcs.lowe,
                varrock_npcs.thessalia,
                varrock_npcs.sword_keeper,
                varrock_npcs.sword_assistant,
                varrock_npcs.general_keeper,
                varrock_npcs.general_assistant,
                varrock_npcs.asyff,
                varrock_npcs.tea_seller,
                varrock_npcs.scavvo,
                varrock_npcs.valaine,
                varrock_npcs.romily,
            )
    }
}
