package javatower.systems;

import javatower.entities.Tower;
import javatower.entities.Tower.TowerType;
import javatower.util.Constants;
import java.util.List;

/**
 * Manages tower synergies - adjacent towers provide bonuses to each other.
 * 
 * Synergy Rules:
 * - Arrow + Magic = Arcane Arrows (pierce through enemies)
 * - Magic + Siege = Gravity Well (pulls enemies to center)
 * - Siege + Support = Overclocked (2x fire rate)
 * - Support + Support = Network (increased range)
 * - Arrow + Arrow + Arrow = Volley (synced firing)
 */
public class TowerSynergyManager {
    
    // Synergy detection radius (in pixels)
    private static final double SYNERGY_RADIUS = 150;
    
    // Synergy type enum
    public enum SynergyType {
        NONE,
        ARCANE_ARROWS,      // Arrow + Magic
        GRAVITY_WELL,       // Magic + Siege
        OVERCLOCKED,        // Siege + Support
        NETWORK,            // Support + Support
        VOLLEY              // 3x Arrow
    }
    
    /**
     * Checks and applies synergies for all towers.
     * Call this whenever towers are placed or removed.
     */
    public static void updateSynergies(List<Tower> towers) {
        // Clear all synergy bonuses first
        for (Tower t : towers) {
            t.clearSynergyBonus();
        }
        
        // Check each tower for synergies
        for (Tower tower : towers) {
            if (!tower.isAlive()) continue;
            
            SynergyType synergy = detectSynergy(tower, towers);
            applySynergyBonus(tower, synergy);
        }
    }
    
    /**
     * Detects what synergy (if any) applies to a tower.
     */
    private static SynergyType detectSynergy(Tower tower, List<Tower> allTowers) {
        TowerType type = tower.getType();
        
        // Count nearby towers by type
        int nearbyArrow = 0, nearbyMagic = 0, nearbySiege = 0, nearbySupport = 0;
        
        for (Tower other : allTowers) {
            if (other == tower || !other.isAlive()) continue;
            
            double dist = distance(tower, other);
            if (dist > SYNERGY_RADIUS) continue;
            
            switch (other.getType()) {
                case ARROW: nearbyArrow++; break;
                case MAGIC: nearbyMagic++; break;
                case SIEGE: nearbySiege++; break;
                case SUPPORT: nearbySupport++; break;
            }
        }
        
        // Check synergy conditions
        switch (type) {
            case ARROW:
                // Arrow + Magic = Arcane Arrows
                if (nearbyMagic > 0) return SynergyType.ARCANE_ARROWS;
                // 3x Arrow = Volley
                if (nearbyArrow >= 2) return SynergyType.VOLLEY;
                break;
                
            case MAGIC:
                // Magic + Siege = Gravity Well
                if (nearbySiege > 0) return SynergyType.GRAVITY_WELL;
                // Magic + Arrow = Arcane Arrows (benefits both)
                if (nearbyArrow > 0) return SynergyType.ARCANE_ARROWS;
                break;
                
            case SIEGE:
                // Siege + Support = Overclocked
                if (nearbySupport > 0) return SynergyType.OVERCLOCKED;
                // Siege + Magic = Gravity Well
                if (nearbyMagic > 0) return SynergyType.GRAVITY_WELL;
                break;
                
            case SUPPORT:
                // Support + Siege = Overclocked
                if (nearbySiege > 0) return SynergyType.OVERCLOCKED;
                // Support + Support = Network
                if (nearbySupport > 0) return SynergyType.NETWORK;
                break;
        }
        
        return SynergyType.NONE;
    }
    
    /**
     * Applies synergy bonus to a tower.
     */
    private static void applySynergyBonus(Tower tower, SynergyType synergy) {
        tower.setActiveSynergy(synergy);
        
        switch (synergy) {
            case ARCANE_ARROWS:
                // Arrow towers pierce through enemies
                if (tower.getType() == TowerType.ARROW) {
                    tower.setPierceEnabled(true);
                    tower.setSynergyDamageMultiplier(1.2);
                }
                // Magic towers get +damage
                if (tower.getType() == TowerType.MAGIC) {
                    tower.setSynergyDamageMultiplier(1.3);
                }
                break;
                
            case GRAVITY_WELL:
                // Both towers pull enemies slightly
                tower.setGravityWellEnabled(true);
                tower.setSynergyDamageMultiplier(1.1);
                break;
                
            case OVERCLOCKED:
                // 2x fire rate
                tower.setSynergySpeedMultiplier(2.0);
                break;
                
            case NETWORK:
                // +30% range
                tower.setSynergyRangeMultiplier(1.3);
                break;
                
            case VOLLEY:
                // Arrows sync fire and +damage
                tower.setVolleyEnabled(true);
                tower.setSynergyDamageMultiplier(1.4);
                break;
                
            case NONE:
                // No bonus
                break;
        }
    }
    
    /**
     * Calculates distance between two towers.
     */
    private static double distance(Tower a, Tower b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Gets a human-readable description of a synergy.
     */
    public static String getSynergyDescription(SynergyType synergy) {
        switch (synergy) {
            case ARCANE_ARROWS: return "Arcane Arrows - Arrows pierce, Magic +30% dmg";
            case GRAVITY_WELL: return "Gravity Well - Pulls enemies to center";
            case OVERCLOCKED: return "Overclocked - 2x fire rate";
            case NETWORK: return "Network - +30% range";
            case VOLLEY: return "Volley - Synced fire, +40% dmg";
            default: return "";
        }
    }
    
    /**
     * Gets the color associated with a synergy type.
     */
    public static String getSynergyColor(SynergyType synergy) {
        switch (synergy) {
            case ARCANE_ARROWS: return "#9f7aea"; // Purple
            case GRAVITY_WELL: return "#ed8936"; // Orange
            case OVERCLOCKED: return "#f56565"; // Red
            case NETWORK: return "#4fd1c5"; // Teal
            case VOLLEY: return "#ecc94b"; // Gold
            default: return "#ffffff";
        }
    }
}
