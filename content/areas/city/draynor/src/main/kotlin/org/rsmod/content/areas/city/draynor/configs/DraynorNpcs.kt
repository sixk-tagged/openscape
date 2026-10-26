@file:Suppress("unused", "SpellCheckingInspection")

package org.rsmod.content.areas.city.draynor.configs

import org.rsmod.api.config.refs.content
import org.rsmod.api.shops.config.ShopParams
import org.rsmod.api.type.editors.npc.NpcEditor
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.varp.VarpReferences
import org.rsmod.game.type.npc.NpcType

typealias draynor_npcs = DraynorNpcs

typealias draynor_varps = DraynorVarps

object DraynorNpcs : NpcReferences() {
    val banker1 = find("banker1")
    val banker2 = find("banker2")
    val bank_guard = find("wom_bankguard")
    val wise_old_man = find("wise_old_man")
    val miss_schism = find("wom_gossip")
    val diango = find("aprilfoolshorsesalesman")
    val olivia = find("seed_merchant")
    val fortunato = find("rag_wine_merchant")
    val ned = find("ned")
    val aggie = find("aggie_2ops")
    val spria = find("slayer_master_9_active")
    val ava = find("anma_assistant")
    val martin = find("martin_the_master_farmer")
    val twiggy = find("diary_queen")
    val town_crier = find("pmod_town_crier_draynor")
    val forester = find("forestry_forester_nostorage")
    val jalarast = find("armourmaking_wizard")
    val rick = find("telecrystal_wizard_shop")
    val traiborn = find("traiborn")
    val mizgog = find("wizard_mizgog")
    val grayzag = find("wizard_grayzag")
    val onglewip = find("aluft_gnome_mage")
    val veronica = find("veronica")
    val oddenstein = find("professor_oddenstein")
    val morgan = find("morgan")
    val witch = find("anma_witch_multi")
    val market_guard = find("farming_market_guard")
}

object DraynorVarps : VarpReferences() {
    /** Set by the seed stalls (content/skills/thieving) when they are robbed. */
    val olivia_angry_until = find("olivia_angry_until")
}

internal object DraynorNpcEditor : NpcEditor() {
    init {
        edit(draynor_npcs.banker1) { contentGroup = content.banker }
        edit(draynor_npcs.banker2) { contentGroup = content.banker }

        // Shop price multipliers from each shop's StoreTableHead: sell %, buy % and the stock
        // delta %, all multiplied by 10 like the other shops' params.
        shop(draynor_npcs.diango, sell = 1500, buy = 500, change = 20)
        shop(draynor_npcs.olivia, sell = 1200, buy = 600, change = 30)
        shop(draynor_npcs.fortunato, sell = 1000, buy = 600, change = 20)
        // 90%: The Fremennik Isles counts as done.
        shop(draynor_npcs.ned, sell = 900, buy = 200, change = 20, indoors = true)
        shop(draynor_npcs.ava, sell = 1300, buy = 400, change = 30, indoors = true)
        shop(draynor_npcs.spria, sell = 1000, buy = 600, change = 10, indoors = true)

        edit(draynor_npcs.aggie) { moveRestrict = indoors }
    }

    private fun shop(npc: NpcType, sell: Int, buy: Int, change: Int, indoors: Boolean = false) {
        edit(npc) {
            if (indoors) {
                moveRestrict = this.indoors
            }
            param[ShopParams.shop_sell_percentage] = sell
            param[ShopParams.shop_buy_percentage] = buy
            param[ShopParams.shop_change_percentage] = change
        }
    }
}
