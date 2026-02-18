package com.reverendinsanity.core.aperture.calamity;

import com.reverendinsanity.core.aperture.BlessedLandGrade;
import com.reverendinsanity.core.aperture.ImmortalAperture;
import com.reverendinsanity.core.path.DaoPath;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import java.util.*;

// 灾劫管理器：调度灾劫周期、生成灾劫、管理活跃灾劫
public class CalamityManager {

    private static final int EARTH_DISASTER_INTERVAL = 3 * 24000;
    private static final int HEAVENLY_TRIBULATION_INTERVAL = 15 * 24000;
    private static final int WARNING_TICKS = 30 * 20;

    private static final Map<UUID, Calamity> activeCalamities = new HashMap<>();
    private static final Map<UUID, Integer> earthDisasterTimers = new HashMap<>();
    private static final Map<UUID, Integer> heavenlyTribulationTimers = new HashMap<>();
    private static final Random random = new Random();

    private static final CalamityType[] EARTH_TYPES = {
        CalamityType.EARTH_CRACK, CalamityType.BEAST_TIDE,
        CalamityType.FIRE_SPREAD, CalamityType.VOID_EROSION
    };

    private static final CalamityType[] HEAVEN_TYPES = {
        CalamityType.THUNDER_TRIBULATION, CalamityType.SILVER_SERPENT,
        CalamityType.CHAOS_STORM
    };

    public static void tick(ServerPlayer player, ImmortalAperture aperture) {
        if (!aperture.isFormed()) return;

        UUID id = player.getUUID();
        Calamity active = activeCalamities.get(id);

        if (active != null) {
            active.tick(player, aperture);
            if (active.isFinished()) {
                onCalamityEnd(player, aperture, active);
                activeCalamities.remove(id);
            }
            return;
        }

        int earthTimer = earthDisasterTimers.getOrDefault(id, 0) + 1;
        int heavenTimer = heavenlyTribulationTimers.getOrDefault(id, 0) + 1;

        if (earthTimer == EARTH_DISASTER_INTERVAL - WARNING_TICKS) {
            player.displayClientMessage(
                Component.literal("[仙窍] ").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal("隐约感到天地灵气紊乱，地灾将至...").withStyle(ChatFormatting.YELLOW)),
                false
            );
            player.playNotifySound(SoundEvents.WARDEN_NEARBY_CLOSER, SoundSource.WEATHER, 0.6f, 0.5f);
        }

        if (heavenTimer == HEAVENLY_TRIBULATION_INTERVAL - WARNING_TICKS) {
            player.displayClientMessage(
                Component.literal("[仙窍] ").withStyle(ChatFormatting.DARK_RED)
                    .append(Component.literal("天空中乌云翻涌，天劫即将降临!").withStyle(ChatFormatting.RED)),
                false
            );
            player.playNotifySound(SoundEvents.WARDEN_ROAR, SoundSource.WEATHER, 0.8f, 0.4f);
        }

        if (earthTimer >= EARTH_DISASTER_INTERVAL) {
            triggerEarthDisaster(player, aperture);
            earthTimer = 0;
        }

        if (heavenTimer >= HEAVENLY_TRIBULATION_INTERVAL) {
            triggerHeavenlyTribulation(player, aperture);
            heavenTimer = 0;
        }

