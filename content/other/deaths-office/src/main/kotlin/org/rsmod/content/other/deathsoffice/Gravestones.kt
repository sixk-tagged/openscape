package org.rsmod.content.other.deathsoffice

import jakarta.inject.Inject
import jakarta.inject.Singleton
import net.rsprot.protocol.game.outgoing.misc.client.HintArrow
import net.rsprot.protocol.game.outgoing.misc.client.HintArrow.NpcHintArrow
import net.rsprot.protocol.game.outgoing.misc.client.HintArrow.ResetHintArrow
import org.rsmod.api.death.DeathKeep
import org.rsmod.api.player.vars.intVarBit
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.repo.npc.NpcRepository
import org.rsmod.content.other.deathsoffice.configs.DeathNpcs
import org.rsmod.content.other.deathsoffice.configs.DeathVarBits
import org.rsmod.content.other.deathsoffice.configs.DeathVarps
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Player
import org.rsmod.game.inv.InvObj
import org.rsmod.game.inv.Inventory
import org.rsmod.game.type.npc.NpcTypeList
import org.rsmod.map.CoordGrid

private var Player.graveTicksVarp by intVarp(DeathVarps.gravestone_ticks)
private var Player.graveHudTicks by intVarBit(DeathVarBits.gravestone_duration)
/** See [GravestoneScript.TUTORIAL_ACTIVE] and [GravestoneScript.TUTORIAL_DONE]. */
internal var Player.deathTutorial by intVarp(DeathVarps.death_tutorial)

/**
 * Brought to Death's Office by a first death that lost items and not back out of his portal yet;
 * the grave waits until then.
 */
internal val Player.heldByDeath: Boolean
    get() = deathTutorial and GravestoneScript.TUTORIAL_ACTIVE != 0

/**
 * [grave]'s fee before any payment: each unit's gravestone fee at [unitPrice], capped at 500k in
 * total.
 */
internal fun graveOwed(grave: Inventory, unitPrice: (InvObj) -> Long): Long {
    val owed = grave.filterNotNull().sumOf { DeathKeep.graveFee(unitPrice(it)) * it.count }
    return minOf(owed, DeathKeep.GRAVE_FEE_CAP)
}

/** Death's Office fills map square 49_89. */
internal val Player.inDeathsOffice: Boolean
    get() = coords.mx == 49 && coords.mz == 89

/** Held by Death before his tutorial is finished, so his portal stays shut. */
internal val Player.inDeathTutorial: Boolean
    get() = heldByDeath && deathTutorial and GravestoneScript.TUTORIAL_DONE == 0

/**
 * Logged-in ticks until the grave collapses, 0 for no grave. Every write also sets the HUD grave
 * timer, which the client only counts down on its own for 3 ticks between updates.
 */
internal var Player.graveTicks: Int
    get() = graveTicksVarp
    set(value) {
        graveTicksVarp = value
        graveHudTicks = value
    }

/**
 * Hands out the 255 gravestone npc slots. Slot N's npc is only drawn for a player whose
 * `gravestone_visible` varbit is N, so every grave is private to its owner, who also gets a hint
 * arrow on it.
 */
@Singleton
class Gravestones
@Inject
constructor(private val npcRepo: NpcRepository, private val npcTypes: NpcTypeList) {
    private var Player.gravestoneVisible by intVarBit(DeathVarBits.gravestone_visible)

    private val slots = arrayOfNulls<Npc>(DeathNpcs.graves.size)
    private val owners = HashMap<Player, Int>()

    /** Spawns [owner]'s grave at [coords]. False when all 255 slots are in use. */
    fun spawn(owner: Player, coords: CoordGrid): Boolean {
        despawn(owner)
        val index = slots.indexOfFirst { it == null }
        if (index == -1) {
            return false
        }
        val npc = Npc(npcTypes[DeathNpcs.graves[index]], coords)
        npcRepo.add(npc, Int.MAX_VALUE)
        slots[index] = npc
        owners[owner] = index
        owner.gravestoneVisible = index + 1
        owner.client.write(HintArrow(NpcHintArrow(npc.slotId)))
        return true
    }

    /** Removes [owner]'s grave npc and arrow, if any, and always clears their slot varbit. */
    fun despawn(owner: Player) {
        owner.gravestoneVisible = 0
        val index = owners.remove(owner) ?: return
        slots[index]?.let { npcRepo.del(it, Int.MAX_VALUE) }
        slots[index] = null
        owner.client.write(HintArrow(ResetHintArrow))
    }

    fun npc(owner: Player): Npc? = owners[owner]?.let { slots[it] }
}
