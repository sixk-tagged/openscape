package org.rsmod.content.skills.thieving

import jakarta.inject.Inject
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.player.stat.thievingLvl
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLoc2
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.hit.HitType
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.type.loc.LocType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The trapped Ardougne chests (OSRS wiki): "Search for traps" is the looting action, which finds
 * and disarms the trap, opens the chest and empties it for a while. "Open" always springs the trap.
 */
class ChestThieving @Inject constructor(private val locRepo: LocRepository) : PluginScript() {
    override fun ScriptContext.startup() {
        for (chest in ThievingChests.all) {
            onOpLoc1(chest.type) { springTrap(chest) }
            onOpLoc2(chest.type) { searchForTraps(it.loc, chest) }
        }
    }

    private suspend fun ProtectedAccess.searchForTraps(
        loc: BoundLocInfo,
        chest: ThievingChests.Chest,
    ) {
        if (player.thievingLvl < chest.level) {
            mes("You find nothing.")
            return
        }
        mes("You search the chest for traps.")
        delay(2)
        mes("You find a trap on the chest...")
        mes("You disable the trap.")
        delay(1)
        // Someone else may have emptied it while we searched.
        if (locRepo.findExact(loc.coords, chest.type) == null) {
            return
        }
        if (!invAddAll(chest.loot)) {
            mes(constants.dm_invspace)
            return
        }
        statAdvance(stats.thieving, chest.xp)
        mes("You find treasure inside!")
        locRepo.change(loc, ThievingChests.empty, chest.respawn)
        chest.exit?.let {
            mes("Suddenly a second magical trap triggers.")
            teleport(it)
        }
    }

    private fun ProtectedAccess.springTrap(chest: ThievingChests.Chest) {
        mes("You have activated a trap on the chest.")
        takeInstantHit(HitType.Typeless, chest.trapDamage(player.hitpoints))
    }
}

private val WITCHAVEN = CoordGrid(2696, 3281, 0)

internal object ThievingChests {
    /** [respawn] is the number of ticks the chest stays empty. */
    class Chest(
        val type: LocType,
        val level: Int,
        val xp: Double,
        val respawn: Int,
        val loot: List<Pair<ObjType, Int>>,
        /** Where the second trap sends the player after the loot. */
        val exit: CoordGrid? = null,
        /** The trap's damage from the player's current hitpoints. */
        val trapDamage: (Int) -> Int = { it * 12 / 100 + 3 },
    )

    val empty = ChestLocs.empty

    val all =
        with(ChestObjs) {
            listOf(
                Chest(ChestLocs.coins_10, 13, 7.8, 6, listOf(objs.coins to 10)),
                Chest(
                    ChestLocs.nature_runes,
                    28,
                    25.0,
                    13,
                    listOf(nature_rune to 1, objs.coins to 3),
                ),
                Chest(ChestLocs.coins_50, 43, 125.0, 75, listOf(objs.coins to 50)),
                Chest(
                    ChestLocs.castle,
                    72,
                    500.0,
                    833,
                    listOf(
                        objs.coins to 1000,
                        raw_shark to 1,
                        adamantite_ore to 1,
                        uncut_sapphire to 1,
                    ),
                    exit = WITCHAVEN,
                ),
                // UNVERIFIED: the exit is a tile in Elena's house (the wiki: "the house west of
                // Elena"). The damage is the wiki's formula.
                Chest(
                    ChestLocs.blood_runes,
                    59,
                    250.0,
                    225,
                    listOf(blood_rune to 2, objs.coins to 500),
                    exit = CoordGrid(2592, 3338, 0),
                    trapDamage = { (9 * it + 225) / 50 },
                ),
            )
        }
}

object ChestLocs : LocReferences() {
    val coins_10 = find("trapchest1")
    val nature_runes = find("trapchest2")
    val coins_50 = find("trapchest3")
    val blood_runes = find("trapchest4")
    val castle = find("trapchest5")
    val empty = find("emptytrapchest")
}

object ChestObjs : ObjReferences() {
    val nature_rune = find("naturerune")
    val blood_rune = find("bloodrune")
    val raw_shark = find("raw_shark")
    val adamantite_ore = find("adamantite_ore")
    val uncut_sapphire = find("uncut_sapphire")
}
