from PIL import Image, ImageDraw
import os

OUTPUT_DIR = "/mnt/e/code/mod/gu/src/main/resources/assets/reverend_insanity/textures/item"
SIZE = 64
PX = 4  # effective pixel size

def px(x, y, draw, color):
    """Draw a single 'pixel' (4x4 block) at grid position x,y"""
    draw.rectangle([x*PX, y*PX, (x+1)*PX-1, (y+1)*PX-1], fill=color)

def save(img, name):
    path = os.path.join(OUTPUT_DIR, name)
    img.save(path)
    print(f"Saved: {path}")

# ============================================================
# 1. BONE SPEAR GU - Ivory/bone attack insect with sharp spear-like appendages
# ============================================================
def gen_bone_spear_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0,0,0,0))
    d = ImageDraw.Draw(img)

    ivory =     (0xEE, 0xDD, 0xCC)
    bone_mid =  (0xCC, 0xBB, 0xAA)
    bone_lt =   (0xDD, 0xCC, 0xBB)
    bone_dk =   (0xBB, 0xAA, 0x99)
    white =     (0xFF, 0xFF, 0xFF)
    outline =   (0x88, 0x77, 0x66)
    eye =       (0xCC, 0x33, 0x33)
    shadow =    (0x99, 0x88, 0x77)

    # Spear tip pointing upward (top of texture)
    for r, c in [(1,7),(1,8),(2,7),(2,8)]:
        px(c, r, d, white)
    for r, c in [(3,7),(3,8),(4,6),(4,9)]:
        px(c, r, d, ivory)
    # Spear shaft
    for r in range(5,8):
        px(7, r, d, bone_lt)
        px(8, r, d, bone_mid)

    # Body (oval, centered around row 8-12)
    body_outline = [
        (8,5),(8,10),(9,4),(9,11),(10,4),(10,11),(11,4),(11,11),(12,5),(12,10)
    ]
    for r,c in body_outline:
        px(c, r, d, outline)

    body_fill = []
    for r in range(8,13):
        if r == 8:
            cols = range(6,10)
        elif r in (9,10,11):
            cols = range(5,11)
        else:
            cols = range(6,10)
        for c in cols:
            body_fill.append((r,c))

    for r,c in body_fill:
        if c < 7:
            px(c, r, d, bone_lt)
        elif c > 8:
            px(c, r, d, bone_dk)
        else:
            px(c, r, d, ivory)

    # Bone segment lines on body
    px(6, 9, d, white)
    px(7, 10, d, white)
    px(9, 9, d, white)

    # Eyes (red, menacing)
    px(6, 9, d, eye)
    px(9, 9, d, eye)

    # Sharp leg appendages (spear-like, pointed)
    for r,c in [(9,3),(10,2),(11,3)]:
        px(c, r, d, bone_mid)
    for r,c in [(9,2),(10,1),(11,2)]:
        px(c, r, d, ivory)
    for r,c in [(9,12),(10,13),(11,12)]:
        px(c, r, d, bone_mid)
    for r,c in [(9,13),(10,14),(11,13)]:
        px(c, r, d, ivory)

    # Lower spear appendage / tail spike
    for row in range(13,16):
        px(7, row, d, bone_mid)
        px(8, row, d, bone_lt)

    # Highlight on body
    px(7, 8, d, white)
    px(8, 8, d, white)

    # Shadow under body
    for c in range(6,10):
        px(c, 13, d, shadow)

    save(img, "bone_spear_gu.png")

