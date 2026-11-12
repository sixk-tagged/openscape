package org.rsmod.content.skills.slayer

import org.rsmod.api.player.output.mes
import org.rsmod.api.player.vars.VarPlayerIntMapSetter
import org.rsmod.api.random.GameRandom
import org.rsmod.api.repo.npc.NpcRepository
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.varbit.VarBitReferences
import org.rsmod.api.type.refs.varp.VarpReferences
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Player
import org.rsmod.game.type.npc.NpcType
import org.rsmod.game.type.npc.NpcTypeList

/**
 * Superior Slayer monsters (wiki "Superior slayer monster"): with "Bigger and Badder" unlocked (bit
 * 35, not toggled off), a task kill of a monster that has one spawns its superior 1 time in 200,
 * one at a time per player. Killing it gives ten times its hitpoints in Slayer xp, task or not.
 */
internal class Superiors(
    private val npcRepo: NpcRepository,
    private val npcTypes: NpcTypeList,
    private val random: GameRandom,
) {
    private val byBase: Map<Int, NpcType> by lazy {
        SuperiorNpcs.all.flatMap { (superior, bases) -> bases.map { it.id to superior } }.toMap()
    }
    private val superiorIds: Set<Int> by lazy { SuperiorNpcs.all.map { it.first.id }.toSet() }

    fun isSuperior(npc: Npc): Boolean = npc.type.id in superiorIds

    /** Ten times the superior's hitpoints (most superiors; the wiki lists a few exceptions). */
    fun xp(npc: Npc): Double = npc.visType.hitpoints * SUPERIOR_XP_MULTIPLIER

    fun killed(player: Player) {
        VarPlayerIntMapSetter.set(player, SuperiorVarps.alive_until, 0)
    }

    /** Called for a task kill of [npc] (before the count goes down). */
    fun roll(player: Player, npc: Npc) {
        val superior = byBase[npc.type.id] ?: return
        if (!player.hasSlayerUnlock(BIGGER_AND_BADDER_BIT)) {
            return
        }
        if (player.vars[SlayerToggles.superiors] == 1) {
            return
        }
        val aliveUntil = player.vars[SuperiorVarps.alive_until]
        if (aliveUntil > 0 && player.currentMapClock < aliveUntil) {
            return
        }
        if (random.of(SPAWN_CHANCE) != 0) {
            return
        }
        npcRepo.add(Npc(npcTypes[superior], npc.coords), DESPAWN_TICKS)
        VarPlayerIntMapSetter.set(
            player,
            SuperiorVarps.alive_until,
            player.currentMapClock + DESPAWN_TICKS,
        )
        player.mes("<col=ff0000>A superior foe has appeared...</col>")
    }

    private companion object {
        const val BIGGER_AND_BADDER_BIT = 35
        const val SPAWN_CHANCE = 200
        const val SUPERIOR_XP_MULTIPLIER = 10.0

        // UNVERIFIED: how long an unattacked superior stays.
        const val DESPAWN_TICKS = 300
    }
}

/** Each superior and the monsters that can turn into it (the ones built so far). */
object SuperiorNpcs : NpcReferences() {
    val crawling_hand = find("superior_crawling_hand")
    val cave_crawler = find("superior_cave_crawler")
    val banshee = find("superior_banshee")
    val rockslug = find("superior_rockslug")
    val cockatrice = find("superior_cockatrice")
    val pyrefiend = find("superior_pyrefiend")
    val basilisk = find("superior_basilisk")
    val infernal_mage = find("superior_infernal_mage")
    val bloodveld = find("superior_bloodveld")
    val jelly = find("superior_jelly")
    val turoth = find("superior_turoth")
    val aberrant_spectre = find("superior_abberant_spectre")
    val kurask = find("superior_kurask")
    val gargoyle = find("superior_gargoyle")
    val nechryael = find("superior_nechryael")
    val abyssal_demon = find("superior_abyssal_demon")

    val all: List<Pair<NpcType, List<NpcType>>> =
        listOf(
            crawling_hand to
                listOf(
                        "slayer_crawling_hand_1",
                        "slayer_crawling_hand_big_1",
                        "slayer_crawling_hand_big_2",
                    )
                    .map { find(it) },
            cave_crawler to (1..4).map { find("slayer_cave_crawler_$it") },
            banshee to listOf("slayer_banshee_1").map { find(it) },
            rockslug to listOf("slayer_rockslug", "slayer_rockslug_baby").map { find(it) },
            cockatrice to listOf("slayer_cockatrice", "slayer_cockatrice_daddy").map { find(it) },
            pyrefiend to (1..4).map { find("slayer_pyrefiend_$it") },
            basilisk to listOf("slayer_basilisk", "slayer_basilisk_baby").map { find(it) },
            infernal_mage to (1..5).map { find("slayer_infernal_mage_$it") },
            bloodveld to
                listOf(
                        "slayer_bloodveld",
                        "slayer_bloodveld_baby",
                        "slayer_bloodveld_strongholdcave",
                        "slayer_bloodveld_baby_strongholdcave",
                    )
                    .map { find(it) },
            jelly to (1..6).map { find("slayer_jelly_$it") },
            turoth to
                listOf(
                        "slayer_turoth",
                        "slayer_turoth_dad",
                        "slayer_turoth_mum",
                        "slayer_turoth_child",
                        "slayer_turoth_baby",
                        "slayer_turoth_dad2",
                        "slayer_turoth_dad3",
                    )
                    .map { find(it) },
            aberrant_spectre to
                (1..4).map { find("slayer_abberant_spectre_$it") } +
                    (1..2).map { find("slayer_aberrantspectre_${it}_strongholdcave") },
            kurask to listOf("slayer_kursk_1", "slayer_kursk_2").map { find(it) },
            gargoyle to listOf("slayer_gargoyle_1", "slayer_cave_gargoyle").map { find(it) },
            nechryael to
                listOf("slayer_nechryael", "slayer_nechryael_strongholdcave").map { find(it) },
            abyssal_demon to
                listOf("slayer_abyssal", "slayer_abyssal_strongholdcave").map { find(it) },
        )
}

/** The unlocks that stay bought and switch on and off instead (1 = switched off), by unlock bit. */
internal object SlayerToggles : VarBitReferences() {
    val superiors = find("slayer_toggleoff_superiormobs")
    val longer_revenants = find("slayer_toggleoff_longerrevenants")
    val fossil_wyverns = find("slayer_toggleoff_fossilwyvernblock")

    val byBit = mapOf(35 to superiors, 43 to fossil_wyverns, 53 to longer_revenants)
}

object SuperiorVarps : VarpReferences() {
    /** The map clock until which this player's superior can still be out (no second one). */
    val alive_until = find("slayer_superior_until")
}
