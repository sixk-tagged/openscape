package org.rsmod.content.areas.city.edgeville.locs

import jakarta.inject.Inject
import org.rsmod.api.config.refs.seqs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.synths
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.stat
import org.rsmod.api.player.stat.statAdd
import org.rsmod.api.player.stat.statBase
import org.rsmod.api.player.stat.statRestore
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLoc2
import org.rsmod.content.areas.city.edgeville.configs.EdgevilleSeqs
import org.rsmod.content.areas.city.edgeville.configs.edgeville_locs
import org.rsmod.content.areas.city.edgeville.configs.edgeville_objs
import org.rsmod.content.areas.city.edgeville.configs.edgeville_varps
import org.rsmod.content.areas.city.edgeville.npcs.fortressGuardWarning
import org.rsmod.content.areas.city.edgeville.npcs.monasteryEntrance
import org.rsmod.content.areas.city.edgeville.npcs.wearingGuardDisguise
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The dungeon's trapdoor, brass key door and odd looking wall, the monastery ladders and altars,
 * the Black Knights' Fortress door, the Goblin Village crates and its two-storey ladders. The other
 * ladders, doors and stairs join the generic content groups in
 * [org.rsmod.content.areas.city.edgeville.configs.EdgevilleLocEditor].
 */
class EdgevilleLocScript @Inject constructor(private val locRepo: LocRepository) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1(edgeville_locs.trapdoor) {
            locRepo.change(it.loc, edgeville_locs.trapdoor_open, TRAPDOOR_OPEN_CYCLES)
        }
        onOpLoc2(edgeville_locs.trapdoor_open) {
            locRepo.change(it.loc, edgeville_locs.trapdoor, TRAPDOOR_OPEN_CYCLES)
        }
        onOpLoc1(edgeville_locs.brass_key_door) { brassKeyDoor(it.loc) }

        onOpLoc1(edgeville_locs.monastery_ladder) { monasteryLadder(it.loc) }
        onOpLoc1(edgeville_locs.monastery_altar) { prayAt(overheal = MONASTERY_BOOST) }
        onOpLoc1(edgeville_locs.chaos_altar) { prayAt(overheal = 0) }

        onOpLoc1(edgeville_locs.fortress_door) { fortressDoor(it.loc) }

        edgeville_locs.goblin_crates.forEachIndexed { index, crate ->
            onOpLoc1(crate) { searchGoblinCrate(index) }
        }
        onOpLoc1(edgeville_locs.goblin_ladder_bottom) { climb(levels = 2) }
        onOpLoc1(edgeville_locs.goblin_ladder_top) { climb(levels = -2) }
    }

    private suspend fun ProtectedAccess.climb(levels: Int) {
        arriveDelay()
        anim(if (levels > 0) seqs.human_reachforladder else seqs.human_pickupfloor)
        delay(1)
        telejump(coords.translateLevel(levels))
    }

    /** The door is a wall on the south edge of its tile, so outside is the tile below it. */
    private fun ProtectedAccess.brassKeyDoor(door: BoundLocInfo) {
        val entering = coords.z < door.coords.z
        if (entering && edgeville_objs.brass_key !in inv) {
            // Note: message unverified.
            mes("The door is locked.")
            return
        }
        stepThrough(if (entering) 1 else -1)
    }

    // Note: the door stays shut and the checked player steps through, like the Cooks' Guild's.
    private fun ProtectedAccess.stepThrough(dz: Int) {
        soundSynth(synths.door_open)
        telejump(coords.translateZ(dz))
    }

    /** The south-east ladder has the Abbot speak, the south-west one a monk; both want a member. */
    private suspend fun ProtectedAccess.monasteryLadder(ladder: BoundLocInfo) {
        if (vars[edgeville_varps.monastery_joined] == 0) {
            startDialogue { monasteryEntrance(ladder.coords.x == EAST_LADDER_X) }
            return
        }
        arriveDelay()
        anim(seqs.human_reachforladder)
        delay(1)
        telejump(coords.translateLevel(1))
    }

    /** Recharges Prayer to its maximum plus [overheal] points (the monastery altar's boost). */
    private fun ProtectedAccess.prayAt(overheal: Int) {
        val limit = player.statBase(stats.prayer) + overheal
        if (player.stat(stats.prayer) >= limit) {
            mes("You already have full Prayer points.")
            return
        }
        anim(EdgevilleSeqs.pray)
        player.statRestore(stats.prayer)
        if (overheal > 0) {
            player.statAdd(stats.prayer, overheal, 0)
        }
        vars[varbits.prayer_drain_counter] = 0
        mes("You recharge your Prayer points.")
    }

    /** The guards' entrance only opens from the south for someone dressed as a guard. */
    private suspend fun ProtectedAccess.fortressDoor(door: BoundLocInfo) {
        val entering = coords.z <= door.coords.z
        if (entering && !player.wearingGuardDisguise()) {
            startDialogue { fortressGuardWarning() }
            return
        }
        stepThrough(if (entering) 1 else -1)
    }

    /** Each crate gives goblin mail once per player; the varp keeps a bit for each crate. */
    private fun ProtectedAccess.searchGoblinCrate(index: Int) {
        val bit = 1 shl index
        val searched = vars[edgeville_varps.goblin_mail_crates]
        if ((searched and bit) != 0) {
            mes("You search the crate, but find nothing of interest.")
            return
        }
        if (inv.isFull()) {
            mes("You don't have room for the goblin mail.")
            return
        }
        vars[edgeville_varps.goblin_mail_crates] = searched or bit
        invAdd(inv, edgeville_objs.goblin_mail)
        // Note: the crates' contents and messages are unverified.
        mes("You search the crate and find some goblin mail.")
    }

    private companion object {
        const val TRAPDOOR_OPEN_CYCLES = 300
        const val MONASTERY_BOOST = 2
        const val EAST_LADDER_X = 3057
    }
}
