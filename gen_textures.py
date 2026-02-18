from PIL import Image, ImageDraw
import math
import os

ITEM_DIR = "src/main/resources/assets/reverend_insanity/textures/item/"
ENTITY_DIR = "src/main/resources/assets/reverend_insanity/textures/entity/"

def blend3(c1, c2, t):
    """Blend two RGB tuples (3 elements). t=0 returns c1, t=1 returns c2."""
    t = max(0.0, min(1.0, t))
    return (
        int(c1[0] + (c2[0] - c1[0]) * t),
        int(c1[1] + (c2[1] - c1[1]) * t),
        int(c1[2] + (c2[2] - c1[2]) * t),
    )

def rgba(rgb, a=255):
    """Convert RGB tuple to RGBA."""
    return (rgb[0], rgb[1], rgb[2], a)

def set_px(img, x, y, color):
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), color)

def get_px(img, x, y):
    if 0 <= x < img.width and 0 <= y < img.height:
        return img.getpixel((x, y))
    return (0, 0, 0, 0)

def get_rgb(img, x, y):
    c = get_px(img, x, y)
    return (c[0], c[1], c[2])

# ============================================================
# 1. blood_gu.png - 血蛊 (32x32)
# ============================================================
def gen_blood_gu():
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))

    DARK   = (74, 0, 0)
    DEEP   = (139, 0, 0)
    MID    = (180, 10, 10)
    BRIGHT = (255, 0, 0)
    HI     = (255, 60, 60)

    cx, cy = 16, 16
    for y in range(32):
        for x in range(32):
            dist = math.sqrt((x - cx)**2 + (y - cy)**2)
            if dist < 14:
                a = int(30 * (1 - dist / 14))
                set_px(img, x, y, (139, 0, 0, a))

    body_segments = [
        (10, 8, 3, 2),
        (12, 10, 3, 2),
        (14, 13, 3, 3),
        (15, 16, 3, 3),
        (16, 19, 3, 3),
        (17, 22, 3, 2),
        (19, 24, 2, 2),
        (21, 26, 2, 2),
    ]

    for i, (sx, sy, rx, ry) in enumerate(body_segments):
        for dy in range(-ry-1, ry+2):
            for dx in range(-rx-1, rx+2):
                dist = (dx*dx)/(float(rx+1)**2) + (dy*dy)/(float(ry+1)**2)
                if dist <= 1.0:
                    set_px(img, sx+dx+1, sy+dy+1, (30, 0, 0, 100))

    for i, (sx, sy, rx, ry) in enumerate(body_segments):
        for dy in range(-ry, ry+1):
            for dx in range(-rx, rx+1):
                dist = (dx*dx)/(float(rx)**2) + (dy*dy)/(float(ry)**2)
                if dist <= 1.0:
                    if dx < 0 and dy < 0:
                        base = blend3(HI, MID, dist)
                    else:
                        base = blend3(MID, DEEP, dist)
                    color = blend3(base, DARK, dist * 0.5)
                    set_px(img, sx+dx, sy+dy, rgba(color))

    vein_points = [(s[0], s[1]) for s in body_segments]
    for i in range(len(vein_points) - 1):
        x1, y1 = vein_points[i]
        x2, y2 = vein_points[i+1]
        steps = max(abs(x2-x1), abs(y2-y1), 1)
        for s in range(steps + 1):
            t = s / float(steps)
            px = int(x1 + (x2 - x1) * t)
            py = int(y1 + (y2 - y1) * t)
            set_px(img, px, py, rgba(BRIGHT))
            set_px(img, px-1, py, (255, 0, 0, 120))
            set_px(img, px+1, py, (255, 0, 0, 120))

    set_px(img, 9, 7, rgba(BRIGHT))
    set_px(img, 11, 7, rgba(BRIGHT))
    set_px(img, 9, 6, rgba(HI))
    set_px(img, 11, 6, rgba(HI))
    set_px(img, 8, 5, rgba(DEEP))
    set_px(img, 7, 4, rgba(MID))
    set_px(img, 12, 5, rgba(DEEP))
    set_px(img, 13, 4, rgba(MID))
    set_px(img, 22, 27, rgba(DEEP))
    set_px(img, 23, 28, rgba(MID))

    for i, (sx, sy, rx, ry) in enumerate(body_segments[1:-1]):
        set_px(img, sx - rx - 1, sy, rgba(DARK))
        set_px(img, sx + rx + 1, sy, rgba(DARK))
        set_px(img, sx - rx - 1, sy + 1, (100, 0, 0, 180))
        set_px(img, sx + rx + 1, sy + 1, (100, 0, 0, 180))

    img.save(os.path.join(ITEM_DIR, "blood_gu.png"))
    print("  [1/7] blood_gu.png")

