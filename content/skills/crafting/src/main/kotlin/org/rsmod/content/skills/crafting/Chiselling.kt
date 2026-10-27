package org.rsmod.content.skills.crafting

import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpHeldU
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.isType
import org.rsmod.game.type.seq.SeqType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** The chisel, used on gems, amethyst, snail shells, crab shells and limestone. */
class Chiselling : PluginScript() {
    override fun ScriptContext.startup() {
        onOpHeldU(CraftingObjs.chisel) { chisel(it.second) }
    }

    private suspend fun ProtectedAccess.chisel(item: ObjType) {
        val recipes = ChiselRecipes.from(item)
        when {
            recipes.isEmpty() -> mes(NOTHING_HAPPENS)
            // No prompt: gems are cut one after another until none of that kind are left.
            recipes.size == 1 -> startCrafting(recipes.single(), MAX_AMOUNT)
            else -> offer(Verb.CUT, "cut", recipes)
        }
    }
}

object GemSeqs : SeqReferences() {
    val opal = find("human_opalcutting")
    val jade = find("human_jadecutting")
    val red_topaz = find("human_redtopazcutting")
    val sapphire = find("human_sapphirecutting")
    val emerald = find("human_emeraldcutting")
    val ruby = find("human_rubycutting")
    val diamond = find("human_diamondcutting")
    val dragonstone = find("human_dragonstonecutting")
    val onyx = find("human_onyxcutting")
    val amethyst = find("human_amethystcutting")
    val zenyte = find("human_zenytecutting")
}

/** Chisel recipes (OSRS wiki levels and xp). Two ticks per cut. */
object ChiselRecipes : ObjReferences() {
    private val chisel = CraftingObjs.chisel
    private val crushed = find("crushed_gemstone")

    /** Semi-precious gems can crush; the chances are the wiki's formulas as `statRandom` pairs. */
    private fun gem(
        name: String,
        item: String,
        level: Int,
        xp: Double,
        anim: SeqType,
        crush: Triple<Int, Int, Double>? = null,
    ): CraftRecipe {
        val spoil =
            crush?.let {
                Spoil(
                    it.first,
                    it.second,
                    "You mis-hit the chisel and smash the $name into pieces!",
                    crushed,
                    it.third,
                )
            }
        return CraftRecipe(
            find(item),
            level,
            xp,
            listOf(find("uncut_$item").x()),
            tools = listOf(chisel),
            ticks = 2,
            anim = anim,
            message = "You cut the $name.",
            levelMessage = "You need a Crafting level of $level to cut this gem.",
            spoil = spoil,
        )
    }

    private val gems =
        listOf(
            // UNVERIFIED: statRandom pairs converted from the wiki's success formulas; the opal
            // crush xp is 3.8 on the Opal page and 3.7 on the Crushed gem page.
            gem("opal", "opal", 1, 15.0, GemSeqs.opal, Triple(128, 250, 3.8)),
            gem("jade", "jade", 13, 20.0, GemSeqs.jade, Triple(100, 245, 5.0)),
            gem("red topaz", "red_topaz", 16, 25.0, GemSeqs.red_topaz, Triple(90, 240, 6.3)),
            gem("sapphire", "sapphire", 20, 50.0, GemSeqs.sapphire),
            gem("emerald", "emerald", 27, 67.5, GemSeqs.emerald),
            gem("ruby", "ruby", 34, 85.0, GemSeqs.ruby),
            gem("diamond", "diamond", 43, 107.5, GemSeqs.diamond),
            gem("dragonstone", "dragonstone", 55, 137.5, GemSeqs.dragonstone),
            gem("onyx", "onyx", 67, 167.5, GemSeqs.onyx),
            gem("zenyte", "zenyte", 89, 200.0, GemSeqs.zenyte),
        )

    private val amethyst = find("amethyst")

    private fun tips(product: String, level: Int, makes: Int) =
        CraftRecipe(
            find(product),
            level,
            60.0,
            listOf(amethyst.x()),
            tools = listOf(chisel),
            makes = makes,
            ticks = 2,
            anim = GemSeqs.amethyst,
        )

    private val amethystTips =
        listOf(
            tips("xbows_bolt_tips_amethyst", 83, 15),
            tips("amethyst_arrowheads", 85, 15),
            tips("amethyst_javelin_head", 87, 5),
            tips("amethyst_dart_tip", 89, 8),
        )

    private fun shell(from: String, to: String) =
        CraftRecipe(
            find(to),
            15,
            32.5,
            listOf(find(from).x()),
            tools = listOf(chisel),
            ticks = 2,
            anim = CraftingSeqs.snail_shell,
        )

    private fun crab(from: String, to: String) =
        CraftRecipe(find(to), 15, 32.5, listOf(find(from).x()), tools = listOf(chisel))

    private val others =
        listOf(
            shell("shellround_swamp", "snelm_round_swamp"),
            shell("shellround_red+black", "snelm_round_red+black"),
            shell("shellround_yellow", "snelm_round_yellow"),
            shell("shellround_blue", "snelm_round_blue"),
            shell("shellround_orange", "snelm_round_orange"),
            shell("shellpoint_swamp", "snelm_point_swamp"),
            shell("shellpoint_red+black", "snelm_point_red+black"),
            shell("shellpoint_yellow", "snelm_point_yellow"),
            shell("shellpoint_blue", "snelm_point_blue"),
            crab("hundred_pirate_crab_shell_head", "hundred_pirate_crab_shell_helm"),
            crab("hundred_pirate_crab_shell_claw", "hundred_pirate_crab_shell_gauntlet"),
            CraftRecipe(
                find("limestonebrick"),
                12,
                6.0,
                listOf(find("limestone").x()),
                tools = listOf(chisel),
                ticks = 2,
                anim = CraftingSeqs.limestone,
            ),
        )

    private val all = gems + amethystTips + others

    /** What the chisel makes from [item]. */
    fun from(item: ObjType): List<CraftRecipe> =
        all.filter { recipe -> recipe.inputs.any { item.isType(it.obj) } }
}
