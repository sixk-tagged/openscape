package org.rsmod.content.areas.misc.wilderness.map

import org.rsmod.api.type.builders.map.npc.MapNpcSpawnBuilder
import org.rsmod.content.areas.misc.wilderness.WildernessScript

object WildernessNpcSpawns : MapNpcSpawnBuilder() {
    override fun onPackMapTask() {
        resourceFile<WildernessScript>("npcs.toml")
    }
}
