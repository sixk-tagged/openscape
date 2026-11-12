package org.rsmod.content.areas.misc.kandarinnorth.configs

import org.rsmod.api.type.builders.varp.VarpBuilder
import org.rsmod.api.type.refs.varp.VarpReferences

typealias kandarin_north_varps = KandarinNorthVarps

object KandarinNorthVarps : VarpReferences() {
    /** The coal in the coal trucks, which they all share. */
    val coal_truck_store = find("coal_truck_store")
}

internal object KandarinNorthVarpBuilder : VarpBuilder() {
    init {
        build("coal_truck_store")
    }
}
