package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.core.gu.GuRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

// 缩地蛊技能：缩地千里——瞬移前方6格
public class ShrinkGroundAbility extends GuAbility {

    public ShrinkGroundAbility() {
        super(GuRegistry.id("shrink_ground_gu"), 10f, 150, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        Vec3 look = player.getLookAngle();
        Vec3 target = player.position().add(look.scale(6.0));
        player.teleportTo(target.x, target.y, target.z);

        VfxHelper.spawn(player, VfxType.SHADOW_FADE,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFF44DDAA, 2.0f, 10);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.7f, 1.3f);
    }
}
