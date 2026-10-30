@file:Suppress("unused", "SpellCheckingInspection")

package org.rsmod.content.areas.city.portsarim.configs

import org.rsmod.api.config.Constants
import org.rsmod.api.type.editors.inv.InvEditor
import org.rsmod.api.type.refs.inv.InvReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.type.inv.InvScope
import org.rsmod.game.type.inv.InvStackType

typealias port_sarim_invs = PortSarimInvs

typealias port_sarim_objs = PortSarimObjs

object PortSarimInvs : InvReferences() {
    val wydin = find("wydinstore")
    val betty = find("magicshop")
    val battleaxes = find("battleaxeshop")
    val gerrant = find("fishingshop")
    val grum = find("goldshop")
    val rommik = find("craftingshop2")
    val archery = find("archeryshop2")
    val rimmington_general = find("generalshop6")
    val karamja_general = find("generalshop7")
    val zembo = find("boozeshop")
    val trader_stan = find("trader_stan_shop")
}

object PortSarimObjs : ObjReferences() {
    val adamant_arrow = find("adamant_arrow")
    val adamant_battleaxe = find("adamant_battleaxe")
    val airrune = find("airrune")
    val amulet_mould = find("amulet_mould")
    val banana = find("banana")
    val beer = find("beer")
    val black_battleaxe = find("black_battleaxe")
    val blackwizhat = find("blackwizhat")
    val bluewizhat = find("bluewizhat")
    val bodyrune = find("bodyrune")
    val bowl_empty = find("bowl_empty")
    val bread = find("bread")
    val bronze_battleaxe = find("bronze_battleaxe")
    val bucket_ectoplasm = find("bucket_ectoplasm")
    val bucket_empty = find("bucket_empty")
    val bucket_sand = find("bucket_sand")
    val cabbage = find("cabbage")
    val cake_tin = find("cake_tin")
    val chaosrune = find("chaosrune")
    val cheese = find("cheese")
    val chisel = find("chisel")
    val chocolate_bar = find("chocolate_bar")
    val deathrune = find("deathrune")
    val diamond_necklace = find("diamond_necklace")
    val diamond_ring = find("diamond_ring")
    val earthrune = find("earthrune")
    val emerald_necklace = find("emerald_necklace")
    val emerald_ring = find("emerald_ring")
    val eye_of_newt = find("eye_of_newt")
    val eye_patch = find("eye_patch")
    val feather = find("feather")
    val firerune = find("firerune")
    val fishing_bait = find("fishing_bait")
    val fishing_rod = find("fishing_rod")
    val fly_fishing_rod = find("fly_fishing_rod")
    val glassblowingpipe = find("glassblowingpipe")
    val gold_necklace = find("gold_necklace")
    val gold_ring = find("gold_ring")
    val hammer = find("hammer")
    val harpoon = find("harpoon")
    val holy_symbol_mould = find("holy_symbol_mould")
    val iron_battleaxe = find("iron_battleaxe")
    val jewl_bracelet_mould = find("jewl_bracelet_mould")
    val jug_empty = find("jug_empty")
    val jug_wine = find("jug_wine")
    val karamja_rum = find("karamja_rum")
    val knife = find("knife")
    val lobster_pot = find("lobster_pot")
    val maple_longbow = find("maple_longbow")
    val maple_shortbow = find("maple_shortbow")
    val mindrune = find("mindrune")
    val mithril_arrow = find("mithril_arrow")
    val mithril_battleaxe = find("mithril_battleaxe")
    val necklace_mould = find("necklace_mould")
    val needle = find("needle")
    val net = find("net")
    val newcomer_map = find("newcomer_map")
    val oak_longbow = find("oak_longbow")
    val oak_shortbow = find("oak_shortbow")
    val orange = find("orange")
    val pack_airrune = find("pack_airrune")
    val pack_bucket = find("pack_bucket")
    val pack_chaosrune = find("pack_chaosrune")
    val pack_earthrune = find("pack_earthrune")
    val pack_eye_newt = find("pack_eye_newt")
    val pack_feather = find("pack_feather")
    val pack_firerune = find("pack_firerune")
    val pack_fishing_bait = find("pack_fishing_bait")
    val pack_jug_empty = find("pack_jug_empty")
    val pack_mindrune = find("pack_mindrune")
    val pack_waterrune = find("pack_waterrune")
    val pineapple = find("pineapple")
    val pot_empty = find("pot_empty")
    val pot_flour = find("pot_flour")
    val potato = find("potato")
    val raw_anchovies = find("raw_anchovies")
    val raw_beef = find("raw_beef")
    val raw_chicken = find("raw_chicken")
    val raw_herring = find("raw_herring")
    val raw_lobster = find("raw_lobster")
    val raw_pike = find("raw_pike")
    val raw_rabbit = find("raw_rabbit")
    val raw_salmon = find("raw_salmon")
    val raw_sardine = find("raw_sardine")
    val raw_shrimp = find("raw_shrimp")
    val raw_swordfish = find("raw_swordfish")
    val raw_trout = find("raw_trout")
    val raw_tuna = find("raw_tuna")
    val redberries = find("redberries")
    val ring_mould = find("ring_mould")
    val rope = find("rope")
    val ruby_necklace = find("ruby_necklace")
    val ruby_ring = find("ruby_ring")
    val sail_tyras_helm = find("sail_tyras_helm")
    val sapphire_necklace = find("sapphire_necklace")
    val sapphire_ring = find("sapphire_ring")
    val seaweed = find("seaweed")
    val shears = find("shears")
    val sickle_mould = find("sickle_mould")
    val soda_ash = find("soda_ash")
    val sos_security_book = find("sos_security_book")
    val steel_arrow = find("steel_arrow")
    val steel_battleaxe = find("steel_battleaxe")
    val strung_diamond_amulet = find("strung_diamond_amulet")
    val strung_emerald_amulet = find("strung_emerald_amulet")
    val strung_gold_amulet = find("strung_gold_amulet")
    val strung_ruby_amulet = find("strung_ruby_amulet")
    val strung_sapphire_amulet = find("strung_sapphire_amulet")
    val swamppaste = find("swamppaste")
    val thread = find("thread")
    val tiara_mould = find("tiara_mould")
    val tinderbox = find("tinderbox")
    val tomato = find("tomato")
    val waterrune = find("waterrune")
    val willow_longbow = find("willow_longbow")
    val willow_shortbow = find("willow_shortbow")
    val xbows_silver_bolt_mould = find("xbows_silver_bolt_mould")

