@file:Suppress("unused", "SpellCheckingInspection")

package org.rsmod.content.areas.city.edgeville.configs

import org.rsmod.api.config.Constants
import org.rsmod.api.config.refs.objs
import org.rsmod.api.type.editors.inv.InvEditor
import org.rsmod.api.type.refs.inv.InvReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.type.inv.InvScope
import org.rsmod.game.type.inv.InvStackType

typealias edgeville_invs = EdgevilleInvs

typealias edgeville_objs = EdgevilleObjs

object EdgevilleInvs : InvReferences() {
    // Oziach has no shop of his own in the cache; `runeplateshop` is the unused rune platebody one.
    val oziach = find("runeplateshop")
    val peksa = find("helmetshop")
    val general = find("generalshop5")
    val richard = find("wildernesscapeshop6")
}

object EdgevilleObjs : ObjReferences() {
    val rune_platebody = find("rune_platebody")
    val green_dhide_body = find("dragonhide_body")
    val anti_dragon_shield = find("antidragonbreathshield")

    val bronze_med_helm = find("bronze_med_helm")
    val iron_med_helm = find("iron_med_helm")
    val steel_med_helm = find("steel_med_helm")
    val mithril_med_helm = find("mithril_med_helm")
    val adamant_med_helm = find("adamant_med_helm")
    val bronze_full_helm = find("bronze_full_helm")
    val iron_full_helm = find("iron_full_helm")
    val steel_full_helm = find("steel_full_helm")
    val mithril_full_helm = find("mithril_full_helm")
    val adamant_full_helm = find("adamant_full_helm")

    val team6_cape = find("wilderness_cape_6")
    val team16_cape = find("wilderness_cape_16")
    val team26_cape = find("wilderness_cape_26")
    val team36_cape = find("wilderness_cape_36")
    val team46_cape = find("wilderness_cape_46")

    val draconic_visage = find("dragonfire_visage")
    val skeletal_visage = find("skeletal_visage")
    val dragonfire_shield = find("dragonfire_shield_uncharged")
    val dragonfire_ward = find("dragonfire_ward_uncharged")

    val unblessed_symbol = find("stringstar")
    val blessed_symbol = find("blessedstar")
    val prayer_cape = find("skillcape_prayer")
    val prayer_hood = find("skillcape_prayer_hood")

    val brass_key = find("edgevilledungeonkey")
    val goblin_mail = find("goblin_armour")

    val bronze_chainbody = find("bronze_chainbody")
    val iron_chainbody = find("iron_chainbody")
    val black_full_helm = find("black_full_helm")
    val black_full_helm_trim = find("black_full_helm_trim")
    val black_full_helm_gold = find("black_full_helm_gold")
    val black_platebody = find("black_platebody")
    val black_platebody_trim = find("black_platebody_trim")
    val black_platebody_gold = find("black_platebody_gold")
    val black_platelegs = find("black_platelegs")
    val black_platelegs_trim = find("black_platelegs_trim")
    val black_platelegs_gold = find("black_platelegs_gold")
    val black_plateskirt = find("black_plateskirt")
    val black_plateskirt_trim = find("black_plateskirt_trim")
    val black_plateskirt_gold = find("black_plateskirt_gold")
    val elite_black_full_helm = find("elite_black_full_helm")
    val elite_black_platebody = find("elite_black_platebody")
    val elite_black_platelegs = find("elite_black_platelegs")
}

internal object EdgevilleInvEditor : InvEditor() {
    init {
        // Oziach's Armour Shop (specialist: buys only what it stocks)
        edit(edgeville_invs.oziach) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(edgeville_objs.rune_platebody, count = 2, restockCycles = 500)
            stock += stock(edgeville_objs.green_dhide_body, count = 2, restockCycles = 500)
            stock += stock(edgeville_objs.anti_dragon_shield, count = 35, restockCycles = 1)
        }

        // Peksa's Helmet Shop (specialist: medium and full helmets)
        edit(edgeville_invs.peksa) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(edgeville_objs.bronze_med_helm, count = 5, restockCycles = 100)
            stock += stock(edgeville_objs.iron_med_helm, count = 3, restockCycles = 200)
            stock += stock(edgeville_objs.steel_med_helm, count = 3, restockCycles = 400)
            stock += stock(edgeville_objs.mithril_med_helm, count = 1, restockCycles = 3000)
            stock += stock(edgeville_objs.adamant_med_helm, count = 1, restockCycles = 12000)
            stock += stock(edgeville_objs.bronze_full_helm, count = 4, restockCycles = 100)
            stock += stock(edgeville_objs.iron_full_helm, count = 3, restockCycles = 200)
            stock += stock(edgeville_objs.steel_full_helm, count = 2, restockCycles = 400)
            stock += stock(edgeville_objs.mithril_full_helm, count = 1, restockCycles = 3000)
            stock += stock(edgeville_objs.adamant_full_helm, count = 1, restockCycles = 12000)
        }

        // Edgeville General Store (buys anything, so allStock)
        edit(edgeville_invs.general) {
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
            stock += stock(objs.knife, count = 1, restockCycles = 2)
            stock += stock(objs.newcomer_map, count = 5, restockCycles = 100)
            stock += stock(objs.sos_security_book, count = 5, restockCycles = 100)
        }

        // Richard's Wilderness Cape Shop (specialist: the five team capes)
        edit(edgeville_invs.richard) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(edgeville_objs.team6_cape, count = 100, restockCycles = 100)
            stock += stock(edgeville_objs.team16_cape, count = 100, restockCycles = 100)
            stock += stock(edgeville_objs.team26_cape, count = 100, restockCycles = 100)
            stock += stock(edgeville_objs.team36_cape, count = 100, restockCycles = 100)
            stock += stock(edgeville_objs.team46_cape, count = 100, restockCycles = 100)
        }
    }
}
