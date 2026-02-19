package com.reverendinsanity.core.combat.ability.impl;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.DotManager;
import com.reverendinsanity.core.combat.ability.GuAbility;
import com.reverendinsanity.core.cultivation.Aperture;
import com.reverendinsanity.core.gu.GuRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import java.util.Comparator;
import java.util.List;

// 爱别离技能：毒道二转超强单体毒——自定义凋零DOT+毒素DOT+真伤
public class LoveSeparationAbility extends GuAbility {

    public LoveSeparationAbility() {
        super(GuRegistry.id("love_separation_gu"), 30f, 800, AbilityType.INSTANT);
    }

    @Override
    protected void onActivate(ServerPlayer player, Aperture aperture) {
        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();

        AABB area = new AABB(px - 12, py - 6, pz - 12, px + 12, py + 6, pz + 12);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class, area,
            e -> e != player && e.isAlive());

        targets.sort(Comparator.comparingDouble(e -> e.distanceToSqr(player)));

        if (!targets.isEmpty()) {
            LivingEntity target = targets.get(0);
            DotManager.applyWither(target, player, 2.0f, 200);
            DotManager.applyPoison(target, player, 1.5f, 200);
            target.hurt(player.damageSources().magic(), 8.0f);

            VfxHelper.spawn(player, VfxType.IMPACT_BURST,
                (float) target.getX(), (float) target.getY() + 1.0f, (float) target.getZ(),
                0f, 1f, 0f,
                0xFF660066, 2.5f, 20);

            player.displayClientMessage(Component.literal("爱别离——二转第一毒！"), true);
        } else {
            player.displayClientMessage(Component.literal("附近无目标"), true);
            aperture.regenerateEssence(15f);
        }

        player.level().playSound(null, px, py, pz,
            SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 0.5f);
    }
}