    val white_apron = find("white_apron")
    val brown_apron = find("brown_apron")
    val golden_apron = find("golden_apron")
    val crafting_cape = find("skillcape_crafting")
    val crafting_cape_trimmed = find("skillcape_crafting_trimmed")
    val crafting_hood = find("skillcape_crafting_hood")
    val smithing_cape = find("skillcape_smithing")
    val smithing_cape_trimmed = find("skillcape_smithing_trimmed")
    val smithing_hood = find("skillcape_smithing_hood")
    val yin_yang_amulet = find("postie_pete_yin_yang")
    val oak_logs = find("oak_logs")
    val spade = find("spade")
}

internal object PortSarimInvEditor : InvEditor() {
    init {
        // Wydin's Food Store (specialist: food)
        edit(port_sarim_invs.wydin) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(port_sarim_objs.pot_flour, count = 3, restockCycles = 100)
            stock += stock(port_sarim_objs.raw_beef, count = 1, restockCycles = 100)
            stock += stock(port_sarim_objs.raw_chicken, count = 1, restockCycles = 100)
            stock += stock(port_sarim_objs.cabbage, count = 3, restockCycles = 100)
            stock += stock(port_sarim_objs.banana, count = 3, restockCycles = 100)
            stock += stock(port_sarim_objs.redberries, count = 1, restockCycles = 100)
            stock += stock(port_sarim_objs.bread, count = 0, restockCycles = 100)
            stock += stock(port_sarim_objs.chocolate_bar, count = 1, restockCycles = 100)
            stock += stock(port_sarim_objs.cheese, count = 3, restockCycles = 100)
            stock += stock(port_sarim_objs.tomato, count = 3, restockCycles = 100)
            stock += stock(port_sarim_objs.potato, count = 1, restockCycles = 100)
        }

