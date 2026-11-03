package org.rsmod.content.areas.misc.wilderness.rules

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.objs
import org.rsmod.api.player.events.interact.HeldDropEvents
import org.rsmod.api.player.events.interact.HeldObjEvents
import org.rsmod.api.player.events.interact.HeldUDefaultEvents
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.inv.InvObj
import org.rsmod.game.inv.Inventory
import org.rsmod.game.type.interf.IfButtonOp
import org.rsmod.game.type.inv.UnpackedInvType
import org.rsmod.map.CoordGrid

object LootingBagTestObjs : ObjReferences() {
    val lobster = find("lobster")
}

private lateinit var bagInv: UnpackedInvType

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class LootingBagTest {
    @Test
    fun GameTestState.`an item used on the bag is stored in the Wilderness and refused outside`() =
        bagTest { deps ->
            deps.wilderness(INSIDE)
            player.setVarBit(LootingBagVarBits.store_all, 1)
            player.inv[0] = InvObj(LootingBagObjs.closed)
            player.inv[1] = InvObj(objs.coins, 100)
            player.inv[2] = InvObj(objs.iron_battleaxe)

            player.teleport(OUTSIDE)
            useOnBag(slot = 1)
            assertEquals(100, player.inv.count(objs.coins))
            assertEquals(0, bag().filterNotNull().size)

            player.teleport(INSIDE)
            useOnBag(slot = 1)
            useOnBag(slot = 2)
            assertEquals(0, player.inv.count(objs.coins))
            assertEquals(0, player.inv.count(objs.iron_battleaxe))
            assertEquals(listOf(objs.coins.id, objs.iron_battleaxe.id), bag().ids())
            assertEquals(100, bag()[0]?.count)

            // The bag does not hold itself.
            player.inv[3] = InvObj(LootingBagObjs.open)
            useOnBag(slot = 3)
            assertEquals(2, bag().filterNotNull().size)
        }

    @Test
    fun GameTestState.`the deposit screen stores a stack with Store-All`() = bagTest { deps ->
        deps.wilderness(INSIDE)
        player.teleport(INSIDE)
        player.inv[0] = InvObj(LootingBagObjs.closed)
        player.inv[1] = InvObj(objs.coins, 250)

        player.withProtectedAccess {
            val type = objTypes[LootingBagObjs.closed]
            eventBus.publish(this, HeldObjEvents.Op3(0, inv[0]!!, type, inv))
        }
        advance()
        assertTrue(player.ui.containsModal(LootingBagInterfaces.bag))

        player.ifButton(LootingBagComponents.items, comsub = 1, op = IfButtonOp.Op3)
        advance()
        assertEquals(0, player.inv.count(objs.coins))
        assertEquals(250, bag()[0]?.count)
    }

    @Test
    fun GameTestState.`destroying the bag drops what it held in the Wilderness but not food`() =
        bagTest { deps ->
            deps.wilderness(INSIDE)
            player.teleport(INSIDE)
            bag()[0] = InvObj(objs.coins, 40)
            bag()[1] = InvObj(LootingBagTestObjs.lobster, 3)
            destroyBag()
            assertEquals(emptyList<Int>(), bag().ids())
            val floor = deps.objRepo.findAll(INSIDE).map { it.type to it.count }.toSet()
            assertEquals(setOf(objs.coins.id to 40), floor)
        }

    @Test
    fun GameTestState.`destroying the bag outside the Wilderness loses everything in it`() =
        bagTest { deps ->
            player.teleport(OUTSIDE)
            bag()[0] = InvObj(objs.coins, 40)
            destroyBag()
            assertEquals(emptyList<Int>(), bag().ids())
            assertEquals(0, deps.objRepo.findAll(OUTSIDE).count())
        }

    /** Uses the pack item in [slot] on the bag in slot 0, as the client would. */
    private fun GameTestScope.useOnBag(slot: Int) {
        player.withProtectedAccess {
            val bagType = objTypes[LootingBagObjs.closed]
            val item = inv[slot] ?: return@withProtectedAccess
            val itemType = objTypes[item]
            eventBus.publish(this, HeldUDefaultEvents.Type(bagType, 0, itemType, slot))
        }
        advance()
    }

    private fun GameTestScope.destroyBag() {
        val type = objTypes[LootingBagObjs.closed]
        eventBus.publish(HeldDropEvents.Destroy(player, 0, InvObj(type), type))
    }

    private fun GameTestState.bagTest(test: GameTestScope.(WildDeps) -> Unit) {
        bagInv = cacheTypes.invs[LootingBagInvs.bag]
        wildernessTest(LootingBagScript::class, test = test)
    }

    private fun GameTestScope.bag() = player.invMap.getOrPut(bagInv)

    private fun Inventory.ids(): List<Int> = filterNotNull().map { it.id }

    private companion object {
        val INSIDE = CoordGrid(3000, 3600)
        val OUTSIDE = CoordGrid(3000, 3500)
    }
}