# ============================================================
# 2. self_heal_gu.png - 自愈蛊 (32x32)
# ============================================================
def gen_self_heal_gu():
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))

    PINK   = (255, 107, 107)
    DPINK  = (204, 68, 68)
    LPINK  = (255, 153, 153)
    DEEP   = (150, 30, 30)

    cx, cy = 16, 16

    for y in range(32):
        for x in range(32):
            dist = math.sqrt((x - cx)**2 + (y - cy)**2)
            if dist < 15:
                a = int(40 * (1 - dist / 15))
                set_px(img, x, y, (255, 107, 107, a))

    heart_outline = set()
    for t_i in range(3600):
        t = t_i * math.pi / 1800.0
        hx = 16 * math.sin(t)**3
        hy = -(13 * math.cos(t) - 5 * math.cos(2*t) - 2 * math.cos(3*t) - math.cos(4*t))
        px = int(cx + hx * 0.42)
        py = int(cy + hy * 0.42 - 1)
        if 0 <= px < 32 and 0 <= py < 32:
            heart_outline.add((px, py))

    heart_pixels = set()
    for y in range(32):
        row_pixels = [x for x in range(32) if (x, y) in heart_outline]
        if len(row_pixels) >= 2:
            for x in range(min(row_pixels), max(row_pixels) + 1):
                heart_pixels.add((x, y))
    heart_pixels.update(heart_outline)

    if heart_pixels:
        min_y = min(p[1] for p in heart_pixels)
        max_y = max(p[1] for p in heart_pixels)

        for (x, y) in heart_pixels:
            norm_y = (y - min_y) / max(1, (max_y - min_y))
            if (x, y) in heart_outline:
                color = DEEP
            elif norm_y < 0.3:
                color = blend3(LPINK, PINK, norm_y / 0.3)
            elif norm_y < 0.7:
                color = PINK
            else:
                color = blend3(DPINK, DEEP, (norm_y - 0.7) / 0.3)

            dist_to_hl = math.sqrt((x - (cx - 3))**2 + (y - (cy - 4))**2)
            if dist_to_hl < 3 and (x, y) not in heart_outline:
                hl_t = dist_to_hl / 3
                color = blend3((255, 200, 200), color, hl_t)

            set_px(img, x, y, rgba(color))

    vein_lines = [
        [(14, 12), (13, 14), (12, 17), (13, 20)],
        [(18, 12), (19, 14), (20, 17), (19, 20)],
        [(16, 14), (16, 17), (16, 20), (16, 23)],
    ]
    for line in vein_lines:
        for i in range(len(line) - 1):
            x1, y1 = line[i]
            x2, y2 = line[i+1]
            steps = max(abs(x2-x1), abs(y2-y1), 1)
            for s in range(steps + 1):
                t = s / float(steps)
                px = int(x1 + (x2-x1) * t)
                py = int(y1 + (y2-y1) * t)
                if (px, py) in heart_pixels and (px, py) not in heart_outline:
                    existing = get_rgb(img, px, py)
                    darker = blend3(existing, DEEP, 0.3)
                    set_px(img, px, py, rgba(darker))

    set_px(img, 13, 7, rgba(DPINK))
    set_px(img, 12, 6, rgba(PINK))
    set_px(img, 11, 5, rgba(LPINK))
    set_px(img, 19, 7, rgba(DPINK))
    set_px(img, 20, 6, rgba(PINK))
    set_px(img, 21, 5, rgba(LPINK))

    set_px(img, 13, 13, (255, 220, 220, 255))
    set_px(img, 19, 13, (255, 220, 220, 255))

    set_px(img, 16, 26, rgba(DPINK))
    set_px(img, 16, 27, (180, 50, 50, 200))
    set_px(img, 16, 28, (150, 30, 30, 150))

    img.save(os.path.join(ITEM_DIR, "self_heal_gu.png"))
    print("  [2/7] self_heal_gu.png")

