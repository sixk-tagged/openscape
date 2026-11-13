package org.rsmod.api.ge

import jakarta.inject.Inject
import net.rsprot.protocol.game.outgoing.inv.UpdateInvFull
import net.rsprot.protocol.game.outgoing.misc.player.UpdateStockMarketSlot
import net.rsprot.protocol.game.outgoing.misc.player.UpdateStockMarketSlot.SetStockMarketSlot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.interfaces
import org.rsmod.api.config.refs.invs
import org.rsmod.api.config.refs.objs
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.capture.CaptureClient
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.entity.Player
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.interf.IfButtonOp
import org.rsmod.game.type.obj.UnpackedObjType

@Execution(ExecutionMode.SAME_THREAD)
class GrandExchangeTest {
    class Deps @Inject constructor(val ge: GrandExchange)

    class TestClock : GeClock {
        var now = 1_000_000L

        override fun millis(): Long = now
    }

    @Test
    fun GameTestState.`a new buy trades at the older sell price and gets the difference back`() =
        exchangeTest { ge, _ ->
            val item = tradeItem()
            val seller = player
            val buyer = registerPlayer(uuid = 1)
            seller.inv[0] = InvObj(objTypes.cert(item), 10)
            buyer.inv[0] = InvObj(objs.coins, 2000)

            assertPlaced(ge.place(seller, 0, GeOfferType.Sell, item, 10, 100))
            assertPlaced(ge.place(buyer, 0, GeOfferType.Buy, item, 10, 150))

            val bought = ge.offers(buyer)[0]!!
            assertEquals(GeOfferStatus.Completed, bought.status)
            assertEquals(10, bought.completedCount)
            assertEquals(1000L, bought.completedGold)
            assertEquals(10, bought.collectItems)
            assertEquals(500L, bought.collectCoins)
            assertEquals(500, buyer.inv.count(objs.coins))

            val sold = ge.offers(seller)[0]!!
            assertEquals(GeOfferStatus.Completed, sold.status)
            assertEquals(1000L, sold.completedGold)
            assertEquals(20L, sold.taxPaid)
            assertEquals(980L, sold.collectCoins)
            assertEquals(0, seller.inv.count(item))

            assertMessageSent("Grand Exchange: Finished buying 10 x ${item.name}.", buyer)
            assertMessageSent("Grand Exchange: Finished selling 10 x ${item.name}.", seller)
        }

    @Test
    fun GameTestState.`a new sell trades at the older buy price`() = exchangeTest { ge, _ ->
        val item = tradeItem()
        val buyer = player
        val seller = registerPlayer(uuid = 1)
        buyer.inv[0] = InvObj(objs.coins, 1000)
        seller.inv[0] = InvObj(objTypes.cert(item), 5)

        assertPlaced(ge.place(buyer, 0, GeOfferType.Buy, item, 5, 200))
        assertPlaced(ge.place(seller, 0, GeOfferType.Sell, item, 5, 100))

        val sold = ge.offers(seller)[0]!!
        assertEquals(1000L, sold.completedGold)
        assertEquals(20L, sold.taxPaid)
        assertEquals(980L, sold.collectCoins)
        val bought = ge.offers(buyer)[0]!!
        assertEquals(0L, bought.collectCoins)
        assertEquals(5, bought.collectItems)
    }

    @Test
    fun GameTestState.`offers fill the cheapest sell first, then the oldest, in parts`() =
        exchangeTest { ge, _ ->
            val item = tradeItem()
            val oldSeller = player
            val cheapSeller = registerPlayer(uuid = 1)
            val buyer = registerPlayer(uuid = 2)
            oldSeller.inv[0] = InvObj(objTypes.cert(item), 5)
            cheapSeller.inv[0] = InvObj(objTypes.cert(item), 5)
            buyer.inv[0] = InvObj(objs.coins, 10_000)

            ge.place(oldSeller, 0, GeOfferType.Sell, item, 5, 100)
            ge.place(cheapSeller, 0, GeOfferType.Sell, item, 5, 90)
            ge.place(buyer, 0, GeOfferType.Buy, item, 7, 100)

            assertEquals(5, ge.offers(cheapSeller)[0]!!.completedCount)
            assertEquals(450L, ge.offers(cheapSeller)[0]!!.completedGold)
            val old = ge.offers(oldSeller)[0]!!
            assertEquals(GeOfferStatus.Active, old.status)
            assertEquals(2, old.completedCount)
            assertEquals(200L, old.completedGold)
            val bought = ge.offers(buyer)[0]!!
            assertEquals(7, bought.completedCount)
            assertEquals(GeOfferStatus.Completed, bought.status)
            assertEquals(650L, bought.completedGold)
            // Reserved 700, spent 650.
            assertEquals(50L, bought.collectCoins)
        }

