package org.rsmod.content.areas.city.varrock.map

import org.rsmod.api.type.builders.map.npc.MapNpcSpawnBuilder
import org.rsmod.content.areas.city.varrock.VarrockScript

object VarrockNpcSpawns : MapNpcSpawnBuilder() {
    override fun onPackMapTask() {
        resourceFile<VarrockScript>("npcs.toml")
    }
}
