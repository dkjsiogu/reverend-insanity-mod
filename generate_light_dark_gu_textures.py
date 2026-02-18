#!/usr/bin/env python3
"""生成光道/暗道蛊虫物品贴图 64x64 - 改进版"""

from PIL import Image, ImageDraw
import math
import random

OUTPUT_DIR = "src/main/resources/assets/reverend_insanity/textures/item/"
SIZE = 64

random.seed(42)


def clamp(v, lo=0, hi=255):
    return max(lo, min(hi, int(v)))


def dist(x1, y1, x2, y2):
    return math.sqrt((x1 - x2) ** 2 + (y1 - y2) ** 2)


def draw_glow(img, cx, cy, radius, color, intensity=1.0):
    """在img上绘制径向光晕，不覆盖已有不透明像素的颜色"""
    px = img.load()
    w, h = img.size
    for y in range(max(0, int(cy - radius)), min(h, int(cy + radius + 1))):
        for x in range(max(0, int(cx - radius)), min(w, int(cx + radius + 1))):
            d = dist(x, y, cx, cy)
            if d < radius:
                factor = (1.0 - d / radius) ** 2.0 * intensity
                r, g, b, a = px[x, y]
                if a < 20:
                    nr = clamp(color[0] * factor)
                    ng = clamp(color[1] * factor)
                    nb = clamp(color[2] * factor)
                    na = clamp(factor * 140)
                    if na > 6:
                        px[x, y] = (nr, ng, nb, na)


def draw_dark_aura(img, cx, cy, radius, color, intensity=1.0):
    """绘制暗色气场/雾气"""
    px = img.load()
    w, h = img.size
    rng = random.Random(99)
    for y in range(max(0, int(cy - radius)), min(h, int(cy + radius + 1))):
        for x in range(max(0, int(cx - radius)), min(w, int(cx + radius + 1))):
            d = dist(x, y, cx, cy)
            if d < radius:
                noise = rng.random() * 0.4 + 0.6
                factor = (1.0 - d / radius) ** 1.2 * intensity * noise
                r, g, b, a = px[x, y]
                if a < 15:
                    nr = clamp(color[0] * factor * 0.5)
                    ng = clamp(color[1] * factor * 0.5)
                    nb = clamp(color[2] * factor * 0.5)
                    na = clamp(factor * 90)
                    if na > 6:
                        px[x, y] = (nr, ng, nb, na)


def set_px(img, x, y, color):
    if 0 <= x < SIZE and 0 <= y < SIZE:
        img.putpixel((x, y), color)


