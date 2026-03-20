package javatower.entities;

/**
 * Represents a pile of bones left behind when an undead enemy dies.
 * Bigger enemies leave bigger piles. Summoner bosses can consume piles
 * to raise new minions.
 */
public class BonePile {
    private double x, y;
    private double size;       // visual radius of the pile
    private int boneCount;     // how many "bones" — determines summon value
    private double age;        // seconds since creation (for fade-in, glow, etc.)

    // Pre-computed random scatter offsets for individual bone sprites (up to 8)
    private final double[] boneOffsetsX;
    private final double[] boneOffsetsY;
    private final double[] boneAngles;

    public BonePile(double x, double y, double enemyRadius, int tier) {
        this.x = x;
        this.y = y;
        this.age = 0;

        // Scale pile size and bone count based on enemy tier/radius
        this.size = Math.max(8, enemyRadius * 0.8);
        this.boneCount = Math.max(1, tier);

        // Generate random scatter for each bone fragment
        int fragments = Math.min(boneCount + 2, 8);
        boneOffsetsX = new double[fragments];
        boneOffsetsY = new double[fragments];
        boneAngles = new double[fragments];
        for (int i = 0; i < fragments; i++) {
            boneOffsetsX[i] = (Math.random() - 0.5) * size * 2;
            boneOffsetsY[i] = (Math.random() - 0.5) * size * 2;
            boneAngles[i] = Math.random() * Math.PI * 2;
        }
    }

    /** Advance age timer. */
    public void update(double dt) {
        age += dt;
    }

    /** Consume bones from this pile. Returns the number actually consumed. */
    public int consume(int amount) {
        int taken = Math.min(amount, boneCount);
        boneCount -= taken;
        size = Math.max(4, size * ((double) boneCount / (boneCount + taken)));
        return taken;
    }

    public boolean isEmpty() { return boneCount <= 0; }

    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public double getSize() { return size; }
    public int getBoneCount() { return boneCount; }
    public double getAge() { return age; }
    public double[] getBoneOffsetsX() { return boneOffsetsX; }
    public double[] getBoneOffsetsY() { return boneOffsetsY; }
    public double[] getBoneAngles() { return boneAngles; }
}
