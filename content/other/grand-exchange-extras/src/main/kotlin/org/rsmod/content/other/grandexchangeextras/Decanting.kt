package org.rsmod.content.other.grandexchangeextras

import org.rsmod.api.config.refs.objs
import org.rsmod.api.invtx.add
import org.rsmod.api.invtx.delete
import org.rsmod.api.invtx.invTransaction
import org.rsmod.api.invtx.select
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.obj.UnpackedObjType

/** An item that comes in doses or charges: [byAmount] has index n - 1 for the n-dose item. */
internal class DoseFamily(val byAmount: List<UnpackedObjType>) {
    val max: Int
        get() = byAmount.size
}

internal enum class DecantResult {
    Done,
    Nothing,
    NoSpace,
    NoVessels,
}

internal class Decanter(private val objTypes: ObjTypeList) {
    /**
     * Regroups every family in [families] held in the inventory (plain items and notes alike) into
     * as many [target]-dose items as possible plus one with the rest. A potion that needs more
     * vials than it frees takes [vial]s from the inventory, then buys them for [vialPrice].
     *
     * Note: all or nothing per family; the real Bob also does what he can when the coins run short.
     */
    fun ProtectedAccess.decant(
        families: List<DoseFamily>,
        target: Int,
        vial: UnpackedObjType?,
        vialPrice: Int,
    ): DecantResult {
        var done = false
        var failure: DecantResult? = null
        for (family in families) {
            for (noted in listOf(false, true)) {
                val forms = family.byAmount.map { if (noted) objTypes.cert(it) else it }
                if (noted && !family.byAmount[0].canCert) continue
                val held = forms.map { invTotal(inv, it) }
                if (held.all { it == 0 }) continue
                val amount = target.coerceAtMost(family.max)
                val charges = held.withIndex().sumOf { (i, n) -> (i + 1L) * n }
                val full = (charges / amount).toInt()
                val rest = (charges % amount).toInt()
                val result = IntArray(family.max)
                result[amount - 1] += full
                if (rest > 0) result[rest - 1] += 1
                if (result.toList() == held) continue
                val spare = held.sum() - result.sum()
                when (val outcome = regroup(forms, held, result, spare, noted, vial, vialPrice)) {
                    DecantResult.Done -> done = true
                    else -> failure = outcome
                }
            }
        }
        return if (done) DecantResult.Done else failure ?: DecantResult.Nothing
    }

    private fun ProtectedAccess.regroup(
        forms: List<UnpackedObjType>,
        held: List<Int>,
        result: IntArray,
        spare: Int,
        noted: Boolean,
        vial: UnpackedObjType?,
        vialPrice: Int,
    ): DecantResult {
        var fromInv = 0
        var fromNotes = 0
        var cost = 0
        val vialNote = vial?.let { objTypes.cert(it) }
        if (vial != null && spare < 0) {
            val need = -spare
            fromInv = minOf(need, invTotal(inv, vial))
            fromNotes = minOf(need - fromInv, invTotal(inv, checkNotNull(vialNote)))
            cost = (need - fromInv - fromNotes) * vialPrice
            if (cost > invTotal(inv, objs.coins)) return DecantResult.NoVessels
        }
        val outcome =
            player.invTransaction(inv) {
                val image = select(inv)
                for (i in forms.indices) {
                    if (held[i] > 0) delete(image, forms[i].id, held[i])
                    if (result[i] > 0) add(image, forms[i].id, result[i])
                }
                if (vial != null) {
                    if (fromInv > 0) delete(image, vial.id, fromInv)
                    if (fromNotes > 0) delete(image, checkNotNull(vialNote).id, fromNotes)
                    if (cost > 0) delete(image, objs.coins.id, cost)
                    // The empty vessels a smaller regrouping frees come back in the same form.
                    if (spare > 0) {
                        add(image, (if (noted) checkNotNull(vialNote) else vial).id, spare)
                    }
                }
            }
        return if (outcome.success) DecantResult.Done else DecantResult.NoSpace
    }
}
