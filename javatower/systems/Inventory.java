package javatower.systems;

import javatower.entities.Item;
import java.util.ArrayList;
import java.util.List;

/**
 * Tetris-style grid inventory management system.
 */
public class Inventory {
    private int width;
    private int height;
    private boolean[][] occupied;
    private Item[][] itemGrid;
    private List<Item> items;

    public Inventory(int width, int height) {
        this.width = width;
        this.height = height;
        this.occupied = new boolean[width][height];
        this.itemGrid = new Item[width][height];
        this.items = new ArrayList<>();
    }

    /**
     * Checks if an item can be placed at the given position.
     */
    public boolean canPlaceItem(Item item, int x, int y) {
        if (x < 0 || y < 0 || x + item.getWidth() > width || y + item.getHeight() > height) return false;
        for (int i = 0; i < item.getWidth(); i++) {
            for (int j = 0; j < item.getHeight(); j++) {
                if (occupied[x + i][y + j]) return false;
            }
        }
        return true;
    }

    /**
     * Finds an existing item in inventory that can stack with the given item
     * (same name, rarity, and slot).
     */
    public Item findStackable(Item item) {
        for (Item existing : items) {
            if (existing.getName().equals(item.getName())
                    && existing.getRarity() == item.getRarity()
                    && existing.getSlot() == item.getSlot()) {
                return existing;
            }
        }
        return null;
    }

    /**
     * Attempts to add an item, stacking with an existing identical item if possible,
     * otherwise auto-finding placement on the grid.
     */
    public boolean addItem(Item item) {
        // Try to stack with an existing item first
        Item existing = findStackable(item);
        if (existing != null) {
            existing.addStack(item.getStackCount());
            return true;
        }
        // Otherwise place on grid normally
        for (int x = 0; x <= width - item.getWidth(); x++) {
            for (int y = 0; y <= height - item.getHeight(); y++) {
                if (canPlaceItem(item, x, y)) {
                    return addItemAt(item, x, y);
                }
            }
        }
        return false;
    }

    /**
     * Adds an item at a specific position.
     */
    public boolean addItemAt(Item item, int x, int y) {
        if (!canPlaceItem(item, x, y)) return false;
        for (int i = 0; i < item.getWidth(); i++) {
            for (int j = 0; j < item.getHeight(); j++) {
                occupied[x + i][y + j] = true;
                itemGrid[x + i][y + j] = item;
            }
        }
        items.add(item);
        return true;
    }

    /**
     * Removes the item at the given position.
     */
    public Item removeItem(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) return null;
        Item item = itemGrid[x][y];
        if (item == null) return null;
        for (int i = 0; i < item.getWidth(); i++) {
            for (int j = 0; j < item.getHeight(); j++) {
                if (x + i < width && y + j < height && itemGrid[x + i][y + j] == item) {
                    occupied[x + i][y + j] = false;
                    itemGrid[x + i][y + j] = null;
                }
            }
        }
        items.remove(item);
        return item;
    }

    /**
     * Expands the inventory grid.
     */
    public void expand(int newWidth, int newHeight) {
        if (newWidth <= width && newHeight <= height) return;
        boolean[][] newOccupied = new boolean[newWidth][newHeight];
        Item[][] newItemGrid = new Item[newWidth][newHeight];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                newOccupied[i][j] = occupied[i][j];
                newItemGrid[i][j] = itemGrid[i][j];
            }
        }
        width = newWidth;
        height = newHeight;
        occupied = newOccupied;
        itemGrid = newItemGrid;
    }

    public int getUsedSpace() {
        int used = 0;
        for (boolean[] row : occupied) {
            for (boolean cell : row) if (cell) used++;
        }
        return used;
    }

    public int getTotalSpace() {
        return width * height;
    }

    public List<Item> getAllItems() {
        return new ArrayList<>(items);
    }

    /**
     * Finds the top-left grid position of the given item.
     * @return int[]{x, y} or null if not found
     */
    public int[] findItemPosition(Item item) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (itemGrid[x][y] == item) return new int[]{x, y};
            }
        }
        return null;
    }

    /**
     * Removes a specific item from the inventory.
     * @return true if removed
     */
    public boolean removeSpecificItem(Item item) {
        int[] pos = findItemPosition(item);
        if (pos == null) return false;
        removeItem(pos[0], pos[1]);
        return true;
    }

    /**
     * Removes one copy from a stack. If the stack count drops to zero,
     * the item is removed from the grid entirely.
     */
    public boolean removeOne(Item item) {
        if (item.getStackCount() > 1) {
            item.addStack(-1);
            return true;
        } else {
            return removeSpecificItem(item);
        }
    }

    // Getters
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
