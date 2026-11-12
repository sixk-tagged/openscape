@file:Suppress("unused", "SpellCheckingInspection")

package org.rsmod.content.areas.misc.kandarinnorth.configs

import org.rsmod.api.config.Constants
import org.rsmod.api.type.editors.inv.InvEditor
import org.rsmod.api.type.refs.inv.InvReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.type.inv.InvScope
import org.rsmod.game.type.inv.InvStackType

typealias kandarin_north_invs = KandarinNorthInvs

typealias kandarin_north_objs = KandarinNorthObjs

object KandarinNorthInvs : InvReferences() {
    val harry = find("fishingshop2")
    val arhein = find("arheinstore")
    val hickton = find("archeryshop2")
    val candle = find("candleshop")
    val vanessa = find("farming_shop_2")
    val roachey = find("fishingguild")
    val aaron = find("ranging_guild_armourshop")
    val dargaud = find("ranging_guild_bowshop")
    val tribal = find("ranging_guild_tribalshop")
    val legends_general = find("generallegends")
    val erkle = find("legendsguildshop")
}

object KandarinNorthObjs : ObjReferences() {
    val coal = find("coal")
    val obj_3dose1attack = find("3dose1attack")
    val adamant_arrow = find("adamant_arrow")
    val adamant_arrowheads = find("adamant_arrowheads")
    val adamant_javelin = find("adamant_javelin")
    val adamnt_thrownaxe = find("adamnt_thrownaxe")
    val apple_pie = find("apple_pie")
    val arrow_shaft = find("arrow_shaft")
    val asgarnian_hops = find("asgarnian_hops")
    val barley = find("barley")
    val basket_empty = find("basket_empty")
    val bass = find("bass")
    val big_net = find("big_net")
    val bolt = find("bolt")
    val bowl_empty = find("bowl_empty")
    val bronze_arrow = find("bronze_arrow")
    val bronze_arrowheads = find("bronze_arrowheads")
    val bronze_javelin = find("bronze_javelin")
    val bronze_pickaxe = find("bronze_pickaxe")
    val bronze_thrownaxe = find("bronze_thrownaxe")
    val bucket_compost = find("bucket_compost")
    val bucket_empty = find("bucket_empty")
    val cabbage = find("cabbage")
    val cake_tin = find("cake_tin")
    val cape_of_legends = find("cape_of_legends")
    val chisel = find("chisel")
    val cod = find("cod")
    val coif = find("coif")
    val crossbow = find("crossbow")
    val dibber = find("dibber")
    val dragonshield_b = find("dragonshield_b")
    val dusty_key = find("dusty_key")
    val fishing_bait = find("fishing_bait")
    val fishing_rod = find("fishing_rod")
    val fly_fishing_rod = find("fly_fishing_rod")
    val gardening_trowel = find("gardening_trowel")
    val hammer = find("hammer")
    val hammerstone_hops = find("hammerstone_hops")
    val hardleather_body = find("hardleather_body")
    val harpoon = find("harpoon")
    val feather = find("feather")
    val iron_arrow = find("iron_arrow")
    val iron_arrowheads = find("iron_arrowheads")
    val iron_javelin = find("iron_javelin")
    val iron_thrownaxe = find("iron_thrownaxe")
    val jute_fibre = find("jute_fibre")
    val knife = find("knife")
    val krandorian_hops = find("krandorian_hops")
    val leather_armour = find("leather_armour")
    val leather_chaps = find("leather_chaps")
    val leather_cowl = find("leather_cowl")
    val leather_vambraces = find("leather_vambraces")
    val lobster = find("lobster")
    val lobster_pot = find("lobster_pot")
    val longbow = find("longbow")
    val mackerel = find("mackerel")
    val melzarkey = find("melzarkey")
    val mithril_arrow = find("mithril_arrow")
    val mithril_arrowheads = find("mithril_arrowheads")
    val mithril_javelin = find("mithril_javelin")
    val mithril_seed = find("mithril_seed")
    val mithril_thrownaxe = find("mithril_thrownaxe")
    val net = find("net")
    val oak_longbow = find("oak_longbow")
    val oak_shortbow = find("oak_shortbow")
    val onion = find("onion")
    val pack_basket = find("pack_basket")
    val pack_bucket = find("pack_bucket")
    val pack_compost = find("pack_compost")
    val pack_feather = find("pack_feather")
    val pack_fishing_bait = find("pack_fishing_bait")
    val pack_plant_pot_compost = find("pack_plant_pot_compost")
    val pack_sack = find("pack_sack")
    val plant_cure = find("plant_cure")
    val plantpot_compost = find("plantpot_compost")
    val pot_empty = find("pot_empty")
    val potato = find("potato")
    val rake = find("rake")
    val raw_anchovies = find("raw_anchovies")
    val raw_bass = find("raw_bass")
    val raw_cod = find("raw_cod")
    val raw_herring = find("raw_herring")
    val raw_lobster = find("raw_lobster")
    val raw_mackerel = find("raw_mackerel")
    val raw_sardine = find("raw_sardine")
    val raw_shark = find("raw_shark")
    val raw_shrimp = find("raw_shrimp")
    val raw_swordfish = find("raw_swordfish")
    val raw_tuna = find("raw_tuna")
    val rope = find("rope")
    val rune_arrow = find("rune_arrow")
    val rune_arrowheads = find("rune_arrowheads")
    val rune_javelin = find("rune_javelin")
    val rune_thrownaxe = find("rune_thrownaxe")
    val sack_empty = find("sack_empty")
    val secateurs = find("secateurs")
    val shortbow = find("shortbow")
    val spade = find("spade")
    val steel_arrow = find("steel_arrow")
    val steel_arrowheads = find("steel_arrowheads")
    val steel_javelin = find("steel_javelin")
    val steel_thrownaxe = find("steel_thrownaxe")
    val strawberry = find("strawberry")
    val studded_body = find("studded_body")
    val studded_chaps = find("studded_chaps")
    val sweetcorn = find("sweetcorn")
    val swordfish = find("swordfish")
    val tinderbox = find("tinderbox")
    val tomato = find("tomato")
    val tuna = find("tuna")
    val unlit_candle = find("unlit_candle")
    val watering_can_0 = find("watering_can_0")
    val watermelon = find("watermelon")
    val wildblood_hops = find("wildblood_hops")
    val willow_longbow = find("willow_longbow")
    val willow_shortbow = find("willow_shortbow")
    val yanillian_hops = find("yanillian_hops")
    val zogre_bow = find("zogre_bow")
    val zogre_brutal_adamant = find("zogre_brutal_adamant")
    val zogre_brutal_black = find("zogre_brutal_black")
    val zogre_brutal_bronze = find("zogre_brutal_bronze")
    val zogre_brutal_iron = find("zogre_brutal_iron")
    val zogre_brutal_mithril = find("zogre_brutal_mithril")
    val zogre_brutal_rune = find("zogre_brutal_rune")
    val zogre_brutal_steel = find("zogre_brutal_steel")
}

