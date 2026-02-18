package com.reverendinsanity.core.combat;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// 死亡回放系统：记录最近受到的伤害并在死亡时显示摘要
public class DeathRecapManager {

    private static final Map<UUID, List<DamageRecord>> records = new ConcurrentHashMap<>();
    private static final int MAX_RECORDS = 10;

    public static void recordDamage(ServerPlayer player, DamageSource source, float damage, String moveId) {
        List<DamageRecord> list = records.computeIfAbsent(player.getUUID(), k -> new ArrayList<>());
        String sourceName = getSourceName(source);
        list.add(new DamageRecord(sourceName, damage, moveId, System.currentTimeMillis()));
        if (list.size() > MAX_RECORDS) {
            list.remove(0);
        }
    }

    public static void onDeath(ServerPlayer player) {
        List<DamageRecord> list = records.get(player.getUUID());
        if (list == null || list.isEmpty()) return;

        DamageRecord lastHit = list.get(list.size() - 1);

        StringBuilder msg = new StringBuilder();
        msg.append("\u00a7c[\u6b7b\u4ea1\u56de\u653e] ");
        msg.append("\u00a7f\u88ab \u00a7e").append(lastHit.source).append(" \u00a7f\u51fb\u6740");
        msg.append(" | \u81f4\u547d\u4e00\u51fb: \u00a7c").append(String.format("%.1f", lastHit.damage));
        if (lastHit.moveId != null && !lastHit.moveId.isEmpty()) {
            KillerMove move = KillerMoveRegistry.get(net.minecraft.resources.ResourceLocation.parse(lastHit.moveId));
            String moveName = move != null ? move.displayName() : lastHit.moveId;
            msg.append("\u00a7f | \u6740\u62db: \u00a7b").append(moveName);
        }

        player.sendSystemMessage(Component.literal(msg.toString()));

        float totalDamage = 0;
        for (DamageRecord record : list) {
            totalDamage += record.damage;
        }
        player.sendSystemMessage(Component.literal(
            "\u00a77  \u6700\u8fd1" + list.size() + "\u6b21\u4f24\u5bb3\u603b\u8ba1: \u00a7c" + String.format("%.1f", totalDamage)
        ));

        Map<String, Float> damageBySource = new LinkedHashMap<>();
        for (DamageRecord record : list) {
            damageBySource.merge(record.source, record.damage, Float::sum);
        }
        damageBySource.entrySet().stream()
            .sorted(Map.Entry.<String, Float>comparingByValue().reversed())
            .limit(3)
            .forEach(entry -> player.sendSystemMessage(Component.literal(
                "\u00a77  - \u00a7e" + entry.getKey() + "\u00a77: \u00a7c" + String.format("%.1f", entry.getValue())
            )));

        records.remove(player.getUUID());
    }

    private static String getSourceName(DamageSource source) {
        Entity direct = source.getDirectEntity();
        Entity indirect = source.getEntity();
        if (indirect instanceof LivingEntity living) {
            return living.getName().getString();
        }
        if (direct instanceof LivingEntity living) {
            return living.getName().getString();
        }
        return source.type().msgId();
    }

    public static void clear(UUID playerId) {
        records.remove(playerId);
    }

    public static class DamageRecord {
        public final String source;
        public final float damage;
        public final String moveId;
        public final long timestamp;

        public DamageRecord(String source, float damage, String moveId, long timestamp) {
            this.source = source;
            this.damage = damage;
            this.moveId = moveId;
            this.timestamp = timestamp;
        }
    }
}
