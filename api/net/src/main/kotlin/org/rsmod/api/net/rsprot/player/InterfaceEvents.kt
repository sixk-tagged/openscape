package org.rsmod.api.net.rsprot.player

import org.rsmod.game.type.comp.UnpackedComponentType
import org.rsmod.game.type.interf.IfEvent
import org.rsmod.game.ui.UserInterfaceMap

internal object InterfaceEvents {
    fun isEnabled(
        ui: UserInterfaceMap,
        component: UnpackedComponentType,
        comsub: Int,
        event: IfEvent,
    ): Boolean {
        val verifyStaticEvents = comsub == -1
        return if (verifyStaticEvents) {
            component.hasEvent(event) || event == IfEvent.Op1 && component.isLegacyButton
        } else {
            ui.hasEvent(component, comsub, event)
        }
    }

    /**
     * Legacy (IF1) OK/select buttons carry no op flags in the cache; the client sends their one
     * click as `IF_BUTTON1`. The tanner interface is made of them.
     */
    private val UnpackedComponentType.isLegacyButton: Boolean
        get() = !v3 && buttonType in LEGACY_BUTTON_TYPES

    private val LEGACY_BUTTON_TYPES = setOf(1, 4, 5)
}
