package org.rsmod.content.other.consumables

import kotlin.math.min
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.varps
import org.rsmod.api.player.stat.hitpoints
import org.rsmod.api.player.stat.stat
import org.rsmod.api.player.stat.statAdd
import org.rsmod.api.player.stat.statBase
import org.rsmod.api.player.stat.statBoost
import org.rsmod.api.player.stat.statHeal
import org.rsmod.api.player.stat.statSub
import org.rsmod.api.player.vars.intVarp
import org.rsmod.content.skills.herblore.Potions
import org.rsmod.game.entity.Player
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.stat.StatType

private var Player.specialEnergy by intVarp(varps.sa_energy)

private const val DIVINE_DAMAGE = 10
private const val SURGE_ENERGY = 250
private const val SURGE_COOLDOWN = 500

/** Boosts [stat] by [constant] plus [percent] of its base level. */
private fun Player.boost(stat: StatType, constant: Int, percent: Int) =
    statBoost(stat, constant, percent)

private fun Player.superMelee(stat: StatType) = boost(stat, 5, 15)

private fun Player.drainByTenth(stat: StatType) = statSub(stat, stat(stat) / 10 + 2, 0)

/** Ancient and forgotten brew restore prayer, up to 5% over the base level. */
private fun Player.brewPrayer() {
    val base = statBase(stats.prayer)
    val room = base + base * 5 / 100 - stat(stats.prayer)
    val gain = min(2 + base * 10 / 100, room)
    if (gain > 0) {
        statAdd(stats.prayer, gain, 0)
    }
}

private fun Player.brewDrain() {
    drainByTenth(stats.attack)
    drainByTenth(stats.strength)
    drainByTenth(stats.defence)
}

// UNVERIFIED: the message, and whether the damage shows as a hitsplat.
private fun Player.divineCheck(): String? =
    if (hitpoints <= DIVINE_DAMAGE) "You do not have enough Hitpoints to drink that." else null

/** A divine potion: the boosts of its base potion, 10 damage and no decay for 5 minutes. */
private fun divine(doses: List<ObjType>, label: String, boosts: Player.() -> Unit) =
    Potion(doses, label, check = { divineCheck() }) {
        statSub(stats.hitpoints, DIVINE_DAMAGE, 0)
        boosts()
        startDivine()
    }

private val Player.combatStats
    get() = listOf(stats.attack, stats.strength, stats.defence, stats.ranged, stats.magic)

/** Drink effects of the potions Herblore makes, bar the antipoison family. */
internal val HERBLORE_POTIONS =
    listOf(
        Potion(Potions.attack, "attack potion") { boost(stats.attack, 3, 10) },
        Potion(Potions.strength, "strength potion") { boost(stats.strength, 3, 10) },
        Potion(Potions.defence, "defence potion") { boost(stats.defence, 3, 10) },
        Potion(Potions.combat, "combat potion") {
            boost(stats.attack, 3, 10)
            boost(stats.strength, 3, 10)
        },
        Potion(Potions.superAttack, "super attack potion") { superMelee(stats.attack) },
        Potion(Potions.superStrength, "super strength potion") { superMelee(stats.strength) },
        Potion(Potions.superDefence, "super defence potion") { superMelee(stats.defence) },
        Potion(Potions.restore, "restore potion") {
            for (stat in combatStats) {
                statHeal(stat, 10, 30)
            }
        },
        Potion(Potions.energy, "energy potion") { restoreRunEnergy(15) },
        Potion(Potions.superEnergy, "super energy potion") { restoreRunEnergy(20) },
        Potion(Potions.agility, "agility potion") { boost(stats.agility, 3, 0) },
        Potion(Potions.fishing, "fishing potion") { boost(stats.fishing, 3, 0) },
        Potion(Potions.hunter, "hunter potion") { boost(stats.hunter, 3, 0) },
        Potion(Potions.magicEssence, "magic essence") { boost(stats.magic, 3, 0) },
        Potion(Potions.zamorak, "Zamorak brew") {
            boost(stats.attack, 2, 20)
            boost(stats.strength, 2, 12)
            drainByTenth(stats.defence)
            statSub(stats.hitpoints, (hitpoints * 12 / 100).coerceAtMost(hitpoints - 1), 0)
            statHeal(stats.prayer, 0, 10)
        },
        Potion(Potions.bastion, "bastion potion") {
            boost(stats.ranged, 4, 10)
            boost(stats.defence, 5, 15)
        },
        Potion(Potions.battlemage, "battlemage potion") {
            boost(stats.magic, 4, 0)
            boost(stats.defence, 5, 15)
        },
        Potion(Potions.ancientBrew, "ancient brew") {
            boost(stats.magic, 2, 5)
            brewPrayer()
            brewDrain()
        },
        Potion(Potions.forgottenBrew, "forgotten brew") {
            boost(stats.magic, 3, 8)
            brewPrayer()
            brewDrain()
        },
        Potion(Potions.stamina, "stamina potion") {
            restoreRunEnergy(20)
            startStamina()
        },
        Potion(Potions.antifire, "antifire potion") { startAntifire(Antifire.NORMAL, 600) },
        Potion(Potions.extendedAntifire, "extended antifire potion") {
            startAntifire(Antifire.EXTENDED, 1200)
        },
        Potion(Potions.superAntifire, "super antifire potion") {
            startAntifire(Antifire.SUPER, 300)
        },
        Potion(Potions.extendedSuperAntifire, "extended super antifire potion") {
            startAntifire(Antifire.EXTENDED_SUPER, 600)
        },
        Potion(Potions.prayerRegeneration, "prayer regeneration potion") {
            startPrayerRegeneration()
        },
        Potion(Potions.menaphiteRemedy, "Menaphite remedy") { startRemedy() },
        // UNVERIFIED: the cooldown message.
        Potion(
            Potions.surge,
            "surge potion",
            check = {
                if (surgeReady > currentMapClock)
                    "You can only drink a surge potion every 5 minutes."
                else null
            },
        ) {
            specialEnergy = min(constants.sa_max_energy, specialEnergy + SURGE_ENERGY)
            surgeReady = currentMapClock + SURGE_COOLDOWN
        },
        divine(Potions.divineAttack, "divine super attack potion") { superMelee(stats.attack) },
        divine(Potions.divineStrength, "divine super strength potion") {
            superMelee(stats.strength)
        },
        divine(Potions.divineDefence, "divine super defence potion") { superMelee(stats.defence) },
        divine(Potions.divineRanging, "divine ranging potion") { boost(stats.ranged, 4, 10) },
        divine(Potions.divineMagic, "divine magic potion") { boost(stats.magic, 4, 0) },
        divine(Potions.divineBastion, "divine bastion potion") {
            boost(stats.ranged, 4, 10)
            boost(stats.defence, 5, 15)
        },
        divine(Potions.divineBattlemage, "divine battlemage potion") {
            boost(stats.magic, 4, 0)
            boost(stats.defence, 5, 15)
        },
        divine(Potions.divineCombat, "divine super combat potion") {
            superMelee(stats.attack)
            superMelee(stats.strength)
            superMelee(stats.defence)
        },
    )
