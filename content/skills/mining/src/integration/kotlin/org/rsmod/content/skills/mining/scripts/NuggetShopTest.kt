package org.rsmod.content.skills.mining.scripts

import org.junit.jupiter.api.Test
import org.rsmod.api.testing.GameTestState
import org.rsmod.content.skills.mining.configs.MiningCurrencies
import org.rsmod.content.skills.mining.configs.MiningInvs
import org.rsmod.content.skills.mining.configs.MiningObjs
import org.rsmod.game.inv.InvObj
import org.rsmod.game.inv.Inventory
import org.rsmod.game.inv.isType
import org.rsmod.game.shop.Shop
import org.rsmod.game.type.interf.IfButtonOp
import org.rsmod.game.type.obj.ObjType

class NuggetShopTest {
    @Test
    fun GameTestState.`buy the prospector helmet with nuggets`() =
        runInjectedGameTest(NuggetShopOperations::class) { ops ->
            val shop = nuggetShop()
            val slot = slotOf(shop, MiningObjs.prospector_helmet)
            player.clearInv()

            player.inv[0] = InvObj(MiningObjs.golden_nugget, 39)
            ops.shopBuy(player, player.inv, shop, slot, request = 1)
            assertMessageSent(NuggetShopOperations.NOT_ENOUGH_NUGGETS)
            assertDoesNotContain(player.inv, MiningObjs.prospector_helmet)

            player.inv[0] = InvObj(MiningObjs.golden_nugget, 40)
            ops.shopBuy(player, player.inv, shop, slot, request = 1)
            assertContains(player.inv, MiningObjs.prospector_helmet)
            assertEquals(0, player.count(MiningObjs.golden_nugget))
        }

    @Test
    fun GameTestState.`full inventory keeps the nuggets`() =
        runInjectedGameTest(NuggetShopOperations::class) { ops ->
            val shop = nuggetShop()
            val slot = slotOf(shop, MiningObjs.prospector_helmet)
            player.fillInv()
            player.inv[0] = InvObj(MiningObjs.golden_nugget, 100)
            ops.shopBuy(player, player.inv, shop, slot, request = 1)
            assertMessageSent(NuggetShopOperations.NOT_ENOUGH_INV_SPACE)
            assertEquals(100, player.count(MiningObjs.golden_nugget))
        }

    @Test
    fun GameTestState.`shop refuses to buy items`() =
        runInjectedGameTest(NuggetShopOperations::class) { ops ->
            val shop = nuggetShop()
            player.clearInv()
            player.inv[0] = InvObj(MiningObjs.golden_nugget, 5)
            ops.sideInvOp(player, player.inv, shop, slot = 0, op = IfButtonOp.Op2)
            assertMessageSent(NuggetShopOperations.CANNOT_SELL)
            assertEquals(5, player.count(MiningObjs.golden_nugget))
        }

    private fun GameTestState.nuggetShop(): Shop {
        val inv = Inventory.create(cacheTypes.invs[MiningInvs.nugget_shop])
        return Shop(inv, MiningCurrencies.golden_nugget, 0.0, 0.0, 0.0)
    }

    private fun slotOf(shop: Shop, obj: ObjType): Int =
        (0 until shop.inv.size).first { shop.inv[it].isType(obj) }
}
