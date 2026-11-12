package org.rsmod.content.areas.misc.wilderness.locs

import jakarta.inject.Inject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign
import org.rsmod.api.combat.commons.poison.curePoison
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.disablePrayers
import org.rsmod.api.player.isTeleblocked
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.righthand
import org.rsmod.api.player.stat.stat
import org.rsmod.api.player.stat.statBase
import org.rsmod.api.player.stat.statRestore
import org.rsmod.api.random.GameRandom
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLoc3
import org.rsmod.api.script.onPlayerQueueWithArgs
import org.rsmod.content.areas.misc.wilderness.configs.WildernessQueues
import org.rsmod.content.areas.misc.wilderness.configs.WildernessSeqs
import org.rsmod.content.areas.misc.wilderness.configs.WildernessSpots
import org.rsmod.content.areas.misc.wilderness.configs.wilderness_locs
import org.rsmod.game.entity.PlayerList
import org.rsmod.game.entity.util.PathingEntityCommon
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.stat.StatTypeList
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext
import org.rsmod.routefinder.collision.CollisionFlagMap

/**
 * The Ferox Enclave's barrier, pool, altar, stairs and bank chest, the teleport levers, the six
 * obelisks, the dungeon stairs and the webs. The Mage Arena's own entrance levers belong to the
 * arena minigame, which is out of scope. Ladders, doors and banks join the generic groups in
 * [org.rsmod.content.areas.misc.wilderness.configs.WildernessLocEditor].
 */
