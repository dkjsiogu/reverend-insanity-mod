package com.reverendinsanity.client.render;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// 伤害浮动数字客户端渲染器
public class DamageNumberRenderer {

    private static final List<DamageEntry> entries = new ArrayList<>();
    private static final int MAX_LIFETIME = 25;

    public static void addEntry(float damage, int damageType, double x, double y, double z) {
        float offsetX = (float)(Math.random() - 0.5) * 0.8f;
        float offsetZ = (float)(Math.random() - 0.5) * 0.8f;
        entries.add(new DamageEntry(x + offsetX, y, z + offsetZ, damage, damageType, 0));
    }

    public static void tick() {
        Iterator<DamageEntry> it = entries.iterator();
        while (it.hasNext()) {
            DamageEntry entry = it.next();
            entry.ticksAlive++;
            entry.y += 0.04;
            if (entry.ticksAlive > MAX_LIFETIME) {
                it.remove();
            }
        }
    }

    public static void render(GuiGraphics graphics, float partialTick) {
        if (entries.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        for (DamageEntry entry : entries) {
            double dx = entry.x - cameraPos.x;
            double dy = entry.y - cameraPos.y;
            double dz = entry.z - cameraPos.z;

            Vector3f worldRel = new Vector3f((float) dx, (float) dy, (float) dz);
            worldRel.rotate(camera.rotation().conjugate(new org.joml.Quaternionf()));

            if (worldRel.z() >= 0) continue;

            float fov = 70.0f;
            float aspect = (float) screenWidth / (float) screenHeight;
            float tanFov = (float) Math.tan(Math.toRadians(fov / 2.0));

            float ndcX = -worldRel.x() / (-worldRel.z() * tanFov * aspect);
            float ndcY = worldRel.y() / (-worldRel.z() * tanFov);

            int sx = (int) ((ndcX + 1.0f) / 2.0f * screenWidth);
            int sy = (int) ((1.0f - ndcY) / 2.0f * screenHeight);

            if (sx < -50 || sx > screenWidth + 50 || sy < -50 || sy > screenHeight + 50) continue;

            float alpha = 1.0f - (float) entry.ticksAlive / MAX_LIFETIME;
            int color = getColor(entry.damageType);
            int alphaInt = (int) (alpha * 255) << 24;
            int finalColor = (color & 0x00FFFFFF) | alphaInt;

            String text = formatDamage(entry.damage, entry.damageType);
            float scale = entry.damageType == 3 ? 1.5f : 1.0f;

            graphics.pose().pushPose();
            graphics.pose().translate(sx, sy, 0);
            graphics.pose().scale(scale, scale, 1.0f);
            int textWidth = mc.font.width(text);
            graphics.drawString(mc.font, text, -textWidth / 2, 0, finalColor, true);
            graphics.pose().popPose();
        }
    }

    private static int getColor(int damageType) {
        return switch (damageType) {
            case 0 -> 0xFFFFFF;
            case 1 -> 0x6688FF;
            case 2 -> 0x44FF44;
            case 3 -> 0xFF4444;
            case 4 -> 0xFFAA00;
            default -> 0xFFFFFF;
        };
    }

    private static String formatDamage(float damage, int damageType) {
        String text = String.format("%.1f", damage);
        if (damageType == 3) {
            text = text + "!";
        }
        return text;
    }

    private static class DamageEntry {
        double x, y, z;
        float damage;
        int damageType;
        int ticksAlive;

        DamageEntry(double x, double y, double z, float damage, int damageType, int ticksAlive) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.damage = damage;
            this.damageType = damageType;
            this.ticksAlive = ticksAlive;
        }
    }
}
