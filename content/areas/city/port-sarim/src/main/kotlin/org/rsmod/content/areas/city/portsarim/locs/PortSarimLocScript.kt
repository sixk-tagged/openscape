package org.rsmod.content.areas.city.portsarim.locs

import jakarta.inject.Inject
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.interfaces
import org.rsmod.api.config.refs.seqs
import org.rsmod.api.config.refs.synths
import org.rsmod.api.player.back
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.baseCraftingLvl
import org.rsmod.api.player.stat.craftingLvl
import org.rsmod.api.player.torso
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLoc2
import org.rsmod.api.script.onOpLoc3
import org.rsmod.api.script.onOpLocU
import org.rsmod.content.areas.city.portsarim.configs.port_sarim_locs
import org.rsmod.content.areas.city.portsarim.configs.port_sarim_objs
import org.rsmod.content.areas.city.portsarim.configs.port_sarim_varps
import org.rsmod.game.entity.Player
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.type.loc.LocType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.seq.SeqType
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Port Sarim's doors that check something (Wydin's storeroom, the Crafting Guild, the jail),
 * Wydin's free crates, Luthas's banana plantation, the Crafting Guild's bank chest and a few odds
 * and ends. Ladders, stairs and search-only scenery join the generic content groups in
 * [org.rsmod.content.areas.city.portsarim.configs.PortSarimLocEditor].
 */
