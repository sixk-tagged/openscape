package org.rsmod.content.skills.fletching

import org.rsmod.api.config.refs.stats
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.content.skills.production.Combo
import org.rsmod.content.skills.production.Ingredient
import org.rsmod.content.skills.production.Recipe
import org.rsmod.content.skills.production.Verb
import org.rsmod.content.skills.production.x
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.seq.SeqType

/** OSRS wiki levels and xp. Ammo xp is per piece. */
object FletchingRecipes : ObjReferences() {
    private val knife = FletchingObjs.knife
    private val chisel = FletchingObjs.chisel

    private fun fletch(
        product: ObjType,
        level: Int,
        xp: Double,
        vararg inputs: Ingredient,
        tools: List<ObjType> = emptyList(),
        makes: Int = 1,
        batch: Int = 1,
        ticks: Int = 2,
        anim: SeqType? = null,
        message: String? = null,
    ) =
        Recipe(
            stats.fletching,
            product,
            level,
            xp,
            inputs.toList(),
            tools,
            makes,
            batch,
            ticks,
            anim,
            message,
            "You need a Fletching level of $level to make that.",
        )

    /** Knife products take 3 ticks, shields 7. */
    private fun cut(
        product: String,
        level: Int,
        xp: Double,
        log: String,
        makes: Int = 1,
        logs: Int = 1,
        ticks: Int = 3,
        message: String? = null,
    ) =
        fletch(
            find(product),
            level,
            xp,
            find(log).x(logs),
            tools = listOf(knife),
            makes = makes,
            ticks = ticks,
            anim = FletchingSeqs.whittle,
            message = message,
        )

    private fun shafts(log: String, level: Int, xp: Double, makes: Int) =
        cut(
            "arrow_shaft",
            level,
            xp,
            log,
            makes,
            message = "You carefully cut the wood into # arrow shafts.",
        )

    private fun stock(log: String, level: Int, xp: Double, wood: String) =
        cut("xbows_crossbow_stock_$wood", level, xp, log)

    private fun shield(log: String, level: Int, xp: Double, product: String) =
        cut(product, level, xp, log, logs = 2, ticks = 7)

    /** Unstrung bows are named `unstrung_<wood>_<shortbow|longbow>`; plain wood has no prefix. */
    private fun bows(log: String, wood: Wood) =
        listOf(
            cut("unstrung_${wood.name}shortbow", wood.short.first, wood.short.second, log),
            cut("unstrung_${wood.name}longbow", wood.long.first, wood.long.second, log),
        )

    /** [name] is the cache prefix. Stringing gives the xp in [stringShort] and [stringLong]. */
    private class Wood(
        val name: String,
        val short: Pair<Int, Double>,
        val long: Pair<Int, Double>,
        val stringShort: Double,
        val stringLong: Double,
    )

    private val normal = Wood("", 5 to 5.0, 10 to 10.0, 5.0, 10.0)
    private val oak = Wood("oak_", 20 to 16.5, 25 to 25.0, 16.5, 25.0)
    private val willow = Wood("willow_", 35 to 33.3, 40 to 41.5, 33.2, 41.5)
    private val maple = Wood("maple_", 50 to 50.0, 55 to 58.3, 50.0, 58.2)
    private val yew = Wood("yew_", 65 to 67.5, 70 to 75.0, 67.5, 75.0)
    private val magic = Wood("magic_", 80 to 83.3, 85 to 91.5, 83.2, 91.5)
    private val woods = listOf(normal, oak, willow, maple, yew, magic)

