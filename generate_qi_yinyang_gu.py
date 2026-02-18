from PIL import Image, ImageDraw
import math, random

random.seed(42)

OUTPUT_DIR = "/mnt/e/code/mod/gu/src/main/resources/assets/reverend_insanity/textures/item/"
SIZE = 64
PX = 4

def hex_to_rgb(h):
    h = h.lstrip('#')
    return tuple(int(h[i:i+2], 16) for i in (0, 2, 4))

def fill_px(draw, gx, gy, color, alpha=255):
    x0, y0 = gx * PX, gy * PX
    if isinstance(color, str):
        color = hex_to_rgb(color)
    draw.rectangle([x0, y0, x0 + PX - 1, y0 + PX - 1], fill=(*color, alpha))

def darken(color, factor=0.7):
    if isinstance(color, str):
        color = hex_to_rgb(color)
    return tuple(int(c * factor) for c in color)

def lighten(color, factor=1.3):
    if isinstance(color, str):
        color = hex_to_rgb(color)
    return tuple(min(255, int(c * factor)) for c in color)


# ============================================================
# 1. TRUE QI GU - Soft green healing insect with flowing energy
# ============================================================
def generate_true_qi_gu():
    img = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    c1 = hex_to_rgb('#66DDAA')
    c2 = hex_to_rgb('#88FFBB')
    c3 = hex_to_rgb('#44BB88')
    c4 = hex_to_rgb('#AAFFCC')
    c5 = hex_to_rgb('#33AA77')

    # Soft glow aura
    for gy in range(16):
        for gx in range(16):
            dx = gx - 7.5
            dy = gy - 7.5
            dist = math.sqrt(dx*dx + dy*dy)
            if 4.5 < dist < 7.5:
                alpha = int(40 * (1 - (dist - 4.5) / 3.0))
                fill_px(draw, gx, gy, c4, alpha)

    # Energy flow lines
    energy_positions = [(3,5), (3,6), (2,7), (2,8), (3,9), (4,10),
                        (12,5), (12,6), (13,7), (13,8), (12,9), (11,10)]
    for gx, gy in energy_positions:
        fill_px(draw, gx, gy, c4, 140)

    # Body
    body_pixels = [
        (7,3), (8,3),
        (6,4), (7,4), (8,4), (9,4),
        (5,5), (6,5), (7,5), (8,5), (9,5), (10,5),
        (5,6), (6,6), (7,6), (8,6), (9,6), (10,6),
        (5,7), (6,7), (7,7), (8,7), (9,7), (10,7),
        (5,8), (6,8), (7,8), (8,8), (9,8), (10,8),
        (6,9), (7,9), (8,9), (9,9),
        (6,10), (7,10), (8,10), (9,10),
        (7,11), (8,11),
    ]
    for gx, gy in body_pixels:
        fill_px(draw, gx, gy, c1)

    highlight_px = [(7,3), (8,3), (6,4), (7,4), (5,5), (6,5), (5,6), (6,6)]
    for gx, gy in highlight_px:
        fill_px(draw, gx, gy, c2)
    shadow_px = [(9,8), (10,8), (9,9), (8,10), (9,10), (7,11), (8,11), (10,7)]
    for gx, gy in shadow_px:
        fill_px(draw, gx, gy, c3)

    outline_pixels = [
        (6,2), (9,2),
        (5,3), (10,3),
        (4,4), (10,4),
        (4,5), (11,5),
        (4,6), (11,6),
        (4,7), (11,7),
        (4,8), (11,8),
        (5,9), (10,9),
        (5,10), (10,10),
        (6,11), (9,11),
        (7,12), (8,12),
    ]
    for gx, gy in outline_pixels:
        fill_px(draw, gx, gy, c5)

    wing_l = [(3,4), (2,5), (3,5), (2,6), (3,6), (3,7)]
    wing_r = [(12,4), (12,5), (13,5), (12,6), (13,6), (12,7)]
    for gx, gy in wing_l + wing_r:
        fill_px(draw, gx, gy, c4, 120)

    fill_px(draw, 6, 4, (255, 255, 255), 220)
    fill_px(draw, 9, 4, (255, 255, 255), 220)

    particles = [(4,2), (11,3), (3,10), (12,10), (8,1), (5,12)]
    for gx, gy in particles:
        fill_px(draw, gx, gy, c2, 100)

    fill_px(draw, 6, 2, c5)
    fill_px(draw, 5, 1, c3)
    fill_px(draw, 9, 2, c5)
    fill_px(draw, 10, 1, c3)

    legs = [(4,9), (4,8), (11,9), (11,8), (5,11), (10,11)]
    for gx, gy in legs:
        fill_px(draw, gx, gy, c5)

    img.save(OUTPUT_DIR + "true_qi_gu.png")
    print("Generated true_qi_gu.png")


