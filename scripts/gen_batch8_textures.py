#!/usr/bin/env python3
"""Batch 8 蛊虫物品贴图生成: 春秋蝉、肉骨蛊、移形蛊"""

from PIL import Image, ImageDraw
import math
import random

OUTPUT_DIR = "/mnt/e/code/mod/gu/src/main/resources/assets/reverend_insanity/textures/item/"
SIZE = 64

random.seed(42)


def blend(c1, c2, t):
    """混合两个颜色, t=0返回c1, t=1返回c2"""
    r = int(c1[0] + (c2[0] - c1[0]) * t)
    g = int(c1[1] + (c2[1] - c1[1]) * t)
    b = int(c1[2] + (c2[2] - c1[2]) * t)
    a = int(c1[3] + (c2[3] - c1[3]) * t) if len(c1) > 3 and len(c2) > 3 else 255
    return (max(0, min(255, r)), max(0, min(255, g)), max(0, min(255, b)), max(0, min(255, a)))


def dist(x1, y1, x2, y2):
    return math.sqrt((x1 - x2) ** 2 + (y1 - y2) ** 2)


def draw_ellipse_filled(img, cx, cy, rx, ry, color):
    """填充椭圆"""
    for y in range(max(0, int(cy - ry)), min(SIZE, int(cy + ry + 1))):
        for x in range(max(0, int(cx - rx)), min(SIZE, int(cx + rx + 1))):
            dx = (x - cx) / rx if rx > 0 else 0
            dy = (y - cy) / ry if ry > 0 else 0
            if dx * dx + dy * dy <= 1.0:
                img.putpixel((x, y), color)


def draw_ellipse_outline(img, cx, cy, rx, ry, color, thickness=1):
    """椭圆轮廓"""
    for y in range(max(0, int(cy - ry - thickness)), min(SIZE, int(cy + ry + thickness + 1))):
        for x in range(max(0, int(cx - rx - thickness)), min(SIZE, int(cx + rx + thickness + 1))):
            dx = (x - cx) / rx if rx > 0 else 0
            dy = (y - cy) / ry if ry > 0 else 0
            d = dx * dx + dy * dy
            if abs(d - 1.0) < thickness * 0.15:
                img.putpixel((x, y), color)


def set_pixel_safe(img, x, y, color):
    if 0 <= x < SIZE and 0 <= y < SIZE:
        img.putpixel((int(x), int(y)), color)


def alpha_composite_pixel(img, x, y, color):
    """带Alpha混合的像素绘制"""
    x, y = int(x), int(y)
    if 0 <= x < SIZE and 0 <= y < SIZE:
        existing = img.getpixel((x, y))
        a = color[3] / 255.0
        r = int(existing[0] * (1 - a) + color[0] * a)
        g = int(existing[1] * (1 - a) + color[1] * a)
        b = int(existing[2] * (1 - a) + color[2] * a)
        na = min(255, existing[3] + color[3])
        img.putpixel((x, y), (r, g, b, na))


def draw_line(img, x1, y1, x2, y2, color):
    """Bresenham画线"""
    dx = abs(x2 - x1)
    dy = abs(y2 - y1)
    steps = max(dx, dy, 1)
    for i in range(steps + 1):
        t = i / steps
        x = int(x1 + (x2 - x1) * t)
        y = int(y1 + (y2 - y1) * t)
        set_pixel_safe(img, x, y, color)


