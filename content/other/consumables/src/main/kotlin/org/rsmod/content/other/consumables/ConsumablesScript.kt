package org.rsmod.content.other.consumables

import jakarta.inject.Inject
import kotlin.math.max
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.baseHitpointsLvl
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.script.onOpHeld1
import org.rsmod.content.other.consumables.configs.ConsumableObjs
import org.rsmod.content.other.consumables.configs.ConsumableSeqs
import org.rsmod.content.other.consumables.configs.ConsumableVarps
import org.rsmod.game.entity.Player
import org.rsmod.game.inv.Inventory
import org.rsmod.game.type.stat.StatTypeList
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * "Eat" and "Drink" (inventory op 1). Food, potions and combo food each have their own 3-tick
 * delay, kept as clock values rather than `delay()` so one doesn't block the others.
 */
class ConsumablesScript @Inject constructor(private val statTypes: StatTypeList) : PluginScript() {
    private var Player.foodDelay by intVarp(ConsumableVarps.food_delay)
    private var Player.potionDelay by intVarp(ConsumableVarps.potion_delay)
    private var Player.comboDelay by intVarp(ConsumableVarps.combo_delay)

    override fun ScriptContext.startup() {
        for (food in FOODS) {
            onOpHeld1(food.obj) { eat(food, it.slot, it.inventory) }
        }
        for (potion in POTIONS) {
            potion.doses.forEachIndexed { dose, obj ->
                onOpHeld1(obj) { drink(potion, dose, it.slot, it.inventory) }
            }
        }
    }

    private fun ProtectedAccess.eat(food: Food, slot: Int, inv: Inventory) {
        if (player.hitpoints == 0) {
            return
        }
        val readyAt = if (food.combo) player.comboDelay else player.foodDelay
        if (readyAt > mapClock) {
            return
        }
        if (!invDel(inv, food.obj, count = 1, slot = slot).success) {
            return
        }
        anim(ConsumableSeqs.eat)
        mes("You eat the ${food.label}.")
        val heal = food.heal(player.baseHitpointsLvl)
        if (food.overheal) {
            statBoost(stats.hitpoints, heal, 0)
        } else {
            statHeal(stats.hitpoints, heal, 0)
        }
        mes("It heals some health.")
        player.foodDelay = mapClock + DELAY
        if (food.combo) {
            player.comboDelay = mapClock + DELAY
            player.potionDelay = mapClock + DELAY
        }
        actionDelay = max(actionDelay, mapClock) + food.attackDelay
    }

    private fun ProtectedAccess.drink(potion: Potion, dose: Int, slot: Int, inv: Inventory) {
        if (player.hitpoints == 0 || player.potionDelay > mapClock) {
            return
        }
        val next = potion.doses.getOrNull(dose + 1) ?: ConsumableObjs.vial
        if (!invReplaceSlot(inv, slot, count = 1, replacement = next).success) {
            return
        }
        anim(ConsumableSeqs.eat)
        mes("You drink some of your ${potion.label}.")
        potion.effect(player, statTypes)
        val left = potion.doses.size - dose - 1
        mes(
            when (left) {
                0 -> "You have finished your potion."
                1 -> "You have 1 dose of potion left."
                else -> "You have $left doses of potion left."
            }
        )
        player.potionDelay = mapClock + DELAY
    }

    private companion object {
        const val DELAY = 3
    }
}