# ============================================================
# 2. BONE ARMOR GU - Thick bone-plated beetle, heavy exoskeleton
# ============================================================
def gen_bone_armor_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0,0,0,0))
    d = ImageDraw.Draw(img)

    bone1 =    (0xDD, 0xCC, 0xBB)
    bone2 =    (0xCC, 0xBB, 0xAA)
    bone3 =    (0xEE, 0xDD, 0xCC)
    bone_dk =  (0xAA, 0x99, 0x88)
    white =    (0xFF, 0xFF, 0xFF)
    outline =  (0x77, 0x66, 0x55)
    eye =      (0xDD, 0x55, 0x33)
    plate_hi = (0xFF, 0xEE, 0xDD)

    # Head (rows 3-5)
    for c in range(6,10):
        px(c, 3, d, outline)
    for c in range(5,11):
        px(c, 4, d, bone1)
    for c in range(5,11):
        px(c, 5, d, bone2)
    px(6, 4, d, eye)
    px(9, 4, d, eye)
    px(5, 5, d, bone_dk)
    px(10, 5, d, bone_dk)

    # Heavy armored body (rows 6-13)
    for r in range(6, 14):
        if r == 6:
            cstart, cend = 4, 12
        elif r <= 8:
            cstart, cend = 3, 13
        elif r <= 11:
            cstart, cend = 3, 13
        elif r == 12:
            cstart, cend = 4, 12
        else:
            cstart, cend = 5, 11

        for c in range(cstart, cend):
            if c == cstart or c == cend-1:
                px(c, r, d, outline)
            elif (r % 2 == 0):
                px(c, r, d, bone1)
            else:
                px(c, r, d, bone3)

    # Armor plate ridge lines
    for c in range(4, 12):
        px(c, 6, d, plate_hi)
    for c in range(4, 12):
        px(c, 9, d, bone_dk)
    for c in range(4, 12):
        px(c, 12, d, bone_dk)

    # Central spine line
    for r in range(6, 14):
        px(7, r, d, bone_dk)
        px(8, r, d, bone_dk)

    # Highlight on shell
    px(5, 7, d, white)
    px(6, 7, d, white)
    px(5, 8, d, plate_hi)

    # Short sturdy legs
    for r,c in [(8,2),(10,2),(12,2)]:
        px(c, r, d, bone2)
        px(c-1, r, d, bone_dk)
    for r,c in [(8,13),(10,13),(12,13)]:
        px(c, r, d, bone2)
        px(c+1, r, d, bone_dk)

    save(img, "bone_armor_gu.png")

# ============================================================
# 3. WHITE BONE GU - Ghostly white skeletal insect with orbiting bone fragments
# ============================================================
def gen_white_bone_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0,0,0,0))
    d = ImageDraw.Draw(img)

    white =    (0xFF, 0xFF, 0xFF)
    ghost1 =   (0xEE, 0xEE, 0xDD)
    ghost2 =   (0xDD, 0xDD, 0xCC)
    ghost3 =   (0xCC, 0xCC, 0xBB)
    cream =    (0xFF, 0xFF, 0xEE)
    outline =  (0xAA, 0xAA, 0x99)
    glow =     (0xFF, 0xFF, 0xDD, 180)
    eye =      (0x88, 0xFF, 0x88)
    frag =     (0xEE, 0xEE, 0xCC)

    # Orbiting bone fragments
    frags = [(2,3),(1,10),(3,13),(13,2),(14,11),(12,14)]
    for r,c in frags:
        px(c, r, d, frag)

    # Glow aura around fragments
    glow_spots = [(1,3),(2,2),(2,4),(0,10),(1,9),(1,11),(2,13),(3,12),
                  (12,2),(13,1),(13,3),(13,11),(14,10),(14,12),(11,14),(12,13)]
    for r,c in glow_spots:
        if 0 <= r < 16 and 0 <= c < 16:
            px(c, r, d, glow)

    # Skeletal head (rows 4-6)
    for c in range(6,10):
        px(c, 4, d, white)
    for c in range(5,11):
        px(c, 5, d, ghost1)
    for c in range(6,10):
        px(c, 6, d, ghost2)

    px(6, 5, d, eye)
    px(9, 5, d, eye)

    # Skeletal body (rows 7-12)
    for r in range(7, 13):
        if r in (7, 12):
            cstart, cend = 6, 10
        else:
            cstart, cend = 5, 11
        for c in range(cstart, cend):
            if c == cstart or c == cend-1:
                px(c, r, d, outline)
            elif (r + c) % 3 == 0:
                px(c, r, d, white)
            elif (r + c) % 3 == 1:
                px(c, r, d, ghost1)
            else:
                px(c, r, d, ghost2)

    # Rib-like bone segments
    for c in range(6, 10):
        px(c, 8, d, white)
        px(c, 10, d, white)

    # Spine
    for r in range(7, 13):
        px(7, r, d, cream)

    # Ghostly legs
    for r,c in [(8,4),(10,3),(9,4)]:
        px(c, r, d, ghost3)
    for r,c in [(8,11),(10,12),(9,11)]:
        px(c, r, d, ghost3)

    # Tail wisps
    px(7, 13, d, ghost2)
    px(8, 13, d, ghost3)
    px(7, 14, d, (0xDD, 0xDD, 0xCC, 150))

    save(img, "white_bone_gu.png")

