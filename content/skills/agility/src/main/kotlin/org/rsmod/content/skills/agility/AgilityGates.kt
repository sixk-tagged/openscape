package org.rsmod.content.skills.agility

import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.agilityLvl
import org.rsmod.api.script.onOpLoc1
import org.rsmod.content.skills.agility.configs.AgilityLocs
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.loc.LocAngle
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The members' gates (every account is a member, so they just open) and the two gates into the
 * Wilderness Agility Course (52 Agility to go in, OSRS wiki). The gates are walked through without
 * swinging open, as the doors of Draynor are.
 */
class AgilityGates : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1(AgilityLocs.member_gate_left) { walkThrough(it.loc) }
        onOpLoc1(AgilityLocs.member_gate_right) { walkThrough(it.loc) }
        onOpLoc1(AgilityLocs.wilderness_gate_south) { courseGate(it.loc) }
        onOpLoc1(AgilityLocs.wilderness_gate_north_left) { courseGate(it.loc) }
        onOpLoc1(AgilityLocs.wilderness_gate_north_right) { courseGate(it.loc) }
    }

    // UNVERIFIED: the refusal message.
    private suspend fun ProtectedAccess.courseGate(gate: BoundLocInfo) {
        if (isOutside(gate) && player.agilityLvl < COURSE_LEVEL) {
            mes("You need an Agility level of $COURSE_LEVEL to go through this gate.")
            return
        }
        // Leaving loses the fee; coming back through a gate is how a death or teleport shows.
        dispenserState = 0
        walkThrough(gate)
    }

    private suspend fun ProtectedAccess.walkThrough(gate: BoundLocInfo) {
        arriveDelay()
        val (dx, dz) = outward(gate.angle)
        val step = if (isOutside(gate)) -1 else 1
        faceSquare(coords.translate(step * dx, step * dz))
        delay(1)
        telejump(coords.translate(step * dx, step * dz))
    }

    /** Whether the player is on the side the gate's wall faces. */
    private fun ProtectedAccess.isOutside(gate: BoundLocInfo): Boolean {
        val (dx, dz) = outward(gate.angle)
        return (coords.x - gate.coords.x) * dx + (coords.z - gate.coords.z) * dz > 0
    }

    private fun outward(angle: LocAngle): Pair<Int, Int> =
        when (angle) {
            LocAngle.West -> -1 to 0
            LocAngle.North -> 0 to 1
            LocAngle.East -> 1 to 0
            LocAngle.South -> 0 to -1
        }

    private companion object {
        const val COURSE_LEVEL = 52
    }
}
