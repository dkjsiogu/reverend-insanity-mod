package com.reverendinsanity.client.vfx;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import org.joml.Matrix4f;
import org.joml.Vector3f;

// 静态几何体绘制工具类，为VFX系统提供各种形状的顶点数据
public class VfxGeometry {

    public static void drawBillboard(VertexConsumer consumer, Matrix4f matrix, Camera camera,
                                     float x, float y, float z, float size,
                                     int r, int g, int b, int a) {
        Vector3f left = new Vector3f(camera.getLeftVector());
        Vector3f up = new Vector3f(camera.getUpVector());

        float halfCore = size * 0.4f;
        float cx1 = x - left.x * halfCore - up.x * halfCore;
        float cy1 = y - left.y * halfCore - up.y * halfCore;
        float cz1 = z - left.z * halfCore - up.z * halfCore;
        float cx2 = x + left.x * halfCore - up.x * halfCore;
        float cy2 = y + left.y * halfCore - up.y * halfCore;
        float cz2 = z + left.z * halfCore - up.z * halfCore;
        float cx3 = x + left.x * halfCore + up.x * halfCore;
        float cy3 = y + left.y * halfCore + up.y * halfCore;
        float cz3 = z + left.z * halfCore + up.z * halfCore;
        float cx4 = x - left.x * halfCore + up.x * halfCore;
        float cy4 = y - left.y * halfCore + up.y * halfCore;
        float cz4 = z - left.z * halfCore + up.z * halfCore;

        consumer.addVertex(matrix, cx1, cy1, cz1).setColor(r, g, b, a);
        consumer.addVertex(matrix, cx2, cy2, cz2).setColor(r, g, b, a);
        consumer.addVertex(matrix, cx3, cy3, cz3).setColor(r, g, b, a);
        consumer.addVertex(matrix, cx1, cy1, cz1).setColor(r, g, b, a);
        consumer.addVertex(matrix, cx3, cy3, cz3).setColor(r, g, b, a);
        consumer.addVertex(matrix, cx4, cy4, cz4).setColor(r, g, b, a);

        float halfOuter = size;
        int outerAlpha = Math.max(0, a / 3);
        float ox1 = x - left.x * halfOuter - up.x * halfOuter;
        float oy1 = y - left.y * halfOuter - up.y * halfOuter;
        float oz1 = z - left.z * halfOuter - up.z * halfOuter;
        float ox2 = x + left.x * halfOuter - up.x * halfOuter;
        float oy2 = y + left.y * halfOuter - up.y * halfOuter;
        float oz2 = z + left.z * halfOuter - up.z * halfOuter;
        float ox3 = x + left.x * halfOuter + up.x * halfOuter;
        float oy3 = y + left.y * halfOuter + up.y * halfOuter;
        float oz3 = z + left.z * halfOuter + up.z * halfOuter;
        float ox4 = x - left.x * halfOuter + up.x * halfOuter;
        float oy4 = y - left.y * halfOuter + up.y * halfOuter;
        float oz4 = z - left.z * halfOuter + up.z * halfOuter;

        consumer.addVertex(matrix, ox1, oy1, oz1).setColor(r, g, b, outerAlpha);
        consumer.addVertex(matrix, ox2, oy2, oz2).setColor(r, g, b, outerAlpha);
        consumer.addVertex(matrix, ox3, oy3, oz3).setColor(r, g, b, outerAlpha);
        consumer.addVertex(matrix, ox1, oy1, oz1).setColor(r, g, b, outerAlpha);
        consumer.addVertex(matrix, ox3, oy3, oz3).setColor(r, g, b, outerAlpha);
        consumer.addVertex(matrix, ox4, oy4, oz4).setColor(r, g, b, outerAlpha);
    }

