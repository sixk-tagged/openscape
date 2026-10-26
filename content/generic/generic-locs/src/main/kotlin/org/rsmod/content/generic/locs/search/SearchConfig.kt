package org.rsmod.content.generic.locs.search

import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.params
import org.rsmod.api.type.editors.loc.LocEditor
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.game.type.loc.LocType

internal typealias search_locs = SearchLocs

internal object SearchLocs : LocReferences() {
    val crate3 = find("crate3")
    val crate2_old = find("crate2_old")
    val crate3_old = find("crate3_old")
    val crate = find("crate")
    val qip_cook_crate_stacked = find("qip_cook_crate_stacked")
    val sacks = find("sacks")
    val boxes = find("boxes")
    val aide_small_crates = find("aide_small_crates")
    val dttd_crate_small = find("dttd_machine_parts_crate_small_full_multi")
    val dttd_crate_lid = find("dttd_machine_crate_open_with_lid")
    val haystack = find("haystack")
    val haystack2 = find("haystack2")
    val haystack3 = find("haystack3")
    val haybail = find("haybail")
}

internal object SearchLocEdits : LocEditor() {
    init {
        crate(search_locs.crate3)
        crate(search_locs.crate2_old)
        crate(search_locs.crate3_old)
        crate(search_locs.crate)
        crate(search_locs.qip_cook_crate_stacked)
        crate(search_locs.aide_small_crates)
        crate(search_locs.dttd_crate_small)
        crate(search_locs.dttd_crate_lid)

        // No game_message param: these fall back to SearchConstants.DEFAULT.
        hay(search_locs.haystack)
        hay(search_locs.haystack2)
        hay(search_locs.haystack3)
        hay(search_locs.haybail)

        sack(search_locs.sacks)

        boxes(search_locs.boxes)
    }

    private fun crate(type: LocType) {
        edit(type) {
            param[params.game_message] = SearchConstants.EMPTY_CRATE
            contentGroup = content.empty_crate
        }
    }

    private fun hay(type: LocType) {
        edit(type) { contentGroup = content.empty_sacks }
    }

    private fun sack(type: LocType) {
        edit(type) {
            param[params.game_message] = SearchConstants.EMPTY_SACKS
            contentGroup = content.empty_sacks
        }
    }

    private fun boxes(type: LocType) {
        edit(type) {
            param[params.game_message] = SearchConstants.EMPTY_BOXES
            contentGroup = content.empty_boxes
        }
    }
}

internal object SearchConstants {
    // You search the crates but find nothing of value. <- one type of box in varrock castle
    const val EMPTY_CRATE = "You search the crate but find nothing."
    const val EMPTY_CHEST = "You search the chest but find nothing."
    const val EMPTY_CRATE2 = "The crate is empty."
    const val EMPTY_BOXES = "There is nothing interesting in these boxes."
    const val EMPTY_SACKS = "There is nothing interesting in these sacks."

    const val DEFAULT = "You find nothing of interest."
}
