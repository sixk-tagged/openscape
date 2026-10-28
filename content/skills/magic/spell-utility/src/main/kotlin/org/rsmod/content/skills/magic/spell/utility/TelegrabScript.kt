package org.rsmod.content.skills.magic.spell.utility

import jakarta.inject.Inject
import org.rsmod.api.combat.commons.magic.MagicSpell
import org.rsmod.api.combat.manager.MagicRuneManager
import org.rsmod.api.combat.manager.MagicRuneManager.Companion.isFailure
import org.rsmod.api.config.Constants
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.invtx.invAdd
import org.rsmod.api.player.events.interact.SpellOnObj
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.protect.ProtectedAccessLauncher
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.repo.world.WorldRepository
import org.rsmod.api.script.onEvent
import org.rsmod.api.spells.MagicSpellRegistry
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.api.type.refs.spot.SpotanimReferences
import org.rsmod.api.type.refs.synth.SynthReferences
import org.rsmod.game.obj.Obj
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Telekinetic Grab: the obj leaves the floor and lands in the inventory a couple of ticks after the
 * cast, from up to [RANGE] tiles away. The runes go at the cast; nothing is taken (and no runes)
 * when the inventory has no room.
 *
 * Note: no line-of-sight or "fences yes, doors no" check, and nothing blocks the grab inside the
 * restricted areas the wiki lists (Tower of Life...). The grab is a map graphic, not a projectile.
 */
class TelegrabScript
@Inject
constructor(
    private val spells: MagicSpellRegistry,
    private val runes: MagicRuneManager,
    private val launcher: ProtectedAccessLauncher,
    private val objRepo: ObjRepository,
    private val worldRepo: WorldRepository,
) : PluginScript() {
    override fun ScriptContext.startup() {
        val spell = checkNotNull(spells.getObjSpell(objs.spell_tele_grab))
        onEvent<SpellOnObj>(spell.component.packed) { launcher.launch(player) { grab(spell, obj) } }
    }

    private suspend fun ProtectedAccess.grab(spell: MagicSpell, obj: Obj) {
        if (actionDelay > mapClock) {
            return
        }
        if (obj.coords.level != coords.level || distanceTo(obj.coords) > RANGE) {
            // UNVERIFIED: the wiki lists no message for a target out of range.
            mes("You're too far away to do that.")
            return
        }
        if (!hasRoom(obj)) {
            mes(Constants.dm_take_invspace)
            return
        }
        if (runes.attemptCast(player, spell).isFailure()) {
            return
        }
        faceSquare(obj.coords)
        anim(TelegrabSeqs.cast)
        spotanim(TelegrabSpots.casting, height = 92)
        spotanimMap(
            worldRepo,
            TelegrabSpots.impact,
            obj.coords,
            height = 0,
            delay = TRAVEL_TICKS * CLIENT_CYCLES_PER_TICK,
        )
        soundSynth(TelegrabSynths.cast)
        statAdvance(stats.magic, spell.castXp)
        actionDelay = mapClock + CAST_TICKS
        // UNVERIFIED: the wiki says the travel time is a fixed number of ticks, not which.
        delay(TRAVEL_TICKS)
        if (!objRepo.del(obj)) {
            mes(Constants.dm_take_taken)
            return
        }
        val take = player.invAdd(player.inv, obj.type, obj.count, autoCommit = false)
        if (take.failure) {
            mes(Constants.dm_take_invspace)
        } else {
            take.commitAll()
        }
    }

    private fun ProtectedAccess.hasRoom(obj: Obj): Boolean =
        player.invAdd(player.inv, obj.type, obj.count, autoCommit = false).success

    private companion object {
        const val RANGE = 10
        const val CAST_TICKS = 3
        const val TRAVEL_TICKS = 2
        const val CLIENT_CYCLES_PER_TICK = 30
    }
}

object TelegrabSeqs : SeqReferences() {
    val cast = find("human_casttelegrab")
}

object TelegrabSpots : SpotanimReferences() {
    val casting = find("telegrab_casting")
    val impact = find("telegrab_impact")
}

object TelegrabSynths : SynthReferences() {
    val cast = find("telegrab_all")
}
