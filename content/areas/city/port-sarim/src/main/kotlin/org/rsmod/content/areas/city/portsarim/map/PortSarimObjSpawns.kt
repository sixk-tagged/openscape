package org.rsmod.content.areas.city.portsarim.map

import org.rsmod.api.type.builders.map.obj.MapObjSpawnBuilder
import org.rsmod.content.areas.city.portsarim.PortSarimScript

object PortSarimObjSpawns : MapObjSpawnBuilder() {
    override fun onPackMapTask() {
        resourceFile<PortSarimScript>("objs.toml")
    }
}
