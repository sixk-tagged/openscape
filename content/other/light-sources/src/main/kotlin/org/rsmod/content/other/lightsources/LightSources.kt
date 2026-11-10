@file:Suppress("SpellCheckingInspection")

package org.rsmod.content.other.lightsources

import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.entity.Player
import org.rsmod.game.type.obj.ObjType

/**
 * A light source (wiki "Light sources"): [lit] lights up dark areas at [brightness] (1-3), [unlit]
 * takes a tinderbox and Firemaking [level], and a [naked] flame sets off swamp gas.
 */
data class LightSource(
    val lit: ObjType,
    val unlit: ObjType?,
    val level: Int,
    val brightness: Int,
    val naked: Boolean,
)

object LightSourceObjs : ObjReferences() {
    val tinderbox = find("tinderbox")
    val torch_lit = find("torch_lit")
    val torch_unlit = find("torch_unlit")
    val candle_lit = find("lit_candle")
    val candle_unlit = find("unlit_candle")
    val black_candle_lit = find("lit_black_candle")
    val black_candle_unlit = find("unlit_black_candle")
    val candle_lantern_lit = find("candle_lantern_lit")
    val candle_lantern_unlit = find("candle_lantern_unlit")
    val black_candle_lantern_lit = find("candle_lantern_black_lit")
    val black_candle_lantern_unlit = find("candle_lantern_black_unlit")
    val oil_lamp_lit = find("oil_lamp_lit")
    val oil_lamp_unlit = find("oil_lamp_unlit")
    val oil_lantern_lit = find("oil_lantern_lit")
    val oil_lantern_unlit = find("oil_lantern_unlit")
    val bullseye_lantern_lit = find("bullseye_lantern_lit")
    val bullseye_lantern_unlit = find("bullseye_lantern_unlit")
    val sapphire_lantern_lit = find("tog_sapphire_lantern_lit")
    val sapphire_lantern_unlit = find("tog_sapphire_lantern_unlit")
    val mining_helmet_lit = find("cave_goblin_mining_helmet_lit")
    val mining_helmet_unlit = find("cave_goblin_mining_helmet_unlit")
    val firemaking_cape = find("skillcape_firemaking")
    val firemaking_cape_trimmed = find("skillcape_firemaking_trimmed")
    val max_cape = find("skillcape_max")
}

object LightSources {
    val all: List<LightSource> =
        with(LightSourceObjs) {
            listOf(
                LightSource(torch_lit, torch_unlit, 1, 1, naked = true),
                LightSource(candle_lit, candle_unlit, 1, 1, naked = true),
                LightSource(black_candle_lit, black_candle_unlit, 1, 1, naked = true),
                LightSource(candle_lantern_lit, candle_lantern_unlit, 4, 1, naked = false),
                LightSource(black_candle_lantern_lit, black_candle_lantern_unlit, 4, 1, false),
                LightSource(oil_lamp_lit, oil_lamp_unlit, 12, 2, naked = true),
                LightSource(oil_lantern_lit, oil_lantern_unlit, 26, 2, naked = false),
                LightSource(bullseye_lantern_lit, bullseye_lantern_unlit, 49, 3, naked = false),
                LightSource(sapphire_lantern_lit, sapphire_lantern_unlit, 49, 2, naked = false),
                LightSource(mining_helmet_lit, mining_helmet_unlit, 65, 2, naked = false),
                LightSource(firemaking_cape, null, 99, 3, naked = false),
                LightSource(firemaking_cape_trimmed, null, 99, 3, naked = false),
                LightSource(max_cape, null, 99, 3, naked = false),
            )
        }

    /** The capes only give light while worn; everything else lights up from the inventory too. */
    private val wornOnly =
        with(LightSourceObjs) { setOf(firemaking_cape, firemaking_cape_trimmed, max_cape) }

    fun carried(player: Player): List<LightSource> =
        all.filter { source ->
            source.lit in player.worn || (source.lit !in wornOnly && source.lit in player.inv)
        }

    /** The brightest light [player] carries, 0 for none. */
    fun brightness(player: Player): Int = carried(player).maxOfOrNull { it.brightness } ?: 0

    /** The lit naked flames in [player]'s inventory (they set off swamp gas). */
    fun nakedFlames(player: Player): List<LightSource> =
        all.filter { it.naked && it.lit in player.inv }
}
