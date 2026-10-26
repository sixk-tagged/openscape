package org.rsmod.api.player.ui

import org.rsmod.api.config.refs.components
import org.rsmod.api.config.refs.interfaces
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.player.output.ClientScripts
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.game.type.interf.IfEvent
import org.rsmod.game.type.interf.IfSubType
import org.rsmod.game.type.obj.ObjType

/** What the player clicked in the make-X chatbox: an index into the offered objs, and how many. */
public data class SkillMultiChoice(val index: Int, val amount: Int)

/** The make-X chatbox has 10 item buttons in this cache revision. */
public const val SKILLMULTI_OPTIONS: Int = 10

/**
 * Opens the OSRS make-X chatbox (interface `skillmulti`, cache script 2046) offering [objs] named
 * [names], and suspends until the player clicks one. Returns null if they click anything else.
 * [verb] picks the button wording (13 is "Smelt"; see cache script 2046 for the others).
 */
public suspend fun ProtectedAccess.skillMulti(
    verb: Int,
    title: String,
    objs: List<ObjType>,
    names: List<String>,
): SkillMultiChoice? {
    require(objs.size in 1..SKILLMULTI_OPTIONS) { "skillmulti shows 1-10 options: $objs" }
    vars[varbits.chatmodal_unclamp] = 1
    ClientScripts.topLevelChatboxResetBackground(player)
    ifOpenSub(interfaces.skillmulti, components.chatbox_chatmodal, IfSubType.Modal)
    for (button in BUTTONS.take(objs.size)) {
        ifSetEvents(button, 1..MAX_AMOUNT, IfEvent.PauseButton)
    }
    val ids = objs.map { it.id } + List(SKILLMULTI_OPTIONS - objs.size) { -1 }
    val labels = (listOf(title) + names).joinToString("|")
    runClientScript(SKILLMULTI_SETUP, verb, labels, MAX_AMOUNT, *ids.toTypedArray(), MAX_AMOUNT)

    val input = pauseButton()
    ifClose()
    val index = BUTTONS.indexOfFirst { it.isType(input.component) }
    if (index !in objs.indices) {
        return null
    }
    return SkillMultiChoice(index, input.subcomponent.coerceIn(1, MAX_AMOUNT))
}

private const val SKILLMULTI_SETUP = 2046
private const val MAX_AMOUNT = 28

private val BUTTONS =
    with(components) {
        listOf(
            skillmulti_a,
            skillmulti_b,
            skillmulti_c,
            skillmulti_d,
            skillmulti_e,
            skillmulti_f,
            skillmulti_g,
            skillmulti_h,
            skillmulti_i,
            skillmulti_j,
        )
    }
