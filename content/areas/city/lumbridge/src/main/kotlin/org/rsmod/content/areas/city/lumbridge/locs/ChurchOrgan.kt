package org.rsmod.content.areas.city.lumbridge.locs

import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** "Play" on the church organ. */
class ChurchOrgan : PluginScript() {
    override fun ScriptContext.startup() {
        // Note: OSRS also plays Organ Music 1 or 2; there's no midi helper for those yet.
        onOpLoc1(ChurchOrganLocs.organ) { anim(ChurchOrganSeqs.play) }
    }
}

object ChurchOrganLocs : LocReferences() {
    val organ = find("smallchurchorgan")
}

object ChurchOrganSeqs : SeqReferences() {
    val play = find("human_use_chapel_organ")
}
