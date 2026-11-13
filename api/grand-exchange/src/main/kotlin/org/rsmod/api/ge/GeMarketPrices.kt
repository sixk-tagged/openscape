package org.rsmod.api.ge

import jakarta.inject.Inject
import org.rsmod.api.market.MarketPrices
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.obj.UnpackedObjType

/**
 * The price checker, Items Kept on Death and the rest value things at the Grand Exchange guide
 * price. Objs that can't be traded there keep their store value.
 */
public class GeMarketPrices
@Inject
constructor(private val objTypes: ObjTypeList, private val exchange: GrandExchange) : MarketPrices {
    override fun get(type: UnpackedObjType): Int {
        val base = objTypes.uncert(type)
        return if (base.stockmarket) exchange.guidePrice(base) else base.cost
    }
}
