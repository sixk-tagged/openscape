package org.rsmod.content.skills.fishing.scripts

import jakarta.inject.Inject
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.clearInteractionRoute
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.fishingLvl
import org.rsmod.api.random.GameRandom
import org.rsmod.api.repo.npc.NpcRepository
import org.rsmod.api.repo.player.PlayerRepository
import org.rsmod.api.script.onAiTimer
import org.rsmod.api.script.onEvent
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.stats.levelmod.InvisibleLevels
import org.rsmod.api.stats.xpmod.XpModifiers
import org.rsmod.api.type.editors.npc.NpcEditor
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.npc.NpcStateEvents
import org.rsmod.game.interact.InteractionNpc
import org.rsmod.game.type.npc.NpcType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.seq.SeqType
import org.rsmod.map.CoordGrid
import org.rsmod.map.zone.ZoneKey
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext
import org.rsmod.routefinder.collision.CollisionFlagMap
import org.rsmod.routefinder.flag.CollisionFlag

class Fishing
@Inject
constructor(
    private val objTypes: ObjTypeList,
    private val xpMods: XpModifiers,
    private val invisibleLvls: InvisibleLevels,
    private val random: GameRandom,
    private val collision: CollisionFlagMap,
    private val npcRepo: NpcRepository,
    private val playerRepo: PlayerRepository,
) : PluginScript() {
    override fun ScriptContext.startup() {
        for ((spot, methods) in FishingData.spots) {
            onOpNpc1(spot) { fish(it.npc, methods.first) }
            methods.second?.let { second -> onOpNpc3(spot) { fish(it.npc, second) } }
            if (spot !in FishingData.fixed) {
                onAiTimer(spot) { move(npc) }
            }
        }
        val movingIds = (FishingData.spots.keys - FishingData.fixed).map { it.id }.toSet()
        onEvent<NpcStateEvents.Create> {
            if (npc.type.id in movingIds) {
                npc.aiTimer(moveDelay())
            }
        }
    }

    private fun ProtectedAccess.fish(spot: Npc, method: FishingData.Method) {
        val lowest = method.catches.minOf { it.level }
        if (player.fishingLvl < lowest) {
            mes("You need a Fishing level of at least $lowest to ${method.verb} these fish.")
            return
        }
        if (method.tool !in inv) {
            val article = if (method.toolName.first() in "aeiou") "an" else "a"
            mes("You need $article ${method.toolName} to ${method.verb} these fish.")
            return
        }
        val bait = method.bait
        if (bait != null && bait.obj !in inv) {
            mes("You don't have any ${bait.name} left.")
            return
        }
        if (inv.isFull()) {
            mes("You can't carry any more fish.")
            return
        }

        if (actionDelay < mapClock) {
            actionDelay = mapClock + FISHING_TICKS
            skillAnimDelay = mapClock
            mes(method.castMessage)
        }
        if (skillAnimDelay <= mapClock) {
            skillAnimDelay = mapClock + 4
            anim(method.anim)
        }
        if (actionDelay == mapClock) {
            actionDelay = mapClock + FISHING_TICKS
            val caught = rollCatch(method)
            if (caught != null) {
                if (bait != null) {
                    invDel(inv, bait.obj, 1)
                }
                val name = objTypes[caught.fish].name.lowercase().removePrefix("raw ")
                spam("You catch ${if (name.endsWith("s")) "some" else "a"} $name.")
                statAdvance(stats.fishing, caught.xp * xpMods.get(player, stats.fishing))
                invAdd(inv, caught.fish)
                if (inv.isFull()) {
                    resetAnim()
                    return
                }
            }
        }
        if (method.op == 1) opNpc1(spot) else opNpc3(spot)
    }

    /** Best fish first, each with its own wiki success roll ("cascade"), as OSRS does. */
    private fun ProtectedAccess.rollCatch(method: FishingData.Method): FishingData.Catch? =
        method.catches
            .filter { player.fishingLvl >= it.level }
            .sortedByDescending { it.level }
            .firstOrNull { statRandom(stats.fishing, it.low, it.high, invisibleLvls) }

    private fun move(spot: Npc) {
        spot.aiTimer(moveDelay())
        val home = spot.spawnCoords
        val options =
            (-MOVE_RADIUS..MOVE_RADIUS)
                .flatMap { dx -> (-MOVE_RADIUS..MOVE_RADIUS).map { dz -> home.translate(dx, dz) } }
                .filter { it != spot.coords && isFishable(it) && npcRepo.findAll(it).none() }
        if (options.isNotEmpty()) {
            stopFishers(spot)
            spot.telejump(collision, options[random.of(options.size)])
        }
    }

    /** Whoever fishes [spot] stops when it moves, as in OSRS, instead of following it. */
    private fun stopFishers(spot: Npc) {
        for (player in playerRepo.findAll(ZoneKey.from(spot.coords), zoneRadius = 1)) {
            val interaction = player.interaction
            if (interaction is InteractionNpc && interaction.target == spot) {
                // The route too: it would still walk them over to where the spot went.
                player.clearInteractionRoute()
                player.resetFaceEntity()
                player.resetAnim()
            }
        }
    }

    /**
     * Open water: blocked floor with no loc and a walkable tile beside it to fish from. Floor is
     * also blocked on mud banks and under piers, so at least [MIN_WATER_AROUND] of its eight
     * neighbours must be blocked floor too.
     */
    private fun isFishable(coords: CoordGrid): Boolean {
        if (!isBlocked(coords, CollisionFlag.BLOCK_WALK) || isBlocked(coords, CollisionFlag.LOC)) {
            return false
        }
        val beside =
            listOf(
                coords.translate(1, 0),
                coords.translate(-1, 0),
                coords.translate(0, 1),
                coords.translate(0, -1),
            )
        val shore = beside.any { !isBlocked(it, CollisionFlag.BLOCK_WALK or CollisionFlag.LOC) }
        val around = (-1..1).flatMap { dx -> (-1..1).map { dz -> coords.translate(dx, dz) } }
        val water = around.count { isBlocked(it, CollisionFlag.BLOCK_WALK) } - 1
        return shore && water >= MIN_WATER_AROUND
    }

    private fun isBlocked(coords: CoordGrid, flags: Int): Boolean =
        collision[coords.x, coords.z, coords.level] and flags != 0

    /** OSRS spots move after 250 to 530 ticks (Mod Ash, OSRS wiki "Fishing spots"). */
    private fun moveDelay(): Int = random.of(250, 530)

    private companion object {
        const val FISHING_TICKS = 5
        const val MOVE_RADIUS = 2
        const val MIN_WATER_AROUND = 4
    }
}

