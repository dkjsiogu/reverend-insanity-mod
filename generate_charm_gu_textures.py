#!/usr/bin/env python3
"""Generate 5 charm/mind-type gu worm item textures for Reverend Insanity mod."""

from PIL import Image, ImageDraw
import math
import random

OUTPUT_DIR = "/mnt/e/code/mod/gu/src/main/resources/assets/reverend_insanity/textures/item"
SIZE = 64

random.seed(42)


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


def draw_ellipse_filled(img, cx, cy, rx, ry, color):
    """Draw a filled ellipse."""
    for y in range(max(0, int(cy - ry)), min(SIZE, int(cy + ry + 1))):
        for x in range(max(0, int(cx - rx)), min(SIZE, int(cx + rx + 1))):
            dx = (x - cx) / max(rx, 0.1)
            dy = (y - cy) / max(ry, 0.1)
            if dx * dx + dy * dy <= 1.0:
                blend_pixel(img, x, y, color)


def draw_circle(img, cx, cy, r, color):
    """Draw a filled circle."""
    draw_ellipse_filled(img, cx, cy, r, r, color)


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


def draw_line_thick(img, x0, y0, x1, y1, color, thickness=1):
    """Draw a thick line using Bresenham-like approach."""
    steps = max(abs(x1 - x0), abs(y1 - y0), 1)
    for i in range(steps + 1):
        t = i / steps
        x = x0 + (x1 - x0) * t
        y = y0 + (y1 - y0) * t
        for dy in range(-thickness, thickness + 1):
            for dx in range(-thickness, thickness + 1):
                if dx * dx + dy * dy <= thickness * thickness:
                    blend_pixel(img, int(x + dx), int(y + dy), color)


def draw_worm_body_segments(img, points, radius_func, color_func):
    """Draw a segmented worm body along a path of points."""
    for i, (px, py) in enumerate(points):
        t = i / max(len(points) - 1, 1)
        r = radius_func(t)
        color = color_func(t)
        draw_circle(img, px, py, r, color)


