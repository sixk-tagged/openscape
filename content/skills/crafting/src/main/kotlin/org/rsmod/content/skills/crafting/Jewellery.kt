package org.rsmod.content.skills.crafting

import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.script.onIfModalButton
import org.rsmod.api.script.onOpHeldU
import org.rsmod.api.script.onOpLocU
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.entity.Player
import org.rsmod.game.type.comp.ComponentType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.isType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

private var Player.makeQuantity: Int by intVarp(CraftingVarps.makexcrafting)

/** Gold and silver jewellery at a furnace (the two panels), and stringing amulets. */
class Jewellery : PluginScript() {
    override fun ScriptContext.startup() {
        val furnace = CraftingContent.furnace
        onOpLocU(furnace, CraftingObjs.gold_bar) {
            ifOpenMainModal(CraftingInterfaces.crafting_gold)
        }
        onOpLocU(furnace, CraftingObjs.silver_bar) {
            ifOpenMainModal(CraftingInterfaces.silver_crafting)
        }
        onOpLocU(furnace, CraftingObjs.tiara_mould) { tiara() }
        for ((button, recipe) in JewelleryRecipes.gold + JewelleryRecipes.silver) {
            onIfModalButton(button) {
                startCrafting(recipe, player.makeQuantity.coerceIn(1, MAX_AMOUNT))
            }
        }
        for (buttons in
            listOf(CraftingComponents.gold_quantity, CraftingComponents.silver_quantity)) {
            val (one, five, ten, x, all) = buttons
            onIfModalButton(one) { player.makeQuantity = 1 }
            onIfModalButton(five) { player.makeQuantity = 5 }
            onIfModalButton(ten) { player.makeQuantity = 10 }
            onIfModalButton(all) { player.makeQuantity = MAX_AMOUNT }
            onIfModalButton(x) { player.makeQuantity = countDialog().coerceIn(1, MAX_AMOUNT) }
        }
        onOpHeldU(CraftingObjs.ball_of_wool) { string(it.second) }
    }

    /** The tiara mould on a furnace makes the gold tiara, else opens the silver panel. */
    private fun ProtectedAccess.tiara() {
        if (CraftingObjs.gold_bar in inv) {
            startCrafting(JewelleryRecipes.goldTiara, 1, fromOp = true)
        } else {
            ifOpenMainModal(CraftingInterfaces.silver_crafting)
        }
    }

    private suspend fun ProtectedAccess.string(item: ObjType) {
        val recipes = JewelleryRecipes.stringing.filter { item.isType(it.inputs.first().obj) }
        if (recipes.isEmpty()) {
            mes(NOTHING_HAPPENS)
        } else {
            offer(Verb.STRING, "string", recipes)
        }
    }
}

/** Jewellery recipes (OSRS wiki levels and xp). Each piece takes three ticks at the furnace. */
object JewelleryRecipes : ObjReferences() {
    private class Row(
        val gem: String,
        val ring: Pair<Int, Int>,
        val necklace: Pair<Int, Int>,
        val amulet: Pair<Int, Int>,
        val bracelet: Pair<Int, Int>,
    )

    private fun jewel(
        product: String,
        level: Int,
        xp: Double,
        bar: ObjType,
        mould: String,
        gem: String? = null,
        makes: Int = 1,
    ) =
        CraftRecipe(
            find(product),
            level,
            xp,
            listOfNotNull(bar.x(), gem?.let { find(it).x() }),
            tools = listOf(find(mould)),
            makes = makes,
            ticks = 3,
            anim = CraftingSeqs.furnace,
        )

    private val goldRows =
        listOf(
            Row("gold", 5 to 15, 6 to 20, 8 to 30, 7 to 25),
            Row("sapphire", 20 to 40, 22 to 55, 24 to 65, 23 to 60),
            Row("emerald", 27 to 55, 29 to 60, 31 to 70, 30 to 65),
            Row("ruby", 34 to 70, 40 to 75, 50 to 85, 42 to 80),
            Row("diamond", 43 to 85, 56 to 90, 70 to 100, 58 to 95),
            Row("dragonstone", 55 to 100, 72 to 105, 80 to 150, 74 to 110),
            Row("onyx", 67 to 115, 82 to 120, 90 to 165, 84 to 125),
            Row("zenyte", 89 to 150, 92 to 165, 98 to 200, 95 to 180),
        )

