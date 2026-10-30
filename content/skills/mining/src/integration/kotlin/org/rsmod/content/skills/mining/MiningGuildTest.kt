package org.rsmod.content.skills.mining

import org.junit.jupiter.api.Test
import org.rsmod.api.testing.GameTestState
import org.rsmod.content.skills.mining.configs.MiningCurrencies
import org.rsmod.content.skills.mining.configs.MiningInvs
import org.rsmod.content.skills.mining.configs.MiningObjs
import org.rsmod.content.skills.mining.scripts.MineralShopOperations
import org.rsmod.game.inv.InvObj
import org.rsmod.game.inv.Inventory
import org.rsmod.game.inv.isType
import org.rsmod.game.shop.Shop
import org.rsmod.map.CoordGrid

class MiningGuildTest {
    @Test
    fun GameTestState.`the guild gives an invisible plus 7 mining`() = runGameTest {
        val boost = MiningGuildBoost()
        player.teleport(GUILD)
        assertEquals(MiningGuild.LEVEL_BOOST, with(boost) { player.calculateBoost() })
        player.teleport(GUILD.translateZ(DUNGEON_ABOVE))
        assertEquals(0, with(boost) { player.calculateBoost() })
    }

    @Test
    fun GameTestState.`belona sells mining gloves for 40 minerals`() =
        runInjectedGameTest(MineralShopOperations::class) { ops ->
            val inv = Inventory.create(cacheTypes.invs[MiningInvs.mineral_shop])
            val shop = Shop(inv, MiningCurrencies.unidentified_minerals, 0.0, 0.0, 0.0)
            val slot = (0 until inv.size).first { inv[it].isType(MiningObjs.mining_gloves) }
            player.clearInv()
            player.inv[0] = InvObj(MiningObjs.unidentified_minerals, 45)

            ops.shopBuy(player, player.inv, shop, slot, request = 1)

            assertContains(player.inv, MiningObjs.mining_gloves)
            assertEquals(5, player.count(MiningObjs.unidentified_minerals))
        }

    private companion object {
        // In the guild's coal field.
        val GUILD = CoordGrid(3040, 9740, 0)
        // The surface above it (Falador), well outside the guild.
        const val DUNGEON_ABOVE = -6400
    }
}
