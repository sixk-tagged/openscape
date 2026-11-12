package org.rsmod.content.other.npcdrops

import jakarta.inject.Inject
import org.rsmod.api.config.refs.objs
import org.rsmod.api.death.Drop
import org.rsmod.api.death.NpcDropTables
import org.rsmod.api.death.NpcDrops
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Drop tables (OSRS wiki, Oct 2025) for the monsters only the Wilderness has. The ones also found
 * elsewhere (hill giants, skeletons, zombies, spiders, ...) keep the tables of the area that wrote
 * them up first. Every table here rolls the looting bag on its own, at the rate the wiki prints for
 * the monster. Ensouled heads, bones and other tertiaries are left out.
 */
class WildernessDrops @Inject constructor(private val tables: NpcDropTables) : PluginScript() {
    override fun ScriptContext.startup() {
        with(wilderness_drop_npcs) {
            green_dragons.forEach { tables[it] = greenDragonDrops }
            black_dragons.forEach { tables[it] = blackDragonDrops }
            lava_dragons.forEach { tables[it] = lavaDragonDrops }
            greater_demons.forEach { tables[it] = greaterDemonDrops }
            black_demons.forEach { tables[it] = blackDemonDrops }
            ankous.forEach { tables[it] = ankouDrops }
            chaos_dwarves.forEach { tables[it] = chaosDwarfDrops }
            chaos_druids.forEach { tables[it] = chaosDruidDrops }
            elder_chaos_druids.forEach { tables[it] = elderChaosDruidDrops }
            dark_warriors.forEach { tables[it] = darkWarriorDrops }
            dark_warriors_high.forEach { tables[it] = darkWarriorHighDrops }
            rogues.forEach { tables[it] = rogueDrops }
            rogues_high.forEach { tables[it] = rogueHighDrops }
            bandits.forEach { tables[it] = banditDrops }
            bandits_high.forEach { tables[it] = banditHighDrops }
            camp_leaders.forEach { tables[it] = campLeaderDrops }
            ice_giants.forEach { tables[it] = iceGiantDrops }
            ice_warriors.forEach { tables[it] = iceWarriorDrops }
            mammoths.forEach { tables[it] = mammothDrops }
            pirates.forEach { tables[it] = pirateDrops }
            earth_warriors.forEach { tables[it] = earthWarriorDrops }
            fire_giants.forEach { tables[it] = fireGiantDrops }
            magic_axes.forEach { tables[it] = magicAxeDrops }
            thugs.forEach { tables[it] = thugDrops }
            hellhounds.forEach { tables[it] = hellhoundDrops }
            monks.forEach { tables[it] = zamorakMonkDrops }
            zombies_low.forEach { tables[it] = bonesOnly.withBag(18) }
            zombies_armed.forEach { tables[it] = zombieLevel24Drops.withBag(24) }
            tables[giantskeleton] = bonesOnly.withBag(45)
            tables[giantrat1] = giantRatDrops.withBag(6)
            tables[black_unicorn] = unicornDrops.withBag(27)
            wolves.forEach { tables[it] = wolfDrops.withBag(64) }
            spiders.forEach { tables[it] = NpcDrops().withBag(52) }
            // The ent's logs and the golem's remains are not built; neither drops bones.
            tables[ent] = NpcDrops()
            tables[rune_golem] = NpcDrops()
        }
    }
}

/** The herb table with a quantity range for each herb, as the stronger monsters drop. */
private fun herbsQty(weight: Int, count: IntRange) =
    Drop(
        table = tableOf(128, *herbs.drops.map { Drop(it.obj, count, it.weight) }.toTypedArray()),
        weight = weight,
        members = true,
        f2p = always(objs.coins, 10),
    )

/** The looting bag, rolled on its own at 1 in `150 / combat level`, between 1/3 and 1/15. */
private fun NpcDrops.withBag(combat: Int) =
    NpcDrops(
        always =
            always +
                Drop(
                    table =
                        tableOf(
                            (150 / combat).coerceIn(3, 15),
                            drop(wilderness_drop_objs.looting_bag, 1),
                        )
                ),
        main = main,
        f2pMain = f2pMain,
    )

private val giantRatDrops = NpcDrops(always = listOf(bones, always(drop_objs.raw_rat_meat)))

private val unicornDrops =
    NpcDrops(always = listOf(bones, members(drop_objs.unicorn_horn, weight = 0)))

private val wolfDrops = NpcDrops(always = listOf(always(extra_objs.wolf_bones)))

/** Wiki section 4.2 (total 128, weights 128). */
private val greenDragonDrops =
    with(wilderness_drop_objs) {
        NpcDrops(
            always =
                listOf(
                    always(dragon_bones),
                    always(dragonhide_green),
                    Drop(table = tableOf(3, drop(looting_bag, 1))),
                ),
            main =
                tableOf(
                    128,
                    drop(steel_platelegs, 4),
                    drop(steel_battleaxe, 3),
                    drop(mithril_axe, 3),
                    drop(mithril_spear, 2),
                    drop(mithril_kiteshield, 1),
                    drop(adamant_full_helm, 1),
                    drop(rune_dagger, 1),
                    drop(waterrune, 8, 75),
                    drop(firerune, 1, 37),
                    drop(naturerune, 5, 15),
                    drop(lawrune, 3, 3),
                    herbs(15),
                    drop(objs.coins, 29, 44),
                    drop(objs.coins, 25, 132),
                    drop(objs.coins, 10, 200),
                    drop(objs.coins, 5, 11),
                    drop(objs.coins, 1, 440),
                    drop(bass, 3),
                    drop(adamantite_ore, 3),
                    gems(5),
                ),
        )
    }

