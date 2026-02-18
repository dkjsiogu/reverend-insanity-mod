package com.reverendinsanity.registry;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.entity.BloodBoltEntity;
import com.reverendinsanity.entity.FireBoltEntity;
import com.reverendinsanity.entity.FormlessHandEntity;
import com.reverendinsanity.entity.GoldBeamEntity;
import com.reverendinsanity.entity.PhantomImmortalEntity;
import com.reverendinsanity.entity.GuMasterEntity;
import com.reverendinsanity.entity.GuMerchantEntity;
import com.reverendinsanity.entity.LightningWolfEntity;
import com.reverendinsanity.entity.MoonBladeEntity;
import com.reverendinsanity.entity.JadeEyeMonkeyEntity;
import com.reverendinsanity.entity.MountainBoarEntity;
import com.reverendinsanity.entity.AncientGuImmortalEntity;
import com.reverendinsanity.entity.MountainSpiderEntity;
import com.reverendinsanity.entity.StrawPuppetEntity;
import com.reverendinsanity.entity.ThunderCrownWolfEntity;
import com.reverendinsanity.entity.VenerableEntity;
import com.reverendinsanity.entity.WildGuEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// 实体类型注册
public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(Registries.ENTITY_TYPE, ReverendInsanity.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<MoonBladeEntity>> MOON_BLADE =
        ENTITY_TYPES.register("moon_blade", () -> EntityType.Builder.<MoonBladeEntity>of(
                (type, level) -> new MoonBladeEntity(type, level), MobCategory.MISC)
            .sized(0.5f, 0.5f)
            .clientTrackingRange(4)
            .updateInterval(10)
            .build("moon_blade"));

    public static final DeferredHolder<EntityType<?>, EntityType<GoldBeamEntity>> GOLD_BEAM =
        ENTITY_TYPES.register("gold_beam", () -> EntityType.Builder.<GoldBeamEntity>of(
                (type, level) -> new GoldBeamEntity(type, level), MobCategory.MISC)
            .sized(0.3f, 0.3f)
            .clientTrackingRange(4)
            .updateInterval(10)
            .build("gold_beam"));

    public static final DeferredHolder<EntityType<?>, EntityType<WildGuEntity>> WILD_GU =
        ENTITY_TYPES.register("wild_gu", () -> EntityType.Builder.<WildGuEntity>of(
                WildGuEntity::new, MobCategory.AMBIENT)
            .sized(0.4f, 0.4f)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build("wild_gu"));

    public static final DeferredHolder<EntityType<?>, EntityType<GuMasterEntity>> GU_MASTER =
        ENTITY_TYPES.register("gu_master", () -> EntityType.Builder.<GuMasterEntity>of(
                GuMasterEntity::new, MobCategory.MONSTER)
            .sized(0.6f, 1.95f)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build("gu_master"));

    public static final DeferredHolder<EntityType<?>, EntityType<BloodBoltEntity>> BLOOD_BOLT =
        ENTITY_TYPES.register("blood_bolt", () -> EntityType.Builder.<BloodBoltEntity>of(
                (type, level) -> new BloodBoltEntity(type, level), MobCategory.MISC)
            .sized(0.3f, 0.3f)
            .clientTrackingRange(4)
            .updateInterval(10)
            .build("blood_bolt"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.reverendinsanity.entity.IceBoltEntity>> ICE_BOLT =
        ENTITY_TYPES.register("ice_bolt", () -> EntityType.Builder.<com.reverendinsanity.entity.IceBoltEntity>of(
                (type, level) -> new com.reverendinsanity.entity.IceBoltEntity(type, level), MobCategory.MISC)
            .sized(0.3f, 0.3f)
            .clientTrackingRange(4)
            .updateInterval(10)
            .build("ice_bolt"));

    public static final DeferredHolder<EntityType<?>, EntityType<FireBoltEntity>> FIRE_BOLT =
        ENTITY_TYPES.register("fire_bolt", () -> EntityType.Builder.<FireBoltEntity>of(
                (type, level) -> new FireBoltEntity(type, level), MobCategory.MISC)
            .sized(0.3f, 0.3f)
            .clientTrackingRange(4)
            .updateInterval(10)
            .build("fire_bolt"));

    public static final DeferredHolder<EntityType<?>, EntityType<GuMerchantEntity>> GU_MERCHANT =
        ENTITY_TYPES.register("gu_merchant", () -> EntityType.Builder.<GuMerchantEntity>of(
                GuMerchantEntity::new, MobCategory.CREATURE)
            .sized(0.6f, 1.95f)
            .clientTrackingRange(10)
            .updateInterval(3)
            .build("gu_merchant"));

    public static final DeferredHolder<EntityType<?>, EntityType<LightningWolfEntity>> LIGHTNING_WOLF =
        ENTITY_TYPES.register("lightning_wolf", () -> EntityType.Builder.<LightningWolfEntity>of(
                LightningWolfEntity::new, MobCategory.MONSTER)
            .sized(0.6f, 0.85f)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build("lightning_wolf"));

    public static final DeferredHolder<EntityType<?>, EntityType<ThunderCrownWolfEntity>> THUNDER_CROWN_WOLF =
        ENTITY_TYPES.register("thunder_crown_wolf", () -> EntityType.Builder.<ThunderCrownWolfEntity>of(
                ThunderCrownWolfEntity::new, MobCategory.MONSTER)
            .sized(0.9f, 1.2f)
            .clientTrackingRange(10)
            .updateInterval(3)
            .build("thunder_crown_wolf"));

    public static final DeferredHolder<EntityType<?>, EntityType<MountainBoarEntity>> MOUNTAIN_BOAR =
        ENTITY_TYPES.register("mountain_boar", () -> EntityType.Builder.<MountainBoarEntity>of(
                MountainBoarEntity::new, MobCategory.MONSTER)
            .sized(1.2f, 1.0f)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build("mountain_boar"));

    public static final DeferredHolder<EntityType<?>, EntityType<JadeEyeMonkeyEntity>> JADE_EYE_MONKEY =
        ENTITY_TYPES.register("jade_eye_monkey", () -> EntityType.Builder.<JadeEyeMonkeyEntity>of(
                JadeEyeMonkeyEntity::new, MobCategory.MONSTER)
            .sized(0.5f, 0.6f)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build("jade_eye_monkey"));

    public static final DeferredHolder<EntityType<?>, EntityType<StrawPuppetEntity>> STRAW_PUPPET =
        ENTITY_TYPES.register("straw_puppet", () -> EntityType.Builder.<StrawPuppetEntity>of(
                StrawPuppetEntity::new, MobCategory.CREATURE)
            .sized(0.6f, 1.8f)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build("straw_puppet"));

    public static final DeferredHolder<EntityType<?>, EntityType<MountainSpiderEntity>> MOUNTAIN_SPIDER =
        ENTITY_TYPES.register("mountain_spider", () -> EntityType.Builder.<MountainSpiderEntity>of(
                MountainSpiderEntity::new, MobCategory.CREATURE)
            .sized(1.4f, 0.9f)
            .clientTrackingRange(10)
            .updateInterval(3)
            .build("mountain_spider"));

    public static final DeferredHolder<EntityType<?>, EntityType<AncientGuImmortalEntity>> ANCIENT_GU_IMMORTAL =
        ENTITY_TYPES.register("ancient_gu_immortal", () -> EntityType.Builder.<AncientGuImmortalEntity>of(
                AncientGuImmortalEntity::new, MobCategory.MONSTER)
            .sized(0.6f, 1.95f)
            .clientTrackingRange(10)
            .updateInterval(3)
            .build("ancient_gu_immortal"));

    public static final DeferredHolder<EntityType<?>, EntityType<VenerableEntity>> VENERABLE =
        ENTITY_TYPES.register("venerable", () -> EntityType.Builder.<VenerableEntity>of(
                VenerableEntity::new, MobCategory.MONSTER)
            .sized(0.6f, 1.95f)
            .clientTrackingRange(12)
            .updateInterval(3)
            .build("venerable"));

    public static final DeferredHolder<EntityType<?>, EntityType<FormlessHandEntity>> FORMLESS_HAND =
        ENTITY_TYPES.register("formless_hand", () -> EntityType.Builder.<FormlessHandEntity>of(
                FormlessHandEntity::new, MobCategory.MISC)
            .sized(0.6f, 0.8f)
            .clientTrackingRange(8)
            .updateInterval(3)
            .build("formless_hand"));

    public static final DeferredHolder<EntityType<?>, EntityType<PhantomImmortalEntity>> PHANTOM_IMMORTAL =
        ENTITY_TYPES.register("phantom_immortal", () -> EntityType.Builder.<PhantomImmortalEntity>of(
                PhantomImmortalEntity::new, MobCategory.MISC)
            .sized(0.8f, 1.9f)
            .clientTrackingRange(10)
            .updateInterval(3)
            .build("phantom_immortal"));
}
