#!/usr/bin/env python3
"""Generate 23 new gu worm item textures for Reverend Insanity mod."""

from PIL import Image, ImageDraw
import math
import os

OUTPUT_DIR = "/mnt/e/code/mod/gu/src/main/resources/assets/reverend_insanity/textures/item"
SIZE = 64
PX = 4  # grid pixel size (64/16 = 4)


def px(x, y, draw, color):
    """Draw a single 'pixel' (4x4 block) at grid position x,y."""
    if 0 <= x < 16 and 0 <= y < 16:
        draw.rectangle([x * PX, y * PX, (x + 1) * PX - 1, (y + 1) * PX - 1], fill=color)


def blend_pixel(img, x, y, color):
    """Alpha-blend a color onto existing pixel."""
    if 0 <= x < SIZE and 0 <= y < SIZE:
        r, g, b, a = color
        if a <= 0:
            return
        existing = img.getpixel((x, y))
        er, eg, eb, ea = existing
        if ea == 0:
            img.putpixel((x, y), (r, g, b, a))
        else:
            alpha = a / 255.0
            nr = int(er * (1 - alpha) + r * alpha)
            ng = int(eg * (1 - alpha) + g * alpha)
            nb = int(eb * (1 - alpha) + b * alpha)
            na = min(255, ea + a)
            img.putpixel((x, y), (nr, ng, nb, na))


def draw_glow(img, cx, cy, radius, color, intensity=80):
    """Draw a soft glow effect around a point."""
    r, g, b = color[:3]
    for y in range(max(0, int(cy - radius)), min(SIZE, int(cy + radius + 1))):
        for x in range(max(0, int(cx - radius)), min(SIZE, int(cx + radius + 1))):
            dist = math.sqrt((x - cx) ** 2 + (y - cy) ** 2)
            if dist < radius:
                alpha = int(intensity * (1 - dist / radius) ** 2)
                if alpha > 0:
                    blend_pixel(img, x, y, (r, g, b, alpha))


def draw_circle_filled(img, cx, cy, r, color):
    """Draw a filled circle at sub-pixel resolution."""
    for y in range(max(0, int(cy - r - 1)), min(SIZE, int(cy + r + 2))):
        for x in range(max(0, int(cx - r - 1)), min(SIZE, int(cx + r + 2))):
            dist = math.sqrt((x - cx) ** 2 + (y - cy) ** 2)
            if dist <= r:
                blend_pixel(img, x, y, color)


def save(img, name):
    path = os.path.join(OUTPUT_DIR, name)
    img.save(path)
    print(f"Saved: {path}")


# ============================================================
# 1. BRONZE SARIRA GU - 青铜舍利蛊
# Round bone bead, copper-brown, Buddhist halo, dark bone veins
# ============================================================
def gen_bronze_sarira_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    bronze_dk = (0x8B, 0x6B, 0x42)
    bronze = (0xB0, 0x85, 0x55)
    bronze_lt = (0xCC, 0xA0, 0x6A)
    bronze_hi = (0xE0, 0xC0, 0x88)
    bone_vein = (0x6B, 0x50, 0x35)
    outline = (0x5A, 0x42, 0x2A)
    halo_gold = (0xDD, 0xBB, 0x55)

    # Buddhist halo glow
    draw_glow(img, 32, 32, 26, (0xDD, 0xBB, 0x55), 35)
    draw_glow(img, 32, 32, 18, (0xCC, 0xAA, 0x44), 25)

    # Halo ring (outer)
    for angle in range(0, 360, 8):
        rad = math.radians(angle)
        x = int(32 + math.cos(rad) * 22)
        y = int(32 + math.sin(rad) * 22)
        blend_pixel(img, x, y, (0xDD, 0xBB, 0x55, 140))
        blend_pixel(img, x + 1, y, (0xDD, 0xBB, 0x55, 80))

    # Main bead - outline
    for r, c in [(3, 6), (3, 7), (3, 8), (3, 9),
                 (4, 5), (4, 10), (5, 4), (5, 11),
                 (6, 4), (6, 11), (7, 4), (7, 11),
                 (8, 4), (8, 11), (9, 4), (9, 11),
                 (10, 4), (10, 11), (11, 5), (11, 10),
                 (12, 6), (12, 7), (12, 8), (12, 9)]:
        px(c, r, d, outline)

    # Main bead fill
    for r in range(4, 12):
        if r == 4 or r == 11:
            cols = range(6, 10)
        elif r == 5 or r == 10:
            cols = range(5, 11)
        else:
            cols = range(5, 11)
        for c in cols:
            if c <= 6 and r >= 8:
                px(c, r, d, bronze_dk)
            elif c >= 9 and r <= 5:
                px(c, r, d, bronze_lt)
            elif c <= 6 and r <= 5:
                px(c, r, d, bronze_lt)
            elif c >= 9:
                px(c, r, d, bronze_dk)
            else:
                px(c, r, d, bronze)

    # Highlight
    px(6, 5, d, bronze_hi)
    px(7, 5, d, bronze_hi)
    px(6, 6, d, bronze_hi)

    # Bone vein pattern
    for r, c in [(6, 7), (7, 8), (8, 6), (9, 8), (7, 5), (10, 7)]:
        px(c, r, d, bone_vein)

    # Small halo rays
    ray_positions = [(2, 7), (2, 8), (7, 2), (8, 2), (7, 13), (8, 13), (13, 7), (13, 8)]
    for r, c in ray_positions:
        px(c, r, d, (0xDD, 0xBB, 0x55, 120))

    save(img, "bronze_sarira_gu.png")


# ============================================================
# 2. IRON SARIRA GU - 赤铁舍利蛊
# Iron-grey with reddish tint, more solid
# ============================================================
def gen_iron_sarira_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    iron_dk = (0x6B, 0x55, 0x55)
    iron = (0x8B, 0x70, 0x70)
    iron_lt = (0xA8, 0x8A, 0x8A)
    iron_hi = (0xCC, 0xAA, 0xAA)
    vein = (0x55, 0x3A, 0x3A)
    outline = (0x44, 0x33, 0x33)
    halo_red = (0xBB, 0x77, 0x55)

    # Subtle red halo
    draw_glow(img, 32, 32, 24, (0xBB, 0x77, 0x55), 30)
    draw_glow(img, 32, 32, 16, (0xAA, 0x66, 0x44), 20)

    # Halo ring
    for angle in range(0, 360, 10):
        rad = math.radians(angle)
        x = int(32 + math.cos(rad) * 21)
        y = int(32 + math.sin(rad) * 21)
        blend_pixel(img, x, y, (0xBB, 0x77, 0x55, 120))

    # Bead outline
    for r, c in [(3, 6), (3, 7), (3, 8), (3, 9),
                 (4, 5), (4, 10), (5, 4), (5, 11),
                 (6, 4), (6, 11), (7, 4), (7, 11),
                 (8, 4), (8, 11), (9, 4), (9, 11),
                 (10, 4), (10, 11), (11, 5), (11, 10),
                 (12, 6), (12, 7), (12, 8), (12, 9)]:
        px(c, r, d, outline)

    # Bead fill
    for r in range(4, 12):
        if r == 4 or r == 11:
            cols = range(6, 10)
        elif r == 5 or r == 10:
            cols = range(5, 11)
        else:
            cols = range(5, 11)
        for c in cols:
            if c <= 6 and r <= 6:
                px(c, r, d, iron_lt)
            elif c >= 9 or r >= 10:
                px(c, r, d, iron_dk)
            else:
                px(c, r, d, iron)

    # Highlight
    px(6, 5, d, iron_hi)
    px(7, 5, d, iron_hi)
    px(6, 6, d, iron_hi)

    # Vein cracks (more angular, metallic)
    for r, c in [(5, 8), (6, 7), (7, 9), (8, 6), (9, 8), (10, 7)]:
        px(c, r, d, vein)

    # Metal sheen dots
    px(8, 7, d, (0xDD, 0xBB, 0xBB))

    save(img, "iron_sarira_gu.png")