def generate_charm_gu():
    """Charm Gu (魅惑蛊) - Seductive pink-rose worm with heart markings."""
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))

    # Outer glow - soft pink aura
    draw_glow(img, 32, 32, 28, (255, 102, 170), 40)
    draw_glow(img, 28, 30, 20, (255, 130, 180), 30)
    draw_glow(img, 36, 34, 20, (255, 130, 180), 30)

    # Worm body - sinuous S-curve
    body_points = []
    for i in range(30):
        t = i / 29.0
        x = 18 + t * 28
        y = 34 + math.sin(t * math.pi * 2.2) * 7
        body_points.append((x, y))

    # Body shadow/outline
    for px, py in body_points:
        t = body_points.index((px, py)) / 29.0
        r = 4.5 + math.sin(t * math.pi) * 2.5
        draw_circle(img, px, py + 1, r + 1, (140, 30, 70, 200))

    # Main body
    def body_radius(t):
        return 4 + math.sin(t * math.pi) * 2.5

    def body_color(t):
        # Gradient from pink to rose
        r = int(255 - t * 34)
        g = int(102 - t * 51)
        b = int(170 - t * 34)
        return (r, g, b, 255)

    draw_worm_body_segments(img, body_points, body_radius, body_color)

    # Body highlight (top edge shine)
    for i in range(len(body_points)):
        t = i / 29.0
        px, py = body_points[i]
        r = body_radius(t) * 0.5
        draw_circle(img, px, py - 2, r, (255, 180, 210, 120))

    # Segment lines
    for i in range(3, 27, 3):
        t = i / 29.0
        px, py = body_points[i]
        r = body_radius(t)
        draw_line_thick(img, int(px), int(py - r + 1), int(px), int(py + r - 1),
                        (180, 40, 90, 100), 0)

    # Heart-shaped markings on body
    heart_positions = [8, 15, 22]
    for idx in heart_positions:
        if idx < len(body_points):
            hx, hy = body_points[idx]
            # Simple pixel heart shape
            heart_pixels = [
                (-1, -1), (1, -1),
                (-2, 0), (-1, 0), (0, 0), (1, 0), (2, 0),
                (-1, 1), (0, 1), (1, 1),
                (0, 2)
            ]
            for dx, dy in heart_pixels:
                blend_pixel(img, int(hx + dx), int(hy + dy), (255, 200, 50, 180))

    # Head features
    hx, hy = body_points[0]
    # Eyes
    draw_circle(img, hx - 1, hy - 3, 2, (255, 255, 255, 255))
    draw_circle(img, hx + 2, hy - 3, 2, (255, 255, 255, 255))
    blend_pixel(img, int(hx - 1), int(hy - 3), (200, 30, 80, 255))
    blend_pixel(img, int(hx + 2), int(hy - 3), (200, 30, 80, 255))

    # Antennae with heart tips
    draw_line_thick(img, int(hx - 2), int(hy - 4), int(hx - 6), int(hy - 10),
                    (255, 102, 170, 220), 0)
    draw_line_thick(img, int(hx + 3), int(hy - 4), int(hx + 7), int(hy - 10),
                    (255, 102, 170, 220), 0)
    # Heart tips on antennae
    for tx, ty in [(hx - 6, hy - 11), (hx + 7, hy - 11)]:
        blend_pixel(img, int(tx - 1), int(ty), (255, 200, 50, 255))
        blend_pixel(img, int(tx + 1), int(ty), (255, 200, 50, 255))
        blend_pixel(img, int(tx), int(ty + 1), (255, 200, 50, 255))

    # Tail - tapered with a curl
    tx, ty = body_points[-1]
    for i in range(8):
        t = i / 7.0
        cx = tx + 2 + t * 4
        cy = ty - t * 3 + math.sin(t * math.pi) * 2
        r = 2 * (1 - t * 0.8)
        draw_circle(img, cx, cy, r, (221, 51, 136, int(255 * (1 - t * 0.5))))

    # Golden sparkle particles
    sparkle_positions = [(12, 20), (50, 22), (25, 44), (40, 46), (32, 16), (20, 38), (44, 38)]
    for sx, sy in sparkle_positions:
        blend_pixel(img, sx, sy, (255, 215, 100, 200))
        blend_pixel(img, sx + 1, sy, (255, 215, 100, 120))
        blend_pixel(img, sx - 1, sy, (255, 215, 100, 120))
        blend_pixel(img, sx, sy + 1, (255, 215, 100, 120))
        blend_pixel(img, sx, sy - 1, (255, 215, 100, 120))

    img.save(f"{OUTPUT_DIR}/charm_gu.png")
    print("Generated charm_gu.png")