    val logs: List<LogRecipes> =
        listOf(
            LogRecipes(
                find("logs"),
                listOf(
                    shafts("logs", 1, 5.0, 15),
                    cut(
                        "javelin_shaft",
                        3,
                        5.0,
                        "logs",
                        makes = 15,
                        ticks = 2,
                        message = "You carefully cut the wood into # javelin shafts.",
                    ),
                ) + bows("logs", normal) + stock("logs", 9, 6.0, "wood"),
            ),
            LogRecipes(
                find("oak_logs"),
                listOf(shafts("oak_logs", 15, 10.0, 30)) +
                    bows("oak_logs", oak) +
                    stock("oak_logs", 24, 16.0, "oak") +
                    shield("oak_logs", 27, 50.0, "oak_shield"),
            ),
            LogRecipes(
                find("willow_logs"),
                listOf(shafts("willow_logs", 30, 15.0, 45)) +
                    bows("willow_logs", willow) +
                    stock("willow_logs", 39, 22.0, "willow") +
                    shield("willow_logs", 42, 83.0, "willow_shield"),
            ),
            LogRecipes(
                find("maple_logs"),
                listOf(shafts("maple_logs", 45, 20.0, 60)) +
                    bows("maple_logs", maple) +
                    stock("maple_logs", 54, 32.0, "maple") +
                    shield("maple_logs", 57, 116.5, "maple_shield"),
            ),
            LogRecipes(
                find("yew_logs"),
                listOf(shafts("yew_logs", 60, 25.0, 75)) +
                    bows("yew_logs", yew) +
                    stock("yew_logs", 69, 50.0, "yew") +
                    shield("yew_logs", 72, 150.0, "yew_shield"),
            ),
            LogRecipes(
                find("magic_logs"),
                listOf(shafts("magic_logs", 75, 30.0, 90)) +
                    bows("magic_logs", magic) +
                    stock("magic_logs", 78, 70.0, "magic") +
                    shield("magic_logs", 87, 183.0, "magic_shield"),
            ),
            LogRecipes(find("teak_logs"), listOf(stock("teak_logs", 46, 27.0, "teak"))),
            LogRecipes(find("mahogany_logs"), listOf(stock("mahogany_logs", 61, 41.0, "mahogany"))),
            LogRecipes(
                find("redwood_logs"),
                listOf(
                    shafts("redwood_logs", 90, 35.0, 105),
                    shield("redwood_logs", 92, 216.0, "redwood_shield"),
                    cut("redwood_hiking_staff", 90, 10.5, "redwood_logs"),
                ),
            ),
        )

    private fun string(wood: Wood): List<Combo> =
        listOf("short" to wood.short, "long" to wood.long).map { (kind, level) ->
            val unstrung = find("unstrung_${wood.name}${kind}bow")
            val xp = if (kind == "short") wood.stringShort else wood.stringLong
            Combo(
                unstrung,
                FletchingObjs.bow_string,
                fletch(
                    find("${wood.name}${kind}bow"),
                    level.first,
                    xp,
                    unstrung.x(),
                    FletchingObjs.bow_string.x(),
                    message = "You add a string to the bow.",
                ),
                Verb.STRING,
                "string",
            )
        }

    /** A crossbow: the cache name of its [metal] limbs, unstrung and strung forms. */
    private class Tier(
        val stock: String,
        val metal: String,
        val level: Int,
        val limbsXp: Double,
        val stringXp: Double,
    )

    private val tiers =
        listOf(
            Tier("wood", "bronze", 9, 12.0, 6.0),
            Tier("oak", "blurite", 24, 32.0, 16.0),
            Tier("willow", "iron", 39, 44.0, 22.0),
            Tier("teak", "steel", 46, 54.0, 27.0),
            Tier("maple", "mithril", 54, 64.0, 32.0),
            Tier("mahogany", "adamantite", 61, 82.0, 41.0),
            Tier("yew", "runite", 69, 100.0, 50.0),
            Tier("magic", "dragon", 78, 135.0, 70.0),
        )

