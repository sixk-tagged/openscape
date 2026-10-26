package org.rsmod.content.skills.smithing.scripts

import jakarta.inject.Inject
import org.rsmod.content.skills.smithing.configs.SmithingComponents
import org.rsmod.content.skills.smithing.configs.SmithingEnums
import org.rsmod.content.skills.smithing.configs.SmithingObjs
import org.rsmod.game.enums.EnumTypeMap
import org.rsmod.game.enums.EnumTypeMapResolver
import org.rsmod.game.type.comp.ComponentType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.obj.UnpackedObjType

/**
 * What each anvil button makes for each metal, copied from the rev-230/233 client script
 * `[proc,smithing_setup]` so the server makes exactly what the button shows. List index = metal =
 * `smithing_bar_type` - 1 (cache enum 1253). Lovakite (type 7, Shayzien armour) is left out;
 * blurite has no anvil items in this revision.
 */
object AnvilTable {
    val bars: List<ObjType> =
        with(SmithingObjs) {
            listOf(bronze_bar, iron_bar, steel_bar, mithril_bar, adamantite_bar, runite_bar)
        }
    val metals = listOf("bronze", "iron", "steel", "mithril", "adamantite", "runite")
    /** Level to work each metal at all (its dagger level). */
    val metalLevels = listOf(1, 15, 30, 50, 70, 85)
    val xpPerBar = listOf(12.5, 25.0, 37.5, 50.0, 62.5, 75.0)

    val products: Map<ComponentType, List<String?>> =
        with(SmithingComponents) {
            mapOf(
                dagger to metal("dagger"),
                sword to metal("sword"),
                scimitar to metal("scimitar"),
                longsword to metal("longsword"),
                twohanded to metal("2h_sword"),
                axe to metal("axe"),
                mace to metal("mace"),
                warhammer to metal("warhammer").map { it.replace("adamant_", "adamnt_") },
                battleaxe to metal("battleaxe"),
                claws to metal("claws"),
                chainbody to metal("chainbody"),
                platelegs to metal("platelegs"),
                plateskirt to metal("plateskirt"),
                platebody to metal("platebody"),
                nails to
                    listOf(
                        "nails_bronze",
                        "nails_iron",
                        "nails",
                        "nails_mithril",
                        "nails_adamant",
                        "nails_rune",
                    ),
                medhelm to metal("med_helm"),
                fullhelm to metal("full_helm"),
                squareshield to metal("sq_shield"),
                kiteshield to metal("kiteshield"),
                darttips to metal("dart_tip"),
                arrowheads to metal("arrowheads"),
                knives to metal("knife"),
                bolts to crossbow("xbows_crossbow_bolts_", "_unfeathered"),
                limbs to crossbow("xbows_crossbow_limbs_", ""),
                other_1 to
                    listOf(
                        "bronzecraftwire",
                        "spit_iron",
                        "studs",
                        "xbows_grapple_tip_mithril",
                        "adamant_javelin_head",
                        "rune_javelin_head",
                    ),
                other_2 to
                    listOf(null, "oil_lantern_frame", "bullseye_lantern_nolens", null, null, null),
                other_3 to
                    listOf(
                        "bronze_javelin_head",
                        "iron_javelin_head",
                        "steel_javelin_head",
                        "mithril_javelin_head",
                        null,
                        null,
                    ),
            )
        }

    /** `bronze_x` .. `adamant_x`, `rune_x`: the usual product naming. */
    private fun metal(suffix: String): List<String> =
        listOf("bronze", "iron", "steel", "mithril", "adamant", "rune").map { "${it}_$suffix" }

    /** Crossbow parts spell the metals out: `..._adamantite...`, `..._runite...`. */
    private fun crossbow(prefix: String, suffix: String): List<String> =
        listOf("bronze", "iron", "steel", "mithril", "adamantite", "runite").map {
            "$prefix$it$suffix"
        }
}

/** One anvil product: level, bars and quantity come from the cache's smithing enums. */
data class AnvilProduct(
    val obj: UnpackedObjType,
    val metal: Int,
    val level: Int,
    val bars: Int,
    val makes: Int,
)

class AnvilProducts @Inject constructor(objTypes: ObjTypeList, enums: EnumTypeMapResolver) {
    private val byButton: Map<Int, List<AnvilProduct?>>

    init {
        val levels = enums[SmithingEnums.product_level].byObjId()
        val bars = enums[SmithingEnums.product_bars].byObjId()
        val makes = enums[SmithingEnums.product_quantity].byObjId()
        val byName = objTypes.values.associateBy { it.internalName }
        byButton =
            AnvilTable.products.entries.associate { (button, names) ->
                button.packed to
                    names.mapIndexed { metal, name ->
                        name?.let {
                            val obj = checkNotNull(byName[it]) { "Unknown anvil product: $it" }
                            AnvilProduct(
                                obj = obj,
                                metal = metal,
                                level = levels.getValue(obj.id),
                                bars = bars.getValue(obj.id),
                                makes = makes[obj.id] ?: 1,
                            )
                        }
                    }
            }
    }

    val all: List<AnvilProduct>
        get() = byButton.values.flatMap { it.filterNotNull() }

    fun find(button: ComponentType, metal: Int): AnvilProduct? =
        byButton[button.packed]?.getOrNull(metal)

    private fun EnumTypeMap<ObjType, Int>.byObjId(): Map<Int, Int> =
        filterValuesNotNull().backing.mapKeys { it.key.id }
}
