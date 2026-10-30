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

class PortSarimDropsTest {
    @Test
    fun GameTestState.`a rimmington hobgoblin can drop goblin mail`() =
        runInjectedGameTest(ObjRepository::class, null, *SCRIPTS) { objRepo ->
            player.members = true
            // Items 14, runes 14, coins 48 and limpwurt roots 22 come before the mail.
            val drops = kill(port_sarim_drop_npcs.hobgoblins.first(), objRepo, 98)
            val expected = setOf(drop(objs.bones), drop(drop_objs.goblin_mail))
            assertEquals(expected, drops.toSet())
        }

    @Test
    fun GameTestState.`the jail pirate can drop a right eye patch on a members world`() =
        runInjectedGameTest(ObjRepository::class, null, *SCRIPTS) { objRepo ->
            player.members = true
            // 105 slots of items, runes and coins come before the eye patch.
            val drops = kill(port_sarim_drop_npcs.pirate, objRepo, 105)
            val expected = setOf(drop(objs.bones), drop(port_sarim_drop_objs.eye_patch))
            assertEquals(expected, drops.toSet())
        }

    @Test
    fun GameTestState.`a monkey drops monkey bones`() =
        runInjectedGameTest(ObjRepository::class, null, *SCRIPTS) { objRepo ->
            val drops = kill(port_sarim_drop_npcs.monkey, objRepo)
            assertEquals(listOf(drop(port_sarim_drop_objs.monkey_bones)), drops)
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
        val SCRIPTS = arrayOf(NpcHitScript::class, NpcDeathScript::class, PortSarimDrops::class)
        val COORDS = CoordGrid(0, 50, 50, 34, 31)
    }
}
