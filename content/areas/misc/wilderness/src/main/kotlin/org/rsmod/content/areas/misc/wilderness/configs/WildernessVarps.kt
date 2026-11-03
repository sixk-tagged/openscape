package org.rsmod.content.areas.misc.wilderness.configs

import org.rsmod.api.type.builders.varp.VarpBuilder
import org.rsmod.api.type.refs.varp.VarpReferences

typealias wilderness_varps = WildernessVarps

object WildernessVarps : VarpReferences() {
    /** Custom: non-zero once the player has paid Mandrith for the Resource Area (not saved). */
    val resource_area_paid = find("resource_area_paid")
}

internal object WildernessVarpBuilder : VarpBuilder() {
    init {
        build("resource_area_paid") { temporary = true }
    }
}
