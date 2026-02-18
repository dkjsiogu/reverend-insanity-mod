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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import java.util.List;

// 魅惑蛊技能：魅惑范围内敌人互相攻击
public class CharmAbility extends GuAbility {

    public CharmAbility() {
        super(GuRegistry.id("charm_gu"), 10f, 120, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        float efficiency = aperture.getEssenceGrade().getEfficiency();
        float pathBonus = 1.0f + aperture.getPathRealm(DaoPath.CHARM).getTier() * 0.15f;
        double range = 6.0;

        VfxHelper.spawn(player, VfxType.AURA_RING,
            player.getX(), player.getY() + 1, player.getZ(),
            0f, 1f, 0f,
            0xFFFF66AA, 3.0f, 20);

        AABB aoe = player.getBoundingBox().inflate(range);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class, aoe, e -> e != player && e.isAlive());

        float damage = 3f * efficiency * pathBonus;
        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), damage);
            if (target instanceof Mob mob && targets.size() > 1) {
                for (LivingEntity other : targets) {
                    if (other != target && other.isAlive()) {
                        mob.setTarget(other);
                        break;
                    }
                }
            }
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.5f);
    }
}
