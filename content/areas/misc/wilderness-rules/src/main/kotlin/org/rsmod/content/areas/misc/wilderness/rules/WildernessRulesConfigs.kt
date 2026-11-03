package org.rsmod.content.areas.misc.wilderness.rules

import org.rsmod.api.type.builders.varbit.VarBitBuilder
import org.rsmod.api.type.builders.varp.VarpBuilder
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.api.type.refs.varbit.VarBitReferences
import org.rsmod.api.type.refs.varp.VarpReferences

object WildernessLocs : LocReferences() {
    val ditch = find("ditch_wilderness_cover")
    val ditch_members = find("ditch_wilderness_cover_members")
}

object WildernessSeqs : SeqReferences() {
    val ditch_jump = find("wild_ditch_jump")
}

object WildernessVarps : VarpReferences() {
    /** Custom: bit 0 is the ditch warning's "don't ask me again" tick. */
    val flags = find("wilderness_flags")
}

object WildernessVarBits : VarBitReferences() {
    /** Custom: set once the player chose to not be asked again at the ditch. */
    val ditch_warning_off = find("wild_ditch_warning_off")
}

internal object WildernessVarpBuilder : VarpBuilder() {
    init {
        build("wilderness_flags") { permanent = true }
    }
}

internal object WildernessVarBitBuilder : VarBitBuilder() {
    init {
        build("wild_ditch_warning_off") {
            baseVar = WildernessVarps.flags
            startBit = 0
            endBit = 0
        }
    }
}