    @Test
    fun GameTestState.`an older offer of the same price is filled first`() = exchangeTest { ge, _ ->
        val item = tradeItem()
        val first = player
        val second = registerPlayer(uuid = 1)
        val buyer = registerPlayer(uuid = 2)
        first.inv[0] = InvObj(objTypes.cert(item), 3)
        second.inv[0] = InvObj(objTypes.cert(item), 3)
        buyer.inv[0] = InvObj(objs.coins, 1000)

        ge.place(first, 0, GeOfferType.Sell, item, 3, 100)
        ge.place(second, 0, GeOfferType.Sell, item, 3, 100)
        ge.place(buyer, 0, GeOfferType.Buy, item, 3, 100)

        assertEquals(GeOfferStatus.Completed, ge.offers(first)[0]!!.status)
        assertEquals(0, ge.offers(second)[0]!!.completedCount)
    }

    @Test
    fun GameTestState.`a player's own offers never match each other`() = exchangeTest { ge, _ ->
        val item = tradeItem()
        player.inv[0] = InvObj(objTypes.cert(item), 1)
        player.inv[1] = InvObj(objs.coins, 500)

        ge.place(player, 0, GeOfferType.Sell, item, 1, 100)
        ge.place(player, 1, GeOfferType.Buy, item, 1, 100)

        assertEquals(0, ge.offers(player)[0]!!.completedCount)
        assertEquals(0, ge.offers(player)[1]!!.completedCount)
    }

    @Test
    fun GameTestState.`abort returns the unspent coins and the bought items to the box`() =
        exchangeTest { ge, _ ->
            val item = tradeItem()
            val seller = player
            val buyer = registerPlayer(uuid = 1)
            seller.inv[0] = InvObj(objTypes.cert(item), 4)
            buyer.inv[0] = InvObj(objs.coins, 1000)

            ge.place(buyer, 0, GeOfferType.Buy, item, 10, 100)
            ge.place(seller, 0, GeOfferType.Sell, item, 4, 100)
            assertTrue(ge.abort(buyer, 0))
            assertTrue(!ge.abort(buyer, 0))

            val offer = ge.offers(buyer)[0]!!
            assertEquals(GeOfferStatus.Aborted, offer.status)
            assertEquals(4, offer.collectItems)
            assertEquals(600L, offer.collectCoins)
            val entry = GeHistoryEntry(item.id, GeOfferType.Buy, 4, 400L, 0L)
            assertEquals(entry, ge.history(buyer).single())
            // The slot is in use until everything is collected.
            assertRejected(ge.place(buyer, 0, GeOfferType.Buy, item, 1, 1))

            ge.collectAll(buyer, toBank = false)
            assertEquals(null, ge.offers(buyer)[0])
            assertEquals(600, buyer.inv.count(objs.coins))
            // More than one item comes as a note.
            assertEquals(4, buyer.inv.count(objTypes.cert(item)))
        }

    @Test
    fun GameTestState.`collect gives the chosen form or goes to the bank`() =
        exchangeTest { ge, _ ->
            val item = tradeItem()
            val seller = player
            val buyer = registerPlayer(uuid = 1)
            seller.inv[0] = InvObj(objTypes.cert(item), 5)
            buyer.inv[0] = InvObj(objs.coins, 1000)
            ge.place(seller, 0, GeOfferType.Sell, item, 3, 10)
            ge.place(buyer, 0, GeOfferType.Buy, item, 3, 10)

            assertTrue(ge.collect(buyer, 0, 0, GeCollectMode.Unnoted))
            assertEquals(3, buyer.inv.count(item))
            assertTrue(!ge.collect(buyer, 0, 0, GeCollectMode.Unnoted))
            assertEquals(null, ge.offers(buyer)[0])

            ge.place(seller, 1, GeOfferType.Sell, item, 2, 10)
            ge.place(buyer, 0, GeOfferType.Buy, item, 2, 10)
            assertTrue(ge.collect(buyer, 0, 0, GeCollectMode.Noted))
            assertEquals(2, buyer.inv.count(objTypes.cert(item)))

            // The seller's coins go to the bank or the inventory as asked.
            assertTrue(ge.collect(seller, 0, 1, GeCollectMode.Bank))
            assertEquals(0, seller.inv.count(objs.coins))
            assertEquals(30, bankCount(seller, objs.coins.id))
            assertTrue(ge.collect(seller, 1, 1, GeCollectMode.Inventory))
            assertEquals(20, seller.inv.count(objs.coins))
        }