/** Wiki section 4.3 (total 128, weights 128). */
private val blackDragonDrops =
    with(wilderness_drop_objs) {
        NpcDrops(
            always =
                listOf(
                    always(dragon_bones),
                    always(dragonhide_black),
                    Drop(table = tableOf(3, drop(looting_bag, 1))),
                ),
            main =
                tableOf(
                    128,
                    drop(mithril_2h_sword, 4),
                    drop(mithril_axe, 3),
                    drop(mithril_battleaxe, 3),
                    drop(rune_knife, 3, 2),
                    drop(mithril_kiteshield, 1),
                    drop(adamant_platebody, 1),
                    drop(rune_longsword, 1),
                    drop(adamant_javelin, 20, 30),
                    drop(adamant_dart_p, 7, 16),
                    drop(firerune, 8, 50),
                    drop(bloodrune, 3, 15),
                    drop(airrune, 1, 75),
                    drop(lawrune, 5, 10),
                    drop(objs.coins, 40, 196),
                    drop(objs.coins, 10, 330),
                    drop(objs.coins, 1, 690),
                    drop(dragon_javelin_head, 6, 10),
                    drop(adamantite_bar, 3),
                    drop(chocolate_cake, 3),
                    gems(3, underground = true),
                    rareTable(2),
                ),
        )
    }

// UNVERIFIED: the wiki's lines add to 135, not 128 (one is mistyped); built as printed.
/** Wiki section 4.4 (total 135, weights 135). */
private val lavaDragonDrops =
    with(wilderness_drop_objs) {
        NpcDrops(
            always =
                listOf(
                    always(lava_dragon_bones),
                    always(dragonhide_black),
                    always(lava_scale),
                    Drop(table = tableOf(3, drop(looting_bag, 1))),
                ),
            main =
                tableOf(
                    135,
                    drop(rune_dart, 6, 12),
                    drop(rune_knife, 4, 8),
                    drop(lava_battlestaff, 3),
                    drop(adamant_2h_sword, 2),
                    drop(adamant_platebody, 2),
                    drop(rune_axe, 2),
                    drop(rune_kiteshield, 2),
                    drop(rune_longsword, 2),
                    drop(rune_med_helm, 1),
                    drop(rune_full_helm, 1),
                    drop(rune_javelin, 10, 20),
                    drop(firerune, 7, 75),
                    drop(bloodrune, 7, 20),
                    drop(xbows_crossbow_bolts_runite, 6, 30),
                    drop(deathrune, 5, 20),
                    drop(lawrune, 5, 20),
                    drop(lavarune, 4, 15),
                    drop(lavarune, 4, 30),
                    herbsQty(5, 1..2),
                    drop(objs.coins, 15, 66),
                    drop(objs.coins, 7, 2960),
                    drop(objs.coins, 1, 690),
                    drop(fire_talisman, 1),
                    drop(fire_orb, 5, 15),
                    drop(chocolate_cake, 3, 3),
                    drop(adamantite_bar, 5, 2),
                    drop(xbows_bolt_tips_onyx, 5, 12),
                    drop(dragon_javelin_head, 7, 15),
                    gems(5),
                    rareTable(3),
                ),
        )
    }

/** Wiki section 4.5 (total 128, weights 126). */
private val greaterDemonDrops =
    with(wilderness_drop_objs) {
        NpcDrops(
            always = listOf(always(vile_ashes), Drop(table = tableOf(3, drop(looting_bag, 1)))),
            main =
                tableOf(
                    128,
                    drop(steel_2h_sword, 4),
                    drop(steel_axe, 3),
                    drop(steel_battleaxe, 3),
                    drop(mithril_kiteshield, 1),
                    drop(adamant_platelegs, 1),
                    drop(rune_full_helm, 1),
                    drop(firerune, 8, 75),
                    drop(chaosrune, 3, 15),
                    drop(deathrune, 3, 5),
                    drop(firerune, 1, 37),
                    drop(objs.coins, 40, 132),
                    drop(objs.coins, 29, 44),
                    drop(objs.coins, 10, 220),
                    drop(objs.coins, 7, 11),
                    drop(objs.coins, 1, 460),
                    drop(tuna, 3),
                    drop(gold_bar, 2),
                    drop(thread, 1, 10),
                    gems(5),
                ),
        )
    }

/** Wiki section 4.7 (total 128, weights 128). */
private val blackDemonDrops =
    with(wilderness_drop_objs) {
        NpcDrops(
            always =
                listOf(always(malicious_ashes), Drop(table = tableOf(3, drop(looting_bag, 1)))),
            main =
                tableOf(
                    128,
                    drop(black_sword, 4),
                    drop(steel_battleaxe, 3),
                    drop(black_axe, 2),
                    drop(mithril_kiteshield, 1),
                    drop(rune_med_helm, 1),
                    drop(rune_chainbody, 1),
                    drop(airrune, 8, 50),
                    drop(chaosrune, 7, 10),
                    drop(bloodrune, 4, 7),
                    drop(firerune, 1, 37),
                    drop(lawrune, 1, 3),
                    herbs(23),
                    drop(objs.coins, 40, 132),
                    drop(objs.coins, 7, 30),
                    drop(objs.coins, 6, 44),
                    drop(objs.coins, 6, 220),
                    drop(objs.coins, 1, 460),
                    drop(lobster, 3),
                    drop(adamantite_bar, 2),
                    drop(n3dose1defense, 1),
                    gems(5, underground = true),
                    rareTable(1),
                ),
        )
    }

