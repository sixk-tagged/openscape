package org.rsmod.content.other.grandexchangeextras

import jakarta.inject.Inject
import org.rsmod.api.invtx.add
import org.rsmod.api.invtx.delete
import org.rsmod.api.invtx.invTransaction
import org.rsmod.api.invtx.select
import org.rsmod.api.market.MarketPrices
import org.rsmod.api.player.output.objExamine
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onIfModalButton
import org.rsmod.api.script.onOpNpc5
import org.rsmod.game.type.enums.EnumTypeList
import org.rsmod.game.type.interf.IfButtonOp
import org.rsmod.game.type.interf.IfEvent
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.obj.UnpackedObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The item sets exchange (451 and 430). The sets come from the cache: `enum_1033` lists one enum
 * per set, whose key -1 is the set and keys 0.. are its pieces; the client draws from the same.
 */
class ItemSetsScript
@Inject
constructor(
    private val enumTypes: EnumTypeList,
    private val objTypes: ObjTypeList,
    private val marketPrices: MarketPrices,
) : PluginScript() {
    private class ItemSet(val obj: Int, val parts: List<Int>)

    private val sets: List<ItemSet> by lazy {
        val list = checkNotNull(enumTypes[SETS_ENUM]) { "enum_$SETS_ENUM missing" }
        list.primitiveMap.entries
            .sortedBy { (it.key as Number).toInt() }
            .map { (_, value) ->
                val inner = checkNotNull(enumTypes[(value as Number).toInt()])
                val entries =
                    inner.primitiveMap.entries.associate { (k, v) ->
                        (k as Number).toInt() to (v as Number).toInt()
                    }
                ItemSet(
                    obj = entries.getValue(-1),
                    parts = entries.filterKeys { it >= 0 }.toSortedMap().values.toList(),
                )
            }
    }

    private val setsByObj: Map<Int, ItemSet> by lazy { sets.associateBy { it.obj } }

    override fun ScriptContext.startup() {
        onIfModalButton(ge_extras_components.item_sets_list) { clickSet(it.comsub, it.op) }
        onIfModalButton(ge_extras_components.item_sets_side_items) { clickInv(it.comsub, it.op) }
        for (clerk in ge_extras_npcs.clerks) {
            onOpNpc5(clerk) { openItemSets() }
        }
    }

    private fun ProtectedAccess.clickSet(index: Int, op: IfButtonOp) {
        val set = sets.getOrNull(index) ?: return
        if (op == IfButtonOp.Op10) {
            examine(objTypes.getValue(set.obj))
            return
        }
        val setType = objTypes.getValue(set.obj)
        val parts = set.parts.map { objTypes.getValue(it) }
        val notedParts = parts.map { objTypes.cert(it) }
        when {
            parts.all { invTotal(inv, it) > 0 } -> swap(parts, listOf(setType))
            // The client offers "Pack" for a full set of notes too, which gives a noted set.
            setType.canCert &&
                parts.all { it.canCert } &&
                notedParts.all { invTotal(inv, it) > 0 } ->
                swap(notedParts, listOf(objTypes.cert(setType)))
            // Also what a damaged Barrows piece gets: only the full-charge pieces are the parts.
            else -> mes("The ${setType.name} contains: ${parts.joinToString { it.name }}.")
        }
    }

    private fun ProtectedAccess.clickInv(slot: Int, op: IfButtonOp) {
        val obj = inv[slot] ?: return
        val type = objTypes[obj]
        if (op == IfButtonOp.Op10) {
            objExamine(inv, slot)
            return
        }
        val set = setsByObj[objTypes.uncert(type).id] ?: return
        val parts = set.parts.map { objTypes.getValue(it) }
        // UNVERIFIED: a noted set unpacking into notes mirrors the noted pack rule.
        val out = if (type.isCert) parts.map { objTypes.cert(it) } else parts
        swap(listOf(type), out)
    }

    /** Takes one of each of [from] and gives one of each of [into], or nothing at all. */
    private fun ProtectedAccess.swap(from: List<UnpackedObjType>, into: List<UnpackedObjType>) {
        val result =
            player.invTransaction(inv) {
                val image = select(inv)
                from.forEach { delete(image, it.id, 1) }
                into.forEach { add(image, it.id, 1) }
            }
        if (!result.success) {
            mes("You don't have enough inventory space.")
        }
    }

    private fun ProtectedAccess.examine(type: UnpackedObjType) {
        player.objExamine(type, 1, marketPrices[type] ?: 1)
    }

    private companion object {
        const val SETS_ENUM = 1033
    }
}

/** Opens the item sets exchange (451 over the inventory side panel 430). */
fun ProtectedAccess.openItemSets() {
    invTransmit(inv)
    ifOpenMainSidePair(
        main = ge_extras_interfaces.item_sets,
        side = ge_extras_interfaces.item_sets_side,
    )
    ifSetEvents(ge_extras_components.item_sets_list, 0..255, IfEvent.Op1, IfEvent.Op10)
    ifSetEvents(ge_extras_components.item_sets_side_items, inv.indices, IfEvent.Op1, IfEvent.Op10)
}
