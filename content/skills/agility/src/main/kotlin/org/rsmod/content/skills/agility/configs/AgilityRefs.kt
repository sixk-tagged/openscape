@file:Suppress("unused", "SpellCheckingInspection")

package org.rsmod.content.skills.agility.configs

import org.rsmod.api.type.builders.varp.VarpBuilder
import org.rsmod.api.type.editors.inv.InvEditor
import org.rsmod.api.type.refs.currency.CurrencyReferences
import org.rsmod.api.type.refs.inv.InvReferences
import org.rsmod.api.type.refs.loc.LocReferences
import org.rsmod.api.type.refs.npc.NpcReferences
import org.rsmod.api.type.refs.obj.ObjReferences
import org.rsmod.api.type.refs.seq.SeqReferences
import org.rsmod.api.type.refs.varp.VarpReferences
import org.rsmod.game.type.inv.InvScope
import org.rsmod.game.type.inv.InvStackType

object AgilityLocs : LocReferences() {
    // Gnome Stronghold
    val gnome_log = find("gnome_log_balance1")
    val gnome_net_up = find("obstical_net2")
    val gnome_branch_up = find("climbing_branch")
    val gnome_rope = find("balancing_rope")
    val gnome_branch_down = find("climbing_tree")
    val gnome_net_down = find("obstical_net3")
    val gnome_pipe_1 = find("obstical_pipe3_1")
    val gnome_pipe_2 = find("obstical_pipe3_2")

    // Draynor
    val draynor_wall = find("rooftops_draynor_wallclimb")
    val draynor_rope_1 = find("rooftops_draynor_tightrope_1")
    val draynor_rope_2 = find("rooftops_draynor_tightrope_2")
    val draynor_narrow_wall = find("rooftops_draynor_wallcrossing")
    val draynor_scramble = find("rooftops_draynor_wallscramble")
    val draynor_gap = find("rooftops_draynor_leapdown")
    val draynor_crate = find("rooftops_draynor_crate")

    // Al Kharid
    val kharid_wall = find("rooftops_kharid_wallclimb")
    val kharid_rope_1 = find("rooftops_kharid_tightrope_1")
    val kharid_cable = find("rooftops_kharid_rope_swing")
    val kharid_zip = find("rooftops_kharid_slide_side")
    val kharid_tree = find("rooftops_kharid_bamboo_tree_top")
    val kharid_beams = find("rooftops_kharid_wallclimb_2")
    val kharid_rope_2 = find("rooftops_kharid_tightrope_4")
    val kharid_gap = find("rooftops_kharid_leapdown")

    // Varrock
    val varrock_wall = find("rooftops_varrock_wallclimb")
    val varrock_clothesline = find("rooftops_varrock_clothesline")
    val varrock_gap_1 = find("rooftops_varrock_leaptoruins")
    val varrock_balance = find("rooftops_varrock_wallswing")
    val varrock_scramble = find("rooftops_varrock_wallscramble")
    val varrock_gap_2 = find("rooftops_varrock_leaptobalcony")
    val varrock_gap_3 = find("rooftops_varrock_leapdown")
    val varrock_ledge = find("rooftops_varrock_stepuproof")
    val varrock_edge = find("rooftops_varrock_finish")

    // Falador
    val falador_wall = find("rooftops_falador_wallclimb")
    val falador_rope_1 = find("rooftops_falador_tightrope_1")
    val falador_handholds = find("rooftops_falador_handholds_start")
    val falador_gap_1 = find("rooftops_falador_gap_1")
    val falador_gap_2 = find("rooftops_falador_gap_2")
    val falador_rope_2 = find("rooftops_falador_tightrope_2")
    val falador_rope_3 = find("rooftops_falador_tightrope_3")
    val falador_gap_3 = find("rooftops_falador_gap_3")
    val falador_ledge_1 = find("rooftops_falador_ledge_1")
    val falador_ledge_2 = find("rooftops_falador_ledge_2")
    val falador_ledge_3a = find("rooftops_falador_ledge_3a")
    val falador_ledge_3b = find("rooftops_falador_ledge_3b")
    val falador_ledge_4 = find("rooftops_falador_ledge_4")
    val falador_edge = find("rooftops_falador_edge")

    // Wilderness
    val wilderness_pipe = find("obstical_pipe2")
    val wilderness_ropeswing = find("obstical_ropeswing2")
    val wilderness_stones = find("steppingstone1")
    val wilderness_log = find("wilderness_log_balance1")
    val wilderness_rocks = find("wildclimbingrock")
    val wilderness_dispenser = find("wildy_agility_pillar")
    val wilderness_gate_south = find("balancegate52a")
    val wilderness_gate_north_left = find("balancegate52b_left")
    val wilderness_gate_north_right = find("balancegate52b_right")

