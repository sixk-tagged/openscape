package org.rsmod.content.other.npcdrops

import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.rsmod.annotations.InternalApi
import org.rsmod.api.config.refs.areas
import org.rsmod.api.config.refs.objs
import org.rsmod.api.death.plugin.NpcDeathScript
import org.rsmod.api.hit.plugin.NpcHitScript
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.game.area.AreaIndex
import org.rsmod.game.hit.HitType
import org.rsmod.map.CoordGrid

object BagRuleNpcs : NpcReferences() {
    val hill_giant = find("giant")
}

class WildernessBagRuleTest {
    class Deps @Inject constructor(val objRepo: ObjRepository, val areaIndex: AreaIndex)

    @Test
    fun GameTestState.`a monster killed in the Wilderness can drop the looting bag`() =
        runInjectedGameTest(Deps::class, null, *SCRIPTS) { deps ->
            deps.wilderness(INSIDE)
            // 1 in 5 for a level 28 monster: a roll of 0 lands it.
            val drops = kill(deps, INSIDE, roll = 0)
            assertEquals(setOf(objs.bones.id, wilderness_drop_objs.looting_bag.id), drops)
        }

    @Test
    fun GameTestState.`the same kill outside the Wilderness never rolls it`() =
        runInjectedGameTest(Deps::class, null, *SCRIPTS) { deps ->
            assertEquals(setOf(objs.bones.id), kill(deps, OUTSIDE, roll = 0))
        }

    @Test
    fun GameTestState.`a missed roll in the Wilderness drops no bag`() =
        runInjectedGameTest(Deps::class, null, *SCRIPTS) { deps ->
            deps.wilderness(INSIDE)
            assertEquals(setOf(objs.bones.id), kill(deps, INSIDE, roll = 4))
        }

    /** Kills a hill giant at [coords] with its looting bag roll pinned to [roll]. */
    private fun GameTestScope.kill(deps: Deps, coords: CoordGrid, roll: Int): Set<Int> {
        // A fresh test player's clock reads -1 until their first tick.
        advance()
        player.observerUUID = player.uuid
        val npc = spawnNpc(coords, npcTypes[BagRuleNpcs.hill_giant])
        player.teleport(coords.translateX(-1))
        npc.heroPoints(player, npc.hitpoints)
        npc.queueHit(player, delay = 1, HitType.Melee, damage = npc.hitpoints)
        advanceUntil({ npc.hitpoints == 0 }, timeoutTicks = 5)
        random.next = roll
        advanceUntil({ deps.objRepo.findAll(npc.coords).any() }, timeoutTicks = 20)
        return deps.objRepo.findAll(npc.coords).map { it.type }.toSet()
    }

    /** Puts the tiles around [centre] in the `wilderness` area, in case the giant wanders. */
    @OptIn(InternalApi::class)
    private fun Deps.wilderness(centre: CoordGrid) {
        for (dx in -4..4) {
            for (dz in -4..4) {
                val tile = centre.translate(dx, dz)
                areaIndex.registerAll(tile, listOf(areas.wilderness.id.toShort()).iterator())
            }
        }
    }

    private companion object {
        val SCRIPTS = arrayOf(NpcHitScript::class, NpcDeathScript::class, WildernessBagRule::class)

        val INSIDE = CoordGrid(3000, 3600)
        val OUTSIDE = CoordGrid(3000, 3500)
    }
}
