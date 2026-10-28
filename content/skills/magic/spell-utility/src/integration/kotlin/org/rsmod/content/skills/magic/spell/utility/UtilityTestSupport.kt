package org.rsmod.content.skills.magic.spell.utility

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import jakarta.inject.Inject
import kotlin.reflect.KClass
import org.rsmod.api.combat.commons.CombatAttack
import org.rsmod.api.combat.weapon.scripts.WeaponAttackStylesScript
import org.rsmod.api.combat.weapon.scripts.WeaponAttackTypesScript
import org.rsmod.api.combat.weapon.styles.AttackStyles
import org.rsmod.api.combat.weapon.types.AttackTypes
import org.rsmod.api.config.refs.components
import org.rsmod.api.config.refs.interfaces
import org.rsmod.api.config.refs.stats
import org.rsmod.api.config.refs.varbits
import org.rsmod.api.hit.plugin.PlayerHitScript
import org.rsmod.api.player.ui.IfOverlayButtonT
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.spells.MagicSpellModule
import org.rsmod.api.spells.MagicSpellRegistry
import org.rsmod.api.spells.attack.SpellAttackRegistry
import org.rsmod.api.spells.runes.combo.ComboRuneScript
import org.rsmod.api.spells.runes.compact.CompactRuneScript
import org.rsmod.api.spells.runes.fake.FakeRuneScript
import org.rsmod.api.spells.runes.staves.StaffSubstituteScript
import org.rsmod.api.spells.runes.subs.RuneSubstituteScript
import org.rsmod.api.spells.runes.unlimited.UnlimitedRuneScript
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.entity.PathingEntity
import org.rsmod.game.inv.InvObj
import org.rsmod.game.stat.PlayerSkillXPTable
import org.rsmod.game.stat.PlayerStatMap
import org.rsmod.game.type.obj.ObjType
import org.rsmod.game.type.stat.StatType
import org.rsmod.game.ui.Component
import org.rsmod.plugin.scripts.PluginScript

class Deps
@Inject
constructor(
    val spells: MagicSpellRegistry,
    val attacks: SpellAttackRegistry,
    val objRepo: ObjRepository,
)

private object MagicModule : AbstractModule() {
    override fun configure() {
        install(MagicSpellModule())
        install(UtilityModule())
        install(internalModule("org.rsmod.api.spells.attack.SpellAttackModule"))
        install(internalModule("org.rsmod.api.spells.runes.MagicRunesModule"))
        bind(AttackTypes::class.java).`in`(Scopes.SINGLETON)
        bind(AttackStyles::class.java).`in`(Scopes.SINGLETON)
    }

    private fun internalModule(name: String): AbstractModule =
        Class.forName(name).getDeclaredConstructor().newInstance() as AbstractModule
}

@Suppress("UNCHECKED_CAST")
private fun script(name: String): KClass<out PluginScript> =
    Class.forName(name).kotlin as KClass<out PluginScript>

/** Runs [test] with the utility spell scripts, the rune scripts and the combat spell scripts. */
internal fun GameTestState.utilityTest(test: GameTestScope.(Deps) -> Unit) =
    runInjectedGameTest(
        Deps::class,
        MagicModule,
        script("org.rsmod.api.spells.attack.scripts.SpellAttackScript"),
        TeleportScript::class,
        TelegrabScript::class,
        AlchemyScript::class,
        SuperheatScript::class,
        BonesToFruitScript::class,
        EnchantScript::class,
        BoundNpcScript::class,
        ChargeScript::class,
        ComboRuneScript::class,
        CompactRuneScript::class,
        FakeRuneScript::class,
        StaffSubstituteScript::class,
        RuneSubstituteScript::class,
        UnlimitedRuneScript::class,
        PlayerHitScript::class,
        WeaponAttackStylesScript::class,
        WeaponAttackTypesScript::class,
        testBody = test,
    )

/** Empties the inventory, fills it with [items] from slot 0 and sets Magic and Smithing to 99. */
internal fun GameTestScope.setUp(vararg items: Pair<ObjType, Int>) {
    player.clearInv()
    // Items that do not stack take one slot each.
    var slot = 0
    for ((obj, count) in items) {
        if (objTypes[obj].isStackable) {
            player.inv[slot++] = InvObj(obj, count)
        } else {
            repeat(count) { player.inv[slot++] = InvObj(obj, 1) }
        }
    }
    player.stats[stats.magic] = 99
    player.stats[stats.smithing] = 99
    player.setVarBit(varbits.spellbook, 0)
    player.ifOpenOverlay(interfaces.magic_spellbook, components.toplevel_target_side6)
}

/** The fine xp gained in [stat] since [setUp] put it at level 99. */
internal fun GameTestScope.gained(stat: StatType): Int =
    player.statMap.getFineXP(stat) - PlayerSkillXPTable.getFineXPFromLevel(99)

internal fun fineXp(xp: Double): Int = PlayerStatMap.toFineXP(xp).toInt()

/** Casts [obj]'s spell on the inventory item in [slot], as the client's spell-on-item click. */
internal fun GameTestScope.castOnItem(deps: Deps, spell: ObjType, slot: Int) {
    val component = checkNotNull(deps.spells.getObjSpell(spell)).component
    val held = checkNotNull(player.inv[slot])
    val event =
        IfOverlayButtonT(
            player = player,
            selectedSlot = -1,
            selectedObj = null,
            targetSlot = slot,
            targetObj = objTypes[held.id],
            selectedComponent = Component(component.packed),
            targetComponent = Component(components.inv_items.packed),
        )
    eventBus.publish(event)
}

/** Runs the combat spell [obj] against [target], skipping the walk into range. */
internal fun GameTestScope.castAt(deps: Deps, spell: ObjType, target: PathingEntity) {
    val magic = checkNotNull(deps.spells.getObjSpell(spell))
    val attack = checkNotNull(deps.attacks[spell])
    player.withProtectedAccess {
        when (target) {
            is org.rsmod.game.entity.Npc ->
                with(attack) { attack(target, CombatAttack.Spell(null, magic, false)) }
            is org.rsmod.game.entity.Player ->
                with(attack) { attack(target, CombatAttack.Spell(null, magic, false)) }
        }
    }
}
