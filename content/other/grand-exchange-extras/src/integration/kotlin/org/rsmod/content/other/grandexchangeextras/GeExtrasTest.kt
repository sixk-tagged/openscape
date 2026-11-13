package org.rsmod.content.other.grandexchangeextras

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.api.type.refs.interf.InterfaceReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.obj.ObjType
import org.rsmod.map.CoordGrid

object GeExtrasTestObjs : ObjReferences() {
    val cannon_base = find("twpart1")
    val cannon_stand = find("twpart2")
    val cannon_barrels = find("twpart3")
    val cannon_furnace = find("twpart4")
    val cannon_set = find("set_cannon")
    val ahrim_hood = find("barrows_ahrim_head")
    val ahrim_hood_damaged = find("barrows_ahrim_head_100")
    val ahrim_top = find("barrows_ahrim_body")
    val ahrim_skirt = find("barrows_ahrim_legs")
    val ahrim_staff = find("barrows_ahrim_weapon")
    val ahrim_set = find("set_barrows_ahrim")
    val prayer_4 = find("4doseprayerrestore")
    val prayer_3 = find("3doseprayerrestore")
    val prayer_2 = find("2doseprayerrestore")
    val prayer_1 = find("1doseprayerrestore")
}

object GeExtrasTestInterfaces : InterfaceReferences() {
    val price_checker = find("ge_pricechecker")
}

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class GeExtrasTest {
    private val cannonParts =
        listOf(
            GeExtrasTestObjs.cannon_base,
            GeExtrasTestObjs.cannon_stand,
            GeExtrasTestObjs.cannon_barrels,
            GeExtrasTestObjs.cannon_furnace,
        )

    private fun GameTestScope.openSets() {
        val clerk = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[GeExtrasNpcs.ge_clerk_1])
        player.teleport(OPEN_GROUND)
        advance()
        player.opNpc5(clerk)
        advanceUntil({ player.ui.containsModal(GeExtrasInterfaces.item_sets) })
    }

    private fun GameTestScope.noted(type: ObjType, count: Int = 1) =
        InvObj(objTypes.cert(objTypes[type]), count)

    @Test
    fun GameTestState.`packing the cannon parts gives the set`() =
        runGameTest(ItemSetsScript::class) {
            player.clearInv()
            cannonParts.forEachIndexed { slot, part -> player.inv[slot] = InvObj(part) }
            openSets()

            // The cannon is the first set the cache lists.
            player.ifButton(GeExtrasComponents.item_sets_list, comsub = 0)
            advance()

            assertEquals(1, player.count(GeExtrasTestObjs.cannon_set))
            cannonParts.forEach { assertEquals(0, player.count(it)) }
        }

    @Test
    fun GameTestState.`packing noted parts gives a noted set`() =
        runGameTest(ItemSetsScript::class) {
            player.clearInv()
            cannonParts.forEachIndexed { slot, part -> player.inv[slot] = noted(part) }
            openSets()

            player.ifButton(GeExtrasComponents.item_sets_list, comsub = 0)
            advance()

            assertEquals(0, player.count(GeExtrasTestObjs.cannon_set))
            assertEquals(1, player.count(objTypes.cert(objTypes[GeExtrasTestObjs.cannon_set])))
            assertEquals(1, player.inv.count { it != null })
        }

    @Test
    fun GameTestState.`unpacking a set gives its parts, noted if the set was`() =
        runGameTest(ItemSetsScript::class) {
            player.clearInv()
            player.inv[0] = InvObj(GeExtrasTestObjs.cannon_set)
            player.inv[1] = noted(GeExtrasTestObjs.cannon_set)
            openSets()

            player.ifButton(GeExtrasComponents.item_sets_side_items, comsub = 0)
            advance()
            cannonParts.forEach { assertEquals(1, player.count(it)) }

            player.ifButton(GeExtrasComponents.item_sets_side_items, comsub = 1)
            advance()
            cannonParts.forEach { assertEquals(1, player.count(objTypes.cert(objTypes[it]))) }
            assertEquals(0, player.count(GeExtrasTestObjs.cannon_set))
        }

    @Test
    fun GameTestState.`a missing part lists the contents instead`() =
        runGameTest(ItemSetsScript::class) {
            player.clearInv()
            player.inv[0] = InvObj(GeExtrasTestObjs.cannon_base)
            openSets()

            player.ifButton(GeExtrasComponents.item_sets_list, comsub = 0)
            advance()

            assertMessageSent(
                "The Dwarf cannon set contains: Cannon base, Cannon stand, Cannon barrels, " +
                    "Cannon furnace."
            )
            assertEquals(0, player.count(GeExtrasTestObjs.cannon_set))
            assertEquals(1, player.count(GeExtrasTestObjs.cannon_base))
        }

    @Test
    fun GameTestState.`a damaged barrows piece does not pack`() =
        runGameTest(ItemSetsScript::class) {
            player.clearInv()
            player.inv[0] = InvObj(GeExtrasTestObjs.ahrim_hood_damaged)
            player.inv[1] = InvObj(GeExtrasTestObjs.ahrim_top)
            player.inv[2] = InvObj(GeExtrasTestObjs.ahrim_skirt)
            player.inv[3] = InvObj(GeExtrasTestObjs.ahrim_staff)
            openSets()

            // Ahrim's armour is the sixth set (see the cache's enum_1033).
            player.ifButton(GeExtrasComponents.item_sets_list, comsub = 5)
            advance()
            assertEquals(0, player.count(GeExtrasTestObjs.ahrim_set))
            assertEquals(1, player.count(GeExtrasTestObjs.ahrim_hood_damaged))

            player.inv[0] = InvObj(GeExtrasTestObjs.ahrim_hood)
            player.ifButton(GeExtrasComponents.item_sets_list, comsub = 5)
            advance()
            assertEquals(1, player.count(GeExtrasTestObjs.ahrim_set))
        }

    @Test
    fun GameTestState.`bob decants three-dose potions into a full one`() =
        runGameTest(GeExpertsScript::class) {
            player.clearInv()
            player.inv[0] = InvObj(GeExtrasTestObjs.prayer_3)
            player.inv[1] = InvObj(GeExtrasTestObjs.prayer_3)
            val bob = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[GeExtrasNpcs.ge_expert_herbs])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc4(bob)
            // The first option is four doses.
            talkThrough(1)

            assertEquals(1, player.count(GeExtrasTestObjs.prayer_4))
            assertEquals(1, player.count(GeExtrasTestObjs.prayer_2))
            assertEquals(0, player.count(GeExtrasTestObjs.prayer_3))
        }

    @Test
    fun GameTestState.`bob splits a potion into single doses using empty vials`() =
        runGameTest(GeExpertsScript::class) {
            player.clearInv()
            val vial = GeExtrasObjs.vial_empty
            player.inv[0] = InvObj(GeExtrasTestObjs.prayer_4)
            repeat(3) { player.inv[1 + it] = InvObj(vial) }
            val bob = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[GeExtrasNpcs.ge_expert_herbs])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc4(bob)
            // The fourth option is single doses.
            talkThrough(4)

            assertEquals(4, player.count(GeExtrasTestObjs.prayer_1))
            assertEquals(0, player.count(vial))
        }

    @Test
    fun GameTestState.`murky matt combines jewellery charges`() =
        runGameTest(GeExpertsScript::class) {
            player.clearInv()
            fun necklace(charges: Int) =
                objTypes.values.first {
                    it.name == "Games necklace($charges)" && !it.isCert && !it.isPlaceholder
                }
            player.inv[0] = InvObj(necklace(1))
            player.inv[1] = InvObj(necklace(1))
            player.inv[2] = InvObj(necklace(3))
            val matt = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[GeExtrasNpcs.ge_expert_runes])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc4(matt)
            talkThrough()

            assertEquals(1, player.count(necklace(5)))
            assertEquals(0, player.count(necklace(1)))
            assertEquals(1, player.inv.count { it != null })
        }

    @Test
    fun GameTestState.`an expert's prices opens the price list with every price`() =
        runGameTest(GeExpertsScript::class) {
            val farid = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[GeExtrasNpcs.ge_expert_ores])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc3(farid)
            advanceUntil({ player.ui.containsModal(GeExtrasInterfaces.price_list) })

            val rows =
                player.invMap.values.single { it.type.internalNameValue == "ge_pricelist_data" }
            assertEquals("Copper ore", objTypes[checkNotNull(rows[0])].name)
            assertEquals(32, rows.count { it != null })
            assertTrue(rows.all { it == null || it.count > 0 })
        }

    @Test
    fun GameTestState.`brugsen's price option opens the price checker`() =
        runGameTest(GeExpertsScript::class) {
            val brugsen = spawnNpc(OPEN_GROUND.translateX(2), npcTypes[GeExtrasNpcs.ge_boss])
            player.teleport(OPEN_GROUND)
            advance()

            player.opNpc1(brugsen)
            talkThrough(2, until = GeExtrasTestInterfaces.price_checker)

            assertModalOpen(GeExtrasTestInterfaces.price_checker)
        }

    private companion object {
        // The Lumbridge spawn tile: truly empty ground.
        val OPEN_GROUND = CoordGrid(0, 50, 50, 30, 18)
    }
}
