package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.entity.FireBoltEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

// 烈焰蛊技能：发射强化火弹，引燃更久+更高伤害（二转）
public class BlazingFlameAbility extends GuAbility {

    public BlazingFlameAbility() {
        super(GuRegistry.id("blazing_flame_gu"), 20f, 100, AbilityType.PROJECTILE);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float efficiency = aperture.getEssenceGrade().getEfficiency();
        float pathBonus = 1.0f + aperture.getPathRealm(DaoPath.FIRE).getTier() * 0.15f;
        float damage = 8.0f * efficiency * pathBonus;

        FireBoltEntity bolt = new FireBoltEntity(player.level(), player, damage, 160);
        bolt.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 1.5f, 0.3f);
        player.level().addFreshEntity(bolt);

        VfxHelper.spawn(player, VfxType.ENERGY_BEAM,
            player.getX(), player.getY() + 1.0, player.getZ(),
            (float) player.getLookAngle().x, (float) player.getLookAngle().y, (float) player.getLookAngle().z,
            0xFFFF8800, 2.0f, 20);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.6f, 1.5f);
    }
}
