package javatower.util;

/**
 * Game difficulty levels that scale enemy stats, rewards, and hero starting power.
 */
public enum Difficulty {
    EASY("Easy", 0.7, 0.7, 1.0, 1.0, 1.3, "#4ecca3"),
    NORMAL("Normal", 1.0, 1.0, 1.0, 1.0, 1.0, "#eee"),
    HARD("Hard", 1.3, 1.2, 1.3, 1.3, 1.0, "#e94560"),
    NIGHTMARE("Nightmare", 1.8, 1.5, 1.8, 1.8, 1.0, "#8b5cf6");

    /** Display name shown in the selection UI. */
    public final String displayName;
    /** Multiplier applied to all enemy HP. */
    public final double enemyHpMul;
    /** Multiplier applied to all enemy attack damage. */
    public final double enemyAtkMul;
    /** Multiplier applied to gold earned from kills. */
    public final double goldMul;
    /** Multiplier applied to XP earned from kills. */
    public final double xpMul;
    /** Multiplier applied to hero starting HP and stats. */
    public final double heroStatMul;
    /** Hex colour for the UI label. */
    public final String colour;

    Difficulty(String displayName, double enemyHpMul, double enemyAtkMul,
              double goldMul, double xpMul, double heroStatMul, String colour) {
        this.displayName = displayName;
        this.enemyHpMul = enemyHpMul;
        this.enemyAtkMul = enemyAtkMul;
        this.goldMul = goldMul;
        this.xpMul = xpMul;
        this.heroStatMul = heroStatMul;
        this.colour = colour;
    }

    /** Currently selected difficulty for the active run. */
    private static Difficulty current = NORMAL;

    public static Difficulty getCurrent() { return current; }
    public static void setCurrent(Difficulty d) { current = d; }
}
