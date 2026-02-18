"""
十大九转尊者64x64 Minecraft玩家皮肤贴图生成器
生成标准64x64 Minecraft skin格式的PNG文件
"""
from PIL import Image
import os

output_dir = "/mnt/e/code/mod/gu/src/main/resources/assets/reverend_insanity/textures/entity"
os.makedirs(output_dir, exist_ok=True)

# ===== Minecraft 64x64 Skin 区域定义 =====
# 头部(Head) - 第一层
HEAD_TOP    = (8, 0, 16, 8)    # 头顶 8x8
HEAD_BOTTOM = (16, 0, 24, 8)   # 头底 8x8
HEAD_RIGHT  = (0, 8, 8, 16)    # 右脸(玩家视角左侧) 8x8
HEAD_FRONT  = (8, 8, 16, 16)   # 前脸 8x8
HEAD_LEFT   = (16, 8, 24, 16)  # 左脸(玩家视角右侧) 8x8
HEAD_BACK   = (24, 8, 32, 16)  # 后脑 8x8

# 头部 - 第二层(overlay/hat)
HAT_TOP     = (40, 0, 48, 8)
HAT_BOTTOM  = (48, 0, 56, 8)
HAT_RIGHT   = (32, 8, 40, 16)
HAT_FRONT   = (40, 8, 48, 16)
HAT_LEFT    = (48, 8, 56, 16)
HAT_BACK    = (56, 8, 64, 16)

# 身体(Body) - 第一层
BODY_TOP    = (20, 16, 28, 20)   # 上面 8x4
BODY_BOTTOM = (28, 16, 36, 20)   # 下面 8x4
BODY_RIGHT  = (16, 20, 20, 32)   # 右侧 4x12
BODY_FRONT  = (20, 20, 28, 32)   # 前面 8x12
BODY_LEFT   = (28, 20, 32, 32)   # 左侧 4x12
BODY_BACK   = (32, 20, 40, 32)   # 后面 8x12

# 身体 - 第二层
BODY2_TOP    = (20, 32, 28, 36)
BODY2_BOTTOM = (28, 32, 36, 36)
BODY2_RIGHT  = (16, 36, 20, 48)
BODY2_FRONT  = (20, 36, 28, 48)
BODY2_LEFT   = (28, 36, 32, 48)
BODY2_BACK   = (32, 36, 40, 48)

# 右臂(Right Arm) - 第一层
RARM_TOP    = (44, 16, 48, 20)   # 上 4x4
RARM_BOTTOM = (48, 16, 52, 20)   # 下 4x4
RARM_RIGHT  = (40, 20, 44, 32)   # 外 4x12
RARM_FRONT  = (44, 20, 48, 32)   # 前 4x12
RARM_LEFT   = (48, 20, 52, 32)   # 内 4x12
RARM_BACK   = (52, 20, 56, 32)   # 后 4x12

# 右臂 - 第二层
RARM2_TOP    = (44, 32, 48, 36)
RARM2_BOTTOM = (48, 32, 52, 36)
RARM2_RIGHT  = (40, 36, 44, 48)
RARM2_FRONT  = (44, 36, 48, 48)
RARM2_LEFT   = (48, 36, 52, 48)
RARM2_BACK   = (52, 36, 56, 48)

# 左臂(Left Arm) - 第一层
LARM_TOP    = (36, 48, 40, 52)
LARM_BOTTOM = (40, 48, 44, 52)
LARM_RIGHT  = (32, 52, 36, 64)
LARM_FRONT  = (36, 52, 40, 64)
LARM_LEFT   = (40, 52, 44, 64)
LARM_BACK   = (44, 52, 48, 64)

# 左臂 - 第二层
LARM2_TOP    = (52, 48, 56, 52)
LARM2_BOTTOM = (56, 48, 60, 52)
LARM2_RIGHT  = (48, 52, 52, 64)
LARM2_FRONT  = (52, 52, 56, 64)
LARM2_LEFT   = (56, 52, 60, 64)
LARM2_BACK   = (60, 52, 64, 64)

# 右腿(Right Leg) - 第一层
RLEG_TOP    = (4, 16, 8, 20)
RLEG_BOTTOM = (8, 16, 12, 20)
RLEG_RIGHT  = (0, 20, 4, 32)
RLEG_FRONT  = (4, 20, 8, 32)
RLEG_LEFT   = (8, 20, 12, 32)
RLEG_BACK   = (12, 20, 16, 32)

# 右腿 - 第二层
RLEG2_TOP    = (4, 32, 8, 36)
RLEG2_BOTTOM = (8, 32, 12, 36)
RLEG2_RIGHT  = (0, 36, 4, 48)
RLEG2_FRONT  = (4, 36, 8, 48)
RLEG2_LEFT   = (8, 36, 12, 48)
RLEG2_BACK   = (12, 36, 16, 48)

# 左腿(Left Leg) - 第一层
LLEG_TOP    = (20, 48, 24, 52)
LLEG_BOTTOM = (24, 48, 28, 52)
LLEG_RIGHT  = (16, 52, 20, 64)
LLEG_FRONT  = (20, 52, 24, 64)
LLEG_LEFT   = (24, 52, 28, 64)
LLEG_BACK   = (28, 52, 32, 64)

# 左腿 - 第二层
LLEG2_TOP    = (4, 48, 8, 52)
LLEG2_BOTTOM = (8, 48, 12, 52)
LLEG2_RIGHT  = (0, 52, 4, 64)
LLEG2_FRONT  = (4, 52, 8, 64)
LLEG2_LEFT   = (8, 52, 12, 64)
LLEG2_BACK   = (12, 52, 16, 64)


