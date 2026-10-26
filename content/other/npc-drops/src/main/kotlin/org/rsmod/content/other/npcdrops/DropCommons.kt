package org.rsmod.content.other.npcdrops

import org.rsmod.api.config.refs.objs
import org.rsmod.api.death.Drop
import org.rsmod.api.death.DropTable
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.type.obj.ObjType

/** Tables and helpers shared by the area drop scripts (wiki, Oct 2025). */
internal fun tableOf(total: Int, vararg drops: Drop) = DropTable(total, drops.toList())

private val megaRare =
    with(extra_objs) {
        tableOf(128, drop(rune_spear, 8), drop(shield_left_half, 4), drop(dragon_spear, 3))
    }

private fun gemTable(talisman: ObjType) =
    with(extra_objs) {
        table(
            drop(uncut_sapphire, 32),
            drop(uncut_emerald, 16),
            drop(uncut_ruby, 8),
            members(talisman, 3),
            drop(uncut_diamond, 2),
            members(rune_javelin, 1, 5..5),
            members(loop_half, 1),
            members(tooth_half, 1),
            Drop(table = megaRare, weight = 1, members = true),
        )
    }

private val surfaceGems = gemTable(extra_objs.nature_talisman)
private val undergroundGems = gemTable(extra_objs.chaos_talisman)

/** The gem drop table; underground monsters give a chaos talisman instead of a nature one. */
internal fun gems(weight: Int, underground: Boolean = false) =
    Drop(table = if (underground) undergroundGems else surfaceGems, weight = weight)

/** The rare drop table, as used by the underground monsters. */
internal fun rareTable(weight: Int) =
    with(extra_objs) {
        Drop(
            table =
                table(
                    drop(drop_objs.nature_rune, 3, 67),
                    members(adamant_javelin, 2, 20..20),
                    drop(death_rune, 2, 45),
                    drop(law_rune, 2, 45),
                    drop(rune_arrow, 2, 42),
                    drop(steel_arrow, 2, 150),
                    drop(rune_2h_sword, 3),
                    drop(rune_battleaxe, 3),
                    drop(rune_sq_shield, 2),
                    members(dragon_med_helm, 1),
                    drop(rune_kiteshield, 1),
                    drop(objs.coins, 21, 3000),
                    members(loop_half, 20),
                    members(tooth_half, 20),
                    drop(runite_bar, 5),
                    drop(dragonstone, 2),
                    drop(noted_silver_ore, 2, 100),
                    Drop(table = undergroundGems, weight = 20),
                    Drop(table = megaRare, weight = 15, members = true),
                ),
            weight = weight,
        )
    }

/** The general seed drop table (combat level 33 or below), coins x5 on a free world. */
internal fun generalSeeds(weight: Int) =
    with(drop_objs) {
        Drop(
            table =
                tableOf(
                    1008,
                    drop(potato_seed, 368, 4),
                    drop(onion_seed, 276, 4),
                    drop(cabbage_seed, 184, 4),
                    drop(tomato_seed, 92, 3),
                    drop(sweetcorn_seed, 46, 3),
                    drop(strawberry_seed, 23, 2),
                    drop(watermelon_seed, 11, 2),
                    drop(snape_grass_seed, 8, 2),
                ),
            weight = weight,
            members = true,
            f2p = always(objs.coins, 5),
        )
    }

/** The uncommon seed drop table (moss giants), coins x5 on a free world. */
internal fun uncommonSeeds(weight: Int) =
    with(extra_objs) {
        Drop(
            table =
                tableOf(
                    1048,
                    drop(limpwurt_seed, 137),
                    drop(strawberry_seed, 131),
                    drop(marrentill_seed, 125),
                    drop(jangerberry_seed, 92),
                    drop(tarromin_seed, 85),
                    drop(wildblood_seed, 83),
                    drop(watermelon_seed, 63),
                    drop(harralander_seed, 56),
                    drop(snape_grass_seed, 40),
                    drop(ranarr_seed, 39),
                    drop(whiteberry_seed, 34),
                    drop(mushroom_spore, 29),
                    drop(toadflax_seed, 27),
                    drop(belladonna_seed, 18),
                    drop(irit_seed, 18),
                    drop(poison_ivy_seed, 13),
                    drop(avantoe_seed, 12),
                    drop(cactus_seed, 12),
                    drop(kwuarm_seed, 9),
                    drop(potato_cactus_seed, 8),
                    drop(snapdragon_seed, 5),
                    drop(cadantine_seed, 4),
                    drop(lantadyme_seed, 3),
                    drop(dwarf_weed_seed, 2),
                    drop(torstol_seed, 1),
                ),
            weight = weight,
            members = true,
            f2p = always(objs.coins, 5),
        )
    }

/** Drop objs the Lumbridge tables did not need ([drop_objs] holds those). */
object extra_objs : ObjReferences() {
    val plainstaff = find("plainstaff")
    val wizard_hat = find("blackwizhat")
    val black_robe = find("black_robe")
    val blue_wizard_robe = find("wizards_robe")
    val air_rune = find("airrune")
    val blood_rune = find("bloodrune")
    val cosmic_rune = find("cosmicrune")
    val law_rune = find("lawrune")
    val death_rune = find("deathrune")
    val soul_rune = find("soulrune")
    val water_talisman = find("water_talisman")
    val fire_talisman = find("fire_talisman")
    val body_talisman = find("body_talisman")
    val iron_sword = find("iron_sword")
    val iron_full_helm = find("iron_full_helm")
    val iron_med_helm = find("iron_med_helm")
    val iron_axe = find("iron_axe")
    val iron_scimitar = find("iron_scimitar")
    val iron_mace = find("iron_mace")
    val iron_ore = find("iron_ore")
    val iron_bolts = find("xbows_crossbow_bolts_iron")
    val iron_arrow = find("iron_arrow")
    val steel_arrow = find("steel_arrow")
    val mithril_arrow = find("mithril_arrow")
    val rune_arrow = find("rune_arrow")
    val steel_mace = find("steel_mace")
    val steel_bar = find("steel_bar")
    val bronze_bar = find("bronze_bar")
    val bronze_longsword = find("bronze_longsword")
    val bronze_kiteshield = find("bronze_kiteshield")
    val pot_of_flour = find("pot_flour")
    val black_cape = find("black_cape")
    val eye_of_newt = find("eye_of_newt")