# ============================================================
# 1. light_beam_gu.png - 光束蛊：金黄色发光细长蛊虫
# ============================================================
def generate_light_beam_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    cx, cy = 32, 34

    gold_bright = (255, 220, 60, 255)
    gold_mid = (230, 190, 30, 255)
    gold_dark = (190, 155, 15, 255)
    gold_outline = (160, 130, 10, 255)

    # 身体 - 细长光束形，从头到尾
    for y in range(18, 52):
        progress = (y - 18) / 34.0
        if progress < 0.1:
            width = 2
        elif progress < 0.25:
            width = 2 + int((progress - 0.1) / 0.15 * 3)
        elif progress < 0.7:
            width = 5
        elif progress < 0.85:
            width = 5 - int((progress - 0.7) / 0.15 * 2)
        else:
            width = 3 - int((progress - 0.85) / 0.15 * 2)
        width = max(1, min(width, 6))

        for dx in range(-width, width + 1):
            px_x = cx + dx
            adx = abs(dx)
            if adx == width:
                set_px(img, px_x, y, gold_outline)
            elif adx == width - 1 and width > 2:
                set_px(img, px_x, y, gold_dark)
            elif adx <= 1:
                # 中心亮线
                set_px(img, px_x, y, gold_bright)
            else:
                set_px(img, px_x, y, gold_mid)

    # 身体分节线
    for seg_y in [24, 30, 36, 42, 47]:
        for dx in range(-4, 5):
            px_x = cx + dx
            if 0 <= px_x < SIZE:
                cur = img.getpixel((px_x, seg_y))
                if cur[3] > 100:
                    set_px(img, px_x, seg_y, (180, 145, 10, 255))

    # 头部
    head_bright = (255, 230, 80, 255)
    head_mid = (240, 210, 50, 255)
    head_outline = (180, 145, 10, 255)
    for dy in range(-4, 2):
        for dx in range(-4, 5):
            d = math.sqrt(dx * dx + dy * dy)
            if d <= 4.2:
                px_x, py = cx + dx, 17 + dy
                if d > 3.5:
                    set_px(img, px_x, py, head_outline)
                elif d > 2:
                    set_px(img, px_x, py, head_mid)
                else:
                    set_px(img, px_x, py, head_bright)

    # 眼睛 - 白色带金瞳
    set_px(img, 30, 15, (255, 255, 255, 255))
    set_px(img, 34, 15, (255, 255, 255, 255))
    set_px(img, 30, 16, (200, 160, 0, 255))
    set_px(img, 34, 16, (200, 160, 0, 255))

    # 透明翅膀 - 左右各一对
    wing_outline = (220, 195, 60, 160)
    wing_fill = (255, 240, 130, 90)
    wing_vein = (240, 210, 50, 130)

    # 上翅
    for side in [-1, 1]:
        for i in range(10):
            for j in range(5):
                wx = cx + side * (4 + i)
                wy = 22 + j - i // 3
                if 0 <= wx < SIZE and 0 <= wy < SIZE:
                    # 翅膀椭圆范围
                    d = math.sqrt((i / 10.0) ** 2 + (j / 5.0) ** 2)
                    if d < 1.0:
                        if d > 0.85:
                            set_px(img, wx, wy, wing_outline)
                        elif j == 0 or (i == j * 2):
                            set_px(img, wx, wy, wing_vein)
                        else:
                            set_px(img, wx, wy, wing_fill)

    # 下翅（稍小）
    for side in [-1, 1]:
        for i in range(7):
            for j in range(3):
                wx = cx + side * (4 + i)
                wy = 33 + j
                if 0 <= wx < SIZE and 0 <= wy < SIZE:
                    d = math.sqrt((i / 7.0) ** 2 + (j / 3.0) ** 2)
                    if d < 1.0:
                        set_px(img, wx, wy, (240, 220, 100, 70))

    # 腿部
    leg_color = (200, 170, 30, 220)
    for i in range(3):
        by = 26 + i * 6
        for seg in range(4):
            set_px(img, cx - 5 - seg, by + seg, leg_color)
            set_px(img, cx + 5 + seg, by + seg, leg_color)

    # 触角
    for i in range(4):
        set_px(img, 29 - i, 13 - i, (230, 200, 50, clamp(230 - i * 40)))
        set_px(img, 35 + i, 13 - i, (230, 200, 50, clamp(230 - i * 40)))

    # 尾部发光点
    set_px(img, cx, 52, (255, 240, 120, 220))
    set_px(img, cx, 53, (255, 235, 100, 160))

    # 柔和金色光晕（只影响透明区域）
    draw_glow(img, cx, cy, 20, (255, 210, 50), 0.4)

    img.save(OUTPUT_DIR + "light_beam_gu.png")
    print("Generated light_beam_gu.png")


