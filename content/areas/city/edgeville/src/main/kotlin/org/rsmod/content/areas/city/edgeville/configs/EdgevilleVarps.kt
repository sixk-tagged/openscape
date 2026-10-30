package org.rsmod.content.areas.city.edgeville.configs

import org.rsmod.api.type.builders.varp.VarpBuilder
import org.rsmod.api.type.refs.varp.VarpReferences

typealias edgeville_varps = EdgevilleVarps

object EdgevilleVarps : VarpReferences() {
    /** Custom: non-zero once Abbot Langley has let the player join the monastery's order. */
    val monastery_joined = find("monastery_joined")

    /** Custom: bit n is set once the goblin mail crate n + 1 has given the player its armour. */
    val goblin_mail_crates = find("goblin_mail_crates")
}

internal object EdgevilleVarpBuilder : VarpBuilder() {
    init {
        build("monastery_joined")
        build("goblin_mail_crates")
    }
}
