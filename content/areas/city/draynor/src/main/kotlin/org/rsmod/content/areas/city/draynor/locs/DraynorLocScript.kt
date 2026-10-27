package org.rsmod.content.areas.city.draynor.locs

import jakarta.inject.Inject
import org.rsmod.api.config.refs.hitmark_groups
import org.rsmod.api.config.refs.seqs
import org.rsmod.api.config.refs.synths
import org.rsmod.api.player.hit.processor.InstantPlayerHitProcessor
import org.rsmod.api.player.hit.takeInstantHit
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.random.GameRandom
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLoc2
import org.rsmod.content.areas.city.draynor.configs.draynor_locs
import org.rsmod.content.areas.city.draynor.configs.draynor_objs
import org.rsmod.game.hit.HitType
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.type.obj.Wearpos
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Sewer trapdoors, the Wizards' Tower basement ladder, Draynor Manor (doors, stairs, crypt and
 * cellar, the bookcase door), the jail gate, Morgan's garlic cupboard and the nettles. Fixed
 * landings are from RuneLite's shortest-path transport list.
 */
class DraynorLocScript
@Inject
constructor(
    private val locRepo: LocRepository,
    private val random: GameRandom,
    private val instantHits: InstantPlayerHitProcessor,
) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1(draynor_locs.sewer_trapdoor) { openTrapdoor(it.loc) }
        onOpLoc1(draynor_locs.sewer_ladder) { climb(SEWER_TRAPDOOR) }
        onOpLoc1(draynor_locs.tower_ladder_top) { climb(BASEMENT_LANDING, down = true) }
        onOpLoc1(draynor_locs.tower_ladder_bottom) { climb(TOWER_LANDING) }
        onOpLoc1(draynor_locs.manor_cabbage) { pickCabbage(it.loc) }
        onOpLoc1(draynor_locs.manor_bookcase_left) { openBookcase() }
        onOpLoc1(draynor_locs.manor_bookcase_right) { openBookcase() }
        onOpLoc1(draynor_locs.manor_lever) { openBookcase() }

        onOpLoc1(draynor_locs.manor_front_door_left) { frontDoor() }
        onOpLoc1(draynor_locs.manor_front_door_right) { frontDoor() }
        onOpLoc1(draynor_locs.manor_back_door) {
            stepThrough(if (coords.z < BACK_DOOR_Z) 1 else -1)
        }
        onOpLoc1(draynor_locs.manor_inner_stairs) { telejump(CoordGrid(coords.x, 3366, 1)) }
        onOpLoc1(draynor_locs.manor_inner_stairs_top) { telejump(CoordGrid(coords.x, 3361, 0)) }
        onOpLoc1(draynor_locs.crypt_stairs) { telejump(CRYPT_LANDING) }
        onOpLoc1(draynor_locs.crypt_stairs_top) { telejump(CRYPT_EXIT) }
        onOpLoc1(draynor_locs.cellar_ladder_top) { climb(CELLAR_LANDING, down = true) }
        onOpLoc1(draynor_locs.cellar_ladder) { climb(CELLAR_EXIT) }

        // Note: needs Prince Ali Rescue's bronze key, which nothing hands out; message
        // unverified.
        onOpLoc1(draynor_locs.jail_gate) { mes("The gate is locked.") }

        onOpLoc1(draynor_locs.garlic_cupboard) {
            locRepo.change(it.loc, draynor_locs.garlic_cupboard_open, CUPBOARD_OPEN_CYCLES)
        }
        onOpLoc1(draynor_locs.garlic_cupboard_open) { takeGarlic() }
        onOpLoc2(draynor_locs.garlic_cupboard_open) {
            locRepo.change(it.loc, draynor_locs.garlic_cupboard, CUPBOARD_OPEN_CYCLES)
        }

        for (nettles in draynor_locs.nettles) {
            onOpLoc1(nettles) { pickNettles(it.loc) }
        }
    }

    private fun ProtectedAccess.openTrapdoor(trapdoor: BoundLocInfo) {
        // The trapdoor closes itself again after three minutes.
        locRepo.change(trapdoor, draynor_locs.sewer_trapdoor_open, TRAPDOOR_OPEN_CYCLES)
    }

    private suspend fun ProtectedAccess.climb(dest: CoordGrid, down: Boolean = false) {
        arriveDelay()
        anim(if (down) seqs.human_pickupfloor else seqs.human_reachforladder)
        delay(1)
        telejump(dest)
    }

    private suspend fun ProtectedAccess.pickCabbage(cabbage: BoundLocInfo) {
        if (inv.isFull()) {
            mes("You don't have room for this cabbage.")
            return
        }
        arriveDelay()
        anim(seqs.human_pickupfloor)
        delay(1)
        // Someone else may have picked it while we reached for it.
        if (!isStillThere(cabbage)) {
            return
        }
        // Note: respawn time is a guess; the wiki lists "100" without units.
        locRepo.del(cabbage, CABBAGE_RESPAWN_CYCLES)
        invAdd(inv, draynor_objs.magic_cabbage)
        mes("You pick a cabbage.")
    }

    // Note: the real bookcase swings open; this one just disappears for a while.
    private fun ProtectedAccess.openBookcase() {
        locRepo.findExact(BOOKCASE_LEFT, draynor_locs.manor_bookcase_left)?.let {
            locRepo.del(it, BOOKCASE_OPEN_CYCLES)
        }
        locRepo.findExact(BOOKCASE_RIGHT, draynor_locs.manor_bookcase_right)?.let {
            locRepo.del(it, BOOKCASE_OPEN_CYCLES)
        }
        mes("You hear a click as the secret door opens.")
    }

    /** The front doors let you in and lock behind you; the way out is the east room's back door. */
    private fun ProtectedAccess.frontDoor() {
        if (coords.z > FRONT_DOOR_Z) {
            // Note: message unverified.
            mes("The doors won't open.")
            return
        }
        stepThrough(1)
    }

    // Note: the doors are walked through without swinging open (no open variants in the cache).
    private fun ProtectedAccess.stepThrough(dz: Int) {
        soundSynth(synths.door_open)
        telejump(coords.translateZ(dz))
    }

    // Note: message unverified; the cupboard never runs out.
    private fun ProtectedAccess.takeGarlic() {
        if (inv.isFull()) {
            mes("You don't have room for the garlic.")
            return
        }
        invAdd(inv, draynor_objs.garlic)
        mes("You find a clove of garlic in the cupboard.")
    }

    /** Gloves (not beekeeper's) are needed; bare hands get stung for up to 2 and no nettles. */
    private suspend fun ProtectedAccess.pickNettles(nettles: BoundLocInfo) {
        val gloves = player.worn[Wearpos.Hands.slot]
        if (gloves == null || gloves.id == draynor_objs.beekeeper_gloves.id) {
            mes("You have been stung by the nettles!")
            player.takeInstantHit(
                HitType.Typeless,
                random.of(1, NETTLE_MAX_DAMAGE),
                instantHits,
                hitmark = hitmark_groups.poison_damage,
            )
            return
        }
        if (inv.isFull()) {
            mes("You don't have room for the nettles.")
            return
        }
        arriveDelay()
        anim(seqs.human_pickupfloor)
        delay(1)
        // Someone else may have picked them while we reached for them.
        if (!isStillThere(nettles)) {
            return
        }
        locRepo.del(nettles, NETTLE_RESPAWN_CYCLES)
        invAdd(inv, draynor_objs.nettles)
        // Note: messages unverified.
        mes("You pick a handful of nettles.")
    }

    // `del` reports success even for a map loc that is already gone, so look it up instead.
    private fun isStillThere(loc: BoundLocInfo): Boolean =
        locRepo.findAll(loc.coords).any { it.entity == loc.entity }

    private companion object {
        const val TRAPDOOR_OPEN_CYCLES = 300
        const val CABBAGE_RESPAWN_CYCLES = 100
        const val BOOKCASE_OPEN_CYCLES = 30
        const val CUPBOARD_OPEN_CYCLES = 500
        const val NETTLE_RESPAWN_CYCLES = 13
        const val NETTLE_MAX_DAMAGE = 2

        const val FRONT_DOOR_Z = 3353
        const val BACK_DOOR_Z = 3361

        val SEWER_TRAPDOOR = CoordGrid(3118, 3244)
        val BASEMENT_LANDING = CoordGrid(3104, 9576)
        val TOWER_LANDING = CoordGrid(3105, 3162)
        val BOOKCASE_LEFT = CoordGrid(3097, 3358)
        val BOOKCASE_RIGHT = CoordGrid(3097, 3359)
        val CRYPT_LANDING = CoordGrid(3077, 9771)
        val CRYPT_EXIT = CoordGrid(3115, 3356)
        val CELLAR_LANDING = CoordGrid(3117, 9753)
        val CELLAR_EXIT = CoordGrid(3092, 3361)
    }
}
