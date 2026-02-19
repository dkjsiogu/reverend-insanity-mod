package com.reverendinsanity.core.combat;

import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.GuMasterData;
import com.reverendinsanity.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// 陷阱系统：在地面放置隐形陷阱，敌人踩中触发伤害+减速
public class TrapManager {

    private static final Map<UUID, List<Trap>> playerTraps = new ConcurrentHashMap<>();
    private static final int MAX_TRAPS = 5;

    public static boolean placeTrap(ServerPlayer player) {
        UUID uuid = player.getUUID();
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();
        if (!aperture.isOpened()) return false;

        float cost = 20f + aperture.getRank().getLevel() * 10f;
        if (!aperture.consumeEssence(cost)) {
            player.displayClientMessage(Component.literal("真元不足").withStyle(ChatFormatting.RED), true);
            return false;
        }

        List<Trap> traps = playerTraps.computeIfAbsent(uuid, k -> new ArrayList<>());
        if (traps.size() >= MAX_TRAPS) {
            traps.remove(0);
        }

        BlockPos pos = player.blockPosition();
        int rank = aperture.getRank().getLevel();
        float damage = 8f + rank * 6f;
        float radius = 2f + rank * 0.5f;

        traps.add(new Trap(pos, damage, radius, 6000));

        player.displayClientMessage(
                Component.literal("陷阱布置！(" + traps.size() + "/" + MAX_TRAPS + ")")
                        .withStyle(ChatFormatting.DARK_GREEN), true);

        player.level().playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.5f, 1.5f);

        return true;
    }

    public static void tickServer(ServerLevel level) {
        for (var entry : playerTraps.entrySet()) {
            List<Trap> traps = entry.getValue();
            Iterator<Trap> it = traps.iterator();
            while (it.hasNext()) {
                Trap trap = it.next();
                trap.lifetime--;

                if (trap.lifetime <= 0) {
                    it.remove();
                    continue;
                }

                if (trap.lifetime % 40 == 0) {
                    level.sendParticles(
                            new DustParticleOptions(new Vector3f(0.2f, 0.8f, 0.2f), 0.5f),
                            trap.pos.getX() + 0.5, trap.pos.getY() + 0.1, trap.pos.getZ() + 0.5,
                            1, 0.1, 0, 0.1, 0);
                }

                AABB box = new AABB(trap.pos).inflate(trap.radius);
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box,
                        e -> !(e instanceof ServerPlayer p && p.getUUID().equals(entry.getKey())) && e.isAlive());

                if (!targets.isEmpty()) {
                    triggerTrap(level, trap, targets, entry.getKey());
                    it.remove();
                }
            }
        }
        playerTraps.values().removeIf(List::isEmpty);
    }

    private static void triggerTrap(ServerLevel level, Trap trap, List<LivingEntity> targets, UUID owner) {
        level.playSound(null, trap.pos.getX(), trap.pos.getY(), trap.pos.getZ(),
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.8f, 1.2f);

        level.sendParticles(
                new DustParticleOptions(new Vector3f(1.0f, 0.3f, 0.0f), 2.0f),
                trap.pos.getX() + 0.5, trap.pos.getY() + 0.5, trap.pos.getZ() + 0.5,
                20, trap.radius * 0.3, 0.5, trap.radius * 0.3, 0.1);

        ServerPlayer ownerPlayer = level.getServer().getPlayerList().getPlayer(owner);

        for (LivingEntity target : targets) {
            if (ownerPlayer != null) {
                target.hurt(ownerPlayer.damageSources().magic(), trap.damage);
            } else {
                target.hurt(level.damageSources().magic(), trap.damage);
            }
        }

        if (ownerPlayer != null) {
            ownerPlayer.displayClientMessage(
                    Component.literal("陷阱触发！命中 " + targets.size() + " 个目标")
                            .withStyle(ChatFormatting.GOLD), true);
        }
    }

    public static void onPlayerLogout(ServerPlayer player) {
        playerTraps.remove(player.getUUID());
    }

    public static void clearAll() {
        playerTraps.clear();
    }

    private static class Trap {
        BlockPos pos;
        float damage;
        float radius;
        int lifetime;

        Trap(BlockPos pos, float damage, float radius, int lifetime) {
            this.pos = pos;
            this.damage = damage;
            this.radius = radius;
            this.lifetime = lifetime;
        }
    }
}
