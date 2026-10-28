package org.rsmod.content.skills.herblore

import org.rsmod.api.config.refs.stats
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.content.skills.production.Combo
import org.rsmod.content.skills.production.Ingredient
import org.rsmod.content.skills.production.Recipe
import org.rsmod.content.skills.production.Verb
import org.rsmod.content.skills.production.x
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.seq.SeqType

/** A herb that is cleaned for [xp] at [cleanLevel], then put in a vial at [unfLevel]. */
class Herb(
    val grimy: ObjType,
    val clean: ObjType,
    val unfinished: ObjType,
    val cleanLevel: Int,
    val xp: Double,
    val unfLevel: Int,
)

/** OSRS wiki levels and xp. A made potion has 3 doses. */
object HerbloreRecipes : ObjReferences() {
    private val vial = HerbloreObjs.vial_water
    private val pestle = HerbloreObjs.pestle_and_mortar

    private fun herblore(
        product: ObjType,
        level: Int,
        xp: Double,
        vararg inputs: Ingredient,
        tools: List<ObjType> = emptyList(),
        makes: Int = 1,
        ticks: Int = 2,
        anim: SeqType? = HerbloreSeqs.mix,
        message: String? = null,
    ) =
        Recipe(
            stats.herblore,
            product,
            level,
            xp,
            inputs.toList(),
            tools,
            makes,
            1,
            ticks,
            anim,
            message,
            "You need level $level Herblore to make this potion.",
        )

    private fun herb(name: String, grimy: String, level: Int, xp: Double, unf: Int, vial: String) =
        Herb(find("unidentified_$grimy"), find(name), find(vial), level, xp, unf)

    val herbs: List<Herb> =
        listOf(
            herb("guam_leaf", "guam", 3, 2.5, 3, "guamvial"),
            herb("marentill", "marentill", 5, 3.8, 5, "marrentillvial"),
            herb("tarromin", "tarromin", 11, 5.0, 12, "tarrominvial"),
            herb("harralander", "harralander", 20, 6.3, 22, "harralandervial"),
            herb("ranarr_weed", "ranarr", 25, 7.5, 30, "ranarrvial"),
            herb("toadflax", "toadflax", 30, 8.0, 34, "toadflaxvial"),
            herb("irit_leaf", "irit", 40, 8.8, 45, "iritvial"),
            herb("avantoe", "avantoe", 48, 10.0, 50, "avantoevial"),
            herb("kwuarm", "kwuarm", 54, 11.3, 55, "kwuarmvial"),
            herb("huasca", "huasca", 58, 11.8, 58, "huascavial"),
            herb("snapdragon", "snapdragon", 59, 11.8, 63, "snapdragonvial"),
            herb("cadantine", "cadantine", 65, 12.5, 66, "cadantinevial"),
            herb("lantadyme", "lantadyme", 67, 13.1, 69, "lantadymevial"),
            herb("dwarf_weed", "dwarf_weed", 70, 13.8, 72, "dwarfweedvial"),
            herb("torstol", "torstol", 75, 15.0, 78, "torstolvial"),
        )

    /** A clean herb in a vial of water, or in the other bases (coconut milk, blood). */
    private fun unfinished(clean: ObjType, base: ObjType, unf: ObjType, level: Int): Combo {
        val recipe =
            herblore(
                unf,
                level,
                0.0,
                clean.x(),
                base.x(),
                ticks = 1,
                message = "You put the {0} into the {1}.",
            )
        return Combo(clean, base, recipe, Verb.MAKE)
    }

    private val unfinishedPotions: List<Combo> =
        herbs.map { unfinished(it.clean, vial, it.unfinished, it.unfLevel) } +
            listOf(
                unfinished(
                    find("toadflax"),
                    HerbloreObjs.coconut_milk,
                    find("unfinished_antidote+"),
                    68,
                ),
                unfinished(
                    find("irit_leaf"),
                    HerbloreObjs.coconut_milk,
                    find("unfinished_antidote++"),
                    79,
                ),
                unfinished(
                    find("cactus_spine"),
                    HerbloreObjs.coconut_milk,
                    find("unfinished_weapon_poison+"),
                    73,
                ),
                unfinished(
                    find("nightshade"),
                    HerbloreObjs.coconut_milk,
                    find("unfinished_weapon_poison++"),
                    82,
                ),
                unfinished(find("cadantine"), find("vial_blood"), find("cadantine_bloodvial"), 80),
            )

    /** An unfinished potion plus one secondary. */
    private fun brew(
        unf: String,
        secondary: String,
        product: ObjType,
        level: Int,
        xp: Double,
    ): Combo {
        val base = find(unf)
        val extra = find(secondary)
        val recipe =
            herblore(
                product,
                level,
                xp,
                base.x(),
                extra.x(),
                message = "You mix the {1} into your {0}.",
            )
        return Combo(base, extra, recipe, Verb.MAKE)
    }

