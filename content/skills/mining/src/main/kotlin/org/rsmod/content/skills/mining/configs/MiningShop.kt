package org.rsmod.content.skills.mining.configs

import org.rsmod.api.type.editors.inv.InvEditor
import org.rsmod.api.type.refs.currency.CurrencyReferences
import org.rsmod.api.type.refs.inv.InvReferences
import org.rsmod.game.type.inv.InvScope
import org.rsmod.game.type.inv.InvStackType

object MiningCurrencies : CurrencyReferences() {
    val golden_nugget = find("golden_nugget")
    val unidentified_minerals = find("mguild_minerals")
}

object MiningInvs : InvReferences() {
    /** Unused vanilla inv, repurposed as Prospector Percy's nugget shop. */
    val nugget_shop = find("miningstore")
    val mineral_shop = find("mguild_rewardshop")
}

internal object MiningInvEditor : InvEditor() {
    init {
        edit(MiningInvs.nugget_shop) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            // Counts are display only: the nugget shop never sells out.
            stock += stock(MiningObjs.prospector_helmet, count = 100, restockCycles = 100)
            stock += stock(MiningObjs.prospector_jacket, count = 100, restockCycles = 100)
            stock += stock(MiningObjs.prospector_legs, count = 100, restockCycles = 100)
            stock += stock(MiningObjs.prospector_boots, count = 100, restockCycles = 100)
            stock += stock(MiningObjs.mining_gloves, count = 100, restockCycles = 100)
            stock += stock(MiningObjs.superior_mining_gloves, count = 100, restockCycles = 100)
            stock += stock(MiningObjs.expert_mining_gloves, count = 100, restockCycles = 100)
        }

        // Belona's Mineral Exchange (the Mining Guild): counts are display only.
        edit(MiningInvs.mineral_shop) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            stock += stock(MiningObjs.mining_gloves, count = 100, restockCycles = 1)
            stock += stock(MiningObjs.superior_mining_gloves, count = 100, restockCycles = 1)
            stock += stock(MiningObjs.soft_clay_pack, count = 1000, restockCycles = 1)
            stock += stock(MiningObjs.bag_full_of_gems, count = 100, restockCycles = 1)
        }
    }
}
