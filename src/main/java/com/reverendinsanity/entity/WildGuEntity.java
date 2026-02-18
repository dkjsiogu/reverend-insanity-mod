package com.reverendinsanity.entity;

import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.core.gu.GuType;
import com.reverendinsanity.registry.ModAttachments;
import com.reverendinsanity.registry.ModEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.joml.Vector3f;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import javax.annotation.Nullable;
import java.util.List;

// 野蛊实体：自然生成在世界中的蛊虫，玩家可右键捕获
public class WildGuEntity extends PathfinderMob {

    private static final EntityDataAccessor<String> DATA_GU_TYPE_ID =
        SynchedEntityData.defineId(WildGuEntity.class, EntityDataSerializers.STRING);

    private static final List<String> RANK_1_GU = List.of(
        "moonlight_gu", "liquor_worm", "bear_strength_gu",
        "jade_skin_gu", "white_boar_gu", "stealth_scales_gu",
        "blood_gu", "self_heal_gu", "solidify_origin_gu", "poison_bee_gu",
        "savage_bull_gu", "taishan_gu", "cold_ice_gu", "frost_armor_gu",
        "fire_seed_gu", "flame_armor_gu", "earth_wall_gu", "earth_split_gu",
        "breeze_gu", "wind_blade_gu", "lightning_gu", "thunder_shield_gu",
        "tide_gu", "water_shield_gu", "soul_search_gu", "soul_shield_gu",
        "light_beam_gu", "radiance_gu", "dark_bolt_gu", "shadow_cloak_gu",
        "dream_gu", "lucid_dream_gu", "phantom_gu", "mirage_gu",
        "flying_sword_gu", "sword_shield_gu", "moon_slash_gu", "blade_armor_gu",
        "starlight_gu", "star_shield_gu", "lucky_gu", "misfortune_ward_gu",
        "kill_intent_gu", "killing_chance_gu", "shrink_ground_gu", "morph_gu",
        "formation_soldier_gu", "golden_armor_gu", "sound_wave_gu", "silence_gu",
        "bone_spear_gu", "bone_armor_gu", "cloud_ride_gu", "flight_wing_gu",
        "true_qi_gu", "qi_shield_gu", "yin_yang_gu", "tai_chi_gu",
        "warp_gu", "space_barrier_gu",
        "void_bolt_gu", "time_decel_gu", "time_shield_gu",
        "charm_gu", "bewitch_gu", "thought_gu", "mind_guard_gu",
        "void_cloak_gu", "seal_gu", "restriction_gu",
        "heaven_will_gu", "heaven_shield_gu", "rule_gu", "order_gu",
        "shadow_dart_gu", "shadow_veil_gu", "mist_gu", "cloud_armor_gu",
        "trap_formation_gu", "formation_shield_gu",
        "refine_fire_gu", "refine_body_gu",
        "pill_poison_gu", "pill_shield_gu",
        "paint_brush_gu", "paint_shield_gu",
        "steal_qi_gu", "steal_hide_gu",
        "info_dart_gu", "info_net_gu",
        "human_heart_gu", "human_bond_gu",
        "enslave_worm_gu", "enslave_shield_gu",
        "feast_gu",
        "electric_eye_gu"
    );

    private static final List<String> RANK_2_GU = List.of(
        "four_flavors_liquor_worm", "gold_light_worm",
        "iron_bone_gu", "enslave_snake_gu", "moonscar_gu",
        "blood_wing_gu", "gold_silkworm_gu",
        "giant_strength_gu", "ice_seal_gu",
        "blazing_flame_gu", "petrify_gu",
        "gale_gu", "thunderstorm_gu",
        "torrent_gu", "soul_crush_gu",
        "blazing_light_gu", "abyss_devour_gu",
        "nightmare_gu", "grand_illusion_gu",
        "myriad_sword_gu", "heaven_blade_gu",
        "star_fall_gu", "heavens_secret_gu",
        "death_strike_gu", "heaven_change_gu",
        "thousand_army_gu", "heavenly_sound_gu",
        "white_bone_gu", "sky_eagle_gu",
        "profound_qi_gu", "primordial_gu",
        "time_reversal_gu",
        "myriad_paint_gu", "heaven_steal_gu", "heaven_info_gu",
        "human_will_gu",
        "snake_tongue_gu", "earth_listener_gu", "keen_ear_gu",
        "hidden_scale_gu"
    );

