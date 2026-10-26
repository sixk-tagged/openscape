package org.rsmod.content.skills.crafting

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.components
import org.rsmod.api.testing.GameTestState

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class NeedleworkTest {
    @Test
    fun GameTestState.`a reel of thread lasts five actions`() =
        runGameTest(Crafting::class, Needlework::class) {
            val leather = TannerHides.all[0].leather
            setUp(1, CraftingObjs.needle, CraftingObjs.thread, *Array(6) { leather })
            val xp = craftingXp()

            useOnAny(CraftingObjs.needle, leather)
            advance(ticks = 2)
            player.resumePauseButton(components.skillmulti_a, sub = 6)
            advanceUntil({ CraftingObjs.thread !in player.inv }, timeoutTicks = 60)
            assertMessageSent("You use up a reel of thread.")
            advance(ticks = 6)

            val gloves = objTypes.values.first { it.internalName == "leather_gloves" }
            assertEquals(5, player.count(gloves))
            assertEquals(1, player.count(leather))
            assertEquals(fineXp(13.8) * 5, craftingXp() - xp)
        }

    @Test
    fun GameTestState.`dragonhide needs the level and three leather for a body`() =
        runGameTest(Crafting::class, Needlework::class) {
            val green = objTypes.values.first { it.internalName == "dragon_leather" }
            val body = objTypes.values.first { it.internalName == "dragonhide_body" }
            setUp(62, CraftingObjs.needle, CraftingObjs.thread, green, green, green)
            useOnAny(CraftingObjs.thread, green)
            advance(ticks = 2)
            // The menu lists vambraces (57), chaps (60), body (63).
            player.resumePauseButton(components.skillmulti_c, sub = 1)
            advance()
            assertMessageSent("You need a Crafting level of 63 to make a green d'hide body.")
            assertEquals(3, player.count(green))

            setUp(63, CraftingObjs.needle, CraftingObjs.thread, green, green, green)
            val xp = craftingXp()
            useOnAny(CraftingObjs.needle, green)
            advance(ticks = 2)
            player.resumePauseButton(components.skillmulti_c, sub = 1)
            advanceUntil({ player.count(body) == 1 }, timeoutTicks = 20)
            assertEquals(0, player.count(green))
            assertEquals(fineXp(186.0), craftingXp() - xp)
        }

    @Test
    fun GameTestState.`studs on a leather body make a studded body`() =
        runGameTest(Crafting::class, Needlework::class) {
            val armour = objTypes.values.first { it.internalName == "leather_armour" }
            val studs = objTypes.values.first { it.internalName == "studs" }
            val studded = objTypes.values.first { it.internalName == "studded_body" }
            setUp(41, armour, studs)
            useOn(studs, armour)
            advanceUntil({ player.count(studded) == 1 }, timeoutTicks = 20)
            assertEquals(0, player.count(studs))
        }
}
