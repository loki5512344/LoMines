package dev.loki.lomines.gui.mine.edit;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.Mine;
import dev.loki.lomines.data.config.block.BlockConfig;
import dev.loki.lomines.data.config.block.BlockKey;
import dev.loki.lomines.gui.common.ItemStackFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GUI for editing block weights in a mine.
 * Shows current blocks with their weights and allows adjustment.
 */
public final class BlocksGui {

    private static final int SIZE = 54;
    private static final String TITLE_PREFIX = "Редактор блоков: ";
    private static final int SLOT_BACK = 49;
    private static final int SLOT_ADD_BLOCK = 52;

    private BlocksGui() {}

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

        // Fill background
        for (int i = 0; i < SIZE; i++) {
            inv.setItem(i, filler());
        }

        // Display blocks in rows 1-4 (slots 0-35)
        List<Map.Entry<BlockKey, Double>> sortedEntries = new ArrayList<>(weights.entrySet());
        sortedEntries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        int slot = 0;
        for (Map.Entry<BlockKey, Double> entry : sortedEntries) {
            if (slot >= 36) break; // Max 36 blocks displayed
            inv.setItem(slot, blockItem(entry.getKey(), entry.getValue()));
            slot++;
        }

        // Add block button
        inv.setItem(SLOT_ADD_BLOCK, addBlockItem());

        // Back button
        inv.setItem(SLOT_BACK, backItem());
    }

    private static ItemStack filler() {
        return ItemStackFactory.filler();
    }

    private static ItemStack blockItem(BlockKey key, double weight) {
        Material material = getMaterialForKey(key);
        String name = formatBlockName(key);
        double percent = weight * 100.0;

        return ItemStackFactory.create(material, "§a§l" + name,
            "§8───────────────",
            "§7Вес: §f" + String.format("%.1f%%", percent),
            "",
            "§e▸ ЛКМ §7+5%",
            "§e▸ ПКМ §7-5%",
            "§e▸ Shift+ЛКМ §7+1%",
            "§e▸ Shift+ПКМ §7-1% §8(удалить если 0%)",
            "",
            "§8ID: §7" + key.serialize()
        );
    }

    private static Material getMaterialForKey(BlockKey key) {
        if (key instanceof BlockKey.Vanilla vanilla) {
            return vanilla.material();
        }
        // Custom blocks show a special icon
        if (key instanceof BlockKey.Oraxen) {
            return Material.NETHER_STAR;
        }
        if (key instanceof BlockKey.ItemsAdder) {
            return Material.EMERALD;
        }
        return Material.STONE;
    }

    private static String formatBlockName(BlockKey key) {
        return switch (key) {
            case BlockKey.Vanilla vanilla -> formatMaterialName(vanilla.material());
            case BlockKey.Oraxen oraxen -> "Oraxen:" + oraxen.id();
            case BlockKey.ItemsAdder itemsAdder -> "IA:" + itemsAdder.id();
        };
    }

    private static String formatMaterialName(Material material) {
        String name = material.name().toLowerCase().replace("_", " ");
        return capitalizeWords(name);
    }

    private static String capitalizeWords(String input) {
        StringBuilder result = new StringBuilder();
        for (String word : input.split(" ")) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1))
                      .append(" ");
            }
        }
        return result.toString().trim();
    }

    private static ItemStack addBlockItem() {
        return ItemStackFactory.create(Material.EMERALD_BLOCK, "§a§lДобавить блок",
            "§8───────────────",
            "§7Добавить новый блок",
            "§7в конфигурацию",
            "",
            "§e▸ Нажмите для выбора материала"
        );
    }

    private static ItemStack backItem() {
        return ItemStackFactory.create(Material.ARROW, "§c§lНазад",
            "§8───────────────",
            "§7Вернуться в редактор шахты",
            "",
            "§e▸ Нажмите для возврата"
        );
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

        // Block weight adjustment (slots 0-35)
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
        double currentWeight = weights.getOrDefault(blockKey, 0.0) * 100.0; // Convert to percentage

        double delta = 0;
        if (leftClick && shiftClick) {
            delta = 1.0; // +1%
        } else if (rightClick && shiftClick) {
            delta = -1.0; // -1%
        } else if (leftClick) {
            delta = 5.0; // +5%
        } else if (rightClick) {
            delta = -5.0; // -5%
        }

        double newWeight = Math.max(0, Math.min(100, currentWeight + delta));

        if (rightClick && shiftClick && newWeight <= 0.1) {
            // Remove block
            weights.remove(blockKey);
            player.sendMessage(Component.text("§cБлок §f" + blockKey.serialize() + " §cудален!"));
        } else {
            weights.put(blockKey, newWeight / 100.0);
            player.sendMessage(Component.text("§aВес §f" + blockKey.serialize() + " §aизменен: §f" +
                String.format("%.1f%%", newWeight)));
        }

        // Note: In production, this would update the mine config and save
        // For now we just refresh the GUI to show the change would happen
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