    /** The obj for [doses] doses of a potion (families list 4 doses first). */
    private fun dose(family: List<ObjType>, doses: Int): ObjType = family[4 - doses]

    private val brews: List<Combo> =
        listOf(
            brew("guamvial", "eye_of_newt", dose(Potions.attack, 3), 3, 25.0),
            brew("marrentillvial", "unicorn_horn_dust", dose(Potions.antipoison, 3), 5, 37.5),
            brew("tarrominvial", "limpwurt_root", dose(Potions.strength, 3), 12, 50.0),
            brew("harralandervial", "red_spiders_eggs", dose(Potions.restore, 3), 22, 62.5),
            brew("harralandervial", "fossil_volcanic_ash", dose(Potions.compost, 3), 22, 60.0),
            brew("harralandervial", "chocolate_dust", dose(Potions.energy, 3), 26, 67.5),
            brew("ranarrvial", "white_berries", dose(Potions.defence, 3), 30, 75.0),
            brew("toadflaxvial", "toads_legs", dose(Potions.agility, 3), 34, 80.0),
            brew("harralandervial", "ground_desert_goat_horn", dose(Potions.combat, 3), 36, 84.0),
            brew("ranarrvial", "snape_grass", dose(Potions.prayer, 3), 38, 87.5),
            brew("iritvial", "eye_of_newt", dose(Potions.superAttack, 3), 45, 100.0),
            brew("iritvial", "unicorn_horn_dust", dose(Potions.superAntipoison, 3), 48, 106.3),
            brew("avantoevial", "snape_grass", dose(Potions.fishing, 3), 50, 112.5),
            brew("avantoevial", "mortmyremushroom", dose(Potions.superEnergy, 3), 52, 117.5),
            brew("avantoevial", "huntingbeast_sabreteeth_dust", dose(Potions.hunter, 3), 53, 120.0),
            brew("harralandervial", "aldarium", dose(Potions.goading, 3), 54, 132.0),
            brew("kwuarmvial", "limpwurt_root", dose(Potions.superStrength, 3), 55, 125.0),
            brew("huascavial", "aldarium", dose(Potions.prayerRegeneration, 3), 58, 132.0),
            brew("kwuarmvial", "dragon_scale_dust", find("weapon_poison"), 60, 137.5),
            brew("snapdragonvial", "red_spiders_eggs", dose(Potions.superRestore, 3), 63, 142.5),
            brew("cadantinevial", "white_berries", dose(Potions.superDefence, 3), 66, 150.0),
            // Antidotes and weapon poison are made in coconut milk and come out with 4 doses.
            brew("unfinished_antidote+", "yew_roots", dose(Potions.antidotePlus, 4), 68, 155.0),
            brew("lantadymevial", "dragon_scale_dust", dose(Potions.antifire, 3), 69, 157.5),
            brew("dwarfweedvial", "wine_of_zamorak", dose(Potions.ranging, 3), 72, 162.5),
            brew(
                "unfinished_weapon_poison+",
                "red_spiders_eggs",
                find("weapon_poison+"),
                73,
                190.0,
            ),
            brew("lantadymevial", "cactus_potato", dose(Potions.magic, 3), 76, 172.5),
            brew("torstolvial", "jangerberries", dose(Potions.zamorak, 3), 78, 175.0),
            brew(
                "unfinished_antidote++",
                "magic_roots",
                dose(Potions.antidotePlusPlus, 4),
                79,
                177.5,
            ),
            brew("cadantine_bloodvial", "wine_of_zamorak", dose(Potions.bastion, 3), 80, 155.0),
            brew("cadantine_bloodvial", "cactus_potato", dose(Potions.battlemage, 3), 80, 155.0),
            brew("toadflaxvial", "crushed_bird_nest", dose(Potions.saradomin, 3), 81, 180.0),
            brew("torstolvial", "demonic_tallow", dose(Potions.surge, 3), 81, 185.0),
            brew(
                "unfinished_weapon_poison++",
                "poisonivy_berries",
                find("weapon_poison++"),
                82,
                190.0,
            ),
            brew("dwarfweedvial", "nihil_dust", dose(Potions.ancientBrew, 3), 85, 190.0),
            brew("dwarfweedvial", "lily_of_the_sands", dose(Potions.menaphiteRemedy, 3), 88, 200.0),
        )

    /** A potion of any dose count plus [per] of a secondary for each dose, xp per dose. */
    private fun perDose(
        secondary: String,
        base: List<ObjType>,
        product: List<ObjType>,
        level: Int,
        xpPerDose: Double,
        per: Int = 1,
    ): List<Combo> =
        (1..4).map { n ->
            val potion = dose(base, n)
            val extra = find(secondary)
            val recipe =
                herblore(
                    dose(product, n),
                    level,
                    xpPerDose * n,
                    potion.x(),
                    extra.x(per * n),
                    message = "You mix the {1} into your {0}.",
                )
            Combo(extra, potion, recipe, Verb.MAKE)
        }

