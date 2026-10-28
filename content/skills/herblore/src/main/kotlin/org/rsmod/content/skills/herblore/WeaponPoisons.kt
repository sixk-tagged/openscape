package org.rsmod.content.skills.herblore

import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.type.obj.ObjType

/** [vial] used on [weapon] makes [poisoned]; a vial does up to [batch] stackable pieces. */
class PoisonedWeapon(val vial: ObjType, val weapon: ObjType, val poisoned: ObjType, val batch: Int)

/** Weapon poison, poison+ and poison++ on ammo and the weapons the wiki lists. */
object WeaponPoisons : ObjReferences() {
    private const val AMMO_BATCH = 5

    /** The vial and what its poisoned variants add to a cache name (`bronze_dagger_p+`). */
    private val tiers =
        listOf(
            HerbloreObjs.weapon_poison to "",
            HerbloreObjs.weapon_poison_plus to "+",
            HerbloreObjs.weapon_poison_plus_plus to "++",
        )

    private val metals = listOf("bronze", "iron", "steel", "mithril", "adamant", "rune", "dragon")

    /** Weapons whose poisoned form is `<name>_p`, with how many one vial does. */
    private val named =
        (metals + "amethyst").map { "${it}_arrow" to AMMO_BATCH } +
            (metals + listOf("amethyst", "black")).map { "${it}_dart" to AMMO_BATCH } +
            (metals + "amethyst").map { "${it}_javelin" to AMMO_BATCH } +
            (metals + "black").map { "${it}_knife" to AMMO_BATCH } +
            (metals + listOf("black", "white")).map { "${it}_dagger" to 1 } +
            (metals + "black").map { "${it}_spear" to 1 }

    /** Bolts and their poisoned names, which the cache spells a few different ways. */
    private val bolts =
        listOf("bolt" to "poison_bolt", "dragon_bolts" to "dragon_bolts_p") +
            listOf("blurite", "iron", "steel", "mithril", "adamantite", "runite", "silver").map {
                "xbows_crossbow_bolts_$it" to "xbows_crossbow_bolts_${it}_poisoned"
            }

    val all: List<PoisonedWeapon> =
        tiers.flatMap { (vial, plus) ->
            named.map { (weapon, batch) ->
                PoisonedWeapon(vial, find(weapon), find("${weapon}_p$plus"), batch)
            } +
                bolts.map { (bolt, poisoned) ->
                    PoisonedWeapon(vial, find(bolt), find("$poisoned$plus"), AMMO_BATCH)
                }
        }
}
