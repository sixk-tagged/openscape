package org.rsmod.content.skills.thieving

import jakarta.inject.Inject
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.hit.processor.InstantPlayerHitProcessor
import org.rsmod.api.player.hit.takeInstantHit
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.thievingLvl
import org.rsmod.api.random.GameRandom
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.api.type.refs.spot.SpotanimReferences
import org.rsmod.game.entity.Npc
import org.rsmod.game.hit.HitType
import org.rsmod.game.type.npc.NpcType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** Pickpocketing the Lumbridge men, women and farmers (OSRS wiki levels, xp, loot and rates). */
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
        val (obj, count) = target.loot(random)
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
        player.takeInstantHit(HitType.Typeless, STUN_DAMAGE, instantHits)
        anim(ThievingSeqs.stunned)
        spotanim(ThievingSpots.stunned, height = 100)
        mes("You've been stunned!")
        delay(target.stunTicks)
    }

    private companion object {
        const val STUN_DAMAGE = 1
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
        val loot: (GameRandom) -> Pair<ObjType, Int>,
    )

    val all =
        with(ThievingNpcs) {
            listOf(
                Target("man", listOf(man, man2, man3, man_indoor), 1, 8.0, 180, 240, 8) {
                    objs.coins to 3
                },
                Target("woman", listOf(woman, woman2, woman3), 1, 8.0, 180, 240, 8) {
                    objs.coins to 3
                },
                Target(
                    "farmer",
                    listOf(farmer1, farmer3, farmer2_f, farmer3_f, farmer4),
                    10,
                    14.5,
                    150,
                    240,
                    8,
                ) { random ->
                    if (random.of(128) < 5) ThievingObjs.potato_seed to 1 else objs.coins to 9
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
    val farmer3 = find("farmer3")
    val farmer2_f = find("farmer2_f")
    val farmer3_f = find("farmer3_f")
    val farmer4 = find("farmer4")
}

object ThievingObjs : ObjReferences() {
    val potato_seed = find("potato_seed")
}

object ThievingSeqs : SeqReferences() {
    val pickpocket = find("human_pickpocket")
    val stunned = find("human_unarmedblock")
}

object ThievingSpots : SpotanimReferences() {
    val stunned = find("stunned_thieving")
}
