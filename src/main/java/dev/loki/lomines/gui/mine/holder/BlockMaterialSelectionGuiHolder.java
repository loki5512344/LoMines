package dev.loki.lomines.gui.mine.holder;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

/**
 * Inventory holder for BlockMaterialSelectionGui.
 */
public final class BlockMaterialSelectionGuiHolder implements InventoryHolder {

    private final UUID playerId;
    private final String mineName;
    private final int page;
    private Inventory inventory;

    public BlockMaterialSelectionGuiHolder(UUID playerId, String mineName, int page) {
        this.playerId = playerId;
        this.mineName = mineName;
        this.page = page;
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

    public int getPage() {
        return page;
    }
}
