"""
生成蛊师NPC贴图变体 - 6种战斗原型皮肤
标准 Minecraft 64x64 玩家皮肤格式 (1.8+ slim=false)
"""
from PIL import Image
import os

OUTPUT_DIR = "/mnt/e/code/mod/gu/src/main/resources/assets/reverend_insanity/textures/entity"
os.makedirs(OUTPUT_DIR, exist_ok=True)

# =============================================================
# Minecraft 1.8+ 玩家皮肤布局 (64x64, wide arms)
# =============================================================
# HEAD (inner):
#   top:    (8,0)  8x8    bottom: (16,0) 8x8
#   right:  (0,8)  8x8    front:  (8,8)  8x8
#   left:   (16,8) 8x8    back:   (24,8) 8x8
#
# HAT (outer, overlay):
#   top:    (40,0) 8x8    bottom: (48,0) 8x8
#   right:  (32,8) 8x8    front:  (40,8) 8x8
#   left:   (48,8) 8x8    back:   (56,8) 8x8
#
# BODY (inner):
#   top:    (20,16) 8x4   bottom: (28,16) 8x4
#   right:  (16,20) 4x12  front:  (20,20) 8x12
#   left:   (28,20) 4x12  back:   (32,20) 8x12
#
# JACKET (outer):
#   top:    (20,32) 8x4   bottom: (28,32) 8x4
#   right:  (16,36) 4x12  front:  (20,36) 8x12
#   left:   (28,36) 4x12  back:   (32,36) 8x12
#
# RIGHT ARM (inner):
#   top:    (44,16) 4x4   bottom: (48,16) 4x4
#   right:  (40,20) 4x12  front:  (44,20) 4x12
#   left:   (48,20) 4x12  back:   (52,20) 4x12
#
# RIGHT ARM SLEEVE (outer):
#   top:    (44,32) 4x4   bottom: (48,32) 4x4
#   right:  (40,36) 4x12  front:  (44,36) 4x12
#   left:   (48,36) 4x12  back:   (52,36) 4x12
#
# LEFT ARM (inner):
#   top:    (36,48) 4x4   bottom: (40,48) 4x4
#   right:  (32,52) 4x12  front:  (36,52) 4x12
#   left:   (40,52) 4x12  back:   (44,52) 4x12
#
# LEFT ARM SLEEVE (outer):
#   top:    (52,48) 4x4   bottom: (56,48) 4x4
#   right:  (48,52) 4x12  front:  (52,52) 4x12
#   left:   (56,52) 4x12  back:   (60,52) 4x12
#
# RIGHT LEG (inner):
#   top:    (4,16) 4x4    bottom: (8,16) 4x4
#   right:  (0,20) 4x12   front:  (4,20) 4x12
#   left:   (8,20) 4x12   back:   (12,20) 4x12
#
# RIGHT LEG PANTS (outer):
#   top:    (4,32) 4x4    bottom: (8,32) 4x4
#   right:  (0,36) 4x12   front:  (4,36) 4x12
#   left:   (8,36) 4x12   back:   (12,36) 4x12
#
# LEFT LEG (inner):
#   top:    (20,48) 4x4   bottom: (24,48) 4x4
#   right:  (16,52) 4x12  front:  (20,52) 4x12
#   left:   (24,52) 4x12  back:   (28,52) 4x12
#
# LEFT LEG PANTS (outer):
#   top:    (4,48) 4x4    bottom: (8,48) 4x4
#   right:  (0,52) 4x12   front:  (4,52) 4x12
#   left:   (8,52) 4x12   back:   (12,52) 4x12
# =============================================================


def fill_rect(pixels, x, y, w, h, color):
    for py in range(y, y + h):
        for px in range(x, x + w):
            if 0 <= px < 64 and 0 <= py < 64:
                pixels[px, py] = color


