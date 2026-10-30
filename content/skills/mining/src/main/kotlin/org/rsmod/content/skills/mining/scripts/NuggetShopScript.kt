package org.rsmod.content.skills.mining.scripts

import jakarta.inject.Inject
import org.rsmod.api.shops.operation.ShopOperationMap
import org.rsmod.content.skills.mining.configs.MiningCurrencies
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class NuggetShopScript
@Inject
constructor(
    private val operations: ShopOperationMap,
    private val nuggetOperations: NuggetShopOperations,
    private val mineralOperations: MineralShopOperations,
) : PluginScript() {
    override fun ScriptContext.startup() {
        operations.register(MiningCurrencies.golden_nugget, nuggetOperations)
        operations.register(MiningCurrencies.unidentified_minerals, mineralOperations)
    }
}
