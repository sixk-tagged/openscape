package org.rsmod.api.player.events.interact

import org.rsmod.events.KeyedEvent
import org.rsmod.game.entity.Player
import org.rsmod.game.obj.Obj
import org.rsmod.game.type.comp.ComponentType

/**
 * openscape: a spell, in target mode, cast on a ground obj (Telekinetic Grab). It is published as
 * soon as the click arrives: spells reach their target from afar, so there is no walk to the obj
 * first. Keyed by the spell's component.
 */
public class SpellOnObj(
    public val player: Player,
    public val obj: Obj,
    public val component: ComponentType,
) : KeyedEvent {
    override val id: Long = component.packed.toLong()
}
