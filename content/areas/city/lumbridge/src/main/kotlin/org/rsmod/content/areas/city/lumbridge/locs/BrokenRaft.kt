package org.rsmod.content.areas.city.lumbridge.locs

import jakarta.inject.Inject
import org.rsmod.api.config.refs.categories
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.quiver
import org.rsmod.api.player.righthand
import org.rsmod.api.player.stat.agilityLvl
import org.rsmod.api.player.stat.rangedLvl
import org.rsmod.api.player.stat.strengthLvl
import org.rsmod.api.script.onApLoc1
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The Broken Raft in the River Lum: members with 8 Agility, 19 Strength and 37 Ranged grapple
 * across with a crossbow and mith grapple worn. No experience; the grapple breaks 1 time in 25.
 */
class BrokenRaft @Inject constructor(private val objTypes: ObjTypeList) : PluginScript() {
    override fun ScriptContext.startup() {
        // The raft sits on a tiny island: Grapple works from the bank, within range.
        onApLoc1(BrokenRaftLocs.raft) { apGrapple(it.loc) }
        onOpLoc1(BrokenRaftLocs.raft) { grapple() }
    }

    private suspend fun ProtectedAccess.apGrapple(raft: BoundLocInfo) {
        if (isWithinApRange(raft, distance = 7)) {
            grapple()
        }
    }

    private suspend fun ProtectedAccess.grapple() {
        // Not on the wiki: OSRS's refusal wording here is undocumented.
        if (!player.members) {
            mes("You need to be on a members' world to use this shortcut.")
            return
        }
        if (player.agilityLvl < 8 || player.strengthLvl < 19 || player.rangedLvl < 37) {
            mes(
                "You need an Agility level of 8, a Strength level of 19 and a Ranged level of " +
                    "37 to do this."
            )
            return
        }
        // Note: the wiki also bars the love crossbow and the ballistae; check their category.
        val crossbow = player.righthand?.let { objTypes[it].isCategoryType(categories.crossbow) }
        if (crossbow != true || player.quiver?.id != BrokenRaftObjs.mith_grapple.id) {
            mes("You need a crossbow and a mith grapple equipped to do this.")
            return
        }
        val dest = if (coords.x < BrokenRaft.RAFT.x) EAST_BANK else WEST_BANK
        faceSquare(BrokenRaft.RAFT)
        anim(BrokenRaftSeqs.fire_and_climb)
        // Note: a telejump, not OSRS's forcemove across the rope; add one if it looks wrong.
        delay(4)
        telejump(dest)
        if (random.randomBoolean(25)) {
            player.quiver = null
            mes(
                "<col=ff0000>After untying the rope, you find that your grapple is broken and useless.</col>"
            )
        }
    }

    companion object {
        val RAFT = CoordGrid(3252, 3179)
        /** The open bank tiles either side, level with the raft. */
        val WEST_BANK = CoordGrid(3246, 3179)
        val EAST_BANK = CoordGrid(3259, 3179)
    }
}

object BrokenRaftLocs : LocReferences() {
    val raft = find("xbows_raft_br")
}

object BrokenRaftObjs : ObjReferences() {
    val mith_grapple = find("xbows_grapple_tip_bolt_mithril_rope")
}

object BrokenRaftSeqs : SeqReferences() {
    val fire_and_climb = find("xbows_human_fire_and_climb_grapple")
}
