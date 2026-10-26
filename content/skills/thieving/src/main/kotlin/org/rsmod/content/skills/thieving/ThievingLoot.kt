package org.rsmod.content.skills.thieving

import org.rsmod.api.random.GameRandom
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.type.obj.ObjType

/** A weighted pick: each entry wins with chance `weight / total weight`. */
internal class Loot(private val entries: List<Entry>) {
    class Entry(val obj: ObjType, val weight: Int, val count: IntRange = 1..1)

    private val total = entries.sumOf { it.weight }

    fun roll(random: GameRandom): Pair<ObjType, Int> {
        var roll = random.of(total)
        for (entry in entries) {
            if (roll < entry.weight) {
                val count = entry.count
                return entry.obj to if (count.first == count.last) count.first else random.of(count)
            }
            roll -= entry.weight
        }
        error("Roll exceeded total weight: $total")
    }
}

internal fun loot(vararg entries: Pair<ObjType, Int>) =
    Loot(entries.map { Loot.Entry(it.first, it.second) })

/**
 * The Master Farmer seed table (OSRS wiki): every seed has its own overall chance of "1 in N" per
 * success. The four herb seeds that get rarer at low Farming levels (guam, ranarr, snapdragon,
 * torstol) scale from their low-level rate at Farming 1 to the listed rate at Farming 85.
 */
internal object MasterFarmerSeeds {
    private class Seed(
        val obj: ObjType,
        val count: IntRange,
        val oneIn: Double,
        val oneInAtFarming1: Double = oneIn,
    )

    // UNVERIFIED: the wiki gives both rates but not how they scale between Farming 1 and 85.
    private val seeds =
        with(ThievingObjs) {
            listOf(
                Seed(potato_seed, 1..4, 5.65),
                Seed(onion_seed, 1..3, 7.53),
                Seed(cabbage_seed, 1..3, 14.4),
                Seed(tomato_seed, 1..2, 15.7),
                Seed(sweetcorn_seed, 1..2, 45.2),
                Seed(strawberry_seed, 1..1, 90.4),
                Seed(watermelon_seed, 1..1, 189.0),
                Seed(snape_grass_seed, 1..1, 260.0),
                Seed(barley_seed, 1..12, 18.0),
                Seed(hammerstone_seed, 1..9, 18.0),
                Seed(asgarnian_seed, 1..6, 23.9),
                Seed(jute_seed, 1..9, 24.1),
                Seed(yanillian_seed, 1..6, 36.1),
                Seed(krandorian_seed, 1..6, 72.2),
                Seed(wildblood_seed, 1..3, 142.0),
                Seed(marigold_seed, 1..1, 21.8),
                Seed(nasturtium_seed, 1..1, 32.9),
                Seed(rosemary_seed, 1..1, 50.9),
                Seed(woad_seed, 1..1, 68.9),
                Seed(limpwurt_seed, 1..1, 86.3),
                Seed(redberry_seed, 1..1, 25.8),
                Seed(cadavaberry_seed, 1..1, 36.8),
                Seed(dwellberry_seed, 1..1, 51.5),
                Seed(jangerberry_seed, 1..1, 129.0),
                Seed(whiteberry_seed, 1..1, 355.0),
                Seed(poison_ivy_seed, 1..1, 937.0),
                Seed(mushroom_spore, 1..1, 492.0),
                Seed(belladonna_seed, 1..1, 820.0),
                Seed(cactus_seed, 1..1, 1230.0),
                Seed(seaweed_spore, 1..1, 1892.0),
                Seed(potato_cactus_seed, 1..1, 2460.0),
                Seed(guam_seed, 1..1, 58.36, 67.2),
                Seed(marrentill_seed, 1..1, 95.6),
                Seed(tarromin_seed, 1..1, 140.0),
                Seed(harralander_seed, 1..1, 206.0),
                Seed(ranarr_seed, 1..1, 268.75, 555.83),
                Seed(toadflax_seed, 1..1, 443.0),
                Seed(irit_seed, 1..1, 651.0),
                Seed(avantoe_seed, 1..1, 947.0),
                Seed(kwuarm_seed, 1..1, 1389.0),
                Seed(snapdragon_seed, 1..1, 1854.4, 3835.23),
                Seed(cadantine_seed, 1..1, 2976.0),
                Seed(lantadyme_seed, 1..1, 4167.0),
                Seed(dwarf_weed_seed, 1..1, 6944.0),
                Seed(torstol_seed, 1..1, 9271.98, 19176.14),
            )
        }

    fun roll(random: GameRandom, farming: Int): Pair<ObjType, Int> {
        val progress = (farming.coerceIn(1, 85) - 1) / 84.0
        val entries =
            seeds.map { seed ->
                val high = 1.0 / seed.oneIn
                val low = 1.0 / seed.oneInAtFarming1
                Loot.Entry(
                    seed.obj,
                    ((low + (high - low) * progress) * WEIGHT_SCALE).toInt(),
                    seed.count,
                )
            }
        return Loot(entries).roll(random)
    }

    private const val WEIGHT_SCALE = 1_000_000
}

object ThievingStallObjs : ObjReferences() {
    val cup_of_tea = find("cup_of_tea")
    val jug = find("jug_empty")
    val jug_of_water = find("jug_water")
    val grapes = find("grapes")
    val jug_of_wine = find("jug_wine")
    val bottle_of_wine = find("rag_bottle_wine")
}
