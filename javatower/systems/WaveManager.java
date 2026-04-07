package javatower.systems;

import java.util.List;
import java.util.ArrayList;
import javatower.entities.Enemy;
import javatower.factories.EnemyFactory;
import javatower.util.Logger;

/**
 * Spawns enemies and manages wave progression.
 * Supports random wave modifiers.
 */
public class WaveManager {
    /** Hard cap to prevent runaway wave sizes/performance collapse. */
    public static final int MAX_WAVE_ENEMIES = 1000;
    private int currentWave = 1;
    private int enemiesRemaining = 0;
    private List<Enemy> activeEnemies = new ArrayList<>();
    private boolean waveInProgress = false;
    private boolean bossWave = false;
    private WaveModifier currentModifier = WaveModifier.NONE;

    /**
     * Starts a new wave with random modifier.
     */
    public void startWave() {
        bossWave = (currentWave % 5 == 0 || currentWave == 30);
        
        // Select random wave modifier (boss waves have no modifiers)
        currentModifier = bossWave ? WaveModifier.NONE : WaveModifier.randomModifier(currentWave);
        
        if (currentModifier.hasModifier()) {
            Logger.info("Wave Modifier: " + currentModifier.getName() + " - " + currentModifier.getDescription());
        }
        
        activeEnemies = spawnEnemies();
        enemiesRemaining = activeEnemies.size();
        waveInProgress = true;
    }

    /**
     * Spawns enemies for the current wave.
     * Applies wave modifiers.
     */
    public List<Enemy> spawnEnemies() {
        if (bossWave) {
            if (currentWave == 30) {
                List<Enemy> boss = new ArrayList<>();
                boss.add(EnemyFactory.createFinalBoss());
                return boss;
            } else {
                List<Enemy> miniBoss = new ArrayList<>();
                miniBoss.add(EnemyFactory.createMiniBoss(currentWave));
                return miniBoss;
            }
        }
        
        // Apply count multiplier from modifier
        int count = (int)(getEnemyCount() * currentModifier.getEnemyCountMultiplier());
        count = Math.min(MAX_WAVE_ENEMIES, Math.max(1, count));
        return EnemyFactory.createWaveEnemies(currentWave, count, currentModifier);
    }

    /**
     * Called when an enemy is killed.
     */
    public void onEnemyKilled(Enemy enemy) {
        activeEnemies.remove(enemy);
        enemiesRemaining--;
    }

    /**
     * Checks if the wave is complete.
     */
    public boolean isWaveComplete() {
        return enemiesRemaining <= 0;
    }

    /**
     * Proceeds to the next wave.
     */
    public void nextWave() {
        currentWave++;
        startWave();
    }

    /**
     * Returns the number of enemies for the wave.
     */
    public int getEnemyCount() {
        // Progressive scaling with stronger late-wave ramp, capped for stability.
        int scaled = 6 + (currentWave * 3) + ((currentWave * currentWave) / 18);
        return Math.min(MAX_WAVE_ENEMIES, scaled);
    }

    // Getters
    public int getCurrentWave() { return currentWave; }
    public List<Enemy> getActiveEnemies() { return activeEnemies; }
    public boolean isWaveInProgress() { return waveInProgress; }
    public boolean isBossWave() { return bossWave; }
    public WaveModifier getCurrentModifier() { return currentModifier; }
    public boolean hasActiveModifier() { return currentModifier != WaveModifier.NONE; }
    
    /**
     * Forcibly sets a modifier (for testing/debug).
     */
    public void setModifier(WaveModifier mod) { 
        this.currentModifier = mod; 
    }
}
