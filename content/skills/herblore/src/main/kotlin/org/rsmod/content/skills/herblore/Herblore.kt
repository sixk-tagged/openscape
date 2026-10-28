package org.rsmod.content.skills.herblore

import kotlin.math.min
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.herbloreLvl
import org.rsmod.api.script.onOpHeld1
import org.rsmod.api.script.onOpHeldU
import org.rsmod.content.skills.production.register
import org.rsmod.game.inv.Inventory
import org.rsmod.game.type.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Cleaning herbs, then the item-on-item recipes (unfinished potions, potions, grinding), weapon
 * poison and decanting. Levels, messages and the repeat loop live in the shared production script.
 */
class Herblore : PluginScript() {
    override fun ScriptContext.startup() {
        for (herb in HerbloreRecipes.herbs) {
            onOpHeld1(herb.grimy) { clean(herb, it.slot, it.inventory) }
        }
        register(HerbloreRecipes.combos)
        for (item in WeaponPoisons.all) {
            onOpHeldU(item.vial, item.weapon) { poison(item) }
        }
        for (family in Potions.all) {
            decant(family)
        }
    }

    private fun ProtectedAccess.clean(herb: Herb, slot: Int, inv: Inventory) {
        if (player.herbloreLvl < herb.cleanLevel) {
            mes("You need level ${herb.cleanLevel} Herblore to clean this herb.")
            return
        }
        if (!invReplaceSlot(inv, slot, count = 1, replacement = herb.clean).success) {
            return
        }
        statAdvance(stats.herblore, herb.xp)
        mes("You clean the dirt from the ${ocName(herb.clean).lowercase()}.")
    }

    private fun ProtectedAccess.poison(item: PoisonedWeapon) {
        val pieces = min(item.batch, invTotal(inv, item.weapon))
        if (pieces < 1 || !invDel(inv, item.weapon, pieces).success) {
            return
        }
        invDel(inv, item.vial)
        if (!invAdd(inv, item.poisoned, pieces).success) {
            invAdd(inv, item.weapon, pieces)
            invAdd(inv, item.vial)
            mes("You don't have enough inventory space.")
            return
        }
        invAdd(inv, HerbloreObjs.vial_empty)
        mes("You dip the ${ocName(item.weapon).lowercase()} in the poison.")
    }

    /**
     * Two potions of one kind pour into the fuller one (the event can't tell which was used on
     * which); a potion on an empty vial splits its doses between them.
     */
    private fun ScriptContext.decant(family: List<ObjType>) {
        fun dose(n: Int): ObjType = family[4 - n]
        for (a in 1..3) {
            for (b in a..3) {
                onOpHeldU(dose(a), dose(b)) {
                    val into = min(4, a + b)
                    val left = a + b - into
                    invReplaceSlot(inv, it.secondSlot, 1, dose(into))
                    invReplaceSlot(
                        inv,
                        it.firstSlot,
                        1,
                        if (left == 0) HerbloreObjs.vial_empty else dose(left),
                    )
                }
            }
        }
        for (n in 2..4) {
            onOpHeldU(dose(n), HerbloreObjs.vial_empty) {
                invReplaceSlot(inv, it.firstSlot, 1, dose((n + 1) / 2))
                invReplaceSlot(inv, it.secondSlot, 1, dose(n / 2))
            }
        }
    }
}
