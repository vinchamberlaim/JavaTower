package javatower.systems;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import javatower.entities.Hero;

/**
 * Branching skill-tree progression system.
 * <p>
 * Each tree (Combat, Magic, Utility) contains a directed acyclic graph
 * of {@link SkillNode}s. A node can only be unlocked if all its
 * prerequisite nodes are already unlocked and the hero has enough
 * {@link Hero#getSkillPoints() skill points}.
 * </p>
 * <p>
 * When a node is unlocked its stat bonuses (ATK, DEF, HP, crit, etc.)
 * are applied to the hero via {@link #applyBonuses(Hero)}.
 * The entire tree can be reset with {@link #respec()}, which refunds
 * all spent points.
 * </p>
 *
 * @author Vincent Chamberlain (2424309)
 * @see SkillNode
 * @see Hero#initSkillTrees()
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

    /**
     * Resets all nodes in this tree and returns refunded skill points.
     * @return Number of skill points refunded
     */
    public int respec() {
        int refunded = 0;
        for (SkillNode node : nodes) {
            if (node.isUnlocked()) {
                refunded += node.getCost();
                node.setUnlocked(false);
            }
        }
        return refunded;
    }
    
    /**
     * Gets total spent skill points in this tree.
     */
    public int getSpentPoints() {
        int spent = 0;
        for (SkillNode node : nodes) {
            if (node.isUnlocked()) {
                spent += node.getCost();
            }
        }
        return spent;
    }

    // Getters and setters
    public String getName() { return name; }
    public List<SkillNode> getNodes() { return nodes; }
    public Map<String, List<SkillNode>> getBranches() { return branches; }
    public void addNode(SkillNode node) { nodes.add(node); }
    public void addBranch(String branch, List<SkillNode> branchNodes) { branches.put(branch, branchNodes); }
}
