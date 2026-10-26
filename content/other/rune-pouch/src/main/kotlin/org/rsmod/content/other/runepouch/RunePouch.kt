package org.rsmod.content.other.runepouch

import jakarta.inject.Inject
import java.util.Locale
import kotlin.math.min
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.invtx.invAdd
import org.rsmod.api.invtx.invDel
import org.rsmod.api.player.vars.VarPlayerIntMapSetter
import org.rsmod.api.spells.runes.compact.CompactRuneRepository
import org.rsmod.game.entity.Player
import org.rsmod.game.inv.Inventory
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.varbit.VarBitType

/**
 * What a rune pouch holds: up to three kinds of rune (four in a divine pouch), at most
 * [MAX_PER_SLOT] of each, kept in the rune pouch varbits that spells already cast from. Runes go in
 * by using them on the pouch and come out with "Empty" (there is no pouch interface yet).
 */
class RunePouch
@Inject
constructor(private val compact: CompactRuneRepository, private val objTypes: ObjTypeList) {
    /** How many kinds of rune the pouch [player] carries holds: none without a pouch. */
    fun capacity(player: Player): Int =
        when {
            player.inv.any { it?.id == objs.divine_rune_pouch.id } -> DIVINE_SLOTS
            player.inv.any { it?.id == objs.rune_pouch.id } -> REGULAR_SLOTS
            else -> 0
        }

    /** How many of [rune] the pouch holds (whether or not the pouch is carried). */
    fun count(player: Player, rune: ObjType): Int {
        if (SLOTS.none { (_, quantity) -> player.vars[quantity] > 0 }) {
            return 0
        }
        val id = compact[rune] ?: return 0
        return SLOTS.sumOf { (type, quantity) ->
            if (player.vars[type] == id) player.vars[quantity] else 0
        }
    }

    /** Every kind of rune held, with its count, in slot order. */
    fun contents(player: Player): List<Pair<ObjType, Int>> =
        SLOTS.mapNotNull { (type, quantity) ->
            val count = player.vars[quantity]
            val rune = if (count > 0) compact.rune(player.vars[type]) else null
            rune?.let { it to count }
        }

    /**
     * Moves the runes in inventory [slot] into the pouch, as many as fit: into the slot already
     * holding that rune, else a free one. Returns how many moved.
     */
    fun store(player: Player, slot: Int): Int {
        val obj = player.inv[slot] ?: return 0
        val id = compact[objTypes[obj]] ?: return 0
        val slots = SLOTS.take(capacity(player))
        val held =
            slots.firstOrNull { (type, quantity) ->
                player.vars[type] == id && player.vars[quantity] > 0
            }
        val target = held ?: slots.firstOrNull { (_, quantity) -> player.vars[quantity] == 0 }
        target ?: return 0
        val (type, quantity) = target
        val current = if (held != null) player.vars[quantity] else 0
        val moved = min(obj.count, MAX_PER_SLOT - current)
        if (moved <= 0) {
            return 0
        }
        // The runes leave the inventory first: if that fails, the pouch is left as it was.
        if (!player.invDel(player.inv, obj.id, moved, slot = slot).success) {
            return 0
        }
        VarPlayerIntMapSetter.set(player, type, id)
        VarPlayerIntMapSetter.set(player, quantity, current + moved)
        return moved
    }

    /** Puts every rune back in the inventory. Returns false if some didn't fit (they stay put). */
    fun empty(player: Player): Boolean = emptyInto(player, player.inv)

    /**
     * Moves every rune [player]'s pouch holds into [into] (one of [player]'s inventories, or one of
     * [adder]'s), slot by slot. A slot that doesn't fit stays in the pouch.
     */
    fun emptyInto(player: Player, into: Inventory, adder: Player = player): Boolean {
        var all = true
        for ((type, quantity) in SLOTS) {
            val count = player.vars[quantity]
            val rune = if (count > 0) compact.rune(player.vars[type]) else null
            if (rune == null) {
                continue
            }
            if (!adder.invAdd(into, rune, count).success) {
                all = false
                continue
            }
            VarPlayerIntMapSetter.set(player, quantity, 0)
            VarPlayerIntMapSetter.set(player, type, 0)
        }
        return all
    }

    /** "Rune pouch: 1,500 x Blood rune, 200 x Death rune." */
    fun describe(player: Player): String {
        val held = contents(player)
        if (held.isEmpty()) {
            return "Your rune pouch is empty."
        }
        val list =
            held.joinToString { (rune, count) ->
                "%,d x %s".format(Locale.ENGLISH, count, objTypes[rune].name)
            }
        return "Rune pouch: $list."
    }

    companion object {
        const val MAX_PER_SLOT = 16_000
        const val REGULAR_SLOTS = 3
        const val DIVINE_SLOTS = 4

        private val SLOTS: List<Pair<VarBitType, VarBitType>> by lazy {
            with(varbits) {
                listOf(
                    rune_pouch_type_1 to rune_pouch_quantity_1,
                    rune_pouch_type_2 to rune_pouch_quantity_2,
                    rune_pouch_type_3 to rune_pouch_quantity_3,
                    rune_pouch_type_4 to rune_pouch_quantity_4,
                )
            }
        }
    }
}
