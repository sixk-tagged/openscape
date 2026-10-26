package org.rsmod.content.areas.city.lumbridge.locs

import jakarta.inject.Inject
import org.rsmod.api.config.refs.seqs
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** Fred's farm stile, the small fishing nets by the Fishing tutor and the castle cellar hole. */
class LumbridgeScenery @Inject constructor(private val locRepo: LocRepository) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1(LumbridgeSceneryLocs.stile) { climbStile(it.loc) }
        onOpLoc1(LumbridgeSceneryLocs.net) { takeNet(it.loc) }
        // The Lost Tribe is not implemented, so the tunnel stays blocked.
        onOpLoc1(LumbridgeSceneryLocs.cellar_hole) { mes("The tunnel is blocked with rocks.") }
    }

    // The only stile (3197,3276) is 1x2 along z and straddles an east-west fence.
    // Note: the 2-tick crossing is unverified.
    private suspend fun ProtectedAccess.climbStile(loc: BoundLocInfo) {
        arriveDelay()
        val south = coords.z <= loc.coords.z
        val dest = if (south) loc.coords.translateZ(2) else loc.coords.translateZ(-1)
        anim(LumbridgeScenerySeqs.stile)
        delay(2)
        telejump(dest)
    }

    private fun ProtectedAccess.takeNet(loc: BoundLocInfo) {
        if (inv.isFull()) {
            mes("You don't have enough inventory space.")
            return
        }
        anim(seqs.human_pickupfloor)
        locRepo.del(loc, NET_RESPAWN)
        invAdd(inv, LumbridgeSceneryObjs.net)
    }

    private companion object {
        // Note: the net respawn time is unverified.
        const val NET_RESPAWN = 100
    }
}

object LumbridgeSceneryLocs : LocReferences() {
    val stile = find("qip_sheep_shearer_fullstyle")
    val net = find("net")
    val cellar_hole = find("lost_tribe_hole_2")
}

object LumbridgeSceneryObjs : ObjReferences() {
    val net = find("net")
}

object LumbridgeScenerySeqs : SeqReferences() {
    val stile = find("human_walk_style")
}
