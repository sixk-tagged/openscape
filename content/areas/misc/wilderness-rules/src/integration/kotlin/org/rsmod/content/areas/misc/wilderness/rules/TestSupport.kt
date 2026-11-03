package org.rsmod.content.areas.misc.wilderness.rules

import jakarta.inject.Inject
import kotlin.reflect.KClass
import org.rsmod.annotations.InternalApi
import org.rsmod.api.config.refs.areas
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.area.AreaIndex
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript

class WildDeps @Inject constructor(val areaIndex: AreaIndex, val objRepo: ObjRepository)

/** Runs [test] with the given [scripts] and the area index the packed map would fill. */
internal fun GameTestState.wildernessTest(
    vararg scripts: KClass<out PluginScript>,
    test: GameTestScope.(WildDeps) -> Unit,
) = runInjectedGameTest(WildDeps::class, null, *scripts, testBody = test)

/** Puts [coord] in the `wilderness` area, as the packed map does for the real tiles. */
@OptIn(InternalApi::class)
internal fun WildDeps.wilderness(coord: CoordGrid) {
    check(WildernessBounds.contains(coord.x, coord.z)) { "Not in the Wilderness: $coord" }
    areaIndex.registerAll(coord, listOf(areas.wilderness.id.toShort()).iterator())
}