def fill_rect(pixels, rect, color):
    """填充矩形区域"""
    x1, y1, x2, y2 = rect
    for y in range(y1, y2):
        for x in range(x1, x2):
            if 0 <= x < 64 and 0 <= y < 64:
                pixels[x, y] = color


def fill_rect_gradient(pixels, rect, color_top, color_bottom):
    """竖直渐变填充"""
    x1, y1, x2, y2 = rect
    h = y2 - y1
    if h <= 0:
        return
    for y in range(y1, y2):
        t = (y - y1) / max(h - 1, 1)
        r = int(color_top[0] + (color_bottom[0] - color_top[0]) * t)
        g = int(color_top[1] + (color_bottom[1] - color_top[1]) * t)
        b = int(color_top[2] + (color_bottom[2] - color_top[2]) * t)
        a = int(color_top[3] + (color_bottom[3] - color_top[3]) * t)
        for x in range(x1, x2):
            if 0 <= x < 64 and 0 <= y < 64:
                pixels[x, y] = (r, g, b, a)


def darken(color, factor=0.65):
    """暗化颜色"""
    return (int(color[0]*factor), int(color[1]*factor), int(color[2]*factor), color[3])


def lighten(color, factor=1.35):
    """亮化颜色"""
    return (min(255, int(color[0]*factor)), min(255, int(color[1]*factor)), min(255, int(color[2]*factor)), color[3])


def blend(c1, c2, t=0.5):
    """混合两种颜色"""
    return (
        int(c1[0]*(1-t) + c2[0]*t),
        int(c1[1]*(1-t) + c2[1]*t),
        int(c1[2]*(1-t) + c2[2]*t),
        int(c1[3]*(1-t) + c2[3]*t)
    )


def set_pixel(pixels, x, y, color):
    """安全设置像素"""
    if 0 <= x < 64 and 0 <= y < 64:
        pixels[x, y] = color


def paint_face(pixels, rect, skin_color, eye_color=(30, 20, 10, 255), has_beard=False,
               beard_color=None, is_female=False, mouth_color=None):
    """在8x8脸部区域画面部特征"""
    x1, y1, x2, y2 = rect
    # 先填皮肤底色
    fill_rect(pixels, rect, skin_color)
    # 眉毛(y1+2行, x1+1和x1+5位置各2像素)
    brow = darken(eye_color, 0.7)
    set_pixel(pixels, x1+1, y1+2, brow)
    set_pixel(pixels, x1+2, y1+2, brow)
    set_pixel(pixels, x1+5, y1+2, brow)
    set_pixel(pixels, x1+6, y1+2, brow)
    # 眼睛(y1+3行): 2像素宽黑眼 + 1白高光
    set_pixel(pixels, x1+1, y1+3, eye_color)
    set_pixel(pixels, x1+2, y1+3, eye_color)
    set_pixel(pixels, x1+2, y1+2, lighten(skin_color, 1.1))  # 高光在眉上
    set_pixel(pixels, x1+5, y1+3, eye_color)
    set_pixel(pixels, x1+6, y1+3, eye_color)
    # 白色高光
    set_pixel(pixels, x1+1, y1+3, (255, 255, 255, 255))
    set_pixel(pixels, x1+5, y1+3, (255, 255, 255, 255))
    # 黑色瞳孔
    set_pixel(pixels, x1+2, y1+3, eye_color)
    set_pixel(pixels, x1+6, y1+3, eye_color)
    # 鼻子(y1+5, 中间)
    nose = darken(skin_color, 0.85)
    set_pixel(pixels, x1+3, y1+5, nose)
    set_pixel(pixels, x1+4, y1+5, nose)
    # 嘴巴(y1+6)
    if mouth_color:
        set_pixel(pixels, x1+3, y1+6, mouth_color)
        set_pixel(pixels, x1+4, y1+6, mouth_color)
    else:
        m = darken(skin_color, 0.75)
        set_pixel(pixels, x1+3, y1+6, m)
        set_pixel(pixels, x1+4, y1+6, m)
    # 胡须
    if has_beard and beard_color:
        for bx in range(x1+1, x1+7):
            set_pixel(pixels, bx, y1+7, beard_color)
        for bx in range(x1+2, x1+6):
            set_pixel(pixels, bx, y1+6, beard_color)


def paint_body_front_robe(pixels, rect, main, dark, accent=None, belt_color=None):
    """画长袍身体前面 8x12"""
    x1, y1, x2, y2 = rect
    fill_rect_gradient(pixels, rect, main, dark)
    # 中线（衣襟）
    mid = blend(main, (0, 0, 0, 255), 0.25)
    for y in range(y1, y2):
        set_pixel(pixels, x1+3, y, mid)
        set_pixel(pixels, x1+4, y, mid)
    # 腰带
    if belt_color:
        for x in range(x1, x2):
            set_pixel(pixels, x, y1+4, belt_color)
            set_pixel(pixels, x, y1+5, belt_color)
    # 点缀花纹
    if accent:
        set_pixel(pixels, x1+1, y1+2, accent)
        set_pixel(pixels, x1+6, y1+2, accent)
        set_pixel(pixels, x1+2, y1+8, accent)
        set_pixel(pixels, x1+5, y1+8, accent)


