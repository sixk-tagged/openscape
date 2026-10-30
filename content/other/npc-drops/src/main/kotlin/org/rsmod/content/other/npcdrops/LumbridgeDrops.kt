package org.rsmod.content.other.npcdrops

import jakarta.inject.Inject
import org.rsmod.api.config.refs.objs
import org.rsmod.api.death.Drop
import org.rsmod.api.death.DropTable
import org.rsmod.api.death.NpcDropTables
import org.rsmod.api.death.NpcDrops
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.type.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Drop tables (OSRS wiki, Oct 2025) for every attackable monster spawned around Lumbridge. Main
 * tables are out of 128. Tertiaries (clues, champion scrolls, ensouled heads) are left out.
 */
class LumbridgeDrops @Inject constructor(private val tables: NpcDropTables) : PluginScript() {
    override fun ScriptContext.startup() {
        with(drop_npcs) {
            listOf(cow, cow2, cow3, cow2_calf, cow3_calf).forEach { tables[it] = cowDrops }
            listOf(chicken, chicken_brown).forEach { tables[it] = chickenDrops }
            listOf(
                    giantrat,
                    giantrat_grey,
                    giantrat_grey2,
                    giantrat_grey3,
                    giantrat1_2,
                    giantrat1_3,
                )
                .forEach { tables[it] = giantRatDrops }
            listOf(little_frog, medium_frog, ram, ram2, ram3).forEach { tables[it] = bonesOnly }
            tables[giant_frog] = NpcDrops(always = listOf(always(drop_objs.big_bones)))
            listOf(rat, spider, giantspider, duck, duck_female, duckling).forEach {
                tables[it] = NpcDrops()
            }
            listOf(man, man2, man3, man_indoor, woman, woman2, woman3).forEach {
                tables[it] = manDrops
            }
            listOf(farmer1, farmer3, farmer2_f, farmer3_f, farmer4).forEach {
                tables[it] = farmerDrops
            }
            listOf(goblin_1, goblin_2, goblin_3, goblin_4, goblin_5, goblin_6, goblin_7, goblin_8)
                .forEach { tables[it] = goblinLevel2Drops }
            tables[goblin_armed] = goblinLevel5Drops
            tables[imp] = impDrops
            tables[mugger] = muggerDrops
            tables[unicorn] = unicornDrops
        }
    }
}

internal fun always(obj: ObjType, count: Int = 1) = Drop(obj, count..count)

internal fun drop(obj: ObjType, weight: Int, count: Int = 1) = Drop(obj, count..count, weight)

internal fun drop(obj: ObjType, weight: Int, count: IntRange) = Drop(obj, count, weight)

/** A members-only slot; on a free world it gives [f2p] (or nothing). */
internal fun members(obj: ObjType, weight: Int, count: IntRange = 1..1, f2p: Drop? = null) =
    Drop(obj, count, weight, members = true, f2p = f2p)

internal fun table(vararg drops: Drop) = DropTable(128, drops.toList())

internal val bones = always(objs.bones)
internal val bonesOnly = NpcDrops(always = listOf(bones))

internal val herbs =
    with(drop_objs) {
        table(
            drop(grimy_guam, 32),
            drop(grimy_marrentill, 24),
            drop(grimy_tarromin, 18),
            drop(grimy_harralander, 14),
            drop(grimy_ranarr, 11),
            drop(grimy_irit, 8),
            drop(grimy_avantoe, 6),
            drop(grimy_kwuarm, 5),
            drop(grimy_cadantine, 4),
            drop(grimy_lantadyme, 3),
            drop(grimy_dwarf_weed, 3),
        )
    }

/** The herb table is members-only; free worlds get 10 coins in its slot. */
internal fun herbs(weight: Int) =
    Drop(table = herbs, weight = weight, members = true, f2p = always(objs.coins, 10))

internal val allotmentSeeds =
    with(drop_objs) {
        table(
            drop(potato_seed, 64, 1..4),
            drop(onion_seed, 32, 1..3),
            drop(cabbage_seed, 16, 1..3),
            drop(tomato_seed, 8, 1..2),
            drop(sweetcorn_seed, 4, 1..2),
            drop(strawberry_seed, 2),
            drop(watermelon_seed, 1),
            drop(snape_grass_seed, 1),
        )
    }

