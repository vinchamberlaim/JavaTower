package javatower.util;

/**
 * Game-wide constants for JavaTower.
 */
public class Constants {
    // Screen / canvas dimensions (pixels)
    public static final int SCREEN_WIDTH = 960;
    public static final int SCREEN_HEIGHT = 640;

    // Fine grid (10x finer than old 12x8)
    public static final int GRID_WIDTH = 120;
    public static final int GRID_HEIGHT = 80;
    public static final int CELL_SIZE = 8;  // fine grid cell

    // Tower placement grid (old coarse grid)
    public static final int TILE_COLS = 15;
    public static final int TILE_ROWS = 10;
    public static final int TILE_SIZE = 64;

    // Entity sizes (pixel radius)
    public static final double HERO_RADIUS = 20;
    public static final double ENEMY_RADIUS_SMALL = 12;
    public static final double ENEMY_RADIUS_MEDIUM = 16;
    public static final double ENEMY_RADIUS_LARGE = 22;
    public static final double ENEMY_RADIUS_BOSS = 30;

    // Movement speeds (pixels per second)
    public static final double HERO_SPEED = 120;
    public static final double ENEMY_SPEED_SLOW = 30;
    public static final double ENEMY_SPEED_NORMAL = 50;
    public static final double ENEMY_SPEED_FAST = 80;

    // Combat
    public static final double MELEE_RANGE = 40;    // attack distance (pixels)
    public static final double TOWER_RANGE_UNIT = 64; // 1 range = 64 pixels

    // Economy / progression
    public static final int STARTING_GOLD = 50;
    public static final int STARTING_INVENTORY_WIDTH = 3;
    public static final int STARTING_INVENTORY_HEIGHT = 3;
    public static final int MAX_WAVES = 30;
    public static final int[] MINI_BOSS_WAVES = {5, 10, 15, 20, 25};

    // Wave transition delay (seconds)
    public static final double WAVE_DELAY = 3.0;
}