def paint_arm(pixels, front_rect, outer_rect, inner_rect, back_rect, top_rect, bot_rect,
              main, dark, skin_color, is_bare=False):
    """画手臂(4x12面) - 上部衣袖，下部露出皮肤"""
    fill_rect(pixels, front_rect, main)
    fill_rect(pixels, outer_rect, dark)
    fill_rect(pixels, inner_rect, darken(main, 0.8))
    fill_rect(pixels, back_rect, darken(main, 0.75))
    fill_rect(pixels, top_rect, main)
    fill_rect(pixels, bot_rect, skin_color)
    # 手部(下面3像素)
    fx1, fy1, fx2, fy2 = front_rect
    for y in range(fy2-3, fy2):
        for x in range(fx1, fx2):
            set_pixel(pixels, x, y, skin_color)
    ox1, oy1, ox2, oy2 = outer_rect
    for y in range(oy2-3, oy2):
        for x in range(ox1, ox2):
            set_pixel(pixels, x, y, skin_color)
    ix1, iy1, ix2, iy2 = inner_rect
    for y in range(iy2-3, iy2):
        for x in range(ix1, ix2):
            set_pixel(pixels, x, y, skin_color)
    bx1, by1, bx2, by2 = back_rect
    for y in range(by2-3, by2):
        for x in range(bx1, bx2):
            set_pixel(pixels, x, y, skin_color)


def paint_leg(pixels, front_rect, outer_rect, inner_rect, back_rect, top_rect, bot_rect,
              robe_main, robe_dark, shoe_color):
    """画腿部 - 上部长袍下摆，下部鞋"""
    fill_rect_gradient(pixels, front_rect, robe_main, robe_dark)
    fill_rect(pixels, outer_rect, darken(robe_main, 0.8))
    fill_rect(pixels, inner_rect, darken(robe_main, 0.85))
    fill_rect_gradient(pixels, back_rect, darken(robe_main, 0.75), darken(robe_dark, 0.75))
    fill_rect(pixels, top_rect, robe_main)
    fill_rect(pixels, bot_rect, shoe_color)
    # 鞋(下面3像素)
    fx1, fy1, fx2, fy2 = front_rect
    for y in range(fy2-3, fy2):
        for x in range(fx1, fx2):
            set_pixel(pixels, x, y, shoe_color)
    ox1, oy1, ox2, oy2 = outer_rect
    for y in range(oy2-3, oy2):
        for x in range(ox1, ox2):
            set_pixel(pixels, x, y, shoe_color)
    ix1, iy1, ix2, iy2 = inner_rect
    for y in range(iy2-3, iy2):
        for x in range(ix1, ix2):
            set_pixel(pixels, x, y, shoe_color)
    bx1, by1, bx2, by2 = back_rect
    for y in range(by2-3, by2):
        for x in range(bx1, bx2):
            set_pixel(pixels, x, y, shoe_color)


def paint_hair(pixels, head_top, head_back, head_left, head_right, hair_color, style='long'):
    """画头发"""
    fill_rect(pixels, head_top, hair_color)
    h_dark = darken(hair_color, 0.8)
    # 后脑全覆盖
    fill_rect(pixels, head_back, hair_color)
    bx1, by1, bx2, by2 = head_back
    for y in range(by1, by2):
        set_pixel(pixels, bx1, y, h_dark)
        set_pixel(pixels, bx2-1, y, h_dark)
    # 两侧上半部分
    rx1, ry1, rx2, ry2 = head_right
    for y in range(ry1, ry1 + 4):
        for x in range(rx1, rx2):
            set_pixel(pixels, x, y, hair_color)
    lx1, ly1, lx2, ly2 = head_left
    for y in range(ly1, ly1 + 4):
        for x in range(lx1, lx2):
            set_pixel(pixels, x, y, hair_color)
    if style == 'long':
        # 两侧全覆盖
        fill_rect(pixels, head_right, hair_color)
        fill_rect(pixels, head_left, hair_color)
        # 侧面留一列可见皮肤(靠前)
        for y in range(ry1+3, ry2):
            set_pixel(pixels, rx2-1, y, darken(hair_color, 0.85))
        for y in range(ly1+3, ly2):
            set_pixel(pixels, lx1, y, darken(hair_color, 0.85))


# ===== 创建单个尊者皮肤 =====

def create_yuan_shi():
    """元始仙尊 - 金色华贵长袍，白色长髯，金色光环头饰"""
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    p = img.load()

    skin = (235, 210, 180, 255)
    gold_main = (218, 175, 50, 255)
    gold_dark = (160, 120, 30, 255)
    gold_accent = (255, 230, 100, 255)
    belt = (180, 140, 40, 255)
    hair = (230, 230, 230, 255)  # 白发
    beard = (220, 220, 215, 255)
    shoe = (120, 90, 30, 255)
    eye = (50, 35, 15, 255)

    # 头部
    paint_face(p, HEAD_FRONT, skin, eye, has_beard=True, beard_color=beard)
    fill_rect(p, HEAD_TOP, hair)
    fill_rect(p, HEAD_BACK, hair)
    fill_rect(p, HEAD_LEFT, blend(skin, hair, 0.5))
    fill_rect(p, HEAD_RIGHT, blend(skin, hair, 0.5))
    fill_rect(p, HEAD_BOTTOM, skin)
    # 金冠(overlay层)
    fill_rect(p, HAT_TOP, gold_accent)
    fill_rect(p, HAT_FRONT, (0, 0, 0, 0))
    # 冠的前额部分
    for x in range(40, 48):
        set_pixel(p, x, 8, gold_accent)
        set_pixel(p, x, 9, gold_accent)
    paint_hair(p, HEAD_TOP, HEAD_BACK, HEAD_LEFT, HEAD_RIGHT, hair, 'long')

    # 身体
    paint_body_front_robe(p, BODY_FRONT, gold_main, gold_dark, gold_accent, belt)
    fill_rect(p, BODY_BACK, darken(gold_main, 0.7))
    fill_rect(p, BODY_LEFT, darken(gold_main, 0.8))
    fill_rect(p, BODY_RIGHT, darken(gold_main, 0.8))
    fill_rect(p, BODY_TOP, gold_main)
    fill_rect(p, BODY_BOTTOM, gold_dark)

    # 右臂
    paint_arm(p, RARM_FRONT, RARM_RIGHT, RARM_LEFT, RARM_BACK, RARM_TOP, RARM_BOTTOM,
              gold_main, gold_dark, skin)
    # 左臂
    paint_arm(p, LARM_FRONT, LARM_RIGHT, LARM_LEFT, LARM_BACK, LARM_TOP, LARM_BOTTOM,
              gold_main, gold_dark, skin)
    # 右腿
    paint_leg(p, RLEG_FRONT, RLEG_RIGHT, RLEG_LEFT, RLEG_BACK, RLEG_TOP, RLEG_BOTTOM,
              gold_main, gold_dark, shoe)
    # 左腿
    paint_leg(p, LLEG_FRONT, LLEG_RIGHT, LLEG_LEFT, LLEG_BACK, LLEG_TOP, LLEG_BOTTOM,
              gold_main, gold_dark, shoe)

    # 外层长袍装饰
    for y in range(36, 48):
        set_pixel(p, 23, y, gold_accent)
        set_pixel(p, 24, y, gold_accent)

    img.save(os.path.join(output_dir, "venerable_yuan_shi.png"))
    print("  [OK] venerable_yuan_shi.png")