# ============================================================
# 2. QI SHIELD GU - Jade-green armored beetle with qi barrier
# ============================================================
def generate_qi_shield_gu():
    img = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    c1 = hex_to_rgb('#55CC99')
    c2 = hex_to_rgb('#77DDBB')
    c3 = hex_to_rgb('#44AA77')
    c4 = hex_to_rgb('#88EECC')
    c5 = hex_to_rgb('#339966')

    # Qi barrier
    for gy in range(16):
        for gx in range(16):
            dx = gx - 7.5
            dy = gy - 7.0
            dist = math.sqrt(dx*dx + (dy*1.2)**2)
            if 5.5 < dist < 7.0:
                alpha = int(55 * (1 - abs(dist - 6.25) / 0.75))
                fill_px(draw, gx, gy, c4, max(0, alpha))
            elif 6.8 < dist < 7.2:
                fill_px(draw, gx, gy, c2, 35)

    body = [
        (6,3), (7,3), (8,3), (9,3),
        (5,4), (6,4), (7,4), (8,4), (9,4), (10,4),
        (5,5), (6,5), (7,5), (8,5), (9,5), (10,5),
        (4,6), (5,6), (6,6), (7,6), (8,6), (9,6), (10,6), (11,6),
        (4,7), (5,7), (6,7), (7,7), (8,7), (9,7), (10,7), (11,7),
        (4,8), (5,8), (6,8), (7,8), (8,8), (9,8), (10,8), (11,8),
        (5,9), (6,9), (7,9), (8,9), (9,9), (10,9),
        (5,10), (6,10), (7,10), (8,10), (9,10), (10,10),
        (6,11), (7,11), (8,11), (9,11),
    ]
    for gx, gy in body:
        fill_px(draw, gx, gy, c1)

    plate1 = [(6,4), (7,4), (8,4), (9,4), (5,5), (6,5), (7,5), (8,5), (9,5), (10,5)]
    for gx, gy in plate1:
        fill_px(draw, gx, gy, c3)
    plate_line = [(5,6), (6,6), (7,6), (8,6), (9,6), (10,6)]
    for gx, gy in plate_line:
        fill_px(draw, gx, gy, c5)
    plate2_hi = [(5,7), (6,7), (5,8), (6,8)]
    for gx, gy in plate2_hi:
        fill_px(draw, gx, gy, c2)
    plate2_sh = [(10,9), (9,10), (10,10), (8,11), (9,11)]
    for gx, gy in plate2_sh:
        fill_px(draw, gx, gy, c3)

    outline = [
        (5,2), (10,2),
        (4,3), (10,3),
        (4,4), (11,4),
        (4,5), (11,5),
        (3,6), (12,6),
        (3,7), (12,7),
        (3,8), (12,8),
        (4,9), (11,9),
        (4,10), (11,10),
        (5,11), (10,11),
        (6,12), (9,12),
    ]
    for gx, gy in outline:
        fill_px(draw, gx, gy, c5)

    emblem = [(7,7), (8,7), (7,8), (8,8)]
    for gx, gy in emblem:
        fill_px(draw, gx, gy, c4, 200)
    emblem_edge = [(7,6), (8,6), (6,7), (9,7), (6,8), (9,8), (7,9), (8,9)]
    for gx, gy in emblem_edge:
        fill_px(draw, gx, gy, c2, 130)

    fill_px(draw, 6, 3, (200, 255, 220))
    fill_px(draw, 9, 3, (200, 255, 220))

    fill_px(draw, 5, 2, c5)
    fill_px(draw, 10, 2, c5)

    legs = [(3,9), (3,8), (12,9), (12,8), (4,11), (11,11)]
    for gx, gy in legs:
        fill_px(draw, gx, gy, c5)

    img.save(OUTPUT_DIR + "qi_shield_gu.png")
    print("Generated qi_shield_gu.png")


