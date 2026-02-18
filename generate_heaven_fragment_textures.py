#!/usr/bin/env python3
"""生成九天碎片物品贴图 (64x64)"""
from PIL import Image, ImageDraw
import math
import random

OUTPUT_DIR = "/mnt/e/code/mod/gu/src/main/resources/assets/reverend_insanity/textures/item/"

HEAVENS = {
    "white":  {"base": (230, 230, 255), "glow": (255, 255, 255), "dark": (180, 180, 210)},
    "red":    {"base": (200, 40, 40),   "glow": (255, 100, 80),  "dark": (120, 20, 20)},
    "orange": {"base": (220, 120, 20),  "glow": (255, 170, 50),  "dark": (150, 70, 10)},
    "yellow": {"base": (220, 200, 30),  "glow": (255, 240, 80),  "dark": (160, 140, 10)},
    "green":  {"base": (40, 160, 50),   "glow": (100, 220, 110), "dark": (20, 100, 30)},
    "cyan":   {"base": (20, 170, 180),  "glow": (80, 230, 240),  "dark": (10, 100, 110)},
    "blue":   {"base": (50, 100, 220),  "glow": (100, 150, 255), "dark": (25, 60, 150)},
    "purple": {"base": (140, 50, 200),  "glow": (190, 110, 255), "dark": (80, 25, 130)},
    "black":  {"base": (40, 35, 50),    "glow": (80, 70, 100),   "dark": (15, 12, 25)},
}

FRAGMENT_SHAPE = [
    (28, 8), (32, 6), (36, 8), (40, 10), (42, 14),
    (44, 18), (43, 24), (40, 30), (38, 36), (40, 40),
    (42, 44), (38, 48), (34, 52), (30, 54), (26, 52),
    (22, 48), (20, 44), (18, 40), (16, 36), (18, 30),
    (20, 24), (22, 18), (24, 14), (26, 10),
]

def lerp_color(c1, c2, t):
    t = max(0.0, min(1.0, t))
    return tuple(int(c1[i] + (c2[i] - c1[i]) * t) for i in range(3))

def generate_fragment(name, colors):
    random.seed(hash(name) % (2**31))
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    jittered = []
    for px, py in FRAGMENT_SHAPE:
        jx = px + random.randint(-2, 2)
        jy = py + random.randint(-2, 2)
        jittered.append((jx, jy))

    draw.polygon(jittered, fill=(*colors["base"], 255))

    cx = sum(p[0] for p in jittered) / len(jittered)
    cy = sum(p[1] for p in jittered) / len(jittered)

    pixels = img.load()
    for y in range(64):
        for x in range(64):
            r, g, b, a = pixels[x, y]
            if a == 0:
                continue

            dx = x - cx
            dy = y - cy
            dist = math.sqrt(dx * dx + dy * dy)
            max_dist = 22.0

            t = dist / max_dist
            if t < 0.3:
                color = lerp_color(colors["glow"], colors["base"], t / 0.3)
            elif t < 0.7:
                color = colors["base"]
            else:
                color = lerp_color(colors["base"], colors["dark"], (t - 0.7) / 0.3)

            noise = random.randint(-8, 8)
            color = tuple(max(0, min(255, c + noise)) for c in color)

            pixels[x, y] = (*color, a)

    for px, py in jittered:
        ex = int(px)
        ey = int(py)
        if 0 <= ex < 64 and 0 <= ey < 64:
            r, g, b, a = pixels[ex, ey]
            if a > 0:
                edge = lerp_color(colors["dark"], (0, 0, 0), 0.3)
                pixels[ex, ey] = (*edge, 255)

    for _ in range(12):
        sx = int(cx + random.uniform(-10, 10))
        sy = int(cy + random.uniform(-10, 10))
        angle = random.uniform(0, math.pi * 2)
        length = random.randint(3, 8)
        for step in range(length):
            px = int(sx + math.cos(angle) * step)
            py = int(sy + math.sin(angle) * step)
            if 0 <= px < 64 and 0 <= py < 64:
                r, g, b, a = pixels[px, py]
                if a > 0:
                    crack = lerp_color((r, g, b), colors["dark"], 0.4)
                    pixels[px, py] = (*crack, a)

    glow_radius = 4
    for y in range(64):
        for x in range(64):
            r, g, b, a = pixels[x, y]
            if a > 0:
                dx = x - cx
                dy = y - cy
                dist = math.sqrt(dx * dx + dy * dy)
                if dist < glow_radius:
                    glow_t = 1.0 - dist / glow_radius
                    bright = lerp_color((r, g, b), colors["glow"], glow_t * 0.6)
                    pixels[x, y] = (*bright, a)

    for _ in range(6):
        sx = int(cx + random.uniform(-6, 6))
        sy = int(cy + random.uniform(-6, 6))
        if 0 <= sx < 64 and 0 <= sy < 64:
            r, g, b, a = pixels[sx, sy]
            if a > 0:
                sparkle = lerp_color((r, g, b), (255, 255, 255), 0.7)
                pixels[sx, sy] = (*sparkle, a)
                for ddx, ddy in [(-1,0),(1,0),(0,-1),(0,1)]:
                    nx, ny = sx + ddx, sy + ddy
                    if 0 <= nx < 64 and 0 <= ny < 64:
                        nr, ng, nb, na = pixels[nx, ny]
                        if na > 0:
                            sp2 = lerp_color((nr, ng, nb), (255, 255, 255), 0.3)
                            pixels[nx, ny] = (*sp2, na)

    path = OUTPUT_DIR + name + "_heaven_fragment.png"
    img.save(path)
    print(f"Generated: {path}")

for name, colors in HEAVENS.items():
    generate_fragment(name, colors)

print("All 9 heaven fragment textures generated.")
