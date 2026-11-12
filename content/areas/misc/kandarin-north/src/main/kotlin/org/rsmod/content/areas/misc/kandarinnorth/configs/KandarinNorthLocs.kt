@file:Suppress("SpellCheckingInspection")

package org.rsmod.content.areas.misc.kandarinnorth.configs

import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.seqs
import org.rsmod.api.config.refs.synths
import org.rsmod.api.type.editors.loc.LocEditor
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.game.type.content.ContentGroupType
import org.rsmod.game.type.loc.LocType

typealias kandarin_north_locs = KandarinNorthLocs

object KandarinNorthLocs : LocReferences() {
    val bank_booth = find("kr_bankbooth")
    val bank_booth_multi = find("kr_bankbooth_multi")
    val catherby_bank_booth_multi = find("bankbooth_multi")
    val deposit_box = find("kr_bank_deposit_box")
    val guild_bank_chest = find("wcguild_bankchest")

    val poor_door = find("kr_poordoor")
    val poor_door_open = find("kr_poordooropen")
    val posh_door = find("kr_poshdoor")
    val posh_door_open = find("kr_poshdooropen")
    val camelot_door = find("kr_cam_poshdoor")
    val camelot_door_open = find("kr_cam_poshdoor_open")
    val mansion_poor_door = find("kr_sin_poordoor")
    val mansion_poor_door_open = find("kr_sin_poordooropen")
    val mansion_posh_door = find("kr_sin_poshdoor")
    val mansion_posh_door_open = find("kr_sin_poshdooropen")

    val bank_door_left = find("kr_doubledoor_l")
    val bank_door_left_open = find("kr_opendoubledoor_l")
    val bank_door_right = find("kr_doubledoor_r")
    val bank_door_right_open = find("kr_opendoubledoor_r")
    val camelot_double_left = find("kr_cam_doubledoorl")
    val camelot_double_left_open = find("kr_cam_doubledoorl_open")
    val camelot_double_right = find("kr_cam_doubledoorr")
    val camelot_double_right_open = find("kr_cam_doubledoorr_open")
    val court_double_left = find("kr_courthouse_double_door_l")
    val court_double_left_open = find("kr_courthouse_double_door_l_open")
    val court_double_right = find("kr_courthouse_double_door_r")
    val court_double_right_open = find("kr_courthouse_double_door_r_open")
    val church_door_left = find("kr_churchdoor_l")
    val church_door_left_open = find("kr_openchurchdoor_l")
    val church_door_right = find("kr_churchdoor_r")
    val church_door_right_open = find("kr_openchurchdoor_r")
    val mansion_double_left = find("kr_mansion_double_door_l")
    val mansion_double_left_open = find("kr_mansion_open_double_door_l")
    val mansion_double_right = find("kr_mansion_double_door_r")
    val mansion_double_right_open = find("kr_mansion_open_double_door_r")

    val camelot_gate_left = find("kr_camelot_metalgateclosedl")
    val camelot_gate_left_open = find("kr_camelot_metalgateopenl")
    val camelot_gate_right = find("kr_camelot_metalgateclosedr")
    val camelot_gate_right_open = find("kr_camelot_metalgateopenr")

    val ladder = find("kr_ladder")
    val ladder_top = find("kr_laddertop")
    val ladder_directional = find("kr_ladder_directional")
    val ladder_top_directional = find("kr_laddertop_directional")
    val camelot_ladder = find("kr_cam_ladder")
    val camelot_ladder_top = find("kr_cam_laddertop")

    val spiral_stairs = find("kr_spiralstairs")
    val spiral_stairs_top = find("kr_spiralstairstop")
    val camelot_stairs = find("kr_cam_woodenstairs")
    val camelot_stairs_top = find("kr_cam_woodenstairstop")
    val sinclair_stairs = find("murder_qip_spiralstairs")
    val sinclair_stairs_top = find("murder_qip_spiralstairstop")
    val courthouse_stairs_top = find("kr_courthouse_stairs_top")
    val roof_ladder = find("favour_seer_ladder")
    val tower_ladder_up = find("ranging_ladder_up")
    val tower_ladder_down = find("ranging_ladder_down")

    val coal_truck = find("coal_truck")

    val loose_railing = find("mcgruborlooserailing")
    val mcgrubor_gate_left = find("mcgruborgatel")
    val mcgrubor_gate_right = find("mcgruborgater")
    val hemenster_gate_left = find("fishinggateclosedl")
    val hemenster_gate_right = find("fishinggateclosedr")
    val tunnel_stairs_down = find("tunnelstairstop")
    val tunnel_stairs_up = find("tunnelstairs")
    val tunnel_stairs_down_east = find("tunnelstairstop2")
    val tunnel_stairs_up_east = find("tunnelstairs2")