# ============================================================
# 3. solidify_origin_gu.png - 固元蛊 (32x32)
# ============================================================
def gen_solidify_origin_gu():
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))

    VDARK  = (50, 0, 0)
    DRED   = (102, 0, 0)
    BRED   = (170, 0, 0)
    MRED   = (200, 20, 20)
    HI     = (255, 51, 51)
    BRITE  = (255, 100, 100)

    cx, cy = 16, 16
    radius = 10

    for y in range(32):
        for x in range(32):
            dist = math.sqrt((x - cx)**2 + (y - cy)**2)
            if radius < dist < radius + 4:
                a = int(50 * (1 - (dist - radius) / 4))
                set_px(img, x, y, (170, 0, 0, a))

    for y in range(32):
        for x in range(32):
            dx = x - cx
            dy = y - cy
            dist = math.sqrt(dx*dx + dy*dy)
            if dist <= radius:
                norm_dist = dist / radius
                light_dot = (dx / max(dist, 0.1) * (-0.5) + dy / max(dist, 0.1) * (-0.6))
                lf = max(0.0, light_dot)
                if lf > 0.6:
                    base = blend3(HI, MRED, (1 - lf) / 0.4)
                elif lf > 0.2:
                    base = blend3(MRED, BRED, (0.6 - lf) / 0.4)
                else:
                    base = blend3(BRED, DRED, (0.2 - lf) / 0.2)
                color = blend3(base, VDARK, (norm_dist ** 2) * 0.5)
                wave1 = math.sin(x * 0.8 + y * 0.3) * 0.5 + 0.5
                wave2 = math.sin(x * 0.3 - y * 0.7 + 2) * 0.5 + 0.5
                wave = (wave1 + wave2) / 2
                if wave > 0.65:
                    color = blend3(color, HI, (wave - 0.65) / 0.35 * 0.3)
                elif wave < 0.35:
                    color = blend3(color, VDARK, (0.35 - wave) / 0.35 * 0.3)
                set_px(img, x, y, rgba(color))

    for dy in range(-2, 1):
        for dx in range(-2, 1):
            hx, hy = cx - 3 + dx, cy - 3 + dy
            d = math.sqrt(dx*dx + dy*dy)
            if d < 2:
                existing = get_rgb(img, hx, hy)
                if get_px(img, hx, hy)[3] > 0:
                    new_c = blend3(existing, (255, 150, 150), 0.4 + 0.3 * (1 - d/2))
                    set_px(img, hx, hy, rgba(new_c))

    legs = [
        (cx - radius + 1, cy - 3), (cx - radius + 1, cy + 3),
        (cx + radius - 1, cy - 3), (cx + radius - 1, cy + 3),
        (cx - 5, cy + radius - 1), (cx + 5, cy + radius - 1),
    ]
    for lx, ly in legs:
        set_px(img, lx, ly, rgba(DRED))
        dx_dir = 1 if lx > cx else -1
        dy_dir = 1 if ly > cy else -1
        set_px(img, lx + dx_dir, ly + dy_dir, (120, 0, 0, 200))

    set_px(img, cx - 3, cy - 2, rgba(HI))
    set_px(img, cx - 1, cy - 2, rgba(HI))
    set_px(img, cx - 3, cy - 1, rgba(BRITE))
    set_px(img, cx - 1, cy - 1, rgba(BRITE))

    img.save(os.path.join(ITEM_DIR, "solidify_origin_gu.png"))
    print("  [3/7] solidify_origin_gu.png")

