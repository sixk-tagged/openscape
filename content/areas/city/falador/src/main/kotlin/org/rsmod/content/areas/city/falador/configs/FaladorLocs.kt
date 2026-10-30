@file:Suppress("unused", "SpellCheckingInspection")

package org.rsmod.content.areas.city.falador.configs

import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.params
import org.rsmod.api.type.editors.loc.LocEditor
import org.rsmod.api.type.refs.content.ContentReferences
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.game.type.content.ContentGroupType
import org.rsmod.game.type.loc.LocType

typealias falador_locs = FaladorLocs

object FaladorContent : ContentReferences() {
    val smithing_anvil = find("smithing_anvil")
}

object FaladorLocs : LocReferences() {
    val bank_booth = find("fai_falador_bankbooth")
    val bank_booth_multi = find("fai_falador_bankbooth_multi")

    val poor_door = find("fai_falador_poor_door")
    val poor_door_open = find("fai_falador_poor_door_open")
    val poor_door_taller = find("fai_falador_poor_door_taller")
    val poor_door_open_taller = find("fai_falador_poor_door_open_taller")
    val poor_door_flip = find("fai_falador_poor_door_flip")
    val poor_door_open_flip = find("fai_falador_poor_door_open_flip")
    val castle_door = find("fai_falador_poor_castle_door")
    val castle_door_open = find("fai_falador_poor_castle_door_open")
    val farm_door = find("farmdoor")
    val farm_door_open = find("farmdooropen")
    val cannon_door = find("mcannondoor")
    val cannon_door_open = find("mcannondoor1")

    val castle_double_door_left = find("fai_falador_castledoubledoorl")
    val castle_double_door_left_open = find("fai_falador_opencastledoubledoorl")
    val castle_double_door_right = find("fai_falador_castledoubledoorr")
    val castle_double_door_right_open = find("fai_falador_opencastledoubledoorr")
    val double_door_left = find("fai_falador_double_door_l")
    val double_door_left_open = find("fai_falador_double_door_l_open")
    val double_door_right = find("fai_falador_double_door_r")
    val double_door_right_open = find("fai_falador_double_door_r_open")

    val castle_ladder = find("fai_falador_castle_ladder_up")
    val castle_ladder_top = find("fai_falador_castle_laddertop")
    val ladder = find("fai_falador_ladder")
    val ladder_taller = find("fai_falador_ladder_taller")
    val ladder_top = find("fai_falador_ladder_top")
    val ladder_top_north = find("fai_falador_ladder_top_forceapproach_north")
    val castle_spiral = find("fai_falador_castle_spiralstairs")
    val castle_spiral_top = find("fai_falador_castle_spiralstairstop")
    val spiral = find("fai_falador_spiralstairs")
    val spiral_top = find("fai_falador_spiralstairstop")
    val dark_tower_spiral = find("fai_darkwiztower_spiralstairs")
    val dark_tower_spiral_middle = find("fai_darkwiztower_spiralstairs_middle")
    val dark_tower_spiral_top = find("fai_darkwiztower_spiralstairstop")
    val outdoor_stairs = find("fai_falador_outdoorstairs_bottom")
    val outdoor_stairs_top = find("fai_falador_outdoorstairs_top")

    val mine_trapdoor = find("fai_dwarf_trapdoor_down")
    val mine_ladder = find("ladder_from_cellar_directional")
    val mine_stairs_down = find("stairs_cellar")
    val mine_stairs_up = find("stairs_falador")

    val guild_ladder = find("mguild_ladder")
    val crypt_entrance = find("fai_death_crypt_entrance")
    val crypt_exit = find("fai_death_crypt_exit")
    val guild_door = find("mguild_door")
    val guild_inner_door = find("mguild_door_inner1")
    val guild_inner_door2 = find("mguild_door_inner2")

    val dorics_anvil = find("dorics_anvil")

    val boxes = find("fai_falador_boxes")
    val crates =
        listOf(find("fai_falador_small_crates"), find("crate2"), find("fai_barbarian_small_crates"))
}

internal object FaladorLocEditor : LocEditor() {
    init {
        edit(falador_locs.bank_booth) { contentGroup = content.bank_booth }
        edit(falador_locs.bank_booth_multi) { contentGroup = content.bank_booth }

        door(falador_locs.poor_door, falador_locs.poor_door_open)
        door(falador_locs.poor_door_taller, falador_locs.poor_door_open_taller)
        door(falador_locs.poor_door_flip, falador_locs.poor_door_open_flip)
        door(falador_locs.castle_door, falador_locs.castle_door_open)
        door(falador_locs.farm_door, falador_locs.farm_door_open)
        // Nulodion's door is locked until Dwarf Cannon, which counts as done.
        door(falador_locs.cannon_door, falador_locs.cannon_door_open)

        doubleDoor(
            falador_locs.castle_double_door_left,
            falador_locs.castle_double_door_left_open,
            content.closed_left_door,
            content.opened_left_door,
        )
        doubleDoor(
            falador_locs.castle_double_door_right,
            falador_locs.castle_double_door_right_open,
            content.closed_right_door,
            content.opened_right_door,
        )
        doubleDoor(
            falador_locs.double_door_left,
            falador_locs.double_door_left_open,
            content.closed_left_door,
            content.opened_left_door,
        )
        doubleDoor(
            falador_locs.double_door_right,
            falador_locs.double_door_right_open,
            content.closed_right_door,
            content.opened_right_door,
        )

        // The castle ladders land on the same tile; the town ladders (FaladorLocScript) do not.
        edit(falador_locs.castle_ladder) { contentGroup = content.ladder_up }
        edit(falador_locs.castle_ladder_top) { contentGroup = content.ladder_down }

        edit(falador_locs.castle_spiral) { contentGroup = content.spiralstaircase_up }
        edit(falador_locs.castle_spiral_top) { contentGroup = content.spiralstaircase_down }
        edit(falador_locs.spiral) { contentGroup = content.spiralstaircase_up }
        edit(falador_locs.spiral_top) { contentGroup = content.spiralstaircase_down }
        edit(falador_locs.dark_tower_spiral) { contentGroup = content.spiralstaircase_up }
        edit(falador_locs.dark_tower_spiral_middle) {
            contentGroup = content.spiralstaircase_option
        }
        edit(falador_locs.dark_tower_spiral_top) { contentGroup = content.spiralstaircase_down }

        // The Ice Mountain trapdoor into the Dwarven Mine and the ladder back up.
        edit(falador_locs.mine_trapdoor) { contentGroup = content.dungeonladder_down }
        edit(falador_locs.mine_ladder) { contentGroup = content.dungeonladder_up }

        // Anyone who has done Doric's Quest (counts as done) may use his anvils.
        edit(falador_locs.dorics_anvil) { contentGroup = FaladorContent.smithing_anvil }

        edit(falador_locs.boxes) {
            param[params.game_message] = "There is nothing interesting in these boxes."
            contentGroup = content.empty_boxes
        }
        for (crate in falador_locs.crates) {
            edit(crate) {
                param[params.game_message] = "You search the crate but find nothing."
                contentGroup = content.empty_crate
            }
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
