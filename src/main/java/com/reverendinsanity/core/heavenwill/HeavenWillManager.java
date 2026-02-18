package com.reverendinsanity.core.heavenwill;

import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.core.faction.Faction;
import com.reverendinsanity.core.faction.FactionReputation;
import com.reverendinsanity.registry.ModAttachments;
import com.reverendinsanity.block.entity.BlessedLandBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// 天意系统：天意主动压制蛊师，境界越高、道痕越多、越逆天，天意关注度越高
public class HeavenWillManager {

    private static final Map<UUID, Float> attentionLevels = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> suppressionTicks = new ConcurrentHashMap<>();

    private static final ResourceLocation SUPPRESS_SPEED = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "heaven_will_slow");
    private static final ResourceLocation SUPPRESS_ATTACK = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "heaven_will_weak");

    private static final int GROWTH_INTERVAL = 200;
    private static final int EFFECT_INTERVAL = 400;

    public static void tick(ServerPlayer player) {
        UUID uuid = player.getUUID();
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        if (!aperture.isOpened()) return;

        Integer suppTicks = suppressionTicks.get(uuid);
        if (suppTicks != null) {
            if (suppTicks <= 0) {
                removeSuppression(player);
                suppressionTicks.remove(uuid);
            } else {
                suppressionTicks.put(uuid, suppTicks - 1);
            }
        }

        if (player.tickCount % GROWTH_INTERVAL == 0) {
            tickAttention(player, data, aperture);
        }

        if (player.tickCount % EFFECT_INTERVAL == 0) {
            tickEffects(player, data, aperture);
        }
    }

    public static void addAttention(ServerPlayer player, float amount) {
        UUID uuid = player.getUUID();
        float current = attentionLevels.getOrDefault(uuid, 0f);
        attentionLevels.put(uuid, Math.min(100f, current + amount));
    }

    public static float getAttention(ServerPlayer player) {
        return attentionLevels.getOrDefault(player.getUUID(), 0f);
    }

    public static void onPlayerDeath(ServerPlayer player) {
        UUID uuid = player.getUUID();
        Float current = attentionLevels.get(uuid);
        if (current != null) {
            attentionLevels.put(uuid, current * 0.5f);
        }
        removeSuppression(player);
        suppressionTicks.remove(uuid);
    }

    public static void onPlayerLogout(ServerPlayer player) {
        UUID uuid = player.getUUID();
        removeSuppression(player);
        suppressionTicks.remove(uuid);
    }

    private static void tickAttention(ServerPlayer player, GuMasterData data, Aperture aperture) {
        UUID uuid = player.getUUID();
        float current = attentionLevels.getOrDefault(uuid, 0f);
        int rank = aperture.getRank().getLevel();

        float growth = switch (rank) {
            case 1 -> 0f;
            case 2 -> 0.1f;
            case 3 -> 0.3f;
            case 4 -> 0.5f;
            default -> 1.0f;
        };

        int totalMarks = 0;
        for (var entry : data.getAllDaoMarks().entrySet()) {
            totalMarks += entry.getValue();
        }
        growth += totalMarks / 100f * 0.05f;

        FactionReputation rep = data.getFactionReputation();
        if (rep.getReputation(Faction.DEMONIC) > rep.getReputation(Faction.RIGHTEOUS)) {
            growth += 0.2f;
        }

        float decay = 0.15f;
        if (player.blockPosition().getY() < 40) {
            decay += 0.1f;
        }
        if (isInBlessedLand(player)) {
            decay += 0.2f;
        }
        if (rep.getReputation(Faction.RIGHTEOUS) > rep.getReputation(Faction.DEMONIC)) {
            decay += 0.05f;
        }

        float newAttention = Math.max(0f, Math.min(100f, current + growth - decay));
        attentionLevels.put(uuid, newAttention);

        if (newAttention >= 25f && current < 25f) {
            player.displayClientMessage(
                    Component.literal("隐约感到天地间有一股意志在注视着你...")
                            .withStyle(ChatFormatting.GRAY), true);
        } else if (newAttention >= 50f && current < 50f) {
            player.displayClientMessage(
                    Component.literal("天意关注加剧，真元不稳...")
                            .withStyle(ChatFormatting.YELLOW), false);
        } else if (newAttention >= 75f && current < 75f) {
            player.displayClientMessage(
                    Component.literal("【天意降临】大地震颤，天雷将至！")
                            .withStyle(ChatFormatting.RED), false);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 0.4f, 0.5f);
        }
    }

    private static void tickEffects(ServerPlayer player, GuMasterData data, Aperture aperture) {
        float attention = attentionLevels.getOrDefault(player.getUUID(), 0f);
        if (attention < 25f) return;

        float roll = player.getRandom().nextFloat();

        if (attention >= 90f && roll < 0.10f) {
            executeHeavenPunishment(player, aperture);
            return;
        }

        if (attention >= 75f && roll < 0.20f) {
            applySuppression(player);
            player.displayClientMessage(
                    Component.literal("天意压制降临，行动受阻！")
                            .withStyle(ChatFormatting.RED), true);
            return;
        }

        if (attention >= 50f && roll < 0.15f && player.blockPosition().getY() > 60 && player.level().canSeeSky(player.blockPosition())) {
            ServerLevel level = player.serverLevel();
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt != null) {
                bolt.moveTo(player.getX(), player.getY(), player.getZ());
                bolt.setVisualOnly(false);
                level.addFreshEntity(bolt);
                player.displayClientMessage(
                        Component.literal("天雷降下！").withStyle(ChatFormatting.YELLOW), true);
            }
            return;
        }

        if (attention >= 25f && roll < 0.10f) {
            float drain = aperture.getMaxEssence() * 0.05f;
            aperture.consumeEssence(drain);
            player.displayClientMessage(
                    Component.literal("真元受天意干扰，流失 " + (int) drain)
                            .withStyle(ChatFormatting.GRAY), true);
        }
    }

    private static void executeHeavenPunishment(ServerPlayer player, Aperture aperture) {
        ServerLevel level = player.serverLevel();

        player.displayClientMessage(
                Component.literal("【天意降罚】逆天之蛊师，受死！")
                        .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 1.0f, 0.3f);

        for (int i = 0; i < 5; i++) {
            double ox = player.getX() + (player.getRandom().nextFloat() - 0.5) * 6;
            double oz = player.getZ() + (player.getRandom().nextFloat() - 0.5) * 6;
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt != null) {
                bolt.moveTo(ox, player.getY(), oz);
                bolt.setVisualOnly(false);
                level.addFreshEntity(bolt);
            }
        }

        aperture.setCurrentEssence(0);
        applySuppression(player);
        suppressionTicks.put(player.getUUID(), 60);

        attentionLevels.put(player.getUUID(), 50f);
    }

    private static void applySuppression(ServerPlayer player) {
        var speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(SUPPRESS_SPEED);
            speed.addTransientModifier(new AttributeModifier(SUPPRESS_SPEED, -0.3, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        var attack = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) {
            attack.removeModifier(SUPPRESS_ATTACK);
            attack.addTransientModifier(new AttributeModifier(SUPPRESS_ATTACK, -0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        UUID uuid = player.getUUID();
        if (!suppressionTicks.containsKey(uuid)) {
            suppressionTicks.put(uuid, 100);
        }
    }

    private static void removeSuppression(ServerPlayer player) {
        var speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) speed.removeModifier(SUPPRESS_SPEED);
        var attack = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) attack.removeModifier(SUPPRESS_ATTACK);
    }

    private static boolean isInBlessedLand(ServerPlayer player) {
        BlockPos landPos = BlessedLandBlockEntity.getActiveBlessedLandPos(player.getUUID());
        if (landPos == null) return false;
        BlockEntity be = player.level().getBlockEntity(landPos);
        return be instanceof BlessedLandBlockEntity blessed && blessed.isActive() && blessed.isInRange(player.blockPosition());
    }
}
