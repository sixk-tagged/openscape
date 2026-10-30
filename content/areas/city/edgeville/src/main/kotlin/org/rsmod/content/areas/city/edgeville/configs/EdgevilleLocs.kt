@file:Suppress("unused", "SpellCheckingInspection")

package org.rsmod.content.areas.city.edgeville.configs

import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.seqs
import org.rsmod.api.type.editors.loc.LocEditor
import org.rsmod.api.type.refs.content.ContentReferences
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.game.type.content.ContentGroupType
import org.rsmod.game.type.loc.LocType

typealias edgeville_locs = EdgevilleLocs

object EdgevilleContent : ContentReferences() {
    val furnace = find("smithing_furnace")
}

object EdgevilleSeqs : SeqReferences() {
    val pray = find("human_pray")
}

object EdgevilleLocs : LocReferences() {
    // The dungeon: the trapdoor, the brass key shed and the odd looking wall.
    val trapdoor = find("trapdoor")
    val trapdoor_open = find("trapdoor_open")
    val brass_key_door = find("brasskeydoor")
    val shed_ladder = find("ladder_cellar")
    val oddwall_left = find("oddwall_l")
    val oddwall_right = find("oddwall_r")
    val oddwall_left_open = find("openoddwall_l")
    val oddwall_right_open = find("openoddwall_r")

    // The monastery and the altars.
    val monastery_ladder = find("monasteryladder")
    val monastery_altar = find("monks_altar")
    val chaos_altar = find("chaosaltar")

    val furnace = find("varrock_diary_furnace")

    // Black Knights' Fortress.
    val fortress_door = find("bkfortressdoor1")
    val fortress_door_2 = find("bkfortressdoor2")
    val fortress_door_3 = find("bkfortressdoor3")
    val wild_door = find("wild_door")
    val wild_door_open = find("wild_door_open")
    val fortress_ladder = find("dk_ladder")
    val fortress_ladder_top = find("dk_laddertop")
    val meeting_ladder = find("dk_meeting_ladder")
    val meeting_ladder_top = find("dk_meeting_laddertop")
    val fortress_stairs = find("dk_spiralstairs")
    val fortress_stairs_top = find("dk_spiralstairstop")

    // Chaos Temple (Asgarnia) ladders.
    val temple_ladder = find("chaos_temple_ladder")
    val temple_ladder_top = find("chaos_temple_laddertop")

    // Barbarian Village.
    val barbarian_door = find("fai_barbarian_poordoor")
    val barbarian_door_open = find("fai_barbarian_poordooropen")
    val tower_ladder = find("ladder_directional")
    val tower_ladder_top = find("laddertop_directional")
    val longhall_door_left = find("fai_barbarian_large_doubledoor_l")
    val longhall_door_right = find("fai_barbarian_large_doubledoor_r")
    val longhall_door_left_open = find("fai_barbarian_large_doubledoor_l_inactive")
    val longhall_door_right_open = find("fai_barbarian_large_doubledoor_r_inactive")

    // Goblin Village.
    val goblin_door = find("goblin_outpost_poordoor")
    val goblin_door_open = find("goblin_outpost_openpoordoor")
    val goblin_hall_left = find("goblin_outpost_poordoor_double_inner")
    val goblin_hall_right = find("goblin_outpost_poordoor_doubler_inner")
    val goblin_hall_left_open = find("goblin_outpost_poordoor_double_inner_noop")
    val goblin_hall_right_open = find("goblin_outpost_poordoor_doubler_inner_noop")
    val goblin_ladder_bottom = find("goblin_ladder_bottom")
    val goblin_ladder_top = find("goblin_ladder_top")
    val goblin_crates =
        listOf(
            find("goblin_outpost_large_crate_armour1"),
            find("goblin_outpost_large_crate_armour2"),
            find("goblin_outpost_large_crate_armour3"),
        )
}

internal object EdgevilleLocEditor : LocEditor() {
    init {
        single(edgeville_locs.wild_door, edgeville_locs.wild_door_open)
        single(edgeville_locs.barbarian_door, edgeville_locs.barbarian_door_open)
        single(edgeville_locs.goblin_door, edgeville_locs.goblin_door_open)
        // The fortress's other doors have the wild door's model and recolours.
        closedOnly(edgeville_locs.fortress_door_2, edgeville_locs.wild_door_open)
        closedOnly(edgeville_locs.fortress_door_3, edgeville_locs.wild_door_open)

        // The open halves have no ops: the doors close again by themselves.
        doubleDoor(
            edgeville_locs.longhall_door_left,
            edgeville_locs.longhall_door_left_open,
            content.closed_left_door,
        )
        doubleDoor(
            edgeville_locs.longhall_door_right,
            edgeville_locs.longhall_door_right_open,
            content.closed_right_door,
        )
        // The Edgeville Dungeon's odd looking wall opens like a double door.
        doubleDoor(
            edgeville_locs.oddwall_left,
            edgeville_locs.oddwall_left_open,
            content.closed_left_door,
        )
        doubleDoor(
            edgeville_locs.oddwall_right,
            edgeville_locs.oddwall_right_open,
            content.closed_right_door,
        )
        doubleDoor(
            edgeville_locs.goblin_hall_left,
            edgeville_locs.goblin_hall_left_open,
            content.closed_left_door,
        )
        doubleDoor(
            edgeville_locs.goblin_hall_right,
            edgeville_locs.goblin_hall_right_open,
            content.closed_right_door,
        )

        // Ladder pairs sit on the same tile one plane apart, so the generic ladders fit.
        for (ladder in
            listOf(
                edgeville_locs.fortress_ladder,
                edgeville_locs.meeting_ladder,
                edgeville_locs.temple_ladder,
                edgeville_locs.tower_ladder,
            )) {
            edit(ladder) { contentGroup = content.ladder_up }
        }
        for (ladder in
            listOf(
                edgeville_locs.fortress_ladder_top,
                edgeville_locs.meeting_ladder_top,
                edgeville_locs.temple_ladder_top,
                edgeville_locs.tower_ladder_top,
            )) {
            edit(ladder) {
                param[params.climb_anim] = seqs.human_pickupfloor
                contentGroup = content.ladder_down
            }
        }
        edit(edgeville_locs.fortress_stairs) { contentGroup = content.spiralstaircase_up }
        edit(edgeville_locs.fortress_stairs_top) { contentGroup = content.spiralstaircase_down }

        // Both climb six thousand four hundred tiles on the y axis, like Lumbridge's.
        for (down in listOf(edgeville_locs.shed_ladder, edgeville_locs.trapdoor_open)) {
            edit(down) {
                param[params.climb_anim] = seqs.human_pickupfloor
                contentGroup = content.dungeonladder_down
            }
        }

        edit(edgeville_locs.furnace) { contentGroup = EdgevilleContent.furnace }
    }

    private fun single(closed: LocType, opened: LocType) {
        closedOnly(closed, opened)
        edit(opened) {
            param[params.next_loc_stage] = closed
            contentGroup = content.opened_single_door
        }
    }

    // The opened type has no ops here: the door reverts on its own after a while.
    private fun closedOnly(closed: LocType, opened: LocType) {
        edit(closed) {
            param[params.next_loc_stage] = opened
            contentGroup = content.closed_single_door
        }
    }

    private fun doubleDoor(closed: LocType, opened: LocType, group: ContentGroupType) {
        edit(closed) {
            param[params.next_loc_stage] = opened
            contentGroup = group
        }
    }
}
