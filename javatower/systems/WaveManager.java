package javatower.systems;

import java.util.List;
import java.util.ArrayList;
import javatower.entities.Enemy;
import javatower.factories.EnemyFactory;

/**
 * Spawns enemies and manages wave progression.
 */
public class WaveManager {
    private int currentWave = 1;
    private int enemiesRemaining = 0;
    private List<Enemy> activeEnemies = new ArrayList<>();
    private boolean waveInProgress = false;
    private boolean bossWave = false;

    /**
     * Starts a new wave.
     */
    public void startWave() {
        bossWave = (currentWave % 5 == 0 || currentWave == 30);
        activeEnemies = spawnEnemies();
        enemiesRemaining = activeEnemies.size();
        waveInProgress = true;
    }

    /**
     * Spawns enemies for the current wave.
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
        return EnemyFactory.createWaveEnemies(currentWave);
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
}
