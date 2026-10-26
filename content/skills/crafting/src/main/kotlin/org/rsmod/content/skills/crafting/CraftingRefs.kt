package org.rsmod.content.skills.crafting

import org.rsmod.api.type.builders.varp.VarpBuilder
import org.rsmod.api.type.refs.comp.ComponentReferences
import org.rsmod.api.type.refs.content.ContentReferences
import org.rsmod.api.type.refs.interf.InterfaceReferences
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.queue.QueueReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.api.type.refs.varp.VarpReferences
import org.rsmod.game.type.comp.ComponentType

object CraftingContent : ContentReferences() {
    /** Joined by Smithing's loc editor; jewellery and molten glass hook the same furnaces. */
    val furnace = find("smithing_furnace")
}

object CraftingLocs : LocReferences() {
    val spinning_wheel = find("spinningwheel")
    val spinning_wheels =
        listOf(
            spinning_wheel,
            find("viking_spinningwheel"),
            find("elf_village_spinning_wheel"),
            find("iznot_spinning_wheel"),
            find("kr_spinningwheel"),
            find("sw_spinningwheel_fixed"),
            find("spinningwheel_quetzacali"),
            find("spinningwheel_2"),
        )
    val sandpits =
        listOf(
            find("sandpit"),
            find("sandpit_2"),
            find("viking_sandpit"),
            find("dorgesh_sandpit"),
            find("prif_sandpit"),
        )
    /** Fires and ranges, as the Cooking module lists them: seaweed is burnt on any of them. */
    val heat_sources =
        listOf(
            find("fire"),
            find("fire_cook"),
            find("fireplacecookingpot"),
            find("qip_sheep_shearer_fireplacecookingpot"),
            find("range"),
            find("cooksquestrange"),
            find("fai_varrock_range"),
            find("fai_varrock_range_with_pipe"),
            find("rimmington_poor_range"),
            find("newbierange"),
            find("tut2_range"),
            find("elf_village_range"),
        )
    val potters_wheels =
        listOf(
            find("potterywheel"),
            find("viking_potterywheel"),
            find("potterywheel_2"),
            find("sw_potterywheel_fixed"),
        )
    val pottery_ovens =
        listOf(find("potteryoven"), find("fai_barbarian_pottery_oven"), find("viking_potteryoven"))
    val looms = listOf(find("loom"), find("fossil_loom_built"))
}

object CraftingNpcs : NpcReferences() {
    val ellis = find("ellis_tanner")
    val crafting_guild_tanner = find("tanner")
    val sbott = find("werewolftanner")
    val eodan = find("hosdun_eodan")
    val mary = find("ga_mary_2op")
    val chouani = find("auburn_tanner")
}

object CraftingObjs : ObjReferences() {
    val wool = find("wool")
    val ball_of_wool = find("ball_of_wool")
    val flax = find("flax")
    val bow_string = find("bow_string")
    val sinew = find("xbows_sinew")
    val oak_roots = find("oak_roots")
    val willow_roots = find("willow_roots")
    val maple_roots = find("maple_roots")
    val yew_roots = find("yew_roots")
    val crossbow_string = find("xbows_crossbow_string")
    val magic_roots = find("magic_roots")
    val magic_string = find("magic_string")
    val yak_hair = find("yak_hair")
    val rope = find("rope")

    val coins = find("coins")
    val needle = find("needle")
    val thread = find("thread")
    val costume_needle = find("costumeneedle")
    val chisel = find("chisel")
    val hammer = find("hammer")
    val gold_bar = find("gold_bar")
    val silver_bar = find("silver_bar")
    val tiara_mould = find("tiara_mould")
    val clay = find("clay")
    val soft_clay = find("softclay")
    val bucket_empty = find("bucket_empty")
    val bucket_sand = find("bucket_sand")
    val soda_ash = find("soda_ash")
    val molten_glass = find("molten_glass")
    val glassblowing_pipe = find("glassblowingpipe")
    val battlestaff = find("battlestaff")
}

