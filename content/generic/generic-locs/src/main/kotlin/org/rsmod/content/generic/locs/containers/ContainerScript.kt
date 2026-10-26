package org.rsmod.content.generic.locs.containers

import jakarta.inject.Inject
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLoc2
import org.rsmod.api.script.onOpLoc3
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.type.loc.LocType
import org.rsmod.game.type.seq.SeqType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** Chests and drawers: Open swaps in the open type, which can be searched (empty) or shut. */
class ContainerScript @Inject constructor(private val locRepo: LocRepository) : PluginScript() {
    override fun ScriptContext.startup() {
        container(ContainerLocs.chest, ContainerLocs.chest_open, "chest")
        container(ContainerLocs.drawers, ContainerLocs.drawers_open, "drawers")
        // Note: OSRS only opens Sigmund's chest with his key during The Lost Tribe; the
        // locked message is unverified.
        onOpLoc1(ContainerLocs.lost_tribe_chest) { mes("This chest is locked.") }
    }

    private fun ScriptContext.container(closed: LocType, open: LocType, noun: String) {
        onOpLoc1(closed) { change(it.loc, open, ContainerSeqs.open) }
        onOpLoc2(open) { mes("You search the $noun but find nothing.") }
        onOpLoc3(open) { change(it.loc, closed, ContainerSeqs.close) }
    }

    private suspend fun ProtectedAccess.change(loc: BoundLocInfo, into: LocType, seq: SeqType) {
        arriveDelay()
        anim(seq)
        locRepo.change(loc, into, DURATION)
    }

    private companion object {
        // Note: same revert time as doors; the OSRS timing is unverified.
        const val DURATION = 500
    }
}

object ContainerLocs : LocReferences() {
    val chest = find("chestclosed")
    val chest_open = find("chestopen")
    val drawers = find("drawers4")
    val drawers_open = find("drawers4open")
    val lost_tribe_chest = find("lost_tribe_chest")
}

// Note: drawers reuse the chest anims; OSRS may use the cupboard ones.
object ContainerSeqs : SeqReferences() {
    val open = find("human_openchest")
    val close = find("human_closechest")
}
