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
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.ObjTypeList

/**
 * A buy-only shop paid in [currency] items at fixed [prices]. Stock never runs out. Golden nuggets
 * (Prospector Percy) and unidentified minerals (Belona) use it.
 */
abstract class ItemCurrencyShopOperations(
    private val objTypes: ObjTypeList,
    private val marketPrices: MarketPrices,
    private val currency: ObjType,
    private val currencyName: String,
    private val prices: List<Pair<ObjType, Int>>,
) : StandardShopOperations {
    override fun examineShopValue(player: Player, shop: Shop, slot: Int) {
        val obj = shop.inv[slot] ?: return
        val price = priceOf(obj) ?: return
        player.mes("${objTypes[obj].name}: currently costs $price $currencyName.")
    }

    override fun shopBuy(player: Player, sideInv: Inventory, shop: Shop, slot: Int, request: Int) {
        val obj = shop.inv[slot] ?: return
        val price = priceOf(obj) ?: return
        val coin = objTypes[currency]
        val affordable = sideInv.count(coin) / price
        if (affordable == 0) {
            player.mes("You don't have enough $currencyName.")
            return
        }
        // One at a time, so a buy that runs out of space still gets what fits.
        repeat(minOf(request, affordable)) {
            if (!buyOne(player, sideInv, obj.id, coin.id, price)) {
                player.mes(NOT_ENOUGH_INV_SPACE)
                return
            }
        }
    }

    private fun buyOne(player: Player, sideInv: Inventory, obj: Int, coin: Int, price: Int) =
        player
            .invTransaction(sideInv) {
                val inv = select(sideInv)
                delete {
                    this.from = inv
                    this.obj = coin
                    this.strictCount = price
                }
                insert {
                    this.into = inv
                    this.obj = obj
                    this.strictCount = 1
                }
            }
            .success

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

    private fun priceOf(obj: InvObj): Int? = prices.firstOrNull { obj.isType(it.first) }?.second

    companion object {
        const val NOT_ENOUGH_INV_SPACE = "You don't have enough inventory space."
        const val CANNOT_SELL = "You can't sell items to this shop."
    }
}

/** Prospector Percy's nugget shop. */
class NuggetShopOperations @Inject constructor(objTypes: ObjTypeList, marketPrices: MarketPrices) :
    ItemCurrencyShopOperations(
        objTypes,
        marketPrices,
        MiningObjs.golden_nugget,
        "golden nuggets",
        PRICES,
    ) {
    companion object {
        const val NOT_ENOUGH_NUGGETS = "You don't have enough golden nuggets."
        const val NOT_ENOUGH_INV_SPACE = ItemCurrencyShopOperations.NOT_ENOUGH_INV_SPACE
        const val CANNOT_SELL = ItemCurrencyShopOperations.CANNOT_SELL

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

/**
 * Belona's Mining Guild Mineral Exchange, paid in unidentified minerals (wiki, May 2024 prices).
 */
class MineralShopOperations @Inject constructor(objTypes: ObjTypeList, marketPrices: MarketPrices) :
    ItemCurrencyShopOperations(
        objTypes,
        marketPrices,
        MiningObjs.unidentified_minerals,
        "unidentified minerals",
        listOf(
            MiningObjs.mining_gloves to 40,
            MiningObjs.superior_mining_gloves to 100,
            MiningObjs.soft_clay_pack to 10,
            MiningObjs.bag_full_of_gems to 20,
        ),
    )
