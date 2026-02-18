package com.reverendinsanity.client.vfx;

// 活跃的VFX效果实例，存储位置、方向、颜色、缩放、持续时间等
public class VfxEffect {

    private final VfxType type;
    private final double x, y, z;
    private final float dirX, dirY, dirZ;
    private final int color;
    private final float scale;
    private final int durationTicks;
    private int age;

    public VfxEffect(VfxType type, double x, double y, double z,
                     float dirX, float dirY, float dirZ,
                     int color, float scale, int durationTicks) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dirX = dirX;
        this.dirY = dirY;
        this.dirZ = dirZ;
        this.color = color;
        this.scale = scale;
        this.durationTicks = durationTicks;
        this.age = 0;
    }

    public void tick() {
        age++;
    }

    public boolean isExpired() {
        return age >= durationTicks;
    }

    public float getProgress() {
        return Math.min(1.0f, (float) age / (float) durationTicks);
    }

    public VfxType getType() { return type; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getDirX() { return dirX; }
    public float getDirY() { return dirY; }
    public float getDirZ() { return dirZ; }
    public int getColor() { return color; }
    public float getScale() { return scale; }
    public int getDurationTicks() { return durationTicks; }
    public int getAge() { return age; }

    public int getAlpha() { return (color >> 24) & 0xFF; }
    public int getRed() { return (color >> 16) & 0xFF; }
    public int getGreen() { return (color >> 8) & 0xFF; }
    public int getBlue() { return color & 0xFF; }
}
