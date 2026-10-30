@file:Suppress("unused", "SpellCheckingInspection")

package org.rsmod.content.areas.city.falador.configs

import org.rsmod.api.type.editors.inv.InvEditor
import org.rsmod.api.type.refs.currency.CurrencyReferences
import org.rsmod.api.type.refs.inv.InvReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.type.inv.InvScope
import org.rsmod.game.type.inv.InvStackType
import org.rsmod.game.type.inv.InvType
import org.rsmod.game.type.obj.ObjType

typealias falador_invs = FaladorInvs

typealias falador_objs = FaladorObjs

typealias falador_currencies = FaladorCurrencies

object FaladorInvs : InvReferences() {
    val general = find("generalshop4")
    val maces = find("maceshop")
    val shields = find("shieldshop")
    // UNVERIFIED: which of the three gem shop invs is Herquin's (Al Kharid's Gem Trader has the
    // first).
    val gems = find("gemshop2")
    val chains = find("chainmailshop")
    val farming = find("farming_shop_1")
    // `miningstore` is Prospector Percy's nugget shop (content/skills/mining), so Drogo gets this
    // unused vanilla inv instead.
    val drogo = find("dueldisplay_dummy")
    val pickaxes = find("pickaxeshop")
    val crossbows = find("xbows_shop")
    val dwarven_general = find("generaldwarf")
    val guild_ores = find("mguild_oreshop")
    val guild_pickaxes = find("mguild_pickaxeshop")
    val white_knight_novice = find("whiteknight_armoury1")
    val white_knight_peon = find("whiteknight_armoury2")
    val white_knight_page = find("whiteknight_armoury3")
    val white_knight_noble = find("whiteknight_armoury4")
    val white_knight_adept = find("whiteknight_armoury5")
    val white_knight_master = find("whiteknight_armoury6")
    val temple_knight = find("templeknight_armoury2")
    /** Stocked by content/skills/mining, which also runs its minerals currency. */
    val mineral_shop = find("mguild_rewardshop")
}

object FaladorCurrencies : CurrencyReferences() {
    val minerals = find("mguild_minerals")
}

object FaladorObjs : ObjReferences() {
    val pot = find("pot_empty")
    val jug = find("jug_empty")
    val jug_pack = find("pack_jug_empty")
    val shears = find("shears")
    val bucket = find("bucket_empty")
    val bucket_pack = find("pack_bucket")
    val bowl = find("bowl_empty")
    val cake_tin = find("cake_tin")
    val tinderbox = find("tinderbox")
    val chisel = find("chisel")
    val hammer = find("hammer")
    val newcomer_map = find("newcomer_map")
    val security_book = find("sos_security_book")

    val bronze_mace = find("bronze_mace")
    val iron_mace = find("iron_mace")
    val steel_mace = find("steel_mace")
    val mithril_mace = find("mithril_mace")
    val adamant_mace = find("adamant_mace")

    val wooden_shield = find("wooden_shield")
    val bronze_sq_shield = find("bronze_sq_shield")
    val bronze_kiteshield = find("bronze_kiteshield")
    val iron_sq_shield = find("iron_sq_shield")
    val iron_kiteshield = find("iron_kiteshield")
    val steel_sq_shield = find("steel_sq_shield")
    val steel_kiteshield = find("steel_kiteshield")
    val mithril_sq_shield = find("mithril_sq_shield")

    val uncut_sapphire = find("uncut_sapphire")
    val uncut_emerald = find("uncut_emerald")
    val uncut_ruby = find("uncut_ruby")
    val uncut_diamond = find("uncut_diamond")
    val sapphire = find("sapphire")
    val emerald = find("emerald")
    val ruby = find("ruby")
    val diamond = find("diamond")

    val bronze_chainbody = find("bronze_chainbody")
    val iron_chainbody = find("iron_chainbody")
    val steel_chainbody = find("steel_chainbody")
    val black_chainbody = find("black_chainbody")
    val mithril_chainbody = find("mithril_chainbody")
    val adamant_chainbody = find("adamant_chainbody")

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

    val bronze_pickaxe = find("bronze_pickaxe")
    val iron_pickaxe = find("iron_pickaxe")
    val steel_pickaxe = find("steel_pickaxe")
    val mithril_pickaxe = find("mithril_pickaxe")
    val adamant_pickaxe = find("adamant_pickaxe")
    val rune_pickaxe = find("rune_pickaxe")
    val copper_ore = find("copper_ore")
    val tin_ore = find("tin_ore")
    val iron_ore = find("iron_ore")
    val coal = find("coal")
    val mithril_ore = find("mithril_ore")
    val adamantite_ore = find("adamantite_ore")
    val runite_ore = find("runite_ore")
    val bronze_bar = find("bronze_bar")
    val iron_bar = find("iron_bar")
    val gold_bar = find("gold_bar")

