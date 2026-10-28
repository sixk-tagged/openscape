package org.rsmod.content.skills.magic.spell.utility

import jakarta.inject.Inject
import kotlin.math.min
import org.rsmod.api.combat.commons.magic.MagicSpell
import org.rsmod.api.combat.commons.magic.MagicSpellType
import org.rsmod.api.combat.commons.magic.Spellbook
import org.rsmod.api.combat.manager.MagicRuneManager
import org.rsmod.api.combat.manager.MagicRuneManager.Companion.isFailure
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.protect.ProtectedAccessLauncher
import org.rsmod.api.script.onIfOverlayButton
import org.rsmod.api.spells.MagicSpellRegistry
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.inv.isType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.obj.UnpackedObjType
import org.rsmod.game.type.obj.isType
import org.rsmod.game.type.seq.SeqType
import org.rsmod.game.type.spot.SpotanimType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Lvl-1 to Lvl-7 Enchant (jewellery) and the crossbow bolt enchantments. The jewellery spells are
 * cast on an inventory item and swap it one for one; the bolt spells come from the bolt menu and
 * swap up to [BOLTS_PER_CAST] bolts of one kind.
 *
 * Note: casting on one item does not roll on to the rest of that type (the wiki's 2019
 * auto-continue), each cast is a click. Enchanted items start with their full charges.
 */
class EnchantScript
@Inject
constructor(
    private val spells: MagicSpellRegistry,
    private val runes: MagicRuneManager,
    private val objTypes: ObjTypeList,
    private val launcher: ProtectedAccessLauncher,
) : PluginScript() {
    override fun ScriptContext.startup() {
        for (level in EnchantObjs.levels) {
            val spell = checkNotNull(spells.getObjSpell(level.spell))
            val enchants = level.enchants.associateBy { it.input.id }
            onSpellOnItem(spell, launcher) { slot, obj ->
                val found = enchants[obj.id]
                if (found == null) {
                    // UNVERIFIED: the wiki lists no message for the wrong item.
                    mes("You can't cast that spell on this item.")
                } else {
                    enchant(level, spell, found, slot, obj)
                }
            }
        }
        startBolts()
    }

    private fun ProtectedAccess.enchant(
        level: EnchantObjs.Level,
        spell: MagicSpell,
        enchant: EnchantObjs.Enchant,
        slot: Int,
        obj: UnpackedObjType,
    ) {
        if (actionDelay > mapClock || inv[slot]?.isType(obj) != true) {
            return
        }
        if (runes.attemptCast(player, spell).isFailure()) {
            return
        }
        invReplaceSlot(inv, slot, 1, enchant.output)
        anim(level.seq)
        spotanim(level.spot, height = 92)
        statAdvance(stats.magic, spell.castXp)
        actionDelay = mapClock + CAST_TICKS
    }

    private fun ScriptContext.startBolts() {
        val menu = checkNotNull(spells.getObjSpell(objs.view_bolt_enchantment_spells))
        // UNVERIFIED: the bolt menu's buttons are assumed to be `comsub` 0..9 of this component in
        // the order of [EnchantObjs.bolts]; the cache names no component for them.
        onIfOverlayButton(menu.component) {
            val bolt = EnchantObjs.bolts.getOrNull(comsub) ?: return@onIfOverlayButton
            launcher.launch(player) { enchantBolts(bolt, menu) }
        }
    }

    private fun ProtectedAccess.enchantBolts(bolt: EnchantObjs.Bolt, menu: MagicSpell) {
        if (actionDelay > mapClock) {
            return
        }
        val have = inv.count(objTypes[bolt.input])
        if (have == 0) {
            // UNVERIFIED: the wiki lists no message for casting without the bolts.
            mes("You don't have any ${objTypes[bolt.input].name.lowercase()} to enchant.")
            return
        }
        val amount = min(have, BOLTS_PER_CAST)
        val spell =
            menu.copy(
                obj = bolt.spell,
                name = objTypes[bolt.output].name,
                type = MagicSpellType.Utility,
                spellbook = Spellbook.Standard,
                levelReq = bolt.level,
                castXp = bolt.xp,
                objReqs =
                    bolt.runes.map { (rune, count) -> MagicSpell.ObjRequirement(rune, count, null) },
            )
        if (runes.attemptCast(player, spell).isFailure()) {
            return
        }
        invDel(inv, bolt.input, amount)
        invAdd(inv, bolt.output, amount)
        anim(UtilitySeqs.enchant_3)
        spotanim(UtilitySpots.enchant_3, height = 92)
        statAdvance(stats.magic, spell.castXp)
        actionDelay = mapClock + CAST_TICKS
    }

    private companion object {
        const val CAST_TICKS = 3
        const val BOLTS_PER_CAST = 10
    }
}

