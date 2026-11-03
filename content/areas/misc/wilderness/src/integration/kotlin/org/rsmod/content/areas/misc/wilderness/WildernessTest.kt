package org.rsmod.content.areas.misc.wilderness

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.config.refs.interfaces
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.varps
import org.rsmod.api.death.PlayerDeath
import org.rsmod.api.player.events.interact.LocUEvents
import org.rsmod.api.player.events.interact.NpcUDefaultEvents
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.player.stat.stat
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.content.areas.misc.wilderness.configs.WildernessLocs
import org.rsmod.content.areas.misc.wilderness.configs.WildernessNpcs
import org.rsmod.content.areas.misc.wilderness.configs.WildernessObjs
import org.rsmod.content.areas.misc.wilderness.configs.WildernessVarps
import org.rsmod.content.areas.misc.wilderness.locs.ChaosAltar
import org.rsmod.content.areas.misc.wilderness.locs.ChaosAltarBones
import org.rsmod.content.areas.misc.wilderness.locs.MuddyChest
import org.rsmod.content.areas.misc.wilderness.locs.MuddyChestObjs
import org.rsmod.content.areas.misc.wilderness.locs.RogueChestObjs
import org.rsmod.content.areas.misc.wilderness.locs.RoguesChest
import org.rsmod.content.areas.misc.wilderness.locs.WildernessLocScript
import org.rsmod.content.areas.misc.wilderness.npcs.ChaosDruid
import org.rsmod.content.areas.misc.wilderness.npcs.FeroxEnclave
import org.rsmod.content.areas.misc.wilderness.npcs.MercenaryObjs
import org.rsmod.content.areas.misc.wilderness.npcs.ResourceArea
import org.rsmod.content.areas.misc.wilderness.npcs.WildernessShops
import org.rsmod.content.generic.locs.banks.BankBooth
import org.rsmod.game.entity.Npc
import org.rsmod.game.inv.InvObj
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.type.npc.NpcType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.map.CoordGrid

object WildernessTestObjs : ObjReferences() {
    val cert_logs = find("cert_logs")
    val cert_big_bones = find("cert_big_bones")
}

/* Obj transaction system is not thread-safe. */
@Execution(ExecutionMode.SAME_THREAD)
class WildernessTest {
    @Test
    fun GameTestState.`a cape merchant's trade opens a shop of five team capes`() =
        runGameTest(WildernessShops::class) {
            // Sam is the tenth seller: the capes 10, 20, 30, 40 and 50.
            tradeOpens(WildernessNpcs.sam, WildernessObjs.team_capes[49], 100)
            val shop = checkNotNull(player.openedShop)
            assertEquals(100, shop.inv.count(WildernessObjs.team_capes[9]))
        }

    @Test
    fun GameTestState.`tony sells pizza bases`() =
        runGameTest(WildernessShops::class) {
            tradeOpens(WildernessNpcs.fat_tony, WildernessObjs.pizza_base, 30)
        }

    @Test
    fun GameTestState.`lundail's trade opens a shop of runes`() =
        runGameTest(WildernessShops::class) {
            // The first line of his stock is 200 fire runes.
            tradeOpens(WildernessNpcs.lundail, WildernessObjs.lundail_stock[0].first, 200)
        }

    @Test
    fun GameTestState.`the ferox bank chest opens the bank`() =
        runGameTest(BankBooth::class) {
            val chest = placeMapLoc(OPEN_GROUND.translateX(1), WildernessLocs.ferox_bank_chest)
            player.teleport(OPEN_GROUND)
            advance()

            player.opLoc1(chest)
            advanceUntil(max = 10) { player.ui.containsModal(interfaces.bank_main) }
            assertTrue(player.ui.containsModal(interfaces.bank_main))
        }

    @Test
    fun GameTestState.`the pool restores hitpoints and prayer`() =
        runGameTest(WildernessLocScript::class) {
            val pool = placeMapLoc(OPEN_GROUND.translateX(1), WildernessLocs.ferox_pool)
            player.stats[stats.hitpoints] = 50
            player.setCurrentLevel(stats.hitpoints, 7)
            player.setBaseLevel(stats.prayer, 40)
            player.setCurrentLevel(stats.prayer, 3)
            player.teleport(OPEN_GROUND)
            advance()

            player.opLoc1(pool)
            advance()

            assertEquals(50, player.hitpoints)
            assertEquals(40, player.stat(stats.prayer))
            assertMessageSent("You feel reinvigorated after drinking from the pool.")
        }

