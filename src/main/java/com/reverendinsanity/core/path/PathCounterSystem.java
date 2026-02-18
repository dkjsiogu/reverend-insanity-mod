package com.reverendinsanity.core.path;

import java.util.*;

// 道路克制系统：定义48道路之间的五行相克关系
public class PathCounterSystem {

    private static final Map<DaoPath, Set<DaoPath>> counterMap = new EnumMap<>(DaoPath.class);

    static {
        counter(DaoPath.ICE, DaoPath.FIRE);
        counter(DaoPath.FIRE, DaoPath.WOOD);
        counter(DaoPath.WOOD, DaoPath.WATER);
        counter(DaoPath.WATER, DaoPath.ICE);

        counter(DaoPath.LIGHT, DaoPath.DARK);
        counter(DaoPath.DARK, DaoPath.ILLUSION);
        counter(DaoPath.ILLUSION, DaoPath.LIGHT);

        counter(DaoPath.STRENGTH, DaoPath.BONE);
        counter(DaoPath.BONE, DaoPath.POISON);
        counter(DaoPath.POISON, DaoPath.STRENGTH);

        counter(DaoPath.WIND, DaoPath.EARTH);
        counter(DaoPath.EARTH, DaoPath.LIGHTNING);
        counter(DaoPath.LIGHTNING, DaoPath.WIND);

        counter(DaoPath.SOUL, DaoPath.DREAM);
        counter(DaoPath.DREAM, DaoPath.CHARM);
        counter(DaoPath.CHARM, DaoPath.SOUL);

        counter(DaoPath.SWORD, DaoPath.BLADE);
        counter(DaoPath.BLADE, DaoPath.SOLDIER);
        counter(DaoPath.SOLDIER, DaoPath.SWORD);

        counter(DaoPath.SPACE, DaoPath.TIME);
        counter(DaoPath.TIME, DaoPath.RULE);
        counter(DaoPath.RULE, DaoPath.SPACE);

        counter(DaoPath.METAL, DaoPath.WOOD);
        counter(DaoPath.WATER, DaoPath.FIRE);

        counter(DaoPath.SHADOW, DaoPath.LIGHT);
        counter(DaoPath.KILL, DaoPath.HUMAN);
        counter(DaoPath.HEAVEN, DaoPath.HUMAN);
        counter(DaoPath.STEAL, DaoPath.FORMATION);
        counter(DaoPath.VOID, DaoPath.RESTRICTION);

        counter(DaoPath.BLOOD, DaoPath.SOUL);
        counter(DaoPath.QI, DaoPath.POISON);
        counter(DaoPath.MOON, DaoPath.DARK);
        counter(DaoPath.STAR, DaoPath.CLOUD);
        counter(DaoPath.ENSLAVE, DaoPath.ILLUSION);
    }

    private static void counter(DaoPath attacker, DaoPath target) {
        counterMap.computeIfAbsent(attacker, k -> EnumSet.noneOf(DaoPath.class)).add(target);
    }

    public static float getDamageMultiplier(DaoPath attackerPath, DaoPath targetPath) {
        if (attackerPath == null || targetPath == null) return 1.0f;
        if (isCountered(attackerPath, targetPath)) return 1.25f;
        if (isCountered(targetPath, attackerPath)) return 0.8f;
        return 1.0f;
    }

    public static boolean isCountered(DaoPath attacker, DaoPath target) {
        Set<DaoPath> countered = counterMap.get(attacker);
        return countered != null && countered.contains(target);
    }

    public static DaoPath getWeakness(DaoPath target) {
        for (Map.Entry<DaoPath, Set<DaoPath>> entry : counterMap.entrySet()) {
            if (entry.getValue().contains(target)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static Set<DaoPath> getAllCountered(DaoPath attacker) {
        return counterMap.getOrDefault(attacker, Collections.emptySet());
    }
}
