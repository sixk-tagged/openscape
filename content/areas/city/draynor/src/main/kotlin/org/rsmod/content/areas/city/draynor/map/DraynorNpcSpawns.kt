package org.rsmod.content.areas.city.draynor.map

import org.rsmod.api.type.builders.map.npc.MapNpcSpawnBuilder
import org.rsmod.content.areas.city.draynor.DraynorScript

object DraynorNpcSpawns : MapNpcSpawnBuilder() {
    override fun onPackMapTask() {
        resourceFile<DraynorScript>("npcs.toml")
    }
}
