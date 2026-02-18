#!/usr/bin/env python3
"""为所有蛊虫物品贴图生成6帧动画效果，根据道路类型应用不同视觉风格。"""

import os
import json
import math
from PIL import Image

TEXTURE_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)),
    "src", "main", "resources", "assets", "reverend_insanity", "textures", "item")

NUM_FRAMES = 6

# 蛊虫ID -> 道路映射 (从GuRegistry提取)
GU_PATH_MAP = {
    "hope_gu": "HUMAN",
    "moonlight_gu": "MOON",
    "bear_strength_gu": "STRENGTH",
    "jade_skin_gu": "METAL",
    "white_boar_gu": "STRENGTH",
    "stealth_scales_gu": "WOOD",
    "gold_light_worm": "METAL",
    "iron_bone_gu": "STRENGTH",
    "enslave_snake_gu": "ENSLAVE",
    "moonscar_gu": "MOON",
    "silver_moon_gu": "MOON",
    "white_jade_gu": "METAL",
    "heavens_eye_gu": "WISDOM",
    "flesh_bone_gu": "BLOOD",
    "displacement_gu": "SPACE",
    "blood_gu": "BLOOD",
    "self_heal_gu": "BLOOD",
    "solidify_origin_gu": "BLOOD",
    "blood_wing_gu": "BLOOD",
    "poison_bee_gu": "POISON",
    "gold_silkworm_gu": "POISON",
    "savage_bull_gu": "STRENGTH",
    "taishan_gu": "STRENGTH",
    "giant_strength_gu": "STRENGTH",
    "cold_ice_gu": "ICE",
    "frost_armor_gu": "ICE",
    "ice_seal_gu": "ICE",
    "fire_seed_gu": "FIRE",
    "flame_armor_gu": "FIRE",
    "blazing_flame_gu": "FIRE",
    "earth_wall_gu": "EARTH",
    "earth_split_gu": "EARTH",
    "petrify_gu": "EARTH",
    "breeze_gu": "WIND",
    "wind_blade_gu": "WIND",
    "gale_gu": "WIND",
    "lightning_gu": "LIGHTNING",
    "thunder_shield_gu": "LIGHTNING",
    "thunderstorm_gu": "LIGHTNING",
    "tide_gu": "WATER",
    "water_shield_gu": "WATER",
    "torrent_gu": "WATER",
    "soul_search_gu": "SOUL",
    "soul_shield_gu": "SOUL",
    "soul_crush_gu": "SOUL",
    "light_beam_gu": "LIGHT",
    "radiance_gu": "LIGHT",
    "blazing_light_gu": "LIGHT",
    "dark_bolt_gu": "DARK",
    "shadow_cloak_gu": "DARK",
    "abyss_devour_gu": "DARK",
    "dream_gu": "DREAM",
    "lucid_dream_gu": "DREAM",
    "nightmare_gu": "DREAM",
    "phantom_gu": "ILLUSION",
    "mirage_gu": "ILLUSION",
    "grand_illusion_gu": "ILLUSION",
    "flying_sword_gu": "SWORD",
    "sword_shield_gu": "SWORD",
    "myriad_sword_gu": "SWORD",
    "moon_slash_gu": "BLADE",
    "blade_armor_gu": "BLADE",
    "heaven_blade_gu": "BLADE",
    "starlight_gu": "STAR",
    "star_shield_gu": "STAR",
    "star_fall_gu": "STAR",
    "lucky_gu": "LUCK",
    "misfortune_ward_gu": "LUCK",
    "heavens_secret_gu": "LUCK",
    "kill_intent_gu": "KILL",
    "killing_chance_gu": "KILL",
    "death_strike_gu": "KILL",
    "shrink_ground_gu": "TRANSFORMATION",
    "morph_gu": "TRANSFORMATION",
    "heaven_change_gu": "TRANSFORMATION",
    "formation_soldier_gu": "SOLDIER",
    "golden_armor_gu": "SOLDIER",
    "thousand_army_gu": "SOLDIER",
    "sound_wave_gu": "SOUND",
    "silence_gu": "SOUND",
    "heavenly_sound_gu": "SOUND",
    "bone_spear_gu": "BONE",
    "bone_armor_gu": "BONE",
    "white_bone_gu": "BONE",
    "cloud_ride_gu": "FLIGHT",
    "flight_wing_gu": "FLIGHT",
    "sky_eagle_gu": "FLIGHT",
    "true_qi_gu": "QI",
    "qi_shield_gu": "QI",
    "profound_qi_gu": "QI",
    "yin_yang_gu": "YIN_YANG",
    "tai_chi_gu": "YIN_YANG",
    "primordial_gu": "YIN_YANG",
    "warp_gu": "SPACE",
    "space_barrier_gu": "SPACE",
    "time_decel_gu": "TIME",
    "time_shield_gu": "TIME",
    "time_reversal_gu": "TIME",
    "charm_gu": "CHARM",
    "bewitch_gu": "CHARM",
    "soul_charm_gu": "CHARM",
    "thought_gu": "WISDOM",
    "mind_guard_gu": "WISDOM",
    "void_bolt_gu": "VOID",
    "void_cloak_gu": "VOID",
    "void_annihilation_gu": "VOID",
    "seal_gu": "RESTRICTION",
    "restriction_gu": "RESTRICTION",
    "heaven_seal_gu": "RESTRICTION",
    "heaven_will_gu": "HEAVEN",
    "heaven_shield_gu": "HEAVEN",
    "heaven_punishment_gu": "HEAVEN",
    "rule_gu": "RULE",
    "order_gu": "RULE",
    "supreme_law_gu": "RULE",
    "shadow_dart_gu": "SHADOW",
    "shadow_veil_gu": "SHADOW",
    "shadow_devour_gu": "SHADOW",
    "mist_gu": "CLOUD",
    "cloud_armor_gu": "CLOUD",
    "cloud_storm_gu": "CLOUD",
    "trap_formation_gu": "FORMATION",
    "formation_shield_gu": "FORMATION",
    "grand_formation_gu": "FORMATION",
    "refine_fire_gu": "REFINEMENT",
    "refine_body_gu": "REFINEMENT",
    "heaven_refine_gu": "REFINEMENT",
    "pill_poison_gu": "PILL",
    "pill_shield_gu": "PILL",
    "immortal_pill_gu": "PILL",
    "paint_brush_gu": "PAINT",
    "paint_shield_gu": "PAINT",
    "myriad_paint_gu": "PAINT",
    "steal_qi_gu": "STEAL",
    "steal_hide_gu": "STEAL",
    "heaven_steal_gu": "STEAL",
    "info_dart_gu": "INFORMATION",
    "info_net_gu": "INFORMATION",
    "heaven_info_gu": "INFORMATION",
    "human_heart_gu": "HUMAN",
    "human_bond_gu": "HUMAN",
    "human_will_gu": "HUMAN",
    "enslave_worm_gu": "ENSLAVE",
    "enslave_shield_gu": "ENSLAVE",
    "feast_gu": "FOOD",
}