# ============================================================
# 2. radiance_gu.png - 耀光蛊：圆形明亮白金色蛊虫
# ============================================================
def generate_radiance_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    cx, cy = 32, 32

    # 光芒射线 - 先画射线，再画身体覆盖上去
    for angle_deg in range(0, 360, 30):
        angle = math.radians(angle_deg)
        ray_len = 12 if angle_deg % 60 == 0 else 7
        for r in range(13, 13 + ray_len):
            x = int(cx + math.cos(angle) * r)
            y = int(cy + math.sin(angle) * r)
            intensity = 1.0 - (r - 13) / ray_len
            a = clamp(intensity * 220)
            c_val = clamp(200 + intensity * 55)
            set_px(img, x, y, (c_val, c_val, clamp(c_val * 0.75), a))
            # 加宽主射线
            if ray_len == 12 and r < 20:
                x2 = int(cx + math.cos(angle + 0.06) * r)
                y2 = int(cy + math.sin(angle + 0.06) * r)
                set_px(img, x2, y2, (c_val, c_val, clamp(c_val * 0.7), clamp(a * 0.6)))
                x3 = int(cx + math.cos(angle - 0.06) * r)
                y3 = int(cy + math.sin(angle - 0.06) * r)
                set_px(img, x3, y3, (c_val, c_val, clamp(c_val * 0.7), clamp(a * 0.6)))

    # 圆形身体 - 白金色硬壳
    shell_bright = (245, 235, 195, 255)
    shell_mid = (225, 215, 165, 255)
    shell_dark = (195, 185, 135, 255)
    shell_outline = (170, 155, 100, 255)

    for dy in range(-12, 13):
        for dx in range(-12, 13):
            d = math.sqrt(dx * dx + dy * dy)
            if d <= 12:
                px_x, px_y = cx + dx, cy + dy
                # 球形高光
                highlight = max(0.3, min(1.0, 1.0 - (dx + dy) / 24.0))
                if d > 11:
                    set_px(img, px_x, px_y, shell_outline)
                elif d > 9:
                    r = clamp(shell_dark[0] * highlight + 30)
                    g = clamp(shell_dark[1] * highlight + 25)
                    b = clamp(shell_dark[2] * highlight + 15)
                    set_px(img, px_x, px_y, (r, g, b, 255))
                elif d > 5:
                    r = clamp(shell_mid[0] * highlight + 20)
                    g = clamp(shell_mid[1] * highlight + 15)
                    b = clamp(shell_mid[2] * highlight + 10)
                    set_px(img, px_x, px_y, (r, g, b, 255))
                else:
                    # 明亮核心
                    core_f = 1.0 - d / 5.0
                    r = clamp(shell_bright[0] + core_f * 10)
                    g = clamp(shell_bright[1] + core_f * 15)
                    b = clamp(shell_bright[2] + core_f * 40)
                    set_px(img, px_x, px_y, (r, g, b, 255))

    # 壳上弧形纹路
    for angle_deg in range(0, 360, 45):
        angle = math.radians(angle_deg)
        for r in range(7, 11):
            x = int(cx + math.cos(angle) * r)
            y = int(cy + math.sin(angle) * r)
            if 0 <= x < SIZE and 0 <= y < SIZE:
                set_px(img, x, y, (180, 165, 110, 255))

    # 外圈光环线
    for angle_deg in range(0, 360, 2):
        angle = math.radians(angle_deg)
        x = int(cx + math.cos(angle) * 14)
        y = int(cy + math.sin(angle) * 14)
        set_px(img, x, y, (240, 230, 180, 200))

    # 眼睛
    set_px(img, 29, 30, (255, 255, 255, 255))
    set_px(img, 35, 30, (255, 255, 255, 255))
    set_px(img, 29, 31, (220, 200, 130, 255))
    set_px(img, 35, 31, (220, 200, 130, 255))

    # 触角
    set_px(img, 28, 19, (220, 210, 150, 230))
    set_px(img, 27, 18, (220, 210, 150, 200))
    set_px(img, 36, 19, (220, 210, 150, 230))
    set_px(img, 37, 18, (220, 210, 150, 200))

    # 短腿
    for i in range(3):
        lx = 26 + i * 4
        set_px(img, lx, 44, (200, 190, 140, 220))
        set_px(img, lx, 45, (200, 190, 140, 190))
        set_px(img, lx + 1, 44, (200, 190, 140, 220))

    # 光晕
    draw_glow(img, cx, cy, 24, (255, 245, 180), 0.5)

    img.save(OUTPUT_DIR + "radiance_gu.png")
    print("Generated radiance_gu.png")


