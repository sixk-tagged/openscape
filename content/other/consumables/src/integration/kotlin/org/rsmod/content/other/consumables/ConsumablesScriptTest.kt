package org.rsmod.content.other.consumables

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
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
class ConsumablesScriptTest {
    @Test
    fun GameTestState.`shark heals 20 and is used up`() =
        runGameTest(ConsumablesScript::class) {
            setUp(hitpoints = 50, ConsumableObjs.shark)
            player.withProtectedAccess { opHeld1(0) }
            assertEquals(70, player.hitpoints)
            assertNull(player.inv[0])
            assertMessagesSent("You eat the shark.", "It heals some health.")
        }

    @Test
    fun GameTestState.`cooked shrimps heal 3`() =
        runGameTest(ConsumablesScript::class) {
            setUp(hitpoints = 50, ConsumableObjs.shrimps)
            player.withProtectedAccess { opHeld1(0) }
            assertEquals(53, player.hitpoints)
            assertMessagesSent("You eat the shrimps.", "It heals some health.")
        }

    @Test
    fun GameTestState.`food delay blocks a second bite for 3 ticks`() =
        runGameTest(ConsumablesScript::class) {
            setUp(hitpoints = 50, ConsumableObjs.shark, ConsumableObjs.shark)
            player.withProtectedAccess { opHeld1(0) }
            player.withProtectedAccess { opHeld1(1) }
            assertEquals(70, player.hitpoints)
            assertNotNull(player.inv[1])
            advance(ticks = 3)
            player.withProtectedAccess { opHeld1(1) }
            assertEquals(90, player.hitpoints)
        }

    @Test
    fun GameTestState.`karambwan combo-eats in the same tick as food`() =
        runGameTest(ConsumablesScript::class) {
            setUp(hitpoints = 50, ConsumableObjs.shark, ConsumableObjs.karambwan)
            player.withProtectedAccess { opHeld1(0) }
            player.withProtectedAccess { opHeld1(1) }
            assertEquals(88, player.hitpoints)
        }

    @Test
    fun GameTestState.`eating delays the next attack`() =
        runGameTest(ConsumablesScript::class) {
            setUp(hitpoints = 50, ConsumableObjs.shark)
            player.withProtectedAccess { opHeld1(0) }
            assertEquals(player.currentMapClock + 3, player.actionDelay)
        }

    @Test
    fun GameTestState.`karambwan delays the next attack by 2`() =
        runGameTest(ConsumablesScript::class) {
            setUp(hitpoints = 50, ConsumableObjs.karambwan)
            player.withProtectedAccess { opHeld1(0) }
            assertEquals(player.currentMapClock + 2, player.actionDelay)
        }

    @Test
    fun GameTestState.`anglerfish overheals to 121 at 99 hitpoints`() =
        runGameTest(ConsumablesScript::class) {
            setUp(hitpoints = 99, ConsumableObjs.anglerfish)
            player.withProtectedAccess { opHeld1(0) }
            assertEquals(121, player.hitpoints)
        }

    @Test
    fun GameTestState.`a dying player cannot eat`() =
        runGameTest(ConsumablesScript::class) {
            setUp(hitpoints = 0, ConsumableObjs.shark)
            player.withProtectedAccess { opHeld1(0) }
            assertEquals(0, player.hitpoints)
            assertNotNull(player.inv[0])
        }

    @Test
    fun GameTestState.`super combat boosts melee stats and leaves 3 doses`() =
        runGameTest(ConsumablesScript::class) {
            setUp(hitpoints = 99, ConsumableObjs.super_combat4)
            player.withProtectedAccess { opHeld1(0) }
            assertEquals(118, player.stat(stats.attack))
            assertEquals(118, player.stat(stats.strength))
            assertEquals(118, player.stat(stats.defence))
            assertTrue(player.inv[0].isType(ConsumableObjs.super_combat3))
            assertMessagesSent(
                "You drink some of your super combat potion.",
                "You have 3 doses of potion left.",
            )
        }

    @Test
    fun GameTestState.`the last dose leaves an empty vial`() =
        runGameTest(ConsumablesScript::class) {
            setUp(hitpoints = 99, ConsumableObjs.ranging1)
            player.withProtectedAccess { opHeld1(0) }
            assertEquals(112, player.stat(stats.ranged))
            assertTrue(player.inv[0].isType(ConsumableObjs.vial))
            assertMessageSent("You have finished your potion.")
        }

    @Test
    fun GameTestState.`saradomin brew heals, boosts defence and drains offence`() =
        runGameTest(ConsumablesScript::class) {
            setUp(hitpoints = 50, ConsumableObjs.brew4)
            player.withProtectedAccess { opHeld1(0) }
            assertEquals(66, player.hitpoints)
            assertEquals(120, player.stat(stats.defence))
            assertEquals(88, player.stat(stats.attack))
            assertEquals(88, player.stat(stats.strength))
            assertEquals(88, player.stat(stats.ranged))
            assertEquals(88, player.stat(stats.magic))
        }

    @Test
    fun GameTestState.`super restore restores drained stats and prayer`() =
        runGameTest(ConsumablesScript::class) {
            setUp(hitpoints = 50, ConsumableObjs.restore4)
            player.setCurrentLevel(stats.attack, 50)
            player.setCurrentLevel(stats.prayer, 10)
            player.withProtectedAccess { opHeld1(0) }
            assertEquals(82, player.stat(stats.attack))
            assertEquals(42, player.stat(stats.prayer))
            assertEquals(50, player.hitpoints)
        }

    @Test
    fun GameTestState.`prayer potion restores prayer points`() =
        runGameTest(ConsumablesScript::class) {
            setUp(hitpoints = 99, ConsumableObjs.prayer4)
            player.setCurrentLevel(stats.prayer, 10)
            player.withProtectedAccess { opHeld1(0) }
            assertEquals(41, player.stat(stats.prayer))
        }

    @Test
    fun GameTestState.`potion delay is separate from food delay`() =
        runGameTest(ConsumablesScript::class) {
            setUp(hitpoints = 50, ConsumableObjs.brew4, ConsumableObjs.brew4, ConsumableObjs.shark)
            player.withProtectedAccess { opHeld1(0) }
            player.withProtectedAccess { opHeld1(1) }
            player.withProtectedAccess { opHeld1(2) }
            assertTrue(player.inv[1].isType(ConsumableObjs.brew4))
            assertNull(player.inv[2])
        }

    /** 99 in every combat stat, [hitpoints] current HP, and [objs] from slot 0. */
    private fun GameTestScope.setUp(hitpoints: Int, vararg objs: ObjType) {
        // A fresh test player's clock reads -1 until their first tick.
        advance()
        for (stat in COMBAT_STATS) {
            player.stats[stat] = 99
        }
        player.setCurrentLevel(stats.hitpoints, hitpoints)
        player.clearInv()
        objs.forEachIndexed { slot, obj -> player.inv[slot] = InvObj(obj) }
    }

    private companion object {
        val COMBAT_STATS =
            listOf(
                stats.attack,
                stats.strength,
                stats.defence,
                stats.ranged,
                stats.magic,
                stats.prayer,
                stats.hitpoints,
            )
    }
}
