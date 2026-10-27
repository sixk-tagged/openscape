package org.rsmod.content.skills.thieving

import jakarta.inject.Inject
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.hit.processor.InstantPlayerHitProcessor
import org.rsmod.api.player.hit.takeInstantHit
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.farmingLvl
import org.rsmod.api.player.stat.thievingLvl
import org.rsmod.api.random.GameRandom
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.api.type.refs.spot.SpotanimReferences
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Player
import org.rsmod.game.hit.HitType
import org.rsmod.game.type.npc.NpcType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Pickpocketing the humans of Lumbridge, Al Kharid, Draynor and Varrock (OSRS wiki levels, xp,
 * loot, rates).
 */
class Pickpocketing @Inject constructor(private val instantHits: InstantPlayerHitProcessor) :
    PluginScript() {
    override fun ScriptContext.startup() {
        for (target in PickpocketTargets.all) {
            for (type in target.npcs) {
                onOpNpc3(type) { pickpocket(it.npc, target) }
            }
        }
    }

    private suspend fun ProtectedAccess.pickpocket(npc: Npc, target: PickpocketTargets.Target) {
        val name = target.name
        if (player.thievingLvl < target.level) {
            mes("You need a Thieving level of ${target.level} to pickpocket the $name.")
            return
        }
        mes("You attempt to pick the $name's pocket.")
        anim(ThievingSeqs.pickpocket)
        delay(1)
        if (!statRandom(stats.thieving, target.low, target.high, invisibleBoost = 0)) {
            stun(npc, target)
            return
        }
        val (obj, count) = target.loot(random, player)
        if (!invAdd(inv, obj, count).success) {
            mes(constants.dm_invspace)
            return
        }
        statAdvance(stats.thieving, target.xp)
        mes("You pick the $name's pocket.")
    }

    private suspend fun ProtectedAccess.stun(npc: Npc, target: PickpocketTargets.Target) {
        mes("You fail to pick the ${target.name}'s pocket.")
        npc.facePlayer(player)
        npc.say("What do you think you're doing?")
        npc.anim(npc.visType.param(params.attack_anim))
        player.takeInstantHit(HitType.Typeless, target.damage, instantHits)
        anim(ThievingSeqs.stunned)
        spotanim(ThievingSpots.stunned, height = 100)
        mes("You've been stunned!")
        delay(target.stunTicks)
    }
}

object PickpocketTargets {
    /** [low]/[high] are the success rolls out of 256 at levels 1 and 99. */
    data class Target(
        val name: String,
        val npcs: List<NpcType>,
        val level: Int,
        val xp: Double,
        val low: Int,
        val high: Int,
        val stunTicks: Int,
        val damage: Int = 1,
        val loot: (GameRandom, Player) -> Pair<ObjType, Int>,
    )

    val all =
        with(ThievingNpcs) {
            listOf(
                Target(
                    "man",
                    listOf(man, man2, man3, man_indoor, al_kharid_man, varrock_man),
                    1,
                    8.0,
                    180,
                    240,
                    8,
                ) { _, _ ->
                    objs.coins to 3
                },
                Target(
                    "woman",
                    listOf(woman, woman2, woman3, varrock_woman),
                    1,
                    8.0,
                    180,
                    240,
                    8,
                ) { _, _ ->
                    objs.coins to 3
                },
                Target(
                    "farmer",
                    listOf(farmer1, farmer2, farmer3, farmer2_f, farmer3_f, farmer4),
                    10,
                    14.5,
                    150,
                    240,
                    8,
                ) { random, _ ->
                    if (random.of(128) < 5) ThievingObjs.potato_seed to 1 else objs.coins to 9
                },
                Target("Al-Kharid warrior", listOf(al_kharid_warrior), 25, 26.0, 100, 240, 8, 2) {
                    _,
                    _ ->
                    objs.coins to 18
                },
                Target("warrior woman", listOf(warrior_woman), 25, 26.0, 100, 240, 8, 2) { _, _ ->
                    objs.coins to 18
                },
                Target(
                    "guard",
                    listOf(
                        varrock_guard,
                        varrock_guard_2,
                        varrock_guard_3,
                        varrock_guard_f,
                        varrock_guard_f2,
                        varrock_guard_f3,
                        varrock_guard_captain,
                    ),
                    40,
                    46.8,
                    50,
                    240,
                    8,
                    2,
                ) { _, _ ->
                    objs.coins to 30
                },
                Target(
                    "Master Farmer",
                    listOf(
                        master_farmer_1,
                        master_farmer_2,
                        master_farmer_1_f,
                        master_farmer_2_f,
                        martin,
                    ),
                    38,
                    43.0,
                    90,
                    240,
                    8,
                    3,
                ) { random, player ->
                    MasterFarmerSeeds.roll(random, player.farmingLvl)
                },
            )
        }
}