def shade(color, factor):
    r, g, b, a = color
    return (max(0, min(255, int(r * factor))),
            max(0, min(255, int(g * factor))),
            max(0, min(255, int(b * factor))), a)


def lighter(color, amount=30):
    r, g, b, a = color
    return (min(255, r + amount), min(255, g + amount), min(255, b + amount), a)


def darker(color, amount=30):
    r, g, b, a = color
    return (max(0, r - amount), max(0, g - amount), max(0, b - amount), a)


def draw_face(pixels, skin_c, hair_c, eye_c, has_veil=False, veil_c=None):
    """绘制头部 - 标准中国古代修士面部"""
    skin = skin_c
    hair = hair_c
    hair_dark = darker(hair_c, 20)
    eye = eye_c

    # --- 头顶 (8,0) 8x8 - 发顶 ---
    fill_rect(pixels, 8, 0, 8, 8, hair)
    # 束发造型 - 中间高
    for x in range(10, 14):
        pixels[x, 0] = hair_dark
        pixels[x, 1] = hair_dark

    # --- 头底 (16,0) 8x8 - 下巴底部 ---
    fill_rect(pixels, 16, 0, 8, 8, skin)
    # 下巴中间略深
    for x in range(18, 22):
        pixels[x, 4] = shade(skin, 0.9)

    # --- 头正面 (8,8) 8x8 ---
    fill_rect(pixels, 8, 8, 8, 8, skin)
    # 额头发际线 (顶部2行为头发)
    fill_rect(pixels, 8, 8, 8, 2, hair)
    # 鬓角
    pixels[8, 10] = hair
    pixels[8, 11] = hair
    pixels[15, 10] = hair
    pixels[15, 11] = hair
    # 眉毛
    pixels[10, 11] = darker(hair_c, 10)
    pixels[11, 11] = darker(hair_c, 10)
    pixels[13, 11] = darker(hair_c, 10)
    pixels[14, 11] = darker(hair_c, 10)
    # 眼睛
    pixels[10, 12] = (255, 255, 255, 255)  # 眼白
    pixels[11, 12] = eye  # 瞳孔
    pixels[13, 12] = eye  # 瞳孔
    pixels[14, 12] = (255, 255, 255, 255)  # 眼白
    # 鼻子
    pixels[12, 13] = shade(skin, 0.85)
    # 嘴
    pixels[11, 14] = shade(skin, 0.8)
    pixels[12, 14] = shade(skin, 0.8)

    if has_veil and veil_c:
        # 面纱遮住下半脸
        for y in range(13, 16):
            for x in range(8, 16):
                pixels[x, y] = veil_c

    # --- 头右侧 (0,8) 8x8 ---
    fill_rect(pixels, 0, 8, 8, 8, skin)
    fill_rect(pixels, 0, 8, 8, 2, hair)
    pixels[0, 10] = hair
    pixels[0, 11] = hair
    pixels[1, 10] = hair
    # 侧面耳朵
    pixels[6, 12] = shade(skin, 0.9)
    pixels[7, 12] = shade(skin, 0.9)

    # --- 头左侧 (16,8) 8x8 ---
    fill_rect(pixels, 16, 8, 8, 8, skin)
    fill_rect(pixels, 16, 8, 8, 2, hair)
    pixels[23, 10] = hair
    pixels[23, 11] = hair
    pixels[22, 10] = hair
    pixels[16, 12] = shade(skin, 0.9)
    pixels[17, 12] = shade(skin, 0.9)

    # --- 头后面 (24,8) 8x8 - 后脑全发 ---
    fill_rect(pixels, 24, 8, 8, 16, hair)
    # 束发
    fill_rect(pixels, 27, 10, 2, 4, hair_dark)