    @Test
    fun GameTestState.`collect moves only what fits`() = exchangeTest { ge, _ ->
        val item = tradeItem()
        val seller = player
        val buyer = registerPlayer(uuid = 1)
        seller.inv[0] = InvObj(objTypes.cert(item), 3)
        buyer.inv[0] = InvObj(objs.coins, 100)
        ge.place(seller, 0, GeOfferType.Sell, item, 3, 10)
        ge.place(buyer, 0, GeOfferType.Buy, item, 3, 10)
        for (slot in 0 until buyer.inv.size) {
            buyer.inv[slot] = if (slot < 2) null else InvObj(objs.beer)
        }

        assertTrue(ge.collect(buyer, 0, 0, GeCollectMode.Unnoted))
        assertEquals(2, buyer.inv.count(item))
        assertEquals(1, ge.offers(buyer)[0]!!.collectItems)
    }

    @Test
    fun GameTestState.`the fee is 2 percent rounded down per item and capped`() =
        exchangeTest { ge, _ ->
            val item = tradeItem()
            val seller = player
            val buyer = registerPlayer(uuid = 1)
            seller.inv[0] = InvObj(objTypes.cert(item), 3)
            buyer.inv[0] = InvObj(objs.coins, Int.MAX_VALUE)

            // 49 per item: no fee. 50 per item: 1 coin each.
            ge.place(seller, 0, GeOfferType.Sell, item, 1, 49)
            ge.place(seller, 1, GeOfferType.Sell, item, 1, 50)
            ge.place(buyer, 0, GeOfferType.Buy, item, 2, 100)
            assertEquals(0L, ge.offers(seller)[0]!!.taxPaid)
            assertEquals(49L, ge.offers(seller)[0]!!.collectCoins)
            assertEquals(1L, ge.offers(seller)[1]!!.taxPaid)
            assertEquals(49L, ge.offers(seller)[1]!!.collectCoins)

            // 300m per item is 6m fee, capped at 5m.
            ge.place(seller, 2, GeOfferType.Sell, item, 1, 300_000_000)
            ge.place(buyer, 1, GeOfferType.Buy, item, 1, 300_000_000)
            assertEquals(5_000_000L, ge.offers(seller)[2]!!.taxPaid)
            assertEquals(295_000_000L, ge.offers(seller)[2]!!.collectCoins)
        }

    @Test
    fun GameTestState.`exempt items pay no fee`() = exchangeTest { ge, _ ->
        val hammer = objTypes.getValue(2347)
        val seller = player
        val buyer = registerPlayer(uuid = 1)
        seller.inv[0] = InvObj(hammer, 1)
        buyer.inv[0] = InvObj(objs.coins, 1000)
        assertPlaced(ge.place(seller, 0, GeOfferType.Sell, hammer, 1, 1000))
        assertPlaced(ge.place(buyer, 0, GeOfferType.Buy, hammer, 1, 1000))
        assertEquals(0L, ge.offers(seller)[0]!!.taxPaid)
        assertEquals(1000L, ge.offers(seller)[0]!!.collectCoins)
    }