    private val silverRows =
        listOf(
            Row("opal", 1 to 10, 16 to 35, 27 to 55, 22 to 45),
            Row("jade", 13 to 32, 25 to 54, 34 to 70, 29 to 60),
            Row("topaz", 16 to 35, 32 to 70, 45 to 80, 38 to 75),
        )

    /** The panel's component names say `dragon`, the items `dragonstone`. */
    private fun button(gem: String, piece: String) =
        gem.replace("dragonstone", "dragon") + "_" + piece

    private fun pieces(
        rows: List<Row>,
        bar: ObjType,
        buttons: Map<String, ComponentType>,
        gemItem: (String) -> String?,
        bracelet: (String) -> String,
    ): List<Pair<ComponentType, CraftRecipe>> =
        rows.flatMap { row ->
            val gem = gemItem(row.gem)
            listOf(
                    Triple("ring", row.ring, "${row.gem}_ring" to "ring_mould"),
                    Triple("necklace", row.necklace, "${row.gem}_necklace" to "necklace_mould"),
                    Triple("amulet", row.amulet, "unstrung_${row.gem}_amulet" to "amulet_mould"),
                    Triple("bracelet", row.bracelet, bracelet(row.gem) to "jewl_bracelet_mould"),
                )
                .map { (piece, lvlXp, item) ->
                    buttons.getValue(button(row.gem, piece)) to
                        jewel(
                            item.first,
                            lvlXp.first,
                            lvlXp.second.toDouble(),
                            bar,
                            item.second,
                            gem,
                        )
                }
        }

    private val goldBar = CraftingObjs.gold_bar
    private val silverBar = CraftingObjs.silver_bar

    val gold: List<Pair<ComponentType, CraftRecipe>> =
        pieces(
            goldRows,
            goldBar,
            CraftingComponents.gold,
            { if (it == "gold") null else it },
            { if (it == "zenyte") "zenyte_bracelet" else "jewl_${it}_bracelet" },
        ) +
            // The Slayer ring needs the Ring bling unlock, treated as met.
            (CraftingComponents.gold.getValue("slayer_ring") to
                jewel("slayer_ring_8", 75, 15.0, goldBar, "ring_mould", "slayer_gem"))

    val silver: List<Pair<ComponentType, CraftRecipe>> =
        pieces(
            silverRows,
            silverBar,
            CraftingComponents.silver,
            { if (it == "topaz") "red_topaz" else it },
            { "${it}_bracelet" },
        ) +
            listOf(
                    "holy_symbol" to
                        jewel("nostringstar", 16, 50.0, silverBar, "holy_symbol_mould"),
                    "unholy_symbol" to
                        jewel("nostringsnake", 17, 50.0, silverBar, "unholy_symbol_mould"),
                    "sickle" to jewel("silver_sickle", 18, 50.0, silverBar, "sickle_mould"),
                    "crossbow_bolt" to
                        jewel(
                            "xbows_crossbow_bolts_silver_unfeathered",
                            21,
                            50.0,
                            silverBar,
                            "xbows_silver_bolt_mould",
                            makes = 10,
                        ),
                    "tiara" to jewel("tiara", 23, 52.5, silverBar, "tiara_mould"),
                )
                .map { (name, recipe) -> CraftingComponents.silver.getValue(name) to recipe }

    // UNVERIFIED: the cache has no gold tiara button, so it is made by the mould on a furnace.
    val goldTiara = jewel("tiara_gold", 42, 35.0, goldBar, "tiara_mould")

    private fun string(from: String, to: String, what: String, level: Int = 1) =
        CraftRecipe(
            find(to),
            level,
            4.0,
            listOf(find(from).x(), CraftingObjs.ball_of_wool.x()),
            ticks = 2,
            message = "You put some string on your $what.",
        )

    /** Ball of wool on an unstrung amulet or symbol (UNVERIFIED: messages, no animation). */
    val stringing =
        listOf(
                "gold",
                "sapphire",
                "emerald",
                "ruby",
                "diamond",
                "dragonstone",
                "onyx",
                "opal",
                "jade",
                "topaz",
            )
            .map { string("unstrung_${it}_amulet", "strung_${it}_amulet", "amulet") } +
            string("unstrung_zenyte_amulet", "zenyte_amulet", "amulet") +
            string("nostringstar", "stringstar", "symbol") +
            string("nostringsnake", "stringsnake", "symbol") +
            string("hunting_rabbit_foot", "hunting_strung_rabbit_foot", "rabbit's foot", level = 37)
}