def create_xing_xiu():
    """星宿仙尊 - 深蓝星光长裙(女性尊者), 银白长发, 星辰图案"""
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    p = img.load()

    skin = (240, 218, 200, 255)
    blue_main = (30, 45, 120, 255)
    blue_dark = (15, 25, 80, 255)
    star = (200, 210, 255, 255)
    silver_hair = (210, 215, 230, 255)
    belt = (80, 90, 180, 255)
    shoe = (25, 30, 70, 255)
    eye = (40, 50, 120, 255)
    lip = (180, 100, 110, 255)

    # 头部
    paint_face(p, HEAD_FRONT, skin, eye, is_female=True, mouth_color=lip)
    paint_hair(p, HEAD_TOP, HEAD_BACK, HEAD_LEFT, HEAD_RIGHT, silver_hair, 'long')
    fill_rect(p, HEAD_BOTTOM, skin)
    # 星辰发饰
    set_pixel(p, 10, 1, star)
    set_pixel(p, 13, 2, star)
    set_pixel(p, 11, 3, (255, 255, 200, 255))

    # 身体
    paint_body_front_robe(p, BODY_FRONT, blue_main, blue_dark, star, belt)
    fill_rect(p, BODY_BACK, darken(blue_main, 0.7))
    fill_rect(p, BODY_LEFT, darken(blue_main, 0.85))
    fill_rect(p, BODY_RIGHT, darken(blue_main, 0.85))
    fill_rect(p, BODY_TOP, blue_main)
    fill_rect(p, BODY_BOTTOM, blue_dark)
    # 星辰点缀
    set_pixel(p, 21, 22, star)
    set_pixel(p, 26, 25, star)
    set_pixel(p, 22, 28, star)
    set_pixel(p, 25, 30, (255, 255, 200, 255))

    # 手臂
    paint_arm(p, RARM_FRONT, RARM_RIGHT, RARM_LEFT, RARM_BACK, RARM_TOP, RARM_BOTTOM,
              blue_main, blue_dark, skin)
    paint_arm(p, LARM_FRONT, LARM_RIGHT, LARM_LEFT, LARM_BACK, LARM_TOP, LARM_BOTTOM,
              blue_main, blue_dark, skin)
    # 腿
    paint_leg(p, RLEG_FRONT, RLEG_RIGHT, RLEG_LEFT, RLEG_BACK, RLEG_TOP, RLEG_BOTTOM,
              blue_main, blue_dark, shoe)
    paint_leg(p, LLEG_FRONT, LLEG_RIGHT, LLEG_LEFT, LLEG_BACK, LLEG_TOP, LLEG_BOTTOM,
              blue_main, blue_dark, shoe)

    img.save(os.path.join(output_dir, "venerable_xing_xiu.png"))
    print("  [OK] venerable_xing_xiu.png")


def create_yuan_lian():
    """元莲仙尊 - 翠绿木系长袍, 莲花纹饰, 绿色头发"""
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    p = img.load()

    skin = (230, 215, 190, 255)
    green_main = (40, 140, 55, 255)
    green_dark = (25, 95, 35, 255)
    lotus = (255, 180, 200, 255)  # 莲花粉色
    green_hair = (60, 160, 80, 255)
    belt = (80, 180, 100, 255)
    shoe = (30, 70, 25, 255)
    eye = (30, 80, 30, 255)

    # 头部
    paint_face(p, HEAD_FRONT, skin, eye)
    paint_hair(p, HEAD_TOP, HEAD_BACK, HEAD_LEFT, HEAD_RIGHT, green_hair, 'long')
    fill_rect(p, HEAD_BOTTOM, skin)

    # 身体
    paint_body_front_robe(p, BODY_FRONT, green_main, green_dark, lotus, belt)
    fill_rect(p, BODY_BACK, darken(green_main, 0.7))
    fill_rect(p, BODY_LEFT, darken(green_main, 0.85))
    fill_rect(p, BODY_RIGHT, darken(green_main, 0.85))
    fill_rect(p, BODY_TOP, green_main)
    fill_rect(p, BODY_BOTTOM, green_dark)
    # 莲花纹饰
    set_pixel(p, 22, 24, lotus)
    set_pixel(p, 23, 23, lotus)
    set_pixel(p, 24, 23, lotus)
    set_pixel(p, 25, 24, lotus)
    set_pixel(p, 23, 25, (255, 200, 210, 255))
    set_pixel(p, 24, 25, (255, 200, 210, 255))

    # 手臂
    paint_arm(p, RARM_FRONT, RARM_RIGHT, RARM_LEFT, RARM_BACK, RARM_TOP, RARM_BOTTOM,
              green_main, green_dark, skin)
    paint_arm(p, LARM_FRONT, LARM_RIGHT, LARM_LEFT, LARM_BACK, LARM_TOP, LARM_BOTTOM,
              green_main, green_dark, skin)
    # 腿
    paint_leg(p, RLEG_FRONT, RLEG_RIGHT, RLEG_LEFT, RLEG_BACK, RLEG_TOP, RLEG_BOTTOM,
              green_main, green_dark, shoe)
    paint_leg(p, LLEG_FRONT, LLEG_RIGHT, LLEG_LEFT, LLEG_BACK, LLEG_TOP, LLEG_BOTTOM,
              green_main, green_dark, shoe)

    img.save(os.path.join(output_dir, "venerable_yuan_lian.png"))
    print("  [OK] venerable_yuan_lian.png")


