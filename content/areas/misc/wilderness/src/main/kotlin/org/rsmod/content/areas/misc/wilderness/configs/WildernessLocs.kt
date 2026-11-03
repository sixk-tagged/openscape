@file:Suppress("unused", "SpellCheckingInspection")

package org.rsmod.content.areas.misc.wilderness.configs

import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.seqs
import org.rsmod.api.type.editors.loc.LocEditor
import org.rsmod.api.type.refs.content.ContentReferences
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.queue.QueueReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.api.type.refs.spot.SpotanimReferences

typealias wilderness_locs = WildernessLocs

object WildernessContent : ContentReferences() {
    val furnace = find("smithing_furnace")
}

object WildernessSeqs : SeqReferences() {
    val lever = find("human_leverdown")
    val teleport = find("human_castteleport")
    val pray = find("human_pray")
}

/** The obelisk's charge time, on a player queue nothing else uses. */
object WildernessQueues : QueueReferences() {
    val obelisk = find("generic_queue10")
}

object WildernessSpots : SpotanimReferences() {
    val teleport = find("teleport_casting")
}

object WildernessLocs : LocReferences() {
    // Ladders, stairs and doors that join the generic groups.
    val ladder = find("wild_ladder")
    val ladder_top = find("wild_laddertop")
    val ladder_middle = find("wild_ladder_middle")
    val dungeon_ladder_top = find("wild_laddertop_dungeon")
    val northern_dungeon_ladder = find("ladder_outside_to_underground")
    val spiral_stairs = find("wild6_spiralstairs")
    val spiral_stairs_middle = find("wild6_spiralstairs_middle")
    val spiral_stairs_top = find("wild6_spiralstairstop")
    val fortress_door_left = find("wild_doubledoor_l")
    val fortress_door_right = find("wild_doubledoor_r")
    val fortress_door_left_open = find("wild_doubledoor_open_l")
    val fortress_door_right_open = find("wild_doubledoor_open_r")
    val resource_furnace = find("wilderness_resource_furnace")
    val sack = find("wild_sack")
    val dungeon_stairs_up = find("stairs_from_cellar")

    // The Lava Maze's two ways down, and the matching ways up (6400 tiles south on the map).
    val lava_maze_ladders_down =
        listOf(find("wildymirrorladdertop1"), find("wildymirrorladdertop2"))
    val lava_maze_ladders_up = listOf(find("wildymirrorladder1"), find("wildymirrorladder2"))

    // The Ferox Enclave and the Mage Arena bank.
    val ferox_barriers = listOf(find("wildy_hub_entry_barrier"), find("wildy_hub_entry_barrier_m"))
    val ferox_pool = find("wildy_hub_pool")
    val ferox_altar = find("wildy_hub_altar")
    val ferox_stairs_up = find("wildy_hub_stairs")
    val ferox_stairs_down = find("wildy_hub_stairs_top")
    val ferox_bank_chest = find("fairy_chest_bank")
    val mage_bank_chest = find("blast_bank_chest")
    val mage_bank_deposit_box = find("bank_deposit_box")

    // Levers.
    val edgeville_lever = find("edgeville_wildy_lever")
    val ardougne_lever = find("wildinlever")
    val keep_lever = find("wildoutlever_default")
    val bank_lever_in = find("magearena_lever_to_cellar")
    val bank_lever_out = find("magearena_lever_from_cellar")

    // The obelisks: four pillars each, then the glowing form.
    val obelisks =
        listOf(
            find("wilderness_portal_stone_0"),
            find("wilderness_portal_stone_1"),
            find("wilderness_portal_stone_2"),
            find("wilderness_portal_stone_3"),
            find("wilderness_portal_stone_4"),
            find("wilderness_portal_stone_5"),
        )
    val obelisk_glowing = find("wilderness_portal_stone_glowing")

    val resource_gate = find("wilderness_resource_gate")
    val chaos_altar = find("chaosaltar")
    val rogue_chest = find("wilderness_rogue_chest")
    val rogue_chest_open = find("wilderness_rogue_chest_open")
    val muddy_chest = find("muddy_chestclosed")
    val muddy_chest_open = find("muddy_chestopen")

    val web = find("bigweb_slashable")
    val web_slashed = find("bigweb_slashed")
}

internal object WildernessLocEditor : LocEditor() {
    init {
        // The fortress's double doors close again by themselves.
        doubleDoor(
            wilderness_locs.fortress_door_left,
            wilderness_locs.fortress_door_left_open,
            content.closed_left_door,
        )
        doubleDoor(
            wilderness_locs.fortress_door_right,
            wilderness_locs.fortress_door_right_open,
            content.closed_right_door,
        )

        edit(wilderness_locs.ladder) { contentGroup = content.ladder_up }
        edit(wilderness_locs.ladder_top) {
            param[params.climb_anim] = seqs.human_pickupfloor
            contentGroup = content.ladder_down
        }
        edit(wilderness_locs.ladder_middle) { contentGroup = content.ladder_option }
        // The ladders into the dungeon sit 6400 tiles south on the map, like the generic ones.
        for (down in
            listOf(wilderness_locs.dungeon_ladder_top, wilderness_locs.northern_dungeon_ladder)) {
            edit(down) {
                param[params.climb_anim] = seqs.human_pickupfloor
                contentGroup = content.dungeonladder_down
            }
        }
        for (down in wilderness_locs.lava_maze_ladders_down) {
            edit(down) {
                param[params.climb_anim] = seqs.human_pickupfloor
                contentGroup = content.dungeonladder_down
            }
        }
        for (up in wilderness_locs.lava_maze_ladders_up) {
            edit(up) { contentGroup = content.dungeonladder_up }
        }
        edit(wilderness_locs.spiral_stairs) { contentGroup = content.spiralstaircase_up }
        edit(wilderness_locs.spiral_stairs_middle) { contentGroup = content.spiralstaircase_option }
        edit(wilderness_locs.spiral_stairs_top) { contentGroup = content.spiralstaircase_down }

        edit(wilderness_locs.resource_furnace) { contentGroup = WildernessContent.furnace }
        edit(wilderness_locs.sack) { contentGroup = content.empty_sacks }

        edit(wilderness_locs.ferox_bank_chest) { contentGroup = content.bank_chest }
        edit(wilderness_locs.mage_bank_chest) { contentGroup = content.bank_chest }
        edit(wilderness_locs.mage_bank_deposit_box) { contentGroup = content.bank_deposit_box }
    }

    private fun doubleDoor(
        closed: org.rsmod.game.type.loc.LocType,
        opened: org.rsmod.game.type.loc.LocType,
        group: org.rsmod.game.type.content.ContentGroupType,
    ) {
        edit(closed) {
            param[params.next_loc_stage] = opened
            contentGroup = group
        }
    }
}
