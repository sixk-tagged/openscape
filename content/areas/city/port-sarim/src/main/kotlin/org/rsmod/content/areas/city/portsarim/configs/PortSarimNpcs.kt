@file:Suppress("unused", "SpellCheckingInspection")

package org.rsmod.content.areas.city.portsarim.configs

import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.huntmodes
import org.rsmod.api.shops.config.ShopParams
import org.rsmod.api.type.editors.npc.NpcEditor
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.game.type.npc.NpcType

typealias port_sarim_npcs = PortSarimNpcs

object PortSarimNpcs : NpcReferences() {
    // Shops.
    val wydin = find("wydin")
    val betty = find("betty")
    val battleaxe_brian = find("brian")
    val gerrant = find("gerrant")
    val grum = find("grum")
    val rommik = find("rommik")
    val archery_brian = find("salesman_ranging")
    val rimmington_keeper = find("generalshopkeeper6")
    val rimmington_assistant = find("generalassistant6")
    val karamja_keeper = find("generalshopkeeper7")
    val karamja_assistant = find("generalassistant7")
    val zembo = find("zembo")
    val stan = find("sailing_transport_trader_stan")
    val stan_crew =
        listOf(
            find("sailing_transport_trader_stan_crew_man3"),
            find("sailing_transport_trader_stan_crew_woman2"),
            find("sailing_transport_trader_stan_crew_woman3"),
        )
    val bartender = find("rustyanchor_bartender")

    // Boats.
    val tobias = find("captain_tobias")
    val lorris = find("seaman_lorris")
    val thresnor = find("seaman_thresnor")
    val customs_officer = find("customs_officer")
    val barnaby = find("captain_barnaby_rimmington")
    val colin = find("corsair_ferry_rimmington")
    val veos = find("veos_sarim")
    val morris = find("seaman_morris")
    val entrana_monks = listOf(find("shipmonk"), find("shipmonk1_b"), find("shipmonk1_c"))

    // Services and role npcs.
    val phials = find("uncerter_rimmington")
    val luthas = find("luthas")
    val thurgo = find("thurgo")
    val master_crafters =
        listOf(find("master_crafter"), find("master_crafter_2"), find("master_crafter_3"))
    val tanner = find("tanner")
    val makeover_mage = find("makeover_mage_female")
    val brock = find("zep_multi_assistant_craft")
    val security_guard = find("sos_guard")

    // Talk-only flavour.
    val cap_n_hand = find("macro_pirate")
    val ahab = find("sarim_pub_sitting_patron")
    val jack_seagull = find("sarim_pub_drinker_1")
    val longbow_ben = find("sarim_pub_drinker_2")
    val chemist = find("chemist")
    val hetty = find("hetty")
    val chancy = find("gambler1")
    val da_vinci = find("artist1")
    val hops = find("drunk1")
    val sleeping_guard = find("jail_guard_sleeping")
    val lookout_guard = find("jail_guard_lookout")
    val wormbrain = find("wormbrain")

    // Humans that chat like the generic man and woman do.
    val people =
        listOf(
            find("man4_for_musa_point"),
            find("karamja_man"),
            find("rimmington_anja"),
            find("rimmington_hengel"),
        )

    /** The wiki's aggressive monsters here (see the generic `AggressiveNpcs` for the rest). */
    val aggressive =
        listOf(
            find("rimmington_hobgoblin_unarmed_1"),
            find("rimmington_hobgoblin_unarmed_2"),
            find("rimmington_hobgoblin_unarmed_3"),
            find("rimmington_hobgoblin_armed_1"),
            find("snake"),
            find("bat"),
            find("skeleton_unarmed2"),
            // The level 82 lesser demons; `lesser_demon` itself stays passive, as the Wizards'
            // Tower cage shares its type.
            find("lesser_demon2"),
            find("lesser_demon3"),
            find("lesser_demon4"),
            find("lesser_demon5"),
        )
}

internal object PortSarimNpcEditor : NpcEditor() {
    init {
        // Sell, buy and change percentages are the wiki's, in tenths of a percent (see ShopParams).
        shop(port_sarim_npcs.wydin, sell = 1000, buy = 700, change = 10)
        shop(port_sarim_npcs.betty, sell = 1000, buy = 600, change = 1)
        shop(port_sarim_npcs.battleaxe_brian, sell = 1000, buy = 550, change = 10)
        shop(port_sarim_npcs.gerrant, sell = 1000, buy = 700, change = 10)
        shop(port_sarim_npcs.grum, sell = 1000, buy = 700, change = 20)
        shop(port_sarim_npcs.rommik, sell = 1000, buy = 650, change = 20)
        shop(port_sarim_npcs.archery_brian, sell = 1000, buy = 650, change = 20)
        shop(port_sarim_npcs.zembo, sell = 1000, buy = 700, change = 20)
        for (npc in
            listOf(
                port_sarim_npcs.rimmington_keeper,
                port_sarim_npcs.rimmington_assistant,
                port_sarim_npcs.karamja_keeper,
                port_sarim_npcs.karamja_assistant,
            )) {
            shop(npc, sell = 1300, buy = 400, change = 30)
        }
        // Trader Stan and his crew work outdoors on the docks.
        for (npc in listOf(port_sarim_npcs.stan) + port_sarim_npcs.stan_crew) {
            shop(npc, sell = 2500, buy = 150, change = 20, indoor = false)
        }

        for (npc in port_sarim_npcs.people) {
            edit(npc) { contentGroup = content.person }
        }
        for (npc in port_sarim_npcs.aggressive) {
            edit(npc) { huntMode = huntmodes.cowardly }
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