    val vile_ashes = find("vile_ashes")
    val steel_full_helm = find("steel_full_helm")
    val steel_axe = find("steel_axe")
    val steel_scimitar = find("steel_scimitar")
    val mithril_sq_shield = find("mithril_sq_shield")
    val mithril_chainbody = find("mithril_chainbody")
    val rune_med_helm = find("rune_med_helm")
    val jug_of_wine = find("jug_wine")
    val gold_ore = find("gold_ore")

    val adamant_scimitar = find("adamant_scimitar")
    val adamant_kiteshield = find("adamant_kiteshield")
    val adamant_javelin = find("adamant_javelin")
    val limpwurt_root = find("limpwurt_root")
    val cooking_apple = find("cooking_apple")
    val potato = find("potato")

    val wolf_bones = find("wolf_bones")
    val raw_ugthanki_meat = find("raw_ugthanki_meat")
    val bear_fur = find("fur")
    val raw_bear_meat = find("raw_bear_meat")
    val zamorak_monk_top = find("zamrobetop")
    val zamorak_monk_bottom = find("zamrobebottom")
    val ring_mould = find("ring_mould")

    val black_sq_shield = find("black_sq_shield")
    val magic_staff = find("magic_staff")
    val steel_med_helm = find("steel_med_helm")
    val mithril_sword = find("mithril_sword")
    val mithril_spear = find("mithril_spear")
    val steel_kiteshield = find("steel_kiteshield")
    val coal = find("coal")
    val spinach_roll = find("spinach_roll")

    val wyvern_bones = find("wyvern_bones")
    val granite_legs = find("granite_legs")
    val dragon_platelegs = find("dragon_platelegs")
    val dragon_plateskirt = find("dragon_plateskirt")
    val earth_battlestaff = find("earth_battlestaff")
    val noted_battlestaff = find("cert_battlestaff")
    val rune_axe = find("rune_axe")
    val rune_battleaxe = find("rune_battleaxe")
    val rune_warhammer = find("rune_warhammer")
    val rune_full_helm = find("rune_full_helm")
    val rune_kiteshield = find("rune_kiteshield")
    val adamant_bolts = find("xbows_crossbow_bolts_adamantite")
    val runite_bolts = find("xbows_crossbow_bolts_runite")
    val noted_pure_essence = find("cert_blankrune_high")
    val noted_magic_logs = find("cert_magic_logs")
    val noted_adamantite_bar = find("cert_adamantite_bar")
    val noted_iron_ore = find("cert_iron_ore")
    val noted_uncut_ruby = find("cert_uncut_ruby")
    val noted_uncut_diamond = find("cert_uncut_diamond")
    val noted_unpowered_orb = find("cert_stafforb")
    val noted_silver_ore = find("cert_silver_ore")
    val lobster = find("lobster")
    val prayer_potion_4 = find("4doseprayerrestore")
    val runite_crossbow_u = find("xbows_crossbow_unstrung_runite")
    val ranarr_seed = find("ranarr_seed")
    val snapdragon_seed = find("snapdragon_seed")

    val uncut_sapphire = find("uncut_sapphire")
    val uncut_emerald = find("uncut_emerald")
    val uncut_ruby = find("uncut_ruby")
    val uncut_diamond = find("uncut_diamond")
    val nature_talisman = find("nature_talisman")
    val chaos_talisman = find("chaos_talisman")
    val rune_javelin = find("rune_javelin")
    val loop_half = find("keyhalf2")
    val tooth_half = find("keyhalf1")
    val rune_spear = find("rune_spear")
    val shield_left_half = find("dragonshield_a")
    val dragon_spear = find("dragon_spear")
    val rune_2h_sword = find("rune_2h_sword")
    val rune_sq_shield = find("rune_sq_shield")
    val dragon_med_helm = find("dragon_med_helm")
    val runite_bar = find("runite_bar")
    val dragonstone = find("dragonstone")

    val limpwurt_seed = find("limpwurt_seed")
    val strawberry_seed = find("strawberry_seed")
    val marrentill_seed = find("marrentill_seed")
    val jangerberry_seed = find("jangerberry_bush_seed")
    val tarromin_seed = find("tarromin_seed")
    val wildblood_seed = find("wildblood_hop_seed")
    val watermelon_seed = find("watermelon_seed")
    val harralander_seed = find("harralander_seed")
    val snape_grass_seed = find("snape_grass_seed")
    val whiteberry_seed = find("whiteberry_bush_seed")
    val mushroom_spore = find("mushroom_seed")
    val toadflax_seed = find("toadflax_seed")
    val belladonna_seed = find("belladonna_seed")
    val irit_seed = find("irit_seed")
    val poison_ivy_seed = find("poisonivy_bush_seed")
    val avantoe_seed = find("avantoe_seed")
    val cactus_seed = find("cactus_seed")
    val kwuarm_seed = find("kwuarm_seed")
    val potato_cactus_seed = find("potato_cactus_seed")
    val cadantine_seed = find("cadantine_seed")
    val lantadyme_seed = find("lantadyme_seed")
    val dwarf_weed_seed = find("dwarf_weed_seed")
    val torstol_seed = find("torstol_seed")
}
