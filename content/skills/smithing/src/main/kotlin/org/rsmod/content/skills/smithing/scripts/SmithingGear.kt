package org.rsmod.content.skills.smithing.scripts

import org.rsmod.api.player.feet
import org.rsmod.api.player.hands
import org.rsmod.api.player.legs
import org.rsmod.api.player.ring
import org.rsmod.api.player.torso
import org.rsmod.api.player.vars.intVarp
import org.rsmod.content.skills.smithing.configs.SmithingObjs
import org.rsmod.content.skills.smithing.configs.SmithingVarps
import org.rsmod.game.entity.Player
import org.rsmod.game.inv.isType

/**
 * Iron bars smelted on the worn ring of forging; it melts at
 * [SmithingGear.RING_OF_FORGING_CHARGES].
 */
var Player.ringOfForgingUsed: Int by intVarp(SmithingVarps.ring_of_forging_charges)

object SmithingGear {
    const val RING_OF_FORGING_CHARGES = 140

    fun hasRingOfForging(player: Player): Boolean =
        player.ring?.isType(SmithingObjs.ring_of_forging) == true

    fun hasGoldsmithGauntlets(player: Player): Boolean =
        player.hands?.isType(SmithingObjs.goldsmith_gauntlets) == true

    /** Smiths' outfit pieces worn, 0-4. */
    fun smithsOutfitPieces(player: Player): Int =
        listOf(
                player.torso?.isType(SmithingObjs.smiths_tunic),
                player.legs?.isType(SmithingObjs.smiths_trousers),
                player.feet?.isType(SmithingObjs.smiths_boots),
                player.hands?.isType(SmithingObjs.smiths_gloves),
            )
            .count { it == true }
}
