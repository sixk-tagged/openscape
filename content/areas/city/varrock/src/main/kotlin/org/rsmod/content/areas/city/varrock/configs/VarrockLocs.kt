@file:Suppress("unused", "SpellCheckingInspection")

package org.rsmod.content.areas.city.varrock.configs

import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.seqs
import org.rsmod.api.config.refs.synths
import org.rsmod.api.type.editors.loc.LocEditor
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.game.type.content.ContentGroupType
import org.rsmod.game.type.loc.LocType

typealias varrock_locs = VarrockLocs

object VarrockLocs : LocReferences() {
    // Banks
    val bank_booth = find("fai_varrock_bankbooth")
    val bank_booth_multi = find("fai_varrock_bankbooth_multi")
    val ge_bank_booth = find("exchange_bank_wall_bank")
    val ge_bank_booth_multi = find("exchange_bank_wall_bank_multi")
    val ge_exchange_booth = find("exchange_bank_wall_exchange")
    val bank_locked_door = find("fai_varrock_bank_locked_door")

    // Single doors, closed then open
    val door = find("fai_varrock_door")
    val door_open = find("fai_varrock_door_open")
    val door_taller = find("fai_varrock_door_taller")
    val door_taller_open = find("fai_varrock_door_open_taller")
    val poor_door = find("fai_varrock_poor_door")
    val poor_door_open = find("fai_varrock_poor_door_open")
    val poor_door_flipped = find("fai_varrock_poor_door_flipped")
    val poor_door_flipped_open = find("fai_varrock_poor_door_open_flipped")
    val shanty_door = find("fai_varrock_shanty_door")
    val shanty_door_open = find("fai_varrock_shanty_door_open")
    val castle_door = find("fai_varrock_castle_door")
    val castle_door_open = find("fai_varrock_castle_door_open")
    val champions_door = find("championdoor")
    val champions_door_open = find("championdoor_open")
    val cooks_guild_door = find("chefdoor")
    val cooks_guild_door_open = find("chefdoor_open")
    val mill_back_door = find("fai_varrock_cook_guild_door")
    val mill_back_door_open = find("fai_varrock_cook_guild_door_inactive")

    // Museum double doors
    val museum_door_left = find("fai_varrock_museum_door_closed_l")
    val museum_door_left_open = find("fai_varrock_museum_door_open_l")
    val museum_door_right = find("fai_varrock_museum_door_closed_r")
    val museum_door_right_open = find("fai_varrock_museum_door_open_r")

    // Double gates, closed then open
    val city_gate_left = find("fai_varrock_gate_l")
    val city_gate_left_open = find("fai_varrock_gate_lc")
    val city_gate_right = find("fai_varrock_gate_r")
    val city_gate_right_open = find("fai_varrock_gate_rc")
    val field_gate_left = find("ii_fencegate_l")
    val field_gate_left_open = find("ii_fencegate_lc")
    val field_gate_right = find("ii_fencegate_r")
    val field_gate_right_open = find("ii_fencegate_rc")
    val member_gate_left = find("fai_varrock_member_gatel")
    val member_gate_left_open = find("fai_varrock_member_gatel_open")
    val member_gate_right = find("fai_varrock_member_gater")
    val member_gate_right_open = find("fai_varrock_member_gater_open")
    val metal_gate_left = find("metalgateclosedl")
    val metal_gate_left_open = find("metalgateopenl")
    val metal_gate_right = find("metalgateclosedr")
    val metal_gate_right_open = find("metalgateopenr")

    // Ladders and stairs
    val ladder = find("fai_varrock_ladder")
    val ladder_top = find("fai_varrock_laddertop")
    val ladder_taller = find("fai_varrock_ladder_taller")
    val ladder_taller_top = find("fai_varrock_ladder_taller_top")
    val manhole_ladder = find("fai_varrock_manhole_ladder")
    val spiral_stairs = find("varrock_spiralstairs")
    val spiral_stairs_taller = find("varrock_spiralstairs_taller")
    val spiral_stairs_middle = find("varrock_spiralstairs_middle")
    val spiral_stairs_middle_taller = find("varrock_spiralstairs_middle_taller")
    val spiral_stairs_top = find("varrock_spiralstairstop")
    val mill_stairs = find("fai_varrock_mill_spiralstairs")
    val mill_stairs_middle = find("fai_varrock_mill_spiralstairs_middle")
    val mill_stairs_top = find("fai_varrock_mill_spiralstairs_top")
    val stairs = find("fai_varrock_stairs")
    val stairs_taller = find("fai_varrock_stairs_taller")
    val stairs_castle = find("fai_varrock_woodenstairs_castle")
    val stairs_palace = find("fai_varrock_stairs_taller_new_fix")
    val stairs_top = find("fai_varrock_stairs_top")

