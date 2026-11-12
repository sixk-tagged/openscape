@file:Suppress("unused", "SpellCheckingInspection")

package org.rsmod.content.areas.city.ardougne.configs

import org.rsmod.api.type.editors.inv.InvEditor
import org.rsmod.api.type.refs.inv.InvReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.type.inv.InvScope
import org.rsmod.game.type.inv.InvStackType
import org.rsmod.game.type.inv.InvType
import org.rsmod.game.type.obj.ObjType

typealias ardougne_invs = ArdougneInvs

typealias ardougne_objs = ArdougneObjs

object ArdougneInvs : InvReferences() {
    val aemad = find("adventurershop")
    // UNVERIFIED: the vanilla invs below are unused ones picked by name, not by what the cache
    // calls them.
    val zenesha = find("2handedshop")
    val bakery = find("bakery")
    val silver = find("silverstall")
    val gems = find("ardougnegemstall")
    val spice = find("spicestall")
    val fur = find("furstall")
    val farming = find("farming_shop_3")
    val west_general = find("regicide_general_shop")
    val khazard_general = find("khazardshop")
    val witchaven_tackle = find("shilofishingshop")
    val gnome_village = find("junglestore")
}

object ArdougneObjs : ObjReferences() {
    val vial_water = find("vial_water")
    val vial_pack = find("pack_vial_water")
    val bronze_pickaxe = find("bronze_pickaxe")
    val iron_axe = find("iron_axe")
    val bronze_axe = find("bronze_axe")
    val cooked_meat = find("cooked_meat")
    val tinderbox = find("tinderbox")
    val ball_of_wool = find("ball_of_wool")
    val bronze_arrow = find("bronze_arrow")
    val rope = find("rope")
    val papyrus = find("papyrus")
    val knife = find("knife")
    val pot = find("pot_empty")
    val jug = find("jug_empty")
    val jug_pack = find("pack_jug_empty")
    val shears = find("shears")
    val bucket = find("bucket_empty")
    val bucket_pack = find("pack_bucket")
    val chisel = find("chisel")
    val hammer = find("hammer")
    val pot_of_flour = find("pot_flour")
    val bailing_bucket = find("bucket_bailing")
    val swamp_paste = find("swamppaste")
    val leather_boots = find("leather_boots")
    val longbow = find("longbow")
    val salmon = find("salmon")
    val meat_pie = find("meat_pie")
    val bread = find("bread")
    val cake = find("cake")
    val chocolate_slice = find("chocolate_slice")
    val chocolate_bar = find("chocolate_bar")

    val bronze_platebody = find("bronze_platebody")
    val iron_platebody = find("iron_platebody")
    val steel_platebody = find("steel_platebody")
    val black_platebody = find("black_platebody")
    val mithril_platebody = find("mithril_platebody")

    val unstrung_symbol = find("nostringstar")
    val silver_ore = find("silver_ore")
    val silver_bar = find("silver_bar")
    val sapphire = find("sapphire")
    val emerald = find("emerald")
    val ruby = find("ruby")
    val diamond = find("diamond")
    val spice = find("spicespot")
    val garlic = find("garlic")
    val bear_fur = find("fur")
    val grey_wolf_fur = find("grey_wolf_fur")
    val polar_kebbit_fur = find("huntingbeast_polar_fur")
    val common_kebbit_fur = find("huntingbeast_woodland_fur")
    val feldip_weasel_fur = find("huntingbeast_jungle_fur")
    val desert_devil_fur = find("huntingbeast_desert_fur")
    val tatty_larupia_fur = find("hunting_fur_jaguar_shabby")
    val larupia_fur = find("hunting_fur_jaguar_perfect")
    val tatty_graahk_fur = find("hunting_fur_leopard_shabby")
    val graahk_fur = find("hunting_fur_leopard_perfect")
    val tatty_kyatt_fur = find("hunting_fur_tiger_shabby")
    val kyatt_fur = find("hunting_fur_tiger_perfect")

