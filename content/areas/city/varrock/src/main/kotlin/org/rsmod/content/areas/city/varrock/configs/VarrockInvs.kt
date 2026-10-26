package org.rsmod.content.areas.city.varrock.configs

import org.rsmod.api.config.Constants
import org.rsmod.api.config.refs.objs
import org.rsmod.api.type.editors.inv.InvEditor
import org.rsmod.api.type.refs.inv.InvReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.type.inv.InvScope
import org.rsmod.game.type.inv.InvStackType

typealias varrock_invs = VarrockInvs

typealias varrock_objs = VarrockObjs

object VarrockInvs : InvReferences() {
    val zaff_shop = find("memberstaffshop")
    val horvik_shop = find("armourshop")
    val lowe_shop = find("archeryshop")
    val thessalia_shop = find("clotheshop")
    val sword_shop = find("swordshop")
    val general_shop = find("generalshop2")
    val fancy_clothes_shop = find("fancyclothesstore")
    val tea_shop = find("teashop")
    val scavvo_shop = find("runiteshop")
    val valaine_shop = find("championshop")
    val pie_shop = find("shop_pies")
    val construction_shop = find("poh_sawmill_shop")
}

object VarrockObjs : ObjReferences() {
    val battlestaff = find("battlestaff")
    val plainstaff = find("plainstaff")
    val magic_staff = find("magic_staff")
    val staff_of_air = find("staff_of_air")
    val staff_of_water = find("staff_of_water")
    val staff_of_earth = find("staff_of_earth")
    val staff_of_fire = find("staff_of_fire")
    val bronze_chainbody = find("bronze_chainbody")
    val iron_chainbody = find("iron_chainbody")
    val steel_chainbody = find("steel_chainbody")
    val mithril_chainbody = find("mithril_chainbody")
    val bronze_platebody = find("bronze_platebody")
    val iron_platebody = find("iron_platebody")
    val steel_platebody = find("steel_platebody")
    val black_platebody = find("black_platebody")
    val mithril_platebody = find("mithril_platebody")
    val iron_platelegs = find("iron_platelegs")
    val studded_body = find("studded_body")
    val studded_chaps = find("studded_chaps")
    val bronze_arrow = find("bronze_arrow")
    val iron_arrow = find("iron_arrow")
    val steel_arrow = find("steel_arrow")
    val mithril_arrow = find("mithril_arrow")
    val adamant_arrow = find("adamant_arrow")
    val bolt = find("bolt")
    val shortbow = find("shortbow")
    val longbow = find("longbow")
    val oak_shortbow = find("oak_shortbow")
    val oak_longbow = find("oak_longbow")
    val willow_shortbow = find("willow_shortbow")
    val willow_longbow = find("willow_longbow")
    val maple_shortbow = find("maple_shortbow")
    val maple_longbow = find("maple_longbow")
    val crossbow = find("crossbow")
    val white_apron = find("white_apron")
    val leather_armour = find("leather_armour")
    val leather_gloves = find("leather_gloves")
    val leather_boots = find("leather_boots")
    val brown_apron = find("brown_apron")
    val pink_skirt = find("pink_skirt")
    val black_skirt = find("black_skirt")
    val blue_skirt = find("blue_skirt")
    val red_cape = find("red_cape")
    val silk = find("silk")
    val priest_robe = find("priest_robe")
    val priest_gown = find("priest_gown")
    val bronze_sword = find("bronze_sword")
    val iron_sword = find("iron_sword")
    val steel_sword = find("steel_sword")
    val black_sword = find("black_sword")
    val mithril_sword = find("mithril_sword")
    val adamant_sword = find("adamant_sword")
    val bronze_longsword = find("bronze_longsword")
    val iron_longsword = find("iron_longsword")
    val steel_longsword = find("steel_longsword")
    val black_longsword = find("black_longsword")
    val mithril_longsword = find("mithril_longsword")
    val adamant_longsword = find("adamant_longsword")
    val bronze_dagger = find("bronze_dagger")
    val iron_dagger = find("iron_dagger")
    val steel_dagger = find("steel_dagger")
    val black_dagger = find("black_dagger")
    val mithril_dagger = find("mithril_dagger")
    val adamant_dagger = find("adamant_dagger")
    val chefs_hat = find("chefs_hat")
    val bluewizhat = find("bluewizhat")
    val yellow_cape = find("yellow_cape")
    val grey_wolf_fur = find("grey_wolf_fur")
    val fur = find("fur")
    val needle = find("needle")
    val thread = find("thread")
    val eye_patch = find("eye_patch")
    val hunting_jungle_feather = find("hunting_jungle_feather")
    val hunting_desert_feather = find("hunting_desert_feather")
    val hunting_woodland_feather = find("hunting_woodland_feather")
    val hunting_polar_feather = find("hunting_polar_feather")
    val hunting_stripy_bird_feather = find("hunting_stripy_bird_feather")
    val cup_of_tea = find("cup_of_tea")
    val rune_plateskirt = find("rune_plateskirt")
    val rune_platelegs = find("rune_platelegs")
    val rune_mace = find("rune_mace")
    val rune_chainbody = find("rune_chainbody")
    val rune_longsword = find("rune_longsword")
    val rune_sword = find("rune_sword")
    val dragonhide_chaps = find("dragonhide_chaps")
    val dragon_vambraces = find("dragon_vambraces")
    val coif = find("coif")
    val blue_cape = find("blue_cape")
    val black_full_helm = find("black_full_helm")
    val black_platelegs = find("black_platelegs")
    val adamant_platebody = find("adamant_platebody")
    val book_recipie_pie = find("book_recipie_pie")
    val redberry_pie = find("redberry_pie")
    val meat_pie = find("meat_pie")
    val mud_pie = find("mud_pie")
    val apple_pie = find("apple_pie")
    val garden_pie = find("garden_pie")
    val fish_pie = find("fish_pie")
    val admiral_pie = find("admiral_pie")
    val wild_pie = find("wild_pie")
    val summer_pie = find("summer_pie")
    val poh_saw = find("poh_saw")
    val cloth = find("cloth")
    val nails_bronze = find("nails_bronze")
    val nails_iron = find("nails_iron")
    val nails = find("nails")
}

