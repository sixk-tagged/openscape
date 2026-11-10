package org.rsmod.content.areas.dungeon.swampcaves

import jakarta.inject.Inject
import kotlin.math.abs
import org.rsmod.api.combat.commons.npc.queueCombatRetaliate
import org.rsmod.api.config.refs.stats
import org.rsmod.api.hunt.Hunt
import org.rsmod.api.player.hit.processor.InstantPlayerHitProcessor
import org.rsmod.api.player.hit.takeInstantHit
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.stat.statBase
import org.rsmod.api.player.ui.ifCloseOverlay
import org.rsmod.api.player.ui.ifOpenFullOverlay
import org.rsmod.api.player.vars.VarPlayerIntMapSetter
import org.rsmod.api.random.GameRandom
import org.rsmod.api.script.onArea
import org.rsmod.api.script.onAreaExit
import org.rsmod.api.script.onPlayerSoftTimer
import org.rsmod.content.other.lightsources.LightSource
import org.rsmod.content.other.lightsources.LightSourceObjs
import org.rsmod.content.other.lightsources.LightSources
import org.rsmod.events.EventBus
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Player
import org.rsmod.game.entity.util.PathingEntityCommon
import org.rsmod.game.hit.HitType
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.hunt.HuntVis
import org.rsmod.game.type.npc.NpcTypeList
import org.rsmod.game.type.varp.VarpType
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext
import org.rsmod.routefinder.collision.CollisionFlagMap

/**
 * The Lumbridge Swamp Caves' hazards (wiki "Lumbridge Swamp Caves", "Light sources", "Darkness",
 * "Wall beast"): darkness and its biting insects, swamp gas that naked flames set off, the escape
 * up the rope without a light, and the wall beasts that grab anyone without a spiny helmet.
 */
