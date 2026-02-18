package com.reverendinsanity.core.oath;

import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// 毒誓系统：蛊师可以立下毒誓获得短期增益，违背则受到严厉惩罚
public class PoisonOathManager {

    public enum OathType {
        KILL_VOW("杀伐毒誓", 1200, 0.3f, "在限时内击杀目标"),
        PROTECTION_VOW("守护毒誓", 2400, 0.2f, "保持满HP持续时间"),
        ASCETIC_VOW("苦修毒誓", 6000, 0.15f, "不使用任何杀招");

        public final String displayName;
        public final int duration;
        public final float bonus;
        public final String description;

        OathType(String name, int dur, float bonus, String desc) {
            this.displayName = name;
            this.duration = dur;
            this.bonus = bonus;
            this.description = desc;
        }
    }

    private static final Map<UUID, ActiveOath> activeOaths = new ConcurrentHashMap<>();

    private static final ResourceLocation OATH_ATTACK = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "oath_attack");
    private static final ResourceLocation OATH_SPEED = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "oath_speed");

    public static boolean makeOath(ServerPlayer player, OathType type) {
        UUID uuid = player.getUUID();
        if (activeOaths.containsKey(uuid)) {
            player.displayClientMessage(Component.literal("已有毒誓生效中").withStyle(ChatFormatting.RED), true);
            return false;
        }

        activeOaths.put(uuid, new ActiveOath(type, type.duration, false));
        applyBonus(player, type);

        player.displayClientMessage(
                Component.literal("立下" + type.displayName + "！" + type.description + " (" + (type.duration / 20) + "秒)")
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), false);
        return true;
    }

    public static void tick(ServerPlayer player) {
        UUID uuid = player.getUUID();
        ActiveOath oath = activeOaths.get(uuid);
        if (oath == null) return;

        oath.remaining--;

        if (oath.type == OathType.PROTECTION_VOW && player.getHealth() < player.getMaxHealth()) {
            oath.violated = true;
        }

        if (oath.remaining <= 0) {
            if (oath.violated) {
                punish(player, oath.type);
            } else {
                reward(player, oath.type);
            }
            removeBonus(player);
            activeOaths.remove(uuid);
        }
    }

    public static void onKillerMoveUsed(ServerPlayer player) {
        UUID uuid = player.getUUID();
        ActiveOath oath = activeOaths.get(uuid);
        if (oath != null && oath.type == OathType.ASCETIC_VOW) {
            oath.violated = true;
            player.displayClientMessage(
                    Component.literal("苦修毒誓违背！使用了杀招！").withStyle(ChatFormatting.RED), false);
        }
    }

    public static void onKillEntity(ServerPlayer player) {
        UUID uuid = player.getUUID();
        ActiveOath oath = activeOaths.get(uuid);
        if (oath != null && oath.type == OathType.KILL_VOW) {
            reward(player, oath.type);
            removeBonus(player);
            activeOaths.remove(uuid);
            player.displayClientMessage(
                    Component.literal("杀伐毒誓完成！").withStyle(ChatFormatting.GREEN), false);
        }
    }

    private static void reward(ServerPlayer player, OathType type) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        data.getAperture().regenerateEssence(data.getAperture().getMaxEssence() * 0.2f);
        data.setLuck(data.getLuck() + 0.05f);

        player.displayClientMessage(
                Component.literal(type.displayName + "履行完毕！真元恢复20%，气运微升")
                        .withStyle(ChatFormatting.GREEN), false);
    }

    private static void punish(ServerPlayer player, OathType type) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        float punishDamage = player.getMaxHealth() * 0.3f;
        player.hurt(player.damageSources().magic(), punishDamage);
        data.getAperture().consumeEssence(data.getAperture().getMaxEssence() * 0.5f);
        data.setLuck(data.getLuck() - 0.1f);
        data.consumeLifespan(50);

        player.displayClientMessage(
                Component.literal("【毒誓反噬】" + type.displayName + "违背！受到惩罚：生命-30%、真元-50%、气运降低、寿元-50")
                        .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
    }

    private static void applyBonus(ServerPlayer player, OathType type) {
        var atk = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null) {
            atk.removeModifier(OATH_ATTACK);
            atk.addTransientModifier(new AttributeModifier(OATH_ATTACK, type.bonus, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        var speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(OATH_SPEED);
            speed.addTransientModifier(new AttributeModifier(OATH_SPEED, type.bonus * 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    private static void removeBonus(ServerPlayer player) {
        var atk = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (atk != null) atk.removeModifier(OATH_ATTACK);
        var speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) speed.removeModifier(OATH_SPEED);
    }

    public static boolean hasActiveOath(ServerPlayer player) {
        return activeOaths.containsKey(player.getUUID());
    }

    public static void onPlayerDeath(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (activeOaths.containsKey(uuid)) {
            removeBonus(player);
            activeOaths.remove(uuid);
        }
    }

    public static void onPlayerLogout(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (activeOaths.containsKey(uuid)) {
            removeBonus(player);
            activeOaths.remove(uuid);
        }
    }

    private static class ActiveOath {
        OathType type;
        int remaining;
        boolean violated;

        ActiveOath(OathType type, int remaining, boolean violated) {
            this.type = type;
            this.remaining = remaining;
            this.violated = violated;
        }
    }
}