# ============================================================
# 1. 春秋蝉 (Spring Autumn Cicada) - 六转仙蛊
# ============================================================
def generate_spring_autumn_cicada():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))

    # 颜色定义
    body_dark = (105, 80, 15, 255)       # 深棕黄
    body_main = (139, 105, 20, 255)      # 棕黄主色
    body_mid = (166, 124, 0, 255)        # 中棕黄
    body_light = (196, 160, 53, 255)     # 亮棕黄
    body_highlight = (220, 185, 80, 255) # 高光
    ring_dark = (90, 65, 10, 255)        # 年轮纹深色
    ring_light = (180, 145, 50, 255)     # 年轮纹浅色
    wing_base = (255, 215, 0, 80)        # 翅膀基色半透明
    wing_vein = (218, 180, 50, 120)      # 翅脉
    wing_highlight = (255, 235, 130, 60) # 翅膀高光
    glow_color = (255, 215, 0, 40)       # 金色光晕
    rune_color = (255, 230, 100, 180)    # 时间符文光点
    eye_color = (220, 50, 50, 255)       # 眼睛
    leg_color = (100, 75, 15, 255)       # 腿

    # --- 金色光晕背景 ---
    cx_body, cy_body = 32, 35
    for y in range(SIZE):
        for x in range(SIZE):
            d = dist(x, y, cx_body, cy_body)
            if d < 28:
                a = int(35 * (1 - d / 28))
                alpha_composite_pixel(img, x, y, (255, 215, 0, a))

    # --- 翅膀 (先画翅膀在身体下面) ---
    # 上翅 - 大片叶形
    wing_cx, wing_cy = 30, 22
    for y in range(8, 40):
        for x in range(8, 56):
            # 上翅形状: 宽大的叶片形
            dx = (x - wing_cx) / 20.0
            dy = (y - wing_cy) / 14.0
            # 不对称叶形
            shape = dx * dx + dy * dy
            # 让翅膀向右后方延展
            skew = dx * 0.3
            if shape + skew < 1.0 and y < cy_body - 2:
                edge_dist = 1.0 - (shape + skew)
                # 翅膀基色
                base_a = int(70 * min(1.0, edge_dist * 3))
                alpha_composite_pixel(img, x, y, (255, 220, 80, base_a))

                # 叶脉纹理 - 主脉
                vein_val = abs(y - wing_cy) % 5
                if vein_val < 1:
                    alpha_composite_pixel(img, x, y, (218, 180, 50, 90))
                # 横脉
                vein_h = abs(x - wing_cx) % 6
                if vein_h < 1 and edge_dist > 0.2:
                    alpha_composite_pixel(img, x, y, (200, 170, 40, 70))

                # 翅膀边缘光
                if edge_dist < 0.15:
                    alpha_composite_pixel(img, x, y, (255, 240, 150, 50))

    # 下翅 - 较小
    wing2_cx, wing2_cy = 32, 30
    for y in range(18, 44):
        for x in range(12, 52):
            dx = (x - wing2_cx) / 16.0
            dy = (y - wing2_cy) / 10.0
            shape = dx * dx + dy * dy
            if shape < 1.0 and y < cy_body + 2:
                edge_dist = 1.0 - shape
                base_a = int(55 * min(1.0, edge_dist * 3))
                alpha_composite_pixel(img, x, y, (255, 210, 60, base_a))
                # 脉纹
                if abs(y - wing2_cy) % 4 < 1:
                    alpha_composite_pixel(img, x, y, (200, 165, 40, 60))

    # --- 蝉身体 (侧面视角) ---
    # 头部 - 圆形
    head_cx, head_cy = 22, 33
    head_r = 7
    for y in range(max(0, head_cy - head_r), min(SIZE, head_cy + head_r + 1)):
        for x in range(max(0, head_cx - head_r), min(SIZE, head_cx + head_r + 1)):
            d = dist(x, y, head_cx, head_cy)
            if d <= head_r:
                t = d / head_r
                # 从亮到暗的渐变
                if x < head_cx:
                    c = blend(body_light, body_main, t)
                else:
                    c = blend(body_highlight, body_mid, t * 0.8)
                # 底部阴影
                if y > head_cy + 3:
                    c = blend(c, body_dark, 0.3)
                set_pixel_safe(img, x, y, c)

    # 眼睛
    for dy in range(-1, 2):
        for dx in range(-1, 2):
            if abs(dx) + abs(dy) <= 1:
                set_pixel_safe(img, 19 + dx, 31 + dy, eye_color)
    # 眼睛高光
    set_pixel_safe(img, 18, 30, (255, 150, 150, 255))

    # 胸部 - 椭圆
    thorax_cx, thorax_cy = 30, 35
    for y in range(max(0, thorax_cy - 7), min(SIZE, thorax_cy + 7)):
        for x in range(max(0, thorax_cx - 6), min(SIZE, thorax_cx + 6)):
            dx = (x - thorax_cx) / 6.0
            dy = (y - thorax_cy) / 6.5
            if dx * dx + dy * dy <= 1.0:
                t = dist(x, y, thorax_cx, thorax_cy) / 7.0
                c = blend(body_mid, body_dark, t * 0.6)
                # 上面亮
                if y < thorax_cy:
                    c = blend(c, body_light, 0.2)
                set_pixel_safe(img, x, y, c)

    # 腹部 - 长椭圆，带年轮纹理
    abdomen_cx, abdomen_cy = 42, 37
    abd_rx, abd_ry = 10, 6
    for y in range(max(0, abdomen_cy - abd_ry - 1), min(SIZE, abdomen_cy + abd_ry + 1)):
        for x in range(max(0, abdomen_cx - abd_rx - 1), min(SIZE, abdomen_cx + abd_rx + 1)):
            dx = (x - abdomen_cx) / abd_rx
            dy = (y - abdomen_cy) / abd_ry
            if dx * dx + dy * dy <= 1.0:
                d = dist(x, y, abdomen_cx, abdomen_cy)
                t = d / max(abd_rx, abd_ry)

                # 基础颜色渐变
                c = blend(body_main, body_dark, t * 0.5)
                if y < abdomen_cy:
                    c = blend(c, body_light, 0.15)

                # 年轮纹理 - 同心弧线
                ring_d = dist(x, y, abdomen_cx - 3, abdomen_cy)
                ring_val = ring_d % 3.5
                if ring_val < 0.8:
                    c = blend(c, ring_dark, 0.4)
                elif ring_val < 1.2:
                    c = blend(c, ring_light, 0.2)

                set_pixel_safe(img, x, y, c)

    # 腹部尖端
    for i in range(4):
        x = abdomen_cx + abd_rx + i
        y_off = max(0, 2 - i)
        for dy in range(-y_off, y_off + 1):
            c = blend(body_dark, body_main, 0.5)
            set_pixel_safe(img, x, abdomen_cy + dy, c)

    # --- 身体轮廓描边 ---
    outline_color = (70, 50, 10, 255)
    # 头部轮廓
    for angle in range(360):
        rad = math.radians(angle)
        x = int(head_cx + (head_r + 0.5) * math.cos(rad))
        y = int(head_cy + (head_r + 0.5) * math.sin(rad))
        if 0 <= x < SIZE and 0 <= y < SIZE:
            px = img.getpixel((x, y))
            if px[3] < 50:
                set_pixel_safe(img, x, y, outline_color)

    # --- 腿部 ---
    # 前腿
    draw_line(img, 24, 40, 20, 48, leg_color)
    draw_line(img, 20, 48, 18, 50, leg_color)
    # 中腿
    draw_line(img, 32, 41, 30, 49, leg_color)
    draw_line(img, 30, 49, 28, 51, leg_color)
    # 后腿
    draw_line(img, 40, 41, 39, 48, leg_color)
    draw_line(img, 39, 48, 37, 50, leg_color)

    # --- 时间符文光点 (六转仙蛊的神秘感) ---
    rune_positions = [
        (12, 18), (50, 15), (8, 38), (54, 42),
        (15, 48), (48, 50), (28, 12), (38, 10),
        (6, 28), (56, 28), (20, 54), (44, 54),
    ]
    for rx, ry in rune_positions:
        d_to_body = dist(rx, ry, cx_body, cy_body)
        if d_to_body > 12:
            brightness = random.randint(120, 220)
            a = random.randint(100, 200)
            set_pixel_safe(img, rx, ry, (255, brightness, 50, a))
            # 小十字形状的光点
            for dd in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
                alpha_composite_pixel(img, rx + dd[0], ry + dd[1], (255, brightness, 50, a // 3))

    # --- 头部触角 ---
    for i in range(8):
        x = 18 - i
        y = 28 - i
        c = blend(body_dark, body_main, i / 8.0)
        set_pixel_safe(img, x, y, c)
        if i > 4:
            set_pixel_safe(img, x - 1, y, (255, 200, 50, 150))

    # 第二根触角
    for i in range(6):
        x = 20 - i
        y = 27 - i * 0.5
        c = blend(body_dark, body_main, i / 6.0)
        set_pixel_safe(img, x, int(y), c)

    # --- 口器 (吻) ---
    for i in range(5):
        set_pixel_safe(img, 16 + i, 36, body_dark)
    set_pixel_safe(img, 15, 36, (80, 60, 10, 255))

    img.save(OUTPUT_DIR + "spring_autumn_cicada.png")
    print("spring_autumn_cicada.png generated")


# ============================================================
# 2. 肉骨蛊 (Flesh Bone Gu) - 一转蛊虫
# ============================================================
def generate_flesh_bone_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))

    # 颜色定义
    flesh_dark = (100, 0, 0, 255)        # 最深红
    flesh_main = (139, 0, 0, 255)        # 深红主色
    flesh_mid = (178, 34, 34, 255)       # 中红
    flesh_light = (200, 50, 50, 255)     # 亮红
    flesh_highlight = (220, 80, 80, 255) # 高光
    bone_white = (255, 228, 225, 255)    # 骨白色
    bone_cream = (240, 210, 200, 255)    # 骨奶色
    bone_line = (220, 190, 180, 200)     # 骨纹线
    outline_color = (60, 0, 0, 255)      # 轮廓
    horn_color = (230, 200, 190, 255)    # 角

    # 蠕虫蜷曲路径 - 用一系列控制点定义
    # 蜷曲的S形/C形身体 (在中心30x30区域)
    cx, cy = 32, 32

    # 身体段落: (x, y, radius) - 蜷曲的蠕虫
    segments = []
    for t in range(60):
        angle = t * 0.12 + 0.5  # 弧度，形成蜷曲
        radius_path = 8 + t * 0.08  # 螺旋半径渐增
        sx = cx + math.cos(angle) * radius_path * 0.8
        sy = cy + math.sin(angle) * radius_path * 0.6
        # 身体粗细: 中间粗两端细
        body_r = 3.5 + 2.0 * math.sin(t / 60.0 * math.pi) - t * 0.01
        body_r = max(1.5, body_r)
        segments.append((sx, sy, body_r))

    # 绘制身体
    for idx, (sx, sy, sr) in enumerate(segments):
        t_along = idx / len(segments)
        for y in range(max(0, int(sy - sr - 2)), min(SIZE, int(sy + sr + 2))):
            for x in range(max(0, int(sx - sr - 2)), min(SIZE, int(sx + sr + 2))):
                d = dist(x, y, sx, sy)
                if d <= sr:
                    # 基础颜色 - 根据深度渐变
                    dt = d / sr
                    c = blend(flesh_light, flesh_dark, dt * 0.7)

                    # 上方高光
                    if y < sy - sr * 0.3:
                        c = blend(c, flesh_highlight, 0.3 * (1 - dt))

                    # 骨骼纹理 - 每隔几个段出现白色骨纹
                    bone_pattern = idx % 8
                    if bone_pattern < 2:
                        # 骨纹区域
                        bone_intensity = 0.3 * (1 - abs(bone_pattern - 1) / 1.5)
                        c = blend(c, bone_white, bone_intensity * (1 - dt * 0.5))

                    # 横向骨骼条纹
                    stripe = (idx + int(d * 2)) % 5
                    if stripe == 0 and dt < 0.7:
                        c = blend(c, bone_cream, 0.15)

                    set_pixel_safe(img, x, y, c)

    # 身体轮廓
    for idx, (sx, sy, sr) in enumerate(segments):
        for angle_deg in range(0, 360, 5):
            rad = math.radians(angle_deg)
            ox = int(sx + (sr + 0.8) * math.cos(rad))
            oy = int(sy + (sr + 0.8) * math.sin(rad))
            if 0 <= ox < SIZE and 0 <= oy < SIZE:
                px = img.getpixel((ox, oy))
                if px[3] < 100:
                    # 检查是否靠近身体
                    near_body = False
                    for sx2, sy2, sr2 in segments:
                        if dist(ox, oy, sx2, sy2) <= sr2 + 1.5:
                            near_body = True
                            break
                    if near_body:
                        set_pixel_safe(img, ox, oy, outline_color)

    # 头部 (第一个段的位置)
    hx, hy = segments[0][0], segments[0][1]

    # 头部更大更圆
    head_r = 5
    for y in range(max(0, int(hy - head_r)), min(SIZE, int(hy + head_r + 1))):
        for x in range(max(0, int(hx - head_r)), min(SIZE, int(hx + head_r + 1))):
            d = dist(x, y, hx, hy)
            if d <= head_r:
                dt = d / head_r
                c = blend(flesh_mid, flesh_dark, dt * 0.5)
                if y < hy - 1:
                    c = blend(c, flesh_highlight, 0.2)
                set_pixel_safe(img, x, y, c)

    # 头部轮廓
    for angle_deg in range(360):
        rad = math.radians(angle_deg)
        ox = int(hx + (head_r + 0.5) * math.cos(rad))
        oy = int(hy + (head_r + 0.5) * math.sin(rad))
        set_pixel_safe(img, ox, oy, outline_color)

    # 小角状突起
    for i in range(4):
        set_pixel_safe(img, int(hx - 2), int(hy - head_r - i), horn_color)
        set_pixel_safe(img, int(hx - 1), int(hy - head_r - i), blend(horn_color, flesh_mid, 0.3))
    for i in range(3):
        set_pixel_safe(img, int(hx + 2), int(hy - head_r - i), horn_color)
        set_pixel_safe(img, int(hx + 3), int(hy - head_r - i), blend(horn_color, flesh_mid, 0.5))

    # 嘴部 - 小口
    set_pixel_safe(img, int(hx - 3), int(hy + 1), (80, 0, 0, 255))
    set_pixel_safe(img, int(hx - 4), int(hy + 1), (80, 0, 0, 255))
    set_pixel_safe(img, int(hx - 4), int(hy), (80, 0, 0, 255))

    # 眼睛 - 小而邪恶
    eye_pos = [(int(hx - 2), int(hy - 2)), (int(hx + 1), int(hy - 2))]
    for ex, ey in eye_pos:
        set_pixel_safe(img, ex, ey, (20, 0, 0, 255))
        set_pixel_safe(img, ex + 1, ey, (20, 0, 0, 255))
        # 微弱红色反光
        set_pixel_safe(img, ex, ey - 1, (255, 100, 100, 120))

    # 骨骼突起点缀 - 沿身体随机分布
    for idx in range(5, len(segments), 10):
        sx, sy, sr = segments[idx]
        # 小骨刺
        angle = random.uniform(0, 2 * math.pi)
        for i in range(3):
            bx = int(sx + (sr + i) * math.cos(angle))
            by = int(sy + (sr + i) * math.sin(angle))
            a = 255 - i * 40
            set_pixel_safe(img, bx, by, (bone_white[0], bone_white[1], bone_white[2], a))

    img.save(OUTPUT_DIR + "flesh_bone_gu.png")
    print("flesh_bone_gu.png generated")


