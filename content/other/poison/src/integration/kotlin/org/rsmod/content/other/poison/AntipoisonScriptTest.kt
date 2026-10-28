package org.rsmod.content.other.poison

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.combat.commons.poison.isPoisoned
import org.rsmod.api.combat.commons.poison.poison
import org.rsmod.api.combat.commons.poison.poisonState
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.player.stat.stat
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.other.consumables.configs.ConsumableObjs
import org.rsmod.game.inv.InvObj
import org.rsmod.game.inv.isType
import org.rsmod.game.type.obj.ObjType

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class AntipoisonScriptTest {
    @Test
    fun GameTestState.`superantipoison cures poison and gives 6 minutes of immunity`() =
        runGameTest(AntipoisonScript::class) {
            setUp(AntipoisonObjs.super4)
            player.poison(15)
            player.withProtectedAccess { opHeld1(0) }
            assertFalse(player.isPoisoned)
            assertEquals(-20, player.poisonState)
            assertFalse(player.poison(15))
            assertTrue(player.inv[0].isType(AntipoisonObjs.super3))
            assertMessagesSent(
                "You drink some of your superantipoison potion.",
                "You have 3 doses of potion left.",
            )
        }

    @Test
    fun GameTestState.`sanfew serum cures poison, restores stats but not hitpoints`() =
        runGameTest(AntipoisonScript::class) {
            setUp(AntipoisonObjs.sanfew4)
            for (stat in listOf(stats.attack, stats.prayer, stats.hitpoints)) {
                player.stats[stat] = 99
                player.setCurrentLevel(stat, 50)
            }
            player.stats[stats.strength] = 99
            player.setCurrentLevel(stats.strength, 90)
            player.poison(15)
            player.withProtectedAccess { opHeld1(0) }
            // 4 + 30% of the base level 99.
            assertEquals(50 + 33, player.stat(stats.attack))
            assertEquals(50 + 33, player.stat(stats.prayer))
            assertEquals(50, player.hitpoints)
            // Never past the base level.
            assertEquals(99, player.stat(stats.strength))
            assertFalse(player.isPoisoned)
            assertEquals(-20, player.poisonState)
            assertTrue(player.inv[0].isType(AntipoisonObjs.sanfew3))
            assertMessagesSent(
                "You drink some of your Sanfew serum.",
                "You have 3 doses of potion left.",
            )
        }

    @Test
    fun GameTestState.`each potion gives its own immunity`() =
        runGameTest(AntipoisonScript::class) {
            val immunities =
                mapOf(
                    AntipoisonObjs.antipoison4 to -5,
                    AntipoisonObjs.antidote_plus4 to -30,
                    AntipoisonObjs.antidote_plusplus4 to -40,
                )
            for ((potion, state) in immunities) {
                setUp(potion)
                player.poisonState = 0
                player.withProtectedAccess { opHeld1(0) }
                assertEquals(state, player.poisonState)
                advance(ticks = 3)
            }
        }

    @Test
    fun GameTestState.`the last dose leaves an empty vial`() =
        runGameTest(AntipoisonScript::class) {
            setUp(AntipoisonObjs.antipoison1)
            player.withProtectedAccess { opHeld1(0) }
            assertTrue(player.inv[0].isType(ConsumableObjs.vial))
            assertMessageSent("You have finished your potion.")
        }

    @Test
    fun GameTestState.`potions share a 3 tick delay`() =
        runGameTest(AntipoisonScript::class) {
            setUp(AntipoisonObjs.antipoison4, AntipoisonObjs.antipoison4)
            player.withProtectedAccess { opHeld1(0) }
            player.withProtectedAccess { opHeld1(1) }
            assertNotNull(player.inv[1])
            assertTrue(player.inv[1].isType(AntipoisonObjs.antipoison4))
            advance(ticks = 3)
            player.withProtectedAccess { opHeld1(1) }
            assertTrue(player.inv[1].isType(AntipoisonObjs.antipoison3))
        }

    private fun GameTestScope.setUp(vararg objs: ObjType) {
        // A fresh test player's clock reads -1 until their first tick.
        advance()
        player.clearInv()
        objs.forEachIndexed { slot, obj -> player.inv[slot] = InvObj(obj) }
    }
}
