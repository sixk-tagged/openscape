package org.rsmod.content.skills.crafting

import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpHeldU
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.isType
import org.rsmod.game.type.seq.SeqType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** Needle (or costume needle) and thread, used on the hide to sew. */
class Needlework : PluginScript() {
    override fun ScriptContext.startup() {
        for (tool in
            listOf(CraftingObjs.needle, CraftingObjs.costume_needle, CraftingObjs.thread)) {
            onOpHeldU(tool) { sew(it.second) }
        }
    }

    private suspend fun ProtectedAccess.sew(material: ObjType) {
        val recipes = NeedleRecipes.sewnFrom(material)
        if (recipes.isEmpty()) {
            mes(NOTHING_HAPPENS)
        } else {
            offer(Verb.MAKE, "make", recipes)
        }
    }
}

/**
 * Everything made with a needle and thread (OSRS wiki levels and xp), then the hammer-and-nail
 * shields.
 */
object NeedleRecipes : ObjReferences() {
    private val leather = find("leather")
    private val hardLeather = find("hard_leather")
    private val greenLeather = find("dragon_leather")
    private val blueLeather = find("dragon_leather_blue")
    private val redLeather = find("dragon_leather_red")
    private val blackLeather = find("dragon_leather_black")
    private val snakeskin = find("village_snake_skin")
    private val hammer = CraftingObjs.hammer

    private fun sew(
        product: String,
        level: Int,
        xp: Double,
        vararg inputs: Ingredient,
        ticks: Int = 3,
    ) =
        CraftRecipe(
            find(product),
            level,
            xp,
            inputs.toList(),
            thread = true,
            ticks = ticks,
            anim = CraftingSeqs.needle,
        )

    private val softLeather =
        listOf(
            sew("leather_gloves", 1, 13.8, leather.x()),
            // UNVERIFIED: the wiki lists 16.3 here and 16.2 on the Leather page; in game it is
            // 16.25.
            sew("leather_boots", 7, 16.25, leather.x()),
            sew("leather_cowl", 9, 18.5, leather.x()),
            sew("leather_vambraces", 11, 22.0, leather.x()),
            sew("leather_armour", 14, 25.0, leather.x()),
            sew("leather_chaps", 18, 27.0, leather.x()),
            sew("coif", 38, 37.0, leather.x()),
            sew("hardleather_body", 28, 35.0, hardLeather.x(), ticks = 2),
        )

    private class Dhide(
        val leather: ObjType,
        val vambraces: Triple<String, Int, Double>,
        val chaps: Triple<String, Int, Double>,
        val body: Triple<String, Int, Double>,
    )

    private val dragonhide =
        listOf(
            Dhide(
                greenLeather,
                Triple("dragon_vambraces", 57, 62.0),
                Triple("dragonhide_chaps", 60, 124.0),
                Triple("dragonhide_body", 63, 186.0),
            ),
            Dhide(
                blueLeather,
                Triple("blue_dragon_vambraces", 66, 70.0),
                Triple("blue_dragonhide_chaps", 68, 140.0),
                Triple("blue_dragonhide_body", 71, 210.0),
            ),
            Dhide(
                redLeather,
                Triple("red_dragon_vambraces", 73, 78.0),
                Triple("red_dragonhide_chaps", 75, 156.0),
                Triple("red_dragonhide_body", 77, 234.0),
            ),
            Dhide(
                blackLeather,
                Triple("black_dragon_vambraces", 79, 86.0),
                Triple("black_dragonhide_chaps", 82, 172.0),
                Triple("black_dragonhide_body", 84, 258.0),
            ),
        )

    private val dragonhideArmour =
        dragonhide.flatMap { hide ->
            listOf(hide.vambraces to 1, hide.chaps to 2, hide.body to 3).map { (piece, hides) ->
                sew(piece.first, piece.second, piece.third, hide.leather.x(hides))
            }
        }

    private val snakeskinArmour =
        listOf(
            sew("snakeskin_boots", 45, 30.0, snakeskin.x(6)),
            sew("snakeskin_vambraces", 47, 35.0, snakeskin.x(8)),
            sew("snakeskin_bandana", 48, 45.0, snakeskin.x(5)),
            sew("snakeskin_chaps", 51, 50.0, snakeskin.x(12)),
            sew("snakeskin_body", 53, 55.0, snakeskin.x(15)),
        )

    private val yak = find("yak_hide_cured")
    private val fabric = find("xeric_fabric")
    private val bark = find("hollow_bark")
    private val cloth = find("fine_cloth")
    private val huey = find("huey_hide")
    private val mixedBase = find("hg_mixedhide_base")

