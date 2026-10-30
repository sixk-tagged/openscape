package org.rsmod.api.death

import org.rsmod.events.UnboundEvent
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Player

/**
 * openscape: published when [npc] dies with a [hero] (the player credited with the kill, who gets
 * the drops), just before its drops spawn. Kill counters (e.g. the White Knight rank) subscribe to
 * this.
 */
public data class NpcKilled(val npc: Npc, val hero: Player) : UnboundEvent
