package org.rsmod.content.other.windmill

import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.intVarBit
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLocU
import org.rsmod.api.type.builders.varp.VarpBuilder
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.api.type.refs.varbit.VarBitReferences
import org.rsmod.api.type.refs.varp.VarpReferences
import org.rsmod.game.entity.Player
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

private var Player.hopperGrain: Int by intVarp(MillVarps.hopper_grain)
private var Player.binFlour: Int by intVarBit(MillVarBits.flour)
private var Player.binShowsFlour: Int by intVarBit(MillVarBits.show_flour)

/** Grain in the hopper, operate the controls, then take the flour from the bin downstairs. */
class WindmillFlourScript : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1(MillLocs.hopper) { fillHopper() }
        onOpLocU(MillLocs.hopper, MillObjs.grain) { fillHopper() }
        onOpLoc1(MillLocs.hopper_controls) { operate() }
        onOpLoc1(MillLocs.flour_bin) { emptyBin() }
        onOpLocU(MillLocs.flour_bin, MillObjs.pot) { emptyBin() }
    }

    private fun ProtectedAccess.fillHopper() {
        if (player.hopperGrain > 0) {
            mes("There is already grain in the hopper.")
            return
        }
        if (!invDel(inv, MillObjs.grain).success) {
            mes("You need some grain to put in the hopper.")
            return
        }
        anim(MillSeqs.reach)
        player.hopperGrain = 1
        mes("You put the grain in the hopper. You should now pull the lever nearby to operate it.")
    }

    private fun ProtectedAccess.operate() {
        anim(MillSeqs.reach)
        if (player.hopperGrain == 0) {
            mes("You operate the empty hopper. Nothing interesting happens.")
            return
        }
        if (player.binFlour >= BIN_CAPACITY) {
            mes("The flour bin downstairs is full, I should empty it first.")
            return
        }
        player.hopperGrain = 0
        player.binFlour++
        player.binShowsFlour = 1
        mes("You operate the hopper. The grain slides down the chute.")
    }

    private fun ProtectedAccess.emptyBin() {
        if (player.binFlour == 0) {
            mes(
                "The flour bin is already empty. You need to place wheat in the hopper upstairs first."
            )
            return
        }
        if (!invReplace(inv, MillObjs.pot, 1, MillObjs.pot_of_flour).success) {
            mes("You need an empty pot to hold the flour in.")
            return
        }
        anim(MillSeqs.reach)
        player.binFlour--
        if (player.binFlour == 0) {
            player.binShowsFlour = 0
        }
        mes("You fill a pot with flour from the bin.")
    }

    private companion object {
        const val BIN_CAPACITY = 30
    }
}

object MillLocs : LocReferences() {
    val hopper = find("hopper1")
    val hopper_controls = find("hopperlevers1")
    /** Multiloc on varbit mill_showflour: empty or full bin. */
    val flour_bin = find("millbase")
}

object MillObjs : ObjReferences() {
    val grain = find("grain")
    val pot = find("pot_empty")
    val pot_of_flour = find("pot_flour")
}

object MillSeqs : SeqReferences() {
    val reach = find("human_pickuptable")
}

object MillVarBits : VarBitReferences() {
    /** Flour waiting in the bin, 0-30. */
    val flour = find("mill_flour")
    /** 1 shows the full bin. */
    val show_flour = find("mill_showflour")
}

object MillVarps : VarpReferences() {
    /** Custom: 1 while grain sits in the hopper. */
    val hopper_grain = find("mill_hopper_grain")
}

internal object MillVarpBuilder : VarpBuilder() {
    init {
        build("mill_hopper_grain")
    }
}