    private fun crossbow(tier: Tier): List<Combo> {
        val stock = find("xbows_crossbow_stock_${tier.stock}")
        val limbs = find("xbows_crossbow_limbs_${tier.metal}")
        val unstrung = find("xbows_crossbow_unstrung_${tier.metal}")
        return listOf(
            Combo(
                limbs,
                stock,
                fletch(
                    unstrung,
                    tier.level,
                    tier.limbsXp,
                    stock.x(),
                    limbs.x(),
                    tools = listOf(FletchingObjs.hammer),
                    message = "You attach the limbs to the stock.",
                ),
                Verb.MAKE,
            ),
            Combo(
                FletchingObjs.crossbow_string,
                unstrung,
                fletch(
                    find("xbows_crossbow_${tier.metal}"),
                    tier.level,
                    tier.stringXp,
                    unstrung.x(),
                    FletchingObjs.crossbow_string.x(),
                    message = "You add a string to the crossbow.",
                ),
                Verb.STRING,
                "string",
            ),
        )
    }

    /** One set of up to [size] pieces ([a] and [b] used one for one); [xp] is per piece. */
    private fun set(
        a: String,
        b: String,
        product: String,
        level: Int,
        xp: Double,
        size: Int,
        ticks: Int = 2,
        message: String,
    ): Combo {
        val first = find(a)
        val second = find(b)
        val recipe =
            fletch(
                find(product),
                level,
                xp,
                first.x(),
                second.x(),
                batch = size,
                ticks = ticks,
                message = message,
            )
        return Combo(first, second, recipe)
    }

    /** Level and xp per piece for arrows, darts and javelins of a metal. */
    private class Metal(
        val name: String,
        val arrow: Pair<Int, Double>,
        val dart: Pair<Int, Double>,
        val javelin: Pair<Int, Double>,
    )

    private val metals =
        listOf(
            Metal("bronze", 1 to 1.3, 10 to 1.8, 3 to 1.0),
            Metal("iron", 15 to 2.5, 22 to 3.8, 17 to 2.0),
            Metal("steel", 30 to 5.0, 37 to 7.5, 32 to 5.0),
            Metal("mithril", 45 to 7.5, 52 to 11.2, 47 to 8.0),
            Metal("adamant", 60 to 10.0, 67 to 15.0, 62 to 10.0),
            Metal("rune", 75 to 12.5, 81 to 18.8, 77 to 12.4),
            Metal("amethyst", 82 to 13.5, 90 to 21.0, 84 to 13.5),
            Metal("dragon", 90 to 15.0, 95 to 25.0, 92 to 15.0),
        )

    private fun ammo(metal: Metal): List<Combo> =
        listOf(
            set(
                "headless_arrow",
                "${metal.name}_arrowheads",
                "${metal.name}_arrow",
                metal.arrow.first,
                metal.arrow.second,
                15,
                message = "You attach arrow tips to # arrows.",
            ),
            set(
                "${metal.name}_dart_tip",
                "feather",
                "${metal.name}_dart",
                metal.dart.first,
                metal.dart.second,
                10,
                message = "You finish making # darts.",
            ),
            set(
                "javelin_shaft",
                "${metal.name}_javelin_head",
                "${metal.name}_javelin",
                metal.javelin.first,
                metal.javelin.second,
                15,
                ticks = 1,
                message = "You attach the heads to # javelins.",
            ),
        )

    /** Unfeathered bolts, their feathered form, level and xp per bolt. */
    private class Bolt(val unfeathered: String, val bolt: String, val level: Int, val xp: Double)

    private val bolts =
        listOf(
            Bolt("xbows_crossbow_bolts_bronze_unfeathered", "bolt", 9, 0.5),
            Bolt(
                "xbows_crossbow_bolts_blurite_unfeathered",
                "xbows_crossbow_bolts_blurite",
                24,
                1.0,
            ),
            Bolt("xbows_crossbow_bolts_iron_unfeathered", "xbows_crossbow_bolts_iron", 39, 1.5),
            Bolt("xbows_crossbow_bolts_silver_unfeathered", "xbows_crossbow_bolts_silver", 43, 2.5),
            Bolt("xbows_crossbow_bolts_steel_unfeathered", "xbows_crossbow_bolts_steel", 46, 3.5),
            Bolt(
                "xbows_crossbow_bolts_mithril_unfeathered",
                "xbows_crossbow_bolts_mithril",
                54,
                5.0,
            ),
            Bolt("slayer_broad_bolt_unfinished", "slayer_broad_bolt", 55, 3.0),
            Bolt(
                "xbows_crossbow_bolts_adamantite_unfeathered",
                "xbows_crossbow_bolts_adamantite",
                61,
                7.0,
            ),
            Bolt(
                "xbows_crossbow_bolts_runite_unfeathered",
                "xbows_crossbow_bolts_runite",
                69,
                10.0,
            ),
            Bolt("dragon_bolts_unfeathered", "dragon_bolts", 84, 12.0),
        )

