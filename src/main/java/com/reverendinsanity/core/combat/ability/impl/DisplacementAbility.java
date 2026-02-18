package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.gu.GuRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

// 移形蛊技能：短距离空间传送
public class DisplacementAbility extends GuAbility {

    public DisplacementAbility() {
        super(GuRegistry.id("displacement_gu"), 20f, 200, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        Vec3 look = player.getLookAngle();
        Vec3 start = player.position();

        for (int dist = 8; dist >= 3; dist--) {
            Vec3 target = start.add(look.scale(dist));
            BlockPos feetPos = BlockPos.containing(target);
            BlockPos headPos = feetPos.above();

            BlockState feetState = player.level().getBlockState(feetPos);
            BlockState headState = player.level().getBlockState(headPos);

            if (!feetState.isSolid() && !headState.isSolid()) {
                VfxHelper.spawn(player, VfxType.RIPPLE,
                    player.getX(), player.getY(), player.getZ(),
                    0f, 1f, 0f,
                    0xFF8844CC, 2.0f, 15);
                player.teleportTo(target.x, target.y, target.z);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
                player.displayClientMessage(Component.literal("移形蛊催动，空间挪移"), true);
                VfxHelper.spawn(player, VfxType.RIPPLE,
                    player.getX(), player.getY(), player.getZ(),
                    0f, 1f, 0f,
                    0xFF8844CC, 2.0f, 15);
                return;
            }
        }

        aperture.regenerateEssence(20f);
        player.displayClientMessage(Component.literal("移形失败，前方无法到达"), true);
    }
}
