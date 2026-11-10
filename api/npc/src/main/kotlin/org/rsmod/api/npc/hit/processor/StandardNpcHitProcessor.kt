package org.rsmod.api.npc.hit.processor

import jakarta.inject.Inject
import org.rsmod.api.config.refs.hitmark_groups
import org.rsmod.api.config.refs.objs
import org.rsmod.api.config.refs.params
import org.rsmod.api.config.refs.queues
import org.rsmod.api.npc.access.StandardNpcAccess
import org.rsmod.api.npc.events.NpcHitEvents
import org.rsmod.api.npc.headbar.InternalNpcHeadbars
import org.rsmod.api.player.output.soundSynth
import org.rsmod.events.EventBus
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.PlayerList
import org.rsmod.game.headbar.Headbar
import org.rsmod.game.hit.Hit
import org.rsmod.game.type.headbar.HeadbarType
import org.rsmod.game.type.obj.ObjType

public class StandardNpcHitProcessor
@Inject
constructor(private val playerList: PlayerList, private val eventBus: EventBus) : NpcHitProcessor {
    override fun StandardNpcAccess.process(hit: Hit) {
        // TODO(combat): Show ironman_blocked hitmark if source is an ironman and target has been
        // damaged by other sources.

        // openscape: hitpoints floors (gargoyles...) and leaf-bladed-only npcs (turoth, kurask).
        val cap = npc.damageCap(hit::isRighthandObj, hit::isSecondaryObj)
        var changedDamage: Int? = null
        if (hit.damage > cap) {
            changedDamage = cap
        }

        if (changedDamage == 0) {
            val zeroDamageHitmark = hitmark_groups.zero_damage
            val modifiedHitmark =
                hit.hitmark.copy(
                    self = zeroDamageHitmark.lit.id,
                    source = zeroDamageHitmark.lit.id,
                    public = if (hit.hitmark.isPrivate) null else zeroDamageHitmark.tint?.id,
                    damage = changedDamage,
                )
            val modifiedHit = hit.copy(hitmark = modifiedHitmark)
            takeHit(modifiedHit)
            return
        }

        if (changedDamage != null) {
            val modifiedHitmark = hit.hitmark.copy(damage = changedDamage)
            val modifiedHit = hit.copy(hitmark = modifiedHitmark)
            takeHit(modifiedHit)
            return
        }

        takeHit(hit)
    }

    private fun StandardNpcAccess.takeHit(hit: Hit) {
        check(hit.damage <= npc.hitpoints) {
            "Expected hit damage to be less than or equal to available hitpoints: " +
                "health=${npc.hitpoints}, hit=$hit"
        }
        // TODO(combat): Process recoils, retribution(?), etc.
        npc.hitpoints -= hit.damage

        playDefendSound(hit)

        val queueDeath = npc.hitpoints == 0 && queues.death !in npc.queueList
        if (queueDeath) {
            queueDeath()
        }

        npc.showHitmark(hit.hitmark)

        val visHeadbar = npc.visHeadbar(params.headbar)
        val headbar = hit.createHeadbar(npc.hitpoints, npc.baseHitpointsLvl, visHeadbar)
        npc.showHeadbar(headbar)

        npc.publishHitEvent(hit)
    }

    private fun StandardNpcAccess.playDefendSound(hit: Hit) {
        val source = if (hit.isFromPlayer) hit.resolvePlayerSource(playerList) else null
        if (source == null) {
            return
        }
        val defendSound = npc.visType.paramOrNull(params.defend_sound) ?: return
        source.soundSynth(defendSound)
    }

    private fun Hit.createHeadbar(currHp: Int, maxHp: Int, headbar: HeadbarType): Headbar =
        InternalNpcHeadbars.createFromHitmark(hitmark, currHp, maxHp, headbar)

    private fun Npc.publishHitEvent(hit: Hit) {
        val event = NpcHitEvents.Impact(this, hit)
        eventBus.publish(event)
    }
}

private val LEAF_BLADED_WEAPONS by lazy {
    listOf(objs.leafbladed_spear, objs.leafbladed_sword, objs.leafbladed_battleaxe)
}

private val LEAF_BLADED_AMMO by lazy {
    listOf(objs.broad_arrows, objs.broad_bolts, objs.amethyst_broad_bolts, objs.spell_magic_dart)
}

/**
 * openscape: the most damage this npc can take from one hit: never below its `hitpoints_floor`
 * (gargoyles, rockslugs), and none at all for a `leafbladed_only` npc (turoth, kurask) unless the
 * weapon ([isRighthand]) or the ammo or spell ([isSecondary]) is leaf-bladed. Combat xp is capped
 * by it too, so zeroed hits give none.
 */
public fun Npc.damageCap(
    isRighthand: (ObjType) -> Boolean,
    isSecondary: (ObjType) -> Boolean,
): Int {
    val leafOnly = visType.param(params.leafbladed_only) == 1
    if (leafOnly && LEAF_BLADED_WEAPONS.none(isRighthand) && LEAF_BLADED_AMMO.none(isSecondary)) {
        return 0
    }
    return (hitpoints - visType.param(params.hitpoints_floor)).coerceAtLeast(0)
}