# ============================================================
# 3. SILVER SARIRA GU - 白银舍利蛊
# Silver-white, crystalline, luminous
# ============================================================
def gen_silver_sarira_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    silver_dk = (0xAA, 0xAA, 0xBB)
    silver = (0xCC, 0xCC, 0xDD)
    silver_lt = (0xDD, 0xDD, 0xEE)
    silver_hi = (0xFF, 0xFF, 0xFF)
    vein = (0x88, 0x88, 0x99)
    outline = (0x77, 0x77, 0x88)
    halo = (0xCC, 0xCC, 0xFF)

    # Bright white-silver halo
    draw_glow(img, 32, 32, 28, (0xDD, 0xDD, 0xFF), 50)
    draw_glow(img, 32, 32, 20, (0xCC, 0xCC, 0xEE), 35)

    # Halo ring sparkle
    for angle in range(0, 360, 12):
        rad = math.radians(angle)
        x = int(32 + math.cos(rad) * 22)
        y = int(32 + math.sin(rad) * 22)
        blend_pixel(img, x, y, (0xFF, 0xFF, 0xFF, 150))
        blend_pixel(img, x + 1, y, (0xDD, 0xDD, 0xFF, 80))

    # Bead outline
    for r, c in [(3, 6), (3, 7), (3, 8), (3, 9),
                 (4, 5), (4, 10), (5, 4), (5, 11),
                 (6, 4), (6, 11), (7, 4), (7, 11),
                 (8, 4), (8, 11), (9, 4), (9, 11),
                 (10, 4), (10, 11), (11, 5), (11, 10),
                 (12, 6), (12, 7), (12, 8), (12, 9)]:
        px(c, r, d, outline)

    # Bead fill - crystalline
    for r in range(4, 12):
        if r == 4 or r == 11:
            cols = range(6, 10)
        elif r == 5 or r == 10:
            cols = range(5, 11)
        else:
            cols = range(5, 11)
        for c in cols:
            if c <= 6 and r <= 6:
                px(c, r, d, silver_hi)
            elif (c + r) % 3 == 0:
                px(c, r, d, silver_lt)
            elif c >= 9 or r >= 10:
                px(c, r, d, silver_dk)
            else:
                px(c, r, d, silver)

    # Crystal-like vein (lighter, not dark)
    for r, c in [(6, 8), (7, 7), (8, 9), (9, 6)]:
        px(c, r, d, silver_hi)

    # Sparkle crosses
    for sx, sy in [(6, 5), (9, 8)]:
        px(sx, sy, d, silver_hi)
        px(sx - 1, sy, d, (0xEE, 0xEE, 0xFF, 180))
        px(sx + 1, sy, d, (0xEE, 0xEE, 0xFF, 180))
        px(sx, sy - 1, d, (0xEE, 0xEE, 0xFF, 180))
        px(sx, sy + 1, d, (0xEE, 0xEE, 0xFF, 180))

    save(img, "silver_sarira_gu.png")


# ============================================================
# 4. GOLD SARIRA GU - 黄金舍利蛊
# Golden, most ornate, radiant golden light
# ============================================================
def gen_gold_sarira_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    gold_dk = (0xBB, 0x88, 0x22)
    gold = (0xDD, 0xAA, 0x33)
    gold_lt = (0xEE, 0xCC, 0x55)
    gold_hi = (0xFF, 0xEE, 0x88)
    gold_white = (0xFF, 0xFF, 0xCC)
    outline = (0x88, 0x66, 0x11)

    # Radiant golden glow
    draw_glow(img, 32, 32, 30, (0xFF, 0xDD, 0x44), 55)
    draw_glow(img, 32, 32, 22, (0xFF, 0xCC, 0x33), 40)
    draw_glow(img, 32, 32, 14, (0xFF, 0xEE, 0x88), 25)

    # Golden rays
    for angle in range(0, 360, 30):
        rad = math.radians(angle)
        for dist in range(20, 28):
            x = int(32 + math.cos(rad) * dist)
            y = int(32 + math.sin(rad) * dist)
            alpha = int(120 * (1 - (dist - 20) / 8.0))
            blend_pixel(img, x, y, (0xFF, 0xDD, 0x44, alpha))

    # Halo ring
    for angle in range(0, 360, 6):
        rad = math.radians(angle)
        x = int(32 + math.cos(rad) * 23)
        y = int(32 + math.sin(rad) * 23)
        blend_pixel(img, x, y, (0xFF, 0xEE, 0x88, 180))

    # Bead outline
    for r, c in [(3, 6), (3, 7), (3, 8), (3, 9),
                 (4, 5), (4, 10), (5, 4), (5, 11),
                 (6, 4), (6, 11), (7, 4), (7, 11),
                 (8, 4), (8, 11), (9, 4), (9, 11),
                 (10, 4), (10, 11), (11, 5), (11, 10),
                 (12, 6), (12, 7), (12, 8), (12, 9)]:
        px(c, r, d, outline)

    # Bead fill - rich gold
    for r in range(4, 12):
        if r == 4 or r == 11:
            cols = range(6, 10)
        elif r == 5 or r == 10:
            cols = range(5, 11)
        else:
            cols = range(5, 11)
        for c in cols:
            if c <= 6 and r <= 6:
                px(c, r, d, gold_hi)
            elif c == 7 and r == 5:
                px(c, r, d, gold_white)
            elif c >= 9 or r >= 10:
                px(c, r, d, gold_dk)
            elif (c + r) % 2 == 0:
                px(c, r, d, gold_lt)
            else:
                px(c, r, d, gold)

    # Ornamental pattern (cross pattern on bead)
    px(7, 7, d, gold_white)
    px(8, 8, d, gold_white)
    px(7, 8, d, gold_hi)
    px(8, 7, d, gold_hi)

    # Sparkles around
    sparkles = [(4, 3), (11, 3), (3, 7), (12, 8), (4, 12), (11, 12)]
    for sx, sy in sparkles:
        px(sx, sy, d, (0xFF, 0xFF, 0xCC, 200))

    save(img, "gold_sarira_gu.png")


# ============================================================
# 5. BROWN BEAR GU - 棕熊本力蛊
# Small brown worm, bear claw texture, powerful
# ============================================================
def gen_brown_bear_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    brown_dk = (0x5A, 0x3A, 0x1E)
    brown = (0x8B, 0x5E, 0x2E)
    brown_lt = (0xAA, 0x77, 0x44)
    brown_hi = (0xCC, 0x99, 0x55)
    outline = (0x3D, 0x28, 0x14)
    claw = (0xDD, 0xCC, 0xAA)
    claw_dk = (0xBB, 0xAA, 0x88)
    eye = (0xFF, 0x33, 0x00)

    # Body outline (stout, muscular worm)
    body_out = [(4, 6), (4, 7), (4, 8), (4, 9),
                (5, 5), (5, 10), (6, 4), (6, 11),
                (7, 4), (7, 11), (8, 4), (8, 11),
                (9, 4), (9, 11), (10, 4), (10, 11),
                (11, 5), (11, 10), (12, 6), (12, 7), (12, 8), (12, 9)]
    for r, c in body_out:
        px(c, r, d, outline)

    # Body fill - thick, powerful
    for r in range(5, 12):
        if r == 5 or r == 11:
            cols = range(6, 10)
        else:
            cols = range(5, 11)
        for c in cols:
            if r <= 6 and c <= 6:
                px(c, r, d, brown_lt)
            elif r >= 10 or c >= 10:
                px(c, r, d, brown_dk)
            else:
                px(c, r, d, brown)

    # Highlight on head area
    px(6, 5, d, brown_hi)
    px(7, 5, d, brown_hi)

    # Bear claw markings on body (3 claw scratches)
    for r, c in [(6, 6), (7, 7), (8, 6)]:
        px(c, r, d, claw)
    for r, c in [(6, 8), (7, 9), (8, 8)]:
        px(c, r, d, claw)
    for r, c in [(7, 5), (8, 5), (9, 5)]:
        px(c, r, d, claw_dk)

    # Eyes (fierce)
    px(6, 6, d, eye)
    px(9, 6, d, eye)

    # Small legs (stumpy, powerful)
    for r, c in [(8, 3), (9, 3), (10, 3)]:
        px(c, r, d, brown_dk)
    for r, c in [(8, 12), (9, 12), (10, 12)]:
        px(c, r, d, brown_dk)

    # Claw tips on legs
    px(3, 8, d, claw)
    px(12, 8, d, claw)
    px(3, 10, d, claw)
    px(12, 10, d, claw)

    # Fur texture dots
    for r, c in [(5, 7), (7, 10), (9, 7), (11, 8)]:
        px(c, r, d, brown_lt)

    save(img, "brown_bear_gu.png")