# ============================================================
# 3. blazing_light_gu.png - 灼光蛊：大型金白色二转蛊虫
# ============================================================
def generate_blazing_light_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    cx, cy = 32, 33

    body_bright = (255, 240, 150, 255)
    body_mid = (240, 215, 100, 255)
    body_dark = (210, 185, 60, 255)
    body_outline = (180, 155, 40, 255)

    # 大型椭圆身体
    for dy in range(-14, 16):
        for dx in range(-9, 10):
            ex = (dx / 9.0) ** 2
            ey = (dy / 15.0) ** 2
            d = math.sqrt(ex + ey)
            if d <= 1.0:
                px_x, px_y = cx + dx, cy + dy
                highlight = max(0.4, min(1.0, 1.0 - (dx + dy) / 20.0))
                if d > 0.88:
                    set_px(img, px_x, px_y, body_outline)
                elif d > 0.65:
                    r = clamp(body_dark[0] * highlight + 15)
                    g = clamp(body_dark[1] * highlight + 10)
                    b = clamp(body_dark[2] * highlight + 5)
                    set_px(img, px_x, px_y, (r, g, b, 255))
                elif d > 0.3:
                    r = clamp(body_mid[0] * highlight + 10)
                    g = clamp(body_mid[1] * highlight + 10)
                    b = clamp(body_mid[2] * highlight + 10)
                    set_px(img, px_x, px_y, (r, g, b, 255))
                else:
                    set_px(img, px_x, px_y, body_bright)

    # 太阳纹路 - 身体上放射状金线
    for angle_deg in range(0, 360, 30):
        angle = math.radians(angle_deg)
        for r in range(3, 13):
            x = int(cx + math.cos(angle) * r * 0.55)
            y = int(cy + math.sin(angle) * r)
            if 0 <= x < SIZE and 0 <= y < SIZE:
                cur = img.getpixel((x, y))
                if cur[3] > 200:
                    set_px(img, x, y, (255, 245, 180, 255))

    # 身体分节 - 水平线
    for seg_y in [23, 28, 33, 38, 43]:
        for dx in range(-8, 9):
            px_x = cx + dx
            if 0 <= px_x < SIZE and 0 <= seg_y < SIZE:
                cur = img.getpixel((px_x, seg_y))
                if cur[3] > 200:
                    set_px(img, px_x, seg_y, (195, 170, 50, 255))

    # 头部 - 精致大头
    for dy in range(-5, 3):
        for dx in range(-6, 7):
            d = math.sqrt(dx * dx + dy * dy)
            if d <= 6:
                px_x, py = cx + dx, 16 + dy
                if d > 5:
                    set_px(img, px_x, py, (190, 165, 50, 255))
                elif d > 3:
                    set_px(img, px_x, py, (235, 215, 110, 255))
                else:
                    set_px(img, px_x, py, (255, 242, 160, 255))

    # 眼睛 - 大而明亮
    for ex_off in [-3, 3]:
        ex = cx + ex_off
        set_px(img, ex, 14, (255, 255, 255, 255))
        set_px(img, ex + 1, 14, (255, 255, 255, 255))
        set_px(img, ex, 15, (255, 200, 30, 255))
        set_px(img, ex + 1, 15, (255, 200, 30, 255))

    # 宽大发光翅膀
    wing_outline = (230, 210, 80, 170)
    wing_fill = (255, 245, 150, 100)
    wing_vein = (250, 230, 100, 150)

    for side in [-1, 1]:
        # 上翅 - 宽大
        for i in range(15):
            for j in range(7):
                wx = cx + side * (6 + i)
                wy = 20 + j - i // 4
                if 0 <= wx < SIZE and 0 <= wy < SIZE:
                    d = math.sqrt((i / 15.0) ** 2 + (j / 7.0) ** 2)
                    if d < 1.0:
                        if d > 0.88:
                            set_px(img, wx, wy, wing_outline)
                        elif j == 0 or j == 3 or (i % 4 == 0 and j < 5):
                            set_px(img, wx, wy, wing_vein)
                        else:
                            set_px(img, wx, wy, wing_fill)

        # 下翅
        for i in range(11):
            for j in range(5):
                wx = cx + side * (5 + i)
                wy = 34 + j
                if 0 <= wx < SIZE and 0 <= wy < SIZE:
                    d = math.sqrt((i / 11.0) ** 2 + (j / 5.0) ** 2)
                    if d < 1.0:
                        if d > 0.88:
                            set_px(img, wx, wy, (220, 205, 80, 140))
                        else:
                            set_px(img, wx, wy, (250, 240, 140, 80))

    # 精致腿部
    leg_color = (210, 185, 60, 230)
    for i in range(4):
        by = 26 + i * 5
        for seg in range(4):
            for side in [-1, 1]:
                lx = cx + side * (8 + seg)
                ly = by + seg
                set_px(img, lx, ly, leg_color)

    # 长触角
    for i in range(6):
        set_px(img, 27 - i, 11 - i, (240, 215, 80, clamp(240 - i * 35)))
        set_px(img, 37 + i, 11 - i, (240, 215, 80, clamp(240 - i * 35)))

    # 尾部
    for i in range(3):
        tw = 3 - i
        for dx in range(-tw, tw + 1):
            set_px(img, cx + dx, 48 + i, (220, 195, 80, clamp(230 - i * 40)))

    # 光晕
    draw_glow(img, cx, cy, 26, (255, 235, 100), 0.45)
    draw_glow(img, cx, 16, 10, (255, 250, 180), 0.4)

    img.save(OUTPUT_DIR + "blazing_light_gu.png")
    print("Generated blazing_light_gu.png")