    @Test
    fun GameTestState.`the barrier lets the player out unless teleblocked`() =
        runGameTest(WildernessLocScript::class) {
            val barrier = placeMapLoc(OPEN_GROUND, WildernessLocs.ferox_barriers.first())
            player.teleport(OPEN_GROUND)
            player.setVarp(varps.teleblock_ticks, 100)
            advance()

            player.opLoc1(barrier)
            advance()
            assertEquals(OPEN_GROUND, player.coords)
            assertMessageSent("A magical force stops you from teleporting.")

            player.setVarp(varps.teleblock_ticks, 0)
            player.opLoc1(barrier)
            advanceUntil(max = 10) { player.coords != OPEN_GROUND }
            // Far south of the enclave, so out is south.
            assertEquals(OPEN_GROUND.translateZ(-1), player.coords)
        }

    @Test
    fun GameTestState.`the edgeville lever takes the player to the deserted keep`() =
        runGameTest(WildernessLocScript::class) {
            val lever = placeMapLoc(OPEN_GROUND.translateX(1), WildernessLocs.edgeville_lever)
            player.teleport(OPEN_GROUND)
            player.setVarp(varps.teleblock_ticks, 100)
            advance()

            player.opLoc1(lever)
            advance(ticks = 8)
            assertEquals(OPEN_GROUND, player.coords)

            player.setVarp(varps.teleblock_ticks, 0)
            player.opLoc1(lever)
            advanceUntil(max = 20) { player.coords != OPEN_GROUND }
            advance(ticks = 3)
            assertEquals(CoordGrid(3154, 3924), player.coords)
        }

    @Test
    fun GameTestState.`the keep lever offers both ardougne and edgeville`() =
        runGameTest(WildernessLocScript::class) {
            val lever = placeMapLoc(SHOP_GROUND.translateX(1), WildernessLocs.keep_lever)
            player.teleport(SHOP_GROUND)
            advance()

            player.opLoc1(lever)
            talkThrough(2)
            advance(ticks = 8)

            assertEquals(CoordGrid(3087, 3500), player.coords)
        }

    @Test
    fun GameTestState.`an obelisk teleports a player standing in its grid to an obelisk`() =
        runGameTest(WildernessLocScript::class) {
            val pillar = placeMapLoc(OBELISK_CORNER, WildernessLocs.obelisks[0])
            val centre = OBELISK_CORNER.translate(2, 2)
            player.teleport(OBELISK_CORNER.translateX(1))
            advance()

            // Activate from the pillar's side, then step into the grid before it fires.
            player.opLoc1(pillar)
            advance(ticks = 2)
            player.teleport(centre)
            advanceUntil(max = 30) { !near(centre, player.coords) }
            val landed = OBELISK_CORNERS.any { near(it.translate(2, 2), player.coords) }
            assertTrue(landed) { "landed at ${player.coords}" }
        }

    @Test
    fun GameTestState.`the resource area gate wants the guardian's fee first`() =
        runGameTest(ResourceArea::class) {
            val gate = placeMapLoc(OPEN_GROUND, WildernessLocs.resource_gate)
            val outside = OPEN_GROUND.translateZ(1)
            player.teleport(outside)
            advance()

            player.opLoc1(gate)
            advance(ticks = 3)
            assertEquals(outside, player.coords)

            player.setVarp(WildernessVarps.resource_area_paid, 1)
            player.opLoc1(gate)
            advanceUntil(max = 10) { player.coords == OPEN_GROUND }
            assertEquals(OPEN_GROUND, player.coords)
        }

    @Test
    fun GameTestState.`mandrith takes 7,500 coins for a pass`() =
        runGameTest(ResourceArea::class) {
            player.clearInv()
            player.inv[0] = InvObj(objs.coins, 10_000)
            val mandrith = spawnNpc(SHOP_GROUND.translateX(2), npcTypes[WildernessNpcs.mandrith])
            player.teleport(SHOP_GROUND)
            advance()

            player.opNpc1(mandrith)
            talkThrough(1)

            assertEquals(2_500, player.count(objs.coins))
            assertEquals(1, player.vars[WildernessVarps.resource_area_paid])
        }