# ============================================================
# 4. CLOUD RIDE GU - Light blue wispy insect with cloud-like wings
# ============================================================
def gen_cloud_ride_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0,0,0,0))
    d = ImageDraw.Draw(img)

    sky1 =     (0xAA, 0xDD, 0xFF)
    sky2 =     (0x88, 0xCC, 0xEE)
    sky3 =     (0xBB, 0xEE, 0xFF)
    sky_dk =   (0x66, 0xAA, 0xDD)
    sky_lt =   (0xCC, 0xDD, 0xFF)
    white =    (0xFF, 0xFF, 0xFF)
    cloud =    (0xEE, 0xEE, 0xFF, 200)
    cloud_hi = (0xFF, 0xFF, 0xFF, 220)
    outline =  (0x55, 0x88, 0xBB)
    eye =      (0xFF, 0xFF, 0xDD)

    # Cloud wing - left
    cloud_l = [(4,2),(4,3),(5,1),(5,2),(5,3),(5,4),(6,1),(6,2),(6,3),(6,4),(6,5),
               (7,2),(7,3),(7,4),(8,1),(8,2),(8,3),(8,4),(9,2),(9,3)]
    for r,c in cloud_l:
        px(c, r, d, cloud)
    for r,c in [(5,2),(6,2),(6,3),(7,3),(8,2)]:
        px(c, r, d, cloud_hi)

    # Cloud wing - right
    cloud_r = [(4,12),(4,13),(5,11),(5,12),(5,13),(5,14),(6,10),(6,11),(6,12),(6,13),(6,14),
               (7,11),(7,12),(7,13),(8,11),(8,12),(8,13),(8,14),(9,12),(9,13)]
    for r,c in cloud_r:
        px(c, r, d, cloud)
    for r,c in [(5,13),(6,12),(6,13),(7,12),(8,13)]:
        px(c, r, d, cloud_hi)

    # Body (small, wispy, centered)
    for c in range(6,10):
        px(c, 5, d, sky3)
    px(6, 5, d, eye)
    px(9, 5, d, eye)

    for r in range(6, 10):
        if r in (6,9):
            cstart, cend = 6, 10
        else:
            cstart, cend = 5, 11
        for c in range(cstart, cend):
            if c == cstart or c == cend-1:
                px(c, r, d, outline)
            elif c < 7:
                px(c, r, d, sky2)
            elif c > 8:
                px(c, r, d, sky1)
            else:
                px(c, r, d, sky3)

    px(7, 6, d, white)
    px(6, 7, d, sky_lt)

    for c in range(6,10):
        px(c, 10, d, sky2)
    for c in range(7,9):
        px(c, 11, d, sky_dk)

    # Wispy antennae
    px(6, 4, d, sky_lt)
    px(5, 3, d, sky_lt)
    px(9, 4, d, sky_lt)
    px(10, 3, d, sky_lt)

    # Cloud trail below
    for c in range(6,10):
        px(c, 12, d, (0xDD, 0xEE, 0xFF, 120))
    for c in range(7,9):
        px(c, 13, d, (0xDD, 0xEE, 0xFF, 80))

    save(img, "cloud_ride_gu.png")

