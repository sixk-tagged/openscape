package org.rsmod.content.areas.city.varrock.locs

import jakarta.inject.Inject
import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.seqs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.synths
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.player.back
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.hat
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.cookingLvl
import org.rsmod.api.player.stat.stat
import org.rsmod.api.player.stat.statBase
import org.rsmod.api.player.stat.statRestore
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLoc2
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.content.areas.city.varrock.configs.varrock_locs
import org.rsmod.content.areas.city.varrock.configs.varrock_npcs
import org.rsmod.game.inv.InvObj
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.loc.LocAngle
import org.rsmod.game.loc.LocShape
import org.rsmod.game.type.content.ContentGroupType
import org.rsmod.game.type.loc.LocType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.seq.SeqType
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Varrock's straight staircases, the sewer manhole, drawers and chests, the west bank's locked
 * door, both altars and the Cooks' Guild door. Doors, gates, ladders, spiral stairs and search
 * crates join the generic content groups in [org.rsmod.content.areas.city.varrock.configs].
 */
class VarrockLocScripts
@Inject
constructor(private val locRepo: LocRepository, private val objTypes: ObjTypeList) :
    PluginScript() {
    override fun ScriptContext.startup() {
        for (stairs in
            listOf(
                varrock_locs.stairs,
                varrock_locs.stairs_taller,
                varrock_locs.stairs_castle,
                varrock_locs.stairs_palace,
            )) {
            onOpLoc1(stairs) { climbUp(it.loc) }
        }
        onOpLoc1(varrock_locs.stairs_top) { climbDown(it.loc) }

        onOpLoc1(varrock_locs.manhole_closed) {
            locRepo.change(it.loc, varrock_locs.manhole_open, 500)
        }
        onOpLoc1(varrock_locs.manhole_open) { climbDownManhole() }
        onOpLoc2(varrock_locs.manhole_open) {
            locRepo.change(it.loc, varrock_locs.manhole_closed, CONTAINER_DURATION)
        }

        container(varrock_locs.chest_closed, varrock_locs.chest_open, "chest")
        container(varrock_locs.drawers, varrock_locs.drawers_open, "drawers")
        container(varrock_locs.posh_drawers, varrock_locs.posh_drawers_open, "drawers")

        onOpLoc1(varrock_locs.bank_locked_door) { knockAtBank() }
        // Note: there is no Grand Exchange yet, so the counter booth only says so.
        onOpLoc1(varrock_locs.ge_exchange_booth) { mes("The Grand Exchange is not available yet.") }

        onOpLoc1(varrock_locs.church_altar) { prayAt() }
        onOpLoc1(varrock_locs.chaos_altar) { prayAt() }

        onOpLoc1(varrock_locs.cooks_guild_door) { cooksGuildDoor(it.loc) }
    }

    /**
     * The staircases are 2 wide and 3 long going up, with a 2x2 landing loc on the floor above. The
     * walkable landing is the tile just past the far end, in the direction the stairs rise: north,
     * east, south and west for angles 0 to 3 (read off the placed up/top loc pairs).
     */
    private fun ProtectedAccess.climbUp(stairs: BoundLocInfo) {
        val x = stairs.coords.x
        val z = stairs.coords.z
        val lane = coords
        val level = stairs.coords.level + 1
        val dest =
            when (stairs.angle) {
                LocAngle.West ->
                    CoordGrid(
                        lane.x.coerceIn(x, x + stairs.adjustedWidth - 1),
                        z + stairs.adjustedLength,
                        level,
                    )
                LocAngle.North ->
                    CoordGrid(
                        x + stairs.adjustedWidth,
                        lane.z.coerceIn(z, z + stairs.adjustedLength - 1),
                        level,
                    )
                LocAngle.East ->
                    CoordGrid(lane.x.coerceIn(x, x + stairs.adjustedWidth - 1), z - 1, level)
                LocAngle.South ->
                    CoordGrid(x - 1, lane.z.coerceIn(z, z + stairs.adjustedLength - 1), level)
            }
        telejump(dest)
    }

    /** The landing at the bottom is just past the low end of the 3 long stairs below [top]. */
    private fun ProtectedAccess.climbDown(top: BoundLocInfo) {
        val x = top.coords.x
        val z = top.coords.z
        val lane = coords
        val level = top.coords.level - 1
        val dest =
            when (top.angle) {
                LocAngle.West ->
                    CoordGrid(lane.x.coerceIn(x, x + top.adjustedWidth - 1), z - 2, level)
                LocAngle.North ->
                    CoordGrid(x - 2, lane.z.coerceIn(z, z + top.adjustedLength - 1), level)
                LocAngle.East ->
                    CoordGrid(lane.x.coerceIn(x, x + top.adjustedWidth - 1), z + 3, level)
                LocAngle.South ->
                    CoordGrid(x + 3, lane.z.coerceIn(z, z + top.adjustedLength - 1), level)
            }
        telejump(dest)
    }

    // The ladder up is in a dead-end corridor two tiles wide; this tile is in front of it.
    private suspend fun ProtectedAccess.climbDownManhole() {
        arriveDelay()
        anim(seqs.human_pickupfloor)
        delay(1)
        telejump(SEWER_LADDER_LANDING)
    }

    /** Chests and drawers: Open swaps in the open type, which can be searched (empty) or shut. */
    private fun ScriptContext.container(closed: LocType, open: LocType, noun: String) {
        onOpLoc1(closed) { change(it.loc, open, VarrockSeqs.open_chest) }
        onOpLoc1(open) { mes("You search the $noun but find nothing.") }
        onOpLoc2(open) { change(it.loc, closed, VarrockSeqs.close_chest) }
    }

    private suspend fun ProtectedAccess.change(loc: BoundLocInfo, into: LocType, seq: SeqType) {
        arriveDelay()
        anim(seq)
        locRepo.change(loc, into, CONTAINER_DURATION)
    }

    private suspend fun ProtectedAccess.knockAtBank() {
        arriveDelay()
        startDialogue {
            chatPlayer(sad, "I don't think I'm ever going to be allowed in there.")
            mes("<col=0000ff>Knock knock...</col>")
            banker("Who's there?")
            val choice =
                choice5(
                    "I'm ${player.displayName}. Please let me in.",
                    1,
                    "Boo.",
                    2,
                    "Kanga.",
                    3,
                    "Thank.",
                    4,
                    "Doctor.",
                    5,
                )
            when (choice) {
                1 -> {
                    chatPlayer(neutral, "I'm ${player.displayName}. Please let me in.")
                    banker("No. Staff only beyond this point. You can't come in here.")
                }
                2 -> {
                    chatPlayer(neutral, "Boo.")
                    banker("Boo who?")
                    chatPlayer(laugh, "There's no need to cry!")
                    banker("What? I'm not... oh, just go away!")
                }
                3 -> {
                    chatPlayer(neutral, "Kanga.")
                    banker("Kanga who?")
                    chatPlayer(laugh, "No, 'kangaroo'.")
                    banker("Stop messing about and go away!")
                }
                4 -> {
                    chatPlayer(neutral, "Thank.")
                    banker("Thank who?")
                    chatPlayer(laugh, "You're welcome!")
                    banker("Stop it!")
                }
                5 -> {
                    chatPlayer(neutral, "Doctor.")
                    banker("Doctor wh.. hang on, I'm not falling for that one again! Go away.")
                }
            }
        }
    }

    private suspend fun Dialogue.banker(text: String) =
        chatNpcSpecific("Banker", varrock_npcs.bank_doorguard, quiz, text)

    private fun ProtectedAccess.prayAt() {
        if (player.stat(stats.prayer) >= player.statBase(stats.prayer)) {
            mes("You already have full Prayer points.")
            return
        }
        anim(VarrockSeqs.pray)
        player.statRestore(stats.prayer)
        vars[varbits.prayer_drain_counter] = 0
        mes("You recharge your Prayer points.")
    }

    /**
     * The guild door: 32 Cooking and a chef's hat, Cooking cape or hood, or a max cape or hood worn
     * when going in from the street (south); leaving needs nothing.
     */
    private suspend fun ProtectedAccess.cooksGuildDoor(door: BoundLocInfo) {
        val entering = coords.z <= door.coords.z
        if (entering) {
            val hat =
                wornAny(
                    player.hat,
                    CookingGuildObjs.chefs_hat,
                    CookingGuildObjs.chefs_hat_gold,
                    objs.cooking_hood,
                )
            val cape = wornAny(player.back, objs.cooking_skillcape, objs.cooking_skillcape_t)
            val maxGear =
                isMax(player.hat, content.max_hood) || isMax(player.back, content.max_cape)
            val dressed = hat || cape || maxGear
            val message =
                when {
                    player.cookingLvl < GUILD_COOKING_LEVEL && dressed ->
                        "Sorry. Only the finest chefs are allowed in here. Get your cooking " +
                            "level up to 32."
                    player.cookingLvl < GUILD_COOKING_LEVEL ->
                        "Sorry. Only the finest chefs are allowed in here. Get your cooking " +
                            "level up to 32 and come back wearing a chef's hat."
                    !dressed ->
                        "You can't come in here unless you're wearing a chef's hat, or something like that."
                    else -> null
                }
            if (message != null) {
                startDialogue {
                    chatNpcSpecific("Head chef", varrock_npcs.head_chef, neutral, message)
                }
                return
            }
        }
        openDoor(door)
        // Only the checked player goes through; the door shuts behind them.
        telejump(coords.translateZ(if (entering) 1 else -1))
    }

    private fun wornAny(worn: InvObj?, vararg types: ObjType): Boolean =
        worn != null && types.any { it.id == worn.id }

    private fun isMax(worn: InvObj?, group: ContentGroupType): Boolean =
        worn != null && objTypes[worn].isContentType(group)

    /** A wall door swings open one tile along its angle and shuts again right away. */
    private fun ProtectedAccess.openDoor(door: BoundLocInfo) {
        soundSynth(synths.door_open)
        val opened =
            when (door.angle) {
                LocAngle.West -> door.coords.translateX(-1)
                LocAngle.North -> door.coords.translateZ(1)
                LocAngle.East -> door.coords.translateX(1)
                LocAngle.South -> door.coords.translateZ(-1)
            }
        locRepo.del(door, DOOR_DURATION)
        locRepo.add(
            opened,
            varrock_locs.cooks_guild_door_open,
            DOOR_DURATION,
            door.turnAngle(1),
            LocShape.WallStraight,
        )
    }

    private companion object {
        const val CONTAINER_DURATION = 500
        const val DOOR_DURATION = 2
        const val GUILD_COOKING_LEVEL = 32
        val SEWER_LADDER_LANDING = CoordGrid(3237, 9859)
    }
}

object VarrockSeqs : SeqReferences() {
    val open_chest = find("human_openchest")
    val close_chest = find("human_closechest")
    val pray = find("human_pray")
}

object CookingGuildObjs : ObjReferences() {
    val chefs_hat = find("chefs_hat")
    val chefs_hat_gold = find("chefs_hat_gold")
}
