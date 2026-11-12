@file:Suppress("unused", "SpellCheckingInspection")

package org.rsmod.content.areas.city.ardougne.configs

import org.rsmod.api.config.refs.content
import org.rsmod.api.shops.config.ShopParams
import org.rsmod.api.type.editors.npc.NpcEditor
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.varp.VarpReferences
import org.rsmod.game.type.npc.NpcType

typealias ardougne_npcs = ArdougneNpcs

object ArdougneNpcs : NpcReferences() {
    // Shops.
    val aemad = find("aemad")
    val kortan = find("kortan")
    val zenesha = find("zenesha")
    val baker_west = find("baker_merchant_ardougne")
    val baker_east = find("baker_merchant_ardougne2")
    val silver_merchant = find("silver_merchant_ardougne")
    val gem_merchant = find("gem_merchant_ardougne")
    val spice_seller = find("spice_merchant_ardougne")
    val fur_trader = find("fur_merchant_ardougne")
    val richard = find("farming_shopkeeper_3")
    val chadwell = find("chadwell")
    val khazard_shopkeeper = find("khazard_shopkeeper")
    val lovecraft = find("slug2_lovecraft")
    val bolkoy = find("treevillage_shopkeeper1")
    val flying_horse_bartender = find("flyinghorse_bartender")
    val khazard_barman = find("khazard_barman")

    /** The town's fighters, who answer a robbed stall owner's call. */
    val guards =
        listOf(
            find("ardougne_guard"),
            find("knight_of_ardougne"),
            find("paladin"),
            find("paladin2"),
            find("hero"),
        )

    /** Humans that chat like the generic man and woman do. */
    val people =
        listOf(
            find("w_ardougnecitizen3"),
            find("w_ardougnecitizen4"),
            find("w_ardoungecitizenlady1"),
            find("w_ardoungecitizenlady2"),
            find("w_ardoungecitizenlady4"),
            find("dskin_w_ardoungecitizen1"),
            find("dskin_w_ardoungecitizen2"),
            find("dskin_w_ardoungecitizenlady1"),
            find("dskin_w_ardoungecitizenlady3"),
            find("dskin_w_ardoungecitizenlady4"),
        )
}

object ArdougneVarps : VarpReferences() {
    /** Set by the market stalls (content/skills/thieving) when they are robbed. */
    val baker_angry_until = find("ardougne_baker_angry_until")
    val spice_angry_until = find("ardougne_spice_angry_until")
}

internal object ArdougneNpcEditor : NpcEditor() {
    init {
        // Sell, buy and change percentages are the wiki's, in tenths of a percent (see ShopParams).
        shop(ardougne_npcs.aemad, sell = 1300, buy = 400, change = 20)
        shop(ardougne_npcs.kortan, sell = 1300, buy = 400, change = 20)
        shop(ardougne_npcs.zenesha, sell = 1000, buy = 600, change = 20)
        shop(ardougne_npcs.richard, sell = 1000, buy = 700, change = 10)
        shop(ardougne_npcs.chadwell, sell = 1200, buy = 550, change = 20)
        shop(ardougne_npcs.khazard_shopkeeper, sell = 1400, buy = 400, change = 10)
        shop(ardougne_npcs.lovecraft, sell = 1000, buy = 500, change = 20)
        shop(ardougne_npcs.bolkoy, sell = 1300, buy = 400, change = 30)
        // The market stalls stand outdoors.
        shop(ardougne_npcs.baker_west, sell = 1000, buy = 800, change = 20, indoor = false)
        shop(ardougne_npcs.baker_east, sell = 1000, buy = 800, change = 20, indoor = false)
        shop(ardougne_npcs.silver_merchant, sell = 1000, buy = 700, change = 20, indoor = false)
        shop(ardougne_npcs.gem_merchant, sell = 1500, buy = 800, change = 30, indoor = false)
        shop(ardougne_npcs.spice_seller, sell = 1000, buy = 600, change = 20, indoor = false)
        shop(ardougne_npcs.fur_trader, sell = 1200, buy = 950, change = 20, indoor = false)

        for (npc in ardougne_npcs.people) {
            edit(npc) { contentGroup = content.person }
        }
    }

    private fun shop(npc: NpcType, sell: Int, buy: Int, change: Int, indoor: Boolean = true) {
        edit(npc) {
            if (indoor) {
                moveRestrict = indoors
            }
            param[ShopParams.shop_sell_percentage] = sell
            param[ShopParams.shop_buy_percentage] = buy
            param[ShopParams.shop_change_percentage] = change
        }
    }
}