object ThievingNpcs : NpcReferences() {
    val man = find("man")
    val man2 = find("man2")
    val man3 = find("man3")
    val man_indoor = find("man_indoor")
    val woman = find("woman")
    val woman2 = find("woman2")
    val woman3 = find("woman3")
    val farmer1 = find("farmer1")
    val farmer2 = find("farmer2")
    val farmer3 = find("farmer3")
    val farmer2_f = find("farmer2_f")
    val farmer3_f = find("farmer3_f")
    val farmer4 = find("farmer4")
    val al_kharid_man = find("al_kharid_man")
    val varrock_man = find("varrock_man1")
    val varrock_woman = find("varrock_woman1")
    val al_kharid_warrior = find("al_kharid_warrior")
    val warrior_woman = find("warrior_woman")
    // Varrock spawns these 2023 guards; the older fai_varrock_guard types are not placed anywhere.
    val varrock_guard = find("fai_varrock_guard02")
    val varrock_guard_2 = find("fai_varrock_guard02_variant01")
    val varrock_guard_3 = find("fai_varrock_guard02_variant02")
    val varrock_guard_f = find("fai_varrock_guard02_f")
    val varrock_guard_f2 = find("fai_varrock_guard02_f_variant01")
    val varrock_guard_f3 = find("fai_varrock_guard02_f_variant02")
    val varrock_guard_captain = find("fai_varrock_guard_captain02")
    val master_farmer_1 = find("master_farmer_1")
    val master_farmer_2 = find("master_farmer_2")
    val master_farmer_1_f = find("master_farmer_1_f")
    val master_farmer_2_f = find("master_farmer_2_f")
    val martin = find("martin_the_master_farmer")
}

object ThievingObjs : ObjReferences() {
    val potato_seed = find("potato_seed")
    val onion_seed = find("onion_seed")
    val cabbage_seed = find("cabbage_seed")
    val tomato_seed = find("tomato_seed")
    val sweetcorn_seed = find("sweetcorn_seed")
    val strawberry_seed = find("strawberry_seed")
    val watermelon_seed = find("watermelon_seed")
    val snape_grass_seed = find("snape_grass_seed")
    val barley_seed = find("barley_seed")
    val hammerstone_seed = find("hammerstone_hop_seed")
    val asgarnian_seed = find("asgarnian_hop_seed")
    val jute_seed = find("jute_seed")
    val yanillian_seed = find("yanillian_hop_seed")
    val krandorian_seed = find("krandorian_hop_seed")
    val wildblood_seed = find("wildblood_hop_seed")
    val marigold_seed = find("marigold_seed")
    val nasturtium_seed = find("nasturtium_seed")
    val rosemary_seed = find("rosemary_seed")
    val woad_seed = find("woad_seed")
    val limpwurt_seed = find("limpwurt_seed")
    val redberry_seed = find("redberry_bush_seed")
    val cadavaberry_seed = find("cadavaberry_bush_seed")
    val dwellberry_seed = find("dwellberry_bush_seed")
    val jangerberry_seed = find("jangerberry_bush_seed")
    val whiteberry_seed = find("whiteberry_bush_seed")
    val poison_ivy_seed = find("poisonivy_bush_seed")
    val mushroom_spore = find("mushroom_seed")
    val belladonna_seed = find("belladonna_seed")
    val cactus_seed = find("cactus_seed")
    val seaweed_spore = find("seaweed_seed")
    val potato_cactus_seed = find("potato_cactus_seed")
    val guam_seed = find("guam_seed")
    val marrentill_seed = find("marrentill_seed")
    val tarromin_seed = find("tarromin_seed")
    val harralander_seed = find("harralander_seed")
    val ranarr_seed = find("ranarr_seed")
    val toadflax_seed = find("toadflax_seed")
    val irit_seed = find("irit_seed")
    val avantoe_seed = find("avantoe_seed")
    val kwuarm_seed = find("kwuarm_seed")
    val snapdragon_seed = find("snapdragon_seed")
    val cadantine_seed = find("cadantine_seed")
    val lantadyme_seed = find("lantadyme_seed")
    val dwarf_weed_seed = find("dwarf_weed_seed")
    val torstol_seed = find("torstol_seed")
}

object ThievingSeqs : SeqReferences() {
    val pickpocket = find("human_pickpocket")
    val stunned = find("human_unarmedblock")
}

object ThievingSpots : SpotanimReferences() {
    val stunned = find("stunned_thieving")
}