    // Searchables
    val crate_old = find("crate_old")
    val large_crate = find("fai_varrock_large_crate")
    val large_crates = find("fai_varrock_large_crates")
    val small_crates = find("fai_varrock_small_crates")
    val red_crate = find("fai_varrock_red_crate")
    val green_crate = find("fai_varrock_green_crate")
    val sarim_crate = find("sarim_crate2")
    val fairy_crate = find("fairy_crate")
    val fairy_crates = find("fairy_crate_2")
    val lumber_crate = find("gertrudeempty_crate")
    val lumber_barrel = find("gertrudeempty_barrel")
    val boxes = find("fai_varrock_boxes")
    val bookcase = find("fai_varrock_bookcase")
    val posh_bookcase = find("fai_varrock_posh_bookcase")
    val posh_bookcase_short = find("fai_varrock_posh_bookcase_short")
    val posh_bookcase_short2 = find("fai_varrock_posh_bookcase_short2")
    val posh_bookcase_short3 = find("fai_varrock_posh_bookcase_short3")
    val posh_bookcase_short_east = find("fai_varrock_posh_bookcase_short2_east_offset")
    val polybooks_bookcase = find("bookcase_polybooks")
    val book_shelf = find("fai_varrock_shelf_books")
    val book_shelves = find("fai_varrock_shelves_books")
    val chemist_shelves = find("fai_varrock_chemisty_shelves1")
    val chemist_shelves2 = find("fai_varrock_chemisty_shelves2")
    val chemist_desk = find("fai_varrock_chemisty_desk")

    // Containers that open and shut
    val chest_closed = find("fai_varrock_chest_closed")
    val chest_open = find("fai_varrock_chest_open")
    val drawers = find("fai_varrock_drawers")
    val drawers_open = find("fai_varrock_drawers_open")
    val posh_drawers = find("fai_varrock_posh_drawers")
    val posh_drawers_open = find("fai_varrock_posh_drawers_open")

    // Sewers, altars and others
    val manhole_closed = find("manholeclosed")
    val manhole_open = find("manholeopen")
    val church_altar = find("fai_varrock_church_altar")
    val chaos_altar = find("thrantaxaltar")
    val dummy = find("fai_varrock_sword_dummy")
    val signpost = find("fai_varrock_signpost_1")
}

