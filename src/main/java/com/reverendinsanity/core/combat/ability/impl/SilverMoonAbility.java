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

// 银月蛊技能：发射5道扇形银月刃（月痕蛊升级版）
public class SilverMoonAbility extends GuAbility {

    public SilverMoonAbility() {
        super(GuRegistry.id("silver_moon_gu"), 30f, 60, AbilityType.PROJECTILE);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float baseDamage = 7.0f;
        float efficiencyMult = 1.0f;
        EssenceGrade grade = aperture.getEssenceGrade();
        if (grade != null) {
            efficiencyMult = grade.getEfficiency();
        }
        float pathMult = 1.0f + aperture.getPathRealm(DaoPath.MOON).getTier() * 0.15f;
        float damage = baseDamage * efficiencyMult * pathMult;

        Vec3 look = player.getLookAngle();

        float[] angles = {-40f, -20f, 0f, 20f, 40f};
        for (float angle : angles) {
            double radians = Math.toRadians(angle);
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);
            double newX = look.x * cos - look.z * sin;
            double newZ = look.x * sin + look.z * cos;
            MoonBladeEntity blade = new MoonBladeEntity(player.level(), player, damage);
            blade.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            blade.shoot(newX, look.y, newZ, 2.0f, 0.0f);
            player.level().addFreshEntity(blade);
        }

        VfxHelper.spawn(player, VfxType.SLASH_ARC, 0xFFCCDDFF, 3.0f, 12);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.TRIDENT_RIPTIDE_3, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