def draw_body_robe(pixels, robe_c, belt_c, inner_c, accent_c=None):
    """绘制身体 - 长袍风格"""
    robe = robe_c
    robe_side = shade(robe_c, 0.85)
    belt = belt_c
    inner = inner_c

    # --- 身体顶部 (20,16) 8x4 ---
    fill_rect(pixels, 20, 16, 8, 4, robe)

    # --- 身体底部 (28,16) 8x4 ---
    fill_rect(pixels, 28, 16, 8, 4, robe)

    # --- 身体正面 (20,20) 8x12 ---
    fill_rect(pixels, 20, 20, 8, 12, robe)
    # 衣领V字 (前2行)
    pixels[20, 20] = robe
    pixels[21, 20] = inner
    pixels[22, 20] = inner
    pixels[25, 20] = inner
    pixels[26, 20] = inner
    pixels[27, 20] = robe
    pixels[22, 21] = inner
    pixels[23, 21] = inner
    pixels[24, 21] = inner
    pixels[25, 21] = inner
    # 腰带 (y=26-27)
    fill_rect(pixels, 20, 26, 8, 2, belt)
    # 腰带扣
    if accent_c:
        pixels[23, 26] = accent_c
        pixels[24, 26] = accent_c

    # --- 身体右侧 (16,20) 4x12 ---
    fill_rect(pixels, 16, 20, 4, 12, robe_side)
    fill_rect(pixels, 16, 26, 4, 2, shade(belt, 0.9))

    # --- 身体左侧 (28,20) 4x12 ---
    fill_rect(pixels, 28, 20, 4, 12, robe_side)
    fill_rect(pixels, 28, 26, 4, 2, shade(belt, 0.9))

    # --- 身体后面 (32,20) 8x12 ---
    fill_rect(pixels, 32, 20, 8, 12, robe)
    fill_rect(pixels, 32, 26, 8, 2, shade(belt, 0.9))


def draw_arms(pixels, sleeve_c, skin_c, is_wide_sleeve=False):
    """绘制手臂"""
    sleeve = sleeve_c
    sleeve_side = shade(sleeve_c, 0.85)
    skin = skin_c

    # --- 右臂 ---
    # 顶 (44,16) 4x4
    fill_rect(pixels, 44, 16, 4, 4, sleeve)
    # 底 (48,16) 4x4
    fill_rect(pixels, 48, 16, 4, 4, skin)
    # 右侧 (40,20) 4x12
    fill_rect(pixels, 40, 20, 4, 12, sleeve_side)
    fill_rect(pixels, 40, 28, 4, 4, skin)  # 手部
    # 正面 (44,20) 4x12
    fill_rect(pixels, 44, 20, 4, 12, sleeve)
    fill_rect(pixels, 44, 28, 4, 4, skin)
    # 左侧 (48,20) 4x12
    fill_rect(pixels, 48, 20, 4, 12, sleeve_side)
    fill_rect(pixels, 48, 28, 4, 4, skin)
    # 后面 (52,20) 4x12
    fill_rect(pixels, 52, 20, 4, 12, sleeve)
    fill_rect(pixels, 52, 28, 4, 4, skin)

    if is_wide_sleeve:
        # 宽袖 - 袖口更长，手不露出
        fill_rect(pixels, 40, 28, 4, 4, sleeve_side)
        fill_rect(pixels, 44, 28, 4, 4, sleeve)
        fill_rect(pixels, 48, 28, 4, 4, sleeve_side)
        fill_rect(pixels, 52, 28, 4, 4, sleeve)

    # --- 左臂 ---
    # 顶 (36,48) 4x4
    fill_rect(pixels, 36, 48, 4, 4, sleeve)
    # 底 (40,48) 4x4
    fill_rect(pixels, 40, 48, 4, 4, skin)
    # 右侧 (32,52) 4x12
    fill_rect(pixels, 32, 52, 4, 12, sleeve_side)
    fill_rect(pixels, 32, 60, 4, 4, skin)
    # 正面 (36,52) 4x12
    fill_rect(pixels, 36, 52, 4, 12, sleeve)
    fill_rect(pixels, 36, 60, 4, 4, skin)
    # 左侧 (40,52) 4x12
    fill_rect(pixels, 40, 52, 4, 12, sleeve_side)
    fill_rect(pixels, 40, 60, 4, 4, skin)
    # 后面 (44,52) 4x12
    fill_rect(pixels, 44, 52, 4, 12, sleeve)
    fill_rect(pixels, 44, 60, 4, 4, skin)

    if is_wide_sleeve:
        fill_rect(pixels, 32, 60, 4, 4, sleeve_side)
        fill_rect(pixels, 36, 60, 4, 4, sleeve)
        fill_rect(pixels, 40, 60, 4, 4, sleeve_side)
        fill_rect(pixels, 44, 60, 4, 4, sleeve)