# ============================================================
# 5. FLIGHT WING GU - Sleek silver-blue insect with large transparent wings
# ============================================================
def gen_flight_wing_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0,0,0,0))
    d = ImageDraw.Draw(img)

    silver1 =  (0x99, 0xBB, 0xDD)
    silver2 =  (0xAA, 0xCC, 0xEE)
    silver3 =  (0x77, 0xAA, 0xCC)
    silver_lt = (0xBB, 0xDD, 0xEE)
    silver_dk = (0x88, 0xAA, 0xCC)
    outline =  (0x55, 0x77, 0x99)
    wing =     (0xCC, 0xDD, 0xEE, 140)
    wing_hi =  (0xEE, 0xEE, 0xFF, 160)
    wing_ln =  (0x88, 0xAA, 0xCC, 180)
    eye =      (0xDD, 0xEE, 0xFF)
    white =    (0xFF, 0xFF, 0xFF)

    # Large transparent wings - left
    wing_l_pts = [(2,3),(2,4),(3,2),(3,3),(3,4),(3,5),(4,1),(4,2),(4,3),(4,4),(4,5),
                  (5,1),(5,2),(5,3),(5,4),(5,5),(6,2),(6,3),(6,4),(6,5),
                  (7,2),(7,3),(7,4),(8,3),(8,4),(9,3),(9,4)]
    for r,c in wing_l_pts:
        px(c, r, d, wing)
    for r,c in [(3,3),(4,2),(5,2),(6,3),(7,3),(4,4),(5,4)]:
        px(c, r, d, wing_ln)
    for r,c in [(3,4),(4,3),(5,3)]:
        px(c, r, d, wing_hi)

    # Large transparent wings - right
    wing_r_pts = [(2,11),(2,12),(3,10),(3,11),(3,12),(3,13),(4,10),(4,11),(4,12),(4,13),(4,14),
                  (5,10),(5,11),(5,12),(5,13),(5,14),(6,10),(6,11),(6,12),(6,13),
                  (7,11),(7,12),(7,13),(8,11),(8,12),(9,11),(9,12)]
    for r,c in wing_r_pts:
        px(c, r, d, wing)
    for r,c in [(3,12),(4,13),(5,13),(6,12),(7,12),(4,11),(5,11)]:
        px(c, r, d, wing_ln)
    for r,c in [(3,11),(4,12),(5,12)]:
        px(c, r, d, wing_hi)

    # Sleek aerodynamic body
    px(7, 4, d, silver2)
    px(8, 4, d, silver2)
    for c in range(6,10):
        px(c, 5, d, silver1)
    px(6, 5, d, eye)
    px(9, 5, d, eye)

    for r in range(6,10):
        if r in (6,9):
            cstart, cend = 6, 10
        else:
            cstart, cend = 5, 11
        for c in range(cstart, cend):
            if c == cstart or c == cend-1:
                px(c, r, d, outline)
            elif c <= 7:
                px(c, r, d, silver1)
            else:
                px(c, r, d, silver3)

    px(7, 6, d, white)
    px(7, 7, d, silver_lt)
    px(8, 6, d, silver_lt)

    for c in range(6,10):
        px(c, 10, d, silver1)
    for c in range(7,9):
        px(c, 11, d, silver_dk)
    px(7, 12, d, silver3)
    px(8, 12, d, outline)

    for r,c in [(7,4),(8,4),(9,5)]:
        px(c, r, d, silver_dk)
    for r,c in [(7,11),(8,11),(9,10)]:
        px(c, r, d, silver_dk)

    save(img, "flight_wing_gu.png")