    val wooden_stock = find("xbows_crossbow_stock_wood")
    val oak_stock = find("xbows_crossbow_stock_oak")
    val willow_stock = find("xbows_crossbow_stock_willow")
    val teak_stock = find("xbows_crossbow_stock_teak")
    val maple_stock = find("xbows_crossbow_stock_maple")
    val mahogany_stock = find("xbows_crossbow_stock_mahogany")
    val yew_stock = find("xbows_crossbow_stock_yew")
    val bronze_limbs = find("xbows_crossbow_limbs_bronze")
    val iron_limbs = find("xbows_crossbow_limbs_iron")
    val steel_limbs = find("xbows_crossbow_limbs_steel")
    val mithril_limbs = find("xbows_crossbow_limbs_mithril")
    val adamantite_limbs = find("xbows_crossbow_limbs_adamantite")
    val runite_limbs = find("xbows_crossbow_limbs_runite")
    val bronze_crossbow = find("xbows_crossbow_bronze")
    val iron_crossbow = find("xbows_crossbow_iron")
    val steel_crossbow = find("xbows_crossbow_steel")
    val mithril_crossbow = find("xbows_crossbow_mithril")
    val adamant_crossbow = find("xbows_crossbow_adamantite")
    val rune_crossbow = find("xbows_crossbow_runite")

    val white_chainbody = find("white_chainbody")
    val white_platelegs = find("white_platelegs")
    val white_dagger = find("white_dagger")
    val white_sword = find("white_sword")
    val white_med_helm = find("white_med_helm")
    val white_plateskirt = find("white_plateskirt")
    val white_sq_shield = find("white_sq_shield")
    val white_mace = find("white_mace")
    val white_magic_staff = find("white_magic_staff")
    val white_longsword = find("white_longsword")
    val white_full_helm = find("white_full_helm")
    val white_platebody = find("white_platebody")
    val white_kiteshield = find("white_kiteshield")
    val white_boots = find("white_armoured_boots")
    val white_gloves = find("white_gloves")
    val white_2h_sword = find("white_2h_sword")
    val white_halberd = find("white_halberd")
    val white_scimitar = find("white_scimitar")
    val white_warhammer = find("white_warhammer")
    val white_battleaxe = find("white_battleaxe")
    val white_claws = find("white_claws")

    val initiate_sallet = find("basic_tk_helm")
    val initiate_cuisse = find("basic_tk_legs")
    val initiate_hauberk = find("basic_tk_body")
    val initiate_harness = find("basic_tk_pack_yellow")
    val proselyte_sallet = find("basic_tk_rank2_helm")
    val proselyte_cuisse = find("basic_tk_rank2_legs")
    val proselyte_hauberk = find("basic_tk_rank2_body")
    val proselyte_tasset = find("basic_tk_rank2_plateskirt")
    val proselyte_harness = find("basic_tk_pack_orange")
    val proselyte_harness_f = find("basic_tk_pack_female")

    val asgarnian_ale = find("asgarnian_ale")
    val wizards_mind_bomb = find("wizards_mind_bomb")
    val dwarven_stout = find("dwarven_stout")
    val beer_glass = find("beer_glass")
    val beer_glass_noted = find("cert_beer_glass")
    val woad_leaf = find("woadleaf")
    val mining_cape = find("skillcape_mining")
    val mining_gloves = find("mguild_gloves")
    val superior_mining_gloves = find("mguild_gloves_superior")
    val expert_mining_gloves = find("mguild_gloves_expert")
    val minerals = find("mguild_minerals")
    val magic_secateurs = find("fairy_enchanted_secateurs")
    val mining_cape_trimmed = find("skillcape_mining_trimmed")
    val mining_hood = find("skillcape_mining_hood")
}

