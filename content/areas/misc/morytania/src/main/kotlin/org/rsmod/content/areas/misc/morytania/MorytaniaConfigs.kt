@file:Suppress("SpellCheckingInspection")

package org.rsmod.content.areas.misc.morytania

import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.params
import org.rsmod.api.type.builders.map.npc.MapNpcSpawnBuilder
import org.rsmod.api.type.editors.loc.LocEditor
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.game.type.content.ContentGroupType
import org.rsmod.game.type.loc.LocType

object MorytaniaLocs : LocReferences() {
    // Paterdomus, the way into Morytania (Priest in Peril counts as done).
    val temple_door_left = find("priestperiltempledoorl")
    val temple_door_right = find("priestperiltempledoorr")
    val mausoleum_gate_west = find("pip_underground_door1")
    val mausoleum_gate_east = find("pip_underground_door2")
    val holy_barrier = find("pip_underground_wall_side_withportal")
    val east_trapdoor = find("pipeastsidetrapdoor")
    val east_trapdoor_open = find("pipeastsidetrapdoor_open")

    // The Slayer Tower.
    val tower_door = find("slayertower_door")
    val tower_door_mirror = find("slayertower_door_mirror")
    val tower_door_arch = find("slayertower_door_arch")
    val tower_door_arch_mirror = find("slayertower_door_arch_mirror")
    val small_door = find("slayertower_small_door")
    val small_door_open = find("slayertower_small_door_open")
    val small_door_left = find("slayertower_small_door_l")
    val small_door_left_open = find("slayertower_small_door_open_l")
    val small_door_right = find("slayertower_small_door_r")
    val small_door_right_open = find("slayertower_small_door_open_r")
    val stairs_1 = find("slayer_stairs_lv1")
    val stairs_1_top = find("slayer_stairs_lv1_top")
    val stairs_2 = find("slayer_stairs_lv2")
    val stairs_2_top = find("slayer_stairs_lv2_top")
    val basement_ladder_top = find("slayer_tower_dungeon_laddertop")
    val basement_ladder_bottom = find("slayer_tower_dungeon_ladderbottom")
    val chain_bottom = find("slayertower_sc_chainbottom")
    val chain_top = find("slayertower_sc_chaintop")
    val window = find("slayertower_window_shortcut_through")
    val ivy = find("slayertower_shortcut_up")
    val ivy_window = find("slayertower_window_shortcut_down")

    // Also stands outside the Fremennik Slayer Dungeon.
    val danger_sign = find("slayer_dangersign")
}

/** The Slayer Tower's small doors open like any other door (they have open versions). */
internal object MorytaniaLocEditor : LocEditor() {
    init {
        with(MorytaniaLocs) {
            door(small_door, small_door_open, content.closed_single_door)
            door(small_door_open, small_door, content.opened_single_door)
            door(small_door_left, small_door_left_open, content.closed_left_door)
            door(small_door_left_open, small_door_left, content.opened_left_door)
            door(small_door_right, small_door_right_open, content.closed_right_door)
            door(small_door_right_open, small_door_right, content.opened_right_door)
        }
    }

    private fun door(from: LocType, into: LocType, group: ContentGroupType) {
        edit(from) {
            param[params.next_loc_stage] = into
            contentGroup = group
        }
    }
}

object MorytaniaNpcSpawns : MapNpcSpawnBuilder() {
    override fun onPackMapTask() {
        resourceFile<MorytaniaLocScript>("npcs.toml")
    }
}
