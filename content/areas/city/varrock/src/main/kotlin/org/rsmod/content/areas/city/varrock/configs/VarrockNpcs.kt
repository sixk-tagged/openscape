@file:Suppress("unused", "SpellCheckingInspection")

package org.rsmod.content.areas.city.varrock.configs

import org.rsmod.api.config.refs.content
import org.rsmod.api.shops.config.ShopParams
import org.rsmod.api.type.editors.npc.NpcEditor
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.game.type.npc.NpcType

typealias varrock_npcs = VarrockNpcs

object VarrockNpcs : NpcReferences() {
    val banker1 = find("banker1")
    val banker1_new = find("banker1_new")
    val banker2_new = find("banker2_new")
    val banker1_west = find("banker1_west")
    val banker1_east = find("banker1_east")
    val banker2_east = find("banker2_east")
    val bank_doorguard = find("varrock_bank_doorguard")
    val head_chef = find("head_chef")
    val smithing_tutor = find("aide_tutor_smithing_master")
    val guildmaster = find("guildmaster")
    val stray_dog = find("dog_stray")
    val stray_dog2 = find("dog_stray2")

    val zaff = find("zaff")
    val horvik = find("horvik_the_armourer")
    val lowe = find("lowe")
    val thessalia = find("thessalia_normal")
    val sword_keeper = find("swordshop1")
    val sword_assistant = find("swordshop2")
    val general_keeper = find("generalshopkeeper2")
    val general_assistant = find("generalassistant2")
    val asyff = find("tailorp")
    val tea_seller = find("tea_seller")
    val scavvo = find("scavvo")
    val valaine = find("valaine")
    val romily = find("merchant_pies")
    val sawmill_operator = find("poh_sawmill_opp")

    val apothecary = find("apothecary")
    val baraek = find("baraek")
    val benny = find("qip_soa_newspaperseller")
    val bluemoon_bartender = find("bluemoon_bartender")
    val donkey_bartender = find("donkey_bartender")
    val jollyboar_bartender = find("jollyboar_bartender")
    val bluemoon_cook = find("fai_varrock_bluemoon_chef")
    val jollyboar_cook = find("fai_varrock_jollyboar_chef")

    // Grand Exchange staff stand at their counters
    val ge_clerk_1 = find("ge_clerk_1")
    val ge_clerk_2 = find("ge_clerk_2")
    val ge_clerk_3 = find("ge_clerk_3")
    val ge_clerk_4 = find("ge_clerk_4")
    val ge_boss = find("ge_boss")
    val ge_expert_ores = find("ge_expert_ores")
    val ge_expert_logs = find("ge_expert_logs")
    val ge_expert_combat = find("ge_expert_combat")
    val ge_expert_herbs = find("ge_expert_herbs")
    val ge_expert_runes = find("ge_expert_runes")
    val clan_clerk = find("clan_clerk")
    val james = find("bond_james_visible")

    // The palace roof trainees stay at their dummies
    val trainee_1 = find("fai_varrock_guard_training02")
    val trainee_2 = find("fai_varrock_guard_training02_variant01")
    val trainee_3 = find("fai_varrock_guard_training02_f")
    val trainee_4 = find("fai_varrock_guard_training02_f_variant01")
}

internal object VarrockNpcEditor : NpcEditor() {
    init {
        bankers(
            varrock_npcs.banker1,
            varrock_npcs.banker1_new,
            varrock_npcs.banker2_new,
            varrock_npcs.banker1_west,
            varrock_npcs.banker1_east,
            varrock_npcs.banker2_east,
        )

        // Sell, buy and change percentages are the wiki's, in tenths of a percent (see ShopParams).
        shop(varrock_npcs.zaff, sell = 1000, buy = 550, change = 20)
        shop(varrock_npcs.horvik, sell = 1000, buy = 600, change = 20)
        shop(varrock_npcs.lowe, sell = 1000, buy = 550, change = 10)
        shop(varrock_npcs.thessalia, sell = 1000, buy = 550, change = 10)
        shop(varrock_npcs.sword_keeper, sell = 1000, buy = 600, change = 20)
        shop(varrock_npcs.sword_assistant, sell = 1000, buy = 600, change = 20)
        shop(varrock_npcs.general_keeper, sell = 1300, buy = 400, change = 30)
        shop(varrock_npcs.general_assistant, sell = 1300, buy = 400, change = 30)
        shop(varrock_npcs.asyff, sell = 1300, buy = 400, change = 20)
        shop(varrock_npcs.tea_seller, sell = 1000, buy = 600, change = 20)
        shop(varrock_npcs.scavvo, sell = 1000, buy = 600, change = 33)
        shop(varrock_npcs.valaine, sell = 1300, buy = 400, change = 30)
        shop(varrock_npcs.romily, sell = 1000, buy = 550, change = 10)
        // The Lumber Yard is outdoors, so the operator is not kept inside.
        edit(varrock_npcs.sawmill_operator) {
            param[ShopParams.shop_sell_percentage] = 1300
            param[ShopParams.shop_buy_percentage] = 500
            param[ShopParams.shop_change_percentage] = 0
        }

        for (npc in
            listOf(
                varrock_npcs.ge_clerk_1,
                varrock_npcs.ge_clerk_2,
                varrock_npcs.ge_clerk_3,
                varrock_npcs.ge_clerk_4,
                varrock_npcs.ge_boss,
                varrock_npcs.ge_expert_ores,
                varrock_npcs.ge_expert_logs,
                varrock_npcs.ge_expert_combat,
                varrock_npcs.ge_expert_herbs,
                varrock_npcs.ge_expert_runes,
                varrock_npcs.clan_clerk,
                varrock_npcs.james,
                varrock_npcs.bank_doorguard,
                varrock_npcs.trainee_1,
                varrock_npcs.trainee_2,
                varrock_npcs.trainee_3,
                varrock_npcs.trainee_4,
            )) {
            edit(npc) { wanderRange = 0 }
        }

        edit(varrock_npcs.apothecary) { moveRestrict = indoors }
        edit(varrock_npcs.head_chef) { moveRestrict = indoors }
        edit(varrock_npcs.bluemoon_cook) { moveRestrict = indoors }
        edit(varrock_npcs.jollyboar_cook) { moveRestrict = indoors }
        edit(varrock_npcs.baraek) { wanderRange = 1 }
        edit(varrock_npcs.bluemoon_bartender) { moveRestrict = indoors }
        edit(varrock_npcs.donkey_bartender) { moveRestrict = indoors }
        edit(varrock_npcs.jollyboar_bartender) { moveRestrict = indoors }
    }

    private fun bankers(vararg types: NpcType) {
        for (type in types) {
            edit(type) {
                contentGroup = content.banker
                wanderRange = 0
            }
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