// UNVERIFIED: the wiki heads this table /100 but its lines add to 126; built as printed.
/** Wiki section 4.8 (total 126, weights 126). */
internal val ankouDrops =
    with(wilderness_drop_objs) {
        NpcDrops(
            always = listOf(always(objs.bones), Drop(table = tableOf(3, drop(looting_bag, 1)))),
            main =
                tableOf(
                    126,
                    drop(black_knife, 1),
                    drop(black_robe, 1),
                    drop(adamant_arrow, 4, 5..14),
                    drop(deathrune, 10, 10),
                    drop(bloodrune, 6, 11),
                    drop(lawrune, 4, 2),
                    drop(bloodrune, 3, 5),
                    herbs(3),
                    uncommonSeeds(1),
                    drop(cert_blankrune_high, 5, 15),
                    drop(mithril_ore, 2, 3..7),
                    drop(objs.coins, 33, 8),
                    drop(objs.coins, 10, 5..204),
                    drop(wilderness_fishing_bait, 33, 60),
                    drop(sos_half_skull1, 3),
                    drop(bass, 2),
                    drop(weapon_poison, 2),
                    drop(bowl_mushroom_fried, 1),
                    gems(2),
                ),
        )
    }

// UNVERIFIED: the wiki's lines and its Nothing 5 add to 133, not 128; built as printed.
/** Wiki section 4.9 (total 133, weights 128). */
private val chaosDwarfDrops =
    with(wilderness_drop_objs) {
        NpcDrops(
            always = listOf(always(objs.bones), Drop(table = tableOf(3, drop(looting_bag, 1)))),
            main =
                tableOf(
                    133,
                    drop(steel_full_helm, 2),
                    drop(mithril_longsword, 1),
                    drop(mithril_sq_shield, 1),
                    drop(lawrune, 4, 3),
                    drop(airrune, 3, 24),
                    drop(chaosrune, 3, 10),
                    drop(mindrune, 3, 37),
                    drop(naturerune, 3, 9),
                    drop(cosmicrune, 2, 3),
                    drop(deathrune, 1, 3),
                    drop(waterrune, 1, 10),
                    drop(objs.coins, 40, 92),
                    drop(objs.coins, 18, 47),
                    drop(objs.coins, 11, 25),
                    drop(objs.coins, 10, 150),
                    drop(objs.coins, 2, 350),
                    drop(objs.coins, 2, 15),
                    drop(coal, 1),
                    drop(muddy_key, 7),
                    drop(cheese, 1),
                    drop(mithril_bar, 6),
                    drop(tomato, 1),
                    gems(5),
                ),
        )
    }

/** Wiki section 4.10 (total 128, weights 95). */
private val chaosDruidDrops =
    with(wilderness_drop_objs) {
        NpcDrops(
            always = listOf(always(objs.bones), Drop(table = tableOf(11, drop(looting_bag, 1)))),
            main =
                tableOf(
                    128,
                    drop(lawrune, 7, 2),
                    drop(xbows_crossbow_bolts_mithril, 4, 2..12),
                    drop(airrune, 3, 36),
                    drop(bodyrune, 2, 9),
                    drop(earthrune, 2, 9),
                    drop(mindrune, 2, 12),
                    drop(naturerune, 1, 3),
                    herbsQty(46, 1..2),
                    drop(objs.coins, 5, 3),
                    drop(objs.coins, 5, 8),
                    drop(objs.coins, 3, 29),
                    drop(objs.coins, 1, 35),
                    drop(vial_water, 10),
                    drop(bronze_longsword, 1),
                    drop(snape_grass, 1),
                    drop(unholy_symbol_mould, 1),
                    gems(1, underground = true),
                ),
        )
    }

/** Wiki section 4.11 (total 129, weights 128). */
private val elderChaosDruidDrops =
    with(wilderness_drop_objs) {
        NpcDrops(
            always =
                listOf(
                    always(objs.bones),
                    Drop(table = tableOf(1419, drop(zamrobetop, 4))),
                    Drop(table = tableOf(1419, drop(zamrobebottom, 4))),
                    Drop(table = tableOf(1419, drop(elderchaos_hood, 1))),
                    Drop(table = tableOf(1419, drop(elderchaos_bottom, 1))),
                    Drop(table = tableOf(1419, drop(elderchaos_top, 1))),
                    Drop(table = tableOf(3, drop(looting_bag, 1))),
                ),
            main =
                tableOf(
                    129,
                    drop(xbows_crossbow_bolts_mithril, 6, 8..28),
                    drop(lawrune, 7, 6),
                    drop(airrune, 5, 56),
                    drop(bodyrune, 5, 19),
                    drop(chaosrune, 5, 7),
                    drop(earthrune, 5, 19),
                    drop(mindrune, 5, 22),
                    drop(naturerune, 1, 12),
                    herbsQty(55, 1..4),
                    drop(objs.coins, 7, 80),
                    drop(objs.coins, 6, 250),
                    drop(vial_water, 10, 4),
                    drop(steel_longsword, 5),
                    drop(wilderness_fishing_bait, 2, 10..24),
                    drop(snape_grass, 1, 4),
                    drop(unholy_symbol_mould, 1),
                    gems(1),
                    rareTable(1),
                ),
        )
    }