    /** Gem (or barbed) tips on a bolt, level and xp per bolt. */
    private class Tipped(
        val tips: String,
        val bolt: String,
        val product: String,
        val level: Int,
        val xp: Double,
    )

    private val tipped =
        listOf(
            Tipped("opal_bolttips", "bolt", "opal_bolt", 11, 1.6),
            Tipped(
                "xbows_bolt_tips_jade",
                "xbows_crossbow_bolts_blurite",
                "xbows_crossbow_bolts_blurite_tipped_jade",
                26,
                2.4,
            ),
            Tipped("pearl_bolttips", "xbows_crossbow_bolts_iron", "pearl_bolt", 41, 3.2),
            Tipped(
                "xbows_bolt_tips_redtopaz",
                "xbows_crossbow_bolts_steel",
                "xbows_crossbow_bolts_steel_tipped_redtopaz",
                48,
                3.9,
            ),
            Tipped("barbed_bolttips", "bolt", "barbed_bolt", 51, 9.5),
            Tipped(
                "xbows_bolt_tips_sapphire",
                "xbows_crossbow_bolts_mithril",
                "xbows_crossbow_bolts_mithril_tipped_sapphire",
                56,
                4.7,
            ),
            Tipped(
                "xbows_bolt_tips_emerald",
                "xbows_crossbow_bolts_mithril",
                "xbows_crossbow_bolts_mithril_tipped_emerald",
                58,
                5.5,
            ),
            Tipped(
                "xbows_bolt_tips_ruby",
                "xbows_crossbow_bolts_adamantite",
                "xbows_crossbow_bolts_adamantite_tipped_ruby",
                63,
                6.3,
            ),
            Tipped(
                "xbows_bolt_tips_diamond",
                "xbows_crossbow_bolts_adamantite",
                "xbows_crossbow_bolts_adamantite_tipped_diamond",
                65,
                7.0,
            ),
            Tipped(
                "xbows_bolt_tips_dragonstone",
                "xbows_crossbow_bolts_runite",
                "xbows_crossbow_bolts_runite_tipped_dragonstone",
                71,
                8.2,
            ),
            Tipped(
                "xbows_bolt_tips_onyx",
                "xbows_crossbow_bolts_runite",
                "xbows_crossbow_bolts_runite_tipped_onyx",
                73,
                9.4,
            ),
            Tipped(
                "xbows_bolt_tips_amethyst",
                "slayer_broad_bolt",
                "slayer_broad_bolt_amethyst",
                76,
                10.6,
            ),
        )

    /** Dragon bolts take every gem tip but amethyst: (tips, product gem, xp per bolt). */
    private val dragonTipped =
        listOf(
            Triple("opal_bolttips", "opal", 1.6),
            Triple("xbows_bolt_tips_jade", "jade", 2.4),
            Triple("pearl_bolttips", "pearl", 3.2),
            Triple("xbows_bolt_tips_redtopaz", "topaz", 4.0),
            Triple("xbows_bolt_tips_sapphire", "sapphire", 4.7),
            Triple("xbows_bolt_tips_emerald", "emerald", 5.5),
            Triple("xbows_bolt_tips_ruby", "ruby", 6.3),
            Triple("xbows_bolt_tips_diamond", "diamond", 7.0),
            Triple("xbows_bolt_tips_dragonstone", "dragonstone", 8.2),
            Triple("xbows_bolt_tips_onyx", "onyx", 9.4),
        )

