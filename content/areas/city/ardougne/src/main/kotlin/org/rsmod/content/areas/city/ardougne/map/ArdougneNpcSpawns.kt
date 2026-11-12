package org.rsmod.content.areas.city.ardougne.map

import org.rsmod.api.type.builders.map.npc.MapNpcSpawnBuilder
import org.rsmod.content.areas.city.ardougne.ArdougneScript

object ArdougneNpcSpawns : MapNpcSpawnBuilder() {
    override fun onPackMapTask() {
        resourceFile<ArdougneScript>("npcs.toml")
    }
}
