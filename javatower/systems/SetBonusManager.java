package javatower.systems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javatower.entities.Item;
import javatower.entities.Item.EquipmentSet;
import javatower.entities.Item.WeaponClass;

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
 *
 * Class-Based Bonuses (based on WeaponClass of equipped items):
 *   NECROMANCY – Death magic bonuses
 *   ARCHER     – Ranged/bow bonuses  
 *   MELEE      – Close combat bonuses
 *   HOLY       – Healing/divine bonuses
 *   DEFENCE    – Shield/armor bonuses
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

    // ========== WEAPON CLASS BONUSES ==========
    // New system: equip multiple items of same WeaponClass to unlock passive spells

    /**
     * Counts how many equipped items have each WeaponClass.
     */
    public static Map<WeaponClass, Integer> countWeaponClassItems(Item[] equipped) {
        Map<WeaponClass, Integer> counts = new HashMap<>();
        for (Item item : equipped) {
            if (item != null && item.getWeaponClass() != WeaponClass.NONE) {
                counts.merge(item.getWeaponClass(), 1, Integer::sum);
            }
        }
        return counts;
    }

    /**
     * Gets the number of equipped items for a specific WeaponClass.
     */
    public static int getWeaponClassCount(Item[] equipped, WeaponClass wc) {
        return countWeaponClassItems(equipped).getOrDefault(wc, 0);
    }

    // ---- NECROMANCY CLASS BONUSES ----
    // Spells: Life Drain, Corpse Explosion, Bone Shield, Summon Skeleton

    /** Necromancy 2+ items: +10% life drain on attacks */
    public static double getNecromancyLifeDrain(Item[] equipped) {
        int count = getWeaponClassCount(equipped, WeaponClass.NECROMANCY);
        return count >= 2 ? (0.05 * count) : 0.0; // 10% at 2, 15% at 3, 20% at 4+
    }

    /** Necromancy 3+ items: Chance to cast Corpse Explosion on kill */
    public static boolean hasNecromancyCorpseExplosion(Item[] equipped) {
        return getWeaponClassCount(equipped, WeaponClass.NECROMANCY) >= 3;
    }

    /** Necromancy 4+ items: Auto-cast Bone Shield periodically */
    public static boolean hasNecromancyBoneShield(Item[] equipped) {
        return getWeaponClassCount(equipped, WeaponClass.NECROMANCY) >= 4;
    }

    /** Necromancy 5+ items: Summon skeleton army */
    public static boolean hasNecromancySummonArmy(Item[] equipped) {
        return getWeaponClassCount(equipped, WeaponClass.NECROMANCY) >= 5;
    }

    /** Necromancy mana cost reduction */
    public static double getNecromancyManaCostReduction(Item[] equipped) {
        int count = getWeaponClassCount(equipped, WeaponClass.NECROMANCY);
        return count >= 2 ? (0.1 * count) : 0.0; // 20% at 2, up to 50% at 5
    }

    // ---- ARCHER CLASS BONUSES ----
    // Spells: Piercing Shot, Multishot, Volley, Marked for Death

    /** Archer 2+ items: +15% range bonus */
    public static double getArcherRangeBonus(Item[] equipped) {
        int count = getWeaponClassCount(equipped, WeaponClass.RANGED);
        return count >= 2 ? (0.15 * count) : 0.0;
    }

    /** Archer 3+ items: Piercing shots (chance to hit multiple enemies) */
    public static boolean hasArcherPiercing(Item[] equipped) {
        return getWeaponClassCount(equipped, WeaponClass.RANGED) >= 3;
    }

    /** Archer 4+ items: Auto-cast Multishot (fire additional projectiles) */
    public static int getArcherExtraProjectiles(Item[] equipped) {
        int count = getWeaponClassCount(equipped, WeaponClass.RANGED);
        return count >= 4 ? (count - 3) : 0; // 1 extra at 4, 2 at 5+
    }

    /** Archer 5+ items: Marked for Death aura (enemies near take more damage) */
    public static boolean hasArcherMarkedForDeath(Item[] equipped) {
        return getWeaponClassCount(equipped, WeaponClass.RANGED) >= 5;
    }

    /** Archer critical strike bonus */
    public static int getArcherCritBonus(Item[] equipped) {
        int count = getWeaponClassCount(equipped, WeaponClass.RANGED);
        return count >= 2 ? (3 * count) : 0; // +6% at 2, up to +15% at 5
    }

    // ---- MELEE CLASS BONUSES ----
    // Spells: Cleave, Whirlwind, Execute, Berserk

    /** Melee 2+ items: Cleave damage (splash to nearby enemies) */
    public static double getMeleeCleavePercent(Item[] equipped) {
        int count = getWeaponClassCount(equipped, WeaponClass.MELEE);
        return count >= 2 ? (0.15 * count) : 0.0; // 30% cleave at 2, up to 75%
    }

    /** Melee 3+ items: Execute threshold (instant kill low health enemies) */
    public static double getMeleeExecuteThreshold(Item[] equipped) {
        int count = getWeaponClassCount(equipped, WeaponClass.MELEE);
        return count >= 3 ? (0.05 * (count - 2)) : 0.0; // 5% at 3, up to 15%
    }

    /** Melee 4+ items: Auto-cast Whirlwind periodically */
    public static boolean hasMeleeWhirlwind(Item[] equipped) {
        return getWeaponClassCount(equipped, WeaponClass.MELEE) >= 4;
    }

    /** Melee 5+ items: Berserk mode (damage increases as health decreases) */
    public static boolean hasMeleeBerserk(Item[] equipped) {
        return getWeaponClassCount(equipped, WeaponClass.MELEE) >= 5;
    }

    /** Melee attack speed bonus */
    public static double getMeleeAttackSpeed(Item[] equipped) {
        int count = getWeaponClassCount(equipped, WeaponClass.MELEE);
        return count >= 2 ? (0.03 * count) : 0.0; // 6% faster at 2, up to 15%
    }

    // ---- HOLY CLASS BONUSES ----
    // Spells: Holy Light, Divine Shield, Consecration, Resurrection

    /** Holy 2+ items: +15% heal power */
    public static double getHolyHealPower(Item[] equipped) {
        int count = getWeaponClassCount(equipped, WeaponClass.HOLY);
        return count >= 2 ? (1.0 + 0.15 * count) : 1.0; // 1.3x at 2, up to 1.75x
    }

    /** Holy 3+ items: Divine Shield (chance to negate damage) */
    public static double getHolyDivineShieldChance(Item[] equipped) {
        int count = getWeaponClassCount(equipped, WeaponClass.HOLY);
        return count >= 3 ? (0.05 * (count - 2)) : 0.0; // 5% at 3, up to 15%
    }

    /** Holy 4+ items: Auto-cast Consecration (damage aura) */
    public static boolean hasHolyConsecration(Item[] equipped) {
        return getWeaponClassCount(equipped, WeaponClass.HOLY) >= 4;
    }

    /** Holy 5+ items: Auto-resurrect once per floor */
    public static boolean hasHolyResurrection(Item[] equipped) {
        return getWeaponClassCount(equipped, WeaponClass.HOLY) >= 5;
    }

    /** Holy damage vs undead bonus */
    public static double getHolyUndeadBonus(Item[] equipped) {
        int count = getWeaponClassCount(equipped, WeaponClass.HOLY);
        return count >= 2 ? (1.0 + 0.1 * count) : 1.0; // 1.2x at 2, up to 1.5x
    }

    // ---- DEFENCE CLASS BONUSES ----
    // Spells: Shield Block, Reflect, Fortress, Unbreakable

    /** Defence 2+ items: +10% damage reduction */
    public static double getDefenceDamageReduction(Item[] equipped) {
        int count = getWeaponClassCount(equipped, WeaponClass.DEFENCE);
        return count >= 2 ? (0.1 * count) : 0.0; // 20% at 2, up to 50%
    }

    /** Defence 3+ items: Reflect damage back to attackers */
    public static double getDefenceReflectDamage(Item[] equipped) {
        int count = getWeaponClassCount(equipped, WeaponClass.DEFENCE);
        return count >= 3 ? (0.1 * (count - 2)) : 0.0; // 10% at 3, up to 30%
    }

    /** Defence 4+ items: Auto-cast Shield Block periodically */
    public static boolean hasDefenceShieldBlock(Item[] equipped) {
        return getWeaponClassCount(equipped, WeaponClass.DEFENCE) >= 4;
    }

    /** Defence 5+ items: Unbreakable (cannot be stunned, +max HP) */
    public static boolean hasDefenceUnbreakable(Item[] equipped) {
        return getWeaponClassCount(equipped, WeaponClass.DEFENCE) >= 5;
    }

    /** Defence max health bonus */
    public static int getDefenceHealthBonus(Item[] equipped) {
        int count = getWeaponClassCount(equipped, WeaponClass.DEFENCE);
        return count >= 2 ? (15 * count) : 0; // +30 HP at 2, up to +75 HP
    }

    /**
     * Returns descriptions of all active class bonuses.
     */
    public static List<String> getClassBonusDescriptions(Item[] equipped) {
        List<String> bonuses = new ArrayList<>();
        Map<WeaponClass, Integer> counts = countWeaponClassItems(equipped);
        
        for (Map.Entry<WeaponClass, Integer> entry : counts.entrySet()) {
            WeaponClass wc = entry.getKey();
            int count = entry.getValue();
            if (count < 2) continue;
            
            switch (wc) {
                case NECROMANCY:
                    bonuses.add("☠️ Necromancy (" + count + "): " + getNecromancyDescription(count));
                    break;
                case RANGED:
                    bonuses.add("🏹 Archer (" + count + "): " + getArcherDescription(count));
                    break;
                case MELEE:
                    bonuses.add("⚔️ Melee (" + count + "): " + getMeleeDescription(count));
                    break;
                case HOLY:
                    bonuses.add("✨ Holy (" + count + "): " + getHolyDescription(count));
                    break;
                case DEFENCE:
                    bonuses.add("🛡️ Defence (" + count + "): " + getDefenceDescription(count));
                    break;
                default:
                    break;
            }
        }
        return bonuses;
    }

    private static String getNecromancyDescription(int count) {
        if (count >= 5) return "Life Drain " + (int)(count*5) + "%, Corpse Explosion, Bone Shield, Summon Army";
        if (count >= 4) return "Life Drain " + (int)(count*5) + "%, Corpse Explosion, Bone Shield";
        if (count >= 3) return "Life Drain " + (int)(count*5) + "%, Corpse Explosion";
        return "Life Drain " + (int)(count*5) + "%";
    }

    private static String getArcherDescription(int count) {
        if (count >= 5) return "+" + (int)(count*15) + "% Range, Piercing, Multishot x" + (count-3) + ", Marked for Death";
        if (count >= 4) return "+" + (int)(count*15) + "% Range, Piercing, Multishot";
        if (count >= 3) return "+" + (int)(count*15) + "% Range, Piercing";
        return "+" + (int)(count*15) + "% Range";
    }

    private static String getMeleeDescription(int count) {
        if (count >= 5) return "Cleave " + (int)(count*15) + "%, Execute 15%, Whirlwind, Berserk";
        if (count >= 4) return "Cleave " + (int)(count*15) + "%, Execute " + (int)((count-2)*5) + "%, Whirlwind";
        if (count >= 3) return "Cleave " + (int)(count*15) + "%, Execute " + (int)((count-2)*5) + "%";
        return "Cleave " + (int)(count*15) + "%";
    }

    private static String getHolyDescription(int count) {
        if (count >= 5) return "+" + (int)(count*15) + "% Heal, Divine Shield, Consecration, Resurrection";
        if (count >= 4) return "+" + (int)(count*15) + "% Heal, Divine Shield, Consecration";
        if (count >= 3) return "+" + (int)(count*15) + "% Heal, Divine Shield " + (int)((count-2)*5) + "%";
        return "+" + (int)(count*15) + "% Heal";
    }

    private static String getDefenceDescription(int count) {
        if (count >= 5) return "-" + (int)(count*10) + "% Damage, Reflect 30%, Shield Block, Unbreakable";
        if (count >= 4) return "-" + (int)(count*10) + "% Damage, Reflect " + (int)((count-2)*10) + "%, Shield Block";
        if (count >= 3) return "-" + (int)(count*10) + "% Damage, Reflect " + (int)((count-2)*10) + "%";
        return "-" + (int)(count*10) + "% Damage";
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
