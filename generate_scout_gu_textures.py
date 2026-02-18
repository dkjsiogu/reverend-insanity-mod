#!/usr/bin/env python3
"""
蛊真人mod - 侦察/感知系蛊虫 + 赌石贴图生成
蛇信蛊、地听肉耳草、顺风耳蛊、隐鳞蛊、真视蛊、电眼蛊、赌石(低/中/高)
"""

from PIL import Image, ImageDraw
import os
import math
import random

OUTPUT_DIR = "src/main/resources/assets/reverend_insanity/textures/item"

# 固定种子保证可复现
random.seed(42)


def ensure_dir():
    os.makedirs(OUTPUT_DIR, exist_ok=True)


def hex_to_rgba(hex_color, alpha=255):
    hex_color = hex_color.lstrip('#')
    r = int(hex_color[0:2], 16)
    g = int(hex_color[2:4], 16)
    b = int(hex_color[4:6], 16)
    return (r, g, b, alpha)


def blend_color(c1, c2, t):
    return tuple(int(c1[i] + (c2[i] - c1[i]) * t) for i in range(len(c1)))


def fill_pixel_block(img, px, py, size, color):
    for dx in range(size):
        for dy in range(size):
            x, y = px * size + dx, py * size + dy
            if 0 <= x < img.width and 0 <= y < img.height:
                img.putpixel((x, y), color)


def draw_circle_filled(img, cx, cy, radius, color, pixel_size=1):
    for y in range(img.height):
        for x in range(img.width):
            dist = math.sqrt((x - cx) ** 2 + (y - cy) ** 2)
            if dist <= radius:
                img.putpixel((x, y), color)


def draw_ellipse_aa(img, cx, cy, rx, ry, color):
    for y in range(max(0, int(cy - ry - 2)), min(img.height, int(cy + ry + 2))):
        for x in range(max(0, int(cx - rx - 2)), min(img.width, int(cx + rx + 2))):
            val = ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2
            if val <= 1.0:
                img.putpixel((x, y), color)


def add_jade_sheen(img, cx, cy, radius, intensity=40):
    for y in range(img.height):
        for x in range(img.width):
            px = img.getpixel((x, y))
            if px[3] > 0:
                dist = math.sqrt((x - cx) ** 2 + (y - cy) ** 2)
                if dist < radius * 0.6:
                    highlight = int(intensity * (1 - dist / (radius * 0.6)))
                    new_c = (
                        min(255, px[0] + highlight),
                        min(255, px[1] + highlight),
                        min(255, px[2] + highlight),
                        px[3]
                    )
                    img.putpixel((x, y), new_c)


