package org.rsmod.content.areas.dungeon.swampcaves

import jakarta.inject.Inject
import org.rsmod.annotations.InternalApi
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.area.AreaIndex
import org.rsmod.map.CoordGrid

class CaveDeps @Inject constructor(val areaIndex: AreaIndex)

/** Runs [test] with the cave script and the area index the packed map would fill. */
internal fun GameTestState.caveTest(test: GameTestScope.(CaveDeps) -> Unit) =
    runInjectedGameTest(CaveDeps::class, null, SwampCaveScript::class, testBody = test)

/** Puts [coord] in the caves' area, as the packed map does for the real tiles. */
@OptIn(InternalApi::class)
internal fun CaveDeps.inCaves(coord: CoordGrid) {
    areaIndex.registerAll(coord, listOf(SwampCaveAreas.caves.id.toShort()).iterator())
}