class SwampCaveScript
@Inject
constructor(
    private val instantHits: InstantPlayerHitProcessor,
    private val hunt: Hunt,
    private val npcTypes: NpcTypeList,
    private val collision: CollisionFlagMap,
    private val eventBus: EventBus,
    private val random: GameRandom,
) : PluginScript() {
    override fun ScriptContext.startup() {
        onArea(SwampCaveAreas.caves) { player.softTimer(SwampCaveTimers.tick, 1) }
        onAreaExit(SwampCaveAreas.caves) { player.leave() }
        onPlayerSoftTimer(SwampCaveTimers.tick) { player.tick() }
    }

    private fun Player.tick() {
        val brightness = LightSources.brightness(this)
        showDarkness(brightness)
        if (brightness == 0) {
            if (coords.chebyshevDistance(ROPE) <= ROPE_ESCAPE_RANGE) {
                PathingEntityCommon.telejump(this, collision, SWAMP_LANDING)
                return
            }
            insects()
        } else {
            set(SwampCaveVarps.dark_ticks, 0)
        }
        gas()
        wallBeasts()
    }

    private fun Player.leave() {
        clearSoftTimer(SwampCaveTimers.tick)
        showDarkness(MAX_BRIGHTNESS)
        set(SwampCaveVarps.dark_ticks, 0)
        set(SwampCaveVarps.gas_ticks, 0)
        set(SwampCaveVarps.beast_tile, 0)
    }

    // ---- Darkness

    private fun Player.showDarkness(brightness: Int) {
        val wanted = (MAX_BRIGHTNESS - brightness).coerceIn(0, MAX_BRIGHTNESS)
        val shown = vars[SwampCaveVarps.overlay]
        if (wanted == shown) {
            return
        }
        overlayFor(shown)?.let { ifCloseOverlay(it, eventBus) }
        overlayFor(wanted)?.let { ifOpenFullOverlay(it, eventBus) }
        set(SwampCaveVarps.overlay, wanted)
    }

    private fun overlayFor(level: Int) =
        when (level) {
            1 -> SwampCaveInterfaces.light
            2 -> SwampCaveInterfaces.medium
            3 -> SwampCaveInterfaces.dark
            else -> null
        }

    /** 15 ticks of dark brings a warning, 30 the swarm, then 1 damage every 3 ticks. */
    private fun Player.insects() {
        val ticks = vars[SwampCaveVarps.dark_ticks] + 1
        set(SwampCaveVarps.dark_ticks, ticks)
        when (ticks) {
            INSECT_WARNING -> mes("You hear tiny insects skittering over the ground...")
            INSECT_SWARM -> mes("<col=ff0000>Tiny biting insects swarm all over you!</col>")
        }
        if (ticks >= INSECT_SWARM && (ticks - INSECT_SWARM) % INSECT_DELAY == 0) {
            takeInstantHit(HitType.Typeless, 1, instantHits)
        }
    }

    // ---- Swamp gas

    /** A naked flame in a gas zone flares up, and 7 ticks later the gas explodes. */
    private fun Player.gas() {
        val flames = LightSources.nakedFlames(this)
        if (flames.isEmpty() || GAS_ZONES.none { it.contains(coords) }) {
            set(SwampCaveVarps.gas_ticks, 0)
            return
        }
        val ticks = vars[SwampCaveVarps.gas_ticks] + 1
        set(SwampCaveVarps.gas_ticks, ticks)
        if (ticks == 1) {
            mes("<col=ff0000>Your ${flames.first().flameName()} flares brightly!</col>")
        }
        if (ticks > GAS_FUSE) {
            explode(flames)
            set(SwampCaveVarps.gas_ticks, 0)
        }
    }

    private fun Player.explode(flames: List<LightSource>) {
        // UNVERIFIED: the message; the damage (a quarter of max hitpoints) is the wiki's.
        mes("The swamp gas explodes!")
        for (slot in inv.indices) {
            val obj = inv[slot] ?: continue
            val unlit = flames.firstOrNull { it.lit.id == obj.id }?.unlit ?: continue
            inv[slot] = InvObj(unlit)
        }
        takeInstantHit(HitType.Typeless, statBase(stats.hitpoints) / 4, instantHits)
    }

    private fun LightSource.flameName(): String =
        when (lit) {
            LightSourceObjs.torch_lit -> "torch"
            LightSourceObjs.oil_lamp_lit -> "oil lamp"
            LightSourceObjs.black_candle_lit -> "black candle"
            else -> "candle"
        }

    // ---- Wall beasts

    /**
     * Stopping on the tile in front of a hole in the wall gets the player grabbed: a spiny helmet
     * (or Slayer helmet) foils it and the wall beast comes out to fight; otherwise it slams the
     * player for up to 18. Running straight past, or staying on the tile, doesn't trigger it.
     */
    private fun Player.wallBeasts() {
        val hole =
            hunt.findNpcs(coords, 1, HuntVis.Off).firstOrNull {
                it.type.id == SwampCaveNpcs.hole.id && !it.isTransmogged() && it.facesTile(coords)
            }
        if (hole == null) {
            set(SwampCaveVarps.beast_tile, 0)
            return
        }
        if (vars[SwampCaveVarps.beast_tile] == coords.packed) {
            return
        }
        set(SwampCaveVarps.beast_tile, coords.packed)
        if (SwampCaveObjs.helmets.any { it in worn }) {
            hole.anim(SwampCaveSeqs.beast_foiled)
            hole.transmog(npcTypes[SwampCaveNpcs.wall_beast], BEAST_OUT_TICKS)
            hole.facePlayer(this)
            hole.queueCombatRetaliate(this)
            return
        }
        hole.anim(SwampCaveSeqs.beast_grab)
        anim(SwampCaveSeqs.player_grabbed)
        takeInstantHit(HitType.Typeless, random.of(1, GRAB_MAX_HIT), instantHits)
    }

    private fun Npc.isTransmogged(): Boolean = visType.id != type.id

    private fun Npc.facesTile(tile: CoordGrid): Boolean =
        coords.level == tile.level && abs(coords.x - tile.x) + abs(coords.z - tile.z) == 1

    private fun Player.set(varp: VarpType, value: Int) {
        VarPlayerIntMapSetter.set(this, varp, value)
    }

    private data class Box(val minX: Int, val minZ: Int, val maxX: Int, val maxZ: Int) {
        fun contains(c: CoordGrid): Boolean = c.x in minX..maxX && c.z in minZ..maxZ
    }

    private companion object {
        const val MAX_BRIGHTNESS = 3
        const val INSECT_WARNING = 15
        const val INSECT_SWARM = 30
        const val INSECT_DELAY = 3
        const val GAS_FUSE = 7
        const val GRAB_MAX_HIT = 18
        const val BEAST_OUT_TICKS = 200
        const val ROPE_ESCAPE_RANGE = 3

        val ROPE = CoordGrid(3169, 9572)
        val SWAMP_LANDING = CoordGrid(3169, 3173)

        // Note: rectangles read off the wiki's hazard map (8 px a tile, fitted on the wall
        // beasts); the real gas areas are rounder.
        val GAS_ZONES = listOf(Box(3155, 9584, 3174, 9598), Box(3201, 9551, 3212, 9562))
    }
}