    private static final List<String> DEEP_CAVE_GU = List.of(
        "gold_light_worm", "iron_bone_gu", "blood_wing_gu",
        "giant_strength_gu", "blazing_flame_gu", "thunderstorm_gu",
        "soul_crush_gu", "abyss_devour_gu",
        "star_fall_gu",
        "death_strike_gu",
        "time_reversal_gu",
        "soul_charm_gu",
        "void_annihilation_gu", "heaven_seal_gu",
        "heaven_punishment_gu", "supreme_law_gu",
        "shadow_devour_gu", "cloud_storm_gu",
        "grand_formation_gu", "heaven_refine_gu", "immortal_pill_gu",
        "myriad_paint_gu", "heaven_steal_gu", "heaven_info_gu",
        "human_will_gu",
        "true_sight_gu"
    );

    private static final List<String> CAVE_GU = List.of(
        "moonlight_gu", "bear_strength_gu", "jade_skin_gu",
        "blood_gu", "solidify_origin_gu",
        "savage_bull_gu", "taishan_gu",
        "fire_seed_gu", "earth_wall_gu", "earth_split_gu",
        "lightning_gu", "soul_search_gu",
        "dark_bolt_gu", "shadow_cloak_gu",
        "flying_sword_gu", "moon_slash_gu",
        "kill_intent_gu", "bone_spear_gu", "bone_armor_gu",
        "warp_gu", "space_barrier_gu",
        "void_bolt_gu", "seal_gu", "restriction_gu",
        "heaven_will_gu", "rule_gu",
        "shadow_dart_gu",
        "paint_brush_gu", "steal_qi_gu", "info_dart_gu",
        "human_heart_gu", "enslave_worm_gu"
    );

    private static final List<String> NIGHT_SURFACE_GU = List.of(
        "moonlight_gu", "stealth_scales_gu", "moonscar_gu",
        "blood_gu", "self_heal_gu",
        "cold_ice_gu", "frost_armor_gu",
        "fire_seed_gu", "wind_blade_gu", "lightning_gu",
        "soul_search_gu", "soul_shield_gu",
        "dark_bolt_gu", "shadow_cloak_gu",
        "dream_gu", "lucid_dream_gu", "phantom_gu",
        "flying_sword_gu", "moon_slash_gu",
        "starlight_gu", "star_shield_gu",
        "kill_intent_gu", "shrink_ground_gu",
        "sound_wave_gu", "silence_gu",
        "time_decel_gu", "time_shield_gu",
        "shadow_dart_gu", "shadow_veil_gu",
        "hidden_scale_gu"
    );

    private static final List<String> DAY_SURFACE_GU = List.of(
        "liquor_worm", "white_boar_gu", "bear_strength_gu",
        "savage_bull_gu", "earth_wall_gu", "earth_split_gu",
        "breeze_gu", "tide_gu", "water_shield_gu",
        "light_beam_gu", "radiance_gu",
        "sword_shield_gu", "blade_armor_gu",
        "starlight_gu", "lucky_gu",
        "killing_chance_gu", "morph_gu",
        "formation_soldier_gu", "golden_armor_gu",
        "cloud_ride_gu", "flight_wing_gu",
        "mist_gu", "cloud_armor_gu"
    );

    private static final List<String> SWAMP_DARK_FOREST_GU = List.of(
        "enslave_snake_gu", "stealth_scales_gu",
        "poison_bee_gu", "gold_silkworm_gu",
        "dream_gu", "nightmare_gu", "phantom_gu",
        "charm_gu", "bewitch_gu"
    );

    private static final List<String> DESERT_GU = List.of(
        "fire_seed_gu", "flame_armor_gu", "earth_wall_gu", "earth_split_gu",
        "breeze_gu", "wind_blade_gu", "light_beam_gu", "radiance_gu",
        "time_decel_gu", "mirage_gu", "refine_fire_gu", "taishan_gu",
        "savage_bull_gu", "kill_intent_gu"
    );

    private static final List<String> OCEAN_GU = List.of(
        "tide_gu", "water_shield_gu", "cold_ice_gu", "frost_armor_gu",
        "breeze_gu", "soul_search_gu", "cloud_ride_gu", "sound_wave_gu",
        "mist_gu", "yin_yang_gu", "flight_wing_gu"
    );

    private static final List<String> SNOWY_GU = List.of(
        "cold_ice_gu", "frost_armor_gu", "wind_blade_gu",
        "bone_spear_gu", "bone_armor_gu", "shadow_cloak_gu",
        "silence_gu", "time_decel_gu", "time_shield_gu",
        "moonlight_gu", "star_shield_gu"
    );