    // Canifis
    val canifis_tree = find("rooftops_canifis_start_tree")
    val canifis_gap_1 = find("rooftops_canifis_jump")
    val canifis_gap_2 = find("rooftops_canifis_jump_2")
    val canifis_gap_3 = find("rooftops_canifis_jump_5")
    val canifis_gap_4 = find("rooftops_canifis_jump_3")
    val canifis_vault = find("rooftops_canifis_polevault")
    val canifis_gap_5 = find("rooftops_canifis_jump_4")
    val canifis_gap_6 = find("rooftops_canifis_leapdown")

    // Barbarian Outpost
    val barbarian_ropeswing = find("obstical_ropeswing1")
    val barbarian_log = find("barbarian_log_balance1")
    val barbarian_net = find("agility_obstical_net_barbarian")
    val barbarian_ledge = find("balancing_ledge1")
    val barbarian_ladder = find("barbarian_laddertop_norim")
    val barbarian_wall = find("castlecrumbly1")

    // Seers' Village
    val seers_wall = find("rooftops_seers_wallclimb")
    val seers_gap_1 = find("rooftops_seers_jump")
    val seers_rope = find("rooftops_seers_tightrope")
    val seers_gap_2 = find("rooftops_seers_jump_1")
    val seers_gap_3 = find("rooftops_seers_jump_2")
    val seers_edge = find("rooftops_seers_leapdown")

    // Pollnivneach
    val pollnivneach_basket = find("rooftops_pollnivneach_basket")
    val pollnivneach_stall = find("rooftops_pollnivneach_marketstall")
    val pollnivneach_banner = find("rooftops_pollnivneach_hangingbanner")
    val pollnivneach_gap = find("rooftops_pollnivneach_gap")
    val pollnivneach_tree_1 = find("rooftops_pollnivneach_tree")
    val pollnivneach_wall = find("rooftops_pollnivneach_wallclimb")
    val pollnivneach_bars = find("rooftops_pollnivneach_monkeybars_start")
    val pollnivneach_tree_2 = find("rooftops_pollnivneach_treetop")
    val pollnivneach_line = find("rooftops_pollnivneach_line")

    // Rellekka
    val rellekka_wall = find("rooftops_rellekka_wallclimb")
    val rellekka_gap_1 = find("rooftops_rellekka_gap_1")
    val rellekka_rope_1 = find("rooftops_rellekka_tightrope_1")
    val rellekka_gap_2 = find("rooftops_rellekka_gap_2")
    val rellekka_gap_3 = find("rooftops_rellekka_gap_3")
    val rellekka_rope_2 = find("rooftops_rellekka_tightrope_3")
    val rellekka_fish = find("rooftops_rellekka_dropoff")

    // Ardougne
    val ardougne_beams = find("rooftops_ardy_wallclimb")
    val ardougne_gap_1 = find("rooftops_ardy_jump")
    val ardougne_plank = find("rooftops_ardy_plank")
    val ardougne_gap_2 = find("rooftops_ardy_jump_2")
    val ardougne_gap_3 = find("rooftops_ardy_jump_3")
    val ardougne_roof = find("rooftops_ardy_wallcrossing")
    val ardougne_gap_4 = find("rooftops_ardy_jump_4")

    // Shortcuts
    val falador_crumbling_wall = find("fai_falador_castle_crumble_mid")
    val varrock_south_fence = find("lumbridge_sc_fencejump")
    val edgeville_bars_end = find("monkeybars_end1")
    val edgeville_bars_end_2 = find("monkeybars_end2")
    val burthorpe_tight_gap = find("burthorpe_diary_shortcut")
    val salve_railing_south = find("morytania_railing_sc_fence_1")
    val salve_railing_north = find("morytania_railing_sc_fence_2")
    val varrock_tunnel_west = find("varrock_sc_tunnel_west")
    val varrock_tunnel_east = find("varrock_sc_tunnel_east")
    val falador_tunnel_south = find("falador_sc_castlewall_south")
    val falador_tunnel_north = find("falador_sc_castlewall_north")
    val draynor_manor_stones = find("lumbridge_sc_stepstone")
    val varrock_trellis = find("garden_trellis_concave_shortcut")
    val alkharid_rocks_top = find("alkharid_mine_sc_top")
    val alkharid_rocks_bottom = find("alkharid_mine_sc_bottom")
    val draynor_tunnel_west = find("draynor_diary_under_wall_w")
    val draynor_tunnel_east = find("draynor_diary_under_wall_e")
    val fremennik_log_south = find("slayer_river_sc_logbalance_3")
    val fremennik_log_north = find("slayer_river_sc_logbalance_1")
    val sewers_pipe = find("varrock_dungeon_pipe_sc")
    val alkharid_palace_window = find("kharid_poshwall_topless")
    val chaos_temple_stones = find("wilderness_chaos_temple_shortcut")
    val lava_dragon_stones = find("wilderness_lava_dragons_shortcut")
    val lava_maze_stones = find("wilderness_lava_maze_northern_shortcut")

