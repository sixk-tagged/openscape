package org.rsmod.content.skills.runecraft.scripts

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.skills.runecraft.configs.RunecraftLocs
import org.rsmod.content.skills.runecraft.configs.RunecraftNpcs
import org.rsmod.content.skills.runecraft.configs.RunecraftVarps
import org.rsmod.game.entity.player.SessionStateEvent
import org.rsmod.game.type.npc.NpcType

@Execution(ExecutionMode.SAME_THREAD)
class EssenceMineTest {
    @Test
    fun GameTestState.`login completes Rune Mysteries`() =
        runGameTest(EssenceMine::class) {
            player.setVarp(RunecraftVarps.rune_mysteries, 0)
            eventBus.publish(SessionStateEvent.Login(player))
            assertEquals(
                EssenceMine.RUNE_MYSTERIES_DONE,
                player.vars[RunecraftVarps.rune_mysteries],
            )
        }

    @Test
    fun GameTestState.`Aubury's Teleport sends the player to the mine`() =
        runGameTest(EssenceMine::class) {
            val npc = teleporterAt(RunecraftNpcs.aubury)

            player.opNpc4(npc)
            advanceUntil({ player.coords == EssenceMine.ARRIVAL }, timeoutTicks = 10)

            assertEquals(
                EssenceOrigin.Aubury.ordinal,
                player.vars[RunecraftVarps.essence_mine_origin],
            )
            assertEquals(1, player.vars[EssenceOrigin.Aubury.flag])
        }

    @Test
    fun GameTestState.`the other wizards use their third option`() =
        runGameTest(EssenceMine::class) {
            val npc = teleporterAt(RunecraftNpcs.archmage_sedridor)

            player.opNpc3(npc)
            advanceUntil({ player.coords == EssenceMine.ARRIVAL }, timeoutTicks = 10)

            assertEquals(
                EssenceOrigin.Sedridor.ordinal,
                player.vars[RunecraftVarps.essence_mine_origin],
            )
        }

    @Test
    fun GameTestState.`a portal returns the player to whoever sent them`() =
        runGameTest(EssenceMine::class) {
            advance()
            val spot = SPOT
            val portal = placeMapLoc(spot, RunecraftLocs.mine_portal)
            allocZoneCollision(EssenceOrigin.Sedridor.returnTile)
            player.teleport(spot.translateX(-1))
            player.setVarp(RunecraftVarps.essence_mine_origin, EssenceOrigin.Sedridor.ordinal)

            player.opLoc1(portal)
            advance(ticks = 2)

            assertEquals(EssenceOrigin.Sedridor.returnTile, player.coords)
        }

    private fun GameTestScope.teleporterAt(type: NpcType) = run {
        allocZoneCollision(EssenceMine.ARRIVAL)
        advance()
        player.setVarp(RunecraftVarps.rune_mysteries, EssenceMine.RUNE_MYSTERIES_DONE)
        player.teleport(SPOT)
        spawnNpc(SPOT.translateX(1), npcTypes[type])
    }
}
