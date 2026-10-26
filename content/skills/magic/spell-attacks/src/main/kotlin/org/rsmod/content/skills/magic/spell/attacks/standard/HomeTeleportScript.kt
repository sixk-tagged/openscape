package org.rsmod.content.skills.magic.spell.attacks.standard

import org.rsmod.api.player.isInCombat
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.script.onIfOverlayButton
import org.rsmod.api.script.onPlayerQueueWithArgs
import org.rsmod.api.type.refs.comp.ComponentReferences
import org.rsmod.api.type.refs.queue.QueueReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.api.type.refs.spot.SpotanimReferences
import org.rsmod.api.type.refs.varp.VarpReferences
import org.rsmod.game.entity.Player
import org.rsmod.game.type.seq.SeqType
import org.rsmod.game.type.spot.SpotanimType
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** Minutes since the Unix epoch of the last home teleport; the client reads it for the cooldown. */
private var Player.lastHomeTeleport: Int by intVarp(HomeTeleportVarps.aide_tele_timer)

/**
 * Lumbridge Home Teleport. Five "aide" stages over 24 ticks on a weak queue, so walking away
 * cancels it; combat cancels it too. The cooldown is 30 minutes of real time, shared with every
 * home teleport in OSRS.
 */
class HomeTeleportScript : PluginScript() {
    override fun ScriptContext.startup() {
        onIfOverlayButton(HomeTeleportComponents.standard) { player.castHomeTeleport() }
        onPlayerQueueWithArgs<Int>(HomeTeleportQueues.home_teleport) { stage(it.args) }
    }

    private fun Player.castHomeTeleport() {
        val wait = COOLDOWN_MINUTES - (epochMinutes() - lastHomeTeleport)
        if (wait > 0) {
            val minutes = if (wait == 1) "minute" else "minutes"
            mes("You need to wait another $wait $minutes to cast this spell.")
            return
        }
        if (isInCombat()) {
            mes("You can't use this teleport after being in combat recently.")
            return
        }
        clearWeakQueue(HomeTeleportQueues.home_teleport)
        weakQueue(HomeTeleportQueues.home_teleport, 1, 0)
    }

    private fun ProtectedAccess.stage(index: Int) {
        if (isInCombat()) {
            resetAnim()
            spotanim(null)
            return
        }
        val stage = STAGES.getOrNull(index)
        if (stage == null) {
            teleport(DESTINATION)
            resetAnim()
            player.lastHomeTeleport = epochMinutes()
            return
        }
        anim(stage.seq)
        spotanim(stage.spot)
        // A queue added while queues are processed counts down this same cycle: +1 keeps N ticks.
        weakQueue(HomeTeleportQueues.home_teleport, stage.ticks + 1, index + 1)
    }

    private data class Stage(val seq: SeqType, val spot: SpotanimType, val ticks: Int)

    private companion object {
        const val COOLDOWN_MINUTES = 30
        val DESTINATION = CoordGrid(0, 50, 50, 21, 18)

        // Note: the 7/6/4/4/3 split of the wiki's 24 ticks is from old server ports, not
        // verified frame by frame.
        val STAGES =
            listOf(
                Stage(HomeTeleportSeqs.draw_circle, HomeTeleportSpots.chalk_circle, 7),
                Stage(HomeTeleportSeqs.sit_down, HomeTeleportSpots.chalk_circle_static, 6),
                Stage(HomeTeleportSeqs.get_book, HomeTeleportSpots.book_get, 4),
                Stage(HomeTeleportSeqs.recite, HomeTeleportSpots.book_portal, 4),
                Stage(HomeTeleportSeqs.teleporting, HomeTeleportSpots.teleport, 3),
            )

        fun epochMinutes(): Int = (System.currentTimeMillis() / 60_000).toInt()
    }
}

object HomeTeleportComponents : ComponentReferences() {
    val standard = find("magic_spellbook:teleport_home_standard")
}

object HomeTeleportQueues : QueueReferences() {
    val home_teleport = find("home_teleport")
}

object HomeTeleportVarps : VarpReferences() {
    val aide_tele_timer = find("aide_tele_timer")
}

object HomeTeleportSeqs : SeqReferences() {
    val draw_circle = find("aide_drawing_chalk_circle")
    val sit_down = find("aide_sitting_down_crosslegged")
    val get_book = find("aide_player_getting_book")
    val recite = find("aide_reciting_incantation")
    val teleporting = find("aide_player_teleporting")
}

object HomeTeleportSpots : SpotanimReferences() {
    val chalk_circle = find("aide_chalk_circle")
    val chalk_circle_static = find("aide_chalk_circle_static")
    val book_get = find("aide_player_book_get")
    val book_portal = find("aide_player_book_portal")
    val teleport = find("aide_player_teleport")
}