# ============================================================
# 3. PROFOUND QI GU - Deep emerald, intense inner glow, rank 2
# ============================================================
def generate_profound_qi_gu():
    img = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    c1 = hex_to_rgb('#228855')
    c2 = hex_to_rgb('#33AA66')
    c3 = hex_to_rgb('#44CC88')
    c4 = hex_to_rgb('#116644')
    c5 = hex_to_rgb('#55DDAA')

    # Swirling qi energy aura
    for gy in range(16):
        for gx in range(16):
            dx = gx - 7.5
            dy = gy - 7.5
            dist = math.sqrt(dx*dx + dy*dy)
            angle = math.atan2(dy, dx)
            swirl = math.sin(angle * 3 + dist * 0.8) * 0.5 + 0.5
            if 5.0 < dist < 7.5:
                alpha = int(60 * swirl * (1 - (dist - 5.0) / 2.5))
                fill_px(draw, gx, gy, c3, max(0, alpha))
            if 4.0 < dist < 5.5:
                alpha = int(35 * swirl)
                fill_px(draw, gx, gy, c5, max(0, alpha))

    body = [
        (7,2), (8,2),
        (6,3), (7,3), (8,3), (9,3),
        (5,4), (6,4), (7,4), (8,4), (9,4), (10,4),
        (5,5), (6,5), (7,5), (8,5), (9,5), (10,5),
        (4,6), (5,6), (6,6), (7,6), (8,6), (9,6), (10,6), (11,6),
        (4,7), (5,7), (6,7), (7,7), (8,7), (9,7), (10,7), (11,7),
        (5,8), (6,8), (7,8), (8,8), (9,8), (10,8),
        (5,9), (6,9), (7,9), (8,9), (9,9), (10,9),
        (6,10), (7,10), (8,10), (9,10),
        (7,11), (8,11),
    ]
    for gx, gy in body:
        fill_px(draw, gx, gy, c1)

    deep_sh = [(7,11), (8,11), (9,10), (10,9), (10,8), (11,7), (11,6)]
    for gx, gy in deep_sh:
        fill_px(draw, gx, gy, c4)
    hi = [(7,2), (8,2), (6,3), (7,3), (5,4), (6,4), (5,5)]
    for gx, gy in hi:
        fill_px(draw, gx, gy, c2)

    core = [(7,6), (8,6), (7,7), (8,7)]
    for gx, gy in core:
        fill_px(draw, gx, gy, c5, 220)
    core_ring = [(6,5), (7,5), (8,5), (9,5), (6,6), (9,6), (6,7), (9,7), (6,8), (7,8), (8,8), (9,8)]
    for gx, gy in core_ring:
        fill_px(draw, gx, gy, c3, 160)

    outline = [
        (6,1), (9,1),
        (5,2), (10,2),
        (4,3), (10,3),
        (4,4), (11,4),
        (4,5), (11,5),
        (3,6), (12,6),
        (3,7), (12,7),
        (4,8), (11,8),
        (4,9), (11,9),
        (5,10), (10,10),
        (6,11), (9,11),
        (7,12), (8,12),
    ]
    for gx, gy in outline:
        fill_px(draw, gx, gy, c4)

    wing_l = [(3,4), (2,5), (3,5), (2,6), (3,6)]
    wing_r = [(12,4), (12,5), (13,5), (12,6), (13,6)]
    for gx, gy in wing_l + wing_r:
        fill_px(draw, gx, gy, c3, 100)

    fill_px(draw, 6, 3, c5, 255)
    fill_px(draw, 9, 3, c5, 255)

    fill_px(draw, 6, 1, c4)
    fill_px(draw, 5, 0, c2)
    fill_px(draw, 9, 1, c4)
    fill_px(draw, 10, 0, c2)

    legs = [(3,8), (3,7), (12,8), (12,7), (4,10), (11,10)]
    for gx, gy in legs:
        fill_px(draw, gx, gy, c4)

    particles = [(2,3), (13,3), (1,7), (14,7), (3,11), (12,11)]
    for gx, gy in particles:
        fill_px(draw, gx, gy, c5, 80)

    img.save(OUTPUT_DIR + "profound_qi_gu.png")
    print("Generated profound_qi_gu.png")


