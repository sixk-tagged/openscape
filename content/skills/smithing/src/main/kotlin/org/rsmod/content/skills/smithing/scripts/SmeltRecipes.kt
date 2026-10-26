package org.rsmod.content.skills.smithing.scripts

import org.rsmod.content.skills.smithing.configs.SmithingObjs
import org.rsmod.game.type.obj.ObjType

/**
 * One furnace product: [ore] (and [secondOre]) plus [coal] heaps make [makes] of [product]. A
 * [tool] must be carried but is kept. Each smelt takes [ticks]; the furnace speeds are the OSRS
 * wiki's (4 ticks, bronze and gold 5, cannonballs 10).
 */
data class SmeltRecipe(
    val product: ObjType,
    val level: Int,
    val xp: Double,
    val ore: ObjType,
    val secondOre: ObjType? = null,
    val coal: Int = 0,
    val makes: Int = 1,
    val tool: ObjType? = null,
    val ticks: Int = 4,
)

object SmeltRecipes {
    val bronze =
        SmeltRecipe(
            SmithingObjs.bronze_bar,
            level = 1,
            xp = 6.2,
            ore = SmithingObjs.copper_ore,
            secondOre = SmithingObjs.tin_ore,
            ticks = 5,
        )
    val blurite = SmeltRecipe(SmithingObjs.blurite_bar, 13, 8.0, SmithingObjs.blurite_ore)
    val iron = SmeltRecipe(SmithingObjs.iron_bar, 15, 12.5, SmithingObjs.iron_ore)
    val silver = SmeltRecipe(SmithingObjs.silver_bar, 20, 13.7, SmithingObjs.silver_ore)
    val steel = SmeltRecipe(SmithingObjs.steel_bar, 30, 17.5, SmithingObjs.iron_ore, coal = 2)
    val gold = SmeltRecipe(SmithingObjs.gold_bar, 40, 22.5, SmithingObjs.gold_ore, ticks = 5)
    val lovakite =
        SmeltRecipe(SmithingObjs.lovakite_bar, 45, 20.0, SmithingObjs.lovakite_ore, coal = 2)
    val mithril =
        SmeltRecipe(SmithingObjs.mithril_bar, 50, 30.0, SmithingObjs.mithril_ore, coal = 4)
    val adamantite =
        SmeltRecipe(SmithingObjs.adamantite_bar, 70, 37.5, SmithingObjs.adamantite_ore, coal = 6)
    val runite = SmeltRecipe(SmithingObjs.runite_bar, 85, 50.0, SmithingObjs.runite_ore, coal = 8)

    val cannonballs =
        SmeltRecipe(
            SmithingObjs.cannonball,
            level = 35,
            xp = 25.6,
            ore = SmithingObjs.steel_bar,
            makes = 4,
            tool = SmithingObjs.ammo_mould,
            ticks = 10,
        )

    /** Chatbox order. */
    val all =
        listOf(bronze, blurite, iron, silver, steel, gold, lovakite, mithril, adamantite, runite) +
            cannonballs

    const val GOLD_XP_WITH_GAUNTLETS = 56.2
}