def draw_legs(pixels, pants_c, shoe_c):
    """绘制腿部"""
    pants = pants_c
    pants_side = shade(pants_c, 0.85)
    shoe = shoe_c

    # --- 右腿 ---
    # 顶 (4,16) 4x4
    fill_rect(pixels, 4, 16, 4, 4, pants)
    # 底 (8,16) 4x4
    fill_rect(pixels, 8, 16, 4, 4, shoe)
    # 右侧 (0,20) 4x12
    fill_rect(pixels, 0, 20, 4, 12, pants_side)
    fill_rect(pixels, 0, 28, 4, 4, shoe)
    # 正面 (4,20) 4x12
    fill_rect(pixels, 4, 20, 4, 12, pants)
    fill_rect(pixels, 4, 28, 4, 4, shoe)
    # 左侧 (8,20) 4x12
    fill_rect(pixels, 8, 20, 4, 12, pants_side)
    fill_rect(pixels, 8, 28, 4, 4, shoe)
    # 后面 (12,20) 4x12
    fill_rect(pixels, 12, 20, 4, 12, pants)
    fill_rect(pixels, 12, 28, 4, 4, shoe)

    # --- 左腿 ---
    # 顶 (20,48) 4x4
    fill_rect(pixels, 20, 48, 4, 4, pants)
    # 底 (24,48) 4x4
    fill_rect(pixels, 24, 48, 4, 4, shoe)
    # 右侧 (16,52) 4x12
    fill_rect(pixels, 16, 52, 4, 12, pants_side)
    fill_rect(pixels, 16, 60, 4, 4, shoe)
    # 正面 (20,52) 4x12
    fill_rect(pixels, 20, 52, 4, 12, pants)
    fill_rect(pixels, 20, 60, 4, 4, shoe)
    # 左侧 (24,52) 4x12
    fill_rect(pixels, 24, 52, 4, 12, pants_side)
    fill_rect(pixels, 24, 60, 4, 4, shoe)
    # 后面 (28,52) 4x12
    fill_rect(pixels, 28, 52, 4, 12, pants)
    fill_rect(pixels, 28, 60, 4, 4, shoe)


def draw_jacket_overlay(pixels, jacket_c, accent_c=None, pattern_func=None):
    """绘制外套覆盖层 (jacket overlay)"""
    jacket = jacket_c
    jacket_side = shade(jacket_c, 0.85)

    # --- 外套正面 (20,36) 8x12 ---
    fill_rect(pixels, 20, 36, 8, 12, jacket)
    # --- 外套右侧 (16,36) 4x12 ---
    fill_rect(pixels, 16, 36, 4, 12, jacket_side)
    # --- 外套左侧 (28,36) 4x12 ---
    fill_rect(pixels, 28, 36, 4, 12, jacket_side)
    # --- 外套后面 (32,36) 8x12 ---
    fill_rect(pixels, 32, 36, 8, 12, jacket)
    # --- 外套顶 (20,32) 8x4 ---
    fill_rect(pixels, 20, 32, 8, 4, jacket)
    # --- 外套底 (28,32) 8x4 ---
    fill_rect(pixels, 28, 32, 8, 4, jacket)

    if pattern_func:
        pattern_func(pixels)


