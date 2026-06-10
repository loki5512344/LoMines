package dev.loki.lomines.gui.mine.edit.blocks;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.Mine;
import dev.loki.lomines.data.config.block.BlockConfig;
import dev.loki.lomines.data.config.block.BlockKey;
import dev.loki.lomines.gui.common.ItemStackFactory;
import dev.loki.lomines.gui.mine.holder.BlocksGuiHolder;
import dev.loki.lomines.gui.mine.main.MineEditGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GUI for editing block weights in a mine.
 */
public final class BlocksGui {

    private static final int SIZE = 54;
    private static final String TITLE_PREFIX = "Редактор блоков: ";
    private static final int SLOT_BACK = 49;
    private static final int SLOT_ADD_BLOCK = 52;

    private BlocksGui() {
    }

    public static void open(LoMinesPlugin plugin, Player player, String mineName) {
        Mine mine = plugin.getMines().find(mineName).orElse(null);
        if (mine == null) {
            player.sendMessage(Component.text("Шахта не найдена: " + mineName, NamedTextColor.RED));
            return;
        }
        BlocksGuiHolder holder = new BlocksGuiHolder(player.getUniqueId(), mineName);
        Inventory inv = Bukkit.createInventory(holder, SIZE,
                Component.text(TITLE_PREFIX + mineName, NamedTextColor.DARK_GREEN));
        holder.setInventory(inv);
        fill(mine, inv);
        player.openInventory(inv);
    }

    public static void refresh(Player player, Inventory inv, String mineName, LoMinesPlugin plugin) {
        Mine mine = plugin.getMines().find(mineName).orElse(null);
        if (mine == null) {
            player.closeInventory();
            return;
        }
        fill(mine, inv);
    }

    private static void fill(Mine mine, Inventory inv) {
        BlockConfig config = mine.getConfig().blocks();
        Map<BlockKey, Double> weights = config.weights();

        for (int i = 0; i < SIZE; i++) {
            inv.setItem(i, ItemStackFactory.filler());
        }

        List<Map.Entry<BlockKey, Double>> sortedEntries = new ArrayList<>(weights.entrySet());
        sortedEntries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        int slot = 0;
        for (Map.Entry<BlockKey, Double> entry : sortedEntries) {
            if (slot >= 36) break;
            inv.setItem(slot, BlocksGuiItems.blockItem(entry.getKey(), entry.getValue()));
            slot++;
        }

        inv.setItem(SLOT_ADD_BLOCK, BlocksGuiItems.addBlockItem());
        inv.setItem(SLOT_BACK, BlocksGuiItems.backItem());
    }

    public static boolean handleClick(LoMinesPlugin plugin, Player player, int rawSlot,
                                      String mineName, boolean leftClick, boolean shiftClick,
                                      boolean rightClick, BlockKey clickedBlock) {
        if (rawSlot < 0 || rawSlot >= SIZE) return false;

        if (rawSlot == SLOT_BACK) {
            MineEditGui.open(plugin, player, mineName);
            return true;
        }

        if (rawSlot == SLOT_ADD_BLOCK) {
            BlockMaterialSelectionGui.open(plugin, player, mineName);
            return true;
        }

        if (rawSlot < 36 && clickedBlock != null) {
            adjustBlockWeight(plugin, player, mineName, clickedBlock, leftClick, shiftClick, rightClick);
            return true;
        }

        return true;
    }

    private static void adjustBlockWeight(LoMinesPlugin plugin, Player player, String mineName,
                                          BlockKey blockKey, boolean leftClick, boolean shiftClick,
                                          boolean rightClick) {
        Mine mine = plugin.getMines().find(mineName).orElse(null);
        if (mine == null) return;

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
            player.sendMessage(Component.text("§cБлок §f" + blockKey.serialize() + " §cудален!"));
        } else {
            weights.put(blockKey, newWeight / 100.0);
            player.sendMessage(Component.text("§aВес §f" + blockKey.serialize() + " §aизменен: §f" +
                    String.format("%.1f%%", newWeight)));
        }

        refresh(player, player.getOpenInventory().getTopInventory(), mineName, plugin);
    }

    public static BlockKey getBlockAtSlot(Inventory inv, int slot, LoMinesPlugin plugin, String mineName) {
        if (slot < 0 || slot >= 36) return null;

        Mine mine = plugin.getMines().find(mineName).orElse(null);
        if (mine == null) return null;

        BlockConfig config = mine.getConfig().blocks();
        List<Map.Entry<BlockKey, Double>> sortedEntries = new ArrayList<>(config.weights().entrySet());
        sortedEntries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        if (slot < sortedEntries.size()) {
            return sortedEntries.get(slot).getKey();
        }
        return null;
    }
}
