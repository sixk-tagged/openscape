package org.rsmod.content.areas.city.varrock.map

import org.rsmod.api.type.builders.map.obj.MapObjSpawnBuilder
import org.rsmod.content.areas.city.varrock.VarrockScript

object VarrockObjSpawns : MapObjSpawnBuilder() {
    override fun onPackMapTask() {
        resourceFile<VarrockScript>("objs.toml")
    }
}