    val fishing_guild_door = find("fishguilddoor")
    val ranging_guild_door = find("ranging_guild_door")
    val legends_door_left = find("legendsguilddoorl")
    val legends_door_right = find("legendsguilddoorr")
    val legends_gate_left = find("legendsguildgatel")
    val legends_gate_right = find("legendsguildgater")

    val crates =
        listOf(
            find("kr_crate"),
            find("crate2"),
            find("crate_old"),
            find("sarim_crate"),
            find("sarim_crate2"),
        )
    val boxes = listOf(find("boxes2"), find("boxes3"))
    val bookcases = listOf(find("kr_bookcase"), find("kr_bookcase2"), find("kr_cam_bookcase"))
}

internal object KandarinNorthLocEditor : LocEditor() {
    init {
        with(KandarinNorthLocs) {
            for (booth in listOf(bank_booth, bank_booth_multi, catherby_bank_booth_multi)) {
                edit(booth) { contentGroup = content.bank_booth }
            }
            edit(deposit_box) { contentGroup = content.bank_deposit_box }
            edit(guild_bank_chest) { contentGroup = content.bank_chest }

            door(poor_door, poor_door_open)
            door(posh_door, posh_door_open)
            door(camelot_door, camelot_door_open)
            door(mansion_poor_door, mansion_poor_door_open)
            door(mansion_posh_door, mansion_posh_door_open)

            doubleDoor(bank_door_left, bank_door_left_open, left = true)
            doubleDoor(bank_door_right, bank_door_right_open, left = false)
            doubleDoor(camelot_double_left, camelot_double_left_open, left = true)
            doubleDoor(camelot_double_right, camelot_double_right_open, left = false)
            doubleDoor(court_double_left, court_double_left_open, left = true)
            doubleDoor(court_double_right, court_double_right_open, left = false)
            doubleDoor(church_door_left, church_door_left_open, left = true)
            doubleDoor(church_door_right, church_door_right_open, left = false)
            doubleDoor(mansion_double_left, mansion_double_left_open, left = true)
            doubleDoor(mansion_double_right, mansion_double_right_open, left = false)

            gate(camelot_gate_left, camelot_gate_left_open, left = true)
            gate(camelot_gate_right, camelot_gate_right_open, left = false)

            // These ladders climb to the same tile a floor up or down, like the generic ones.
            for (up in listOf(ladder, ladder_directional, camelot_ladder, roof_ladder)) {
                edit(up) { contentGroup = content.ladder_up }
            }
            for (down in listOf(ladder_top, ladder_top_directional, camelot_ladder_top)) {
                edit(down) {
                    param[params.climb_anim] = seqs.human_pickupfloor
                    contentGroup = content.ladder_down
                }
            }

            for (crate in crates) {
                edit(crate) {
                    param[params.game_message] = "You search the crate but find nothing."
                    contentGroup = content.empty_crate
                }
            }
            for (box in boxes) {
                edit(box) {
                    param[params.game_message] = "There is nothing interesting in these boxes."
                    contentGroup = content.empty_boxes
                }
            }
            for (bookcase in bookcases) {
                edit(bookcase) { contentGroup = content.bookcase }
            }
        }
    }

    private fun door(closed: LocType, opened: LocType) {
        pair(closed, opened, content.closed_single_door, content.opened_single_door)
    }

    private fun doubleDoor(closed: LocType, opened: LocType, left: Boolean) {
        val closedGroup = if (left) content.closed_left_door else content.closed_right_door
        val openedGroup = if (left) content.opened_left_door else content.opened_right_door
        pair(closed, opened, closedGroup, openedGroup)
    }

    private fun gate(closed: LocType, opened: LocType, left: Boolean) {
        val closedGroup =
            if (left) content.closed_left_picketgate else content.closed_right_picketgate
        val openedGroup =
            if (left) content.opened_left_picketgate else content.opened_right_picketgate
        edit(closed) {
            param[params.opensound] = synths.picketgate_open
            param[params.next_loc_stage] = opened
            contentGroup = closedGroup
        }
        edit(opened) {
            param[params.closesound] = synths.picketgate_close
            param[params.next_loc_stage] = closed
            contentGroup = openedGroup
        }
    }

    private fun pair(
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