# 蛊虫ID -> 转数 (rank)，用于决定动画强度
GU_RANK_MAP = {
    "hope_gu": 1,
    "moonlight_gu": 1, "moonscar_gu": 2, "silver_moon_gu": 3,
    "jade_skin_gu": 1, "gold_light_worm": 2, "white_jade_gu": 3,
    "stealth_scales_gu": 1,
    "bear_strength_gu": 1, "white_boar_gu": 1, "savage_bull_gu": 1,
    "taishan_gu": 1, "iron_bone_gu": 2, "giant_strength_gu": 2,
    "flesh_bone_gu": 1, "blood_gu": 1, "self_heal_gu": 1,
    "solidify_origin_gu": 1, "blood_wing_gu": 2,
    "poison_bee_gu": 1, "gold_silkworm_gu": 2,
    "cold_ice_gu": 1, "frost_armor_gu": 1, "ice_seal_gu": 2,
    "fire_seed_gu": 1, "flame_armor_gu": 1, "blazing_flame_gu": 2,
    "earth_wall_gu": 1, "earth_split_gu": 1, "petrify_gu": 2,
    "breeze_gu": 1, "wind_blade_gu": 1, "gale_gu": 2,
    "lightning_gu": 1, "thunder_shield_gu": 1, "thunderstorm_gu": 2,
    "tide_gu": 1, "water_shield_gu": 1, "torrent_gu": 2,
    "soul_search_gu": 1, "soul_shield_gu": 1, "soul_crush_gu": 2,
    "light_beam_gu": 1, "radiance_gu": 1, "blazing_light_gu": 2,
    "dark_bolt_gu": 1, "shadow_cloak_gu": 1, "abyss_devour_gu": 2,
    "dream_gu": 1, "lucid_dream_gu": 1, "nightmare_gu": 2,
    "phantom_gu": 1, "mirage_gu": 1, "grand_illusion_gu": 2,
    "flying_sword_gu": 1, "sword_shield_gu": 1, "myriad_sword_gu": 2,
    "moon_slash_gu": 1, "blade_armor_gu": 1, "heaven_blade_gu": 2,
    "starlight_gu": 1, "star_shield_gu": 1, "star_fall_gu": 2,
    "lucky_gu": 1, "misfortune_ward_gu": 1, "heavens_secret_gu": 2,
    "kill_intent_gu": 1, "killing_chance_gu": 1, "death_strike_gu": 2,
    "shrink_ground_gu": 1, "morph_gu": 1, "heaven_change_gu": 2,
    "formation_soldier_gu": 1, "golden_armor_gu": 1, "thousand_army_gu": 2,
    "sound_wave_gu": 1, "silence_gu": 1, "heavenly_sound_gu": 2,
    "bone_spear_gu": 1, "bone_armor_gu": 1, "white_bone_gu": 2,
    "cloud_ride_gu": 1, "flight_wing_gu": 1, "sky_eagle_gu": 2,
    "true_qi_gu": 1, "qi_shield_gu": 1, "profound_qi_gu": 2,
    "yin_yang_gu": 1, "tai_chi_gu": 1, "primordial_gu": 2,
    "warp_gu": 1, "space_barrier_gu": 1, "displacement_gu": 2,
    "time_decel_gu": 1, "time_shield_gu": 1, "time_reversal_gu": 2,
    "charm_gu": 1, "bewitch_gu": 1, "soul_charm_gu": 2,
    "thought_gu": 1, "mind_guard_gu": 1, "heavens_eye_gu": 3,
    "void_bolt_gu": 1, "void_cloak_gu": 1, "void_annihilation_gu": 2,
    "seal_gu": 1, "restriction_gu": 1, "heaven_seal_gu": 2,
    "heaven_will_gu": 1, "heaven_shield_gu": 1, "heaven_punishment_gu": 2,
    "rule_gu": 1, "order_gu": 1, "supreme_law_gu": 2,
    "shadow_dart_gu": 1, "shadow_veil_gu": 1, "shadow_devour_gu": 2,
    "mist_gu": 1, "cloud_armor_gu": 1, "cloud_storm_gu": 2,
    "trap_formation_gu": 1, "formation_shield_gu": 1, "grand_formation_gu": 2,
    "refine_fire_gu": 1, "refine_body_gu": 1, "heaven_refine_gu": 2,
    "pill_poison_gu": 1, "pill_shield_gu": 1, "immortal_pill_gu": 2,
    "paint_brush_gu": 1, "paint_shield_gu": 1, "myriad_paint_gu": 2,
    "steal_qi_gu": 1, "steal_hide_gu": 1, "heaven_steal_gu": 2,
    "info_dart_gu": 1, "info_net_gu": 1, "heaven_info_gu": 2,
    "human_heart_gu": 1, "human_bond_gu": 1, "human_will_gu": 2,
    "enslave_worm_gu": 1, "enslave_shield_gu": 1, "enslave_snake_gu": 2,
    "feast_gu": 1,
}

