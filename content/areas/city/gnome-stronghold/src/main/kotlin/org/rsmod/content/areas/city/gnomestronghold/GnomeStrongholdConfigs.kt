@file:Suppress("unused", "SpellCheckingInspection")

package org.rsmod.content.areas.city.gnomestronghold

import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.params
import org.rsmod.api.shops.config.ShopParams
import org.rsmod.api.type.builders.map.npc.MapNpcSpawnBuilder
import org.rsmod.api.type.editors.loc.LocEditor
import org.rsmod.api.type.editors.npc.NpcEditor
import org.rsmod.api.type.refs.inv.InvReferences
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.game.type.content.ContentGroupType
import org.rsmod.game.type.loc.LocType
import org.rsmod.game.type.npc.NpcType

typealias gnome_npcs = GnomeNpcs

typealias gnome_invs = GnomeInvs

typealias gnome_locs = GnomeLocs

object GnomeNpcs : NpcReferences() {
    val banker = find("gnomebanker")
    val heckel = find("gnome_heckelfunch")
    val hudo = find("gnome_hudo")
    val gulluck = find("gnome_gulluck")
    val rometti = find("gnome_rometti")
}

object GnomeInvs : InvReferences() {
    val heckel = find("gnomeshop_heck")
    val hudo = find("gnomeshop_hudo")
    val gulluck = find("gnomeshop_gulluck")
    val rometti = find("gnomeshop_rometti")
}

object GnomeLocs : LocReferences() {
    val bank_booth = find("bankbooth")
    val bank_booth_multi = find("bankbooth_multi")
    val spiral = find("spiralstairs_wooden")
    val spiral_top = find("spiralstairstop_wooden")

    val tree_door_left = find("treedoorl")
    val tree_door_left_open = find("treedoorl_open")
    val tree_door_right = find("treedoorr")
    val tree_door_right_open = find("treedoorr_open")

    val area_gate = find("gnome_areagate")

    val ladder_ground = find("grandtree_ladderbottom")
    val ladder_first = find("grandtree_laddermiddle_bottom")
    val ladder_second = find("grandtree_laddermiddle_top")
    val ladder_roof = find("grandtree_laddertop")
}

internal object GnomeStrongholdNpcEditor : NpcEditor() {
    init {
        edit(gnome_npcs.banker) { contentGroup = content.banker }

        // Shop price multipliers from each shop's StoreTableHead, times 10.
        shop(gnome_npcs.heckel, sell = 1000, buy = 550, change = 10)
        shop(gnome_npcs.hudo, sell = 1000, buy = 550, change = 10)
        shop(gnome_npcs.gulluck, sell = 1000, buy = 250, change = 10)
        shop(gnome_npcs.rometti, sell = 1000, buy = 550, change = 10)
    }

    private fun shop(npc: NpcType, sell: Int, buy: Int, change: Int) {
        edit(npc) {
            param[ShopParams.shop_sell_percentage] = sell
            param[ShopParams.shop_buy_percentage] = buy
            param[ShopParams.shop_change_percentage] = change
        }
    }
}

internal object GnomeStrongholdLocEditor : LocEditor() {
    init {
        edit(gnome_locs.bank_booth) { contentGroup = content.bank_booth }
        edit(gnome_locs.bank_booth_multi) { contentGroup = content.bank_booth }

        edit(gnome_locs.spiral) { contentGroup = content.spiralstaircase_up }
        edit(gnome_locs.spiral_top) { contentGroup = content.spiralstaircase_down }

        doubleDoor(
            gnome_locs.tree_door_left,
            gnome_locs.tree_door_left_open,
            content.closed_left_door,
            content.opened_left_door,
        )
        doubleDoor(
            gnome_locs.tree_door_right,
            gnome_locs.tree_door_right_open,
            content.closed_right_door,
            content.opened_right_door,
        )

        // The ladders land on the player's own tile; the floor options are in the script.
        edit(gnome_locs.ladder_ground) { contentGroup = content.ladder_up }
        edit(gnome_locs.ladder_first) { contentGroup = content.ladder_option }
        edit(gnome_locs.ladder_second) { contentGroup = content.ladder_option }
        edit(gnome_locs.ladder_roof) { contentGroup = content.ladder_down }
    }

    private fun doubleDoor(
        closed: LocType,
        opened: LocType,
        closedGroup: ContentGroupType,
        openedGroup: ContentGroupType,
    ) {
        edit(closed) {
            param[params.next_loc_stage] = opened
            contentGroup = closedGroup
        }
        edit(opened) {
            param[params.next_loc_stage] = closed
            contentGroup = openedGroup
        }
    }
}

object GnomeStrongholdNpcSpawns : MapNpcSpawnBuilder() {
    override fun onPackMapTask() {
        resourceFile<GnomeStrongholdScript>("npcs.toml")
        resourceFile<GnomeStrongholdScript>("outpost-npcs.toml")
    }
}
