package org.rsmod.content.areas.city.edgeville.map

import org.rsmod.api.type.builders.map.obj.MapObjSpawnBuilder
import org.rsmod.content.areas.city.edgeville.EdgevilleScript

object EdgevilleObjSpawns : MapObjSpawnBuilder() {
    override fun onPackMapTask() {
        resourceFile<EdgevilleScript>("objs.toml")
    }
}
