package org.rsmod.content.areas.city.lumbridge.locs

import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.midi.MidiReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** "Play" on the church organ: one of its two songs. */
class ChurchOrgan : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1(ChurchOrganLocs.organ) {
            anim(ChurchOrganSeqs.play)
            midiSong(
                if (random.of(maxExclusive = 2) == 0) ChurchOrganMidis.song1
                else ChurchOrganMidis.song2
            )
        }
    }
}

object ChurchOrganLocs : LocReferences() {
    val organ = find("smallchurchorgan")
}

object ChurchOrganSeqs : SeqReferences() {
    val play = find("human_use_chapel_organ")
}

object ChurchOrganMidis : MidiReferences() {
    val song1 = find("church_music_1")
    val song2 = find("church_music_2")
}