def create_wu_ji():
    """无极魔尊 - 暗紫法则纹路长袍, 紫黑色调"""
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    p = img.load()

    skin = (200, 185, 190, 255)  # 苍白偏紫
    purple_main = (80, 30, 100, 255)
    purple_dark = (45, 15, 60, 255)
    law_line = (160, 80, 200, 255)  # 法则纹路亮紫
    hair = (50, 20, 60, 255)  # 深紫黑发
    belt = (100, 40, 120, 255)
    shoe = (40, 15, 50, 255)
    eye = (130, 50, 170, 255)  # 紫眼

    # 头部
    paint_face(p, HEAD_FRONT, skin, eye)
    paint_hair(p, HEAD_TOP, HEAD_BACK, HEAD_LEFT, HEAD_RIGHT, hair, 'long')
    fill_rect(p, HEAD_BOTTOM, skin)

    # 身体
    paint_body_front_robe(p, BODY_FRONT, purple_main, purple_dark, law_line, belt)
    fill_rect(p, BODY_BACK, darken(purple_main, 0.7))
    fill_rect(p, BODY_LEFT, darken(purple_main, 0.85))
    fill_rect(p, BODY_RIGHT, darken(purple_main, 0.85))
    fill_rect(p, BODY_TOP, purple_main)
    fill_rect(p, BODY_BOTTOM, purple_dark)
    # 法则纹路
    set_pixel(p, 21, 21, law_line)
    set_pixel(p, 22, 23, law_line)
    set_pixel(p, 25, 22, law_line)
    set_pixel(p, 26, 26, law_line)
    set_pixel(p, 21, 29, law_line)
    set_pixel(p, 27, 28, law_line)

    # 手臂
    paint_arm(p, RARM_FRONT, RARM_RIGHT, RARM_LEFT, RARM_BACK, RARM_TOP, RARM_BOTTOM,
              purple_main, purple_dark, skin)
    paint_arm(p, LARM_FRONT, LARM_RIGHT, LARM_LEFT, LARM_BACK, LARM_TOP, LARM_BOTTOM,
              purple_main, purple_dark, skin)
    # 腿
    paint_leg(p, RLEG_FRONT, RLEG_RIGHT, RLEG_LEFT, RLEG_BACK, RLEG_TOP, RLEG_BOTTOM,
              purple_main, purple_dark, shoe)
    paint_leg(p, LLEG_FRONT, LLEG_RIGHT, LLEG_LEFT, LLEG_BACK, LLEG_TOP, LLEG_BOTTOM,
              purple_main, purple_dark, shoe)

    img.save(os.path.join(output_dir, "venerable_wu_ji.png"))
    print("  [OK] venerable_wu_ji.png")


def create_kuang_man():
    """狂蛮魔尊 - 兽皮披肩+红色战甲, 最强壮外观"""
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    p = img.load()

    skin = (180, 140, 110, 255)  # 深色粗犷皮肤
    red_main = (160, 30, 20, 255)
    red_dark = (100, 15, 10, 255)
    fur = (140, 110, 70, 255)  # 兽皮
    fur_dark = (100, 75, 45, 255)
    hair = (80, 30, 20, 255)  # 深红褐发
    belt = (90, 70, 40, 255)  # 皮革腰带
    shoe = (80, 55, 30, 255)
    eye = (180, 40, 20, 255)  # 红眼

    # 头部
    paint_face(p, HEAD_FRONT, skin, eye)
    paint_hair(p, HEAD_TOP, HEAD_BACK, HEAD_LEFT, HEAD_RIGHT, hair, 'long')
    fill_rect(p, HEAD_BOTTOM, skin)
    # 野性疤痕
    set_pixel(p, 14, 10, darken(skin, 0.6))
    set_pixel(p, 14, 11, darken(skin, 0.6))

    # 身体 - 战甲+兽皮
    fill_rect_gradient(p, BODY_FRONT, red_main, red_dark)
    fill_rect(p, BODY_BACK, darken(red_main, 0.7))
    fill_rect(p, BODY_LEFT, darken(red_main, 0.85))
    fill_rect(p, BODY_RIGHT, darken(red_main, 0.85))
    fill_rect(p, BODY_TOP, red_main)
    fill_rect(p, BODY_BOTTOM, red_dark)
    # 兽皮披肩(身体上部)
    for x in range(20, 28):
        set_pixel(p, x, 20, fur)
        set_pixel(p, x, 21, fur_dark)
    # 腰带
    for x in range(20, 28):
        set_pixel(p, x, 24, belt)
        set_pixel(p, x, 25, darken(belt, 0.8))
    # 铆钉
    set_pixel(p, 21, 24, (180, 170, 140, 255))
    set_pixel(p, 26, 24, (180, 170, 140, 255))

    # 手臂 - 裸臂+护腕
    paint_arm(p, RARM_FRONT, RARM_RIGHT, RARM_LEFT, RARM_BACK, RARM_TOP, RARM_BOTTOM,
              skin, darken(skin, 0.8), skin, is_bare=True)
    paint_arm(p, LARM_FRONT, LARM_RIGHT, LARM_LEFT, LARM_BACK, LARM_TOP, LARM_BOTTOM,
              skin, darken(skin, 0.8), skin, is_bare=True)
    # 肩甲(overlay层)
    fill_rect(p, RARM2_TOP, fur)
    fill_rect(p, LARM2_TOP, fur)
    for x in range(44, 48):
        set_pixel(p, x, 36, fur)
        set_pixel(p, x, 37, fur_dark)
    for x in range(52, 56):
        set_pixel(p, x, 48, fur)
        set_pixel(p, x, 49, fur_dark)

    # 腿
    paint_leg(p, RLEG_FRONT, RLEG_RIGHT, RLEG_LEFT, RLEG_BACK, RLEG_TOP, RLEG_BOTTOM,
              red_dark, darken(red_dark, 0.8), shoe)
    paint_leg(p, LLEG_FRONT, LLEG_RIGHT, LLEG_LEFT, LLEG_BACK, LLEG_TOP, LLEG_BOTTOM,
              red_dark, darken(red_dark, 0.8), shoe)

    img.save(os.path.join(output_dir, "venerable_kuang_man.png"))
    print("  [OK] venerable_kuang_man.png")


