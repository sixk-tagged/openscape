@file:Suppress("unused", "SpellCheckingInspection")

package org.rsmod.content.areas.city.draynor.configs

import org.rsmod.api.type.editors.inv.InvEditor
import org.rsmod.api.type.refs.inv.InvReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.type.inv.InvScope
import org.rsmod.game.type.inv.InvStackType
import org.rsmod.game.type.inv.InvType
import org.rsmod.game.type.obj.ObjType

typealias draynor_invs = DraynorInvs

typealias draynor_objs = DraynorObjs

object DraynorInvs : InvReferences() {
    val diango = find("aprilfoolshorseshop")
    val seeds = find("seed_stall")
    val wine = find("wine_merchant")

    // Ned's one-line rope shop has no inv of its own in the cache: this one is unused elsewhere.
    val rope = find("pizzabaseshop")
    val ava = find("anma_shop")
    val slayer = find("slayershop")
}

object DraynorObjs : ObjReferences() {
    val horsey_brown = find("horsey_brown")
    val horsey_white = find("horsey_white")
    val horsey_black = find("horsey_black")
    val horsey_grey = find("horsey_grey")
    val spinning_plate = find("spinning_plate")
    val beach_gloves_yellow = find("beachparty_boxinggloves_yellow")
    val beach_gloves_pink = find("beachparty_boxinggloves_purple")
    val cursed_goblin_hammer = find("goblinraid_warhammer")
    val cursed_goblin_bow = find("goblinraid_bow")
    val cursed_goblin_staff = find("goblinraid_staff")
    val goblin_paint_cannon = find("goblin_rpg")
    val dianngos_claws = find("af15_dragon_claws")
    val green_banner = find("charity_banner")
    val chronicle = find("chronicle")
    val teleport_card = find("chronicle_tele_card")
    val cabbage_cape = find("skillcape_cabbage")
    val cruciferous_codex = find("eoc_book")
    val tiger_toy = find("wwf_toy_tiger")
    val lion_toy = find("wwf_toy_lion")
    val snow_leopard_toy = find("wwf_toy_snow_leopard")
    val amur_leopard_toy = find("wwf_toy_amur_leopard")
    val rainbow_scarf = find("pride17_scarf")
    val holy_handegg = find("easter18_handegg_light")
    val peaceful_handegg = find("easter18_handegg_balance")
    val chaotic_handegg = find("easter18_handegg_chaos")
    val hornwood_helm = find("midsum_helm16")
    val hand_fan = find("midsummer_fan")
    val mask_of_balance = find("summer2015_mask")
    val druidic_wreath = find("wreath")
    val disk_of_returning = find("discofreturning")
    val oculus_orb = find("oculus")

    val potato_seed = find("potato_seed")
    val onion_seed = find("onion_seed")
    val cabbage_seed = find("cabbage_seed")
    val tomato_seed = find("tomato_seed")
    val sweetcorn_seed = find("sweetcorn_seed")
    val strawberry_seed = find("strawberry_seed")
    val watermelon_seed = find("watermelon_seed")
    val barley_seed = find("barley_seed")
    val jute_seed = find("jute_seed")
    val rosemary_seed = find("rosemary_seed")
    val marigold_seed = find("marigold_seed")
    val hammerstone_seed = find("hammerstone_hop_seed")
    val asgarnian_seed = find("asgarnian_hop_seed")
    val yanillian_seed = find("yanillian_hop_seed")
    val krandorian_seed = find("krandorian_hop_seed")
    val wildblood_seed = find("wildblood_hop_seed")

    val jug_wine = find("jug_wine")
    val jug_empty = find("jug_empty")
    val pack_jug_empty = find("pack_jug_empty")
    val bottle_wine = find("rag_bottle_wine")
    val bottle_empty = find("rag_bottle_empty")

    val rope = find("rope")
    val ball_of_wool = find("ball_of_wool")

    val feather = find("feather")
    val pack_feather = find("pack_feather")
    val iron_arrow = find("iron_arrow")
    val steel_arrow = find("steel_arrow")
    val iron_arrowheads = find("iron_arrowheads")
    val steel_arrowheads = find("steel_arrowheads")

