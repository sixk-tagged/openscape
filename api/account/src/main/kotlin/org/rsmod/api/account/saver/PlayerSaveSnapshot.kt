package org.rsmod.api.account.saver

import org.rsmod.game.entity.Player
import org.rsmod.game.inv.Inventory
import org.rsmod.game.type.inv.InvScope

/**
 * A detached copy of everything an account save writes (see `CharacterAccountRepository.save` and
 * the `CharacterDataStage.Pipeline`s), taken on the game thread so a save can't catch an item
 * halfway between two inventories. `PlayerSaveSnapshotTest` fails if a save step reads a field this
 * doesn't copy.
 */
public fun Player.saveSnapshot(): Player {
    val source = this
    val copy = Player()
    copy.accountId = source.accountId
    copy.characterId = source.characterId
    copy.coords = source.coords
    copy.displayName = source.displayName
    copy.lastKnownDevice = source.lastKnownDevice
    copy.modLevel = source.modLevel
    copy.lastLogin = source.lastLogin
    copy.runEnergy = source.runEnergy
    copy.xpRate = source.xpRate
    copy.vars.backing.putAll(source.vars.backing)
    copy.statMap.copyFrom(source.statMap)
    for (inventory in source.invMap.backing.values) {
        if (inventory.type.scope != InvScope.Perm) {
            continue
        }
        // InvObj is immutable: sharing the slot references is a safe copy.
        val into = Inventory.create(inventory.type)
        for (slot in inventory.indices) {
            into[slot] = inventory[slot]
        }
        copy.invMap[inventory.type] = into
    }
    return copy
}