# ============================================================
# 4. YIN YANG GU - Half black, half white, yin-yang pattern
# ============================================================
def generate_yin_yang_gu():
    img = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    black = hex_to_rgb('#111111')
    white = hex_to_rgb('#EEEEEE')
    dk_gray = hex_to_rgb('#444444')
    lt_gray = hex_to_rgb('#BBBBBB')
    mid_gray = hex_to_rgb('#777777')

    body_coords = [
        (7,2), (8,2),
        (6,3), (7,3), (8,3), (9,3),
        (5,4), (6,4), (7,4), (8,4), (9,4), (10,4),
        (5,5), (6,5), (7,5), (8,5), (9,5), (10,5),
        (5,6), (6,6), (7,6), (8,6), (9,6), (10,6),
        (5,7), (6,7), (7,7), (8,7), (9,7), (10,7),
        (5,8), (6,8), (7,8), (8,8), (9,8), (10,8),
        (5,9), (6,9), (7,9), (8,9), (9,9), (10,9),
        (6,10), (7,10), (8,10), (9,10),
        (7,11), (8,11),
    ]

    for gx, gy in body_coords:
        if gx <= 7:
            fill_px(draw, gx, gy, black)
        else:
            fill_px(draw, gx, gy, white)

    s_curve_white = [(7,3), (7,4), (7,5), (8,5), (8,6)]
    for gx, gy in s_curve_white:
        fill_px(draw, gx, gy, white)
    s_curve_black = [(8,7), (8,8), (8,9), (7,8), (7,9)]
    for gx, gy in s_curve_black:
        fill_px(draw, gx, gy, black)

    fill_px(draw, 6, 5, white)
    fill_px(draw, 9, 8, black)

    for gx, gy in [(5,4), (5,5), (5,6)]:
        fill_px(draw, gx, gy, dk_gray)
    for gx, gy in [(10,8), (10,9), (9,10)]:
        fill_px(draw, gx, gy, lt_gray)

    outline = [
        (6,1), (9,1),
        (5,2), (10,2),
        (4,3), (10,3),
        (4,4), (11,4),
        (4,5), (11,5),
        (4,6), (11,6),
        (4,7), (11,7),
        (4,8), (11,8),
        (4,9), (11,9),
        (5,10), (10,10),
        (6,11), (9,11),
        (7,12), (8,12),
    ]
    for gx, gy in outline:
        fill_px(draw, gx, gy, mid_gray)

    wing_l = [(3,4), (2,5), (3,5), (2,6), (3,6), (3,7)]
    for gx, gy in wing_l:
        fill_px(draw, gx, gy, dk_gray, 140)
    wing_r = [(12,4), (12,5), (13,5), (12,6), (13,6), (12,7)]
    for gx, gy in wing_r:
        fill_px(draw, gx, gy, lt_gray, 140)

    fill_px(draw, 6, 3, white)
    fill_px(draw, 9, 3, black)

    fill_px(draw, 6, 1, dk_gray)
    fill_px(draw, 5, 0, black)
    fill_px(draw, 9, 1, lt_gray)
    fill_px(draw, 10, 0, white)

    legs_l = [(4,9), (4,8), (5,11)]
    legs_r = [(11,9), (11,8), (10,11)]
    for gx, gy in legs_l:
        fill_px(draw, gx, gy, black)
    for gx, gy in legs_r:
        fill_px(draw, gx, gy, dk_gray)

    fill_px(draw, 2, 3, black, 60)
    fill_px(draw, 13, 3, white, 60)
    fill_px(draw, 1, 7, dk_gray, 50)
    fill_px(draw, 14, 7, lt_gray, 50)

    img.save(OUTPUT_DIR + "yin_yang_gu.png")
    print("Generated yin_yang_gu.png")