def draw_hat_overlay(pixels, hat_c=None):
    """绘制帽子/头饰覆盖层"""
    if hat_c is None:
        return
    # HAT overlay front (40,8) 8x8 - 只画部分做发冠
    # 发冠/束发饰物
    pixels[43, 8] = hat_c
    pixels[44, 8] = hat_c
    pixels[42, 9] = hat_c
    pixels[43, 9] = hat_c
    pixels[44, 9] = hat_c
    pixels[45, 9] = hat_c


def add_robe_skirt(pixels, robe_c):
    """给长袍加裙摆效果 - 通过pants overlay实现"""
    robe = robe_c
    robe_side = shade(robe_c, 0.85)

    # 右腿裤子覆盖层 (让长袍延伸到腿部)
    # 顶 (4,32) 4x4
    fill_rect(pixels, 4, 32, 4, 4, robe)
    # 底 (8,32) 4x4
    fill_rect(pixels, 8, 32, 4, 4, robe)
    # 右侧 (0,36) 4x12
    fill_rect(pixels, 0, 36, 4, 12, robe_side)
    # 正面 (4,36) 4x12
    fill_rect(pixels, 4, 36, 4, 12, robe)
    # 左侧 (8,36) 4x12
    fill_rect(pixels, 8, 36, 4, 12, robe_side)
    # 后面 (12,36) 4x12
    fill_rect(pixels, 12, 36, 4, 12, robe)

    # 左腿裤子覆盖层
    # 顶 (4,48) 4x4
    fill_rect(pixels, 4, 48, 4, 4, robe)
    # 底 (8,48) 4x4
    fill_rect(pixels, 8, 48, 4, 4, robe)
    # 右侧 (0,52) 4x12
    fill_rect(pixels, 0, 52, 4, 12, robe_side)
    # 正面 (4,52) 4x12
    fill_rect(pixels, 4, 52, 4, 12, robe)
    # 左侧 (8,52) 4x12
    fill_rect(pixels, 8, 52, 4, 12, robe_side)
    # 后面 (12,52) 4x12
    fill_rect(pixels, 12, 52, 4, 12, robe)


# ==============================================================================
# 蛊师贴图变体
# ==============================================================================

def create_melee_skin():
    """力道/近战蛊师 - 厚重铠甲外观，深棕/铁灰"""
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    pixels = img.load()

    skin_c = (194, 160, 128, 255)      # 偏黄皮肤
    hair_c = (40, 30, 25, 255)          # 深棕发
    eye_c = (60, 45, 30, 255)           # 棕色眼
    robe_c = (85, 70, 55, 255)          # 深棕色铠甲衣
    belt_c = (50, 40, 30, 255)          # 暗棕腰带
    inner_c = (120, 100, 75, 255)       # 棕色内衬
    pants_c = (65, 55, 45, 255)         # 深棕裤
    shoe_c = (45, 35, 28, 255)          # 暗棕靴
    accent_c = (160, 130, 60, 255)      # 金铜色点缀
    armor_c = (100, 95, 88, 255)        # 铁灰色护甲

    draw_face(pixels, skin_c, hair_c, eye_c)
    draw_body_robe(pixels, robe_c, belt_c, inner_c, accent_c)
    draw_arms(pixels, robe_c, skin_c, is_wide_sleeve=False)
    draw_legs(pixels, pants_c, shoe_c)

    # 护甲覆盖层 - 胸甲通过jacket overlay
    def armor_pattern(px):
        # 前胸护甲板
        fill_rect(px, 21, 36, 6, 4, armor_c)
        fill_rect(px, 22, 37, 4, 2, lighter(armor_c, 15))
        # 肩甲
        fill_rect(px, 20, 36, 1, 3, armor_c)
        fill_rect(px, 27, 36, 1, 3, armor_c)
        # 腰带金属扣
        fill_rect(px, 22, 42, 4, 1, accent_c)
        px[23, 42] = lighter(accent_c, 20)
        px[24, 42] = lighter(accent_c, 20)

    draw_jacket_overlay(pixels, shade(robe_c, 0.95), accent_c, armor_pattern)
    draw_hat_overlay(pixels, accent_c)

    img.save(os.path.join(OUTPUT_DIR, "gu_master_melee.png"))
    print("  [OK] gu_master_melee.png")


