package org.rsmod.content.areas.city.falador.map

import org.rsmod.api.type.builders.map.npc.MapNpcSpawnBuilder
import org.rsmod.content.areas.city.falador.FaladorScript

object FaladorNpcSpawns : MapNpcSpawnBuilder() {
    override fun onPackMapTask() {
        resourceFile<FaladorScript>("npcs.toml")
    }
}
