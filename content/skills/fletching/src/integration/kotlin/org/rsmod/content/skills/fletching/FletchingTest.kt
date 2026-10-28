package org.rsmod.content.skills.fletching

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.components
import org.rsmod.api.testing.GameTestState
import org.rsmod.content.skills.production.SkillProduction

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class FletchingTest {
    @Test
    fun GameTestState.`knife on logs makes arrow shafts and gives xp`() =
        runGameTest(SkillProduction::class, Fletching::class) {
            setUp(1, FletchingObjs.knife to 1, obj("logs") to 1)
            val xp = fletchingXp()

            useOn(FletchingObjs.knife, obj("logs"))
            advance(ticks = 2)
            // The menu lists arrow shafts, javelin shafts, then the bows and the stock.
            player.resumePauseButton(components.skillmulti_a, sub = 1)
            advanceUntil({ player.count(FletchingObjs.arrow_shaft) == 15 }, timeoutTicks = 20)
            assertEquals(0, player.count(obj("logs")))
            assertEquals(fineXp(5.0), fletchingXp() - xp)
        }

    @Test
    fun GameTestState.`a bow string on an unstrung shortbow makes a shortbow`() =
        runGameTest(SkillProduction::class, Fletching::class) {
            val unstrung = obj("unstrung_shortbow")
            setUp(5, unstrung to 1, FletchingObjs.bow_string to 1)
            val xp = fletchingXp()

            useOn(unstrung, FletchingObjs.bow_string)
            advance(ticks = 2)
            player.resumePauseButton(components.skillmulti_a, sub = 1)
            advanceUntil({ player.count(obj("shortbow")) == 1 }, timeoutTicks = 20)
            assertEquals(fineXp(5.0), fletchingXp() - xp)
        }

    @Test
    fun GameTestState.`feathers on shafts make a short set of headless arrows`() =
        runGameTest(SkillProduction::class, Fletching::class) {
            setUp(1, FletchingObjs.arrow_shaft to 7, FletchingObjs.feather to 20)
            val xp = fletchingXp()

            useOn(FletchingObjs.arrow_shaft, FletchingObjs.feather)
            advanceUntil({ player.count(FletchingObjs.headless_arrow) == 7 }, timeoutTicks = 20)
            assertEquals(13, player.count(FletchingObjs.feather))
            assertEquals(fineXp(7.0), fletchingXp() - xp)
        }

    @Test
    fun GameTestState.`tipping arrows takes 15 of each and keeps going`() =
        runGameTest(SkillProduction::class, Fletching::class) {
            val tips = obj("bronze_arrowheads")
            setUp(1, FletchingObjs.headless_arrow to 30, tips to 30)
            val xp = fletchingXp()

            useOn(FletchingObjs.headless_arrow, tips)
            advanceUntil({ player.count(obj("bronze_arrow")) == 15 }, timeoutTicks = 20)
            assertEquals(15, player.count(tips))
            assertEquals(fineXp(1.3 * 15), fletchingXp() - xp)
            advanceUntil({ player.count(obj("bronze_arrow")) == 30 }, timeoutTicks = 20)
            assertEquals(0, player.count(tips))
        }

    @Test
    fun GameTestState.`arrows need the level and nothing is used up`() =
        runGameTest(SkillProduction::class, Fletching::class) {
            val tips = obj("iron_arrowheads")
            setUp(14, FletchingObjs.headless_arrow to 15, tips to 15)

            useOn(FletchingObjs.headless_arrow, tips)
            assertMessageSent("You need a Fletching level of 15 to make that.")
            assertEquals(15, player.count(tips))
        }

    @Test
    fun GameTestState.`a chisel cuts a sapphire into twelve bolt tips`() =
        runGameTest(SkillProduction::class, Fletching::class) {
            val tips = obj("xbows_bolt_tips_sapphire")
            setUp(56, FletchingObjs.chisel to 1, obj("sapphire") to 1)
            val xp = fletchingXp()

            useOn(FletchingObjs.chisel, obj("sapphire"))
            advanceUntil({ player.count(tips) == 12 }, timeoutTicks = 20)
            assertEquals(fineXp(4.0), fletchingXp() - xp)
        }

    @Test
    fun GameTestState.`limbs and a string turn a stock into a crossbow`() =
        runGameTest(SkillProduction::class, Fletching::class) {
            val stock = obj("xbows_crossbow_stock_wood")
            val limbs = obj("xbows_crossbow_limbs_bronze")
            val unstrung = obj("xbows_crossbow_unstrung_bronze")
            setUp(
                9,
                stock to 1,
                limbs to 1,
                FletchingObjs.hammer to 1,
                FletchingObjs.crossbow_string to 1,
            )
            val xp = fletchingXp()

            useOn(limbs, stock)
            advance(ticks = 2)
            player.resumePauseButton(components.skillmulti_a, sub = 1)
            advanceUntil({ player.count(unstrung) == 1 }, timeoutTicks = 20)
            assertEquals(fineXp(12.0), fletchingXp() - xp)

            useOn(FletchingObjs.crossbow_string, unstrung)
            advance(ticks = 2)
            player.resumePauseButton(components.skillmulti_a, sub = 1)
            advanceUntil({ player.count(obj("xbows_crossbow_bronze")) == 1 }, timeoutTicks = 20)
            assertEquals(fineXp(18.0), fletchingXp() - xp)
        }
}
