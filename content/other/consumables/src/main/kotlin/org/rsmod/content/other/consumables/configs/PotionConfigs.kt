package org.rsmod.content.other.consumables.configs

import org.rsmod.api.type.builders.varp.VarpBuilder
import org.rsmod.api.type.refs.timer.TimerReferences
import org.rsmod.api.type.refs.varp.VarpReferences

object PotionTimers : TimerReferences() {
    /** Ticks every cycle while a timed potion effect (stamina, antifire, regeneration...) is on. */
    val effects = find("potion_effects")
}

/** Absolute map clock cycles at which timed potion effects end (0 when off). */
object PotionVarps : VarpReferences() {
    val stamina_until = find("potion_stamina_until")
    val antifire_until = find("potion_antifire_until")
    val antifire_kind = find("potion_antifire_kind")
    val regen_until = find("potion_regen_until")
    val remedy_until = find("potion_remedy_until")
    val divine_until = find("potion_divine_until")
    val surge_ready = find("potion_surge_ready")
}

internal object PotionVarpBuilder : VarpBuilder() {
    init {
        build("potion_stamina_until") { temporary = true }
        build("potion_antifire_until") { temporary = true }
        build("potion_antifire_kind") { temporary = true }
        build("potion_regen_until") { temporary = true }
        build("potion_remedy_until") { temporary = true }
        build("potion_divine_until") { temporary = true }
        build("potion_surge_ready") { temporary = true }
    }
}
