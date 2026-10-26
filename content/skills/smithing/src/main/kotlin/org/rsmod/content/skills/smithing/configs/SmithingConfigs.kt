package org.rsmod.content.skills.smithing.configs

import org.rsmod.api.type.builders.varp.VarpBuilder
import org.rsmod.api.type.editors.loc.LocEditor

internal object SmithingLocEditor : LocEditor() {
    init {
        edit(SmithingLocs.furnace) { contentGroup = SmithingContent.furnace }
        edit(SmithingLocs.lumbridge_furnace) { contentGroup = SmithingContent.furnace }
        edit(SmithingLocs.anvil) { contentGroup = SmithingContent.anvil }
    }
}

internal object SmithingVarpBuilder : VarpBuilder() {
    init {
        build("ring_of_forging_charges")
    }
}