    private static final List<String> JUNGLE_GU = List.of(
        "poison_bee_gu", "blood_gu", "self_heal_gu", "stealth_scales_gu",
        "enslave_worm_gu", "charm_gu", "bewitch_gu", "phantom_gu",
        "feast_gu", "human_bond_gu", "paint_brush_gu",
        "dream_gu", "shrink_ground_gu"
    );

    private static final List<String> MOUNTAIN_GU = List.of(
        "earth_wall_gu", "earth_split_gu", "taishan_gu",
        "bone_spear_gu", "bone_armor_gu", "golden_armor_gu",
        "formation_soldier_gu", "true_qi_gu", "qi_shield_gu",
        "heaven_will_gu", "rule_gu", "iron_bone_gu",
        "wind_blade_gu", "lightning_gu",
        "electric_eye_gu"
    );

    private static final List<String> MUSHROOM_GU = List.of(
        "pill_poison_gu", "pill_shield_gu", "feast_gu",
        "dream_gu", "lucid_dream_gu", "morph_gu",
        "lucky_gu", "yin_yang_gu", "phantom_gu",
        "charm_gu", "info_dart_gu"
    );

    private static final List<String> DEEP_DARK_GU = List.of(
        "soul_search_gu", "soul_shield_gu", "dark_bolt_gu", "shadow_cloak_gu",
        "void_bolt_gu", "void_cloak_gu", "dream_gu", "kill_intent_gu",
        "seal_gu", "restriction_gu", "silence_gu",
        "shadow_dart_gu", "shadow_veil_gu", "enslave_worm_gu",
        "hidden_scale_gu"
    );

