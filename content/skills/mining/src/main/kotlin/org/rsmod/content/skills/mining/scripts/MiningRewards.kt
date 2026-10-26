package org.rsmod.content.skills.mining.scripts

import org.rsmod.api.config.refs.objs
import org.rsmod.content.skills.mining.configs.MiningObjs
import org.rsmod.game.inv.InvObj
import org.rsmod.game.inv.isType
import org.rsmod.game.type.obj.ObjType

object MiningRewards {
    val randomGemTable =
        MiningProducts.WeightedTable(
            listOf(
                MiningObjs.uncut_sapphire to 32,
                MiningObjs.uncut_emerald to 16,
                MiningObjs.uncut_ruby to 8,
                MiningObjs.uncut_diamond to 2,
            )
        )

    private val chargedGlories =
        listOf(
            MiningObjs.amulet_of_glory_1,
            MiningObjs.amulet_of_glory_2,
            MiningObjs.amulet_of_glory_3,
            objs.amulet_of_glory_4,
            MiningObjs.amulet_of_glory_5,
            MiningObjs.amulet_of_glory_6,
            MiningObjs.amulet_of_glory_inf,
        )

    fun isChargedGlory(amulet: InvObj?): Boolean = chargedGlories.any { amulet.isType(it) }

    /**
     * Product -> (lowest glove tier that helps, per-mille chance the rock is saved). Tier 1 =
     * mining gloves, 2 = superior, 3 = expert; a higher tier covers everything below it. Classic
     * pre-2024 OSRS values.
     */
    private val gloveSaves: List<Triple<ObjType, Int, Int>> =
        listOf(
            Triple(MiningObjs.silver_ore, 1, 500),
            Triple(MiningObjs.coal, 1, 400),
            Triple(MiningObjs.gold_ore, 1, 333),
            Triple(MiningObjs.mithril_ore, 2, 250),
            Triple(MiningObjs.adamantite_ore, 2, 167),
            Triple(MiningObjs.runite_ore, 2, 125),
            Triple(MiningObjs.amethyst, 3, 250),
        )

    fun gloveSavePermille(gloves: InvObj?, product: ObjType): Int {
        val tier =
            when {
                gloves.isType(MiningObjs.expert_mining_gloves) -> 3
                gloves.isType(MiningObjs.superior_mining_gloves) -> 2
                gloves.isType(MiningObjs.mining_gloves) -> 1
                else -> return 0
            }
        val (_, requiredTier, permille) =
            gloveSaves.firstOrNull { it.first.id == product.id } ?: return 0
        return if (tier >= requiredTier) permille else 0
    }
}
