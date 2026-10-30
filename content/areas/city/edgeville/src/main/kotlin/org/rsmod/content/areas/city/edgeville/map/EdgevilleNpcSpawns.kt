package org.rsmod.content.areas.city.edgeville.map

import org.rsmod.api.type.builders.map.npc.MapNpcSpawnBuilder
import org.rsmod.content.areas.city.edgeville.EdgevilleScript

object EdgevilleNpcSpawns : MapNpcSpawnBuilder() {
    override fun onPackMapTask() {
        resourceFile<EdgevilleScript>("npcs.toml")
    }
}
