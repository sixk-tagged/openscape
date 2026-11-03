package org.rsmod.content.other.npcdrops

import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.objs
import org.rsmod.api.death.plugin.NpcDeathScript
import org.rsmod.api.hit.plugin.NpcHitScript
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.hit.HitType
import org.rsmod.game.type.npc.NpcType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.map.CoordGrid

class WildernessDropsTest {
    @Test
    fun GameTestState.`a green dragon drops bones, hide, a looting bag and the first line`() =
        runInjectedGameTest(ObjRepository::class, null, *SCRIPTS) { objRepo ->
            // The bag is the first independent roll (a hit on 0); then the main table's first line.
            val drops = kill(wilderness_drop_npcs.green_dragons.first(), objRepo, 0, 0)
            val expected =
                setOf(
                    drop(wilderness_drop_objs.dragon_bones),
                    drop(wilderness_drop_objs.dragonhide_green),
                    drop(wilderness_drop_objs.looting_bag),
                    drop(wilderness_drop_objs.steel_platelegs),
                )
            assertEquals(expected, drops.toSet())
        }

    @Test
    fun GameTestState.`a thug drops bones and only rolls the looting bag when the roll lands`() =
        runInjectedGameTest(ObjRepository::class, null, *SCRIPTS) { objRepo ->
            val thug = wilderness_drop_npcs.thugs.first()
            // A miss on the bag (1 in 15), then a roll past the end of the main table.
            val missed = kill(thug, objRepo, 1, 127)
            assertEquals(setOf(drop(objs.bones)), missed.toSet())
        }

    /** Kills a fresh [type] with [rolls] queued for its drop table; returns (obj id, count)s. */
    private fun GameTestScope.kill(
        type: NpcType,
        objRepo: ObjRepository,
        vararg rolls: Int,
    ): List<Pair<Int, Int>> {
        // A fresh test player's clock reads -1 until their first tick.
        advance()
        // Loot is owned by the killer's observer id, which a test player only gets if we set it.
        player.observerUUID = player.uuid
        val npc = spawnNpc(COORDS, npcTypes[type])
        player.teleport(COORDS.translateX(-1))
        // Raw test hits skip the combat manager, which is what normally credits the killer.
        npc.heroPoints(player, npc.hitpoints)
        npc.queueHit(player, delay = 1, HitType.Melee, damage = npc.hitpoints)
        advanceUntil({ npc.hitpoints == 0 }, timeoutTicks = 5)
        // Queued only now so nothing before the death sequence (e.g. wandering) eats the rolls.
        rolls.forEach { random.next = it }
        // Drops land where the npc died, which can be a tile or two from its spawn if it wandered.
        advanceUntil({ objRepo.findAll(npc.coords).any() }, timeoutTicks = 20)
        return objRepo.findAll(npc.coords).map { it.type to it.count }.toList()
    }

    private fun drop(obj: ObjType, count: Int = 1): Pair<Int, Int> = obj.id to count

    private companion object {
        val SCRIPTS = arrayOf(NpcHitScript::class, NpcDeathScript::class, WildernessDrops::class)
        val COORDS = CoordGrid(0, 50, 50, 34, 31)
    }
}
