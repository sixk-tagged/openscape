package org.rsmod.content.areas.city.draynor.map

import org.rsmod.api.type.builders.map.obj.MapObjSpawnBuilder
import org.rsmod.content.areas.city.draynor.DraynorScript

object DraynorObjSpawns : MapObjSpawnBuilder() {
    override fun onPackMapTask() {
        resourceFile<DraynorScript>("objs.toml")
    }
}
