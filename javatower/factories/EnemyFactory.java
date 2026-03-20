package javatower.factories;

import java.util.List;
import java.util.ArrayList;
import javatower.entities.Enemy;
import javatower.entities.Enemy.EnemyType;

/**
 * Factory for creating enemies based on wave.
 */
public class EnemyFactory {
    public static Enemy createEnemy(EnemyType type, int waveLevel) {
        switch (type) {
            case ZOMBIE:
                return new javatower.entities.enemies.Zombie(waveLevel);
            case SKELETON:
                return new javatower.entities.enemies.Skeleton(waveLevel);
            case GHOUL:
                return new javatower.entities.enemies.Ghoul(waveLevel);
            case WIGHT:
                return new javatower.entities.enemies.Wight(waveLevel);
            case WRAITH:
                return new javatower.entities.enemies.Wraith(waveLevel);
            case REVENANT:
                return new javatower.entities.enemies.Revenant(waveLevel);
            case DEATH_KNIGHT:
                return new javatower.entities.enemies.DeathKnight(waveLevel);
            case LICH:
                return new javatower.entities.enemies.Lich(waveLevel);
            case BONE_COLOSSUS:
                return new javatower.entities.enemies.BoneColossus(waveLevel);
            case NECROMANCER_KING:
                return new javatower.entities.enemies.NecromancerKing(waveLevel);
            default:
                return null;
        }
    }

    public static List<Enemy> createWaveEnemies(int waveNumber) {
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
        return enemies;
    }

    private static void addN(List<Enemy> list, EnemyType type, int wave, int count) {
        for (int i = 0; i < count; i++) list.add(createEnemy(type, wave));
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