    @Test
    fun GameTestState.`a buy over its limit fills to the limit and carries on after the reset`() =
        exchangeTest { ge, clock ->
            val item = firstObjType {
                it.tradeable() &&
                    !it.members &&
                    it.canCert &&
                    GeBuyLimits[it.id] in 5..10000 &&
                    !GeTax.isExempt(it.id)
            }
            val limit = GeBuyLimits[item.id]
            val seller = player
            val buyer = registerPlayer(uuid = 1)
            seller.inv[0] = InvObj(objTypes.cert(item), limit + 1)
            buyer.inv[0] = InvObj(objs.coins, 100_000)

            assertEquals(limit, ge.buyLimitRemaining(buyer, item))

            // An offer that has not filled yet does not use the limit.
            assertPlaced(ge.place(buyer, 0, GeOfferType.Buy, item, limit + 1, 2))
            assertEquals(limit, ge.buyLimitRemaining(buyer, item))

            ge.place(seller, 0, GeOfferType.Sell, item, limit - 1, 1)
            assertEquals(1, ge.buyLimitRemaining(buyer, item))
            ge.place(seller, 1, GeOfferType.Sell, item, 2, 1)
            assertEquals(0, ge.buyLimitRemaining(buyer, item))
            assertEquals(limit, ge.offers(buyer)[0]!!.completedCount)
            assertEquals(1, ge.offers(seller)[1]!!.completedCount)

            clock.now += GrandExchangeImpl.LIMIT_WINDOW_MILLIS - 1
            advance(GeStalledBuysScript.RETRY_CYCLES)
            assertEquals(limit, ge.offers(buyer)[0]!!.completedCount)

            clock.now += 1
            advance(GeStalledBuysScript.RETRY_CYCLES)
            val bought = ge.offers(buyer)[0]!!
            assertEquals(GeOfferStatus.Completed, bought.status)
            // The older buy sets the price of the late fill too.
            assertEquals((limit + 1) * 2L, bought.completedGold)
            assertEquals(limit - 1, ge.buyLimitRemaining(buyer, item))
        }

    @Test
    fun GameTestState.`an offline owner's offer fills and is collected after login`() =
        exchangeTest { ge, _ ->
            val item = tradeItem()
            val seller = player
            val buyer = registerPlayer(uuid = 1)
            seller.inv[0] = InvObj(objTypes.cert(item), 3)
            buyer.inv[0] = InvObj(objs.coins, 1000)
            ge.place(seller, 0, GeOfferType.Sell, item, 3, 100)

            unregisterPlayer(seller)
            ge.place(buyer, 0, GeOfferType.Buy, item, 3, 100)

            val back = registerPlayer(player = Player(), uuid = 0)
            val slot =
                messages(back).filterIsInstance<UpdateStockMarketSlot>().first { it.slot == 0 }
            val set = slot.update as SetStockMarketSlot
            assertEquals(13, set.status)
            assertEquals(3, set.completedCount)
            assertTrue(ge.collect(back, 0, 1, GeCollectMode.Inventory))
            assertEquals(294, back.inv.count(objs.coins))
            assertEquals(null, ge.offers(back)[0])
        }

    @Test
    fun GameTestState.`the guide price starts at the store value and follows trades`() =
        exchangeTest { ge, _ ->
            val item = tradeItem()
            val start = maxOf(1, item.cost)
            assertEquals(start, ge.guidePrice(item))
            assertEquals(start, GeMarketPrices(objTypes, ge)[item])

            val seller = player
            val buyer = registerPlayer(uuid = 1)
            seller.inv[0] = InvObj(objTypes.cert(item), 2)
            buyer.inv[0] = InvObj(objs.coins, 100_000)
            val traded = start * 4 + 100
            ge.place(seller, 0, GeOfferType.Sell, item, 1, traded)
            ge.place(buyer, 0, GeOfferType.Buy, item, 1, traded)

            val moved = start + (traded - start) / 8
            assertEquals(moved, ge.guidePrice(item))
            assertEquals(moved, GeMarketPrices(objTypes, ge)[item])
            assertEquals(moved, ge.guidePrice(objTypes.cert(item)))
        }

    @Test
    fun GameTestState.`the collection box collects per box and all at once`() =
        exchangeTest { ge, _ ->
            val item = tradeItem()
            val seller = player
            val buyer = registerPlayer(uuid = 1)
            seller.inv[0] = InvObj(objTypes.cert(item), 2)
            buyer.inv[0] = InvObj(objs.coins, 1000)
            ge.place(seller, 0, GeOfferType.Sell, item, 1, 100)
            ge.place(seller, 1, GeOfferType.Sell, item, 1, 100)
            ge.place(buyer, 0, GeOfferType.Buy, item, 2, 100)

            seller.ifOpenMain(interfaces.ge_collection_box)
            // Coin box of slot 0.
            seller.ifButton(GeComponents.collect[0], comsub = 2, op = IfButtonOp.Op1)
            advance()
            assertEquals(98, seller.inv.count(objs.coins))
            assertEquals(null, ge.offers(seller)[0])

            // The buyer collects everything to the bank from the top button.
            buyer.ifOpenMain(interfaces.ge_collection_box)
            buyer.ifButton(GeComponents.collect_bank)
            advance()
            assertEquals(2, bankCount(buyer, item.id))
            assertEquals(null, ge.offers(buyer)[0])
        }