    @Test
    fun GameTestState.`half the bones offered at the chaos altar are spared`() =
        runGameTest(ChaosAltar::class) {
            player.clearInv()
            player.inv[0] = InvObj(ChaosAltarBones.big_bones, 2)
            val altar = placeMapLoc(HUT_ALTAR, WildernessLocs.chaos_altar)
            player.teleport(HUT_ALTAR.translateX(1))
            advance()

            val xp = player.statMap.getFineXP(stats.prayer)
            random.next = 0
            offerBones(altar)
            assertMessageSent(
                "The Dark Lord spares your sacrifice but still rewards you for your efforts."
            )
            assertTrue(player.statMap.getFineXP(stats.prayer) > xp)
            assertEquals(2, player.count(ChaosAltarBones.big_bones))

            random.next = 1
            offerBones(altar)
            advanceUntil(max = 10) { player.count(ChaosAltarBones.big_bones) < 2 }
            assertEquals(1, player.count(ChaosAltarBones.big_bones))
        }

    @Test
    fun GameTestState.`piles notes what is used on him for 50 coins an item`() =
        runGameTest(ResourceArea::class) {
            player.clearInv()
            player.inv[0] = InvObj(objs.coins, 1_000)
            repeat(10) { player.inv[it + 1] = InvObj(objs.logs) }
            val piles = spawnNpc(SHOP_GROUND.translateX(2), npcTypes[WildernessNpcs.piles])
            player.teleport(SHOP_GROUND)
            advance()

            useOn(piles, WildernessNpcs.piles, objs.logs)
            advance()
            assertEquals(500, player.count(objs.coins))
            assertEquals(0, player.count(objs.logs))
            assertEquals(10, player.count(WildernessTestObjs.cert_logs))

            // With only 49 coins he notes nothing.
            player.inv[11] = InvObj(objs.logs)
            player.inv[12] = InvObj(objs.coins, 49)
            player.inv[0] = null
            useOn(piles, WildernessNpcs.piles, objs.logs)
            advance(ticks = 3)
            assertEquals(1, player.count(objs.logs))
        }

    @Test
    fun GameTestState.`ferox moves the respawn to the enclave for 5 million coins`() =
        runGameTest(FeroxEnclave::class) {
            player.clearInv()
            player.inv[0] = InvObj(objs.coins, 5_000_000)
            val ferox = spawnNpc(SHOP_GROUND.translateX(2), npcTypes[WildernessNpcs.ferox])
            player.teleport(SHOP_GROUND)
            advance()

            // "What can you do for me?", "Tell me more.", then the switch.
            player.opNpc1(ferox)
            talkThrough(2, 1, 1)
            assertEquals(0, player.count(objs.coins))
            assertEquals(PlayerDeath.FEROX_RESPAWN_SET, player.vars[varps.ferox_respawn])

            // Back to Lumbridge is free and keeps the unlock: "Ask about your respawn point.".
            player.opNpc1(ferox)
            talkThrough(2, 1, 1)
            assertEquals(PlayerDeath.FEROX_RESPAWN_PAID, player.vars[varps.ferox_respawn])
        }

    @Test
    fun GameTestState.`the mercenary swaps coins for platinum tokens`() =
        runGameTest(FeroxEnclave::class) {
            player.clearInv()
            player.inv[0] = InvObj(objs.coins, 3_500)
            val mercenary = spawnNpc(SHOP_GROUND.translateX(2), npcTypes[WildernessNpcs.mercenary])
            player.teleport(SHOP_GROUND)
            advance()

            useOn(mercenary, WildernessNpcs.mercenary, objs.coins)
            talkThrough(1)
            assertEquals(500, player.count(objs.coins))
            assertEquals(3, player.count(MercenaryObjs.platinum))
        }

    @Test
    fun GameTestState.`the mercenary keeps the coins if the tokens would have nowhere to go`() =
        runGameTest(FeroxEnclave::class) {
            player.clearInv()
            player.inv[0] = InvObj(objs.coins, 3_500)
            for (slot in 1 until player.inv.size) {
                player.inv[slot] = InvObj(ChaosAltarBones.big_bones)
            }
            val mercenary = spawnNpc(SHOP_GROUND.translateX(2), npcTypes[WildernessNpcs.mercenary])
            player.teleport(SHOP_GROUND)
            advance()

            useOn(mercenary, WildernessNpcs.mercenary, objs.coins)
            talkThrough(1)
            assertEquals(3_500, player.count(objs.coins))
            assertEquals(0, player.count(MercenaryObjs.platinum))
        }

