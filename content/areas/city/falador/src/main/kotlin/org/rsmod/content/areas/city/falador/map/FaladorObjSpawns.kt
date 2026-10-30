package org.rsmod.content.areas.city.falador.map

import org.rsmod.api.type.builders.map.obj.MapObjSpawnBuilder
import org.rsmod.content.areas.city.falador.FaladorScript

object FaladorObjSpawns : MapObjSpawnBuilder() {
    override fun onPackMapTask() {
        resourceFile<FaladorScript>("objs.toml")
    }
}
