package org.rsmod.content.skills.runecraft.scripts

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.player.back
import org.rsmod.api.player.events.interact.HeldEquipEvents
import org.rsmod.api.player.hat
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.skills.runecraft.configs.Altar
import org.rsmod.content.skills.runecraft.configs.Altars
import org.rsmod.content.skills.runecraft.configs.RunecraftObjs
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.Wearpos

/* Inventory transactions share a backing list and are not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class RuinsScriptTest {
    @Test
    fun GameTestState.`a talisman used on the ruins enters the altar, a portal leaves`() =
        runGameTest(RuinsScript::class) {
            val altar = Altars.air
            val ruins = checkNotNull(altar.ruins)
            allocZoneCollision(ruins.inside)
            allocZoneCollision(ruins.outside)
            advance()
            val loc = placeMapLoc(SPOT, ruins.loc)
            player.teleport(SPOT.translateX(-1))
            player.clearInv()
            player.inv[0] = InvObj(RunecraftObjs.air_talisman)

            useOn(loc, RunecraftObjs.air_talisman)
            advance(ticks = 2)
            assertEquals(ruins.inside, player.coords)

            val portal = placeMapLoc(SPOT.translateZ(10), ruins.portal)
            player.teleport(SPOT.translateZ(10).translateX(-1))
            player.opLoc1(portal)
            advance(ticks = 2)
            assertEquals(ruins.outside, player.coords)
        }

    @Test
    fun GameTestState
        .`the elemental talisman opens elemental ruins and the catalytic one the rest`() =
        runGameTest(RuinsScript::class) {
            advance()
            val pairs =
                listOf(
                    Altars.water to RunecraftObjs.elemental_talisman,
                    Altars.body to RunecraftObjs.catalytic_talisman,
                )
            for ((index, pair) in pairs.withIndex()) {
                val (altar, talisman) = pair
                val ruins = checkNotNull(altar.ruins)
                allocZoneCollision(ruins.inside)
                val spot = SPOT.translateZ(index * 10)
                val loc = placeMapLoc(spot, ruins.loc)
                player.teleport(spot.translateX(-1))
                player.inv[0] = InvObj(talisman)
                useOn(loc, talisman)
                advance(ticks = 2)
                assertEquals(ruins.inside, player.coords)
            }
        }

    @Test
    fun GameTestState.`a worn tiara shows Enter on its ruins and walks in`() =
        runGameTest(RuinsScript::class) {
            val altar = Altars.mind
            val ruins = checkNotNull(altar.ruins)
            allocZoneCollision(ruins.inside)
            advance()
            val loc = placeMapLoc(SPOT, ruins.loc)
            player.teleport(SPOT.translateX(-1))

            wear(Wearpos.Hat, RunecraftObjs.tiara_mind)
            assertEquals(1, player.vars[ruins.bit])
            assertEquals(0, player.vars[checkNotNull(Altars.air.ruins).bit])

            player.opLoc1(loc)
            advance(ticks = 2)
            assertEquals(ruins.inside, player.coords)

            // Taking the tiara off closes the ruins again.
            wear(Wearpos.Hat, null)
            assertEquals(0, player.vars[ruins.bit])
        }

    @Test
    fun GameTestState.`elemental tiaras open four ruins and a Runecraft cape opens them all`() =
        runGameTest(RuinsScript::class) {
            advance()
            wear(Wearpos.Hat, RunecraftObjs.tiara_elemental)
            assertOpen(Altars.air, Altars.water, Altars.earth, Altars.fire)

            wear(Wearpos.Hat, null)
            wear(Wearpos.Back, RunecraftObjs.runecrafting_cape)
            assertOpen(*Altars.withRuins.toTypedArray())

            wear(Wearpos.Back, null)
            wear(Wearpos.Hat, RunecraftObjs.tiara_catalytic)
            assertOpen(*Altars.catalyticRuins.toTypedArray())
        }

    private fun GameTestScope.assertOpen(vararg open: Altar) {
        for (altar in Altars.withRuins) {
            val expected = if (altar in open) 1 else 0
            val actual = player.vars[checkNotNull(altar.ruins).bit]
            assertTrue(expected == actual) {
                "${altar.name} ruins: expected $expected, was $actual"
            }
        }
    }

    private fun GameTestScope.wear(position: Wearpos, obj: ObjType?) {
        val item = obj?.let { InvObj(it) }
        if (position == Wearpos.Hat) player.hat = item else player.back = item
        val type = objTypes[obj ?: RunecraftObjs.tiara]
        eventBus.publish(HeldEquipEvents.WearposChange(player, position, type))
    }
}
