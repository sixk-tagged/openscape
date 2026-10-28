package org.rsmod.api.net.rsprot.handlers

import jakarta.inject.Inject
import net.rsprot.protocol.game.incoming.objs.OpObjT
import org.rsmod.api.net.rsprot.player.InterfaceEvents
import org.rsmod.api.player.events.interact.SpellOnObj
import org.rsmod.api.player.protect.clearPendingAction
import org.rsmod.api.registry.obj.ObjRegistry
import org.rsmod.events.EventBus
import org.rsmod.game.entity.Player
import org.rsmod.game.type.comp.ComponentTypeList
import org.rsmod.game.type.interf.IfEvent
import org.rsmod.game.type.interf.InterfaceTypeList
import org.rsmod.game.ui.Component
import org.rsmod.map.CoordGrid

/** openscape: a component (a spell) targeting a ground obj. Only publishes [SpellOnObj]. */
class OpObjTHandler
@Inject
constructor(
    private val eventBus: EventBus,
    private val objRegistry: ObjRegistry,
    private val componentTypes: ComponentTypeList,
    private val interfaceTypes: InterfaceTypeList,
) : MessageHandler<OpObjT> {
    override fun handle(player: Player, message: OpObjT) {
        if (player.isDelayed) {
            return
        }
        val selected = Component(message.selectedInterfaceId, message.selectedComponentId)
        val interfaceType = interfaceTypes[selected]
        val isValidInterface =
            player.ui.containsOverlay(interfaceType) || player.ui.containsModal(interfaceType)
        if (!isValidInterface) {
            return
        }
        val componentType = componentTypes[selected]
        val enabled =
            InterfaceEvents.isEnabled(player.ui, componentType, message.selectedSub, IfEvent.TgtObj)
        if (!enabled) {
            return
        }
        val coords = CoordGrid(message.x, message.z, player.level)
        val obj =
            objRegistry.findAll(coords).firstOrNull {
                it.type == message.id && it.isVisibleTo(player)
            } ?: return
        player.clearPendingAction(eventBus)
        player.resetFaceEntity()
        eventBus.publish(SpellOnObj(player, obj, componentType))
    }
}
