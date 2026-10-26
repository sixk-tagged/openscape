package org.rsmod.content.skills.mining.configs

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.stats
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.assertions.assertNotNullContract
import org.rsmod.content.skills.mining.scripts.Mining

class MiningConfigTest {
    @Test
    fun GameTestState.`every pickaxe has level, animation and speed`() = runBasicGameTest {
        val pickaxes = cacheTypes.objs.values.filter { it.isContentType(MiningContent.pickaxe) }
        assertEquals(12, pickaxes.size)
        for (pickaxe in pickaxes) {
            val pickaxeParams = pickaxe.paramMap
            assertNotNullContract(pickaxeParams)
            assertTrue(params.levelrequire in pickaxeParams)
            assertTrue(params.skill_anim in pickaxeParams)
            assertTrue(MiningParams.pickaxe_ticks in pickaxeParams)
        }
    }

    @Test
    fun GameTestState.`every rock has its mining params`() = runBasicGameTest {
        val rocks = cacheTypes.locs.values.filter { it.isContentType(MiningContent.rock) }
        // 50 loc types: 29 ores x 2, minus the 9 ores whose two rocks share one loc type
        // (essence, ash, sandstone, granite, basalt, te/efh/urt salt, ancient essence), plus the
        // Rune Essence Mine's own rock.
        assertEquals(50, rocks.size)
        for (rock in rocks) {
            val rockParams = rock.paramMap
            assertNotNullContract(rockParams)
            assertTrue(params.levelrequire in rockParams)
            assertTrue(params.skill_xp in rockParams)
            assertTrue(params.skill_productitem in rockParams)
            assertTrue(params.next_loc_stage in rockParams)
            assertTrue(params.respawn_time in rockParams)
            assertTrue(params.deplete_chance in rockParams)
            assertTrue(MiningParams.success_rate in rockParams)
            val (low, high) = Mining.successRate(rock)
            assertTrue(low in 0..255 && high in 1..1023) { "Bad success rate on $rock" }
        }
    }

    @Test
    fun GameTestState.`depleted rocks are marked`() = runBasicGameTest {
        val depleted =
            cacheTypes.locs.values.filter { it.isContentType(MiningContent.depleted_rock) }
        assertEquals(10, depleted.size)
    }

    @Test
    fun GameTestState.`only golden nuggets are tradeable`() = runBasicGameTest {
        assertTrue(cacheTypes.objs[MiningObjs.golden_nugget].tradeable)
        val untradeable =
            listOf(
                MiningObjs.prospector_helmet,
                MiningObjs.prospector_jacket,
                MiningObjs.prospector_legs,
                MiningObjs.prospector_boots,
                MiningObjs.mining_gloves,
                MiningObjs.superior_mining_gloves,
                MiningObjs.expert_mining_gloves,
            )
        for (obj in untradeable) {
            assertFalse(cacheTypes.objs[obj].tradeable) { "$obj should be untradeable" }
        }
    }

    @Test
    fun GameTestState.`mining gloves need mining levels to wear`() = runBasicGameTest {
        val expected =
            mapOf(
                MiningObjs.mining_gloves to 20,
                MiningObjs.superior_mining_gloves to 55,
                MiningObjs.expert_mining_gloves to 70,
            )
        for ((gloves, level) in expected) {
            val type = cacheTypes.objs[gloves]
            assertEquals(stats.mining, type.param(params.statreq1_skill))
            assertEquals(level, type.param(params.statreq1_level))
        }
    }
}
