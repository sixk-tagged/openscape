package org.rsmod.content.areas.city.edgeville.npcs

import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.hat
import org.rsmod.api.player.legs
import org.rsmod.api.player.torso
import org.rsmod.api.script.onOpNpc1
import org.rsmod.content.areas.city.edgeville.configs.edgeville_npcs
import org.rsmod.content.areas.city.edgeville.configs.edgeville_objs
import org.rsmod.game.entity.Player
import org.rsmod.game.type.obj.ObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The Black Knights' Fortress guards. The guards' entrance only opens for someone dressed as a
 * guard (see [wearingGuardDisguise]); the guards themselves tell everyone else off.
 */
class Fortress : PluginScript() {
    override fun ScriptContext.startup() {
        for (guard in edgeville_npcs.fortress_guards) {
            onOpNpc1(guard) {
                val disguised = player.wearingGuardDisguise()
                startDialogue(it.npc) {
                    if (disguised) {
                        chatNpc(angry, "Hey! Get back on duty!")
                    } else {
                        fortressGuardWarning()
                    }
                }
            }
        }
    }
}

private const val NONE = -1

private val GUARD_HELMS = ids(edgeville_objs.bronze_med_helm)
private val GUARD_BODIES = ids(edgeville_objs.iron_chainbody)
private val BLACK_HELMS =
    ids(
        edgeville_objs.black_full_helm,
        edgeville_objs.black_full_helm_trim,
        edgeville_objs.black_full_helm_gold,
    )
private val BLACK_BODIES =
    ids(
        edgeville_objs.black_platebody,
        edgeville_objs.black_platebody_trim,
        edgeville_objs.black_platebody_gold,
    )
private val BLACK_LEGS =
    ids(
        edgeville_objs.black_platelegs,
        edgeville_objs.black_platelegs_trim,
        edgeville_objs.black_platelegs_gold,
        edgeville_objs.black_plateskirt,
        edgeville_objs.black_plateskirt_trim,
        edgeville_objs.black_plateskirt_gold,
    )
private val ELITE_HELMS = ids(edgeville_objs.elite_black_full_helm)
private val ELITE_BODIES = ids(edgeville_objs.elite_black_platebody)
private val ELITE_LEGS = ids(edgeville_objs.elite_black_platelegs)

private fun ids(vararg types: ObjType): Set<Int> = types.map { it.id }.toSet()

/**
 * A bronze medium helm and an iron chainbody, or a full black set (plain, trimmed or gold), or an
 * elite black set. Note: the Dark Squall robes are not accepted.
 */
internal fun Player.wearingGuardDisguise(): Boolean {
    val head = hat?.id ?: NONE
    val chest = torso?.id ?: NONE
    val shins = legs?.id ?: NONE
    val guard = head in GUARD_HELMS && chest in GUARD_BODIES
    val black = head in BLACK_HELMS && chest in BLACK_BODIES && shins in BLACK_LEGS
    val elite = head in ELITE_HELMS && chest in ELITE_BODIES && shins in ELITE_LEGS
    return guard || black || elite
}

/** What a fortress guard says to someone at the guards' entrance without the right uniform. */
internal suspend fun Dialogue.fortressGuardWarning() {
    val guard = edgeville_npcs.fortress_guards.first()
    chatNpcSpecific(
        "Fortress Guard",
        guard,
        angry,
        "Hey! You can't come in here! This is a high security military installation!",
    )
    val choice = choice2("Yes, but I work here!", 1, "Oh, sorry.", 2)
    if (choice == 2) {
        chatPlayer(neutral, "Oh, sorry.")
        return
    }
    chatPlayer(neutral, "Yes, but I work here!")
    chatNpcSpecific(
        "Fortress Guard",
        guard,
        angry,
        "Well, this is the guards' entrance. I might be new here but I can tell you're not a " +
            "guard.",
    )
    chatNpcSpecific(
        "Fortress Guard",
        guard,
        angry,
        "You're not even wearing proper guards uniform!",
    )
    chatPlayer(quiz, "So what is this uniform?")
    chatNpcSpecific(
        "Fortress Guard",
        guard,
        neutral,
        "It's an iron chainbody and a medium bronze helm.",
    )
}