# ============================================================
# 3. 移形蛊 (Displacement Gu) - 二转蛊虫
# ============================================================
def generate_displacement_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))

    # 颜色定义
    shell_dark = (45, 0, 80, 255)         # 最深紫
    shell_main = (75, 0, 130, 255)        # 深紫主色
    shell_mid = (72, 61, 139, 255)        # 中紫蓝
    shell_light = (106, 90, 205, 255)     # 亮紫蓝
    shell_highlight = (140, 120, 230, 255) # 高光
    crack_color = (218, 112, 214, 255)    # 空间裂纹亮紫
    crack_glow = (218, 112, 214, 100)     # 裂纹辉光
    ender_particle = (123, 104, 238, 150) # 末影粒子
    outline_color = (25, 0, 50, 255)      # 轮廓
    eye_color = (180, 100, 255, 255)      # 眼睛
    leg_color = (50, 20, 80, 255)         # 腿
    distort_glow = (150, 120, 255, 80)    # 扭曲光点

    cx, cy = 32, 34

    # --- 空间扭曲光点背景 ---
    warp_points = [(12, 14), (52, 12), (8, 50), (55, 48), (20, 8),
                   (44, 8), (10, 30), (54, 32), (25, 56), (40, 56),
                   (6, 20), (58, 20), (15, 44), (50, 44)]
    for wx, wy in warp_points:
        d = dist(wx, wy, cx, cy)
        if d > 15:
            a = random.randint(40, 100)
            r = random.randint(100, 180)
            g = random.randint(80, 140)
            b = random.randint(200, 255)
            set_pixel_safe(img, wx, wy, (r, g, b, a))
            # 扩散
            for dd in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
                alpha_composite_pixel(img, wx + dd[0], wy + dd[1], (r, g, b, a // 3))

    # --- 甲虫身体 ---
    # 甲壳(鞘翅) - 主体
    shell_cx, shell_cy = cx, cy + 2
    shell_rx, shell_ry = 13, 11

    for y in range(max(0, shell_cy - shell_ry - 1), min(SIZE, shell_cy + shell_ry + 1)):
        for x in range(max(0, shell_cx - shell_rx - 1), min(SIZE, shell_cx + shell_rx + 1)):
            dx = (x - shell_cx) / shell_rx
            dy = (y - shell_cy) / shell_ry
            dd = dx * dx + dy * dy
            if dd <= 1.0:
                d = math.sqrt(dd)
                # 基础渐变
                c = blend(shell_light, shell_dark, d * 0.6)

                # 上方高光
                if y < shell_cy - shell_ry * 0.3:
                    c = blend(c, shell_highlight, 0.3 * (1 - d))

                # 鞘翅中线
                if abs(x - shell_cx) <= 1 and y > shell_cy - shell_ry * 0.5:
                    c = blend(c, shell_dark, 0.5)

                # 甲壳光泽纹理
                sheen = math.sin(x * 0.3 + y * 0.2) * 0.15
                if sheen > 0:
                    c = blend(c, shell_highlight, sheen)

                set_pixel_safe(img, x, y, c)

    # --- 空间裂纹纹理 (在甲壳上) ---
    # 裂纹路径
    cracks = [
        # (起点x, 起点y, 方向角度, 长度)
        (25, 30, 45, 12),
        (38, 32, 135, 10),
        (28, 40, -30, 9),
        (36, 38, 160, 8),
        (32, 28, 90, 7),
    ]
    for start_x, start_y, angle_deg, length in cracks:
        rad = math.radians(angle_deg)
        for i in range(length):
            px = int(start_x + i * math.cos(rad) + random.uniform(-0.5, 0.5))
            py = int(start_y + i * math.sin(rad) + random.uniform(-0.5, 0.5))
            # 只在甲壳范围内画
            ddx = (px - shell_cx) / shell_rx
            ddy = (py - shell_cy) / shell_ry
            if ddx * ddx + ddy * ddy < 0.9:
                set_pixel_safe(img, px, py, crack_color)
                # 裂纹辉光
                for dd in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
                    alpha_composite_pixel(img, px + dd[0], py + dd[1], crack_glow)
                # 分叉
                if i > 3 and random.random() < 0.3:
                    fork_rad = rad + random.choice([-0.5, 0.5])
                    for j in range(3):
                        fx = int(px + j * math.cos(fork_rad))
                        fy = int(py + j * math.sin(fork_rad))
                        alpha_composite_pixel(img, fx, fy, (218, 112, 214, 180))

    # 甲壳轮廓
    for angle_deg in range(360):
        rad = math.radians(angle_deg)
        ox = int(shell_cx + (shell_rx + 0.8) * math.cos(rad))
        oy = int(shell_cy + (shell_ry + 0.8) * math.sin(rad))
        set_pixel_safe(img, ox, oy, outline_color)

    # --- 头部 ---
    head_cx, head_cy = cx, cy - 10
    head_rx, head_ry = 8, 6

    for y in range(max(0, head_cy - head_ry - 1), min(SIZE, head_cy + head_ry + 1)):
        for x in range(max(0, head_cx - head_rx - 1), min(SIZE, head_cx + head_rx + 1)):
            dx_h = (x - head_cx) / head_rx
            dy_h = (y - head_cy) / head_ry
            if dx_h * dx_h + dy_h * dy_h <= 1.0:
                d = dist(x, y, head_cx, head_cy) / max(head_rx, head_ry)
                c = blend(shell_mid, shell_dark, d * 0.5)
                if y < head_cy:
                    c = blend(c, shell_light, 0.2)
                set_pixel_safe(img, x, y, c)

    # 头部轮廓
    for angle_deg in range(360):
        rad = math.radians(angle_deg)
        ox = int(head_cx + (head_rx + 0.5) * math.cos(rad))
        oy = int(head_cy + (head_ry + 0.5) * math.sin(rad))
        set_pixel_safe(img, ox, oy, outline_color)

    # 眼睛
    for ex_off in [-3, 3]:
        ex = int(head_cx + ex_off)
        ey = int(head_cy - 1)
        set_pixel_safe(img, ex, ey, eye_color)
        set_pixel_safe(img, ex + 1, ey, eye_color)
        set_pixel_safe(img, ex, ey + 1, (150, 80, 220, 255))
        set_pixel_safe(img, ex + 1, ey + 1, (150, 80, 220, 255))
        # 眼睛高光
        set_pixel_safe(img, ex, ey - 1, (220, 180, 255, 180))

    # 触角
    for i in range(6):
        set_pixel_safe(img, int(head_cx - 5 - i), int(head_cy - 4 - i), (80, 40, 130, 255))
        set_pixel_safe(img, int(head_cx + 5 + i), int(head_cy - 4 - i), (80, 40, 130, 255))
    # 触角尖端光点
    alpha_composite_pixel(img, int(head_cx - 11), int(head_cy - 10), ender_particle)
    alpha_composite_pixel(img, int(head_cx + 11), int(head_cy - 10), ender_particle)

    # 下颚
    for dx in range(-2, 3):
        set_pixel_safe(img, head_cx + dx, head_cy + head_ry, (40, 15, 60, 255))

    # --- 腿部 ---
    # 左侧3条腿
    leg_starts_l = [(22, 30), (20, 36), (22, 42)]
    leg_ends_l = [(14, 38), (12, 44), (14, 50)]
    for (lx1, ly1), (lx2, ly2) in zip(leg_starts_l, leg_ends_l):
        draw_line(img, lx1, ly1, lx2, ly2, leg_color)
        draw_line(img, lx2, ly2, lx2 - 2, ly2 + 2, leg_color)

    # 右侧3条腿
    leg_starts_r = [(42, 30), (44, 36), (42, 42)]
    leg_ends_r = [(50, 38), (52, 44), (50, 50)]
    for (lx1, ly1), (lx2, ly2) in zip(leg_starts_r, leg_ends_r):
        draw_line(img, lx1, ly1, lx2, ly2, leg_color)
        draw_line(img, lx2, ly2, lx2 + 2, ly2 + 2, leg_color)

    # --- 末影粒子效果 ---
    particles = [
        (10, 18), (52, 16), (7, 42), (56, 40),
        (16, 10), (48, 10), (8, 52), (56, 52),
        (14, 28), (50, 28), (18, 50), (46, 50),
    ]
    for px_pos, py_pos in particles:
        d = dist(px_pos, py_pos, cx, cy)
        if d > 14:
            size = random.randint(1, 2)
            a = random.randint(80, 180)
            for dy in range(size):
                for dx_p in range(size):
                    alpha_composite_pixel(img, px_pos + dx_p, py_pos + dy,
                                          (123, 104, 238, a))
            # 拖尾效果 - 向外的短线
            trail_angle = math.atan2(py_pos - cy, px_pos - cx)
            for i in range(1, 3):
                tx = int(px_pos + i * math.cos(trail_angle))
                ty = int(py_pos + i * math.sin(trail_angle))
                alpha_composite_pixel(img, tx, ty, (123, 104, 238, a // (i + 1)))

    # --- 甲壳上的空间扭曲光泽 ---
    for y in range(max(0, shell_cy - shell_ry), min(SIZE, shell_cy + shell_ry)):
        for x in range(max(0, shell_cx - shell_rx), min(SIZE, shell_cx + shell_rx)):
            dx = (x - shell_cx) / shell_rx
            dy = (y - shell_cy) / shell_ry
            if dx * dx + dy * dy < 0.85:
                wave = math.sin(x * 0.4 + y * 0.3 + 1.5) * math.cos(x * 0.2 - y * 0.4)
                if wave > 0.6:
                    alpha_composite_pixel(img, x, y, (180, 150, 255, int(wave * 40)))

    img.save(OUTPUT_DIR + "displacement_gu.png")
    print("displacement_gu.png generated")


# ============================================================
# 主程序
# ============================================================
if __name__ == "__main__":
    print("Generating Batch 8 Gu textures...")
    generate_spring_autumn_cicada()
    generate_flesh_bone_gu()
    generate_displacement_gu()
    print("All Batch 8 textures generated!")
