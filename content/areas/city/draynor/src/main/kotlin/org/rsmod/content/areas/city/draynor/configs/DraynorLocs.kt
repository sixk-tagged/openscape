@file:Suppress("unused", "SpellCheckingInspection")

package org.rsmod.content.areas.city.draynor.configs

import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.seqs
import org.rsmod.api.config.refs.synths
import org.rsmod.api.type.editors.loc.LocEditor
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.game.type.content.ContentGroupType
import org.rsmod.game.type.loc.LocType

typealias draynor_locs = DraynorLocs

object DraynorLocs : LocReferences() {
    val bankbooth = find("bankbooth")

    val panelled_door = find("draynor_panelled_door")
    val panelled_door_open = find("draynor_panelled_door_open")
    val tower_door = find("fai_wiztower_poor_door")
    val tower_door_open = find("fai_wiztower_poor_door_open")
    val pen_gate_left = find("farming_fencegate_l")
    val pen_gate_right = find("farming_fencegate_r")
    val pen_gate_left_open = find("farming_openfencegate_l")
    val pen_gate_right_open = find("farming_openfencegate_r")

    val sewer_trapdoor = find("vampire_trap1")
    val sewer_trapdoor_open = find("vampire_trap2")
    val sewer_ladder = find("vampire_ladder")
    val tower_ladder_top = find("wizards_tower_laddertop")
    val tower_ladder_bottom = find("wizards_tower_ladder")
    val tower_stairs = find("fai_wiztower_spiralstairs")
    val tower_stairs_middle = find("fai_wiztower_spiralstairs_middle")
    val tower_stairs_top = find("fai_wiztower_spiralstairstop")
    val manor_stairs = find("draynor_spiralstairs")
    val manor_stairs_top = find("sarim_spiralstairstop")

    val manor_cabbage = find("draynor_magic_cabbage")
    val manor_bookcase_left = find("hauntedbookcasel")
    val manor_bookcase_right = find("hauntedbookcaser")
    val manor_lever = find("hauntedleverup")
    val manor_front_door_left = find("haunteddoorl")
    val manor_front_door_right = find("haunteddoorr")
    val manor_back_door = find("hauntedbackdoor")
    val manor_inner_stairs = find("draynor_manor_stairs_up")
    val manor_inner_stairs_top = find("draynor_manor_stairs_down")
    val crypt_stairs = find("cryptstairsdown")
    val crypt_stairs_top = find("cryptstairsup")
    val cellar_ladder = find("puzzle_ladder")
    val cellar_ladder_top = find("puzzle_ladder_top")

    val jail_gate = find("alidoor")
    val garlic_cupboard = find("garliccupboardshut")
    val garlic_cupboard_open = find("garliccupboardopen")
    val nettles =
        listOf(
            find("nettles"),
            find("nettles1"),
            find("nettles2"),
            find("nettles3"),
            find("nettles4"),
            find("nettles5"),
            find("nettles6"),
        )

    val crate_old = find("crate_old")
    val boxes_old = find("boxes3")
    val crates =
        listOf(
            find("draynor_crate"),
            find("draynor_crates"),
            find("fai_wiztower_crate2"),
            find("fai_wiztower_crate3"),
            find("aggie_courtyard_crate"),
            find("aggie_courtyard_crates_stacked"),
        )
    val boxes = find("fai_wiztower_boxes")
    val bookcases =
        listOf(
            find("bookcase2"),
            find("bookcase_polybooks"),
            find("fai_wiztower_bookcase"),
            find("fai_wiztower_bookcase_wall"),
            find("bookshelf_old_tall_double"),
            find("bookshelf_old_tall_movedin"),
            find("bookshelf_old_tall2_movedin"),
            find("bookshelf_old_short_movedin"),
            find("bookshelf_old_tall_movedin2"),
            find("bookshelf_old_tall2_movedin2"),
            find("bookshelf_old_tall3_movedin2"),
            find("bookshelf_old_short_movedin2"),
            find("bookshelf_old_short3_movedin2"),
            find("bookshelf_old_tall_movedin3"),
            find("bookshelf_old_tall2_movedin3"),
            find("bookshelf_old_tall3_movedin3"),
            find("bookshelf_old_short2_movedin3"),
            find("bookshelf_old_short3_movedin3"),
            find("bookshelf_old_short_withsphinx"),
            find("wom_bookshelf_old_tall_double"),
            find("wom_bookshelf_old_tall_double2"),
        )
}

internal object DraynorLocEditor : LocEditor() {
    init {
        edit(draynor_locs.bankbooth) { contentGroup = content.bank_booth }

        door(draynor_locs.panelled_door, draynor_locs.panelled_door_open)
        door(draynor_locs.tower_door, draynor_locs.tower_door_open)

        gate(
            draynor_locs.pen_gate_left,
            draynor_locs.pen_gate_left_open,
            content.closed_left_picketgate,
            content.opened_left_picketgate,
        )
        gate(
            draynor_locs.pen_gate_right,
            draynor_locs.pen_gate_right_open,
            content.closed_right_picketgate,
            content.opened_right_picketgate,
        )

        // The open trapdoor is climbed through like Lumbridge's: down 6400 tiles on the y axis.
        edit(draynor_locs.sewer_trapdoor_open) {
            param[params.climb_anim] = seqs.human_pickupfloor
            contentGroup = content.dungeonladder_down
        }

        edit(draynor_locs.tower_stairs) { contentGroup = content.spiralstaircase_up }
        edit(draynor_locs.tower_stairs_middle) { contentGroup = content.spiralstaircase_option }
        edit(draynor_locs.tower_stairs_top) { contentGroup = content.spiralstaircase_down }
        edit(draynor_locs.manor_stairs) { contentGroup = content.spiralstaircase_up }
        edit(draynor_locs.manor_stairs_top) { contentGroup = content.spiralstaircase_down }

        for (crate in draynor_locs.crates) {
            edit(crate) {
                param[params.game_message] = "You search the crate but find nothing."
                contentGroup = content.empty_crate
            }
        }
        edit(draynor_locs.crate_old) {
            param[params.game_message] = "You search the crate but find nothing."
            contentGroup = content.empty_crate
        }
        edit(draynor_locs.boxes) {
            param[params.game_message] = "There is nothing interesting in these boxes."
            contentGroup = content.empty_boxes
        }
        edit(draynor_locs.boxes_old) {
            param[params.game_message] = "There is nothing interesting in these boxes."
            contentGroup = content.empty_boxes
        }
        for (bookcase in draynor_locs.bookcases) {
            edit(bookcase) { contentGroup = content.bookcase }
        }
    }

    private fun door(closed: LocType, opened: LocType) {
        edit(closed) {
            param[params.next_loc_stage] = opened
            contentGroup = content.closed_single_door
        }
        edit(opened) {
            param[params.next_loc_stage] = closed
            contentGroup = content.opened_single_door
        }
    }

    private fun gate(
        closed: LocType,
        opened: LocType,
        closedGroup: ContentGroupType,
        openedGroup: ContentGroupType,
    ) {
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
}
