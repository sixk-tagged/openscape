package org.rsmod.content.other.grandexchangeextras

import jakarta.inject.Inject
import kotlin.random.Random
import org.rsmod.api.config.refs.objs
import org.rsmod.api.invtx.add
import org.rsmod.api.invtx.delete
import org.rsmod.api.invtx.invTransaction
import org.rsmod.api.invtx.select
import org.rsmod.api.market.MarketPrices
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.output.objExamine
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onIfModalButton
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.script.onOpNpc4
import org.rsmod.content.interfaces.equipment.prices.GuidePriceScript
import org.rsmod.content.skills.herblore.Potions
import org.rsmod.game.inv.InvObj
import org.rsmod.game.type.interf.IfButtonOp
import org.rsmod.game.type.interf.IfEvent
import org.rsmod.game.type.npc.NpcType
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.obj.UnpackedObjType
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Brugsen Bursen and the five Grand Exchange price experts: Talk-to, Prices, and the two decanting
 * services of Bob Barter and Murky Matt.
 */
class GeExpertsScript
@Inject
constructor(
    private val objTypes: ObjTypeList,
    private val marketPrices: MarketPrices,
    private val guide: GuidePriceScript,
) : PluginScript() {
    private class Expert(val npc: NpcType, val title: String, val names: List<String>)

    private val experts =
        listOf(
            Expert(ge_extras_npcs.ge_expert_ores, "Ores and Bars", PriceListItems.ORES),
            Expert(ge_extras_npcs.ge_expert_herbs, "Herbs and Potions", PriceListItems.HERBS),
            Expert(ge_extras_npcs.ge_expert_runes, "Runes", PriceListItems.RUNES),
            Expert(ge_extras_npcs.ge_expert_logs, "Logs", PriceListItems.LOGS),
            Expert(ge_extras_npcs.ge_expert_combat, "Weapons and Armour", PriceListItems.COMBAT),
        )

    /** Tradeable GE objs by lowercase name; the lowest id wins a shared name. */
    private val byName: Map<String, UnpackedObjType> by lazy {
        objTypes.values
            .filter { !it.isCert && !it.isPlaceholder && it.stockmarket }
            .sortedBy { it.id }
            .groupBy { it.name.lowercase() }
            .mapValues { it.value.first() }
    }

    private val decanter = Decanter(objTypes)

    /** Every "Name(n)" obj by lowercase name, as (n, obj) pairs. */
    private val dosed: Map<String, List<Pair<Int, UnpackedObjType>>> by lazy {
        objTypes.values
            .filter { !it.isCert && !it.isPlaceholder && it.tradeable }
            .mapNotNull { type -> DOSE_NAME.matchEntire(type.name)?.let { it to type } }
            .groupBy(
                { it.first.groupValues[1].lowercase() },
                { it.first.groupValues[2].toInt() to it.second },
            )
    }

    /** Bob's potions, plus the oils and mixes that come in the same vials. */
    private val potionFamilies: List<DoseFamily> by lazy {
        val extra =
            dosed
                .filterKeys { base -> OIL_OR_MIX.any { base.endsWith(it) } }
                .values
                .mapNotNull { doseFamily(it) }
        Potions.all.map { family -> DoseFamily(family.reversed().map { objTypes[it] }) } + extra
    }

    /** Murky Matt's jewellery, up to the charges it holds from the shop. */
    private val jewelleryFamilies: List<DoseFamily> by lazy {
        JEWELLERY.mapNotNull { (base, cap) ->
            doseFamily(dosed[base].orEmpty().filter { it.first <= cap }, cap)
        }
    }

    private fun doseFamily(
        doses: List<Pair<Int, UnpackedObjType>>,
        cap: Int = doses.maxOfOrNull { it.first } ?: 0,
    ): DoseFamily? {
        val byDose = doses.sortedBy { it.second.id }.associate { it.first to it.second }
        if (cap < 2 || (1..cap).any { it !in byDose }) return null
        return DoseFamily((1..cap).map { byDose.getValue(it) })
    }

    override fun ScriptContext.startup() {
        onOpNpc1(ge_extras_npcs.ge_boss) { startDialogue(it.npc) { brugsen() } }
        onOpNpc3(ge_extras_npcs.ge_boss) { with(guide) { openGuide() } }
        onOpNpc1(ge_extras_npcs.ge_expert_ores) { startDialogue(it.npc) { farid() } }
        onOpNpc1(ge_extras_npcs.ge_expert_herbs) { startDialogue(it.npc) { bob() } }
        onOpNpc1(ge_extras_npcs.ge_expert_runes) { startDialogue(it.npc) { murkyMatt() } }
        onOpNpc1(ge_extras_npcs.ge_expert_logs) { startDialogue(it.npc) { relobo() } }
        onOpNpc1(ge_extras_npcs.ge_expert_combat) { startDialogue(it.npc) { hofuthand() } }
        for (expert in experts) {
            onOpNpc3(expert.npc) { openPrices(expert) }
        }
        onOpNpc4(ge_extras_npcs.ge_expert_herbs) { startDialogue(it.npc) { bobDecant() } }
        onOpNpc4(ge_extras_npcs.ge_expert_runes) { startDialogue(it.npc) { mattCombine() } }
        onIfModalButton(ge_extras_components.price_list_items) { examinePrice(it.comsub, it.op) }
    }

    // ---- Price list (237)

    private fun ProtectedAccess.openPrices(expert: Expert) {
        val rows = inv(ge_extras_invs.price_list)
        invClear(rows)
        val items = expert.names.mapNotNull { byName[it.lowercase()] }
        for ((slot, type) in items.take(rows.size).withIndex()) {
            rows[slot] = InvObj(type, (marketPrices[type] ?: 1).coerceAtLeast(1))
        }
        invTransmit(rows)
        ifOpenMainModal(ge_extras_interfaces.price_list)
        runClientScript(PRICE_LIST_SCRIPT, rows.type.id, expert.title)
        ifSetEvents(ge_extras_components.price_list_items, rows.indices, IfEvent.Op10)
    }

    private fun ProtectedAccess.examinePrice(slot: Int, op: IfButtonOp) {
        if (op != IfButtonOp.Op10) return
        val obj = inv(ge_extras_invs.price_list)[slot] ?: return
        player.objExamine(objTypes[obj], 1, obj.count)
    }

    private fun Dialogue.showPrices(npc: NpcType) {
        access.openPrices(experts.first { it.npc == npc })
    }

    // ---- Brugsen Bursen

    private suspend fun Dialogue.brugsen() {
        chatNpc(happy, "Hello, and welcome to the Grand Exchange! How can I help you?")
        while (true) {
            when (
                choice3(
                    "What is this place?",
                    1,
                    "Can you tell me the price of an item?",
                    2,
                    "I'm fine, thanks.",
                    3,
                )
            ) {
                1 -> {
                    chatPlayer(quiz, "What is this place?")
                    chatNpc(
                        happy,
                        "This is the Grand Exchange, the best trading spot in the land! People " +
                            "come from all across the country to buy and sell every item under " +
                            "the sun.",
                    )
                    chatNpc(
                        happy,
                        "When you want to buy or sell an item, talk to the clerks in the middle. " +
                            "They'll tell you how to set up a trade offer.",
                    )
                    chatNpc(happy, "Now, is there something I can do for you?")
                }
                2 -> {
                    chatPlayer(quiz, "Can you tell me the price of an item?")
                    chatNpc(happy, "What had you in mind?")
                    chatPlayer(happy, "Thank you.")
                    with(guide) { access.openGuide() }
                    return
                }
                else -> {
                    chatPlayer(neutral, "I'm fine, thanks.")
                    return
                }
            }
        }
    }

    // ---- Farid Morrisane

    private suspend fun Dialogue.farid() {
        chatPlayer(neutral, "Hello, little boy.")
        chatNpc(
            angry,
            "I would prefer it if you didn't speak to me in such a manner. I'll have you know " +
                "I'm an accomplished merchant.",
        )
        while (true) {
            when (
                choice3(
                    "Calm down, junior.",
                    1,
                    "Can you show me the prices of ores and bars?",
                    2,
                    "I best go and speak with someone more my height.",
                    3,
                )
            ) {
                1 -> {
                    chatPlayer(neutral, "Calm down, junior.")
                    chatNpc(angry, "Don't tell me to calm down! And don't call me 'junior'.")
                    chatNpc(
                        angry,
                        "I'll have you know I am Farid Morrisane, son of Ali Morrisane, the " +
                            "world's greatest merchant!",
                    )
                    chatPlayer(quiz, "Then why are you here and not him?")
                    chatNpc(
                        neutral,
                        "My dad has given me the responsibility of expanding our business here.",
                    )
                    chatPlayer(
                        laugh,
                        "And you're up to the task? What a grown up boy you are! Mummy and " +
                            "daddy must be very pleased!",
                    )
                    // UNVERIFIED: the wiki gives [mate/darling] by gender; this is the first.
                    chatNpc(
                        angry,
                        "Look, mate - I may be young, I may be short, but I'm a respected " +
                            "merchant around here and don't have time to deal with simpletons " +
                            "like you.",
                    )
                }
                2 -> {
                    chatPlayer(quiz, "Can you show me the prices of ores and bars?")
                    showPrices(ge_extras_npcs.ge_expert_ores)
                    return
                }
                else -> {
                    chatPlayer(neutral, "I best go and speak with someone more my height.")
                    chatNpc(neutral, "Then I shall not stop you, mister. I've too much work to do.")
                    return
                }
            }
        }
    }

    // ---- Bob Barter

    private suspend fun Dialogue.bob() {
        chatNpc(
            happy,
            "Hello, chum, fancy buyin' some designer jewellry? They've come all the way from " +
                "Ardougne! Most pukka!",
        )
        chatPlayer(neutral, "Erm, no. I'm all set, thanks.")
        chatNpc(happy, "Okay, chum, would you like to show you the very latest potion prices?")
        while (true) {
            when (
                choice3(
                    "Who are you?",
                    1,
                    "Can you show me the prices for potions?",
                    2,
                    "I'll leave you to it.",
                    3,
                )
            ) {
                1 -> {
                    chatPlayer(quiz, "Who are you?")
                    chatNpc(happy, "Why, I'm Bob! Your friendly seller of smashin' goods!")
                    chatPlayer(quiz, "So what do you have to sell?")
                    chatNpc(
                        neutral,
                        "Oh, not much at the moment. Cuz, ya know, business being so well and " +
                            "cushie.",
                    )
                    chatPlayer(neutral, "You don't really look like you're being so successful.")
                    chatNpc(
                        angry,
                        "You plonka! It's all a show, innit! If I let people knows I'm in good " +
                            "business they'll want a share of the moolah!",
                    )
                    chatPlayer(neutral, "You conveniently have a response for everything.")
                    chatNpc(happy, "That's the Ardougne way, my son.")
                }
                2 -> {
                    chatPlayer(quiz, "Can you show me the prices for potions?")
                    showPrices(ge_extras_npcs.ge_expert_herbs)
                    return
                }
                else -> {
                    chatPlayer(neutral, "I'll leave you to it.")
                    return
                }
            }
        }
    }

    private suspend fun Dialogue.bobDecant() {
        // UNVERIFIED: the wiki says Bob decants into "a specified number of doses (1-4)"; the
        // menu wording is a guess.
        val doses =
            choice4(
                "4 doses",
                4,
                "3 doses",
                3,
                "2 doses",
                2,
                "1 dose",
                1,
                title = "Decant into how many doses?",
            )
        if (doses < 4) {
            val line =
                if (Random.nextBoolean()) "Lets see what you have..."
                else "Two plus two is four, minus one that's three..."
            chatNpc(neutral, line)
        }
        val vial = objTypes[GeExtrasObjs.vial_empty]
        val result = with(decanter) { access.decant(potionFamilies, doses, vial, VIAL_PRICE) }
        when (result) {
            DecantResult.Done -> chatNpc(happy, "There, all done.")
            DecantResult.Nothing ->
                chatNpc(neutral, "I don't think you've got anything that I can decant.")
            DecantResult.NoSpace -> chatNpc(neutral, "You're a bit short of inventory space.")
            DecantResult.NoVessels ->
                chatNpc(
                    neutral,
                    "You're a bit short of empty vessels or cash. Empty vials cost 5 coins " +
                        "each, cups 8 coins each, and anything else is 5 coins each.",
                )
        }
    }

    // ---- Murky Matt

    private suspend fun Dialogue.murkyMatt() {
        chatNpc(
            happy,
            "Arrr, what is it that ye be wantin? I can tell ye all about the prices of runes, " +
                "or perhaps I could combine the charges on yer teleport jewellery.",
        )
        while (true) {
            when (
                choice5(
                    "What's a pirate doing here?",
                    1,
                    "Tell me about the prices of runes.",
                    2,
                    "Combine the charges on my teleport jewellery, please.",
                    3,
                    "I hear you deal in rings of forging?",
                    4,
                    "I got to go, erm, swab some decks! Yarr!",
                    5,
                )
            ) {
                1 -> pirate()
                2 -> {
                    chatPlayer(quiz, "Tell me about the prices of runes.")
                    showPrices(ge_extras_npcs.ge_expert_runes)
                    return
                }
                3 -> {
                    chatPlayer(neutral, "Combine the charges on my teleport jewellery, please.")
                    mattCombine()
                    return
                }
                4 -> {
                    ringsOfForging()
                    return
                }
                else -> {
                    chatPlayer(laugh, "I got to go, erm, swab some decks! Yarr!")
                    chatNpc(
                        angry,
                        "Defer your speech right there! Quit this derogatory and somewhat " +
                            "narrow-minded allusion that all folks of sea voyage are only " +
                            "concerned with washing the decks, looking after parrots and " +
                            "drinking rum. I'll have ye",
                    )
                    chatNpc(angry, "know there is much more to a pirate than meets the eye.")
                    chatPlayer(neutral, "Aye-aye, captain!")
                    chatNpc(silent, "...")
                    chatPlayer(angry, "Oh, come on! Lighten up!")
                    return
                }
            }
        }
    }

    private suspend fun Dialogue.pirate() {
        chatPlayer(quiz, "What's a pirate doing here?")
        chatNpc(shocked, "By my sea-blistered skin, I could ask the same of you!")
        chatPlayer(confused, "But... I'm not a pirate?")
        chatNpc(
            neutral,
            "No? Then what's that smell? The smell o' someone spent too long at sea without a " +
                "bath!",
        )
        chatPlayer(neutral, "I think that's probably you.")
        chatNpc(
            laugh,
            "Har har har! We've got a stern landlubber 'ere! Well, let me tell ye, I'm here " +
                "for the Grand Exchange! Gonna cash in me loot!",
        )
        chatPlayer(
            quiz,
            "Don't you just want to sell it in a shop or trade it to someone specific?",
        )
        chatNpc(
            happy,
            "By my wave-battered bones! Not when I can sell to the whole world from this very " +
                "spot!",
        )
    }

    private suspend fun Dialogue.mattCombine() {
        val result = with(decanter) { access.decant(jewelleryFamilies, Int.MAX_VALUE, null, 0) }
        when (result) {
            DecantResult.Done -> chatNpc(happy, "Arr, all done.")
            DecantResult.NoSpace ->
                chatNpc(neutral, "Arrr, I think ye be a bit short of inventory space.")
            else -> chatNpc(neutral, "Arrr, ye've got nothing that I can combine.")
        }
    }

    private suspend fun Dialogue.ringsOfForging() {
        chatPlayer(quiz, "I hear you deal in rings of forging?")
        val rings = access.invTotal(access.inv, GeExtrasObjs.ruby_ring)
        val coins = access.invTotal(access.inv, objs.coins)
        if (rings == 0) {
            chatNpc(neutral, "Arrrr, bring me yer ruby rings to enchant.")
            return
        }
        if (coins < FORGING_COST) {
            chatNpc(neutral, "Arrrr, but they arrre 250gp per ring.")
            return
        }
        chatNpc(happy, "Arrr, 250gp per ring. How many would ye like me to enchant?")
        val wanted = access.countDialog("How many rings of forging?")
        if (wanted <= 0) {
            chatNpc(neutral, "Arrr, be seeing ya.")
            return
        }
        val count = minOf(wanted, rings, coins / FORGING_COST)
        val done =
            access.player.invTransaction(access.inv) {
                val image = select(access.inv)
                delete(image, GeExtrasObjs.ruby_ring.id, count)
                delete(image, objs.coins.id, count * FORGING_COST)
                add(image, GeExtrasObjs.ring_of_forging.id, count)
            }
        if (done.success) {
            chatNpc(happy, "Arrr, pleasure doin' business with ya.")
        }
    }

    // ---- Relobo Blinyo

    private suspend fun Dialogue.relobo() {
        chatNpc(happy, "Hello, my friend. Would you like me to show you the prices of logs?")
        while (true) {
            when (
                choice3(
                    "You look like you've travelled a fair distance.",
                    1,
                    "Okay, show me the prices of logs.",
                    2,
                    "Sorry, I need to be making tracks.",
                    3,
                )
            ) {
                1 -> {
                    chatPlayer(neutral, "You look like you've travelled a fair distance.")
                    chatNpc(quiz, "What gave me away?")
                    chatPlayer(
                        neutral,
                        "I don't mean to be rude, but the clothing and hair, for starters.",
                    )
                    chatNpc(
                        happy,
                        "Ah, yes. I'm from Shilo Village on Karamja, it's a style I've had since " +
                            "I was little.",
                    )
                    chatPlayer(quiz, "Then tell me, why are you so far from home?")
                    chatNpc(
                        happy,
                        "This Grand Exchange! Isn't it marvellous! I've never seen anything " +
                            "like it in my life.",
                    )
                    chatNpc(
                        neutral,
                        "My people were saddened when I chose to leave home and travel here, " +
                            "but I hope to make some serious profit, then return to my tribe.",
                    )
                    chatPlayer(quiz, "So, what are you selling?")
                    chatNpc(
                        happy,
                        "Logs! Of all kinds! That's my plan, at least. Living in the jungle as " +
                            "we do, my people understand exotic wood very well indeed.",
                    )
                }
                2 -> {
                    chatPlayer(neutral, "Okay, show me the prices of logs.")
                    showPrices(ge_extras_npcs.ge_expert_logs)
                    return
                }
                else -> {
                    chatPlayer(neutral, "Sorry, I need to be making tracks.")
                    chatNpc(happy, "Okay, nice talking to you!")
                    return
                }
            }
        }
    }

    // ---- Hofuthand

    // Note: the bank auto-charging option is left out, the service is not built.
    private suspend fun Dialogue.hofuthand() {
        chatPlayer(happy, "Hello!")
        chatNpc(
            quiz,
            "What? Oh, hello. I was deep in thought. Did you want me to show you the prices of " +
                "weapons and armour?",
        )
        while (true) {
            when (
                choice3(
                    "You seem a bit flustered.",
                    1,
                    "Yes, show me the prices of weapons and armour.",
                    2,
                    "I'll leave you alone.",
                    3,
                )
            ) {
                1 -> {
                    chatPlayer(quiz, "You seem a bit flustered.")
                    chatNpc(
                        neutral,
                        "Sorry, I'm just deep in thought. I'm waiting for many deals to " +
                            "complete today.",
                    )
                    chatPlayer(quiz, "What sort of things are you selling?")
                    chatNpc(
                        happy,
                        "Good old weapons and armour! My people - dwarves, you understand - are " +
                            "hoping I can trade with humans.",
                    )
                    chatPlayer(happy, "It looks like you've come to the right place for that.")
                    chatNpc(happy, "I have indeed, my friend. Now, can I help you?")
                }
                2 -> {
                    chatPlayer(neutral, "Yes, show me the prices of weapons and armour.")
                    showPrices(ge_extras_npcs.ge_expert_combat)
                    return
                }
                else -> {
                    chatPlayer(neutral, "I'll leave you alone.")
                    chatNpc(neutral, "Thank you, I have much on my mind.")
                    return
                }
            }
        }
    }

    private companion object {
        const val PRICE_LIST_SCRIPT = 799
        const val FORGING_COST = 250
        const val VIAL_PRICE = 5
        val DOSE_NAME = Regex("""^(.+?)\s*\((\d+)\)$""")
        val OIL_OR_MIX = listOf("olive oil", "sacred oil", "moth mix", " mix")

        /** Jewellery by lowercase name and its charges when full from the shop. */
        val JEWELLERY =
            mapOf(
                "amulet of glory" to 4,
                "combat bracelet" to 4,
                "skills necklace" to 4,
                "ring of wealth" to 5,
                "games necklace" to 8,
                "ring of dueling" to 8,
                "necklace of passage" to 5,
                "burning amulet" to 5,
                "digsite pendant" to 5,
                "slayer ring" to 8,
                "ring of returning" to 5,
            )
    }
}
