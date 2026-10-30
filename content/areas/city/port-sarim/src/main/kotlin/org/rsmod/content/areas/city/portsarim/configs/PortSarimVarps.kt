package org.rsmod.content.areas.city.portsarim.configs

import org.rsmod.api.type.builders.varp.VarpBuilder
import org.rsmod.api.type.refs.varp.VarpReferences

typealias port_sarim_varps = PortSarimVarps

object PortSarimVarps : VarpReferences() {
    /** Custom: non-zero once Luthas has given the player the banana crate job. */
    val banana_job = find("banana_job")

    /** Custom: how many bananas the plantation's export crate holds (it takes 10). */
    val banana_crate = find("banana_crate")
}

internal object PortSarimVarpBuilder : VarpBuilder() {
    init {
        build("banana_job")
        build("banana_crate")
    }
}