    @Test
    fun GameTestState.`the elder chaos druid un-notes bones for 50 coins each`() =
        runGameTest(ChaosDruid::class) {
            player.clearInv()
            player.inv[0] = InvObj(objs.coins, 120)
            player.inv[1] = InvObj(WildernessTestObjs.cert_big_bones, 5)
            val druid = spawnNpc(SHOP_GROUND.translateX(2), npcTypes[WildernessNpcs.bone_druid])
            player.teleport(SHOP_GROUND)
            advance()

            // 120 coins pay for two of the five.
            useOn(druid, WildernessNpcs.bone_druid, WildernessTestObjs.cert_big_bones, slot = 1)
            advance()
            assertEquals(20, player.count(objs.coins))
            assertEquals(2, player.count(ChaosAltarBones.big_bones))
            assertEquals(3, player.count(WildernessTestObjs.cert_big_bones))
        }

    @Test
    fun GameTestState.`the muddy chest takes a key for its loot`() =
        runGameTest(MuddyChest::class) {
            player.clearInv()
            player.inv[0] = InvObj(MuddyChestObjs.muddy_key)
            val chest = placeMapLoc(OPEN_GROUND.translateX(1), WildernessLocs.muddy_chest)
            advance()

            // 39 misses the 3-in-40 roll for a rare item. The chest can't be reached from every
            // side, so try them all.
            random.next = 39
            val sides =
                listOf(
                    OPEN_GROUND,
                    OPEN_GROUND.translate(2, 0),
                    OPEN_GROUND.translate(1, 1),
                    OPEN_GROUND.translate(1, -1),
                )
            for (side in sides) {
                player.teleport(side)
                player.opLoc1(chest)
                advance(ticks = 3)
                if (player.count(MuddyChestObjs.uncut_ruby) > 0) {
                    break
                }
            }
            assertEquals(1, player.count(MuddyChestObjs.uncut_ruby))
            assertEquals(0, player.count(MuddyChestObjs.muddy_key))
            assertEquals(15, player.count(MuddyChestObjs.chaosrune))
            assertEquals(2, player.count(MuddyChestObjs.cert_mithril_bar))
        }

    /** Uses [obj] on [npc], as the client would. */
    private fun GameTestScope.useOn(npc: Npc, type: NpcType, obj: ObjType, slot: Int = 0) {
        val event = NpcUDefaultEvents.OpType(npc, slot, objTypes[obj], npcTypes[type])
        player.withProtectedAccess { eventBus.publish(this, event) }
    }

    private fun GameTestScope.offerBones(altar: BoundLocInfo) {
        val bone = objTypes[ChaosAltarBones.big_bones]
        val event = LocUEvents.Op(altar, altar, locTypes[altar], bone, 0)
        player.withProtectedAccess { eventBus.publish(this, event) }
    }

    @Test
    fun GameTestState.`a rogues' chest gives its loot to a level 84 thief`() =
        runGameTest(RoguesChest::class) {
            player.clearInv()
            player.setBaseLevel(stats.thieving, 84)
            player.setCurrentLevel(stats.thieving, 84)
            val chest = placeMapLoc(OPEN_GROUND.translateX(1), WildernessLocs.rogue_chest)
            player.teleport(OPEN_GROUND)
            advance()

            random.next = 0
            player.opLoc1(chest)
            advanceUntil(max = 10) { player.count(RogueChestObjs.naturerune) > 0 }
            assertEquals(30, player.count(RogueChestObjs.naturerune))
        }

    private fun near(centre: CoordGrid, coords: CoordGrid) =
        kotlin.math.abs(centre.x - coords.x) <= 1 && kotlin.math.abs(centre.z - coords.z) <= 1

    private fun GameTestScope.tradeOpens(keeper: NpcType, stocked: ObjType, count: Int) {
        val npc = spawnNpc(SHOP_GROUND.translateX(2), npcTypes[keeper])
        player.teleport(SHOP_GROUND)
        advance()

        player.opNpc3(npc)
        advanceUntil(max = 10) { player.openedShop != null }

        assertEquals(count, checkNotNull(player.openedShop).inv.count(stocked))
    }

    private companion object {
        // The Lumbridge spawn tile: truly empty ground for locs placed just for a test.
        val OPEN_GROUND = CoordGrid(0, 50, 50, 30, 18)

        // The first obelisk's south-west pillar, from the rev 233 map.
        // Locs placed by one test stay for the next, so the shops get ground of their own.
        val SHOP_GROUND = CoordGrid(0, 50, 50, 34, 31)
        val HUT_ALTAR = CoordGrid(2947, 3820)
        val OBELISK_CORNER = CoordGrid(2978, 3864)
        val OBELISK_CORNERS =
            listOf(
                OBELISK_CORNER,
                CoordGrid(3033, 3730),
                CoordGrid(3104, 3792),
                CoordGrid(3154, 3618),
                CoordGrid(3225, 3665),
                CoordGrid(3305, 3914),
            )
    }
}
