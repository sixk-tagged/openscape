@file:Suppress("unused", "SpellCheckingInspection")

package org.rsmod.content.areas.city.alkharid.configs

import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.seqs
import org.rsmod.api.config.refs.synths
import org.rsmod.api.type.editors.loc.LocEditor
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.game.type.content.ContentGroupType
import org.rsmod.game.type.loc.LocType

typealias alkharid_locs = AlKharidLocs

object AlKharidLocs : LocReferences() {
    val bankbooth = find("bankbooth")
    val bank_chest = find("thbankchest")

    // The toll gate's two leaves are multis on the Prince Ali Rescue varp.
    val gate_left = find("kharidmetalgateclosedl")
    val gate_right = find("kharidmetalgateclosedr")
    val gate_left_toll = find("kharidmetalgateclosedl_2op")
    val gate_right_toll = find("kharidmetalgateclosedr_2op")
    val gate_left_open = find("metalgateopenl")
    val gate_right_open = find("metalgateopenr")

    val palace_door_right = find("bankdoor_r")
    val palace_door_right_open = find("openbankdoor_r")
    val palace_door_left = find("bankdoor_l")
    val palace_door_left_open = find("openbankdoor_l")
    val curtain = find("desertdoorclosed")
    val curtain_open = find("desertdooropen")

    val pass_doorway = find("shantay_pass_henge_doorway")
    val prison_door = find("shantay_prisondoor")

    val sorceress_ladder = find("sorceress_ladder")
    val sorceress_ladder_top = find("sorceress_ladder_top")
    val sorceress_shelves = find("sorceress_shelves")

    val crates = listOf(find("crate_old"))
    val boxes = find("boxes3")
}

internal object AlKharidLocEditor : LocEditor() {
    init {
        edit(alkharid_locs.bankbooth) { contentGroup = content.bank_booth }
        edit(alkharid_locs.bank_chest) { contentGroup = content.bank_chest }

        edit(alkharid_locs.sorceress_ladder) { contentGroup = content.ladder_up }
        edit(alkharid_locs.sorceress_ladder_top) {
            param[params.climb_anim] = seqs.human_pickupfloor
            contentGroup = content.ladder_down
        }

        // Quest requirements count as met: Prince Ali Rescue is done, so the toll is free. The
        // multi's two-op variants hide "Pay-toll(10gp)" and the generic picket gate script swings
        // the plain "Open".
        freeGate(
            alkharid_locs.gate_left,
            alkharid_locs.gate_left_toll,
            alkharid_locs.gate_left_open,
            content.closed_left_picketgate,
        )
        freeGate(
            alkharid_locs.gate_right,
            alkharid_locs.gate_right_toll,
            alkharid_locs.gate_right_open,
            content.closed_right_picketgate,
        )
        // The open gates (metalgateopenl/r) are shared with Varrock's metal gates, whose editor
        // makes them closeable. Closing one here swaps in Varrock's identical-looking gate until
        // the toll gate comes back on its own.

        palaceDoor(
            alkharid_locs.palace_door_left,
            alkharid_locs.palace_door_left_open,
            content.closed_left_door,
            content.opened_left_door,
        )
        palaceDoor(
            alkharid_locs.palace_door_right,
            alkharid_locs.palace_door_right_open,
            content.closed_right_door,
            content.opened_right_door,
        )

        edit(alkharid_locs.curtain) {
            param[params.next_loc_stage] = alkharid_locs.curtain_open
            contentGroup = content.closed_single_door
        }
        edit(alkharid_locs.curtain_open) {
            param[params.next_loc_stage] = alkharid_locs.curtain
            contentGroup = content.opened_single_door
        }

        for (crate in alkharid_locs.crates) {
            edit(crate) {
                param[params.game_message] = "You search the crate but find nothing."
                contentGroup = content.empty_crate
            }
        }
        edit(alkharid_locs.boxes) {
            param[params.game_message] = "There is nothing interesting in these boxes."
            contentGroup = content.empty_boxes
        }
    }

    private fun freeGate(
        multi: LocType,
        toll: LocType,
        opened: LocType,
        closedGroup: ContentGroupType,
    ) {
        edit(multi) {
            param[params.opensound] = synths.picketgate_open
            param[params.next_loc_stage] = opened
            contentGroup = closedGroup
        }
        // The client skips ops whose text is "Hidden". Setting any op replaces the whole list.
        edit(toll) {
            op1 = "Open"
            op4 = "Hidden"
        }
    }

    private fun palaceDoor(
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
