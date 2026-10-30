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

class EdgevilleDropsTest {
    @Test
    fun GameTestState.`a hill giant drops big bones and rolls the giant key first`() =
        runInjectedGameTest(ObjRepository::class, null, *SCRIPTS) { objRepo ->
            // The key pre-roll hits on 0; 127 then falls past the end of the main table.
            val drops = kill(edgeville_drop_npcs.hill_giants.first(), objRepo, 0, 127)
            val expected = setOf(drop(drop_objs.big_bones), drop(edgeville_drop_objs.giant_key))
            assertEquals(expected, drops.toSet())
        }

    @Test
    fun GameTestState.`an icefiend leaves no bones, only a gem on the gem roll`() =
        runInjectedGameTest(ObjRepository::class, null, *SCRIPTS) { objRepo ->
            player.members = true
            // The runes and coins take the first 13; the next 3 are the gem table, then sapphire.
            val drops = kill(edgeville_drop_npcs.icefiend, objRepo, 13, 0)
            assertEquals(listOf(drop(extra_objs.uncut_sapphire)), drops)
        }

    @Test
    fun GameTestState.`the goblin village's red goblins can drop red goblin mail`() =
        runInjectedGameTest(ObjRepository::class, null, *SCRIPTS) { objRepo ->
            player.members = true
            // 107 slots come before the mail: items 29, herbs 2, coins 65, a hammer 9, a book 2.
            val drops = kill(edgeville_drop_npcs.red_goblins.first(), objRepo, 107)
            val expected = setOf(drop(objs.bones), drop(edgeville_drop_objs.red_mail))
            assertEquals(expected, drops.toSet())
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
        val SCRIPTS = arrayOf(NpcHitScript::class, NpcDeathScript::class, EdgevilleDrops::class)
        val COORDS = CoordGrid(0, 50, 50, 34, 31)
    }
}