/**
 * Vanilla shop invs reused for the Varrock shops; stock, counts and restock cycles are wiki values.
 */
internal object VarrockInvEditor : InvEditor() {
    init {
        // Zaff's Superior Staffs!
        edit(varrock_invs.zaff_shop) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(varrock_objs.battlestaff, count = 5, restockCycles = 100)
            stock += stock(varrock_objs.plainstaff, count = 5, restockCycles = 100)
            stock += stock(varrock_objs.magic_staff, count = 5, restockCycles = 200)
            stock += stock(varrock_objs.staff_of_air, count = 2, restockCycles = 1000)
            stock += stock(varrock_objs.staff_of_water, count = 2, restockCycles = 1000)
            stock += stock(varrock_objs.staff_of_earth, count = 2, restockCycles = 1000)
            stock += stock(varrock_objs.staff_of_fire, count = 2, restockCycles = 1000)
        }

        // Horvik's Armour Shop
        edit(varrock_invs.horvik_shop) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(varrock_objs.bronze_chainbody, count = 5, restockCycles = 100)
            stock += stock(varrock_objs.iron_chainbody, count = 3, restockCycles = 200)
            stock += stock(varrock_objs.steel_chainbody, count = 3, restockCycles = 400)
            stock += stock(varrock_objs.mithril_chainbody, count = 1, restockCycles = 3000)
            stock += stock(varrock_objs.bronze_platebody, count = 3, restockCycles = 100)
            stock += stock(varrock_objs.iron_platebody, count = 1, restockCycles = 200)
            stock += stock(varrock_objs.steel_platebody, count = 1, restockCycles = 400)
            stock += stock(varrock_objs.black_platebody, count = 1, restockCycles = 3000)
            stock += stock(varrock_objs.mithril_platebody, count = 1, restockCycles = 3000)
            stock += stock(varrock_objs.iron_platelegs, count = 1, restockCycles = 100)
            stock += stock(varrock_objs.studded_body, count = 1, restockCycles = 700)
            stock += stock(varrock_objs.studded_chaps, count = 1, restockCycles = 700)
        }

        // Lowe's Archery Emporium
        edit(varrock_invs.lowe_shop) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(varrock_objs.bronze_arrow, count = 2000, restockCycles = 10)
            stock += stock(varrock_objs.iron_arrow, count = 1500, restockCycles = 15)
            stock += stock(varrock_objs.steel_arrow, count = 1000, restockCycles = 20)
            stock += stock(varrock_objs.mithril_arrow, count = 800, restockCycles = 15)
            stock += stock(varrock_objs.adamant_arrow, count = 600, restockCycles = 20)
            stock += stock(varrock_objs.bolt, count = 1500, restockCycles = 10)
            stock += stock(varrock_objs.shortbow, count = 4, restockCycles = 100)
            stock += stock(varrock_objs.longbow, count = 4, restockCycles = 100)
            stock += stock(varrock_objs.oak_shortbow, count = 3, restockCycles = 200)
            stock += stock(varrock_objs.oak_longbow, count = 3, restockCycles = 200)
            stock += stock(varrock_objs.willow_shortbow, count = 2, restockCycles = 200)
            stock += stock(varrock_objs.willow_longbow, count = 2, restockCycles = 200)
            stock += stock(varrock_objs.maple_shortbow, count = 1, restockCycles = 100)
            stock += stock(varrock_objs.maple_longbow, count = 1, restockCycles = 100)
            stock += stock(varrock_objs.crossbow, count = 2, restockCycles = 400)
        }

        // Thessalia's Fine Clothes
        edit(varrock_invs.thessalia_shop) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(varrock_objs.white_apron, count = 3, restockCycles = 100)
            stock += stock(varrock_objs.leather_armour, count = 12, restockCycles = 100)
            stock += stock(varrock_objs.leather_gloves, count = 10, restockCycles = 100)
            stock += stock(varrock_objs.leather_boots, count = 10, restockCycles = 100)
            stock += stock(varrock_objs.brown_apron, count = 1, restockCycles = 100)
            stock += stock(varrock_objs.pink_skirt, count = 5, restockCycles = 100)
            stock += stock(varrock_objs.black_skirt, count = 3, restockCycles = 100)
            stock += stock(varrock_objs.blue_skirt, count = 2, restockCycles = 100)
            stock += stock(varrock_objs.red_cape, count = 4, restockCycles = 100)
            stock += stock(varrock_objs.silk, count = 5, restockCycles = 500)
            stock += stock(varrock_objs.priest_robe, count = 3, restockCycles = 100)
            stock += stock(varrock_objs.priest_gown, count = 3, restockCycles = 100)
        }

        // Varrock Swordshop
        edit(varrock_invs.sword_shop) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(varrock_objs.bronze_sword, count = 5, restockCycles = 100)
            stock += stock(varrock_objs.iron_sword, count = 4, restockCycles = 200)
            stock += stock(varrock_objs.steel_sword, count = 4, restockCycles = 400)
            stock += stock(varrock_objs.black_sword, count = 3, restockCycles = 1000)
            stock += stock(varrock_objs.mithril_sword, count = 3, restockCycles = 3000)
            stock += stock(varrock_objs.adamant_sword, count = 2, restockCycles = 12000)
            stock += stock(varrock_objs.bronze_longsword, count = 4, restockCycles = 100)
            stock += stock(varrock_objs.iron_longsword, count = 3, restockCycles = 200)
            stock += stock(varrock_objs.steel_longsword, count = 3, restockCycles = 400)
            stock += stock(varrock_objs.black_longsword, count = 2, restockCycles = 1000)
            stock += stock(varrock_objs.mithril_longsword, count = 2, restockCycles = 3000)
            stock += stock(varrock_objs.adamant_longsword, count = 1, restockCycles = 12000)
            stock += stock(varrock_objs.bronze_dagger, count = 10, restockCycles = 100)
            stock += stock(varrock_objs.iron_dagger, count = 6, restockCycles = 200)
            stock += stock(varrock_objs.steel_dagger, count = 5, restockCycles = 400)
            stock += stock(varrock_objs.black_dagger, count = 4, restockCycles = 1000)
            stock += stock(varrock_objs.mithril_dagger, count = 3, restockCycles = 3000)
            stock += stock(varrock_objs.adamant_dagger, count = 2, restockCycles = 12000)
        }

        // Varrock General Store (buys anything, so allStock)
        edit(varrock_invs.general_shop) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            size = Constants.shop_default_size
            restock = true
            allStock = true
            stock += stock(objs.pot_empty, count = 5, restockCycles = 10)
            stock += stock(objs.jug_empty, count = 2, restockCycles = 100)
            stock += stock(objs.pack_jug_empty, count = 5, restockCycles = 20)
            stock += stock(objs.shears, count = 2, restockCycles = 100)
            stock += stock(objs.bucket_empty, count = 3, restockCycles = 10)
            stock += stock(objs.pack_bucket, count = 15, restockCycles = 10)
            stock += stock(objs.bowl_empty, count = 2, restockCycles = 50)
            stock += stock(objs.cake_tin, count = 2, restockCycles = 50)
            stock += stock(objs.tinderbox, count = 2, restockCycles = 100)
            stock += stock(objs.chisel, count = 2, restockCycles = 100)
            stock += stock(objs.hammer, count = 5, restockCycles = 100)
            stock += stock(objs.newcomer_map, count = 5, restockCycles = 100)
            stock += stock(objs.sos_security_book, count = 5, restockCycles = 100)
        }

        // Fancy Clothes Store
        edit(varrock_invs.fancy_clothes_shop) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(varrock_objs.chefs_hat, count = 0, restockCycles = 100)
            stock += stock(varrock_objs.bluewizhat, count = 3, restockCycles = 100)
            stock += stock(varrock_objs.yellow_cape, count = 1, restockCycles = 100)
            stock += stock(varrock_objs.grey_wolf_fur, count = 3, restockCycles = 100)
            stock += stock(varrock_objs.fur, count = 3, restockCycles = 100)
            stock += stock(varrock_objs.needle, count = 3, restockCycles = 100)
            stock += stock(varrock_objs.thread, count = 100, restockCycles = 5)
            stock += stock(varrock_objs.leather_gloves, count = 10, restockCycles = 100)
            stock += stock(varrock_objs.leather_boots, count = 10, restockCycles = 100)
            stock += stock(varrock_objs.priest_robe, count = 3, restockCycles = 100)
            stock += stock(varrock_objs.priest_gown, count = 3, restockCycles = 100)
            stock += stock(varrock_objs.brown_apron, count = 1, restockCycles = 100)
            stock += stock(varrock_objs.pink_skirt, count = 5, restockCycles = 100)
            stock += stock(varrock_objs.black_skirt, count = 3, restockCycles = 100)
            stock += stock(varrock_objs.blue_skirt, count = 2, restockCycles = 100)
            stock += stock(varrock_objs.red_cape, count = 4, restockCycles = 100)
            stock += stock(varrock_objs.eye_patch, count = 3, restockCycles = 2000)
            stock += stock(varrock_objs.hunting_jungle_feather, count = 0, restockCycles = 20)
            stock += stock(varrock_objs.hunting_desert_feather, count = 0, restockCycles = 20)
            stock += stock(varrock_objs.hunting_woodland_feather, count = 0, restockCycles = 20)
            stock += stock(varrock_objs.hunting_polar_feather, count = 0, restockCycles = 20)
            stock += stock(varrock_objs.hunting_stripy_bird_feather, count = 0, restockCycles = 20)
        }

        // Ye olde Tea Shoppe
        edit(varrock_invs.tea_shop) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(varrock_objs.cup_of_tea, count = 20, restockCycles = 50)
        }

        // Scavvo's Rune Store
        edit(varrock_invs.scavvo_shop) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(varrock_objs.rune_plateskirt, count = 1, restockCycles = 12000)
            stock += stock(varrock_objs.rune_platelegs, count = 1, restockCycles = 11500)
            stock += stock(varrock_objs.rune_mace, count = 1, restockCycles = 7000)
            stock += stock(varrock_objs.rune_chainbody, count = 1, restockCycles = 15000)
            stock += stock(varrock_objs.rune_longsword, count = 1, restockCycles = 14000)
            stock += stock(varrock_objs.rune_sword, count = 1, restockCycles = 10000)
            stock += stock(varrock_objs.dragonhide_chaps, count = 1, restockCycles = 500)
            stock += stock(varrock_objs.dragon_vambraces, count = 1, restockCycles = 500)
            stock += stock(varrock_objs.coif, count = 2, restockCycles = 1000)
        }

        // Valaine's Shop of Champions (buys anything, so allStock)
        edit(varrock_invs.valaine_shop) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            size = Constants.shop_default_size
            restock = true
            allStock = true
            stock += stock(varrock_objs.blue_cape, count = 2, restockCycles = 1000)
            stock += stock(varrock_objs.black_full_helm, count = 1, restockCycles = 1000)
            stock += stock(varrock_objs.black_platelegs, count = 1, restockCycles = 20000)
            stock += stock(varrock_objs.adamant_platebody, count = 1, restockCycles = 8000)
        }

        // Pie Shop
        edit(varrock_invs.pie_shop) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(varrock_objs.book_recipie_pie, count = 50, restockCycles = 50)
            stock += stock(varrock_objs.redberry_pie, count = 5, restockCycles = 100)
            stock += stock(varrock_objs.meat_pie, count = 4, restockCycles = 100)
            stock += stock(varrock_objs.mud_pie, count = 0, restockCycles = 100)
            stock += stock(varrock_objs.apple_pie, count = 3, restockCycles = 100)
            stock += stock(varrock_objs.garden_pie, count = 2, restockCycles = 100)
            stock += stock(varrock_objs.fish_pie, count = 1, restockCycles = 100)
            stock += stock(varrock_objs.admiral_pie, count = 0, restockCycles = 100)
            stock += stock(varrock_objs.wild_pie, count = 0, restockCycles = 100)
            stock += stock(varrock_objs.summer_pie, count = 0, restockCycles = 100)
        }

        // Construction supplies
        edit(varrock_invs.construction_shop) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(varrock_objs.poh_saw, count = 1000, restockCycles = 10)
            stock += stock(varrock_objs.cloth, count = 1000, restockCycles = 100)
            stock += stock(varrock_objs.nails_bronze, count = 1000, restockCycles = 100)
            stock += stock(varrock_objs.nails_iron, count = 1000, restockCycles = 100)
            stock += stock(varrock_objs.nails, count = 1000, restockCycles = 100)
        }
    }
}
