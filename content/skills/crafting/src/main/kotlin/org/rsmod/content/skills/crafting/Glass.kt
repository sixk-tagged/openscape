package org.rsmod.content.skills.crafting

import org.rsmod.api.script.onOpHeldU
import org.rsmod.api.script.onOpLocU
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** Molten glass at a furnace, and glassblowing with a pipe. */
class Glass : PluginScript() {
    override fun ScriptContext.startup() {
        for (ingredient in listOf(CraftingObjs.bucket_sand, CraftingObjs.soda_ash)) {
            onOpLocU(CraftingContent.furnace, ingredient) {
                offer(Verb.MAKE, "make", listOf(GlassRecipes.moltenGlass))
            }
        }
        onOpHeldU(CraftingObjs.glassblowing_pipe, CraftingObjs.molten_glass) {
            offer(Verb.MAKE, "make", GlassRecipes.blowing)
        }
        for (pit in CraftingLocs.sandpits) {
            onOpLocU(pit, CraftingObjs.bucket_empty) {
                startCrafting(GlassRecipes.fillBucket, MAX_AMOUNT)
            }
        }
        // More specific than the Cooking module's catch-all for fires and ranges.
        for (heat in CraftingLocs.heat_sources) {
            for (burnt in GlassRecipes.sodaAsh) {
                onOpLocU(heat, burnt.inputs.first().obj) { startCrafting(burnt, MAX_AMOUNT) }
            }
        }
    }
}

/** Glass (OSRS wiki levels and xp). */
object GlassRecipes : ObjReferences() {
    /** Sand and soda ash; the bucket comes back empty (UNVERIFIED: the wiki recipe omits it). */
    val moltenGlass =
        CraftRecipe(
            CraftingObjs.molten_glass,
            1,
            20.0,
            listOf(CraftingObjs.bucket_sand.x(), CraftingObjs.soda_ash.x()),
            returns = listOf(CraftingObjs.bucket_empty.x()),
            ticks = 2,
            anim = CraftingSeqs.furnace,
            message = "You heat the sand and soda ash in the furnace to make glass.",
        )

    val fillBucket =
        CraftRecipe(
            CraftingObjs.bucket_sand,
            1,
            0.0,
            listOf(CraftingObjs.bucket_empty.x()),
            ticks = 2,
            anim = CraftingSeqs.fill_bucket,
            message = "You fill the bucket with sand.",
        )

    /** Seaweed burnt on a fire or range; giant seaweed gives six. */
    val sodaAsh =
        listOf("seaweed" to 1, "giant_seaweed" to 6, "dorgesh_swamp_weed" to 1).map { (weed, ash) ->
            CraftRecipe(
                CraftingObjs.soda_ash,
                1,
                0.0,
                listOf(find(weed).x()),
                makes = ash,
                ticks = 4,
                anim = CraftingSeqs.cooking,
                message = "You burn the ${weed.replace('_', ' ')} into soda ash.",
            )
        }

    private fun blow(product: String, level: Int, xp: Double) =
        CraftRecipe(
            find(product),
            level,
            xp,
            listOf(CraftingObjs.molten_glass.x()),
            tools = listOf(CraftingObjs.glassblowing_pipe),
            anim = CraftingSeqs.glassblowing,
        )

    val blowing =
        listOf(
            blow("beer_glass", 1, 17.5),
            blow("candle_lantern_empty", 4, 19.0),
            blow("oil_lamp_empty", 12, 25.0),
            blow("vial_empty", 33, 35.0),
            blow("fishbowl_empty", 42, 42.5),
            blow("stafforb", 46, 52.5),
            blow("bullseye_lantern_lens", 49, 55.0),
            blow("dorgesh_lightbulb_nofilament", 87, 70.0),
        )

    val combos =
        listOf(
            Combo(
                find("oil_lantern_frame"),
                find("oil_lamp_empty"),
                CraftRecipe(
                    find("oil_lantern_empty"),
                    26,
                    50.0,
                    listOf(find("oil_lantern_frame").x(), find("oil_lamp_empty").x()),
                    ticks = 2,
                ),
            ),
            Combo(
                find("dorgesh_lightbulb_nofilament"),
                find("dorgesh_wire"),
                CraftRecipe(
                    find("dorgesh_light_bulb"),
                    87,
                    104.0,
                    listOf(find("dorgesh_lightbulb_nofilament").x(), find("dorgesh_wire").x()),
                    ticks = 1,
                ),
            ),
        )
}
