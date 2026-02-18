#!/usr/bin/env python3
"""Generate 6 Gu worm item textures: Breeze, Wind Blade, Gale, Lightning, Thunder Shield, Thunderstorm."""

from PIL import Image
import os

OUTPUT_DIR = "/mnt/e/code/mod/gu/src/main/resources/assets/reverend_insanity/textures/item"
T = (0, 0, 0, 0)


def hex_to_rgba(h, a=255):
    h = h.lstrip('#')
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)


def scale_up(img16, size=64):
    return img16.resize((size, size), Image.NEAREST)


def draw_from_grid(img, grid, palette):
    for y, row in enumerate(grid):
        for x, ch in enumerate(row):
            if ch != '.' and ch in palette:
                img.putpixel((x, y), palette[ch])


# =============================================================================
# 1. Breeze Gu - Airy mint green wind worm with translucent wings
# =============================================================================
def generate_breeze_gu():
    img = Image.new('RGBA', (16, 16), T)

    palette = {
        'o': hex_to_rgba('#336655'),
        'M': hex_to_rgba('#88FFCC'),
        'L': hex_to_rgba('#AAFFDD'),
        'W': hex_to_rgba('#FFFFFF'),
        'w': hex_to_rgba('#88FFCC', 140),
        'g': hex_to_rgba('#AAFFDD', 90),
        'h': hex_to_rgba('#FFFFFF', 220),
        'd': hex_to_rgba('#55CC99'),
        'e': hex_to_rgba('#CCFFEE', 120),
    }

    grid = [
        '................',
        '......gg........',
        '.....gWeg..g....',
        '....oLhLo.geg...',
        '...oMLMMLogweg...',
        '..oMhMLMLoweg...',
        '..oMLhMLMLo.....',
        '.oMMMLMhMLo.....',
        '.oMdMMMLMLo.....',
        '.oMMLMdMMLow....',
        '..oMdMMLMoweg...',
        '..oMMLMMLogeg...',
        '...oMdMo..ge....',
        '....ooo.........',
        '................',
        '................',
    ]
    draw_from_grid(img, grid, palette)
    return scale_up(img)


# =============================================================================
# 2. Wind Blade Gu - Sharp blade shape, angular and aggressive
# =============================================================================
def generate_wind_blade_gu():
    img = Image.new('RGBA', (16, 16), T)

    palette = {
        'o': hex_to_rgba('#223322'),
        'G': hex_to_rgba('#88FFAA'),
        'D': hex_to_rgba('#446644'),
        'S': hex_to_rgba('#CCCCCC'),
        'b': hex_to_rgba('#DDDDDD'),
        'B': hex_to_rgba('#66AA77'),
        'h': hex_to_rgba('#FFFFFF'),
        's': hex_to_rgba('#AACCBB', 160),
        'e': hex_to_rgba('#EEEEFF'),
    }

    grid = [
        '................',
        '.............o..',
        '............oeo.',
        '...........oSeo.',
        '..........oSGo..',
        '.........oSGo...',
        '........oSGo....',
        '.......obGo.....',
        '......oGGo......',
        '.....oBGo.......',
        '....oBGos.......',
        '...oGGos........',
        '..oDGo..........',
        '..oDo...........',
        '...o............',
        '................',
    ]
    draw_from_grid(img, grid, palette)
    return scale_up(img)


# =============================================================================
# 3. Gale Gu - Evolved wind worm, dynamic with spiral wind trails
# =============================================================================
def generate_gale_gu():
    img = Image.new('RGBA', (16, 16), T)

    palette = {
        'o': hex_to_rgba('#225544'),
        'G': hex_to_rgba('#44DD88'),
        'B': hex_to_rgba('#66FFBB'),
        'h': hex_to_rgba('#EEFFFF'),
        'd': hex_to_rgba('#339966'),
        't': hex_to_rgba('#AAFFDD', 130),
        'w': hex_to_rgba('#FFFFFF'),
        's': hex_to_rgba('#CCFFEE', 90),
    }

    grid = [
        '................',
        '...t..oo........',
        '..ts.oBBo.t.....',
        '..t.oGhGBo.ts...',
        '...oGBGhGBo.t...',
        '..oGhBGBGBGo....',
        '..oGBGhBGBGo....',
        '.oGBGBGhGBGo....',
        '.odGBGBGhBGo....',
        '.odGBGBGBGot....',
        '..odGBGBGots.s..',
        '..t.odGGo..t....',
        '...t..oo..t.....',
        '....ts..ts......',
        '................',
        '................',
    ]
    draw_from_grid(img, grid, palette)
    return scale_up(img)