def create_ranged_skin():
    """远程蛊师 - 长袍，深蓝/暗紫，宽袖"""
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    pixels = img.load()

    skin_c = (200, 170, 140, 255)       # 浅皮肤
    hair_c = (25, 20, 40, 255)          # 深紫黑发
    eye_c = (80, 100, 180, 255)         # 蓝色眼
    robe_c = (35, 40, 75, 255)          # 深蓝色长袍
    belt_c = (60, 50, 90, 255)          # 暗紫腰带
    inner_c = (70, 65, 110, 255)        # 蓝紫内衬
    pants_c = (30, 30, 55, 255)         # 深蓝裤
    shoe_c = (25, 25, 45, 255)          # 暗蓝靴
    accent_c = (140, 160, 220, 255)     # 浅蓝色星光点缀

    draw_face(pixels, skin_c, hair_c, eye_c)
    draw_body_robe(pixels, robe_c, belt_c, inner_c, accent_c)
    draw_arms(pixels, robe_c, skin_c, is_wide_sleeve=True)
    draw_legs(pixels, pants_c, shoe_c)

    # 长袍裙摆
    add_robe_skirt(pixels, robe_c)

    # 星辰纹饰overlay
    def star_pattern(px):
        # 前胸星纹
        px[23, 38] = accent_c
        px[24, 38] = accent_c
        px[21, 40] = lighter(accent_c, 30)
        px[26, 41] = lighter(accent_c, 30)
        px[22, 43] = accent_c
        px[25, 44] = accent_c
        # 月亮标记
        px[23, 36] = (180, 190, 230, 255)
        px[24, 36] = (180, 190, 230, 255)
        px[24, 37] = (200, 210, 240, 255)

    draw_jacket_overlay(pixels, shade(robe_c, 1.05), accent_c, star_pattern)
    # 发冠 - 银色
    draw_hat_overlay(pixels, (160, 170, 200, 255))

    img.save(os.path.join(OUTPUT_DIR, "gu_master_ranged.png"))
    print("  [OK] gu_master_ranged.png")


def create_control_skin():
    """控制蛊师 - 暗黑长袍，面纱遮面"""
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    pixels = img.load()

    skin_c = (180, 155, 130, 255)       # 偏暗皮肤
    hair_c = (20, 15, 25, 255)          # 黑紫发
    eye_c = (140, 60, 180, 255)         # 紫色眼
    veil_c = (30, 25, 35, 255)          # 面纱色
    robe_c = (30, 25, 40, 255)          # 暗黑紫长袍
    belt_c = (50, 35, 60, 255)          # 暗紫腰带
    inner_c = (60, 45, 75, 255)         # 深紫内衬
    pants_c = (25, 20, 35, 255)         # 暗黑裤
    shoe_c = (20, 15, 30, 255)          # 黑靴
    accent_c = (120, 50, 160, 255)      # 紫色点缀

    draw_face(pixels, skin_c, hair_c, eye_c, has_veil=True, veil_c=veil_c)
    draw_body_robe(pixels, robe_c, belt_c, inner_c, accent_c)
    draw_arms(pixels, robe_c, skin_c, is_wide_sleeve=True)
    draw_legs(pixels, pants_c, shoe_c)

    # 长袍裙摆
    add_robe_skirt(pixels, robe_c)

    # 魂道纹饰
    def soul_pattern(px):
        # 胸前魂纹
        px[23, 37] = accent_c
        px[24, 37] = accent_c
        px[22, 39] = shade(accent_c, 0.7)
        px[25, 39] = shade(accent_c, 0.7)
        px[23, 41] = shade(accent_c, 0.8)
        px[24, 41] = shade(accent_c, 0.8)
        # 暗道符文
        px[21, 44] = (80, 40, 100, 255)
        px[26, 44] = (80, 40, 100, 255)

    draw_jacket_overlay(pixels, shade(robe_c, 0.95), accent_c, soul_pattern)

    # 面纱在hat overlay - 延伸面纱到外层
    # HAT overlay front (40,8) 8x8
    for y in range(13, 16):
        for x in range(40, 48):
            pixels[x, y] = veil_c
    # 额前暗纹
    pixels[43, 10] = accent_c
    pixels[44, 10] = accent_c

    img.save(os.path.join(OUTPUT_DIR, "gu_master_control.png"))
    print("  [OK] gu_master_control.png")


