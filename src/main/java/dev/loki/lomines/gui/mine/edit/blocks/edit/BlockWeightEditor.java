package dev.loki.lomines.gui.mine.edit.blocks.edit;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.model.Mine;
import dev.loki.lomines.data.config.block.BlockConfig;
import dev.loki.lomines.data.config.block.BlockKey;
import dev.loki.lomines.gui.mine.edit.blocks.view.BlocksGui;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class BlockWeightEditor {

    private BlockWeightEditor() {
    }

    public static void adjustBlockWeight(LoMinesPlugin plugin, Player player, String mineName,
                                          BlockKey blockKey, boolean leftClick, boolean shiftClick,
                                          boolean rightClick) {
        Mine mine = plugin.getMines().find(mineName).orElse(null);
        if (mine == null) {
            return;
        }

        BlockConfig config = mine.getConfig().blocks();
        Map<BlockKey, Double> weights = new java.util.HashMap<>(config.weights());
        double currentWeight = weights.getOrDefault(blockKey, 0.0) * 100.0;

        double delta = 0;
        if (leftClick && shiftClick) {
            delta = 1.0;
        } else if (rightClick && shiftClick) {
            delta = -1.0;
        } else if (leftClick) {
            delta = 5.0;
        } else if (rightClick) {
            delta = -5.0;
        }

        double newWeight = Math.max(0, Math.min(100, currentWeight + delta));

        if (rightClick && shiftClick && newWeight <= 0.1) {
            weights.remove(blockKey);
            player.sendMessage(Component.text(
                    "\u00a7c\u0411\u043b\u043e\u043a \u00a7f" + blockKey.serialize() + " \u00a7c\u0443\u0434\u0430\u043b\u0435\u043d!"));
        } else {
            weights.put(blockKey, newWeight / 100.0);
            player.sendMessage(Component.text(
                    "\u00a7a\u0412\u0435\u0441 \u00a7f" + blockKey.serialize() + " \u00a7a\u0438\u0437\u043c\u0435\u043d\u0435\u043d: \u00a7f"
                            + String.format("%.1f%%", newWeight)));
        }

        BlocksGui.refresh(player, player.getOpenInventory().getTopInventory(), mineName, plugin);
    }

    public static BlockKey getBlockAtSlot(Inventory inv, int slot, LoMinesPlugin plugin, String mineName) {
        if (slot < 0 || slot >= 36) {
            return null;
        }

        Mine mine = plugin.getMines().find(mineName).orElse(null);
        if (mine == null) {
            return null;
        }

        BlockConfig config = mine.getConfig().blocks();
        List<Map.Entry<BlockKey, Double>> sortedEntries = new ArrayList<>(config.weights().entrySet());
        sortedEntries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        if (slot < sortedEntries.size()) {
            return sortedEntries.get(slot).getKey();
        }
        return null;
    }
}