    @Test
    fun GameTestState.`placing is checked`() = exchangeTest { ge, _ ->
        val item = tradeItem()
        val memberItem = firstObjType { it.tradeable() && it.members }
        val untradeable = firstObjType { !it.stockmarket && !it.isCert && !it.isPlaceholder }
        player.inv[0] = InvObj(objTypes.cert(item), 1)
        player.inv[1] = InvObj(objs.coins, 100)

        assertRejected(ge.place(player, 0, GeOfferType.Sell, item, 2, 10))
        assertRejected(ge.place(player, 0, GeOfferType.Buy, item, 11, 10))
        assertRejected(ge.place(player, 0, GeOfferType.Buy, item, 0, 10))
        assertRejected(ge.place(player, 0, GeOfferType.Buy, item, 1, 0))
        assertRejected(ge.place(player, 0, GeOfferType.Buy, item, 1 shl 20, 1 shl 20))
        assertRejected(ge.place(player, 0, GeOfferType.Buy, untradeable, 1, 1))
        // Free players have 3 slots and may only buy free items.
        assertRejected(ge.place(player, 3, GeOfferType.Buy, item, 1, 1))
        assertRejected(ge.place(player, 0, GeOfferType.Buy, memberItem, 1, 1))
        assertEquals(1, player.inv.count(objTypes.cert(item)))
        assertEquals(100, player.inv.count(objs.coins))

        player.members = true
        assertPlaced(ge.place(player, 3, GeOfferType.Buy, item, 1, 1))
        assertPlaced(ge.place(player, 0, GeOfferType.Buy, memberItem, 1, 1))
    }

    @Test
    fun GameTestState.`buying takes inventory coins first, then the bank`() =
        exchangeTest { ge, _ ->
            val item = tradeItem()
            val bank = player.invMap.getOrPut(cacheTypes.invs[invs.bank])
            player.inv[0] = InvObj(objs.coins, 60)
            bank[0] = InvObj(objs.coins, 100)

            assertPlaced(ge.place(player, 0, GeOfferType.Buy, item, 1, 100))
            assertEquals(0, player.inv.count(objs.coins))
            assertEquals(60, bank[0]!!.count)
        }

    @Test
    fun GameTestState.`login sends all slots, boxes and the varps the client reads`() =
        exchangeTest { _, _ ->
            val sent = messages(player)
            assertEquals(8, sent.filterIsInstance<UpdateStockMarketSlot>().size)
            val boxes = GeInvs.collect.map { cacheTypes.invs[it].id }
            val invIds = sent.filterIsInstance<UpdateInvFull>().map { it.inventoryId }
            assertTrue(invIds.containsAll(boxes)) { "Sent invs: $invIds" }
            assertEquals(20, player.vars[GeVarBits.taxrate])
            for (slot in 0 until 8) {
                assertEquals(-1, player.vars[GeVarps.itemsink_obj[slot]])
            }
            assertEquals(-1, player.vars[GeVarps.tradingpost_search])
        }

    private fun GameTestState.exchangeTest(body: GameTestScope.(GrandExchange, TestClock) -> Unit) {
        val clock = TestClock()
        runInjectedGameTest(
            Deps::class,
            GrandExchangeTestModule(clock = clock),
            GeLoginScript::class,
            GeCollectScript::class,
            GeStalledBuysScript::class,
        ) { deps ->
            body(this, deps.ge, clock)
        }
    }

    private fun messages(player: Player): List<Any> =
        (player.client as CaptureClient).outgoingMessages

    private fun GameTestState.bankCount(player: Player, obj: Int): Int =
        player.invMap.getOrPut(cacheTypes.invs[invs.bank]).objs.sumOf {
            if (it != null && it.id == obj) it.count else 0
        }

    private fun UnpackedObjType.tradeable(): Boolean =
        stockmarket && tradeable && !isCert && !isPlaceholder && !stackable

    private fun GameTestScope.tradeItem(): UnpackedObjType = firstObjType {
        it.tradeable() &&
            !it.members &&
            it.canCert &&
            it.cost in 20..1000 &&
            !GeTax.isExempt(it.id) &&
            (GeBuyLimits[it.id] == 0 || GeBuyLimits[it.id] >= 100)
    }

    private fun assertPlaced(result: GePlaceResult) =
        assertEquals(GePlaceResult.Placed, result) { "Rejected: $result" }

    private fun assertRejected(result: GePlaceResult) =
        assertTrue(result is GePlaceResult.Rejected) { "Not rejected: $result" }
}
