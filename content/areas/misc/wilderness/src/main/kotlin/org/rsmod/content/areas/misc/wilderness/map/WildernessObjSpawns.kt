package org.rsmod.content.areas.misc.wilderness.map

import org.rsmod.api.type.builders.map.obj.MapObjSpawnBuilder
import org.rsmod.content.areas.misc.wilderness.WildernessScript

object WildernessObjSpawns : MapObjSpawnBuilder() {
    override fun onPackMapTask() {
        resourceFile<WildernessScript>("objs.toml")
    }
}