# ============================================================
# 4. dark_bolt_gu.png - 暗箭蛊：暗紫黑色箭形蛊虫
# ============================================================
def generate_dark_bolt_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    cx, cy = 32, 32

    body_core = (55, 20, 75, 255)
    body_mid = (38, 12, 52, 255)
    body_dark = (22, 6, 32, 255)
    body_outline = (15, 3, 22, 255)

    # 箭形身体
    for y in range(12, 54):
        progress = (y - 12) / 42.0
        if progress < 0.12:
            width = max(1, int(progress / 0.12 * 4))
        elif progress < 0.45:
            width = 4 + int((progress - 0.12) / 0.33 * 6)
        elif progress < 0.52:
            width = 10
        elif progress < 0.62:
            width = 10 - int((progress - 0.52) / 0.1 * 6)
        else:
            width = max(2, 4 - int((progress - 0.62) / 0.38 * 2))

        for dx in range(-width, width + 1):
            px_x = cx + dx
            adx = abs(dx)
            if 0 <= px_x < SIZE:
                if adx >= width:
                    set_px(img, px_x, y, body_outline)
                elif adx >= width - 2:
                    set_px(img, px_x, y, body_dark)
                elif adx <= 1:
                    set_px(img, px_x, y, body_core)
                else:
                    set_px(img, px_x, y, body_mid)

    # 箭头侧翼
    for i in range(7):
        for j in range(2):
            a = clamp(230 - i * 28)
            c = (45, 15, 60, a)
            set_px(img, cx - 9 - i, 28 + j + i, c)
            set_px(img, cx + 9 + i, 28 + j + i, c)

    # 身体暗紫能量纹路
    for y in range(16, 48, 3):
        for dx in range(-3, 4):
            px_x = cx + dx
            if 0 <= px_x < SIZE and 0 <= y < SIZE:
                cur = img.getpixel((px_x, y))
                if cur[3] > 200:
                    set_px(img, px_x, y, (85, 35, 110, 255))

    # 中心能量线
    for y in range(14, 50):
        if 0 <= y < SIZE:
            cur = img.getpixel((cx, y))
            if cur[3] > 200:
                set_px(img, cx, y, (100, 40, 130, 255))

    # 头部区域
    for dy in range(-3, 2):
        for dx in range(-3, 4):
            d = math.sqrt(dx * dx + dy * dy)
            if d <= 3.5:
                px_x, py = cx + dx, 13 + dy
                if 0 <= px_x < SIZE and 0 <= py < SIZE:
                    set_px(img, px_x, py, (35, 12, 48, 255))

    # 眼睛 - 暗红发光
    for ex in [30, 34]:
        set_px(img, ex, 11, (200, 40, 30, 255))
        set_px(img, ex, 12, (240, 60, 50, 255))

    # 小型暗翅
    for side in [-1, 1]:
        for i in range(6):
            for j in range(2):
                wx = cx + side * (6 + i)
                wy = 22 + j
                a = clamp(170 - i * 25)
                set_px(img, wx, wy, (50, 20, 68, a))

    # 腿
    leg_c = (35, 12, 48, 210)
    for i in range(3):
        by = 25 + i * 6
        for s in range(4):
            set_px(img, cx - 6 - s, by + s, leg_c)
            set_px(img, cx + 6 + s, by + s, leg_c)

    # 尾部尖刺
    for i in range(5):
        set_px(img, cx, 53 + i, (70, 28, 90, clamp(220 - i * 40)))
        if i < 3:
            set_px(img, cx - 1, 53 + i, (50, 18, 65, clamp(160 - i * 40)))
            set_px(img, cx + 1, 53 + i, (50, 18, 65, clamp(160 - i * 40)))

    # 暗紫雾气
    draw_dark_aura(img, cx, cy, 22, (90, 25, 130), 0.55)

    img.save(OUTPUT_DIR + "dark_bolt_gu.png")
    print("Generated dark_bolt_gu.png")