internal object VarrockLocEditor : LocEditor() {
    init {
        // Bank booths join the generic bank booth group (Bank and Collect).
        for (booth in
            listOf(
                varrock_locs.bank_booth,
                varrock_locs.bank_booth_multi,
                varrock_locs.ge_bank_booth,
                varrock_locs.ge_bank_booth_multi,
                varrock_locs.ge_exchange_booth,
            )) {
            edit(booth) { contentGroup = content.bank_booth }
        }

        door(varrock_locs.door, varrock_locs.door_open)
        door(varrock_locs.door_taller, varrock_locs.door_taller_open)
        door(varrock_locs.poor_door, varrock_locs.poor_door_open)
        door(varrock_locs.poor_door_flipped, varrock_locs.poor_door_flipped_open)
        door(varrock_locs.shanty_door, varrock_locs.shanty_door_open)
        door(varrock_locs.castle_door, varrock_locs.castle_door_open)
        // The open forms of these have no Close op: the door swings back on its own.
        closedOnly(varrock_locs.champions_door, varrock_locs.champions_door_open)
        closedOnly(varrock_locs.cooks_guild_door, varrock_locs.cooks_guild_door_open)
        closedOnly(varrock_locs.mill_back_door, varrock_locs.mill_back_door_open)

        museumDoor(
            varrock_locs.museum_door_left,
            varrock_locs.museum_door_left_open,
            content.closed_left_door,
        )
        museumDoor(
            varrock_locs.museum_door_right,
            varrock_locs.museum_door_right_open,
            content.closed_right_door,
        )

        gates(
            varrock_locs.city_gate_left,
            varrock_locs.city_gate_left_open,
            varrock_locs.city_gate_right,
            varrock_locs.city_gate_right_open,
        )
        gates(
            varrock_locs.field_gate_left,
            varrock_locs.field_gate_left_open,
            varrock_locs.field_gate_right,
            varrock_locs.field_gate_right_open,
        )
        gates(
            varrock_locs.member_gate_left,
            varrock_locs.member_gate_left_open,
            varrock_locs.member_gate_right,
            varrock_locs.member_gate_right_open,
        )
        gates(
            varrock_locs.metal_gate_left,
            varrock_locs.metal_gate_left_open,
            varrock_locs.metal_gate_right,
            varrock_locs.metal_gate_right_open,
        )

        for (ladder in listOf(varrock_locs.ladder, varrock_locs.ladder_taller)) {
            edit(ladder) { contentGroup = content.ladder_up }
        }
        for (ladder in listOf(varrock_locs.ladder_top, varrock_locs.ladder_taller_top)) {
            edit(ladder) {
                param[params.climb_anim] = seqs.human_pickupfloor
                contentGroup = content.ladder_down
            }
        }
        edit(varrock_locs.manhole_ladder) { contentGroup = content.dungeonladder_up }

        for (stairs in
            listOf(
                varrock_locs.spiral_stairs,
                varrock_locs.spiral_stairs_taller,
                varrock_locs.mill_stairs,
            )) {
            edit(stairs) { contentGroup = content.spiralstaircase_up }
        }
        for (stairs in
            listOf(
                varrock_locs.spiral_stairs_middle,
                varrock_locs.spiral_stairs_middle_taller,
                varrock_locs.mill_stairs_middle,
            )) {
            edit(stairs) { contentGroup = content.spiralstaircase_option }
        }
        for (stairs in listOf(varrock_locs.spiral_stairs_top, varrock_locs.mill_stairs_top)) {
            edit(stairs) { contentGroup = content.spiralstaircase_down }
        }

        for (crate in
            listOf(
                varrock_locs.crate_old,
                varrock_locs.large_crate,
                varrock_locs.large_crates,
                varrock_locs.small_crates,
                varrock_locs.red_crate,
                varrock_locs.green_crate,
                varrock_locs.sarim_crate,
                varrock_locs.fairy_crate,
                varrock_locs.fairy_crates,
                varrock_locs.lumber_crate,
            )) {
            edit(crate) {
                param[params.game_message] = "You search the crate but find nothing."
                contentGroup = content.empty_crate
            }
        }
        edit(varrock_locs.lumber_barrel) {
            param[params.game_message] = "You search the barrel but find nothing."
            contentGroup = content.empty_crate
        }
        edit(varrock_locs.boxes) {
            param[params.game_message] = "There is nothing interesting in these boxes."
            contentGroup = content.empty_boxes
        }
        for (shelf in
            listOf(
                varrock_locs.bookcase,
                varrock_locs.posh_bookcase,
                varrock_locs.posh_bookcase_short,
                varrock_locs.posh_bookcase_short2,
                varrock_locs.posh_bookcase_short3,
                varrock_locs.posh_bookcase_short_east,
                varrock_locs.polybooks_bookcase,
                varrock_locs.book_shelf,
                varrock_locs.book_shelves,
            )) {
            edit(shelf) { contentGroup = content.bookcase }
        }
        // No game_message param: these fall back to the generic "You find nothing of interest."
        for (shelf in
            listOf(
                varrock_locs.chemist_shelves,
                varrock_locs.chemist_shelves2,
                varrock_locs.chemist_desk,
            )) {
            edit(shelf) { contentGroup = content.empty_sacks }
        }
    }

    private fun door(closed: LocType, opened: LocType) {
        closedOnly(closed, opened)
        edit(opened) {
            param[params.next_loc_stage] = closed
            contentGroup = content.opened_single_door
        }
    }

    private fun closedOnly(closed: LocType, opened: LocType) {
        edit(closed) {
            param[params.next_loc_stage] = opened
            contentGroup = content.closed_single_door
        }
    }

    private fun museumDoor(closed: LocType, opened: LocType, group: ContentGroupType) {
        edit(closed) {
            param[params.next_loc_stage] = opened
            contentGroup = group
        }
    }

    private fun gates(left: LocType, leftOpen: LocType, right: LocType, rightOpen: LocType) {
        edit(left) {
            param[params.opensound] = synths.picketgate_open
            param[params.next_loc_stage] = leftOpen
            contentGroup = content.closed_left_picketgate
        }
        edit(right) {
            param[params.opensound] = synths.picketgate_open
            param[params.next_loc_stage] = rightOpen
            contentGroup = content.closed_right_picketgate
        }
        edit(leftOpen) {
            param[params.closesound] = synths.picketgate_close
            param[params.next_loc_stage] = left
            contentGroup = content.opened_left_picketgate
        }
        edit(rightOpen) {
            param[params.closesound] = synths.picketgate_close
            param[params.next_loc_stage] = right
            contentGroup = content.opened_right_picketgate
        }
    }
}
