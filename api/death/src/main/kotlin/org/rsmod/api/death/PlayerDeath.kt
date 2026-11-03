package org.rsmod.api.death

import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.rsmod.api.combat.commons.poison.curePoison
import org.rsmod.api.config.constants
import org.rsmod.api.config.refs.components
import org.rsmod.api.config.refs.jingles
import org.rsmod.api.config.refs.midis
import org.rsmod.api.config.refs.queues
import org.rsmod.api.config.refs.seqs
import org.rsmod.api.config.refs.varps
import org.rsmod.api.player.deathResetTimers
import org.rsmod.api.player.disablePrayers
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.realm.Realm
import org.rsmod.events.EventBus
import org.rsmod.game.entity.Player
import org.rsmod.game.type.stat.StatTypeList
import org.rsmod.map.CoordGrid

@Singleton
public class PlayerDeath
@Inject
constructor(
    private val statTypes: StatTypeList,
    private val realm: Realm,
    private val eventBus: EventBus,
) {
    private var Player.specialAttackType by intVarp(varps.sa_attack)
    private var Player.specialAttackEnergy by intVarp(varps.sa_energy)
    private var Player.skullTicks by intVarp(varps.skull_ticks)
    private var Player.teleblockTicks by intVarp(varps.teleblock_ticks)
    private var Player.pkPrey1 by intVarp(varps.pk_prey1)
    private var Player.pkPrey2 by intVarp(varps.pk_prey2)
    private var Player.pkPredator1 by intVarp(varps.pk_predator1)
    private var Player.pkPredator2 by intVarp(varps.pk_predator2)
    private var Player.pkPredator3 by intVarp(varps.pk_predator3)

    public suspend fun death(access: ProtectedAccess) {
        access.deathSequence()
    }

    private suspend fun ProtectedAccess.deathSequence() {
        val respawn =
            if (player.vars[varps.ferox_respawn] == FEROX_RESPAWN_SET) {
                FEROX_RESPAWN
            } else {
                realm.config.respawnCoord
            }
        val randomRespawn = mapFindSquareLineOfWalk(respawn, minRadius = 0, maxRadius = 2)
        stopAction()
        delay(2)
        anim(seqs.human_death)
        delay(4)
        combatClearQueue()
        clearQueue(queues.death)
        midiSong(midis.stop_music)
        midiJingle(jingles.death_jingle_2)
        mes("Oh dear, you are dead!")
        val deathCoords = player.coords
        telejump(randomRespawn ?: respawn)
        resetAnim()
        eventBus.publish(this, PlayerDeathItems(deathCoords))
        resetPlayerState(statTypes)
        restoreToplevelTabs(
            components.toplevel_target_pvp_icons,
            components.toplevel_target_side1,
            components.toplevel_target_side2,
            components.toplevel_target_side4,
            components.toplevel_target_side5,
            components.toplevel_target_side6,
            components.toplevel_target_side9,
            components.toplevel_target_side8,
            components.toplevel_target_side7,
            components.toplevel_target_side10,
            components.toplevel_target_side11,
            components.toplevel_target_side12,
            components.toplevel_target_side13,
        )
    }

    private fun ProtectedAccess.resetPlayerState(stats: StatTypeList) {
        player.disablePrayers()
        player.deathResetTimers()
        player.curePoison()

        player.specialAttackType = 0
        player.specialAttackEnergy = constants.sa_max_energy
        player.runEnergy = constants.run_max_energy
        player.skullIcon = null
        player.skullTicks = 0
        player.teleblockTicks = 0

        // Previous PvP combat is wiped on death, so fighting the killer again is a new fight.
        player.pkPrey1 = 0
        player.pkPrey2 = 0
        player.pkPredator1 = 0
        player.pkPredator2 = 0
        player.pkPredator3 = 0

        rebuildAppearance()

        camReset()
        statRestoreAll(stats.values)
        minimapReset()
    }

    public companion object {
        /**
         * Ferox's `ferox_respawn` states: 0 never paid him, 1 paid but respawning in Lumbridge, 2
         * paid and respawning in the Ferox Enclave.
         */
        public const val FEROX_RESPAWN_PAID: Int = 1
        public const val FEROX_RESPAWN_SET: Int = 2

        // UNVERIFIED: the wiki puts it "by the eastern barrier", next to the pub.
        public val FEROX_RESPAWN: CoordGrid = CoordGrid(3151, 3636)
    }
}
