package org.rsmod.content.areas.city.portsarim

import org.rsmod.api.config.refs.components
import org.rsmod.api.config.refs.interfaces
import org.rsmod.api.testing.scope.GameTestScope

/**
 * Clicks through the open chatbox dialogue, picking [options] in order on each menu, until the
 * chatbox closes and the dialogue script is done, or another interface (a shop) takes its place.
 */
internal fun GameTestScope.talkThrough(vararg options: Int) {
    val choices = options.iterator()
    repeat(200) {
        advance(ticks = 1)
        val ui = player.ui
        when {
            ui.containsModal(interfaces.chatmenu) ->
                player.resumePauseButton(components.chatmenu_pbutton, choices.next())
            ui.containsModal(interfaces.chat_left) ->
                player.resumePauseButton(components.chat_left_pbutton)
            ui.containsModal(interfaces.chat_right) ->
                player.resumePauseButton(components.chat_right_pbutton)
            ui.containsModal(interfaces.messagebox) ->
                player.resumePauseButton(components.messagebox_pbutton)
            ui.containsModal(interfaces.obj_dialogue) ->
                player.resumePauseButton(components.objectbox_pbutton)
            ui.containsModal(interfaces.double_obj_dialogue) ->
                player.resumePauseButton(components.objectbox_double_pbutton)
            // Still walking over, or between two dialogue screens.
            player.interaction != null || player.isAccessProtected -> Unit
            else -> {
                check(!choices.hasNext()) { "Dialogue ended with options left over" }
                return
            }
        }
    }
    error("Dialogue did not end")
}