    val slayer_gem = find("slayer_gem")
    val mirror_shield = find("slayer_mirror_shield")
    val leaf_bladed_spear = find("slayer_leafbladed_spear")
    val broad_arrows = find("slayer_broad_arrows")
    val bag_of_salt = find("slayer_bag_of_salt")
    val rock_hammer = find("slayer_rock_hammer")
    val facemask = find("slayer_facemask")
    val earmuffs = find("slayer_earmuffs")
    val nose_peg = find("slayer_nosepeg")
    val slayer_staff = find("slayer_staff")
    val spiny_helmet = find("wallbeast_spike_helmet")
    val fishing_explosive = find("fishing_explosive")
    val ice_cooler = find("slayer_icy_water")
    val slayer_gloves = find("deal_slayer_gloves")
    val bug_lantern = find("slayer_buglan_off")
    val insulated_boots = find("slayer_boots")
    val fungicide_spray = find("slayer_spray_pump_10")
    val fungicide = find("slayer_fungicide")
    val witchwood_icon = find("witchwood_icon")
    val slayer_bell = find("molanisk_bell")
    val broad_arrowheads = find("slayer_broad_arrowhead")
    val broad_arrowhead_pack = find("pack_slayer_broad_arrowhead")
    val broad_bolts_unfinished = find("slayer_broad_bolt_unfinished")
    val broad_bolts_pack = find("pack_slayer_broad_bolt_unfinished")
    val rock_thrownhammer = find("slayer_rock_thrownhammer")
    val boots_of_stone = find("boots_of_stone")
    val reinforced_goggles = find("slayer_reinforced_goggles")

    val qp_cape = find("skillcape_qp")
    val qp_hood = find("skillcape_qp_hood")
    val farming_cape = find("skillcape_farming")
    val farming_hood = find("skillcape_farming_hood")
    val escape_crystal = find("gauntlet_escape_crystal")
    val stew = find("stew")
    val magic_cabbage = find("magic_cabbage")
    val garlic = find("garlic")
    val nettles = find("nettles_picked")
    val beekeeper_gloves = find("beekeeper_gloves")
    val chocolate_bar = find("chocolate_bar")
    val attractor = find("anma_30_reward")
    val splitbark_helm = find("splitbark_helm")
    val splitbark_body = find("splitbark_body")
    val splitbark_legs = find("splitbark_legs")
    val splitbark_gauntlets = find("splitbark_gauntlets")
    val splitbark_boots = find("splitbark_greaves")
    val bark = find("hollow_bark")
    val fine_cloth = find("fine_cloth")
    val red_dye = find("reddye")
    val yellow_dye = find("yellowdye")
    val blue_dye = find("bluedye")
    val redberries = find("redberries")
    val onion = find("onion")
    val woad_leaf = find("woadleaf")
}