    val rake = find("rake")
    val seed_dibber = find("dibber")
    val secateurs = find("secateurs")
    val spade = find("spade")
    val trowel = find("gardening_trowel")
    val watering_can = find("watering_can_0")
    val filled_plant_pot = find("plantpot_compost")
    val plant_pot_pack = find("pack_plant_pot_compost")
    val compost = find("bucket_compost")
    val compost_pack = find("pack_compost")
    val empty_sack = find("sack_empty")
    val sack_pack = find("pack_sack")
    val basket = find("basket_empty")
    val basket_pack = find("pack_basket")
    val potato = find("potato")
    val onion = find("onion")
    val cabbage = find("cabbage")
    val tomato = find("tomato")
    val sweetcorn = find("sweetcorn")
    val strawberry = find("strawberry")
    val watermelon = find("watermelon")
    val hammerstone_hops = find("hammerstone_hops")
    val asgarnian_hops = find("asgarnian_hops")
    val yanillian_hops = find("yanillian_hops")
    val krandorian_hops = find("krandorian_hops")
    val wildblood_hops = find("wildblood_hops")
    val jute_fibre = find("jute_fibre")
    val barley = find("barley")
    val plant_cure = find("plant_cure")

    val small_net = find("net")
    val fishing_rod = find("fishing_rod")
    val fly_fishing_rod = find("fly_fishing_rod")
    val harpoon = find("harpoon")
    val lobster_pot = find("lobster_pot")
    val fishing_bait = find("fishing_bait")
    val bait_pack = find("pack_fishing_bait")
    val feather = find("feather")
    val feather_pack = find("pack_feather")
    val raw_shrimp = find("raw_shrimp")
    val raw_sardine = find("raw_sardine")
    val raw_herring = find("raw_herring")
    val raw_anchovies = find("raw_anchovies")
    val raw_trout = find("raw_trout")
    val raw_pike = find("raw_pike")
    val raw_salmon = find("raw_salmon")
    val raw_tuna = find("raw_tuna")
    val raw_lobster = find("raw_lobster")
    val raw_swordfish = find("raw_swordfish")

    val beer = find("beer")
    val khali_brew = find("khali_brew")
}

