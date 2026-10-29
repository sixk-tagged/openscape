package org.rsmod.content.skills.herblore

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.components
import org.rsmod.api.testing.GameTestState
import org.rsmod.content.skills.production.SkillProduction

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class HerbloreTest {
    @Test
    fun GameTestState.`cleaning a grimy herb needs the level and gives xp`() =
        runGameTest(SkillProduction::class, Herblore::class) {
            val grimy = obj("unidentified_guam")
            setUp(2, grimy to 1)
            player.withProtectedAccess { opHeld1(0) }
            assertMessageSent("You need level 3 Herblore to clean this herb.")
            assertEquals(1, player.count(grimy))

            setUp(3, grimy to 1)
            val xp = herbloreXp()
            player.withProtectedAccess { opHeld1(0) }
            assertEquals(1, player.count(obj("guam_leaf")))
            assertEquals(fineXp(2.5), herbloreXp() - xp)
        }

    @Test
    fun GameTestState.`a clean herb in a vial makes an unfinished potion`() =
        runGameTest(SkillProduction::class, Herblore::class) {
            val guam = obj("guam_leaf")
            setUp(3, guam to 1, HerbloreObjs.vial_water to 1)

            useOn(guam, HerbloreObjs.vial_water)
            advance(ticks = 2)
            player.resumePauseButton(components.skillmulti_a, sub = 1)
            advanceUntil({ player.count(obj("guamvial")) == 1 }, timeoutTicks = 20)
            assertEquals(0, player.count(guam))
        }

    @Test
    fun GameTestState.`eye of newt on a guam potion makes a 3 dose attack potion`() =
        runGameTest(SkillProduction::class, Herblore::class) {
            val newt = obj("eye_of_newt")
            setUp(3, obj("guamvial") to 1, newt to 1)
            val xp = herbloreXp()

            useOn(obj("guamvial"), newt)
            advance(ticks = 2)
            player.resumePauseButton(components.skillmulti_a, sub = 1)
            advanceUntil({ player.count(Potions.attack[1]) == 1 }, timeoutTicks = 20)
            assertEquals(fineXp(25.0), herbloreXp() - xp)
        }

    @Test
    fun GameTestState.`stamina takes an amylase crystal per dose`() =
        runGameTest(SkillProduction::class, Herblore::class) {
            val amylase = obj("amylase")
            setUp(77, Potions.superEnergy[2] to 1, amylase to 2)
            val xp = herbloreXp()

            useOn(amylase, Potions.superEnergy[2])
            advance(ticks = 2)
            player.resumePauseButton(components.skillmulti_a, sub = 1)
            advanceUntil({ player.count(Potions.stamina[2]) == 1 }, timeoutTicks = 20)
            assertEquals(0, player.count(amylase))
            assertEquals(fineXp(51.0), herbloreXp() - xp)
        }

    @Test
    fun GameTestState.`a pestle and mortar grinds the whole stack`() =
        runGameTest(SkillProduction::class, Herblore::class) {
            val horn = obj("unicorn_horn")
            setUp(1, HerbloreObjs.pestle_and_mortar to 1, horn to 2)

            useOn(HerbloreObjs.pestle_and_mortar, horn)
            advanceUntil({ player.count(obj("unicorn_horn_dust")) == 2 }, timeoutTicks = 30)
            assertEquals(0, player.count(horn))
        }

    @Test
    fun GameTestState.`a lava scale at 99 herblore grinds into six shards`() =
        runGameTest(SkillProduction::class, Herblore::class) {
            val scale = obj("lava_scale")
            setUp(99, HerbloreObjs.pestle_and_mortar to 1, scale to 1)

            useOn(HerbloreObjs.pestle_and_mortar, scale)
            advanceUntil({ player.count(scale) == 0 }, timeoutTicks = 30)
            assertEquals(6, player.count(obj("lava_shard")))
        }

    @Test
    fun GameTestState.`weapon poison coats five arrows and leaves a vial`() =
        runGameTest(SkillProduction::class, Herblore::class) {
            val arrows = obj("bronze_arrow")
            setUp(1, HerbloreObjs.weapon_poison to 1, arrows to 12)

            useOn(HerbloreObjs.weapon_poison, arrows)
            assertEquals(7, player.count(arrows))
            assertEquals(5, player.count(obj("bronze_arrow_p")))
            assertEquals(1, player.count(HerbloreObjs.vial_empty))
        }

    @Test
    fun GameTestState.`combining doses fills one potion and empties the other`() =
        runGameTest(SkillProduction::class, Herblore::class) {
            val three = Potions.attack[1]
            val two = Potions.attack[2]
            setUp(1, three to 1, two to 1)

            useOn(two, three)
            assertEquals(1, player.count(Potions.attack[0]))
            assertEquals(1, player.count(Potions.attack[3]))
            assertEquals(0, player.count(two))
        }

    @Test
    fun GameTestState.`an empty vial splits a four dose potion in two`() =
        runGameTest(SkillProduction::class, Herblore::class) {
            setUp(1, Potions.attack[0] to 1, HerbloreObjs.vial_empty to 1)

            useOn(Potions.attack[0], HerbloreObjs.vial_empty)
            assertEquals(2, player.count(Potions.attack[2]))
        }
}
