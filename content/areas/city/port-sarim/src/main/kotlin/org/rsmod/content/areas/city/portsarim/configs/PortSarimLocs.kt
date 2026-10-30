@file:Suppress("unused", "SpellCheckingInspection")

package org.rsmod.content.areas.city.portsarim.configs

import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.seqs
import org.rsmod.api.type.editors.loc.LocEditor
import org.rsmod.api.type.refs.loc.LocReferences

typealias port_sarim_locs = PortSarimLocs

object PortSarimLocs : LocReferences() {
    // Doors that check something before letting a player through.
    val wydin_door = find("wydindoor")
    val grum_door = find("jewellersdoor")
    val guild_door = find("craftingguilddoor")
    val cell_door = find("port_sarim_jaildoor")
    val cell_bars = find("prisonbarsdoor_locked")

    // Banks and the guild's chest.
    val sarim_deposit_box = find("sarim_deposit_box")
    val guild_deposit_box = find("diary_guild_deposit_box")
    val guild_chest = find("diary_guild_bankchest")

    // Crates that give food away, and the banana plantation.
    val banana_crate = find("grocerycrate")
    val cabbage_crate = find("cabbage_crate")
    val potato_crate = find("potato_crate")
    val chicken_crate = find("chicken_crate")
    val job_crate = find("bananacrate")
    val banana_trees =
        listOf(
            find("bananatreefull"),
            find("bananatreefour"),
            find("bananatreethree"),
            find("bananatreetwo"),
            find("bananatreeone"),
        )
    val empty_banana_tree = find("bananatreeempty")

    // The volcano and its dungeon.
    val volcano_entrance = find("volcano_entrance")
    val volcano_rope = find("climbing_rope2")

    // Odds and ends.
    val mudskipper_sign = find("mudskipper_sign")
    val rimmington_spade = find("rimmington_mining_spade")
    val sand_pile = find("sand2")

    // Ladders, trapdoors and stairs that join the generic content groups.
    val ladders_up =
        listOf(
            find("prison_ladder_up"),
            find("ship_ladder"),
            find("shipladder_angled"),
            find("ship_ladder_no_approach"),
            find("jewellersladder"),
        )
    val ladders_down =
        listOf(
            find("prison_laddertop"),
            find("prison_trapdoor"),
            find("ship_laddertop"),
            find("shipladder_top_angled"),
            find("ship_laddertop_no_approach"),
            find("ship_laddertop_noclimb"),
            find("dragonshipladdertop"),
        )
    val spiral_stairs = find("sarim_spiralstairs")

    // Search-only scenery.
    val crates = listOf(find("sarim_crate"), find("sarim_crate3"))
    val boxes = find("sarim_boxes")
    val prison_bucket = find("prison_bucket")
    val bookcases =
        listOf(
            find("sarim_bookcase2_offset"),
            find("sarim_bookcase2_offset_northfacing"),
            find("sarim_bookcase2_offset_westfacing"),
        )
}

internal object PortSarimLocEditor : LocEditor() {
    init {
        for (ladder in port_sarim_locs.ladders_up) {
            edit(ladder) { contentGroup = content.ladder_up }
        }
        for (ladder in port_sarim_locs.ladders_down) {
            edit(ladder) {
                param[params.climb_anim] = seqs.human_pickupfloor
                contentGroup = content.ladder_down
            }
        }
        edit(port_sarim_locs.spiral_stairs) { contentGroup = content.spiralstaircase_up }

        for (crate in port_sarim_locs.crates) {
            edit(crate) {
                param[params.game_message] = "You search the crate but find nothing."
                contentGroup = content.empty_crate
            }
        }
        edit(port_sarim_locs.prison_bucket) {
            param[params.game_message] = "You search the bucket but find nothing."
            contentGroup = content.empty_crate
        }
        edit(port_sarim_locs.boxes) {
            param[params.game_message] = "There is nothing interesting in these boxes."
            contentGroup = content.empty_boxes
        }
        for (bookcase in port_sarim_locs.bookcases) {
            edit(bookcase) { contentGroup = content.bookcase }
        }

        edit(port_sarim_locs.sarim_deposit_box) { contentGroup = content.bank_deposit_box }
        edit(port_sarim_locs.guild_deposit_box) { contentGroup = content.bank_deposit_box }
    }
}