def create_dao_tian():
    """盗天魔尊 - 全身深灰/黑色盗装, 蒙面巾"""
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    p = img.load()

    skin = (210, 195, 175, 255)
    gray_main = (55, 55, 60, 255)
    gray_dark = (30, 30, 35, 255)
    cloth = (70, 70, 75, 255)
    hair = (35, 35, 40, 255)
    belt = (60, 55, 50, 255)
    shoe = (25, 25, 28, 255)
    eye = (180, 180, 180, 255)  # 灰白锐利眼
    mask = (45, 45, 50, 255)  # 蒙面巾

    # 头部 - 蒙面
    fill_rect(p, HEAD_FRONT, skin)
    # 只露出眼睛区域, 其余覆盖蒙面巾
    for y in range(8, 16):
        for x in range(8, 16):
            if y == 11:  # 眼睛行
                continue
            set_pixel(p, x, y, mask)
    # 眼睛
    set_pixel(p, 9, 11, eye)
    set_pixel(p, 10, 11, (40, 40, 40, 255))
    set_pixel(p, 13, 11, eye)
    set_pixel(p, 14, 11, (40, 40, 40, 255))
    # 头巾
    fill_rect(p, HEAD_TOP, gray_main)
    fill_rect(p, HEAD_BACK, gray_main)
    fill_rect(p, HEAD_LEFT, gray_main)
    fill_rect(p, HEAD_RIGHT, gray_main)
    fill_rect(p, HEAD_BOTTOM, mask)

    # 身体
    paint_body_front_robe(p, BODY_FRONT, gray_main, gray_dark, cloth, belt)
    fill_rect(p, BODY_BACK, darken(gray_main, 0.7))
    fill_rect(p, BODY_LEFT, darken(gray_main, 0.85))
    fill_rect(p, BODY_RIGHT, darken(gray_main, 0.85))
    fill_rect(p, BODY_TOP, gray_main)
    fill_rect(p, BODY_BOTTOM, gray_dark)

    # 手臂
    paint_arm(p, RARM_FRONT, RARM_RIGHT, RARM_LEFT, RARM_BACK, RARM_TOP, RARM_BOTTOM,
              gray_main, gray_dark, darken(skin, 0.9))
    paint_arm(p, LARM_FRONT, LARM_RIGHT, LARM_LEFT, LARM_BACK, LARM_TOP, LARM_BOTTOM,
              gray_main, gray_dark, darken(skin, 0.9))
    # 腿
    paint_leg(p, RLEG_FRONT, RLEG_RIGHT, RLEG_LEFT, RLEG_BACK, RLEG_TOP, RLEG_BOTTOM,
              gray_main, gray_dark, shoe)
    paint_leg(p, LLEG_FRONT, LLEG_RIGHT, LLEG_LEFT, LLEG_BACK, LLEG_TOP, LLEG_BOTTOM,
              gray_main, gray_dark, shoe)

    img.save(os.path.join(output_dir, "venerable_dao_tian.png"))
    print("  [OK] venerable_dao_tian.png")


