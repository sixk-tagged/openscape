package org.rsmod.content.skills.mining.scripts

import jakarta.inject.Inject
import org.rsmod.api.config.Constants
import org.rsmod.api.config.locParam
import org.rsmod.api.config.locXpParam
import org.rsmod.api.config.objParam
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.stats
import org.rsmod.api.player.front
import org.rsmod.api.player.hands
import org.rsmod.api.player.output.ClientScripts
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.righthand
import org.rsmod.api.player.stat.miningLvl
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.repo.player.PlayerRepository
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLoc3
import org.rsmod.api.script.onOpLocU
import org.rsmod.api.stats.levelmod.InvisibleLevels
import org.rsmod.api.stats.xpmod.XpModifiers
import org.rsmod.content.skills.mining.configs.MiningContent
import org.rsmod.content.skills.mining.configs.MiningObjs
import org.rsmod.content.skills.mining.configs.MiningParams
import org.rsmod.content.skills.mining.configs.MiningRocks
import org.rsmod.content.skills.mining.configs.ProductRoll
import org.rsmod.game.entity.Player
import org.rsmod.game.inv.InvObj
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.type.loc.LocType
import org.rsmod.game.type.loc.UnpackedLocType
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.obj.UnpackedObjType
import org.rsmod.game.type.seq.SeqType
import org.rsmod.map.zone.ZoneKey
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class Mining
@Inject
constructor(
    private val objTypes: ObjTypeList,
    private val locRepo: LocRepository,
    private val objRepo: ObjRepository,
    private val playerRepo: PlayerRepository,
    private val xpMods: XpModifiers,
    private val invisibleLvls: InvisibleLevels,
) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1(MiningContent.rock) { attempt(it.loc, it.type) }
        onOpLoc3(MiningContent.rock) { mine(it.loc, it.type) }
        onOpLocU(MiningContent.rock, MiningContent.pickaxe) { mine(it.loc, it.type) }
        onOpLoc1(MiningContent.depleted_rock) { mes(NO_ORE) }
    }

    private fun ProtectedAccess.attempt(rock: BoundLocInfo, type: UnpackedLocType) {
        if (!canMine(type)) {
            return
        }
        if (actionDelay < mapClock) {
            actionDelay = mapClock + 3
            skillAnimDelay = mapClock + 3
            opLoc1(rock)
            return
        }
        val pickaxe = findPickaxe(player, objTypes)
        if (pickaxe == null) {
            mesNoPickaxe()
            return
        }
        anim(objTypes[pickaxe].pickaxeAnim)
        spam("You swing your pick at the rock.")
        mine(rock, type)
    }

    private fun ProtectedAccess.mine(rock: BoundLocInfo, type: UnpackedLocType) {
        val pickaxe = findPickaxe(player, objTypes)
        if (pickaxe == null) {
            mesNoPickaxe()
            return
        }
        if (!canMine(type)) {
            return
        }

        val pickaxeType = objTypes[pickaxe]
        if (skillAnimDelay <= mapClock) {
            skillAnimDelay = mapClock + 4
            anim(pickaxeType.pickaxeAnim)
        }

        var success = false
        if (actionDelay < mapClock) {
            actionDelay = mapClock + pickaxeType.pickaxeTicks - 1
        } else if (actionDelay == mapClock) {
            val (low, high) = successRate(type)
            success = statRandom(stats.mining, low, high, invisibleLvls)
        }

        if (success) {
            val (product, xp) = rollProduct(type)
            spam("You manage to mine some ${objTypes[product].name.lowercase()}.")
            statAdvance(stats.mining, xp * xpMods.get(player, stats.mining))
            invAdd(inv, product)
            if (type.rockBonusRolls) {
                rollBonusDrops()
            }
            if (isDepleted(type)) {
                deplete(rock, type)
                return
            }
        }
        opLoc3(rock)
    }

    private fun ProtectedAccess.canMine(type: UnpackedLocType): Boolean {
        if (player.miningLvl < type.rockLevelReq) {
            mes("You need a Mining level of ${type.rockLevelReq} to mine this rock.")
            return false
        }
        val product = objTypes[type.rockProduct]
        val stacksOntoExisting = product.isStackable && product in inv
        if (inv.isFull() && !stacksOntoExisting) {
            mes("Your inventory is too full to hold any more ${product.name.lowercase()}.")
            return false
        }
        return true
    }

    private fun ProtectedAccess.mesNoPickaxe() {
        mes("You need a pickaxe to mine this rock.")
        mes("You do not have a pickaxe which you have the Mining level to use.")
    }

    private fun ProtectedAccess.rollProduct(type: UnpackedLocType): Pair<ObjType, Double> =
        when (type.rockProductRoll) {
            ProductRoll.ESSENCE -> {
                val pure = player.miningLvl >= PURE_ESSENCE_LEVEL
                (if (pure) MiningObjs.pure_essence else MiningObjs.rune_essence) to type.rockXp
            }
            ProductRoll.SANDSTONE -> rollSize(MiningProducts.sandstone)
            ProductRoll.GRANITE -> rollSize(MiningProducts.granite)
            ProductRoll.GEM_ROCK -> MiningProducts.gemRockTable.roll(random) to type.rockXp
            else -> type.rockProduct to type.rockXp
        }

    private fun ProtectedAccess.rollSize(sizes: List<MiningProducts.Size>): Pair<ObjType, Double> {
        for (size in sizes.dropLast(1)) {
            if (statRandom(stats.mining, size.low, size.high, invisibleLvls)) {
                return size.obj to size.xp
            }
        }
        val smallest = sizes.last()
        return smallest.obj to smallest.xp
    }

    /** `deplete_chance` is the chance out of 255 that a success does *not* deplete the rock. */
    private fun ProtectedAccess.isDepleted(type: UnpackedLocType): Boolean {
        val saveChance = type.rockDepleteChance
        if (saveChance >= MiningRocks.NEVER_DEPLETES) {
            return false
        }
        if (saveChance > 0 && random.of(1, 255) <= saveChance) {
            return false
        }
        val gloveSave = MiningRewards.gloveSavePermille(player.hands, type.rockProduct)
        if (gloveSave > 0 && random.of(1, 1000) <= gloveSave) {
            mes("Your mining gloves stop the rock from being depleted.")
            return false
        }
        return true
    }

    private fun ProtectedAccess.rollBonusDrops() {
        val glory = MiningRewards.isChargedGlory(player.front)
        val gemChance = if (glory) GEM_CHANCE_GLORY else GEM_CHANCE
        if (random.of(1, gemChance) == 1) {
            val gem = MiningRewards.randomGemTable.roll(random)
            invAddOrDrop(objRepo, gem)
            mes("You just found an ${objTypes[gem].name.lowercase()}!")
        }
        if (random.of(1, NUGGET_CHANCE) == 1) {
            invAddOrDrop(objRepo, MiningObjs.golden_nugget)
            mes("You find a golden nugget!")
        }
    }

    private fun ProtectedAccess.deplete(rock: BoundLocInfo, type: UnpackedLocType) {
        val respawnTime = type.rockRespawnTime
        locRepo.change(rock, type.rockDepleted, respawnTime)
        resetAnim()
        for (observer in playerRepo.findAll(ZoneKey.from(rock.coords), zoneRadius = 3)) {
            ClientScripts.addOverlayTimerLoc(
                player = observer,
                coords = rock.coords,
                loc = type,
                shape = rock.shape,
                // Note: reuses woodcutting's timer style; swap if OSRS has a mining one.
                timer = Constants.overlay_timer_woodcutting,
                ticks = respawnTime,
                colour = OVERLAY_COLOUR,
            )
        }
    }

    companion object {
        const val NO_ORE = "There is currently no ore available in this rock."
        const val PURE_ESSENCE_LEVEL = 30
        const val GEM_CHANCE = 256
        const val GEM_CHANCE_GLORY = 86
        // Note: flat rate; scale by rock level if low-tier power-mining proves too efficient.
        const val NUGGET_CHANCE = 25
        private const val OVERLAY_COLOUR = 16765184

        val UnpackedObjType.pickaxeLevelReq: Int by objParam(params.levelrequire)
        val UnpackedObjType.pickaxeAnim: SeqType by objParam(params.skill_anim)
        val UnpackedObjType.pickaxeTicks: Int by objParam(MiningParams.pickaxe_ticks)

        val UnpackedLocType.rockLevelReq: Int by locParam(params.levelrequire)
        val UnpackedLocType.rockProduct: ObjType by locParam(params.skill_productitem)
        val UnpackedLocType.rockXp: Double by locXpParam(params.skill_xp)
        val UnpackedLocType.rockDepleted: LocType by locParam(params.next_loc_stage)
        val UnpackedLocType.rockRespawnTime: Int by locParam(params.respawn_time)
        val UnpackedLocType.rockDepleteChance: Int by locParam(params.deplete_chance)
        val UnpackedLocType.rockProductRoll: Int by locParam(MiningParams.product_roll)
        val UnpackedLocType.rockBonusRolls: Boolean by locParam(MiningParams.bonus_rolls)
        private val UnpackedLocType.rockSuccessRate: Int by locParam(MiningParams.success_rate)

        fun successRate(type: UnpackedLocType): Pair<Int, Int> {
            val packed = type.rockSuccessRate
            return (packed shr 16) to (packed and 0xFFFF)
        }

        /** The best usable pickaxe, worn or carried, mirroring `Woodcutting.findAxe`. */
        fun findPickaxe(player: Player, objTypes: ObjTypeList): InvObj? {
            val worn = player.righthand?.takeIf { objTypes[it].isUsablePickaxe(player.miningLvl) }
            val carried =
                player.inv
                    .filterNotNull { objTypes[it].isUsablePickaxe(player.miningLvl) }
                    .maxByOrNull { objTypes[it].pickaxeLevelReq }
            if (worn != null && carried != null) {
                val wornLevel = objTypes[worn].pickaxeLevelReq
                return if (wornLevel >= objTypes[carried].pickaxeLevelReq) worn else carried
            }
            return worn ?: carried
        }

        private fun UnpackedObjType.isUsablePickaxe(miningLevel: Int): Boolean =
            isContentType(MiningContent.pickaxe) && miningLevel >= pickaxeLevelReq
    }
}