    public WildGuEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 2.0)
            .add(Attributes.MOVEMENT_SPEED, 0.35);
    }

    public static boolean checkSpawnRules(EntityType<WildGuEntity> type, LevelAccessor level,
                                          MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getRawBrightness(pos, 0) >= 0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_GU_TYPE_ID, "liquor_worm");
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class, 8.0f, 1.0, 1.5));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        spawnData = super.finalizeSpawn(level, difficulty, reason, spawnData);

        RandomSource rand = level.getRandom();
        BlockPos pos = this.blockPosition();
        int y = pos.getY();
        boolean isNight = level.getLevel().isNight();

        Holder<Biome> biomeHolder = level.getBiome(pos);

        String chosen;
        if (y < 30) {
            if (rand.nextFloat() < 0.12f) {
                chosen = pickRandom(RANK_2_GU, rand);
            } else {
                chosen = pickRandom(DEEP_CAVE_GU, rand);
            }
        } else if (y < 50) {
            if (biomeHolder.is(Biomes.DEEP_DARK) && rand.nextFloat() < 0.3f) {
                chosen = pickRandom(DEEP_DARK_GU, rand);
            } else {
                chosen = pickRandom(CAVE_GU, rand);
            }
        } else if (biomeHolder.is(Biomes.SWAMP) || biomeHolder.is(Biomes.MANGROVE_SWAMP) || biomeHolder.is(Biomes.DARK_FOREST)) {
            chosen = pickRandom(SWAMP_DARK_FOREST_GU, rand);
        } else if (biomeHolder.is(Biomes.DESERT) || biomeHolder.is(Biomes.BADLANDS) || biomeHolder.is(Biomes.ERODED_BADLANDS)) {
            chosen = pickRandom(DESERT_GU, rand);
        } else if (biomeHolder.is(BiomeTags.IS_OCEAN) || biomeHolder.is(BiomeTags.IS_RIVER) || biomeHolder.is(BiomeTags.IS_BEACH)) {
            chosen = pickRandom(OCEAN_GU, rand);
        } else if (biomeHolder.is(Biomes.SNOWY_PLAINS) || biomeHolder.is(Biomes.ICE_SPIKES) || biomeHolder.is(Biomes.FROZEN_RIVER)
                || biomeHolder.is(Biomes.SNOWY_TAIGA) || biomeHolder.is(Biomes.FROZEN_PEAKS) || biomeHolder.is(Biomes.SNOWY_SLOPES)) {
            chosen = pickRandom(SNOWY_GU, rand);
        } else if (biomeHolder.is(BiomeTags.IS_JUNGLE)) {
            chosen = pickRandom(JUNGLE_GU, rand);
        } else if (biomeHolder.is(BiomeTags.IS_MOUNTAIN) || biomeHolder.is(Biomes.STONY_PEAKS) || biomeHolder.is(Biomes.WINDSWEPT_HILLS)) {
            chosen = pickRandom(MOUNTAIN_GU, rand);
        } else if (biomeHolder.is(Biomes.MUSHROOM_FIELDS)) {
            chosen = pickRandom(MUSHROOM_GU, rand);
        } else if (isNight) {
            chosen = pickRandom(NIGHT_SURFACE_GU, rand);
        } else {
            chosen = pickRandom(DAY_SURFACE_GU, rand);
        }

        if (chosen == null) {
            chosen = pickRandom(RANK_1_GU, rand);
        }

        this.setGuTypeId(chosen);
        return spawnData;
    }

    private static String pickRandom(List<String> list, RandomSource rand) {
        if (list.isEmpty()) return null;
        return list.get(rand.nextInt(list.size()));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        String guTypeId = this.getGuTypeId();
        GuType guType = GuRegistry.get(ResourceLocation.fromNamespaceAndPath("reverend_insanity", guTypeId));

        float successRate = 0.40f;

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA);
        if (data.getAperture().isOpened()) {
            successRate += 0.20f;
        }

        if (guType != null && guType.rank() >= 2) {
            successRate -= 0.15f;
        }

        if (this.random.nextFloat() < successRate) {
            ResourceLocation itemId = ResourceLocation.fromNamespaceAndPath("reverend_insanity", guTypeId);
            Item item = BuiltInRegistries.ITEM.get(itemId);
            if (item != Items.AIR) {
                this.spawnAtLocation(new ItemStack(item));
            }
            this.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

            if (this.level() instanceof ServerLevel serverLevel && guType != null) {
                int color = guType.path().getColor();
                float r = ((color >> 16) & 0xFF) / 255f;
                float g = ((color >> 8) & 0xFF) / 255f;
                float b = (color & 0xFF) / 255f;
                DustParticleOptions dust = new DustParticleOptions(new Vector3f(r, g, b), 1.5f);
                serverLevel.sendParticles(dust,
                    this.getX(), this.getY() + 0.5, this.getZ(),
                    15, 0.3, 0.3, 0.3, 0.05);
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                    this.getX(), this.getY() + 0.3, this.getZ(),
                    5, 0.2, 0.2, 0.2, 0.03);
            }

            this.discard();

            GuMasterData captureData = player.getData(ModAttachments.GU_MASTER_DATA);
            if (captureData.getAperture().isOpened()) {
                captureData.discoverGu(ResourceLocation.fromNamespaceAndPath("reverend_insanity", guTypeId));
                com.reverendinsanity.util.AdvancementHelper.grant((net.minecraft.server.level.ServerPlayer) player, "capture_wild_gu");
            }

            return InteractionResult.SUCCESS;
        } else {
            this.playSound(SoundEvents.VILLAGER_NO, 1.0f, 1.0f);

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SMOKE,
                    this.getX(), this.getY() + 0.3, this.getZ(),
                    5, 0.2, 0.2, 0.2, 0.02);
            }

            this.setDeltaMovement(
                (this.random.nextFloat() - 0.5) * 0.8,
                0.3,
                (this.random.nextFloat() - 0.5) * 0.8
            );
            return InteractionResult.FAIL;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel serverLevel && this.tickCount % 10 == 0) {
            String guTypeId = this.getGuTypeId();
            GuType guType = GuRegistry.get(ResourceLocation.fromNamespaceAndPath("reverend_insanity", guTypeId));
            if (guType != null) {
                int color = guType.path().getColor();
                float r = ((color >> 16) & 0xFF) / 255f;
                float g = ((color >> 8) & 0xFF) / 255f;
                float b = (color & 0xFF) / 255f;
                DustParticleOptions dust = new DustParticleOptions(new Vector3f(r, g, b), guType.rank() >= 2 ? 1.2f : 0.8f);
                serverLevel.sendParticles(dust,
                    this.getX(), this.getY() + 0.3, this.getZ(),
                    2, 0.15, 0.15, 0.15, 0.01);
            } else {
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                    this.getX(), this.getY() + 0.2, this.getZ(),
                    1, 0.1, 0.1, 0.1, 0.01);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("GuTypeId", this.getGuTypeId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("GuTypeId")) {
            this.setGuTypeId(tag.getString("GuTypeId"));
        }
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return distanceToClosestPlayer > 48.0 * 48.0;
    }

    public String getGuTypeId() {
        return this.entityData.get(DATA_GU_TYPE_ID);
    }

    public void setGuTypeId(String id) {
        this.entityData.set(DATA_GU_TYPE_ID, id);
    }
}
