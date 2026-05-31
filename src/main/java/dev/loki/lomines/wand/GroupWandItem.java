package dev.loki.lomines.wand;

import dev.loki.lomines.LoMinesPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/**
 * Blaze rod marked with PDC so only this item drives group wand behaviour.
 */
public final class GroupWandItem {

    private static final String KEY = "group_wand";
    /**
     * Same namespace as {@code plugin.yml} name, avoids IDE needing full {@link org.bukkit.plugin.Plugin} classpath.
     */
    private static final String NAMESPACE = "lomines";

    private GroupWandItem() {
    }

    public static NamespacedKey key(LoMinesPlugin plugin) {
        return new NamespacedKey(NAMESPACE, KEY);
    }

    public static ItemStack create(LoMinesPlugin plugin) {
        ItemStack item = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("LoMines · группа", NamedTextColor.GOLD));
            meta.lore(List.of(
                    Component.text("ЛКМ блок — угол 1 (слот ", NamedTextColor.GRAY)
                            .append(Component.text("1–9", NamedTextColor.YELLOW))
                            .append(Component.text(" в меню)", NamedTextColor.GRAY)),
                    Component.text("ПКМ блок — угол 2", NamedTextColor.GRAY),
                    Component.text("Shift+ПКМ — меню: 9 шахт, одни настройки", NamedTextColor.GRAY),
                    Component.text("/lm group <префикс> — имена префикс_1…префикс_9", NamedTextColor.DARK_GRAY)
            ));
            meta.getPersistentDataContainer().set(key(plugin), PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    public static boolean isWand(LoMinesPlugin plugin, ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR) {
            return false;
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return false;
        }
        return meta.getPersistentDataContainer().has(key(plugin), PersistentDataType.BYTE);
    }
}
