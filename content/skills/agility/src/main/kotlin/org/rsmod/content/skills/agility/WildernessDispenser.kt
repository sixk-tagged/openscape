package org.rsmod.content.skills.agility

import jakarta.inject.Inject
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.output.UpdateRun
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLoc2
import org.rsmod.api.script.onOpLoc3
import org.rsmod.content.skills.agility.configs.AgilityLocs
import org.rsmod.content.skills.agility.configs.AgilityObjs
import org.rsmod.content.skills.agility.configs.AgilityVarps
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** 0 until the dispenser's fee is paid, then 1 + the laps done since. A course gate loses it. */
internal var ProtectedAccess.dispenserState by intVarp(AgilityVarps.dispenser)

/**
 * The Wilderness Agility Course's dispenser (OSRS wiki): tagging it restores run energy, tickets
 * from laps are redeemed for xp, more per ticket the more are redeemed at once, and paying its fee
 * makes each lap roll for loot. Not built: logging out inside taking 10 off the streak, and the
 * medium clue scroll, as clues don't exist.
 */
class WildernessDispenser
@Inject
constructor(private val objTypes: ObjTypeList, private val objRepo: ObjRepository) :
    PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1(AgilityLocs.wilderness_dispenser) { tag() }
        onOpLoc2(AgilityLocs.wilderness_dispenser) { pay() }
        onOpLoc3(AgilityLocs.wilderness_dispenser) { redeem() }
    }

    private fun ProtectedAccess.tag() {
        player.runEnergy = constants.run_max_energy
        UpdateRun.energy(player, player.runEnergy)
        mes("You tag the dispenser and feel your energy return.")
    }

    // UNVERIFIED: the messages.
    private fun ProtectedAccess.pay() {
        if (dispenserState > 0) {
            mes("You have already paid the dispenser.")
            return
        }
        if (inv.count(objTypes[AgilityObjs.coins]) < FEE) {
            mes("You need 150,000 coins to pay the dispenser.")
            return
        }
        invDel(inv, AgilityObjs.coins, FEE)
        dispenserState = 1
        mes("You pay the dispenser 150,000 coins.")
        val bags = listOf(AgilityObjs.looting_bag, AgilityObjs.looting_bag_open)
        if (bags.none { inv.count(objTypes[it]) > 0 || bank.count(objTypes[it]) > 0 }) {
            invAddOrDrop(objRepo, AgilityObjs.looting_bag)
        }
    }

    // UNVERIFIED: the messages.
    private fun ProtectedAccess.redeem() {
        val tickets = inv.count(objTypes[AgilityObjs.wilderness_ticket])
        if (tickets == 0) {
            mes("You have no Wilderness agility tickets to redeem.")
            return
        }
        invDel(inv, AgilityObjs.wilderness_ticket, tickets)
        statAdvance(stats.agility, tickets * xpPerTicket(tickets).toDouble())
        mes("You redeem $tickets Wilderness agility tickets for Agility experience.")
    }

    companion object {
        const val FEE = 150_000

        /** The rate applies to every ticket redeemed at once. */
        fun xpPerTicket(tickets: Int): Int =
            when {
                tickets <= 10 -> 200
                tickets <= 50 -> 210
                tickets <= 100 -> 220
                else -> 230
            }
    }
}

/** One band of laps' loot: noted food and restores, and a weighted table of noted armour. */
private class DispenserTier(
    val lastLap: Int,
    val food: IntRange,
    val restores: IntRange,
    val armour: List<Pair<ObjType, Int>>,
)

private val tiers =
    listOf(
        DispenserTier(
            15,
            3..6,
            1..2,
            listOf(
                AgilityObjs.adamant_full_helm to 1,
                AgilityObjs.adamant_platebody to 2,
                AgilityObjs.adamant_platelegs to 1,
                AgilityObjs.mithril_chainbody to 1,
                AgilityObjs.mithril_platelegs to 1,
                AgilityObjs.mithril_plateskirt to 1,
                AgilityObjs.rune_med_helm to 2,
                AgilityObjs.steel_platebody to 1,
            ),
        ),
        DispenserTier(
            30,
            6..11,
            2..4,
            listOf(
                AgilityObjs.adamant_full_helm to 1,
                AgilityObjs.adamant_platebody to 1,
                AgilityObjs.adamant_platelegs to 1,
                AgilityObjs.mithril_chainbody to 1,
                AgilityObjs.mithril_platelegs to 1,
                AgilityObjs.mithril_plateskirt to 1,
                AgilityObjs.rune_chainbody to 1,
                AgilityObjs.rune_kiteshield to 1,
                AgilityObjs.rune_med_helm to 1,
            ),
        ),
        DispenserTier(
            60,
            10..16,
            3..6,
            listOf(
                AgilityObjs.adamant_full_helm to 1,
                AgilityObjs.adamant_platebody to 1,
                AgilityObjs.adamant_platelegs to 1,
                AgilityObjs.mithril_platelegs to 1,
                AgilityObjs.mithril_plateskirt to 1,
                AgilityObjs.rune_chainbody to 2,
                AgilityObjs.rune_kiteshield to 2,
                AgilityObjs.rune_med_helm to 1,
            ),
        ),
        DispenserTier(
            Int.MAX_VALUE,
            14..20,
            4..8,
            listOf(
                AgilityObjs.adamant_full_helm to 1,
                AgilityObjs.adamant_platebody to 2,
                AgilityObjs.adamant_platelegs to 1,
                AgilityObjs.mithril_platelegs to 1,
                AgilityObjs.mithril_plateskirt to 1,
                AgilityObjs.rune_chainbody to 6,
                AgilityObjs.rune_kiteshield to 6,
                AgilityObjs.rune_med_helm to 2,
            ),
        ),
    )

private val foods =
    listOf(
        AgilityObjs.blighted_anglerfish,
        AgilityObjs.blighted_manta_ray,
        AgilityObjs.blighted_karambwan,
    )

/**
 * A lap with the fee paid: one roll for noted resources and one for noted armour, the richer the
 * more laps are done, plus an unnoted supply when there's room.
 */
// UNVERIFIED: since 2025-09-03 the loot needs an open looting bag; that's taken to mean it has to
// be held, with the loot still going to the inventory.
internal fun ProtectedAccess.dispenserLap(objTypes: ObjTypeList, objRepo: ObjRepository) {
    val state = dispenserState
    if (state == 0) {
        return
    }
    dispenserState = state + 1
    if (inv.count(objTypes[AgilityObjs.looting_bag_open]) == 0) {
        return
    }
    val tier = tiers.first { state <= it.lastLap }
    val resource = weighted(foods.map { it to 8 } + (AgilityObjs.blighted_super_restore to 7))
    val range = if (resource == AgilityObjs.blighted_super_restore) tier.restores else tier.food
    invAddOrDrop(objRepo, ocCert(resource), random.of(range.first, range.last))
    invAddOrDrop(objRepo, ocCert(weighted(tier.armour)))
    if (inv.freeSpace() > 0) {
        invAddOrDrop(
            objRepo,
            weighted(foods.map { it to 5 } + (AgilityObjs.blighted_super_restore to 1)),
        )
    }
}

private fun ProtectedAccess.weighted(table: List<Pair<ObjType, Int>>): ObjType {
    var roll = random.of(0, table.sumOf { it.second } - 1)
    for ((obj, weight) in table) {
        roll -= weight
        if (roll < 0) {
            return obj
        }
    }
    error("Roll is outside the table: $table")
}