object CraftingSeqs : SeqReferences() {
    val spin = find("human_spinningwheel")
    val needle = find("human_leather_crafting")
    val leather_shield = find("human_shield_crafting_leather")
    val green_shield = find("human_shield_crafting_green_dhide")
    val blue_shield = find("human_shield_crafting_blue_dhide")
    val red_shield = find("human_shield_crafting_red_dhide")
    val black_shield = find("human_shield_crafting_black_dhide")
    val snakeskin_shield = find("human_shield_crafting_snakeskin")
    val potters_wheel = find("human_potterywheel")
    val furnace = find("human_furnace")
    val glassblowing = find("human_glassblowing")
    val loom = find("farming_useloom")
    val battlestaff = find("human_battlestaff_crafting")
    val snail_shell = find("human_snailshellcutting")
    val limestone = find("human_limestonecutting")
    val spiked_vambraces = find("human_crafting_spikedvambraces")
    val fill_bucket = find("human_fillbucket_sandpit")
    val cooking = find("human_cooking")
}

object CraftingQueues : QueueReferences() {
    val spin = find("crafting_spin")
    val make = find("crafting_make")
}

object CraftingVarps : VarpReferences() {
    /** Custom: uses already taken from the thread in the inventory, 0-4. */
    val thread_uses = find("crafting_thread_uses")
    /** Custom: price list of the tanner whose interface is open (see [TannerPrices]). */
    val tanner = find("crafting_tanner")
    /** Quantity picked on the anvil and jewellery panels (cs2 `skillmain_init`). */
    val makexcrafting = find("makexcrafting")
}

internal object CraftingVarpBuilder : VarpBuilder() {
    init {
        build("crafting_thread_uses")
        build("crafting_tanner")
    }
}

object CraftingInterfaces : InterfaceReferences() {
    val tanner = find("tanner")
    val crafting_gold = find("crafting_gold")
    val silver_crafting = find("silver_crafting")
}

object CraftingComponents : ComponentReferences() {
    /** Per hide slot a-h: Tan All, Tan X, Tan 5, Tan 1 (separate components, each IF_BUTTON1). */
    val tanning =
        ('a'..'h').map { slot ->
            TanningSlot(
                model = find("tanner:tanning_${slot}_model"),
                text = find("tanner:tanning_${slot}_text"),
                price = find("tanner:tanning_${slot}_price"),
                all = find("tanner:tanning_${slot}_all"),
                x = find("tanner:tanning_${slot}_x"),
                five = find("tanner:tanning_${slot}_5"),
                one = find("tanner:tanning_${slot}_1"),
            )
        }

    /** The product buttons of the gold panel, by `<gem>_<ring|necklace|amulet|bracelet>`. */
    val gold: Map<String, ComponentType> =
        listOf("gold", "sapphire", "emerald", "ruby", "diamond", "dragon", "onyx", "zenyte")
            .flatMap { gem ->
                listOf("ring", "necklace", "amulet", "bracelet").map { "${gem}_$it" }
            }
            .plus("slayer_ring")
            .associateWith { find("crafting_gold:$it") }

    /** The product buttons of the silver panel. */
    val silver: Map<String, ComponentType> =
        listOf("opal", "jade", "topaz")
            .flatMap { gem ->
                listOf("ring", "necklace", "amulet", "bracelet").map { "${gem}_$it" }
            }
            .plus(listOf("holy_symbol", "unholy_symbol", "sickle", "crossbow_bolt", "tiara"))
            .associateWith { find("silver_crafting:$it") }

    /** The quantity buttons of a jewellery panel, in the order 1, 5, 10, X, All. */
    private fun quantity(panel: String) =
        listOf("make_1", "make_5", "make_10", "make_x", "make_all").map { find("$panel:$it") }

    val gold_quantity = quantity("crafting_gold")
    val silver_quantity = quantity("silver_crafting")
}
