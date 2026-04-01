package javatower.factories;

import java.util.List;
import java.util.ArrayList;
import javatower.entities.Enemy;
import javatower.entities.Enemy.EnemyType;
import javatower.systems.WaveModifier;

/**
 * Factory for creating enemies based on wave.
 * Supports elite modifiers and wave modifiers.
 */
public class EnemyFactory {
    public static Enemy createEnemy(EnemyType type, int waveLevel) {
        return createEnemy(type, waveLevel, WaveModifier.NONE);
    }
    
    public static Enemy createEnemy(EnemyType type, int waveLevel, WaveModifier modifier) {
        Enemy enemy;
        switch (type) {
            case ZOMBIE:
                enemy = new javatower.entities.enemies.Zombie(waveLevel); break;
            case SKELETON:
                enemy = new javatower.entities.enemies.Skeleton(waveLevel); break;
            case GHOUL:
                enemy = new javatower.entities.enemies.Ghoul(waveLevel); break;
            case WIGHT:
                enemy = new javatower.entities.enemies.Wight(waveLevel); break;
            case WRAITH:
                enemy = new javatower.entities.enemies.Wraith(waveLevel); break;
            case REVENANT:
                enemy = new javatower.entities.enemies.Revenant(waveLevel); break;
            case DEATH_KNIGHT:
                enemy = new javatower.entities.enemies.DeathKnight(waveLevel); break;
            case LICH:
                enemy = new javatower.entities.enemies.Lich(waveLevel); break;
            case BONE_COLOSSUS:
                enemy = new javatower.entities.enemies.BoneColossus(waveLevel); break;
            case NECROMANCER_KING:
                enemy = new javatower.entities.enemies.NecromancerKing(waveLevel); break;
            default:
                return null;
        }
        
        // Apply wave modifier effects (HP, speed multipliers)
        if (enemy != null && modifier != null && modifier.hasModifier()) {
            applyWaveModifier(enemy, modifier);
        }
        
        // Apply elite modifier (skip for bosses)
        if (enemy != null && !enemy.isBoss() && !enemy.isMiniBoss()) {
            // Elite Wave modifier makes ALL enemies elite
            if (modifier == WaveModifier.ELITE_WAVE) {
                enemy.applyEliteModifier(javatower.entities.EliteModifier.randomForced());
            } else {
                javatower.entities.EliteModifier eliteMod = javatower.entities.EliteModifier.randomModifier(waveLevel);
                enemy.applyEliteModifier(eliteMod);
            }
        }
        
        return enemy;
    }
    
    private static void applyWaveModifier(Enemy enemy, WaveModifier mod) {
        // Apply HP multiplier
        double hpMult = mod.getHpMultiplier();
        if (hpMult != 1.0) {
            int newMaxHp = (int)(enemy.getMaxHealth() * hpMult);
            enemy.setMaxHealth(newMaxHp);
            enemy.setCurrentHealth(newMaxHp);
        }
        
        // Apply speed multiplier
        double speedMult = mod.getSpeedMultiplier();
        if (speedMult != 1.0) {
            enemy.setSpeed(enemy.getSpeed() * speedMult);
        }
    }

    public static List<Enemy> createWaveEnemies(int waveNumber) {
        return createWaveEnemies(waveNumber, -1, WaveModifier.NONE);
    }
    