# ============================================================
# 5. TAI CHI GU - Swirling gray, circular pattern, balanced
# ============================================================
def generate_tai_chi_gu():
    img = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    c1 = hex_to_rgb('#888888')
    c2 = hex_to_rgb('#AAAAAA')
    c3 = hex_to_rgb('#555555')
    c4 = hex_to_rgb('#CCCCCC')
    c5 = hex_to_rgb('#333333')

    body = [
        (6,3), (7,3), (8,3), (9,3),
        (5,4), (6,4), (7,4), (8,4), (9,4), (10,4),
        (5,5), (6,5), (7,5), (8,5), (9,5), (10,5),
        (4,6), (5,6), (6,6), (7,6), (8,6), (9,6), (10,6), (11,6),
        (4,7), (5,7), (6,7), (7,7), (8,7), (9,7), (10,7), (11,7),
        (4,8), (5,8), (6,8), (7,8), (8,8), (9,8), (10,8), (11,8),
        (5,9), (6,9), (7,9), (8,9), (9,9), (10,9),
        (5,10), (6,10), (7,10), (8,10), (9,10), (10,10),
        (6,11), (7,11), (8,11), (9,11),
    ]
    for gx, gy in body:
        fill_px(draw, gx, gy, c1)

    ring_outer = [(6,5), (7,5), (8,5), (9,5),
                  (5,6), (10,6),
                  (5,7), (10,7),
                  (5,8), (10,8),
                  (6,9), (7,9), (8,9), (9,9)]
    for gx, gy in ring_outer:
        fill_px(draw, gx, gy, c3)

    swirl_light = [(7,6), (6,6), (6,7), (7,7)]
    for gx, gy in swirl_light:
        fill_px(draw, gx, gy, c4)
    swirl_dark = [(8,6), (9,6), (9,7), (8,7)]
    for gx, gy in swirl_dark:
        fill_px(draw, gx, gy, c5)
    fill_px(draw, 8, 6, c4)
    fill_px(draw, 7, 7, c5)
    fill_px(draw, 7, 6, c5)
    fill_px(draw, 8, 7, c4)

    fill_px(draw, 6, 8, c2)
    fill_px(draw, 7, 8, c1)
    fill_px(draw, 8, 8, c1)
    fill_px(draw, 9, 8, c3)

    for gx, gy in [(6,3), (7,3), (8,3), (9,3), (5,4), (6,4)]:
        fill_px(draw, gx, gy, c2)
    for gx, gy in [(9,10), (10,10), (8,11), (9,11)]:
        fill_px(draw, gx, gy, c3)

    outline = [
        (5,2), (10,2),
        (4,3), (10,3),
        (4,4), (11,4),
        (4,5), (11,5),
        (3,6), (12,6),
        (3,7), (12,7),
        (3,8), (12,8),
        (4,9), (11,9),
        (4,10), (11,10),
        (5,11), (10,11),
        (6,12), (9,12),
    ]
    for gx, gy in outline:
        fill_px(draw, gx, gy, c5)

    fill_px(draw, 6, 3, c4, 230)
    fill_px(draw, 9, 3, c5, 230)

    fill_px(draw, 5, 2, c5)
    fill_px(draw, 4, 1, c3)
    fill_px(draw, 10, 2, c5)
    fill_px(draw, 11, 1, c3)

    legs = [(3,9), (3,8), (12,9), (12,8), (4,11), (11,11)]
    for gx, gy in legs:
        fill_px(draw, gx, gy, c5)

    for gy in range(16):
        for gx in range(16):
            dx = gx - 7.5
            dy = gy - 7.0
            dist = math.sqrt(dx*dx + (dy*1.1)**2)
            if 6.0 < dist < 7.5:
                angle = math.atan2(dy, dx)
                if math.sin(angle * 2) > 0:
                    fill_px(draw, gx, gy, c4, 30)
                else:
                    fill_px(draw, gx, gy, c5, 30)

    img.save(OUTPUT_DIR + "tai_chi_gu.png")
    print("Generated tai_chi_gu.png")


