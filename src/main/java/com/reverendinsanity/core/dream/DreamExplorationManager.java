package com.reverendinsanity.core.dream;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.levelgen.Heightmap;
import org.joml.Vector3f;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// 梦境探索系统：梦境外显+靠近卷入+梦中事件
public class DreamExplorationManager {

    private static final Map<UUID, DreamState> activeDreamers = new ConcurrentHashMap<>();
    private static final Map<UUID, DreamManifest> activeManifests = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> pullTimers = new ConcurrentHashMap<>();

    private static final ResourceLocation DREAM_GU_ID = GuRegistry.id("dream_gu");
    private static final ResourceLocation LUCID_DREAM_GU_ID = GuRegistry.id("lucid_dream_gu");
    private static final ResourceLocation NIGHTMARE_GU_ID = GuRegistry.id("nightmare_gu");

    private static final int BASE_DURATION_MIN = 200;
    private static final int BASE_DURATION_MAX = 400;
    private static final int EVENT_INTERVAL = 40;
    private static final float ESSENCE_COST = 20.0f;

    private static final int MANIFEST_SPAWN_INTERVAL = 36000;
    private static final int MANIFEST_DURATION = 6000;
    private static final int MANIFEST_RANGE = 48;
    private static final double PULL_RANGE = 5.0;
    private static final int PULL_TICKS_REQUIRED = 60;
    private static final double PARTICLE_RANGE = 16.0;

    private static final DustParticleOptions DREAM_PURPLE = new DustParticleOptions(
            new Vector3f(0.73f, 0.40f, 1.0f), 1.5f);
    private static final DustParticleOptions DREAM_PINK = new DustParticleOptions(
            new Vector3f(1.0f, 0.53f, 0.80f), 1.2f);

    public static void tick(ServerPlayer player) {
        UUID uuid = player.getUUID();
        DreamState state = activeDreamers.get(uuid);

        if (state != null) {
            tickActiveDream(player, state);
            return;
        }

        tickManifest(player);
        tickProximity(player);
    }

    private static void tickManifest(ServerPlayer player) {
        UUID uuid = player.getUUID();
        DreamManifest manifest = activeManifests.get(uuid);

        if (manifest != null) {
            manifest.remainingTicks--;
            if (manifest.remainingTicks <= 0) {
                activeManifests.remove(uuid);
                pullTimers.remove(uuid);
                return;
            }
            spawnManifestParticles(player, manifest);
            return;
        }

        if (player.tickCount % MANIFEST_SPAWN_INTERVAL == 0 && player.tickCount > 0) {
            trySpawnManifest(player);
        }
    }

    private static void trySpawnManifest(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        double angle = player.getRandom().nextDouble() * Math.PI * 2;
        double dist = 20 + player.getRandom().nextInt(MANIFEST_RANGE - 20);
        int x = (int) (player.getX() + Math.cos(angle) * dist);
        int z = (int) (player.getZ() + Math.sin(angle) * dist);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);

        if (y < level.getMinBuildHeight() + 5) return;

        BlockPos pos = new BlockPos(x, y + 2, z);
        DreamManifest manifest = new DreamManifest(pos, MANIFEST_DURATION);
        activeManifests.put(player.getUUID(), manifest);

