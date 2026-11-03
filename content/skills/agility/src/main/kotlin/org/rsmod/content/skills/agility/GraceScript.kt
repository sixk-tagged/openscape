package org.rsmod.content.skills.agility

import jakarta.inject.Inject
import org.rsmod.api.invtx.invTransaction
import org.rsmod.api.invtx.select
import org.rsmod.api.market.MarketPrices
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.output.objExamine
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.shops.Shops
import org.rsmod.api.shops.operation.ShopOperationMap
import org.rsmod.api.shops.operation.StandardShopOperations
import org.rsmod.content.skills.agility.configs.AgilityCurrencies
import org.rsmod.content.skills.agility.configs.AgilityInvs
import org.rsmod.content.skills.agility.configs.AgilityNpcs
import org.rsmod.content.skills.agility.configs.AgilityObjs
import org.rsmod.game.entity.Player
import org.rsmod.game.inv.InvObj
import org.rsmod.game.inv.Inventory
import org.rsmod.game.inv.isType
import org.rsmod.game.shop.Shop
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Grace of the Rogues' Den and her Graceful Clothing: the graceful outfit and amylase packs for
 * marks of grace (OSRS wiki prices). The shop never runs out and does not buy anything.
 */
class GraceScript
@Inject
constructor(
    private val shops: Shops,
    private val operations: ShopOperationMap,
    private val graceOperations: GraceShopOperations,
) : PluginScript() {
    override fun ScriptContext.startup() {
        operations.register(AgilityCurrencies.mark_of_grace, graceOperations)
        onOpNpc1(AgilityNpcs.grace) { startDialogue(it.npc) { talk() } }
        onOpNpc3(AgilityNpcs.grace) {
            shops.open(
                player,
                it.npc,
                "Grace's Graceful Clothing",
                AgilityInvs.grace_shop,
                currency = AgilityCurrencies.mark_of_grace,
            )
        }
    }

    // UNVERIFIED: her words.
    private suspend fun Dialogue.talk() {
        chatNpc(neutral, "Hello. If you have marks of grace, I can sell you graceful clothing.")
    }
}

/** Grace's Graceful Clothing, paid in marks of grace. */
class GraceShopOperations
@Inject
constructor(private val objTypes: ObjTypeList, private val marketPrices: MarketPrices) :
    StandardShopOperations {
    override fun examineShopValue(player: Player, shop: Shop, slot: Int) {
        val obj = shop.inv[slot] ?: return
        val price = priceOf(obj) ?: return
        player.mes("${objTypes[obj].name}: currently costs $price marks of grace.")
    }

    override fun shopBuy(player: Player, sideInv: Inventory, shop: Shop, slot: Int, request: Int) {
        val obj = shop.inv[slot] ?: return
        val price = priceOf(obj) ?: return
        val mark = objTypes[AgilityObjs.mark_of_grace]
        val affordable = sideInv.count(mark) / price
        if (affordable == 0) {
            player.mes("You don't have enough marks of grace.")
            return
        }
        // One at a time, so a buy that runs out of space still gets what fits.
        repeat(minOf(request, affordable)) {
            if (!buyOne(player, sideInv, obj.id, mark.id, price)) {
                player.mes(NOT_ENOUGH_INV_SPACE)
                return
            }
        }
    }

    private fun buyOne(player: Player, sideInv: Inventory, obj: Int, mark: Int, price: Int) =
        player
            .invTransaction(sideInv) {
                val inv = select(sideInv)
                delete {
                    this.from = inv
                    this.obj = mark
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

    private fun priceOf(obj: InvObj): Int? = PRICES.firstOrNull { obj.isType(it.first) }?.second

    companion object {
        const val NOT_ENOUGH_INV_SPACE = "You don't have enough inventory space."
        const val CANNOT_SELL = "You can't sell items to this shop."

        /** The wiki's prices: the set of six is 260 marks. */
        val PRICES: List<Pair<ObjType, Int>> =
            listOf(
                AgilityObjs.graceful_hood to 35,
                AgilityObjs.graceful_top to 55,
                AgilityObjs.graceful_legs to 60,
                AgilityObjs.graceful_gloves to 30,
                AgilityObjs.graceful_boots to 40,
                AgilityObjs.graceful_cape to 40,
                AgilityObjs.amylase_pack to 10,
            )
    }
}
