package org.rsmod.content.areas.misc.kandarinnorth.locs

import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLoc2
import org.rsmod.api.script.onOpLocU
import org.rsmod.content.areas.misc.kandarinnorth.configs.kandarin_north_locs
import org.rsmod.content.areas.misc.kandarinnorth.configs.kandarin_north_objs
import org.rsmod.content.areas.misc.kandarinnorth.configs.kandarin_north_varps
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The coal trucks west of McGrubor's Wood and north of Seers' Village share one store of up to 120
 * coal (the Kandarin Diary's larger stores are not built).
 */
// UNVERIFIED: the wording of every message.
class CoalTrucks : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1(kandarin_north_locs.coal_truck) { removeCoal() }
        onOpLoc2(kandarin_north_locs.coal_truck) { investigate() }
        onOpLocU(kandarin_north_locs.coal_truck, kandarin_north_objs.coal) { storeCoal() }
    }

    private fun ProtectedAccess.removeCoal() {
        val stored = vars[kandarin_north_varps.coal_truck_store]
        if (stored == 0) {
            mes("The coal truck is empty.")
            return
        }
        var taken = 0
        while (taken < stored && invAdd(inv, kandarin_north_objs.coal).success) {
            taken++
        }
        if (taken == 0) {
            mes("You don't have enough room to take any coal.")
            return
        }
        vars[kandarin_north_varps.coal_truck_store] = stored - taken
        mes("You take $taken coal from the coal truck.")
    }

    private fun ProtectedAccess.storeCoal() {
        val stored = vars[kandarin_north_varps.coal_truck_store]
        var added = 0
        while (stored + added < CAPACITY && invDel(inv, kandarin_north_objs.coal).success) {
            added++
        }
        if (added == 0) {
            mes(
                if (stored >= CAPACITY) "The coal truck is full." else "You have no coal to put in."
            )
            return
        }
        vars[kandarin_north_varps.coal_truck_store] = stored + added
        mes("You put $added coal in the coal truck. It now holds ${stored + added}.")
    }

    private fun ProtectedAccess.investigate() {
        val stored = vars[kandarin_north_varps.coal_truck_store]
        mes("The coal trucks hold $stored of $CAPACITY coal.")
    }

    private companion object {
        const val CAPACITY = 120
    }
}