internal object FaladorInvEditor : InvEditor() {
    init {
        shop(
            falador_invs.general,
            general = true,
            line(falador_objs.pot, 5, 10),
            line(falador_objs.jug, 2, 100),
            line(falador_objs.jug_pack, 5, 20),
            line(falador_objs.shears, 2, 100),
            line(falador_objs.bucket, 3, 10),
            line(falador_objs.bucket_pack, 15, 10),
            line(falador_objs.bowl, 2, 50),
            line(falador_objs.cake_tin, 2, 50),
            line(falador_objs.tinderbox, 2, 100),
            line(falador_objs.chisel, 2, 100),
            line(falador_objs.hammer, 5, 100),
            line(falador_objs.newcomer_map, 5, 100),
            line(falador_objs.security_book, 5, 100),
        )

        shop(
            falador_invs.maces,
            line(falador_objs.bronze_mace, 5, 100),
            line(falador_objs.iron_mace, 4, 200),
            line(falador_objs.steel_mace, 4, 400),
            line(falador_objs.mithril_mace, 3, 3000),
            line(falador_objs.adamant_mace, 2, 12000),
        )

        shop(
            falador_invs.shields,
            line(falador_objs.wooden_shield, 5, 100),
            line(falador_objs.bronze_sq_shield, 3, 100),
            line(falador_objs.bronze_kiteshield, 3, 300),
            line(falador_objs.iron_sq_shield, 2, 400),
            line(falador_objs.iron_kiteshield, 0, 1500),
            line(falador_objs.steel_sq_shield, 0, 1500),
            line(falador_objs.steel_kiteshield, 0, 1500),
            line(falador_objs.mithril_sq_shield, 0, 1500),
        )

        shop(
            falador_invs.gems,
            line(falador_objs.uncut_sapphire, 1, 25000),
            line(falador_objs.uncut_emerald, 0, 1000),
            line(falador_objs.uncut_ruby, 0, 2000),
            line(falador_objs.uncut_diamond, 0, 4000),
            line(falador_objs.sapphire, 1, 15000),
            line(falador_objs.emerald, 0, 1000),
            line(falador_objs.ruby, 0, 2000),
            line(falador_objs.diamond, 0, 4000),
        )

        shop(
            falador_invs.chains,
            line(falador_objs.bronze_chainbody, 3, 200),
            line(falador_objs.iron_chainbody, 2, 300),
            line(falador_objs.steel_chainbody, 1, 2000),
            line(falador_objs.black_chainbody, 1, 2500),
            line(falador_objs.mithril_chainbody, 1, 3500),
            line(falador_objs.adamant_chainbody, 1, 9000),
        )

        // Sarah buys crops: the produce lines start empty and fill as players sell them.
        shop(
            falador_invs.farming,
            line(falador_objs.rake, 500, 10),
            line(falador_objs.seed_dibber, 500, 10),
            line(falador_objs.secateurs, 500, 10),
            line(falador_objs.spade, 500, 10),
            line(falador_objs.trowel, 500, 120),
            line(falador_objs.watering_can, 500, 10),
            line(falador_objs.filled_plant_pot, 500, 10),
            line(falador_objs.plant_pot_pack, 30, 2),
            line(falador_objs.compost, 500, 10),
            line(falador_objs.compost_pack, 10, 50),
            line(falador_objs.empty_sack, 500, 10),
            line(falador_objs.sack_pack, 30, 2),
            line(falador_objs.basket, 500, 10),
            line(falador_objs.basket_pack, 30, 2),
            line(falador_objs.bucket, 100, 10),
            line(falador_objs.bucket_pack, 15, 10),
            line(falador_objs.potato, 0, 100),
            line(falador_objs.onion, 0, 100),
            line(falador_objs.cabbage, 0, 100),
            line(falador_objs.tomato, 0, 100),
            line(falador_objs.sweetcorn, 0, 100),
            line(falador_objs.strawberry, 0, 100),
            line(falador_objs.watermelon, 0, 100),
            line(falador_objs.hammerstone_hops, 0, 100),
            line(falador_objs.asgarnian_hops, 0, 100),
            line(falador_objs.yanillian_hops, 0, 100),
            line(falador_objs.krandorian_hops, 0, 100),
            line(falador_objs.wildblood_hops, 0, 100),
            line(falador_objs.jute_fibre, 0, 100),
            line(falador_objs.barley, 0, 100),
            line(falador_objs.plant_cure, 100, 10),
        )

        shop(
            falador_invs.drogo,
            line(falador_objs.hammer, 4, 100),
            line(falador_objs.bronze_pickaxe, 4, 100),
            line(falador_objs.copper_ore, 0, 15000),
            line(falador_objs.tin_ore, 0, 15000),
            line(falador_objs.iron_ore, 0, 5000),
            line(falador_objs.coal, 0, 5000),
            line(falador_objs.bronze_bar, 0, 4000),
            line(falador_objs.iron_bar, 0, 4000),
            line(falador_objs.gold_bar, 0, 3000),
        )

        // Nurmof's and Yarsul's shops stock the same pickaxes.
        for (inv in listOf(falador_invs.pickaxes, falador_invs.guild_pickaxes)) {
            shop(
                inv,
                line(falador_objs.bronze_pickaxe, 6, 50),
                line(falador_objs.iron_pickaxe, 5, 100),
                line(falador_objs.steel_pickaxe, 4, 200),
                line(falador_objs.mithril_pickaxe, 3, 300),
                line(falador_objs.adamant_pickaxe, 2, 500),
                line(falador_objs.rune_pickaxe, 1, 700),
            )
        }

        shop(
            falador_invs.crossbows,
            line(falador_objs.wooden_stock, 5, 100),
            line(falador_objs.oak_stock, 5, 100),
            line(falador_objs.willow_stock, 5, 100),
            line(falador_objs.teak_stock, 5, 100),
            line(falador_objs.maple_stock, 5, 100),
            line(falador_objs.mahogany_stock, 0, 100),
            line(falador_objs.yew_stock, 0, 100),
            line(falador_objs.bronze_limbs, 5, 100),
            line(falador_objs.iron_limbs, 5, 100),
            line(falador_objs.steel_limbs, 5, 100),
            line(falador_objs.mithril_limbs, 5, 100),
            line(falador_objs.adamantite_limbs, 0, 100),
            line(falador_objs.runite_limbs, 0, 100),
            line(falador_objs.bronze_crossbow, 0, 100),
            line(falador_objs.iron_crossbow, 0, 100),
            line(falador_objs.steel_crossbow, 0, 100),
            line(falador_objs.mithril_crossbow, 0, 100),
            line(falador_objs.adamant_crossbow, 0, 100),
            line(falador_objs.rune_crossbow, 0, 100),
        )

        shop(
            falador_invs.dwarven_general,
            general = true,
            line(falador_objs.pot, 3, 100),
            line(falador_objs.jug, 2, 100),
            line(falador_objs.jug_pack, 5, 20),
            line(falador_objs.shears, 2, 100),
            line(falador_objs.bucket, 3, 10),
            line(falador_objs.bucket_pack, 15, 10),
            line(falador_objs.tinderbox, 2, 100),
            line(falador_objs.chisel, 2, 100),
            line(falador_objs.hammer, 5, 100),
        )

        // Hendor stocks nothing of his own: only what players sell him.
        shop(
            falador_invs.guild_ores,
            line(falador_objs.copper_ore, 0, 15000),
            line(falador_objs.tin_ore, 0, 15000),
            line(falador_objs.iron_ore, 0, 5000),
            line(falador_objs.mithril_ore, 0, 2000),
            line(falador_objs.adamantite_ore, 0, 800),
            line(falador_objs.runite_ore, 0, 200),
            line(falador_objs.coal, 0, 5000),
        )

        whiteKnightShops()

        shop(
            falador_invs.temple_knight,
            line(falador_objs.initiate_sallet, 30, 1),
            line(falador_objs.initiate_cuisse, 30, 1),
            line(falador_objs.initiate_hauberk, 30, 1),
            line(falador_objs.initiate_harness, 30, 1),
            line(falador_objs.proselyte_sallet, 30, 1),
            line(falador_objs.proselyte_cuisse, 30, 1),
            line(falador_objs.proselyte_hauberk, 30, 1),
            line(falador_objs.proselyte_tasset, 30, 1),
            line(falador_objs.proselyte_harness, 30, 1),
            line(falador_objs.proselyte_harness_f, 30, 1),
        )
    }

