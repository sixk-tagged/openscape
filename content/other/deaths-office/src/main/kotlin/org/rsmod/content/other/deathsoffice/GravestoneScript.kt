package org.rsmod.content.other.deathsoffice

import jakarta.inject.Inject
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.death.DeathKeep
import org.rsmod.api.death.PlayerDeathItems
import org.rsmod.api.invtx.invTransfer
import org.rsmod.api.market.MarketPrices
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.player.worn.WornUnequipOp
import org.rsmod.api.script.onPlayerLogin
import org.rsmod.api.script.onPlayerLogout
import org.rsmod.api.script.onPlayerSoftTimer
import org.rsmod.api.script.onProtectedEvent
import org.rsmod.content.other.deathsoffice.configs.DeathInvs
import org.rsmod.content.other.deathsoffice.configs.DeathQueues
import org.rsmod.content.other.deathsoffice.configs.DeathTimers
import org.rsmod.content.other.deathsoffice.configs.DeathVarps
import org.rsmod.content.other.deathsoffice.configs.GraveInterfaces
import org.rsmod.events.EventBus
import org.rsmod.game.entity.Player
import org.rsmod.game.inv.InvObj
import org.rsmod.game.inv.Inventory
import org.rsmod.game.type.inv.InvType
import org.rsmod.game.type.inv.InvTypeList
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.obj.Wearpos
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class GravestoneScript
@Inject
constructor(
    private val graves: Gravestones,
    private val objTypes: ObjTypeList,
    private val marketPrices: MarketPrices,
    private val invTypes: InvTypeList,
    private val eventBus: EventBus,
) : PluginScript() {
    private var Player.graveCoord by intVarp(DeathVarps.gravestone_coord)
    private var Player.gravePaid by intVarp(DeathVarps.gravestone_paid)

    override fun ScriptContext.startup() {
        onProtectedEvent<PlayerDeathItems>(0) { loseItems(it.deathCoords) }
        onPlayerLogin { player.restoreGrave() }
        onPlayerLogout { graves.despawn(player) }
        onPlayerSoftTimer(DeathTimers.gravestone) { player.tickGrave() }
    }

    private fun ProtectedAccess.loseItems(deathCoords: CoordGrid) {
        val skulled = player.skullIcon != null
        val protectItem = player.vars[varbits.protect_item] == 1
        val carried = (inv.filterNotNull() + worn.filterNotNull()).filterNot(::isBond)
        val (kept, lost) =
            DeathKeep.split(carried, DeathKeep.keepCount(skulled, protectItem), ::unitPrice)
        if (lost.isEmpty()) {
            return
        }

        val hadGrave = player.graveTicks > 0
        val office = inv(DeathInvs.death_permanent)
        val grave = inv(DeathInvs.gravestone)
        if (!hadGrave && !graves.spawn(player, deathCoords)) {
            // All 255 grave slots are taken: the items go straight to Death.
            moveLost(kept, into = office, overflow = office)
            return
        }
        if (!hadGrave) {
            player.graveCoord = deathCoords.packed
            player.gravePaid = 0
        }
        val trimmed = hadGrave && player.trimGrave(grave, office)
        // Credit paid for items the trim sent to Death no longer covers the grave.
        player.gravePaid = minOf(player.gravePaid.toLong(), graveOwed(grave, ::unitPrice)).toInt()
        val overflowed = moveLost(kept, into = grave, overflow = office) || trimmed
        player.graveTicks = GRAVE_TICKS
        player.softTimer(DeathTimers.gravestone, 1)
        if (player.deathTutorial and TUTORIAL_DONE == 0) {
            // Death brings first-timers to his office to explain, in place of the respawn.
            player.deathTutorial = player.deathTutorial or TUTORIAL_ACTIVE
            telejump(DeathsOfficeScript.OFFICE_ARRIVAL)
            queue(DeathQueues.tutorial, 1)
        }
        if (overflowed) {
            mes("Some items have been moved to Death's Office from your gravestone.")
        }
        if (hadGrave) {
            mes("Some of your items have been added to your previous gravestone.")
        }
    }

    /**
     * Moves every carried unit not in [kept] into [into], or into [overflow] when [into] is full.
     * Anything neither can hold is deleted, as OSRS does past Death's 120 slots. Worn items go in
     * as their inventory form and are unequipped properly. Returns whether anything overflowed.
     */
    private fun ProtectedAccess.moveLost(
        kept: List<InvObj>,
        into: Inventory,
        overflow: Inventory,
    ): Boolean {
        val keepLeft = kept.groupingBy { it.id }.eachCount().toMutableMap()
        val wornBefore = Wearpos.entries.associateWith { worn[it.slot] }
        var overflowed = false
        for (from in listOf(inv, worn)) {
            for (slot in from.indices) {
                val obj = from[slot] ?: continue
                if (isBond(obj)) {
                    continue
                }
                val keepHere = minOf(obj.count, keepLeft[obj.id] ?: 0)
                keepLeft[obj.id] = (keepLeft[obj.id] ?: 0) - keepHere
                val lose = obj.count - keepHere
                if (lose == 0) {
                    continue
                }
                if (player.invTransfer(from, slot, lose, into, untransform = true).success) {
                    continue
                }
                overflowed = true
                if (!player.invTransfer(from, slot, lose, overflow, untransform = true).success) {
                    from[slot] = if (keepHere > 0) obj.copy(count = keepHere) else null
                }
            }
        }
        for ((wearpos, old) in wornBefore) {
            if (old != null && worn[wearpos.slot]?.id != old.id) {
                WornUnequipOp.notifyWornUnequip(player, wearpos, objTypes[old], eventBus)
            }
        }
        return overflowed
    }

    /**
     * Before a death adds to an existing grave, OSRS sends each unstackable obj's units past 28 to
     * Death's Office (deleted past its 120 slots). Returns whether anything was sent.
     */
    // Note: the wiki's resources (bones, essence, ...) that all go to Death are not handled.
    private fun Player.trimGrave(grave: Inventory, office: Inventory): Boolean {
        var trimmed = false
        val unstackables = grave.filterNotNull().filterNot { objTypes[it].isStackable }
        for ((id, units) in unstackables.groupBy { it.id }) {
            var excess = units.sumOf { it.count } - KEPT_PER_UNSTACKABLE
            for (slot in grave.indices.reversed()) {
                val obj = grave[slot]?.takeIf { it.id == id } ?: continue
                if (excess <= 0) {
                    break
                }
                val move = minOf(excess, obj.count)
                if (!invTransfer(grave, slot, move, office).success) {
                    grave[slot] = if (move < obj.count) obj.copy(count = obj.count - move) else null
                }
                excess -= move
                trimmed = true
            }
        }
        return trimmed
    }

    private fun Player.restoreGrave() {
        graves.despawn(this)
        if (graveTicks <= 0) {
            return
        }
        if (!graves.spawn(this, CoordGrid(graveCoord))) {
            collapseGrave()
            return
        }
        // Rewritten so the HUD timer, which is not saved, shows again.
        graveTicks = graveTicks
        softTimer(DeathTimers.gravestone, 1)
    }

    /**
     * Counts down one logged-in tick, except while Death holds the player for his first-death
     * tutorial or the loot screen is open.
     */
    private fun Player.tickGrave() {
        if (graveTicks <= 0) {
            clearSoftTimer(DeathTimers.gravestone)
            return
        }
        if (heldByDeath && !inDeathsOffice) {
            // Left some way other than Death's portal (e.g. a teleport): the wait is over.
            deathTutorial = deathTutorial and TUTORIAL_ACTIVE.inv()
        }
        if (heldByDeath || ui.containsModal(GraveInterfaces.loot)) {
            return
        }
        graveTicks--
        if (graveTicks == 0) {
            collapseGrave()
        }
    }

    /** Sends the grave's items to Death's Office; past its 120 slots they are lost, as in OSRS. */
    private fun Player.collapseGrave() {
        val grave = inv(DeathInvs.gravestone)
        val office = inv(DeathInvs.death_permanent)
        for (slot in grave.indices) {
            val obj = grave[slot] ?: continue
            if (!invTransfer(grave, slot, obj.count, office).success) {
                grave[slot] = null
            }
        }
        graves.despawn(this)
        graveTicks = 0
        graveCoord = 0
        gravePaid = 0
        clearSoftTimer(DeathTimers.gravestone)
        // Not on the wiki: OSRS's wording for a collapsing grave is undocumented.
        mes("Your gravestone has collapsed. Death is now holding your items.")
    }

    private fun Player.inv(type: InvType): Inventory = invMap.getOrPut(invTypes[type])

    /** [obj]'s type, as its inventory form if it is a worn-only variant (e.g. a max cape). */
    private fun typeOf(obj: InvObj) = objTypes.untransform(objTypes[obj])

    private fun isBond(obj: InvObj): Boolean = typeOf(obj).param(params.bond_item)

    // Note: shop value until a Grand Exchange exists; its prices plug into MarketPrices.
    private fun unitPrice(obj: InvObj): Long {
        val type = typeOf(obj)
        return (marketPrices[type] ?: type.cost).toLong()
    }

    companion object {
        const val GRAVE_TICKS: Int = 1500
        const val KEPT_PER_UNSTACKABLE: Int = 28
        /** `death_tutorial` bit: in Death's Office for the tutorial, cleared by his portal. */
        const val TUTORIAL_ACTIVE: Int = 1
        /** `death_tutorial` bit: every topic covered; bits 2, 4 and 16 are the topics. */
        const val TUTORIAL_DONE: Int = 8
    }
}
