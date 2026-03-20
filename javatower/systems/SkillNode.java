package javatower.systems;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * Represents a node in the skill tree.
 */
public class SkillNode {
    private String id;
    private String name;
    private String description;
    private String branch;
    private int cost;
    private List<String> prerequisites;
    private boolean unlocked;
    private Map<String, Integer> bonuses;
    private Runnable specialEffect;

    public SkillNode(String id, String name, String description, String branch, int cost, List<String> prerequisites, Map<String, Integer> bonuses, Runnable specialEffect) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.branch = branch;
        this.cost = cost;
        this.prerequisites = prerequisites != null ? prerequisites : new ArrayList<>();
        this.unlocked = false;
        this.bonuses = bonuses;
        this.specialEffect = specialEffect;
    }

    // Getters and setters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getBranch() { return branch; }
    public int getCost() { return cost; }
    public List<String> getPrerequisites() { return prerequisites; }
    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
    public Map<String, Integer> getBonuses() { return bonuses; }
    public Runnable getSpecialEffect() { return specialEffect; }
}