# ============================================================
# 4. blood_wing_gu.png - 血翼蛊 (32x32)
# ============================================================
def gen_blood_wing_gu():
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))

    DBODY  = (88, 0, 0)
    BODY   = (136, 0, 0)
    MID    = (170, 20, 20)
    HI     = (255, 80, 80)
    WVEIN  = (255, 68, 68, 220)
    WMEM   = (180, 20, 20, 100)
    EYE    = (255, 160, 160, 255)

    cx, cy = 16, 18

    def draw_line(img, x1, y1, x2, y2, color):
        steps = max(abs(x2-x1), abs(y2-y1), 1)
        for s in range(steps + 1):
            t = s / float(steps)
            set_px(img, int(x1+(x2-x1)*t), int(y1+(y2-y1)*t), color)

    # Left wing membrane
    left_wing_outline = [
        (3, 5), (2, 6), (2, 7), (3, 7), (3, 8), (2, 9), (3, 10),
        (3, 11), (4, 12), (3, 13), (3, 14), (4, 15), (5, 16),
        (6, 16), (7, 16), (8, 16), (9, 17), (10, 17), (11, 17),
        (10, 16), (9, 15), (8, 14), (7, 13), (6, 12), (6, 11),
        (5, 10), (5, 9), (4, 8), (4, 7), (4, 6), (3, 5),
    ]
    for y in range(4, 18):
        row = [p[0] for p in left_wing_outline if p[1] == y]
        if len(row) >= 2:
            for x in range(min(row), max(row) + 1):
                if get_px(img, x, y)[3] == 0:
                    set_px(img, x, y, WMEM)

    left_bones = [
        [(11,17),(8,12),(5,8),(3,5)],
        [(8,12),(5,11),(3,10)],
        [(8,12),(6,14),(4,15)],
        [(5,8),(3,7)],
    ]
    for bone in left_bones:
        for i in range(len(bone)-1):
            draw_line(img, bone[i][0], bone[i][1], bone[i+1][0], bone[i+1][1], WVEIN)

    # Right wing (mirror)
    right_wing_outline = [
        (29,5),(30,6),(30,7),(29,7),(29,8),(30,9),(29,10),
        (29,11),(28,12),(29,13),(29,14),(28,15),(27,16),
        (26,16),(25,16),(24,16),(23,17),(22,17),(21,17),
        (22,16),(23,15),(24,14),(25,13),(26,12),(26,11),
        (27,10),(27,9),(28,8),(28,7),(28,6),(29,5),
    ]
    for y in range(4, 18):
        row = [p[0] for p in right_wing_outline if p[1] == y]
        if len(row) >= 2:
            for x in range(min(row), max(row) + 1):
                if get_px(img, x, y)[3] == 0:
                    set_px(img, x, y, WMEM)

    right_bones = [
        [(21,17),(24,12),(27,8),(29,5)],
        [(24,12),(27,11),(29,10)],
        [(24,12),(26,14),(28,15)],
        [(27,8),(29,7)],
    ]
    for bone in right_bones:
        for i in range(len(bone)-1):
            draw_line(img, bone[i][0], bone[i][1], bone[i+1][0], bone[i+1][1], WVEIN)

    # Body
    body_rx, body_ry = 4, 6
    for dy in range(-body_ry, body_ry + 1):
        for dx in range(-body_rx, body_rx + 1):
            dist = (dx*dx)/(float(body_rx)**2) + (dy*dy)/(float(body_ry)**2)
            if dist <= 1.0:
                if dx < 0 and dy < 0:
                    color = blend3(HI, MID, dist)
                else:
                    color = blend3(MID, DBODY, dist)
                set_px(img, cx + dx, cy + dy, rgba(color))

    for seg_y in [cy-4, cy-2, cy, cy+2, cy+4]:
        for dx in range(-body_rx+1, body_rx):
            if (dx*dx)/(float(body_rx)**2) + ((seg_y-cy)**2)/(float(body_ry)**2) <= 0.9:
                existing = get_rgb(img, cx+dx, seg_y)
                if get_px(img, cx+dx, seg_y)[3] > 0:
                    set_px(img, cx+dx, seg_y, rgba(blend3(existing, DBODY, 0.3)))

    set_px(img, cx-2, cy-4, EYE)
    set_px(img, cx+2, cy-4, EYE)
    set_px(img, cx-3, cy-6, rgba(BODY))
    set_px(img, cx-4, cy-7, rgba(MID))
    set_px(img, cx+3, cy-6, rgba(BODY))
    set_px(img, cx+4, cy-7, rgba(MID))
    set_px(img, cx, cy+body_ry+1, rgba(BODY))
    set_px(img, cx, cy+body_ry+2, rgba(DBODY))

    img.save(os.path.join(ITEM_DIR, "blood_wing_gu.png"))
    print("  [4/7] blood_wing_gu.png")