/** Wiki section 4.12 (total 128, weights 110). */
private val darkWarriorDrops =
    with(wilderness_drop_objs) {
        NpcDrops(
            always = listOf(always(objs.bones), Drop(table = tableOf(15, drop(looting_bag, 1)))),
            main =
                tableOf(
                    128,
                    drop(bronze_med_helm, 3),
                    drop(iron_mace, 1),
                    drop(black_med_helm, 1),
                    drop(black_mace, 1),
                    drop(mindrune, 3, 2),
                    drop(waterrune, 2, 3),
                    drop(earthrune, 1, 2),
                    drop(bronze_arrow, 4, 8),
                    drop(chaosrune, 1, 2),
                    drop(naturerune, 2, 3),
                    herbs(3),
                    drop(objs.coins, 31),
                    drop(objs.coins, 20, 2),
                    drop(objs.coins, 20, 6),
                    drop(objs.coins, 7, 13),
                    drop(objs.coins, 6, 20),
                    drop(objs.coins, 2, 30),
                    drop(iron_ore, 1),
                    drop(sardine, 1),
                ),
        )
    }

/** Wiki section 4.12 (total 128, weights 112). */
private val darkWarriorHighDrops =
    with(wilderness_drop_objs) {
        NpcDrops(
            always = listOf(always(objs.bones), Drop(table = tableOf(3, drop(looting_bag, 1)))),
            main =
                tableOf(
                    128,
                    drop(adamant_med_helm, 3),
                    drop(black_mace, 1),
                    drop(black_med_helm, 1),
                    drop(mithril_mace, 1),
                    drop(mindrune, 3, 12),
                    drop(waterrune, 2, 12),
                    drop(earthrune, 1, 12),
                    drop(bronze_arrow, 4, 28),
                    drop(chaosrune, 1, 8),
                    drop(naturerune, 2, 9),
                    herbs(3),
                    drop(objs.coins, 31, 100),
                    drop(objs.coins, 20, 20),
                    drop(objs.coins, 20, 60),
                    drop(objs.coins, 7, 130),
                    drop(objs.coins, 6, 200),
                    drop(objs.coins, 2, 90),
                    drop(wilderness_fishing_bait, 2, 10..24),
                    drop(iron_ore, 1, 3),
                    drop(sardine, 1, 3),
                ),
        )
    }

/** Wiki section 4.13 (total 128, weights 120). */
private val rogueDrops =
    with(wilderness_drop_objs) {
        NpcDrops(
            always = listOf(always(objs.bones), Drop(table = tableOf(10, drop(looting_bag, 1)))),
            main =
                tableOf(
                    128,
                    drop(bronze_med_helm, 2),
                    drop(iron_dagger, 1),
                    drop(bolt, 22, 2..12),
                    drop(bronze_arrow, 3, 7),
                    drop(earthrune, 2, 4),
                    drop(firerune, 2, 6),
                    drop(mindrune, 2, 9),
                    drop(chaosrune, 1, 2),
                    herbs(23),
                    drop(objs.coins, 38, 3),
                    drop(objs.coins, 9, 5),
                    drop(objs.coins, 4, 15),
                    drop(objs.coins, 1, 25),
                    drop(fishing_bait, 5),
                    drop(copper_ore, 2),
                    drop(earth_talisman, 2),
                    drop(cabbage, 1),
                ),
        )
    }

/** Wiki section 4.13 (total 128, weights 0). */
private val rogueHighDrops =
    with(wilderness_drop_objs) {
        NpcDrops(
            always =
                listOf(
                    always(objs.bones),
                    Drop(objs.coins, 1000..3000),
                    Drop(table = tableOf(3, drop(looting_bag, 1))),
                )
        )
    }

/** Wiki section 4.14 (total 128, weights 126). */
private val banditDrops =
    with(wilderness_drop_objs) {
        NpcDrops(
            always = listOf(always(objs.bones), Drop(table = tableOf(6, drop(looting_bag, 1)))),
            main =
                tableOf(
                    128,
                    drop(iron_scimitar, 4),
                    drop(steel_sq_shield, 2),
                    drop(steel_axe, 1),
                    drop(chaosrune, 3, 6),
                    drop(waterrune, 3, 9),
                    drop(airrune, 2, 10),
                    drop(deathrune, 2, 2),
                    drop(lawrune, 2, 3),
                    members(bloodrune, 1, 2..2),
                    drop(mindrune, 1, 2),
                    drop(naturerune, 1, 2),
                    herbs(37),
                    drop(objs.coins, 26, 35),
                    drop(objs.coins, 13, 12),
                    drop(objs.coins, 10, 53),
                    drop(objs.coins, 7),
                    drop(objs.coins, 2, 80),
                    drop(coal, 6),
                    gems(3),
                ),
        )
    }

/** Wiki section 4.14 (total 143, weights 143). */
private val banditHighDrops =
    with(wilderness_drop_objs) {
        NpcDrops(
            always = listOf(always(objs.bones), Drop(table = tableOf(3, drop(looting_bag, 1)))),
            main =
                tableOf(
                    143,
                    drop(adamant_scimitar, 4),
                    drop(mithril_sq_shield, 2),
                    drop(mithril_axe, 1),
                    drop(rune_full_helm, 5),
                    drop(rune_med_helm, 5),
                    drop(rune_scimitar, 5),
                    drop(dragon_longsword, 1),
                    drop(chaosrune, 3, 10),
                    drop(waterrune, 3, 11),
                    drop(mindrune, 1, 12),
                    drop(airrune, 2, 25),
                    drop(deathrune, 2, 4),
                    drop(lawrune, 2, 6),
                    drop(bloodrune, 1, 6),
                    drop(naturerune, 1, 3),
                    herbs(41),
                    drop(objs.coins, 26, 35),
                    drop(objs.coins, 13, 120),
                    drop(objs.coins, 10, 53),
                    drop(objs.coins, 2, 250),
                    drop(objs.coins, 1, 10),
                    drop(coal, 6, 5),
                    drop(wilderness_fishing_bait, 2, 10..24),
                    gems(4),
                ),
        )
    }