def create_support_skin():
    """辅助蛊师 - 绿白长袍，温和外观"""
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    pixels = img.load()

    skin_c = (210, 185, 158, 255)       # 白净皮肤
    hair_c = (35, 30, 25, 255)          # 深棕发
    eye_c = (60, 120, 80, 255)          # 绿色眼
    robe_c = (65, 100, 65, 255)         # 翠绿长袍
    belt_c = (180, 170, 155, 255)       # 白色腰带
    inner_c = (200, 195, 185, 255)      # 白色内衬
    pants_c = (55, 85, 55, 255)         # 绿色裤
    shoe_c = (45, 65, 45, 255)          # 深绿靴
    accent_c = (120, 200, 130, 255)     # 浅绿点缀

    draw_face(pixels, skin_c, hair_c, eye_c)
    draw_body_robe(pixels, robe_c, belt_c, inner_c, accent_c)
    draw_arms(pixels, robe_c, skin_c, is_wide_sleeve=True)
    draw_legs(pixels, pants_c, shoe_c)

    # 长袍裙摆
    add_robe_skirt(pixels, robe_c)

    # 木道/水道纹饰
    def nature_pattern(px):
        # 藤蔓纹
        px[21, 37] = accent_c
        px[22, 38] = accent_c
        px[23, 39] = accent_c
        px[22, 40] = shade(accent_c, 0.8)
        # 水纹
        px[25, 38] = (100, 150, 200, 255)
        px[26, 39] = (100, 150, 200, 255)
        px[25, 40] = (100, 150, 200, 255)
        # 白色领口装饰
        fill_rect(px, 22, 36, 4, 1, inner_c)

    draw_jacket_overlay(pixels, shade(robe_c, 1.05), accent_c, nature_pattern)
    # 发冠 - 翡翠色
    draw_hat_overlay(pixels, (100, 180, 110, 255))

    img.save(os.path.join(OUTPUT_DIR, "gu_master_support.png"))
    print("  [OK] gu_master_support.png")


