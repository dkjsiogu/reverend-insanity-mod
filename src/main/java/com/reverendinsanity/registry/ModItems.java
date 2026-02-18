package com.reverendinsanity.registry;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.item.BreakthroughStoneItem;
import com.reverendinsanity.item.GuCodexItem;
import com.reverendinsanity.item.GuItem;
import com.reverendinsanity.item.HeavenFragmentItem;
import com.reverendinsanity.item.HopeGuItem;
import com.reverendinsanity.item.PrimevalStoneItem;
import com.reverendinsanity.item.SpringAutumnCicadaItem;
import com.reverendinsanity.item.VenerableSpawnEggItem;
import com.reverendinsanity.entity.VenerableType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// 物品注册
public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ReverendInsanity.MODID);

    public static final DeferredItem<Item> HOPE_GU = ITEMS.register("hope_gu",
        () -> new HopeGuItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> MOONLIGHT_GU = ITEMS.register("moonlight_gu",
        () -> new GuItem(GuRegistry.id("moonlight_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> LIQUOR_WORM = ITEMS.register("liquor_worm",
        () -> new GuItem(GuRegistry.id("liquor_worm"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> BEAR_STRENGTH_GU = ITEMS.register("bear_strength_gu",
        () -> new GuItem(GuRegistry.id("bear_strength_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> JADE_SKIN_GU = ITEMS.register("jade_skin_gu",
        () -> new GuItem(GuRegistry.id("jade_skin_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> WHITE_BOAR_GU = ITEMS.register("white_boar_gu",
        () -> new GuItem(GuRegistry.id("white_boar_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> STEALTH_SCALES_GU = ITEMS.register("stealth_scales_gu",
        () -> new GuItem(GuRegistry.id("stealth_scales_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> MOON_ORCHID_PETAL = ITEMS.register("moon_orchid_petal",
        () -> new Item(new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> FOUR_FLAVORS_LIQUOR_WORM = ITEMS.register("four_flavors_liquor_worm",
        () -> new GuItem(GuRegistry.id("four_flavors_liquor_worm"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> GOLD_LIGHT_WORM = ITEMS.register("gold_light_worm",
        () -> new GuItem(GuRegistry.id("gold_light_worm"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> IRON_BONE_GU = ITEMS.register("iron_bone_gu",
        () -> new GuItem(GuRegistry.id("iron_bone_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> ENSLAVE_SNAKE_GU = ITEMS.register("enslave_snake_gu",
        () -> new GuItem(GuRegistry.id("enslave_snake_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> MOONSCAR_GU = ITEMS.register("moonscar_gu",
        () -> new GuItem(GuRegistry.id("moonscar_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> SILVER_MOON_GU = ITEMS.register("silver_moon_gu",
        () -> new GuItem(GuRegistry.id("silver_moon_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> WHITE_JADE_GU = ITEMS.register("white_jade_gu",
        () -> new GuItem(GuRegistry.id("white_jade_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> HEAVENS_EYE_GU = ITEMS.register("heavens_eye_gu",
        () -> new GuItem(GuRegistry.id("heavens_eye_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> SPRING_AUTUMN_CICADA = ITEMS.register("spring_autumn_cicada",
        () -> new SpringAutumnCicadaItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> FLESH_BONE_GU = ITEMS.register("flesh_bone_gu",
        () -> new GuItem(GuRegistry.id("flesh_bone_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> DISPLACEMENT_GU = ITEMS.register("displacement_gu",
        () -> new GuItem(GuRegistry.id("displacement_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> LORE_SCROLL = ITEMS.register("lore_scroll",
        () -> new com.reverendinsanity.item.LoreScrollItem(new Item.Properties().stacksTo(16)));

    public static final DeferredItem<Item> BREAKTHROUGH_STONE = ITEMS.register("breakthrough_stone",
        () -> new BreakthroughStoneItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> APERTURE_EXIT_PORTAL = ITEMS.register("aperture_exit_portal",
        () -> new BlockItem(ModBlocks.APERTURE_EXIT_PORTAL.get(), new Item.Properties()));

    public static final DeferredItem<Item> PRIMEVAL_STONE = ITEMS.register("primeval_stone",
        () -> new PrimevalStoneItem(new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> REFINEMENT_CAULDRON = ITEMS.register("refinement_cauldron",
        () -> new BlockItem(ModBlocks.REFINEMENT_CAULDRON.get(), new Item.Properties()));

    public static final DeferredItem<Item> PRIMEVAL_STONE_ORE = ITEMS.register("primeval_stone_ore",
        () -> new BlockItem(ModBlocks.PRIMEVAL_STONE_ORE.get(), new Item.Properties()));

    public static final DeferredItem<Item> DEEPSLATE_PRIMEVAL_STONE_ORE = ITEMS.register("deepslate_primeval_stone_ore",
        () -> new BlockItem(ModBlocks.DEEPSLATE_PRIMEVAL_STONE_ORE.get(), new Item.Properties()));

    public static final DeferredItem<Item> MOON_ORCHID_SEEDS = ITEMS.register("moon_orchid_seeds",
        () -> new ItemNameBlockItem(ModBlocks.MOON_ORCHID.get(), new Item.Properties()));

    public static final DeferredItem<Item> SPIRIT_SPRING = ITEMS.register("spirit_spring",
        () -> new BlockItem(ModBlocks.SPIRIT_SPRING.get(), new Item.Properties()));

    public static final DeferredItem<Item> BLOOD_GU = ITEMS.register("blood_gu",
        () -> new GuItem(GuRegistry.id("blood_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> SELF_HEAL_GU = ITEMS.register("self_heal_gu",
        () -> new GuItem(GuRegistry.id("self_heal_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> SOLIDIFY_ORIGIN_GU = ITEMS.register("solidify_origin_gu",
        () -> new GuItem(GuRegistry.id("solidify_origin_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> BLOOD_WING_GU = ITEMS.register("blood_wing_gu",
        () -> new GuItem(GuRegistry.id("blood_wing_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> POISON_BEE_GU = ITEMS.register("poison_bee_gu",
        () -> new GuItem(GuRegistry.id("poison_bee_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> GOLD_SILKWORM_GU = ITEMS.register("gold_silkworm_gu",
        () -> new GuItem(GuRegistry.id("gold_silkworm_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> SAVAGE_BULL_GU = ITEMS.register("savage_bull_gu",
        () -> new GuItem(GuRegistry.id("savage_bull_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> TAISHAN_GU = ITEMS.register("taishan_gu",
        () -> new GuItem(GuRegistry.id("taishan_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> GIANT_STRENGTH_GU = ITEMS.register("giant_strength_gu",
        () -> new GuItem(GuRegistry.id("giant_strength_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> COLD_ICE_GU = ITEMS.register("cold_ice_gu",
        () -> new GuItem(GuRegistry.id("cold_ice_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> FROST_ARMOR_GU = ITEMS.register("frost_armor_gu",
        () -> new GuItem(GuRegistry.id("frost_armor_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> ICE_SEAL_GU = ITEMS.register("ice_seal_gu",
        () -> new GuItem(GuRegistry.id("ice_seal_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> FIRE_SEED_GU = ITEMS.register("fire_seed_gu",
        () -> new GuItem(GuRegistry.id("fire_seed_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> FLAME_ARMOR_GU = ITEMS.register("flame_armor_gu",
        () -> new GuItem(GuRegistry.id("flame_armor_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> BLAZING_FLAME_GU = ITEMS.register("blazing_flame_gu",
        () -> new GuItem(GuRegistry.id("blazing_flame_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> EARTH_WALL_GU = ITEMS.register("earth_wall_gu",
        () -> new GuItem(GuRegistry.id("earth_wall_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> EARTH_SPLIT_GU = ITEMS.register("earth_split_gu",
        () -> new GuItem(GuRegistry.id("earth_split_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> PETRIFY_GU = ITEMS.register("petrify_gu",
        () -> new GuItem(GuRegistry.id("petrify_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> BREEZE_GU = ITEMS.register("breeze_gu",
        () -> new GuItem(GuRegistry.id("breeze_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> WIND_BLADE_GU = ITEMS.register("wind_blade_gu",
        () -> new GuItem(GuRegistry.id("wind_blade_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> GALE_GU = ITEMS.register("gale_gu",
        () -> new GuItem(GuRegistry.id("gale_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> LIGHTNING_GU = ITEMS.register("lightning_gu",
        () -> new GuItem(GuRegistry.id("lightning_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> THUNDER_SHIELD_GU = ITEMS.register("thunder_shield_gu",
        () -> new GuItem(GuRegistry.id("thunder_shield_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> THUNDERSTORM_GU = ITEMS.register("thunderstorm_gu",
        () -> new GuItem(GuRegistry.id("thunderstorm_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> TIDE_GU = ITEMS.register("tide_gu",
        () -> new GuItem(GuRegistry.id("tide_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> WATER_SHIELD_GU = ITEMS.register("water_shield_gu",
        () -> new GuItem(GuRegistry.id("water_shield_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> TORRENT_GU = ITEMS.register("torrent_gu",
        () -> new GuItem(GuRegistry.id("torrent_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> SOUL_SEARCH_GU = ITEMS.register("soul_search_gu",
        () -> new GuItem(GuRegistry.id("soul_search_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> SOUL_SHIELD_GU = ITEMS.register("soul_shield_gu",
        () -> new GuItem(GuRegistry.id("soul_shield_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> SOUL_CRUSH_GU = ITEMS.register("soul_crush_gu",
        () -> new GuItem(GuRegistry.id("soul_crush_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> LIGHT_BEAM_GU = ITEMS.register("light_beam_gu",
        () -> new GuItem(GuRegistry.id("light_beam_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> RADIANCE_GU = ITEMS.register("radiance_gu",
        () -> new GuItem(GuRegistry.id("radiance_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> BLAZING_LIGHT_GU = ITEMS.register("blazing_light_gu",
        () -> new GuItem(GuRegistry.id("blazing_light_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> DARK_BOLT_GU = ITEMS.register("dark_bolt_gu",
        () -> new GuItem(GuRegistry.id("dark_bolt_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> SHADOW_CLOAK_GU = ITEMS.register("shadow_cloak_gu",
        () -> new GuItem(GuRegistry.id("shadow_cloak_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> ABYSS_DEVOUR_GU = ITEMS.register("abyss_devour_gu",
        () -> new GuItem(GuRegistry.id("abyss_devour_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> DREAM_GU = ITEMS.register("dream_gu",
        () -> new GuItem(GuRegistry.id("dream_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> LUCID_DREAM_GU = ITEMS.register("lucid_dream_gu",
        () -> new GuItem(GuRegistry.id("lucid_dream_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> NIGHTMARE_GU = ITEMS.register("nightmare_gu",
        () -> new GuItem(GuRegistry.id("nightmare_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> PHANTOM_GU = ITEMS.register("phantom_gu",
        () -> new GuItem(GuRegistry.id("phantom_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> MIRAGE_GU = ITEMS.register("mirage_gu",
        () -> new GuItem(GuRegistry.id("mirage_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> GRAND_ILLUSION_GU = ITEMS.register("grand_illusion_gu",
        () -> new GuItem(GuRegistry.id("grand_illusion_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> FLYING_SWORD_GU = ITEMS.register("flying_sword_gu",
        () -> new GuItem(GuRegistry.id("flying_sword_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> SWORD_SHIELD_GU = ITEMS.register("sword_shield_gu",
        () -> new GuItem(GuRegistry.id("sword_shield_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> MYRIAD_SWORD_GU = ITEMS.register("myriad_sword_gu",
        () -> new GuItem(GuRegistry.id("myriad_sword_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> MOON_SLASH_GU = ITEMS.register("moon_slash_gu",
        () -> new GuItem(GuRegistry.id("moon_slash_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> BLADE_ARMOR_GU = ITEMS.register("blade_armor_gu",
        () -> new GuItem(GuRegistry.id("blade_armor_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> HEAVEN_BLADE_GU = ITEMS.register("heaven_blade_gu",
        () -> new GuItem(GuRegistry.id("heaven_blade_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> STARLIGHT_GU = ITEMS.register("starlight_gu",
        () -> new GuItem(GuRegistry.id("starlight_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> STAR_SHIELD_GU = ITEMS.register("star_shield_gu",
        () -> new GuItem(GuRegistry.id("star_shield_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> STAR_FALL_GU = ITEMS.register("star_fall_gu",
        () -> new GuItem(GuRegistry.id("star_fall_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> LUCKY_GU = ITEMS.register("lucky_gu",
        () -> new GuItem(GuRegistry.id("lucky_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> MISFORTUNE_WARD_GU = ITEMS.register("misfortune_ward_gu",
        () -> new GuItem(GuRegistry.id("misfortune_ward_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> HEAVENS_SECRET_GU = ITEMS.register("heavens_secret_gu",
        () -> new GuItem(GuRegistry.id("heavens_secret_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> KILL_INTENT_GU = ITEMS.register("kill_intent_gu",
        () -> new GuItem(GuRegistry.id("kill_intent_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> KILLING_CHANCE_GU = ITEMS.register("killing_chance_gu",
        () -> new GuItem(GuRegistry.id("killing_chance_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> DEATH_STRIKE_GU = ITEMS.register("death_strike_gu",
        () -> new GuItem(GuRegistry.id("death_strike_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> SHRINK_GROUND_GU = ITEMS.register("shrink_ground_gu",
        () -> new GuItem(GuRegistry.id("shrink_ground_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> MORPH_GU = ITEMS.register("morph_gu",
        () -> new GuItem(GuRegistry.id("morph_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> HEAVEN_CHANGE_GU = ITEMS.register("heaven_change_gu",
        () -> new GuItem(GuRegistry.id("heaven_change_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> FORMATION_SOLDIER_GU = ITEMS.register("formation_soldier_gu",
        () -> new GuItem(GuRegistry.id("formation_soldier_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> GOLDEN_ARMOR_GU = ITEMS.register("golden_armor_gu",
        () -> new GuItem(GuRegistry.id("golden_armor_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> THOUSAND_ARMY_GU = ITEMS.register("thousand_army_gu",
        () -> new GuItem(GuRegistry.id("thousand_army_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> SOUND_WAVE_GU = ITEMS.register("sound_wave_gu",
        () -> new GuItem(GuRegistry.id("sound_wave_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> SILENCE_GU = ITEMS.register("silence_gu",
        () -> new GuItem(GuRegistry.id("silence_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> HEAVENLY_SOUND_GU = ITEMS.register("heavenly_sound_gu",
        () -> new GuItem(GuRegistry.id("heavenly_sound_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> BONE_SPEAR_GU = ITEMS.register("bone_spear_gu",
        () -> new GuItem(GuRegistry.id("bone_spear_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> BONE_ARMOR_GU = ITEMS.register("bone_armor_gu",
        () -> new GuItem(GuRegistry.id("bone_armor_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> WHITE_BONE_GU = ITEMS.register("white_bone_gu",
        () -> new GuItem(GuRegistry.id("white_bone_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> CLOUD_RIDE_GU = ITEMS.register("cloud_ride_gu",
        () -> new GuItem(GuRegistry.id("cloud_ride_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> FLIGHT_WING_GU = ITEMS.register("flight_wing_gu",
        () -> new GuItem(GuRegistry.id("flight_wing_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> SKY_EAGLE_GU = ITEMS.register("sky_eagle_gu",
        () -> new GuItem(GuRegistry.id("sky_eagle_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> TRUE_QI_GU = ITEMS.register("true_qi_gu",
        () -> new GuItem(GuRegistry.id("true_qi_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> QI_SHIELD_GU = ITEMS.register("qi_shield_gu",
        () -> new GuItem(GuRegistry.id("qi_shield_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> PROFOUND_QI_GU = ITEMS.register("profound_qi_gu",
        () -> new GuItem(GuRegistry.id("profound_qi_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> YIN_YANG_GU = ITEMS.register("yin_yang_gu",
        () -> new GuItem(GuRegistry.id("yin_yang_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> TAI_CHI_GU = ITEMS.register("tai_chi_gu",
        () -> new GuItem(GuRegistry.id("tai_chi_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> PRIMORDIAL_GU = ITEMS.register("primordial_gu",
        () -> new GuItem(GuRegistry.id("primordial_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> WARP_GU = ITEMS.register("warp_gu",
        () -> new GuItem(GuRegistry.id("warp_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> SPACE_BARRIER_GU = ITEMS.register("space_barrier_gu",
        () -> new GuItem(GuRegistry.id("space_barrier_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> TIME_DECEL_GU = ITEMS.register("time_decel_gu",
        () -> new GuItem(GuRegistry.id("time_decel_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> TIME_SHIELD_GU = ITEMS.register("time_shield_gu",
        () -> new GuItem(GuRegistry.id("time_shield_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> TIME_REVERSAL_GU = ITEMS.register("time_reversal_gu",
        () -> new GuItem(GuRegistry.id("time_reversal_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> CHARM_GU = ITEMS.register("charm_gu",
        () -> new GuItem(GuRegistry.id("charm_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> BEWITCH_GU = ITEMS.register("bewitch_gu",
        () -> new GuItem(GuRegistry.id("bewitch_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> SOUL_CHARM_GU = ITEMS.register("soul_charm_gu",
        () -> new GuItem(GuRegistry.id("soul_charm_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> THOUGHT_GU = ITEMS.register("thought_gu",
        () -> new GuItem(GuRegistry.id("thought_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> MIND_GUARD_GU = ITEMS.register("mind_guard_gu",
        () -> new GuItem(GuRegistry.id("mind_guard_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> VOID_BOLT_GU = ITEMS.register("void_bolt_gu",
        () -> new GuItem(GuRegistry.id("void_bolt_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> VOID_CLOAK_GU = ITEMS.register("void_cloak_gu",
        () -> new GuItem(GuRegistry.id("void_cloak_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> VOID_ANNIHILATION_GU = ITEMS.register("void_annihilation_gu",
        () -> new GuItem(GuRegistry.id("void_annihilation_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> SEAL_GU = ITEMS.register("seal_gu",
        () -> new GuItem(GuRegistry.id("seal_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> RESTRICTION_GU = ITEMS.register("restriction_gu",
        () -> new GuItem(GuRegistry.id("restriction_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> HEAVEN_SEAL_GU = ITEMS.register("heaven_seal_gu",
        () -> new GuItem(GuRegistry.id("heaven_seal_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> HEAVEN_WILL_GU = ITEMS.register("heaven_will_gu",
        () -> new GuItem(GuRegistry.id("heaven_will_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> HEAVEN_SHIELD_GU = ITEMS.register("heaven_shield_gu",
        () -> new GuItem(GuRegistry.id("heaven_shield_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> HEAVEN_PUNISHMENT_GU = ITEMS.register("heaven_punishment_gu",
        () -> new GuItem(GuRegistry.id("heaven_punishment_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> RULE_GU = ITEMS.register("rule_gu",
        () -> new GuItem(GuRegistry.id("rule_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> ORDER_GU = ITEMS.register("order_gu",
        () -> new GuItem(GuRegistry.id("order_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> SUPREME_LAW_GU = ITEMS.register("supreme_law_gu",
        () -> new GuItem(GuRegistry.id("supreme_law_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> SHADOW_DART_GU = ITEMS.register("shadow_dart_gu",
        () -> new GuItem(GuRegistry.id("shadow_dart_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> SHADOW_VEIL_GU = ITEMS.register("shadow_veil_gu",
        () -> new GuItem(GuRegistry.id("shadow_veil_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> SHADOW_DEVOUR_GU = ITEMS.register("shadow_devour_gu",
        () -> new GuItem(GuRegistry.id("shadow_devour_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> MIST_GU = ITEMS.register("mist_gu",
        () -> new GuItem(GuRegistry.id("mist_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> CLOUD_ARMOR_GU = ITEMS.register("cloud_armor_gu",
        () -> new GuItem(GuRegistry.id("cloud_armor_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> CLOUD_STORM_GU = ITEMS.register("cloud_storm_gu",
        () -> new GuItem(GuRegistry.id("cloud_storm_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> TRAP_FORMATION_GU = ITEMS.register("trap_formation_gu",
        () -> new GuItem(GuRegistry.id("trap_formation_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> FORMATION_SHIELD_GU = ITEMS.register("formation_shield_gu",
        () -> new GuItem(GuRegistry.id("formation_shield_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> GRAND_FORMATION_GU = ITEMS.register("grand_formation_gu",
        () -> new GuItem(GuRegistry.id("grand_formation_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> REFINE_FIRE_GU = ITEMS.register("refine_fire_gu",
        () -> new GuItem(GuRegistry.id("refine_fire_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> REFINE_BODY_GU = ITEMS.register("refine_body_gu",
        () -> new GuItem(GuRegistry.id("refine_body_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> HEAVEN_REFINE_GU = ITEMS.register("heaven_refine_gu",
        () -> new GuItem(GuRegistry.id("heaven_refine_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> PILL_POISON_GU = ITEMS.register("pill_poison_gu",
        () -> new GuItem(GuRegistry.id("pill_poison_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> PILL_SHIELD_GU = ITEMS.register("pill_shield_gu",
        () -> new GuItem(GuRegistry.id("pill_shield_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> IMMORTAL_PILL_GU = ITEMS.register("immortal_pill_gu",
        () -> new GuItem(GuRegistry.id("immortal_pill_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> PAINT_BRUSH_GU = ITEMS.register("paint_brush_gu",
        () -> new GuItem(GuRegistry.id("paint_brush_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> PAINT_SHIELD_GU = ITEMS.register("paint_shield_gu",
        () -> new GuItem(GuRegistry.id("paint_shield_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> MYRIAD_PAINT_GU = ITEMS.register("myriad_paint_gu",
        () -> new GuItem(GuRegistry.id("myriad_paint_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> STEAL_QI_GU = ITEMS.register("steal_qi_gu",
        () -> new GuItem(GuRegistry.id("steal_qi_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> STEAL_HIDE_GU = ITEMS.register("steal_hide_gu",
        () -> new GuItem(GuRegistry.id("steal_hide_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> HEAVEN_STEAL_GU = ITEMS.register("heaven_steal_gu",
        () -> new GuItem(GuRegistry.id("heaven_steal_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> INFO_DART_GU = ITEMS.register("info_dart_gu",
        () -> new GuItem(GuRegistry.id("info_dart_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> INFO_NET_GU = ITEMS.register("info_net_gu",
        () -> new GuItem(GuRegistry.id("info_net_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> HEAVEN_INFO_GU = ITEMS.register("heaven_info_gu",
        () -> new GuItem(GuRegistry.id("heaven_info_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> HUMAN_HEART_GU = ITEMS.register("human_heart_gu",
        () -> new GuItem(GuRegistry.id("human_heart_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> HUMAN_BOND_GU = ITEMS.register("human_bond_gu",
        () -> new GuItem(GuRegistry.id("human_bond_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> HUMAN_WILL_GU = ITEMS.register("human_will_gu",
        () -> new GuItem(GuRegistry.id("human_will_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> ENSLAVE_WORM_GU = ITEMS.register("enslave_worm_gu",
        () -> new GuItem(GuRegistry.id("enslave_worm_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> ENSLAVE_SHIELD_GU = ITEMS.register("enslave_shield_gu",
        () -> new GuItem(GuRegistry.id("enslave_shield_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> FEAST_GU = ITEMS.register("feast_gu",
        () -> new GuItem(GuRegistry.id("feast_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> SNAKE_TONGUE_GU = ITEMS.register("snake_tongue_gu",
        () -> new GuItem(GuRegistry.id("snake_tongue_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> EARTH_LISTENER_GU = ITEMS.register("earth_listener_gu",
        () -> new GuItem(GuRegistry.id("earth_listener_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> KEEN_EAR_GU = ITEMS.register("keen_ear_gu",
        () -> new GuItem(GuRegistry.id("keen_ear_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> HIDDEN_SCALE_GU = ITEMS.register("hidden_scale_gu",
        () -> new GuItem(GuRegistry.id("hidden_scale_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> TRUE_SIGHT_GU = ITEMS.register("true_sight_gu",
        () -> new GuItem(GuRegistry.id("true_sight_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> ELECTRIC_EYE_GU = ITEMS.register("electric_eye_gu",
        () -> new GuItem(GuRegistry.id("electric_eye_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> GU_MERCHANT_SPAWN_EGG = ITEMS.register("gu_merchant_spawn_egg",
        () -> new DeferredSpawnEggItem(ModEntities.GU_MERCHANT, 0x4A3728, 0xC9A86C, new Item.Properties()));

    public static final DeferredItem<Item> GU_CODEX = ITEMS.register("gu_codex",
        () -> new GuCodexItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> BLESSED_LAND_CORE = ITEMS.register("blessed_land_core",
        () -> new BlockItem(ModBlocks.BLESSED_LAND_CORE.get(), new Item.Properties()));

    public static final DeferredItem<Item> WILD_GU_SPAWN_EGG = ITEMS.register("wild_gu_spawn_egg",
        () -> new DeferredSpawnEggItem(ModEntities.WILD_GU, 0x66CC99, 0xFFDD44, new Item.Properties()));

    public static final DeferredItem<Item> GU_MASTER_SPAWN_EGG = ITEMS.register("gu_master_spawn_egg",
        () -> new DeferredSpawnEggItem(ModEntities.GU_MASTER, 0x333366, 0xCC3333, new Item.Properties()));

    public static final DeferredItem<Item> AGED_WINE = ITEMS.register("aged_wine",
        () -> new Item(new Item.Properties().stacksTo(16)
            .food(new FoodProperties.Builder()
                .nutrition(2)
                .saturationModifier(0.3f)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 40, 0), 1.0f)
                .build())));

    public static final DeferredItem<Item> BEAST_BONE = ITEMS.register("beast_bone",
        () -> new Item(new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> BLOOD_VIAL = ITEMS.register("blood_vial",
        () -> new Item(new Item.Properties().stacksTo(16)));

    public static final DeferredItem<Item> WILD_MOON_ORCHID = ITEMS.register("wild_moon_orchid",
        () -> new BlockItem(ModBlocks.WILD_MOON_ORCHID.get(), new Item.Properties()));

    public static final DeferredItem<Item> RAINBOW_STALACTITE = ITEMS.register("rainbow_stalactite",
        () -> new BlockItem(ModBlocks.RAINBOW_STALACTITE.get(), new Item.Properties()));

    public static final DeferredItem<Item> SPEAR_BAMBOO = ITEMS.register("spear_bamboo",
        () -> new BlockItem(ModBlocks.SPEAR_BAMBOO.get(), new Item.Properties()));

    public static final DeferredItem<Item> GU_SHELF = ITEMS.register("gu_shelf",
        () -> new BlockItem(ModBlocks.GU_SHELF.get(), new Item.Properties()));

    public static final DeferredItem<Item> WINE_JAR = ITEMS.register("wine_jar",
        () -> new BlockItem(ModBlocks.WINE_JAR.get(), new Item.Properties()));

    public static final DeferredItem<Item> FORMATION_STONE = ITEMS.register("formation_stone",
        () -> new BlockItem(ModBlocks.FORMATION_STONE.get(), new Item.Properties()));

    public static final DeferredItem<Item> NAIVE_MUSHROOM = ITEMS.register("naive_mushroom",
        () -> new BlockItem(ModBlocks.NAIVE_MUSHROOM.get(), new Item.Properties()));

    private static final FoodProperties GREEN_BAMBOO_WINE_FOOD = new FoodProperties.Builder()
        .nutrition(4)
        .saturationModifier(0.6f)
        .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 60, 0), 1.0f)
        .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 200, 0), 1.0f)
        .build();

    public static final DeferredItem<Item> GREEN_BAMBOO_WINE = ITEMS.register("green_bamboo_wine",
        () -> new Item(new Item.Properties().stacksTo(16).food(GREEN_BAMBOO_WINE_FOOD)));

    public static final DeferredItem<Item> KNOW_HEART_GRASS = ITEMS.register("know_heart_grass",
        () -> new Item(new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> LIGHTNING_WOLF_SPAWN_EGG = ITEMS.register("lightning_wolf_spawn_egg",
        () -> new DeferredSpawnEggItem(ModEntities.LIGHTNING_WOLF, 0x3344AA, 0xFFFF44, new Item.Properties()));

    public static final DeferredItem<Item> THUNDER_CROWN_WOLF_SPAWN_EGG = ITEMS.register("thunder_crown_wolf_spawn_egg",
        () -> new DeferredSpawnEggItem(ModEntities.THUNDER_CROWN_WOLF, 0x2233AA, 0xFFDD00, new Item.Properties()));

    public static final DeferredItem<Item> MOUNTAIN_BOAR_SPAWN_EGG = ITEMS.register("mountain_boar_spawn_egg",
        () -> new DeferredSpawnEggItem(ModEntities.MOUNTAIN_BOAR, 0x6B4226, 0x8B6914, new Item.Properties()));

    public static final DeferredItem<Item> JADE_EYE = ITEMS.register("jade_eye",
        () -> new Item(new Item.Properties().stacksTo(64)));

    private static final FoodProperties BITTER_WINE_FOOD = new FoodProperties.Builder()
        .nutrition(3)
        .saturationModifier(0.4f)
        .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 0), 1.0f)
        .build();

    public static final DeferredItem<Item> BITTER_WINE = ITEMS.register("bitter_wine",
        () -> new Item(new Item.Properties().stacksTo(16).food(BITTER_WINE_FOOD)));

    public static final DeferredItem<Item> JADE_EYE_MONKEY_SPAWN_EGG = ITEMS.register("jade_eye_monkey_spawn_egg",
        () -> new DeferredSpawnEggItem(ModEntities.JADE_EYE_MONKEY, 0x7B8B6F, 0x00FF66, new Item.Properties()));

    public static final DeferredItem<Item> STRAW_PUPPET_SPAWN_EGG = ITEMS.register("straw_puppet_spawn_egg",
        () -> new DeferredSpawnEggItem(ModEntities.STRAW_PUPPET, 0xC4A35A, 0x8B7355, new Item.Properties()));

    public static final DeferredItem<Item> SPIDER_SILK = ITEMS.register("spider_silk",
        () -> new Item(new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> MOUNTAIN_SPIDER_SPAWN_EGG = ITEMS.register("mountain_spider_spawn_egg",
        () -> new DeferredSpawnEggItem(ModEntities.MOUNTAIN_SPIDER, 0x3B2F2F, 0x8B0000, new Item.Properties()));

    public static final DeferredItem<Item> ANCIENT_GU_IMMORTAL_SPAWN_EGG = ITEMS.register("ancient_gu_immortal_spawn_egg",
        () -> new DeferredSpawnEggItem(ModEntities.ANCIENT_GU_IMMORTAL, 0x2A0A4A, 0xC0A0FF, new Item.Properties()));

    public static final DeferredItem<Item> CULTIVATION_MANUAL = ITEMS.register("cultivation_manual",
        () -> new com.reverendinsanity.item.CultivationManualItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> GAMBLING_STONE_LOW = ITEMS.register("gambling_stone_low",
        () -> new com.reverendinsanity.item.GamblingStoneItem(
            com.reverendinsanity.item.GamblingStoneItem.Grade.LOW, new Item.Properties().stacksTo(16)));

    public static final DeferredItem<Item> GAMBLING_STONE_MEDIUM = ITEMS.register("gambling_stone_medium",
        () -> new com.reverendinsanity.item.GamblingStoneItem(
            com.reverendinsanity.item.GamblingStoneItem.Grade.MEDIUM, new Item.Properties().stacksTo(16)));

    public static final DeferredItem<Item> GAMBLING_STONE_HIGH = ITEMS.register("gambling_stone_high",
        () -> new com.reverendinsanity.item.GamblingStoneItem(
            com.reverendinsanity.item.GamblingStoneItem.Grade.HIGH, new Item.Properties().stacksTo(16)));

    public static final DeferredItem<Item> BRONZE_SARIRA_GU = ITEMS.register("bronze_sarira_gu",
        () -> new GuItem(GuRegistry.id("bronze_sarira_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> IRON_SARIRA_GU = ITEMS.register("iron_sarira_gu",
        () -> new GuItem(GuRegistry.id("iron_sarira_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> SILVER_SARIRA_GU = ITEMS.register("silver_sarira_gu",
        () -> new GuItem(GuRegistry.id("silver_sarira_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> GOLD_SARIRA_GU = ITEMS.register("gold_sarira_gu",
        () -> new GuItem(GuRegistry.id("gold_sarira_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> BLACK_BOAR_GU = ITEMS.register("black_boar_gu",
        () -> new GuItem(GuRegistry.id("black_boar_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> BROWN_BEAR_GU = ITEMS.register("brown_bear_gu",
        () -> new GuItem(GuRegistry.id("brown_bear_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> FLOWER_BOAR_GU = ITEMS.register("flower_boar_gu",
        () -> new GuItem(GuRegistry.id("flower_boar_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> YELLOW_CAMEL_BEETLE_GU = ITEMS.register("yellow_camel_beetle_gu",
        () -> new GuItem(GuRegistry.id("yellow_camel_beetle_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> STONE_SKIN_GU = ITEMS.register("stone_skin_gu",
        () -> new GuItem(GuRegistry.id("stone_skin_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> IRON_SKIN_GU = ITEMS.register("iron_skin_gu",
        () -> new GuItem(GuRegistry.id("iron_skin_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> BEAST_SKIN_GU = ITEMS.register("beast_skin_gu",
        () -> new GuItem(GuRegistry.id("beast_skin_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> BLACK_BRISTLE_GU = ITEMS.register("black_bristle_gu",
        () -> new GuItem(GuRegistry.id("black_bristle_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> STEEL_BRISTLE_GU = ITEMS.register("steel_bristle_gu",
        () -> new GuItem(GuRegistry.id("steel_bristle_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> HEAVEN_CANOPY_GU = ITEMS.register("heaven_canopy_gu",
        () -> new GuItem(GuRegistry.id("heaven_canopy_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> VITALITY_GRASS_GU = ITEMS.register("vitality_grass_gu",
        () -> new GuItem(GuRegistry.id("vitality_grass_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> VITALITY_LEAF_GU = ITEMS.register("vitality_leaf_gu",
        () -> new GuItem(GuRegistry.id("vitality_leaf_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> WATER_SPIDER_GU = ITEMS.register("water_spider_gu",
        () -> new GuItem(GuRegistry.id("water_spider_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> SIGNAL_GU = ITEMS.register("signal_gu",
        () -> new GuItem(GuRegistry.id("signal_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> FLASH_GU = ITEMS.register("flash_gu",
        () -> new GuItem(GuRegistry.id("flash_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> SHADOW_FOLLOWER_GU = ITEMS.register("shadow_follower_gu",
        () -> new GuItem(GuRegistry.id("shadow_follower_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> DRAGON_CRICKET_GU = ITEMS.register("dragon_cricket_gu",
        () -> new GuItem(GuRegistry.id("dragon_cricket_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> QUIET_STEP_GU = ITEMS.register("quiet_step_gu",
        () -> new GuItem(GuRegistry.id("quiet_step_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> SCENT_LOCK_GU = ITEMS.register("scent_lock_gu",
        () -> new GuItem(GuRegistry.id("scent_lock_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> LOVE_SEPARATION_GU = ITEMS.register("love_separation_gu",
        () -> new GuItem(GuRegistry.id("love_separation_gu"), new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> WHITE_HEAVEN_FRAGMENT = ITEMS.register("white_heaven_fragment",
        () -> new HeavenFragmentItem(com.reverendinsanity.core.heaven.HeavenType.WHITE));
    public static final DeferredItem<Item> RED_HEAVEN_FRAGMENT = ITEMS.register("red_heaven_fragment",
        () -> new HeavenFragmentItem(com.reverendinsanity.core.heaven.HeavenType.RED));
    public static final DeferredItem<Item> ORANGE_HEAVEN_FRAGMENT = ITEMS.register("orange_heaven_fragment",
        () -> new HeavenFragmentItem(com.reverendinsanity.core.heaven.HeavenType.ORANGE));
    public static final DeferredItem<Item> YELLOW_HEAVEN_FRAGMENT = ITEMS.register("yellow_heaven_fragment",
        () -> new HeavenFragmentItem(com.reverendinsanity.core.heaven.HeavenType.YELLOW));
    public static final DeferredItem<Item> GREEN_HEAVEN_FRAGMENT = ITEMS.register("green_heaven_fragment",
        () -> new HeavenFragmentItem(com.reverendinsanity.core.heaven.HeavenType.GREEN));
    public static final DeferredItem<Item> CYAN_HEAVEN_FRAGMENT = ITEMS.register("cyan_heaven_fragment",
        () -> new HeavenFragmentItem(com.reverendinsanity.core.heaven.HeavenType.CYAN));
    public static final DeferredItem<Item> BLUE_HEAVEN_FRAGMENT = ITEMS.register("blue_heaven_fragment",
        () -> new HeavenFragmentItem(com.reverendinsanity.core.heaven.HeavenType.BLUE));
    public static final DeferredItem<Item> PURPLE_HEAVEN_FRAGMENT = ITEMS.register("purple_heaven_fragment",
        () -> new HeavenFragmentItem(com.reverendinsanity.core.heaven.HeavenType.PURPLE));
    public static final DeferredItem<Item> BLACK_HEAVEN_FRAGMENT = ITEMS.register("black_heaven_fragment",
        () -> new HeavenFragmentItem(com.reverendinsanity.core.heaven.HeavenType.BLACK));

    public static final DeferredItem<Item> VENERABLE_YUAN_SHI_EGG = ITEMS.register("venerable_yuan_shi_spawn_egg",
        () -> new VenerableSpawnEggItem(VenerableType.YUAN_SHI));
    public static final DeferredItem<Item> VENERABLE_XING_XIU_EGG = ITEMS.register("venerable_xing_xiu_spawn_egg",
        () -> new VenerableSpawnEggItem(VenerableType.XING_XIU));
    public static final DeferredItem<Item> VENERABLE_YUAN_LIAN_EGG = ITEMS.register("venerable_yuan_lian_spawn_egg",
        () -> new VenerableSpawnEggItem(VenerableType.YUAN_LIAN));
    public static final DeferredItem<Item> VENERABLE_WU_JI_EGG = ITEMS.register("venerable_wu_ji_spawn_egg",
        () -> new VenerableSpawnEggItem(VenerableType.WU_JI));
    public static final DeferredItem<Item> VENERABLE_KUANG_MAN_EGG = ITEMS.register("venerable_kuang_man_spawn_egg",
        () -> new VenerableSpawnEggItem(VenerableType.KUANG_MAN));
    public static final DeferredItem<Item> VENERABLE_DAO_TIAN_EGG = ITEMS.register("venerable_dao_tian_spawn_egg",
        () -> new VenerableSpawnEggItem(VenerableType.DAO_TIAN));
    public static final DeferredItem<Item> VENERABLE_JU_YANG_EGG = ITEMS.register("venerable_ju_yang_spawn_egg",
        () -> new VenerableSpawnEggItem(VenerableType.JU_YANG));
    public static final DeferredItem<Item> VENERABLE_YOU_HUN_EGG = ITEMS.register("venerable_you_hun_spawn_egg",
        () -> new VenerableSpawnEggItem(VenerableType.YOU_HUN));
    public static final DeferredItem<Item> VENERABLE_LE_TU_EGG = ITEMS.register("venerable_le_tu_spawn_egg",
        () -> new VenerableSpawnEggItem(VenerableType.LE_TU));
    public static final DeferredItem<Item> VENERABLE_HONG_LIAN_EGG = ITEMS.register("venerable_hong_lian_spawn_egg",
        () -> new VenerableSpawnEggItem(VenerableType.HONG_LIAN));
}
