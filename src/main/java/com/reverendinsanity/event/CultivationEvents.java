package com.reverendinsanity.event;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.FortunePlunderManager;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.cultivation.LifespanManager;
import com.reverendinsanity.core.cultivation.SubRank;
import com.reverendinsanity.core.cultivation.TribulationManager;
import com.reverendinsanity.core.clone.CloneManager;
import com.reverendinsanity.core.dream.DreamExplorationManager;
import com.reverendinsanity.core.heavenwill.HeavenWillManager;
import com.reverendinsanity.core.inheritance.InheritanceTrialManager;
import com.reverendinsanity.core.transformation.TransformationManager;
import com.reverendinsanity.core.gu.GuInstance;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.core.gu.GuType;
import com.reverendinsanity.registry.ModAttachments;
import com.reverendinsanity.registry.ModBlocks;
import com.reverendinsanity.registry.ModItems;
import com.reverendinsanity.block.entity.BlessedLandBlockEntity;
import com.reverendinsanity.util.AdvancementHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.joml.Vector3f;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// 修炼事件：蹲下冥想突破小境界
@EventBusSubscriber(modid = ReverendInsanity.MODID)
public class CultivationEvents {

    private static final Map<UUID, Integer> meditationTicks = new HashMap<>();
    private static final int MEDITATION_THRESHOLD = 100;
    private static final int STRUCTURE_CHECK_INTERVAL = 200;
    private static final int STARVATION_WARN_INTERVAL = 600;
    private static final int AUTO_ABSORB_INTERVAL = 100;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        UUID uuid = player.getUUID();
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();

        if (!aperture.isOpened()) return;

        TribulationManager.tick(player);
        DreamExplorationManager.tick(player);
        TransformationManager.tick(player);
        InheritanceTrialManager.tick(player);
        HeavenWillManager.tick(player);
        LifespanManager.tick(player);
        CloneManager.tick(player);
        FortunePlunderManager.tick(player);
        com.reverendinsanity.core.combat.SelfDestructManager.tick(player);
        com.reverendinsanity.core.oath.PoisonOathManager.tick(player);
        com.reverendinsanity.core.cultivation.SeclusionManager.tick(player);
        com.reverendinsanity.core.combat.LifeDeathGateManager.tick(player);
        com.reverendinsanity.core.combat.AmbushManager.tick(player);
        com.reverendinsanity.core.combat.SealManager.tickPlayerCooldown(player);
        com.reverendinsanity.core.cultivation.DaoInsightManager.tick(player);
        com.reverendinsanity.core.cultivation.DaoInsightManager.checkForInsight(player);
        com.reverendinsanity.core.tutorial.TutorialManager.tick(player);
        com.reverendinsanity.core.combat.DefenseManager.tick(player);

        naturalRegeneration(player, aperture);

        if (player.tickCount % AUTO_ABSORB_INTERVAL == 0) {
            autoAbsorbPrimevalStone(player, aperture);
        }

        if (player.tickCount % 40 == 0) {
            spawnRealmParticles(player, aperture);
        }

        if (player.tickCount % STRUCTURE_CHECK_INTERVAL == 0) {
            checkGuCaveStructure(player);
            checkInheritanceGroundStructure(player);
            checkClanSettlementStructure(player);
            checkMoonOrchidCaveStructure(player);
        }

        if (player.tickCount % STARVATION_WARN_INTERVAL == 0) {
            checkGuStarvation(player, aperture);
        }

        boolean isMeditating = player.isCrouching()
                && player.getDeltaMovement().horizontalDistanceSqr() < 0.0001;

        if (!isMeditating) {
            meditationTicks.remove(uuid);
            return;
        }

        int ticks = meditationTicks.getOrDefault(uuid, 0) + 1;

        if (ticks >= MEDITATION_THRESHOLD) {
            meditationTicks.remove(uuid);

            SubRank currentSub = aperture.getSubRank();
            if (currentSub.next() == null) {
                player.displayClientMessage(
                        Component.literal("已达巅峰小境界，需使用突破石进行大境界突破"), true);
                return;
            }

            float cost = aperture.getMaxEssence() * 0.3f;
            if (aperture.getCurrentEssence() < cost) {
                player.displayClientMessage(
                        Component.literal("真元不足，无法突破（需要 " + (int) cost + " 真元）"), true);
                return;
            }

            if (aperture.tryAdvanceSubRank()) {
                String msg = "突破成功！境界提升至 "
                        + aperture.getRank().getDisplayName() + "·"
                        + aperture.getSubRank().getDisplayName();
                player.displayClientMessage(Component.literal(msg), false);

                if (aperture.getRank().getLevel() == 1 && aperture.getSubRank() == SubRank.PEAK) {
                    AdvancementHelper.grant(player, "rank1_peak");
                }

                ServerLevel serverLevel = (ServerLevel) player.level();
                serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        50, 0.5, 1.0, 0.5, 0.2);

                player.level().playSound(null, player.blockPosition(),
                        SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        } else {
            meditationTicks.put(uuid, ticks);
        }
    }

