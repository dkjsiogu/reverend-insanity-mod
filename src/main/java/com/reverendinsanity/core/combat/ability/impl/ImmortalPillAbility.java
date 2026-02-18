package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.gu.GuRegistry;
import com.reverendinsanity.core.path.DaoPath;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import java.util.List;

// 仙丹蛊技能：仙级丹道攻击+恢复
public class ImmortalPillAbility extends GuAbility {

    public ImmortalPillAbility() {
        super(GuRegistry.id("immortal_pill_gu"), 22f, 400, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float efficiency = aperture.getEssenceGrade().getEfficiency();
        float pathBonus = 1.0f + aperture.getPathRealm(DaoPath.PILL).getTier() * 0.2f;
        double range = 10.0;

        VfxHelper.spawn(player, VfxType.HEAL_SPIRAL,
            player.getX(), player.getY() + 1, player.getZ(),
            0f, 1f, 0f,
            0xFF33FF33, 4.0f, 25);
        VfxHelper.spawn(player, VfxType.PULSE_WAVE,
            player.getX(), player.getY() + 0.5, player.getZ(),
            0f, 1f, 0f,
            0xFF44DD44, 5.0f, 25);

        AABB aoe = player.getBoundingBox().inflate(range);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class, aoe, e -> e != player && e.isAlive());

        float damage = 7f * efficiency * pathBonus;
        float totalHeal = 0f;
        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), damage);
            totalHeal += damage * 0.15f;
        }
        player.heal(Math.min(totalHeal, 20f));

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.5f, 0.4f);
    }
}
