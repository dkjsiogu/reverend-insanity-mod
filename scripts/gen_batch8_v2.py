#!/usr/bin/env python3
"""Batch 8 蛊虫物品贴图生成 v2: 改进春秋蝉和肉骨蛊"""

from PIL import Image, ImageDraw
import math
import random

OUTPUT_DIR = "/mnt/e/code/mod/gu/src/main/resources/assets/reverend_insanity/textures/item/"
SIZE = 64

random.seed(42)


def blend(c1, c2, t):
    t = max(0.0, min(1.0, t))
    r = int(c1[0] + (c2[0] - c1[0]) * t)
    g = int(c1[1] + (c2[1] - c1[1]) * t)
    b = int(c1[2] + (c2[2] - c1[2]) * t)
    a = int(c1[3] + (c2[3] - c1[3]) * t) if len(c1) > 3 and len(c2) > 3 else 255
    return (max(0, min(255, r)), max(0, min(255, g)), max(0, min(255, b)), max(0, min(255, a)))


def dist(x1, y1, x2, y2):
    return math.sqrt((x1 - x2) ** 2 + (y1 - y2) ** 2)


def set_pixel(img, x, y, color):
    x, y = int(x), int(y)
    if 0 <= x < SIZE and 0 <= y < SIZE:
        img.putpixel((x, y), color)


def alpha_blend(img, x, y, color):
    x, y = int(x), int(y)
    if 0 <= x < SIZE and 0 <= y < SIZE:
        existing = img.getpixel((x, y))
        a = color[3] / 255.0
        r = int(existing[0] * (1 - a) + color[0] * a)
        g = int(existing[1] * (1 - a) + color[1] * a)
        b = int(existing[2] * (1 - a) + color[2] * a)
        na = min(255, existing[3] + color[3])
        img.putpixel((x, y), (r, g, b, na))