# 道路 -> 动画参数 (r_shift, g_shift, b_shift, style)
# style: "pulse"=正弦脉冲, "flicker"=随机闪烁, "wave"=波纹, "dual"=双色交替
PATH_EFFECTS = {
    "MOON":           {"r": -5,  "g": -5,  "b": 15,  "style": "pulse",   "hue": (200, 210, 255)},
    "METAL":          {"r": 15,  "g": 12,  "b": -3,  "style": "flicker", "hue": (255, 230, 150)},
    "WOOD":           {"r": -8,  "g": 15,  "b": -5,  "style": "pulse",   "hue": (100, 220, 100)},
    "BLOOD":          {"r": 18,  "g": -8,  "b": -8,  "style": "pulse",   "hue": (200, 50, 50)},
    "POISON":         {"r": -5,  "g": 12,  "b": 15,  "style": "dual",    "hue": (130, 200, 80)},
    "STRENGTH":       {"r": 18,  "g": 8,   "b": -5,  "style": "pulse",   "hue": (255, 160, 60)},
    "ICE":            {"r": -5,  "g": 10,  "b": 20,  "style": "flicker", "hue": (160, 220, 255)},
    "FIRE":           {"r": 20,  "g": 5,   "b": -8,  "style": "pulse",   "hue": (255, 120, 40)},
    "EARTH":          {"r": 10,  "g": 6,   "b": -3,  "style": "pulse",   "hue": (180, 140, 80)},
    "WIND":           {"r": -5,  "g": 12,  "b": 10,  "style": "wave",    "hue": (150, 230, 200)},
    "LIGHTNING":      {"r": 5,   "g": 5,   "b": 20,  "style": "flicker", "hue": (180, 180, 255)},
    "WATER":          {"r": -8,  "g": 5,   "b": 18,  "style": "wave",    "hue": (80, 140, 220)},
    "SOUL":           {"r": -5,  "g": 5,   "b": 18,  "style": "pulse",   "hue": (120, 160, 255)},
    "LIGHT":          {"r": 15,  "g": 15,  "b": 15,  "style": "pulse",   "hue": (255, 255, 240)},
    "DARK":           {"r": 8,   "g": -5,  "b": 15,  "style": "pulse",   "hue": (100, 60, 160)},
    "DREAM":          {"r": 10,  "g": 5,   "b": 15,  "style": "wave",    "hue": (180, 140, 220)},
    "ILLUSION":       {"r": 8,   "g": -3,  "b": 12,  "style": "dual",    "hue": (170, 120, 210)},
    "SWORD":          {"r": 10,  "g": 10,  "b": 15,  "style": "flicker", "hue": (200, 200, 240)},
    "BLADE":          {"r": 12,  "g": 8,   "b": 5,   "style": "flicker", "hue": (220, 180, 150)},
    "STAR":           {"r": 10,  "g": 8,   "b": 18,  "style": "flicker", "hue": (200, 190, 255)},
    "LUCK":           {"r": 15,  "g": 15,  "b": -5,  "style": "pulse",   "hue": (255, 230, 80)},
    "KILL":           {"r": 18,  "g": -5,  "b": -5,  "style": "pulse",   "hue": (220, 50, 50)},
    "TRANSFORMATION": {"r": 8,   "g": 12,  "b": 8,   "style": "dual",    "hue": (160, 200, 160)},
    "SOLDIER":        {"r": 12,  "g": 10,  "b": 5,   "style": "pulse",   "hue": (210, 180, 120)},
    "SOUND":          {"r": 10,  "g": 10,  "b": 12,  "style": "wave",    "hue": (200, 200, 220)},
    "BONE":           {"r": 12,  "g": 12,  "b": 8,   "style": "pulse",   "hue": (230, 220, 190)},
    "FLIGHT":         {"r": -3,  "g": 12,  "b": 15,  "style": "wave",    "hue": (160, 220, 240)},
    "QI":             {"r": 8,   "g": 15,  "b": 8,   "style": "pulse",   "hue": (160, 230, 160)},
    "YIN_YANG":       {"r": 10,  "g": 10,  "b": 10,  "style": "dual",    "hue": (200, 200, 200)},
    "SPACE":          {"r": 8,   "g": -3,  "b": 18,  "style": "wave",    "hue": (140, 100, 220)},
    "TIME":           {"r": 15,  "g": 12,  "b": -3,  "style": "pulse",   "hue": (240, 210, 120)},
    "CHARM":          {"r": 18,  "g": 5,   "b": 12,  "style": "pulse",   "hue": (255, 140, 200)},
    "WISDOM":         {"r": 8,   "g": 12,  "b": 15,  "style": "pulse",   "hue": (160, 200, 240)},
    "VOID":           {"r": 5,   "g": -5,  "b": 15,  "style": "wave",    "hue": (120, 80, 200)},
    "RESTRICTION":    {"r": 10,  "g": 5,   "b": 5,   "style": "pulse",   "hue": (200, 150, 150)},
    "HEAVEN":         {"r": 15,  "g": 15,  "b": 18,  "style": "pulse",   "hue": (240, 240, 255)},
    "RULE":           {"r": 12,  "g": 12,  "b": 12,  "style": "pulse",   "hue": (210, 210, 210)},
    "SHADOW":         {"r": -3,  "g": -5,  "b": 8,   "style": "pulse",   "hue": (80, 70, 120)},
    "CLOUD":          {"r": 5,   "g": 8,   "b": 12,  "style": "wave",    "hue": (180, 200, 230)},
    "FORMATION":      {"r": 8,   "g": 5,   "b": 15,  "style": "flicker", "hue": (170, 150, 230)},
    "REFINEMENT":     {"r": 18,  "g": 8,   "b": -3,  "style": "pulse",   "hue": (255, 160, 80)},
    "PILL":           {"r": 5,   "g": 15,  "b": 8,   "style": "pulse",   "hue": (140, 220, 160)},
    "PAINT":          {"r": 15,  "g": 8,   "b": 15,  "style": "dual",    "hue": (230, 160, 230)},
    "STEAL":          {"r": -3,  "g": -3,  "b": 10,  "style": "wave",    "hue": (100, 100, 160)},
    "INFORMATION":    {"r": 5,   "g": 10,  "b": 15,  "style": "pulse",   "hue": (150, 190, 240)},
    "HUMAN":          {"r": 15,  "g": 10,  "b": 5,   "style": "pulse",   "hue": (240, 200, 150)},
    "ENSLAVE":        {"r": 10,  "g": -3,  "b": 8,   "style": "pulse",   "hue": (180, 100, 160)},
    "FOOD":           {"r": 12,  "g": 10,  "b": -3,  "style": "pulse",   "hue": (230, 200, 100)},
}

