package javatower.ai;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects per-wave and cumulative gameplay data for AI-driven balance analysis.
 * Each wave produces a WaveSnapshot; at the end of a run we have a full RunReport.
 */
public class GameMetrics {

    /** Snapshot of stats for a single wave. */
    public static class WaveSnapshot {
        public int waveNumber;
        public double durationSeconds;
        public int enemiesKilled;
        public int damageDealt;
        public int damageTaken;
        public int goldEarned;
        public int goldSpent;
        public int xpEarned;
        public int heroHPAtEnd;
        public int heroMaxHPAtEnd;
        public int heroManaAtEnd;
        public int heroLevelAtEnd;
        public int towersPlaced;
        public int towersTotal;
        public int abilitiesUsed;   // Q/W/E/R total
        public int healsCast;
        public int closeCalls;      // HP dropped below 20%
        public double heroDPS;      // damageDealt / durationSeconds
        public double damageTakenPerSec;
        public String deathCause;   // null if survived

        @Override
        public String toString() {
            return String.format(
                "Wave %2d | %5.1fs | Kills:%3d | DPS:%.0f | Dealt:%5d | Taken:%5d | HP:%3d/%3d | Gold+%d-%d | Lvl:%d | Towers:%d | Abilities:%d | Close:%d%s",
                waveNumber, durationSeconds, enemiesKilled, heroDPS,
                damageDealt, damageTaken, heroHPAtEnd, heroMaxHPAtEnd,
                goldEarned, goldSpent, heroLevelAtEnd, towersTotal,
                abilitiesUsed, closeCalls,
                deathCause != null ? " | DIED: " + deathCause : ""
            );
        }
    }

    // --- Running counters (reset per wave) ---
    private double waveStartTime;
    private double waveElapsed;
    private int waveDamageDealt;
    private int waveDamageTaken;
    private int waveGoldEarned;
    private int waveGoldSpent;
    private int waveXPEarned;
    private int waveKills;
    private int waveTowersPlaced;
    private int waveAbilitiesUsed;
    private int waveHealsCast;
    private int waveCloseCalls;
    private boolean closeCallThisFrame;

    // --- Cumulative ---
    private int totalKills;
    private int totalDamageDealt;
    private int totalDamageTaken;
    private int totalGoldEarned;
    private int totalAbilitiesUsed;
    private double totalPlayTime;
    private int runsCompleted;

    private List<WaveSnapshot> waveHistory = new ArrayList<>();
    private List<String> balanceFlags = new ArrayList<>(); // flagged issues

    public GameMetrics() {
        resetWave();
    }

    /** Call at the start of each wave. */
    public void resetWave() {
        waveStartTime = 0;
        waveElapsed = 0;
        waveDamageDealt = 0;
        waveDamageTaken = 0;
        waveGoldEarned = 0;
        waveGoldSpent = 0;
        waveXPEarned = 0;
        waveKills = 0;
        waveTowersPlaced = 0;
        waveAbilitiesUsed = 0;
        waveHealsCast = 0;
        waveCloseCalls = 0;
        closeCallThisFrame = false;
    }

    // --- Event recording ---
    public void recordDamageDealt(int amount) { waveDamageDealt += amount; totalDamageDealt += amount; }
    public void recordDamageTaken(int amount) { waveDamageTaken += amount; totalDamageTaken += amount; }
    public void recordKill() { waveKills++; totalKills++; }
    public void recordGoldEarned(int amount) { waveGoldEarned += amount; totalGoldEarned += amount; }
    public void recordGoldSpent(int amount) { waveGoldSpent += amount; }
    public void recordXPEarned(int amount) { waveXPEarned += amount; }
    public void recordTowerPlaced() { waveTowersPlaced++; }
    public void recordAbilityUsed() { waveAbilitiesUsed++; totalAbilitiesUsed++; }
    public void recordHealCast() { waveHealsCast++; }
    public void recordCloseCall() { if (!closeCallThisFrame) { waveCloseCalls++; closeCallThisFrame = true; } }
    public void clearCloseCallFlag() { closeCallThisFrame = false; }

    public void tick(double dt) { waveElapsed += dt; }

