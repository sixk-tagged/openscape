package org.rsmod.content.other.poison

import jakarta.inject.Inject
import org.rsmod.api.combat.commons.poison.antipoison
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.player.stat.statHeal
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
 * "Drink" on the antipoison family and Sanfew serum. It shares `ConsumablesScript`'s potion delay,
 * so it can't be drunk in the same 3 ticks as another potion.
 */
class AntipoisonScript @Inject constructor(private val statTypes: StatTypeList) : PluginScript() {
    private var Player.potionDelay by intVarp(ConsumableVarps.potion_delay)

    override fun ScriptContext.startup() {
        for (potion in ANTIPOISONS) {
            potion.doses.forEachIndexed { dose, obj ->
                onOpHeld1(obj) { drink(potion, dose, it.slot, it.inventory) }
            }
        }
    }

    private fun ProtectedAccess.drink(potion: Antipoison, dose: Int, slot: Int, inv: Inventory) {
        if (player.hitpoints == 0 || player.potionDelay > mapClock) {
            return
        }
        val next = potion.doses.getOrNull(dose + 1) ?: ConsumableObjs.vial
        if (!invReplaceSlot(inv, slot, count = 1, replacement = next).success) {
            return
        }
        anim(ConsumableSeqs.eat)
        mes("You drink some of your ${potion.label}.")
        player.antipoison(potion.immunity)
        if (potion.restores) {
            for (stat in statTypes.values) {
                if (!stat.isType(stats.hitpoints)) {
                    player.statHeal(stat, 4, 30)
                }
            }
        }
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
