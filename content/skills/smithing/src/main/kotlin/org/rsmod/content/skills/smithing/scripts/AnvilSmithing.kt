package org.rsmod.content.skills.smithing.scripts

import jakarta.inject.Inject
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.smithingLvl
import org.rsmod.api.player.vars.intVarBit
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.script.onIfModalButton
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLocU
import org.rsmod.api.script.onPlayerQueueWithArgs
import org.rsmod.content.skills.smithing.configs.SmithingComponents
import org.rsmod.content.skills.smithing.configs.SmithingContent
import org.rsmod.content.skills.smithing.configs.SmithingInterfaces
import org.rsmod.content.skills.smithing.configs.SmithingLocs
import org.rsmod.content.skills.smithing.configs.SmithingObjs
import org.rsmod.content.skills.smithing.configs.SmithingQueues
import org.rsmod.content.skills.smithing.configs.SmithingSeqs
import org.rsmod.content.skills.smithing.configs.SmithingVarBits
import org.rsmod.content.skills.smithing.configs.SmithingVarps
import org.rsmod.game.entity.Player
import org.rsmod.game.type.comp.ComponentType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.isType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

private var Player.smithingBarType: Int by intVarBit(SmithingVarBits.smithing_bar_type)
private var Player.makeQuantity: Int by intVarp(SmithingVarps.makexcrafting)

