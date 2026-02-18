package com.reverendinsanity.core.gu;

import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.ReverendInsanity;
import net.minecraft.resources.ResourceLocation;
import java.util.*;
import java.util.stream.Collectors;

// 蛊虫注册表
public class GuRegistry {

    private static final Map<ResourceLocation, GuType> REGISTRY = new LinkedHashMap<>();

    public static void register(GuType type) {
        REGISTRY.put(type.id(), type);
    }

    public static GuType get(ResourceLocation id) {
        return REGISTRY.get(id);
    }

    public static Collection<GuType> getAll() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    public static List<GuType> getByPath(DaoPath path) {
        return REGISTRY.values().stream()
            .filter(g -> g.path() == path)
            .collect(Collectors.toList());
    }

    public static List<GuType> getByRank(int rank) {
        return REGISTRY.values().stream()
            .filter(g -> g.rank() == rank)
            .collect(Collectors.toList());
    }

    public static List<GuType> getByCategory(GuType.GuCategory category) {
        return REGISTRY.values().stream()
            .filter(g -> g.category() == category)
            .collect(Collectors.toList());
    }

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, name);
    }

    public static void registerDefaults() {
        register(new GuType(id("hope_gu"), "希望蛊", 1, DaoPath.HUMAN, GuType.GuCategory.SPECIAL, 0, 0, ""));
        register(new GuType(id("moonlight_gu"), "月光蛊", 1, DaoPath.MOON, GuType.GuCategory.ATTACK, 10, 60, "reverend_insanity:moon_orchid_petal"));
        register(new GuType(id("liquor_worm"), "酒虫", 1, DaoPath.FOOD, GuType.GuCategory.SUPPORT, 5, 120, "minecraft:potion"));
        register(new GuType(id("bear_strength_gu"), "熊力蛊", 1, DaoPath.STRENGTH, GuType.GuCategory.ATTACK, 8, 90, "minecraft:honeycomb"));
        register(new GuType(id("jade_skin_gu"), "铜皮蛊", 1, DaoPath.METAL, GuType.GuCategory.DEFENSE, 8, 90, "minecraft:copper_ingot"));
        register(new GuType(id("white_boar_gu"), "黑豕蛊", 1, DaoPath.STRENGTH, GuType.GuCategory.ATTACK, 12, 90, "minecraft:porkchop"));
        register(new GuType(id("stealth_scales_gu"), "青丝蛊", 1, DaoPath.WOOD, GuType.GuCategory.DEFENSE, 15, 90, "minecraft:string"));
        register(new GuType(id("four_flavors_liquor_worm"), "四味酒虫", 2, DaoPath.FOOD, GuType.GuCategory.SUPPORT, 8, 150, "minecraft:honey_bottle"));
        register(new GuType(id("gold_light_worm"), "一气金光虫", 2, DaoPath.METAL, GuType.GuCategory.ATTACK, 20, 90, "minecraft:gold_ingot"));
        register(new GuType(id("iron_bone_gu"), "铁骨蛊", 2, DaoPath.STRENGTH, GuType.GuCategory.DEFENSE, 16, 120, "minecraft:iron_ingot"));
        register(new GuType(id("enslave_snake_gu"), "奴蛇蛊", 2, DaoPath.ENSLAVE, GuType.GuCategory.ENSLAVE, 25, 120, "minecraft:spider_eye"));
        register(new GuType(id("moonscar_gu"), "月痕蛊", 2, DaoPath.MOON, GuType.GuCategory.ATTACK, 18, 90, "reverend_insanity:moon_orchid_petal"));
        register(new GuType(id("silver_moon_gu"), "银月蛊", 3, DaoPath.MOON, GuType.GuCategory.ATTACK, 30, 120, "reverend_insanity:moon_orchid_petal"));
        register(new GuType(id("white_jade_gu"), "白玉蛊", 3, DaoPath.METAL, GuType.GuCategory.DEFENSE, 25, 150, "minecraft:emerald"));
        register(new GuType(id("heavens_eye_gu"), "天眼蛊", 3, DaoPath.WISDOM, GuType.GuCategory.DETECTION, 20, 150, "minecraft:ender_eye"));

        register(new GuType(id("flesh_bone_gu"), "肉骨蛊", 1, DaoPath.BLOOD, GuType.GuCategory.HEALING, 15, 90, "minecraft:rotten_flesh"));
        register(new GuType(id("displacement_gu"), "移形蛊", 2, DaoPath.SPACE, GuType.GuCategory.MOVEMENT, 20, 120, "minecraft:chorus_fruit"));

        register(new GuType(id("spring_autumn_cicada"), "春秋蝉", 6, DaoPath.TIME, GuType.GuCategory.SPECIAL, 5000, 0, ""));

        register(new GuType(id("blood_gu"), "血蛊", 1, DaoPath.BLOOD, GuType.GuCategory.ATTACK, 8, 90, "minecraft:rotten_flesh"));
        register(new GuType(id("self_heal_gu"), "自愈蛊", 1, DaoPath.BLOOD, GuType.GuCategory.HEALING, 10, 90, "minecraft:rotten_flesh"));
        register(new GuType(id("solidify_origin_gu"), "固元蛊", 1, DaoPath.BLOOD, GuType.GuCategory.SUPPORT, 12, 90, "minecraft:rotten_flesh"));
        register(new GuType(id("blood_wing_gu"), "血翼蛊", 2, DaoPath.BLOOD, GuType.GuCategory.MOVEMENT, 20, 120, "minecraft:spider_eye"));
        register(new GuType(id("poison_bee_gu"), "毒蜂蛊", 1, DaoPath.POISON, GuType.GuCategory.ATTACK, 10, 90, "minecraft:spider_eye"));
        register(new GuType(id("gold_silkworm_gu"), "金蚕蛊", 2, DaoPath.POISON, GuType.GuCategory.ENSLAVE, 25, 120, "minecraft:gold_ingot"));

        register(new GuType(id("savage_bull_gu"), "蛮力天牛蛊", 1, DaoPath.STRENGTH, GuType.GuCategory.ATTACK, 5, 90, "minecraft:honeycomb"));
        register(new GuType(id("taishan_gu"), "泰山压顶蛊", 1, DaoPath.STRENGTH, GuType.GuCategory.ATTACK, 12, 90, "minecraft:cobblestone"));
        register(new GuType(id("giant_strength_gu"), "巨力蛊", 2, DaoPath.STRENGTH, GuType.GuCategory.SUPPORT, 18, 120, "minecraft:raw_iron"));
        register(new GuType(id("cold_ice_gu"), "寒冰蛊", 1, DaoPath.ICE, GuType.GuCategory.ATTACK, 8, 90, "minecraft:snowball"));
        register(new GuType(id("frost_armor_gu"), "霜甲蛊", 1, DaoPath.ICE, GuType.GuCategory.DEFENSE, 12, 90, "minecraft:packed_ice"));
        register(new GuType(id("ice_seal_gu"), "冰封蛊", 2, DaoPath.ICE, GuType.GuCategory.ENSLAVE, 25, 120, "minecraft:blue_ice"));

        register(new GuType(id("fire_seed_gu"), "火种蛊", 1, DaoPath.FIRE, GuType.GuCategory.ATTACK, 8, 90, "minecraft:blaze_powder"));
        register(new GuType(id("flame_armor_gu"), "炎铠蛊", 1, DaoPath.FIRE, GuType.GuCategory.DEFENSE, 12, 90, "minecraft:magma_cream"));
        register(new GuType(id("blazing_flame_gu"), "烈焰蛊", 2, DaoPath.FIRE, GuType.GuCategory.ATTACK, 20, 120, "minecraft:blaze_rod"));
        register(new GuType(id("earth_wall_gu"), "土壁蛊", 1, DaoPath.EARTH, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:clay_ball"));
        register(new GuType(id("earth_split_gu"), "地裂蛊", 1, DaoPath.EARTH, GuType.GuCategory.ATTACK, 10, 90, "minecraft:gravel"));
        register(new GuType(id("petrify_gu"), "岩化蛊", 2, DaoPath.EARTH, GuType.GuCategory.ENSLAVE, 20, 120, "minecraft:deepslate"));

        register(new GuType(id("breeze_gu"), "清风蛊", 1, DaoPath.WIND, GuType.GuCategory.MOVEMENT, 8, 90, "minecraft:feather"));
        register(new GuType(id("wind_blade_gu"), "风刃蛊", 1, DaoPath.WIND, GuType.GuCategory.ATTACK, 10, 90, "minecraft:phantom_membrane"));
        register(new GuType(id("gale_gu"), "疾风蛊", 2, DaoPath.WIND, GuType.GuCategory.MOVEMENT, 18, 120, "minecraft:wind_charge"));
        register(new GuType(id("lightning_gu"), "雷电蛊", 1, DaoPath.LIGHTNING, GuType.GuCategory.ATTACK, 12, 90, "minecraft:glowstone_dust"));
        register(new GuType(id("thunder_shield_gu"), "雷盾蛊", 1, DaoPath.LIGHTNING, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:prismarine_shard"));
        register(new GuType(id("thunderstorm_gu"), "雷暴蛊", 2, DaoPath.LIGHTNING, GuType.GuCategory.ATTACK, 25, 120, "minecraft:heart_of_the_sea"));

        register(new GuType(id("tide_gu"), "潮涌蛊", 1, DaoPath.WATER, GuType.GuCategory.ATTACK, 10, 90, "minecraft:kelp"));
        register(new GuType(id("water_shield_gu"), "水盾蛊", 1, DaoPath.WATER, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:sea_pickle"));
        register(new GuType(id("torrent_gu"), "激流蛊", 2, DaoPath.WATER, GuType.GuCategory.ATTACK, 22, 120, "minecraft:nautilus_shell"));
        register(new GuType(id("soul_search_gu"), "搜魂蛊", 1, DaoPath.SOUL, GuType.GuCategory.ATTACK, 12, 90, "minecraft:echo_shard"));
        register(new GuType(id("soul_shield_gu"), "守魂蛊", 1, DaoPath.SOUL, GuType.GuCategory.DEFENSE, 8, 90, "minecraft:amethyst_shard"));
        register(new GuType(id("soul_crush_gu"), "碎魂蛊", 2, DaoPath.SOUL, GuType.GuCategory.ATTACK, 25, 120, "minecraft:sculk_catalyst"));

        register(new GuType(id("light_beam_gu"), "光束蛊", 1, DaoPath.LIGHT, GuType.GuCategory.ATTACK, 8, 90, "minecraft:glowstone"));
        register(new GuType(id("radiance_gu"), "耀光蛊", 1, DaoPath.LIGHT, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:glow_berries"));
        register(new GuType(id("blazing_light_gu"), "灼光蛊", 2, DaoPath.LIGHT, GuType.GuCategory.ATTACK, 20, 120, "minecraft:sea_lantern"));
        register(new GuType(id("dark_bolt_gu"), "暗箭蛊", 1, DaoPath.DARK, GuType.GuCategory.ATTACK, 10, 90, "minecraft:ink_sac"));
        register(new GuType(id("shadow_cloak_gu"), "暗影斗篷蛊", 1, DaoPath.DARK, GuType.GuCategory.DEFENSE, 8, 90, "minecraft:coal"));
        register(new GuType(id("abyss_devour_gu"), "深渊吞噬蛊", 2, DaoPath.DARK, GuType.GuCategory.ATTACK, 22, 120, "minecraft:obsidian"));

        register(new GuType(id("dream_gu"), "入梦蛊", 1, DaoPath.DREAM, GuType.GuCategory.ATTACK, 10, 90, "minecraft:chorus_fruit"));
        register(new GuType(id("lucid_dream_gu"), "清明梦蛊", 1, DaoPath.DREAM, GuType.GuCategory.DEFENSE, 8, 90, "minecraft:spider_eye"));
        register(new GuType(id("nightmare_gu"), "噩梦蛊", 2, DaoPath.DREAM, GuType.GuCategory.ATTACK, 22, 120, "minecraft:fermented_spider_eye"));
        register(new GuType(id("phantom_gu"), "幻影蛊", 1, DaoPath.ILLUSION, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:ender_pearl"));
        register(new GuType(id("mirage_gu"), "海市蛊", 1, DaoPath.ILLUSION, GuType.GuCategory.ATTACK, 8, 90, "minecraft:prismarine_shard"));
        register(new GuType(id("grand_illusion_gu"), "大幻蛊", 2, DaoPath.ILLUSION, GuType.GuCategory.ATTACK, 20, 120, "minecraft:ender_eye"));

        register(new GuType(id("flying_sword_gu"), "飞剑蛊", 1, DaoPath.SWORD, GuType.GuCategory.ATTACK, 10, 90, "minecraft:iron_sword"));
        register(new GuType(id("sword_shield_gu"), "剑甲蛊", 1, DaoPath.SWORD, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:iron_nugget"));
        register(new GuType(id("myriad_sword_gu"), "万剑蛊", 2, DaoPath.SWORD, GuType.GuCategory.ATTACK, 22, 120, "minecraft:diamond_sword"));
        register(new GuType(id("moon_slash_gu"), "斩月蛊", 1, DaoPath.BLADE, GuType.GuCategory.ATTACK, 8, 90, "minecraft:iron_axe"));
        register(new GuType(id("blade_armor_gu"), "刀铠蛊", 1, DaoPath.BLADE, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:leather"));
        register(new GuType(id("heaven_blade_gu"), "天刀蛊", 2, DaoPath.BLADE, GuType.GuCategory.ATTACK, 20, 120, "minecraft:netherite_scrap"));

        register(new GuType(id("starlight_gu"), "星光蛊", 1, DaoPath.STAR, GuType.GuCategory.ATTACK, 10, 90, "minecraft:amethyst_shard"));
        register(new GuType(id("star_shield_gu"), "星盾蛊", 1, DaoPath.STAR, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:prismarine_shard"));
        register(new GuType(id("star_fall_gu"), "星陨蛊", 2, DaoPath.STAR, GuType.GuCategory.ATTACK, 22, 120, "minecraft:nether_star"));
        register(new GuType(id("lucky_gu"), "幸运蛊", 1, DaoPath.LUCK, GuType.GuCategory.SUPPORT, 8, 90, "minecraft:rabbit_foot"));
        register(new GuType(id("misfortune_ward_gu"), "避祸蛊", 1, DaoPath.LUCK, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:totem_of_undying"));
        register(new GuType(id("heavens_secret_gu"), "天机蛊", 2, DaoPath.LUCK, GuType.GuCategory.ATTACK, 25, 120, "minecraft:ender_eye"));

        register(new GuType(id("kill_intent_gu"), "杀意蛊", 1, DaoPath.KILL, GuType.GuCategory.ATTACK, 10, 90, "minecraft:flint"));
        register(new GuType(id("killing_chance_gu"), "杀机蛊", 1, DaoPath.KILL, GuType.GuCategory.SUPPORT, 8, 90, "minecraft:bone"));
        register(new GuType(id("death_strike_gu"), "必杀蛊", 2, DaoPath.KILL, GuType.GuCategory.ATTACK, 22, 120, "minecraft:wither_skeleton_skull"));
        register(new GuType(id("shrink_ground_gu"), "缩地蛊", 1, DaoPath.TRANSFORMATION, GuType.GuCategory.MOVEMENT, 10, 90, "minecraft:chorus_fruit"));
        register(new GuType(id("morph_gu"), "变形蛊", 1, DaoPath.TRANSFORMATION, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:rabbit_hide"));
        register(new GuType(id("heaven_change_gu"), "天变蛊", 2, DaoPath.TRANSFORMATION, GuType.GuCategory.ATTACK, 20, 120, "minecraft:dragon_breath"));

        register(new GuType(id("formation_soldier_gu"), "阵兵蛊", 1, DaoPath.SOLDIER, GuType.GuCategory.ATTACK, 10, 90, "minecraft:arrow"));
        register(new GuType(id("golden_armor_gu"), "金甲蛊", 1, DaoPath.SOLDIER, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:iron_ingot"));
        register(new GuType(id("thousand_army_gu"), "万军蛊", 2, DaoPath.SOLDIER, GuType.GuCategory.ATTACK, 22, 120, "minecraft:crossbow"));
        register(new GuType(id("sound_wave_gu"), "音波蛊", 1, DaoPath.SOUND, GuType.GuCategory.ATTACK, 8, 90, "minecraft:note_block"));
        register(new GuType(id("silence_gu"), "静音蛊", 1, DaoPath.SOUND, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:white_wool"));
        register(new GuType(id("heavenly_sound_gu"), "天籁蛊", 2, DaoPath.SOUND, GuType.GuCategory.ATTACK, 20, 120, "minecraft:bell"));

        register(new GuType(id("bone_spear_gu"), "骨枪蛊", 1, DaoPath.BONE, GuType.GuCategory.ATTACK, 8, 90, "minecraft:bone"));
        register(new GuType(id("bone_armor_gu"), "骨甲蛊", 1, DaoPath.BONE, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:bone_meal"));
        register(new GuType(id("white_bone_gu"), "白骨蛊", 2, DaoPath.BONE, GuType.GuCategory.ATTACK, 22, 120, "minecraft:bone_block"));
        register(new GuType(id("cloud_ride_gu"), "腾云蛊", 1, DaoPath.FLIGHT, GuType.GuCategory.MOVEMENT, 10, 90, "minecraft:feather"));
        register(new GuType(id("flight_wing_gu"), "飞翼蛊", 1, DaoPath.FLIGHT, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:phantom_membrane"));
        register(new GuType(id("sky_eagle_gu"), "天鹰蛊", 2, DaoPath.FLIGHT, GuType.GuCategory.ATTACK, 20, 120, "minecraft:elytra"));

        register(new GuType(id("true_qi_gu"), "真气蛊", 1, DaoPath.QI, GuType.GuCategory.HEALING, 8, 90, "minecraft:glow_berries"));
        register(new GuType(id("qi_shield_gu"), "气盾蛊", 1, DaoPath.QI, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:turtle_scute"));
        register(new GuType(id("profound_qi_gu"), "玄气蛊", 2, DaoPath.QI, GuType.GuCategory.ATTACK, 22, 120, "minecraft:breeze_rod"));
        register(new GuType(id("yin_yang_gu"), "阴阳蛊", 1, DaoPath.YIN_YANG, GuType.GuCategory.ATTACK, 10, 90, "minecraft:ender_pearl"));
        register(new GuType(id("tai_chi_gu"), "太极蛊", 1, DaoPath.YIN_YANG, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:amethyst_shard"));
        register(new GuType(id("primordial_gu"), "混元蛊", 2, DaoPath.YIN_YANG, GuType.GuCategory.ATTACK, 22, 120, "minecraft:nether_star"));

        register(new GuType(id("warp_gu"), "折空蛊", 1, DaoPath.SPACE, GuType.GuCategory.ATTACK, 8, 90, "minecraft:chorus_fruit"));
        register(new GuType(id("space_barrier_gu"), "空间壁障蛊", 1, DaoPath.SPACE, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:ender_pearl"));

        register(new GuType(id("time_decel_gu"), "时缓蛊", 1, DaoPath.TIME, GuType.GuCategory.ATTACK, 10, 90, "minecraft:clock"));
        register(new GuType(id("time_shield_gu"), "时盾蛊", 1, DaoPath.TIME, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:echo_shard"));
        register(new GuType(id("time_reversal_gu"), "逆时蛊", 2, DaoPath.TIME, GuType.GuCategory.HEALING, 22, 120, "minecraft:recovery_compass"));

        register(new GuType(id("charm_gu"), "魅惑蛊", 1, DaoPath.CHARM, GuType.GuCategory.ATTACK, 10, 90, "minecraft:glow_berries"));
        register(new GuType(id("bewitch_gu"), "妖媚蛊", 1, DaoPath.CHARM, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:pink_petals"));
        register(new GuType(id("soul_charm_gu"), "摄魂蛊", 2, DaoPath.CHARM, GuType.GuCategory.ENSLAVE, 22, 120, "minecraft:sculk_catalyst"));

        register(new GuType(id("thought_gu"), "思维蛊", 1, DaoPath.WISDOM, GuType.GuCategory.ATTACK, 8, 90, "minecraft:book"));
        register(new GuType(id("mind_guard_gu"), "神智蛊", 1, DaoPath.WISDOM, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:amethyst_shard"));

        register(new GuType(id("void_bolt_gu"), "虚无蛊", 1, DaoPath.VOID, GuType.GuCategory.ATTACK, 10, 90, "minecraft:ender_pearl"));
        register(new GuType(id("void_cloak_gu"), "虚空隐匿蛊", 1, DaoPath.VOID, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:chorus_flower"));
        register(new GuType(id("void_annihilation_gu"), "虚空吞灭蛊", 2, DaoPath.VOID, GuType.GuCategory.ATTACK, 22, 120, "minecraft:end_crystal"));

        register(new GuType(id("seal_gu"), "禁制蛊", 1, DaoPath.RESTRICTION, GuType.GuCategory.ATTACK, 10, 90, "minecraft:chain"));
        register(new GuType(id("restriction_gu"), "禁锢蛊", 1, DaoPath.RESTRICTION, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:iron_bars"));
        register(new GuType(id("heaven_seal_gu"), "天禁蛊", 2, DaoPath.RESTRICTION, GuType.GuCategory.ENSLAVE, 22, 120, "minecraft:lodestone"));

        register(new GuType(id("heaven_will_gu"), "天意蛊", 1, DaoPath.HEAVEN, GuType.GuCategory.ATTACK, 10, 90, "minecraft:nether_star"));
        register(new GuType(id("heaven_shield_gu"), "天盾蛊", 1, DaoPath.HEAVEN, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:totem_of_undying"));
        register(new GuType(id("heaven_punishment_gu"), "天罚蛊", 2, DaoPath.HEAVEN, GuType.GuCategory.ATTACK, 22, 120, "minecraft:end_crystal"));

        register(new GuType(id("rule_gu"), "规则蛊", 1, DaoPath.RULE, GuType.GuCategory.ATTACK, 10, 90, "minecraft:chain"));
        register(new GuType(id("order_gu"), "秩序蛊", 1, DaoPath.RULE, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:iron_bars"));
        register(new GuType(id("supreme_law_gu"), "至律蛊", 2, DaoPath.RULE, GuType.GuCategory.ATTACK, 22, 120, "minecraft:lodestone"));

        register(new GuType(id("shadow_dart_gu"), "影针蛊", 1, DaoPath.SHADOW, GuType.GuCategory.ATTACK, 8, 90, "minecraft:ink_sac"));
        register(new GuType(id("shadow_veil_gu"), "影幕蛊", 1, DaoPath.SHADOW, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:coal"));
        register(new GuType(id("shadow_devour_gu"), "噬影蛊", 2, DaoPath.SHADOW, GuType.GuCategory.ATTACK, 22, 120, "minecraft:obsidian"));

        register(new GuType(id("mist_gu"), "雾蛊", 1, DaoPath.CLOUD, GuType.GuCategory.ATTACK, 8, 90, "minecraft:white_wool"));
        register(new GuType(id("cloud_armor_gu"), "云甲蛊", 1, DaoPath.CLOUD, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:cobweb"));
        register(new GuType(id("cloud_storm_gu"), "云暴蛊", 2, DaoPath.CLOUD, GuType.GuCategory.ATTACK, 22, 120, "minecraft:lightning_rod"));

        register(new GuType(id("trap_formation_gu"), "困阵蛊", 1, DaoPath.FORMATION, GuType.GuCategory.ATTACK, 10, 90, "minecraft:redstone"));
        register(new GuType(id("formation_shield_gu"), "阵盾蛊", 1, DaoPath.FORMATION, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:lapis_lazuli"));
        register(new GuType(id("grand_formation_gu"), "天地大阵蛊", 2, DaoPath.FORMATION, GuType.GuCategory.ATTACK, 22, 120, "minecraft:enchanted_book"));

        register(new GuType(id("refine_fire_gu"), "炼火蛊", 1, DaoPath.REFINEMENT, GuType.GuCategory.ATTACK, 10, 90, "minecraft:blaze_powder"));
        register(new GuType(id("refine_body_gu"), "炼体蛊", 1, DaoPath.REFINEMENT, GuType.GuCategory.HEALING, 10, 90, "minecraft:raw_iron"));
        register(new GuType(id("heaven_refine_gu"), "天炼蛊", 2, DaoPath.REFINEMENT, GuType.GuCategory.ATTACK, 22, 120, "minecraft:blaze_rod"));

        register(new GuType(id("pill_poison_gu"), "丹毒蛊", 1, DaoPath.PILL, GuType.GuCategory.ATTACK, 8, 90, "minecraft:spider_eye"));
        register(new GuType(id("pill_shield_gu"), "丹甲蛊", 1, DaoPath.PILL, GuType.GuCategory.HEALING, 10, 90, "minecraft:glistering_melon_slice"));
        register(new GuType(id("immortal_pill_gu"), "仙丹蛊", 2, DaoPath.PILL, GuType.GuCategory.ATTACK, 22, 120, "minecraft:golden_apple"));

        register(new GuType(id("paint_brush_gu"), "画笔蛊", 1, DaoPath.PAINT, GuType.GuCategory.ATTACK, 8, 90, "minecraft:pink_dye"));
        register(new GuType(id("paint_shield_gu"), "画盾蛊", 1, DaoPath.PAINT, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:painting"));
        register(new GuType(id("myriad_paint_gu"), "万彩蛊", 2, DaoPath.PAINT, GuType.GuCategory.ATTACK, 22, 120, "minecraft:glow_ink_sac"));

        register(new GuType(id("steal_qi_gu"), "窃气蛊", 1, DaoPath.STEAL, GuType.GuCategory.ATTACK, 8, 90, "minecraft:fermented_spider_eye"));
        register(new GuType(id("steal_hide_gu"), "窃影蛊", 1, DaoPath.STEAL, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:leather"));
        register(new GuType(id("heaven_steal_gu"), "天窃蛊", 2, DaoPath.STEAL, GuType.GuCategory.ATTACK, 22, 120, "minecraft:ender_eye"));

        register(new GuType(id("info_dart_gu"), "信蛊", 1, DaoPath.INFORMATION, GuType.GuCategory.ATTACK, 8, 90, "minecraft:paper"));
        register(new GuType(id("info_net_gu"), "信息网蛊", 1, DaoPath.INFORMATION, GuType.GuCategory.SUPPORT, 10, 90, "minecraft:book"));
        register(new GuType(id("heaven_info_gu"), "天机信蛊", 2, DaoPath.INFORMATION, GuType.GuCategory.ATTACK, 22, 120, "minecraft:writable_book"));

        register(new GuType(id("human_heart_gu"), "人心蛊", 1, DaoPath.HUMAN, GuType.GuCategory.ATTACK, 10, 90, "minecraft:golden_apple"));
        register(new GuType(id("human_bond_gu"), "人情蛊", 1, DaoPath.HUMAN, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:bread"));
        register(new GuType(id("human_will_gu"), "人意蛊", 2, DaoPath.HUMAN, GuType.GuCategory.ATTACK, 22, 120, "minecraft:enchanted_golden_apple"));

        register(new GuType(id("enslave_worm_gu"), "奴虫蛊", 1, DaoPath.ENSLAVE, GuType.GuCategory.ATTACK, 8, 90, "minecraft:spider_eye"));
        register(new GuType(id("enslave_shield_gu"), "奴盾蛊", 1, DaoPath.ENSLAVE, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:lead"));

        register(new GuType(id("feast_gu"), "宴蛊", 1, DaoPath.FOOD, GuType.GuCategory.ATTACK, 8, 90, "minecraft:cooked_beef"));

        register(new GuType(id("snake_tongue_gu"), "蛇信蛊", 2, DaoPath.WISDOM, GuType.GuCategory.DETECTION, 20, 120, "minecraft:string"));
        register(new GuType(id("earth_listener_gu"), "地听肉耳草", 2, DaoPath.WISDOM, GuType.GuCategory.DETECTION, 25, 120, "minecraft:glistering_melon_slice"));
        register(new GuType(id("keen_ear_gu"), "顺风耳蛊", 2, DaoPath.WISDOM, GuType.GuCategory.SUPPORT, 15, 120, "minecraft:brown_mushroom"));
        register(new GuType(id("hidden_scale_gu"), "隐鳞蛊", 2, DaoPath.SHADOW, GuType.GuCategory.DEFENSE, 20, 120, "minecraft:ink_sac"));
        register(new GuType(id("true_sight_gu"), "真视蛊", 3, DaoPath.LIGHT, GuType.GuCategory.DETECTION, 35, 150, "minecraft:glow_berries"));
        register(new GuType(id("electric_eye_gu"), "电眼蛊", 1, DaoPath.LIGHTNING, GuType.GuCategory.DETECTION, 10, 90, "minecraft:copper_ingot"));

        register(new GuType(id("bronze_sarira_gu"), "铜舍利蛊", 1, DaoPath.REFINEMENT, GuType.GuCategory.SUPPORT, 30, 0, "minecraft:clay_ball"));
        register(new GuType(id("iron_sarira_gu"), "铁舍利蛊", 2, DaoPath.REFINEMENT, GuType.GuCategory.SUPPORT, 60, 0, "minecraft:iron_nugget"));
        register(new GuType(id("silver_sarira_gu"), "银舍利蛊", 3, DaoPath.REFINEMENT, GuType.GuCategory.SUPPORT, 100, 0, "minecraft:quartz"));
        register(new GuType(id("gold_sarira_gu"), "金舍利蛊", 4, DaoPath.REFINEMENT, GuType.GuCategory.SUPPORT, 150, 0, "minecraft:gold_nugget"));
        register(new GuType(id("black_boar_gu"), "黑豕积累蛊", 1, DaoPath.STRENGTH, GuType.GuCategory.SUPPORT, 20, 90, "minecraft:porkchop"));
        register(new GuType(id("brown_bear_gu"), "棕熊本力蛊", 2, DaoPath.STRENGTH, GuType.GuCategory.SUPPORT, 40, 120, "minecraft:cooked_beef"));
        register(new GuType(id("flower_boar_gu"), "花豕蛊", 1, DaoPath.STRENGTH, GuType.GuCategory.ATTACK, 5, 90, "minecraft:pink_petals"));
        register(new GuType(id("yellow_camel_beetle_gu"), "黄骆天牛蛊", 1, DaoPath.STRENGTH, GuType.GuCategory.DEFENSE, 5, 90, "minecraft:honeycomb"));

        register(new GuType(id("stone_skin_gu"), "石皮蛊", 1, DaoPath.METAL, GuType.GuCategory.DEFENSE, 6, 90, "minecraft:cobblestone"));
        register(new GuType(id("iron_skin_gu"), "铁皮蛊", 1, DaoPath.METAL, GuType.GuCategory.DEFENSE, 10, 90, "minecraft:iron_ingot"));
        register(new GuType(id("beast_skin_gu"), "兽皮蛊", 1, DaoPath.STRENGTH, GuType.GuCategory.DEFENSE, 4, 90, "minecraft:leather"));
        register(new GuType(id("black_bristle_gu"), "黑鬃蛊", 2, DaoPath.STRENGTH, GuType.GuCategory.DEFENSE, 15, 120, "minecraft:black_wool"));
        register(new GuType(id("steel_bristle_gu"), "钢鬃蛊", 3, DaoPath.STRENGTH, GuType.GuCategory.DEFENSE, 25, 150, "minecraft:iron_block"));
        register(new GuType(id("heaven_canopy_gu"), "天蓬蛊", 3, DaoPath.METAL, GuType.GuCategory.DEFENSE, 40, 150, "minecraft:golden_apple"));
        register(new GuType(id("vitality_grass_gu"), "生机草蛊", 2, DaoPath.WOOD, GuType.GuCategory.HEALING, 20, 120, "minecraft:fern"));
        register(new GuType(id("vitality_leaf_gu"), "生机叶蛊", 1, DaoPath.WOOD, GuType.GuCategory.HEALING, 3, 90, "minecraft:oak_leaves"));
        register(new GuType(id("water_spider_gu"), "水蛛蛊", 1, DaoPath.WATER, GuType.GuCategory.DEFENSE, 8, 90, "minecraft:prismarine_shard"));

        register(new GuType(id("signal_gu"), "信号蛊", 1, DaoPath.INFORMATION, GuType.GuCategory.SUPPORT, 5, 0, "minecraft:firework_rocket"));
        register(new GuType(id("flash_gu"), "闪光蛊", 1, DaoPath.LIGHT, GuType.GuCategory.ATTACK, 8, 0, "minecraft:glowstone_dust"));
        register(new GuType(id("shadow_follower_gu"), "影随蛊", 2, DaoPath.SHADOW, GuType.GuCategory.DEFENSE, 20, 120, "minecraft:ink_sac"));
        register(new GuType(id("dragon_cricket_gu"), "龙蟋蛊", 1, DaoPath.STRENGTH, GuType.GuCategory.MOVEMENT, 8, 90, "minecraft:slime_ball"));
        register(new GuType(id("quiet_step_gu"), "静步蛊", 1, DaoPath.SHADOW, GuType.GuCategory.MOVEMENT, 6, 90, "minecraft:rabbit_foot"));
        register(new GuType(id("scent_lock_gu"), "气味锁定蛊", 1, DaoPath.WISDOM, GuType.GuCategory.DETECTION, 5, 90, "minecraft:brown_mushroom"));
        register(new GuType(id("love_separation_gu"), "情离蛊", 2, DaoPath.CHARM, GuType.GuCategory.ATTACK, 30, 120, "minecraft:rose_bush"));
    }
}
