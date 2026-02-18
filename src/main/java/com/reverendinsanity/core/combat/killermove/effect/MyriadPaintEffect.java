package com.reverendinsanity.core.combat.killermove.effect;

import com.reverendinsanity.client.vfx.VfxHelper;
import com.reverendinsanity.client.vfx.VfxType;
import com.reverendinsanity.core.combat.KillerMove;
import com.reverendinsanity.core.combat.killermove.MoveEffect;
import com.reverendinsanity.core.cultivation.Aperture;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import java.util.List;

// 万彩杀招效果：画道绚丽爆发
public class MyriadPaintEffect implements MoveEffect {

    @Override
    public void execute(ServerPlayer player, Aperture aperture, KillerMove move, float calculatedDamage) {
        double range = 12.0;

        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            int color = switch (i % 4) {
                case 0 -> 0xFFFF44AA;
                case 1 -> 0xFF44FFAA;
                case 2 -> 0xFF44AAFF;
                default -> 0xFFFFAA44;
            };
            VfxHelper.spawn(player, VfxType.SLASH_ARC,
                player.getX() + Math.cos(angle) * 3, player.getY() + 1, player.getZ() + Math.sin(angle) * 3,
                (float) -Math.cos(angle), 0.3f, (float) -Math.sin(angle),
                color, 3.0f, 25);
        }
        VfxHelper.spawn(player, VfxType.RIPPLE,
            player.getX(), player.getY() + 0.5, player.getZ(),
            0f, 1f, 0f,
            0xFFFF88CC, 5.0f, 30);

        AABB aoe = player.getBoundingBox().inflate(range);
        List<LivingEntity> targets = player.level().getEntitiesOfClass(
            LivingEntity.class, aoe, e -> e != player && e.isAlive());

        for (LivingEntity target : targets) {
            target.hurt(player.damageSources().magic(), calculatedDamage * 0.60f);
        }

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 2.0f, 0.5f);
    }
}
