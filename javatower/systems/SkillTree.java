package javatower.systems;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import javatower.entities.Hero;

/**
 * Branching skill progression system for the hero.
 */
public class SkillTree {
    private String name;
    private List<SkillNode> nodes;
    private Map<String, List<SkillNode>> branches;

    public SkillTree(String name) {
        this.name = name;
        this.nodes = new ArrayList<>();
        this.branches = new HashMap<>();
    }

    /**
     * Unlocks a skill node if prerequisites and skill points are met.
     */
    public boolean unlockNode(String nodeId, int skillPoints) {
        for (SkillNode node : nodes) {
            if (node.getId().equals(nodeId) && !node.isUnlocked() && skillPoints >= node.getCost()) {
                // Check prerequisites
                boolean prereqsMet = true;
                for (String pre : node.getPrerequisites()) {
                    if (!isNodeUnlocked(pre)) {
                        prereqsMet = false;
                        break;
                    }
                }
                if (prereqsMet) {
                    node.setUnlocked(true);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a node is unlocked.
     */
    public boolean isNodeUnlocked(String nodeId) {
        for (SkillNode node : nodes) {
            if (node.getId().equals(nodeId)) return node.isUnlocked();
        }
        return false;
    }

    /**
     * Returns nodes whose prerequisites are met and are not yet unlocked.
     */
    public List<SkillNode> getAvailableNodes() {
        List<SkillNode> available = new ArrayList<>();
        for (SkillNode node : nodes) {
            if (!node.isUnlocked()) {
                boolean prereqsMet = true;
                for (String pre : node.getPrerequisites()) {
                    if (!isNodeUnlocked(pre)) {
                        prereqsMet = false;
                        break;
                    }
                }
                if (prereqsMet) available.add(node);
            }
        }
        return available;
    }

    /**
     * Applies all unlocked node bonuses to the hero.
     */
    public void applyBonuses(Hero hero) {
        for (SkillNode node : nodes) {
            if (node.isUnlocked()) {
                for (Map.Entry<String, Integer> entry : node.getBonuses().entrySet()) {
                    String stat = entry.getKey();
                    int value = entry.getValue();
                    switch (stat) {
                        case "maxHealth":
                            hero.setMaxHealth(hero.getMaxHealth() + value);
                            hero.setCurrentHealth(Math.min(hero.getCurrentHealth() + value, hero.getMaxHealth()));
                            break;
                        case "attack":
                            hero.setAttack(hero.getAttack() + value);
                            break;
                        case "defence":
                            hero.setDefence(hero.getDefence() + value);
                            break;
                        case "critChance":
                            hero.setCritChance(hero.getCritChance() + value);
                            break;
                        case "maxMana":
                            hero.setMaxMana(hero.getMaxMana() + value);
                            break;
                        case "speed":
                            hero.setMoveSpeed(hero.getMoveSpeed() + value);
                            break;
                        default: break;
                    }
                }
                if (node.getSpecialEffect() != null) {
                    node.getSpecialEffect().run();
                }
            }
        }
    }

    // Getters and setters
    public String getName() { return name; }
    public List<SkillNode> getNodes() { return nodes; }
    public Map<String, List<SkillNode>> getBranches() { return branches; }
    public void addNode(SkillNode node) { nodes.add(node); }
    public void addBranch(String branch, List<SkillNode> branchNodes) { branches.put(branch, branchNodes); }
}
