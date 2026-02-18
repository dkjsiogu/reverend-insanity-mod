package com.reverendinsanity.client.vfx;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// VFX管理器单例，负责更新和渲染所有活跃的VFX效果
public class VfxManager {

    private static final VfxManager INSTANCE = new VfxManager();
    private final List<VfxEffect> activeEffects = new ArrayList<>();

    public static VfxManager getInstance() {
        return INSTANCE;
    }

    public void addEffect(VfxEffect effect) {
        activeEffects.add(effect);
    }

    public void tick() {
        Iterator<VfxEffect> it = activeEffects.iterator();
        while (it.hasNext()) {
            VfxEffect effect = it.next();
            effect.tick();
            if (effect.isExpired()) {
                it.remove();
            }
        }
    }

    public void render(PoseStack poseStack, Camera camera, float partialTick) {
        if (activeEffects.isEmpty()) return;
        Vec3 cameraPos = camera.getPosition();

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        for (VfxEffect effect : activeEffects) {
            renderEffect(effect, poseStack, bufferSource, camera, partialTick);
        }

        poseStack.popPose();

        bufferSource.endBatch(ModRenderTypes.VFX_GLOW);
        bufferSource.endBatch(ModRenderTypes.VFX_TRANSLUCENT);
    }

    private void renderEffect(VfxEffect effect, PoseStack poseStack, MultiBufferSource bufferSource, Camera camera, float partialTick) {
        float progress = effect.getProgress();
        float x = (float) effect.getX();
        float y = (float) effect.getY();
        float z = (float) effect.getZ();
        float scale = effect.getScale();
        int r = effect.getRed();
        int g = effect.getGreen();
        int b = effect.getBlue();
        int baseAlpha = effect.getAlpha();
        Matrix4f matrix = poseStack.last().pose();

        switch (effect.getType()) {
            case SLASH_ARC -> {
                VertexConsumer consumer = bufferSource.getBuffer(ModRenderTypes.VFX_GLOW);
                float arcProgress = Math.min(1.0f, progress * 2.0f);
                int alpha = progress > 0.5f ? (int) (baseAlpha * (1.0f - (progress - 0.5f) * 2.0f)) : baseAlpha;
                alpha = Math.max(0, Math.min(255, alpha));
                VfxGeometry.drawSlashArc(consumer, matrix, x, y, z,
                    effect.getDirX(), effect.getDirZ(),
                    scale, 120.0f, arcProgress, r, g, b, alpha);
            }
            case ENERGY_BEAM -> {
                VertexConsumer consumer = bufferSource.getBuffer(ModRenderTypes.VFX_GLOW);
                float beamLen = scale * 3.0f;
                float endX = x + effect.getDirX() * beamLen;
                float endY = y + effect.getDirY() * beamLen;
                float endZ = z + effect.getDirZ() * beamLen;
                float width = 0.3f * (1.0f - progress * 0.5f);
                int alpha = (int) (baseAlpha * (1.0f - progress));
                alpha = Math.max(0, Math.min(255, alpha));
                VfxGeometry.drawBeamLine(consumer, matrix, camera,
                    x, y, z, endX, endY, endZ, width, r, g, b, alpha);
            }
            case AURA_RING -> {
                VertexConsumer consumer = bufferSource.getBuffer(ModRenderTypes.VFX_GLOW);
                float innerR = scale * 0.8f;
                float outerR = scale * 1.2f;
                float yOff = progress < 0.5f ? progress * 1.0f : (1.0f - progress) * 1.0f;
                int alpha;
                if (progress < 0.3f) {
                    alpha = (int) (baseAlpha * (progress / 0.3f));
                } else if (progress > 0.7f) {
                    alpha = (int) (baseAlpha * ((1.0f - progress) / 0.3f));
                } else {
                    alpha = baseAlpha;
                }
                alpha = Math.max(0, Math.min(255, alpha));
                float rotation = effect.getAge() * 5.0f;
                poseStack.pushPose();
                poseStack.translate(x, y + yOff, z);
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));
                poseStack.translate(-x, -(y + yOff), -z);
                Matrix4f rotMatrix = poseStack.last().pose();
                VfxGeometry.drawRing(consumer, rotMatrix, x, y + yOff, z, innerR, outerR, 32, r, g, b, alpha);
                poseStack.popPose();
            }
            case HEAL_SPIRAL -> {
                VertexConsumer consumer = bufferSource.getBuffer(ModRenderTypes.VFX_GLOW);
                int alpha = (int) (baseAlpha * (1.0f - progress));
                alpha = Math.max(0, Math.min(255, alpha));
                VfxGeometry.drawSpiral(consumer, matrix, x, y, z,
                    scale * 0.8f, 2.0f, 2.0f, progress, r, g, b, alpha);
            }
            case RIPPLE -> {
                VertexConsumer consumer = bufferSource.getBuffer(ModRenderTypes.VFX_TRANSLUCENT);
                float radius = progress * scale * 3.0f;
                int alpha = (int) (baseAlpha * (1.0f - progress));
                alpha = Math.max(0, Math.min(255, alpha));
                VfxGeometry.drawExpandingRing(consumer, matrix, x, y + 0.1f, z,
                    radius, 0.3f, 32, r, g, b, alpha);
            }
            case PULSE_WAVE -> {
                VertexConsumer consumer = bufferSource.getBuffer(ModRenderTypes.VFX_GLOW);
                float radius = progress * scale * 5.0f;
                int alpha = (int) (baseAlpha * (1.0f - progress));
                alpha = Math.max(0, Math.min(255, alpha));
                VfxGeometry.drawExpandingRing(consumer, matrix, x, y + 0.1f, z,
                    radius, 0.5f, 32, r, g, b, alpha);
            }
            case GLOW_BURST -> {
                VertexConsumer consumer = bufferSource.getBuffer(ModRenderTypes.VFX_GLOW);
                float size;
                int alpha;
                if (progress < 0.3f) {
                    size = scale * (progress / 0.3f) * 1.5f;
                    alpha = (int) (baseAlpha * (progress / 0.3f));
                } else {
                    size = scale * 1.5f * (1.0f - (progress - 0.3f) / 0.7f);
                    alpha = (int) (baseAlpha * (1.0f - (progress - 0.3f) / 0.7f));
                }
                alpha = Math.max(0, Math.min(255, alpha));
                VfxGeometry.drawBillboard(consumer, matrix, camera, x, y + 1.0f, z, size, r, g, b, alpha);
            }
            case SHADOW_FADE -> {
                VertexConsumer consumer = bufferSource.getBuffer(ModRenderTypes.VFX_TRANSLUCENT);
                int alpha = (int) (baseAlpha * (1.0f - progress));
                alpha = Math.max(0, Math.min(255, alpha));
                long seed = Double.doubleToLongBits(effect.getX() * 31 + effect.getZ() * 17);
                java.util.Random rand = new java.util.Random(seed);
                for (int i = 0; i < 5; i++) {
                    float ox = (rand.nextFloat() - 0.5f) * scale * (1.0f + progress);
                    float oy = rand.nextFloat() * scale * 0.5f + progress * 1.5f;
                    float oz = (rand.nextFloat() - 0.5f) * scale * (1.0f + progress);
                    float size = scale * 0.4f * (1.0f - progress * 0.5f);
                    VfxGeometry.drawBillboard(consumer, matrix, camera,
                        x + ox, y + oy, z + oz, size, r, g, b, alpha);
                }
            }
            case IMPACT_BURST -> {
                VertexConsumer consumer = bufferSource.getBuffer(ModRenderTypes.VFX_GLOW);
                float size;
                int alpha;
                if (progress < 0.2f) {
                    size = scale * (progress / 0.2f) * 2.0f;
                    alpha = baseAlpha;
                } else {
                    size = scale * 2.0f * (1.0f - (progress - 0.2f) / 0.8f * 0.5f);
                    alpha = (int) (baseAlpha * (1.0f - (progress - 0.2f) / 0.8f));
                }
                alpha = Math.max(0, Math.min(255, alpha));
                VfxGeometry.drawBillboard(consumer, matrix, camera, x, y + 1f, z, size * 0.6f, r, g, b, alpha);
                float ringRadius = progress * scale * 3f;
                int ringAlpha = Math.max(0, Math.min(255, (int) (alpha * 0.7f)));
                VfxGeometry.drawExpandingRing(consumer, matrix, x, y + 0.1f, z, ringRadius, 0.4f, 24, r, g, b, ringAlpha);
            }
            case BLACK_HOLE -> {
                float coreProgress;
                float streamIntensity;
                float fadeOut;

                if (progress < 0.15f) {
                    coreProgress = progress / 0.15f * 0.3f;
                    streamIntensity = progress / 0.15f;
                    fadeOut = 1.0f;
                } else if (progress < 0.7f) {
                    float p = (progress - 0.15f) / 0.55f;
                    coreProgress = 0.3f + p * 0.7f;
                    streamIntensity = 1.0f;
                    fadeOut = 1.0f;
                } else {
                    float p = (progress - 0.7f) / 0.3f;
                    coreProgress = 1.0f - p * 0.5f;
                    streamIntensity = 1.0f - p;
                    fadeOut = 1.0f - p;
                }

                float coreRadius = scale * coreProgress;
                float maxStreamRadius = scale * 3.0f;
                float rotation = effect.getAge() * 3.0f;

                if (streamIntensity > 0.01f) {
                    VertexConsumer tc = bufferSource.getBuffer(ModRenderTypes.VFX_TRANSLUCENT);
                    int sa = Math.max(0, Math.min(255, (int) (180 * streamIntensity * fadeOut)));
                    VfxGeometry.drawVortexStreams(tc, matrix,
                        x, y, z, maxStreamRadius, Math.max(0.1f, coreRadius * 0.8f),
                        8, (float) Math.toRadians(rotation),
                        15, 5, 25, sa);
                }

                if (coreProgress > 0.01f) {
                    VertexConsumer tc = bufferSource.getBuffer(ModRenderTypes.VFX_TRANSLUCENT);
                    int ca = Math.max(0, Math.min(255, (int) (240 * fadeOut)));
                    VfxGeometry.drawSphere(tc, matrix, x, y, z, coreRadius, 10, 16, 5, 0, 10, ca);
                }

                if (coreProgress > 0.2f) {
                    VertexConsumer gc = bufferSource.getBuffer(ModRenderTypes.VFX_GLOW);
                    float ringR = coreRadius * 1.4f;
                    int ra = Math.max(0, Math.min(255, (int) (160 * fadeOut * Math.min(1f, (coreProgress - 0.2f) / 0.3f))));
                    poseStack.pushPose();
                    poseStack.translate(x, y, z);
                    poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(30f));
                    poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotation));
                    poseStack.translate(-x, -y, -z);
                    Matrix4f rm = poseStack.last().pose();
                    VfxGeometry.drawRing(gc, rm, x, y, z, ringR * 0.7f, ringR, 32, 60, 15, 90, ra);
                    poseStack.popPose();
                }

                if (coreProgress > 0.3f) {
                    VertexConsumer gc = bufferSource.getBuffer(ModRenderTypes.VFX_GLOW);
                    float edgeR = coreRadius * 1.1f;
                    int ea = Math.max(0, Math.min(255, (int) (100 * fadeOut)));
                    for (int i = -2; i <= 2; i++) {
                        float yOff = i * coreRadius * 0.25f;
                        float rScale = (float) Math.sqrt(Math.max(0, 1.0f - (i * 0.25f) * (i * 0.25f)));
                        float rr = edgeR * rScale;
                        if (rr > 0.1f) {
                            int a2 = Math.max(0, Math.min(255, (int) (ea * rScale)));
                            VfxGeometry.drawRing(gc, matrix, x, y + yOff, z, rr * 0.95f, rr, 24, 80, 20, 120, a2);
                        }
                    }
                }
            }
            case TORNADO -> {
                VertexConsumer gc = bufferSource.getBuffer(ModRenderTypes.VFX_GLOW);
                float height = scale * 3.0f;
                float baseR = scale * 0.4f;
                float topR = scale * 1.5f;
                int rings = 12;
                float rot = effect.getAge() * 8.0f;

                int alpha;
                if (progress < 0.15f) {
                    alpha = (int) (baseAlpha * (progress / 0.15f));
                } else if (progress > 0.8f) {
                    alpha = (int) (baseAlpha * ((1f - progress) / 0.2f));
                } else {
                    alpha = baseAlpha;
                }
                alpha = Math.max(0, Math.min(255, alpha));

                for (int i = 0; i < rings; i++) {
                    float t = (float) i / (rings - 1);
                    float ringY = y + t * height;
                    float ringR = baseR + (topR - baseR) * t;
                    float innerR = ringR * 0.7f;
                    int ra = Math.max(0, Math.min(255, (int) (alpha * (0.5f + 0.5f * (1f - t)))));

                    poseStack.pushPose();
                    poseStack.translate(x, ringY, z);
                    poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rot + t * 120f));
                    poseStack.translate(-x, -ringY, -z);
                    Matrix4f rm = poseStack.last().pose();
                    VfxGeometry.drawRing(gc, rm, x, ringY, z, innerR, ringR, 24, r, g, b, ra);
                    poseStack.popPose();
                }

                VertexConsumer tc = bufferSource.getBuffer(ModRenderTypes.VFX_TRANSLUCENT);
                VfxGeometry.drawSpiral(tc, matrix, x, y, z, baseR * 1.2f, height, 3.0f,
                    Math.min(1f, progress * 1.5f), r, g, b, Math.max(0, Math.min(255, (int) (alpha * 0.6f))));
            }
            case SKY_STRIKE -> {
                float beamH = 15.0f;
                float beamW = scale * 0.4f;
                float impactR = progress * scale * 2.5f;

                int alpha;
                if (progress < 0.1f) {
                    alpha = (int) (baseAlpha * (progress / 0.1f));
                } else {
                    alpha = (int) (baseAlpha * (1.0f - (progress - 0.1f) / 0.9f));
                }
                alpha = Math.max(0, Math.min(255, alpha));

                VertexConsumer gc = bufferSource.getBuffer(ModRenderTypes.VFX_GLOW);
                VfxGeometry.drawBeamLine(gc, matrix, camera,
                    x, y + beamH, z, x, y, z,
                    beamW * (1.0f - progress * 0.5f), r, g, b, alpha);

                int flashAlpha = Math.max(0, Math.min(255, (int) (alpha * 1.5f)));
                float flashSize = scale * (progress < 0.2f ? progress / 0.2f : 1.0f - (progress - 0.2f) / 0.8f);
                VfxGeometry.drawBillboard(gc, matrix, camera, x, y + 0.5f, z, flashSize, r, g, b, flashAlpha);

                int ringAlpha = Math.max(0, Math.min(255, (int) (alpha * 0.8f)));
                VfxGeometry.drawExpandingRing(gc, matrix, x, y + 0.1f, z, impactR, 0.4f, 24, r, g, b, ringAlpha);
            }
            case DOME_FIELD -> {
                float domeR = scale * 2.0f;
                float rot = effect.getAge() * 2.0f;

                int alpha;
                if (progress < 0.2f) {
                    alpha = (int) (baseAlpha * 0.5f * (progress / 0.2f));
                } else if (progress > 0.8f) {
                    alpha = (int) (baseAlpha * 0.5f * ((1f - progress) / 0.2f));
                } else {
                    alpha = (int) (baseAlpha * 0.5f);
                }
                alpha = Math.max(0, Math.min(255, alpha));

                VertexConsumer tc = bufferSource.getBuffer(ModRenderTypes.VFX_TRANSLUCENT);
                poseStack.pushPose();
                poseStack.translate(x, y, z);
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rot));
                poseStack.translate(-x, -y, -z);
                Matrix4f rm = poseStack.last().pose();
                VfxGeometry.drawHemisphere(tc, rm, x, y, z, domeR, 8, 16, r, g, b, alpha);
                poseStack.popPose();

                VertexConsumer gc = bufferSource.getBuffer(ModRenderTypes.VFX_GLOW);
                int edgeAlpha = Math.max(0, Math.min(255, (int) (alpha * 1.5f)));
                VfxGeometry.drawRing(gc, matrix, x, y + 0.1f, z, domeR * 0.9f, domeR, 32, r, g, b, edgeAlpha);
            }
        }
    }
}
