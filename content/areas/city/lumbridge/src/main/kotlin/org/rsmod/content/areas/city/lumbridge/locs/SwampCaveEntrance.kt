package org.rsmod.content.areas.city.lumbridge.locs

import org.rsmod.api.config.refs.seqs
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.boolVarBit
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLocU
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.varbit.VarBitReferences
import org.rsmod.game.entity.Player
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** The dark hole into the Lumbridge Swamp Caves (a rope is tied once), its rope and signs. */
class SwampCaveEntrance : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLocU(SwampCaveLocs.dark_hole, SwampCaveObjs.rope) { tieRope() }
        onOpLoc1(SwampCaveLocs.dark_hole) { climbDown() }
        onOpLoc1(SwampCaveLocs.climbing_rope) { climbUp() }
        onOpLoc1(SwampCaveLocs.warning_sign) { readSign() }
    }

    // Note: the rope messages are unverified.
    private fun ProtectedAccess.tieRope() {
        if (player.swampRopeTied) {
            mes("Nothing interesting happens.")
            return
        }
        if (invDel(inv, SwampCaveObjs.rope).failure) {
            return
        }
        player.swampRopeTied = true
        mes("You tie the rope to the top of the entrance and throw it down.")
    }

    // Note: light sources, swamp gas and the insect swarm are not implemented.
    private suspend fun ProtectedAccess.climbDown() {
        if (!player.swampRopeTied) {
            mes("There is a sheer drop below the hole. You will need a rope.")
            return
        }
        arriveDelay()
        anim(seqs.human_pickupfloor)
        delay(1)
        telejump(CAVE_LANDING)
    }

    private suspend fun ProtectedAccess.climbUp() {
        arriveDelay()
        anim(seqs.human_reachforladder)
        delay(1)
        telejump(SWAMP_LANDING)
    }

    // Note: OSRS shows this in a sign interface, not mesboxes.
    private suspend fun ProtectedAccess.readSign() {
        mesbox(
            "~-~-~ WARNING ~-~-~<br>Noxious gases vent into this cave.<br>" +
                "Naked flames may cause an explosion!"
        )
        mesbox(
            "Beware of vicious head-grabbing beasts!<br>" +
                "Contact a Slayer Master for protective headgear."
        )
    }

    private companion object {
        // Note: landings sit next to the rope (3169,9572) and the hole (3169,3172); unverified.
        val CAVE_LANDING = CoordGrid(3168, 9572)
        val SWAMP_LANDING = CoordGrid(3169, 3173)
    }
}

// The real OSRS varbit; vanilla varps are saved on logout by default.
private var Player.swampRopeTied by boolVarBit(SwampCaveVarBits.roped_entrance)

object SwampCaveLocs : LocReferences() {
    val dark_hole = find("goblin_cave_entrance")
    val climbing_rope = find("swamp_cave_climbing_rope")
    val warning_sign = find("goblin_caves_warningsign")
}

object SwampCaveObjs : ObjReferences() {
    val rope = find("rope")
}

object SwampCaveVarBits : VarBitReferences() {
    val roped_entrance = find("swamp_caves_roped_entrance")
}
