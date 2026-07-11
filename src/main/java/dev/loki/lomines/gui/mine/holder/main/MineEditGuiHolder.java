package dev.loki.lomines.gui.mine.holder.main;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

/**
 * Inventory holder for MineEditGui.
 */
public class MineEditGuiHolder implements InventoryHolder {

    private final UUID playerId;
    private final String mineName;
    private Inventory inventory;

    public MineEditGuiHolder(UUID playerId, String mineName) {
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