# ============================================================
# 5. poison_bee_gu.png - 毒蜂蛊 (32x32)
# ============================================================
def gen_poison_bee_gu():
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))

    PGRN  = (68, 170, 0)
    DGRN  = (34, 102, 0)
    BGRN  = (100, 200, 30)
    YEL   = (204, 204, 0)
    DYEL  = (160, 160, 0)
    WCOL  = (136, 255, 136, 100)
    WEDGE = (100, 220, 100, 160)
    EYE   = (200, 0, 0)
    BLK   = (20, 40, 0)

    cx, cy = 16, 15

    # Wings
    wing_shapes = [(10,9,5,3),(22,9,5,3),(9,13,4,2),(23,13,4,2)]
    for wx, wy, wrx, wry in wing_shapes:
        for dy in range(-wry, wry+1):
            for dx in range(-wrx, wrx+1):
                dist = (dx*dx)/(float(wrx)**2) + (dy*dy)/(float(wry)**2)
                if dist <= 1.0:
                    set_px(img, wx+dx, wy+dy, WEDGE if dist > 0.8 else WCOL)
        for dx in range(-wrx+1, wrx):
            if abs(dx) % 2 == 0:
                set_px(img, wx+dx, wy, WEDGE)
        set_px(img, wx, wy-1, WEDGE)
        set_px(img, wx, wy+1, WEDGE)

    # Head
    head_r, head_y = 3, cy-5
    for dy in range(-head_r, head_r+1):
        for dx in range(-head_r, head_r+1):
            dist = math.sqrt(dx*dx + dy*dy)
            if dist <= head_r:
                set_px(img, cx+dx, head_y+dy, rgba(blend3(BGRN, DGRN, dist/head_r)))

    set_px(img, cx-2, head_y-1, rgba(EYE))
    set_px(img, cx+2, head_y-1, rgba(EYE))
    set_px(img, cx-2, head_y, (220, 30, 30, 255))
    set_px(img, cx+2, head_y, (220, 30, 30, 255))

    set_px(img, cx-2, head_y-3, rgba(DGRN))
    set_px(img, cx-3, head_y-4, rgba(PGRN))
    set_px(img, cx-4, head_y-5, rgba(BGRN))
    set_px(img, cx+2, head_y-3, rgba(DGRN))
    set_px(img, cx+3, head_y-4, rgba(PGRN))
    set_px(img, cx+4, head_y-5, rgba(BGRN))

    # Thorax
    trx, try_ = 4, 3
    ty = cy
    for dy in range(-try_, try_+1):
        for dx in range(-trx, trx+1):
            dist = (dx*dx)/(float(trx)**2) + (dy*dy)/(float(try_)**2)
            if dist <= 1.0:
                stripe = ((ty + dy) % 3 == 0)
                base = blend3(YEL, DYEL, dist) if stripe else blend3(PGRN, DGRN, dist)
                set_px(img, cx+dx, ty+dy, rgba(base))

    # Abdomen
    arx, ary = 5, 5
    ay = cy + 6
    for dy in range(-ary, ary+1):
        for dx in range(-arx, arx+1):
            local_rx = arx * (1 - max(0, dy) / (ary * 2))
            dist = (dx*dx)/(float(max(local_rx,1))**2) + (dy*dy)/(float(ary)**2)
            if dist <= 1.0:
                si = (ay + dy) % 3
                if si == 0:
                    base = blend3(YEL, DYEL, dist)
                elif si == 1:
                    base = blend3(PGRN, DGRN, dist)
                else:
                    base = blend3(BGRN, PGRN, dist)
                if dx < 0:
                    base = blend3(base, BGRN, 0.1)
                set_px(img, cx+dx, ay+dy, rgba(base))

    # Stinger
    for i in range(3):
        a = 255 - i * 40
        g = 170 - i * 30
        set_px(img, cx, ay+ary+1+i, (40, g, 0, a))

    # Legs
    for ly in [cy-1, cy, cy+1]:
        set_px(img, cx-trx-1, ly, rgba(DGRN))
        set_px(img, cx-trx-2, ly+1, rgba(DGRN))
        set_px(img, cx-trx-3, ly+2, rgba(BLK))
        set_px(img, cx+trx+1, ly, rgba(DGRN))
        set_px(img, cx+trx+2, ly+1, rgba(DGRN))
        set_px(img, cx+trx+3, ly+2, rgba(BLK))

    img.save(os.path.join(ITEM_DIR, "poison_bee_gu.png"))
    print("  [5/7] poison_bee_gu.png")

