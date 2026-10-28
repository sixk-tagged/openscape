package org.rsmod.content.skills.magic.spell.utility

import jakarta.inject.Inject
import org.rsmod.api.combat.commons.magic.MagicSpell
import org.rsmod.api.combat.manager.MagicRuneManager
import org.rsmod.api.combat.manager.MagicRuneManager.Companion.isFailure
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.protect.ProtectedAccessLauncher
import org.rsmod.api.spells.MagicSpellRegistry
import org.rsmod.game.inv.isType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.obj.isType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Superheat Item: an ore in the inventory becomes its bar, with the Smithing xp of the furnace
 * recipe. Nothing is taken when the ore, the coal or the Smithing level is missing. The recipes are
 * the wiki's table for the items the cache has (lead and nickel came with the later Sailing
 * update).
 *
 * Note: gold gauntlets and the Smithing cape/catalyst xp boosts are not applied.
 */
class SuperheatScript
@Inject
constructor(
    private val spells: MagicSpellRegistry,
    private val runes: MagicRuneManager,
    private val objTypes: ObjTypeList,
    private val launcher: ProtectedAccessLauncher,
) : PluginScript() {
    private class Recipe(
        val inputs: List<Pair<ObjType, Int>>,
        val bar: ObjType,
        val level: Int,
        val xp: Double,
    )

    override fun ScriptContext.startup() {
        val spell = checkNotNull(spells.getObjSpell(objs.spell_superheat))
        onSpellOnItem(spell, launcher) { slot, obj ->
            if (actionDelay > mapClock) {
                return@onSpellOnItem
            }
            if (inv[slot]?.isType(obj) != true) {
                return@onSpellOnItem
            }
            val candidates = RECIPES_BY_ORE[obj.id]
            when {
                obj.isType(UtilityObjs.elemental_ore) ->
                    fail("Even this spell is not hot enough to heat this item.")
                candidates == null -> fail("You need to cast superheat item on ore.")
                else -> {
                    // The first recipe the inventory can pay for wins (iron with coal is steel).
                    val recipe = candidates.firstOrNull { canPay(it) } ?: candidates.last()
                    smelt(recipe, spell)
                }
            }
        }
    }

    private fun ProtectedAccess.fail(message: String) {
        mes(message)
        spotanim(UtilitySpots.failed_spell)
        soundSynth(UtilitySynths.superheat_fail)
    }

    private fun ProtectedAccess.canPay(recipe: Recipe): Boolean =
        recipe.inputs.all { (type, count) -> inv.count(objTypes[type]) >= count }

    private fun ProtectedAccess.smelt(recipe: Recipe, spell: MagicSpell) {
        val barName = objTypes[recipe.bar].name.lowercase()
        if (statBase(stats.smithing) < recipe.level) {
            // UNVERIFIED: the wiki lists no message for a low Smithing level.
            mes("You need a Smithing level of ${recipe.level} to smelt a $barName.")
            return
        }
        if (!canPay(recipe)) {
            // UNVERIFIED: the wiki lists no message for missing ore or coal.
            mes("You do not have the ore needed to smelt a $barName.")
            return
        }
        if (runes.attemptCast(player, spell).isFailure()) {
            return
        }
        for ((type, count) in recipe.inputs) {
            invDel(inv, type, count)
        }
        invAdd(inv, recipe.bar)
        anim(UtilitySeqs.superheat)
        spotanim(UtilitySpots.superheat, height = 92)
        soundSynth(UtilitySynths.superheat)
        statAdvance(stats.magic, spell.castXp)
        statAdvance(stats.smithing, recipe.xp)
        actionDelay = mapClock + CAST_TICKS
    }

    private companion object {
        const val CAST_TICKS = 3

        val RECIPES =
            listOf(
                // Steel is listed before iron: iron ore with two coal makes steel.
                Recipe(
                    listOf(UtilityObjs.iron_ore to 1, UtilityObjs.coal to 2),
                    UtilityObjs.steel_bar,
                    30,
                    17.5,
                ),
                Recipe(
                    listOf(UtilityObjs.tin_ore to 1, UtilityObjs.copper_ore to 1),
                    UtilityObjs.bronze_bar,
                    1,
                    6.2,
                ),
                Recipe(listOf(UtilityObjs.iron_ore to 1), UtilityObjs.iron_bar, 15, 12.5),
                Recipe(listOf(UtilityObjs.silver_ore to 1), UtilityObjs.silver_bar, 20, 13.7),
                Recipe(listOf(UtilityObjs.gold_ore to 1), UtilityObjs.gold_bar, 40, 22.5),
                Recipe(
                    listOf(UtilityObjs.lovakite_ore to 1, UtilityObjs.coal to 2),
                    UtilityObjs.lovakite_bar,
                    45,
                    20.0,
                ),
                Recipe(
                    listOf(UtilityObjs.mithril_ore to 1, UtilityObjs.coal to 4),
                    UtilityObjs.mithril_bar,
                    50,
                    30.0,
                ),
                Recipe(
                    listOf(UtilityObjs.adamantite_ore to 1, UtilityObjs.coal to 6),
                    UtilityObjs.adamantite_bar,
                    70,
                    37.5,
                ),
                Recipe(
                    listOf(UtilityObjs.runite_ore to 1, UtilityObjs.coal to 8),
                    UtilityObjs.runite_bar,
                    85,
                    50.0,
                ),
            )

        /** Ore id to its recipes, in preference order. Coal is not an ore to cast on. */
        val RECIPES_BY_ORE: Map<Int, List<Recipe>> by lazy {
            val byOre = hashMapOf<Int, MutableList<Recipe>>()
            for (recipe in RECIPES) {
                for ((type, _) in recipe.inputs) {
                    if (!type.isType(UtilityObjs.coal)) {
                        byOre.getOrPut(type.id) { mutableListOf() } += recipe
                    }
                }
            }
            byOre
        }
    }
}
