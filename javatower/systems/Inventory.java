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
     * Attempts to add an item, auto-finding placement.
     */
    public boolean addItem(Item item) {
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

    // Getters
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