internal val cowDrops =
    NpcDrops(always = listOf(always(drop_objs.cowhide), always(drop_objs.raw_beef), bones))

internal val chickenDrops =
    NpcDrops(
        always = listOf(bones, always(drop_objs.raw_chicken)),
        main = table(drop(drop_objs.feather, 64, 5), drop(drop_objs.feather, 32, 15)),
    )

private val giantRatDrops = NpcDrops(always = listOf(bones, always(drop_objs.raw_rat_meat)))

internal val manTable =
    with(drop_objs) {
        table(
            drop(bronze_med_helm, 2),
            drop(iron_dagger, 1),
            members(bronze_bolts, 22, 2..12),
            drop(bronze_arrow, 3, 7),
            drop(earth_rune, 2, 4),
            drop(fire_rune, 2, 6),
            drop(mind_rune, 2, 9),
            drop(chaos_rune, 1, 2),
            herbs(23),
            drop(objs.coins, 38, 3),
            drop(objs.coins, 9, 5),
            drop(objs.coins, 4, 15),
            drop(objs.coins, 1, 25),
            drop(fishing_bait, 5),
            drop(copper_ore, 2),
            drop(earth_talisman, 2),
            drop(cabbage, 1),
        )
    }

internal val manDrops = NpcDrops(always = listOf(bones), main = manTable)

/** Members farmers have their own table; free-world farmers drop the man's table. */
internal val farmerDrops =
    with(drop_objs) {
        NpcDrops(
            always = listOf(bones),
            main =
                table(
                    drop(earth_rune, 2, 4),
                    drop(fire_rune, 2, 6),
                    drop(mind_rune, 2, 9),
                    drop(chaos_rune, 1, 2),
                    herbs(11),
                    Drop(table = allotmentSeeds, weight = 27),
                    drop(objs.coins, 38, 3),
                    drop(objs.coins, 1, 25),
                    drop(fishing_bait, 5),
                    drop(rake, 3),
                    drop(earth_talisman, 2),
                    drop(gardening_boots, 2),
                    drop(seed_dibber, 2),
                    drop(secateurs, 1),
                    drop(watering_can_8, 1),
                ),
            f2pMain = manTable,
        )
    }

internal val goblinLevel2Drops =
    with(drop_objs) {
        NpcDrops(
            always = listOf(bones),
            main =
                table(
                    drop(bronze_sq_shield, 3),
                    members(bronze_spear, 4),
                    drop(body_rune, 5, 7),
                    drop(water_rune, 6, 6),
                    drop(earth_rune, 3, 4),
                    drop(bronze_bolts, 3, 8),
                    drop(objs.coins, 28, 5),
                    drop(objs.coins, 3, 9),
                    drop(objs.coins, 3, 15),
                    drop(objs.coins, 2, 20),
                    drop(objs.coins, 1, 1),
                    drop(hammer, 15),
                    members(goblin_book, 2),
                    drop(goblin_mail, 5),
                    drop(chefs_hat, 3),
                    drop(beer, 2),
                    drop(brass_necklace, 1),
                    drop(air_talisman, 1),
                ),
        )
    }

internal val goblinLevel5Drops =
    with(drop_objs) {
        NpcDrops(
            always = listOf(bones),
            main =
                table(
                    drop(bronze_axe, 3),
                    drop(bronze_scimitar, 1),
                    members(bronze_spear, 9),
                    drop(bronze_arrow, 3, 7),
                    drop(mind_rune, 3, 2),
                    drop(earth_rune, 3, 4),
                    drop(body_rune, 3, 2),
                    members(bronze_javelin, 2, 5..5),
                    drop(chaos_rune, 1),
                    drop(nature_rune, 1),
                    herbs(2),
                    drop(objs.coins, 34, 1),
                    drop(objs.coins, 13, 3),
                    drop(objs.coins, 8, 5),
                    drop(objs.coins, 7, 16),
                    drop(objs.coins, 3, 24),
                    drop(hammer, 9),
                    members(goblin_book, 2),
                    drop(goblin_mail, 10),
                    drop(grapes, 1),
                    drop(red_cape, 1),
                    drop(tin_ore, 1),
                ),
        )
    }

