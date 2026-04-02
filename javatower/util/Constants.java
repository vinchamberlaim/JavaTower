package javatower.util;

/**
 * Centralised game-wide constants for JavaTower.
 * <p>
 * All numeric tuning values (screen size, entity radii, movement speeds,
 * combat ranges, economy settings) live here so that balance adjustments
 * only require changes in one place.
 * </p>
 *
 * @author Vincent Chamberlain (2424309)
 * @author Nicolas Alfaro (2301126)
 * @author Emmanuel Adewumi (2507044)
 * @version 2.0 — CIS096-1 Assessment 2
 */
public class Constants {

    // ==================== Screen / Canvas ====================

    /** Visible screen width in pixels (JavaFX Canvas). */
    public static final int SCREEN_WIDTH = 960;
    /** Visible screen height in pixels (JavaFX Canvas). */
    public static final int SCREEN_HEIGHT = 640;

    // ==================== Fine Movement Grid ====================

    /** Fine-grain grid width (10× finer than the old 12×8 coarse grid). */
    public static final int GRID_WIDTH = 120;
    /** Fine-grain grid height. */
    public static final int GRID_HEIGHT = 80;
    /** Fine grid cell size in pixels. */
    public static final int CELL_SIZE = 8;

    // ==================== Tower Placement Grid ====================

    /** Number of columns in the coarse tower-placement grid. */
    public static final int TILE_COLS = 15;
    /** Number of rows in the coarse tower-placement grid. */
    public static final int TILE_ROWS = 10;
    /** Size of one coarse grid tile in pixels. */
    public static final int TILE_SIZE = 64;

    // ==================== Entity Sizes (pixel radius) ====================

    /** Hero collision / render radius. */
    public static final double HERO_RADIUS = 20;
    /** Small enemy radius (Zombie, Skeleton, Ghoul). */
    public static final double ENEMY_RADIUS_SMALL = 12;
    /** Medium enemy radius (Wight, Wraith, Revenant). */
    public static final double ENEMY_RADIUS_MEDIUM = 16;
    /** Large enemy radius (Death Knight, Lich). */
    public static final double ENEMY_RADIUS_LARGE = 22;
    /** Boss-tier enemy radius (Bone Colossus, Necromancer King). */
    public static final double ENEMY_RADIUS_BOSS = 30;
    /** Maximum radius an enemy can reach via bone-pile consumption. */
    public static final double ENEMY_RADIUS_MAX = 60;

    // ==================== World Dimensions ====================

    /** World width in pixels — 1.5× screen width for camera scrolling. */
    public static final int WORLD_WIDTH = 1440;
    /** World height in pixels — 1.5× screen height for camera scrolling. */
    public static final int WORLD_HEIGHT = 960;

    // ==================== Movement Speeds (pixels / second) ====================

    /** Hero base movement speed. */
    public static final double HERO_SPEED = 120;
    /** Slow enemy speed (Zombie, Death Knight, Lich, Bone Colossus). */
    public static final double ENEMY_SPEED_SLOW = 30;
    /** Normal enemy speed (Skeleton, Wight, Revenant, Necromancer King). */
    public static final double ENEMY_SPEED_NORMAL = 50;
    /** Fast enemy speed (Ghoul, Wraith). */
    public static final double ENEMY_SPEED_FAST = 80;

    // ==================== Combat ====================

    /** Default melee attack range in pixels. */
    public static final double MELEE_RANGE = 40;
    /** Conversion factor: 1 range unit = 64 pixels (for tower targeting). */
    public static final double TOWER_RANGE_UNIT = 64;

    // ==================== Economy / Progression ====================

    /** Gold the hero starts with when beginning a new run. */
    public static final int STARTING_GOLD = 50;
    /** Starting inventory grid width (expands every 3 hero levels). */
    public static final int STARTING_INVENTORY_WIDTH = 3;
    /** Starting inventory grid height. */
    public static final int STARTING_INVENTORY_HEIGHT = 3;
    /** Total number of waves before the player wins. */
    public static final int MAX_WAVES = 30;
    /** Wave numbers that spawn a mini-boss alongside regular enemies. */
    public static final int[] MINI_BOSS_WAVES = {5, 10, 15, 20, 25};

    // ==================== Wave Transition ====================

    /** Delay in seconds between waves (used by wave-transition UI). */
    public static final double WAVE_DELAY = 3.0;
}