    /**
     * Finalize the current wave and produce a snapshot.
     */
    public WaveSnapshot finalizeWave(int waveNumber, int heroHP, int heroMaxHP,
                                      int heroMana, int heroLevel, int towersTotal, String deathCause) {
        WaveSnapshot snap = new WaveSnapshot();
        snap.waveNumber = waveNumber;
        snap.durationSeconds = waveElapsed;
        snap.enemiesKilled = waveKills;
        snap.damageDealt = waveDamageDealt;
        snap.damageTaken = waveDamageTaken;
        snap.goldEarned = waveGoldEarned;
        snap.goldSpent = waveGoldSpent;
        snap.xpEarned = waveXPEarned;
        snap.heroHPAtEnd = heroHP;
        snap.heroMaxHPAtEnd = heroMaxHP;
        snap.heroManaAtEnd = heroMana;
        snap.heroLevelAtEnd = heroLevel;
        snap.towersPlaced = waveTowersPlaced;
        snap.towersTotal = towersTotal;
        snap.abilitiesUsed = waveAbilitiesUsed;
        snap.healsCast = waveHealsCast;
        snap.closeCalls = waveCloseCalls;
        snap.heroDPS = waveElapsed > 0 ? waveDamageDealt / waveElapsed : 0;
        snap.damageTakenPerSec = waveElapsed > 0 ? waveDamageTaken / waveElapsed : 0;
        snap.deathCause = deathCause;

        totalPlayTime += waveElapsed;
        waveHistory.add(snap);

        // --- Auto-detect balance flags ---
        analyzeWave(snap);

        resetWave();
        return snap;
    }

    /** Heuristic balance analysis — flags potential issues. */
    private void analyzeWave(WaveSnapshot w) {
        // Too easy: wave cleared in < 5s with no damage taken
        if (w.durationSeconds < 5 && w.damageTaken == 0 && w.waveNumber > 3) {
            balanceFlags.add(String.format("[EASY] Wave %d cleared in %.1fs with 0 damage taken", w.waveNumber, w.durationSeconds));
        }
        // Too hard: died or HP < 10%
        if (w.deathCause != null) {
            balanceFlags.add(String.format("[DEATH] Died on wave %d after %.1fs: %s", w.waveNumber, w.durationSeconds, w.deathCause));
        } else if (w.heroHPAtEnd < w.heroMaxHPAtEnd * 0.1) {
            balanceFlags.add(String.format("[HARD] Wave %d ended with only %d/%d HP", w.waveNumber, w.heroHPAtEnd, w.heroMaxHPAtEnd));
        }
        // DPS spike check — if DPS doubled since 2 waves ago
        if (waveHistory.size() >= 3) {
            WaveSnapshot prev = waveHistory.get(waveHistory.size() - 3);
            if (prev.heroDPS > 0 && w.heroDPS > prev.heroDPS * 2.5) {
                balanceFlags.add(String.format("[SPIKE] DPS jumped from %.0f to %.0f between wave %d and %d",
                    prev.heroDPS, w.heroDPS, prev.waveNumber, w.waveNumber));
            }
        }
        // Gold hoarding — unspent gold > 200
        if (w.goldEarned - w.goldSpent > 200 && w.waveNumber > 5) {
            balanceFlags.add(String.format("[GOLD] Hoarding %dg on wave %d — economy too generous or nothing worth buying",
                w.goldEarned - w.goldSpent, w.waveNumber));
        }
        // Ability spam or neglect
        if (w.abilitiesUsed == 0 && w.durationSeconds > 10) {
            balanceFlags.add(String.format("[NEGLECT] No abilities used on wave %d (%.1fs) — abilities too costly?", w.waveNumber, w.durationSeconds));
        }
    }

    /** Generate full run report as a string. */
    public String generateReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════════════════════════════════════╗\n");
        sb.append("║                    AI PLAYTEST RUN REPORT                           ║\n");
        sb.append("╠══════════════════════════════════════════════════════════════════════╣\n");
        sb.append(String.format("║ Total Play Time: %.1fs | Waves: %d | Kills: %d\n",
            totalPlayTime, waveHistory.size(), totalKills));
        sb.append(String.format("║ Total Damage Dealt: %d | Taken: %d | Abilities: %d\n",
            totalDamageDealt, totalDamageTaken, totalAbilitiesUsed));
        sb.append("╠══════════════════════════════════════════════════════════════════════╣\n");
        sb.append("║ WAVE-BY-WAVE:\n");
        for (WaveSnapshot w : waveHistory) {
            sb.append("║  ").append(w).append("\n");
        }
        if (!balanceFlags.isEmpty()) {
            sb.append("╠══════════════════════════════════════════════════════════════════════╣\n");
            sb.append("║ BALANCE FLAGS:\n");
            for (String flag : balanceFlags) {
                sb.append("║  ").append(flag).append("\n");
            }
        }
        sb.append("╚══════════════════════════════════════════════════════════════════════╝\n");
        return sb.toString();
    }

    // Getters for live overlay
    public List<WaveSnapshot> getWaveHistory() { return waveHistory; }
    public List<String> getBalanceFlags() { return balanceFlags; }
    public int getTotalKills() { return totalKills; }
    public double getTotalPlayTime() { return totalPlayTime; }
    public double getWaveElapsed() { return waveElapsed; }
    public int getWaveDamageDealt() { return waveDamageDealt; }
    public int getWaveDamageTaken() { return waveDamageTaken; }
    public int getWaveKills() { return waveKills; }
    public int getWaveAbilitiesUsed() { return waveAbilitiesUsed; }
    public double getCurrentDPS() { return waveElapsed > 0 ? waveDamageDealt / waveElapsed : 0; }
}