private val impDrops =
    with(drop_objs) {
        NpcDrops(
            always = listOf(always(fiendish_ashes)),
            main =
                table(
                    drop(black_bead, 5),
                    drop(red_bead, 5),
                    drop(white_bead, 5),
                    drop(yellow_bead, 5),
                    drop(bronze_bolts, 8),
                    drop(blue_wizard_hat, 8),
                    drop(egg, 5),
                    drop(raw_chicken, 5),
                    drop(burnt_bread, 4),
                    drop(burnt_meat, 4),
                    drop(bread, 1),
                    drop(hammer, 8),
                    drop(tinderbox, 5),
                    drop(shears, 4),
                    drop(bucket, 4),
                    drop(bucket_of_water, 2),
                    drop(jug, 2),
                    drop(jug_of_water, 2),
                    drop(pot, 2),
                    drop(pot_of_flour, 2),
                    drop(cabbage, 2),
                    drop(cooked_meat, 1),
                    drop(ball_of_wool, 8),
                    drop(mind_talisman, 7),
                    drop(ashes, 6),
                    drop(clay, 4),
                    drop(cadava_berries, 4),
                    drop(grain, 3),
                    drop(chefs_hat, 2),
                    drop(flyer, 2),
                    drop(potion, 1),
                    drop(bread_dough, 2),
                ),
        )
    }

internal val muggerDrops =
    with(drop_objs) {
        NpcDrops(
            always = listOf(bones),
            main =
                table(
                    members(bronze_bolts, 27, 2..12),
                    drop(mind_rune, 3, 9),
                    drop(water_rune, 2, 6),
                    drop(earth_rune, 2, 5),
                    herbs(13),
                    drop(objs.coins, 12, 5),
                    drop(objs.coins, 3, 15),
                    drop(objs.coins, 1, 25),
                    drop(rope, 40),
                    drop(fishing_bait, 6),
                    drop(copper_ore, 2),
                    drop(bronze_med_helm, 2),
                    drop(knife, 1),
                    drop(cabbage, 1),
                ),
        )
    }

private val unicornDrops =
    NpcDrops(always = listOf(bones, members(drop_objs.unicorn_horn, weight = 0)))

object drop_objs : ObjReferences() {
    val big_bones = find("big_bones")
    val cowhide = find("cow_hide")
    val raw_beef = find("raw_beef")
    val raw_chicken = find("raw_chicken")
    val raw_rat_meat = find("raw_rat_meat")
    val feather = find("feather")

    val grimy_guam = find("unidentified_guam")
    val grimy_marrentill = find("unidentified_marentill")
    val grimy_tarromin = find("unidentified_tarromin")
    val grimy_harralander = find("unidentified_harralander")
    val grimy_ranarr = find("unidentified_ranarr")
    val grimy_irit = find("unidentified_irit")
    val grimy_avantoe = find("unidentified_avantoe")
    val grimy_kwuarm = find("unidentified_kwuarm")
    val grimy_cadantine = find("unidentified_cadantine")
    val grimy_lantadyme = find("unidentified_lantadyme")
    val grimy_dwarf_weed = find("unidentified_dwarf_weed")

    val potato_seed = find("potato_seed")
    val onion_seed = find("onion_seed")
    val cabbage_seed = find("cabbage_seed")
    val tomato_seed = find("tomato_seed")
    val sweetcorn_seed = find("sweetcorn_seed")
    val strawberry_seed = find("strawberry_seed")
    val watermelon_seed = find("watermelon_seed")
    val snape_grass_seed = find("snape_grass_seed")

    val bronze_med_helm = find("bronze_med_helm")
    val iron_dagger = find("iron_dagger")
    val bronze_bolts = find("bolt")
    val bronze_arrow = find("bronze_arrow")
    val earth_rune = find("earthrune")
    val fire_rune = find("firerune")
    val mind_rune = find("mindrune")
    val chaos_rune = find("chaosrune")
    val body_rune = find("bodyrune")
    val water_rune = find("waterrune")
    val nature_rune = find("naturerune")
    val fishing_bait = find("fishing_bait")
    val copper_ore = find("copper_ore")
    val tin_ore = find("tin_ore")
    val earth_talisman = find("earth_talisman")
    val air_talisman = find("air_talisman")
    val mind_talisman = find("mind_talisman")
    val cabbage = find("cabbage")

