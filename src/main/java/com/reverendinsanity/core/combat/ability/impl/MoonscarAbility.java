package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.cultivation.EssenceGrade;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.core.path.DaoPath;
import com.reverendinsanity.entity.MoonBladeEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

// 月痕蛊技能：发射三道扇形月刃（月光蛊升级版）
public class MoonscarAbility extends GuAbility {

    public MoonscarAbility() {
        super(GuRegistry.id("moonscar_gu"), 18f, 50, AbilityType.PROJECTILE);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float baseDamage = 5.0f;
        float efficiencyMult = 1.0f;
        EssenceGrade grade = aperture.getEssenceGrade();
        if (grade != null) {
            efficiencyMult = grade.getEfficiency();
        }
        float pathMult = 1.0f + aperture.getPathRealm(DaoPath.MOON).getTier() * 0.15f;
        float damage = baseDamage * efficiencyMult * pathMult;

        Vec3 look = player.getLookAngle();

        float[] angles = {-15f, 0f, 15f};
        for (float angle : angles) {
            double radians = Math.toRadians(angle);
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);
            double newX = look.x * cos - look.z * sin;
            double newZ = look.x * sin + look.z * cos;
            MoonBladeEntity blade = new MoonBladeEntity(player.level(), player, damage);
            blade.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            blade.shoot(newX, look.y, newZ, 1.5f, 0.0f);
            player.level().addFreshEntity(blade);
        }

        VfxHelper.spawn(player, VfxType.SLASH_ARC, 0xFF88AAFF, 2.5f, 10);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
