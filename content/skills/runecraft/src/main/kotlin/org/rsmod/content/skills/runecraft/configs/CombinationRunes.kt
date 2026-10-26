package org.rsmod.content.skills.runecraft.configs

import org.rsmod.game.type.obj.ObjType

/**
 * Combination rune made at [altar] from pure essence, runes of the [other] altar's element and one
 * of that element's talismans. [xp] is per rune made; each rune has a 50% chance to be made.
 */
class ComboRecipe(
    val altar: Altar,
    val other: Altar,
    val rune: ObjType,
    val level: Int,
    val xp: Double,
) {
    val otherRune: ObjType
        get() = other.rune

    val otherTalisman: ObjType
        get() = checkNotNull(other.binding).talisman
}

object CombinationRunes {
    private val objs = RunecraftObjs

    val all =
        listOf(
            ComboRecipe(Altars.air, Altars.water, objs.mist_rune, 6, 8.0),
            ComboRecipe(Altars.water, Altars.air, objs.mist_rune, 6, 8.5),
            ComboRecipe(Altars.air, Altars.earth, objs.dust_rune, 10, 8.3),
            ComboRecipe(Altars.earth, Altars.air, objs.dust_rune, 10, 9.0),
            ComboRecipe(Altars.water, Altars.earth, objs.mud_rune, 13, 9.3),
            ComboRecipe(Altars.earth, Altars.water, objs.mud_rune, 13, 9.5),
            ComboRecipe(Altars.air, Altars.fire, objs.smoke_rune, 15, 8.5),
            ComboRecipe(Altars.fire, Altars.air, objs.smoke_rune, 15, 9.5),
            ComboRecipe(Altars.water, Altars.fire, objs.steam_rune, 19, 9.3),
            ComboRecipe(Altars.fire, Altars.water, objs.steam_rune, 19, 10.0),
            ComboRecipe(Altars.earth, Altars.fire, objs.lava_rune, 23, 10.0),
            ComboRecipe(Altars.fire, Altars.earth, objs.lava_rune, 23, 10.5),
        )

    /** Binding necklace charges, spent one per craft. */
    const val NECKLACE_CHARGES = 16
}
