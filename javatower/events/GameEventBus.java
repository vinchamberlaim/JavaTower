package javatower.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Simple event bus for decoupled communication between game components.
 * Implements the Observer pattern for game events.
 * 
 * Example usage:
 *   // Subscribe to an event
 *   GameEventBus.subscribe(EnemyKilledEvent.class, event -> {
 *       System.out.println("Enemy killed: " + event.getEnemyType());
 *   });
 * 
 *   // Publish an event
 *   GameEventBus.publish(new EnemyKilledEvent(EnemyType.ZOMBIE, 100));
 */
public class GameEventBus {
    
    @SuppressWarnings("rawtypes")
    private static final Map<Class<?>, List<Consumer>> listeners = new HashMap<>();
    
    /**
     * Subscribes a listener to a specific event type.
     * 
     * @param <T> the event type
     * @param eventType the class of the event
     * @param listener the consumer that handles the event
     */
    @SuppressWarnings("unchecked")
    public static <T> void subscribe(Class<T> eventType, Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }
    
    /**
     * Unsubscribes a listener from a specific event type.
     * 
     * @param <T> the event type
     * @param eventType the class of the event
     * @param listener the consumer to remove
     */
    public static <T> void unsubscribe(Class<T> eventType, Consumer<T> listener) {
        List<Consumer> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.remove(listener);
        }
    }
    
    /**
     * Publishes an event to all registered listeners.
     * 
     * @param event the event to publish
     */
    @SuppressWarnings("unchecked")
    public static void publish(Object event) {
        Class<?> eventType = event.getClass();
        List<Consumer> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            for (Consumer listener : eventListeners) {
                try {
                    listener.accept(event);
                } catch (Exception e) {
                    System.err.println("Error handling event " + eventType.getSimpleName() + ": " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Clears all listeners for a specific event type.
     * 
     * @param eventType the class of the event
     */
    public static void clear(Class<?> eventType) {
        listeners.remove(eventType);
    }
    
    /**
     * Clears all listeners for all event types.
     */
    public static void clearAll() {
        listeners.clear();
    }
    
    // ========== Event Classes ==========
    
    /**
     * Event fired when an enemy is killed.
     */
    public static class EnemyKilledEvent {
        private final String enemyType;
        private final int experienceGained;
        private final int goldGained;
        private final int waveNumber;
        
        public EnemyKilledEvent(String enemyType, int experienceGained, int goldGained, int waveNumber) {
            this.enemyType = enemyType;
            this.experienceGained = experienceGained;
            this.goldGained = goldGained;
            this.waveNumber = waveNumber;
        }
        
        public String getEnemyType() { return enemyType; }
        public int getExperienceGained() { return experienceGained; }
        public int getGoldGained() { return goldGained; }
        public int getWaveNumber() { return waveNumber; }
    }
    
    /**
     * Event fired when a wave is completed.
     */
    public static class WaveCompleteEvent {
        private final int waveNumber;
        private final int enemiesKilled;
        private final boolean isBossWave;
        
        public WaveCompleteEvent(int waveNumber, int enemiesKilled, boolean isBossWave) {
            this.waveNumber = waveNumber;
            this.enemiesKilled = enemiesKilled;
            this.isBossWave = isBossWave;
        }
        
        public int getWaveNumber() { return waveNumber; }
        public int getEnemiesKilled() { return enemiesKilled; }
        public boolean isBossWave() { return isBossWave; }
    }
    
    /**
     * Event fired when the hero levels up.
     */
    public static class HeroLevelUpEvent {
        private final int newLevel;
        private final int skillPointsGained;
        
        public HeroLevelUpEvent(int newLevel, int skillPointsGained) {
            this.newLevel = newLevel;
            this.skillPointsGained = skillPointsGained;
        }
        
        public int getNewLevel() { return newLevel; }
        public int getSkillPointsGained() { return skillPointsGained; }
    }
    
    /**
     * Event fired when a tower is placed.
     */
    public static class TowerPlacedEvent {
        private final String towerType;
        private final int gridX;
        private final int gridY;
        private final int cost;
        
        public TowerPlacedEvent(String towerType, int gridX, int gridY, int cost) {
            this.towerType = towerType;
            this.gridX = gridX;
            this.gridY = gridY;
            this.cost = cost;
        }
        
        public String getTowerType() { return towerType; }
        public int getGridX() { return gridX; }
        public int getGridY() { return gridY; }
        public int getCost() { return cost; }
    }
    
    /**
     * Event fired when a tower is upgraded.
     */
    public static class TowerUpgradedEvent {
        private final String towerType;
        private final int newLevel;
        private final int cost;
        
        public TowerUpgradedEvent(String towerType, int newLevel, int cost) {
            this.towerType = towerType;
            this.newLevel = newLevel;
            this.cost = cost;
        }
        
        public String getTowerType() { return towerType; }
        public int getNewLevel() { return newLevel; }
        public int getCost() { return cost; }
    }
    
    /**
     * Event fired when an item is purchased from the shop.
     */
    public static class ItemPurchasedEvent {
        private final String itemName;
        private final int cost;
        
        public ItemPurchasedEvent(String itemName, int cost) {
            this.itemName = itemName;
            this.cost = cost;
        }
        
        public String getItemName() { return itemName; }
        public int getCost() { return cost; }
    }
    
    /**
     * Event fired when the game state changes.
     */
    public static class GameStateChangedEvent {
        private final javatower.util.GameState oldState;
        private final javatower.util.GameState newState;
        
        public GameStateChangedEvent(javatower.util.GameState oldState, javatower.util.GameState newState) {
            this.oldState = oldState;
            this.newState = newState;
        }
        
        public javatower.util.GameState getOldState() { return oldState; }
        public javatower.util.GameState getNewState() { return newState; }
    }
}
