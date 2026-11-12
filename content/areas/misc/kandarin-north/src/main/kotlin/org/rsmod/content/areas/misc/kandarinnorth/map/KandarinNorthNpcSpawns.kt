package org.rsmod.content.areas.misc.kandarinnorth.map

import org.rsmod.api.type.builders.map.npc.MapNpcSpawnBuilder
import org.rsmod.content.areas.misc.kandarinnorth.KandarinNorthScript

object KandarinNorthNpcSpawns : MapNpcSpawnBuilder() {
    override fun onPackMapTask() {
        resourceFile<KandarinNorthScript>("npcs.toml")
    }
}
