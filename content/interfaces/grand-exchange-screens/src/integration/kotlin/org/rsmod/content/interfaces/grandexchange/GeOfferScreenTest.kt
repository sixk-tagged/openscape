package org.rsmod.content.interfaces.grandexchange

import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.objs
import org.rsmod.api.ge.GeOfferStatus
import org.rsmod.api.ge.GeOfferType
import org.rsmod.api.ge.GeVarps
import org.rsmod.api.ge.GrandExchange
import org.rsmod.api.ge.GrandExchangeTestModule
import org.rsmod.api.player.input.ResumePObjDialogInput
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.other.grandexchangeextras.ge_extras_npcs
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.interf.IfButtonOp
import org.rsmod.game.type.obj.UnpackedObjType
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class GeOfferScreenTest {
    class Deps @Inject constructor(val ge: GrandExchange, val screen: GeOfferScreen)

    @Test
    fun `the setup buttons do the client's sums`() {
        val max = Int.MAX_VALUE
        // From 1, +10 and +100 give 10 and 100; after that they add.
        assertEquals(10, GeSetupMath.quantity(1, 10, sell = false, available = 0))
        assertEquals(20, GeSetupMath.quantity(10, 10, sell = false, available = 0))
        assertEquals(1000, GeSetupMath.quantity(1, max, sell = false, available = 0))
        assertEquals(1, GeSetupMath.quantity(1, -1, sell = false, available = 0))
        assertEquals(max, GeSetupMath.quantity(max - 5, 10, sell = false, available = 0))
        // A sell stops at what the player has; "All" is all of it.
        assertEquals(50, GeSetupMath.quantity(1, 100, sell = true, available = 50))
        assertEquals(50, GeSetupMath.quantity(3, max, sell = true, available = 50))
        // 5% is price / 20 rounded down, at least 1; never below 1 or above the max.
        assertEquals(105, GeSetupMath.percent(100, 5))
        assertEquals(9, GeSetupMath.percent(10, -5))
        assertEquals(1, GeSetupMath.percent(1, -5))
        assertEquals(max, GeSetupMath.percent(max - 1, 5))
        assertEquals(130, GeSetupMath.percent(100, 30))
        assertEquals(1, GeSetupMath.step(1, -1))
        assertEquals(max, GeSetupMath.step(max, 1))
    }

    @Test
    fun GameTestState.`a buy offer is set up with the buttons and placed`() = screenTest { deps ->
        val item = tradeItem()
        val guide = deps.ge.guidePrice(item)
        player.inv[0] = InvObj(objs.coins, 10_000_000)
        open(deps)

        player.ifButton(ge_screen_components.index[0], comsub = 3)
        advance()
        // The item search is open; pick the item.
        player.resumeActiveCoroutine(ResumePObjDialogInput(item))
        advance()
        assertEquals(1, player.vars[ge_screen_varbits.selectedslot])
        assertEquals(0, player.vars[ge_screen_varbits.newoffer_type])
        assertEquals(item.id, player.vars[ge_screen_varps.tradingpost_search])
        assertEquals(item.id, player.vars[ge_screen_varps.last_searched])
        assertEquals(1, quantity())
        assertEquals(guide, price())

        setup(4) // +10
        assertEquals(10, quantity())
        setup(5) // +100
        assertEquals(110, quantity())
        setup(6) // +1K
        assertEquals(1110, quantity())
        setup(1) // -1
        assertEquals(1109, quantity())
        setup(13) // +5%
        assertEquals(guide + maxOf(1, guide / 20), price())
        setup(11) // Guide price
        assertEquals(guide, price())
        setup(9) // +1
        assertEquals(guide + 1, price())

        player.ifButton(ge_screen_components.setup_confirm)
        advance()
        val offer = deps.ge.offers(player)[0]!!
        assertEquals(GeOfferType.Buy, offer.type)
        assertEquals(item.id, offer.obj)
        assertEquals(1109, offer.count)
        assertEquals(guide + 1, offer.price)
        // Back to the index, with "Repeat Offer" remembering it.
        assertEquals(0, player.vars[ge_screen_varbits.selectedslot])
        assertEquals(-1, player.vars[ge_screen_varps.tradingpost_search])
        assertEquals(item.id, player.vars[ge_screen_varps.last_offer_item])
        assertEquals(1109, player.vars[ge_screen_varps.last_offer_quantity])

        player.ifButton(ge_screen_components.collectall, comsub = 1)
        advance()
        assertEquals(2, player.vars[ge_screen_varbits.selectedslot])
        assertEquals(item.id, player.vars[ge_screen_varps.tradingpost_search])
        assertEquals(1109, quantity())
        assertEquals(guide + 1, price())
    }

    @Test
    fun GameTestState
        .`an inventory item is offered, sold, collected and bought again from the history`() =
        screenTest { deps ->
            val item = tradeItem()
            val guide = deps.ge.guidePrice(item)
            val seller = player
            seller.inv[0] = InvObj(objTypes.cert(item), 5)
            open(deps)

            seller.ifButton(ge_screen_components.side_items, comsub = 0)
            advance()
            assertEquals(1, seller.vars[ge_screen_varbits.selectedslot])
            assertEquals(1, seller.vars[ge_screen_varbits.newoffer_type])
            assertEquals(item.id, seller.vars[ge_screen_varps.tradingpost_search])
            assertEquals(5, quantity())
            assertEquals(guide, price())
            setup(1) // -1
            assertEquals(4, quantity())
            setup(6) // All
            assertEquals(5, quantity())

            seller.ifButton(ge_screen_components.setup_confirm)
            advance()
            assertEquals(GeOfferStatus.Active, deps.ge.offers(seller)[0]!!.status)
            assertEquals(0, seller.inv.count(objTypes.cert(item)))

            val buyer = registerPlayer(uuid = 1)
            buyer.inv[0] = InvObj(objs.coins, 10_000_000)
            deps.ge.place(buyer, 0, GeOfferType.Buy, item, 5, guide)
            val coins = deps.ge.offers(seller)[0]!!.collectCoins
            assertTrue(coins > 0)

            seller.ifButton(ge_screen_components.index[0], comsub = 2) // View offer
            advance()
            assertEquals(1, seller.vars[ge_screen_varbits.selectedslot])
            seller.ifButton(ge_screen_components.details_collect, comsub = 3) // Coin box
            advance()
            assertEquals(coins.toInt(), seller.inv.count(objs.coins))
            assertNull(deps.ge.offers(seller)[0])
            assertEquals(0, seller.vars[ge_screen_varbits.selectedslot])

            seller.ifButton(ge_screen_components.history)
            advance()
            assertTrue(seller.ui.containsModal(ge_screen_interfaces.history))
            seller.ifButton(ge_screen_components.history_list, comsub = 0, op = IfButtonOp.Op1)
            advance()
            assertTrue(seller.ui.containsModal(ge_screen_interfaces.offers))
            assertEquals(1, seller.vars[ge_screen_varbits.selectedslot])
            assertEquals(0, seller.vars[ge_screen_varbits.newoffer_type])
            assertEquals(item.id, seller.vars[ge_screen_varps.tradingpost_search])
        }

    @Test
    fun GameTestState.`the clerks set reminders and open the exchange and the history`() =
        screenTest { _ ->
            val clerk = spawnNpc(GROUND.translateX(2), npcTypes[ge_extras_npcs.ge_clerk_1])
            player.teleport(GROUND)
            advance()

            player.opNpc1(clerk)
            talkThrough(4, 1) // Reminders on, "Thank you."
            assertEquals(1, player.vars[GeVarps.collect_reminder])

            player.opNpc1(clerk)
            talkThrough(2, until = ge_screen_interfaces.offers)
            assertTrue(player.ui.containsModal(ge_screen_interfaces.offers_side))
            player.ifClose()
            advance()

            player.opNpc4(clerk)
            advanceUntil { player.ui.containsModal(ge_screen_interfaces.history) }
            player.ifButton(ge_screen_components.history_exchange)
            advance()
            assertTrue(player.ui.containsModal(ge_screen_interfaces.offers))
        }

    private fun GameTestState.screenTest(body: GameTestScope.(Deps) -> Unit) {
        runInjectedGameTest(
            Deps::class,
            GrandExchangeTestModule(),
            GeOfferScreen::class,
            GeClerks::class,
        ) { deps ->
            player.members = true
            body(this, deps)
        }
    }

    private fun GameTestScope.open(deps: Deps) {
        player.withProtectedAccess { with(deps.screen) { openExchange() } }
        advance()
        assertTrue(player.ui.containsModal(ge_screen_interfaces.offers))
    }

    private fun GameTestScope.setup(sub: Int) {
        player.ifButton(ge_screen_components.setup, comsub = sub)
        advance()
    }

    private fun GameTestScope.quantity(): Int = player.vars[ge_screen_varbits.newoffer_quantity]

    private fun GameTestScope.price(): Int = player.vars[ge_screen_varbits.newoffer_price]

    private fun GameTestScope.tradeItem(): UnpackedObjType = firstObjType {
        it.stockmarket &&
            it.tradeable &&
            !it.isCert &&
            !it.isPlaceholder &&
            !it.stackable &&
            !it.members &&
            it.canCert &&
            it.cost in 100..1000
    }

    private companion object {
        val GROUND = CoordGrid(0, 50, 50, 30, 18)
    }
}
