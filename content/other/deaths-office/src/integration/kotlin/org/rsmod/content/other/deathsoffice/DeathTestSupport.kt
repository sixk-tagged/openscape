package org.rsmod.content.other.deathsoffice

import kotlin.reflect.KClass
import org.rsmod.api.config.refs.components
import org.rsmod.api.config.refs.interfaces
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.death.plugin.PlayerDeathScript
import org.rsmod.api.player.queueDeath
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.content.other.deathsoffice.configs.DeathInvs
import org.rsmod.content.other.deathsoffice.configs.DeathVarps
import org.rsmod.game.entity.player.SessionStateEvent
import org.rsmod.game.inv.InvObj
import org.rsmod.game.inv.Inventory
import org.rsmod.game.type.inv.InvType
import org.rsmod.game.type.inv.InvTypeList
import org.rsmod.plugin.scripts.PluginScript

internal val whip: Int
    get() = objs.abyssal_whip.id
internal val platebody: Int
    get() = objs.rune_platebody.id
internal val mithril: Int
    get() = objs.mithril_battleaxe.id
internal val steel: Int
    get() = objs.steel_battleaxe.id
internal val iron: Int
    get() = objs.iron_battleaxe.id
internal val logs: Int
    get() = objs.logs.id
internal val bones: Int
    get() = objs.bones.id

private lateinit var invTypes: InvTypeList

/** Runs [body] with the death, gravestone and any extra [scripts] loaded. */
internal fun GameTestState.deathTest(
    vararg scripts: KClass<out PluginScript>,
    body: GameTestScope.(Gravestones) -> Unit,
) {
    invTypes = cacheTypes.invs
    runInjectedGameTest(
        Gravestones::class,
        null,
        PlayerDeathScript::class,
        GravestoneScript::class,
        *scripts,
        testBody = body,
    )
}

/**
 * Carries six objs whose shop values order them whip > platebody > mithril > steel > iron > logs.
 */
internal fun GameTestScope.carrySix() {
    player.inv[0] = InvObj(objs.logs)
    player.inv[1] = InvObj(objs.iron_battleaxe)
    player.inv[2] = InvObj(objs.abyssal_whip)
    player.inv[3] = InvObj(objs.steel_battleaxe)
    player.inv[4] = InvObj(objs.rune_platebody)
    player.inv[5] = InvObj(objs.mithril_battleaxe)
}

internal fun GameTestScope.die() {
    player.setCurrentLevel(stats.hitpoints, 0)
    player.queueDeath()
    advance(ticks = 12)
}

internal fun GameTestScope.finishDeathTutorial() {
    player.setVarp(DeathVarps.death_tutorial, 8)
}

internal fun GameTestScope.logout() {
    eventBus.publish(SessionStateEvent.Logout(player))
    unregisterPlayer(player)
}

internal fun GameTestScope.login() {
    registerPlayer(player = player)
}

internal fun GameTestScope.carried(): Set<Int> =
    (player.inv.filterNotNull() + player.worn.filterNotNull()).map { it.id }.toSet()

internal fun GameTestScope.inv(type: InvType): Inventory = player.invMap.getOrPut(invTypes[type])

internal fun GameTestScope.objIds(type: InvType): Set<Int> =
    player.invMap[type]?.filterNotNull()?.map { it.id }?.toSet().orEmpty()

internal fun GameTestScope.graveObjs(): Set<Int> = objIds(DeathInvs.gravestone)

/**
 * Clicks through the open chatbox dialogue, picking [options] in order on each menu, until the
 * chatbox closes.
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
            else -> {
                check(!choices.hasNext()) { "Dialogue ended with options left over" }
                return
            }
        }
    }
    error("Dialogue did not end")
}
