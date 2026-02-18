package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.Rank;
import com.reverendinsanity.core.cultivation.SubRank;
import com.reverendinsanity.core.gu.GuInstance;
import com.reverendinsanity.core.gu.GuRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

// 舍利蛊：消耗品，直接提升一个小境界
public class SariraAbility extends GuAbility {

    private final Rank requiredRank;
    private final ResourceLocation guId;

    protected SariraAbility(String id, Rank requiredRank, float essenceCost) {
        super(GuRegistry.id(id), essenceCost, 0, AbilityType.INSTANT);
        this.requiredRank = requiredRank;
        this.guId = GuRegistry.id(id);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        if (aperture.getRank() != requiredRank) {
            player.sendSystemMessage(Component.literal("§c当前境界无法使用此舍利蛊"));
            return;
        }
        if (aperture.getSubRank() == SubRank.PEAK) {
            player.sendSystemMessage(Component.literal("§e已达巅峰，需使用突破石晋升大境界"));
            return;
        }

        GuInstance guInst = aperture.findGuInstance(guId);
        if (guInst != null) aperture.removeGu(guInst);

        boolean advanced = aperture.tryAdvanceSubRank();
        if (advanced) {
            player.sendSystemMessage(Component.literal("§6舍利灌顶！境界提升至" + aperture.getRank().getDisplayName() + aperture.getSubRank().getDisplayName()));
            ServerLevel serverLevel = (ServerLevel) player.level();
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                player.getX(), player.getY() + 1, player.getZ(),
                50, 1.0, 1.5, 1.0, 0.3);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }
}