/** Fishing methods and catch rates from the OSRS wiki skilling success charts (Oct 2025). */
object FishingData {
    data class Catch(
        val fish: ObjType,
        val level: Int,
        val xp: Double,
        val low: Int,
        val high: Int,
    )

    data class Bait(val obj: ObjType, val name: String)

    data class Method(
        val op: Int,
        val verb: String,
        val tool: ObjType,
        val toolName: String,
        val bait: Bait?,
        val anim: SeqType,
        val castMessage: String,
        val catches: List<Catch>,
    )

    private val fishingBait = Bait(FishingObjs.fishing_bait, "fishing bait")

    val smallNet =
        Method(
            op = 1,
            verb = "catch",
            tool = FishingObjs.net,
            toolName = "small fishing net",
            bait = null,
            anim = FishingSeqs.smallnet,
            castMessage = "You cast out your net...",
            catches =
                listOf(
                    Catch(FishingObjs.raw_shrimp, 1, 10.0, 48, 256),
                    Catch(FishingObjs.raw_anchovies, 15, 40.0, 24, 128),
                ),
        )

    val seaBait =
        Method(
            op = 3,
            verb = "bait",
            tool = FishingObjs.fishing_rod,
            toolName = "fishing rod",
            bait = fishingBait,
            anim = FishingSeqs.rod,
            castMessage = "You cast out your line...",
            catches =
                listOf(
                    Catch(FishingObjs.raw_sardine, 5, 20.0, 32, 192),
                    Catch(FishingObjs.raw_herring, 10, 30.0, 24, 128),
                ),
        )

    val lure =
        Method(
            op = 1,
            verb = "lure",
            tool = FishingObjs.fly_fishing_rod,
            toolName = "fly fishing rod",
            bait = Bait(FishingObjs.feather, "feathers"),
            anim = FishingSeqs.rod,
            castMessage = "You cast out your line...",
            catches =
                listOf(
                    Catch(FishingObjs.raw_trout, 20, 50.0, 32, 192),
                    Catch(FishingObjs.raw_salmon, 30, 70.0, 16, 96),
                ),
        )

    val riverBait =
        Method(
            op = 3,
            verb = "bait",
            tool = FishingObjs.fishing_rod,
            toolName = "fishing rod",
            bait = fishingBait,
            anim = FishingSeqs.rod,
            castMessage = "You cast out your line...",
            catches = listOf(Catch(FishingObjs.raw_pike, 25, 60.0, 16, 96)),
        )

    val cage =
        Method(
            op = 1,
            verb = "catch",
            tool = FishingObjs.lobster_pot,
            toolName = "lobster pot",
            bait = null,
            anim = FishingSeqs.cage,
            castMessage = "You attempt to catch a lobster...",
            catches = listOf(Catch(FishingObjs.raw_lobster, 40, 90.0, 6, 95)),
        )

    /** The swordfish roll goes first, then tuna. */
    val harpoon =
        Method(
            op = 3,
            verb = "harpoon",
            tool = FishingObjs.harpoon,
            toolName = "harpoon",
            bait = null,
            anim = FishingSeqs.harpoon,
            castMessage = "You start harpooning fish...",
            catches =
                listOf(
                    Catch(FishingObjs.raw_swordfish, 50, 100.0, 4, 48),
                    Catch(FishingObjs.raw_tuna, 35, 80.0, 8, 64),
                ),
        )