# ============================================================
# 5. shadow_cloak_gu.png - 暗影斗篷蛊：黑色烟雾蛊虫
# ============================================================
def generate_shadow_cloak_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    cx, cy = 32, 32

    rng = random.Random(77)

    # 烟雾体 - 不规则斗篷形状：上窄中宽下散
    for y in range(8, 58):
        for x in range(8, 56):
            dx = x - cx
            dy = y - cy

            # 斗篷形状：上方窄（头），中间宽（肩），下方散开（斗篷下摆）
            progress = (y - 8) / 50.0
            if progress < 0.2:
                max_w = 6 + progress / 0.2 * 4
            elif progress < 0.45:
                max_w = 10 + (progress - 0.2) / 0.25 * 6
            elif progress < 0.7:
                max_w = 16
            else:
                max_w = 16 + (progress - 0.7) / 0.3 * 4

            noise_val = math.sin(dx * 0.7 + y * 0.3) * 2 + rng.random() * 2.5
            if abs(dx) < max_w + noise_val:
                d_from_center = abs(dx) / max(1, max_w)

                if d_from_center < 0.3:
                    r, g, b = 10, 5, 14
                    a = clamp(245 * (rng.random() * 0.2 + 0.8))
                elif d_from_center < 0.6:
                    r, g, b = 18, 10, 24
                    a = clamp(220 * (rng.random() * 0.25 + 0.75))
                elif d_from_center < 0.85:
                    r, g, b = 28, 15, 35
                    a = clamp(170 * (rng.random() * 0.3 + 0.7))
                else:
                    r, g, b = 35, 18, 42
                    a = clamp(100 * (rng.random() * 0.4 + 0.3))

                # 下摆逐渐消散
                if progress > 0.75:
                    fade = (progress - 0.75) / 0.25
                    a = clamp(a * (1.0 - fade * 0.7) * (rng.random() * 0.4 + 0.6))

                if a > 8:
                    set_px(img, x, y, (r, g, b, a))

    # 烟雾触须 - 底部
    rng2 = random.Random(88)
    for tendril in range(7):
        tx = cx - 10 + tendril * 3 + rng2.randint(-1, 1)
        for i in range(10):
            ty = 50 + i
            tx_off = tx + rng2.randint(-1, 1)
            a = clamp(120 - i * 12)
            if a > 5:
                set_px(img, tx_off, ty, (18, 8, 22, a))
                set_px(img, tx_off + 1, ty, (22, 10, 28, clamp(a * 0.5)))

    # 暗红色眼睛
    eye_positions = [(28, 25), (36, 25)]
    for ex, ey in eye_positions:
        # 外圈暗红辉光
        for dy in range(-3, 4):
            for dx in range(-3, 4):
                d = math.sqrt(dx * dx + dy * dy)
                if d <= 3.2:
                    px_x, px_y = ex + dx, ey + dy
                    if d <= 1.0:
                        set_px(img, px_x, px_y, (230, 50, 35, 255))
                    elif d <= 2.0:
                        set_px(img, px_x, px_y, (160, 25, 18, 240))
                    else:
                        set_px(img, px_x, px_y, (90, 12, 8, clamp(160 - d * 30)))

    # 眼睛光晕 - 微弱红光
    draw_glow(img, 28, 25, 6, (160, 25, 15), 0.35)
    draw_glow(img, 36, 25, 6, (160, 25, 15), 0.35)

    # 整体暗气场
    draw_dark_aura(img, cx, cy, 26, (35, 12, 45), 0.4)

    img.save(OUTPUT_DIR + "shadow_cloak_gu.png")
    print("Generated shadow_cloak_gu.png")


