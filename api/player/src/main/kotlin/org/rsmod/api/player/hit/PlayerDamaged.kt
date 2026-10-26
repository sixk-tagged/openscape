package org.rsmod.api.player.hit

import org.rsmod.events.UnboundEvent
import org.rsmod.game.entity.Player
import org.rsmod.game.hit.Hit

/**
 * openscape: published when a queued [hit] lands on [target] and takes [damage] hitpoints (0 for a
 * blocked hit), before the death check. The attacker is `hit.resolvePlayerSource(players)` or
 * `hit.resolveNpcSource(npcs)`.
 *
 * On-hit effects (recoil, Vengeance, Smite, Redemption, set effects) subscribe to this. Damage they
 * deal back must use `takeInstantHit`, whose default processor does not publish this event, so
 * effects can't trigger each other in a loop.
 */
public data class PlayerDamaged(val target: Player, val hit: Hit, val damage: Int) : UnboundEvent