/** Wiki section 4.15 (total 128, weights 126). */
private val campLeaderDrops =
    with(wilderness_drop_objs) {
        NpcDrops(
            always = listOf(always(objs.bones), Drop(table = tableOf(4, drop(looting_bag, 1)))),
            main =
                tableOf(
                    128,
                    drop(steel_longsword, 1),
                    drop(steel_full_helm, 1),
                    drop(lawrune, 4, 2),
                    drop(naturerune, 4, 4),
                    drop(bodyrune, 3, 12),
                    drop(chaosrune, 3, 3),
                    drop(waterrune, 3, 30),
                    drop(mindrune, 1, 5),
                    herbs(15),
                    drop(objs.coins, 30, 48),
                    drop(objs.coins, 18, 15),
                    drop(objs.coins, 11, 8),
                    drop(objs.coins, 10, 70),
                    drop(objs.coins, 5, 5),
                    drop(objs.coins, 2, 150),
                    drop(silver_ore, 11),
                    drop(swordfish, 2, 5),
                    gems(2),
                ),
        )
    }

/** Wiki section 4.16 (total 128, weights 128). */
private val iceGiantDrops =
    with(wilderness_drop_objs) {
        NpcDrops(
            always = listOf(always(big_bones), Drop(table = tableOf(3, drop(looting_bag, 1)))),
            main =
                tableOf(
                    128,
                    drop(iron_2h_sword, 5),
                    drop(black_kiteshield, 4),
                    drop(steel_axe, 4),
                    drop(steel_sword, 4),
                    drop(iron_platelegs, 1),
                    drop(mithril_mace, 1),
                    drop(mithril_sq_shield, 1),
                    drop(adamant_arrow, 6, 5),
                    drop(naturerune, 4, 6),
                    drop(mindrune, 3, 24),
                    drop(bodyrune, 3, 37),
                    drop(lawrune, 2, 3),
                    drop(waterrune, 1, 12),
                    drop(cosmicrune, 1, 4),
                    drop(deathrune, 1, 3),
                    members(bloodrune, 1, 2..2),
                    uncommonSeeds(8),
                    drop(objs.coins, 32, 117),
                    drop(objs.coins, 12, 53),
                    drop(objs.coins, 10, 196),
                    drop(objs.coins, 7, 8),
                    drop(objs.coins, 6, 22),
                    drop(objs.coins, 2, 400),
                    drop(jug_wine, 3),
                    drop(mithril_ore, 1),
                    drop(banana, 1),
                    gems(4),
                ),
        )
    }

/** Wiki section 4.17 (total 128, weights 115). */
private val iceWarriorDrops =
    with(wilderness_drop_objs) {
        NpcDrops(
            always = listOf(Drop(table = tableOf(3, drop(looting_bag, 1)))),
            main =
                tableOf(
                    128,
                    drop(iron_battleaxe, 3),
                    drop(mithril_mace, 1),
                    drop(naturerune, 10, 4),
                    drop(chaosrune, 8, 3),
                    drop(lawrune, 7, 2),
                    drop(cosmicrune, 5, 2),
                    drop(mithril_arrow, 5, 3),
                    drop(adamant_arrow, 2, 2),
                    drop(deathrune, 3, 2),
                    members(bloodrune, 1, 2..2),
                    herbs(10),
                    uncommonSeeds(18),
                    drop(objs.coins, 39, 15),
                    gems(3),
                ),
        )
    }

/** Wiki section 4.18 (total 128, weights 128). */
private val mammothDrops =
    with(wilderness_drop_objs) {
        NpcDrops(
            always = listOf(always(big_bones), Drop(table = tableOf(3, drop(looting_bag, 1)))),
            main =
                tableOf(
                    128,
                    herbs(8),
                    drop(objs.coins, 13, 30),
                    drop(objs.coins, 7, 180),
                    drop(acorn, 5),
                    drop(limpwurt_seed, 5, 2),
                    drop(n1doseprayerrestore, 5),
                    drop(steel_arrow, 5, 15),
                    drop(lobster, 3, 2),
                    drop(wilderness_fishing_bait, 2, 12),
                    gems(3),
                    rareTable(2),
                    Drop(
                        table =
                            tableOf(
                                640,
                                drop(potato_seed, 30, 3),
                                drop(onion_seed, 20, 3),
                                drop(cabbage_seed, 16, 3),
                                drop(tomato_seed, 14, 3),
                                drop(sweetcorn_seed, 10, 3),
                                drop(strawberry_seed, 8, 3),
                                drop(watermelon_seed, 2, 3),
                                drop(redberry_seed_2, 30, 2),
                                drop(cadavaberry_seed_2, 20, 2),
                                drop(dwellberry_seed_2, 16, 2),
                                drop(jangerberry_seed_2, 14, 2),
                                drop(whiteberry_seed_2, 10, 2),
                                drop(poisonivy_bush_seed, 10, 2),
                                drop(barley_seed, 30, 4),
                                drop(hammerstone_hop_seed, 20, 4),
                                drop(asgarnian_hop_seed, 16, 4),
                                drop(jute_seed, 14, 4),
                                drop(yanillian_hop_seed, 10, 4),
                                drop(krandorian_hop_seed, 8, 4),
                                drop(wildblood_hop_seed, 2, 4),
                                drop(apple_tree_seed, 18),
                                drop(banana_tree_seed, 12),
                                drop(orange_tree_seed, 10),
                                drop(curry_tree_seed, 6),
                                drop(pineapple_seed_2, 3),
                                drop(papaya_tree_seed, 1),
                            ),
                        weight = 70,
                        members = true,
                    ),
                ),
        )
    }

