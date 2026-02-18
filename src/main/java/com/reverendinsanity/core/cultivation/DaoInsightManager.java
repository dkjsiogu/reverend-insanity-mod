package com.reverendinsanity.core.cultivation;

import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// 道悟系统：战斗/修炼中随机触发顿悟，获得临时强化和永久道痕
public class DaoInsightManager {

    private static final Map<UUID, Integer> insightTicks = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> cooldowns = new ConcurrentHashMap<>();

    private static final ResourceLocation INSIGHT_ATTACK = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "dao_insight_atk");
    private static final ResourceLocation INSIGHT_SPEED = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "dao_insight_spd");

    private static final int INSIGHT_DURATION = 200;
    private static final int COOLDOWN = 6000;

    public static void checkForInsight(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (insightTicks.containsKey(uuid)) return;

        Integer cd = cooldowns.get(uuid);
        if (cd != null && cd > 0) return;

        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        if (!data.getAperture().isOpened()) return;

        float chance = 0.003f;
        int totalMarks = data.getTotalDaoMarks();
        if (totalMarks > 500) chance += 0.002f;
        if (totalMarks > 2000) chance += 0.003f;

        if (SeclusionManager.isInSeclusion(player)) chance *= 3f;

        if (player.getRandom().nextFloat() < chance) {
            triggerInsight(player, data);
        }
    }

    private static void triggerInsight(ServerPlayer player, GuMasterData data) {
        UUID uuid = player.getUUID();
        DaoPath primary = data.getAperture().getPrimaryPath();

        int markBonus = 15 + player.getRandom().nextInt(25);
        if (primary != null) {
            data.addDaoMarks(primary, markBonus);
        }

        var atk = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null) {
            atk.removeModifier(INSIGHT_ATTACK);
            atk.addTransientModifier(new AttributeModifier(INSIGHT_ATTACK, 0.4, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        var spd = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (spd != null) {
            spd.removeModifier(INSIGHT_SPEED);
            spd.addTransientModifier(new AttributeModifier(INSIGHT_SPEED, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }

        insightTicks.put(uuid, INSIGHT_DURATION);
        cooldowns.put(uuid, COOLDOWN);

        String pathName = primary != null ? primary.getDisplayName() : "未知";
        player.displayClientMessage(
                Component.literal("【顿悟】" + pathName + "之道真意涌现！道痕 +" + markBonus)
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);

        if (player.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    player.getX(), player.getY() + 1, player.getZ(),
                    40, 1.0, 1.5, 1.0, 0.3);
        }
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.5f);
    }

    public static void tick(ServerPlayer player) {
        UUID uuid = player.getUUID();

        Integer cd = cooldowns.get(uuid);
        if (cd != null) {
            if (cd <= 0) cooldowns.remove(uuid);
            else cooldowns.put(uuid, cd - 1);
        }

        Integer remaining = insightTicks.get(uuid);
        if (remaining == null) return;

        if (remaining <= 0) {
            removeModifiers(player);
            insightTicks.remove(uuid);
            player.displayClientMessage(
                    Component.literal("顿悟结束，真意消散").withStyle(ChatFormatting.GRAY), true);
            return;
        }

        insightTicks.put(uuid, remaining - 1);

        if (player.tickCount % 10 == 0 && player.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.END_ROD,
                    player.getX(), player.getY() + 1.5, player.getZ(),
                    2, 0.3, 0.5, 0.3, 0.02);
        }
    }

    private static void removeModifiers(ServerPlayer player) {
        var atk = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null) atk.removeModifier(INSIGHT_ATTACK);
        var spd = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (spd != null) spd.removeModifier(INSIGHT_SPEED);
    }

    public static void onPlayerDeath(ServerPlayer player) {
        removeModifiers(player);
        insightTicks.remove(player.getUUID());
    }

    public static void onPlayerLogout(ServerPlayer player) {
        removeModifiers(player);
        insightTicks.remove(player.getUUID());
        cooldowns.remove(player.getUUID());
    }
}
