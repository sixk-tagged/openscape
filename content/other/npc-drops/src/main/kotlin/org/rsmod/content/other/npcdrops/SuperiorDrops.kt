package org.rsmod.content.other.npcdrops

import kotlin.math.pow
import kotlin.math.roundToInt
import org.rsmod.api.death.Drop
import org.rsmod.api.death.DropTable
import org.rsmod.api.death.NpcDrops
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.obj.ObjReferences

/**
 * A superior Slayer monster's drops (wiki "Superior slayer monster"): three rolls on its lesser
 * monster's table (its always-drops once), and the two unique tables, each hit 1 time in `200 -
 * (slayerReq + 55)^2 / 125`: the first holds the imbued heart (1/8) and the dust and mist
 * battlestaves (7/16 each), the second the eternal gem (1/8). Out of 16 times that, the weights are
 * heart 2, staves 7 each and gem 2.
 */
internal fun superiorDrops(lesser: NpcDrops, slayerReq: Int): NpcDrops {
    val odds = 200.0 - (slayerReq + 55.0).pow(2) / 125
    val uniques =
        DropTable(
            (odds * 16).roundToInt(),
            with(superior_drop_objs) {
                listOf(drop(imbued_heart, 2), drop(dust_battlestaff, 7), drop(mist_battlestaff, 7))
            } + drop(superior_drop_objs.eternal_gem, 2),
        )
    val rolls = lesser.main?.let { main -> List(LESSER_ROLLS) { Drop(table = main) } }.orEmpty()
    return NpcDrops(always = lesser.always + rolls + Drop(table = uniques))
}

private const val LESSER_ROLLS = 3

object superior_drop_objs : ObjReferences() {
    val imbued_heart = find("imbued_heart")
    val dust_battlestaff = find("dust_battlestaff")
    val mist_battlestaff = find("mist_battlestaff")
    val eternal_gem = find("slayer_eternal_gem")
}

object superior_drop_npcs : NpcReferences() {
    val crawling_hand = find("superior_crawling_hand")
    val cave_crawler = find("superior_cave_crawler")
    val banshee = find("superior_banshee")
    val rockslug = find("superior_rockslug")
    val cockatrice = find("superior_cockatrice")
    val pyrefiend = find("superior_pyrefiend")
    val basilisk = find("superior_basilisk")
    val infernal_mage = find("superior_infernal_mage")
    val bloodveld = find("superior_bloodveld")
    val jelly = find("superior_jelly")
    val turoth = find("superior_turoth")
    val aberrant_spectre = find("superior_abberant_spectre")
    val kurask = find("superior_kurask")
    val gargoyle = find("superior_gargoyle")
    val nechryael = find("superior_nechryael")
    val abyssal_demon = find("superior_abyssal_demon")
}
