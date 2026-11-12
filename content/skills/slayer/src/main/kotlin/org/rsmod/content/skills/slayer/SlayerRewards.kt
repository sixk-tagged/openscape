package org.rsmod.content.skills.slayer

import jakarta.inject.Inject
import org.rsmod.api.config.refs.varps
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.VarPlayerIntMapSetter
import org.rsmod.api.script.onIfModalButton
import org.rsmod.api.type.refs.comp.ComponentReferences
import org.rsmod.api.type.refs.enums.EnumReferences
import org.rsmod.api.type.refs.interf.InterfaceReferences
import org.rsmod.api.type.refs.varbit.VarBitReferences
import org.rsmod.api.type.refs.varp.VarpReferences
import org.rsmod.content.skills.slayer.configs.SlayerColumns
import org.rsmod.content.skills.slayer.configs.SlayerTables
import org.rsmod.content.skills.slayer.configs.slayer_varbits
import org.rsmod.content.skills.slayer.configs.slayer_varps
import org.rsmod.game.dbtable.DbTableResolver
import org.rsmod.game.entity.Player
import org.rsmod.game.enums.EnumTypeMap
import org.rsmod.game.enums.EnumTypeMapResolver
import org.rsmod.game.type.interf.IfButtonOp
import org.rsmod.game.type.interf.IfEvent
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.UnpackedObjType
import org.rsmod.game.type.varbit.VarBitType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The Slayer Rewards screen (interface 426). The client draws every tab from the cache and asks for
 * confirmation itself; a confirmed choice arrives as op 1 on `confirm_button` with a code for
 * comsub (the client scripts `slayer_rewards_unlock_confirm`, `..._tasks_canceltask` and
 * `..._tasks_unblocktask`, rev 233): 0-57 is an unlock bit (unlock it, or disable a refundable
 * one), 58 cancels the task, 59 blocks it, 60-65 and 69 unblock slots 1-6 and 7. Buy sends ops 2-5
 * on `buy_items` with the shop index. The block list is the master's own
 * (`slayer_master_in_focus`).
 */
class SlayerRewards
@Inject
internal constructor(
    private val dbTables: DbTableResolver,
    private val enums: EnumTypeMapResolver,
) : PluginScript() {
    private lateinit var unlocks: Map<Int, Unlock>

    override fun ScriptContext.startup() {
        unlocks =
            dbTables[SlayerTables.unlock].associate { row ->
                row[SlayerColumns.unlock_bit] to
                    Unlock(
                        cost = row[SlayerColumns.unlock_cost],
                        refundable = row.getOrNull(SlayerColumns.unlock_refundable) ?: false,
                    )
            }
        onIfModalButton(RewardComponents.confirm_button) { confirm(it.comsub) }
        onIfModalButton(RewardComponents.buy_items) { buy(it.comsub, it.op) }
    }

    private fun ProtectedAccess.confirm(code: Int) {
        when (code) {
            in 0..MAX_BIT -> unlock(code)
            CANCEL -> cancel()
            BLOCK -> block()
            in UNBLOCK_CODES -> unblock(UNBLOCK_CODES.indexOf(code) + 1)
            // UNVERIFIED: the wording; "extend everything" and task storage aren't built.
            else -> mes("That option isn't available yet.")
        }
    }

    // ---- Unlock and Extend

    private fun ProtectedAccess.unlock(bit: Int) {
        val unlock = unlocks[bit] ?: return
        // A few unlocks stay bought: confirming one again switches it off or on.
        val toggle = SlayerToggles.byBit[bit]
        if (toggle != null && player.hasSlayerUnlock(bit)) {
            vars[toggle] = 1 - vars[toggle]
            return
        }
        if (player.hasSlayerUnlock(bit)) {
            if (unlock.refundable) {
                player.setSlayerUnlock(bit, false)
            }
            return
        }
        if (!spend(unlock.cost)) {
            return
        }
        player.setSlayerUnlock(bit, true)
    }

    // ---- Tasks

    private fun ProtectedAccess.cancel() {
        if (vars[varps.slayer_count] <= 0) {
            mes(NO_TASK)
            return
        }
        if (!spend(CANCEL_COST)) {
            return
        }
        clearTask()
    }

    private fun ProtectedAccess.block() {
        val task = vars[varps.slayer_target]
        if (vars[varps.slayer_count] <= 0 || task <= 0) {
            mes(NO_TASK)
            return
        }
        val master = SlayerMaster.byId(vars[RewardVarBits.master_in_focus]) ?: return
        val slots = master.blockSlots()
        val free = slots.firstOrNull { vars[it] == 0 }
        if (free == null) {
            // UNVERIFIED: the wording.
            mes("You don't have any free block slots.")
            return
        }
        if (!spend(master.blockCost)) {
            return
        }
        vars[free] = task
        clearTask()
    }

    private fun ProtectedAccess.unblock(slot: Int) {
        val master = SlayerMaster.byId(vars[RewardVarBits.master_in_focus]) ?: return
        val varbit = master.blockSlots().getOrNull(slot - 1) ?: return
        vars[varbit] = 0
    }

    private fun ProtectedAccess.clearTask() {
        vars[varps.slayer_count] = 0
        vars[varps.if1] = 0
        vars[varps.if2] = 0
    }

    // ---- Buy

    private fun ProtectedAccess.buy(index: Int, op: IfButtonOp) {
        val sets =
            when (op) {
                IfButtonOp.Op2 -> 1
                IfButtonOp.Op3 -> 5
                IfButtonOp.Op4 -> 10
                IfButtonOp.Op5 -> 50
                else -> return
            }
        val obj = enums[RewardEnums.item_ids].getOrNull(index) ?: return
        val perSet = enums[RewardEnums.item_quantities].valueFor(obj) ?: 1
        val cost = enums[RewardEnums.item_costs].valueFor(obj) ?: return
        if (vars[slayer_varbits.points] < cost * sets) {
            mes(notEnoughPoints(cost * sets))
            return
        }
        if (invAdd(inv, obj, perSet * sets).failure) {
            // UNVERIFIED: the wording.
            mes("You don't have enough inventory space.")
            return
        }
        vars[slayer_varbits.points] -= cost * sets
    }

    /** Matched by id: the enum's obj keys aren't the same instances as the reward obj. */
    private fun EnumTypeMap<ObjType, Int>.valueFor(obj: ObjType): Int? =
        firstOrNull { it.key.id == obj.id }?.value

    private fun ProtectedAccess.spend(cost: Int): Boolean {
        val points = vars[slayer_varbits.points]
        if (points < cost) {
            mes(notEnoughPoints(cost))
            return false
        }
        vars[slayer_varbits.points] = points - cost
        return true
    }

    // UNVERIFIED: the wording.
    private fun notEnoughPoints(cost: Int) = "You need $cost Slayer points for that."

    private data class Unlock(val cost: Int, val refundable: Boolean)

    private companion object {
        const val MAX_BIT = 57
        const val CANCEL = 58
        const val BLOCK = 59
        const val CANCEL_COST = 30
        const val NO_TASK = "You don't have a Slayer task."

        /** Slots 1-7's codes: 57 + 3, 4, 5, 6, 7, 12, 8 (`slayer_rewards_tasks_blocked_draw`). */
        val UNBLOCK_CODES = listOf(60, 61, 62, 63, 64, 69, 65)
    }
}

