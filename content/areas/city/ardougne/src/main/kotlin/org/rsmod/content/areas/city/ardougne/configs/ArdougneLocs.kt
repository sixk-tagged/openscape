@file:Suppress("unused", "SpellCheckingInspection")

package org.rsmod.content.areas.city.ardougne.configs

import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.seqs
import org.rsmod.api.config.refs.synths
import org.rsmod.api.type.editors.loc.LocEditor
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.game.type.content.ContentGroupType
import org.rsmod.game.type.loc.LocType

typealias ardougne_locs = ArdougneLocs

object ArdougneLocs : LocReferences() {
    // `bankbooth` itself is edited by the Draynor and Al Kharid modules.
    val bank_booth_end = find("bankbooth_end_right")
    val bank_booth_multi = find("bankbooth_multi")

    // Wall doors, closed then open.
    val poor_door_cross = find("poordoor_cross")
    val poor_door_cross_open = find("poordoor_crossopen")
    val posh_door_reverse = find("poshdoor_reverse")
    val posh_door_reverse_open = find("poshdooropen_reverse")
    val zoo_door = find("peng_ardougne_enclosure_door")
    val zoo_door_open = find("peng_ardougne_enclosure_door_open")
    val castle_door = find("castledoor")
    val castle_door_open = find("opencastledoor")
    val mourner_door = find("mournerstewdoor")
    val mourner_door_open = find("mournerstewdooropen")
    val mourner_door_up = find("mournerstewdoorup")
    val mourner_door_up_open = find("mournerstewdoorupopen")
    val braveks_door = find("bravekdoorshut")
    val braveks_door_open = find("bravekdooropen")
    val rehnison_door = find("rehnisondoorshut")
    val rehnison_door_open = find("rehnisondooropen")
    val elena_door = find("elenadoor2")
    val elena_door_open = find("elenadoor2open")
    val plague_door = find("plagueelenadoorshut")
    val plague_door_open = find("plagueelenadooropen")

    val church_door_left = find("w_ardougnedoubledoorl")
    val church_door_left_open = find("w_ardougnedoubledoorlopen")
    val church_door_right = find("w_ardougnedoubledoorr")
    val church_door_right_open = find("w_ardougnedoubledoorropen")

    // The plague wall's doors: a left and a right half.
    val wall_door_left = find("ardougnedoor_l")
    val wall_door_right = find("ardougnedoor_r")

    val ladder_middle = find("laddermiddle")
    val witchaven_ladder = find("slug2_ladder")
    val witchaven_ladder_top = find("slug2_laddertop")
    val witchaven_baitshop_ladder_top = find("slug2_baitshop_laddertop")
    val sewer_ladder = find("ladder_from_cellar")

    val farm_gate_left = find("fencegate_l")
    val farm_gate_left_open = find("openfencegate_l")
    val farm_gate_right = find("fencegate_r")
    val farm_gate_right_open = find("openfencegate_r")
    val stile = find("fullstyle")
    val gnome_railing = find("treegnomelooserailing")
    val gnome_ladder_top = find("laddertop_norim")
    val spiral_stairs_middle = find("spiralstairsmiddle")

    // Locked doors and gates that Pick-lock opens (Thieving).
    val locked_door_coins = find("picklock1")
    val locked_door_nature = find("picklock2")
    val sewer_gate_left = find("picklock3_l")
    val sewer_gate_right = find("picklock3_r")
    val chaos_tower_door = find("picklock4")
    val castle_locked_door = find("picklock5")
    val ross_door = find("con_contract_ardy_west_door_locked")

    val outdoor_stairs_bottom = find("outdoorstairs_wooden_bottom")
    val outdoor_stairs_top = find("outdoorstairs_wooden_top")
}

internal object ArdougneLocEditor : LocEditor() {
    init {
        edit(ardougne_locs.bank_booth_end) { contentGroup = content.bank_booth }
        edit(ardougne_locs.bank_booth_multi) { contentGroup = content.bank_booth }

        door(ardougne_locs.poor_door_cross, ardougne_locs.poor_door_cross_open)
        door(ardougne_locs.posh_door_reverse, ardougne_locs.posh_door_reverse_open)
        door(ardougne_locs.zoo_door, ardougne_locs.zoo_door_open)
        door(ardougne_locs.castle_door, ardougne_locs.castle_door_open)
        door(ardougne_locs.mourner_door, ardougne_locs.mourner_door_open)
        door(ardougne_locs.mourner_door_up, ardougne_locs.mourner_door_up_open)
        door(ardougne_locs.braveks_door, ardougne_locs.braveks_door_open)
        door(ardougne_locs.rehnison_door, ardougne_locs.rehnison_door_open)
        door(ardougne_locs.elena_door, ardougne_locs.elena_door_open)
        door(ardougne_locs.plague_door, ardougne_locs.plague_door_open)

        doubleDoor(
            ardougne_locs.church_door_left,
            ardougne_locs.church_door_left_open,
            content.closed_left_door,
            content.opened_left_door,
        )
        doubleDoor(
            ardougne_locs.church_door_right,
            ardougne_locs.church_door_right_open,
            content.closed_right_door,
            content.opened_right_door,
        )

        gate(
            ardougne_locs.farm_gate_left,
            ardougne_locs.farm_gate_left_open,
            content.closed_left_picketgate,
            content.opened_left_picketgate,
        )
        gate(
            ardougne_locs.farm_gate_right,
            ardougne_locs.farm_gate_right_open,
            content.closed_right_picketgate,
            content.opened_right_picketgate,
        )
        edit(ardougne_locs.spiral_stairs_middle) { contentGroup = content.spiralstaircase_option }
        edit(ardougne_locs.gnome_ladder_top) {
            param[params.climb_anim] = seqs.human_pickupfloor
            contentGroup = content.ladder_down
        }

        // The middle floor of a three-storey ladder offers both ways.
        edit(ardougne_locs.ladder_middle) { contentGroup = content.ladder_option }
        edit(ardougne_locs.witchaven_ladder) { contentGroup = content.ladder_up }
        for (top in
            listOf(
                ardougne_locs.witchaven_ladder_top,
                ardougne_locs.witchaven_baitshop_ladder_top,
            )) {
            edit(top) {
                param[params.climb_anim] = seqs.human_pickupfloor
                contentGroup = content.ladder_down
            }
        }
        // The eastern sewer's way out, six thousand four hundred tiles down the y axis.
        edit(ardougne_locs.sewer_ladder) { contentGroup = content.dungeonladder_up }
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