DEFAULT_EFFECT = {"r": 5, "g": 8, "b": 12, "style": "pulse", "hue": (160, 190, 230)}


def clamp(v, lo=0, hi=255):
    return max(lo, min(hi, int(v)))


def generate_frame(original, frame_idx, num_frames, effect, intensity):
    """生成单帧动画图像。"""
    phase = frame_idx / num_frames
    angle = phase * 2 * math.pi
    img = original.copy()
    pixels = img.load()
    w, h = img.size
    style = effect["style"]
    r_shift = effect["r"]
    g_shift = effect["g"]
    b_shift = effect["b"]

    for y in range(h):
        for x in range(w):
            r, g, b, a = pixels[x, y]
            if a == 0:
                continue

            if style == "pulse":
                # 正弦脉冲：全局亮度随帧变化
                factor = math.sin(angle) * intensity
                dr = factor + r_shift * math.sin(angle) * 0.5
                dg = factor + g_shift * math.sin(angle) * 0.5
                db = factor + b_shift * math.sin(angle) * 0.5

            elif style == "flicker":
                # 金属闪烁：帧间跳跃式亮度变化，模拟高光闪烁
                # 用不同频率的正弦叠加，产生不规则闪烁
                f1 = math.sin(angle) * 0.6
                f2 = math.sin(angle * 2.7 + 1.3) * 0.4
                factor = (f1 + f2) * intensity
                dr = factor + r_shift * abs(math.sin(angle * 1.5))
                dg = factor + g_shift * abs(math.sin(angle * 1.5))
                db = factor + b_shift * abs(math.sin(angle * 1.5))

            elif style == "wave":
                # 波纹：从中心向外扩散的亮度波
                cx, cy = w / 2, h / 2
                dist = math.sqrt((x - cx) ** 2 + (y - cy) ** 2) / (w / 2)
                wave = math.sin(angle - dist * math.pi * 2) * intensity
                dr = wave * 0.7 + r_shift * math.sin(angle) * 0.4
                dg = wave * 0.7 + g_shift * math.sin(angle) * 0.4
                db = wave * 0.7 + b_shift * math.sin(angle) * 0.4

            elif style == "dual":
                # 双色交替：在原色和道路色之间过渡
                blend = (math.sin(angle) + 1) / 2  # 0~1
                hue = effect["hue"]
                # 微妙地混入道路色
                mix = blend * intensity / 80  # 很轻微的混合
                dr = (hue[0] - r) * mix + r_shift * math.sin(angle) * 0.3
                dg = (hue[1] - g) * mix + g_shift * math.sin(angle) * 0.3
                db = (hue[2] - b) * mix + b_shift * math.sin(angle) * 0.3
            else:
                dr = dg = db = 0

            pixels[x, y] = (clamp(r + dr), clamp(g + dg), clamp(b + db), a)

    return img