        earthDisasterTimers.put(id, earthTimer);
        heavenlyTribulationTimers.put(id, heavenTimer);
    }

    public static void triggerEarthDisaster(ServerPlayer player, ImmortalAperture aperture) {
        CalamityType type = EARTH_TYPES[random.nextInt(EARTH_TYPES.length)];
        startCalamity(player, aperture, type);
    }

    public static void triggerHeavenlyTribulation(ServerPlayer player, ImmortalAperture aperture) {
        CalamityType type = HEAVEN_TYPES[random.nextInt(HEAVEN_TYPES.length)];
        startCalamity(player, aperture, type);
    }

    public static void triggerSpecialCalamity(ServerPlayer player, ImmortalAperture aperture, CalamityType type) {
        startCalamity(player, aperture, type);
    }

    private static void startCalamity(ServerPlayer player, ImmortalAperture aperture, CalamityType type) {
        float devFactor = 1.0f + (aperture.getDevelopmentLevel() / 100f);
        int baseDuration = type.getScaledDuration(aperture.getGrade());
        int duration = (int)(baseDuration * devFactor);
        Calamity calamity = new Calamity(type, duration);
        activeCalamities.put(player.getUUID(), calamity);
        aperture.resetCalamityTimer();

        ChatFormatting color = type.isHeavenlyTribulation() ? ChatFormatting.DARK_RED : ChatFormatting.GOLD;
        player.displayClientMessage(
            Component.literal("[仙窍] ").withStyle(color)
                .append(Component.literal(type.getCategory().getDisplayName() + "降临: " + type.getDisplayName() + "!").withStyle(color)),
            false
        );
        player.playNotifySound(
            type.isHeavenlyTribulation() ? SoundEvents.WITHER_SPAWN : SoundEvents.GENERIC_EXPLODE.value(),
            SoundSource.WEATHER, 1.0f, 0.5f
        );
    }

    private static void onCalamityEnd(ServerPlayer player, ImmortalAperture aperture, Calamity calamity) {
        float totalDamage = calamity.getFinalDamage();

        aperture.onCalamitySurvived();

        if (random.nextFloat() < 0.3f) {
            aperture.addBreach();
            player.displayClientMessage(
                Component.literal("[仙窍] ").withStyle(ChatFormatting.DARK_PURPLE)
                    .append(Component.literal("灾劫过后，仙窍出现裂缝漏洞!").withStyle(ChatFormatting.LIGHT_PURPLE)),
                false
            );
        }

        if (aperture.getIntegrity() <= 0) {
            player.displayClientMessage(
                Component.literal("[仙窍] ").withStyle(ChatFormatting.DARK_RED)
                    .append(Component.literal("仙窍严重损毁! 品质跌落!").withStyle(ChatFormatting.RED)),
                false
            );
        } else {
            if (calamity.getType().isHeavenlyTribulation()) {
                DaoPath[] paths = DaoPath.values();
                DaoPath rewardPath = paths[random.nextInt(paths.length)];
                int markReward = 50 + random.nextInt(100);
                aperture.addDaoMark(rewardPath, markReward);

                player.displayClientMessage(
                    Component.literal("[仙窍] ").withStyle(ChatFormatting.GREEN)
                        .append(Component.literal("天劫已过! 仙窍获得" + rewardPath.getDisplayName() + "道痕" + markReward + "点").withStyle(ChatFormatting.AQUA)),
                    false
                );
            } else {
                DaoPath[] paths = DaoPath.values();
                DaoPath rewardPath = paths[random.nextInt(paths.length)];
                int markReward = 10 + random.nextInt(30);
                aperture.addDaoMark(rewardPath, markReward);

                player.displayClientMessage(
                    Component.literal("[仙窍] ").withStyle(ChatFormatting.GREEN)
                        .append(Component.literal("地灾已过! 仙窍获得" + rewardPath.getDisplayName() + "道痕" + markReward + "点").withStyle(ChatFormatting.YELLOW)),
                    false
                );
            }
        }

        player.displayClientMessage(
            Component.literal("[仙窍] ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal("仙窍完整度: " + String.format("%.1f", aperture.getIntegrity()) + "% | 总损伤: " + String.format("%.1f", totalDamage)).withStyle(ChatFormatting.GRAY)),
            false
        );
    }

    public static void resistCalamity(UUID playerId, float amount) {
        Calamity calamity = activeCalamities.get(playerId);
        if (calamity != null) {
            calamity.resist(amount);
        }
    }

    public static boolean isInCalamity(UUID playerId) {
        return activeCalamities.containsKey(playerId);
    }

    public static Calamity getActiveCalamity(UUID playerId) {
        return activeCalamities.get(playerId);
    }

    public static void clearPlayer(UUID playerId) {
        activeCalamities.remove(playerId);
        earthDisasterTimers.remove(playerId);
        heavenlyTribulationTimers.remove(playerId);
    }

    public static void savePlayerData(UUID playerId, net.minecraft.nbt.CompoundTag tag) {
        Calamity active = activeCalamities.get(playerId);
        if (active != null && !active.isFinished()) {
            tag.put("activeCalamity", active.save());
        }
        if (earthDisasterTimers.containsKey(playerId)) {
            tag.putInt("earthTimer", earthDisasterTimers.get(playerId));
        }
        if (heavenlyTribulationTimers.containsKey(playerId)) {
            tag.putInt("heavenTimer", heavenlyTribulationTimers.get(playerId));
        }
    }

    public static void loadPlayerData(UUID playerId, net.minecraft.nbt.CompoundTag tag) {
        if (tag.contains("activeCalamity")) {
            Calamity cal = Calamity.load(tag.getCompound("activeCalamity"));
            if (cal != null) {
                activeCalamities.put(playerId, cal);
            }
        }
        if (tag.contains("earthTimer")) {
            earthDisasterTimers.put(playerId, tag.getInt("earthTimer"));
        }
        if (tag.contains("heavenTimer")) {
            heavenlyTribulationTimers.put(playerId, tag.getInt("heavenTimer"));
        }
    }
}