# ============================================================
# 6. gold_silkworm_gu.png - 金蚕蛊 (32x32)
# ============================================================
def gen_gold_silkworm_gu():
    img = Image.new('RGBA', (32, 32), (0, 0, 0, 0))

    GOLD   = (255, 215, 0)
    DGOLD  = (170, 136, 0)
    LGOLD  = (255, 235, 100)
    BGOLD  = (255, 245, 160)
    XDGOLD = (120, 90, 0)
    PGRN   = (68, 170, 0)
    DGRN   = (34, 102, 0)

    cx, cy = 16, 16
    for y in range(32):
        for x in range(32):
            dist = math.sqrt((x-cx)**2 + (y-cy)**2)
            if dist < 15:
                set_px(img, x, y, (255, 215, 0, int(25 * (1 - dist/15))))

    segments = [
        (7,15,3,3), (11,15,3,3), (15,15,4,4),
        (19,15,4,4), (23,15,3,3), (26,15,2,2),
    ]

    for si, (sx, sy, rx, ry) in enumerate(segments):
        for dy in range(-ry, ry+1):
            for dx in range(-rx, rx+1):
                dist = (dx*dx)/(float(rx)**2) + (dy*dy)/(float(ry)**2)
                if dist <= 1.0:
                    base = blend3(BGOLD, GOLD, dist) if dy < 0 else blend3(GOLD, DGOLD, dist)
                    base = blend3(base, XDGOLD, dist * 0.3)
                    silk = math.sin((sx+dx) * 1.5) * 0.5 + 0.5
                    if silk > 0.7:
                        base = blend3(base, LGOLD, 0.2)
                    elif silk < 0.3:
                        base = blend3(base, DGOLD, 0.15)
                    set_px(img, sx+dx, sy+dy, rgba(base))

        if si < len(segments) - 1:
            nsx = segments[si+1][0]
            gap_x = (sx + rx + nsx - segments[si+1][2]) // 2
            for gy in range(sy - ry + 1, sy + ry):
                if get_px(img, gap_x, gy)[3] > 0:
                    set_px(img, gap_x, gy, rgba(XDGOLD))

    for si, (sx, sy, rx, ry) in enumerate(segments):
        for dx in range(-rx+1, rx):
            hl_y = sy - ry + 1
            if get_px(img, sx+dx, hl_y)[3] > 0:
                existing = get_rgb(img, sx+dx, hl_y)
                set_px(img, sx+dx, hl_y, rgba(blend3(existing, BGOLD, 0.4)))

    poison_spots = [(12,14),(16,13),(20,14),(18,17),(14,17),(22,16),(10,16),(24,14)]
    for px, py in poison_spots:
        if get_px(img, px, py)[3] > 0:
            set_px(img, px, py, rgba(PGRN))
            for ndy in [-1,0,1]:
                for ndx in [-1,0,1]:
                    if ndx == 0 and ndy == 0: continue
                    nx, ny = px+ndx, py+ndy
                    if get_px(img, nx, ny)[3] > 0:
                        set_px(img, nx, ny, rgba(blend3(get_rgb(img,nx,ny), DGRN, 0.15)))

    set_px(img, 5, 13, rgba(PGRN))
    set_px(img, 5, 14, (100, 200, 30, 255))
    set_px(img, 6, 13, rgba(DGRN))
    set_px(img, 4, 15, rgba(DGOLD))
    set_px(img, 3, 15, rgba(XDGOLD))
    set_px(img, 5, 11, rgba(GOLD))
    set_px(img, 4, 10, rgba(LGOLD))
    set_px(img, 6, 11, rgba(GOLD))
    set_px(img, 7, 10, rgba(LGOLD))

    for si, (sx, sy, rx, ry) in enumerate(segments[:-1]):
        set_px(img, sx, sy+ry+1, rgba(DGOLD))
        set_px(img, sx, sy+ry+2, rgba(XDGOLD))
        if si > 0:
            set_px(img, sx-1, sy+ry+1, (140,100,0,200))
            set_px(img, sx+1, sy+ry+1, (140,100,0,200))

    set_px(img, 28, 15, (200,180,50,200))
    set_px(img, 29, 15, (180,160,40,150))
    set_px(img, 30, 14, (160,140,30,100))
    set_px(img, 30, 16, (160,140,30,100))

    img.save(os.path.join(ITEM_DIR, "gold_silkworm_gu.png"))
    print("  [6/7] gold_silkworm_gu.png")