    public static List<Enemy> createWaveEnemies(int waveNumber, int forcedCount, WaveModifier modifier) {
        List<Enemy> enemies = new ArrayList<>();
        int w = waveNumber;

        // Waves 1-4: Tier 1 — Zombies, Skeletons, Ghouls
        if (w == 1) {
            addN(enemies, EnemyType.ZOMBIE, w, 5);
        } else if (w == 2) {
            addN(enemies, EnemyType.ZOMBIE, w, 3);
            addN(enemies, EnemyType.SKELETON, w, 2);
        } else if (w == 3) {
            addN(enemies, EnemyType.SKELETON, w, 2);
            addN(enemies, EnemyType.GHOUL, w, 3);
        } else if (w == 4) {
            addN(enemies, EnemyType.ZOMBIE, w, 2);
            addN(enemies, EnemyType.SKELETON, w, 2);
            addN(enemies, EnemyType.GHOUL, w, 2);
        }
        // Waves 6-9: Tier 2 — Introduce Wights and Wraiths
        else if (w == 6) {
            addN(enemies, EnemyType.GHOUL, w, 3);
            addN(enemies, EnemyType.WIGHT, w, 3);
        } else if (w == 7) {
            addN(enemies, EnemyType.SKELETON, w, 2);
            addN(enemies, EnemyType.WIGHT, w, 3);
            addN(enemies, EnemyType.WRAITH, w, 2);
        } else if (w == 8) {
            addN(enemies, EnemyType.WIGHT, w, 3);
            addN(enemies, EnemyType.WRAITH, w, 3);
            addN(enemies, EnemyType.GHOUL, w, 2);
        } else if (w == 9) {
            addN(enemies, EnemyType.WRAITH, w, 4);
            addN(enemies, EnemyType.WIGHT, w, 3);
            addN(enemies, EnemyType.SKELETON, w, 2);
        }
        // Waves 11-14: Tier 3 — Introduce Revenants and DeathKnights
        else if (w == 11) {
            addN(enemies, EnemyType.WIGHT, w, 3);
            addN(enemies, EnemyType.WRAITH, w, 2);
            addN(enemies, EnemyType.REVENANT, w, 2);
        } else if (w == 12) {
            addN(enemies, EnemyType.REVENANT, w, 3);
            addN(enemies, EnemyType.DEATH_KNIGHT, w, 2);
            addN(enemies, EnemyType.GHOUL, w, 3);
        } else if (w == 13) {
            addN(enemies, EnemyType.DEATH_KNIGHT, w, 3);
            addN(enemies, EnemyType.WRAITH, w, 3);
            addN(enemies, EnemyType.WIGHT, w, 2);
        } else if (w == 14) {
            addN(enemies, EnemyType.DEATH_KNIGHT, w, 3);
            addN(enemies, EnemyType.REVENANT, w, 3);
            addN(enemies, EnemyType.SKELETON, w, 3);
        }
        // Waves 16-19: Tier 4 — Introduce Liches
        else if (w == 16) {
            addN(enemies, EnemyType.DEATH_KNIGHT, w, 3);
            addN(enemies, EnemyType.LICH, w, 2);
            addN(enemies, EnemyType.WRAITH, w, 3);
        } else if (w == 17) {
            addN(enemies, EnemyType.LICH, w, 3);
            addN(enemies, EnemyType.REVENANT, w, 3);
            addN(enemies, EnemyType.WIGHT, w, 2);
        } else if (w == 18) {
            addN(enemies, EnemyType.LICH, w, 3);
            addN(enemies, EnemyType.DEATH_KNIGHT, w, 4);
            addN(enemies, EnemyType.GHOUL, w, 3);
        } else if (w == 19) {
            addN(enemies, EnemyType.LICH, w, 4);
            addN(enemies, EnemyType.WRAITH, w, 3);
            addN(enemies, EnemyType.REVENANT, w, 3);
        }
        // Waves 21-24: Tier 5 — Introduce BoneColossus, elite mixes
        else if (w == 21) {
            addN(enemies, EnemyType.BONE_COLOSSUS, w, 2);
            addN(enemies, EnemyType.LICH, w, 3);
            addN(enemies, EnemyType.DEATH_KNIGHT, w, 3);
        } else if (w == 22) {
            addN(enemies, EnemyType.BONE_COLOSSUS, w, 2);
            addN(enemies, EnemyType.REVENANT, w, 3);
            addN(enemies, EnemyType.WRAITH, w, 4);
        } else if (w == 23) {
            addN(enemies, EnemyType.BONE_COLOSSUS, w, 3);
            addN(enemies, EnemyType.LICH, w, 3);
            addN(enemies, EnemyType.WIGHT, w, 3);
        } else if (w == 24) {
            addN(enemies, EnemyType.BONE_COLOSSUS, w, 3);
            addN(enemies, EnemyType.DEATH_KNIGHT, w, 4);
            addN(enemies, EnemyType.LICH, w, 4);
        }
        // Waves 26-29: Endgame — All elite enemies
        else if (w == 26) {
            addN(enemies, EnemyType.BONE_COLOSSUS, w, 3);
            addN(enemies, EnemyType.LICH, w, 4);
            addN(enemies, EnemyType.DEATH_KNIGHT, w, 4);
        } else if (w == 27) {
            addN(enemies, EnemyType.BONE_COLOSSUS, w, 4);
            addN(enemies, EnemyType.LICH, w, 4);
            addN(enemies, EnemyType.REVENANT, w, 3);
        } else if (w == 28) {
            addN(enemies, EnemyType.BONE_COLOSSUS, w, 4);
            addN(enemies, EnemyType.LICH, w, 5);
            addN(enemies, EnemyType.WRAITH, w, 4);
        } else if (w == 29) {
            addN(enemies, EnemyType.BONE_COLOSSUS, w, 5);
            addN(enemies, EnemyType.LICH, w, 5);
            addN(enemies, EnemyType.DEATH_KNIGHT, w, 5);
        }
        
        // Apply count multiplier from wave modifier
        if (forcedCount > 0 && !enemies.isEmpty()) {
            // Scale enemies to match forced count (approximate by duplicating)
            while (enemies.size() < forcedCount) {
                Enemy template = enemies.get(enemies.size() % enemies.size());
                enemies.add(createEnemy(template.getEnemyType(), waveNumber, modifier));
            }
        }
        
        return enemies;
    }

    private static void addN(List<Enemy> list, EnemyType type, int wave, int count) {
        addN(list, type, wave, count, WaveModifier.NONE);
    }
    
    private static void addN(List<Enemy> list, EnemyType type, int wave, int count, WaveModifier mod) {
        for (int i = 0; i < count; i++) list.add(createEnemy(type, wave, mod));
    }

    public static Enemy createMiniBoss(int waveNumber) {
        switch (waveNumber) {
            case 5:  return createEnemy(EnemyType.REVENANT, waveNumber);
            case 10: return createEnemy(EnemyType.DEATH_KNIGHT, waveNumber);
            case 15: return createEnemy(EnemyType.LICH, waveNumber);
            case 20: return createEnemy(EnemyType.BONE_COLOSSUS, waveNumber);
            case 25: return createEnemy(EnemyType.BONE_COLOSSUS, waveNumber);
            default: return createEnemy(EnemyType.REVENANT, waveNumber);
        }
    }

    public static Enemy createFinalBoss() {
        return createEnemy(EnemyType.NECROMANCER_KING, 30);
    }
}