internal object ArdougneInvEditor : InvEditor() {
    init {
        // The stock lines are the wiki's: (item, stock, restock cycles).
        shop(
            ardougne_invs.aemad,
            general = true,
            line(ardougne_objs.vial_water, 500, 30),
            line(ardougne_objs.vial_pack, 250, 10),
            line(ardougne_objs.bronze_pickaxe, 2, 100),
            line(ardougne_objs.iron_axe, 2, 100),
            line(ardougne_objs.cooked_meat, 2, 100),
            line(ardougne_objs.tinderbox, 2, 100),
            line(ardougne_objs.ball_of_wool, 30, 100),
            line(ardougne_objs.bronze_arrow, 500, 10),
            line(ardougne_objs.rope, 20, 100),
            line(ardougne_objs.papyrus, 50, 1000),
            line(ardougne_objs.knife, 2, 100),
        )

        shop(
            ardougne_invs.zenesha,
            line(ardougne_objs.bronze_platebody, 3, 100),
            line(ardougne_objs.iron_platebody, 1, 200),
            line(ardougne_objs.steel_platebody, 1, 400),
            line(ardougne_objs.black_platebody, 1, 3000),
            line(ardougne_objs.mithril_platebody, 1, 3000),
        )

        // UNVERIFIED: the east and west bakers have their own restock times on the wiki but share
        // this one inv; these are the east baker's.
        shop(
            ardougne_invs.bakery,
            line(ardougne_objs.bread, 10, 150),
            line(ardougne_objs.cake, 3, 1000),
            line(ardougne_objs.chocolate_slice, 6, 400),
            line(ardougne_objs.chocolate_bar, 9, 250),
        )

        shop(
            ardougne_invs.silver,
            line(ardougne_objs.unstrung_symbol, 2, 100),
            line(ardougne_objs.silver_ore, 1, 2000),
            line(ardougne_objs.silver_bar, 1, 1000),
        )

        shop(
            ardougne_invs.gems,
            line(ardougne_objs.sapphire, 2, 15000),
            line(ardougne_objs.emerald, 1, 35000),
            line(ardougne_objs.ruby, 1, 60000),
            line(ardougne_objs.diamond, 0, 4000),
        )

        shop(
            ardougne_invs.spice,
            line(ardougne_objs.spice, 1, 100),
            line(ardougne_objs.knife, 1, 2),
            line(ardougne_objs.garlic, 2, 5),
        )

        shop(
            ardougne_invs.fur,
            line(ardougne_objs.bear_fur, 3, 100),
            line(ardougne_objs.grey_wolf_fur, 3, 100),
            line(ardougne_objs.polar_kebbit_fur, 0, 100),
            line(ardougne_objs.common_kebbit_fur, 0, 100),
            line(ardougne_objs.feldip_weasel_fur, 0, 100),
            line(ardougne_objs.desert_devil_fur, 0, 100),
            line(ardougne_objs.tatty_larupia_fur, 0, 100),
            line(ardougne_objs.larupia_fur, 0, 100),
            line(ardougne_objs.tatty_graahk_fur, 0, 100),
            line(ardougne_objs.graahk_fur, 0, 100),
            line(ardougne_objs.tatty_kyatt_fur, 0, 100),
            line(ardougne_objs.kyatt_fur, 0, 100),
        )

        // Richard buys crops: the produce lines start empty and fill as players sell them.
        shop(
            ardougne_invs.farming,
            line(ardougne_objs.rake, 500, 10),
            line(ardougne_objs.seed_dibber, 500, 10),
            line(ardougne_objs.secateurs, 500, 10),
            line(ardougne_objs.spade, 500, 10),
            line(ardougne_objs.trowel, 500, 120),
            line(ardougne_objs.watering_can, 500, 10),
            line(ardougne_objs.filled_plant_pot, 500, 10),
            line(ardougne_objs.plant_pot_pack, 30, 2),
            line(ardougne_objs.compost, 500, 10),
            line(ardougne_objs.compost_pack, 10, 50),
            line(ardougne_objs.empty_sack, 500, 10),
            line(ardougne_objs.sack_pack, 30, 2),
            line(ardougne_objs.basket, 500, 10),
            line(ardougne_objs.basket_pack, 30, 2),
            line(ardougne_objs.bucket, 100, 10),
            line(ardougne_objs.bucket_pack, 15, 10),
            line(ardougne_objs.potato, 0, 100),
            line(ardougne_objs.onion, 0, 100),
            line(ardougne_objs.cabbage, 0, 100),
            line(ardougne_objs.tomato, 0, 100),
            line(ardougne_objs.sweetcorn, 0, 100),
            line(ardougne_objs.strawberry, 0, 100),
            line(ardougne_objs.watermelon, 0, 100),
            line(ardougne_objs.hammerstone_hops, 0, 100),
            line(ardougne_objs.asgarnian_hops, 0, 100),
            line(ardougne_objs.yanillian_hops, 0, 100),
            line(ardougne_objs.krandorian_hops, 0, 100),
            line(ardougne_objs.wildblood_hops, 0, 100),
            line(ardougne_objs.jute_fibre, 0, 100),
            line(ardougne_objs.barley, 0, 100),
            line(ardougne_objs.plant_cure, 100, 10),
        )

        shop(
            ardougne_invs.west_general,
            general = true,
            line(ardougne_objs.pot, 3, 100),
            line(ardougne_objs.rope, 3, 100),
            line(ardougne_objs.bronze_pickaxe, 2, 100),
            line(ardougne_objs.bucket, 2, 100),
            line(ardougne_objs.bucket_pack, 10, 10),
            line(ardougne_objs.tinderbox, 2, 100),
            line(ardougne_objs.hammer, 3, 100),
            line(ardougne_objs.leather_boots, 2, 100),
            line(ardougne_objs.longbow, 2, 100),
            line(ardougne_objs.bronze_arrow, 20, 100),
            line(ardougne_objs.salmon, 10, 100),
            line(ardougne_objs.meat_pie, 10, 100),
            line(ardougne_objs.bread, 5, 100),
            line(ardougne_objs.cooked_meat, 10, 100),
        )

        shop(
            ardougne_invs.khazard_general,
            general = true,
            line(ardougne_objs.bronze_pickaxe, 5, 100),
            line(ardougne_objs.pot, 3, 100),
            line(ardougne_objs.jug, 2, 100),
            line(ardougne_objs.jug_pack, 5, 20),
            line(ardougne_objs.shears, 2, 100),
            line(ardougne_objs.bucket, 2, 100),
            line(ardougne_objs.bucket_pack, 15, 10),
            line(ardougne_objs.tinderbox, 2, 100),
            line(ardougne_objs.chisel, 2, 100),
            line(ardougne_objs.hammer, 5, 100),
            line(ardougne_objs.rope, 30, 20),
            line(ardougne_objs.pot_of_flour, 30, 100),
            line(ardougne_objs.bailing_bucket, 30, 100),
            line(ardougne_objs.swamp_paste, 5000, 10),
            line(ardougne_objs.knife, 2, 100),
            line(ardougne_objs.bronze_axe, 5, 100),
        )

        shop(
            ardougne_invs.witchaven_tackle,
            line(ardougne_objs.small_net, 5, 100),
            line(ardougne_objs.fishing_rod, 5, 100),
            line(ardougne_objs.fly_fishing_rod, 5, 100),
            line(ardougne_objs.harpoon, 2, 400),
            line(ardougne_objs.lobster_pot, 2, 400),
            line(ardougne_objs.fishing_bait, 1500, 1),
            line(ardougne_objs.bait_pack, 80, 5),
            line(ardougne_objs.feather, 1000, 1),
            line(ardougne_objs.feather_pack, 100, 2),
            line(ardougne_objs.raw_shrimp, 0, 300),
            line(ardougne_objs.raw_sardine, 200, 1),
            line(ardougne_objs.raw_herring, 0, 900),
            line(ardougne_objs.raw_anchovies, 0, 1200),
            line(ardougne_objs.raw_trout, 0, 1500),
            line(ardougne_objs.raw_pike, 0, 1800),
            line(ardougne_objs.raw_salmon, 0, 2100),
            line(ardougne_objs.raw_tuna, 0, 2300),
            line(ardougne_objs.raw_lobster, 0, 2600),
            line(ardougne_objs.raw_swordfish, 0, 2900),
        )

        shop(
            ardougne_invs.gnome_village,
            general = true,
            line(ardougne_objs.pot, 3, 100),
            line(ardougne_objs.bronze_pickaxe, 5, 100),
            line(ardougne_objs.jug, 2, 100),
            line(ardougne_objs.jug_pack, 5, 20),
            line(ardougne_objs.shears, 2, 100),
            line(ardougne_objs.bucket, 2, 100),
            line(ardougne_objs.bucket_pack, 15, 10),
            line(ardougne_objs.tinderbox, 2, 100),
            line(ardougne_objs.chisel, 2, 100),
            line(ardougne_objs.hammer, 5, 100),
            line(ardougne_objs.bronze_arrow, 30, 100),
            line(ardougne_objs.cooked_meat, 2, 100),
        )
    }

    private class Line(val obj: ObjType, val count: Int, val restockCycles: Int)

    private fun line(obj: ObjType, count: Int, restockCycles: Int) = Line(obj, count, restockCycles)

    private fun shop(inv: InvType, vararg lines: Line) = shop(inv, general = false, *lines)

    /** A general store keeps the cache's 40 slots so it has room for what players sell it. */
    private fun shop(inv: InvType, general: Boolean, vararg lines: Line) {
        edit(inv) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            restock = true
            allStock = general
            autoSize = !general
            for (line in lines) {
                stock += stock(line.obj, line.count, line.restockCycles)
            }
        }
    }
}