object EnchantObjs : ObjReferences() {
    class Enchant(val input: ObjType, val output: ObjType)

    class Level(
        val spell: ObjType,
        val seq: SeqType,
        val spot: SpotanimType,
        val enchants: List<Enchant>,
    )

    class Bolt(
        val spell: ObjType,
        val level: Int,
        val xp: Double,
        val runes: List<Pair<ObjType, Int>>,
        val input: ObjType,
        val output: ObjType,
    )

    private val cosmic = UtilityObjs.cosmic_rune to 1

    /** Input to output internal names, from the wiki's tables (the cache names are irregular). */
    val levels =
        listOf(
            Level(
                objs.spell_enchant_lvl_1,
                UtilitySeqs.enchant_1,
                UtilitySpots.enchant_1,
                listOf(
                    enchant("sapphire_ring", "ring_of_recoil"),
                    enchant("jewl_sapphire_bracelet", "jewl_bracelet_of_clay"),
                    enchant("sapphire_necklace", "necklace_of_minigames_8"),
                    enchant("strung_sapphire_amulet", "amulet_of_magic"),
                    enchant("opal_ring", "ring_of_pursuit"),
                    enchant("opal_bracelet", "expeditious_bracelet"),
                    enchant("opal_necklace", "dodgy_necklace"),
                    enchant("strung_opal_amulet", "amulet_of_bounty"),
                ),
            ),
            Level(
                objs.spell_enchant_lvl_2,
                UtilitySeqs.enchant_2,
                UtilitySpots.enchant_2,
                listOf(
                    enchant("emerald_ring", "ring_of_dueling_8"),
                    enchant("jewl_emerald_bracelet", "jewl_castlewars_bracelet3"),
                    enchant("emerald_necklace", "magic_emerald_necklace"),
                    enchant("strung_emerald_amulet", "amulet_of_defence"),
                    enchant("jade_ring", "ring_of_returning_5"),
                    enchant("jade_bracelet", "flamtaer_bracelet"),
                    enchant("jade_necklace", "necklace_of_passage_5"),
                    enchant("strung_jade_amulet", "amulet_of_chemistry"),
                ),
            ),
            Level(
                objs.spell_enchant_lvl_3,
                UtilitySeqs.enchant_3,
                UtilitySpots.enchant_3,
                listOf(
                    enchant("ruby_ring", "ring_of_forging"),
                    enchant("jewl_ruby_bracelet", "jewl_bracelet_of_innoculation"),
                    enchant("ruby_necklace", "necklace_of_digsite_5"),
                    enchant("strung_ruby_amulet", "amulet_of_strength"),
                    enchant("topaz_ring", "vampyre_ring"),
                    enchant("topaz_bracelet", "bracelet_of_slaughter"),
                    enchant("topaz_necklace", "necklace_of_faith"),
                    enchant("strung_topaz_amulet", "burning_amulet_5"),
                ),
            ),
            Level(
                objs.spell_enchant_lvl_4,
                UtilitySeqs.enchant_3,
                UtilitySpots.enchant_4,
                listOf(
                    enchant("diamond_ring", "ring_of_life"),
                    enchant("jewl_diamond_bracelet", "jewl_runerunning_bracelet_5"),
                    enchant("diamond_necklace", "jewl_necklace_of_phoenix"),
                    enchant("strung_diamond_amulet", "amulet_of_power"),
                ),
            ),
            Level(
                objs.spell_enchant_lvl_5,
                UtilitySeqs.enchant_3,
                UtilitySpots.enchant_5,
                listOf(
                    enchant("dragonstone_ring", "ring_of_wealth"),
                    enchant("jewl_dragonstone_bracelet", "jewl_bracelet_of_combat"),
                    enchant("dragonstone_necklace", "jewl_necklace_of_skills"),
                    enchant("strung_dragonstone_amulet", "amulet_of_glory"),
                ),
            ),
            Level(
                objs.spell_enchant_lvl_6,
                UtilitySeqs.enchant_3,
                UtilitySpots.enchant_6,
                listOf(
                    enchant("onyx_ring", "enchanted_onyx_ring"),
                    enchant("jewl_onyx_bracelet", "jewl_bracelet_regen"),
                    enchant("onyx_necklace", "jewl_beserker_necklace"),
                    enchant("strung_onyx_amulet", "enchanted_onyx_amulet"),
                ),
            ),
            Level(
                objs.spell_enchant_lvl_7,
                UtilitySeqs.enchant_3,
                UtilitySpots.enchant_6,
                listOf(
                    enchant("zenyte_ring", "zenyte_ring_enchanted"),
                    enchant("zenyte_bracelet", "zenyte_bracelet_enchanted"),
                    enchant("zenyte_necklace", "zenyte_necklace_enchanted"),
                    enchant("zenyte_amulet", "zenyte_amulet_enchanted"),
                ),
            ),
        )