class AnvilSmithing @Inject constructor(private val products: AnvilProducts) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1(SmithingContent.anvil) { openBestMetal() }
        for ((metal, bar) in AnvilTable.bars.withIndex()) {
            onOpLocU(SmithingContent.anvil, bar) { openAnvil(metal) }
        }
        onOpLoc1(SmithingLocs.lumbridge_anvil) { openRustedAnvil() }
        onOpLocU(SmithingLocs.lumbridge_anvil) { useOnRustedAnvil(it.objType) }
        for (button in AnvilTable.products.keys) {
            onIfModalButton(button) { smith(it.component) }
        }
        onIfModalButton(SmithingComponents.make_1) { player.makeQuantity = 1 }
        onIfModalButton(SmithingComponents.make_5) { player.makeQuantity = 5 }
        onIfModalButton(SmithingComponents.make_10) { player.makeQuantity = 10 }
        onIfModalButton(SmithingComponents.make_all) { player.makeQuantity = MAX_QUANTITY }
        onIfModalButton(SmithingComponents.make_x) {
            player.makeQuantity = countDialog().coerceIn(1, MAX_QUANTITY)
        }
        onPlayerQueueWithArgs<AnvilTask>(SmithingQueues.anvil) { hammer(it.args) }
    }

    /** "Smith" picks the best metal carried that the player can work, else any metal carried. */
    private fun ProtectedAccess.openBestMetal() {
        val carried = AnvilTable.bars.indices.filter { AnvilTable.bars[it] in inv }
        val metal =
            carried.lastOrNull { player.smithingLvl >= AnvilTable.metalLevels[it] }
                ?: carried.firstOrNull()
        if (metal == null) {
            mes(NO_BARS)
            return
        }
        openAnvil(metal)
    }

    private fun ProtectedAccess.openRustedAnvil() {
        if (SmithingObjs.bronze_bar !in inv) {
            mes(NO_BRONZE)
            return
        }
        openAnvil(BRONZE)
    }

    private fun ProtectedAccess.useOnRustedAnvil(obj: ObjType) {
        when {
            obj.isType(SmithingObjs.bronze_bar) -> openAnvil(BRONZE)
            AnvilTable.bars.any { obj.isType(it) } -> mes(BRONZE_ONLY)
            else -> mes("Nothing interesting happens.")
        }
    }

    private fun ProtectedAccess.openAnvil(metal: Int) {
        if (SmithingObjs.hammer !in inv) {
            mes(NO_HAMMER)
            return
        }
        val level = AnvilTable.metalLevels[metal]
        if (player.smithingLvl < level) {
            mes("You need a Smithing level of $level to work ${AnvilTable.metals[metal]} bars.")
            return
        }
        player.smithingBarType = metal + 1
        ifOpenMainModal(SmithingInterfaces.smithing)
    }

    private fun ProtectedAccess.smith(button: ComponentType) {
        val product = products.find(button, player.smithingBarType - 1) ?: return
        // The client hides these behind Tourist Trap / Death Plateau; refuse a forged click too.
        val questLocked =
            button.isType(SmithingComponents.darttips) &&
                vars[SmithingVarps.desertrescue] < TOURIST_TRAP_DONE ||
                button.isType(SmithingComponents.claws) &&
                    vars[SmithingVarps.death_equiproom] < DEATH_PLATEAU_DONE
        if (questLocked) {
            mes(QUEST_LOCKED)
            return
        }
        ifClose() // Clears weak queues: must come before the queue below.
        if (canSmith(product)) {
            anim(SmithingSeqs.anvil)
            val amount = player.makeQuantity.coerceIn(1, MAX_QUANTITY)
            weakQueue(SmithingQueues.anvil, queueDelay(ticksPerItem()), AnvilTask(product, amount))
        }
    }

    private fun ProtectedAccess.hammer(task: AnvilTask) {
        val product = task.product
        if (!canSmith(product)) {
            resetAnim()
            return
        }
        invDel(inv, AnvilTable.bars[product.metal], product.bars)
        invAdd(inv, product.obj, product.makes)
        statAdvance(stats.smithing, product.bars * AnvilTable.xpPerBar[product.metal])
        spam("You hammer the ${AnvilTable.metals[product.metal]} and make ${described(product)}.")
        if (task.remaining > 1 && hasMaterials(product)) {
            anim(SmithingSeqs.anvil)
            weakQueue(
                SmithingQueues.anvil,
                queueDelay(ticksPerItem()),
                task.copy(remaining = task.remaining - 1),
            )
        }
    }

    private fun ProtectedAccess.canSmith(product: AnvilProduct): Boolean {
        val name = withArticle(product.obj.name.lowercase())
        if (SmithingObjs.hammer !in inv) {
            mes(NO_HAMMER)
            return false
        }
        if (player.smithingLvl < product.level) {
            mes("You need a Smithing level of ${product.level} to make $name.")
            return false
        }
        if (invTotal(inv, AnvilTable.bars[product.metal]) < product.bars) {
            mes("You don't have enough ${AnvilTable.metals[product.metal]} bars to make $name.")
            return false
        }
        return true
    }

    private fun ProtectedAccess.hasMaterials(product: AnvilProduct): Boolean =
        SmithingObjs.hammer in inv && invTotal(inv, AnvilTable.bars[product.metal]) >= product.bars

    /** 5 ticks; each Smiths' outfit piece gives a 20% chance of 4, the full set always 4. */
    private fun ProtectedAccess.ticksPerItem(): Int {
        val pieces = SmithingGear.smithsOutfitPieces(player)
        val fast = pieces == 4 || pieces > 0 && random.of(1, 100) <= pieces * 20
        return if (fast) 4 else 5
    }

    private fun described(product: AnvilProduct): String {
        val name = product.obj.name.lowercase()
        return if (product.makes > 1) "some $name" else withArticle(name)
    }

    private data class AnvilTask(val product: AnvilProduct, val remaining: Int)

    companion object {
        const val NO_HAMMER = "You need a hammer to work the metal with."
        const val NO_BARS = "You need some metal bars to smith on this anvil."
        const val NO_BRONZE = "You need a bronze bar to smith equipment on this anvil."
        const val BRONZE_ONLY = "You can only use bronze bars to smith on this anvil."
        const val QUEST_LOCKED = "You need to complete a quest before you can make that."
        private const val BRONZE = 0
        private const val MAX_QUANTITY = 28
        private const val TOURIST_TRAP_DONE = 16
        private const val DEATH_PLATEAU_DONE = 80
    }
}