class WildernessLocScript
@Inject
constructor(
    private val locRepo: LocRepository,
    private val collision: CollisionFlagMap,
    private val players: PlayerList,
    private val statTypes: StatTypeList,
    private val objTypes: ObjTypeList,
    private val random: GameRandom,
) : PluginScript() {
    override fun ScriptContext.startup() {
        // Ferox Enclave.
        for (barrier in wilderness_locs.ferox_barriers) {
            onOpLoc1(barrier) { feroxBarrier(it.loc) }
        }
        onOpLoc1(wilderness_locs.ferox_pool) { drinkFromPool() }
        onOpLoc1(wilderness_locs.ferox_altar) { prayAt() }
        onOpLoc1(wilderness_locs.ferox_stairs_up) { climbFeroxStairs(up = true) }
        onOpLoc1(wilderness_locs.ferox_stairs_down) { climbFeroxStairs(up = false) }
        for (chest in listOf(wilderness_locs.ferox_bank_chest, wilderness_locs.mage_bank_chest)) {
            onOpLoc3(chest) { mes("You have nothing to collect.") }
        }

        // The way down is in Falador's script: the stairs share a type with its mine's.
        onOpLoc1(wilderness_locs.dungeon_stairs_up) {
            // Yanille's two cellars (UNVERIFIED: the transport table's landings).
            telejump(YANILLE_STAIRS_UP[it.loc.coords] ?: coords.translateZ(-DUNGEON_Z))
        }

        // Levers.
        onOpLoc1(wilderness_locs.edgeville_lever) {
            pullLever(KEEP, "... and teleport into the Wilderness.")
        }
        onOpLoc1(wilderness_locs.ardougne_lever) {
            pullLever(KEEP, "... and teleport into the Wilderness.")
        }
        onOpLoc1(wilderness_locs.keep_lever) { keepLever() }
        onOpLoc1(wilderness_locs.bank_lever_in) {
            pullLever(MAGE_BANK, "... and teleport into the Mage Arena bank.")
        }
        onOpLoc1(wilderness_locs.bank_lever_out) {
            pullLever(MAGE_BANK_EXIT, "... and teleport out of the Mage Arena bank.")
        }

        // Obelisks.
        for ((index, obelisk) in wilderness_locs.obelisks.withIndex()) {
            onOpLoc1(obelisk) { activateObelisk(index) }
        }

        onPlayerQueueWithArgs<Int>(WildernessQueues.obelisk) { fireObelisk(it.args) }

        onOpLoc1(wilderness_locs.web) { slashWeb(it.loc) }
    }

    /** The barrier's tile is the enclave's last: crossing moves one tile out, or back onto it. */
    private fun ProtectedAccess.feroxBarrier(barrier: BoundLocInfo) {
        if (player.isTeleblocked()) {
            mes(TELEBLOCKED)
            return
        }
        val dx = barrier.coords.x - FEROX_CENTRE.x
        val dz = barrier.coords.z - FEROX_CENTRE.z
        val outside =
            if (abs(dx) > abs(dz)) {
                barrier.coords.translateX(dx.sign)
            } else {
                barrier.coords.translateZ(dz.sign)
            }
        telejump(if (coords == barrier.coords) outside else barrier.coords)
    }

    /** The pools fully restore a player, but not their special attack energy. */
    private fun ProtectedAccess.drinkFromPool() {
        statRestoreAll(statTypes.values)
        player.runEnergy = constants.run_max_energy
        player.disablePrayers()
        player.curePoison()
        mes("You feel reinvigorated after drinking from the pool.")
    }

    private fun ProtectedAccess.prayAt() {
        if (player.stat(stats.prayer) >= player.statBase(stats.prayer)) {
            mes("You already have full Prayer points.")
            return
        }
        anim(WildernessSeqs.pray)
        player.statRestore(stats.prayer)
        mes("You recharge your Prayer points.")
    }

    /** Each staircase moves the player three tiles through it; the table is RuneLite's list. */
    private fun ProtectedAccess.climbFeroxStairs(up: Boolean) {
        val route =
            FEROX_STAIRS.map { if (up) it else it.second to it.first }
                .filter { it.first.level == coords.level }
                .minByOrNull { max(abs(it.first.x - coords.x), abs(it.first.z - coords.z)) }
        if (route != null) {
            telejump(route.second)
        }
    }

    // The Easy Wilderness Diary is out of scope, but the lever offers Edgeville as well as
    // Ardougne as if it were done: otherwise a player who came in by the Edgeville lever could
    // only walk back out through the whole Wilderness.
    private suspend fun ProtectedAccess.keepLever() {
        if (player.isTeleblocked()) {
            mes(TELEBLOCKED)
            return
        }
        startDialogue {
            val dest = choice2("Ardougne.", ARDOUGNE, "Edgeville.", EDGEVILLE, title = "Where to?")
            pullLever(dest, "... and teleport out of the Wilderness.")
        }
    }

    private suspend fun ProtectedAccess.pullLever(dest: CoordGrid, message: String) {
        if (player.isTeleblocked()) {
            mes(TELEBLOCKED)
            return
        }
        anim(WildernessSeqs.lever)
        mes("You pull the lever...")
        delay(2)
        anim(WildernessSeqs.teleport)
        spotanim(WildernessSpots.teleport)
        delay(3)
        teleport(dest)
        resetAnim()
        mes(message)
    }

    // Note: the delay and the glow time are not on the wiki. Someone who clicks a pillar from
    // outside the 3x3 grid has these ticks to step into it.
    private fun ProtectedAccess.activateObelisk(index: Int) {
        if (player.isTeleblocked()) {
            mes(TELEBLOCKED)
            return
        }
        val (x, z) = OBELISKS[index]
        val corners = listOf(0, 4).flatMap { dx -> listOf(0, 4).map { CoordGrid(x + dx, z + it) } }
        if (corners.any { locRepo.findExact(it, wilderness_locs.obelisk_glowing) != null }) {
            return
        }
        for (corner in corners) {
            val pillar = locRepo.findExact(corner, wilderness_locs.obelisks[index])
            if (pillar != null) {
                locRepo.change(pillar, wilderness_locs.obelisk_glowing, OBELISK_DELAY + 1)
            }
        }
        strongQueue(WildernessQueues.obelisk, OBELISK_DELAY, index)
    }

    /** Everyone in the grid goes to the same random obelisk, a random tile of its grid. */
    private fun ProtectedAccess.fireObelisk(index: Int) {
        val (x, z) = OBELISKS[index]
        val (destX, destZ) = OBELISKS[random.of(OBELISKS.size)]
        val centre = CoordGrid(x + 2, z + 2)
        for (other in players) {
            val outside = abs(other.coords.x - centre.x) > 1 || abs(other.coords.z - centre.z) > 1
            if (outside || other.coords.level != 0 || other.isTeleblocked()) {
                continue
            }
            val dest = CoordGrid(destX + 1 + random.of(3), destZ + 1 + random.of(3))
            if (other == player) {
                teleport(dest)
            } else {
                PathingEntityCommon.teleport(other, collision, dest)
            }
            other.mes("Ancient magic teleports you somewhere in the Wilderness!")
        }
    }

    /** A knife or a weapon that slashes cuts the web, which grows back after a while. */
    private fun ProtectedAccess.slashWeb(web: BoundLocInfo) {
        val weapon = player.righthand
        val slashes = weapon != null && (objTypes[weapon].paramOrNull(params.attack_slash) ?: 0) > 0
        if (objs.knife !in inv && !slashes) {
            mes("Only a sharp blade can cut through this sticky web.")
            return
        }
        // Note: the message and the respawn time are not on the wiki, nor the success chance.
        locRepo.change(web, wilderness_locs.web_slashed, WEB_TICKS)
        mes("You slash the web apart.")
    }

    private companion object {
        const val TELEBLOCKED = "A magical force stops you from teleporting."
        const val DUNGEON_Z = 6400

        val YANILLE_STAIRS_UP =
            mapOf(
                CoordGrid(2569, 9522, 0) to CoordGrid(2569, 3121, 0),
                CoordGrid(2603, 9478, 0) to CoordGrid(2606, 3078, 0),
            )
        const val OBELISK_DELAY = 6
        const val WEB_TICKS = 100

        val FEROX_CENTRE = CoordGrid(3139, 3628)

        // UNVERIFIED: the lever destinations are recalled from memory, not the wiki.
        val KEEP = CoordGrid(3154, 3924)
        val ARDOUGNE = CoordGrid(2561, 3311)
        val EDGEVILLE = CoordGrid(3087, 3500)
        val MAGE_BANK = CoordGrid(2539, 4713)
        val MAGE_BANK_EXIT = CoordGrid(3091, 3956)

        /** South-west pillars of the six obelisks, in the order of their loc types. */
        val OBELISKS =
            listOf(
                2978 to 3864,
                3033 to 3730,
                3104 to 3792,
                3154 to 3618,
                3225 to 3665,
                3305 to 3914,
            )

        private fun stair(fx: Int, fz: Int, fl: Int, tx: Int, tz: Int, tl: Int) =
            CoordGrid(fx, fz, fl) to CoordGrid(tx, tz, tl)

        /** (from, to) climbing up; the way down is the reverse. */
        val FEROX_STAIRS =
            listOf(
                stair(3140, 3637, 0, 3137, 3637, 1),
                stair(3140, 3636, 0, 3137, 3636, 1),
                stair(3143, 3637, 0, 3146, 3637, 1),
                stair(3143, 3636, 0, 3146, 3636, 1),
                stair(3142, 3634, 1, 3142, 3637, 2),
                stair(3141, 3634, 1, 3141, 3637, 2),
                stair(3134, 3622, 0, 3131, 3622, 1),
                stair(3134, 3621, 0, 3131, 3621, 1),
                stair(3135, 3622, 0, 3138, 3622, 1),
                stair(3135, 3621, 0, 3138, 3621, 1),
            )
    }
}
