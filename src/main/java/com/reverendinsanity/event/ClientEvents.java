package com.reverendinsanity.event;

import com.reverendinsanity.ReverendInsanity;
import com.reverendinsanity.client.render.DamageNumberRenderer;
import com.reverendinsanity.world.dimension.ModDimensions;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.joml.Vector3f;

// 客户端事件：仙窍维度氛围粒子 + 伤害数字渲染
@EventBusSubscriber(modid = ReverendInsanity.MODID, value = Dist.CLIENT)
public class ClientEvents {

    private static final DustParticleOptions HEAVEN_QI = new DustParticleOptions(
        new Vector3f(0.4f, 0.6f, 1.0f), 1.2f);

    private static final DustParticleOptions EARTH_QI = new DustParticleOptions(
        new Vector3f(0.8f, 0.65f, 0.2f), 1.0f);

    @SubscribeEvent
    public static void onClientPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Level level = player.level();
        if (!level.isClientSide()) return;
        if (!level.dimension().equals(ModDimensions.APERTURE_DIM)) return;

        if (player.tickCount % 3 != 0) return;

        double px = player.getX();
        double py = player.getY();
        double pz = player.getZ();

        for (int i = 0; i < 2; i++) {
            double hx = px + (level.random.nextDouble() - 0.5) * 16;
            double hy = py + 8 + level.random.nextDouble() * 8;
            double hz = pz + (level.random.nextDouble() - 0.5) * 16;
            level.addParticle(HEAVEN_QI, hx, hy, hz, 0, -0.02, 0);
        }

        for (int i = 0; i < 2; i++) {
            double ex = px + (level.random.nextDouble() - 0.5) * 16;
            double ey = py - 2 + level.random.nextDouble() * 3;
            double ez = pz + (level.random.nextDouble() - 0.5) * 16;
            level.addParticle(EARTH_QI, ex, ey, ez, 0, 0.03, 0);
        }
    }

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        DamageNumberRenderer.render(event.getGuiGraphics(), event.getPartialTick().getGameTimeDeltaPartialTick(false));
    }
}
