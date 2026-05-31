package dev.loki.lomines.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

/**
 * Inventory holder for ConfirmDeleteGui.
 */
public class ConfirmDeleteGuiHolder implements InventoryHolder {

    private final UUID playerId;
    private final String mineName;
    private Inventory inventory;

    public ConfirmDeleteGuiHolder(UUID playerId, String mineName) {
        this.playerId = playerId;
        this.mineName = mineName;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getMineName() {
        return mineName;
    }
}