# ============================================================
# 6. FLOWER BOAR GU - 花豕蛊
# Cute piglet-shaped worm, pink+white spots
# ============================================================
def gen_flower_boar_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    pink = (0xEE, 0x88, 0x99)
    pink_lt = (0xFF, 0xAA, 0xBB)
    pink_dk = (0xCC, 0x66, 0x77)
    white = (0xFF, 0xEE, 0xEE)
    spot = (0xFF, 0xDD, 0xDD)
    outline = (0xAA, 0x44, 0x55)
    snout = (0xFF, 0x99, 0xAA)
    eye = (0x33, 0x11, 0x11)

    # Body outline (rounded, chubby)
    body_out = [(4, 5), (4, 6), (4, 7), (4, 8), (4, 9), (4, 10),
                (5, 4), (5, 11), (6, 3), (6, 12),
                (7, 3), (7, 12), (8, 3), (8, 12),
                (9, 3), (9, 12), (10, 4), (10, 11),
                (11, 5), (11, 6), (11, 7), (11, 8), (11, 9), (11, 10)]
    for r, c in body_out:
        px(c, r, d, outline)

    # Body fill
    for r in range(5, 11):
        if r == 5 or r == 10:
            cols = range(5, 11)
        else:
            cols = range(4, 12)
        for c in cols:
            if r <= 6 and c <= 5:
                px(c, r, d, pink_lt)
            elif r >= 9 or c >= 10:
                px(c, r, d, pink_dk)
            else:
                px(c, r, d, pink)

    # White flower spots
    spots = [(5, 6), (6, 9), (7, 5), (8, 8), (9, 6), (6, 7)]
    for r, c in spots:
        px(c, r, d, spot)

    # White belly stripe
    for c in range(5, 10):
        px(c, 9, d, white)

    # Highlight
    px(5, 5, d, pink_lt)
    px(6, 5, d, pink_lt)

    # Snout (front/right side)
    px(12, 7, d, snout)
    px(12, 8, d, snout)
    px(13, 7, d, snout)
    px(13, 8, d, snout)
    # Nostrils
    px(13, 7, d, outline)
    px(13, 8, d, outline)

    # Eyes (cute, round)
    px(6, 5, d, eye)
    px(9, 5, d, eye)
    # Eye shine
    px(6, 5, d, (0x33, 0x11, 0x11))

    # Ears (small triangles)
    px(5, 3, d, pink_dk)
    px(5, 4, d, pink)
    px(10, 3, d, pink_dk)
    px(10, 4, d, pink)

    # Small hooves (4 stubby legs)
    for r, c in [(5, 12), (6, 12), (9, 12), (10, 12)]:
        px(c, r, d, outline)

    # Curly tail
    px(5, 2, d, pink_dk)
    px(4, 2, d, pink)
    px(4, 1, d, pink_dk)

    save(img, "flower_boar_gu.png")