        // Betty's Magic Emporium (specialist: magic)
        edit(port_sarim_invs.betty) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(port_sarim_objs.firerune, count = 5000, restockCycles = 10)
            stock += stock(port_sarim_objs.waterrune, count = 5000, restockCycles = 10)
            stock += stock(port_sarim_objs.airrune, count = 5000, restockCycles = 10)
            stock += stock(port_sarim_objs.earthrune, count = 5000, restockCycles = 10)
            stock += stock(port_sarim_objs.mindrune, count = 5000, restockCycles = 10)
            stock += stock(port_sarim_objs.bodyrune, count = 5000, restockCycles = 10)
            stock += stock(port_sarim_objs.chaosrune, count = 250, restockCycles = 10)
            stock += stock(port_sarim_objs.deathrune, count = 250, restockCycles = 15)
            stock += stock(port_sarim_objs.pack_firerune, count = 80, restockCycles = 10)
            stock += stock(port_sarim_objs.pack_waterrune, count = 80, restockCycles = 10)
            stock += stock(port_sarim_objs.pack_airrune, count = 80, restockCycles = 10)
            stock += stock(port_sarim_objs.pack_earthrune, count = 80, restockCycles = 10)
            stock += stock(port_sarim_objs.pack_mindrune, count = 40, restockCycles = 10)
            stock += stock(port_sarim_objs.pack_chaosrune, count = 35, restockCycles = 10)
            stock += stock(port_sarim_objs.eye_of_newt, count = 300, restockCycles = 10)
            stock += stock(port_sarim_objs.pack_eye_newt, count = 60, restockCycles = 50)
            stock += stock(port_sarim_objs.bluewizhat, count = 1, restockCycles = 100)
            stock += stock(port_sarim_objs.blackwizhat, count = 1, restockCycles = 100)
        }

        // Brian's Battleaxe Bazaar (specialist: axes)
        edit(port_sarim_invs.battleaxes) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(port_sarim_objs.bronze_battleaxe, count = 4, restockCycles = 200)
            stock += stock(port_sarim_objs.iron_battleaxe, count = 3, restockCycles = 300)
            stock += stock(port_sarim_objs.steel_battleaxe, count = 2, restockCycles = 500)
            stock += stock(port_sarim_objs.black_battleaxe, count = 1, restockCycles = 700)
            stock += stock(port_sarim_objs.mithril_battleaxe, count = 1, restockCycles = 1000)
            stock += stock(port_sarim_objs.adamant_battleaxe, count = 1, restockCycles = 15000)
        }

        // Gerrant's Fishy Business (specialist: fishing)
        edit(port_sarim_invs.gerrant) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(port_sarim_objs.net, count = 5, restockCycles = 100)
            stock += stock(port_sarim_objs.fishing_rod, count = 5, restockCycles = 100)
            stock += stock(port_sarim_objs.fly_fishing_rod, count = 5, restockCycles = 100)
            stock += stock(port_sarim_objs.harpoon, count = 2, restockCycles = 400)
            stock += stock(port_sarim_objs.lobster_pot, count = 2, restockCycles = 400)
            stock += stock(port_sarim_objs.fishing_bait, count = 1500, restockCycles = 1)
            stock += stock(port_sarim_objs.pack_fishing_bait, count = 80, restockCycles = 5)
            stock += stock(port_sarim_objs.feather, count = 1000, restockCycles = 1)
            stock += stock(port_sarim_objs.pack_feather, count = 100, restockCycles = 2)
            stock += stock(port_sarim_objs.raw_shrimp, count = 0, restockCycles = 300)
            stock += stock(port_sarim_objs.raw_sardine, count = 200, restockCycles = 1)
            stock += stock(port_sarim_objs.raw_herring, count = 0, restockCycles = 900)
            stock += stock(port_sarim_objs.raw_anchovies, count = 0, restockCycles = 1200)
            stock += stock(port_sarim_objs.raw_trout, count = 0, restockCycles = 1500)
            stock += stock(port_sarim_objs.raw_pike, count = 0, restockCycles = 1800)
            stock += stock(port_sarim_objs.raw_salmon, count = 0, restockCycles = 2100)
            stock += stock(port_sarim_objs.raw_tuna, count = 0, restockCycles = 2300)
            stock += stock(port_sarim_objs.raw_lobster, count = 0, restockCycles = 2600)
            stock += stock(port_sarim_objs.raw_swordfish, count = 0, restockCycles = 2900)
        }

        // Grum's Gold Exchange (specialist: jewellery, empty until sold to)
        edit(port_sarim_invs.grum) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(port_sarim_objs.gold_ring, count = 0, restockCycles = 1000)
            stock += stock(port_sarim_objs.sapphire_ring, count = 0, restockCycles = 1000)
            stock += stock(port_sarim_objs.emerald_ring, count = 0, restockCycles = 2000)
            stock += stock(port_sarim_objs.ruby_ring, count = 0, restockCycles = 2000)
            stock += stock(port_sarim_objs.diamond_ring, count = 0, restockCycles = 3000)
            stock += stock(port_sarim_objs.gold_necklace, count = 0, restockCycles = 1000)
            stock += stock(port_sarim_objs.sapphire_necklace, count = 0, restockCycles = 2000)
            stock += stock(port_sarim_objs.emerald_necklace, count = 0, restockCycles = 2000)
            stock += stock(port_sarim_objs.ruby_necklace, count = 0, restockCycles = 3000)
            stock += stock(port_sarim_objs.diamond_necklace, count = 0, restockCycles = 3000)
            stock += stock(port_sarim_objs.strung_gold_amulet, count = 0, restockCycles = 1000)
            stock += stock(port_sarim_objs.strung_sapphire_amulet, count = 0, restockCycles = 2000)
            stock += stock(port_sarim_objs.strung_emerald_amulet, count = 0, restockCycles = 3000)
            stock += stock(port_sarim_objs.strung_ruby_amulet, count = 0, restockCycles = 4000)
            stock += stock(port_sarim_objs.strung_diamond_amulet, count = 0, restockCycles = 5000)
        }

        // Rommik's Crafty Supplies (specialist: crafting)
        edit(port_sarim_invs.rommik) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(port_sarim_objs.chisel, count = 2, restockCycles = 100)
            stock += stock(port_sarim_objs.ring_mould, count = 4, restockCycles = 100)
            stock += stock(port_sarim_objs.necklace_mould, count = 2, restockCycles = 100)
            stock += stock(port_sarim_objs.amulet_mould, count = 2, restockCycles = 100)
            stock += stock(port_sarim_objs.needle, count = 3, restockCycles = 100)
            stock += stock(port_sarim_objs.thread, count = 100, restockCycles = 5)
            stock += stock(port_sarim_objs.holy_symbol_mould, count = 3, restockCycles = 100)
            stock += stock(port_sarim_objs.sickle_mould, count = 6, restockCycles = 15)
            stock += stock(port_sarim_objs.tiara_mould, count = 10, restockCycles = 10)
            stock += stock(port_sarim_objs.xbows_silver_bolt_mould, count = 10, restockCycles = 10)
            stock += stock(port_sarim_objs.jewl_bracelet_mould, count = 5, restockCycles = 100)
        }

        // Brian's Archery Supplies (specialist: archery)
        edit(port_sarim_invs.archery) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(port_sarim_objs.steel_arrow, count = 1500, restockCycles = 10)
            stock += stock(port_sarim_objs.mithril_arrow, count = 1000, restockCycles = 15)
            stock += stock(port_sarim_objs.adamant_arrow, count = 800, restockCycles = 20)
            stock += stock(port_sarim_objs.oak_shortbow, count = 4, restockCycles = 100)
            stock += stock(port_sarim_objs.oak_longbow, count = 4, restockCycles = 100)
            stock += stock(port_sarim_objs.willow_shortbow, count = 3, restockCycles = 200)
            stock += stock(port_sarim_objs.willow_longbow, count = 3, restockCycles = 200)
            stock += stock(port_sarim_objs.maple_shortbow, count = 2, restockCycles = 200)
            stock += stock(port_sarim_objs.maple_longbow, count = 2, restockCycles = 200)
        }

        // Rimmington General Store (buys anything, so allStock)
        edit(port_sarim_invs.rimmington_general) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            size = Constants.shop_default_size
            restock = true
            allStock = true
            stock += stock(port_sarim_objs.pot_empty, count = 5, restockCycles = 10)
            stock += stock(port_sarim_objs.jug_empty, count = 2, restockCycles = 100)
            stock += stock(port_sarim_objs.pack_jug_empty, count = 5, restockCycles = 20)
            stock += stock(port_sarim_objs.shears, count = 2, restockCycles = 100)
            stock += stock(port_sarim_objs.bucket_empty, count = 3, restockCycles = 10)
            stock += stock(port_sarim_objs.pack_bucket, count = 15, restockCycles = 10)
            stock += stock(port_sarim_objs.bowl_empty, count = 2, restockCycles = 50)
            stock += stock(port_sarim_objs.cake_tin, count = 2, restockCycles = 50)
            stock += stock(port_sarim_objs.tinderbox, count = 2, restockCycles = 100)
            stock += stock(port_sarim_objs.chisel, count = 2, restockCycles = 100)
            stock += stock(port_sarim_objs.hammer, count = 5, restockCycles = 100)
            stock += stock(port_sarim_objs.newcomer_map, count = 5, restockCycles = 100)
            stock += stock(port_sarim_objs.sos_security_book, count = 5, restockCycles = 100)
        }

        // Karamja General Store (buys anything, so allStock)
        edit(port_sarim_invs.karamja_general) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            size = Constants.shop_default_size
            restock = true
            allStock = true
            stock += stock(port_sarim_objs.pot_empty, count = 5, restockCycles = 10)
            stock += stock(port_sarim_objs.jug_empty, count = 2, restockCycles = 100)
            stock += stock(port_sarim_objs.pack_jug_empty, count = 5, restockCycles = 20)
            stock += stock(port_sarim_objs.shears, count = 2, restockCycles = 100)
            stock += stock(port_sarim_objs.bucket_empty, count = 3, restockCycles = 10)
            stock += stock(port_sarim_objs.pack_bucket, count = 15, restockCycles = 10)
            stock += stock(port_sarim_objs.bowl_empty, count = 2, restockCycles = 50)
            stock += stock(port_sarim_objs.cake_tin, count = 2, restockCycles = 50)
            stock += stock(port_sarim_objs.tinderbox, count = 2, restockCycles = 100)
            stock += stock(port_sarim_objs.chisel, count = 2, restockCycles = 100)
            stock += stock(port_sarim_objs.hammer, count = 5, restockCycles = 100)
        }

        // Karamja Wines, Spirits, and Beers (specialist: its three lines)
        edit(port_sarim_invs.zembo) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(port_sarim_objs.beer, count = 3, restockCycles = 100)
            stock += stock(port_sarim_objs.karamja_rum, count = 3, restockCycles = 100)
            stock += stock(port_sarim_objs.jug_wine, count = 1, restockCycles = 15000)
        }

        // Trader Stan's Trading Post (buys anything, so allStock)
        edit(port_sarim_invs.trader_stan) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            size = Constants.shop_default_size
            restock = true
            allStock = true
            stock += stock(port_sarim_objs.pot_empty, count = 5, restockCycles = 10)
            stock += stock(port_sarim_objs.jug_empty, count = 2, restockCycles = 100)
            stock += stock(port_sarim_objs.pack_jug_empty, count = 6, restockCycles = 15)
            stock += stock(port_sarim_objs.shears, count = 2, restockCycles = 100)
            stock += stock(port_sarim_objs.bucket_empty, count = 3, restockCycles = 10)
            stock += stock(port_sarim_objs.pack_bucket, count = 10, restockCycles = 10)
            stock += stock(port_sarim_objs.bowl_empty, count = 2, restockCycles = 50)
            stock += stock(port_sarim_objs.cake_tin, count = 2, restockCycles = 50)
            stock += stock(port_sarim_objs.tinderbox, count = 2, restockCycles = 100)
            stock += stock(port_sarim_objs.chisel, count = 2, restockCycles = 100)
            stock += stock(port_sarim_objs.hammer, count = 5, restockCycles = 100)
            stock += stock(port_sarim_objs.newcomer_map, count = 5, restockCycles = 100)
            stock += stock(port_sarim_objs.sos_security_book, count = 5, restockCycles = 100)
            stock += stock(port_sarim_objs.rope, count = 2, restockCycles = 300)
            stock += stock(port_sarim_objs.knife, count = 2, restockCycles = 100)
            stock += stock(port_sarim_objs.pineapple, count = 15, restockCycles = 100)
            stock += stock(port_sarim_objs.banana, count = 15, restockCycles = 100)
            stock += stock(port_sarim_objs.orange, count = 10, restockCycles = 100)
            stock += stock(port_sarim_objs.bucket_ectoplasm, count = 10, restockCycles = 100)
            stock += stock(port_sarim_objs.glassblowingpipe, count = 15, restockCycles = 100)
            stock += stock(port_sarim_objs.bucket_sand, count = 10, restockCycles = 100)
            stock += stock(port_sarim_objs.seaweed, count = 20, restockCycles = 100)
            stock += stock(port_sarim_objs.soda_ash, count = 10, restockCycles = 100)
            stock += stock(port_sarim_objs.lobster_pot, count = 20, restockCycles = 100)
            stock += stock(port_sarim_objs.fishing_rod, count = 20, restockCycles = 100)
            stock += stock(port_sarim_objs.swamppaste, count = 30, restockCycles = 100)
            stock += stock(port_sarim_objs.sail_tyras_helm, count = 25, restockCycles = 200)
            stock += stock(port_sarim_objs.raw_rabbit, count = 20, restockCycles = 100)
            stock += stock(port_sarim_objs.eye_patch, count = 5, restockCycles = 100)
        }
    }
}
