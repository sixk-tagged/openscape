package org.rsmod.content.areas.city.falador.npcs

import org.rsmod.api.death.NpcKilled
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.vars.VarPlayerIntMapSetter
import org.rsmod.api.script.onEvent
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.content.areas.city.falador.configs.falador_invs
import org.rsmod.content.areas.city.falador.configs.falador_npcs
import org.rsmod.content.areas.city.falador.configs.falador_varps
import org.rsmod.game.entity.Player
import org.rsmod.game.type.inv.InvType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** Sir Vyvin's ranks: each rank's kill score and its armoury. */
internal enum class WhiteKnightRank(val label: String, val score: Int, val shop: InvType) {
    Novice("Novice", 100, falador_invs.white_knight_novice),
    Peon("Peon", 200, falador_invs.white_knight_peon),
    Page("Page", 300, falador_invs.white_knight_page),
    Noble("Noble", 500, falador_invs.white_knight_noble),
    Adept("Adept", 800, falador_invs.white_knight_adept),
    Master("Master", 1300, falador_invs.white_knight_master),
}

/**
 * The Wanted! kill score: +1 for a Black Knight, +12 for an Elite Black Knight, -1 for a White
 * Knight (except once a Master). Wanted! counts as done, and finishing it gives 100, so the varp
 * holds the score above that and every account starts as a Novice.
 */
internal val Player.whiteKnightScore: Int
    get() = WANTED_SCORE + vars[falador_varps.white_knight_score]

internal val Player.whiteKnightRank: WhiteKnightRank?
    get() = rankOf(whiteKnightScore)

private fun rankOf(score: Int): WhiteKnightRank? =
    WhiteKnightRank.entries.lastOrNull { score >= it.score }

class WhiteKnightRankScript : PluginScript() {
    private val whiteKnightIds by lazy {
        with(falador_npcs) {
            setOf(white_knight, white_knight_yellow, white_knight_green, white_knight_blue)
                .map { it.id }
                .toSet()
        }
    }

    override fun ScriptContext.startup() {
        onEvent<NpcKilled> { scoreKill(hero, npc.type.id) }
    }

    private fun scoreKill(player: Player, npc: Int) {
        val change =
            when (npc) {
                in black_knights.ids -> 1
                in black_knights.elite_ids -> ELITE_SCORE
                in whiteKnightIds -> -1
                else -> return
            }
        val before = player.whiteKnightRank
        if (change < 0 && before == WhiteKnightRank.Master) {
            return
        }
        val score = player.vars[falador_varps.white_knight_score] + change
        VarPlayerIntMapSetter.set(player, falador_varps.white_knight_score, score)
        val after = player.whiteKnightRank
        if (after != null && after != before && change > 0) {
            player.mes("Congratulations! You are now a White Knight ${after.label}!")
        }
    }

    private companion object {
        const val ELITE_SCORE = 12
    }
}

private const val WANTED_SCORE = 100

/** Every attackable Black Knight; the Port Sarim jail one doesn't count. */
object black_knights : NpcReferences() {
    val black_knight = find("black_knight")
    private val aggressive = find("aggressive_black_knight")
    private val kinshra = find("kr_aggressive_black_knight")
    private val female = find("black_knight_f")
    private val aggressive_female = find("aggressive_black_knight_f")
    private val elite = find("elite_black_knight_1")
    private val elite2 = find("elite_black_knight_2")

    val ids by lazy {
        setOf(black_knight, aggressive, kinshra, female, aggressive_female).map { it.id }.toSet()
    }
    val elite_ids by lazy { setOf(elite.id, elite2.id) }
}