    public static void drawSlashArc(VertexConsumer consumer, Matrix4f matrix,
                                     float x, float y, float z,
                                     float dirX, float dirZ,
                                     float radius, float arcAngle, float progress,
                                     int r, int g, int b, int a) {
        int segments = 16;
        float innerRadius = radius * 0.3f;
        float halfArc = (float) Math.toRadians(arcAngle * progress * 0.5f);
        float baseAngle = (float) Math.atan2(dirZ, dirX);

        for (int i = 0; i < segments; i++) {
            float t0 = (float) i / segments;
            float t1 = (float) (i + 1) / segments;
            float angle0 = baseAngle - halfArc + t0 * halfArc * 2.0f;
            float angle1 = baseAngle - halfArc + t1 * halfArc * 2.0f;

            float distFromCenter0 = Math.abs(t0 - 0.5f) * 2.0f;
            float distFromCenter1 = Math.abs(t1 - 0.5f) * 2.0f;
            int alpha0 = (int) (a * (1.0f - distFromCenter0 * 0.8f));
            int alpha1 = (int) (a * (1.0f - distFromCenter1 * 0.8f));
            alpha0 = Math.max(0, Math.min(255, alpha0));
            alpha1 = Math.max(0, Math.min(255, alpha1));

            float cos0 = (float) Math.cos(angle0);
            float sin0 = (float) Math.sin(angle0);
            float cos1 = (float) Math.cos(angle1);
            float sin1 = (float) Math.sin(angle1);

            float ix0 = x + cos0 * innerRadius;
            float iz0 = z + sin0 * innerRadius;
            float ox0 = x + cos0 * radius;
            float oz0 = z + sin0 * radius;
            float ix1 = x + cos1 * innerRadius;
            float iz1 = z + sin1 * innerRadius;
            float ox1 = x + cos1 * radius;
            float oz1 = z + sin1 * radius;

            consumer.addVertex(matrix, ix0, y, iz0).setColor(r, g, b, alpha0);
            consumer.addVertex(matrix, ox0, y, oz0).setColor(r, g, b, alpha0);
            consumer.addVertex(matrix, ox1, y, oz1).setColor(r, g, b, alpha1);

            consumer.addVertex(matrix, ix0, y, iz0).setColor(r, g, b, alpha0);
            consumer.addVertex(matrix, ox1, y, oz1).setColor(r, g, b, alpha1);
            consumer.addVertex(matrix, ix1, y, iz1).setColor(r, g, b, alpha1);
        }
    }

    public static void drawRing(VertexConsumer consumer, Matrix4f matrix,
                                 float x, float y, float z,
                                 float innerRadius, float outerRadius, int segments,
                                 int r, int g, int b, int a) {
        float step = (float) (Math.PI * 2.0 / segments);
        for (int i = 0; i < segments; i++) {
            float angle0 = i * step;
            float angle1 = (i + 1) * step;
            float cos0 = (float) Math.cos(angle0);
            float sin0 = (float) Math.sin(angle0);
            float cos1 = (float) Math.cos(angle1);
            float sin1 = (float) Math.sin(angle1);

            float ix0 = x + cos0 * innerRadius;
            float iz0 = z + sin0 * innerRadius;
            float ox0 = x + cos0 * outerRadius;
            float oz0 = z + sin0 * outerRadius;
            float ix1 = x + cos1 * innerRadius;
            float iz1 = z + sin1 * innerRadius;
            float ox1 = x + cos1 * outerRadius;
            float oz1 = z + sin1 * outerRadius;

            int outerAlpha = Math.max(0, a / 4);

            consumer.addVertex(matrix, ix0, y, iz0).setColor(r, g, b, a);
            consumer.addVertex(matrix, ox0, y, oz0).setColor(r, g, b, outerAlpha);
            consumer.addVertex(matrix, ox1, y, oz1).setColor(r, g, b, outerAlpha);

            consumer.addVertex(matrix, ix0, y, iz0).setColor(r, g, b, a);
            consumer.addVertex(matrix, ox1, y, oz1).setColor(r, g, b, outerAlpha);
            consumer.addVertex(matrix, ix1, y, iz1).setColor(r, g, b, a);
        }
    }

