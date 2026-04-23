package com.loki.lomines.data.reward;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

/**
 * Immutable reward configuration for mine blocks.
 * All collections are copied to ensure immutability.
 */
public final class Reward {
    private final List<Material> materials;
    private final double chance;
    private final List<ItemStack> items;
    private final List<String> commands;
    private final boolean preventDrops;
    
    public Reward(List<Material> materials, double chance, 
                  List<ItemStack> items, List<String> commands, 
                  boolean preventDrops) {
        this.materials = List.copyOf(materials);
        this.chance = chance;
        this.items = List.copyOf(items);
        this.commands = List.copyOf(commands);
        this.preventDrops = preventDrops;
    }
    
    public boolean matches(Material material) {
        return materials.contains(material);
    }
    
    public List<Material> getMaterials() {
        return materials;
    }
    
    public double getChance() {
        return chance;
    }
    
    public List<ItemStack> getItems() {
        return items;
    }
    
    public List<String> getCommands() {
        return commands;
    }
    
    public boolean isPreventDrops() {
        return preventDrops;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reward reward = (Reward) o;
        return Double.compare(reward.chance, chance) == 0 &&
                preventDrops == reward.preventDrops &&
                Objects.equals(materials, reward.materials) &&
                Objects.equals(items, reward.items) &&
                Objects.equals(commands, reward.commands);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(materials, chance, items, commands, preventDrops);
    }
}
