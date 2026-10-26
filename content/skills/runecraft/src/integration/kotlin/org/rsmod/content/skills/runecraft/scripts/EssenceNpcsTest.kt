package org.rsmod.content.skills.runecraft.scripts

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.stats
import org.rsmod.api.shops.config.ShopInterfaces
import org.rsmod.api.testing.GameTestState
import org.rsmod.content.skills.runecraft.configs.RunecraftNpcs
import org.rsmod.content.skills.runecraft.configs.RunecraftObjs
import org.rsmod.content.skills.runecraft.configs.RunecraftVarps
import org.rsmod.game.inv.isType

@Execution(ExecutionMode.SAME_THREAD)
class EssenceNpcsTest {
    @Test
    fun GameTestState.`Trade opens Aubury's rune shop with the wiki stock`() =
        runGameTest(EssenceNpcs::class) {
            advance()
            player.setVarp(RunecraftVarps.rune_mysteries, EssenceMine.RUNE_MYSTERIES_DONE)
            player.teleport(SPOT)
            val aubury = spawnNpc(SPOT.translateX(1), npcTypes[RunecraftNpcs.aubury])

            player.opNpc3(aubury)
            advance(ticks = 2)

            assertModalOpen(ShopInterfaces.shop_main)
            val shop = checkNotNull(player.openedShop).inv
            val stock = (0 until shop.size).mapNotNull { shop[it] }
            assertEquals(14, stock.size)
            assertEquals(5000, stock.first { it.isType(RunecraftObjs.air_rune) }.count)
            assertEquals(250, stock.first { it.isType(RunecraftObjs.death_rune) }.count)
            assertEquals(40, stock.first { it.isType(RunecraftObjs.pack_mind_rune) }.count)
            assertEquals(35, stock.first { it.isType(RunecraftObjs.pack_chaos_rune) }.count)
        }

    @Test
    fun GameTestState.`at 99 Runecraft the shop offers the cape`() =
        runGameTest(EssenceNpcs::class) {
            advance()
            player.setVarp(RunecraftVarps.rune_mysteries, EssenceMine.RUNE_MYSTERIES_DONE)
            player.stats[stats.runecrafting] = 99
            player.teleport(SPOT)
            val aubury = spawnNpc(SPOT.translateX(1), npcTypes[RunecraftNpcs.aubury])

            player.opNpc3(aubury)
            advance(ticks = 2)

            val shop = checkNotNull(player.openedShop).inv
            val stock = (0 until shop.size).mapNotNull { shop[it] }
            assertEquals(15, stock.size)
            assertTrue(stock.any { it.isType(RunecraftObjs.runecrafting_cape) })
        }
}
