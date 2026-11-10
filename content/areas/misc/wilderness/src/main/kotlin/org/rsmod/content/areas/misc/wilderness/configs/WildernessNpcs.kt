@file:Suppress("unused", "SpellCheckingInspection")

package org.rsmod.content.areas.misc.wilderness.configs

import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.huntmodes
import org.rsmod.api.config.refs.params
import org.rsmod.api.shops.config.ShopParams
import org.rsmod.api.type.editors.npc.NpcEditor
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.game.type.npc.NpcType

typealias wilderness_npcs = WildernessNpcs

object WildernessNpcs : NpcReferences() {
    val ferox_banker = find("wildy_hub_banker")
    val fat_tony = find("fat_tony")
    val noterazzo = find("noterazzo")
    val ent = find("wilderness_ent")
    val mandrith = find("wilderness_resource_guardian")
    val piles = find("wilderness_resource_certer")

    /** The Elder Chaos druid outside the Chaos Temple's hut, who un-notes bones. */
    val bone_druid = find("wild_chaos_uncerter")

    /** The Mage Arena bank's banker and rune seller (Gundai and Lundail). */
    val gundai = find("magearena_banker")
    val lundail = find("magearena_runeshop")

    /** Ferox Enclave's founder, who moves a player's respawn, and the bank's mercenary. */
    val ferox = find("wildy_hub_chief")
    val mercenary = find("wildy_banker_protector")

    // Richard (6) sells in Edgeville; the other nine roam the Wilderness.
    val william = find("wilderness_capeseller_1")
    val ian = find("wilderness_capeseller_2")
    val larry = find("wilderness_capeseller_3")
    val darren = find("wilderness_capeseller_4")
    val edward = find("wilderness_capeseller_5")
    val neil = find("wilderness_capeseller_7")
    val edmond = find("wilderness_capeseller_8")
    val simon = find("wilderness_capeseller_9")
    val sam = find("wilderness_capeseller_10")

    /** The wiki's aggressive monsters here (see the generic `AggressiveNpcs` for the others). */
    val aggressive =
        listOf(
            find("wilderness_ankou"),
            find("brawling_bandit"),
            find("wilderness_bandit"),
            find("black_demon2"),
            find("black_demon3"),
            find("black_demon5"),
            find("black_dragon4"),
            find("black_dragon5"),
            find("black_unicorn"),
            find("chaos_druid"),
            find("dwarf_chaos"),
            find("dark_warrior"),
            find("wilderness_dark_warrior"),
            find("earthwarrior"),
            find("wilderness_chaos_druid"),
            find("wilderness_ent"),
            find("firegiant"),
            find("firegiant2"),
            find("firegiant3"),
            find("ghost2"),
            find("ghost3"),
            find("ghost6"),
            find("ghost7"),
            find("greater_demon2"),
            find("greater_demon3"),
            find("greater_demon4"),
            find("greater_demon5"),
            find("green_dragon"),
            find("green_dragon2"),
            find("green_dragon3"),
            find("green_dragon4"),
            find("green_dragon5"),
            find("hellhound"),
            find("icegiant"),
            find("icegiant2"),
            find("icegiant3"),
            find("ice_spider"),
            find("icewarrior"),
            find("kingscorpion"),
            find("lava_dragon"),
            find("lesser_demon"),
            find("magicaxe"),
            find("wilderness_mammoth"),
            find("chaos_temple_monk_wilderness"),
            find("pirate_aggressive"),
            find("poisonspider"),
            find("shadow_spider"),
            find("giantskeleton"),
            find("thug"),
            find("whitewolf"),
            find("wolf"),
            find("zombie2_rural1"),
            find("zombie2_rural2"),
            find("zombie2_rural3"),
            find("zombie2_rural4"),
            find("zombie_armed_rural1"),
            find("zombie_armed_rural2"),
            find("zombie_armed_rural3"),
            find("zombie_armed_rural4"),
            find("zombie_armed_rural5"),
            find("wilderness_zombie"),
            find("wilderness_zombie2"),
            find("wilderness_zombie3"),
            find("guard_bandit"),
        )

    /** Dark wizards cast from a distance (`NpcAttackProfiles` has their spells). */
    val aggressive_ranged = listOf(find("bearded_dark_wizard"))
}

internal object WildernessNpcEditor : NpcEditor() {
    init {
        // Sell, buy and change percentages are the wiki's, in tenths of a percent (see ShopParams).
        for (seller in
            listOf(
                wilderness_npcs.william,
                wilderness_npcs.ian,
                wilderness_npcs.larry,
                wilderness_npcs.darren,
                wilderness_npcs.edward,
                wilderness_npcs.neil,
                wilderness_npcs.edmond,
                wilderness_npcs.simon,
                wilderness_npcs.sam,
            )) {
            shop(seller, sell = 1000, buy = 600, change = 10, indoor = false)
        }
        shop(wilderness_npcs.fat_tony, sell = 1000, buy = 600, change = 30)
        shop(wilderness_npcs.noterazzo, sell = 900, buy = 600, change = 30)
        shop(wilderness_npcs.lundail, sell = 1000, buy = 600, change = 10)

        for (banker in listOf(wilderness_npcs.ferox_banker, wilderness_npcs.gundai)) {
            edit(banker) {
                contentGroup = content.banker
                wanderRange = 0
            }
        }
        edit(wilderness_npcs.fat_tony) { wanderRange = 3 }
        edit(wilderness_npcs.noterazzo) { wanderRange = 3 }

        for (npc in wilderness_npcs.aggressive) {
            edit(npc) { huntMode = huntmodes.cowardly }
        }
        // Ents can be neither poisoned nor envenomed (wiki).
        edit(wilderness_npcs.ent) { param[params.poison_immunity] = 1 }
        for (npc in wilderness_npcs.aggressive_ranged) {
            edit(npc) { huntMode = huntmodes.ranged }
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
