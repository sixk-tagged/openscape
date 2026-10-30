@file:Suppress("unused", "SpellCheckingInspection")

package org.rsmod.content.areas.city.falador.configs

import org.rsmod.api.config.refs.content
import org.rsmod.api.shops.config.ShopParams
import org.rsmod.api.type.builders.varp.VarpBuilder
import org.rsmod.api.type.editors.npc.NpcEditor
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.varp.VarpReferences
import org.rsmod.game.type.npc.NpcType

typealias falador_npcs = FaladorNpcs

typealias falador_varps = FaladorVarps

object FaladorNpcs : NpcReferences() {
    val east_banker = find("falador_banker")

    val shop_keeper = find("generalshopkeeper4")
    val shop_assistant = find("generalassistant4")
    val flynn = find("flynn")
    val cassie = find("cassie")
    val herquin = find("herquin")
    val wayne = find("wayne")
    val sarah = find("farming_shopkeeper_1")
    val drogo = find("drogo")
    val nurmof = find("nurmof")
    val hura = find("xbows_sales_dwarf_mine")
    val dwarven_shopkeeper = find("dwarven_shopkeeper")
    val hendor = find("mguild_oreseller")
    val yarsul = find("mguild_pickaxeseller")
    val belona = find("mguild_rewardseller")
    val nulodion = find("nulodion")

    val sir_vyvin = find("sir_vyvin")
    val sir_tiffy = find("rd_teleporter_guy")
    val emily = find("risingsun_barmaid")
    val kaylee = find("risingsun_barmaid2")
    val tina = find("risingsun_barmaid3")
    val wyson = find("wyson")
    val gadrin = find("mguild_capeseller")
    val guild_dwarf = find("mguild_dwarf1")
    val guild_dwarf2 = find("mguild_dwarf2")
    val guild_guard = find("mguild_guard1")
    val guild_guard2 = find("mguild_guard2")
    val doric = find("doric")
    val malignius = find("elemental_wizard_boss")

    val white_knight = find("white_knight")
    val white_knight_yellow = find("white_knight_yellow_plumes")
    val white_knight_green = find("white_knight_green_plumes")
    val white_knight_blue = find("white_knight_blue_plumes")
}

object FaladorVarps : VarpReferences() {
    /** The Wanted! kill score: Black Knight kills raise it, White Knight kills lower it. */
    val white_knight_score = find("white_knight_score")
    /** Built by content/skills/mining, which skips the minerals while it is set. */
    val minerals_off = find("mguild_minerals_off")
}

internal object FaladorVarpBuilder : VarpBuilder() {
    init {
        build("white_knight_score")
    }
}

internal object FaladorNpcEditor : NpcEditor() {
    init {
        // The west bank's `banker2` is already a banker (Draynor's config).
        edit(falador_npcs.east_banker) { contentGroup = content.banker }

        // Shop price multipliers from each shop's StoreTableHead: sell %, buy % and the stock
        // delta %, all multiplied by 10 like the other shops' params.
        shop(falador_npcs.shop_keeper, sell = 1300, buy = 400, change = 30)
        shop(falador_npcs.shop_assistant, sell = 1300, buy = 400, change = 30)
        shop(falador_npcs.flynn, sell = 1000, buy = 600, change = 10)
        shop(falador_npcs.cassie, sell = 1000, buy = 600, change = 20)
        shop(falador_npcs.herquin, sell = 1000, buy = 700, change = 30)
        shop(falador_npcs.wayne, sell = 1000, buy = 650, change = 10)
        shop(falador_npcs.sarah, sell = 1000, buy = 700, change = 10)
        shop(falador_npcs.drogo, sell = 1000, buy = 700, change = 20)
        shop(falador_npcs.nurmof, sell = 1000, buy = 600, change = 20)
        shop(falador_npcs.yarsul, sell = 1000, buy = 600, change = 20)
        shop(falador_npcs.hura, sell = 3600, buy = 500, change = 20)
        shop(falador_npcs.dwarven_shopkeeper, sell = 1300, buy = 300, change = 30)
        shop(falador_npcs.hendor, sell = 1000, buy = 700, change = 20)
        shop(falador_npcs.belona, sell = 1000, buy = 800, change = 0)
        shop(falador_npcs.sir_vyvin, sell = 1000, buy = 1000, change = 0)
        shop(falador_npcs.sir_tiffy, sell = 1000, buy = 500, change = 0)
    }

    private fun shop(npc: NpcType, sell: Int, buy: Int, change: Int) {
        edit(npc) {
            param[ShopParams.shop_sell_percentage] = sell
            param[ShopParams.shop_buy_percentage] = buy
            param[ShopParams.shop_change_percentage] = change
        }
    }
}
