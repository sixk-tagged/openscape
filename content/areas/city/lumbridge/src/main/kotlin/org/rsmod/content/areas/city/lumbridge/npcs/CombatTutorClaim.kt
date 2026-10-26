package org.rsmod.content.areas.city.lumbridge.npcs

import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.type.builders.varp.VarpBuilder
import org.rsmod.api.type.refs.varp.VarpReferences
import org.rsmod.game.entity.Player

object CombatTutorVarps : VarpReferences() {
    val last_claim = find("aide_tutor_claim_minute")
}

internal object CombatTutorVarpBuilder : VarpBuilder() {
    init {
        build("aide_tutor_claim_minute") { permanent = true }
    }
}

// Epoch minutes, not mapClock: mapClock restarts with the server.
private var Player.lastTutorClaim by intVarp(CombatTutorVarps.last_claim)

private fun epochMinute(): Int = (System.currentTimeMillis() / 60_000).toInt()

/** The Ranged and Magic tutors share one claim every 30 minutes. */
internal val Player.tutorClaimOnCooldown: Boolean
    get() = epochMinute() - lastTutorClaim < 30

internal fun Player.markTutorClaim() {
    lastTutorClaim = epochMinute()
}