/** Wiki section 4.19 (total 128, weights 120). */
private val pirateDrops =
    with(wilderness_drop_objs) {
        NpcDrops(
            always = listOf(always(objs.bones), Drop(table = tableOf(6, drop(looting_bag, 1)))),
            main =
                tableOf(
                    128,
                    drop(iron_dagger, 6),
                    drop(bronze_scimitar, 4),
                    drop(iron_platebody, 1),
                    members(xbows_crossbow_bolts_iron, 10, 2..12),
                    drop(chaosrune, 6, 2),
                    drop(naturerune, 5, 2),
                    drop(bronze_arrow, 3, 9),
                    drop(bronze_arrow, 2, 12),
                    drop(airrune, 2, 10),
                    drop(earthrune, 2, 9),
                    drop(firerune, 2, 5),
                    drop(lawrune, 1, 2),
                    drop(objs.coins, 29, 4),
                    drop(objs.coins, 13, 25),
                    drop(objs.coins, 8, 7),
                    drop(objs.coins, 6, 12),
                    drop(objs.coins, 4, 35),
                    drop(objs.coins, 1, 55),
                    members(eye_patch, 12),
                    drop(chefs_hat, 1),
                    drop(iron_bar, 1),
                    gems(1),
                ),
        )
    }

/** Wiki section 4.20 (total 128, weights 100). */
private val earthWarriorDrops =
    with(wilderness_drop_objs) {
        NpcDrops(
            always = listOf(Drop(table = tableOf(3, drop(looting_bag, 1)))),
            main =
                tableOf(
                    128,
                    drop(steel_spear, 3),
                    drop(staff_of_earth, 2),
                    drop(earthrune, 13, 12),
                    drop(naturerune, 9, 3),
                    drop(chaosrune, 7, 3),
                    drop(lawrune, 6, 2),
                    drop(deathrune, 4, 2),
                    drop(earthrune, 3, 60),
                    drop(bloodrune, 1, 2),
                    herbs(14),
                    uncommonSeeds(18),
                    drop(objs.coins, 18, 12),
                    gems(2, underground = true),
                ),
        )
    }

/** Wiki section 4.22 (total 128, weights 128). */
internal val fireGiantDrops =
    with(wilderness_drop_objs) {
        NpcDrops(
            always = listOf(always(big_bones), Drop(table = tableOf(3, drop(looting_bag, 1)))),
            main =
                tableOf(
                    128,
                    drop(steel_axe, 3),
                    drop(mithril_sq_shield, 2),
                    drop(fire_battlestaff, 1),
                    drop(rune_scimitar, 1),
                    drop(firerune, 10, 150),
                    drop(chaosrune, 7, 5),
                    drop(rune_arrow, 5, 12),
                    drop(bloodrune, 4, 5),
                    drop(firerune, 1, 37),
                    drop(lawrune, 1, 2),
                    herbs(19),
                    drop(objs.coins, 40, 60),
                    drop(objs.coins, 7, 15),
                    drop(objs.coins, 6, 25),
                    drop(objs.coins, 2, 300),
                    drop(objs.coins, 1, 50),
                    drop(lobster, 3),
                    drop(steel_bar, 2),
                    drop(n2dose1strength, 1),
                    gems(11, underground = true),
                    rareTable(1),
                ),
        )
    }

/** Wiki section 4.23 (total 128, weights 0). */
private val magicAxeDrops =
    with(wilderness_drop_objs) {
        NpcDrops(
            always = listOf(always(iron_battleaxe), Drop(table = tableOf(3, drop(looting_bag, 1))))
        )
    }

/** Wiki section 4.24 (total 128, weights 98). */
private val thugDrops =
    with(wilderness_drop_objs) {
        NpcDrops(
            always = listOf(always(objs.bones), Drop(table = tableOf(15, drop(looting_bag, 1)))),
            main =
                tableOf(
                    128,
                    drop(iron_med_helm, 4),
                    drop(iron_battleaxe, 2),
                    drop(steel_axe, 1),
                    drop(naturerune, 13, 2),
                    drop(chaosrune, 4, 2),
                    drop(cosmicrune, 1, 2),
                    drop(lawrune, 1, 2),
                    drop(deathrune, 1, 2),
                    herbs(24),
                    drop(iron_ore, 4),
                    drop(iron_bar, 3),
                    drop(coal, 2),
                    drop(objs.coins, 23, 8),
                    drop(objs.coins, 12, 15),
                    drop(objs.coins, 2, 30),
                    drop(objs.coins, 1, 20),
                ),
        )
    }

/** Wiki section 4.25 (total 50, weights 10). */
internal val hellhoundDrops =
    with(wilderness_drop_objs) {
        NpcDrops(
            always =
                listOf(
                    always(vile_ashes),
                    Drop(table = tableOf(32768, drop(smouldering_stone, 1))),
                    Drop(table = tableOf(3, drop(looting_bag, 1))),
                ),
            main =
                tableOf(50, drop(deathrune, 6, 10), drop(deathrune, 3, 20), drop(deathrune, 1, 50)),
        )
    }

/** Wiki section 4.26 (total 20, weights 2). */
private val zamorakMonkDrops =
    with(wilderness_drop_objs) {
        NpcDrops(
            always = listOf(always(objs.bones), Drop(table = tableOf(6, drop(looting_bag, 1)))),
            main = tableOf(20, drop(zamrobebottom, 1), drop(zamrobetop, 1)),
        )
    }

