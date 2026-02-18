package com.reverendinsanity.core.cultivation;

import com.reverendinsanity.registry.ModAttachments;
import com.reverendinsanity.util.AdvancementHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.ChatFormatting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// 天劫管理器：管理渡劫状态、闪电、怪物生成、成败判定
public class TribulationManager {

    private static final Map<UUID, TribulationState> activeTribulations = new HashMap<>();

    public static boolean isInTribulation(ServerPlayer player) {
        return activeTribulations.containsKey(player.getUUID());
    }

    public static void startTribulation(ServerPlayer player, Rank targetRank) {
        UUID uuid = player.getUUID();
        if (activeTribulations.containsKey(uuid)) return;

        int duration;
        int lightningInterval;
        int monsterInterval;
        int monsterCount;

        if (targetRank == Rank.RANK_4) {
            duration = 60 * 20;
            lightningInterval = 5 * 20;
            monsterInterval = 15 * 20;
            monsterCount = 2;
        } else {
            duration = 90 * 20;
            lightningInterval = 3 * 20;
            monsterInterval = 10 * 20;
            monsterCount = 3;
        }

        TribulationState state = new TribulationState(
                targetRank, duration, lightningInterval, monsterInterval, monsterCount,
                player.blockPosition()
        );
        activeTribulations.put(uuid, state);

        ServerLevel level = player.serverLevel();
        level.playSound(null, player.blockPosition(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 2.0f, 0.5f);
        level.playSound(null, player.blockPosition(),
                SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.5f, 0.8f);

        level.sendParticles(ParticleTypes.FLASH,
                player.getX(), player.getY() + 2, player.getZ(),
                5, 1, 2, 1, 0);
        level.sendParticles(ParticleTypes.LARGE_SMOKE,
                player.getX(), player.getY(), player.getZ(),
                80, 3, 0.5, 3, 0.02);

        player.setGlowingTag(true);

        String msg = targetRank == Rank.RANK_4
                ? "天劫降临！在60秒内存活即可突破至四转！"
                : "大天劫降临！在90秒内存活即可突破至五转！";
        player.displayClientMessage(
                Component.literal(msg).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);
    }

    public static void tick(ServerPlayer player) {
        UUID uuid = player.getUUID();
        TribulationState state = activeTribulations.get(uuid);
        if (state == null) return;

        ServerLevel level = player.serverLevel();
        state.ticksElapsed++;

        if (state.ticksElapsed >= state.duration) {
            onTribulationSuccess(player, state);
            activeTribulations.remove(uuid);
            return;
        }

        if (state.ticksElapsed % state.lightningInterval == 0) {
            spawnLightningNearPlayer(player, level, state);
        }

        if (state.ticksElapsed % state.monsterInterval == 0) {
            spawnTribulationMonsters(player, level, state);
        }

        if (state.ticksElapsed % 40 == 0) {
            spawnAmbientParticles(player, level, state);
        }

        if (state.targetRank == Rank.RANK_5 && state.ticksElapsed % 60 == 0) {
            level.sendParticles(ParticleTypes.PORTAL,
                    player.getX(), player.getY() + 1, player.getZ(),
                    30, 2, 2, 2, 0.5);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                    player.getX(), player.getY() + 1, player.getZ(),
                    20, 1.5, 1.5, 1.5, 0.3);
        }

        if (state.targetRank == Rank.RANK_5) {
            int voidPhaseStart = state.duration - (20 * 20);
            if (state.ticksElapsed >= voidPhaseStart) {
                if (state.ticksElapsed % 20 == 0) {
                    player.hurt(level.damageSources().magic(), 2.0f);
                    level.sendParticles(ParticleTypes.SCULK_SOUL,
                            player.getX(), player.getY() + 1, player.getZ(),
                            10, 1, 1, 1, 0.1);
                    if (state.ticksElapsed == voidPhaseStart) {
                        player.displayClientMessage(
                                Component.literal("虚空波动开始！坚持住！")
                                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), false);
                    }
                }
            }
        }

