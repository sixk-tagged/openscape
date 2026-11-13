package org.rsmod.api.player.output

import net.rsprot.protocol.game.outgoing.misc.player.UpdateStockMarketSlot
import net.rsprot.protocol.game.outgoing.misc.player.UpdateStockMarketSlot.ResetStockMarketSlot
import net.rsprot.protocol.game.outgoing.misc.player.UpdateStockMarketSlot.SetStockMarketSlot
import org.rsmod.game.entity.Player

public object UpdateStockMarket {
    /**
     * Sets a Grand Exchange slot. [status] is the client's offer flags: 2 buying, 5 buy finished,
     * 10 selling, 13 sell finished. [obj] is the un-noted obj id.
     *
     * @see [UpdateStockMarketSlot]
     */
    public fun setSlot(
        player: Player,
        slot: Int,
        status: Int,
        obj: Int,
        price: Int,
        count: Int,
        completedCount: Int,
        completedGold: Int,
    ) {
        val set = SetStockMarketSlot(status, obj, price, count, completedCount, completedGold)
        player.client.write(UpdateStockMarketSlot(slot, set))
    }

    /** Clears a Grand Exchange slot. */
    public fun resetSlot(player: Player, slot: Int) {
        player.client.write(UpdateStockMarketSlot(slot, ResetStockMarketSlot))
    }
}
