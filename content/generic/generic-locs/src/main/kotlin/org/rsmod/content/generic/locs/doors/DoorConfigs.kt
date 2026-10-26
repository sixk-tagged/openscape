package org.rsmod.content.generic.locs.doors

import org.rsmod.api.config.refs.content
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.synths
import org.rsmod.api.type.editors.loc.LocEditor
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.game.type.loc.LocType

internal typealias door_locs = DoorLocs

internal object DoorConstants {
    /** The (cycle) duration that a door remains changed before reverting to its original state. */
    const val DURATION = 500
}

internal object DoorLocs : LocReferences() {
    val door_opened = find("poordooropen", 5065226005806317467)
    val door_closed = find("poordoor", 1827052337340400340)
    val poordoor_closed = find("poordoor_m", 1827052337340455181)
    val poordoor_opened = find("poordooropen_m", 5065226005806372308)
    val poshdoor_closed = find("poshdoor", 1827052337340400345)
    val poshdoor_opened = find("poshdooropen", 5065226005806317472)
    val door_left_closed = find("castledoubledoorl", 8682664997836417386)
    val door_left_opened = find("opencastledoubledoorl", 2697466629447558705)
    val door_right_closed = find("castledoubledoorr", 8682664997836417389)
    val door_right_opened = find("opencastledoubledoorr", 2697466629447558708)
    val elfdoor_closed = find("elfdoor")
    val elfdoor_opened = find("elfdooropen")
    val castle_door = find("hundred_lumbridge_door")
    val castle_door_left = find("hundred_lumbridge_doubledoorl")
    val castle_door_right = find("hundred_lumbridge_doubledoorr")
    val fred_door_closed = find("qip_sheep_shearer_poordoor")
    val fred_door_opened = find("qip_sheep_shearer_poordooropen")
    val zanaris_door = find("zanarisdoor")
    val zanaris_door_opened = find("zanarisdoor_open")
    val farming_shed_door = find("farming_shed_poordoor")
    val farming_shed_door_opened = find("farming_shed_poordoor_inactive")
}

internal object DoorLocEdits : LocEditor() {
    init {
        edit(door_locs.door_opened) {
            param[params.next_loc_stage] = door_locs.door_closed
            contentGroup = content.opened_single_door
        }

        edit(door_locs.door_closed) {
            param[params.next_loc_stage] = door_locs.door_opened
            contentGroup = content.closed_single_door
        }

        edit(door_locs.poordoor_opened) {
            param[params.next_loc_stage] = door_locs.poordoor_closed
            contentGroup = content.opened_single_door
        }

        edit(door_locs.poordoor_closed) {
            param[params.next_loc_stage] = door_locs.poordoor_opened
            contentGroup = content.closed_single_door
        }

        edit(door_locs.poshdoor_opened) {
            param[params.next_loc_stage] = door_locs.poshdoor_closed
            param[params.closesound] = synths.nicedoor_close
            contentGroup = content.opened_single_door
        }

        edit(door_locs.poshdoor_closed) {
            param[params.next_loc_stage] = door_locs.poshdoor_opened
            param[params.opensound] = synths.nicedoor_open
            contentGroup = content.closed_single_door
        }

        edit(door_locs.door_left_closed) {
            param[params.next_loc_stage] = door_locs.door_left_opened
            contentGroup = content.closed_left_door
        }

        edit(door_locs.door_left_opened) {
            param[params.next_loc_stage] = door_locs.door_left_closed
            contentGroup = content.opened_left_door
        }

        edit(door_locs.door_right_closed) {
            param[params.next_loc_stage] = door_locs.door_right_opened
            contentGroup = content.closed_right_door
        }

        edit(door_locs.door_right_opened) {
            param[params.next_loc_stage] = door_locs.door_right_closed
            contentGroup = content.opened_right_door
        }

        // Same models as the doors they open into (checked in the rev 233 cache).
        single(door_locs.elfdoor_closed, door_locs.elfdoor_opened)
        single(door_locs.fred_door_closed, door_locs.fred_door_opened)
        closedOnly(door_locs.castle_door, door_locs.elfdoor_opened)
        closedOnly(door_locs.zanaris_door, door_locs.zanaris_door_opened)
        closedOnly(door_locs.farming_shed_door, door_locs.farming_shed_door_opened)

        edit(door_locs.castle_door_left) {
            param[params.next_loc_stage] = door_locs.door_left_opened
            contentGroup = content.closed_left_door
        }

        edit(door_locs.castle_door_right) {
            param[params.next_loc_stage] = door_locs.door_right_opened
            contentGroup = content.closed_right_door
        }
    }

    private fun single(closed: LocType, opened: LocType) {
        closedOnly(closed, opened)
        edit(opened) {
            param[params.next_loc_stage] = closed
            contentGroup = content.opened_single_door
        }
    }

    // The opened type has no ops; the door reverts on its own after DoorConstants.DURATION.
    private fun closedOnly(closed: LocType, opened: LocType) {
        edit(closed) {
            param[params.next_loc_stage] = opened
            contentGroup = content.closed_single_door
        }
    }
}