def create_ju_yang():
    """巨阳仙尊 - 橙金色太阳纹长袍, 金色头冠"""
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    p = img.load()

    skin = (240, 215, 180, 255)
    orange_main = (220, 150, 30, 255)
    orange_dark = (180, 100, 15, 255)
    sun_glow = (255, 220, 80, 255)
    hair = (200, 140, 40, 255)  # 金褐发
    belt = (200, 130, 20, 255)
    shoe = (140, 85, 15, 255)
    eye = (180, 120, 20, 255)
    crown = (255, 210, 50, 255)

    # 头部
    paint_face(p, HEAD_FRONT, skin, eye)
    paint_hair(p, HEAD_TOP, HEAD_BACK, HEAD_LEFT, HEAD_RIGHT, hair, 'long')
    fill_rect(p, HEAD_BOTTOM, skin)
    # 金冠(overlay)
    for x in range(40, 48):
        set_pixel(p, x, 8, crown)
        set_pixel(p, x, 9, darken(crown, 0.85))
    set_pixel(p, 43, 8, (255, 255, 180, 255))  # 冠顶宝石
    set_pixel(p, 44, 8, (255, 255, 180, 255))

    # 身体
    paint_body_front_robe(p, BODY_FRONT, orange_main, orange_dark, sun_glow, belt)
    fill_rect(p, BODY_BACK, darken(orange_main, 0.7))
    fill_rect(p, BODY_LEFT, darken(orange_main, 0.85))
    fill_rect(p, BODY_RIGHT, darken(orange_main, 0.85))
    fill_rect(p, BODY_TOP, orange_main)
    fill_rect(p, BODY_BOTTOM, orange_dark)
    # 太阳纹
    set_pixel(p, 23, 23, sun_glow)
    set_pixel(p, 24, 23, sun_glow)
    set_pixel(p, 22, 24, sun_glow)
    set_pixel(p, 25, 24, sun_glow)
    set_pixel(p, 23, 25, sun_glow)
    set_pixel(p, 24, 25, sun_glow)
    set_pixel(p, 23, 24, (255, 240, 150, 255))  # 中心
    set_pixel(p, 24, 24, (255, 240, 150, 255))

    # 手臂
    paint_arm(p, RARM_FRONT, RARM_RIGHT, RARM_LEFT, RARM_BACK, RARM_TOP, RARM_BOTTOM,
              orange_main, orange_dark, skin)
    paint_arm(p, LARM_FRONT, LARM_RIGHT, LARM_LEFT, LARM_BACK, LARM_TOP, LARM_BOTTOM,
              orange_main, orange_dark, skin)
    # 腿
    paint_leg(p, RLEG_FRONT, RLEG_RIGHT, RLEG_LEFT, RLEG_BACK, RLEG_TOP, RLEG_BOTTOM,
              orange_main, orange_dark, shoe)
    paint_leg(p, LLEG_FRONT, LLEG_RIGHT, LLEG_LEFT, LLEG_BACK, LLEG_TOP, LLEG_BOTTOM,
              orange_main, orange_dark, shoe)

    img.save(os.path.join(output_dir, "venerable_ju_yang.png"))
    print("  [OK] venerable_ju_yang.png")


def create_you_hun():
    """幽魂魔尊 - 深紫黑色魂道长袍, 苍白面容, 幽暗气息"""
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    p = img.load()

    skin = (185, 175, 185, 255)  # 苍白偏紫
    soul_main = (40, 20, 55, 255)
    soul_dark = (20, 8, 30, 255)
    ghost = (100, 60, 140, 255)  # 幽光
    hair = (25, 12, 35, 255)  # 黑紫发
    belt = (60, 30, 80, 255)
    shoe = (20, 10, 28, 255)
    eye = (160, 80, 200, 255)  # 鬼紫眼

    # 头部
    paint_face(p, HEAD_FRONT, skin, eye)
    paint_hair(p, HEAD_TOP, HEAD_BACK, HEAD_LEFT, HEAD_RIGHT, hair, 'long')
    fill_rect(p, HEAD_BOTTOM, skin)
    # 眼下黑眼圈
    set_pixel(p, 9, 12, darken(skin, 0.7))
    set_pixel(p, 10, 12, darken(skin, 0.7))
    set_pixel(p, 13, 12, darken(skin, 0.7))
    set_pixel(p, 14, 12, darken(skin, 0.7))

    # 身体
    paint_body_front_robe(p, BODY_FRONT, soul_main, soul_dark, ghost, belt)
    fill_rect(p, BODY_BACK, darken(soul_main, 0.7))
    fill_rect(p, BODY_LEFT, darken(soul_main, 0.85))
    fill_rect(p, BODY_RIGHT, darken(soul_main, 0.85))
    fill_rect(p, BODY_TOP, soul_main)
    fill_rect(p, BODY_BOTTOM, soul_dark)
    # 幽魂纹路
    set_pixel(p, 21, 22, ghost)
    set_pixel(p, 22, 25, ghost)
    set_pixel(p, 26, 23, ghost)
    set_pixel(p, 25, 27, ghost)
    set_pixel(p, 23, 30, ghost)

    # 手臂
    paint_arm(p, RARM_FRONT, RARM_RIGHT, RARM_LEFT, RARM_BACK, RARM_TOP, RARM_BOTTOM,
              soul_main, soul_dark, skin)
    paint_arm(p, LARM_FRONT, LARM_RIGHT, LARM_LEFT, LARM_BACK, LARM_TOP, LARM_BOTTOM,
              soul_main, soul_dark, skin)
    # 腿
    paint_leg(p, RLEG_FRONT, RLEG_RIGHT, RLEG_LEFT, RLEG_BACK, RLEG_TOP, RLEG_BOTTOM,
              soul_main, soul_dark, shoe)
    paint_leg(p, LLEG_FRONT, LLEG_RIGHT, LLEG_LEFT, LLEG_BACK, LLEG_TOP, LLEG_BOTTOM,
              soul_main, soul_dark, shoe)

    img.save(os.path.join(output_dir, "venerable_you_hun.png"))
    print("  [OK] venerable_you_hun.png")


