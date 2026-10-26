package org.rsmod.content.other.levelup

import jakarta.inject.Inject
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.components
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.advanced.onAdvanceStat
import org.rsmod.api.type.refs.comp.ComponentReferences
import org.rsmod.api.type.refs.interf.InterfaceReferences
import org.rsmod.api.type.refs.jingle.JingleReferences
import org.rsmod.api.type.refs.spot.SpotanimReferences
import org.rsmod.game.type.interf.IfEvent
import org.rsmod.game.type.interf.IfSubType
import org.rsmod.game.type.jingle.JingleType
import org.rsmod.game.type.stat.StatType
import org.rsmod.game.type.stat.StatTypeList
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** OSRS level-up: fireworks, the skill's jingle, a game message and the chatbox level-up box. */
class LevelUpScript @Inject constructor(private val statTypes: StatTypeList) : PluginScript() {
    override fun ScriptContext.startup() {
        onAdvanceStat { levelUp(it.args) }
    }

    // Note: no combat-level box (the stat event doesn't carry the old combat level) and no
    // "(Unlocks)" jingle variants (needs a per-skill unlock table).
    private suspend fun ProtectedAccess.levelUp(stat: StatType) {
        val type = statTypes[stat]
        val skill = type.internalName ?: return
        val name = type.displayName
        val level = statBase(stat)
        val fireworks =
            if (level >= 99) LevelUpSpotanims.fireworks_99 else LevelUpSpotanims.fireworks
        spotanim(fireworks, height = 100)
        LevelUpJingles.forStat(skill, level)?.let(::midiJingle)
        mes("Congratulations, you've just advanced your $name level. You are now level $level.")

        ifOpenSub(LevelUpInterfaces.display, components.chatbox_chatmodal, IfSubType.Modal)
        val article = if (name.first().lowercaseChar() in "aeiou") "an" else "a"
        ifSetText(
            LevelUpComponents.text1,
            "<col=000080>Congratulations, you just advanced $article $name level.",
        )
        ifSetText(LevelUpComponents.text2, "Your $name level is now $level.")
        ifSetText(LevelUpComponents.pause, constants.cm_pausebutton)
        ifSetEvents(LevelUpComponents.pause, -1..-1, IfEvent.PauseButton)
        LevelUpComponents.icons[skill]?.let { ifSetHide(it, false) }
        pauseButton()
        ifClose()
    }
}

/** Stats that play one jingle at every level; the rest are special-cased in [forStat]. */
private val PLAIN_JINGLE_STATS =
    listOf(
        "agility",
        "attack",
        "cooking",
        "crafting",
        "defence",
        "farming",
        "firemaking",
        "fishing",
        "fletching",
        "herblore",
        "magic",
        "mining",
        "prayer",
        "ranged",
        "runecrafting",
        "slayer",
        "smithing",
        "thieving",
        "woodcutting",
    )

/** Jingle ids from the OSRS wiki jingle list; names are ours (the cache has none). */
object LevelUpJingles : JingleReferences() {
    private val plain = PLAIN_JINGLE_STATS.associateWith { find("levelup_$it") }
    private val strength = find("levelup_strength")
    private val strength_high = find("levelup_strength_high")
    private val hitpoints = find("levelup_hitpoints")
    private val hitpoints_high = find("levelup_hitpoints_high")
    private val hunter = find("levelup_hunter")
    private val hunter_odd = find("levelup_hunter_odd")
    private val construction = find("levelup_construction")
    private val construction_tenth = find("levelup_construction_tenth")
    private val sailing = find("levelup_sailing")
    private val sailing_99 = find("levelup_sailing_99")

    fun forStat(skill: String, level: Int): JingleType? =
        when (skill) {
            "strength" -> if (level >= 50) strength_high else strength
            "hitpoints" -> if (level >= 50) hitpoints_high else hitpoints
            "hunter" -> if (level % 2 == 1) hunter_odd else hunter
            "construction" -> if (level % 10 == 0) construction_tenth else construction
            "sailing" -> if (level >= 99) sailing_99 else sailing
            else -> plain[skill]
        }
}

object LevelUpInterfaces : InterfaceReferences() {
    val display = find("levelup_display")
}

object LevelUpComponents : ComponentReferences() {
    val text1 = find("levelup_display:text1")
    val text2 = find("levelup_display:text2")
    val pause = find("levelup_display:continue")

    /** Hidden icon layer per skill; the cache has none for hunter or sailing. */
    val icons =
        (PLAIN_JINGLE_STATS + listOf("strength", "hitpoints", "construction")).associateWith {
            find("levelup_display:${if (it == "ranged") "ranging" else it}")
        }
}

object LevelUpSpotanims : SpotanimReferences() {
    val fireworks = find("levelup_anim")
    val fireworks_99 = find("levelup_99_anim")
}