    private static void spawnRealmParticles(ServerPlayer player, Aperture aperture) {
        int rankLevel = aperture.getRank().getLevel();
        if (player.isInvisible()) return;

        ServerLevel level = (ServerLevel) player.level();
        double px = player.getX();
        double py = player.getY() + 1.0;
        double pz = player.getZ();

        boolean meditating = player.isCrouching()
                && player.getDeltaMovement().horizontalDistanceSqr() < 0.0001;

        Vector3f color;
        float size;
        int dustCount;

        if (rankLevel <= 1) {
            color = new Vector3f(0.2f, 0.7f, 0.3f);
            size = 1.0f;
            dustCount = 1;
        } else if (rankLevel == 2) {
            color = new Vector3f(0.8f, 0.2f, 0.2f);
            size = 1.0f;
            dustCount = 1;
        } else if (rankLevel == 3) {
            color = new Vector3f(0.85f, 0.85f, 0.95f);
            size = 1.2f;
            dustCount = 2;
        } else if (rankLevel == 4) {
            color = new Vector3f(1.0f, 0.84f, 0.0f);
            size = 1.5f;
            dustCount = 2;
        } else {
            color = new Vector3f(0.6f, 0.2f, 0.8f);
            size = 1.5f;
            dustCount = 3;
        }

        DustParticleOptions dustOptions = new DustParticleOptions(color, size);
        level.sendParticles(dustOptions, px, py, pz, dustCount, 0.5, 0.5, 0.5, 0.02);

        if (rankLevel >= 3) {
            level.sendParticles(ParticleTypes.ENCHANT, px, py, pz, dustCount, 0.5, 0.5, 0.5, 0.05);
        }
        if (rankLevel >= 4) {
            level.sendParticles(ParticleTypes.END_ROD, px, py, pz, dustCount, 0.3, 0.5, 0.3, 0.02);
        }
        if (rankLevel >= 5) {
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, px, py, pz, 2, 0.3, 0.5, 0.3, 0.01);
        }

