package org.rsmod.content.skills.crafting

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.components
import org.rsmod.api.testing.GameTestState
import org.rsmod.game.inv.InvObj
import org.rsmod.map.CoordGrid

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class PotteryGlassTest {
    @Test
    fun GameTestState.`clay and a bucket of water make soft clay and the bucket comes back`() =
        runGameTest(Crafting::class, Pottery::class) {
            setUp(1, CraftingObjs.clay, obj("bucket_water"))
            useOn(CraftingObjs.clay, obj("bucket_water"))
            advance(ticks = 2)
            player.resumePauseButton(components.skillmulti_a, sub = 1)
            advanceUntil({ player.count(CraftingObjs.soft_clay) == 1 }, timeoutTicks = 20)
            assertEquals(0, player.count(CraftingObjs.clay))
            assertEquals(1, player.count(CraftingObjs.bucket_empty))
        }

    @Test
    fun GameTestState.`the wheel shapes pots, the oven fires them and can crack one`() =
        runGameTest(Crafting::class, Pottery::class) {
            val wheel = placeMapLoc(SPOT, CraftingLocs.potters_wheels.first())
            val oven = placeMapLoc(SPOT.translateX(4), CraftingLocs.pottery_ovens.first())
            val unfired = obj("pot_unfired")
            val pot = obj("pot_empty")
            setUp(1, CraftingObjs.soft_clay, CraftingObjs.soft_clay)
            var xp = craftingXp()

            useOnLoc(wheel, CraftingObjs.soft_clay)
            advance(ticks = 2)
            // Bowl (level 8) is refused at level 1; the pot is the first choice.
            player.resumePauseButton(components.skillmulti_d, sub = 1)
            advance()
            assertMessageSent("You need a Crafting level of 8 to make an unfired bowl.")
            useOnLoc(wheel, CraftingObjs.soft_clay)
            advance(ticks = 2)
            player.resumePauseButton(components.skillmulti_a, sub = 2)
            advanceUntil({ player.count(unfired) == 2 }, timeoutTicks = 30)
            assertEquals(fineXp(6.3) * 2, craftingXp() - xp)

            xp = craftingXp()
            random.next = 0 // the first pot survives
            random.next = 100 // the second cracks
            useOnLoc(oven, unfired)
            advance(ticks = 2)
            player.resumePauseButton(components.skillmulti_a, sub = 2)
            advanceUntil({ player.count(pot) == 1 }, timeoutTicks = 30)
            assertEquals(fineXp(6.3), craftingXp() - xp)
            advanceUntil({ player.count(unfired) == 0 }, timeoutTicks = 30)
            assertMessageSent("The pot cracks in the oven.")
            assertEquals(1, player.count(pot))
        }

    @Test
    fun GameTestState.`molten glass then glassblowing`() =
        runGameTest(Crafting::class, Glass::class) {
            val furnace = placeMapLoc(SPOT, findLocType(CraftingContent.furnace))
            setUp(1, CraftingObjs.bucket_sand, CraftingObjs.soda_ash)
            var xp = craftingXp()
            useOnFurnace(furnace, CraftingObjs.soda_ash)
            advance(ticks = 2)
            player.resumePauseButton(components.skillmulti_a, sub = 1)
            advanceUntil({ player.count(CraftingObjs.molten_glass) == 1 }, timeoutTicks = 20)
            assertEquals(1, player.count(CraftingObjs.bucket_empty))
            assertEquals(fineXp(20.0), craftingXp() - xp)

            player.inv[5] = InvObj(CraftingObjs.glassblowing_pipe)
            xp = craftingXp()
            useOn(CraftingObjs.glassblowing_pipe, CraftingObjs.molten_glass)
            advance(ticks = 2)
            player.resumePauseButton(components.skillmulti_a, sub = 1)
            advanceUntil({ player.count(obj("beer_glass")) == 1 }, timeoutTicks = 20)
            assertEquals(fineXp(17.5), craftingXp() - xp)
        }

    @Test
    fun GameTestState.`sand fills buckets and seaweed burns into soda ash`() =
        runGameTest(Crafting::class, Glass::class) {
            val pit = placeMapLoc(SPOT, CraftingLocs.sandpits.first())
            val bucket = CraftingObjs.bucket_empty
            setUp(1, bucket, bucket, bucket)
            useOnLocItem(pit, bucket)
            advanceUntil({ player.count(CraftingObjs.bucket_sand) == 3 }, timeoutTicks = 30)
            assertEquals(0, player.count(bucket))

            val fire = placeMapLoc(SPOT.translateX(4), CraftingLocs.heat_sources.first())
            val giant = obj("giant_seaweed")
            setUp(1, giant)
            useOnLocItem(fire, giant)
            advanceUntil({ player.count(CraftingObjs.soda_ash) == 6 }, timeoutTicks = 30)
            assertEquals(0, player.count(giant))
        }

    @Test
    fun GameTestState.`every crafting script registers together`() =
        runGameTest(
            Crafting::class,
            Spinning::class,
            Needlework::class,
            Tanning::class,
            Chiselling::class,
            Jewellery::class,
            Pottery::class,
            Glass::class,
            Loom::class,
        ) {
            advance()
        }

    @Test
    fun GameTestState.`the loom weaves a strip of cloth and an orb makes a battlestaff`() =
        runGameTest(Crafting::class, Loom::class) {
            val loom = placeMapLoc(SPOT, CraftingLocs.looms.first())
            val wool = CraftingObjs.ball_of_wool
            setUp(10, wool, wool, wool, wool)
            useOnLoc(loom, wool)
            advance(ticks = 2)
            player.resumePauseButton(components.skillmulti_a, sub = 1)
            advanceUntil({ player.count(obj("regicide_cloth")) == 1 }, timeoutTicks = 20)
            assertEquals(0, player.count(wool))

            setUp(66, CraftingObjs.battlestaff, obj("air_orb"))
            val xp = craftingXp()
            useOn(CraftingObjs.battlestaff, obj("air_orb"))
            advance(ticks = 2)
            player.resumePauseButton(components.skillmulti_a, sub = 1)
            advanceUntil({ player.count(obj("air_battlestaff")) == 1 }, timeoutTicks = 20)
            assertEquals(fineXp(137.5), craftingXp() - xp)
        }

    private companion object {
        val SPOT = CoordGrid(0, 50, 50, 34, 31)
    }
}