# ============================================================
# 6. PRIMORDIAL GU - Pure black/white with golden accents, cosmic
# ============================================================
def generate_primordial_gu():
    img = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    black = hex_to_rgb('#000000')
    white = hex_to_rgb('#FFFFFF')
    gold1 = hex_to_rgb('#DDAA33')
    gold2 = hex_to_rgb('#FFCC44')
    gray = hex_to_rgb('#888888')

    # Cosmic energy aura
    for gy in range(16):
        for gx in range(16):
            dx = gx - 7.5
            dy = gy - 7.0
            dist = math.sqrt(dx*dx + dy*dy)
            angle = math.atan2(dy, dx)
            if 5.5 < dist < 8.0:
                swirl = math.sin(angle * 4 + dist * 1.5)
                if swirl > 0.3:
                    alpha = int(45 * (1 - (dist - 5.5) / 2.5))
                    fill_px(draw, gx, gy, gold2, max(0, alpha))
                elif swirl < -0.3:
                    if math.sin(angle * 2) > 0:
                        fill_px(draw, gx, gy, white, 25)
                    else:
                        fill_px(draw, gx, gy, gray, 25)

    body = [
        (7,1), (8,1),
        (6,2), (7,2), (8,2), (9,2),
        (5,3), (6,3), (7,3), (8,3), (9,3), (10,3),
        (5,4), (6,4), (7,4), (8,4), (9,4), (10,4),
        (4,5), (5,5), (6,5), (7,5), (8,5), (9,5), (10,5), (11,5),
        (4,6), (5,6), (6,6), (7,6), (8,6), (9,6), (10,6), (11,6),
        (4,7), (5,7), (6,7), (7,7), (8,7), (9,7), (10,7), (11,7),
        (5,8), (6,8), (7,8), (8,8), (9,8), (10,8),
        (5,9), (6,9), (7,9), (8,9), (9,9), (10,9),
        (6,10), (7,10), (8,10), (9,10),
        (7,11), (8,11),
    ]

    for gx, gy in body:
        if (gx + gy * 0.3) < 8.0:
            fill_px(draw, gx, gy, black)
        else:
            fill_px(draw, gx, gy, white)

    gold_border = [
        (8,2), (7,3), (8,3), (7,4), (8,4),
        (7,5), (8,5), (7,6), (8,6),
        (7,7), (7,8), (8,8),
        (7,9), (8,9),
        (7,10), (8,10),
        (7,11),
    ]
    for gx, gy in gold_border:
        fill_px(draw, gx, gy, gold1)

    crown = [(6,1), (9,1), (7,1), (8,1)]
    for gx, gy in crown:
        fill_px(draw, gx, gy, gold2)
    fill_px(draw, 5, 0, gold1)
    fill_px(draw, 10, 0, gold1)
    fill_px(draw, 7, 0, gold2)
    fill_px(draw, 8, 0, gold2)

    core_ring = [(6,5), (7,5), (8,5), (9,5), (6,6), (9,6), (6,7), (9,7), (6,8), (7,8), (8,8), (9,8)]
    for gx, gy in core_ring:
        fill_px(draw, gx, gy, gold1, 200)
    fill_px(draw, 7, 6, white, 240)
    fill_px(draw, 8, 6, black, 240)
    fill_px(draw, 7, 7, black, 240)
    fill_px(draw, 8, 7, white, 240)

    outline = [
        (5,1), (10,1),
        (4,2), (10,2),
        (4,3), (11,3),
        (4,4), (11,4),
        (3,5), (12,5),
        (3,6), (12,6),
        (3,7), (12,7),
        (4,8), (11,8),
        (4,9), (11,9),
        (5,10), (10,10),
        (6,11), (9,11),
        (7,12), (8,12),
    ]
    for gx, gy in outline:
        fill_px(draw, gx, gy, gold1)

    wing_l = [(2,3), (1,4), (2,4), (3,4), (1,5), (2,5), (3,5), (2,6), (3,6)]
    wing_r = [(13,3), (12,4), (13,4), (14,4), (12,5), (13,5), (14,5), (12,6), (13,6)]
    for gx, gy in wing_l:
        fill_px(draw, gx, gy, gray, 100)
    for gx, gy in wing_r:
        fill_px(draw, gx, gy, gray, 100)
    wing_edge_l = [(1,4), (1,5), (2,3), (2,6)]
    wing_edge_r = [(14,4), (14,5), (13,3), (13,6)]
    for gx, gy in wing_edge_l + wing_edge_r:
        fill_px(draw, gx, gy, gold2, 150)

    fill_px(draw, 6, 2, gold2, 255)
    fill_px(draw, 9, 2, gold2, 255)

    legs = [(3,8), (3,7), (12,8), (12,7), (4,10), (11,10)]
    for gx, gy in legs:
        fill_px(draw, gx, gy, gold1)

    stars = [(0,2), (15,2), (0,8), (15,8), (3,12), (12,12), (7,14), (8,14)]
    for gx, gy in stars:
        fill_px(draw, gx, gy, gold2, 60)

    img.save(OUTPUT_DIR + "primordial_gu.png")
    print("Generated primordial_gu.png")


# ============================================================
# Run all generators
# ============================================================
if __name__ == "__main__":
    generate_true_qi_gu()
    generate_qi_shield_gu()
    generate_profound_qi_gu()
    generate_yin_yang_gu()
    generate_tai_chi_gu()
    generate_primordial_gu()
    print("\nAll 6 Gu worm textures generated successfully!")