    /** In the order of the bolt menu: Opal to Onyx. Levels, xp and runes from the wiki. */
    val bolts =
        listOf(
            bolt(
                "04_enchant_bolt_opal",
                4,
                9.0,
                listOf(cosmic, objs.air_rune to 2),
                "opal_bolt",
                "xbows_crossbow_bolts_bronze_tipped_opal_enchanted",
            ),
            bolt(
                "07_enchant_bolt_sapphire",
                7,
                17.5,
                listOf(cosmic, objs.water_rune to 1, objs.mind_rune to 1),
                "xbows_crossbow_bolts_mithril_tipped_sapphire",
                "xbows_crossbow_bolts_mithril_tipped_sapphire_enchanted",
            ),
            bolt(
                "14_enchant_bolt_jade",
                14,
                19.0,
                listOf(cosmic, objs.earth_rune to 2),
                "xbows_crossbow_bolts_blurite_tipped_jade",
                "xbows_crossbow_bolts_blurite_tipped_jade_enchanted",
            ),
            bolt(
                "24_enchant_bolt_pearl",
                24,
                29.5,
                listOf(cosmic, objs.water_rune to 2),
                "pearl_bolt",
                "xbows_crossbow_bolts_iron_tipped_pearl_enchanted",
            ),
            bolt(
                "27_enchant_bolt_emerald",
                27,
                37.0,
                listOf(cosmic, objs.air_rune to 3, objs.nature_rune to 1),
                "xbows_crossbow_bolts_mithril_tipped_emerald",
                "xbows_crossbow_bolts_mithril_tipped_emerald_enchanted",
            ),
            bolt(
                "29_enchant_bolt_redtopaz",
                29,
                33.0,
                listOf(cosmic, objs.fire_rune to 2),
                "xbows_crossbow_bolts_steel_tipped_redtopaz",
                "xbows_crossbow_bolts_steel_tipped_redtopaz_enchanted",
            ),
            bolt(
                "49_enchant_bolt_ruby",
                49,
                59.0,
                listOf(cosmic, objs.fire_rune to 5, objs.blood_rune to 1),
                "xbows_crossbow_bolts_adamantite_tipped_ruby",
                "xbows_crossbow_bolts_adamantite_tipped_ruby_enchanted",
            ),
            bolt(
                "57_enchant_bolt_diamond",
                57,
                67.0,
                listOf(cosmic, objs.earth_rune to 10, objs.law_rune to 2),
                "xbows_crossbow_bolts_adamantite_tipped_diamond",
                "xbows_crossbow_bolts_adamantite_tipped_diamond_enchanted",
            ),
            bolt(
                "68_enchant_bolt_dragonstone",
                68,
                78.0,
                listOf(cosmic, objs.earth_rune to 15, objs.soul_rune to 1),
                "xbows_crossbow_bolts_runite_tipped_dragonstone",
                "xbows_crossbow_bolts_runite_tipped_dragonstone_enchanted",
            ),
            bolt(
                "87_enchant_bolt_onyx",
                87,
                97.0,
                listOf(cosmic, objs.fire_rune to 20, objs.death_rune to 1),
                "xbows_crossbow_bolts_runite_tipped_onyx",
                "xbows_crossbow_bolts_runite_tipped_onyx_enchanted",
            ),
        )

    private fun enchant(input: String, output: String) = Enchant(find(input), find(output))

    private fun bolt(
        spell: String,
        level: Int,
        xp: Double,
        runes: List<Pair<ObjType, Int>>,
        input: String,
        output: String,
    ) = Bolt(find(spell), level, xp, runes, find(input), find(output))
}
