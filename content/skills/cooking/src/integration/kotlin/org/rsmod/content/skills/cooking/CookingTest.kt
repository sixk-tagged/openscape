package org.rsmod.content.skills.cooking

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.components
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.events.interact.LocUDefaultEvents
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.inv.InvObj
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.type.loc.LocType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class CookingTest {
    @Test
    fun GameTestState.`cooking shrimps on a fire`() =
        runGameTest(Cooking::class) {
            val fire = setUp(level = 99, CookingLocs.fire, CookingObjs.raw_shrimp)
            val xp = player.statMap.getFineXP(stats.cooking)

            useOn(fire, CookingObjs.raw_shrimp)
            advanceUntil({ player.count(CookingObjs.shrimp) == 1 }, timeoutTicks = 10)

            assertMessageSent("You successfully cook some shrimps.")
            assertTrue(player.statMap.getFineXP(stats.cooking) > xp)
            assertEquals(0, player.count(CookingObjs.raw_shrimp))
        }

    @Test
    fun GameTestState.`a failed roll burns the food`() =
        runGameTest(Cooking::class) {
            val fire = setUp(level = 1, CookingLocs.fire, CookingObjs.raw_shrimp)
            useOn(fire, CookingObjs.raw_shrimp)
            random.next = 255
            advanceUntil({ player.count(CookingObjs.burnt_shrimp) == 1 }, timeoutTicks = 10)
            assertMessageSent("You accidentally burn the shrimps.")
        }

    @Test
    fun GameTestState.`bread dough needs a range`() =
        runGameTest(Cooking::class) {
            val fire = setUp(level = 99, CookingLocs.fire, CookingObjs.bread_dough)
            useOn(fire, CookingObjs.bread_dough)
            assertMessageSent("You need to cook this on a range.")
        }

    @Test
    fun GameTestState.`the cook op asks how many and cooks that many`() =
        runGameTest(Cooking::class) {
            val raw = CookingObjs.raw_shrimp
            val fire = setUp(level = 99, CookingLocs.fire_cook, raw, raw, raw)

            player.opLoc1(fire)
            advance(ticks = 2)
            player.resumePauseButton(components.skillmulti_a, sub = 2)
            advanceUntil({ player.count(CookingObjs.shrimp) == 2 }, timeoutTicks = 20)

            advance(ticks = 6)
            assertEquals(2, player.count(CookingObjs.shrimp))
            assertEquals(1, player.count(raw))
        }

    @Test
    fun GameTestState.`churning milk into butter leaves the bucket`() =
        runGameTest(Churning::class) {
            advance()
            player.stats[stats.cooking] = 99
            player.clearInv()
            player.inv[0] = InvObj(CookingObjs.bucket_of_milk)
            val churn = placeMapLoc(SPOT, CookingLocs.dairy_churn)
            player.teleport(SPOT.translateZ(-1))

            player.opLoc1(churn)
            advance(ticks = 2)
            player.resumePauseButton(components.skillmulti_b, sub = 1)
            advanceUntil({ player.count(CookingObjs.pat_of_butter) == 1 }, timeoutTicks = 20)

            assertEquals(1, player.count(CookingObjs.bucket))
            assertMessageSent("You make a pat of butter.")
        }

    private fun GameTestScope.setUp(level: Int, heat: LocType, vararg objs: ObjType): BoundLocInfo {
        // A fresh test player's clock reads -1 until their first tick.
        advance()
        player.stats[stats.cooking] = level
        player.clearInv()
        objs.forEachIndexed { slot, obj -> player.inv[slot] = InvObj(obj) }
        val loc = placeMapLoc(SPOT, heat)
        player.teleport(SPOT.translateX(-1))
        return loc
    }

    private fun GameTestScope.useOn(loc: BoundLocInfo, obj: ObjType) {
        val event = LocUDefaultEvents.OpType(loc, loc, locTypes[loc], objTypes[obj], 0)
        player.withProtectedAccess { eventBus.publish(this, event) }
    }

    private companion object {
        val SPOT = CoordGrid(0, 50, 50, 34, 31)
    }
}
