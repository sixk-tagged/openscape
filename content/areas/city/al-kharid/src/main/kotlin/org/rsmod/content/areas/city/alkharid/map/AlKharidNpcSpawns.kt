package org.rsmod.content.areas.city.alkharid.map

import org.rsmod.api.type.builders.map.npc.MapNpcSpawnBuilder
import org.rsmod.content.areas.city.alkharid.AlKharidScript

object AlKharidNpcSpawns : MapNpcSpawnBuilder() {
    override fun onPackMapTask() {
        resourceFile<AlKharidScript>("npcs.toml")
    }
}