/** Stock and restock times from each shop's wiki page; the skillcape lines are not built. */
internal object KandarinNorthInvEditor : InvEditor() {
    init {
        edit(kandarin_north_invs.harry) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(kandarin_north_objs.net, count = 5, restockCycles = 100)
            stock += stock(kandarin_north_objs.fishing_rod, count = 5, restockCycles = 100)
            stock += stock(kandarin_north_objs.harpoon, count = 2, restockCycles = 400)
            stock += stock(kandarin_north_objs.lobster_pot, count = 2, restockCycles = 400)
            stock += stock(kandarin_north_objs.fishing_bait, count = 1200, restockCycles = 1)
            stock += stock(kandarin_north_objs.pack_fishing_bait, count = 80, restockCycles = 5)
            stock += stock(kandarin_north_objs.big_net, count = 5, restockCycles = 100)
            stock += stock(kandarin_north_objs.raw_shrimp, count = 0, restockCycles = 300)
            stock += stock(kandarin_north_objs.raw_sardine, count = 0, restockCycles = 600)
            stock += stock(kandarin_north_objs.raw_herring, count = 0, restockCycles = 900)
            stock += stock(kandarin_north_objs.raw_mackerel, count = 0, restockCycles = 1000)
            stock += stock(kandarin_north_objs.raw_cod, count = 0, restockCycles = 1100)
            stock += stock(kandarin_north_objs.raw_anchovies, count = 0, restockCycles = 1200)
            stock += stock(kandarin_north_objs.raw_tuna, count = 0, restockCycles = 2300)
            stock += stock(kandarin_north_objs.raw_lobster, count = 0, restockCycles = 2600)
            stock += stock(kandarin_north_objs.raw_bass, count = 0, restockCycles = 2700)
            stock += stock(kandarin_north_objs.raw_swordfish, count = 0, restockCycles = 2900)
            stock += stock(kandarin_north_objs.raw_shark, count = 0, restockCycles = 3500)
        }

