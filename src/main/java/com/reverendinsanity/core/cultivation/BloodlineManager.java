package com.reverendinsanity.core.cultivation;

import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// 血脉系统：玩家开窍时随机获得血脉，提供永久被动加成
public class BloodlineManager {

    public enum Bloodline {
        NONE("无", 0),
        BEAR_STRENGTH("熊力血脉", 1),
        WOLF_SPEED("狼速血脉", 2),
        TURTLE_DEFENSE("龟甲血脉", 3),
        EAGLE_SIGHT("鹰眼血脉", 4),
        SERPENT_VENOM("蛇毒血脉", 5),
        DRAGON_MIGHT("龙威血脉", 6);

        public final String displayName;
        public final int id;

        Bloodline(String name, int id) {
            this.displayName = name;
            this.id = id;
        }

        public static Bloodline fromId(int id) {
            for (Bloodline b : values()) {
                if (b.id == id) return b;
            }
            return NONE;
        }
    }

    private static final Map<UUID, Bloodline> playerBloodlines = new ConcurrentHashMap<>();

    private static final ResourceLocation BLOOD_STRENGTH = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "bloodline_strength");
    private static final ResourceLocation BLOOD_SPEED = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "bloodline_speed");
    private static final ResourceLocation BLOOD_ARMOR = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "bloodline_armor");
    private static final ResourceLocation BLOOD_ATTACK = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "bloodline_attack");

    public static void assignBloodline(ServerPlayer player) {
        Random rng = new Random(player.getUUID().hashCode());
        Bloodline[] options = {Bloodline.BEAR_STRENGTH, Bloodline.WOLF_SPEED, Bloodline.TURTLE_DEFENSE,
                Bloodline.EAGLE_SIGHT, Bloodline.SERPENT_VENOM, Bloodline.DRAGON_MIGHT};

        float roll = rng.nextFloat();
        Bloodline assigned;
        if (roll < 0.05f) {
            assigned = Bloodline.DRAGON_MIGHT;
        } else if (roll < 0.15f) {
            assigned = options[rng.nextInt(options.length - 1)];
        } else {
            assigned = options[rng.nextInt(4)];
        }

        playerBloodlines.put(player.getUUID(), assigned);
        applyBloodlineModifiers(player, assigned);

        player.displayClientMessage(
                Component.literal("血脉觉醒：" + assigned.displayName + "！")
                        .withStyle(assigned == Bloodline.DRAGON_MIGHT ? ChatFormatting.GOLD : ChatFormatting.GREEN), false);
    }

    public static void onPlayerLogin(ServerPlayer player) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        if (!data.getAperture().isOpened()) return;
        int bloodId = data.getBloodlineId();
        if (bloodId > 0) {
            Bloodline bl = Bloodline.fromId(bloodId);
            playerBloodlines.put(player.getUUID(), bl);
            applyBloodlineModifiers(player, bl);
        }
    }

    public static Bloodline getBloodline(ServerPlayer player) {
        return playerBloodlines.getOrDefault(player.getUUID(), Bloodline.NONE);
    }

    public static DaoPath getAffinityPath(Bloodline bloodline) {
        return switch (bloodline) {
            case BEAR_STRENGTH -> DaoPath.STRENGTH;
            case WOLF_SPEED -> DaoPath.WIND;
            case TURTLE_DEFENSE -> DaoPath.EARTH;
            case EAGLE_SIGHT -> DaoPath.LIGHT;
            case SERPENT_VENOM -> DaoPath.POISON;
            case DRAGON_MIGHT -> DaoPath.FIRE;
            default -> null;
        };
    }

    private static void applyBloodlineModifiers(ServerPlayer player, Bloodline bloodline) {
        removeAllModifiers(player);
        switch (bloodline) {
            case BEAR_STRENGTH -> {
                addMod(player, Attributes.ATTACK_DAMAGE, BLOOD_ATTACK, 1.5);
                addMod(player, Attributes.MAX_HEALTH, BLOOD_STRENGTH, 4.0);
            }
            case WOLF_SPEED -> {
                addMod(player, Attributes.MOVEMENT_SPEED, BLOOD_SPEED, 0.02);
                addMod(player, Attributes.ATTACK_SPEED, BLOOD_ATTACK, 0.3);
            }
            case TURTLE_DEFENSE -> {
                addMod(player, Attributes.ARMOR, BLOOD_ARMOR, 4.0);
                addMod(player, Attributes.ARMOR_TOUGHNESS, BLOOD_STRENGTH, 2.0);
            }
            case EAGLE_SIGHT -> {
                addMod(player, Attributes.ATTACK_DAMAGE, BLOOD_ATTACK, 0.8);
                addMod(player, Attributes.ENTITY_INTERACTION_RANGE, BLOOD_SPEED, 1.0);
            }
            case SERPENT_VENOM -> {
                addMod(player, Attributes.ATTACK_DAMAGE, BLOOD_ATTACK, 1.0);
                addMod(player, Attributes.MOVEMENT_SPEED, BLOOD_SPEED, 0.01);
            }
            case DRAGON_MIGHT -> {
                addMod(player, Attributes.ATTACK_DAMAGE, BLOOD_ATTACK, 2.0);
                addMod(player, Attributes.MAX_HEALTH, BLOOD_STRENGTH, 6.0);
                addMod(player, Attributes.ARMOR, BLOOD_ARMOR, 2.0);
                addMod(player, Attributes.MOVEMENT_SPEED, BLOOD_SPEED, 0.01);
            }
            default -> {}
        }
    }

    private static void addMod(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr,
                               ResourceLocation id, double value) {
        var instance = player.getAttribute(attr);
        if (instance != null) {
            instance.removeModifier(id);
            instance.addPermanentModifier(new AttributeModifier(id, value, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static void removeAllModifiers(ServerPlayer player) {
        ResourceLocation[] ids = {BLOOD_STRENGTH, BLOOD_SPEED, BLOOD_ARMOR, BLOOD_ATTACK};
        for (var rl : ids) {
            var speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) speed.removeModifier(rl);
            var atk = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (atk != null) atk.removeModifier(rl);
            var armor = player.getAttribute(Attributes.ARMOR);
            if (armor != null) armor.removeModifier(rl);
            var hp = player.getAttribute(Attributes.MAX_HEALTH);
            if (hp != null) hp.removeModifier(rl);
            var tough = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
            if (tough != null) tough.removeModifier(rl);
            var atkSpd = player.getAttribute(Attributes.ATTACK_SPEED);
            if (atkSpd != null) atkSpd.removeModifier(rl);
            var range = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
            if (range != null) range.removeModifier(rl);
        }
    }

    public static void onPlayerLogout(ServerPlayer player) {
        playerBloodlines.remove(player.getUUID());
    }
}
