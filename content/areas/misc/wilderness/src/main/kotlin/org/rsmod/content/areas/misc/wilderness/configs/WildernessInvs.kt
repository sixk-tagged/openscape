@file:Suppress("unused", "SpellCheckingInspection")

package org.rsmod.content.areas.misc.wilderness.configs

import org.rsmod.api.config.Constants
import org.rsmod.api.config.refs.objs
import org.rsmod.api.type.editors.inv.InvEditor
import org.rsmod.api.type.refs.inv.InvReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.type.inv.InvScope
import org.rsmod.game.type.inv.InvStackType
import org.rsmod.game.type.obj.ObjType

typealias wilderness_invs = WildernessInvs

typealias wilderness_objs = WildernessObjs

object WildernessInvs : InvReferences() {
    val pizza_bases = find("foodshop")
    val duty_free = find("generalshop8")
    val lundail_shop = find("magearena_runeshop")

    /** The cape shops of sellers 1 to 10, in order (Richard's, the sixth, is in Edgeville). */
    val cape_shops =
        listOf(
            find("wildernesscapeshop1"),
            find("wildernesscapeshop2"),
            find("wildernesscapeshop3"),
            find("wildernesscapeshop4"),
            find("wildernesscapeshop5"),
            find("wildernesscapeshop7"),
            find("wildernesscapeshop8"),
            find("wildernesscapeshop9"),
            find("wildernesscapeshop10"),
        )
}

object WildernessObjs : ObjReferences() {
    val pizza_base = find("pizza_base")

    /** Lundail's rune shop stock (wiki): the obj, how many, and the restock ticks. */
    val lundail_stock: List<Triple<ObjType, Int, Int>> =
        listOf(
            Triple(find("firerune"), 200, 50),
            Triple(find("waterrune"), 200, 50),
            Triple(find("airrune"), 200, 50),
            Triple(find("earthrune"), 200, 50),
            Triple(find("mindrune"), 140, 50),
            Triple(find("bodyrune"), 140, 50),
            Triple(find("naturerune"), 250, 30),
            Triple(find("chaosrune"), 250, 10),
            Triple(find("lawrune"), 250, 30),
            Triple(find("cosmicrune"), 20, 100),
            Triple(find("deathrune"), 250, 15),
            Triple(find("pack_firerune"), 5, 40),
            Triple(find("pack_waterrune"), 5, 40),
            Triple(find("pack_airrune"), 5, 40),
            Triple(find("pack_earthrune"), 5, 40),
            Triple(find("pack_mindrune"), 4, 40),
            Triple(find("pack_chaosrune"), 3, 40),
        )

    /** The team capes 1 to 50 in order: seller n sells n, n + 10, n + 20, n + 30 and n + 40. */
    val team_capes =
        listOf(
            find("wilderness_cape_1"),
            find("wilderness_cape_2"),
            find("wilderness_cape_3"),
            find("wilderness_cape_4"),
            find("wilderness_cape_5"),
            find("wilderness_cape_6"),
            find("wilderness_cape_7"),
            find("wilderness_cape_8"),
            find("wilderness_cape_9"),
            find("wilderness_cape_10"),
            find("wilderness_cape_11"),
            find("wilderness_cape_12"),
            find("wilderness_cape_13"),
            find("wilderness_cape_14"),
            find("wilderness_cape_15"),
            find("wilderness_cape_16"),
            find("wilderness_cape_17"),
            find("wilderness_cape_18"),
            find("wilderness_cape_19"),
            find("wilderness_cape_20"),
            find("wilderness_cape_21"),
            find("wilderness_cape_22"),
            find("wilderness_cape_23"),
            find("wilderness_cape_24"),
            find("wilderness_cape_25"),
            find("wilderness_cape_26"),
            find("wilderness_cape_27"),
            find("wilderness_cape_28"),
            find("wilderness_cape_29"),
            find("wilderness_cape_30"),
            find("wilderness_cape_31"),
            find("wilderness_cape_32"),
            find("wilderness_cape_33"),
            find("wilderness_cape_34"),
            find("wilderness_cape_35"),
            find("wilderness_cape_36"),
            find("wilderness_cape_37"),
            find("wilderness_cape_38"),
            find("wilderness_cape_39"),
            find("wilderness_cape_40"),
            find("wilderness_cape_41"),
            find("wilderness_cape_42"),
            find("wilderness_cape_43"),
            find("wilderness_cape_44"),
            find("wilderness_cape_45"),
            find("wilderness_cape_46"),
            find("wilderness_cape_47"),
            find("wilderness_cape_48"),
            find("wilderness_cape_49"),
            find("wilderness_cape_50"),
        )
}

internal object WildernessInvEditor : InvEditor() {
    init {
        // The seller numbers behind WildernessInvs.cape_shops (6 is Richard's, in Edgeville).
        val sellers = listOf(1, 2, 3, 4, 5, 7, 8, 9, 10)
        for ((index, shop) in wilderness_invs.cape_shops.withIndex()) {
            edit(shop) {
                scope = InvScope.Shared
                stack = InvStackType.Always
                autoSize = true
                restock = true
                for (step in 0..4) {
                    val cape = wilderness_objs.team_capes[sellers[index] + step * 10 - 1]
                    stock += stock(cape, count = 100, restockCycles = 100)
                }
            }
        }

        // Tony's Pizza Bases (specialist: buys only pizza bases)
        edit(wilderness_invs.pizza_bases) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(wilderness_objs.pizza_base, count = 30, restockCycles = 100)
        }

        // Lundail's Arena-side Rune Shop (specialist: buys only its own runes)
        edit(wilderness_invs.lundail_shop) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            for ((rune, count, ticks) in wilderness_objs.lundail_stock) {
                stock += stock(rune, count = count, restockCycles = ticks)
            }
        }

        // Bandit Duty Free (buys anything, so allStock)
        edit(wilderness_invs.duty_free) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            size = Constants.shop_default_size
            restock = true
            allStock = true
            stock += stock(objs.pot_empty, count = 5, restockCycles = 50)
            stock += stock(objs.jug_empty, count = 2, restockCycles = 100)
            stock += stock(objs.pack_jug_empty, count = 5, restockCycles = 15)
            stock += stock(objs.tinderbox, count = 2, restockCycles = 100)
            stock += stock(objs.chisel, count = 2, restockCycles = 100)
            stock += stock(objs.hammer, count = 5, restockCycles = 100)
            stock += stock(objs.bronze_pickaxe, count = 5, restockCycles = 100)
            stock += stock(objs.bronze_axe, count = 10, restockCycles = 100)
        }
    }
}
