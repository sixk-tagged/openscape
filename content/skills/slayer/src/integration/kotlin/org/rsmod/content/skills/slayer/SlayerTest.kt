package org.rsmod.content.skills.slayer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.config.refs.varps
import org.rsmod.api.death.NpcKilled
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.skills.slayer.configs.SlayerNpcs
import org.rsmod.content.skills.slayer.configs.SlayerObjs
import org.rsmod.content.skills.slayer.configs.SlayerVarBits
import org.rsmod.content.skills.slayer.configs.SlayerVarps
import org.rsmod.game.entity.Npc
import org.rsmod.game.inv.InvObj
import org.rsmod.map.CoordGrid

@Execution(ExecutionMode.SAME_THREAD)
class SlayerTest {
    @Test
    fun `task points follow the streak multipliers`() {
        assertEquals(0, taskPoints(8, 4))
        assertEquals(8, taskPoints(8, 5))
        assertEquals(40, taskPoints(8, 10))
        assertEquals(120, taskPoints(8, 50))
        assertEquals(200, taskPoints(8, 100))
        assertEquals(280, taskPoints(8, 250))
        assertEquals(400, taskPoints(8, 1000))
        assertEquals(0, taskPoints(0, 10))
    }

    @Test
    fun GameTestState.`slayer monsters carry their task and slayer level`() =
        runGameTest(SlayerScript::class) {
            val crawler = npcTypes.types.getValue(CAVE_CRAWLER_ID)
            assertEquals(CAVE_CRAWLERS, crawler.param(params.slayer_category))
            assertEquals(10, crawler.param(params.slayer_levelrequire))
            val goblin = npcTypes.types.getValue(GOBLIN_ID)
            assertEquals(GOBLINS, goblin.param(params.slayer_category))
            assertEquals(0, goblin.param(params.slayer_levelrequire))
        }

    @Test
    fun GameTestState.`turael assigns a task and hands over the enchanted gem`() =
        runGameTest(SlayerScript::class) {
            player.clearInv()
            val turael = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[SlayerNpcs.turael])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc3(turael)
            talkThrough()