class PortSarimLocScript
@Inject
constructor(private val locRepo: LocRepository, private val objTypes: ObjTypeList) :
    PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1(port_sarim_locs.wydin_door) { wydinDoor(it.loc) }
        onOpLoc1(port_sarim_locs.grum_door) { passWest(it.loc) }
        onOpLoc1(port_sarim_locs.guild_door) { guildDoor(it.loc) }
        onOpLoc1(port_sarim_locs.cell_door) { mes("The door is locked.") }
        onOpLoc1(port_sarim_locs.cell_bars) { mes("The cell door is locked.") }

        onOpLoc1(port_sarim_locs.volcano_entrance) { climb(VOLCANO_BOTTOM, seqs.human_pickupfloor) }
        onOpLoc1(port_sarim_locs.volcano_rope) { climb(VOLCANO_TOP, seqs.human_reachforladder) }

        onOpLoc1(port_sarim_locs.guild_chest) {
            if (player.baseCraftingLvl >= GUILD_BANK_LEVEL) {
                ifOpenMainSidePair(main = interfaces.bank_main, side = interfaces.bank_side)
            } else {
                mes("You need a Crafting level of 99 to use the guild's bank.")
            }
        }
        onOpLoc3(port_sarim_locs.guild_chest) { mes("You have nothing to collect.") }

        onOpLoc1(port_sarim_locs.banana_crate) { takeFromCrate(port_sarim_objs.banana, "banana") }
        onOpLoc1(port_sarim_locs.cabbage_crate) {
            takeFromCrate(port_sarim_objs.cabbage, "cabbage")
        }
        onOpLoc1(port_sarim_locs.potato_crate) { takeFromCrate(port_sarim_objs.potato, "potato") }
        onOpLoc1(port_sarim_locs.chicken_crate) {
            takeFromCrate(port_sarim_objs.raw_chicken, "raw chicken")
        }

        onOpLoc1(port_sarim_locs.job_crate) { searchJobCrate() }
        onOpLoc2(port_sarim_locs.job_crate) { fillJobCrate() }
        for (tree in port_sarim_locs.banana_trees) {
            onOpLoc1(tree) { pickBanana(it.loc, tree) }
        }
        onOpLoc1(port_sarim_locs.empty_banana_tree) {
            mes("There are no bananas left on the tree.")
        }

        onOpLoc1(port_sarim_locs.mudskipper_sign) { mes("BEWARE OF THE MUDSKIPPERS!") }
        onOpLoc1(port_sarim_locs.rimmington_spade) { takeSpade() }
        onOpLocU(port_sarim_locs.sand_pile, port_sarim_objs.bucket_empty) { fillBucket() }
    }

    /**
     * These doors are walls on the west edge of their tile with the room to the west, so the door
     * tile is outside. The player steps through; the door stays shut.
     */
    private suspend fun ProtectedAccess.climb(dest: CoordGrid, seq: SeqType) {
        arriveDelay()
        anim(seq)
        delay(1)
        telejump(dest)
    }

    private fun ProtectedAccess.passWest(door: BoundLocInfo) {
        soundSynth(synths.door_open)
        telejump(if (coords.x >= door.coords.x) door.coords.translateX(-1) else door.coords)
    }

    // Note: message unverified.
    private fun ProtectedAccess.wydinDoor(door: BoundLocInfo) {
        val entering = coords.x >= door.coords.x
        if (entering && player.torso?.id != port_sarim_objs.white_apron.id) {
            mes("You need to be wearing a white apron to go in there.")
            return
        }
        passWest(door)
    }

    /** The guild's door is on the south edge of its tile, with the guild to the south. */
    private fun ProtectedAccess.guildDoor(door: BoundLocInfo) {
        val entering = coords.z >= door.coords.z
        if (entering && !canEnterGuild()) {
            // Note: message unverified.
            mes("You need a Crafting level of 40 and to wear a brown apron to enter the guild.")
            return
        }
        soundSynth(synths.door_open)
        telejump(if (entering) door.coords.translateZ(-1) else door.coords)
    }

    private fun ProtectedAccess.canEnterGuild(): Boolean =
        player.craftingLvl >= GUILD_LEVEL && (player.wearsGuildApron() || player.wearsGuildCape())

    private fun Player.wearsGuildApron(): Boolean {
        val apron = torso?.id
        return apron == port_sarim_objs.brown_apron.id || apron == port_sarim_objs.golden_apron.id
    }

    private fun Player.wearsGuildCape(): Boolean {
        val cape = back ?: return false
        return cape.id == port_sarim_objs.crafting_cape.id ||
            cape.id == port_sarim_objs.crafting_cape_trimmed.id ||
            objTypes[cape].isContentType(content.max_cape)
    }

    // Note: the crates give an item per search; the wiki's take-one prompt is skipped.
    private fun ProtectedAccess.takeFromCrate(obj: ObjType, name: String) {
        if (!invAdd(inv, obj).success) {
            mes(constants.dm_invspace)
            return
        }
        mes("You take a $name from the crate.")
    }

    private fun ProtectedAccess.searchJobCrate() {
        val count = vars[port_sarim_varps.banana_crate]
        when (count) {
            0 -> mes("The crate is empty.")
            1 -> mes("The crate contains 1 banana.")
            else -> mes("The crate contains $count bananas.")
        }
    }

    // Note: Fill is unverified; it moves bananas from the inventory until the crate has 10.
    private fun ProtectedAccess.fillJobCrate() {
        if (vars[port_sarim_varps.banana_job] == 0) {
            mes("You should ask Luthas about a job first.")
            return
        }
        val inCrate = vars[port_sarim_varps.banana_crate]
        if (inCrate >= CRATE_SIZE) {
            mes("The crate is already full.")
            return
        }
        val bananas = minOf(CRATE_SIZE - inCrate, invTotal(inv, port_sarim_objs.banana))
        if (bananas == 0) {
            mes("You have no bananas to put in the crate.")
            return
        }
        invDel(inv, port_sarim_objs.banana, bananas)
        vars[port_sarim_varps.banana_crate] = inCrate + bananas
        mes("You fill the crate with $bananas ${if (bananas == 1) "banana" else "bananas"}.")
    }

    // Note: the tree as a whole grows back 5 minutes after the last pick.
    private fun ProtectedAccess.pickBanana(tree: BoundLocInfo, type: LocType) {
        if (!invAdd(inv, port_sarim_objs.banana).success) {
            mes(constants.dm_invspace)
            return
        }
        val trees = port_sarim_locs.banana_trees
        val next = trees.getOrNull(trees.indexOf(type) + 1) ?: port_sarim_locs.empty_banana_tree
        locRepo.change(tree, next, REGROW_TICKS)
        mes("You pick a banana.")
    }

    private fun ProtectedAccess.takeSpade() {
        if (!invAdd(inv, port_sarim_objs.spade).success) {
            mes(constants.dm_invspace)
            return
        }
        mes("You take a spade.")
    }

    private fun ProtectedAccess.fillBucket() {
        invReplace(inv, port_sarim_objs.bucket_empty, 1, port_sarim_objs.bucket_sand)
        mes("You fill the bucket with sand.")
    }

    private companion object {
        const val GUILD_LEVEL = 40
        const val GUILD_BANK_LEVEL = 99
        const val CRATE_SIZE = 10
        const val REGROW_TICKS = 500

        // UNVERIFIED: the reference's landings beside the Rocks (2856,3168) and the rope
        // (2856,9569), which are 6401 tiles apart, so the generic dungeon ladders miss by one.
        val VOLCANO_TOP = CoordGrid(2856, 3167)
        val VOLCANO_BOTTOM = CoordGrid(2856, 9567)
    }
}