    private val divine =
        listOf(
            Triple(Potions.superAttack, Potions.divineAttack, 70),
            Triple(Potions.superStrength, Potions.divineStrength, 70),
            Triple(Potions.superDefence, Potions.divineDefence, 70),
            Triple(Potions.ranging, Potions.divineRanging, 74),
            Triple(Potions.magic, Potions.divineMagic, 78),
            Triple(Potions.bastion, Potions.divineBastion, 86),
            Triple(Potions.battlemage, Potions.divineBattlemage, 86),
            Triple(Potions.superCombat, Potions.divineCombat, 97),
        )

    /** Super combat takes the three super potions and a torstol; any of the four starts it. */
    private fun superCombat(): List<Combo> {
        val torstol = find("torstol")
        val recipe =
            herblore(
                dose(Potions.superCombat, 4),
                90,
                150.0,
                torstol.x(),
                dose(Potions.superAttack, 4).x(),
                dose(Potions.superStrength, 4).x(),
                dose(Potions.superDefence, 4).x(),
                message = "You mix the {0} into your super potions.",
            )
        return listOf(Potions.superAttack, Potions.superStrength, Potions.superDefence).map {
            Combo(torstol, dose(it, 4), recipe, Verb.MAKE)
        }
    }

    /** Three steps on a super restore: (secondary, result, xp base + xp per dose). */
    private val sanfewSteps =
        listOf(
            Triple("unicorn_horn_dust", Potions.sanfewStep1, 19.0 to 9.5),
            Triple("snake_weed", Potions.sanfewStep2, 21.0 to 10.5),
            Triple("nail_beast_nail", Potions.sanfew, 24.0 to 12.0),
        )

    private fun sanfew(): List<Combo> {
        var from = Potions.superRestore
        return sanfewSteps.flatMap { (secondary, to, xp) ->
            val previous = from
            from = to
            (1..4).map { n ->
                val potion = dose(previous, n)
                val extra = find(secondary)
                val recipe =
                    herblore(
                        dose(to, n),
                        65,
                        xp.first + xp.second * n,
                        potion.x(),
                        extra.x(),
                        message = "You mix the {1} into your {0}.",
                    )
                Combo(extra, potion, recipe, Verb.MAKE)
            }
        }
    }

    private fun grind(input: String, product: String, makes: Int = 1): Combo {
        val item = find(input)
        val recipe =
            herblore(
                find(product),
                1,
                0.0,
                item.x(),
                tools = listOf(pestle),
                makes = makes,
                ticks = 3,
                anim = HerbloreSeqs.grind,
                message = "You grind the {0} to dust.",
            )
        return Combo(pestle, item, recipe)
    }

    private val grinding: List<Combo> =
        listOf(
            grind("unicorn_horn", "unicorn_horn_dust"),
            grind("cave_unicorn_horn", "unicorn_horn_dust"),
            grind("chocolate_bar", "chocolate_dust"),
            grind("desert_goat_horn", "ground_desert_goat_horn"),
            grind("huntingbeast_sabreteeth", "huntingbeast_sabreteeth_dust"),
            grind("blue_dragon_scale", "dragon_scale_dust"),
            grind("bird_nest_empty", "crushed_bird_nest"),
            grind("dragon_bones_superior", "crushed_dragon_bones"),
            // Note: the wiki gives 3-6 shards, more at higher Herblore; flat 4 until Recipe
            // takes a random yield.
            grind("lava_scale", "lava_shard", makes = 4),
        )

    private fun crushedBones(): List<Combo> {
        val bones = find("crushed_dragon_bones")
        return listOf(
            Combo(
                bones,
                dose(Potions.antifire, 4),
                herblore(
                    dose(Potions.superAntifire, 4),
                    92,
                    130.0,
                    dose(Potions.antifire, 4).x(),
                    bones.x(),
                    message = "You mix the {1} into your {0}.",
                ),
                Verb.MAKE,
            ),
            Combo(
                bones,
                dose(Potions.extendedAntifire, 4),
                herblore(
                    dose(Potions.extendedSuperAntifire, 4),
                    98,
                    180.0,
                    dose(Potions.extendedAntifire, 4).x(),
                    bones.x(),
                    message = "You mix the {1} into your {0}.",
                ),
                Verb.MAKE,
            ),
        )
    }

    val combos: List<Combo> =
        unfinishedPotions +
            brews +
            superCombat() +
            sanfew() +
            crushedBones() +
            perDose("amylase", Potions.superEnergy, Potions.stamina, 77, 25.5) +
            perDose("lava_shard", Potions.antifire, Potions.extendedAntifire, 84, 27.5) +
            perDose("lava_shard", Potions.superAntifire, Potions.extendedSuperAntifire, 98, 40.0) +
            perDose("ancient_essence", Potions.ancientBrew, Potions.forgottenBrew, 91, 36.25, 20) +
            divine.flatMap { (base, product, level) ->
                perDose("sote_crystal_dust", base, product, level, 0.5)
            } +
            grinding
}