            val count = player.vars[varps.slayer_count]
            assertTrue(count in 10..30) { "count $count" }
            assertEquals(count, player.vars[SlayerVarps.count_original])
            assertEquals(1, player.vars[varbits.slayer_master])
            assertTrue(player.vars[varps.slayer_target] > 0)
            assertEquals(1, player.inv.count(SlayerObjs.enchanted_gem))
        }

    @Test
    fun GameTestState.`vannaka turns away a level 3 player`() =
        runGameTest(SlayerScript::class) {
            val vannaka = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[SlayerNpcs.vannaka])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc3(vannaka)
            talkThrough()

            assertEquals(0, player.vars[varps.slayer_count])
            assertEquals(0, player.vars[varps.slayer_target])
        }

    @Test
    fun GameTestState.`goblin kills count down a goblin task and give slayer xp`() =
        runGameTest(SlayerScript::class) {
            giveTask(master = 1, task = GOBLINS, count = 2)
            val goblin = spawnGoblin()
            val xp = player.statMap.getFineXP(stats.slayer)

            eventBus.publish(NpcKilled(goblin, player))
            assertEquals(1, player.vars[varps.slayer_count])
            assertTrue(player.statMap.getFineXP(stats.slayer) > xp)

            eventBus.publish(NpcKilled(goblin, player))
            assertEquals(0, player.vars[varps.slayer_count])
            assertEquals(1, player.vars[SlayerVarBits.streak])
            assertEquals(0, player.vars[SlayerVarBits.points])
        }

    @Test
    fun GameTestState.`the fifth vannaka task pays 8 points and the tenth 40`() =
        runGameTest(SlayerScript::class) {
            val goblin = spawnGoblin()
            player.withProtectedAccess { vars[SlayerVarBits.streak] = 4 }
            giveTask(master = 3, task = GOBLINS, count = 1)
            eventBus.publish(NpcKilled(goblin, player))
            assertEquals(8, player.vars[SlayerVarBits.points])
            assertEquals(5, player.vars[SlayerVarBits.streak])

            player.withProtectedAccess { vars[SlayerVarBits.streak] = 9 }
            giveTask(master = 3, task = GOBLINS, count = 1)
            eventBus.publish(NpcKilled(goblin, player))
            assertEquals(48, player.vars[SlayerVarBits.points])
        }

    @Test
    fun GameTestState.`a task kill can bring out its superior`() =
        runGameTest(SlayerScript::class) {
            giveTask(master = 1, task = CAVE_CRAWLERS, count = 5)
            // Bigger and Badder is unlock bit 35: bit 3 of the second unlock varp.
            player.withProtectedAccess { vars[SlayerVarps.unlocks1] = 1 shl 3 }
            val crawler =
                spawnNpc(OPEN_GROUND.translateX(4), npcTypes.types.getValue(CAVE_CRAWLER_ID))
            random.next = 0
            eventBus.publish(NpcKilled(crawler, player))
            assertMessageSent("<col=ff0000>A superior foe has appeared...</col>")
            assertTrue(player.vars[SuperiorVarps.alive_until] > 0)
            assertEquals(4, player.vars[varps.slayer_count])
        }

    @Test
    fun GameTestState.`other monsters don't count`() =
        runGameTest(SlayerScript::class) {
            giveTask(master = 1, task = CAVE_CRAWLERS, count = 5)
            eventBus.publish(NpcKilled(spawnGoblin(), player))
            assertEquals(5, player.vars[varps.slayer_count])
        }

    @Test
    fun GameTestState.`krystilia's tasks only count inside the wilderness`() =
        runGameTest(SlayerScript::class) {
            val goblin = spawnGoblin()
            giveTask(master = 7, task = GOBLINS, count = 5)
            eventBus.publish(NpcKilled(goblin, player))
            assertEquals(5, player.vars[varps.slayer_count])

            player.withProtectedAccess { vars[varbits.inside_wilderness] = 1 }
            eventBus.publish(NpcKilled(goblin, player))
            assertEquals(4, player.vars[varps.slayer_count])
        }

    @Test
    fun GameTestState.`the enchanted gem checks the task`() =
        runGameTest(SlayerScript::class) {
            player.clearInv()
            player.inv[0] = InvObj(SlayerObjs.enchanted_gem)
            giveTask(master = 1, task = GOBLINS, count = 7)

            player.withProtectedAccess { opHeld2(0) }
            assertMessageSent("You're assigned to kill goblins; only 7 more to go.")
        }

    @Test
    fun GameTestState.`turael swaps a task he wouldn't give and resets the streak`() =
        runGameTest(SlayerScript::class) {
            player.withProtectedAccess { vars[SlayerVarBits.streak] = 7 }
            giveTask(master = 3, task = ABERRANT_SPECTRES, count = 60)
            val turael = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[SlayerNpcs.turael])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc3(turael)
            talkThrough(1)

            assertEquals(0, player.vars[SlayerVarBits.streak])
            assertEquals(1, player.vars[varbits.slayer_master])
            assertTrue(player.vars[varps.slayer_target] != ABERRANT_SPECTRES)
            assertTrue(player.vars[varps.slayer_count] in 10..30)
        }

    @Test
    fun GameTestState.`turael swapping a krystilia task resets the wilderness streak`() =
        runGameTest(SlayerScript::class) {
            player.withProtectedAccess {
                vars[SlayerVarBits.streak] = 7
                vars[SlayerVarBits.wilderness_streak] = 3
            }
            giveTask(master = 7, task = ABERRANT_SPECTRES, count = 60)
            val turael = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[SlayerNpcs.turael])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc3(turael)
            talkThrough(1)

            assertEquals(7, player.vars[SlayerVarBits.streak])
            assertEquals(0, player.vars[SlayerVarBits.wilderness_streak])
            assertEquals(1, player.vars[varbits.slayer_master])
        }

    @Test
    fun GameTestState.`spria's trade opens the slayer equipment shop`() =
        runGameTest(SlayerScript::class) {
            val spria = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[SlayerNpcs.spria])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc4(spria)
            advanceUntil(max = 10) { player.openedShop != null }

            val shop = checkNotNull(player.openedShop)
            assertEquals(50, shop.inv.count(SlayerObjs.enchanted_gem))
        }

    private fun GameTestScope.giveTask(master: Int, task: Int, count: Int) {
        player.withProtectedAccess {
            vars[varbits.slayer_master] = master
            vars[varps.slayer_target] = task
            vars[varps.slayer_count] = count
            vars[SlayerVarps.count_original] = count
        }
    }

    private fun GameTestScope.spawnGoblin(): Npc =
        spawnNpc(OPEN_GROUND.translateX(4), npcTypes.types.getValue(GOBLIN_ID))

    private companion object {
        const val GOBLINS = 2
        const val CAVE_CRAWLERS = 37
        const val ABERRANT_SPECTRES = 41

        // Cache ids (npc.sym): `goblin` and `slayer_cave_crawler_1`.
        const val GOBLIN_ID = 3028
        const val CAVE_CRAWLER_ID = 406

        // The Lumbridge spawn tile: open ground.
        val OPEN_GROUND = CoordGrid(0, 50, 50, 30, 18)
    }
}
