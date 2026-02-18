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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.List;

// 泰山压顶蛊技能：以自身为中心砸地，AOE击飞+伤害
public class TaishanAbility extends GuAbility {

    public TaishanAbility() {
        super(GuRegistry.id("taishan_gu"), 12f, 240, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float efficiency = aperture.getEssenceGrade().getEfficiency();
        float pathBonus = 1.0f + aperture.getPathRealm(DaoPath.STRENGTH).getTier() * 0.15f;
        float damage = 8.0f * efficiency * pathBonus;

        AABB area = player.getBoundingBox().inflate(4.0, 2.0, 4.0);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().playerAttack(player), damage);
            Vec3 knockDir = target.position().subtract(player.position()).normalize();
            target.setDeltaMovement(knockDir.x * 0.8, 0.6, knockDir.z * 0.8);
            target.hurtMarked = true;
        }

        VfxHelper.spawn(player, VfxType.PULSE_WAVE,
            player.getX(), player.getY(), player.getZ(),
            0f, 1f, 0f,
            0xFF886633, 4.0f, 25);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0f, 0.6f);
    }
}
