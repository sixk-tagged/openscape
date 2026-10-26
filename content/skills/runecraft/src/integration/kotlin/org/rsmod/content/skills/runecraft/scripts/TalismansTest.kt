package org.rsmod.content.skills.runecraft.scripts

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.skills.runecraft.configs.RunecraftObjs
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.obj.ObjType
import org.rsmod.map.CoordGrid

@Execution(ExecutionMode.SAME_THREAD)
class TalismansTest {
    @Test
    fun GameTestState.`locate points at the matching ruins`() =
        runGameTest(Talismans::class) {
            // The air ruins are at 2984,3291, south-west of here; the earth ruins are due east.
            locate(RunecraftObjs.air_talisman, CoordGrid(3000, 3300))
            assertMessageSent("The talisman pulls to the South-West.")

            locate(RunecraftObjs.earth_talisman, CoordGrid(3200, 3473))
            assertMessageSent("The talisman pulls to the East.")
        }

    @Test
    fun GameTestState.`the elemental talisman points at the nearest of four ruins`() =
        runGameTest(Talismans::class) {
            // Of the four elemental ruins, water (3184,3164) is the nearest to Lumbridge.
            locate(RunecraftObjs.elemental_talisman, CoordGrid(3200, 3200))
            assertMessageSent("The talisman pulls to the South-West.")
        }

    @Test
    fun GameTestState.`locate gives up underground and for the catalytic talisman`() =
        runGameTest(Talismans::class) {
            locate(RunecraftObjs.mind_talisman, CoordGrid(3103, 9571))
            assertMessageSent("You cannot tell which direction the Talisman is pulling...")

            locate(RunecraftObjs.catalytic_talisman, CoordGrid(3200, 3200))
            assertMessageSent("Nothing interesting happens.")
        }

    private fun GameTestScope.locate(talisman: ObjType, from: CoordGrid) {
        advance()
        player.teleport(from)
        player.clearInv()
        player.inv[0] = InvObj(talisman)
        player.withProtectedAccess { opHeld4(0) }
    }
}
