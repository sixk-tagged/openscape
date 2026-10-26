package org.rsmod.content.areas.city.alkharid.map

import org.rsmod.api.type.builders.map.obj.MapObjSpawnBuilder
import org.rsmod.content.areas.city.alkharid.AlKharidScript

object AlKharidObjSpawns : MapObjSpawnBuilder() {
    override fun onPackMapTask() {
        resourceFile<AlKharidScript>("objs.toml")
    }
}
