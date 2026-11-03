package org.rsmod.content.other.deathsoffice

import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.annotations.InternalApi
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.areas
import org.rsmod.api.config.refs.objs
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.content.other.deathsoffice.configs.DeathInvs
import org.rsmod.content.other.deathsoffice.configs.DeathObjs
import org.rsmod.game.area.AreaIndex
import org.rsmod.game.entity.Player
import org.rsmod.game.inv.InvObj
import org.rsmod.game.obj.Obj
import org.rsmod.map.CoordGrid

object WildDeathObjs : ObjReferences() {
    val lobster = find("lobster")
    val strength_potion = find("3dose1strength")
}

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class WildernessDeathTest {
    @Test
    fun GameTestState.`a player's death in the Wilderness drops the loot for the killer`() =
        wildTest { deps ->
            val killer = startFight(deps, wilderness = true)
            die()

            assertNull(deps.graves.npc(player))
            assertEquals(emptySet<Int>(), graveObjs())
            // Unskulled, the three dearest items are kept.
            assertEquals(setOf(whip, platebody, mithril), carried())
            assertEquals(setOf(steel, iron, logs), floor(deps, killer))
            assertEquals(emptySet<Int>(), floor(deps, other()))
        }

    @Test
    fun GameTestState.`a skulled player keeps nothing`() = wildTest { deps ->
        val killer = startFight(deps, wilderness = true)
        player.skullIcon = constants.skullicon_default
        die()

        assertEquals(emptySet<Int>(), carried())
        assertEquals(setOf(whip, platebody, mithril, steel, iron, logs), floor(deps, killer))
    }

    @Test
    fun GameTestState.`the killer's loot turns public after a minute and is gone after five`() =
        wildTest { deps ->
            val killer = startFight(deps, wilderness = true)
            die()
            assertEquals(emptySet<Int>(), floor(deps, other()))

            advance(ticks = 100)
            assertEquals(setOf(steel, iron, logs), floor(deps, other()))

            advance(ticks = 420)
            assertEquals(emptySet<Int>(), floor(deps, killer))
        }

    @Test
    fun GameTestState.`dying with no player to blame in the Wilderness still makes a gravestone`() =
        wildTest { deps ->
            startFight(deps, wilderness = true, damage = 0)
            die()

            assertNotNull(deps.graves.npc(player))
            assertEquals(setOf(steel, iron, logs), graveObjs())
            assertEquals(emptySet<Int>(), floor(deps, other()))
        }

    @Test
    fun GameTestState.`a player's death outside the Wilderness still makes a gravestone`() =
        wildTest { deps ->
            val killer = startFight(deps, wilderness = false)
            die()

            assertNotNull(deps.graves.npc(player))
            assertEquals(setOf(steel, iron, logs), graveObjs())
            assertEquals(emptySet<Int>(), floor(deps, killer))
        }

    @Test
    fun GameTestState.`a looting bag is lost and its contents, bar food, go to the killer`() =
        wildTest { deps ->
            val killer = startFight(deps, wilderness = true)
            // With so little to keep, the bag would otherwise be one of the three kept.
            player.clearInv()
            player.inv[0] = InvObj(objs.abyssal_whip)
            player.inv[1] = InvObj(DeathObjs.looting_bag_open)
            fillBag()
            die()

            assertEquals(setOf(whip), carried())
            assertEquals(emptySet<Int>(), objIds(DeathInvs.looting_bag))
            assertEquals(setOf(bones, logs), floor(deps, killer))
            assertEquals(emptySet<Int>(), floor(deps, other()))
        }

    @Test
    fun GameTestState.`a looting bag's contents go to the grave when no player is to blame`() =
        wildTest { deps ->
            startFight(deps, wilderness = true, damage = 0)
            player.clearInv()
            player.inv[0] = InvObj(DeathObjs.looting_bag)
            fillBag()
            die()

            assertEquals(emptySet<Int>(), carried())
            assertEquals(setOf(bones, logs), graveObjs())
        }

    class WildDeps
    @Inject
    constructor(val graves: Gravestones, val areaIndex: AreaIndex, val objRepo: ObjRepository)

    private fun GameTestState.wildTest(test: GameTestScope.(WildDeps) -> Unit) =
        deathTestWith(WildDeps::class, body = test)

    /**
     * The player carries [carrySix], stands at [DEATH] and has been hurt by a second player
     * standing next to them for [damage] (none: the death has no player to blame).
     */
    @OptIn(InternalApi::class)
    private fun GameTestScope.startFight(
        deps: WildDeps,
        wilderness: Boolean,
        damage: Int = 10,
    ): Player {
        finishDeathTutorial()
        if (wilderness) {
            deps.areaIndex.registerAll(DEATH, listOf(areas.wilderness.id.toShort()).iterator())
        }
        player.teleport(DEATH)
        player.observerUUID = player.uuid
        val killer = registerPlayer(DEATH.translateX(1))
        killer.observerUUID = killer.uuid
        player.heroPoints(killer, damage)
        carrySix()
        return killer
    }

    /** Bones and logs, which survive a Wilderness death, and the food and potion that do not. */
    private fun GameTestScope.fillBag() {
        val bag = inv(DeathInvs.looting_bag)
        bag[0] = InvObj(objs.bones)
        bag[1] = InvObj(WildDeathObjs.lobster, 3)
        bag[2] = InvObj(WildDeathObjs.strength_potion)
        bag[3] = InvObj(objs.logs, 20)
    }

    /** A bystander who dealt no damage. */
    private fun GameTestScope.other(): Player =
        registerPlayer(DEATH.translateX(2)).also { it.observerUUID = it.uuid }

    /** The ids of the objs lying where the player died that [viewer] can see. */
    private fun GameTestScope.floor(deps: WildDeps, viewer: Player): Set<Int> =
        deps.objRepo.findAll(DEATH).filter { it.isVisibleTo(viewer) }.map(Obj::type).toSet()

    private companion object {
        val DEATH = CoordGrid(3000, 3525)
    }
}