        if (meditating) {
            for (int i = 0; i < 4; i++) {
                double angle = (player.tickCount * 0.1 + i * Math.PI / 2) % (2 * Math.PI);
                double radius = 1.2;
                double ex = px + Math.cos(angle) * radius;
                double ez = pz + Math.sin(angle) * radius;
                level.sendParticles(dustOptions, ex, py + 0.5, ez, 1, 0, 0.3, 0, 0.01);
            }
        }
    }

    private static void checkGuCaveStructure(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos pos = player.blockPosition();
        try {
            Structure guCave = level.registryAccess()
                .registryOrThrow(Registries.STRUCTURE)
                .get(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "gu_cave"));
            if (guCave != null) {
                StructureStart start = level.structureManager().getStructureAt(pos, guCave);
                if (start.isValid()) {
                    AdvancementHelper.grant(player, "explore_gu_cave");
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static void checkInheritanceGroundStructure(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos pos = player.blockPosition();
        try {
            Structure inheritance = level.registryAccess()
                .registryOrThrow(Registries.STRUCTURE)
                .get(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "inheritance_ground"));
            if (inheritance != null) {
                StructureStart start = level.structureManager().getStructureAt(pos, inheritance);
                if (start.isValid()) {
                    AdvancementHelper.grant(player, "explore_inheritance_ground");
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static void checkClanSettlementStructure(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos pos = player.blockPosition();
        try {
            Structure clanSettlement = level.registryAccess()
                .registryOrThrow(Registries.STRUCTURE)
                .get(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "clan_settlement"));
            if (clanSettlement != null) {
                StructureStart start = level.structureManager().getStructureAt(pos, clanSettlement);
                if (start.isValid()) {
                    AdvancementHelper.grant(player, "explore_clan_settlement");
                }
            }
        } catch (Exception ignored) {}
    }

    private static void checkMoonOrchidCaveStructure(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos pos = player.blockPosition();
        try {
            Structure moonOrchidCave = level.registryAccess()
                .registryOrThrow(Registries.STRUCTURE)
                .get(ResourceLocation.fromNamespaceAndPath(ReverendInsanity.MODID, "moon_orchid_cave"));
            if (moonOrchidCave != null) {
                StructureStart start = level.structureManager().getStructureAt(pos, moonOrchidCave);
                if (start.isValid()) {
                    AdvancementHelper.grant(player, "explore_moon_orchid_cave");
                }
            }
        } catch (Exception ignored) {}
    }

    private static void naturalRegeneration(ServerPlayer player, Aperture aperture) {
        float aptitudeRatio = aperture.getAptitude().getEssenceRatio();
        boolean isNight = !player.level().isDay();
        float nightBonus = isNight ? 1.5f : 1.0f;

        float spiritSpringMultiplier = 1.0f;
        if (hasBlessedLandNearby(player)) {
            spiritSpringMultiplier = 5.0f;
            AdvancementHelper.grant(player, "create_blessed_land");
        } else if (hasSpiritSpringNearby(player)) {
            spiritSpringMultiplier = 3.0f;
            AdvancementHelper.grant(player, "find_spirit_spring");
        }

        float essenceEventMultiplier = 1.0f;
        float thoughtsEventMultiplier = 1.0f;
        if (com.reverendinsanity.core.event.WorldEventManager.isEventActive(player.level(),
                com.reverendinsanity.core.event.WorldEventType.ESSENCE_STORM)) {
            essenceEventMultiplier = 3.0f;
        }
        if (com.reverendinsanity.core.event.WorldEventManager.isEventActive(player.level(),
                com.reverendinsanity.core.event.WorldEventType.THOUGHTS_CLARITY)) {
            thoughtsEventMultiplier = 3.0f;
        }
        if (com.reverendinsanity.core.event.WorldEventManager.isEventActive(player.level(),
                com.reverendinsanity.core.event.WorldEventType.HEAVEN_WRATH)) {
            essenceEventMultiplier *= 0.5f;
        }

        float essenceRegen = aperture.getMaxEssence() * 0.0005f * aptitudeRatio * nightBonus * spiritSpringMultiplier * essenceEventMultiplier;
        if (aperture.getCurrentEssence() < aperture.getMaxEssence()) {
            aperture.regenerateEssence(essenceRegen);
        }

        float thoughtsRegen = aperture.getMaxThoughts() * 0.0002f * nightBonus * spiritSpringMultiplier * thoughtsEventMultiplier;
        if (aperture.getThoughts() < aperture.getMaxThoughts()) {
            aperture.regenerateThoughts(thoughtsRegen);
        }
    }

    private static boolean hasSpiritSpringNearby(ServerPlayer player) {
        BlockPos center = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-8, -8, -8), center.offset(8, 8, 8))) {
            if (player.level().getBlockState(pos).is(ModBlocks.SPIRIT_SPRING.get())) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasBlessedLandNearby(ServerPlayer player) {
        BlockPos landPos = BlessedLandBlockEntity.getActiveBlessedLandPos(player.getUUID());
        if (landPos == null) return false;
        BlockEntity be = player.level().getBlockEntity(landPos);
        if (be instanceof BlessedLandBlockEntity blessed && blessed.isActive()
                && blessed.isInRange(player.blockPosition())) {
            return true;
        }
        return false;
    }

    private static void autoAbsorbPrimevalStone(ServerPlayer player, Aperture aperture) {
        if (aperture.getCurrentEssence() >= aperture.getMaxEssence() * 0.2f) return;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.PRIMEVAL_STONE.get())) {
                float restoreAmount = aperture.getMaxEssence() * 0.2f;
                aperture.regenerateEssence(restoreAmount);
                stack.shrink(1);
                player.level().playSound(null, player.blockPosition(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5f, 1.5f);
                player.displayClientMessage(
                    Component.literal("元石自动碎裂，恢复 " + (int) restoreAmount + " 真元")
                        .withStyle(ChatFormatting.GREEN), true);
                return;
            }
        }
    }

    private static void checkGuStarvation(ServerPlayer player, Aperture aperture) {
        for (GuInstance gu : aperture.getStoredGu()) {
            if (!gu.isAlive()) {
                GuType type = GuRegistry.get(gu.getTypeId());
                String name = type != null ? type.displayName() : "蛊虫";
                player.displayClientMessage(
                    Component.literal(name + " 已饿死！").withStyle(ChatFormatting.DARK_RED), false);
            } else if (gu.getHunger() < 30f) {
                GuType type = GuRegistry.get(gu.getTypeId());
                String name = type != null ? type.displayName() : "蛊虫";
                player.displayClientMessage(
                    Component.literal(name + " 饥饿警告！(" + String.format("%.0f", gu.getHunger()) + "%)")
                        .withStyle(ChatFormatting.RED), true);
            }
        }
    }
}
