package org.rsmod.content.skills.cooking

import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.queue.QueueReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.game.type.obj.ObjType

/** Levels, xp and burn rates (low/high for statRandom) from the OSRS wiki, Oct 2025. */
object CookingRecipes {
    data class Rate(val low: Int, val high: Int)

    data class Recipe(
        val raw: ObjType,
        val cooked: ObjType,
        val burnt: ObjType,
        val level: Int,
        val xp: Double,
        val fire: Rate,
        val range: Rate = fire,
        val gauntlets: Rate? = null,
        val rangeOnly: Boolean = false,
        val cookedMessage: String? = null,
        val burntMessage: String? = null,
    )

    private val basic = Rate(128, 512)

    val all =
        with(CookingObjs) {
            listOf(
                Recipe(raw_shrimp, shrimp, burnt_shrimp, 1, 30.0, basic),
                Recipe(raw_anchovies, anchovies, burnt_anchovies, 1, 30.0, basic),
                Recipe(raw_sardine, sardine, burnt_sardine, 1, 40.0, Rate(118, 492)),
                Recipe(raw_herring, herring, burnt_herring, 5, 50.0, Rate(108, 472)),
                Recipe(raw_trout, trout, burnt_river_fish, 15, 70.0, Rate(88, 432)),
                Recipe(raw_pike, pike, burnt_river_fish, 20, 80.0, Rate(78, 412)),
                Recipe(raw_salmon, salmon, burnt_river_fish, 25, 90.0, Rate(68, 392)),
                Recipe(raw_tuna, tuna, burnt_tuna, 30, 100.0, Rate(58, 372)),
                Recipe(
                    raw_lobster,
                    lobster,
                    burnt_lobster,
                    40,
                    120.0,
                    Rate(38, 332),
                    gauntlets = Rate(55, 368),
                ),
                Recipe(
                    raw_swordfish,
                    swordfish,
                    burnt_swordfish,
                    45,
                    140.0,
                    fire = Rate(18, 292),
                    range = Rate(30, 310),
                    gauntlets = Rate(30, 310),
                ),
                meat(raw_beef),
                meat(raw_rat_meat),
                Recipe(
                    raw_chicken,
                    cooked_chicken,
                    burnt_chicken,
                    1,
                    30.0,
                    basic,
                    cookedMessage = "You cook the chicken.",
                    burntMessage = "You accidentally burn the chicken.",
                ),
                Recipe(
                    bread_dough,
                    bread,
                    burnt_bread,
                    1,
                    40.0,
                    Rate(118, 492),
                    rangeOnly = true,
                    cookedMessage = "You remove the bread from the oven.",
                    burntMessage = "You accidentally burn the bread.",
                ),
            )
        }

    val byRaw: Map<Int, Recipe> by lazy { all.associateBy { it.raw.id } }

    private fun meat(raw: ObjType) =
        Recipe(
            raw,
            CookingObjs.cooked_meat,
            CookingObjs.burnt_meat,
            1,
            30.0,
            basic,
            cookedMessage = "You cook a piece of meat.",
            burntMessage = "You accidentally burn the meat.",
        )
}

object CookingLocs : LocReferences() {
    val fire = find("fire")
    val fire_cook = find("fire_cook")
    val fireplace_pot = find("fireplacecookingpot")
    val fred_fireplace_pot = find("qip_sheep_shearer_fireplacecookingpot")

    val range = find("range")
    val lumbridge_castle_range = find("cooksquestrange")
    val varrock_range = find("fai_varrock_range")
    val varrock_range_pipe = find("fai_varrock_range_with_pipe")
    val rimmington_range = find("rimmington_poor_range")
    val newbie_range = find("newbierange")
    val tutorial_range = find("tut2_range")
    val elf_village_range = find("elf_village_range")
    val sorceress_range = find("sorceress_range")

    val dairy_churn = find("dairy_churn")

    val fires
        get() = listOf(fire, fire_cook, fireplace_pot, fred_fireplace_pot)

    val ranges
        get() =
            listOf(
                range,
                lumbridge_castle_range,
                varrock_range,
                varrock_range_pipe,
                rimmington_range,
                newbie_range,
                tutorial_range,
                elf_village_range,
                sorceress_range,
            )
}

object CookingObjs : ObjReferences() {
    val raw_shrimp = find("raw_shrimp")
    val shrimp = find("shrimp")
    val burnt_shrimp = find("burnt_shrimp")
    val raw_anchovies = find("raw_anchovies")
    val anchovies = find("anchovies")
    val burnt_anchovies = find("burntfish1")
    val raw_sardine = find("raw_sardine")
    val sardine = find("sardine")
    val burnt_sardine = find("burntfish5")
    val raw_herring = find("raw_herring")
    val herring = find("herring")
    val burnt_herring = find("burntfish3")
    val raw_trout = find("raw_trout")
    val trout = find("trout")
    /** Trout, pike and salmon all burn into the same obj. */
    val burnt_river_fish = find("burntfish2")
    val raw_pike = find("raw_pike")
    val pike = find("pike")
    val raw_salmon = find("raw_salmon")
    val salmon = find("salmon")
    val raw_tuna = find("raw_tuna")
    val tuna = find("tuna")
    val burnt_tuna = find("burntfish4")
    val raw_lobster = find("raw_lobster")
    val lobster = find("lobster")
    val burnt_lobster = find("burnt_lobster")
    val raw_swordfish = find("raw_swordfish")
    val swordfish = find("swordfish")
    val burnt_swordfish = find("burnt_swordfish")
    val raw_beef = find("raw_beef")
    val raw_rat_meat = find("raw_rat_meat")
    val cooked_meat = find("cooked_meat")
    val burnt_meat = find("burnt_meat")
    val raw_chicken = find("raw_chicken")
    val cooked_chicken = find("cooked_chicken")
    val burnt_chicken = find("burnt_chicken")
    val bread_dough = find("bread_dough")
    val bread = find("bread")
    val burnt_bread = find("burnt_bread")
    val cooking_gauntlets = find("gauntlets_of_cooking")

    val bucket_of_milk = find("bucket_milk")
    val pot_of_cream = find("pot_of_cream")
    val pat_of_butter = find("pot_of_butter")
    val cheese = find("cheese")
    val bucket = find("bucket_empty")
    val pot = find("pot_empty")
}

object CookingSeqs : SeqReferences() {
    val fire = find("human_firecooking")
    val range = find("human_cooking")
    val churn_short = find("player_churns_milk_short")
    val churn_medium = find("player_churns_milk_medium")
    val churn_long = find("player_churns_milk_long")
}

object CookingQueues : QueueReferences() {
    val cook = find("cooking_cook")
    val churn = find("cooking_churn")
}