        if (state.ticksElapsed % 200 == 0) {
            int secondsLeft = (state.duration - state.ticksElapsed) / 20;
            player.displayClientMessage(
                    Component.literal("天劫进行中... 剩余 " + secondsLeft + " 秒")
                            .withStyle(ChatFormatting.YELLOW), true);
        }
    }

    public static void onPlayerDeath(ServerPlayer player) {
        UUID uuid = player.getUUID();
        TribulationState state = activeTribulations.get(uuid);
        if (state == null) return;

        activeTribulations.remove(uuid);
        player.setGlowingTag(false);

        player.displayClientMessage(
                Component.literal("渡劫失败！天劫之力将你击溃...")
                        .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), false);

        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.EXPLOSION,
                player.getX(), player.getY() + 1, player.getZ(),
                10, 2, 2, 2, 0);
        level.playSound(null, player.blockPosition(),
                SoundEvents.GENERIC_EXPLODE.value(), SoundSource.WEATHER, 2.0f, 0.5f);
    }

    public static void onPlayerDisconnect(ServerPlayer player) {
        TribulationState state = activeTribulations.remove(player.getUUID());
        if (state != null) {
            player.setGlowingTag(false);
            GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
            Aperture aperture = data.getAperture();
            Rank prevRank = getPreviousRank(state.targetRank);
            if (prevRank != null) {
                aperture.setRank(prevRank);
                aperture.setSubRank(SubRank.PEAK);
            }
        }
    }

    public static boolean requiresTribulation(Rank targetRank) {
        return targetRank == Rank.RANK_4 || targetRank == Rank.RANK_5;
    }

    private static void onTribulationSuccess(ServerPlayer player, TribulationState state) {
        GuMasterData data = player.getData(ModAttachments.GU_MASTER_DATA.get());
        Aperture aperture = data.getAperture();

        aperture.setCurrentEssence(0);
        aperture.setRank(state.targetRank);
        aperture.setSubRank(SubRank.INITIAL);

        String msg = "渡劫成功！晋升为 "
                + aperture.getRank().getDisplayName() + "·"
                + aperture.getSubRank().getDisplayName() + " 蛊师！";
        player.displayClientMessage(
                Component.literal(msg).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);

        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                player.getX(), player.getY() + 1, player.getZ(),
                300, 2, 3, 2, 0.5);
        level.sendParticles(ParticleTypes.END_ROD,
                player.getX(), player.getY() + 1, player.getZ(),
                150, 1.5, 2.5, 1.5, 0.2);
        level.sendParticles(ParticleTypes.FLASH,
                player.getX(), player.getY() + 2, player.getZ(),
                3, 0, 0, 0, 0);

        level.playSound(null, player.blockPosition(),
                SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 2.0f, 1.0f);
        level.playSound(null, player.blockPosition(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 1.5f, 1.2f);

        player.setGlowingTag(false);

        int rankLevel = aperture.getRank().getLevel();
        if (rankLevel >= 4) AdvancementHelper.grant(player, "rank4");
        if (rankLevel >= 5) AdvancementHelper.grant(player, "rank5");
        AdvancementHelper.grant(player, "survive_tribulation");
    }

    private static void spawnLightningNearPlayer(ServerPlayer player, ServerLevel level, TribulationState state) {
        double offsetX = (player.getRandom().nextDouble() - 0.5) * 10;
        double offsetZ = (player.getRandom().nextDouble() - 0.5) * 10;
        double dist = Math.sqrt(offsetX * offsetX + offsetZ * offsetZ);
        if (dist < 2) {
            offsetX = offsetX / dist * 2;
            offsetZ = offsetZ / dist * 2;
        }

        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.moveTo(player.getX() + offsetX, player.getY(), player.getZ() + offsetZ);
            bolt.setVisualOnly(false);
            level.addFreshEntity(bolt);
        }

        level.playSound(null, player.blockPosition(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 1.5f, 0.7f + player.getRandom().nextFloat() * 0.6f);
    }

    private static void spawnTribulationMonsters(ServerPlayer player, ServerLevel level, TribulationState state) {
        int extra = player.getRandom().nextInt(2);
        int count = state.monsterCount + extra;

        for (int i = 0; i < count; i++) {
            double angle = player.getRandom().nextDouble() * Math.PI * 2;
            double radius = 3 + player.getRandom().nextDouble() * 5;
            double mx = player.getX() + Math.cos(angle) * radius;
            double mz = player.getZ() + Math.sin(angle) * radius;
            BlockPos spawnPos = level.getHeightmapPos(
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    BlockPos.containing(mx, 0, mz));

            if (player.getRandom().nextBoolean()) {
                Zombie zombie = EntityType.ZOMBIE.create(level);
                if (zombie != null) {
                    zombie.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                            player.getRandom().nextFloat() * 360, 0);
                    equipTribulationMob(zombie, state, player);
                    zombie.setTarget(player);
                    level.addFreshEntity(zombie);
                }
            } else {
                Skeleton skeleton = EntityType.SKELETON.create(level);
                if (skeleton != null) {
                    skeleton.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                            player.getRandom().nextFloat() * 360, 0);
                    equipTribulationMob(skeleton, state, player);
                    skeleton.setTarget(player);
                    level.addFreshEntity(skeleton);
                }
            }
        }

        level.sendParticles(ParticleTypes.SMOKE,
                player.getX(), player.getY() + 1, player.getZ(),
                20, 2, 1, 2, 0.05);
    }

    private static void equipTribulationMob(net.minecraft.world.entity.Mob mob, TribulationState state, ServerPlayer player) {
        boolean isHighTier = state.targetRank == Rank.RANK_5;

        if (isHighTier) {
            mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.NETHERITE_HELMET));
            mob.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.NETHERITE_CHESTPLATE));
            mob.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.NETHERITE_LEGGINGS));
            mob.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.NETHERITE_BOOTS));
            if (mob instanceof Zombie) {
                mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
            }
        } else {
            mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
            mob.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.DIAMOND_CHESTPLATE));
            mob.setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.DIAMOND_LEGGINGS));
            mob.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
            if (mob instanceof Zombie) {
                mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
            }
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            mob.setDropChance(slot, 0.0f);
        }

        ResourceLocation tribDmg = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "tribulation_damage");
        ResourceLocation tribSpd = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "tribulation_speed");
        ResourceLocation tribArmor = ResourceLocation.fromNamespaceAndPath("reverend_insanity", "tribulation_armor");

        var dmgAttr = mob.getAttribute(Attributes.ATTACK_DAMAGE);
        if (dmgAttr != null) {
            dmgAttr.addTransientModifier(new AttributeModifier(tribDmg, isHighTier ? 6.0 : 3.0, AttributeModifier.Operation.ADD_VALUE));
        }
        var spdAttr = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (spdAttr != null) {
            spdAttr.addTransientModifier(new AttributeModifier(tribSpd, 0.04, AttributeModifier.Operation.ADD_VALUE));
        }
        if (isHighTier) {
            var armorAttr = mob.getAttribute(Attributes.ARMOR);
            if (armorAttr != null) {
                armorAttr.addTransientModifier(new AttributeModifier(tribArmor, 8.0, AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

    private static void spawnAmbientParticles(ServerPlayer player, ServerLevel level, TribulationState state) {
        double px = player.getX();
        double py = player.getY() + 1;
        double pz = player.getZ();

        level.sendParticles(ParticleTypes.LARGE_SMOKE,
                px, py + 5, pz, 15, 5, 2, 5, 0.01);
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                px, py + 8, pz, 8, 4, 1, 4, 0.005);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                px, py + 2, pz, 5, 3, 3, 3, 0.1);
    }

    private static Rank getPreviousRank(Rank targetRank) {
        if (targetRank == Rank.RANK_4) return Rank.RANK_3;
        if (targetRank == Rank.RANK_5) return Rank.RANK_4;
        return null;
    }

    public static void clearAll() {
        activeTribulations.clear();
    }

    private static class TribulationState {
        final Rank targetRank;
        final int duration;
        final int lightningInterval;
        final int monsterInterval;
        final int monsterCount;
        final BlockPos startPos;
        int ticksElapsed;

        TribulationState(Rank targetRank, int duration, int lightningInterval,
                         int monsterInterval, int monsterCount, BlockPos startPos) {
            this.targetRank = targetRank;
            this.duration = duration;
            this.lightningInterval = lightningInterval;
            this.monsterInterval = monsterInterval;
            this.monsterCount = monsterCount;
            this.startPos = startPos;
            this.ticksElapsed = 0;
        }
    }
}