    /** Lava eels are baited with the oily rod; the lava spot has no other option. */
    val lavaEel =
        Method(
            op = 1,
            verb = "bait",
            tool = FishingObjs.oily_fishing_rod,
            toolName = "oily fishing rod",
            bait = fishingBait,
            anim = FishingSeqs.rod,
            castMessage = "You cast out your line...",
            catches = listOf(Catch(FishingObjs.raw_lava_eel, 53, 60.0, 16, 96)),
        )

    /** Dark crabs use a lobster pot and dark fishing bait, spent on each catch. */
    val darkCrab =
        Method(
            op = 1,
            verb = "catch",
            tool = FishingObjs.lobster_pot,
            toolName = "lobster pot",
            bait = Bait(FishingObjs.wilderness_fishing_bait, "dark fishing bait"),
            anim = FishingSeqs.cage,
            // UNVERIFIED: the wording of the dark crab start message.
            castMessage = "You attempt to catch a dark crab...",
            // The Elite Wilderness Diary's better rates are not built.
            catches = listOf(Catch(FishingObjs.raw_dark_crab, 85, 130.0, 3, 40)),
        )

    /** Spot npc type -> (op1 method, op3 method or null). Only spawned spots are listed. */
    val spots: Map<NpcType, Pair<Method, Method?>> =
        mapOf(
            FishingNpcs.lumbridge_sea_spot to (smallNet to seaBait),
            FishingNpcs.lumbridge_river_spot to (lure to riverBait),
            FishingNpcs.draynor_sea_spot to (smallNet to seaBait),
            FishingNpcs.alkharid_sea_spot to (smallNet to seaBait),
            FishingNpcs.barbarian_river_spot to (lure to riverBait),
            FishingNpcs.musa_sea_spot to (smallNet to seaBait),
            FishingNpcs.mudskipper_sea_spot to (smallNet to seaBait),
            FishingNpcs.musa_cage_spot to (cage to harpoon),
            FishingNpcs.bandit_camp_sea_spot to (smallNet to seaBait),
            FishingNpcs.lava_eel_spot to (lavaEel to null),
            FishingNpcs.resource_area_crab_spot to (darkCrab to null),
            FishingNpcs.wilderness_crab_spot to (darkCrab to null),
        )

    /**
     * Spots that never relocate: the Resource Area's dark crabs (OSRS wiki, Raw dark crab), and the
     * lava eels, as the lava around them is all covered by locs and offers no other tile.
     */
    val fixed = setOf(FishingNpcs.resource_area_crab_spot, FishingNpcs.lava_eel_spot)
}

/**
 * Spots relocate through [Fishing] on a timer, and nothing else may move them: left to the default
 * wander mode they walk out of the water onto the shore, and the wander mode's "return to spawn"
 * would undo each relocation.
 */
object FishingNpcEdits : NpcEditor() {
    init {
        for (spot in FishingData.spots.keys) {
            edit(spot) {
                defaultMode = none
                moveRestrict = nomove
                wanderRange = 0
            }
        }
    }
}

object FishingNpcs : NpcReferences() {
    val lumbridge_sea_spot = find("0_50_49_saltfish")
    val lumbridge_river_spot = find("0_50_50_freshfish")
    val draynor_sea_spot = find("0_48_50_saltfish")
    val alkharid_sea_spot = find("0_51_49_saltfish")
    val barbarian_river_spot = find("0_48_53_freshfish")
    val musa_sea_spot = find("0_45_49_saltfish")
    val mudskipper_sea_spot = find("0_46_49_saltfish")
    val musa_cage_spot = find("0_45_49_rarefish")
    val bandit_camp_sea_spot = find("0_47_57_saltfish")
    val lava_eel_spot = find("0_47_59_lavafish")
    val resource_area_crab_spot = find("49_61_crabs")
    val wilderness_crab_spot = find("52_59_crabs")
}

object FishingObjs : ObjReferences() {
    val net = find("net")
    val fishing_rod = find("fishing_rod")
    val fly_fishing_rod = find("fly_fishing_rod")
    val fishing_bait = find("fishing_bait")
    val oily_fishing_rod = find("oily_fishing_rod")
    val wilderness_fishing_bait = find("wilderness_fishing_bait")
    val feather = find("feather")
    val raw_shrimp = find("raw_shrimp")
    val raw_anchovies = find("raw_anchovies")
    val raw_sardine = find("raw_sardine")
    val raw_herring = find("raw_herring")
    val raw_trout = find("raw_trout")
    val raw_salmon = find("raw_salmon")
    val raw_pike = find("raw_pike")
    val lobster_pot = find("lobster_pot")
    val harpoon = find("harpoon")
    val raw_lobster = find("raw_lobster")
    val raw_tuna = find("raw_tuna")
    val raw_swordfish = find("raw_swordfish")
    val raw_lava_eel = find("raw_lava_eel")
    val raw_dark_crab = find("raw_dark_crab")
}

object FishingSeqs : SeqReferences() {
    val smallnet = find("human_smallnet")
    val rod = find("human_fishing_casting")
    val cage = find("human_lobster")
    val harpoon = find("human_harpoon")
}