    /** A gem the chisel turns into [makes] bolt tips. */
    private class Gem(
        val gem: String,
        val tips: String,
        val level: Int,
        val xp: Double,
        val makes: Int = 12,
    )

    private val gems =
        listOf(
            Gem("opal", "opal_bolttips", 11, 1.5),
            Gem("jade", "xbows_bolt_tips_jade", 26, 2.0),
            Gem("smalloysterpearls", "pearl_bolttips", 41, 3.2, 6),
            Gem("bigoysterpearls", "pearl_bolttips", 41, 3.2, 24),
            Gem("red_topaz", "xbows_bolt_tips_redtopaz", 48, 3.9),
            Gem("sapphire", "xbows_bolt_tips_sapphire", 56, 4.0),
            Gem("emerald", "xbows_bolt_tips_emerald", 58, 5.5),
            Gem("ruby", "xbows_bolt_tips_ruby", 63, 6.3),
            Gem("diamond", "xbows_bolt_tips_diamond", 65, 7.0),
            Gem("dragonstone", "xbows_bolt_tips_dragonstone", 71, 8.2),
            Gem("onyx", "xbows_bolt_tips_onyx", 73, 9.4, 24),
        )

    private fun cutGem(gem: Gem): Combo {
        val obj = find(gem.gem)
        val recipe =
            fletch(
                find(gem.tips),
                gem.level,
                gem.xp,
                obj.x(),
                tools = listOf(chisel),
                makes = gem.makes,
                ticks = 5,
                message = "You cut the gem into # bolt tips.",
            )
        return Combo(chisel, obj, recipe)
    }

    private val others =
        listOf(
            set(
                "arrow_shaft",
                "feather",
                "headless_arrow",
                1,
                1.0,
                15,
                message = "You attach feathers to # arrow shafts.",
            ),
            set(
                "headless_arrow",
                "slayer_broad_arrowhead",
                "slayer_broad_arrows",
                52,
                10.0,
                15,
                message = "You attach broad arrowheads to # arrows.",
            ),
            set(
                "xbows_grapple_tip_mithril",
                "xbows_crossbow_bolts_mithril",
                "xbows_grapple_tip_bolt_mithril",
                59,
                11.0,
                1,
                message = "You attach the tip to the bolt.",
            ),
            set(
                "xbows_grapple_tip_bolt_mithril",
                "rope",
                "xbows_grapple_tip_bolt_mithril_rope",
                59,
                0.0,
                1,
                ticks = 1,
                message = "You tie the rope to the grapple.",
            ),
            Combo(
                find("celastrus_wood"),
                knife,
                fletch(
                    find("battlestaff"),
                    40,
                    80.0,
                    find("celastrus_wood").x(),
                    tools = listOf(knife),
                    ticks = 4,
                    anim = FletchingSeqs.whittle,
                ),
            ),
            Combo(
                chisel,
                find("blowpipe_fang"),
                fletch(
                    find("toxic_blowpipe"),
                    78,
                    120.0,
                    find("blowpipe_fang").x(),
                    tools = listOf(chisel),
                ),
            ),
        )

    val combos: List<Combo> =
        woods.flatMap { string(it) } +
            tiers.flatMap { crossbow(it) } +
            metals.flatMap { ammo(it) } +
            bolts.map {
                set(
                    it.unfeathered,
                    "feather",
                    it.bolt,
                    it.level,
                    it.xp,
                    10,
                    message = "You attach feathers to # bolts.",
                )
            } +
            tipped.map {
                set(it.tips, it.bolt, it.product, it.level, it.xp, 10, message = "You tip # bolts.")
            } +
            dragonTipped.map { (tips, gem, xp) ->
                set(
                    tips,
                    "dragon_bolts",
                    "dragon_bolts_unenchanted_$gem",
                    84,
                    xp,
                    10,
                    message = "You tip # bolts.",
                )
            } +
            gems.map { cutGem(it) } +
            others
}