    // Gates, Rogues' Den, spirit trees
    val member_gate_left = find("membergatel")
    val member_gate_right = find("membergater")
    val roguesden_trapdoor = find("roguesden_trapdoor_entrance")
    val roguesden_passage = find("roguesden_passage_to_pub")
    val spirit_tree_big_1 = find("spirittree_big_1op")
    val spirit_tree_big_2 = find("spirittree_big_2ops")
    val spirit_tree_big_orbs = find("spirittree_big_2ops_orbs")
    val spirit_tree_small_1 = find("spirittree_small_1op")
    val spirit_tree_small_2 = find("spirittree_small_2ops")
}

object AgilitySeqs : SeqReferences() {
    val climb = find("human_reachforladder")
    val balance_loop = find("human_walk_logbalance_loop")
    val balance_walk = find("human_walk_logbalance")
    val balance_ready = find("human_walk_logbalance_ready")
    val jump_up = find("agility_shortcut_wall_jump")
    val jump_down = find("agility_shortcut_wall_jumpdown")
    val hurdle = find("human_jump_hurdle")
    val pipe = find("human_pipesqueeze")
    val ropeswing = find("human_ropeswing")
    val zipline = find("zipline_slide")
    val pole_vault = find("rooftops_pole_vault")
    val crumbled_wall = find("human_walk_crumbledwall")
    val monkeybars = find("human_monkeybars_walk")
    val stepping_stone = find("human_steppingstonejump")
}

object AgilityObjs : ObjReferences() {
    val mark_of_grace = find("grace")
    val wilderness_ticket = find("wildy_agility_token")
    val looting_bag = find("looting_bag")
    val looting_bag_open = find("looting_bag_open")
    val blighted_anglerfish = find("blighted_anglerfish")
    val blighted_manta_ray = find("blighted_mantaray")
    val blighted_karambwan = find("blighted_karambwan")
    val blighted_super_restore = find("blighted_4dose2restore")
    val steel_platebody = find("steel_platebody")
    val mithril_chainbody = find("mithril_chainbody")
    val mithril_platelegs = find("mithril_platelegs")
    val mithril_plateskirt = find("mithril_plateskirt")
    val adamant_full_helm = find("adamant_full_helm")
    val adamant_platebody = find("adamant_platebody")
    val adamant_platelegs = find("adamant_platelegs")
    val rune_med_helm = find("rune_med_helm")
    val rune_chainbody = find("rune_chainbody")
    val rune_kiteshield = find("rune_kiteshield")
    val coins = find("coins")
    val graceful_hood = find("graceful_hood")
    val graceful_top = find("graceful_top")
    val graceful_legs = find("graceful_legs")
    val graceful_gloves = find("graceful_gloves")
    val graceful_boots = find("graceful_boots")
    val graceful_cape = find("graceful_cape")
    val amylase_pack = find("pack_amylase")
}

object AgilityNpcs : NpcReferences() {
    val grace = find("rooftops_grace")
}

object AgilityCurrencies : CurrencyReferences() {
    val mark_of_grace = find("mark_of_grace")
}

object AgilityInvs : InvReferences() {
    /** Unused vanilla inv, repurposed as Grace's Graceful Clothing. */
    val grace_shop = find("roguesden_shop")
}

object AgilityVarps : VarpReferences() {
    /** Custom: `(course id shl 8) or obstacles passed in order` of the lap in progress. */
    val progress = find("agility_progress")

    /** Custom: the game minute (minutes since 1970) from which marks of grace can spawn again. */
    val mark_minute = find("agility_mark_minute")

    /** Custom: 0 until the Wilderness dispenser's fee is paid, then 1 + the laps done since. */
    val dispenser = find("agility_dispenser")
}

internal object AgilityVarpBuilder : VarpBuilder() {
    init {
        build("agility_progress")
        build("agility_mark_minute")
        build("agility_dispenser")
    }
}

internal object AgilityInvEditor : InvEditor() {
    init {
        // Grace's Graceful Clothing: counts are display only, the stock never runs out.
        edit(AgilityInvs.grace_shop) {
            scope = InvScope.Shared
            stack = InvStackType.Always
            autoSize = true
            stock += stock(AgilityObjs.graceful_hood, count = 100, restockCycles = 1)
            stock += stock(AgilityObjs.graceful_top, count = 100, restockCycles = 1)
            stock += stock(AgilityObjs.graceful_legs, count = 100, restockCycles = 1)
            stock += stock(AgilityObjs.graceful_gloves, count = 100, restockCycles = 1)
            stock += stock(AgilityObjs.graceful_boots, count = 100, restockCycles = 1)
            stock += stock(AgilityObjs.graceful_cape, count = 100, restockCycles = 1)
            stock += stock(AgilityObjs.amylase_pack, count = 1000, restockCycles = 1)
        }
    }
}