# ============================================================
# 7. YELLOW CAMEL BEETLE GU - 黄骆天牛蛊
# Yellow-brown longhorn beetle, long antennae, tough
# ============================================================
def gen_yellow_camel_beetle_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    yellow = (0xCC, 0xAA, 0x44)
    yellow_lt = (0xDD, 0xBB, 0x66)
    yellow_dk = (0xAA, 0x88, 0x33)
    brown = (0x88, 0x66, 0x22)
    outline = (0x55, 0x44, 0x11)
    horn = (0x44, 0x33, 0x11)
    eye = (0x22, 0x11, 0x00)
    leg = (0x66, 0x55, 0x22)

    # Long antennae (curved upward)
    for i in range(6):
        px(4 - i, 3 - i // 2, d, horn)
    for i in range(6):
        px(11 + i, 3 - i // 2, d, horn)

    # Head
    for c in range(6, 10):
        px(c, 4, d, outline)
    for c in range(6, 10):
        px(c, 5, d, yellow_lt)

    # Eyes
    px(6, 5, d, eye)
    px(9, 5, d, eye)

    # Pronotum (thorax shield)
    for c in range(5, 11):
        px(c, 6, d, outline)
    for c in range(5, 11):
        px(c, 7, d, yellow)
    px(5, 7, d, yellow_dk)
    px(10, 7, d, yellow_dk)

    # Elytra (wing covers) - main body
    for r in range(8, 13):
        for c in range(4, 12):
            if c == 4 or c == 11:
                px(c, r, d, outline)
            elif c == 7 or c == 8:
                px(c, r, d, yellow_dk)  # center line
            elif c <= 6:
                px(c, r, d, yellow if r % 2 == 0 else yellow_lt)
            else:
                px(c, r, d, yellow if r % 2 == 1 else yellow_lt)

    # Bottom of elytra
    for c in range(5, 11):
        px(c, 13, d, outline)

    # Elytra ridges
    for r in range(8, 13):
        px(6, r, d, yellow_dk)
        px(9, r, d, yellow_dk)

    # Highlight on elytra
    px(5, 8, d, (0xEE, 0xCC, 0x77))
    px(5, 9, d, (0xEE, 0xCC, 0x77))

    # Legs (3 pairs)
    leg_positions = [(8, 3), (10, 3), (12, 3), (8, 12), (10, 12), (12, 12)]
    for r, c in leg_positions:
        px(c, r, d, leg)
    # Leg joints
    for r, c in [(8, 2), (10, 2), (12, 2), (8, 13), (10, 13), (12, 13)]:
        px(c, r, d, brown)

    save(img, "yellow_camel_beetle_gu.png")


# ============================================================
# 8. STONE SKIN GU - 石皮蛊
# Flat grey worm, stone texture surface, rough
# ============================================================
def gen_stone_skin_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    stone_dk = (0x66, 0x66, 0x66)
    stone = (0x88, 0x88, 0x88)
    stone_lt = (0xAA, 0xAA, 0xAA)
    stone_hi = (0xCC, 0xCC, 0xCC)
    crack = (0x44, 0x44, 0x44)
    outline = (0x33, 0x33, 0x33)
    eye = (0xAA, 0x88, 0x33)

    # Flat, oval body outline
    body_out = [(5, 5), (5, 6), (5, 7), (5, 8), (5, 9), (5, 10),
                (6, 4), (6, 11), (7, 3), (7, 12),
                (8, 3), (8, 12), (9, 3), (9, 12),
                (10, 4), (10, 11),
                (11, 5), (11, 6), (11, 7), (11, 8), (11, 9), (11, 10)]
    for r, c in body_out:
        px(c, r, d, outline)

    # Body fill - rough stone texture
    for r in range(6, 11):
        if r == 6 or r == 10:
            cols = range(5, 11)
        else:
            cols = range(4, 12)
        for c in cols:
            # Pseudo-random stone pattern
            v = ((r * 7 + c * 13) % 4)
            if v == 0:
                px(c, r, d, stone_dk)
            elif v == 1:
                px(c, r, d, stone)
            elif v == 2:
                px(c, r, d, stone_lt)
            else:
                px(c, r, d, stone)

    # Crack lines
    for r, c in [(6, 6), (7, 7), (8, 8), (7, 5), (8, 5), (9, 6),
                 (8, 10), (9, 9), (10, 8)]:
        px(c, r, d, crack)

    # Highlight (top-left)
    px(5, 6, d, stone_hi)
    px(6, 6, d, stone_hi)

    # Eyes (small, amber)
    px(5, 7, d, eye)
    px(10, 7, d, eye)

    # Small stubby legs
    for r, c in [(7, 2), (9, 2), (7, 13), (9, 13)]:
        px(c, r, d, stone_dk)

    save(img, "stone_skin_gu.png")


# ============================================================
# 9. IRON SKIN GU - 铁皮蛊
# Deep grey, metallic sheen, smoother than stone
# ============================================================
def gen_iron_skin_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    iron_dk = (0x55, 0x55, 0x66)
    iron = (0x77, 0x77, 0x88)
    iron_lt = (0x99, 0x99, 0xAA)
    iron_hi = (0xCC, 0xCC, 0xDD)
    sheen = (0xDD, 0xDD, 0xEE)
    outline = (0x33, 0x33, 0x44)
    eye = (0x88, 0xCC, 0xFF)

    # Subtle metallic glow
    draw_glow(img, 32, 32, 20, (0x88, 0x88, 0xAA), 20)

    # Body outline (same shape as stone but slightly rounder)
    body_out = [(5, 5), (5, 6), (5, 7), (5, 8), (5, 9), (5, 10),
                (6, 4), (6, 11), (7, 3), (7, 12),
                (8, 3), (8, 12), (9, 3), (9, 12),
                (10, 4), (10, 11),
                (11, 5), (11, 6), (11, 7), (11, 8), (11, 9), (11, 10)]
    for r, c in body_out:
        px(c, r, d, outline)

    # Body fill - smooth metal
    for r in range(6, 11):
        if r == 6 or r == 10:
            cols = range(5, 11)
        else:
            cols = range(4, 12)
        for c in cols:
            if r <= 7 and c <= 6:
                px(c, r, d, iron_lt)
            elif r >= 9 or c >= 10:
                px(c, r, d, iron_dk)
            else:
                px(c, r, d, iron)

    # Metal sheen line (diagonal highlight)
    for i, (r, c) in enumerate([(6, 5), (6, 6), (7, 6), (7, 7), (7, 8)]):
        px(c, r, d, sheen)

    # Rivets / metal texture dots
    for r, c in [(7, 5), (7, 10), (9, 5), (9, 10)]:
        px(c, r, d, iron_hi)

    # Eyes (blue-white, cold)
    px(5, 7, d, eye)
    px(10, 7, d, eye)

    # Legs
    for r, c in [(7, 2), (9, 2), (7, 13), (9, 13)]:
        px(c, r, d, iron_dk)

    save(img, "iron_skin_gu.png")


# ============================================================
# 10. BEAST SKIN GU - 兽皮蛊
# Furry brown worm, beast fur texture
# ============================================================
def gen_beast_skin_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    fur_dk = (0x5A, 0x3E, 0x1E)
    fur = (0x8B, 0x66, 0x3A)
    fur_lt = (0xAA, 0x82, 0x55)
    fur_hi = (0xCC, 0xAA, 0x77)
    outline = (0x3D, 0x2A, 0x14)
    eye = (0xDD, 0x88, 0x00)
    belly = (0xCC, 0xAA, 0x77)

    # Body outline (wider, muscular)
    body_out = [(4, 5), (4, 6), (4, 7), (4, 8), (4, 9), (4, 10),
                (5, 4), (5, 11), (6, 3), (6, 12),
                (7, 3), (7, 12), (8, 3), (8, 12),
                (9, 3), (9, 12), (10, 3), (10, 12),
                (11, 4), (11, 11),
                (12, 5), (12, 6), (12, 7), (12, 8), (12, 9), (12, 10)]
    for r, c in body_out:
        px(c, r, d, outline)

    # Body fill
    for r in range(5, 12):
        if r == 5 or r == 11:
            cols = range(5, 11)
        else:
            cols = range(4, 12)
        for c in cols:
            if r <= 6:
                px(c, r, d, fur_lt)
            elif r >= 10:
                px(c, r, d, fur_dk)
            else:
                px(c, r, d, fur)

    # Fur texture (short lines sticking up/out)
    fur_tufts = [(3, 5), (3, 7), (3, 9), (4, 4), (4, 11),
                 (6, 2), (8, 2), (10, 2),
                 (6, 13), (8, 13), (10, 13),
                 (13, 6), (13, 8), (13, 10)]
    for r, c in fur_tufts:
        px(c, r, d, fur)

    # Belly (lighter underside)
    for c in range(5, 10):
        px(c, 10, d, belly)
        px(c, 11, d, belly)

    # Highlight
    px(5, 5, d, fur_hi)
    px(6, 5, d, fur_hi)

    # Eyes (wild, amber)
    px(5, 6, d, eye)
    px(10, 6, d, eye)

    # Small sturdy legs
    for r, c in [(7, 2), (9, 2), (11, 3), (7, 13), (9, 13), (11, 12)]:
        px(c, r, d, outline)

    save(img, "beast_skin_gu.png")


# ============================================================
# 11. BLACK BRISTLE GU - 黑鬃蛊
# Black body, covered in sharp black bristles
# ============================================================
def gen_black_bristle_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    black = (0x22, 0x22, 0x22)
    dark = (0x33, 0x33, 0x33)
    body = (0x44, 0x44, 0x44)
    body_lt = (0x55, 0x55, 0x55)
    bristle = (0x11, 0x11, 0x11)
    bristle_tip = (0x33, 0x33, 0x44)
    outline = (0x11, 0x11, 0x11)
    eye = (0xCC, 0x00, 0x00)

    # Body outline
    body_out = [(5, 5), (5, 6), (5, 7), (5, 8), (5, 9), (5, 10),
                (6, 4), (6, 11), (7, 4), (7, 11),
                (8, 4), (8, 11), (9, 4), (9, 11),
                (10, 4), (10, 11),
                (11, 5), (11, 6), (11, 7), (11, 8), (11, 9), (11, 10)]
    for r, c in body_out:
        px(c, r, d, outline)

    # Body fill
    for r in range(6, 11):
        if r == 6 or r == 10:
            cols = range(5, 11)
        else:
            cols = range(5, 11)
        for c in cols:
            if r <= 7:
                px(c, r, d, body_lt)
            else:
                px(c, r, d, body)

    # Bristles extending from body (many sharp spikes)
    # Top bristles
    for c in [5, 6, 7, 8, 9, 10]:
        px(c, 4, d, bristle)
        px(c, 3, d, bristle_tip)
    # Left bristles
    for r in [6, 7, 8, 9, 10]:
        px(3, r, d, bristle)
        if r % 2 == 0:
            px(2, r, d, bristle_tip)
    # Right bristles
    for r in [6, 7, 8, 9, 10]:
        px(12, r, d, bristle)
        if r % 2 == 1:
            px(13, r, d, bristle_tip)
    # Bottom bristles
    for c in [5, 6, 7, 8, 9, 10]:
        px(c, 12, d, bristle)
        if c % 2 == 0:
            px(c, 13, d, bristle_tip)

    # Extra long top bristles
    px(6, 2, d, bristle)
    px(9, 2, d, bristle)

    # Eyes (red, menacing through bristles)
    px(6, 7, d, eye)
    px(9, 7, d, eye)

    # Body highlight
    px(7, 6, d, (0x66, 0x66, 0x66))

    save(img, "black_bristle_gu.png")


# ============================================================
# 12. STEEL BRISTLE GU - 钢鬃蛊
# Silver body, steel needle bristles, metallic reflection
# ============================================================
def gen_steel_bristle_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    steel = (0xAA, 0xAA, 0xBB)
    steel_dk = (0x88, 0x88, 0x99)
    steel_lt = (0xCC, 0xCC, 0xDD)
    steel_hi = (0xEE, 0xEE, 0xFF)
    bristle = (0x99, 0x99, 0xAA)
    bristle_tip = (0xDD, 0xDD, 0xEE)
    outline = (0x55, 0x55, 0x66)
    eye = (0x44, 0xBB, 0xFF)

    # Metallic glow
    draw_glow(img, 32, 32, 22, (0xAA, 0xAA, 0xCC), 25)

    # Body outline
    body_out = [(5, 5), (5, 6), (5, 7), (5, 8), (5, 9), (5, 10),
                (6, 4), (6, 11), (7, 4), (7, 11),
                (8, 4), (8, 11), (9, 4), (9, 11),
                (10, 4), (10, 11),
                (11, 5), (11, 6), (11, 7), (11, 8), (11, 9), (11, 10)]
    for r, c in body_out:
        px(c, r, d, outline)

    # Body fill - polished steel
    for r in range(6, 11):
        cols = range(5, 11)
        for c in cols:
            if r <= 7 and c <= 6:
                px(c, r, d, steel_lt)
            elif r >= 9 or c >= 10:
                px(c, r, d, steel_dk)
            else:
                px(c, r, d, steel)

    # Steel bristles (sharp, metallic)
    # Top
    for c in [5, 6, 7, 8, 9, 10]:
        px(c, 4, d, bristle)
        px(c, 3, d, bristle_tip)
    # Left
    for r in [6, 7, 8, 9, 10]:
        px(3, r, d, bristle)
        px(2, r, d, bristle_tip)
    # Right
    for r in [6, 7, 8, 9, 10]:
        px(12, r, d, bristle)
        px(13, r, d, bristle_tip)
    # Bottom
    for c in [5, 6, 7, 8, 9, 10]:
        px(c, 12, d, bristle)
        px(c, 13, d, bristle_tip)

    # Extra long top spines
    px(6, 1, d, steel_hi)
    px(6, 2, d, bristle_tip)
    px(9, 1, d, steel_hi)
    px(9, 2, d, bristle_tip)

    # Metal sheen on body
    px(6, 6, d, steel_hi)
    px(7, 6, d, steel_hi)
    px(7, 7, d, steel_hi)

    # Eyes (ice blue)
    px(6, 7, d, eye)
    px(9, 7, d, eye)

    save(img, "steel_bristle_gu.png")


# ============================================================
# 13. HEAVEN CANOPY GU - 天蓬蛊
# Golden beetle, jewel-like surface, glowing
# ============================================================
def gen_heaven_canopy_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    gold = (0xDD, 0xAA, 0x33)
    gold_lt = (0xEE, 0xCC, 0x55)
    gold_dk = (0xBB, 0x88, 0x22)
    gold_hi = (0xFF, 0xEE, 0x88)
    gem_green = (0x33, 0xCC, 0x66)
    gem_blue = (0x33, 0x88, 0xCC)
    gem_red = (0xCC, 0x33, 0x44)
    outline = (0x88, 0x66, 0x11)
    white = (0xFF, 0xFF, 0xEE)

    # Royal golden glow
    draw_glow(img, 32, 32, 26, (0xFF, 0xDD, 0x44), 40)
    draw_glow(img, 32, 32, 18, (0xFF, 0xCC, 0x33), 30)

    # Shell outline (shield-shaped, ornate)
    body_out = [(3, 6), (3, 7), (3, 8), (3, 9),
                (4, 5), (4, 10), (5, 4), (5, 11),
                (6, 3), (6, 12), (7, 3), (7, 12),
                (8, 3), (8, 12), (9, 3), (9, 12),
                (10, 3), (10, 12), (11, 4), (11, 11),
                (12, 5), (12, 10), (13, 6), (13, 7), (13, 8), (13, 9)]
    for r, c in body_out:
        px(c, r, d, outline)

    # Shell fill - rich gold gradient
    for r in range(4, 13):
        if r == 4 or r == 12:
            cols = range(6, 10)
        elif r == 5 or r == 11:
            cols = range(5, 11)
        else:
            cols = range(4, 12)
        for c in cols:
            if r <= 5 and c <= 7:
                px(c, r, d, gold_hi)
            elif r >= 11 or c >= 11:
                px(c, r, d, gold_dk)
            elif (r + c) % 3 == 0:
                px(c, r, d, gold_lt)
            else:
                px(c, r, d, gold)

    # Center gem (main jewel)
    px(7, 7, d, gem_green)
    px(8, 7, d, gem_green)
    px(7, 8, d, gem_green)
    px(8, 8, d, gem_green)
    px(7, 7, d, (0x66, 0xEE, 0x88))  # gem highlight

    # Smaller jewels arranged around center
    px(6, 5, d, gem_blue)
    px(9, 5, d, gem_blue)
    px(5, 8, d, gem_red)
    px(10, 8, d, gem_red)
    px(6, 11, d, gem_blue)
    px(9, 11, d, gem_blue)

    # Golden filigree lines
    for r in [6, 10]:
        for c in range(5, 11):
            if (c + r) % 2 == 0:
                px(c, r, d, gold_hi)

    # Head (small, at top)
    px(7, 3, d, gold_lt)
    px(8, 3, d, gold_lt)

    # White sparkles
    for sx, sy in [(5, 4), (10, 4), (4, 9), (11, 9)]:
        px(sx, sy, d, white)

    # Legs (small, golden)
    for r, c in [(7, 2), (9, 2), (11, 3), (7, 13), (9, 13), (11, 12)]:
        px(c, r, d, gold_dk)

    save(img, "heaven_canopy_gu.png")


# ============================================================
# 14. VITALITY GRASS GU - 九叶生机草蛊
# Green plant-form gu, 9 leaves, vibrant life energy
# ============================================================
def gen_vitality_grass_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    green = (0x33, 0xAA, 0x44)
    green_lt = (0x55, 0xCC, 0x66)
    green_dk = (0x22, 0x88, 0x33)
    green_hi = (0x88, 0xEE, 0x88)
    stem = (0x44, 0x77, 0x33)
    stem_dk = (0x33, 0x55, 0x22)
    glow = (0x66, 0xFF, 0x66)

    # Life energy glow
    draw_glow(img, 32, 32, 24, (0x44, 0xFF, 0x44), 35)
    draw_glow(img, 32, 30, 16, (0x88, 0xFF, 0x88), 25)

    # Central stem
    for r in range(6, 14):
        px(7, r, d, stem)
        px(8, r, d, stem_dk)

    # Root base
    px(6, 14, d, stem_dk)
    px(7, 14, d, stem)
    px(8, 14, d, stem)
    px(9, 14, d, stem_dk)
    px(6, 15, d, stem_dk)
    px(9, 15, d, stem_dk)

    # 9 leaves arranged around stem
    # Leaf drawing helper: each leaf is 2-3 pixels
    leaves = [
        # (row, col, color) - 9 leaves arranged symmetrically
        # Top leaf (1)
        [(3, 7, green_lt), (3, 8, green_lt), (2, 7, green_hi), (2, 8, green)],
        # Upper left (2)
        [(4, 5, green_lt), (5, 5, green), (4, 4, green_hi), (5, 4, green_dk)],
        # Upper right (3)
        [(4, 10, green_lt), (5, 10, green), (4, 11, green_hi), (5, 11, green_dk)],
        # Mid left upper (4)
        [(6, 4, green), (7, 4, green_dk), (6, 3, green_lt), (7, 3, green)],
        # Mid right upper (5)
        [(6, 11, green), (7, 11, green_dk), (6, 12, green_lt), (7, 12, green)],
        # Mid left lower (6)
        [(9, 4, green), (9, 5, green_dk), (9, 3, green_lt), (10, 4, green_dk)],
        # Mid right lower (7)
        [(9, 10, green), (9, 11, green_dk), (9, 12, green_lt), (10, 11, green_dk)],
        # Lower left (8)
        [(11, 5, green), (12, 5, green_dk), (11, 4, green_lt), (12, 4, green_dk)],
        # Lower right (9)
        [(11, 10, green), (12, 10, green_dk), (11, 11, green_lt), (12, 11, green_dk)],
    ]

    for leaf in leaves:
        for r, c, color in leaf:
            px(c, r, d, color)

    # Leaf vein highlights
    for r, c in [(3, 7), (4, 5), (4, 10), (6, 4), (6, 11)]:
        px(c, r, d, green_hi)

    # Life sparkles
    sparkle_pos = [(5, 7), (8, 6), (8, 9), (11, 7)]
    for r, c in sparkle_pos:
        px(c, r, d, (0xAA, 0xFF, 0xAA, 200))

    save(img, "vitality_grass_gu.png")


# ============================================================
# 15. VITALITY LEAF GU - 生机叶蛊
# Single vivid green leaf shape, clear veins, glowing
# ============================================================
def gen_vitality_leaf_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    green = (0x33, 0xBB, 0x55)
    green_lt = (0x55, 0xDD, 0x77)
    green_dk = (0x22, 0x88, 0x33)
    green_hi = (0x88, 0xFF, 0x99)
    vein = (0x22, 0x77, 0x33)
    stem_c = (0x55, 0x88, 0x33)
    outline = (0x11, 0x66, 0x22)

    # Soft green glow
    draw_glow(img, 32, 30, 22, (0x44, 0xFF, 0x66), 30)

    # Leaf shape (larger single leaf, pointed tip at top)
    # Outline
    leaf_outline = [
        (2, 7), (2, 8),  # tip
        (3, 6), (3, 9),
        (4, 5), (4, 10),
        (5, 4), (5, 11),
        (6, 3), (6, 12),
        (7, 3), (7, 12),
        (8, 3), (8, 12),
        (9, 3), (9, 12),
        (10, 4), (10, 11),
        (11, 5), (11, 10),
        (12, 6), (12, 9),
        (13, 7), (13, 8),  # base
    ]
    for r, c in leaf_outline:
        px(c, r, d, outline)

    # Leaf fill
    for r in range(3, 13):
        if r == 3 or r == 12:
            cols = range(7, 9)
        elif r == 4 or r == 11:
            cols = range(6, 10)
        elif r == 5 or r == 10:
            cols = range(5, 11)
        else:
            cols = range(4, 12)
        for c in cols:
            if r <= 5 and c <= 6:
                px(c, r, d, green_lt)
            elif r >= 10 or c >= 10:
                px(c, r, d, green_dk)
            else:
                px(c, r, d, green)

    # Central vein (midrib)
    for r in range(3, 13):
        if r <= 5:
            px(7, r, d, vein)
        else:
            px(7, r, d, vein)
            px(8, r, d, vein)

    # Side veins (branching from midrib)
    side_veins = [(4, 6), (5, 5), (6, 5), (7, 4), (8, 5), (9, 5), (10, 6),
                  (4, 9), (5, 10), (6, 10), (7, 11), (8, 10), (9, 10), (10, 9)]
    for r, c in side_veins:
        px(c, r, d, vein)

    # Highlight shimmer
    px(5, 6, d, green_hi)
    px(6, 6, d, green_hi)
    px(6, 7, d, green_hi)

    # Stem at bottom
    px(7, 13, d, stem_c)
    px(7, 14, d, stem_c)
    px(8, 14, d, stem_c)
    px(8, 15, d, stem_c)

    # Glow sparkles
    for r, c in [(3, 7), (6, 5), (6, 10), (9, 6), (9, 9)]:
        px(c, r, d, (0xAA, 0xFF, 0xBB, 180))

    save(img, "vitality_leaf_gu.png")


# ============================================================
# 16. WATER SPIDER GU - 水蛛蛊
# Translucent blue spider, water droplet texture
# ============================================================
def gen_water_spider_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    blue = (0x44, 0x88, 0xCC, 200)
    blue_lt = (0x66, 0xAA, 0xEE, 180)
    blue_dk = (0x22, 0x66, 0xAA, 220)
    blue_hi = (0xAA, 0xDD, 0xFF, 200)
    outline = (0x22, 0x55, 0x88, 230)
    eye = (0xFF, 0xFF, 0xFF, 255)
    drop = (0x88, 0xCC, 0xFF, 150)
    bubble = (0xBB, 0xEE, 0xFF, 120)

    # Water glow
    draw_glow(img, 32, 32, 22, (0x44, 0x88, 0xDD), 30)

    # Spider body - cephalothorax
    for r, c in [(6, 6), (6, 7), (6, 8), (6, 9),
                 (7, 5), (7, 10), (8, 5), (8, 10),
                 (9, 6), (9, 7), (9, 8), (9, 9)]:
        px(c, r, d, outline)
    for r in range(7, 9):
        for c in range(6, 10):
            px(c, r, d, blue)

    # Highlight on cephalothorax
    px(6, 7, d, blue_hi)
    px(7, 7, d, blue_lt)

    # Abdomen (larger, rounded)
    for r, c in [(10, 5), (10, 6), (10, 7), (10, 8), (10, 9), (10, 10),
                 (11, 4), (11, 11), (12, 4), (12, 11),
                 (13, 5), (13, 6), (13, 7), (13, 8), (13, 9), (13, 10)]:
        px(c, r, d, outline)
    for r in range(11, 13):
        for c in range(5, 11):
            px(c, r, d, blue)
    # Abdomen highlight
    px(6, 11, d, blue_hi)
    px(7, 11, d, blue_lt)

    # Water droplet markings on abdomen
    px(7, 12, d, drop)
    px(8, 12, d, drop)
    px(7, 11, d, (0xAA, 0xDD, 0xFF, 160))

    # Eyes (8 spider eyes, simplified to 2 main)
    px(6, 7, d, eye)
    px(9, 7, d, eye)

    # 8 legs (4 per side, curved)
    # Left legs
    leg_l = [(6, 4), (5, 3), (4, 2),  # front
             (7, 4), (7, 3), (6, 2),
             (8, 4), (8, 3), (9, 2),
             (9, 4), (10, 3), (11, 2)]
    for r, c in leg_l:
        px(c, r, d, blue_dk)

    # Right legs
    leg_r = [(6, 11), (5, 12), (4, 13),
             (7, 11), (7, 12), (6, 13),
             (8, 11), (8, 12), (9, 13),
             (9, 11), (10, 12), (11, 13)]
    for r, c in leg_r:
        px(c, r, d, blue_dk)

    # Bubble decorations
    for bx, by in [(3, 4), (12, 3), (3, 11), (13, 12)]:
        px(bx, by, d, bubble)

    save(img, "water_spider_gu.png")


# ============================================================
# 17. SIGNAL GU - 信号蛊
# Red+yellow glowing worm, like a firework, glowing antennae
# ============================================================
def gen_signal_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    red = (0xDD, 0x33, 0x22)
    red_lt = (0xFF, 0x55, 0x33)
    yellow = (0xFF, 0xCC, 0x33)
    yellow_lt = (0xFF, 0xEE, 0x66)
    orange = (0xFF, 0x88, 0x22)
    outline = (0x99, 0x22, 0x11)
    white = (0xFF, 0xFF, 0xDD)

    # Bright glow
    draw_glow(img, 32, 28, 24, (0xFF, 0x66, 0x22), 50)
    draw_glow(img, 32, 28, 14, (0xFF, 0xCC, 0x33), 35)

    # Glowing antennae (like firework sparks)
    # Left antenna
    for i, (r, c) in enumerate([(3, 5), (2, 4), (1, 3)]):
        px(c, r, d, yellow_lt if i < 2 else white)
    # Right antenna
    for i, (r, c) in enumerate([(3, 10), (2, 11), (1, 12)]):
        px(c, r, d, yellow_lt if i < 2 else white)

    # Spark tips
    px(3, 0, d, white)
    px(12, 0, d, white)

    # Body - elongated oval
    body_out = [(4, 6), (4, 7), (4, 8), (4, 9),
                (5, 5), (5, 10), (6, 5), (6, 10),
                (7, 5), (7, 10), (8, 5), (8, 10),
                (9, 5), (9, 10), (10, 5), (10, 10),
                (11, 6), (11, 7), (11, 8), (11, 9)]
    for r, c in body_out:
        px(c, r, d, outline)

    # Body fill - red/yellow gradient
    for r in range(5, 11):
        for c in range(6, 10):
            if r <= 6:
                px(c, r, d, yellow)
            elif r <= 8:
                px(c, r, d, orange)
            else:
                px(c, r, d, red)

    # Body segments
    for c in range(6, 10):
        px(c, 7, d, yellow_lt)
        px(c, 9, d, red_lt)

    # Center bright spot
    px(7, 6, d, white)
    px(8, 6, d, white)

    # Spark emission particles
    sparks = [(3, 3), (12, 3), (2, 7), (13, 8),
              (4, 12), (11, 12), (1, 6), (14, 7)]
    for c, r in sparks:
        px(c, r, d, (0xFF, 0xEE, 0x44, 180))

    # Tail glow
    px(7, 12, d, orange)
    px(8, 12, d, orange)
    px(7, 13, d, (0xFF, 0x66, 0x22, 180))

    save(img, "signal_gu.png")


# ============================================================
# 18. FLASH GU - 闪光蛊
# Pure white glowing body, brightest at center
# ============================================================
def gen_flash_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    white = (0xFF, 0xFF, 0xFF)
    near_white = (0xEE, 0xEE, 0xFF)
    light_yellow = (0xFF, 0xFF, 0xCC)
    pale_blue = (0xDD, 0xDD, 0xFF)
    outline = (0xCC, 0xCC, 0xDD)
    body_c = (0xEE, 0xEE, 0xFF)

    # Intense multi-layer central glow
    draw_glow(img, 32, 32, 30, (0xFF, 0xFF, 0xDD), 70)
    draw_glow(img, 32, 32, 24, (0xFF, 0xFF, 0xEE), 60)
    draw_glow(img, 32, 32, 16, (0xFF, 0xFF, 0xFF), 50)

    # 4 main light rays (thick cross)
    for dist in range(1, 14):
        alpha = max(0, 220 - dist * 14)
        width = max(1, 3 - dist // 4)
        for w in range(-width, width + 1):
            # Horizontal rays
            blend_pixel(img, 32 + dist * 2, 32 + w, (255, 255, 240, alpha))
            blend_pixel(img, 32 - dist * 2, 32 + w, (255, 255, 240, alpha))
            # Vertical rays
            blend_pixel(img, 32 + w, 32 + dist * 2, (255, 255, 240, alpha))
            blend_pixel(img, 32 + w, 32 - dist * 2, (255, 255, 240, alpha))
        # Diagonal rays (thinner)
        blend_pixel(img, 32 + dist, 32 + dist, (255, 255, 220, alpha * 2 // 3))
        blend_pixel(img, 32 - dist, 32 - dist, (255, 255, 220, alpha * 2 // 3))
        blend_pixel(img, 32 + dist, 32 - dist, (255, 255, 220, alpha * 2 // 3))
        blend_pixel(img, 32 - dist, 32 + dist, (255, 255, 220, alpha * 2 // 3))

    # Worm body (oval, visible but bright)
    body_out = [(5, 6), (5, 7), (5, 8), (5, 9),
                (6, 5), (6, 10), (7, 5), (7, 10),
                (8, 5), (8, 10), (9, 5), (9, 10),
                (10, 6), (10, 7), (10, 8), (10, 9)]
    for r, c in body_out:
        px(c, r, d, outline)

    for r in range(6, 10):
        for c in range(6, 10):
            px(c, r, d, white)

    # Highlight core (brightest center)
    px(7, 7, d, light_yellow)
    px(8, 7, d, light_yellow)
    px(7, 8, d, light_yellow)
    px(8, 8, d, light_yellow)

    # Eyes (barely visible, pale blue)
    px(6, 7, d, pale_blue)
    px(9, 7, d, pale_blue)

    # Concentric glow rings
    for radius in [10, 16, 22]:
        for angle in range(0, 360, 3):
            rad = math.radians(angle)
            x = int(32 + math.cos(rad) * radius)
            y = int(32 + math.sin(rad) * radius)
            alpha = 150 - radius * 5
            if alpha > 0:
                blend_pixel(img, x, y, (255, 255, 240, alpha))

    # Sparkle bursts at ray tips
    for sx, sy in [(4, 32), (60, 32), (32, 4), (32, 60),
                   (16, 16), (48, 16), (16, 48), (48, 48)]:
        if 0 <= sx < SIZE and 0 <= sy < SIZE:
            blend_pixel(img, sx, sy, (255, 255, 255, 200))
            for dx, dy in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
                blend_pixel(img, sx + dx, sy + dy, (255, 255, 220, 120))

    save(img, "flash_gu.png")


# ============================================================
# 19. SHADOW FOLLOWER GU - 幽影随行蛊
# Dark purple, translucent, shadow-like, blurry
# ============================================================
def gen_shadow_follower_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    shadow = (0x33, 0x22, 0x44, 160)
    shadow_dk = (0x22, 0x11, 0x33, 200)
    shadow_lt = (0x55, 0x33, 0x66, 140)
    purple = (0x66, 0x44, 0x88, 180)
    eye = (0xBB, 0x55, 0xFF, 220)
    wisp = (0x44, 0x22, 0x55, 100)

    # Shadow aura (dark, diffuse)
    draw_glow(img, 32, 32, 26, (0x33, 0x11, 0x44), 40)
    draw_glow(img, 30, 34, 18, (0x44, 0x22, 0x55), 25)

    # Wisps of shadow extending out
    for angle in range(0, 360, 40):
        rad = math.radians(angle)
        for dist in range(14, 22):
            x = int(32 + math.cos(rad + dist * 0.1) * dist)
            y = int(32 + math.sin(rad + dist * 0.1) * dist)
            alpha = int(60 * (1 - (dist - 14) / 8.0))
            blend_pixel(img, x, y, (0x33, 0x11, 0x44, alpha))

    # Body - amorphous, blurry shape
    for r in range(5, 12):
        if r == 5 or r == 11:
            cols = range(6, 10)
        elif r == 6 or r == 10:
            cols = range(5, 11)
        else:
            cols = range(5, 11)
        for c in cols:
            px(c, r, d, shadow)

    # Darker core
    for r in range(7, 10):
        for c in range(6, 10):
            px(c, r, d, shadow_dk)

    # Lighter edges (translucent)
    edge_pixels = [(5, 6), (5, 7), (5, 8), (5, 9), (11, 6), (11, 7), (11, 8), (11, 9)]
    for r, c in edge_pixels:
        px(c, r, d, shadow_lt)

    # Purple highlights
    px(6, 7, d, purple)
    px(7, 8, d, purple)

    # Glowing eyes (only visible part)
    px(6, 7, d, eye)
    px(9, 7, d, eye)

    # Shadow tendrils extending down and sides
    for r, c in [(12, 6), (13, 5), (12, 9), (13, 10),
                 (7, 3), (8, 3), (7, 12), (8, 12)]:
        px(c, r, d, wisp)

    save(img, "shadow_follower_gu.png")


# ============================================================
# 20. DRAGON CRICKET GU - 龙丸蛐蛐蛊
# Deep green cricket, small dragon horns, lively
# ============================================================
def gen_dragon_cricket_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    green = (0x22, 0x77, 0x33)
    green_lt = (0x44, 0x99, 0x55)
    green_dk = (0x11, 0x55, 0x22)
    green_hi = (0x66, 0xBB, 0x77)
    horn = (0xCC, 0xAA, 0x33)
    horn_dk = (0xAA, 0x88, 0x22)
    outline = (0x11, 0x44, 0x11)
    eye = (0xFF, 0x88, 0x00)
    leg = (0x33, 0x66, 0x33)

    # Head with dragon horns
    # Horns
    px(5, 2, d, horn)
    px(5, 1, d, horn)
    px(4, 1, d, (0xDD, 0xBB, 0x44))
    px(10, 2, d, horn)
    px(10, 1, d, horn)
    px(11, 1, d, (0xDD, 0xBB, 0x44))

    # Head
    for c in range(6, 10):
        px(c, 3, d, outline)
    for c in range(5, 11):
        px(c, 4, d, green_lt)
    px(5, 4, d, outline)
    px(10, 4, d, outline)

    # Eyes
    px(6, 4, d, eye)
    px(9, 4, d, eye)

    # Long antennae
    px(4, 3, d, green_dk)
    px(3, 2, d, green_dk)
    px(11, 3, d, green_dk)
    px(12, 2, d, green_dk)

    # Thorax
    for c in range(5, 11):
        px(c, 5, d, green)
    px(4, 5, d, outline)
    px(11, 5, d, outline)

    # Abdomen (elongated)
    for r in range(6, 11):
        for c in range(5, 11):
            if c == 5 or c == 10:
                px(c, r, d, outline)
            elif r <= 7:
                px(c, r, d, green_lt)
            elif r >= 9:
                px(c, r, d, green_dk)
            else:
                px(c, r, d, green)

    # Bottom
    for c in range(6, 10):
        px(c, 11, d, outline)

    # Wing covers (slight pattern)
    px(6, 7, d, green_hi)
    px(9, 7, d, green_hi)
    px(7, 8, d, green_hi)
    px(8, 8, d, green_hi)

    # Segment lines
    for c in range(6, 10):
        px(c, 8, d, green_dk)
        px(c, 10, d, green_dk)

    # Cricket jumping legs (large hind legs)
    # Left hind leg
    px(4, 9, d, leg)
    px(3, 10, d, leg)
    px(2, 11, d, leg)
    px(1, 12, d, leg)
    px(2, 12, d, leg)
    px(3, 11, d, leg)
    # Right hind leg
    px(11, 9, d, leg)
    px(12, 10, d, leg)
    px(13, 11, d, leg)
    px(14, 12, d, leg)
    px(13, 12, d, leg)
    px(12, 11, d, leg)

    # Front/mid legs (smaller)
    px(4, 6, d, leg)
    px(3, 6, d, leg)
    px(11, 6, d, leg)
    px(12, 6, d, leg)
    px(4, 8, d, leg)
    px(3, 8, d, leg)
    px(11, 8, d, leg)
    px(12, 8, d, leg)

    # Dragon scale spots
    px(7, 6, d, horn_dk)
    px(8, 9, d, horn_dk)

    save(img, "dragon_cricket_gu.png")


# ============================================================
# 21. QUIET STEP GU - 悄步蛊
# Pale grey, almost invisible, soft and translucent
# ============================================================
def gen_quiet_step_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    pale = (0xCC, 0xCC, 0xCC, 140)
    pale_lt = (0xDD, 0xDD, 0xDD, 120)
    pale_dk = (0xAA, 0xAA, 0xAA, 160)
    outline = (0x99, 0x99, 0x99, 180)
    eye = (0xBB, 0xBB, 0xDD, 220)
    ghost = (0xCC, 0xCC, 0xDD, 90)

    # Very subtle presence glow
    draw_glow(img, 32, 32, 18, (0xCC, 0xCC, 0xDD), 15)

    # Body - soft, nearly invisible worm
    body_out = [(5, 6), (5, 7), (5, 8), (5, 9),
                (6, 5), (6, 10), (7, 5), (7, 10),
                (8, 5), (8, 10), (9, 5), (9, 10),
                (10, 5), (10, 10),
                (11, 6), (11, 7), (11, 8), (11, 9)]
    for r, c in body_out:
        px(c, r, d, outline)

    # Body fill - very translucent
    for r in range(6, 11):
        for c in range(6, 10):
            px(c, r, d, pale)

    # Lighter center
    px(7, 7, d, pale_lt)
    px(8, 7, d, pale_lt)
    px(7, 8, d, pale_lt)
    px(8, 8, d, pale_lt)

    # Ghost-like lower extension (fading out)
    for r, c in [(12, 7), (12, 8), (13, 7)]:
        px(c, r, d, ghost)

    # Barely visible eyes
    px(6, 7, d, eye)
    px(9, 7, d, eye)

    # Soft legs
    for r, c in [(7, 4), (9, 4), (7, 11), (9, 11)]:
        px(c, r, d, ghost)

    # Footprint hint (below body)
    px(6, 13, d, (0xBB, 0xBB, 0xBB, 50))
    px(7, 13, d, (0xBB, 0xBB, 0xBB, 50))
    px(8, 13, d, (0xBB, 0xBB, 0xBB, 50))
    px(9, 13, d, (0xBB, 0xBB, 0xBB, 50))

    save(img, "quiet_step_gu.png")


# ============================================================
# 22. SCENT LOCK GU - 锁气蛊
# Pale purple, closed/sealed form, like a small pouch
# ============================================================
def gen_scent_lock_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    purple = (0x99, 0x77, 0xBB)
    purple_lt = (0xBB, 0x99, 0xDD)
    purple_dk = (0x77, 0x55, 0x99)
    purple_hi = (0xDD, 0xBB, 0xFF)
    seal = (0xDD, 0xAA, 0x55)
    seal_dk = (0xBB, 0x88, 0x33)
    outline = (0x55, 0x33, 0x77)
    string = (0xCC, 0x99, 0x44)

    # Drawstring/seal at top
    px(6, 3, d, string)
    px(7, 2, d, string)
    px(8, 2, d, string)
    px(9, 3, d, string)
    # Knot
    px(7, 1, d, seal)
    px(8, 1, d, seal)
    px(7, 0, d, seal_dk)
    px(8, 0, d, seal_dk)

    # Neck (cinched area)
    px(6, 4, d, outline)
    px(7, 4, d, purple_dk)
    px(8, 4, d, purple_dk)
    px(9, 4, d, outline)

    # Pouch body outline
    body_out = [(5, 5), (5, 10),
                (6, 4), (6, 11),
                (7, 3), (7, 12),
                (8, 3), (8, 12),
                (9, 3), (9, 12),
                (10, 3), (10, 12),
                (11, 4), (11, 11),
                (12, 5), (12, 6), (12, 7), (12, 8), (12, 9), (12, 10)]
    for r, c in body_out:
        px(c, r, d, outline)

    # Pouch fill
    for r in range(5, 12):
        if r == 5:
            cols = range(6, 10)
        elif r == 6 or r == 11:
            cols = range(5, 11)
        else:
            cols = range(4, 12)
        for c in cols:
            if r <= 7 and c <= 6:
                px(c, r, d, purple_lt)
            elif r >= 10 or c >= 10:
                px(c, r, d, purple_dk)
            else:
                px(c, r, d, purple)

    # Seal mark on pouch (centered emblem)
    px(7, 8, d, seal)
    px(8, 8, d, seal)
    px(7, 9, d, seal_dk)
    px(8, 9, d, seal_dk)
    # Seal cross
    px(6, 8, d, seal_dk)
    px(9, 8, d, seal_dk)
    px(7, 7, d, seal_dk)
    px(8, 10, d, seal_dk)

    # Highlight
    px(5, 6, d, purple_hi)
    px(6, 6, d, purple_hi)

    # Wrinkle lines
    for r, c in [(7, 5), (9, 6), (10, 8)]:
        px(c, r, d, purple_dk)

    # Subtle gas wisps escaping (almost sealed)
    px(6, 3, d, (0xCC, 0xBB, 0xDD, 80))
    px(5, 2, d, (0xCC, 0xBB, 0xDD, 50))
    px(10, 3, d, (0xCC, 0xBB, 0xDD, 80))
    px(11, 2, d, (0xCC, 0xBB, 0xDD, 50))

    save(img, "scent_lock_gu.png")


# ============================================================
# 23. LOVE SEPARATION GU - 爱别离蛊
# Purple-black + dark red, heart with crack, eerie
# ============================================================
def gen_love_separation_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    dark_purple = (0x44, 0x22, 0x55)
    dark_red = (0x88, 0x22, 0x33)
    red = (0xAA, 0x33, 0x44)
    purple = (0x66, 0x33, 0x77)
    crack = (0x11, 0x00, 0x11)
    outline = (0x22, 0x11, 0x22)
    eye_l = (0xCC, 0x44, 0x66)
    eye_r = (0x88, 0x33, 0xAA)
    drip = (0x66, 0x11, 0x22)

    # Eerie dark aura
    draw_glow(img, 32, 32, 24, (0x44, 0x11, 0x33), 35)
    draw_glow(img, 28, 28, 14, (0x88, 0x22, 0x44), 20)
    draw_glow(img, 36, 28, 14, (0x55, 0x22, 0x77), 20)

    # Heart shape outline (cracked)
    # Top left lobe
    heart_out_l = [(3, 4), (3, 5), (3, 6),
                   (4, 3), (4, 7),
                   (5, 3), (5, 7),
                   (6, 3), (7, 4)]
    for r, c in heart_out_l:
        px(c, r, d, outline)

    # Top right lobe
    heart_out_r = [(3, 9), (3, 10), (3, 11),
                   (4, 8), (4, 12),
                   (5, 8), (5, 12),
                   (6, 12), (7, 11)]
    for r, c in heart_out_r:
        px(c, r, d, outline)

    # Bottom of heart (comes to a point)
    heart_out_b = [(7, 4), (7, 11),
                   (8, 4), (8, 11),
                   (9, 5), (9, 10),
                   (10, 5), (10, 10),
                   (11, 6), (11, 9),
                   (12, 7), (12, 8)]
    for r, c in heart_out_b:
        px(c, r, d, outline)

    # Heart fill - left lobe (dark red)
    for r in range(4, 8):
        if r == 4:
            cols = range(4, 7)
        elif r <= 6:
            cols = range(4, 7)
        else:
            cols = range(5, 7)
        for c in cols:
            px(c, r, d, dark_red)

    # Heart fill - right lobe (dark purple)
    for r in range(4, 8):
        if r == 4:
            cols = range(9, 12)
        elif r <= 6:
            cols = range(9, 12)
        else:
            cols = range(9, 11)
        for c in cols:
            px(c, r, d, dark_purple)

    # Heart fill - lower merge area
    for r in range(7, 12):
        if r == 7:
            cols = range(5, 11)
        elif r == 8:
            cols = range(5, 11)
        elif r == 9:
            cols = range(6, 10)
        elif r == 10:
            cols = range(6, 10)
        elif r == 11:
            cols = range(7, 9)
        else:
            cols = []
        for c in cols:
            if c <= 7:
                px(c, r, d, dark_red if r % 2 == 0 else red)
            else:
                px(c, r, d, purple if r % 2 == 0 else dark_purple)

    # CRACK through the center (jagged vertical line)
    crack_path = [(4, 7), (5, 7), (5, 8), (6, 7), (6, 8),
                  (7, 7), (7, 8), (8, 7), (8, 8),
                  (9, 7), (9, 8), (10, 7), (10, 8), (11, 7)]
    for r, c in crack_path:
        px(c, r, d, crack)

    # Crack branches
    px(6, 6, d, crack)
    px(8, 9, d, crack)
    px(5, 9, d, crack)
    px(9, 6, d, crack)

    # Eyes (one on each side, different colors)
    px(5, 5, d, eye_l)
    px(10, 5, d, eye_r)

    # Dark drips from bottom
    px(7, 13, d, drip)
    px(8, 13, d, drip)
    px(7, 14, d, (0x44, 0x00, 0x11, 180))

    # Ghostly particles
    for sx, sy in [(3, 6), (12, 6), (2, 9), (13, 9)]:
        px(sx, sy, d, (0x77, 0x33, 0x55, 100))

    save(img, "love_separation_gu.png")


# ============================================================
# MAIN - Generate all 23 textures
# ============================================================
if __name__ == "__main__":
    print("Generating 23 new gu worm textures...")
    print("=" * 50)

    # Sarira series (4)
    gen_bronze_sarira_gu()
    gen_iron_sarira_gu()
    gen_silver_sarira_gu()
    gen_gold_sarira_gu()

    # Consumption/Accumulation (3)
    gen_brown_bear_gu()
    gen_flower_boar_gu()
    gen_yellow_camel_beetle_gu()

    # Defense series (6)
    gen_stone_skin_gu()
    gen_iron_skin_gu()
    gen_beast_skin_gu()
    gen_black_bristle_gu()
    gen_steel_bristle_gu()
    gen_heaven_canopy_gu()

    # Healing series (2)
    gen_vitality_grass_gu()
    gen_vitality_leaf_gu()

    # Water protection (1)
    gen_water_spider_gu()

    # Tools/Tactical (7)
    gen_signal_gu()
    gen_flash_gu()
    gen_shadow_follower_gu()
    gen_dragon_cricket_gu()
    gen_quiet_step_gu()
    gen_scent_lock_gu()
    gen_love_separation_gu()

    print("=" * 50)
    print("All 23 textures generated successfully!")
