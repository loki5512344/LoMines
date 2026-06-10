package dev.loki.lomines.gui.mine.edit.blocks;

import dev.loki.lomines.data.config.block.BlockKey;
import dev.loki.lomines.gui.common.ItemStackFactory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Item factory for BlocksGui.
 */
final class BlocksGuiItems {

    private BlocksGuiItems() {
    }

    static ItemStack blockItem(BlockKey key, double weight) {
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

    static ItemStack addBlockItem() {
        return ItemStackFactory.create(Material.EMERALD_BLOCK, "§a§lДобавить блок",
                "§8───────────────",
                "§7Добавить новый блок",
                "§7в конфигурацию",
                "",
                "§e▸ Нажмите для выбора материала"
        );
    }

    static ItemStack backItem() {
        return ItemStackFactory.create(Material.ARROW, "§c§lНазад",
                "§8───────────────",
                "§7Вернуться в редактор шахты",
                "",
                "§e▸ Нажмите для возврата"
        );
    }

    static Material getMaterialForKey(BlockKey key) {
        if (key instanceof BlockKey.Vanilla(Material material)) {
            return material;
        }
        if (key instanceof BlockKey.Oraxen) {
            return Material.NETHER_STAR;
        }
        if (key instanceof BlockKey.ItemsAdder) {
            return Material.EMERALD;
        }
        return Material.STONE;
    }

    static String formatBlockName(BlockKey key) {
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
}