    val rake = find("rake")
    val gardening_boots = find("gardening_boots")
    val seed_dibber = find("dibber")
    val secateurs = find("secateurs")
    val watering_can_8 = find("watering_can_8")

    val bronze_sq_shield = find("bronze_sq_shield")
    val bronze_spear = find("bronze_spear")
    val bronze_axe = find("bronze_axe")
    val bronze_scimitar = find("bronze_scimitar")
    val bronze_javelin = find("bronze_javelin")
    val hammer = find("hammer")
    val goblin_book = find("slice_goblin_history_book")
    val goblin_mail = find("goblin_armour")
    val chefs_hat = find("chefs_hat")
    val beer = find("beer")
    val brass_necklace = find("brass_necklace")
    val grapes = find("grapes")
    val red_cape = find("red_cape")

    val fiendish_ashes = find("fiendish_ashes")
    val black_bead = find("black_bead")
    val red_bead = find("red_bead")
    val white_bead = find("white_bead")
    val yellow_bead = find("yellow_bead")
    val blue_wizard_hat = find("bluewizhat")
    val egg = find("egg")
    val burnt_bread = find("burnt_bread")
    val burnt_meat = find("burnt_meat")
    val bread = find("bread")
    val tinderbox = find("tinderbox")
    val shears = find("shears")
    val bucket = find("bucket_empty")
    val bucket_of_water = find("bucket_water")
    val jug = find("jug_empty")
    val jug_of_water = find("jug_water")
    val pot = find("pot_empty")
    val pot_of_flour = find("pot_flour")
    val cooked_meat = find("cooked_meat")
    val ball_of_wool = find("ball_of_wool")
    val ashes = find("ashes")
    val clay = find("clay")
    val cadava_berries = find("cadavaberries")
    val grain = find("grain")
    val flyer = find("flier")
    val potion = find("acne_potion")
    val bread_dough = find("bread_dough")

    val rope = find("rope")
    val knife = find("knife")
    val unicorn_horn = find("unicorn_horn")
}

object drop_npcs : NpcReferences() {
    val cow = find("cow")
    val cow2 = find("cow2")
    val cow3 = find("cow3")
    val cow2_calf = find("cow2_calf")
    val cow3_calf = find("cow3_calf")
    val chicken = find("chicken")
    val chicken_brown = find("chicken_brown")
    val giantrat = find("giantrat")
    val giantrat_grey = find("giantrat_grey")
    val giantrat_grey2 = find("giantrat_grey2")
    val giantrat_grey3 = find("giantrat_grey3")
    val giantrat1_2 = find("giantrat1_2")
    val giantrat1_3 = find("giantrat1_3")
    val little_frog = find("little_frog_nodrops")
    val medium_frog = find("medium_frog_nodrops")
    val giant_frog = find("giant_frog_nodrops")
    val ram = find("ramunsheered")
    val ram2 = find("ramunsheered2")
    val ram3 = find("ramunsheered3")
    val rat = find("rat")
    val spider = find("spider")
    val giantspider = find("giantspider1")
    val duck = find("duck")
    val duck_female = find("duck_female")
    val duckling = find("duckling")

    val man = find("man")
    val man2 = find("man2")
    val man3 = find("man3")
    val man_indoor = find("man_indoor")
    val woman = find("woman")
    val woman2 = find("woman2")
    val woman3 = find("woman3")
    val farmer1 = find("farmer1")
    val farmer3 = find("farmer3")
    val farmer2_f = find("farmer2_f")
    val farmer3_f = find("farmer3_f")
    val farmer4 = find("farmer4")
    val goblin_1 = find("goblin_unarmed_melee_1")
    val goblin_2 = find("goblin_unarmed_melee_2")
    val goblin_3 = find("goblin_unarmed_melee_3")
    val goblin_4 = find("goblin_unarmed_melee_4")
    val goblin_5 = find("goblin_unarmed_melee_5")
    val goblin_6 = find("goblin_unarmed_melee_6")
    val goblin_7 = find("goblin_unarmed_melee_7")
    val goblin_8 = find("goblin_unarmed_melee_8")
    val goblin_armed = find("goblin_armed")
    val imp = find("imp")
    val mugger = find("mugger")
    val unicorn = find("unicorn")
}
