package org.rsmod.api.market

import org.rsmod.module.ExtendedModule

public object MarketModule : ExtendedModule() {
    override fun bind() {
        // `MarketPrices` is bound by the Grand Exchange module (guide prices from our own trades);
        // `DefaultMarketPrices` is the store-value fallback for tests.
    }
}
