package org.rsmod.api.player.bonus

import org.rsmod.api.config.refs.varobjs
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.entity.Player
import org.rsmod.game.inv.InvObj
import org.rsmod.game.inv.isType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.Wearpos
import org.rsmod.utils.bits.getBits
import org.rsmod.utils.bits.withBits

/** Toxic blowpipe obj types and the darts it can hold, read from the blowpipe's obj vars. */
public object BlowpipeDarts : ObjReferences() {
    public val empty: ObjType = find("toxic_blowpipe")
    public val loaded: ObjType = find("toxic_blowpipe_loaded")

    /** `darttype` var `n` (1-based) is `darts[n - 1]`. */
    public val darts: List<ObjType> =
        listOf(
            find("bronze_dart"),
            find("iron_dart"),
            find("steel_dart"),
            find("mithril_dart"),
            find("adamant_dart"),
            find("rune_dart"),
            find("dragon_dart"),
        )

    public fun dartType(blowpipe: InvObj?): ObjType? {
        if (!blowpipe.isType(loaded)) return null
        val type = blowpipe.vars.getBits(varobjs.snakeboss_blowpipe_darrtype.bits)
        return darts.getOrNull(type - 1)
    }

    public fun dartCount(blowpipe: InvObj?): Int =
        blowpipe?.vars?.getBits(varobjs.snakeboss_blowpipe_dartcount.bits) ?: 0

    /**
     * Takes one dart out of the wielded blowpipe, leaving it empty but wieldable once the last dart
     * is gone.
     */
    public fun useDart(player: Player) {
        val slot = Wearpos.RightHand.slot
        val obj = player.worn[slot] ?: return
        val left = dartCount(obj) - 1
        val vars = obj.vars.withBits(varobjs.snakeboss_blowpipe_dartcount.bits, maxOf(left, 0))
        player.worn[slot] =
            if (left > 0) {
                obj.copy(vars = vars)
            } else {
                InvObj(loaded, vars = vars.withBits(varobjs.snakeboss_blowpipe_darrtype.bits, 0))
            }
    }

    /** [blowpipe] holding [count] of [dart]; always the loaded variant. */
    public fun withDarts(blowpipe: InvObj, dart: ObjType, count: Int): InvObj {
        val vars =
            blowpipe.vars
                .withBits(
                    varobjs.snakeboss_blowpipe_darrtype.bits,
                    darts.indexOfFirst { it.id == dart.id } + 1,
                )
                .withBits(varobjs.snakeboss_blowpipe_dartcount.bits, count)
        return InvObj(loaded, vars = vars)
    }
}