internal object DraynorInvEditor : InvEditor() {
    init {
        shop(
            draynor_invs.diango,
            line(draynor_objs.horsey_brown, 5, 20),
            line(draynor_objs.horsey_white, 5, 20),
            line(draynor_objs.horsey_black, 5, 20),
            line(draynor_objs.horsey_grey, 5, 20),
            line(draynor_objs.spinning_plate, 250, 20),
            line(draynor_objs.beach_gloves_yellow, 5, 5),
            line(draynor_objs.beach_gloves_pink, 5, 5),
            line(draynor_objs.cursed_goblin_hammer, 3, 5),
            line(draynor_objs.cursed_goblin_bow, 3, 5),
            line(draynor_objs.cursed_goblin_staff, 3, 5),
            line(draynor_objs.goblin_paint_cannon, 100, 1),
            line(draynor_objs.dianngos_claws, 5, 5),
            line(draynor_objs.green_banner, 5, 5),
            line(draynor_objs.chronicle, 1, 1),
            line(draynor_objs.teleport_card, 100, 1),
            line(draynor_objs.cabbage_cape, 10, 1),
            line(draynor_objs.cruciferous_codex, 15, 2),
            line(draynor_objs.tiger_toy, 15, 3),
            line(draynor_objs.lion_toy, 15, 2),
            line(draynor_objs.snow_leopard_toy, 15, 2),
            line(draynor_objs.amur_leopard_toy, 15, 2),
            line(draynor_objs.rainbow_scarf, 5, 1),
            line(draynor_objs.holy_handegg, 100, 1),
            line(draynor_objs.peaceful_handegg, 100, 1),
            line(draynor_objs.chaotic_handegg, 100, 1),
            line(draynor_objs.hornwood_helm, 100, 1),
            line(draynor_objs.hand_fan, 100, 1),
            line(draynor_objs.mask_of_balance, 100, 1),
            line(draynor_objs.druidic_wreath, 100, 1),
            line(draynor_objs.disk_of_returning, 7, 160),
            line(draynor_objs.oculus_orb, 100, 1),
        )

        // Seeds with a base stock of 0 only show once players sell them.
        shop(
            draynor_invs.seeds,
            line(draynor_objs.potato_seed, 20, 100),
            line(draynor_objs.onion_seed, 10, 100),
            line(draynor_objs.cabbage_seed, 5, 100),
            line(draynor_objs.tomato_seed, 0, 100),
            line(draynor_objs.sweetcorn_seed, 0, 100),
            line(draynor_objs.strawberry_seed, 0, 100),
            line(draynor_objs.watermelon_seed, 0, 100),
            line(draynor_objs.barley_seed, 20, 100),
            line(draynor_objs.jute_seed, 5, 100),
            line(draynor_objs.rosemary_seed, 20, 100),
            line(draynor_objs.marigold_seed, 20, 100),
            line(draynor_objs.hammerstone_seed, 20, 100),
            line(draynor_objs.asgarnian_seed, 10, 100),
            line(draynor_objs.yanillian_seed, 5, 100),
            line(draynor_objs.krandorian_seed, 0, 100),
            line(draynor_objs.wildblood_seed, 0, 100),
        )

        // The jug of vinegar is only stocked mid Rag and Bone Man, so it is left out.
        shop(
            draynor_invs.wine,
            line(draynor_objs.jug_wine, 5, 1500),
            line(draynor_objs.jug_empty, 3, 100),
            line(draynor_objs.pack_jug_empty, 5, 15),
            line(draynor_objs.bottle_wine, 2, 1500),
        )

        shop(draynor_invs.rope, line(draynor_objs.rope, 30, 3))

        shop(
            draynor_invs.ava,
            line(draynor_objs.feather, 1000, 100),
            line(draynor_objs.pack_feather, 100, 10),
            line(draynor_objs.iron_arrow, 40, 100),
            line(draynor_objs.steel_arrow, 10, 100),
            line(draynor_objs.iron_arrowheads, 30, 100),
            line(draynor_objs.steel_arrowheads, 20, 100),
        )

        shop(
            draynor_invs.slayer,
            line(draynor_objs.slayer_gem, 50, 100),
            line(draynor_objs.mirror_shield, 100, 100),
            line(draynor_objs.leaf_bladed_spear, 50, 100),
            line(draynor_objs.broad_arrows, 50000, 5),
            line(draynor_objs.bag_of_salt, 5000, 5),
            line(draynor_objs.rock_hammer, 50, 100),
            line(draynor_objs.facemask, 50, 100),
            line(draynor_objs.earmuffs, 50, 100),
            line(draynor_objs.nose_peg, 50, 100),
            line(draynor_objs.slayer_staff, 50, 100),
            line(draynor_objs.spiny_helmet, 50, 100),
            line(draynor_objs.fishing_explosive, 5000, 50),
            line(draynor_objs.ice_cooler, 5000, 5),
            line(draynor_objs.slayer_gloves, 50, 16),
            line(draynor_objs.bug_lantern, 50, 100),
            line(draynor_objs.insulated_boots, 50, 100),
            line(draynor_objs.fungicide_spray, 50, 100),
            line(draynor_objs.fungicide, 5000, 5),
            line(draynor_objs.witchwood_icon, 50, 100),
            line(draynor_objs.slayer_bell, 50, 100),
            line(draynor_objs.broad_arrowheads, 3000, 1),
            line(draynor_objs.broad_arrowhead_pack, 800, 1),
            line(draynor_objs.broad_bolts_unfinished, 5000, 1),
            line(draynor_objs.broad_bolts_pack, 1000, 1),
            line(draynor_objs.rock_thrownhammer, 5000, 5),
            line(draynor_objs.boots_of_stone, 50, 1),
            line(draynor_objs.reinforced_goggles, 50, 1),
        )
    }

    private class Line(val obj: ObjType, val count: Int, val restockCycles: Int)

    private fun line(obj: ObjType, count: Int, restockCycles: Int) = Line(obj, count, restockCycles)

    private fun shop(inv: InvType, vararg lines: Line) {
        edit(inv) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            restock = true
            for (line in lines) {
                stock += stock(line.obj, line.count, line.restockCycles)
            }
        }
    }
}
