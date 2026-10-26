@file:Suppress("unused", "SpellCheckingInspection")

package org.rsmod.content.areas.city.alkharid.configs

import org.rsmod.api.config.refs.content
import org.rsmod.api.shops.config.ShopParams
import org.rsmod.api.type.editors.npc.NpcEditor
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.game.type.npc.NpcType

typealias alkharid_npcs = AlKharidNpcs

object AlKharidNpcs : NpcReferences() {
    val banker1 = find("kharidbanker1")
    val banker2 = find("kharidbanker2")
    val shop_keeper = find("generalshopkeeper3")
    val shop_assistant = find("generalassistant3")
    val dommik = find("dommik")
    val louie = find("louie_legs")
    val ranael = find("ranael")
    val zeke = find("zeke")
    val gem_trader = find("gem_trader")
    val ali_morrisane = find("feud_ali_m")
    val karim = find("kebab_seller")
    val silk_trader = find("silk_trader")
    val border_guard1 = find("borderguard1")
    val border_guard2 = find("borderguard2")
    val shantay = find("shantay")
    val shantay_guard_still = find("shantay_guard_still")
    val shantay_guard = find("shantay_guard")
    val al_kharid_warrior = find("al_kharid_warrior")
    val selim = find("bcs_selim_vis")
    val prince_ali = find("prince_ali_vis")
    val hassan = find("hassan")
    val al_kharid_man = find("al_kharid_man")

    val camels =
        listOf(
            find("al_the_camel"),
            find("cam_the_camel"),
            find("elly_the_camel"),
            find("ollie_the_camel"),
            find("alice_the_camel"),
            find("camel"),
            find("ugthanki"),
        )
}

internal object AlKharidNpcEditor : NpcEditor() {
    init {
        edit(alkharid_npcs.banker1) { contentGroup = content.banker }
        edit(alkharid_npcs.banker2) { contentGroup = content.banker }
        edit(alkharid_npcs.al_kharid_man) { contentGroup = content.person }

        // The general store keeps the default 130% / 40% / 3% params.
        edit(alkharid_npcs.shop_keeper) { moveRestrict = indoors }
        edit(alkharid_npcs.shop_assistant) { moveRestrict = indoors }

        // Shop price multipliers from each shop's StoreTableHead: sell %, buy % and the stock
        // delta %, all multiplied by 10 like the other shops' params.
        shop(alkharid_npcs.dommik, sell = 1000, buy = 650, change = 20)
        shop(alkharid_npcs.louie, sell = 1000, buy = 650, change = 10)
        shop(alkharid_npcs.ranael, sell = 1000, buy = 650, change = 10)
        shop(alkharid_npcs.zeke, sell = 1000, buy = 550, change = 20)
        shop(alkharid_npcs.gem_trader, sell = 1000, buy = 700, change = 30)
        shop(alkharid_npcs.ali_morrisane, sell = 1000, buy = 550, change = 10)
        shop(alkharid_npcs.shantay, sell = 1000, buy = 700, change = 10)
    }

    private fun shop(npc: NpcType, sell: Int, buy: Int, change: Int) {
        edit(npc) {
            param[ShopParams.shop_sell_percentage] = sell
            param[ShopParams.shop_buy_percentage] = buy
            param[ShopParams.shop_change_percentage] = change
        }
    }
}