def generate_bewitch_gu():
    """Bewitch Gu (妖媚蛊) - Enchanting worm with hypnotic eye patterns."""
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))

    # Mystical swirl aura background
    for angle in range(0, 360, 15):
        rad = math.radians(angle)
        for dist in range(5, 26):
            x = 32 + math.cos(rad + dist * 0.08) * dist
            y = 32 + math.sin(rad + dist * 0.08) * dist
            alpha = int(35 * (1 - dist / 26.0))
            blend_pixel(img, int(x), int(y), (170, 17, 102, alpha))
            blend_pixel(img, int(x) + 1, int(y), (170, 17, 102, alpha // 2))

    # Outer glow
    draw_glow(img, 32, 33, 24, (204, 34, 102), 50)
    draw_glow(img, 32, 33, 16, (170, 17, 102), 35)

    # Worm body - elegant curve
    body_points = []
    for i in range(32):
        t = i / 31.0
        x = 15 + t * 34
        y = 33 + math.sin(t * math.pi * 1.8 + 0.5) * 8
        body_points.append((x, y))

    # Body shadow
    for px, py in body_points:
        idx = body_points.index((px, py))
        t = idx / 31.0
        r = 5 + math.sin(t * math.pi) * 2.5
        draw_circle(img, px, py + 1, r + 1, (100, 10, 50, 180))

    # Main body - deep pink to magenta gradient
    def body_radius(t):
        return 4.5 + math.sin(t * math.pi) * 2.5

    def body_color(t):
        r = int(204 + (170 - 204) * t)
        g = int(34 + (17 - 34) * t)
        b = int(102 + (102 - 102) * t)
        return (r, g, b, 255)

    draw_worm_body_segments(img, body_points, body_radius, body_color)

    # Body sheen
    for i in range(len(body_points)):
        t = i / 31.0
        px, py = body_points[i]
        r = body_radius(t) * 0.4
        draw_circle(img, px, py - 2, r, (255, 120, 180, 100))

    # Hypnotic eye patterns along body
    eye_positions = [6, 13, 20, 26]
    for idx in eye_positions:
        if idx < len(body_points):
            ex, ey = body_points[idx]
            # Outer eye ring
            draw_circle(img, ex, ey, 3, (140, 10, 80, 255))
            draw_circle(img, ex, ey, 2, (200, 30, 120, 255))
            # Inner eye - bright
            draw_circle(img, ex, ey, 1, (255, 180, 220, 255))
            # Center pupil
            blend_pixel(img, int(ex), int(ey), (80, 0, 50, 255))
            # Eye glow
            draw_glow(img, ex, ey, 5, (255, 80, 180), 40)

    # Head
    hx, hy = body_points[0]
    # Large hypnotic main eyes
    for offset_x, offset_y in [(-2, -3), (2, -3)]:
        ex, ey = hx + offset_x, hy + offset_y
        draw_circle(img, ex, ey, 2.5, (255, 255, 255, 255))
        draw_circle(img, ex, ey, 1.5, (200, 34, 102, 255))
        blend_pixel(img, int(ex), int(ey), (255, 220, 240, 255))

    # Flowing antennae - longer, more elegant
    for side in [-1, 1]:
        for i in range(12):
            t = i / 11.0
            ax = hx + side * (3 + t * 6)
            ay = hy - 4 - t * 8 + math.sin(t * math.pi * 1.5) * 2 * side
            alpha = int(220 * (1 - t * 0.6))
            r = 1.5 * (1 - t * 0.7)
            draw_circle(img, ax, ay, r, (204, 34, 102, alpha))

    # Tail with mystical trail
    tx, ty = body_points[-1]
    for i in range(10):
        t = i / 9.0
        cx = tx + 1 + t * 5
        cy = ty + math.sin(t * math.pi * 2) * 3
        r = 2.5 * (1 - t * 0.8)
        alpha = int(220 * (1 - t * 0.7))
        draw_circle(img, cx, cy, r, (170, 17, 102, alpha))

    # Purple mystical swirl particles
    for i in range(12):
        angle = i * 30
        rad = math.radians(angle)
        dist = 18 + random.randint(-3, 3)
        sx = 32 + math.cos(rad) * dist
        sy = 33 + math.sin(rad) * dist
        blend_pixel(img, int(sx), int(sy), (200, 50, 150, 160))
        blend_pixel(img, int(sx + 1), int(sy), (200, 50, 150, 80))

    img.save(f"{OUTPUT_DIR}/bewitch_gu.png")
    print("Generated bewitch_gu.png")


def generate_soul_charm_gu():
    """Soul Charm Gu (摄魂蛊) - Powerful rank 2 charm worm with mesmerizing spirals."""
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))

    # Soul energy trails - spiraling outward
    for spiral in range(3):
        offset = spiral * 120
        for i in range(60):
            t = i / 59.0
            angle = math.radians(offset + t * 540)
            dist = 5 + t * 20
            x = 32 + math.cos(angle) * dist
            y = 33 + math.sin(angle) * dist
            alpha = int(60 * (1 - t * 0.7))
            r, g, b = 255, 68, 187  # #FF44BB
            blend_pixel(img, int(x), int(y), (r, g, b, alpha))
            blend_pixel(img, int(x + 1), int(y), (r, g, b, alpha // 2))
            blend_pixel(img, int(x), int(y + 1), (r, g, b, alpha // 2))

    # Central glow - soul energy
    draw_glow(img, 32, 33, 22, (153, 17, 85), 55)
    draw_glow(img, 32, 33, 14, (255, 68, 187), 40)

    # Worm body - coiled/spiral shape (more powerful looking)
    body_points = []
    for i in range(36):
        t = i / 35.0
        x = 16 + t * 32
        y = 33 + math.sin(t * math.pi * 2.5) * 6
        body_points.append((x, y))

    # Body shadow
    for px, py in body_points:
        idx = body_points.index((px, py))
        t = idx / 35.0
        r = 5 + math.sin(t * math.pi) * 3
        draw_circle(img, px, py + 1, r + 1, (80, 8, 40, 200))

    # Main body - dark magenta
    def body_radius(t):
        return 5 + math.sin(t * math.pi) * 2.5

    def body_color(t):
        base_r, base_g, base_b = 153, 17, 85
        # Pulsing brightness
        pulse = math.sin(t * math.pi * 4) * 0.2
        r = int(min(255, base_r * (1 + pulse)))
        g = int(min(255, base_g * (1 + pulse)))
        b = int(min(255, base_b * (1 + pulse)))
        return (r, g, b, 255)

    draw_worm_body_segments(img, body_points, body_radius, body_color)

    # Body highlight
    for i in range(len(body_points)):
        t = i / 35.0
        px, py = body_points[i]
        r = body_radius(t) * 0.35
        draw_circle(img, px, py - 2, r, (255, 130, 200, 90))

    # Mesmerizing spiral patterns on body
    spiral_centers = [8, 16, 24, 30]
    for idx in spiral_centers:
        if idx < len(body_points):
            sx, sy = body_points[idx]
            for j in range(20):
                t = j / 19.0
                angle = t * math.pi * 3
                dist = t * 3
                px = sx + math.cos(angle) * dist
                py = sy + math.sin(angle) * dist
                alpha = int(200 * (1 - t * 0.5))
                blend_pixel(img, int(px), int(py), (255, 68, 187, alpha))

    # Head - more imposing
    hx, hy = body_points[0]
    # Larger head
    draw_circle(img, hx, hy, 6, (153, 17, 85, 255))
    draw_circle(img, hx, hy, 4.5, (180, 30, 100, 255))
    # Soul-capturing eyes - glowing
    for ox in [-2, 2]:
        ex, ey = hx + ox, hy - 2
        draw_circle(img, ex, ey, 2, (255, 68, 187, 255))
        draw_circle(img, ex, ey, 1, (255, 200, 230, 255))
        draw_glow(img, ex, ey, 4, (255, 68, 187), 60)

    # Third eye on forehead
    draw_circle(img, hx, hy - 5, 1.5, (255, 100, 200, 255))
    blend_pixel(img, int(hx), int(hy - 5), (255, 220, 240, 255))
    draw_glow(img, hx, hy - 5, 4, (255, 68, 187), 50)

    # Crown-like horns
    for side in [-1, 1]:
        for i in range(6):
            t = i / 5.0
            cx = hx + side * (4 + t * 3)
            cy = hy - 5 - t * 5
            r = 1.2 * (1 - t * 0.6)
            alpha = int(230 * (1 - t * 0.4))
            draw_circle(img, cx, cy, r, (200, 40, 120, alpha))

    # Tail - soul energy dissipation
    tx, ty = body_points[-1]
    for i in range(12):
        t = i / 11.0
        cx = tx + 1 + t * 7
        cy = ty + math.sin(t * math.pi * 1.5) * 4
        r = 3 * (1 - t * 0.8)
        alpha = int(200 * (1 - t * 0.7))
        draw_circle(img, cx, cy, r, (255, 68, 187, alpha))

    # Bright soul energy particles
    for i in range(8):
        angle = i * 45 + 20
        rad = math.radians(angle)
        dist = 20 + random.randint(-4, 4)
        px = 32 + math.cos(rad) * dist
        py = 33 + math.sin(rad) * dist
        draw_circle(img, px, py, 1, (255, 68, 187, 180))
        draw_glow(img, px, py, 3, (255, 68, 187), 50)

    img.save(f"{OUTPUT_DIR}/soul_charm_gu.png")
    print("Generated soul_charm_gu.png")


def generate_thought_gu():
    """Thought Gu (思维蛊) - Cerebral worm with brain-like patterns and thought waves."""
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))

    # Thought wave emanations - concentric arcs
    for wave in range(4):
        radius = 14 + wave * 5
        alpha_base = 40 - wave * 8
        for angle in range(-60, 61, 2):
            rad = math.radians(angle - 90)  # Emanating upward
            x = 32 + math.cos(rad) * radius
            y = 28 + math.sin(rad) * radius
            blend_pixel(img, int(x), int(y), (136, 221, 255, max(alpha_base, 5)))
            blend_pixel(img, int(x + 1), int(y), (136, 221, 255, max(alpha_base // 2, 3)))

    # Central glow - cyan/blue
    draw_glow(img, 32, 35, 22, (68, 204, 255), 45)
    draw_glow(img, 32, 35, 14, (136, 221, 255), 30)

    # Worm body
    body_points = []
    for i in range(28):
        t = i / 27.0
        x = 17 + t * 30
        y = 37 + math.sin(t * math.pi * 1.6) * 5
        body_points.append((x, y))

    # Body shadow
    for px, py in body_points:
        idx = body_points.index((px, py))
        t = idx / 27.0
        r = 4.5 + math.sin(t * math.pi) * 2
        draw_circle(img, px, py + 1, r + 1, (20, 100, 150, 180))

    # Main body - cyan gradient
    def body_radius(t):
        return 4 + math.sin(t * math.pi) * 2

    def body_color(t):
        r = int(68 + (136 - 68) * math.sin(t * math.pi))
        g = int(204 + (221 - 204) * math.sin(t * math.pi))
        b = 255
        return (r, g, b, 255)

    draw_worm_body_segments(img, body_points, body_radius, body_color)

    # Body highlight
    for i in range(len(body_points)):
        t = i / 27.0
        px, py = body_points[i]
        r = body_radius(t) * 0.4
        draw_circle(img, px, py - 2, r, (200, 240, 255, 100))

    # Brain-like wrinkle patterns on body
    for seg_start in [4, 10, 16, 22]:
        if seg_start + 3 < len(body_points):
            sx, sy = body_points[seg_start]
            # Wavy brain fold lines
            for j in range(6):
                t = j / 5.0
                wx = sx - 2 + t * 4
                wy = sy + math.sin(t * math.pi * 2) * 1.5
                blend_pixel(img, int(wx), int(wy), (40, 160, 220, 180))
                blend_pixel(img, int(wx), int(wy - 1), (40, 160, 220, 100))

    # Head - bulbous, brain-like
    hx, hy = body_points[0]
    # Enlarged head (brain)
    draw_circle(img, hx, hy - 1, 6, (50, 180, 230, 255))
    draw_circle(img, hx, hy - 1, 4.5, (68, 204, 255, 255))
    # Brain fold detail on head
    for fold_y in [-3, -1, 1]:
        for fold_x in range(-3, 4):
            wave = math.sin(fold_x * 0.8) * 0.8
            blend_pixel(img, int(hx + fold_x), int(hy + fold_y + wave),
                        (100, 220, 255, 150))

    # Eyes - thoughtful, glowing
    for ox in [-2, 2]:
        ex, ey = hx + ox, hy + 1
        draw_circle(img, ex, ey, 1.5, (255, 255, 255, 255))
        blend_pixel(img, int(ex), int(ey), (68, 204, 255, 255))

    # Thought energy antennae
    for side in [-1, 1]:
        for i in range(8):
            t = i / 7.0
            ax = hx + side * (3 + t * 4)
            ay = hy - 4 - t * 7
            alpha = int(200 * (1 - t * 0.5))
            r = 1 * (1 - t * 0.5)
            draw_circle(img, ax, ay, r, (136, 221, 255, alpha))
        # Thought spark at tip
        tip_x = hx + side * 7
        tip_y = hy - 11
        blend_pixel(img, int(tip_x), int(tip_y), (255, 255, 255, 220))
        draw_glow(img, tip_x, tip_y, 3, (136, 221, 255), 60)

    # Silver thought energy lines radiating from head
    for angle in range(-80, -10, 15):
        rad = math.radians(angle)
        for dist in range(8, 18):
            x = hx + math.cos(rad) * dist
            y = hy - 2 + math.sin(rad) * dist
            alpha = int(80 * (1 - (dist - 8) / 10.0))
            blend_pixel(img, int(x), int(y), (200, 220, 240, alpha))

    # Tail
    tx, ty = body_points[-1]
    for i in range(8):
        t = i / 7.0
        cx = tx + 1 + t * 5
        cy = ty - t * 1
        r = 2 * (1 - t * 0.8)
        alpha = int(200 * (1 - t * 0.6))
        draw_circle(img, cx, cy, r, (68, 204, 255, alpha))

    # Floating thought spark particles
    thought_sparks = [(22, 18), (42, 20), (15, 28), (50, 28), (30, 12), (36, 14)]
    for sx, sy in thought_sparks:
        blend_pixel(img, sx, sy, (200, 230, 255, 160))
        blend_pixel(img, sx + 1, sy, (200, 230, 255, 80))
        blend_pixel(img, sx, sy - 1, (200, 230, 255, 80))

    img.save(f"{OUTPUT_DIR}/thought_gu.png")
    print("Generated thought_gu.png")


def generate_mind_guard_gu():
    """Mind Guard Gu (神智蛊) - Protective worm with third-eye motif and crystalline armor."""
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))

    # Shield glow aura
    draw_glow(img, 32, 33, 26, (102, 187, 255), 40)
    draw_glow(img, 32, 33, 18, (51, 102, 187), 35)

    # Hexagonal shield pattern in background
    for ring in range(3):
        radius = 10 + ring * 6
        for i in range(6):
            angle = i * 60 + 30
            rad = math.radians(angle)
            x1 = 32 + math.cos(rad) * radius
            y1 = 33 + math.sin(rad) * radius
            next_angle = (i + 1) * 60 + 30
            next_rad = math.radians(next_angle)
            x2 = 32 + math.cos(next_rad) * radius
            y2 = 33 + math.sin(next_rad) * radius
            alpha = 30 - ring * 8
            draw_line_thick(img, int(x1), int(y1), int(x2), int(y2),
                            (102, 187, 255, max(alpha, 5)), 0)

    # Worm body - sturdy, armored
    body_points = []
    for i in range(30):
        t = i / 29.0
        x = 16 + t * 32
        y = 34 + math.sin(t * math.pi * 1.5) * 5
        body_points.append((x, y))

    # Body shadow
    for px, py in body_points:
        idx = body_points.index((px, py))
        t = idx / 29.0
        r = 5 + math.sin(t * math.pi) * 2.5
        draw_circle(img, px, py + 1, r + 1, (20, 40, 100, 200))

    # Main body - deep blue
    def body_radius(t):
        return 5 + math.sin(t * math.pi) * 2

    def body_color(t):
        r = 51
        g = int(102 + math.sin(t * math.pi * 6) * 20)
        b = int(187 + math.sin(t * math.pi * 6) * 20)
        return (r, g, b, 255)

    draw_worm_body_segments(img, body_points, body_radius, body_color)

    # Crystalline armor plates
    armor_positions = [4, 9, 14, 19, 24]
    for idx in armor_positions:
        if idx < len(body_points):
            ax, ay = body_points[idx]
            t = idx / 29.0
            r = body_radius(t)
            # Diamond-shaped armor plate
            plate_pixels = [
                (0, -int(r) - 1),
                (-1, -int(r)), (0, -int(r)), (1, -int(r)),
                (-2, -int(r) + 1), (2, -int(r) + 1),
            ]
            for dx, dy in plate_pixels:
                blend_pixel(img, int(ax + dx), int(ay + dy), (150, 200, 255, 200))
            # Also bottom armor
            for dx, dy in plate_pixels:
                blend_pixel(img, int(ax + dx), int(ay - dy), (100, 160, 220, 180))

    # Body highlight - crystalline sheen
    for i in range(len(body_points)):
        t = i / 29.0
        px, py = body_points[i]
        r = body_radius(t) * 0.35
        draw_circle(img, px, py - 2, r, (180, 220, 255, 110))

    # Head - wise, protective
    hx, hy = body_points[0]
    draw_circle(img, hx, hy, 6, (51, 102, 187, 255))
    draw_circle(img, hx, hy, 4.5, (60, 120, 200, 255))

    # Regular eyes
    for ox in [-2, 2]:
        ex, ey = hx + ox, hy
        draw_circle(img, ex, ey, 1.5, (255, 255, 255, 255))
        blend_pixel(img, int(ex), int(ey), (51, 102, 187, 255))

    # Third eye (wisdom eye) - prominent
    third_eye_x, third_eye_y = hx, hy - 4
    draw_circle(img, third_eye_x, third_eye_y, 3, (102, 187, 255, 255))
    draw_circle(img, third_eye_x, third_eye_y, 2, (150, 220, 255, 255))
    draw_circle(img, third_eye_x, third_eye_y, 1, (255, 255, 255, 255))
    draw_glow(img, third_eye_x, third_eye_y, 6, (102, 187, 255), 70)

    # Wisdom symbol markings - small runes along body
    rune_positions = [7, 12, 17, 22, 27]
    for idx in rune_positions:
        if idx < len(body_points):
            rx, ry = body_points[idx]
            # Simple wisdom rune (eye-like symbol)
            blend_pixel(img, int(rx - 1), int(ry), (150, 220, 255, 200))
            blend_pixel(img, int(rx + 1), int(ry), (150, 220, 255, 200))
            blend_pixel(img, int(rx), int(ry - 1), (150, 220, 255, 200))
            blend_pixel(img, int(rx), int(ry + 1), (150, 220, 255, 200))
            blend_pixel(img, int(rx), int(ry), (255, 255, 255, 220))

    # Protective crown/crest
    for side in [-1, 1]:
        for i in range(5):
            t = i / 4.0
            cx = hx + side * (4 + t * 2)
            cy = hy - 3 - t * 4
            r = 1.2 * (1 - t * 0.5)
            draw_circle(img, cx, cy, r, (102, 187, 255, int(220 * (1 - t * 0.3))))

    # Shield energy around head
    for angle in range(0, 360, 20):
        rad = math.radians(angle)
        dist = 8
        px = hx + math.cos(rad) * dist
        py = hy + math.sin(rad) * dist
        alpha = 50 + int(30 * math.sin(angle * 0.1))
        blend_pixel(img, int(px), int(py), (102, 187, 255, alpha))

    # Tail - crystalline taper
    tx, ty = body_points[-1]
    for i in range(10):
        t = i / 9.0
        cx = tx + 1 + t * 6
        cy = ty - t * 2
        r = 3 * (1 - t * 0.8)
        alpha = int(220 * (1 - t * 0.6))
        draw_circle(img, cx, cy, r, (51, 102, 187, alpha))
        # Crystal facet highlights
        if i % 3 == 0:
            blend_pixel(img, int(cx), int(cy - 1), (180, 220, 255, alpha))

    # Floating shield crystal particles
    for i in range(6):
        angle = i * 60
        rad = math.radians(angle)
        dist = 22
        sx = 32 + math.cos(rad) * dist
        sy = 33 + math.sin(rad) * dist
        blend_pixel(img, int(sx), int(sy), (150, 210, 255, 140))
        blend_pixel(img, int(sx + 1), int(sy), (150, 210, 255, 70))
        blend_pixel(img, int(sx), int(sy + 1), (150, 210, 255, 70))

    img.save(f"{OUTPUT_DIR}/mind_guard_gu.png")
    print("Generated mind_guard_gu.png")


if __name__ == "__main__":
    generate_charm_gu()
    generate_bewitch_gu()
    generate_soul_charm_gu()
    generate_thought_gu()
    generate_mind_guard_gu()
    print("\nAll 5 gu worm textures generated successfully!")