def draw_line(img, x1, y1, x2, y2, color, thickness=1):
    steps = max(abs(x2 - x1), abs(y2 - y1), 1)
    for i in range(steps + 1):
        t = i / steps
        x = x1 + (x2 - x1) * t
        y = y1 + (y2 - y1) * t
        if thickness <= 1:
            set_pixel(img, x, y, color)
        else:
            for dy in range(-thickness // 2, thickness // 2 + 1):
                for dx in range(-thickness // 2, thickness // 2 + 1):
                    if abs(dx) + abs(dy) <= thickness:
                        set_pixel(img, x + dx, y + dy, color)


def in_ellipse(x, y, cx, cy, rx, ry):
    dx = (x - cx) / rx if rx > 0 else 999
    dy = (y - cy) / ry if ry > 0 else 999
    return dx * dx + dy * dy


# ============================================================
# 1. 春秋蝉 (Spring Autumn Cicada) - 六转仙蛊 v2
# ============================================================
def generate_spring_autumn_cicada():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))

    body_dark = (85, 60, 10, 255)
    body_main = (139, 105, 20, 255)
    body_mid = (166, 124, 0, 255)
    body_light = (196, 160, 53, 255)
    body_highlight = (220, 190, 90, 255)
    ring_dark = (75, 55, 8, 255)
    outline = (55, 40, 5, 255)
    eye_dark = (30, 10, 0, 255)

    # 身体中心参考
    bcx, bcy = 32, 36

    # === 淡金色光晕 ===
    for y in range(SIZE):
        for x in range(SIZE):
            d = dist(x, y, bcx, bcy - 4)
            if d < 30:
                a = int(25 * max(0, 1 - d / 30))
                alpha_blend(img, x, y, (255, 220, 80, a))

    # === 翅膀 (半透明叶形，在身体之前绘制) ===
    # 上翅 - 大叶形，向右上方展开
    def draw_wing(img, base_x, base_y, length, width, angle_deg, flip=False):
        angle = math.radians(angle_deg)
        cos_a, sin_a = math.cos(angle), math.sin(angle)

        for i in range(int(length * 2)):
            t = i / (length * 2)
            # 沿翅膀长轴的位置
            along = t * length
            # 翅膀宽度: 从根部窄到中间宽再到尖端窄
            w = width * math.sin(t * math.pi) * (1 - t * 0.2)

            for j in range(int(w * 2 + 1)):
                cross = (j - w)
                if flip:
                    cross = -cross

                # 旋转到翅膀方向
                px = base_x + along * cos_a - cross * sin_a
                py = base_y + along * sin_a + cross * cos_a

                if 0 <= int(px) < SIZE and 0 <= int(py) < SIZE:
                    # 翅膀颜色 - 半透明金色
                    edge_t = abs(cross) / max(w, 0.1)
                    base_alpha = int(65 * (1 - edge_t * 0.5) * (1 - t * 0.3))

                    # 叶脉主脉 (沿长轴中心)
                    if abs(cross) < 0.8:
                        alpha_blend(img, px, py, (200, 165, 40, base_alpha + 40))
                    else:
                        alpha_blend(img, px, py, (255, 215, 70, base_alpha))

                    # 横向叶脉
                    vein_spacing = 4
                    if int(along) % vein_spacing < 1 and abs(cross) > 1:
                        alpha_blend(img, px, py, (210, 175, 50, 50))

                    # 斜向网脉
                    diag = (int(along) + int(abs(cross))) % 3
                    if diag == 0 and abs(cross) > 1.5 and t > 0.15:
                        alpha_blend(img, px, py, (195, 160, 45, 35))

                    # 翅膀边缘高光
                    if edge_t > 0.8:
                        alpha_blend(img, px, py, (255, 240, 150, 20))

    # 右上翅
    draw_wing(img, 30, 30, 22, 9, -55, flip=False)
    # 左上翅 (稍短，侧面视角)
    draw_wing(img, 30, 30, 10, 5, -125, flip=True)
    # 右下翅 (较小)
    draw_wing(img, 32, 34, 17, 7, -40, flip=False)
    # 左下翅
    draw_wing(img, 32, 34, 8, 4, -140, flip=True)

    # === 蝉身体 (侧面朝左) ===
    # 头部
    head_cx, head_cy = 23, 34
    head_r = 6
    for y in range(head_cy - head_r - 1, head_cy + head_r + 2):
        for x in range(head_cx - head_r - 1, head_cx + head_r + 2):
            d = dist(x, y, head_cx, head_cy)
            if d <= head_r:
                t = d / head_r
                c = blend(body_highlight, body_dark, t * 0.6)
                if y < head_cy - 2:
                    c = blend(c, body_highlight, 0.3 * (1 - t))
                if y > head_cy + 2:
                    c = blend(c, body_dark, 0.25)
                set_pixel(img, x, y, c)

    # 头部轮廓
    for a in range(360):
        rad = math.radians(a)
        set_pixel(img, head_cx + (head_r + 0.5) * math.cos(rad),
                  head_cy + (head_r + 0.5) * math.sin(rad), outline)

    # 复眼 (大而圆)
    eye_cx, eye_cy = 20, 32
    for dy in range(-2, 3):
        for dx in range(-2, 3):
            if dx * dx + dy * dy <= 4:
                c = eye_dark if dx * dx + dy * dy <= 2 else (60, 30, 5, 255)
                set_pixel(img, eye_cx + dx, eye_cy + dy, c)
    # 眼睛高光
    set_pixel(img, 19, 31, (180, 140, 80, 255))

    # 触角 (两根，向前上方)
    for i in range(7):
        x = 19 - i * 0.8
        y1 = 29 - i * 1.1
        y2 = 30 - i * 0.7
        set_pixel(img, x, y1, body_dark)
        set_pixel(img, x + 0.5, y2, body_dark)
    # 触角尖端金光
    alpha_blend(img, 13, 21, (255, 210, 60, 180))
    alpha_blend(img, 15, 25, (255, 210, 60, 150))

    # 口器 (长喙，向下前方)
    for i in range(6):
        set_pixel(img, 18 - i * 0.3, 38 + i * 0.5, (70, 50, 10, 255))

    # 前胸
    thorax_cx, thorax_cy = 28, 35
    for y in range(thorax_cy - 5, thorax_cy + 6):
        for x in range(thorax_cx - 4, thorax_cx + 5):
            dd = in_ellipse(x, y, thorax_cx, thorax_cy, 4, 5)
            if dd <= 1.0:
                d = math.sqrt(dd)
                c = blend(body_mid, body_dark, d * 0.5)
                if y < thorax_cy - 1:
                    c = blend(c, body_light, 0.25 * (1 - d))
                set_pixel(img, x, y, c)

    # 中后胸连接
    for y in range(32, 40):
        for x in range(31, 36):
            dd = in_ellipse(x, y, 33, 36, 3, 4)
            if dd <= 1.0:
                d = math.sqrt(dd)
                c = blend(body_main, body_dark, d * 0.4)
                set_pixel(img, x, y, c)

    # 腹部 - 带年轮纹的长椭圆
    abd_cx, abd_cy = 41, 37
    abd_rx, abd_ry = 11, 6
    for y in range(abd_cy - abd_ry - 1, abd_cy + abd_ry + 2):
        for x in range(abd_cx - abd_rx - 1, abd_cx + abd_rx + 2):
            dd = in_ellipse(x, y, abd_cx, abd_cy, abd_rx, abd_ry)
            if dd <= 1.0:
                d = math.sqrt(dd)
                c = blend(body_main, body_dark, d * 0.5)

                # 上面亮
                if y < abd_cy - 1:
                    c = blend(c, body_light, 0.2 * (1 - d))
                # 底部暗
                if y > abd_cy + 2:
                    c = blend(c, body_dark, 0.2)

                # 年轮纹理 - 从腹部中心向外的同心弧
                ring_d = dist(x, y, abd_cx - 4, abd_cy)
                ring_phase = ring_d % 3.0
                if ring_phase < 0.7:
                    c = blend(c, ring_dark, 0.35)
                elif 1.2 < ring_phase < 1.8:
                    c = blend(c, body_light, 0.15)

                # 腹节分段线 (水平)
                seg = (x - abd_cx + abd_rx) % 4
                if seg == 0 and d < 0.85:
                    c = blend(c, ring_dark, 0.2)

                set_pixel(img, x, y, c)

    # 腹部尖端
    for i in range(1, 5):
        y_range = max(1, 3 - i)
        for dy in range(-y_range, y_range + 1):
            t = i / 5.0
            c = blend(body_main, body_dark, 0.3 + t * 0.4)
            set_pixel(img, abd_cx + abd_rx + i, abd_cy + dy, c)

    # 腹部轮廓
    for a in range(360):
        rad = math.radians(a)
        ox = abd_cx + (abd_rx + 0.5) * math.cos(rad)
        oy = abd_cy + (abd_ry + 0.5) * math.sin(rad)
        if 0 <= int(ox) < SIZE and 0 <= int(oy) < SIZE:
            px = img.getpixel((int(ox), int(oy)))
            if px[3] < 100:
                set_pixel(img, ox, oy, outline)

    # 身体下方轮廓线
    for a in range(180, 360):
        rad = math.radians(a)
        # 前胸
        set_pixel(img, thorax_cx + 4.5 * math.cos(rad), thorax_cy + 5.5 * math.sin(rad), outline)

    # === 腿 (3对) ===
    # 前腿
    draw_line(img, 24, 39, 21, 45, (80, 60, 10, 255))
    draw_line(img, 21, 45, 19, 47, (80, 60, 10, 255))
    draw_line(img, 19, 47, 17, 46, (80, 60, 10, 255))
    # 中腿
    draw_line(img, 30, 40, 28, 47, (80, 60, 10, 255))
    draw_line(img, 28, 47, 26, 49, (80, 60, 10, 255))
    draw_line(img, 26, 49, 24, 48, (80, 60, 10, 255))
    # 后腿
    draw_line(img, 38, 42, 37, 48, (80, 60, 10, 255))
    draw_line(img, 37, 48, 35, 50, (80, 60, 10, 255))
    draw_line(img, 35, 50, 33, 49, (80, 60, 10, 255))

    # === 时间符文光点 (六转仙蛊神秘感) ===
    rune_positions = [
        (8, 12), (54, 10), (5, 28), (58, 26),
        (6, 48), (57, 46), (14, 56), (50, 56),
        (26, 8), (42, 6), (10, 20), (52, 18),
    ]
    for rx, ry in rune_positions:
        d_to_body = dist(rx, ry, bcx, bcy)
        if d_to_body > 14:
            brightness = random.randint(180, 255)
            a = random.randint(140, 230)
            # 核心亮点
            set_pixel(img, rx, ry, (255, brightness, 50, a))
            # 十字扩散
            for dd in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
                alpha_blend(img, rx + dd[0], ry + dd[1], (255, brightness, 50, a // 3))
            # 对角微弱
            for dd in [(-1, -1), (1, -1), (-1, 1), (1, 1)]:
                alpha_blend(img, rx + dd[0], ry + dd[1], (255, brightness, 50, a // 6))

    img.save(OUTPUT_DIR + "spring_autumn_cicada.png")
    print("spring_autumn_cicada.png v2 generated")


# ============================================================
# 2. 肉骨蛊 (Flesh Bone Gu) - 一转蛊虫 v2
# ============================================================
def generate_flesh_bone_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))

    flesh_dark = (90, 0, 0, 255)
    flesh_main = (139, 10, 10, 255)
    flesh_mid = (178, 34, 34, 255)
    flesh_light = (200, 55, 55, 255)
    flesh_highlight = (225, 90, 90, 255)
    bone_white = (255, 230, 225, 255)
    bone_cream = (240, 215, 205, 255)
    outline = (50, 0, 0, 255)
    horn_light = (240, 220, 210, 255)
    horn_dark = (200, 170, 160, 255)

    cx, cy = 32, 33

    # 蜷曲的蠕虫身体路径 - C形蜷曲
    # 用贝塞尔曲线定义蜷曲路径
    path_points = []
    num_pts = 80
    for i in range(num_pts):
        t = i / (num_pts - 1)
        # C形蜷曲路径
        angle = -0.5 + t * 3.8  # 弧度范围
        r = 9 + 2 * math.sin(t * math.pi * 1.5)  # 路径半径变化
        px = cx + r * math.cos(angle)
        py = cy + r * math.sin(angle) * 0.7  # Y方向压缩一些

        # 身体粗细: 头粗 -> 中间粗 -> 尾细
        if t < 0.15:
            width = 3.5 + t / 0.15 * 1.5  # 头部到身体
        elif t < 0.6:
            width = 5.0  # 中段粗
        else:
            width = 5.0 * (1 - (t - 0.6) / 0.4) + 1.5  # 尾部渐细

        path_points.append((px, py, width))

    # 绘制身体 (从尾到头，让头部覆盖)
    for idx in range(len(path_points) - 1, -1, -1):
        sx, sy, sw = path_points[idx]
        t_along = idx / len(path_points)

        for y in range(max(0, int(sy - sw - 2)), min(SIZE, int(sy + sw + 2))):
            for x in range(max(0, int(sx - sw - 2)), min(SIZE, int(sx + sw + 2))):
                d = dist(x, y, sx, sy)
                if d <= sw:
                    dt = d / sw  # 0=中心, 1=边缘

                    # 基础颜色 - 圆柱体明暗
                    c = blend(flesh_light, flesh_dark, dt * 0.7)
                    # 上方高光
                    if y < sy - sw * 0.2:
                        c = blend(c, flesh_highlight, 0.3 * (1 - dt))
                    # 下方阴影
                    if y > sy + sw * 0.3:
                        c = blend(c, flesh_dark, 0.2)

                    # 骨骼纹理 - 沿身体间隔出现白色骨节
                    bone_segment = idx % 12
                    if bone_segment < 3 and dt < 0.7:
                        bone_t = 0.25 * (1 - bone_segment / 3.0) * (1 - dt * 0.8)
                        c = blend(c, bone_white, bone_t)
                        # 骨节突起效果 - 中间更白
                        if bone_segment == 1 and dt < 0.4:
                            c = blend(c, bone_cream, 0.2)

                    # 肉纹理 - 细微横纹
                    if idx % 3 == 0 and dt < 0.85:
                        c = blend(c, flesh_dark, 0.08)

                    # 身体分节纹
                    if idx % 6 == 0 and dt < 0.6:
                        c = blend(c, flesh_dark, 0.15)

                    set_pixel(img, x, y, c)

    # 身体轮廓 (沿路径绘制)
    for idx, (sx, sy, sw) in enumerate(path_points):
        for a in range(0, 360, 4):
            rad = math.radians(a)
            ox = int(sx + (sw + 0.6) * math.cos(rad))
            oy = int(sy + (sw + 0.6) * math.sin(rad))
            if 0 <= ox < SIZE and 0 <= oy < SIZE:
                px = img.getpixel((ox, oy))
                if px[3] < 80:
                    set_pixel(img, ox, oy, outline)

    # 头部 (在路径起点)
    hx, hy, _ = path_points[0]
    head_r = 5.5
    for y in range(max(0, int(hy - head_r - 1)), min(SIZE, int(hy + head_r + 2))):
        for x in range(max(0, int(hx - head_r - 1)), min(SIZE, int(hx + head_r + 2))):
            d = dist(x, y, hx, hy)
            if d <= head_r:
                dt = d / head_r
                c = blend(flesh_mid, flesh_dark, dt * 0.5)
                if y < hy - 1:
                    c = blend(c, flesh_highlight, 0.25 * (1 - dt))
                if y > hy + 2:
                    c = blend(c, flesh_dark, 0.2)
                set_pixel(img, x, y, c)

    # 头部轮廓
    for a in range(360):
        rad = math.radians(a)
        set_pixel(img, hx + (head_r + 0.5) * math.cos(rad),
                  hy + (head_r + 0.5) * math.sin(rad), outline)

    # 角状突起 (头顶两个小角)
    # 向路径前方计算方向
    dx_dir = path_points[0][0] - path_points[5][0]
    dy_dir = path_points[0][1] - path_points[5][1]
    head_angle = math.atan2(dy_dir, dx_dir)

    for horn_side in [-0.6, 0.6]:
        horn_angle = head_angle + math.pi / 2 + horn_side
        for i in range(5):
            t = i / 4.0
            hpx = hx + math.cos(horn_angle - 0.3) * (head_r + i * 0.8)
            hpy = hy + math.sin(horn_angle - 0.3) * (head_r + i * 0.8) - i * 0.5
            c = blend(horn_light, horn_dark, t * 0.5)
            set_pixel(img, hpx, hpy, c)
            if i < 3:
                set_pixel(img, hpx + 1, hpy, blend(c, flesh_mid, 0.3))

    # 眼睛
    eye_offset_x = math.cos(head_angle) * 3
    eye_offset_y = math.sin(head_angle) * 3
    for side in [-1.5, 1.5]:
        perp_x = -math.sin(head_angle) * side
        perp_y = math.cos(head_angle) * side
        ex = int(hx + eye_offset_x + perp_x)
        ey = int(hy + eye_offset_y + perp_y - 1)
        # 小红眼
        set_pixel(img, ex, ey, (220, 40, 40, 255))
        set_pixel(img, ex + 1, ey, (220, 40, 40, 255))
        # 瞳孔
        set_pixel(img, ex, ey, (40, 0, 0, 255))
        # 高光
        alpha_blend(img, ex + 1, ey - 1, (255, 180, 180, 150))

    # 嘴 - 前方小口
    mouth_x = int(hx + math.cos(head_angle) * (head_r - 1))
    mouth_y = int(hy + math.sin(head_angle) * (head_r - 1))
    set_pixel(img, mouth_x, mouth_y, (60, 0, 0, 255))
    set_pixel(img, mouth_x + 1, mouth_y, (60, 0, 0, 255))
    set_pixel(img, mouth_x, mouth_y + 1, (60, 0, 0, 255))

    # 骨刺突起 (身体上几个位置)
    spine_indices = [20, 40, 55, 68]
    for si in spine_indices:
        if si < len(path_points):
            sx, sy, sw = path_points[si]
            # 向外突出的小骨刺
            spine_angle = random.uniform(0, 2 * math.pi)
            for i in range(4):
                t = i / 3.0
                spx = sx + (sw + i * 0.8) * math.cos(spine_angle)
                spy = sy + (sw + i * 0.8) * math.sin(spine_angle)
                c = blend(bone_white, bone_cream, t)
                c = (c[0], c[1], c[2], int(255 * (1 - t * 0.3)))
                set_pixel(img, spx, spy, c)

    # 尾部 - 最后的点
    tx, ty, tw = path_points[-1]
    for i in range(3):
        set_pixel(img, tx + i, ty, blend(flesh_dark, flesh_main, 0.5))

    img.save(OUTPUT_DIR + "flesh_bone_gu.png")
    print("flesh_bone_gu.png v2 generated")


# ============================================================
# 主程序
# ============================================================
if __name__ == "__main__":
    print("Regenerating improved textures...")
    generate_spring_autumn_cicada()
    generate_flesh_bone_gu()
    print("Improved textures done!")
