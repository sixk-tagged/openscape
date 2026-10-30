@file:Suppress("unused", "SpellCheckingInspection")

package org.rsmod.content.areas.city.edgeville.configs

import org.rsmod.api.config.refs.huntmodes
import org.rsmod.api.shops.config.ShopParams
import org.rsmod.api.type.editors.npc.NpcEditor
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.game.type.npc.NpcType

typealias edgeville_npcs = EdgevilleNpcs

object EdgevilleNpcs : NpcReferences() {
    val abbot = find("abbot_langley")
    val monk = find("monk")
    val jered = find("brother_jered")
    val althric = find("brother_althric")
    val oziach = find("oziach")
    val peksa = find("peksa")
    val general_keeper = find("generalshopkeeper5")
    val general_assistant = find("generalassistant5")
    val richard = find("wilderness_capeseller_6")
    val oracle = find("oracle")
    val hunding = find("fai_barbarian_9")
    val tassie = find("favour_tassie_slipcast")
    val hari = find("canoeing_hari")
    val sigurd = find("canoeing_sigurd")
    val bentnoze = find("general_bentnoze_red")
    val wartface = find("general_wartface_green")
    val gunthor = find("gunthor_the_brave")

    val barbarians =
        listOf(
            find("fai_barbarian_1"),
            find("fai_barbarian_2"),
            find("fai_barbarian_3"),
            find("fai_barbarian_4"),
            find("fai_barbarian_5"),
            find("fai_barbarian_6"),
            find("fai_barbarian_7"),
            find("fai_barbarian_8"),
            find("fai_barbarian_10"),
            find("fai_barbarian_11"),
            find("fai_barbarian_12"),
            find("fai_barbarian_13"),
            find("fai_barbarian_14"),
            find("fai_barbarian_female_1"),
            find("fai_barbarian_female_2"),
            find("fai_barbarian_female_3"),
            find("fai_barbarian_female_4"),
        )

    val fortress_guards =
        listOf(
            find("fortressguard_01"),
            find("fortressguard_02"),
            find("fortressguard_03"),
            find("fortressguard_04"),
        )

    val red_goblins =
        listOf(
            find("goblin_red_soldier_2"),
            find("goblin_red_soldier_3"),
            find("goblin_red_soldier_4"),
            find("goblin_red_soldier_5"),
            find("goblin_red_soldier_6"),
            find("goblin_red_soldier_7"),
            find("goblin_red_soldier_8"),
        )

    val green_goblins =
        listOf(
            find("goblin_green_soldier_2"),
            find("goblin_green_soldier_3"),
            find("goblin_green_soldier_4"),
            find("goblin_green_soldier_5"),
            find("goblin_green_soldier_7"),
            find("goblin_green_soldier_8"),
        )

    /** The wiki's aggressive monsters (see the generic `AggressiveNpcs` for the other areas). */
    val aggressive =
        listOf(
            find("giant"),
            find("giant2"),
            find("giant3"),
            find("giant4"),
            find("giant5"),
            find("giant6"),
            find("hobgoblin_unarmed"),
            find("hobgoblin_armed"),
            find("aggressive_black_knight"),
            find("kr_aggressive_black_knight"),
            gunthor,
        )
}

internal object EdgevilleNpcEditor : NpcEditor() {
    init {
        // Sell, buy and change percentages are the wiki's, in tenths of a percent (see ShopParams).
        shop(edgeville_npcs.oziach, sell = 1300, buy = 400, change = 30)
        shop(edgeville_npcs.peksa, sell = 1000, buy = 600, change = 10)
        shop(edgeville_npcs.general_keeper, sell = 1300, buy = 400, change = 30)
        shop(edgeville_npcs.general_assistant, sell = 1300, buy = 400, change = 30)
        edit(edgeville_npcs.richard) {
            param[ShopParams.shop_sell_percentage] = 1000
            param[ShopParams.shop_buy_percentage] = 600
            param[ShopParams.shop_change_percentage] = 10
        }

        for (npc in edgeville_npcs.aggressive) {
            edit(npc) { huntMode = huntmodes.cowardly }
        }
    }

    private fun shop(npc: NpcType, sell: Int, buy: Int, change: Int) {
        edit(npc) {
            moveRestrict = indoors
            param[ShopParams.shop_sell_percentage] = sell
            param[ShopParams.shop_buy_percentage] = buy
            param[ShopParams.shop_change_percentage] = change
        }
    }
}
