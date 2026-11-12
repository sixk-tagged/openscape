@file:Suppress("unused", "SpellCheckingInspection")

package org.rsmod.content.areas.city.gnomestronghold

import org.rsmod.api.type.editors.inv.InvEditor
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.type.inv.InvScope
import org.rsmod.game.type.inv.InvStackType
import org.rsmod.game.type.inv.InvType
import org.rsmod.game.type.obj.ObjType

typealias gnome_objs = GnomeObjs

object GnomeObjs : ObjReferences() {
    val adamant_2h_sword = find("adamant_2h_sword")
    val batta_tin = find("batta_tin")
    val black_2h_sword = find("black_2h_sword")
    val bolt = find("bolt")
    val brandy = find("brandy")
    val bronze_2h_sword = find("bronze_2h_sword")
    val bronze_arrow = find("bronze_arrow")
    val bronze_arrowheads = find("bronze_arrowheads")
    val bucket_milk = find("bucket_milk")
    val cabbage = find("cabbage")
    val cheese = find("cheese")
    val chocolate_bar = find("chocolate_bar")
    val chocolate_dust = find("chocolate_dust")
    val cocktail_glass_empty = find("cocktail_glass_empty")
    val cocktail_shaker = find("cocktail_shaker")
    val crossbow = find("crossbow")
    val crunchy_tray = find("crunchy_tray")
    val dwellberries = find("dwellberries")
    val equa_leaves = find("equa_leaves")
    val gianne_dough = find("gianne_dough")
    val giannes_cook_book = find("giannes_cook_book")
    val giant_frog_legs = find("giant_frog_legs")
    val gin = find("gin")
    val gnome_boots_blue = find("gnome_boots_blue")
    val gnome_boots_cream = find("gnome_boots_cream")
    val gnome_boots_green = find("gnome_boots_green")
    val gnome_boots_pink = find("gnome_boots_pink")
    val gnome_boots_turquoise = find("gnome_boots_turquoise")
    val gnome_hat_blue = find("gnome_hat_blue")
    val gnome_hat_cream = find("gnome_hat_cream")
    val gnome_hat_green = find("gnome_hat_green")
    val gnome_hat_pink = find("gnome_hat_pink")
    val gnome_hat_turquoise = find("gnome_hat_turquoise")
    val gnome_robebottoms_blue = find("gnome_robebottoms_blue")
    val gnome_robebottoms_cream = find("gnome_robebottoms_cream")
    val gnome_robebottoms_green = find("gnome_robebottoms_green")
    val gnome_robebottoms_pink = find("gnome_robebottoms_pink")
    val gnome_robebottoms_turquoise = find("gnome_robebottoms_turquoise")
    val gnome_robetop_blue = find("gnome_robetop_blue")
    val gnome_robetop_cream = find("gnome_robetop_cream")
    val gnome_robetop_green = find("gnome_robetop_green")
    val gnome_robetop_pink = find("gnome_robetop_pink")
    val gnome_robetop_turquoise = find("gnome_robetop_turquoise")
    val gnome_spice = find("gnome_spice")
    val gnomebowl_mould = find("gnomebowl_mould")
    val iron_2h_sword = find("iron_2h_sword")
    val iron_arrowheads = find("iron_arrowheads")
    val iron_axe = find("iron_axe")
    val iron_battleaxe = find("iron_battleaxe")
    val knife = find("knife")
    val lemon = find("lemon")
    val lime = find("lime")
    val longbow = find("longbow")
    val mithril_2h_sword = find("mithril_2h_sword")
    val mithril_arrowheads = find("mithril_arrowheads")
    val mithril_battleaxe = find("mithril_battleaxe")
    val onion = find("onion")
    val orange = find("orange")
    val pearl_bolts = find("pearl_bolts")
    val pineapple = find("pineapple")
    val pot_flour = find("pot_flour")
    val pot_of_cream = find("pot_of_cream")
    val potato = find("potato")
    val shortbow = find("shortbow")
    val steel_2h_sword = find("steel_2h_sword")
    val steel_arrowheads = find("steel_arrowheads")
    val steel_axe = find("steel_axe")
    val steel_battleaxe = find("steel_battleaxe")
    val tbw_spider_on_shaft_raw = find("tbw_spider_on_shaft_raw")
    val tbw_spider_on_stick_raw = find("tbw_spider_on_stick_raw")
    val tomato = find("tomato")
    val vodka = find("vodka")
    val whisky = find("whisky")
}

