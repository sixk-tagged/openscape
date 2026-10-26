package org.rsmod.content.skills.runecraft.configs

import org.rsmod.api.type.editors.inv.InvEditor
import org.rsmod.api.type.script.dsl.InvPluginBuilder
import org.rsmod.game.type.inv.InvScope
import org.rsmod.game.type.inv.InvStackType

/**
 * Aubury's Rune Shop stock, from the wiki: 5000 of each basic rune (restocking every 10 ticks),
 * chaos and death runes, and the rune packs. At 99 Runecraft he uses the cape copies, which add the
 * cape.
 */
internal object AuburyShopInvs : InvEditor() {
    private val objs = RunecraftObjs

    init {
        edit(RunecraftInvs.rune_shop) { stockRunes() }
        edit(RunecraftInvs.rune_shop_cape) {
            stockRunes()
            stock += stock(objs.runecrafting_cape, count = 1, restockCycles = 1)
        }
        edit(RunecraftInvs.rune_shop_cape_trimmed) {
            stockRunes()
            stock += stock(objs.runecrafting_cape_trimmed, count = 1, restockCycles = 1)
        }
    }

    private fun InvPluginBuilder.stockRunes() {
        scope = InvScope.Shared
        stack = InvStackType.Always
        autoSize = true
        restock = true
        val basics =
            listOf(
                objs.air_rune,
                objs.fire_rune,
                objs.water_rune,
                objs.earth_rune,
                objs.mind_rune,
                objs.body_rune,
            )
        for (rune in basics) {
            stock += stock(rune, count = 5000, restockCycles = 10)
        }
        stock += stock(objs.chaos_rune, count = 250, restockCycles = 10)
        stock += stock(objs.death_rune, count = 250, restockCycles = 15)
        val packs =
            listOf(
                objs.pack_fire_rune,
                objs.pack_water_rune,
                objs.pack_air_rune,
                objs.pack_earth_rune,
            )
        for (pack in packs) {
            stock += stock(pack, count = 80, restockCycles = 10)
        }
        stock += stock(objs.pack_mind_rune, count = 40, restockCycles = 10)
        stock += stock(objs.pack_chaos_rune, count = 35, restockCycles = 10)
    }
}
