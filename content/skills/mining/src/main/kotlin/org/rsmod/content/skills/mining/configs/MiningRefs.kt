package org.rsmod.content.skills.mining.configs

import org.rsmod.api.config.aliases.ParamBool
import org.rsmod.api.config.aliases.ParamInt
import org.rsmod.api.type.builders.param.ParamBuilder
import org.rsmod.api.type.refs.content.ContentReferences
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.param.ParamReferences
import org.rsmod.api.type.refs.seq.SeqReferences

object MiningContent : ContentReferences() {
    val rock = find("mining_rock")
    val pickaxe = find("mining_pickaxe")
    val depleted_rock = find("mining_rock_depleted")
}

object MiningParams : ParamReferences() {
    /** Success rate packed as `(low shl 16) or high`, the pair `statRandom` takes. */
    val success_rate: ParamInt = find("mining_success_rate")
    /** How the product is chosen; one of [ProductRoll]. */
    val product_roll: ParamInt = find("mining_product_roll")
    /** Whether a success also rolls for random gems and golden nuggets. */
    val bonus_rolls: ParamBool = find("mining_bonus_rolls")
    /** Ticks between success rolls for a pickaxe. */
    val pickaxe_ticks: ParamInt = find("mining_pickaxe_ticks")
}

internal object MiningParamBuilder : ParamBuilder() {
    init {
        build<Int>("mining_success_rate")
        build<Int>("mining_product_roll") { default = ProductRoll.FIXED }
        build<Boolean>("mining_bonus_rolls") { default = false }
        build<Int>("mining_pickaxe_ticks")
    }
}

object ProductRoll {
    const val FIXED = 0
    const val ESSENCE = 1
    const val SANDSTONE = 2
    const val GRANITE = 3
    const val GEM_ROCK = 4
}

object MiningSeqs : SeqReferences() {
    val bronze = find("human_mining_bronze_pickaxe")
    val iron = find("human_mining_iron_pickaxe")
    val steel = find("human_mining_steel_pickaxe")
    val black = find("human_mining_black_pickaxe")
    val mithril = find("human_mining_mithril_pickaxe")
    val adamant = find("human_mining_adamant_pickaxe")
    val rune = find("human_mining_rune_pickaxe")
    val gilded = find("human_mining_gilded_pickaxe")
    val dragon = find("human_mining_dragon_pickaxe")
    val third_age = find("human_mining_3a_pickaxe")
    val infernal = find("human_mining_infernal_pickaxe")
    val crystal = find("human_mining_crystal_pickaxe")
}

/** Objs not already in `BaseObjs` (which has bronze/dragon/infernal/3a/crystal pickaxes). */
object MiningObjs : ObjReferences() {
    val iron_pickaxe = find("iron_pickaxe")
    val steel_pickaxe = find("steel_pickaxe")
    val black_pickaxe = find("black_pickaxe")
    val mithril_pickaxe = find("mithril_pickaxe")
    val adamant_pickaxe = find("adamant_pickaxe")
    val rune_pickaxe = find("rune_pickaxe")
    val gilded_pickaxe = find("trail_gilded_pickaxe")

    val clay = find("clay")
    val copper_ore = find("copper_ore")
    val tin_ore = find("tin_ore")
    val rune_essence = find("blankrune")
    val pure_essence = find("blankrune_high")
    val blurite_ore = find("blurite_ore")
    val limestone = find("limestone")
    val barronite_shard = find("camdozaal_barronite_shard")
    val iron_ore = find("iron_ore")
    val silver_ore = find("silver_ore")
    val volcanic_ash = find("fossil_volcanic_ash")
    val coal = find("coal")
    val sandstone_1kg = find("enakh_sandstone_tiny")
    val sandstone_2kg = find("enakh_sandstone_small")
    val sandstone_5kg = find("enakh_sandstone_medium")
    val sandstone_10kg = find("enakh_sandstone_large")
    val gold_ore = find("gold_ore")
    val uncut_opal = find("uncut_opal")
    val uncut_jade = find("uncut_jade")
    val uncut_red_topaz = find("uncut_red_topaz")
    val uncut_sapphire = find("uncut_sapphire")
    val uncut_emerald = find("uncut_emerald")
    val uncut_ruby = find("uncut_ruby")
    val uncut_diamond = find("uncut_diamond")
    val blessed_bone_shard = find("blessed_bone_shard")
    val volcanic_sulphur = find("lovakengj_sulphur")
    val granite_500g = find("enakh_granite_tiny")
    val granite_2kg = find("enakh_granite_small")
    val granite_5kg = find("enakh_granite_medium")
    val mithril_ore = find("mithril_ore")
    val lovakite_ore = find("lovakite_ore")
    val adamantite_ore = find("adamantite_ore")
    val soft_clay = find("softclay")
    val basalt = find("basalt")
    val te_salt = find("red_salt")
    val efh_salt = find("blue_salt")
    val urt_salt = find("green_salt")
    val ancient_essence = find("ancient_essence")
    val infernal_shale = find("infernal_shale")
    val runite_ore = find("runite_ore")
    val amethyst = find("amethyst")

