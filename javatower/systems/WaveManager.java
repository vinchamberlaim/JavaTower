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
        return 5 + (currentWave * 2);
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