/** Opens the rewards screen as [master] (whose block list and block cost it shows). */
internal fun ProtectedAccess.openSlayerRewards(master: SlayerMaster) {
    vars[RewardVarBits.master_in_focus] = master.id
    // The Tasks tab reads the current task from the interface varps.
    vars[varps.if1] = vars[varps.slayer_count]
    vars[varps.if2] = vars[varps.slayer_target]
    vars[varps.if3] = 0
    vars[RewardVarps.if4] = 0
    vars[RewardVarps.if5] = 0
    vars[RewardVarps.if6] = 0
    ifOpenMainModal(RewardInterfaces.slayer_rewards)
    ifSetEvents(RewardComponents.confirm_button, 0..CONFIRM_CODES, IfEvent.Op1)
    ifSetEvents(
        RewardComponents.buy_items,
        0 until BUY_SLOTS,
        IfEvent.Op1,
        IfEvent.Op2,
        IfEvent.Op3,
        IfEvent.Op4,
        IfEvent.Op5,
        IfEvent.Op10,
    )
}

/** The tasks [player] has blocked with [master]. */
internal fun blockedTasks(player: Player, master: SlayerMaster): Set<Int> =
    master.blockSlots().map { player.vars[it] }.filter { it > 0 }.toSet()

private fun SlayerMaster.blockSlots(): List<VarBitType> =
    when (this) {
        SlayerMaster.Turael,
        SlayerMaster.Spria -> RewardVarBits.turael_blocks
        SlayerMaster.Mazchna -> RewardVarBits.mazchna_blocks
        SlayerMaster.Vannaka -> RewardVarBits.vannaka_blocks
        SlayerMaster.Nieve -> RewardVarBits.nieve_blocks
        SlayerMaster.Krystilia -> RewardVarBits.krystilia_blocks
    }

internal fun Player.setSlayerUnlock(bit: Int, on: Boolean) {
    val varp = if (bit < 32) slayer_varps.unlocks else slayer_varps.unlocks1
    val mask = 1 shl (bit % 32)
    val value = if (on) vars[varp] or mask else vars[varp] and mask.inv()
    VarPlayerIntMapSetter.set(this, varp, value)
}

private const val CONFIRM_CODES = 69
private const val BUY_SLOTS = 64

object RewardInterfaces : InterfaceReferences() {
    val slayer_rewards = find("slayer_rewards")
}

object RewardComponents : ComponentReferences() {
    val confirm_button = find("slayer_rewards:confirm_button")
    val buy_items = find("slayer_rewards:buy_items")
}

internal object RewardEnums : EnumReferences() {
    val item_ids = find<Int, UnpackedObjType>("slayer_item_rewards_ids")
    val item_quantities = find<ObjType, Int>("slayer_item_rewards_quantities")
    val item_costs = find<ObjType, Int>("slayer_item_rewards_cost")
}

object RewardVarps : VarpReferences() {
    val if4 = find("if4")
    val if5 = find("if5")
    val if6 = find("if6")
}

object RewardVarBits : VarBitReferences() {
    val master_in_focus = find("slayer_master_in_focus")
    val turael_blocks = (1..6).map { find("slayer_blocked_turael_$it") }
    val mazchna_blocks = (1..6).map { find("slayer_blocked_mazchna_$it") }
    val vannaka_blocks = (1..6).map { find("slayer_blocked_vannaka_$it") }
    val nieve_blocks = (1..6).map { find("slayer_blocked_nieve_$it") }
    val krystilia_blocks = (1..6).map { find("slayer_blocked_krystilia_$it") }
}