internal object GnomeStrongholdInvEditor : InvEditor() {
    init {
        shop(
            gnome_invs.hudo,
            line(gnome_objs.gianne_dough, 50, 100),
            line(gnome_objs.equa_leaves, 20, 100),
            line(gnome_objs.pot_flour, 10, 200),
            line(gnome_objs.gnome_spice, 10, 100),
            line(gnome_objs.onion, 10, 100),
            line(gnome_objs.potato, 10, 100),
            line(gnome_objs.cabbage, 10, 100),
            line(gnome_objs.tomato, 10, 100),
            line(gnome_objs.cheese, 10, 100),
            line(gnome_objs.lime, 10, 100),
            line(gnome_objs.orange, 10, 100),
            line(gnome_objs.lemon, 10, 100),
            line(gnome_objs.pineapple, 10, 100),
            line(gnome_objs.dwellberries, 10, 100),
            line(gnome_objs.cocktail_shaker, 10, 200),
            line(gnome_objs.chocolate_bar, 20, 100),
            line(gnome_objs.chocolate_dust, 10, 100),
            line(gnome_objs.pot_of_cream, 5, 100),
            line(gnome_objs.bucket_milk, 5, 100),
            line(gnome_objs.knife, 5, 200),
            line(gnome_objs.giannes_cook_book, 5, 100),
            line(gnome_objs.batta_tin, 5, 200),
            line(gnome_objs.crunchy_tray, 5, 200),
            line(gnome_objs.gnomebowl_mould, 5, 200),
        )

        shop(
            gnome_invs.heckel,
            line(gnome_objs.brandy, 10, 100),
            line(gnome_objs.gin, 10, 100),
            line(gnome_objs.vodka, 10, 1),
            line(gnome_objs.whisky, 10, 100),
            line(gnome_objs.pineapple, 10, 200),
            line(gnome_objs.equa_leaves, 20, 100),
            line(gnome_objs.orange, 20, 100),
            line(gnome_objs.lemon, 20, 100),
            line(gnome_objs.lime, 20, 100),
            line(gnome_objs.dwellberries, 5, 100),
            line(gnome_objs.cocktail_shaker, 10, 200),
            line(gnome_objs.chocolate_bar, 20, 100),
            line(gnome_objs.chocolate_dust, 10, 100),
            line(gnome_objs.pot_of_cream, 5, 100),
            line(gnome_objs.bucket_milk, 5, 100),
            line(gnome_objs.knife, 5, 200),
            line(gnome_objs.cocktail_glass_empty, 20, 10),
        )

        shop(
            gnome_invs.gulluck,
            line(gnome_objs.bronze_arrow, 200, 10),
            line(gnome_objs.bolt, 150, 10),
            line(gnome_objs.pearl_bolts, 1, 1500),
            line(gnome_objs.shortbow, 4, 100),
            line(gnome_objs.longbow, 2, 200),
            line(gnome_objs.crossbow, 2, 400),
            line(gnome_objs.bronze_arrowheads, 200, 10),
            line(gnome_objs.iron_arrowheads, 180, 20),
            line(gnome_objs.steel_arrowheads, 160, 40),
            line(gnome_objs.mithril_arrowheads, 140, 80),
            line(gnome_objs.iron_axe, 5, 200),
            line(gnome_objs.steel_axe, 3, 400),
            line(gnome_objs.iron_battleaxe, 5, 100),
            line(gnome_objs.steel_battleaxe, 2, 200),
            line(gnome_objs.mithril_battleaxe, 1, 3000),
            line(gnome_objs.bronze_2h_sword, 4, 200),
            line(gnome_objs.iron_2h_sword, 3, 300),
            line(gnome_objs.steel_2h_sword, 2, 500),
            line(gnome_objs.black_2h_sword, 1, 700),
            line(gnome_objs.mithril_2h_sword, 1, 1000),
            line(gnome_objs.adamant_2h_sword, 1, 15000),
        )

        shop(
            gnome_invs.rometti,
            line(gnome_objs.gnome_robebottoms_pink, 5, 200),
            line(gnome_objs.gnome_robebottoms_green, 5, 200),
            line(gnome_objs.gnome_robebottoms_blue, 5, 200),
            line(gnome_objs.gnome_robebottoms_cream, 5, 200),
            line(gnome_objs.gnome_robebottoms_turquoise, 5, 200),
            line(gnome_objs.gnome_hat_pink, 5, 200),
            line(gnome_objs.gnome_hat_green, 5, 200),
            line(gnome_objs.gnome_hat_blue, 5, 200),
            line(gnome_objs.gnome_hat_cream, 5, 200),
            line(gnome_objs.gnome_hat_turquoise, 5, 200),
            line(gnome_objs.gnome_robetop_pink, 5, 200),
            line(gnome_objs.gnome_robetop_green, 5, 200),
            line(gnome_objs.gnome_robetop_blue, 5, 200),
            line(gnome_objs.gnome_robetop_cream, 5, 200),
            line(gnome_objs.gnome_robetop_turquoise, 5, 200),
            line(gnome_objs.gnome_boots_pink, 5, 200),
            line(gnome_objs.gnome_boots_green, 5, 200),
            line(gnome_objs.gnome_boots_blue, 5, 200),
            line(gnome_objs.gnome_boots_cream, 5, 200),
            line(gnome_objs.gnome_boots_turquoise, 5, 200),
        )
    }

    private class Line(val obj: ObjType, val count: Int, val restockCycles: Int)

    private fun line(obj: ObjType, count: Int, restockCycles: Int) = Line(obj, count, restockCycles)

    private fun shop(inv: InvType, vararg lines: Line) {
        edit(inv) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            restock = true
            autoSize = true
            for (line in lines) {
                stock += stock(line.obj, line.count, line.restockCycles)
            }
        }
    }
}
