@file:Suppress("SpellCheckingInspection")

package org.rsmod.content.areas.misc.kandarinnorth.configs

import org.rsmod.api.shops.config.ShopParams
import org.rsmod.api.type.editors.npc.NpcEditor
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.game.type.npc.NpcType

typealias kandarin_north_npcs = KandarinNorthNpcs

object KandarinNorthNpcs : NpcReferences() {
    val harry = find("harry")
    val arhein = find("arhein")
    val hickton = find("hickton")
    val candle_maker = find("candle_maker")
    val vanessa = find("farming_shopkeeper_2")
    val roachey = find("fishguildshop")
    val aaron = find("ranging_guild_armour_salesman")
    val dargaud = find("ranging_guild_bow_salesman")
    val tribal_salesman = find("ranging_guild_tribal_salesman")
    val fionella = find("fionella")
    val siegfried = find("siegfried_erkle")
}

internal object KandarinNorthNpcEditor : NpcEditor() {
    init {
        // Shop price multipliers from each shop's StoreTableHead (sell %, buy %, stock delta %),
        // all multiplied by 10 like the other shops' params.
        shop(kandarin_north_npcs.harry, sell = 1000, buy = 700, change = 10)
        shop(kandarin_north_npcs.arhein, sell = 1300, buy = 400, change = 30)
        shop(kandarin_north_npcs.hickton, sell = 1000, buy = 500, change = 10)
        shop(kandarin_north_npcs.candle_maker, sell = 1300, buy = 400, change = 30)
        shop(kandarin_north_npcs.vanessa, sell = 1000, buy = 700, change = 10)
        shop(kandarin_north_npcs.roachey, sell = 1000, buy = 700, change = 10)
        shop(kandarin_north_npcs.aaron, sell = 1000, buy = 500, change = 10)
        shop(kandarin_north_npcs.dargaud, sell = 1000, buy = 500, change = 10)
        shop(kandarin_north_npcs.tribal_salesman, sell = 1300, buy = 500, change = 10)
        shop(kandarin_north_npcs.fionella, sell = 1550, buy = 550, change = 10)
        shop(kandarin_north_npcs.siegfried, sell = 1500, buy = 500, change = 20)
    }

    private fun shop(npc: NpcType, sell: Int, buy: Int, change: Int) {
        edit(npc) {
            param[ShopParams.shop_sell_percentage] = sell
            param[ShopParams.shop_buy_percentage] = buy
            param[ShopParams.shop_change_percentage] = change
        }
    }
}