    public static void drawSpiral(VertexConsumer consumer, Matrix4f matrix,
                                   float x, float y, float z,
                                   float radius, float height, float rotations, float progress,
                                   int r, int g, int b, int a) {
        int steps = 32;
        float maxT = progress;
        float totalAngle = rotations * (float) Math.PI * 2.0f;

        for (int i = 0; i < steps - 1; i++) {
            float t0 = (float) i / (steps - 1) * maxT;
            float t1 = (float) (i + 1) / (steps - 1) * maxT;

            float angle0 = t0 * totalAngle;
            float angle1 = t1 * totalAngle;

            float rad0 = radius * (1.0f - t0 * 0.3f);
            float rad1 = radius * (1.0f - t1 * 0.3f);

            float px0 = x + (float) Math.cos(angle0) * rad0;
            float py0 = y + t0 * height;
            float pz0 = z + (float) Math.sin(angle0) * rad0;
            float px1 = x + (float) Math.cos(angle1) * rad1;
            float py1 = y + t1 * height;
            float pz1 = z + (float) Math.sin(angle1) * rad1;

            float width0 = 0.15f * (1.0f - t0 * 0.7f);
            float width1 = 0.15f * (1.0f - t1 * 0.7f);

            int alpha0 = (int) (a * (1.0f - t0 * 0.6f));
            int alpha1 = (int) (a * (1.0f - t1 * 0.6f));
            alpha0 = Math.max(0, Math.min(255, alpha0));
            alpha1 = Math.max(0, Math.min(255, alpha1));

            consumer.addVertex(matrix, px0, py0 - width0, pz0).setColor(r, g, b, alpha0);
            consumer.addVertex(matrix, px0, py0 + width0, pz0).setColor(r, g, b, alpha0);
            consumer.addVertex(matrix, px1, py1 + width1, pz1).setColor(r, g, b, alpha1);

            consumer.addVertex(matrix, px0, py0 - width0, pz0).setColor(r, g, b, alpha0);
            consumer.addVertex(matrix, px1, py1 + width1, pz1).setColor(r, g, b, alpha1);
            consumer.addVertex(matrix, px1, py1 - width1, pz1).setColor(r, g, b, alpha1);
        }
    }