# =============================================================================
# 4. Lightning Gu - Jagged electric worm, yellow-white lightning
# =============================================================================
def generate_lightning_gu():
    img = Image.new('RGBA', (16, 16), T)

    palette = {
        'o': hex_to_rgba('#665500'),
        'Y': hex_to_rgba('#FFFF44'),
        'G': hex_to_rgba('#FFDD00'),
        'W': hex_to_rgba('#FFFFFF'),
        'h': hex_to_rgba('#FFFFAA'),
        's': hex_to_rgba('#FFFFFF', 180),
        'g': hex_to_rgba('#FFFF88', 110),
        'd': hex_to_rgba('#CCAA00'),
    }

    grid = [
        '................',
        '....g..g........',
        '...goWog........',
        '...oYWYog.......',
        '..oYhYWYo..s....',
        '..oYWYhYo.......',
        '.oGYWYWYo.......',
        '.oGYhYWYo.......',
        '.oGYWYhYo..s....',
        '..oYYWYYo.......',
        '..oGYhYo........',
        '...oYYo...s.....',
        '...goo..........',
        '....g...........',
        '................',
        '................',
    ]
    draw_from_grid(img, grid, palette)
    return scale_up(img)


# =============================================================================
# 5. Thunder Shield Gu - Round shield with electric vein patterns
# =============================================================================
def generate_thunder_shield_gu():
    img = Image.new('RGBA', (16, 16), T)

    palette = {
        'o': hex_to_rgba('#222244'),
        'B': hex_to_rgba('#4466AA'),
        'Y': hex_to_rgba('#FFDD44'),
        'L': hex_to_rgba('#5577BB'),
        'D': hex_to_rgba('#334488'),
        'h': hex_to_rgba('#FFFFFF'),
        'y': hex_to_rgba('#FFFF88'),
        'g': hex_to_rgba('#FFFF88', 110),
        'v': hex_to_rgba('#FFEE66'),
    }

    grid = [
        '................',
        '.....oooo.......',
        '....oLBBLo......',
        '...oLYBBYLo.....',
        '..oBBYBBBBo.....',
        '..oBBBYBYBo.....',
        '..oyBBhBBBo.....',
        '..oBBYhBYBo.....',
        '..oBYBBYBBo.....',
        '..oDBBYBBDo.....',
        '...oDBYBDo......',
        '....oDDDo.......',
        '.....ooo........',
        '................',
        '................',
        '................',
    ]
    draw_from_grid(img, grid, palette)
    return scale_up(img)


# =============================================================================
# 6. Thunderstorm Gu - Dark purple body with intense lightning bolts
# =============================================================================
def generate_thunderstorm_gu():
    img = Image.new('RGBA', (16, 16), T)

    palette = {
        'o': hex_to_rgba('#110022'),
        'P': hex_to_rgba('#442266'),
        'Y': hex_to_rgba('#FFFF00'),
        'D': hex_to_rgba('#331155'),
        'M': hex_to_rgba('#553388'),
        'y': hex_to_rgba('#FFDD88'),
        'w': hex_to_rgba('#FFFFFF'),
        's': hex_to_rgba('#FFFFAA', 180),
        'g': hex_to_rgba('#FFFF00', 110),
        'L': hex_to_rgba('#FFFF44'),
    }

    grid = [
        '......g.........',
        '.....gsg........',
        '....oooo........',
        '...oMPYPoo......',
        '..oPYPMPPPo.....',
        '..oPPYPPYPo.....',
        '.oMYPPYPPMPo....',
        '.oDPPYPPYPPo....',
        '.oDPYPPPPYPo....',
        '.oDPPPYPPMPo....',
        '..oDPYPPYPo.....',
        '..oPPPYPPo.s....',
        '...oDPPDo.......',
        '....oooo........',
        '.....g..........',
        '................',
    ]
    draw_from_grid(img, grid, palette)
    return scale_up(img)


# =============================================================================
# Main
# =============================================================================
def main():
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    textures = {
        'breeze_gu.png': generate_breeze_gu,
        'wind_blade_gu.png': generate_wind_blade_gu,
        'gale_gu.png': generate_gale_gu,
        'lightning_gu.png': generate_lightning_gu,
        'thunder_shield_gu.png': generate_thunder_shield_gu,
        'thunderstorm_gu.png': generate_thunderstorm_gu,
    }

    for name, gen_func in textures.items():
        path = os.path.join(OUTPUT_DIR, name)
        img = gen_func()
        img.save(path)
        print(f"Generated: {path} ({img.size[0]}x{img.size[1]})")

    print(f"\nAll {len(textures)} textures generated successfully.")


if __name__ == '__main__':
    main()