def create_rush_skin():
    """速攻蛊师 - 轻甲，风道/剑道，灵活"""
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    pixels = img.load()

    skin_c = (200, 172, 145, 255)       # 正常皮肤
    hair_c = (30, 30, 35, 255)          # 深灰黑发
    eye_c = (100, 140, 160, 255)        # 灰蓝眼
    robe_c = (140, 145, 155, 255)       # 浅灰色轻衣
    belt_c = (80, 85, 95, 255)          # 灰色腰带
    inner_c = (180, 180, 190, 255)      # 白灰内衬
    pants_c = (95, 100, 110, 255)       # 灰色裤
    shoe_c = (55, 55, 65, 255)          # 深灰靴
    accent_c = (160, 200, 220, 255)     # 风蓝色点缀

    draw_face(pixels, skin_c, hair_c, eye_c)
    draw_body_robe(pixels, robe_c, belt_c, inner_c, accent_c)
    draw_arms(pixels, robe_c, skin_c, is_wide_sleeve=False)
    draw_legs(pixels, pants_c, shoe_c)

    # 轻甲风格 - 不用裙摆，更紧凑

    # 风道纹饰
    def wind_pattern(px):
        # 风纹流线
        px[21, 37] = accent_c
        px[22, 37] = accent_c
        px[23, 38] = accent_c
        px[24, 38] = accent_c
        px[25, 39] = accent_c
        px[26, 39] = accent_c
        # 剑纹
        px[23, 41] = (200, 210, 220, 255)
        px[24, 41] = (200, 210, 220, 255)
        px[23, 42] = (180, 190, 200, 255)
        px[24, 42] = (180, 190, 200, 255)
        px[23, 43] = (160, 170, 180, 255)

    draw_jacket_overlay(pixels, shade(robe_c, 0.98), accent_c, wind_pattern)
    # 简单束发
    draw_hat_overlay(pixels, (120, 130, 150, 255))

    img.save(os.path.join(OUTPUT_DIR, "gu_master_rush.png"))
    print("  [OK] gu_master_rush.png")


def create_demonic_skin():
    """魔道蛊师 - 黑红衣服，邪气外观"""
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    pixels = img.load()

    skin_c = (175, 150, 130, 255)       # 偏暗皮肤
    hair_c = (20, 10, 10, 255)          # 黑红发
    eye_c = (180, 40, 40, 255)          # 红色眼
    robe_c = (35, 15, 15, 255)          # 暗红黑长袍
    belt_c = (80, 25, 25, 255)          # 血红腰带
    inner_c = (120, 35, 30, 255)        # 红色内衬
    pants_c = (30, 12, 12, 255)         # 暗黑红裤
    shoe_c = (20, 8, 8, 255)            # 黑靴
    accent_c = (200, 50, 50, 255)       # 鲜红点缀
    blood_c = (150, 20, 20, 255)        # 血色

    draw_face(pixels, skin_c, hair_c, eye_c)
    draw_body_robe(pixels, robe_c, belt_c, inner_c, accent_c)
    draw_arms(pixels, robe_c, skin_c, is_wide_sleeve=True)
    draw_legs(pixels, pants_c, shoe_c)

    # 长袍裙摆
    add_robe_skirt(pixels, robe_c)

    # 魔道血纹
    def demonic_pattern(px):
        # 血色纹路
        px[22, 37] = blood_c
        px[25, 37] = blood_c
        px[21, 39] = accent_c
        px[26, 39] = accent_c
        px[23, 38] = blood_c
        px[24, 38] = blood_c
        # 杀字纹
        px[23, 40] = accent_c
        px[24, 40] = accent_c
        px[22, 41] = shade(accent_c, 0.8)
        px[25, 41] = shade(accent_c, 0.8)
        px[23, 42] = blood_c
        px[24, 42] = blood_c
        # 骨道装饰 - 腰部骨饰
        px[21, 43] = (200, 195, 180, 255)
        px[26, 43] = (200, 195, 180, 255)

    draw_jacket_overlay(pixels, shade(robe_c, 1.1), accent_c, demonic_pattern)

    # 角状发饰
    pixels[42, 8] = (60, 15, 15, 255)
    pixels[45, 8] = (60, 15, 15, 255)
    pixels[43, 9] = accent_c
    pixels[44, 9] = accent_c

    img.save(os.path.join(OUTPUT_DIR, "gu_master_demonic.png"))
    print("  [OK] gu_master_demonic.png")


if __name__ == "__main__":
    print("=== 生成蛊师NPC贴图变体 ===")
    print(f"输出目录: {OUTPUT_DIR}")
    print()

    create_melee_skin()
    create_ranged_skin()
    create_control_skin()
    create_support_skin()
    create_rush_skin()
    create_demonic_skin()

    print()
    print("=== 全部贴图生成完成 ===")
