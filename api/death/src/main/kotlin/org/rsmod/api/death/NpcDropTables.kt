package org.rsmod.api.death

import jakarta.inject.Singleton
import org.rsmod.api.random.GameRandom
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.npc.NpcType
import org.rsmod.game.type.obj.ObjType

/**
 * One drop. In a [DropTable] it wins with chance `weight / table.total`; as an "always" drop the
 * weight is ignored. A drop is an obj ([obj] x [count]) or a sub-table roll ([table]). A
 * [members]-only drop gives [f2p] instead on a free world (null = nothing).
 */
public class Drop(
    public val obj: ObjType? = null,
    public val count: IntRange = 1..1,
    public val weight: Int = 0,
    public val table: DropTable? = null,
    public val members: Boolean = false,
    public val f2p: Drop? = null,
)

/** Weighted table out of [total]; weight not used by [drops] is "nothing". */
public class DropTable(public val total: Int, public val drops: List<Drop>) {
    init {
        require(drops.sumOf { it.weight } <= total) { "Drop weights exceed $total" }
    }

    public fun roll(random: GameRandom, members: Boolean, out: MutableList<InvObj>) {
        var roll = random.of(maxExclusive = total)
        for (drop in drops) {
            if (roll < drop.weight) {
                drop.give(random, members, out)
                return
            }
            roll -= drop.weight
        }
    }
}

public class NpcDrops(
    public val always: List<Drop> = emptyList(),
    public val main: DropTable? = null,
    public val f2pMain: DropTable? = main,
) {
    public fun roll(random: GameRandom, members: Boolean): List<InvObj> {
        val out = mutableListOf<InvObj>()
        for (drop in always) {
            drop.give(random, members, out)
        }
        (if (members) main else f2pMain)?.roll(random, members, out)
        return out
    }
}

private fun Drop.give(random: GameRandom, members: Boolean, out: MutableList<InvObj>) {
    val drop = if (this.members && !members) f2p ?: return else this
    drop.table?.let {
        it.roll(random, members, out)
        return
    }
    val obj = drop.obj ?: return
    val count = if (drop.count.first == drop.count.last) drop.count.first else random.of(drop.count)
    out += InvObj(obj, count)
}

/** Npc type id -> drops. Content registers tables at startup; unregistered npcs drop bones. */
@Singleton
public class NpcDropTables {
    private val tables = hashMapOf<Int, NpcDrops>()

    public operator fun get(type: NpcType): NpcDrops? = tables[type.id]

    public operator fun set(type: NpcType, drops: NpcDrops) {
        tables[type.id] = drops
    }
}
