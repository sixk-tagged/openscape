@file:Suppress("SpellCheckingInspection", "unused")

package org.rsmod.content.skills.slayer.configs

import org.rsmod.api.config.refs.params
import org.rsmod.api.shops.config.ShopParams
import org.rsmod.api.type.builders.varp.VarpBuilder
import org.rsmod.api.type.editors.npc.NpcEditor
import org.rsmod.api.type.refs.dbcol.DbColumnReferences
import org.rsmod.api.type.refs.dbtable.DbTableReferences
import org.rsmod.api.type.refs.inv.InvReferences
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.varbit.VarBitReferences
import org.rsmod.api.type.refs.varp.VarpReferences
import org.rsmod.game.dbtable.DbColumnCodec
import org.rsmod.game.stat.StatRequirement
import org.rsmod.game.type.TypeListMap
import org.rsmod.game.type.literal.CacheVarLiteral
import org.rsmod.game.type.npc.NpcType

typealias slayer_npcs = SlayerNpcs

typealias slayer_objs = SlayerObjs

typealias slayer_varps = SlayerVarps

typealias slayer_varbits = SlayerVarBits

object SlayerNpcs : NpcReferences() {
    val turael = find("slayer_master_1_tureal")
    val mazchna = find("slayer_master_2_mazchna")
    val vannaka = find("slayer_master_3")
    val nieve = find("slayer_master_nieve")
    val krystilia = find("slayer_master_7")
    val spria = find("slayer_master_9_active")
}

object SlayerObjs : ObjReferences() {
    val enchanted_gem = find("slayer_gem")
}

object SlayerInvs : InvReferences() {
    val equipment = find("slayershop")
}

object SlayerVarps : VarpReferences() {
    val count_original = find("slayer_count_original")
    val unlocks = find("slayer_rewards_unlocks")
    val unlocks1 = find("slayer_rewards_unlocks1")
    /** openscape: set by "Stop checking my combat level"; masters then ignore combat levels. */
    val any_combat = find("slayer_any_combat")
}

object SlayerVarBits : VarBitReferences() {
    val points = find("slayer_points")
    val streak = find("slayer_tasks_completed")
    val wilderness_streak = find("slayer_wilderness_tasks_completed")
}

internal object SlayerVarpBuilder : VarpBuilder() {
    init {
        build("slayer_any_combat") { permanent = true }
        build("slayer_superior_until") { temporary = true }
    }
}

internal object SlayerColumns : DbColumnReferences() {
    val task_id = int("slayer_task:id")
    val task_combat = int("slayer_task:min_comlevel")
    val task_stats = list("slayer_task:min_stat_requirement_all", LevelStatCodec)
    val task_stats_any = list("slayer_task:min_stat_requirement_any", LevelStatCodec)
    val task_extension = value("slayer_task:extension_min_max", ExtensionCodec)
    val task_name = string("slayer_task:name_lowercase")

    val master_id = int("slayer_master_task:master_id")
    val master_task = dbRow("slayer_master_task:task")
    val master_weight = int("slayer_master_task:weight")
    val master_min = int("slayer_master_task:min_amount")
    val master_max = int("slayer_master_task:max_amount")
    val master_unlock = dbRow("slayer_master_task:task_unlock")

    val unlock_bit = int("slayer_unlock:bit")
    val unlock_cost = int("slayer_unlock:cost")
    val unlock_refundable = boolean("slayer_unlock:refundable")
}

/**
 * `slayer_task`'s requirement pairs are stored level first, then stat ([DbColumnCodec.StatReqCodec]
 * expects the other order).
 */
private object LevelStatCodec : DbColumnCodec<Int, StatRequirement> {
    override val types: List<CacheVarLiteral> = listOf(CacheVarLiteral.INT, CacheVarLiteral.STAT)

    override fun decode(
        iterator: DbColumnCodec.Iterator<Int, StatRequirement>,
        types: TypeListMap,
    ): StatRequirement {
        val level = iterator.next()
        val stat = types.stats.getValue(iterator.next()).toHashedType()
        return StatRequirement(stat, level)
    }

    override fun encode(value: StatRequirement): List<Int> = listOf(value.level, value.stat.id)
}

/** `slayer_task:extension_min_max`: the unlock's dbrow id, then the longer task's kill range. */
private object ExtensionCodec : DbColumnCodec<Int, Triple<Int, Int, Int>> {
    override val types: List<CacheVarLiteral> =
        listOf(CacheVarLiteral.DBROW, CacheVarLiteral.INT, CacheVarLiteral.INT)

    override fun decode(
        iterator: DbColumnCodec.Iterator<Int, Triple<Int, Int, Int>>,
        types: TypeListMap,
    ): Triple<Int, Int, Int> = Triple(iterator.next(), iterator.next(), iterator.next())

    override fun encode(value: Triple<Int, Int, Int>): List<Int> =
        listOf(value.first, value.second, value.third)
}

internal object SlayerTables : DbTableReferences() {
    val task = find("slayer_task")
    val master_task = find("slayer_master_task")
    val unlock = find("slayer_unlock")
}

/** The npcs of the generated [SLAYER_LEVELS] table. */
internal object SlayerLevelNpcs : NpcReferences() {
    val levels: List<Pair<NpcType, Int>> =
        SLAYER_LEVELS.map { (name, level) -> find(name) to level }
}

internal object SlayerNpcEditor : NpcEditor() {
    init {
        for ((npc, level) in SlayerLevelNpcs.levels) {
            edit(npc) { param[params.slayer_levelrequire] = level }
        }
        // The Slayer Equipment shop's StoreTableHead (Spria's is set in Draynor).
        for (master in
            listOf(SlayerNpcs.turael, SlayerNpcs.mazchna, SlayerNpcs.vannaka, SlayerNpcs.nieve)) {
            shop(master)
        }
        shop(SlayerNpcs.krystilia)
    }

    private fun shop(npc: NpcType) {
        edit(npc) {
            param[ShopParams.shop_sell_percentage] = 1000
            param[ShopParams.shop_buy_percentage] = 600
            param[ShopParams.shop_change_percentage] = 10
        }
    }
}