        player.displayClientMessage(
                Component.literal("远处出现了朦胧的梦境外显...").withStyle(ChatFormatting.LIGHT_PURPLE),
                false
        );
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.AMBIENT, 0.6f, 0.5f);
    }

    private static void spawnManifestParticles(ServerPlayer player, DreamManifest manifest) {
        ServerLevel level = player.serverLevel();
        BlockPos pos = manifest.pos;
        double dx = player.getX() - pos.getX();
        double dz = player.getZ() - pos.getZ();
        double distSq = dx * dx + dz * dz;

        if (distSq > PARTICLE_RANGE * PARTICLE_RANGE) return;

        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;

        int tick = player.tickCount;

        for (int i = 0; i < 3; i++) {
            double a = (tick * 0.05 + i * Math.PI * 2.0 / 3.0) % (Math.PI * 2);
            double r = 2.0 + Math.sin(tick * 0.02) * 0.5;
            double px = cx + Math.cos(a) * r;
            double pz = cz + Math.sin(a) * r;
            double py = cy + Math.sin(tick * 0.03 + i) * 1.0;
            level.sendParticles(DREAM_PURPLE, px, py, pz, 1, 0.1, 0.2, 0.1, 0.01);
        }

        if (tick % 5 == 0) {
            level.sendParticles(DREAM_PINK,
                    cx + (player.getRandom().nextFloat() - 0.5) * 3,
                    cy + (player.getRandom().nextFloat() - 0.5) * 2,
                    cz + (player.getRandom().nextFloat() - 0.5) * 3,
                    2, 0.3, 0.5, 0.3, 0.02);
            level.sendParticles(ParticleTypes.PORTAL,
                    cx, cy, cz, 3, 1.0, 1.0, 1.0, 0.3);
        }

        if (tick % 20 == 0) {
            level.sendParticles(ParticleTypes.WITCH,
                    cx, cy + 1.0, cz, 5, 1.5, 1.5, 1.5, 0.02);
        }

        if (tick % 80 == 0 && distSq < PARTICLE_RANGE * PARTICLE_RANGE) {
            level.playSound(null, cx, cy, cz,
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.AMBIENT, 0.3f, 0.4f);
        }
    }

    private static void tickProximity(ServerPlayer player) {
        UUID uuid = player.getUUID();
        DreamManifest manifest = activeManifests.get(uuid);
        if (manifest == null) return;

        BlockPos pos = manifest.pos;
        double dx = player.getX() - (pos.getX() + 0.5);
        double dy = player.getY() - (pos.getY() + 0.5);
        double dz = player.getZ() - (pos.getZ() + 0.5);
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (dist > PULL_RANGE) {
            pullTimers.remove(uuid);
            return;
        }

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        if (!aperture.isOpened()) return;

        int timer = pullTimers.getOrDefault(uuid, 0) + 1;
        pullTimers.put(uuid, timer);

        if (timer % 20 == 0) {
            player.displayClientMessage(
                    Component.literal("梦境如烟，轻轻一卷...").withStyle(ChatFormatting.LIGHT_PURPLE),
                    true
            );
        }

        ServerLevel level = player.serverLevel();
        if (timer % 10 == 0) {
            double angle = (timer * 0.15) % (Math.PI * 2);
            double r = PULL_RANGE * (1.0 - (double) timer / PULL_TICKS_REQUIRED);
            double px = player.getX() + Math.cos(angle) * r;
            double pz = player.getZ() + Math.sin(angle) * r;
            level.sendParticles(DREAM_PURPLE, px, player.getEyeY(), pz, 3, 0.2, 0.3, 0.2, 0.01);
        }

        if (timer >= PULL_TICKS_REQUIRED) {
            pullTimers.remove(uuid);
            enterDreamFromManifest(player, manifest);
        }
    }

    private static void enterDreamFromManifest(ServerPlayer player, DreamManifest manifest) {
        UUID uuid = player.getUUID();
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();

        boolean hasDreamGu = aperture.findGuInstance(DREAM_GU_ID) != null;
        float cost = hasDreamGu ? ESSENCE_COST : ESSENCE_COST * 2;

        if (aperture.getCurrentEssence() < cost) {
            player.displayClientMessage(
                    Component.literal("真元不足，被梦境弹出...").withStyle(ChatFormatting.RED),
                    false
            );
            activeManifests.remove(uuid);
            return;
        }

        aperture.consumeEssence(cost);
        activeManifests.remove(uuid);

        DreamState state = new DreamState();
        state.active = true;
        state.startTick = player.tickCount;
        state.hasLucidDream = aperture.findGuInstance(LUCID_DREAM_GU_ID) != null;
        state.hasNightmare = aperture.findGuInstance(NIGHTMARE_GU_ID) != null;
        state.hasDreamGu = hasDreamGu;
        state.entryPos = player.blockPosition();

        int duration = BASE_DURATION_MIN + player.getRandom().nextInt(BASE_DURATION_MAX - BASE_DURATION_MIN);
        if (state.hasLucidDream) {
            duration = (int) (duration * 1.5f);
        }
        if (!hasDreamGu) {
            duration = (int) (duration * 0.6f);
        }
        state.durationTicks = duration;
        state.lastEventTick = player.tickCount;

        activeDreamers.put(uuid, state);

        player.displayClientMessage(
                Component.literal("被梦境卷入，意识沉入梦中...").withStyle(ChatFormatting.LIGHT_PURPLE),
                false
        );
        ServerLevel level = player.serverLevel();
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.PLAYERS, 0.8f, 0.5f);

        VfxHelper.spawn(player, VfxType.AURA_RING,
                player.getX(), player.getY() + 0.5, player.getZ(),
                0, 1, 0, 0xFFCC88FF, 2.5f, 60);
        VfxHelper.spawn(player, VfxType.DOME_FIELD,
                player.getX(), player.getY(), player.getZ(),
                0, 1, 0, 0xFFBB66FF, 3.0f, 40);
    }

    public static void startDream(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (activeDreamers.containsKey(uuid)) return;

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        if (!aperture.isOpened()) return;
        if (aperture.getCurrentEssence() < ESSENCE_COST) return;

        aperture.consumeEssence(ESSENCE_COST);

        DreamState state = new DreamState();
        state.active = true;
        state.startTick = player.tickCount;
        state.hasLucidDream = aperture.findGuInstance(LUCID_DREAM_GU_ID) != null;
        state.hasNightmare = aperture.findGuInstance(NIGHTMARE_GU_ID) != null;
        state.hasDreamGu = aperture.findGuInstance(DREAM_GU_ID) != null;
        state.entryPos = player.blockPosition();

        int duration = BASE_DURATION_MIN + player.getRandom().nextInt(BASE_DURATION_MAX - BASE_DURATION_MIN);
        if (state.hasLucidDream) duration = (int) (duration * 1.5f);
        state.durationTicks = duration;
        state.lastEventTick = player.tickCount;

        activeDreamers.put(uuid, state);

        player.displayClientMessage(
                Component.literal("意识沉入梦境...").withStyle(ChatFormatting.LIGHT_PURPLE),
                false
        );
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.PLAYERS, 0.8f, 0.5f);
        VfxHelper.spawn(player, VfxType.AURA_RING,
                player.getX(), player.getY() + 0.5, player.getZ(),
                0, 1, 0, 0xFFCC88FF, 2.5f, 60);
    }

    public static void onDamaged(ServerPlayer player) {
        DreamState state = activeDreamers.get(player.getUUID());
        if (state != null && state.active) {
            exitDream(player, true);
        }
    }

    private static void tickActiveDream(ServerPlayer player, DreamState state) {
        if (!state.active) return;

        int elapsed = player.tickCount - state.startTick;

        if (elapsed >= state.durationTicks) {
            exitDream(player, false);
            return;
        }

        if (player.getDeltaMovement().horizontalDistanceSqr() > 0.01) {
            exitDream(player, true);
            return;
        }

        if (elapsed % 10 == 0) {
            ServerLevel level = (ServerLevel) player.level();
            double angle = (elapsed * 0.1) % (Math.PI * 2);
            double px = player.getX() + Math.cos(angle) * 1.5;
            double pz = player.getZ() + Math.sin(angle) * 1.5;
            level.sendParticles(ParticleTypes.WITCH,
                    px, player.getEyeY() + 0.5, pz,
                    3, 0.2, 0.3, 0.2, 0.01);
            level.sendParticles(ParticleTypes.PORTAL,
                    player.getX(), player.getEyeY(), player.getZ(),
                    2, 0.3, 0.5, 0.3, 0.5);
        }

        if (player.tickCount - state.lastEventTick >= EVENT_INTERVAL) {
            state.lastEventTick = player.tickCount;
            processDreamEvent(player, state);
        }
    }

    private static void processDreamEvent(ServerPlayer player, DreamState state) {
        float roll = player.getRandom().nextFloat();
        DreamEvent event = rollDreamEvent(roll, state);

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();

        float riskMultiplier = state.hasDreamGu ? 1.0f : 1.5f;

        switch (event) {
            case LEGACY_KNOWLEDGE -> {
                DaoPath[] paths = DaoPath.values();
                DaoPath randomPath = paths[player.getRandom().nextInt(paths.length)];
                int amount = 5 + player.getRandom().nextInt(11);
                if (state.hasNightmare) {
                    amount *= 2;
                }
                data.addDaoMarks(randomPath, amount);
                player.displayClientMessage(
                        Component.literal("梦中见证古老传承..." + randomPath.getDisplayName() + " 道痕+" + amount)
                                .withStyle(ChatFormatting.AQUA),
                        false
                );
                spawnEventParticles(player, ParticleTypes.ENCHANT);
            }
            case ESSENCE_RECOVERY -> {
                float recovered = aperture.getMaxEssence() * 0.3f;
                aperture.regenerateEssence(recovered);
                player.displayClientMessage(
                        Component.literal("梦境中真元缓缓恢复...+" + (int) recovered)
                                .withStyle(ChatFormatting.GREEN),
                        false
                );
                spawnEventParticles(player, ParticleTypes.HAPPY_VILLAGER);
            }
            case RECIPE_VISION -> {
                String[] recipes = {
                        "梦境中隐约看见：月光蛊可用月兰花瓣在月光下炼化...",
                        "恍惚间感悟：铁骨蛊需以兽骨和元石为引...",
                        "梦中有声音低语：四味酒虫需集齐四种美酒...",
                        "传承记忆涌来：高阶蛊虫需要对应道路的材料...",
                        "古蛊师的记忆：融合同道路蛊虫可引发道共鸣...",
                        "朦胧中的启示：杀招推演需要足够的道痕积累..."
                };
                String recipe = recipes[player.getRandom().nextInt(recipes.length)];
                player.displayClientMessage(
                        Component.literal(recipe).withStyle(ChatFormatting.GOLD),
                        false
                );
                spawnEventParticles(player, ParticleTypes.INSTANT_EFFECT);
            }
            case NIGHTMARE -> {
                float damage = player.getMaxHealth() * 0.1f * riskMultiplier;
                if (state.hasNightmare) {
                    damage *= 0.5f;
                }
                player.hurt(player.damageSources().magic(), damage);
                player.displayClientMessage(
                        Component.literal("噩梦侵袭！精神受到冲击！")
                                .withStyle(ChatFormatting.DARK_RED),
                        false
                );
                ServerLevel level = (ServerLevel) player.level();
                level.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                        player.getX(), player.getEyeY(), player.getZ(),
                        8, 0.5, 0.5, 0.5, 0.1);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 0.3f, 0.4f);
            }
            case DAO_INSIGHT -> {
                DaoPath primaryPath = aperture.getPrimaryPath();
                if (primaryPath == null) {
                    primaryPath = aperture.getDominantPath();
                }
                if (primaryPath == null) {
                    primaryPath = DaoPath.DREAM;
                }
                data.addDaoMarks(primaryPath, 25);
                player.displayClientMessage(
                        Component.literal("顿悟！对" + primaryPath.getDisplayName() + "的理解加深！道痕+25")
                                .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD),
                        false
                );
                VfxHelper.spawn(player, VfxType.GLOW_BURST,
                        player.getX(), player.getEyeY(), player.getZ(),
                        0, 1, 0, 0xFFFFDD44, 3.0f, 40);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6f, 1.5f);
            }
        }
    }

    private static DreamEvent rollDreamEvent(float roll, DreamState state) {
        float legacyChance;
        float essenceChance;
        float recipeChance;
        float nightmareChance;

        if (state.hasNightmare) {
            legacyChance = 0.35f;
            essenceChance = 0.15f;
            recipeChance = 0.05f;
            nightmareChance = 0.35f;
        } else if (state.hasLucidDream) {
            legacyChance = 0.35f;
            essenceChance = 0.25f;
            recipeChance = 0.20f;
            nightmareChance = 0.10f;
        } else {
            legacyChance = 0.30f;
            essenceChance = 0.25f;
            recipeChance = 0.15f;
            nightmareChance = 0.20f;
        }

        if (!state.hasDreamGu) {
            nightmareChance += 0.10f;
            legacyChance -= 0.05f;
            essenceChance -= 0.05f;
        }

        if (roll < legacyChance) return DreamEvent.LEGACY_KNOWLEDGE;
        roll -= legacyChance;
        if (roll < essenceChance) return DreamEvent.ESSENCE_RECOVERY;
        roll -= essenceChance;
        if (roll < recipeChance) return DreamEvent.RECIPE_VISION;
        roll -= recipeChance;
        if (roll < nightmareChance) return DreamEvent.NIGHTMARE;
        return DreamEvent.DAO_INSIGHT;
    }

    private static void exitDream(ServerPlayer player, boolean interrupted) {
        activeDreamers.remove(player.getUUID());

        if (interrupted) {
            player.displayClientMessage(
                    Component.literal("梦境被打断！意识强制回归！")
                            .withStyle(ChatFormatting.RED),
                    false
            );
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.5f, 0.8f);
        } else {
            player.displayClientMessage(
                    Component.literal("从梦境中安全醒来。")
                            .withStyle(ChatFormatting.LIGHT_PURPLE),
                    false
            );
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.5f, 1.2f);
        }

        VfxHelper.spawn(player, VfxType.RIPPLE,
                player.getX(), player.getY() + 0.5, player.getZ(),
                0, 1, 0, 0xFFAA66FF, 2.0f, 30);
    }

    private static void spawnEventParticles(ServerPlayer player, net.minecraft.core.particles.SimpleParticleType type) {
        ServerLevel level = (ServerLevel) player.level();
        level.sendParticles(type,
                player.getX(), player.getEyeY() + 0.5, player.getZ(),
                15, 0.8, 0.8, 0.8, 0.05);
    }

    public static boolean isDreaming(ServerPlayer player) {
        DreamState state = activeDreamers.get(player.getUUID());
        return state != null && state.active;
    }

    public static boolean hasManifest(ServerPlayer player) {
        return activeManifests.containsKey(player.getUUID());
    }

    public static BlockPos getManifestPos(ServerPlayer player) {
        DreamManifest manifest = activeManifests.get(player.getUUID());
        return manifest != null ? manifest.pos : null;
    }

    public static void forceSpawnManifest(ServerPlayer player) {
        trySpawnManifest(player);
    }

    public static void onPlayerLogout(ServerPlayer player) {
        UUID uuid = player.getUUID();
        activeDreamers.remove(uuid);
        activeManifests.remove(uuid);
        pullTimers.remove(uuid);
    }

    public enum DreamEvent {
        LEGACY_KNOWLEDGE,
        ESSENCE_RECOVERY,
        RECIPE_VISION,
        NIGHTMARE,
        DAO_INSIGHT
    }

    private static class DreamState {
        boolean active;
        int startTick;
        int durationTicks;
        int lastEventTick;
        boolean hasLucidDream;
        boolean hasNightmare;
        boolean hasDreamGu;
        BlockPos entryPos;
    }

    private static class DreamManifest {
        final BlockPos pos;
        int remainingTicks;

        DreamManifest(BlockPos pos, int durationTicks) {
            this.pos = pos;
            this.remainingTicks = durationTicks;
        }
    }
}