    public static void drawBeamLine(VertexConsumer consumer, Matrix4f matrix, Camera camera,
                                     float x1, float y1, float z1,
                                     float x2, float y2, float z2,
                                     float width, int r, int g, int b, int a) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 0.001f) return;

        Vector3f camPos = new Vector3f(
            (float) camera.getPosition().x,
            (float) camera.getPosition().y,
            (float) camera.getPosition().z
        );
        float midX = (x1 + x2) * 0.5f;
        float midY = (y1 + y2) * 0.5f;
        float midZ = (z1 + z2) * 0.5f;
        Vector3f toCamera = new Vector3f(camPos.x - midX, camPos.y - midY, camPos.z - midZ);
        Vector3f beamDir = new Vector3f(dx / len, dy / len, dz / len);
        Vector3f perpendicular = new Vector3f();
        beamDir.cross(toCamera, perpendicular);
        if (perpendicular.lengthSquared() < 0.0001f) {
            perpendicular.set(0, 1, 0);
        }
        perpendicular.normalize().mul(width * 0.5f);

        float hw = width * 0.5f;
        int endAlpha = Math.max(0, a / 4);

        consumer.addVertex(matrix, x1 + perpendicular.x, y1 + perpendicular.y, z1 + perpendicular.z).setColor(r, g, b, endAlpha);
        consumer.addVertex(matrix, x1 - perpendicular.x, y1 - perpendicular.y, z1 - perpendicular.z).setColor(r, g, b, endAlpha);
        consumer.addVertex(matrix, x2 - perpendicular.x, y2 - perpendicular.y, z2 - perpendicular.z).setColor(r, g, b, endAlpha);

        consumer.addVertex(matrix, x1 + perpendicular.x, y1 + perpendicular.y, z1 + perpendicular.z).setColor(r, g, b, endAlpha);
        consumer.addVertex(matrix, x2 - perpendicular.x, y2 - perpendicular.y, z2 - perpendicular.z).setColor(r, g, b, endAlpha);
        consumer.addVertex(matrix, x2 + perpendicular.x, y2 + perpendicular.y, z2 + perpendicular.z).setColor(r, g, b, endAlpha);

        float coreW = width * 0.3f;
        Vector3f corePerp = new Vector3f(perpendicular).normalize().mul(coreW * 0.5f);
        consumer.addVertex(matrix, x1 + corePerp.x, y1 + corePerp.y, z1 + corePerp.z).setColor(r, g, b, a);
        consumer.addVertex(matrix, x1 - corePerp.x, y1 - corePerp.y, z1 - corePerp.z).setColor(r, g, b, a);
        consumer.addVertex(matrix, x2 - corePerp.x, y2 - corePerp.y, z2 - corePerp.z).setColor(r, g, b, a);

        consumer.addVertex(matrix, x1 + corePerp.x, y1 + corePerp.y, z1 + corePerp.z).setColor(r, g, b, a);
        consumer.addVertex(matrix, x2 - corePerp.x, y2 - corePerp.y, z2 - corePerp.z).setColor(r, g, b, a);
        consumer.addVertex(matrix, x2 + corePerp.x, y2 + corePerp.y, z2 + corePerp.z).setColor(r, g, b, a);
    }

    public static void drawExpandingRing(VertexConsumer consumer, Matrix4f matrix,
                                          float x, float y, float z,
                                          float radius, float thickness, int segments,
                                          int r, int g, int b, int a) {
        float innerR = Math.max(0.0f, radius - thickness);
        drawRing(consumer, matrix, x, y, z, innerR, radius, segments, r, g, b, a);
    }

    public static void drawSphere(VertexConsumer consumer, Matrix4f matrix,
                                   float cx, float cy, float cz,
                                   float radius, int latSegments, int lonSegments,
                                   int r, int g, int b, int a) {
        for (int lat = 0; lat < latSegments; lat++) {
            float theta0 = (float) Math.PI * lat / latSegments;
            float theta1 = (float) Math.PI * (lat + 1) / latSegments;
            float sinT0 = (float) Math.sin(theta0);
            float cosT0 = (float) Math.cos(theta0);
            float sinT1 = (float) Math.sin(theta1);
            float cosT1 = (float) Math.cos(theta1);

            for (int lon = 0; lon < lonSegments; lon++) {
                float phi0 = (float) (Math.PI * 2.0 * lon / lonSegments);
                float phi1 = (float) (Math.PI * 2.0 * (lon + 1) / lonSegments);
                float cosP0 = (float) Math.cos(phi0);
                float sinP0 = (float) Math.sin(phi0);
                float cosP1 = (float) Math.cos(phi1);
                float sinP1 = (float) Math.sin(phi1);

                float x0 = cx + radius * sinT0 * cosP0;
                float y0 = cy + radius * cosT0;
                float z0 = cz + radius * sinT0 * sinP0;
                float x1 = cx + radius * sinT0 * cosP1;
                float y1 = cy + radius * cosT0;
                float z1 = cz + radius * sinT0 * sinP1;
                float x2 = cx + radius * sinT1 * cosP1;
                float y2 = cy + radius * cosT1;
                float z2 = cz + radius * sinT1 * sinP1;
                float x3 = cx + radius * sinT1 * cosP0;
                float y3 = cy + radius * cosT1;
                float z3 = cz + radius * sinT1 * sinP0;

                consumer.addVertex(matrix, x0, y0, z0).setColor(r, g, b, a);
                consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, a);
                consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a);
                consumer.addVertex(matrix, x0, y0, z0).setColor(r, g, b, a);
                consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, a);
                consumer.addVertex(matrix, x3, y3, z3).setColor(r, g, b, a);
            }
        }
    }

    public static void drawVortexStreams(VertexConsumer consumer, Matrix4f matrix,
                                          float cx, float cy, float cz,
                                          float outerRadius, float innerRadius,
                                          int streamCount, float rotationRad,
                                          int r, int g, int b, int a) {
        int steps = 20;
        float spiralTurns = 1.5f;

        for (int s = 0; s < streamCount; s++) {
            float baseAngle = rotationRad + (float) (Math.PI * 2.0 * s / streamCount);

            for (int i = 0; i < steps - 1; i++) {
                float t0 = (float) i / (steps - 1);
                float t1 = (float) (i + 1) / (steps - 1);

                float rad0 = outerRadius + (innerRadius - outerRadius) * t0;
                float rad1 = outerRadius + (innerRadius - outerRadius) * t1;

                float angle0 = baseAngle + t0 * spiralTurns * (float) (Math.PI * 2.0);
                float angle1 = baseAngle + t1 * spiralTurns * (float) (Math.PI * 2.0);

                float px0 = cx + (float) Math.cos(angle0) * rad0;
                float pz0 = cz + (float) Math.sin(angle0) * rad0;
                float px1 = cx + (float) Math.cos(angle1) * rad1;
                float pz1 = cz + (float) Math.sin(angle1) * rad1;

                float py0 = cy + (float) Math.sin(t0 * Math.PI) * outerRadius * 0.1f;
                float py1 = cy + (float) Math.sin(t1 * Math.PI) * outerRadius * 0.1f;

                float w0 = outerRadius * 0.08f * (1.0f - t0 * 0.6f);
                float w1 = outerRadius * 0.08f * (1.0f - t1 * 0.6f);

                int a0 = Math.max(0, Math.min(255, (int) (a * (0.3f + t0 * 0.7f))));
                int a1 = Math.max(0, Math.min(255, (int) (a * (0.3f + t1 * 0.7f))));

                consumer.addVertex(matrix, px0, py0 - w0, pz0).setColor(r, g, b, a0);
                consumer.addVertex(matrix, px0, py0 + w0, pz0).setColor(r, g, b, a0);
                consumer.addVertex(matrix, px1, py1 + w1, pz1).setColor(r, g, b, a1);

                consumer.addVertex(matrix, px0, py0 - w0, pz0).setColor(r, g, b, a0);
                consumer.addVertex(matrix, px1, py1 + w1, pz1).setColor(r, g, b, a1);
                consumer.addVertex(matrix, px1, py1 - w1, pz1).setColor(r, g, b, a1);
            }
        }
    }

    public static void drawHemisphere(VertexConsumer consumer, Matrix4f matrix,
                                       float cx, float cy, float cz,
                                       float radius, int latSegments, int lonSegments,
                                       int r, int g, int b, int a) {
        for (int lat = 0; lat < latSegments; lat++) {
            float theta0 = (float) Math.PI * 0.5f * lat / latSegments;
            float theta1 = (float) Math.PI * 0.5f * (lat + 1) / latSegments;
            float sinT0 = (float) Math.sin(theta0);
            float cosT0 = (float) Math.cos(theta0);
            float sinT1 = (float) Math.sin(theta1);
            float cosT1 = (float) Math.cos(theta1);

            int ringAlpha = Math.max(0, Math.min(255, (int) (a * (1.0f - (float) lat / latSegments * 0.5f))));

            for (int lon = 0; lon < lonSegments; lon++) {
                float phi0 = (float) (Math.PI * 2.0 * lon / lonSegments);
                float phi1 = (float) (Math.PI * 2.0 * (lon + 1) / lonSegments);
                float cosP0 = (float) Math.cos(phi0);
                float sinP0 = (float) Math.sin(phi0);
                float cosP1 = (float) Math.cos(phi1);
                float sinP1 = (float) Math.sin(phi1);

                float x0 = cx + radius * cosT0 * cosP0;
                float y0 = cy + radius * sinT0;
                float z0 = cz + radius * cosT0 * sinP0;
                float x1 = cx + radius * cosT0 * cosP1;
                float y1 = cy + radius * sinT0;
                float z1 = cz + radius * cosT0 * sinP1;
                float x2 = cx + radius * cosT1 * cosP1;
                float y2 = cy + radius * sinT1;
                float z2 = cz + radius * cosT1 * sinP1;
                float x3 = cx + radius * cosT1 * cosP0;
                float y3 = cy + radius * sinT1;
                float z3 = cz + radius * cosT1 * sinP0;

                consumer.addVertex(matrix, x0, y0, z0).setColor(r, g, b, ringAlpha);
                consumer.addVertex(matrix, x1, y1, z1).setColor(r, g, b, ringAlpha);
                consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, ringAlpha);
                consumer.addVertex(matrix, x0, y0, z0).setColor(r, g, b, ringAlpha);
                consumer.addVertex(matrix, x2, y2, z2).setColor(r, g, b, ringAlpha);
                consumer.addVertex(matrix, x3, y3, z3).setColor(r, g, b, ringAlpha);
            }
        }
    }
}
