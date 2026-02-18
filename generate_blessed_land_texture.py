from PIL import Image, ImageDraw
import math

img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
draw = ImageDraw.Draw(img)

for y in range(16):
    for x in range(16):
        dist_center = math.sqrt((x - 7.5) ** 2 + (y - 7.5) ** 2)

        if dist_center < 3:
            t = dist_center / 3.0
            r = int(255 * (1 - t * 0.3))
            g = int(215 * (1 - t * 0.2))
            b = int(80 + 40 * t)
            a = 255
        elif dist_center < 5:
            t = (dist_center - 3) / 2.0
            r = int(200 - 30 * t)
            g = int(165 - 20 * t)
            b = int(60 + 20 * t)
            a = 255
        elif dist_center < 7:
            t = (dist_center - 5) / 2.0
            r = int(160 - 40 * t)
            g = int(130 - 30 * t)
            b = int(50 + 15 * t)
            a = 255
        else:
            t = min((dist_center - 7) / 2.0, 1.0)
            r = int(100 - 30 * t)
            g = int(80 - 20 * t)
            b = int(50 + 10 * t)
            a = 255

        img.putpixel((x, y), (r, g, b, a))

border_color = (80, 60, 40, 255)
for i in range(16):
    img.putpixel((i, 0), border_color)
    img.putpixel((i, 15), border_color)
    img.putpixel((0, i), border_color)
    img.putpixel((15, i), border_color)

rune_color = (255, 240, 180, 255)
rune_positions = [
    (7, 5), (8, 5),
    (6, 6), (9, 6),
    (6, 7), (9, 7),
    (7, 8), (8, 8),
    (7, 9), (8, 9),
    (6, 10), (9, 10),
]
for rx, ry in rune_positions:
    img.putpixel((rx, ry), rune_color)

glow_color = (255, 255, 200, 200)
glow_positions = [
    (7, 6), (8, 6),
    (7, 7), (8, 7),
]
for gx, gy in glow_positions:
    img.putpixel((gx, gy), glow_color)

flow_color_1 = (220, 190, 100, 180)
flow_color_2 = (200, 170, 80, 160)
flow_positions = [
    ((2, 3), flow_color_1), ((3, 2), flow_color_2),
    ((12, 4), flow_color_1), ((13, 3), flow_color_2),
    ((3, 12), flow_color_1), ((2, 13), flow_color_2),
    ((12, 12), flow_color_1), ((13, 11), flow_color_2),
    ((4, 7), flow_color_2), ((11, 8), flow_color_2),
    ((7, 3), flow_color_2), ((8, 12), flow_color_2),
]
for (fx, fy), fc in flow_positions:
    img.putpixel((fx, fy), fc)

corner_highlight = (190, 160, 90, 230)
img.putpixel((1, 1), corner_highlight)
img.putpixel((14, 1), corner_highlight)
img.putpixel((1, 14), corner_highlight)
img.putpixel((14, 14), corner_highlight)

output_path = 'src/main/resources/assets/reverend_insanity/textures/block/blessed_land_core.png'
img.save(output_path)
print(f"Texture saved to {output_path}")
