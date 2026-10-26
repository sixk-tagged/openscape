package org.rsmod.content.skills.mining.scripts

import jakarta.inject.Inject
import org.rsmod.api.invtx.invTransaction
import org.rsmod.api.invtx.select
import org.rsmod.api.market.MarketPrices
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.output.objExamine
import org.rsmod.api.shops.operation.StandardShopOperations
import org.rsmod.content.skills.mining.configs.MiningObjs
import org.rsmod.game.entity.Player
import org.rsmod.game.inv.InvObj
import org.rsmod.game.inv.Inventory
import org.rsmod.game.inv.isType
import org.rsmod.game.shop.Shop
import org.rsmod.game.type.obj.ObjTypeList

/** Buy-only, fixed-price shop paid in golden nuggets. Stock never runs out. */
class NuggetShopOperations
@Inject
constructor(private val objTypes: ObjTypeList, private val marketPrices: MarketPrices) :
    StandardShopOperations {
    override fun examineShopValue(player: Player, shop: Shop, slot: Int) {
        val obj = shop.inv[slot] ?: return
        val price = priceOf(obj) ?: return
        player.mes("${objTypes[obj].name}: currently costs $price golden nuggets.")
    }

    override fun shopBuy(player: Player, sideInv: Inventory, shop: Shop, slot: Int, request: Int) {
        val obj = shop.inv[slot] ?: return
        val price = priceOf(obj) ?: return
        val nugget = objTypes[MiningObjs.golden_nugget]
        if (sideInv.count(nugget) < price) {
            player.mes(NOT_ENOUGH_NUGGETS)
            return
        }
        val transaction =
            player.invTransaction(sideInv) {
                val inv = select(sideInv)
                delete {
                    this.from = inv
                    this.obj = nugget.id
                    this.strictCount = price
                }
                insert {
                    this.into = inv
                    this.obj = obj.id
                    this.strictCount = 1
                }
            }
        if (!transaction.success) {
            player.mes(NOT_ENOUGH_INV_SPACE)
        }
    }

    override fun examineInvValue(player: Player, sideInv: Inventory, shop: Shop, slot: Int) {
        player.mes(CANNOT_SELL)
    }

    override fun invSell(player: Player, sideInv: Inventory, shop: Shop, slot: Int, request: Int) {
        player.mes(CANNOT_SELL)
    }

    override fun examineDesc(player: Player, inv: Inventory, shop: Shop, slot: Int) {
        val obj = inv[slot] ?: return
        val type = objTypes[obj]
        player.objExamine(type, obj.count, marketPrices[type] ?: 0)
    }

    private fun priceOf(obj: InvObj): Int? = PRICES.firstOrNull { obj.isType(it.first) }?.second

    companion object {
        const val NOT_ENOUGH_NUGGETS = "You don't have enough golden nuggets."
        const val NOT_ENOUGH_INV_SPACE = "You don't have enough inventory space."
        const val CANNOT_SELL = "You can't sell items to this shop."

        private val PRICES =
            listOf(
                MiningObjs.prospector_helmet to 40,
                MiningObjs.prospector_jacket to 60,
                MiningObjs.prospector_legs to 50,
                MiningObjs.prospector_boots to 30,
                MiningObjs.mining_gloves to 60,
                MiningObjs.superior_mining_gloves to 120,
                MiningObjs.expert_mining_gloves to 180,
            )
    }
}