    /**
     * Sir Vyvin's six rank shops. Each rank keeps the earlier ranks' items; from Adept on every
     * line stocks 20 instead of 10.
     */
    private fun whiteKnightShops() {
        val novice = listOf(falador_objs.white_chainbody, falador_objs.white_platelegs)
        val peon = novice + listOf(falador_objs.white_dagger, falador_objs.white_sword)
        val page =
            peon +
                listOf(
                    falador_objs.white_med_helm,
                    falador_objs.white_plateskirt,
                    falador_objs.white_sq_shield,
                )
        val noble =
            page +
                listOf(
                    falador_objs.white_mace,
                    falador_objs.white_magic_staff,
                    falador_objs.white_longsword,
                )
        val adept =
            noble +
                listOf(
                    falador_objs.white_full_helm,
                    falador_objs.white_platebody,
                    falador_objs.white_kiteshield,
                    falador_objs.white_boots,
                    falador_objs.white_gloves,
                )
        val master =
            adept +
                listOf(
                    falador_objs.white_2h_sword,
                    falador_objs.white_halberd,
                    falador_objs.white_scimitar,
                    falador_objs.white_warhammer,
                    falador_objs.white_battleaxe,
                    falador_objs.white_claws,
                )
        val ranks =
            listOf(
                falador_invs.white_knight_novice to novice,
                falador_invs.white_knight_peon to peon,
                falador_invs.white_knight_page to page,
                falador_invs.white_knight_noble to noble,
                falador_invs.white_knight_adept to adept,
                falador_invs.white_knight_master to master,
            )
        for ((index, rank) in ranks.withIndex()) {
            val (inv, objs) = rank
            val count = if (index >= ADEPT_INDEX) 20 else 10
            // Gear above the rank is listed empty, so a knight who drops rank can sell it back.
            shop(inv, *master.map { line(it, if (it in objs) count else 0, 100) }.toTypedArray())
        }
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

    private const val ADEPT_INDEX = 4
}