def process_gu_texture(filename):
    """处理单个蛊虫贴图，生成6帧动画。"""
    gu_id = filename.replace(".png", "")
    path_type = GU_PATH_MAP.get(gu_id, None)
    rank = GU_RANK_MAP.get(gu_id, 1)
    effect = PATH_EFFECTS.get(path_type, DEFAULT_EFFECT) if path_type else DEFAULT_EFFECT

    # 根据转数决定动画强度
    if rank >= 3:
        intensity = 28
    elif rank >= 2:
        intensity = 22
    else:
        intensity = 15

    filepath = os.path.join(TEXTURE_DIR, filename)
    original = Image.open(filepath).convert("RGBA")
    w, h = original.size

    # 创建垂直堆叠的多帧图像
    result = Image.new("RGBA", (w, h * NUM_FRAMES), (0, 0, 0, 0))

    for i in range(NUM_FRAMES):
        frame = generate_frame(original, i, NUM_FRAMES, effect, intensity)
        result.paste(frame, (0, i * h))

    # 覆盖原文件
    result.save(filepath, "PNG")

    # 生成mcmeta
    mcmeta_path = filepath + ".mcmeta"
    mcmeta = {"animation": {"frametime": 4, "interpolate": True}}
    with open(mcmeta_path, "w") as f:
        json.dump(mcmeta, f)

    return gu_id, path_type or "DEFAULT", rank, intensity


def main():
    if not os.path.isdir(TEXTURE_DIR):
        print(f"ERROR: Texture directory not found: {TEXTURE_DIR}")
        return

    gu_files = sorted(f for f in os.listdir(TEXTURE_DIR) if f.endswith("_gu.png"))
    print(f"Found {len(gu_files)} gu textures to animate")

    results = []
    for filename in gu_files:
        try:
            gu_id, path_type, rank, intensity = process_gu_texture(filename)
            results.append((gu_id, path_type, rank, intensity))
            print(f"  [OK] {gu_id}: path={path_type}, rank={rank}, intensity={intensity}")
        except Exception as e:
            print(f"  [FAIL] {filename}: {e}")

    print(f"\nDone! Animated {len(results)}/{len(gu_files)} textures ({NUM_FRAMES} frames each)")
    print(f"Generated {len(results)} .mcmeta files")

    # 统计
    paths_used = {}
    for _, p, _, _ in results:
        paths_used[p] = paths_used.get(p, 0) + 1
    print("\nPath distribution:")
    for p in sorted(paths_used.keys()):
        print(f"  {p}: {paths_used[p]}")


if __name__ == "__main__":
    main()
