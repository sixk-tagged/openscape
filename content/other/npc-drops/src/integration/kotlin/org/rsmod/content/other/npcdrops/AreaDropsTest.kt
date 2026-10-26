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

class AreaDropsTest {
    @Test
    fun GameTestState.`a sewer zombie rolls its fishing bait slot`() =
        runInjectedGameTest(ObjRepository::class, null, *SCRIPTS) { objRepo ->
            player.members = true
            val drops = kill(draynor_npcs.zombie_unarmed_ids.first(), objRepo, ZOMBIE_BAIT_SLOT)
            assertEquals(setOf(drop(objs.bones), drop(drop_objs.fishing_bait, 5)), drops.toSet())
        }

    @Test
    fun GameTestState.`plain skeletons drop only bones`() =
        runInjectedGameTest(ObjRepository::class, null, *SCRIPTS) { objRepo ->
            val drops = kill(draynor_npcs.skeleton_unarmed3, objRepo)
            assertEquals(listOf(drop(objs.bones)), drops)
        }

    @Test
    fun GameTestState.`the black knight's seed slot gives coins on a free world`() =
        runInjectedGameTest(ObjRepository::class, null, *SCRIPTS) { objRepo ->
            player.members = false
            val drops = kill(draynor_npcs.black_knight, objRepo, BLACK_KNIGHT_SEED_SLOT)
            assertEquals(setOf(drop(objs.bones), drop(objs.coins, 5)), drops.toSet())
        }

    @Test
    fun GameTestState.`desert wolves drop wolf bones`() =
        runInjectedGameTest(ObjRepository::class, null, *SCRIPTS) { objRepo ->
            val drops = kill(alkharid_npcs.desert_wolf, objRepo)
            assertEquals(listOf(drop(extra_objs.wolf_bones)), drops)
        }

    @Test
    fun GameTestState.`ugthankis drop bones and raw meat`() =
        runInjectedGameTest(ObjRepository::class, null, *SCRIPTS) { objRepo ->
            val drops = kill(alkharid_npcs.ugthanki, objRepo)
            val expected = setOf(drop(objs.bones), drop(extra_objs.raw_ugthanki_meat))
            assertEquals(expected, drops.toSet())
        }

    @Test
    fun GameTestState.`bears drop bones, fur and meat`() =
        runInjectedGameTest(ObjRepository::class, null, *SCRIPTS) { objRepo ->
            val drops = kill(varrock_npcs.black_bear, objRepo)
            val expected =
                setOf(drop(objs.bones), drop(extra_objs.bear_fur), drop(extra_objs.raw_bear_meat))
            assertEquals(expected, drops.toSet())
        }

    @Test
    fun GameTestState.`moss giants drop big bones and the uncommon seed slot`() =
        runInjectedGameTest(ObjRepository::class, null, *SCRIPTS) { objRepo ->
            player.members = true
            // The items before the seed slot add up to 42; its table's first roll is limpwurt.
            val drops = kill(varrock_npcs.moss_giant, objRepo, MOSS_SEED_SLOT, 0)
            assertEquals(
                setOf(drop(drop_objs.big_bones), drop(extra_objs.limpwurt_seed)),
                drops.toSet(),
            )
        }

    @Test
    fun GameTestState.`the dark wizard's blood rune slot is a wizard hat on a free world`() =
        runInjectedGameTest(ObjRepository::class, null, *SCRIPTS) { objRepo ->
            player.members = false
            // The slots before blood add up to 58 (staff 4, hat 4, robe 3, runes 47).
            val drops = kill(varrock_npcs.dark_wizard3, objRepo, 58)
            assertEquals(setOf(drop(objs.bones), drop(extra_objs.wizard_hat)), drops.toSet())
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
        val SCRIPTS =
            arrayOf(
                NpcHitScript::class,
                NpcDeathScript::class,
                LumbridgeDrops::class,
                DraynorDrops::class,
                AlKharidDrops::class,
                VarrockDrops::class,
            )
        val COORDS = CoordGrid(0, 50, 50, 34, 31)

        // Level 13 zombie: 34 of items, herbs 25, coins 22, then the bait slot (37).
        const val ZOMBIE_BAIT_SLOT = 81
        // Black knight: 26 of items, herbs 3, then the general seed slot.
        const val BLACK_KNIGHT_SEED_SLOT = 29
        const val MOSS_SEED_SLOT = 42
    }
}
