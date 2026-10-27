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
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.npc.NpcStateEvents
import org.rsmod.game.interact.InteractionNpc
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
            onOpNpc3(spot) { fish(it.npc, methods.second) }
            onAiTimer(spot) { move(npc) }
        }
        val spotIds = FishingData.spots.keys.map { it.id }.toSet()
        onEvent<NpcStateEvents.Create> {
            if (npc.type.id in spotIds) {
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
            mes("You need a ${method.toolName} to ${method.verb} these fish.")
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

    /** Open water (blocked floor, no loc) with a walkable tile beside it to fish from. */
    private fun isFishable(coords: CoordGrid): Boolean {
        val flags = collision[coords.x, coords.z, coords.level]
        if (flags and CollisionFlag.BLOCK_WALK == 0 || flags and CollisionFlag.LOC != 0) {
            return false
        }
        val shore = listOf(coords.translate(1, 0), coords.translate(-1, 0))
        return (shore + listOf(coords.translate(0, 1), coords.translate(0, -1))).any {
            collision[it.x, it.z, it.level] and (CollisionFlag.BLOCK_WALK or CollisionFlag.LOC) == 0
        }
    }

    /** OSRS spots move after 250 to 530 ticks (Mod Ash, OSRS wiki "Fishing spots"). */
    private fun moveDelay(): Int = random.of(250, 530)

    private companion object {
        const val FISHING_TICKS = 5
        const val MOVE_RADIUS = 2
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

    /** Spot npc type -> (op1 method, op3 method). Only spawned spots are listed. */
    val spots =
        mapOf(
            FishingNpcs.lumbridge_sea_spot to (smallNet to seaBait),
            FishingNpcs.lumbridge_river_spot to (lure to riverBait),
            FishingNpcs.draynor_sea_spot to (smallNet to seaBait),
            FishingNpcs.alkharid_sea_spot to (smallNet to seaBait),
        )
}

object FishingNpcs : NpcReferences() {
    val lumbridge_sea_spot = find("0_50_49_saltfish")
    val lumbridge_river_spot = find("0_50_50_freshfish")
    val draynor_sea_spot = find("0_48_50_saltfish")
    val alkharid_sea_spot = find("0_51_49_saltfish")
}

object FishingObjs : ObjReferences() {
    val net = find("net")
    val fishing_rod = find("fishing_rod")
    val fly_fishing_rod = find("fly_fishing_rod")
    val fishing_bait = find("fishing_bait")
    val feather = find("feather")
    val raw_shrimp = find("raw_shrimp")
    val raw_anchovies = find("raw_anchovies")
    val raw_sardine = find("raw_sardine")
    val raw_herring = find("raw_herring")
    val raw_trout = find("raw_trout")
    val raw_salmon = find("raw_salmon")
    val raw_pike = find("raw_pike")
}

object FishingSeqs : SeqReferences() {
    val smallnet = find("human_smallnet")
    val rod = find("human_fishing_casting")
}
