package com.reverendinsanity.core.event;

import com.reverendinsanity.core.combat.MeritManager;
import com.reverendinsanity.entity.LightningWolfEntity;
import com.reverendinsanity.entity.MountainBoarEntity;
import com.reverendinsanity.entity.ThunderCrownWolfEntity;
import com.reverendinsanity.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;

// 兽潮事件逻辑：分5波在玩家附近生成野兽群，后期出精英和百兽王BOSS
public class BeastTideEvent {

    private static final int TOTAL_WAVES = 5;
    private static final int WAVE_INTERVAL = 600;
    private static final ResourceLocation ELITE_HP_MOD = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "beast_tide_elite_hp");
    private static final ResourceLocation ELITE_SPEED_MOD = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "beast_tide_elite_speed");

    private int ticksSinceLastWave = 0;
    private int currentWave = 0;
    private boolean finished = false;

    public boolean tick(ServerLevel level) {
        if (finished) return true;

        ticksSinceLastWave++;
        if (ticksSinceLastWave < WAVE_INTERVAL) return false;

        ticksSinceLastWave = 0;
        currentWave++;

        MeritManager.setBeastTideActive(true);

        for (ServerPlayer player : level.players()) {
            level.playSound(null, player.blockPosition(), SoundEvents.RAID_HORN.value(), SoundSource.HOSTILE, 1.5f, 0.8f);
            player.displayClientMessage(
                Component.literal("兽潮第" + currentWave + "波来袭！").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD),
                false);
            spawnWave(level, player);
        }

        if (currentWave >= TOTAL_WAVES) {
            finished = true;
            MeritManager.setBeastTideActive(false);
            for (ServerPlayer player : level.players()) {
                MeritManager.onBeastTideEnd(player);
            }
        }

        return false;
    }

    private void spawnWave(ServerLevel level, ServerPlayer player) {
        int wolfCount = 3 + level.random.nextInt(4);
        int boarCount = 1 + level.random.nextInt(2);

        player.displayClientMessage(
            Component.literal("第" + currentWave + "波兽潮来袭！").withStyle(ChatFormatting.RED),
            true);

        for (int i = 0; i < wolfCount; i++) {
            LightningWolfEntity wolf = ModEntities.LIGHTNING_WOLF.get().create(level);
            if (wolf == null) continue;
            BlockPos pos = findSpawnPos(level, player);
            wolf.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.random.nextFloat() * 360, 0);
            if (currentWave >= 3 && level.random.nextFloat() < 0.3f) {
                makeElite(wolf);
            }
            level.addFreshEntity(wolf);
        }

        for (int i = 0; i < boarCount; i++) {
            MountainBoarEntity boar = ModEntities.MOUNTAIN_BOAR.get().create(level);
            if (boar == null) continue;
            BlockPos pos = findSpawnPos(level, player);
            boar.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.random.nextFloat() * 360, 0);
            if (currentWave >= 3 && level.random.nextFloat() < 0.3f) {
                makeElite(boar);
            }
            level.addFreshEntity(boar);
        }

        if (currentWave >= 4) {
            int eliteCount = currentWave == 4 ? 2 : 3;
            for (int i = 0; i < eliteCount; i++) {
                LightningWolfEntity elite = ModEntities.LIGHTNING_WOLF.get().create(level);
                if (elite == null) continue;
                BlockPos pos = findSpawnPos(level, player);
                elite.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.random.nextFloat() * 360, 0);
                makeElite(elite);
                level.addFreshEntity(elite);
            }
        }

        if (currentWave >= TOTAL_WAVES) {
            ThunderCrownWolfEntity boss = ModEntities.THUNDER_CROWN_WOLF.get().create(level);
            if (boss != null) {
                BlockPos pos = findSpawnPos(level, player);
                boss.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.random.nextFloat() * 360, 0);
                level.addFreshEntity(boss);
                player.displayClientMessage(
                    Component.literal("雷冠头狼降临！").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                    false);
            }

            spawnBeastKing(level, player);
        }
    }

    private void spawnBeastKing(ServerLevel level, ServerPlayer player) {
        MountainBoarEntity beastKing = ModEntities.MOUNTAIN_BOAR.get().create(level);
        if (beastKing == null) return;

        BlockPos pos = findSpawnPos(level, player);
        beastKing.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.random.nextFloat() * 360, 0);

        beastKing.getAttribute(Attributes.MAX_HEALTH).setBaseValue(beastKing.getMaxHealth() * 8);
        beastKing.setHealth(beastKing.getMaxHealth());
        beastKing.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(
            beastKing.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue() * 2);
        beastKing.refreshDimensions();

        beastKing.setCustomName(Component.literal("\u00a7c百兽王").withStyle(ChatFormatting.BOLD));
        beastKing.setGlowingTag(true);

        beastKing.getPersistentData().putBoolean("BeastKing", true);

        level.addFreshEntity(beastKing);

        player.displayClientMessage(
            Component.literal("百兽王降临！").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
            false);
    }

    private void makeElite(net.minecraft.world.entity.Mob mob) {
        if (mob.getAttribute(Attributes.MAX_HEALTH) != null) {
            mob.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(
                new AttributeModifier(ELITE_HP_MOD, mob.getMaxHealth(), AttributeModifier.Operation.ADD_VALUE));
            mob.setHealth(mob.getMaxHealth());
        }
        if (mob.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            mob.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(
                new AttributeModifier(ELITE_SPEED_MOD, mob.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue() * 0.3, AttributeModifier.Operation.ADD_VALUE));
        }
        mob.setCustomName(Component.literal("\u00a7c精锐 ").append(mob.getType().getDescription()));
        mob.getPersistentData().putBoolean("BeastTideElite", true);
    }

    private BlockPos findSpawnPos(ServerLevel level, ServerPlayer player) {
        double angle = level.random.nextDouble() * Math.PI * 2;
        double dist = 30 + level.random.nextDouble() * 20;
        int x = (int) (player.getX() + Math.cos(angle) * dist);
        int z = (int) (player.getZ() + Math.sin(angle) * dist);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        return new BlockPos(x, y, z);
    }

    public boolean isFinished() {
        return finished;
    }
}
