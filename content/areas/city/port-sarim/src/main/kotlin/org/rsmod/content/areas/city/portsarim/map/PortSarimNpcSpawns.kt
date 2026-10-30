package org.rsmod.content.areas.city.portsarim.map

import org.rsmod.api.type.builders.map.npc.MapNpcSpawnBuilder
import org.rsmod.content.areas.city.portsarim.PortSarimScript

object PortSarimNpcSpawns : MapNpcSpawnBuilder() {
    override fun onPackMapTask() {
        resourceFile<PortSarimScript>("npcs.toml")
    }
}