object wilderness_drop_npcs : NpcReferences() {
    val green_dragons =
        listOf(
            find("green_dragon"),
            find("green_dragon2"),
            find("green_dragon3"),
            find("green_dragon4"),
            find("green_dragon5"),
        )
    val black_dragons = listOf(find("black_dragon4"), find("black_dragon5"))
    val lava_dragons = listOf(find("lava_dragon"))
    val greater_demons =
        listOf(
            find("greater_demon2"),
            find("greater_demon3"),
            find("greater_demon4"),
            find("greater_demon5"),
        )
    val black_demons = listOf(find("black_demon2"), find("black_demon3"), find("black_demon5"))
    val ankous = listOf(find("wilderness_ankou"))
    val chaos_dwarves = listOf(find("dwarf_chaos"))
    val chaos_druids = listOf(find("chaos_druid"))
    val elder_chaos_druids = listOf(find("wilderness_chaos_druid"))
    val dark_warriors = listOf(find("dark_warrior"))
    val dark_warriors_high = listOf(find("wilderness_dark_warrior"))
    val rogues = listOf(find("rogue"))
    val rogues_high = listOf(find("wilderness_rogue"))
    val bandits = listOf(find("brawling_bandit"), find("guard_bandit"))
    val bandits_high = listOf(find("wilderness_bandit"))
    val camp_leaders = listOf(find("black_heather"), find("donny_the_lad"), find("speedy_keith"))
    val ice_giants = listOf(find("icegiant"), find("icegiant2"), find("icegiant3"))
    val ice_warriors = listOf(find("icewarrior"))
    val mammoths = listOf(find("wilderness_mammoth"))
    val pirates = listOf(find("pirate_aggressive"))
    val earth_warriors = listOf(find("earthwarrior"))
    val fire_giants = listOf(find("firegiant"), find("firegiant2"), find("firegiant3"))
    val magic_axes = listOf(find("magicaxe"))
    val thugs = listOf(find("thug"))
    val hellhounds = listOf(find("hellhound"))
    val monks = listOf(find("chaos_temple_monk_wilderness"))
    val zombies_low =
        listOf(
            find("zombie2_rural1"),
            find("zombie2_rural2"),
            find("zombie2_rural3"),
            find("zombie2_rural4"),
            find("wilderness_zombie"),
            find("wilderness_zombie2"),
            find("wilderness_zombie3"),
        )
    val zombies_armed =
        listOf(
            find("zombie_armed_rural1"),
            find("zombie_armed_rural2"),
            find("zombie_armed_rural3"),
            find("zombie_armed_rural4"),
            find("zombie_armed_rural5"),
        )
    val giantskeleton = find("giantskeleton")
    val giantrat1 = find("giantrat1")
    val black_unicorn = find("black_unicorn")
    val wolves = listOf(find("wolf"), find("whitewolf"))
    val spiders = listOf(find("ice_spider"), find("shadow_spider"), find("poisonspider"))
    val ent = find("wilderness_ent")
    val rune_golem = find("wilderness_rune_golem")
}

