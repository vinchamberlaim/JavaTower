package javatower.systems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javatower.entities.Item;
import javatower.entities.Item.EquipmentSet;

/**
 * Tracks and applies equipment set bonuses based on what the hero is wearing.
 *
 * Set bonus tiers:
 *   2-piece: moderate passive boost
 *   4-piece: powerful class-defining bonus
 *
 * Sets:
 *   HOLY  (Paladin)    – 2pc: +20% heal | 4pc: +50% vs undead, HP regen
 *   DEATH (Necromancer) – 2pc: +25% summon HP | 4pc: 10% life steal, mana regen
 *   FIRE  (Pyromancer)  – 2pc: +25% damage | 4pc: 30% AoE splash on hit
 *   KNIGHT(Warrior)     – 2pc: +25% defence | 4pc: thorns 15%, melee speed
 */
public class SetBonusManager {

    /**
     * Counts how many pieces of each set are equipped from the given items.
     */
    public static Map<EquipmentSet, Integer> countSetPieces(Item[] equipped) {
        Map<EquipmentSet, Integer> counts = new HashMap<>();
        for (Item item : equipped) {
            if (item != null && item.getEquipmentSet() != EquipmentSet.NONE) {
                counts.merge(item.getEquipmentSet(), 1, Integer::sum);
            }
        }
        return counts;
    }

    /**
     * Returns a list of human-readable active bonus descriptions.
     */
    public static List<String> getActiveBonusDescriptions(Item[] equipped) {
        Map<EquipmentSet, Integer> counts = countSetPieces(equipped);
        List<String> bonuses = new ArrayList<>();
        for (Map.Entry<EquipmentSet, Integer> e : counts.entrySet()) {
            EquipmentSet set = e.getKey();
            int count = e.getValue();
            if (count >= set.twoPieceThreshold) {
                bonuses.add(set.className + " (2): " + getTwoPieceDescription(set));
            }
            if (count >= set.fourPieceThreshold) {
                bonuses.add(set.className + " (4): " + getFourPieceDescription(set));
            }
        }
        return bonuses;
    }

    // ---- 2-piece bonuses ----

    public static boolean hasTwoPiece(Item[] equipped, EquipmentSet set) {
        return countSetPieces(equipped).getOrDefault(set, 0) >= set.twoPieceThreshold;
    }

    public static boolean hasFourPiece(Item[] equipped, EquipmentSet set) {
        return countSetPieces(equipped).getOrDefault(set, 0) >= set.fourPieceThreshold;
    }

    /** Holy 2pc: +20% heal effectiveness. */
    public static double getHolyHealBonus(Item[] equipped) {
        return hasTwoPiece(equipped, EquipmentSet.HOLY) ? 1.2 : 1.0;
    }

    /** Death 2pc: +25% summon HP. */
    public static double getDeathSummonBonus(Item[] equipped) {
        return hasTwoPiece(equipped, EquipmentSet.DEATH) ? 1.25 : 1.0;
    }

    /** Fire 2pc: +25% attack damage. */
    public static double getFireDamageBonus(Item[] equipped) {
        return hasTwoPiece(equipped, EquipmentSet.FIRE) ? 1.25 : 1.0;
    }

    /** Knight 2pc: +25% defence. */
    public static double getKnightDefenceBonus(Item[] equipped) {
        return hasTwoPiece(equipped, EquipmentSet.KNIGHT) ? 1.25 : 1.0;
    }

    // ---- 4-piece bonuses ----

    /** Holy 4pc: +50% damage vs undead + passive HP regen. */
    public static double getHolyUndeadDamageBonus(Item[] equipped) {
        return hasFourPiece(equipped, EquipmentSet.HOLY) ? 1.5 : 1.0;
    }

    public static int getHolyPassiveRegen(Item[] equipped) {
        return hasFourPiece(equipped, EquipmentSet.HOLY) ? 2 : 0;
    }

    /** Death 4pc: 10% life steal. Returns fraction (0.1). */
    public static double getDeathLifeSteal(Item[] equipped) {
        return hasFourPiece(equipped, EquipmentSet.DEATH) ? 0.10 : 0.0;
    }

    public static int getDeathManaRegen(Item[] equipped) {
        return hasFourPiece(equipped, EquipmentSet.DEATH) ? 3 : 0;
    }

    /** Fire 4pc: 30% AoE splash. Returns the splash fraction. */
    public static double getFireSplashFraction(Item[] equipped) {
        return hasFourPiece(equipped, EquipmentSet.FIRE) ? 0.30 : 0.0;
    }

    /** Knight 4pc: 15% thorns damage reflection. */
    public static double getKnightThornsFraction(Item[] equipped) {
        return hasFourPiece(equipped, EquipmentSet.KNIGHT) ? 0.15 : 0.0;
    }

    /** Knight 4pc: melee attack speed bonus (seconds off cooldown). */
    public static double getKnightSpeedBonus(Item[] equipped) {
        return hasFourPiece(equipped, EquipmentSet.KNIGHT) ? 0.15 : 0.0;
    }

    // ---- Descriptions ----

    private static String getTwoPieceDescription(EquipmentSet set) {
        switch (set) {
            case HOLY:   return "+20% Heal Power";
            case DEATH:  return "+25% Summon HP";
            case FIRE:   return "+25% Attack Damage";
            case KNIGHT: return "+25% Defence";
            default:     return "";
        }
    }

    private static String getFourPieceDescription(EquipmentSet set) {
        switch (set) {
            case HOLY:   return "+50% vs Undead, HP Regen";
            case DEATH:  return "10% Life Steal, Mana Regen";
            case FIRE:   return "30% AoE Splash Damage";
            case KNIGHT: return "Thorns 15%, +Melee Speed";
            default:     return "";
        }
    }
}
