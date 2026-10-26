package org.rsmod.content.skills.thieving

import jakarta.inject.Inject
import org.rsmod.api.combat.commons.npc.queueCombatRetaliate
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.varps
import org.rsmod.api.hunt.Hunt
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.thievingLvl
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.script.onOpLoc2
import org.rsmod.api.type.builders.varp.VarpBuilder
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.api.type.refs.varp.VarpReferences
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.type.hunt.HuntVis
import org.rsmod.game.type.loc.LocType
import org.rsmod.game.type.npc.NpcType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Stealing from market stalls (OSRS wiki): a theft always works, gives the stall's loot and xp, and
 * leaves an empty stall for a few ticks. Guards in sight of the thief (the Draynor Market Guards)
 * attack, and the stall's owner shouts.
 */
class StallThieving
@Inject
constructor(
    private val locRepo: LocRepository,
    private val hunt: Hunt,
    private val objTypes: ObjTypeList,
) : PluginScript() {
    private var ProtectedAccess.oliviaAngryUntil by intVarp(ThievingVarps.olivia_angry_until)

    override fun ScriptContext.startup() {
        for (stall in ThievingStalls.all) {
            onOpLoc2(stall.type) { steal(it.loc, stall) }
        }
    }

    private suspend fun ProtectedAccess.steal(loc: BoundLocInfo, stall: ThievingStalls.Stall) {
        if (player.thievingLvl < stall.level) {
            mes("You need a Thieving level of ${stall.level} to steal from this stall.")
            return
        }
        anim(ThievingStallSeqs.steal)
        delay(2)
        // Someone else may have emptied it while we reached for it.
        if (locRepo.findExact(loc.coords, stall.type) == null) {
            return
        }
        val (obj, count) = stall.loot.roll(random)
        if (!invAdd(inv, obj, count).success) {
            mes(constants.dm_invspace)
            return
        }
        statAdvance(stats.thieving, stall.xp)
        mes("You steal ${withArticle(objTypes[obj].name)}.")
        locRepo.change(loc, ThievingStalls.empty, stall.respawn)
        if (stall.type == ThievingLocs.seed_stall) {
            // Olivia's shop (content/areas/city/draynor) reads this.
            oliviaAngryUntil = player.vars[varps.playtime] + GRUDGE_TICKS
        }
        alert(stall)
    }

    private fun ProtectedAccess.alert(stall: ThievingStalls.Stall) {
        for (npc in hunt.findNpcs(player.coords, ALERT_RANGE, HuntVis.LineOfSight)) {
            val id = npc.type.id
            if (stall.owners.any { it.id == id }) {
                npc.say(OWNER_SHOUT)
            } else if (stall.guards.any { it.id == id }) {
                npc.facePlayer(player)
                npc.queueCombatRetaliate(player)
            }
        }
    }

    private fun withArticle(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.endsWith("s") -> lower
            lower.first() in "aeiou" -> "an $lower"
            else -> "a $lower"
        }
    }

    private companion object {
        // UNVERIFIED: how far the wiki's "nearby" guards and owners see a theft.
        const val ALERT_RANGE = 8
        const val OWNER_SHOUT = "Hey! Get your hands off there!"
        // Olivia's wiki page: she won't trade for 10 minutes, counted only while logged in.
        const val GRUDGE_TICKS = 1000
    }
}

internal object ThievingStalls {
    /** [respawn] is the number of ticks the stall stays empty. */
    class Stall(
        val type: LocType,
        val level: Int,
        val xp: Double,
        val respawn: Int,
        val loot: Loot,
        val owners: List<NpcType> = emptyList(),
        val guards: List<NpcType> = emptyList(),
    )

    val empty: LocType = ThievingLocs.market

    val all =
        with(ThievingStallObjs) {
            listOf(
                // Ye olde Tea Shoppe, Varrock. The seller shouts only when talked to, and no guard
                // is near enough to come.
                Stall(ThievingLocs.tea_stall, 5, 16.0, 4, loot(cup_of_tea to 1)),
                // Olivia's seed stalls, Draynor. UNVERIFIED respawn: the wiki says 3 s in the text
                // but 2.4 s in the Thieving table.
                Stall(
                    ThievingLocs.seed_stall,
                    27,
                    10.0,
                    5,
                    with(ThievingObjs) {
                        loot(
                            hammerstone_seed to 120,
                            potato_seed to 119,
                            marigold_seed to 119,
                            barley_seed to 118,
                            onion_seed to 89,
                            asgarnian_seed to 83,
                            cabbage_seed to 71,
                            yanillian_seed to 47,
                            rosemary_seed to 36,
                            nasturtium_seed to 35,
                            tomato_seed to 35,
                            jute_seed to 35,
                            sweetcorn_seed to 30,
                            krandorian_seed to 24,
                            strawberry_seed to 18,
                            wildblood_seed to 12,
                            watermelon_seed to 9,
                        )
                    },
                    owners = listOf(ThievingStallNpcs.olivia),
                    guards = listOf(ThievingStallNpcs.market_guard),
                ),
                // Fortunato's wine stall, Draynor (the wiki's respawn: 4.8 s).
                Stall(
                    ThievingLocs.wine_stall,
                    22,
                    27.0,
                    8,
                    loot(
                        jug to 39,
                        jug_of_water to 20,
                        grapes to 17,
                        jug_of_wine to 13,
                        bottle_of_wine to 11,
                    ),
                    owners = listOf(ThievingStallNpcs.fortunato),
                    guards = listOf(ThievingStallNpcs.market_guard),
                ),
            )
        }
}

object ThievingLocs : LocReferences() {
    val tea_stall = find("tea_stall")
    val seed_stall = find("seed_stall")
    val wine_stall = find("rag_market_stall")

    /** The bare "Market stall" every stall turns into while it respawns. */
    val market = find("market")
}

object ThievingStallNpcs : NpcReferences() {
    val olivia = find("seed_merchant")
    val fortunato = find("rag_wine_merchant")
    val market_guard = find("farming_market_guard")
}

object ThievingStallSeqs : SeqReferences() {
    val steal = find("human_pickuptable")
}

object ThievingVarps : VarpReferences() {
    /** Custom: the playtime until which Olivia won't trade with someone who robbed her stalls. */
    val olivia_angry_until = find("olivia_angry_until")
}

internal object ThievingVarpBuilder : VarpBuilder() {
    init {
        build("olivia_angry_until")
    }
}