    private val otherArmour =
        listOf(
            sew("yak_hide_armour_greaves", 43, 32.0, yak.x()),
            sew("yak_hide_armour_body", 46, 32.0, yak.x(2)),
            sew("xeric_hat", 14, 66.0, fabric.x(3)),
            sew("xeric_robe", 17, 88.0, fabric.x(4)),
            sew("xeric_top", 22, 110.0, fabric.x(5)),
            sew("splitbark_gauntlets", 60, 62.0, bark.x(1), cloth.x(1)),
            sew("splitbark_greaves", 60, 62.0, bark.x(1), cloth.x(1)),
            sew("splitbark_helm", 61, 124.0, bark.x(2), cloth.x(2)),
            sew("splitbark_legs", 62, 186.0, bark.x(3), cloth.x(3)),
            sew("splitbark_body", 62, 248.0, bark.x(4), cloth.x(4)),
            sew("huey_vambraces", 76, 95.0, huey.x(1)),
            sew("huey_coif", 76, 190.0, huey.x(2)),
            sew("huey_chaps", 77, 190.0, huey.x(2)),
            sew("huey_body", 78, 285.0, huey.x(3)),
            sew("hide_cape", 68, 62.0, mixedBase.x(), find("varlamore_jaguar_fur").x()),
            sew("hide_boots", 69, 75.0, mixedBase.x(), find("hunting_antelopesun_fur").x()),
            sew("hide_legs", 71, 210.0, mixedBase.x(), find("hunting_fennecfox_fur").x(3)),
            sew("hide_top", 72, 150.0, mixedBase.x(), find("hunting_antelopesun_fur").x(2)),
        )

    private val all = softLeather + dragonhideArmour + snakeskinArmour + otherArmour

    /** What the needle offers for [material]: every recipe that takes it, in level order. */
    fun sewnFrom(material: ObjType): List<CraftRecipe> =
        all.filter { recipe -> recipe.inputs.any { material.isType(it.obj) } }.sortedBy { it.level }

    /** Studs on leather armour (no needle) and the hammer-and-nails shields. */
    private fun nailed(
        product: String,
        level: Int,
        xp: Double,
        base: ObjType,
        hide: Ingredient,
        nails: ObjType,
        anim: SeqType,
    ) =
        Combo(
            base,
            hide.obj,
            CraftRecipe(
                find(product),
                level,
                xp,
                listOf(base.x(), hide, nails.x(15)),
                tools = listOf(hammer),
                ticks = 5,
                anim = anim,
            ),
        )

    private val studs = find("studs")

    private fun studded(product: String, level: Int, xp: Double, armour: String): Combo {
        val body = find(armour)
        return Combo(
            studs,
            body,
            CraftRecipe(
                find(product),
                level,
                xp,
                listOf(body.x(), studs.x()),
                anim = CraftingSeqs.needle,
            ),
        )
    }

    /** UNVERIFIED: the spiked vambraces' xp (the wiki's tables disagree), level 32 each. */
    private fun spiked(product: String, vambraces: String, xp: Double): Combo {
        val claws = find("huntingbeast_claws")
        val base = find(vambraces)
        return Combo(
            claws,
            base,
            CraftRecipe(
                find(product),
                32,
                xp,
                listOf(claws.x(), base.x()),
                anim = CraftingSeqs.spiked_vambraces,
            ),
        )
    }

    val combos =
        listOf(
            spiked("spiked_vambraces", "leather_vambraces", 6.0),
            spiked("hunting_green_spiked_vambraces", "dragon_vambraces", 5.0),
            spiked("hunting_blue_spiked_vambraces", "blue_dragon_vambraces", 6.0),
            spiked("hunting_red_spiked_vambraces", "red_dragon_vambraces", 5.8),
            spiked("hunting_black_spiked_vambraces", "black_dragon_vambraces", 6.0),
            studded("studded_body", 41, 40.0, "leather_armour"),
            studded("studded_chaps", 44, 42.0, "leather_chaps"),
            nailed(
                "leather_shield",
                41,
                70.0,
                find("oak_shield"),
                hardLeather.x(2),
                find("nails_bronze"),
                CraftingSeqs.leather_shield,
            ),
            nailed(
                "green_dhide_shield",
                62,
                124.0,
                find("maple_shield"),
                greenLeather.x(2),
                find("nails"),
                CraftingSeqs.green_shield,
            ),
            nailed(
                "blue_dhide_shield",
                69,
                140.0,
                find("yew_shield"),
                blueLeather.x(2),
                find("nails_mithril"),
                CraftingSeqs.blue_shield,
            ),
            nailed(
                "red_dhide_shield",
                76,
                156.0,
                find("magic_shield"),
                redLeather.x(2),
                find("nails_adamant"),
                CraftingSeqs.red_shield,
            ),
            nailed(
                "black_dhide_shield",
                83,
                172.0,
                find("redwood_shield"),
                blackLeather.x(2),
                find("nails_rune"),
                CraftingSeqs.black_shield,
            ),
            nailed(
                "snakeskin_shield",
                56,
                100.0,
                find("willow_shield"),
                snakeskin.x(2),
                find("nails_iron"),
                CraftingSeqs.snakeskin_shield,
            ),
        )
}
