package org.rsmod.content.skills.mining.scripts

import org.rsmod.api.random.GameRandom
import org.rsmod.content.skills.mining.configs.MiningObjs
import org.rsmod.game.type.obj.ObjType

object MiningProducts {
    /** One size of a sandstone/granite rock: its item, xp and own success rate. */
    data class Size(val obj: ObjType, val xp: Double, val low: Int, val high: Int)

    class WeightedTable(private val entries: List<Pair<ObjType, Int>>) {
        private val total = entries.sumOf { it.second }

        fun roll(random: GameRandom): ObjType {
            var pick = random.of(1, total)
            for ((obj, weight) in entries) {
                if (pick <= weight) {
                    return obj
                }
                pick -= weight
            }
            return entries.last().first
        }
    }

    /** Biggest first; the smallest size is what the rock's own success roll guarantees. */
    val sandstone =
        listOf(
            Size(MiningObjs.sandstone_10kg, 60.0, 4, 50),
            Size(MiningObjs.sandstone_5kg, 50.0, 8, 75),
            Size(MiningObjs.sandstone_2kg, 40.0, 16, 100),
            Size(MiningObjs.sandstone_1kg, 30.0, 25, 200),
        )

    val granite =
        listOf(
            Size(MiningObjs.granite_5kg, 75.0, 6, 64),
            Size(MiningObjs.granite_2kg, 60.0, 8, 75),
            Size(MiningObjs.granite_500g, 50.0, 16, 100),
        )

    val gemRockTable =
        WeightedTable(
            listOf(
                MiningObjs.uncut_opal to 60,
                MiningObjs.uncut_jade to 30,
                MiningObjs.uncut_red_topaz to 15,
                MiningObjs.uncut_sapphire to 9,
                MiningObjs.uncut_emerald to 5,
                MiningObjs.uncut_ruby to 5,
                MiningObjs.uncut_diamond to 4,
            )
        )
}