# ============================================================
# 7. blood_bolt.png - 血弹投射物 (16x16)
# ============================================================
def gen_blood_bolt():
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))

    BRITE = (255, 68, 68)
    RED   = (255, 0, 0)
    DRED  = (136, 0, 0)
    CORE  = (255, 120, 120)

    cx, cy = 8, 8

    # Outer blood mist with spiral effect
    for y in range(16):
        for x in range(16):
            dx = x - cx
            dy = y - cy
            dist = math.sqrt(dx*dx + dy*dy)
            if 3 < dist < 7:
                angle = math.atan2(dy, dx)
                spiral = math.sin(angle * 3 + dist * 0.8) * 0.5 + 0.5
                base_alpha = int(80 * (1 - (dist - 3) / 4))
                if spiral > 0.5:
                    alpha = int(base_alpha * spiral)
                    color = blend3(DRED, RED, (spiral - 0.5) * 2)
                    set_px(img, x, y, (color[0], color[1], color[2], alpha))

    # Core sphere
    core_r = 3.5
    for y in range(16):
        for x in range(16):
            dx = x - cx + 0.5
            dy = y - cy + 0.5
            dist = math.sqrt(dx*dx + dy*dy)
            if dist <= core_r:
                norm = dist / core_r
                if norm < 0.3:
                    color = blend3(CORE, BRITE, norm / 0.3)
                elif norm < 0.7:
                    color = blend3(BRITE, RED, (norm - 0.3) / 0.4)
                else:
                    color = blend3(RED, DRED, (norm - 0.7) / 0.3)
                set_px(img, x, y, rgba(color))

    set_px(img, cx-1, cy-1, rgba(CORE))
    set_px(img, cx-1, cy-2, (255, 160, 160, 200))

    # Blood mist trails
    trails = [
        [(cx+3,cy-1),(cx+4,cy-2),(cx+5,cy-3),(cx+6,cy-3)],
        [(cx+2,cy+2),(cx+3,cy+3),(cx+4,cy+4),(cx+5,cy+4)],
        [(cx-2,cy-2),(cx-3,cy-3),(cx-4,cy-3)],
        [(cx+1,cy-3),(cx+2,cy-4),(cx+3,cy-5)],
        [(cx-1,cy+3),(cx-2,cy+4),(cx-3,cy+5)],
    ]
    for trail in trails:
        for i, (tx, ty) in enumerate(trail):
            alpha = max(40, 160 - i * 50)
            t = i / max(1, len(trail)-1)
            color = blend3(RED, DRED, t)
            set_px(img, tx, ty, (color[0], color[1], color[2], alpha))

    img.save(os.path.join(ENTITY_DIR, "blood_bolt.png"))
    print("  [7/7] blood_bolt.png")


# ============================================================
if __name__ == "__main__":
    os.makedirs(ITEM_DIR, exist_ok=True)
    os.makedirs(ENTITY_DIR, exist_ok=True)

    print("Generating textures...")
    gen_blood_gu()
    gen_self_heal_gu()
    gen_solidify_origin_gu()
    gen_blood_wing_gu()
    gen_poison_bee_gu()
    gen_gold_silkworm_gu()
    gen_blood_bolt()
    print("\nAll 7 textures generated successfully!")
