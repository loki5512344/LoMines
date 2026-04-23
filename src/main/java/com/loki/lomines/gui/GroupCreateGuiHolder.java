package com.loki.lomines.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

/**
 * Marks the group-create menu and ties it to a player.
 */
public final class GroupCreateGuiHolder implements InventoryHolder {
    
    private final UUID playerId;
    private Inventory inventory;
    
    public GroupCreateGuiHolder(UUID playerId) {
        this.playerId = playerId;
    }
    
    public UUID getPlayerId() {
        return playerId;
    }
    
    void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
    
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