        edit(kandarin_north_invs.arhein) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            size = Constants.shop_default_size
            restock = true
            allStock = true
            stock += stock(kandarin_north_objs.bucket_empty, count = 10, restockCycles = 50)
            stock += stock(kandarin_north_objs.pack_bucket, count = 15, restockCycles = 10)
            stock += stock(kandarin_north_objs.bronze_pickaxe, count = 2, restockCycles = 100)
            stock += stock(kandarin_north_objs.bowl_empty, count = 2, restockCycles = 100)
            stock += stock(kandarin_north_objs.cake_tin, count = 2, restockCycles = 100)
            stock += stock(kandarin_north_objs.tinderbox, count = 2, restockCycles = 100)
            stock += stock(kandarin_north_objs.chisel, count = 2, restockCycles = 100)
            stock += stock(kandarin_north_objs.hammer, count = 5, restockCycles = 100)
            stock += stock(kandarin_north_objs.rope, count = 2, restockCycles = 300)
            stock += stock(kandarin_north_objs.pot_empty, count = 2, restockCycles = 100)
            stock += stock(kandarin_north_objs.knife, count = 2, restockCycles = 100)
        }

        edit(kandarin_north_invs.hickton) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(kandarin_north_objs.bolt, count = 200, restockCycles = 10)
            stock += stock(kandarin_north_objs.bronze_arrow, count = 1000, restockCycles = 10)
            stock += stock(kandarin_north_objs.iron_arrow, count = 800, restockCycles = 20)
            stock += stock(kandarin_north_objs.steel_arrow, count = 0, restockCycles = 1000)
            stock += stock(kandarin_north_objs.mithril_arrow, count = 0, restockCycles = 2000)
            stock += stock(kandarin_north_objs.adamant_arrow, count = 0, restockCycles = 3000)
            stock += stock(kandarin_north_objs.rune_arrow, count = 0, restockCycles = 4000)
            stock += stock(kandarin_north_objs.zogre_brutal_bronze, count = 0, restockCycles = 1000)
            stock += stock(kandarin_north_objs.zogre_brutal_iron, count = 0, restockCycles = 1000)
            stock += stock(kandarin_north_objs.zogre_brutal_steel, count = 0, restockCycles = 1000)
            stock += stock(kandarin_north_objs.zogre_brutal_black, count = 0, restockCycles = 1500)
            stock +=
                stock(kandarin_north_objs.zogre_brutal_mithril, count = 0, restockCycles = 2000)
            stock +=
                stock(kandarin_north_objs.zogre_brutal_adamant, count = 0, restockCycles = 3000)
            stock += stock(kandarin_north_objs.zogre_brutal_rune, count = 0, restockCycles = 4000)
            stock += stock(kandarin_north_objs.bronze_arrowheads, count = 1000, restockCycles = 10)
            stock += stock(kandarin_north_objs.iron_arrowheads, count = 800, restockCycles = 20)
            stock += stock(kandarin_north_objs.steel_arrowheads, count = 600, restockCycles = 40)
            stock += stock(kandarin_north_objs.mithril_arrowheads, count = 400, restockCycles = 40)
            stock += stock(kandarin_north_objs.adamant_arrowheads, count = 200, restockCycles = 40)
            stock += stock(kandarin_north_objs.rune_arrowheads, count = 100, restockCycles = 40)
            stock += stock(kandarin_north_objs.shortbow, count = 4, restockCycles = 100)
            stock += stock(kandarin_north_objs.longbow, count = 2, restockCycles = 200)
            stock += stock(kandarin_north_objs.crossbow, count = 2, restockCycles = 400)
            stock += stock(kandarin_north_objs.oak_shortbow, count = 4, restockCycles = 800)
            stock += stock(kandarin_north_objs.oak_longbow, count = 4, restockCycles = 1600)
            stock += stock(kandarin_north_objs.zogre_bow, count = 0, restockCycles = 2000)
            stock += stock(kandarin_north_objs.studded_body, count = 2, restockCycles = 1000)
            stock += stock(kandarin_north_objs.studded_chaps, count = 2, restockCycles = 1000)
        }

        edit(kandarin_north_invs.candle) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(kandarin_north_objs.unlit_candle, count = 10, restockCycles = 100)
        }

        edit(kandarin_north_invs.vanessa) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(kandarin_north_objs.rake, count = 500, restockCycles = 10)
            stock += stock(kandarin_north_objs.dibber, count = 500, restockCycles = 10)
            stock += stock(kandarin_north_objs.secateurs, count = 500, restockCycles = 10)
            stock += stock(kandarin_north_objs.spade, count = 500, restockCycles = 10)
            stock += stock(kandarin_north_objs.gardening_trowel, count = 500, restockCycles = 120)
            stock += stock(kandarin_north_objs.watering_can_0, count = 500, restockCycles = 10)
            stock += stock(kandarin_north_objs.plantpot_compost, count = 500, restockCycles = 10)
            stock +=
                stock(kandarin_north_objs.pack_plant_pot_compost, count = 30, restockCycles = 2)
            stock += stock(kandarin_north_objs.bucket_compost, count = 500, restockCycles = 10)
            stock += stock(kandarin_north_objs.pack_compost, count = 10, restockCycles = 50)
            stock += stock(kandarin_north_objs.sack_empty, count = 500, restockCycles = 10)
            stock += stock(kandarin_north_objs.pack_sack, count = 30, restockCycles = 2)
            stock += stock(kandarin_north_objs.basket_empty, count = 500, restockCycles = 10)
            stock += stock(kandarin_north_objs.pack_basket, count = 30, restockCycles = 2)
            stock += stock(kandarin_north_objs.bucket_empty, count = 100, restockCycles = 10)
            stock += stock(kandarin_north_objs.pack_bucket, count = 15, restockCycles = 10)
            stock += stock(kandarin_north_objs.potato, count = 0, restockCycles = 100)
            stock += stock(kandarin_north_objs.onion, count = 0, restockCycles = 100)
            stock += stock(kandarin_north_objs.cabbage, count = 0, restockCycles = 100)
            stock += stock(kandarin_north_objs.tomato, count = 0, restockCycles = 100)
            stock += stock(kandarin_north_objs.sweetcorn, count = 0, restockCycles = 100)
            stock += stock(kandarin_north_objs.strawberry, count = 0, restockCycles = 100)
            stock += stock(kandarin_north_objs.watermelon, count = 0, restockCycles = 100)
            stock += stock(kandarin_north_objs.hammerstone_hops, count = 0, restockCycles = 100)
            stock += stock(kandarin_north_objs.asgarnian_hops, count = 0, restockCycles = 100)
            stock += stock(kandarin_north_objs.yanillian_hops, count = 0, restockCycles = 100)
            stock += stock(kandarin_north_objs.krandorian_hops, count = 0, restockCycles = 100)
            stock += stock(kandarin_north_objs.wildblood_hops, count = 0, restockCycles = 100)
            stock += stock(kandarin_north_objs.jute_fibre, count = 0, restockCycles = 100)
            stock += stock(kandarin_north_objs.barley, count = 0, restockCycles = 100)
            stock += stock(kandarin_north_objs.plant_cure, count = 100, restockCycles = 10)
        }

        edit(kandarin_north_invs.roachey) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(kandarin_north_objs.net, count = 5, restockCycles = 100)
            stock += stock(kandarin_north_objs.big_net, count = 5, restockCycles = 100)
            stock += stock(kandarin_north_objs.fishing_rod, count = 5, restockCycles = 100)
            stock += stock(kandarin_north_objs.fly_fishing_rod, count = 5, restockCycles = 100)
            stock += stock(kandarin_north_objs.harpoon, count = 2, restockCycles = 400)
            stock += stock(kandarin_north_objs.lobster_pot, count = 2, restockCycles = 400)
            stock += stock(kandarin_north_objs.fishing_bait, count = 2000, restockCycles = 1)
            stock += stock(kandarin_north_objs.pack_fishing_bait, count = 150, restockCycles = 5)
            stock += stock(kandarin_north_objs.feather, count = 1500, restockCycles = 1)
            stock += stock(kandarin_north_objs.pack_feather, count = 150, restockCycles = 2)
            stock += stock(kandarin_north_objs.raw_cod, count = 0, restockCycles = 300)
            stock += stock(kandarin_north_objs.raw_mackerel, count = 0, restockCycles = 600)
            stock += stock(kandarin_north_objs.raw_bass, count = 0, restockCycles = 900)
            stock += stock(kandarin_north_objs.raw_tuna, count = 0, restockCycles = 2300)
            stock += stock(kandarin_north_objs.raw_lobster, count = 0, restockCycles = 2600)
            stock += stock(kandarin_north_objs.raw_swordfish, count = 0, restockCycles = 2900)
            stock += stock(kandarin_north_objs.cod, count = 0, restockCycles = 300)
            stock += stock(kandarin_north_objs.mackerel, count = 0, restockCycles = 600)
            stock += stock(kandarin_north_objs.bass, count = 0, restockCycles = 900)
            stock += stock(kandarin_north_objs.tuna, count = 0, restockCycles = 2300)
            stock += stock(kandarin_north_objs.lobster, count = 0, restockCycles = 2600)
            stock += stock(kandarin_north_objs.swordfish, count = 0, restockCycles = 2900)
        }

        edit(kandarin_north_invs.aaron) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(kandarin_north_objs.leather_armour, count = 10, restockCycles = 50)
            stock += stock(kandarin_north_objs.hardleather_body, count = 10, restockCycles = 60)
            stock += stock(kandarin_north_objs.studded_body, count = 10, restockCycles = 70)
            stock += stock(kandarin_north_objs.leather_chaps, count = 20, restockCycles = 50)
            stock += stock(kandarin_north_objs.studded_chaps, count = 15, restockCycles = 80)
            stock += stock(kandarin_north_objs.coif, count = 10, restockCycles = 50)
            stock += stock(kandarin_north_objs.leather_cowl, count = 10, restockCycles = 70)
            stock += stock(kandarin_north_objs.leather_vambraces, count = 10, restockCycles = 150)
        }

        edit(kandarin_north_invs.dargaud) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(kandarin_north_objs.arrow_shaft, count = 1000, restockCycles = 50)
            stock += stock(kandarin_north_objs.bronze_arrowheads, count = 500, restockCycles = 10)
            stock += stock(kandarin_north_objs.iron_arrowheads, count = 400, restockCycles = 20)
            stock += stock(kandarin_north_objs.steel_arrowheads, count = 300, restockCycles = 30)
            stock += stock(kandarin_north_objs.mithril_arrowheads, count = 200, restockCycles = 40)
            stock += stock(kandarin_north_objs.adamant_arrowheads, count = 200, restockCycles = 50)
            stock += stock(kandarin_north_objs.rune_arrowheads, count = 150, restockCycles = 60)
            stock += stock(kandarin_north_objs.bronze_arrow, count = 1000, restockCycles = 10)
            stock += stock(kandarin_north_objs.iron_arrow, count = 500, restockCycles = 20)
            stock += stock(kandarin_north_objs.steel_arrow, count = 500, restockCycles = 40)
            stock += stock(kandarin_north_objs.mithril_arrow, count = 500, restockCycles = 60)
            stock += stock(kandarin_north_objs.adamant_arrow, count = 450, restockCycles = 120)
            stock += stock(kandarin_north_objs.rune_arrow, count = 400, restockCycles = 130)
            stock += stock(kandarin_north_objs.zogre_brutal_bronze, count = 0, restockCycles = 1000)
            stock += stock(kandarin_north_objs.zogre_brutal_iron, count = 0, restockCycles = 1000)
            stock += stock(kandarin_north_objs.zogre_brutal_steel, count = 0, restockCycles = 1000)
            stock += stock(kandarin_north_objs.zogre_brutal_black, count = 0, restockCycles = 1500)
            stock +=
                stock(kandarin_north_objs.zogre_brutal_mithril, count = 0, restockCycles = 2000)
            stock +=
                stock(kandarin_north_objs.zogre_brutal_adamant, count = 0, restockCycles = 3000)
            stock += stock(kandarin_north_objs.zogre_brutal_rune, count = 0, restockCycles = 4000)
            stock += stock(kandarin_north_objs.shortbow, count = 20, restockCycles = 50)
            stock += stock(kandarin_north_objs.oak_shortbow, count = 20, restockCycles = 60)
            stock += stock(kandarin_north_objs.willow_shortbow, count = 20, restockCycles = 70)
            stock += stock(kandarin_north_objs.longbow, count = 20, restockCycles = 50)
            stock += stock(kandarin_north_objs.oak_longbow, count = 20, restockCycles = 60)
            stock += stock(kandarin_north_objs.zogre_bow, count = 0, restockCycles = 2000)
            stock += stock(kandarin_north_objs.willow_longbow, count = 20, restockCycles = 70)
        }

        edit(kandarin_north_invs.tribal) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(kandarin_north_objs.bronze_javelin, count = 900, restockCycles = 20)
            stock += stock(kandarin_north_objs.iron_javelin, count = 800, restockCycles = 20)
            stock += stock(kandarin_north_objs.steel_javelin, count = 700, restockCycles = 40)
            stock += stock(kandarin_north_objs.mithril_javelin, count = 600, restockCycles = 60)
            stock += stock(kandarin_north_objs.adamant_javelin, count = 500, restockCycles = 80)
            stock += stock(kandarin_north_objs.rune_javelin, count = 400, restockCycles = 100)
            stock += stock(kandarin_north_objs.bronze_thrownaxe, count = 900, restockCycles = 20)
            stock += stock(kandarin_north_objs.iron_thrownaxe, count = 800, restockCycles = 20)
            stock += stock(kandarin_north_objs.steel_thrownaxe, count = 700, restockCycles = 40)
            stock += stock(kandarin_north_objs.mithril_thrownaxe, count = 600, restockCycles = 60)
            stock += stock(kandarin_north_objs.adamnt_thrownaxe, count = 500, restockCycles = 80)
            stock += stock(kandarin_north_objs.rune_thrownaxe, count = 400, restockCycles = 100)
        }

        edit(kandarin_north_invs.legends_general) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            size = Constants.shop_default_size
            restock = true
            allStock = true
            stock += stock(kandarin_north_objs.swordfish, count = 20, restockCycles = 200)
            stock += stock(kandarin_north_objs.apple_pie, count = 5, restockCycles = 200)
            stock += stock(kandarin_north_objs.obj_3dose1attack, count = 3, restockCycles = 100)
            stock += stock(kandarin_north_objs.steel_arrow, count = 500, restockCycles = 100)
        }

        edit(kandarin_north_invs.erkle) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            stock += stock(kandarin_north_objs.mithril_seed, count = 6, restockCycles = 50)
            stock += stock(kandarin_north_objs.dusty_key, count = 5, restockCycles = 100)
            stock += stock(kandarin_north_objs.melzarkey, count = 3, restockCycles = 300)
            stock += stock(kandarin_north_objs.dragonshield_b, count = 1, restockCycles = 500)
            stock += stock(kandarin_north_objs.cape_of_legends, count = 3, restockCycles = 500)
        }
    }
}