# ============================================================
# 1. 蛇信蛊 (snake_tongue_gu.png) - 64x64
# ============================================================
def generate_snake_tongue_gu():
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    body_green = hex_to_rgba('#2D8B3A')
    dark_green = hex_to_rgba('#1A5C26')
    belly_green = hex_to_rgba('#4CAF50')
    eye_gold = hex_to_rgba('#FFD700')
    pupil_dark = hex_to_rgba('#2A2A00')
    tongue_red = hex_to_rgba('#FF2222')
    tongue_dark = hex_to_rgba('#CC1111')
    highlight = hex_to_rgba('#5FBF6A', 180)
    shadow = hex_to_rgba('#0F3A14')

    # 蛇身蜷缩成圆形 - 用粗弧线模拟
    # 外圈蛇身 (顺时针蜷缩)
    cx, cy = 32, 34
    for angle_deg in range(0, 330, 1):
        angle = math.radians(angle_deg)
        # 螺旋半径从大到小
        r = 20 - angle_deg / 330 * 8
        x = cx + r * math.cos(angle)
        y = cy + r * math.sin(angle)
        # 蛇身宽度
        width = 5.5 - angle_deg / 330 * 1.5
        for dy in range(-int(width), int(width) + 1):
            for dx in range(-int(width), int(width) + 1):
                dist = math.sqrt(dx * dx + dy * dy)
                if dist <= width:
                    px_x = int(x + dx)
                    px_y = int(y + dy)
                    if 0 <= px_x < 64 and 0 <= px_y < 64:
                        # 鳞片花纹 - 用角度和距离中心来判断
                        if (angle_deg + int(dist * 3)) % 12 < 6:
                            c = body_green
                        else:
                            c = dark_green
                        # 肚皮（内侧较浅）
                        inner_dist = math.sqrt((px_x - cx) ** 2 + (px_y - cy) ** 2)
                        if inner_dist < r - 2:
                            c = belly_green
                        img.putpixel((px_x, px_y), c)

    # 蛇头 (在蜷缩的起点位置，右侧偏上)
    head_cx, head_cy = 50, 26
    # 三角形蛇头
    for y in range(18, 35):
        for x in range(40, 60):
            dx = x - head_cx
            dy = y - head_cy
            # 椭圆形头部
            if (dx / 9) ** 2 + (dy / 7) ** 2 <= 1:
                # 头部颜色
                if (x + y) % 8 < 4:
                    img.putpixel((x, y), body_green)
                else:
                    img.putpixel((x, y), dark_green)

    # 头部上方高光
    for y in range(20, 26):
        for x in range(44, 54):
            dx = x - 49
            dy = y - 23
            if dx * dx + dy * dy <= 16:
                px = img.getpixel((x, y))
                if px[3] > 0:
                    img.putpixel((x, y), highlight)

    # 蛇眼 (金黄竖瞳)
    eye_x, eye_y = 52, 24
    # 眼白
    draw_ellipse_aa(img, eye_x, eye_y, 3, 2.5, hex_to_rgba('#DDDDAA'))
    # 金色虹膜
    draw_ellipse_aa(img, eye_x, eye_y, 2.5, 2, eye_gold)
    # 竖瞳
    for dy in range(-2, 3):
        if 0 <= eye_y + dy < 64:
            img.putpixel((eye_x, eye_y + dy), pupil_dark)

    # 蛇信 (分叉红舌)
    tongue_start_x = 56
    tongue_start_y = 27
    # 舌根
    for i in range(6):
        x = tongue_start_x + i
        if x < 64:
            img.putpixel((x, tongue_start_y), tongue_red)
            img.putpixel((x, tongue_start_y - 1), tongue_dark)
    # 上叉
    for i in range(4):
        x = tongue_start_x + 5 + i
        y = tongue_start_y - 1 - i
        if 0 <= x < 64 and 0 <= y < 64:
            img.putpixel((x, y), tongue_red)
    # 下叉
    for i in range(4):
        x = tongue_start_x + 5 + i
        y = tongue_start_y + 1 + i
        if 0 <= x < 64 and 0 <= y < 64:
            img.putpixel((x, y), tongue_red)

    # 蛇尾 (蜷缩中心，逐渐变细)
    tail_cx, tail_cy = 26, 28
    for angle_deg in range(0, 180, 2):
        angle = math.radians(angle_deg)
        r = 5 - angle_deg / 180 * 4
        x = int(tail_cx + r * math.cos(angle))
        y = int(tail_cy + r * math.sin(angle))
        w = max(1, 3 - angle_deg // 60)
        for dx in range(-w, w + 1):
            for dy in range(-w, w + 1):
                if dx * dx + dy * dy <= w * w:
                    px_x = x + dx
                    px_y = y + dy
                    if 0 <= px_x < 64 and 0 <= px_y < 64:
                        img.putpixel((px_x, px_y), dark_green)

    # 玉石质感光泽
    add_jade_sheen(img, 35, 32, 22, 30)

    # 微光粒子
    for _ in range(8):
        px = random.randint(10, 54)
        py = random.randint(10, 54)
        if img.getpixel((px, py))[3] > 0:
            img.putpixel((px, py), hex_to_rgba('#AAFFAA', 200))

    img.save(os.path.join(OUTPUT_DIR, "snake_tongue_gu.png"))
    print("  [OK] snake_tongue_gu.png")


# ============================================================
# 2. 地听肉耳草 (earth_listener_gu.png) - 64x64
# ============================================================
def generate_earth_listener_gu():
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    flesh_pink = hex_to_rgba('#E8A0A0')
    deep_pink = hex_to_rgba('#CC6666')
    inner_pink = hex_to_rgba('#FFCCCC')
    root_brown = hex_to_rgba('#886644')
    root_dark = hex_to_rgba('#664422')
    vein_color = hex_to_rgba('#D08888')
    highlight = hex_to_rgba('#FFDDDD')
    glow = hex_to_rgba('#FFE0E0', 160)

    cx, cy = 32, 28

    # 外耳轮廓 (耳朵形状 - 略偏左的椭圆+弯曲)
    for y in range(6, 52):
        for x in range(10, 54):
            # 外耳轮廓用参数方程
            dx = x - cx
            dy = y - cy
            # 耳朵形: 上窄下宽的椭圆
            ear_rx = 16 + (y - 6) * 0.15  # 越往下越宽
            ear_ry = 22
            if (dx / ear_rx) ** 2 + (dy / ear_ry) ** 2 <= 1:
                # 内外区分
                inner_rx = ear_rx * 0.65
                inner_ry = ear_ry * 0.6
                inner_cx = cx + 2
                inner_cy = cy + 2
                inner_val = ((x - inner_cx) / inner_rx) ** 2 + ((y - inner_cy) / inner_ry) ** 2

                if inner_val <= 1.0:
                    # 内耳 - 深粉色
                    depth = 1 - inner_val
                    if depth > 0.5:
                        c = inner_pink  # 最内部微光
                    else:
                        c = deep_pink
                    # 耳道暗处
                    ear_canal_dist = math.sqrt((x - (cx + 3)) ** 2 + ((y - (cy + 4)) * 1.3) ** 2)
                    if ear_canal_dist < 6:
                        c = hex_to_rgba('#AA4444')
                else:
                    # 外耳肉
                    c = flesh_pink
                    # 植物纹理 - 叶脉状
                    vein_check = math.sin(y * 0.5) * 3 + math.cos(x * 0.3) * 2
                    if abs(vein_check) < 0.8:
                        c = vein_color

                img.putpixel((x, y), c)

    # 耳垂 (底部圆润突出)
    for y in range(46, 56):
        for x in range(24, 42):
            dx = x - 33
            dy = y - 48
            if (dx / 8) ** 2 + (dy / 6) ** 2 <= 1:
                c = flesh_pink
                # 肉质感
                if (x + y) % 5 == 0:
                    c = deep_pink
                img.putpixel((x, y), c)

    # 耳朵边缘加深 (肉耳草的卷边)
    for y in range(6, 52):
        for x in range(10, 54):
            px = img.getpixel((x, y))
            if px[3] > 0:
                # 检查是否边缘
                edge = False
                for ddx, ddy in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
                    nx, ny = x + ddx, y + ddy
                    if 0 <= nx < 64 and 0 <= ny < 64:
                        if img.getpixel((nx, ny))[3] == 0:
                            edge = True
                            break
                    else:
                        edge = True
                        break
                if edge:
                    img.putpixel((x, y), hex_to_rgba('#BB7777'))

    # 底部根须
    root_starts = [(28, 54), (32, 55), (36, 54), (25, 52), (39, 52)]
    for rx, ry in root_starts:
        length = random.randint(4, 8)
        cur_x, cur_y = rx, ry
        for i in range(length):
            c = root_brown if i % 2 == 0 else root_dark
            if 0 <= cur_x < 64 and 0 <= cur_y < 64:
                img.putpixel((cur_x, cur_y), c)
                if cur_x + 1 < 64:
                    img.putpixel((cur_x + 1, cur_y), c)
            cur_x += random.choice([-1, 0, 1])
            cur_y += 1

    # 内部微光 (活跃感)
    for _ in range(15):
        gx = random.randint(22, 42)
        gy = random.randint(18, 40)
        px = img.getpixel((gx, gy))
        if px[3] > 0 and px == deep_pink:
            img.putpixel((gx, gy), glow)

    # 玉石质感
    add_jade_sheen(img, cx, cy, 20, 25)

    img.save(os.path.join(OUTPUT_DIR, "earth_listener_gu.png"))
    print("  [OK] earth_listener_gu.png")


# ============================================================
# 3. 顺风耳蛊 (keen_ear_gu.png) - 64x64
# ============================================================
def generate_keen_ear_gu():
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    dark_brown = hex_to_rgba('#5C3A1A')
    light_brown = hex_to_rgba('#C8A070')
    mid_brown = hex_to_rgba('#8B6840')
    wave_blue = hex_to_rgba('#88CCFF', 180)
    wave_blue2 = hex_to_rgba('#AADDFF', 140)
    body_color = hex_to_rgba('#4A2A10')
    eye_color = hex_to_rgba('#FFCC00')

    cx, cy = 32, 36

    # 小虫身体 (中央下方)
    for y in range(32, 46):
        for x in range(26, 38):
            dx = x - 32
            dy = y - 39
            if (dx / 5.5) ** 2 + (dy / 6.5) ** 2 <= 1:
                if (x + y) % 4 < 2:
                    img.putpixel((x, y), body_color)
                else:
                    img.putpixel((x, y), dark_brown)

    # 虫子小眼睛
    img.putpixel((30, 34), eye_color)
    img.putpixel((34, 34), eye_color)
    img.putpixel((30, 35), hex_to_rgba('#AA8800'))
    img.putpixel((34, 35), hex_to_rgba('#AA8800'))

    # 左耳 (大型蝙蝠耳)
    for y in range(4, 38):
        for x in range(4, 30):
            # 耳朵形状: 三角形+弧线顶
            # 底边在(12,36)到(28,34), 顶点在(16,6)
            # 用三角+椭圆混合
            top_x, top_y = 16, 6
            bl_x, bl_y = 10, 36
            br_x, br_y = 28, 34

            # 三角形内部检测 (重心坐标)
            def in_triangle(px, py, ax, ay, bx, by, ccx, ccy):
                d = (by - ccy) * (ax - ccx) + (ccx - bx) * (ay - ccy)
                if abs(d) < 0.001:
                    return False
                a_w = ((by - ccy) * (px - ccx) + (ccx - bx) * (py - ccy)) / d
                b_w = ((ccy - ay) * (px - ccx) + (ax - ccx) * (py - ccy)) / d
                c_w = 1 - a_w - b_w
                return a_w >= -0.05 and b_w >= -0.05 and c_w >= -0.05

            if in_triangle(x, y, top_x, top_y, bl_x, bl_y, br_x, br_y):
                # 外耳深棕 vs 内耳浅棕
                # 内耳偏右偏下
                inner_check = ((x - 20) / 8) ** 2 + ((y - 22) / 12) ** 2
                if inner_check < 1:
                    c = light_brown
                else:
                    c = dark_brown
                # 耳膜纹路
                if abs(math.sin(y * 0.6 + x * 0.2) * 4) < 0.7:
                    c = mid_brown
                img.putpixel((x, y), c)

    # 右耳 (镜像)
    for y in range(4, 38):
        for x in range(34, 60):
            top_x, top_y = 48, 6
            bl_x, bl_y = 36, 34
            br_x, br_y = 54, 36

            def in_triangle2(px, py, ax, ay, bx, by, ccx, ccy):
                d = (by - ccy) * (ax - ccx) + (ccx - bx) * (ay - ccy)
                if abs(d) < 0.001:
                    return False
                a_w = ((by - ccy) * (px - ccx) + (ccx - bx) * (py - ccy)) / d
                b_w = ((ccy - ay) * (px - ccx) + (ax - ccx) * (py - ccy)) / d
                c_w = 1 - a_w - b_w
                return a_w >= -0.05 and b_w >= -0.05 and c_w >= -0.05

            if in_triangle2(x, y, top_x, top_y, bl_x, bl_y, br_x, br_y):
                inner_check = ((x - 44) / 8) ** 2 + ((y - 22) / 12) ** 2
                if inner_check < 1:
                    c = light_brown
                else:
                    c = dark_brown
                if abs(math.sin(y * 0.6 + (64 - x) * 0.2) * 4) < 0.7:
                    c = mid_brown
                img.putpixel((x, y), c)

    # 音波弧线 (从耳朵向外散发)
    for ring in range(3):
        r = 28 + ring * 5
        alpha = 180 - ring * 50
        wave_c = (0x88, 0xCC, 0xFF, max(60, alpha))
        for angle_deg in range(200, 340):
            angle = math.radians(angle_deg)
            x = int(cx + r * math.cos(angle))
            y = int(cy - 10 + r * math.sin(angle))
            if 0 <= x < 64 and 0 <= y < 64:
                existing = img.getpixel((x, y))
                if existing[3] == 0:
                    img.putpixel((x, y), wave_c)

    # 小音波 (耳朵之间)
    for ring in range(2):
        for angle_deg in range(-20, 20):
            angle = math.radians(angle_deg - 90)
            r = 8 + ring * 4
            x = int(cx + r * math.cos(angle))
            y = int(30 + r * math.sin(angle))
            if 0 <= x < 64 and 0 <= y < 64:
                existing = img.getpixel((x, y))
                if existing[3] == 0:
                    img.putpixel((x, y), wave_blue2)

    # 玉石光泽
    add_jade_sheen(img, 32, 38, 15, 20)

    # 触角/小须
    for i in range(4):
        sx = 29 + i * 2
        for j in range(3):
            if 0 <= 45 + j < 64:
                img.putpixel((sx, 45 + j), mid_brown)

    img.save(os.path.join(OUTPUT_DIR, "keen_ear_gu.png"))
    print("  [OK] keen_ear_gu.png")


# ============================================================
# 4. 隐鳞蛊 (hidden_scale_gu.png) - 64x64
# ============================================================
def generate_hidden_scale_gu():
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    scale_base = hex_to_rgba('#C0D0E0', 180)
    scale_light = hex_to_rgba('#D8E4F0', 160)
    scale_edge = hex_to_rgba('#8899AA', 200)
    iridescent1 = hex_to_rgba('#AADDFF', 140)
    iridescent2 = hex_to_rgba('#DDAAFF', 140)
    iridescent3 = hex_to_rgba('#AAFFDD', 140)
    eye_color = hex_to_rgba('#334455', 220)
    checker_a = hex_to_rgba('#B8C8D8', 120)
    checker_b = hex_to_rgba('#C8D8E8', 80)

    cx, cy = 32, 32

    # 鳞片形状 (菱形/盾形)
    for y in range(8, 56):
        for x in range(12, 52):
            dx = abs(x - cx)
            dy = abs(y - cy)
            # 鳞片形: 上尖下圆的盾形
            top_dist = dx + max(0, (cy - y)) * 0.6  # 上方更尖
            bot_dist = (dx / 18) ** 2 + (max(0, y - cy) / 22) ** 2  # 下方圆润

            in_scale = False
            if y <= cy:
                in_scale = dx + (cy - y) * 0.8 < 20
            else:
                in_scale = (dx / 19) ** 2 + ((y - cy) / 23) ** 2 < 1

            if in_scale:
                # 棋盘格透明模式
                if (x // 4 + y // 4) % 2 == 0:
                    base = checker_a
                else:
                    base = checker_b

                # 鳞片纹理 (同心弧线)
                ring_val = math.sqrt(dx * dx + dy * dy)
                if int(ring_val) % 6 < 1:
                    base = scale_edge

                # 虹彩色 (根据角度变化)
                angle = math.atan2(y - cy, x - cx)
                hue_shift = (angle + math.pi) / (2 * math.pi)
                if hue_shift < 0.33:
                    irid = iridescent1
                elif hue_shift < 0.66:
                    irid = iridescent2
                else:
                    irid = iridescent3

                # 混合虹彩
                edge_factor = ring_val / 20
                if edge_factor > 0.7:
                    # 确保base和irid都是4元素再混合
                    base4 = base if len(base) == 4 else (*base, 160)
                    irid4 = irid if len(irid) == 4 else (*irid, 140)
                    blended = blend_color(base4, irid4, (edge_factor - 0.7) / 0.3)
                    base = (blended[0], blended[1], blended[2], min(200, blended[3]))

                if len(base) == 3:
                    base = (*base, 160)
                img.putpixel((x, y), tuple(int(v) for v in base))

    # 鳞片边缘描边 (虹彩色)
    for y in range(8, 56):
        for x in range(12, 52):
            px = img.getpixel((x, y))
            if px[3] > 0:
                is_edge = False
                for ddx, ddy in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
                    nx, ny = x + ddx, y + ddy
                    if 0 <= nx < 64 and 0 <= ny < 64:
                        if img.getpixel((nx, ny))[3] == 0:
                            is_edge = True
                            break
                    else:
                        is_edge = True
                        break
                if is_edge:
                    angle = math.atan2(y - cy, x - cx)
                    t = (math.sin(angle * 3) + 1) / 2
                    if t < 0.5:
                        edge_c = iridescent1
                    else:
                        edge_c = iridescent2
                    img.putpixel((x, y), edge_c)

    # 隐蔽的小眼睛
    for ey in range(-1, 2):
        for ex in range(-1, 2):
            if ex * ex + ey * ey <= 1:
                img.putpixel((28 + ex, 28 + ey), eye_color)

    # 中线纹路 (脊椎线)
    for y in range(12, 52):
        px = img.getpixel((32, y))
        if px[3] > 0:
            img.putpixel((32, y), scale_edge)

    # 微弱光点
    for _ in range(6):
        gx = random.randint(16, 48)
        gy = random.randint(12, 52)
        if img.getpixel((gx, gy))[3] > 0:
            img.putpixel((gx, gy), hex_to_rgba('#EEFFFF', 200))

    img.save(os.path.join(OUTPUT_DIR, "hidden_scale_gu.png"))
    print("  [OK] hidden_scale_gu.png")


# ============================================================
# 5. 真视蛊 (true_sight_gu.png) - 64x64
# ============================================================
def generate_true_sight_gu():
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    sclera_white = hex_to_rgba('#FFFFFF')
    sclera_shadow = hex_to_rgba('#E8E0D8')
    iris_gold = hex_to_rgba('#FFD700')
    iris_dark_gold = hex_to_rgba('#CC9900')
    iris_bright = hex_to_rgba('#FFE855')
    pupil_blue = hex_to_rgba('#1A1A66')
    pupil_deep = hex_to_rgba('#0A0A44')
    glow_gold = hex_to_rgba('#FFEEAA', 160)
    glow_outer = hex_to_rgba('#FFE88A', 100)
    ray_color = hex_to_rgba('#FFE066', 120)

    cx, cy = 32, 32

    # 光芒射线 (先画，在眼睛后面)
    for ray in range(12):
        angle = math.radians(ray * 30)
        for dist in range(22, 30):
            x = int(cx + dist * math.cos(angle))
            y = int(cy + dist * math.sin(angle))
            if 0 <= x < 64 and 0 <= y < 64:
                alpha = max(40, 120 - (dist - 22) * 12)
                img.putpixel((x, y), (0xFF, 0xE0, 0x66, alpha))
                # 粗射线
                for d in [-1, 1]:
                    nx = int(x + d * math.sin(angle))
                    ny = int(y - d * math.cos(angle))
                    if 0 <= nx < 64 and 0 <= ny < 64:
                        img.putpixel((nx, ny), (0xFF, 0xE8, 0x88, alpha // 2))

    # 外部光晕
    for y in range(4, 60):
        for x in range(4, 60):
            dist = math.sqrt((x - cx) ** 2 + (y - cy) ** 2)
            if 21 < dist < 26:
                alpha = int(100 * (1 - (dist - 21) / 5))
                existing = img.getpixel((x, y))
                if existing[3] < alpha:
                    img.putpixel((x, y), (0xFF, 0xEE, 0xAA, alpha))

    # 眼白 (巩膜) - 杏仁形/眼睛形
    for y in range(14, 50):
        for x in range(8, 56):
            dx = x - cx
            dy = y - cy
            # 眼睛形状: 横向椭圆 + 上下尖
            eye_shape = (dx / 22) ** 2 + (dy / 16) ** 2
            # 让上下更尖
            if abs(dy) > 8:
                squeeze = 1 + (abs(dy) - 8) * 0.04
                eye_shape = (dx / (22 / squeeze)) ** 2 + (dy / 16) ** 2

            if eye_shape <= 1:
                # 巩膜渐变 (边缘略暗)
                t = eye_shape
                if t > 0.7:
                    c = sclera_shadow
                else:
                    c = sclera_white
                img.putpixel((x, y), c)

    # 虹膜 (金色 + 辐射纹理)
    iris_r = 11
    for y in range(cy - iris_r - 1, cy + iris_r + 1):
        for x in range(cx - iris_r - 1, cx + iris_r + 1):
            dx = x - cx
            dy = y - cy
            dist = math.sqrt(dx * dx + dy * dy)
            if dist <= iris_r:
                # 辐射纹理
                angle = math.atan2(dy, dx)
                radial = math.sin(angle * 8) * 0.5 + 0.5
                t = dist / iris_r
                if radial > 0.6:
                    c = iris_bright
                elif t > 0.8:
                    c = iris_dark_gold
                else:
                    c = iris_gold
                # 外圈深色环
                if 0.85 < t <= 1.0:
                    c = hex_to_rgba('#AA7700')
                img.putpixel((x, y), c)

    # 瞳孔 (深蓝色十字形)
    pupil_r = 4
    for y in range(cy - pupil_r, cy + pupil_r + 1):
        for x in range(cx - pupil_r, cx + pupil_r + 1):
            dx = abs(x - cx)
            dy = abs(y - cy)
            # 十字形瞳孔
            if (dx <= 1 and dy <= pupil_r) or (dy <= 1 and dx <= pupil_r):
                # 中心更深
                dist = max(dx, dy)
                if dist <= 1:
                    img.putpixel((x, y), pupil_deep)
                else:
                    img.putpixel((x, y), pupil_blue)
            # 中心圆
            elif dx * dx + dy * dy <= 4:
                img.putpixel((x, y), pupil_deep)

    # 眼睛高光
    for dy in range(-2, 1):
        for dx in range(-2, 1):
            px_x = cx - 4 + dx
            px_y = cy - 5 + dy
            if 0 <= px_x < 64 and 0 <= px_y < 64:
                img.putpixel((px_x, px_y), hex_to_rgba('#FFFFFF', 240))

    # 小高光
    img.putpixel((cx + 3, cy - 3), hex_to_rgba('#FFFFFF', 200))
    img.putpixel((cx + 4, cy - 3), hex_to_rgba('#FFFFFF', 200))

    # 金色光芒粒子
    particle_positions = [
        (8, 8), (56, 8), (8, 56), (56, 56),
        (4, 32), (60, 32), (32, 4), (32, 60),
        (12, 14), (52, 14), (12, 50), (52, 50),
    ]
    for px, py in particle_positions:
        if 0 <= px < 64 and 0 <= py < 64:
            dist = math.sqrt((px - cx) ** 2 + (py - cy) ** 2)
            alpha = max(60, int(160 - dist * 3))
            img.putpixel((px, py), (0xFF, 0xEE, 0xAA, alpha))
            # 小十字
            for ddx, ddy in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
                nx, ny = px + ddx, py + ddy
                if 0 <= nx < 64 and 0 <= ny < 64:
                    img.putpixel((nx, ny), (0xFF, 0xEE, 0xAA, alpha // 2))

    img.save(os.path.join(OUTPUT_DIR, "true_sight_gu.png"))
    print("  [OK] true_sight_gu.png")


# ============================================================
# 6. 电眼蛊 (electric_eye_gu.png) - 64x64
# ============================================================
def generate_electric_eye_gu():
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    sclera = hex_to_rgba('#E8EEFF')
    sclera_shadow = hex_to_rgba('#C0D0EE')
    iris_blue = hex_to_rgba('#4488FF')
    iris_light = hex_to_rgba('#88BBFF')
    iris_dark = hex_to_rgba('#2255CC')
    pupil_color = hex_to_rgba('#112244')
    arc_yellow = hex_to_rgba('#FFFF44')
    arc_white = hex_to_rgba('#FFFFFF', 220)
    arc_blue = hex_to_rgba('#88CCFF', 200)
    glow_blue = hex_to_rgba('#4488FF', 80)

    cx, cy = 32, 32
    eye_rx, eye_ry = 14, 14  # 圆形眼球

    # 电弧光晕 (底层)
    for y in range(2, 62):
        for x in range(2, 62):
            dist = math.sqrt((x - cx) ** 2 + (y - cy) ** 2)
            if 16 < dist < 24:
                alpha = int(60 * (1 - (dist - 16) / 8))
                img.putpixel((x, y), (0x44, 0x88, 0xFF, alpha))

    # 眼球本体 (圆形)
    for y in range(cy - eye_ry - 1, cy + eye_ry + 2):
        for x in range(cx - eye_rx - 1, cx + eye_rx + 2):
            dx = x - cx
            dy = y - cy
            dist = math.sqrt(dx * dx + dy * dy)
            if dist <= eye_ry:
                t = dist / eye_ry
                if t > 0.85:
                    c = sclera_shadow
                else:
                    c = sclera
                img.putpixel((x, y), c)

    # 虹膜 (电蓝色)
    iris_r = 9
    for y in range(cy - iris_r - 1, cy + iris_r + 1):
        for x in range(cx - iris_r - 1, cx + iris_r + 1):
            dx = x - cx
            dy = y - cy
            dist = math.sqrt(dx * dx + dy * dy)
            if dist <= iris_r:
                t = dist / iris_r
                # 电流纹理
                angle = math.atan2(dy, dx)
                elec = math.sin(angle * 6 + dist * 0.5) * 0.5 + 0.5
                if elec > 0.7:
                    c = iris_light
                elif t > 0.8:
                    c = iris_dark
                else:
                    c = iris_blue
                # 外圈深色
                if t > 0.9:
                    c = hex_to_rgba('#1A3399')
                img.putpixel((x, y), c)

    # 瞳孔 (闪电形状)
    pupil_r = 3
    for y in range(cy - pupil_r, cy + pupil_r + 1):
        for x in range(cx - pupil_r, cx + pupil_r + 1):
            dx = x - cx
            dy = y - cy
            if dx * dx + dy * dy <= pupil_r * pupil_r:
                img.putpixel((x, y), pupil_color)

    # 瞳孔闪电符号
    lightning_pixels = [
        (32, 28), (31, 29), (31, 30), (32, 30),
        (33, 31), (32, 31), (32, 32), (31, 33),
        (31, 34), (32, 35), (33, 34),
    ]
    for lx, ly in lightning_pixels:
        if 0 <= lx < 64 and 0 <= ly < 64:
            img.putpixel((lx, ly), arc_yellow)

    # 高光
    for dy in range(-2, 0):
        for dx in range(-2, 0):
            img.putpixel((cx - 4 + dx, cy - 5 + dy), hex_to_rgba('#FFFFFF', 240))
    img.putpixel((cx + 3, cy - 3), hex_to_rgba('#FFFFFF', 200))

    # 电弧 (锯齿形，围绕眼球)
    random.seed(123)
    for arc_idx in range(6):
        start_angle = arc_idx * 60 + random.randint(-10, 10)
        arc_length = random.randint(30, 60)
        r = eye_ry + 3
        prev_x, prev_y = None, None
        for i in range(0, arc_length, 2):
            angle = math.radians(start_angle + i)
            jitter = random.randint(-2, 2)
            x = int(cx + (r + jitter) * math.cos(angle))
            y = int(cy + (r + jitter) * math.sin(angle))
            if 0 <= x < 64 and 0 <= y < 64:
                img.putpixel((x, y), arc_yellow)
                # 电弧粗度
                for ddx, ddy in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
                    nx, ny = x + ddx, y + ddy
                    if 0 <= nx < 64 and 0 <= ny < 64:
                        existing = img.getpixel((nx, ny))
                        if existing[3] < 100:
                            img.putpixel((nx, ny), (0xFF, 0xFF, 0x88, 140))

            # 分支电弧
            if i % 8 == 0 and random.random() > 0.4:
                branch_angle = angle + random.choice([-0.5, 0.5])
                for j in range(3, 7):
                    bx = int(cx + (r + jitter + j) * math.cos(branch_angle))
                    by = int(cy + (r + jitter + j) * math.sin(branch_angle))
                    if 0 <= bx < 64 and 0 <= by < 64:
                        alpha = max(60, 200 - j * 30)
                        img.putpixel((bx, by), (0xFF, 0xFF, 0x44, alpha))

    random.seed(42)  # 恢复种子

    img.save(os.path.join(OUTPUT_DIR, "electric_eye_gu.png"))
    print("  [OK] electric_eye_gu.png")


# ============================================================
# 7. 赌石 - 低档 (gambling_stone_low.png) - 64x64
# ============================================================
def generate_gambling_stone_low():
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    stone_gray = hex_to_rgba('#888888')
    dark_spot = hex_to_rgba('#666666')
    light_spot = hex_to_rgba('#AAAAAA')
    crack_color = hex_to_rgba('#555555')
    shadow = hex_to_rgba('#444444')
    hint_glow = hex_to_rgba('#BBBBAA', 160)

    cx, cy = 32, 34

    # 不规则石头形状 (用扰动的椭圆)
    random.seed(77)
    for y in range(10, 58):
        for x in range(10, 54):
            dx = x - cx
            dy = y - cy
            # 基础椭圆
            angle = math.atan2(dy, dx)
            # 不规则边缘
            noise = math.sin(angle * 3) * 2 + math.cos(angle * 5) * 1.5 + math.sin(angle * 7) * 0.8
            rx = 19 + noise
            ry = 18 + noise * 0.7
            if (dx / rx) ** 2 + (dy / ry) ** 2 <= 1:
                # 表面粗糙纹理
                tex = (x * 7 + y * 13) % 17
                if tex < 3:
                    c = dark_spot
                elif tex < 5:
                    c = light_spot
                else:
                    c = stone_gray

                # 底部阴影
                if dy > 10:
                    shade = min(40, (dy - 10) * 4)
                    c = (max(0, c[0] - shade), max(0, c[1] - shade), max(0, c[2] - shade), 255)

                # 顶部高光
                if dy < -5 and abs(dx) < 10:
                    bright = min(30, (-5 - dy) * 5)
                    c = (min(255, c[0] + bright), min(255, c[1] + bright), min(255, c[2] + bright), 255)

                img.putpixel((x, y), c)

    # 裂纹
    crack_start = [(22, 20), (38, 28), (28, 42)]
    for csx, csy in crack_start:
        cur_x, cur_y = csx, csy
        for i in range(8):
            if 0 <= cur_x < 64 and 0 <= cur_y < 64:
                px = img.getpixel((cur_x, cur_y))
                if px[3] > 0:
                    img.putpixel((cur_x, cur_y), crack_color)
            cur_x += random.choice([-1, 0, 1, 1])
            cur_y += random.choice([-1, 0, 1])

    # 一侧微弱光泽 (暗示内部有东西)
    for y in range(24, 38):
        for x in range(38, 48):
            px = img.getpixel((x, y))
            if px[3] > 0:
                dist = math.sqrt((x - 43) ** 2 + (y - 31) ** 2)
                if dist < 6:
                    brightness = int(20 * (1 - dist / 6))
                    c = (min(255, px[0] + brightness),
                         min(255, px[1] + brightness),
                         min(255, px[2] + brightness - 5), px[3])
                    img.putpixel((x, y), c)

    random.seed(42)
    img.save(os.path.join(OUTPUT_DIR, "gambling_stone_low.png"))
    print("  [OK] gambling_stone_low.png")


# ============================================================
# 8. 赌石 - 中档 (gambling_stone_medium.png) - 64x64
# ============================================================
def generate_gambling_stone_medium():
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    stone_base = hex_to_rgba('#777788')
    stone_light = hex_to_rgba('#8899AA')
    crystal_line = hex_to_rgba('#AABBCC')
    crystal_bright = hex_to_rgba('#BBCCDD')
    highlight = hex_to_rgba('#99AABB')
    shadow = hex_to_rgba('#556677')

    cx, cy = 32, 33

    # 更规整的石头形状
    random.seed(88)
    for y in range(10, 56):
        for x in range(10, 54):
            dx = x - cx
            dy = y - cy
            angle = math.atan2(dy, dx)
            noise = math.sin(angle * 4) * 1.5 + math.cos(angle * 6) * 0.8
            rx = 19 + noise
            ry = 18 + noise * 0.5
            if (dx / rx) ** 2 + (dy / ry) ** 2 <= 1:
                # 光滑表面
                t = ((dx / rx) ** 2 + (dy / ry) ** 2)
                if t > 0.85:
                    c = shadow
                elif dy < -6 and abs(dx) < 12:
                    c = highlight
                elif (x * 3 + y * 7) % 11 < 2:
                    c = stone_light
                else:
                    c = stone_base

                img.putpixel((x, y), c)

    # 水晶纹路 (几条弯曲的线)
    for line_idx in range(3):
        start_y = 18 + line_idx * 12
        for x_off in range(30):
            x = 14 + x_off
            y = int(start_y + math.sin(x_off * 0.3 + line_idx) * 3)
            if 0 <= x < 64 and 0 <= y < 64:
                px = img.getpixel((x, y))
                if px[3] > 0:
                    img.putpixel((x, y), crystal_line)
                    if y + 1 < 64 and img.getpixel((x, y + 1))[3] > 0:
                        img.putpixel((x, y + 1), crystal_bright)

    # 矿物斑点
    mineral_spots = [(24, 22), (38, 30), (30, 40), (42, 24), (20, 35)]
    for mx, my in mineral_spots:
        for ddy in range(-2, 3):
            for ddx in range(-2, 3):
                if ddx * ddx + ddy * ddy <= 4:
                    px_x, px_y = mx + ddx, my + ddy
                    if 0 <= px_x < 64 and 0 <= px_y < 64:
                        px = img.getpixel((px_x, px_y))
                        if px[3] > 0:
                            img.putpixel((px_x, px_y), crystal_bright)

    # 整体光泽感
    add_jade_sheen(img, cx - 4, cy - 6, 16, 25)

    random.seed(42)
    img.save(os.path.join(OUTPUT_DIR, "gambling_stone_medium.png"))
    print("  [OK] gambling_stone_medium.png")


# ============================================================
# 9. 赌石 - 高档 (gambling_stone_high.png) - 64x64
# ============================================================
def generate_gambling_stone_high():
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    stone_base = hex_to_rgba('#556677')
    stone_dark = hex_to_rgba('#445566')
    gu_glow = hex_to_rgba('#44BB66')
    gu_glow_light = hex_to_rgba('#66DD88', 200)
    gu_glow_faint = hex_to_rgba('#44BB66', 80)
    pattern_color = hex_to_rgba('#667788')
    highlight = hex_to_rgba('#778899')
    shadow = hex_to_rgba('#334455')

    cx, cy = 32, 33

    # 绿色蛊气光晕 (底层)
    for y in range(4, 60):
        for x in range(4, 60):
            dist = math.sqrt((x - cx) ** 2 + (y - cy) ** 2)
            if 20 < dist < 28:
                alpha = int(60 * (1 - (dist - 20) / 8))
                green_val = 0xBB - int((dist - 20) * 8)
                img.putpixel((x, y), (0x44, max(0x66, green_val), 0x66, alpha))

    # 精美石头形状 (更光滑)
    random.seed(99)
    for y in range(10, 56):
        for x in range(10, 54):
            dx = x - cx
            dy = y - cy
            angle = math.atan2(dy, dx)
            noise = math.sin(angle * 5) * 1.0 + math.cos(angle * 3) * 0.5
            rx = 19 + noise
            ry = 18 + noise * 0.4
            if (dx / rx) ** 2 + (dy / ry) ** 2 <= 1:
                t = ((dx / rx) ** 2 + (dy / ry) ** 2)
                if t > 0.9:
                    c = shadow
                elif dy < -8 and abs(dx) < 10:
                    c = highlight
                else:
                    c = stone_base

                # 天然花纹 (精细)
                pattern = math.sin(x * 0.4 + y * 0.3) + math.cos(x * 0.2 - y * 0.5)
                if abs(pattern) < 0.3:
                    c = pattern_color

                img.putpixel((x, y), c)

    # 蛊气纹路 (绿色脉络穿过石头)
    for line_idx in range(4):
        start_x = 16 + line_idx * 7
        start_y = 14 + line_idx * 5
        cur_x, cur_y = start_x, start_y
        for step in range(20):
            if 0 <= cur_x < 64 and 0 <= cur_y < 64:
                px = img.getpixel((cur_x, cur_y))
                if px[3] > 100:
                    img.putpixel((cur_x, cur_y), gu_glow)
                    # 纹路发光 (周围像素微绿)
                    for ddx, ddy in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
                        nx, ny = cur_x + ddx, cur_y + ddy
                        if 0 <= nx < 64 and 0 <= ny < 64:
                            npx = img.getpixel((nx, ny))
                            if npx[3] > 100:
                                img.putpixel((nx, ny), (
                                    max(0, npx[0] - 10),
                                    min(255, npx[1] + 20),
                                    max(0, npx[2] - 5),
                                    npx[3]
                                ))
            cur_x += random.choice([0, 1, 1])
            cur_y += random.choice([0, 1, 1, 0])

    # 一角隐约蛊虫轮廓 (非常淡)
    gu_cx, gu_cy = 40, 38
    gu_alpha = 50  # 极淡
    # 小椭圆蛊虫轮廓
    for y in range(gu_cy - 4, gu_cy + 5):
        for x in range(gu_cx - 3, gu_cx + 4):
            dx = x - gu_cx
            dy = y - gu_cy
            if (dx / 3) ** 2 + (dy / 4) ** 2 <= 1:
                px = img.getpixel((x, y))
                if px[3] > 100:
                    # 在石头色上微微叠加绿色蛊虫影
                    img.putpixel((x, y), (
                        max(0, px[0] - 15),
                        min(255, px[1] + 25),
                        max(0, px[2] - 10),
                        px[3]
                    ))

    # 表面光泽 (偏青色)
    add_jade_sheen(img, cx - 3, cy - 5, 15, 30)

    # 蛊气微光粒子
    glow_positions = [(14, 20), (48, 20), (16, 44), (50, 42), (32, 10), (32, 54)]
    for gx, gy in glow_positions:
        if 0 <= gx < 64 and 0 <= gy < 64:
            img.putpixel((gx, gy), (0x66, 0xDD, 0x88, 120))
            for ddx, ddy in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
                nx, ny = gx + ddx, gy + ddy
                if 0 <= nx < 64 and 0 <= ny < 64:
                    existing = img.getpixel((nx, ny))
                    if existing[3] < 80:
                        img.putpixel((nx, ny), (0x44, 0xBB, 0x66, 60))

    random.seed(42)
    img.save(os.path.join(OUTPUT_DIR, "gambling_stone_high.png"))
    print("  [OK] gambling_stone_high.png")


# ============================================================
# 主程序
# ============================================================
if __name__ == "__main__":
    ensure_dir()
    print("Generating scout/perception gu textures...")
    print()

    generate_snake_tongue_gu()
    generate_earth_listener_gu()
    generate_keen_ear_gu()
    generate_hidden_scale_gu()
    generate_true_sight_gu()
    generate_electric_eye_gu()
    generate_gambling_stone_low()
    generate_gambling_stone_medium()
    generate_gambling_stone_high()

    print()
    print("All 9 textures generated successfully!")