# ============================================================
# 6. SKY EAGLE GU - Majestic blue-gold insect with eagle-like wing spread
# ============================================================
def gen_sky_eagle_gu():
    img = Image.new("RGBA", (SIZE, SIZE), (0,0,0,0))
    d = ImageDraw.Draw(img)

    blue1 =    (0x44, 0x88, 0xCC)
    gold1 =    (0xDD, 0xAA, 0x33)
    blue2 =    (0x66, 0x99, 0xDD)
    gold2 =    (0xFF, 0xCC, 0x44)
    blue_dk =  (0x33, 0x77, 0xBB)
    outline =  (0x22, 0x55, 0x88)
    eye =      (0xFF, 0xDD, 0x44)
    white =    (0xFF, 0xFF, 0xFF)
    gold_dk =  (0xBB, 0x88, 0x22)
    blue_lt =  (0x88, 0xBB, 0xEE)

    # Eagle-like spread wings - LEFT
    for c in range(1,6):
        px(c, 3, d, blue2)
    for c in range(0,6):
        px(c, 4, d, blue1)
    for c in range(0,6):
        px(c, 5, d, blue2)
    for c in range(1,6):
        px(c, 6, d, blue1)
    for c in range(2,6):
        px(c, 7, d, blue_dk)
    for c in range(3,6):
        px(c, 8, d, blue_dk)
    px(0, 4, d, gold1)
    px(0, 5, d, gold2)
    px(1, 3, d, gold1)
    px(1, 6, d, gold1)
    px(2, 4, d, blue_lt)
    px(3, 4, d, blue_lt)

    # Eagle-like spread wings - RIGHT
    for c in range(10,15):
        px(c, 3, d, blue2)
    for c in range(10,16):
        px(c, 4, d, blue1)
    for c in range(10,16):
        px(c, 5, d, blue2)
    for c in range(10,15):
        px(c, 6, d, blue1)
    for c in range(10,14):
        px(c, 7, d, blue_dk)
    for c in range(10,13):
        px(c, 8, d, blue_dk)
    px(15, 4, d, gold1)
    px(15, 5, d, gold2)
    px(14, 3, d, gold1)
    px(14, 6, d, gold1)
    px(13, 4, d, blue_lt)
    px(12, 4, d, blue_lt)

    # Head
    for c in range(6,10):
        px(c, 4, d, blue1)
    px(7, 3, d, gold2)
    px(8, 3, d, gold2)
    px(6, 4, d, eye)
    px(9, 4, d, eye)
    for c in range(6,10):
        px(c, 5, d, blue2)

    # Body
    for r in range(6, 12):
        if r in (6, 11):
            cstart, cend = 6, 10
        else:
            cstart, cend = 5, 11
        for c in range(cstart, cend):
            if c == cstart or c == cend-1:
                px(c, r, d, outline)
            elif (r % 2 == 0):
                px(c, r, d, blue1)
            else:
                px(c, r, d, blue2)

    # Gold chest plate
    for r in range(7, 10):
        px(7, r, d, gold1)
        px(8, r, d, gold_dk)

    px(6, 6, d, blue_lt)
    px(7, 6, d, white)

    # Tail feathers
    px(6, 12, d, blue_dk)
    px(7, 12, d, blue1)
    px(8, 12, d, blue1)
    px(9, 12, d, blue_dk)
    px(5, 13, d, gold1)
    px(6, 13, d, blue2)
    px(7, 13, d, blue_dk)
    px(8, 13, d, blue_dk)
    px(9, 13, d, blue2)
    px(10, 13, d, gold1)

    px(6, 12, d, gold_dk)
    px(9, 12, d, gold_dk)

    save(img, "sky_eagle_gu.png")


# Generate all 6 textures
gen_bone_spear_gu()
gen_bone_armor_gu()
gen_white_bone_gu()
gen_cloud_ride_gu()
gen_flight_wing_gu()
gen_sky_eagle_gu()

print("\nAll 6 Gu worm textures generated successfully!")
