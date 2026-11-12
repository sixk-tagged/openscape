@file:Suppress("unused", "SpellCheckingInspection")

package org.rsmod.content.areas.city.yanille

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

typealias yanille_npcs = YanilleNpcs

typealias yanille_invs = YanilleInvs

typealias yanille_locs = YanilleLocs

object YanilleNpcs : NpcReferences() {
    val aleck = find("hunting_shop_owner_yanille")
    val frenita = find("frenita")
    val akutha = find("magic_store_owner")
    val sinina = find("magic_store_owner2")
}

object YanilleInvs : InvReferences() {
    val aleck = find("hunting_shop_yanille")
    val frenita = find("cookeryshop")
    val akutha = find("magicguildshop")
    val sinina = find("magicguildshop2")
}

object YanilleLocs : LocReferences() {
    val guild_door_left = find("magicguild_door_l")
    val guild_door_right = find("magicguild_door_r")
    val dungeon_stairs_down = find("yanillestairsdown")
    val dungeon_stairs_up = find("yanillestairsup")

    val castle_door = find("xbows_castle_door")
    val castle_door_open = find("xbowscastledoor_open")
    val castle_double_left = find("xbowsdoubledoorl")
    val castle_double_left_open = find("xbowsdoubledoor_openl")
    val castle_double_right = find("xbowsdoubledoorr")
    val castle_double_right_open = find("xbowsdoubledoor_openr")
}

/** The west castle's doors, which nothing else gave a door content group. */
internal object YanilleLocEditor : LocEditor() {
    init {
        link(
            yanille_locs.castle_door,
            yanille_locs.castle_door_open,
            content.closed_single_door,
            content.opened_single_door,
        )
        link(
            yanille_locs.castle_double_left,
            yanille_locs.castle_double_left_open,
            content.closed_left_door,
            content.opened_left_door,
        )
        link(
            yanille_locs.castle_double_right,
            yanille_locs.castle_double_right_open,
            content.closed_right_door,
            content.opened_right_door,
        )
    }

    private fun link(
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

internal object YanilleNpcEditor : NpcEditor() {
    init {
        // Shop price multipliers from each shop's StoreTableHead, times 10.
        shop(yanille_npcs.aleck, sell = 1200, buy = 700, change = 20)
        shop(yanille_npcs.frenita, sell = 1000, buy = 550, change = 10)
        shop(yanille_npcs.akutha, sell = 1000, buy = 600, change = 1)
        shop(yanille_npcs.sinina, sell = 1000, buy = 600, change = 1)
    }

    private fun shop(npc: NpcType, sell: Int, buy: Int, change: Int) {
        edit(npc) {
            param[ShopParams.shop_sell_percentage] = sell
            param[ShopParams.shop_buy_percentage] = buy
            param[ShopParams.shop_change_percentage] = change
        }
    }
}

object YanilleNpcSpawns : MapNpcSpawnBuilder() {
    override fun onPackMapTask() {
        resourceFile<YanilleScript>("npcs.toml")
        resourceFile<YanilleScript>("dungeon-npcs.toml")
    }
}
