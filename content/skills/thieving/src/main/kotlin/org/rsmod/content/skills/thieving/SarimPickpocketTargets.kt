package org.rsmod.content.skills.thieving

import org.rsmod.api.config.refs.objs
import org.rsmod.api.type.refs.npc.NpcReferences

/** Port Sarim, Rimmington and Musa Point: the same numbers as the men and guards elsewhere. */
internal object SarimPickpocketTargets {
    val all =
        with(SarimNpcs) {
            listOf(
                PickpocketTargets.Target("man", men, 1, 8.0, 180, 240, 8) { _, _ ->
                    objs.coins to 3
                },
                PickpocketTargets.Target("woman", women, 1, 8.0, 180, 240, 8) { _, _ ->
                    objs.coins to 3
                },
                PickpocketTargets.Target("guard", jail_guards, 40, 46.8, 50, 240, 8, 2) { _, _ ->
                    objs.coins to 30
                },
            )
        }
}

internal object SarimNpcs : NpcReferences() {
    val men = listOf(find("man4_for_musa_point"), find("karamja_man"), find("rimmington_hengel"))
    val women = listOf(find("rimmington_anja"))
    val jail_guards =
        listOf(
            find("jail_guard_1"),
            find("jail_guard_2"),
            find("jail_guard_3"),
            find("jail_guard_4"),
            find("jail_guard_5"),
        )
}