# ============================================================
# 6. abyss_devour_gu.png - 深渊吞噬蛊：大型深黑色蛊虫
# ============================================================
def generate_abyss_devour_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    cx, cy = 32, 32

    body_core = (8, 2, 12, 255)
    body_mid = (18, 6, 26, 255)
    body_dark = (28, 10, 38, 255)
    body_outline = (40, 16, 52, 255)

    # 大型粗壮身体
    for dy in range(-14, 18):
        for dx in range(-11, 12):
            ex = (dx / 11.0) ** 2
            ey = (dy / 16.0) ** 2
            d = math.sqrt(ex + ey)
            if d <= 1.0:
                px_x, px_y = cx + dx, cy + dy
                if d > 0.88:
                    set_px(img, px_x, px_y, body_outline)
                elif d > 0.65:
                    set_px(img, px_x, px_y, body_dark)
                elif d > 0.3:
                    set_px(img, px_x, px_y, body_mid)
                else:
                    set_px(img, px_x, px_y, body_core)

    # 大口 - 深渊之口（椭圆大嘴）
    mouth_cx, mouth_cy = cx, cy - 7
    for dy in range(-7, 5):
        for dx in range(-9, 10):
            d = math.sqrt((dx / 9.0) ** 2 + (dy / 6.0) ** 2)
            if d <= 1.0:
                px_x, px_y = mouth_cx + dx, mouth_cy + dy
                if d < 0.45:
                    set_px(img, px_x, px_y, (0, 0, 0, 255))
                elif d < 0.7:
                    set_px(img, px_x, px_y, (10, 2, 16, 255))
                else:
                    # 口缘 - 锯齿状
                    if (dx + dy) % 3 == 0:
                        set_px(img, px_x, px_y, (65, 25, 80, 255))
                    else:
                        set_px(img, px_x, px_y, (38, 14, 50, 255))

    # 口内暗紫色漩涡
    for angle_deg in range(0, 360, 6):
        angle = math.radians(angle_deg)
        for r in range(1, 5):
            spiral = angle + r * 0.5
            x = int(mouth_cx + math.cos(spiral) * r * 1.2)
            y = int(mouth_cy + math.sin(spiral) * r * 0.8)
            if 0 <= x < SIZE and 0 <= y < SIZE:
                cur = img.getpixel((x, y))
                if cur[3] > 200 and cur[0] < 30:
                    intensity = 1.0 - r / 5.0
                    pr = clamp(90 * intensity)
                    pg = clamp(15 * intensity)
                    pb = clamp(130 * intensity)
                    set_px(img, x, y, (pr, pg, pb, 255))

    # 上颚牙齿
    teeth_c = (75, 35, 88, 255)
    teeth_tip = (110, 55, 125, 255)
    for i in range(6):
        tx = mouth_cx - 7 + i * 3
        for j in range(3):
            ty = mouth_cy - 6 - j
            if j == 2:
                set_px(img, tx, ty, teeth_tip)
            else:
                set_px(img, tx, ty, teeth_c)

    # 下颚牙齿
    for i in range(5):
        tx = mouth_cx - 6 + i * 3
        for j in range(2):
            ty = mouth_cy + 4 + j
            if j == 1:
                set_px(img, tx, ty, teeth_tip)
            else:
                set_px(img, tx, ty, teeth_c)

    # 身体纹路 - 暗色分节
    for seg_y in [30, 35, 40, 45]:
        for dx in range(-10, 11):
            px_x = cx + dx
            if 0 <= px_x < SIZE and 0 <= seg_y < SIZE:
                cur = img.getpixel((px_x, seg_y))
                if cur[3] > 200:
                    set_px(img, px_x, seg_y, (12, 3, 18, 255))

    # 小眼睛 - 嘴两侧上方
    for ex, ey in [(22, 20), (42, 20)]:
        set_px(img, ex, ey, (130, 35, 160, 255))
        set_px(img, ex + 1, ey, (130, 35, 160, 255))
        set_px(img, ex, ey + 1, (90, 22, 110, 255))
        set_px(img, ex + 1, ey + 1, (90, 22, 110, 255))

    # 粗壮节肢腿
    leg_c = (30, 10, 40, 240)
    leg_joint = (50, 20, 62, 240)
    for i in range(4):
        by = 30 + i * 4
        for seg in range(5):
            for side in [-1, 1]:
                lx = cx + side * (10 + seg)
                ly = by + seg
                if seg == 2:
                    set_px(img, lx, ly, leg_joint)
                else:
                    set_px(img, lx, ly, leg_c)
                # 加粗
                set_px(img, lx, ly - 1, (25, 8, 34, 160))

    # 短粗触角
    for i in range(4):
        set_px(img, 24 - i, 15 - i, (42, 16, 55, clamp(230 - i * 45)))
        set_px(img, 40 + i, 15 - i, (42, 16, 55, clamp(230 - i * 45)))

    # 尾部
    for i in range(4):
        tw = max(1, 5 - i)
        for dx in range(-tw, tw + 1):
            set_px(img, cx + dx, 48 + i, (18, 5, 25, clamp(230 - i * 35)))

    # 暗紫色气场
    draw_dark_aura(img, cx, cy, 26, (75, 18, 105), 0.6)
    # 嘴部微弱紫光晕
    draw_glow(img, mouth_cx, mouth_cy, 7, (70, 12, 90), 0.25)

    img.save(OUTPUT_DIR + "abyss_devour_gu.png")
    print("Generated abyss_devour_gu.png")


# ============================================================
if __name__ == "__main__":
    print("Generating light/dark path Gu worm textures (improved)...")
    generate_light_beam_gu()
    generate_radiance_gu()
    generate_blazing_light_gu()
    generate_dark_bolt_gu()
    generate_shadow_cloak_gu()
    generate_abyss_devour_gu()
    print("All 6 textures generated successfully!")