def create_le_tu():
    """乐土仙尊 - 浅蓝白色温和长袍, 和善面容"""
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    p = img.load()

    skin = (240, 220, 195, 255)
    blue_main = (160, 200, 230, 255)
    blue_dark = (110, 150, 190, 255)
    white_accent = (230, 240, 250, 255)
    hair = (100, 85, 70, 255)  # 褐发
    belt = (130, 170, 200, 255)
    shoe = (80, 100, 130, 255)
    eye = (60, 90, 140, 255)

    # 头部
    paint_face(p, HEAD_FRONT, skin, eye)
    paint_hair(p, HEAD_TOP, HEAD_BACK, HEAD_LEFT, HEAD_RIGHT, hair, 'long')
    fill_rect(p, HEAD_BOTTOM, skin)
    # 温和的微笑线
    set_pixel(p, 11, 14, darken(skin, 0.8))
    set_pixel(p, 12, 14, darken(skin, 0.85))

    # 身体
    paint_body_front_robe(p, BODY_FRONT, blue_main, blue_dark, white_accent, belt)
    fill_rect(p, BODY_BACK, darken(blue_main, 0.75))
    fill_rect(p, BODY_LEFT, darken(blue_main, 0.88))
    fill_rect(p, BODY_RIGHT, darken(blue_main, 0.88))
    fill_rect(p, BODY_TOP, blue_main)
    fill_rect(p, BODY_BOTTOM, blue_dark)
    # 云纹装饰
    set_pixel(p, 21, 23, white_accent)
    set_pixel(p, 22, 22, white_accent)
    set_pixel(p, 23, 22, white_accent)
    set_pixel(p, 25, 27, white_accent)
    set_pixel(p, 26, 26, white_accent)

    # 手臂
    paint_arm(p, RARM_FRONT, RARM_RIGHT, RARM_LEFT, RARM_BACK, RARM_TOP, RARM_BOTTOM,
              blue_main, blue_dark, skin)
    paint_arm(p, LARM_FRONT, LARM_RIGHT, LARM_LEFT, LARM_BACK, LARM_TOP, LARM_BOTTOM,
              blue_main, blue_dark, skin)
    # 腿
    paint_leg(p, RLEG_FRONT, RLEG_RIGHT, RLEG_LEFT, RLEG_BACK, RLEG_TOP, RLEG_BOTTOM,
              blue_main, blue_dark, shoe)
    paint_leg(p, LLEG_FRONT, LLEG_RIGHT, LLEG_LEFT, LLEG_BACK, LLEG_TOP, LLEG_BOTTOM,
              blue_main, blue_dark, shoe)

    img.save(os.path.join(output_dir, "venerable_le_tu.png"))
    print("  [OK] venerable_le_tu.png")


def create_hong_lian():
    """红莲魔尊 - 鲜红+黑色时间纹路长袍, 红莲花纹饰, 神秘面纱"""
    img = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    p = img.load()

    skin = (220, 200, 190, 255)
    red_main = (180, 20, 30, 255)
    red_dark = (110, 10, 15, 255)
    black_line = (30, 10, 15, 255)  # 时间纹路黑
    lotus_red = (255, 50, 60, 255)
    hair = (20, 5, 8, 255)  # 黑发
    belt = (140, 15, 25, 255)
    shoe = (60, 8, 12, 255)
    eye = (220, 30, 40, 255)  # 红眼
    veil = (150, 15, 25, 200)  # 半透明面纱

    # 头部 - 带面纱
    paint_face(p, HEAD_FRONT, skin, eye)
    # 面纱覆盖下半脸
    for y in range(13, 16):
        for x in range(8, 16):
            set_pixel(p, x, y, veil)
    paint_hair(p, HEAD_TOP, HEAD_BACK, HEAD_LEFT, HEAD_RIGHT, hair, 'long')
    fill_rect(p, HEAD_BOTTOM, skin)

    # 身体
    paint_body_front_robe(p, BODY_FRONT, red_main, red_dark, lotus_red, belt)
    fill_rect(p, BODY_BACK, darken(red_main, 0.7))
    fill_rect(p, BODY_LEFT, darken(red_main, 0.85))
    fill_rect(p, BODY_RIGHT, darken(red_main, 0.85))
    fill_rect(p, BODY_TOP, red_main)
    fill_rect(p, BODY_BOTTOM, red_dark)
    # 时间纹路(对角线)
    set_pixel(p, 21, 21, black_line)
    set_pixel(p, 22, 22, black_line)
    set_pixel(p, 23, 23, black_line)
    set_pixel(p, 25, 21, black_line)
    set_pixel(p, 26, 22, black_line)
    set_pixel(p, 27, 23, black_line)
    set_pixel(p, 21, 28, black_line)
    set_pixel(p, 22, 29, black_line)
    # 红莲花
    set_pixel(p, 23, 26, lotus_red)
    set_pixel(p, 24, 26, lotus_red)
    set_pixel(p, 22, 27, lotus_red)
    set_pixel(p, 25, 27, lotus_red)
    set_pixel(p, 23, 28, (255, 100, 100, 255))
    set_pixel(p, 24, 28, (255, 100, 100, 255))

    # 手臂
    paint_arm(p, RARM_FRONT, RARM_RIGHT, RARM_LEFT, RARM_BACK, RARM_TOP, RARM_BOTTOM,
              red_main, red_dark, skin)
    paint_arm(p, LARM_FRONT, LARM_RIGHT, LARM_LEFT, LARM_BACK, LARM_TOP, LARM_BOTTOM,
              red_main, red_dark, skin)
    # 腿
    paint_leg(p, RLEG_FRONT, RLEG_RIGHT, RLEG_LEFT, RLEG_BACK, RLEG_TOP, RLEG_BOTTOM,
              red_main, red_dark, shoe)
    paint_leg(p, LLEG_FRONT, LLEG_RIGHT, LLEG_LEFT, LLEG_BACK, LLEG_TOP, LLEG_BOTTOM,
              red_main, red_dark, shoe)

    img.save(os.path.join(output_dir, "venerable_hong_lian.png"))
    print("  [OK] venerable_hong_lian.png")


# ===== 主函数 =====
if __name__ == "__main__":
    print("=== 十大九转尊者贴图生成器 ===")
    print(f"输出目录: {output_dir}\n")

    create_yuan_shi()
    create_xing_xiu()
    create_yuan_lian()
    create_wu_ji()
    create_kuang_man()
    create_dao_tian()
    create_ju_yang()
    create_you_hun()
    create_le_tu()
    create_hong_lian()

    print(f"\n=== 全部10个尊者贴图生成完毕 ===")
