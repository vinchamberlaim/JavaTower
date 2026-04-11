package javatower.systems;

import java.util.HashMap;
import java.util.Map;
import javatower.entities.Item.WeaponClass;

/**
 * Use-based skill progression system. The hero learns by doing — each weapon class
 * has its own skill level that increases through use. Higher levels require
 * exponentially more XP (diminishing returns). Each level grants passive modifiers
 * when using items of that class.
 */
public class SkillProgression {
    private Map<WeaponClass, Integer> skillLevels;
    private Map<WeaponClass, Double> skillXP;

    /** Base XP required for level 1. */
    private static final double BASE_XP = 20.0;
    /** Exponent for diminishing returns — each level needs more XP. */
    private static final double SCALING_EXPONENT = 1.6;
    /** Maximum skill level. */
    public static final int MAX_LEVEL = 20;

    public SkillProgression() {
        skillLevels = new HashMap<>();
        skillXP = new HashMap<>();
        for (WeaponClass wc : WeaponClass.values()) {
            if (wc != WeaponClass.NONE) {
                skillLevels.put(wc, 0);
                skillXP.put(wc, 0.0);
            }
        }
    }

    /**
     * Awards XP to a weapon class skill from using that item type.
     * Returns true if the skill levelled up.
     */
    public boolean addXP(WeaponClass wc, double amount) {
        if (wc == WeaponClass.NONE) return false;
        int currentLevel = skillLevels.getOrDefault(wc, 0);
        if (currentLevel >= MAX_LEVEL) return false;

        double currentXP = skillXP.getOrDefault(wc, 0.0) + amount;
        double needed = xpForNextLevel(currentLevel);

        if (currentXP >= needed) {
            currentXP -= needed;
            skillLevels.put(wc, currentLevel + 1);
            skillXP.put(wc, currentXP);
            return true;
        }
        skillXP.put(wc, currentXP);
        return false;
    }

    /**
     * XP required to advance from the given level to the next.
     * Scales exponentially — slow progress at higher levels.
     */
    public double xpForNextLevel(int currentLevel) {
        return BASE_XP * Math.pow(currentLevel + 1, SCALING_EXPONENT);
    }

    public int getLevel(WeaponClass wc) {
        return skillLevels.getOrDefault(wc, 0);
    }

    public double getXP(WeaponClass wc) {
        return skillXP.getOrDefault(wc, 0.0);
    }

    public double getXPToNext(WeaponClass wc) {
        return xpForNextLevel(getLevel(wc));
    }

    /**
     * Returns the damage multiplier bonus for the given weapon class.
     * Each level adds 3% bonus damage (e.g., level 10 = +30%).
     */
    public double getDamageMultiplier(WeaponClass wc) {
        return 1.0 + getLevel(wc) * 0.03;
    }

    /**
     * Melee skill bonus: attack speed reduction (seconds off cooldown).
     * Each level gives 0.02s faster attacks.
     */
    public double getMeleeSpeedBonus() {
        return getLevel(WeaponClass.MELEE) * 0.02;
    }

    /**
     * Ranged skill bonus: extra range in pixels.
     * Each level gives +8px range.
     */
    public double getRangedRangeBonus() {
        return getLevel(WeaponClass.RANGED) * 8.0;
    }

    /**
     * Necromancy skill bonus: summon HP multiplier.
     * Each level gives +5% HP to summoned creatures.
     */
    public double getNecromancySummonBonus() {
        return 1.0 + getLevel(WeaponClass.NECROMANCY) * 0.05;
    }

    /**
     * Holy skill bonus: heal effectiveness multiplier.
     * Each level gives +5% stronger heals.
     */
    public double getHolyHealBonus() {
        return 1.0 + getLevel(WeaponClass.HOLY) * 0.05;
    }

    /**
     * Defence skill bonus: flat damage reduction.
     * Each level gives +1 damage reduction.
     */
    public int getDefenceBonus() {
        return getLevel(WeaponClass.DEFENCE);
    }

    /**
     * Returns a map of all skill levels for display.
     */
    public Map<WeaponClass, Integer> getAllLevels() {
        return new HashMap<>(skillLevels);
    }

    /**
     * Restores a skill level and carry-over XP from save data.
     */
    public void setSkillState(WeaponClass wc, int level, double xp) {
        if (wc == null || wc == WeaponClass.NONE) return;
        int clampedLevel = Math.max(0, Math.min(MAX_LEVEL, level));
        double clampedXp = Math.max(0.0, xp);
        skillLevels.put(wc, clampedLevel);
        skillXP.put(wc, clampedXp);
    }
}
