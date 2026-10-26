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

class LumbridgeDropsTest {
    @Test
    fun GameTestState.`cows always drop bones, cowhide and raw beef`() =
        runInjectedGameTest(ObjRepository::class, null, *SCRIPTS) { objRepo ->
            val drops = kill(drop_npcs.cow, objRepo)
            val expected =
                setOf(drop(objs.bones), drop(drop_objs.cowhide), drop(drop_objs.raw_beef))
            assertEquals(expected, drops.toSet())
        }

    @Test
    fun GameTestState.`a free world gets 10 coins in the herb slot`() =
        runInjectedGameTest(ObjRepository::class, null, *SCRIPTS) { objRepo ->
            player.members = false
            val drops = kill(drop_npcs.man, objRepo, HERB_SLOT)
            assertEquals(setOf(drop(objs.bones), drop(objs.coins, 10)), drops.toSet())
        }

    @Test
    fun GameTestState.`a members world rolls the herb table`() =
        runInjectedGameTest(ObjRepository::class, null, *SCRIPTS) { objRepo ->
            player.members = true
            val drops = kill(drop_npcs.man, objRepo, HERB_SLOT, 0)
            assertEquals(setOf(drop(objs.bones), drop(drop_objs.grimy_guam)), drops.toSet())
        }

    @Test
    fun GameTestState.`members-only drops give nothing on a free world`() =
        runInjectedGameTest(ObjRepository::class, null, *SCRIPTS) { objRepo ->
            player.members = false
            val drops = kill(drop_npcs.man, objRepo, BOLTS_SLOT)
            assertEquals(listOf(drop(objs.bones)), drops)
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
        val SCRIPTS = arrayOf(NpcHitScript::class, NpcDeathScript::class, LumbridgeDrops::class)
        val COORDS = CoordGrid(0, 50, 50, 34, 31)

        // Man table: helm 2, dagger 1, bolts 22, arrows 3, earth 2, fire 2, mind 2, chaos 1 = 35.
        const val BOLTS_SLOT = 3
        const val HERB_SLOT = 35
    }
}
