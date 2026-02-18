from PIL import Image, ImageDraw

img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)

# Base stone brick pattern - dark cyan/teal tones
for y in range(16):
    for x in range(16):
        # Stone brick base
        if (x == 0 or x == 8) and y < 16:
            r, g, b = 30, 60, 70
        elif (y == 0 or y == 8) and x < 16:
            r, g, b = 30, 60, 70
        else:
            # Main face with slight variation
            base_r, base_g, base_b = 50, 100, 110
            variation = ((x * 7 + y * 13) % 5) - 2
            r = max(0, min(255, base_r + variation * 3))
            g = max(0, min(255, base_g + variation * 3))
            b = max(0, min(255, base_b + variation * 3))

        img.putpixel((x, y), (r, g, b, 255))

# Central portal glow - bright cyan/aqua
for y in range(4, 12):
    for x in range(4, 12):
        dx = x - 7.5
        dy = y - 7.5
        dist = (dx*dx + dy*dy) ** 0.5
        if dist < 4:
            intensity = 1.0 - (dist / 4.0)
            r = int(100 + 155 * intensity)
            g = int(200 + 55 * intensity)
            b = 255
            a = int(200 + 55 * intensity)
            img.putpixel((x, y), (r, g, b, a))

# Inner bright core
for y in range(6, 10):
    for x in range(6, 10):
        dx = x - 7.5
        dy = y - 7.5
        dist = (dx*dx + dy*dy) ** 0.5
        if dist < 2:
            intensity = 1.0 - (dist / 2.0)
            r = int(180 + 75 * intensity)
            g = int(240 + 15 * intensity)
            b = 255
            img.putpixel((x, y), (r, g, b, 255))

# Corner accents - golden highlights
corners = [(1,1), (1,14), (14,1), (14,14)]
for cx, cy in corners:
    img.putpixel((cx, cy), (200, 160, 50, 255))

# Edge rune marks
for i in range(3, 13, 3):
    img.putpixel((i, 1), (120, 180, 200, 255))
    img.putpixel((i, 14), (120, 180, 200, 255))
    img.putpixel((1, i), (120, 180, 200, 255))
    img.putpixel((14, i), (120, 180, 200, 255))

img.save('src/main/resources/assets/reverend_insanity/textures/block/aperture_exit_portal.png')
print("Generated aperture_exit_portal.png")