/** Drop objs for [WildernessDrops]. */
object wilderness_drop_objs : ObjReferences() {
    val acorn = find("acorn")
    val adamant_2h_sword = find("adamant_2h_sword")
    val adamant_arrow = find("adamant_arrow")
    val adamant_dart_p = find("adamant_dart_p")
    val adamant_full_helm = find("adamant_full_helm")
    val adamant_javelin = find("adamant_javelin")
    val adamant_med_helm = find("adamant_med_helm")
    val adamant_platebody = find("adamant_platebody")
    val adamant_platelegs = find("adamant_platelegs")
    val adamant_scimitar = find("adamant_scimitar")
    val adamantite_bar = find("adamantite_bar")
    val adamantite_ore = find("adamantite_ore")
    val airrune = find("airrune")
    val apple_tree_seed = find("apple_tree_seed")
    val asgarnian_hop_seed = find("asgarnian_hop_seed")
    val banana = find("banana")
    val banana_tree_seed = find("banana_tree_seed")
    val barley_seed = find("barley_seed")
    val bass = find("bass")
    val big_bones = find("big_bones")
    val black_axe = find("black_axe")
    val black_kiteshield = find("black_kiteshield")
    val black_knife = find("black_knife")
    val black_mace = find("black_mace")
    val black_med_helm = find("black_med_helm")
    val black_robe = find("black_robe")
    val black_sword = find("black_sword")
    val bloodrune = find("bloodrune")
    val bodyrune = find("bodyrune")
    val bolt = find("bolt")
    val bowl_mushroom_fried = find("bowl_mushroom_fried")
    val bronze_arrow = find("bronze_arrow")
    val bronze_longsword = find("bronze_longsword")
    val bronze_med_helm = find("bronze_med_helm")
    val bronze_scimitar = find("bronze_scimitar")
    val cabbage = find("cabbage")
    val cabbage_seed = find("cabbage_seed")
    val cadavaberry_seed_2 = find("cadavaberry_seed_2")
    val cert_blankrune_high = find("cert_blankrune_high")
    val chaosrune = find("chaosrune")
    val cheese = find("cheese")
    val chefs_hat = find("chefs_hat")
    val chocolate_cake = find("chocolate_cake")
    val coal = find("coal")
    val copper_ore = find("copper_ore")
    val cosmicrune = find("cosmicrune")
    val curry_tree_seed = find("curry_tree_seed")
    val deathrune = find("deathrune")
    val dragon_bones = find("dragon_bones")
    val dragon_javelin_head = find("dragon_javelin_head")
    val dragon_longsword = find("dragon_longsword")
    val dragonhide_black = find("dragonhide_black")
    val dragonhide_green = find("dragonhide_green")
    val dwellberry_seed_2 = find("dwellberry_seed_2")
    val earth_talisman = find("earth_talisman")
    val earthrune = find("earthrune")
    val elderchaos_bottom = find("elderchaos_bottom")
    val elderchaos_hood = find("elderchaos_hood")
    val elderchaos_top = find("elderchaos_top")
    val eye_patch = find("eye_patch")
    val fire_battlestaff = find("fire_battlestaff")
    val fire_orb = find("fire_orb")
    val fire_talisman = find("fire_talisman")
    val firerune = find("firerune")
    val fishing_bait = find("fishing_bait")
    val gold_bar = find("gold_bar")
    val hammerstone_hop_seed = find("hammerstone_hop_seed")
    val iron_2h_sword = find("iron_2h_sword")
    val iron_bar = find("iron_bar")
    val iron_battleaxe = find("iron_battleaxe")
    val iron_dagger = find("iron_dagger")
    val iron_mace = find("iron_mace")
    val iron_med_helm = find("iron_med_helm")
    val iron_ore = find("iron_ore")
    val iron_platebody = find("iron_platebody")
    val iron_platelegs = find("iron_platelegs")
    val iron_scimitar = find("iron_scimitar")
    val jangerberry_seed_2 = find("jangerberry_seed_2")
    val jug_wine = find("jug_wine")
    val jute_seed = find("jute_seed")
    val krandorian_hop_seed = find("krandorian_hop_seed")
    val lava_battlestaff = find("lava_battlestaff")
    val lava_dragon_bones = find("lava_dragon_bones")
    val lava_scale = find("lava_scale")
    val lavarune = find("lavarune")
    val lawrune = find("lawrune")
    val limpwurt_seed = find("limpwurt_seed")
    val lobster = find("lobster")
    val looting_bag = find("looting_bag")
    val malicious_ashes = find("malicious_ashes")
    val mindrune = find("mindrune")
    val mithril_2h_sword = find("mithril_2h_sword")
    val mithril_arrow = find("mithril_arrow")
    val mithril_axe = find("mithril_axe")
    val mithril_bar = find("mithril_bar")
    val mithril_battleaxe = find("mithril_battleaxe")
    val mithril_kiteshield = find("mithril_kiteshield")
    val mithril_longsword = find("mithril_longsword")
    val mithril_mace = find("mithril_mace")
    val mithril_ore = find("mithril_ore")
    val mithril_spear = find("mithril_spear")
    val mithril_sq_shield = find("mithril_sq_shield")
    val muddy_key = find("muddy_key")
    val n1doseprayerrestore = find("1doseprayerrestore")
    val n2dose1strength = find("2dose1strength")
    val n3dose1defense = find("3dose1defense")
    val naturerune = find("naturerune")
    val onion_seed = find("onion_seed")
    val orange_tree_seed = find("orange_tree_seed")
    val papaya_tree_seed = find("papaya_tree_seed")
    val pineapple_seed_2 = find("pineapple_seed_2")
    val poisonivy_bush_seed = find("poisonivy_bush_seed")
    val potato_seed = find("potato_seed")
    val redberry_seed_2 = find("redberry_seed_2")
    val rune_arrow = find("rune_arrow")
    val rune_axe = find("rune_axe")
    val rune_chainbody = find("rune_chainbody")
    val rune_dagger = find("rune_dagger")
    val rune_dart = find("rune_dart")
    val rune_full_helm = find("rune_full_helm")
    val rune_javelin = find("rune_javelin")
    val rune_kiteshield = find("rune_kiteshield")
    val rune_knife = find("rune_knife")
    val rune_longsword = find("rune_longsword")
    val rune_med_helm = find("rune_med_helm")
    val rune_scimitar = find("rune_scimitar")
    val sardine = find("sardine")
    val silver_ore = find("silver_ore")
    val smouldering_stone = find("smouldering_stone")
    val snape_grass = find("snape_grass")
    val sos_half_skull1 = find("sos_half_skull1")
    val staff_of_earth = find("staff_of_earth")
    val steel_2h_sword = find("steel_2h_sword")
    val steel_arrow = find("steel_arrow")
    val steel_axe = find("steel_axe")
    val steel_bar = find("steel_bar")
    val steel_battleaxe = find("steel_battleaxe")
    val steel_full_helm = find("steel_full_helm")
    val steel_longsword = find("steel_longsword")
    val steel_platelegs = find("steel_platelegs")
    val steel_spear = find("steel_spear")
    val steel_sq_shield = find("steel_sq_shield")
    val steel_sword = find("steel_sword")
    val strawberry_seed = find("strawberry_seed")
    val sweetcorn_seed = find("sweetcorn_seed")
    val swordfish = find("swordfish")
    val thread = find("thread")
    val tomato = find("tomato")
    val tomato_seed = find("tomato_seed")
    val tuna = find("tuna")
    val unholy_symbol_mould = find("unholy_symbol_mould")
    val vial_water = find("vial_water")
    val vile_ashes = find("vile_ashes")
    val watermelon_seed = find("watermelon_seed")
    val waterrune = find("waterrune")
    val weapon_poison = find("weapon_poison")
    val whiteberry_seed_2 = find("whiteberry_seed_2")
    val wildblood_hop_seed = find("wildblood_hop_seed")
    val wilderness_fishing_bait = find("wilderness_fishing_bait")
    val xbows_bolt_tips_onyx = find("xbows_bolt_tips_onyx")
    val xbows_crossbow_bolts_iron = find("xbows_crossbow_bolts_iron")
    val xbows_crossbow_bolts_mithril = find("xbows_crossbow_bolts_mithril")
    val xbows_crossbow_bolts_runite = find("xbows_crossbow_bolts_runite")
    val yanillian_hop_seed = find("yanillian_hop_seed")
    val zamrobebottom = find("zamrobebottom")
    val zamrobetop = find("zamrobetop")
}