    val golden_nugget = find("motherlode_nugget")
    val prospector_helmet = find("motherlode_reward_hat")
    val prospector_jacket = find("motherlode_reward_top")
    val prospector_legs = find("motherlode_reward_legs")
    val prospector_boots = find("motherlode_reward_boots")
    val mining_gloves = find("mguild_gloves")
    val superior_mining_gloves = find("mguild_gloves_superior")
    val expert_mining_gloves = find("mguild_gloves_expert")
    val unidentified_minerals = find("mguild_minerals")
    val soft_clay_pack = find("pack_softclay_guild")
    val bag_full_of_gems = find("reward_gem_bag_guild")
    // `amulet_of_glory_4` is already in BaseObjs.
    val amulet_of_glory_1 = find("amulet_of_glory_1")
    val amulet_of_glory_2 = find("amulet_of_glory_2")
    val amulet_of_glory_3 = find("amulet_of_glory_3")
    val amulet_of_glory_5 = find("amulet_of_glory_5")
    val amulet_of_glory_6 = find("amulet_of_glory_6")
    val amulet_of_glory_inf = find("amulet_of_glory_inf")
}

object MiningLocs : LocReferences() {
    val empty_1 = find("rocks1")
    val empty_2 = find("rocks2")
    val clay_1 = find("clayrock1")
    val clay_2 = find("clayrock2")
    val copper_1 = find("copperrock1")
    val copper_2 = find("copperrock2")
    val tin_1 = find("tinrock1")
    val tin_2 = find("tinrock2")
    val essence = find("big_essence_rock")
    /** The rocks actually placed in the Rune Essence Mine. */
    val essence_mine = find("blankrunestone")
    val blurite_1 = find("blurite_rock_1")
    val blurite_2 = find("blurite_rock_2")
    val limestone_1 = find("limestone_rock1")
    val limestone_2 = find("limestone_rock2")
    val limestone_empty = find("limestone_rock_noore")
    val barronite_1 = find("camdozaalrock1")
    val barronite_2 = find("camdozaalrock2")
    val barronite_empty_1 = find("camdozaalrock1_empty")
    val barronite_empty_2 = find("camdozaalrock2_empty")
    val iron_1 = find("ironrock1")
    val iron_2 = find("ironrock2")
    val silver_1 = find("silverrock1")
    val silver_2 = find("silverrock2")
    val ash = find("fossil_ashpile")
    val ash_empty = find("fossil_ashpile_empty")
    val coal_1 = find("coalrock1")
    val coal_2 = find("coalrock2")
    val sandstone = find("enakh_sandstone_rocks")
    val gold_1 = find("goldrock1")
    val gold_2 = find("goldrock2")
    val gem_1 = find("gemrock1")
    val gem_2 = find("gemrock")
    val calcified_1 = find("varlamore_mining_rock")
    val calcified_2 = find("varlamore_mining_rock02")
    val calcified_empty_1 = find("varlamore_mining_rock_empty")
    val calcified_empty_2 = find("varlamore_mining_rock_empty02")
    val sulphur_1 = find("sulphur_rock_01")
    val sulphur_2 = find("sulphur_rock_02")
    val granite = find("enakh_granite_rocks")
    val mithril_1 = find("mithrilrock1")
    val mithril_2 = find("mithrilrock2")
    val lovakite_1 = find("lovakite_rock1")
    val lovakite_2 = find("lovakite_rock2")
    val adamantite_1 = find("adamantiterock1")
    val adamantite_2 = find("adamantiterock2")
    val soft_clay_1 = find("softclayrock1")
    val soft_clay_2 = find("softclayrock2")
    val basalt = find("my2arm_saltrock_special")
    val te_salt = find("my2arm_saltrock_red")
    val efh_salt = find("my2arm_saltrock_blue")
    val urt_salt = find("my2arm_saltrock_green")
    val salt_empty = find("my2arm_saltrock_empty")
    val ancient_essence = find("ancient_essence_rock_active")
    val infernal_shale_1 = find("crimson_lovakite1")
    val infernal_shale_2 = find("crimson_lovakite2")
    val runite_1 = find("runiterock1")
    val runite_2 = find("runiterock2")
    val amethyst_1 = find("amethystrock1")
    val amethyst_2 = find("amethystrock2")
    val amethyst_empty = find("amethystrock_empty")
}
